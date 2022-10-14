// Copyright 2018, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionplugin.request;

import java.io.*;
import java.lang.Integer;
import java.lang.Throwable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.HashMap;

import com.marimba.apps.subscriptionplugin.intf.request.ISecurityServiceRequest;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceConstants;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceSettings;
import com.marimba.apps.subscriptionplugin.intf.ISecurityComplianceResult;
import com.marimba.apps.subscriptionplugin.util.SecurityComplianceResult;
import com.marimba.apps.subscriptionplugin.util.SecurityComplianceResultNode;
import com.marimba.apps.subscriptionplugin.LogConstants;
import com.marimba.apps.subscriptionplugin.IPluginDebug;
import com.marimba.apps.subscriptionplugin.SecurityComplianceBean;
import com.marimba.apps.subscriptionplugin.SecurityScanReport;
import com.marimba.apps.subscriptionplugin.impl.db.DbDataStorage;

import com.marimba.castanet.tools.PluginConnection;
import com.marimba.castanet.checksum.*;
import com.marimba.intf.castanet.IChecksum;
import com.marimba.intf.plugin.*;
import com.marimba.io.*;
import com.marimba.tools.util.*;

import com.marimba.apps.subscriptionplugin.util.json.*;

/**
 * Represents an incoming vInspector "Send Compliance Details" request.
 *
 */
public class SendComplianceDetailsRequest implements ISecurityServiceRequest, LogConstants, IPluginDebug {
    final static int BUFFER_SIZE = 4 * 1024;
    public static int HTTP_OK = 200;
    String pluginDataDir = null;
    String clientSecurityComplianceResultStr = null;
    String currentTimeMilli = null;

    //request parameters
    long size = 0;
    IChecksum cs;
    ISecurityServiceSettings securityServiceSettings;
    String target = null;

	public void handleRequest(FastInputStream in, IHttpRequest request, ISecurityServiceSettings _securityServiceSettings) throws IOException {
        this.securityServiceSettings = _securityServiceSettings;

        currentTimeMilli = "" + System.currentTimeMillis();
        debug(INFO, "handleRequest(), currentTimeMilli - " + currentTimeMilli);

        pluginDataDir = securityServiceSettings.getDataDir();
        debug(INFO, "handleRequest(), pluginDataDir - " + pluginDataDir);

        clientSecurityComplianceResultStr = PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT + currentTimeMilli;
        debug(INFO, "handleRequest(), clientSecurityComplianceResultStr - " + clientSecurityComplianceResultStr);

        handleComplientResultData(in);

        FastOutputStream out = new FastOutputStream(1024);
        boolean insertScanDetailsResult = false;
        boolean forwardScanDetailsResult = false;

        String machineName = "";
        int totalScanners = 0;

        String subscriptionPluginStatus = securityServiceSettings.getPluginContext().getPluginContext().getChannelProperties().getProperty("subscriptionplugin.pluginStatus");
        boolean pluginEnabled = "enable".equals(subscriptionPluginStatus);
        debug(INFO, "handleRequest(), pluginEnabled - " + pluginEnabled);
        if (!pluginEnabled) {
            securityServiceSettings.getPluginContext().log(LOG_PLUGIN_DISABLED, LOG_INFO, "Security Plugin Disabled...");
        }

        ISecurityComplianceResult securityComplianceResult = new SecurityComplianceResult(new File(pluginDataDir + File.separator + clientSecurityComplianceResultStr));

        Vector machineDetailsList = (Vector)securityComplianceResult.findNodesByPrefix(ISecurityServiceConstants.MACHINE_NODE_PREFIX);
        debug(INFO, "handleRequest(), machineDetailsList - " + machineDetailsList);

        for (int i=0; i < machineDetailsList.size(); i++) {
            SecurityComplianceResultNode machineDetailsNode = (SecurityComplianceResultNode) machineDetailsList.get(i);
            String[] machineDetailsNodeKeyVal = machineDetailsNode.getKeyVal();
            debug(INFO, "handleRequest(), machineDetailsNodeKeyVal - " + machineDetailsNodeKeyVal);
            if (machineDetailsNodeKeyVal != null) {
                debug(INFO, "handleRequest(), machineDetailsNodeKeyVal.length - " + machineDetailsNodeKeyVal.length);
                debug(DETAILED_INFO, "***********  machineDetailsNodeKeyVals ********************");
                for (int j = 0; ((machineDetailsNodeKeyVal != null) && (j < machineDetailsNodeKeyVal.length)); j += 2) {
                    String key = machineDetailsNodeKeyVal[j];
                    String val = machineDetailsNodeKeyVal[j + 1];
                    if (DETAILED_INFO) {
                        System.out.println(key + "=" + val);
                    }
                    if (MACHINE_NAME.equals(key)) {
                        machineName = val;
                    } else if (MACHINE_SCANNER_COUNT.equals(key)) {
                        try {
                            totalScanners = Integer.parseInt(val);
                        } catch (Throwable t) {
                            totalScanners = 0;
                        }
                    }
                }
                debug(DETAILED_INFO, "*********************************************************************");
            } else {
                debug(INFO, "handleRequest(), machineDetailsNodeKeyVal NULL");
            }
        }

        if (machineName == null) {
            securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_PROCESSING, LOG_AUDIT);
        } else {
            securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_PROCESSING, LOG_AUDIT, machineName);
        }

        debug(INFO, "handleRequest(), sending back reply...");

        request.reply(HTTP_OK, "vInspector Send Compliance Details Request received, sending back details");

        out.writeInt(SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REPLY);

        try {
            SecurityScanReport securityScanReport = securityServiceSettings.getSecurityScanReportQueue().addReport(new File(pluginDataDir + File.separator + clientSecurityComplianceResultStr), PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT_QUEUE + machineName);
            debug(INFO, "handleRequest(), queued securityScanReport - " + securityScanReport);

            new File(pluginDataDir + File.separator + clientSecurityComplianceResultStr).delete();
            debug(INFO, "handleRequest(), deleted original scan report in attempt 1 - " + (!new File(pluginDataDir + File.separator + clientSecurityComplianceResultStr).exists()));

            if (machineName == null) {
                securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED, LOG_AUDIT);
            } else {
                securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED, LOG_AUDIT, machineName);
            }
            out.writeInt(1); //queuing success

            if (pluginEnabled) {
                boolean result = processQueueReport(securityScanReport.getFile(), securityServiceSettings);
                debug(INFO, "handleRequest(), Processed queued report - " + securityScanReport + " ,result - " + result);
                if (result) {
                    securityServiceSettings.getSecurityScanReportQueue().nukeReport(PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT_QUEUE + machineName);
                }
            }
        } catch (Throwable t) {
            //failed to add report to queue... ignore for now, we can only wait for next scan report from this target
            if (DETAILED_INFO) {
                t.printStackTrace();
            }
            if (machineName == null) {
                securityServiceSettings.getPluginContext().log(LOG_ERROR_QUEUING, LOG_MAJOR);
            } else {
                securityServiceSettings.getPluginContext().log(LOG_ERROR_QUEUING, LOG_MAJOR, machineName + " , Error: " + t.getMessage());
            }
            out.writeInt(0); //queuing failed
        } finally {
            new File(pluginDataDir + File.separator + clientSecurityComplianceResultStr).delete();
            debug(INFO, "handleRequest(), deleted original scan report in attempt 2 - " + (!new File(pluginDataDir + File.separator + clientSecurityComplianceResultStr).exists()));
        }

        byte[] outBuff = out.getByteArray();
        OutputStream rout = request.getOutputStream(outBuff.length);
        rout.write(outBuff);
        rout.flush();
        out.close();
        rout.close();
        in.close();
    }

    public boolean processQueueReport(File queueReport, ISecurityServiceSettings _securityServiceSettings) throws IOException {
        if (!queueReport.exists()) {
            return true;
        }

        this.securityServiceSettings = _securityServiceSettings;

        boolean machineInDB = true;
        boolean insertScanDetailsResult = false;
        boolean forwardScanDetailsResult = false;
        String subscriptionPluginStatus = securityServiceSettings.getPluginContext().getPluginContext().getChannelProperties().getProperty("subscriptionplugin.pluginStatus");

        boolean pluginEnabled = "enable".equals(subscriptionPluginStatus);
        debug(INFO, "processQueueReport(), pluginEnabled - " + pluginEnabled);
        if (!pluginEnabled) {
            securityServiceSettings.getPluginContext().log(LOG_PLUGIN_DISABLED, LOG_INFO, "Security Plugin Disabled...");
        }

        if(!securityServiceSettings.needToForwardRequests()) {
            /* If we are the "master" plugin, then we can insert scan details... */

            ISecurityComplianceResult securityComplianceResult = new SecurityComplianceResult(queueReport);

            Vector machineDetailsList = (Vector)securityComplianceResult.findNodesByPrefix(ISecurityServiceConstants.MACHINE_NODE_PREFIX);
            debug(INFO, "processQueueReport(), machineDetailsList - " + machineDetailsList);

            String machineName = "";
            int totalScanners = 0;

            for (int i=0; i < machineDetailsList.size(); i++) {
                SecurityComplianceResultNode machineDetailsNode = (SecurityComplianceResultNode) machineDetailsList.get(i);
                String[] machineDetailsNodeKeyVal = machineDetailsNode.getKeyVal();
                debug(INFO, "processQueueReport(), machineDetailsNodeKeyVal - " + machineDetailsNodeKeyVal);
                if (machineDetailsNodeKeyVal != null) {
                    debug(INFO, "processQueueReport(), machineDetailsNodeKeyVal.length - " + machineDetailsNodeKeyVal.length);
                    debug(DETAILED_INFO, "***********  machineDetailsNodeKeyVals ********************");
                    for (int j = 0; ((machineDetailsNodeKeyVal != null) && (j < machineDetailsNodeKeyVal.length)); j += 2) {
                        String key = machineDetailsNodeKeyVal[j];
                        String val = machineDetailsNodeKeyVal[j + 1];
                        if (DETAILED_INFO) {
                            System.out.println(key + "=" + val);
                        }
                        if (MACHINE_NAME.equals(key)) {
                            machineName = val;
                        } else if (MACHINE_SCANNER_COUNT.equals(key)) {
                            try {
                                totalScanners = Integer.parseInt(val);
                            } catch (Throwable t) {
                                totalScanners = 0;
                            }
                        }
                    }
                    debug(DETAILED_INFO, "*********************************************************************");
                } else {
                    debug(INFO, "processQueueReport(), machineDetailsNodeKeyVal NULL");
                }
            }

            Vector securityComplianceList = (Vector)securityComplianceResult.findNodesByPrefix(ISecurityServiceConstants.COMPLIANCE_DETAILS_NODE_PREFIX);
            debug(INFO, "processQueueReport(), securityComplianceList - " + securityComplianceList);

            ArrayList<SecurityComplianceBean> securityComplianceBeans = new ArrayList<SecurityComplianceBean>();
            debug(INFO, "processQueueReport(), securityComplianceList.size() - " + securityComplianceList.size());

            for (int i=0; i < securityComplianceList.size(); i++) {
                SecurityComplianceResultNode securityComplianceResultNode = (SecurityComplianceResultNode)securityComplianceList.get(i);
                String[] securityComplianceResultNodeKeyVal = securityComplianceResultNode.getKeyVal();
                debug(INFO, "processQueueReport(), securityComplianceResultNodeKeyVal - " + securityComplianceResultNodeKeyVal);
                if (securityComplianceResultNodeKeyVal != null) {
                    debug(INFO, "processQueueReport(), securityComplianceResultNodeKeyVal.length - " + securityComplianceResultNodeKeyVal.length);
                    debug(INFO, "***********  securityComplianceResultNodeKeyVals ********************");
                    for (int j=0; ((securityComplianceResultNodeKeyVal != null) && (j < securityComplianceResultNodeKeyVal.length)); j+=2) {
                        String key = securityComplianceResultNodeKeyVal[j];
                        String val = securityComplianceResultNodeKeyVal[j + 1];
                        if(DETAILED_INFO) {
                            System.out.println(key + "=" + val);
                        }
                    }
                    debug(INFO, "*********************************************************************");
                } else {
                    debug(INFO, "processQueueReport(), securityComplianceResultNodeKeyVal NULL");
                }
                SecurityComplianceBean securityComplianceBean = new SecurityComplianceBean();
                for (int j=0; ((securityComplianceResultNodeKeyVal != null) && (j < securityComplianceResultNodeKeyVal.length)); j+=2) {
                    String key = securityComplianceResultNodeKeyVal[j];
                    String val = securityComplianceResultNodeKeyVal[j+1];
                    if ((key != null) && key.startsWith(COMPLIANCE_DETAILS_RULE_PREFIX)) {
                        key = key.substring(5);
                        securityComplianceBean.addRulesCompliance(key, val);
                    } else {
                        if (COMPLIANCE_DETAILS_CONTENT_ID.equals(key)) {
                            securityComplianceBean.setContentId(val);
                        } else if (COMPLIANCE_DETAILS_CONTENT_FILE_NAME.equals(key)) {
                            securityComplianceBean.setContentFileName(val);
                        } else if (COMPLIANCE_DETAILS_CONTENT_TITLE.equals(key)) {
                            securityComplianceBean.setContentTitle(val);
                        } else if (COMPLIANCE_DETAILS_CONTENT_TARGET_OS.equals(key)) {
                            securityComplianceBean.setContentTargetOS(val);
                        } else if (COMPLIANCE_DETAILS_PROFILE_ID.equals(key)) {
                            securityComplianceBean.setProfileID(val);
                        } else if (COMPLIANCE_DETAILS_PROFILE_TITLE.equals(key)) {
                            securityComplianceBean.setProfileTitle(val);
                        } else if (COMPLIANCE_DETAILS_SCANNED_BY.equals(key)) {
                            securityComplianceBean.setScannedBy(val);
                        } else if (COMPLIANCE_DETAILS_TARGET_NAME.equals(key)) {
                            securityComplianceBean.setTargetName(val);
                        } else if (COMPLIANCE_DETAILS_OVERALL_COMPLIANCE.equals(key)) {
                            securityComplianceBean.setOverallCompliance(val);
                        } else if (COMPLIANCE_DETAILS_INDIVIDUAL_COMPLIANCE.equals(key)) {
                            securityComplianceBean.setIndividualCompliance(val);
                        } else if (COMPLIANCE_DETAILS_MACHINE_NAME.equals(key)) {
                            securityComplianceBean.setMachineName(val);
                        } else if (COMPLIANCE_DETAILS_START_TIME.equals(key)) {
                            securityComplianceBean.setStartTime(Long.parseLong(val));
                        } else if (COMPLIANCE_DETAILS_FINISH_TIME.equals(key)) {
                            securityComplianceBean.setFinishTime(Long.parseLong(val));
                        } else if (COMPLIANCE_DETAILS_LAST_POLICY_UPDATE_TIME.equals(key)) {
                            securityComplianceBean.setLastPolicyUpdateTime(Long.parseLong(val));
                        } else if (COMPLIANCE_DETAILS_SCAN_TYPE.equals(key)) {
                            securityComplianceBean.setScanType(val);
                        }
                    }
                }
                if ((securityComplianceResultNodeKeyVal != null) && (securityComplianceResultNodeKeyVal.length > 0)) {
                    securityComplianceBean.setRulesCompliance(getRulesComplianceAsMap(securityComplianceBean.getIndividualCompliance()));
                    securityComplianceBeans.add(securityComplianceBean);
                }
            }

            if (securityComplianceBeans.size() > 0) {
                target = "[" + securityComplianceBeans.get(0).getMachineName() + "(" + securityComplianceBeans.get(0).getTargetName() + ")]";
            }

            if (target == null) {
                securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_PROCESSING, LOG_AUDIT);
            } else {
                securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_PROCESSING, LOG_AUDIT, target);
            }

            if (pluginEnabled) {
                HashMap<String, String> moreInfo = new HashMap<String, String>();
                insertScanDetailsResult = securityServiceSettings.getStorage().insertScanDetails(securityComplianceBeans, machineName, moreInfo);
                debug(INFO, "processQueueReport(), insertScanDetailsResult1 - " + insertScanDetailsResult);

                if (insertScanDetailsResult) {
                    for (SecurityComplianceBean securityComplianceBean : securityComplianceBeans) {
                        HashMap<String, String> rulesBean = securityComplianceBean.getRulesCompliance();
                        for (Map.Entry<String, String> entry : rulesBean.entrySet()) {
                            String ruleName = entry.getKey();
                            String[] group = ((DbDataStorage) securityServiceSettings.getStorage("db")).getGroupName(securityComplianceBean.getScanType(), securityComplianceBean.getContentId(), ruleName);
                            securityComplianceBean.addRuleAndGroupMap(ruleName, group);
                        }
                    }
                    moreInfo = new HashMap<String, String>();
                    insertScanDetailsResult = securityServiceSettings.getStorage("bigdata").insertScanDetails(securityComplianceBeans, machineName, moreInfo);
                    debug(INFO, "processQueueReport(), insertScanDetailsResult2 - " + insertScanDetailsResult);
                }

                if (insertScanDetailsResult) {
                    if ("true".equals(moreInfo.get("result"))) {
                        //log this message only if result=true from bigdata... which means it actually inserted the record, not just queued...
                        securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_PROCESSED, LOG_AUDIT, ((target != null) ? target : "") + " --> SUCCESS");
                    }
                } else {
                    securityServiceSettings.getPluginContext().log(LOG_ERROR_PROCESSING, LOG_MAJOR, ((target != null) ? target : "") + " --> FAILED [ " + moreInfo.get("error") + " ]");
                    if ("Machine Entry missing in Database".equals(moreInfo.get("error"))) {
                        machineInDB = false;
                    }
                }
            } else {
                if (target == null) {
                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED, LOG_AUDIT);
                } else {
                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED, LOG_AUDIT, target);
                }
            }
        } else {
            if (target == null) {
                securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_FORWARDING, LOG_AUDIT);
            } else {
                securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_FORWARDING, LOG_AUDIT, target);
            }

            if (pluginEnabled) {
                PluginConnection conn = securityServiceSettings.getFreshPluginConnection();
                if (conn == null) {
                    debug(INFO, "processQueueReport(), Need to get forward securitycompliance insert request to master plugin, but unable to create plugin connection!");
                } else {
                    forwardScanDetailsResult = forwardSendComplianceDetailsRequestAndGetReply(securityServiceSettings, conn, new FastOutputStream(1024), queueReport);
                    conn.close();
                }
            } else {
                if (target == null) {
                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED, LOG_AUDIT);
                } else {
                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED, LOG_AUDIT, target);
                }
            }
        }

        if (securityServiceSettings.needToForwardRequests()) {
            if (forwardScanDetailsResult) {
                return true;
            } else {
                return false;
            }
        } else {
            if (insertScanDetailsResult || (!machineInDB)) {
                return true;
            } else {
                return false;
            }
        }
    }

    HashMap<String, String> getRulesComplianceAsMap(String individualCompliance) {
        HashMap<String, String> result = new HashMap<String, String>();
        debug(INFO, "getRulesComplianceAsMap() :: individualCompliance - " + individualCompliance);
        try {
            JSONObject individualComplianceJson = new com.marimba.apps.subscriptionplugin.util.json.JSONObject(individualCompliance);
            debug(INFO, "getRulesComplianceAsMap() :: individualComplianceJson - " + individualComplianceJson);

            JSONObject rulesComplianceJson = individualComplianceJson.getJSONObject("rules_compliance");
            debug(INFO, "getRulesComplianceAsMap() :: rulesComplianceJson - " + rulesComplianceJson);

            Iterator rulesComplianceJsonIterator = rulesComplianceJson.keys();
            while (rulesComplianceJsonIterator.hasNext()) {
                String ruleId = (String) rulesComplianceJsonIterator.next();
                String ruleCompliance = (String) rulesComplianceJson.get(ruleId);
                result.put(ruleId, ruleCompliance);
            }
        } catch (Throwable t) {
            if (DETAILED_INFO) {
                t.printStackTrace();
            }
        }
        debug(INFO, "getRulesComplianceAsMap() :: result - " + result);
        return result;
    }
	
    protected boolean forwardSendComplianceDetailsRequestAndGetReply(ISecurityServiceSettings _securityServiceSettings, PluginConnection conn, FastOutputStream out, File reportFile) throws IOException {
        this.securityServiceSettings = _securityServiceSettings;

		conn.open();
		conn.setCredential("Basic ");
		conn.setSegment(".configurator");
		conn.addField("Request-Type", "securityservice");
		conn.addField("Request-sender", "tuner");
		conn.addField("tuner-id", securityServiceSettings.getNewGUID());

		String credentials = securityServiceSettings.getPluginSubscribeCredentials();
		if(credentials == null) {
            debug(INFO, "forwardSendComplianceDetailsRequestAndGetReply() :: Not using credentials");
			conn.setCredential("Basic ");
		} else {
            debug(INFO, "forwardSendComplianceDetailsRequestAndGetReply() :: Will use credentials when connecting...");
			conn.setCredential(credentials);
		}

		FastOutputStream fout = new FastOutputStream(1024);
        fout.writeInt(MAGIC);
        fout.writeInt(VERSION);
        fout.writeInt(SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REQUEST);
        if (cs == null) {
            cs = getChecksumForFile(reportFile);
            size = reportFile.length();
        }
        cs.write(fout);
        fout.writeLong(size);

        FastInputStream reportFileIn = null;
        try {
            reportFileIn = new FastInputStream(reportFile, 1024);

            long length = size;
            byte[] inbuf = new byte[BUFFER_SIZE];
            while (length > 0) {
                int n = (length > inbuf.length) ? inbuf.length : (int) length;
                try {
                    int m = n;
                    if ((n = reportFileIn.read(inbuf, 0, n)) < 0) {
                        debug(INFO, "forwardSendComplianceDetailsRequestAndGetReply() :: Got less bytes when streaming securitycompliance file to plugin.");
                    }
                } catch (Exception ex) {
                    debug(INFO, "forwardSendComplianceDetailsRequestAndGetReply() :: Got IOException when streaming securitycompliance file to plugin - " + ex.getMessage());
                }
                if (n > 0) {
                    fout.write(inbuf, 0, n);
                    length -= n;
                }
            }
            reportFileIn.close();
        } catch (Exception e) {
            debug(INFO, "forwardSendComplianceDetailsRequestAndGetReply() :: Got Exception when streaming securitycompliance file to plugin - " + e.getMessage());
        }

		byte[] outBuff = fout.getByteArray();
		int contentLength = outBuff.length;

		OutputStream rout = conn.getOutputStream(contentLength);
		rout.write(outBuff, 0, outBuff.length);
		rout.flush();

		int reply = conn.reply();
        debug(INFO, "forwardSendComplianceDetailsRequestAndGetReply() :: Got reply from plugin - " + reply);

		if(reply == HTTP_OK) {
			FastInputStream in = new FastInputStream(conn.getInputStream(), 1024);
            int replyType = in.readInt();
            int isQueued = in.readInt();
            debug(INFO, "forwardSendComplianceDetailsRequestAndGetReply() :: replyType - " + replyType + " ,isQueued - " + isQueued);

			if(replyType == SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REPLY) {
                securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_FORWARDED, LOG_AUDIT, ((target != null) ? target : "") + " --> SUCCESS");
                debug(INFO, "Received SECURITY_SERVICE_SEND_COMPLIANCE_DETAILS_REPLY...");
                out.writeInt(replyType);
                out.writeInt(isQueued);
                in.close();
                return true;
            } else {
                securityServiceSettings.getPluginContext().log(LOG_ERROR_FORWARDING, LOG_MAJOR, ((target != null) ? target : "") + " --> FAILED [ Reply type received from forward plugin " + replyType + " ]");
                debug(INFO, "Didn't receive proper reply " + replyType);
                out.writeInt(replyType);
                in.close();
                return false;
            }
		} else {
            securityServiceSettings.getPluginContext().log(LOG_ERROR_FORWARDING, LOG_MAJOR, ((target != null) ? target : "") + " --> FAILED [ Connection to forward plugin returned " + reply + " ]");
            debug(INFO, "Connection to forward plugin returned " + reply);
            return false;
        }
	}

    protected IChecksum getChecksumForFile(File f) {
        int BUFFER_SIZE = 4 * 1024;
        IChecksum cs = null;
        try {
            byte[] inbuf = new byte[BUFFER_SIZE];
            long fileSize = f.length();
            FastInputStream in = new FastInputStream(f, 1024);
            ChecksumAlgorithm md5 = ChecksumFactory.getAlgorithm("md5");
            ChecksumInputStream md5in = new ChecksumInputStream(in, md5);
            try {
                for (long j = fileSize; j > 0;) {
                    int n = md5in.read(inbuf, 0, (int) Math.min(j, inbuf.length));
                    if (n < 0) {

                    }
                    j -= n;
                }
            } finally {
                md5in.close();
                cs = md5in.getChecksum();
            }
        } catch (IOException ie) {
            debug(ERROR, "getChecksumForFile() :: Exception while trying to get checksum of off file " + f.getAbsolutePath() + " - " + ie.getMessage());
            cs = null;
        }
        return cs;
    }

    protected boolean handleComplientResultData(FastInputStream in) throws IOException {
        cs = new MD5Checksum(in.readLong(), in.readLong());
        debug(INFO, "handleComplientResultData() :: cs - " + cs.toString());

        size = in.readLong();
        debug(INFO, "handleComplientResultData() :: size - " + size);

        return writeComplientResult(in, size);
    }

    protected boolean writeComplientResult(FastInputStream in, long size) {
        debug(INFO, "writeComplientResult()...");
        File clientLatestComplianceResult = new File(pluginDataDir, clientSecurityComplianceResultStr);
        File clientOldComplianceResult = new File(clientLatestComplianceResult.getParent(), PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT_PREVIOUS + currentTimeMilli);
        debug(INFO, "writeComplientResult() :: clientLatestComplianceResult - " + clientLatestComplianceResult);
        debug(INFO, "writeComplientResult() :: clientOldComplianceResult - " + clientOldComplianceResult);

        try {
            if (clientLatestComplianceResult.exists()) {
                if(clientOldComplianceResult.exists()) {
                    debug(INFO, "writeComplientResult() :: Delete securityservice_prev which is present at client...");
                    clientOldComplianceResult.delete();
                }

                if (!clientLatestComplianceResult.renameTo(clientOldComplianceResult)) {
                    debug(INFO, "writeComplientResult() :: Taking back-up of existing client securitycompliance file failed...");
                    return false;
                } else {
                    debug(INFO, "writeComplientResult() :: Taking back-up of existing client securitycompliance file success...");
                }
            }

            FastOutputStream fout = new FastOutputStream(clientLatestComplianceResult.getAbsolutePath(), BUFFER_SIZE);
            try {
                byte[] inbuf = new byte[BUFFER_SIZE];
                while (size > 0) {
                    int n = (size > inbuf.length) ? inbuf.length : (int) size;
                    try {
                        int m = n;
                        if ((n = in.read(inbuf, 0, n)) < 0) {
                            debug(INFO, "writeComplientResult() :: Got less bytes when streaming securitycompliance file from plugin.");
                        }
                    } catch (IOException ex) {
                        debug(INFO, "writeComplientResult() :: Got IOException when streaming securitycompliance file from plugin.");
                        ex.printStackTrace();
                    }
                    if (n > 0) {
                        fout.write(inbuf, 0, n);
                        size -= n;
                    }
                }
                fout.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fout.close();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (!clientLatestComplianceResult.exists()) {
                clientOldComplianceResult.renameTo(clientLatestComplianceResult);
            } else {
                clientOldComplianceResult.delete();
            }
        }

    }

    protected String getFileNameFromChecksum(String checksum) {
        String fileName = checksum.substring("urn:md5:".length());
        fileName = fileName.replace('/', '_')
                           .replace('\\', '_')
                           .replace(':', '_')
                           .replace('?', '_')
                           .replace('"', '_')
                           .replace('<', '_')
                           .replace('>', '_')
                           .replace('|', '_')
                           .replace('*', '_');

        debug(INFO, "getFileNameFromChecksum() :: checksum - " + checksum + " fileName - " + fileName);
        return fileName;
    }

    public void debug(boolean debugType, String msg) {
        if (debugType) {
            securityServiceSettings.getPluginContext().logToConsole("SendComplianceDetailsRequest.java -- " + msg);
        }
    }

}

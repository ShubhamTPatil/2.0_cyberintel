// Copyright 2017-2019, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionplugin.impl.bigdata;

import java.io.*;
import java.lang.String;
import java.lang.Throwable;
import java.nio.channels.FileChannel;
import java.util.*;
import java.net.*;

import com.marimba.apps.subscriptionplugin.SecurityComplianceBean;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceSettings;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceConstants;
import com.marimba.apps.subscriptionplugin.util.elastic.ElasticInserter;
import com.marimba.apps.subscriptionplugin.util.elastic.ElasticPoster;
import com.marimba.apps.subscriptionplugin.util.json.JSONObject;
import com.marimba.intf.plugin.IPluginContext;
import com.marimba.apps.subscriptionplugin.intf.IDataStorage;
import com.marimba.io.FastInputStream;
import com.marimba.io.FastOutputStream;
import com.marimba.tools.util.*;
import com.marimba.tools.net.*;
import com.marimba.castanet.http.*;
import com.marimba.intf.http.*;

public class BigDataStorage implements ISecurityServiceConstants, IDataStorage, HTTPConstants {
    private final IPluginContext context;
    private final ISecurityServiceSettings securityServiceSettings;
    private final Object lock = new Object();
    private final File down;
    private final Timer timer;
    private int BIGDATA_LOGS = DebugFlag.getDebug("SECURITY/PLUGINBIGDATA");
    private boolean EXTRA_LOGS = DebugFlag.getDebug("SECURITY/PLUGINEXTRALOGS") >= 2; 
    private final String REPORT_START_NAME = "delastic_";

    HTTPManager httpMgr;
    HTTPConfig httpConfig;

    private static final String DEFAULT_ELASTIC_URL = "http://localhost:9200/marimba/securitymgmt";
    private static final String ELASTIC_PROP = "subscriptionplugin.elastic.url";
    String elasticUrl = DEFAULT_ELASTIC_URL;
    private final ElasticPoster poster;
    
    private class ClearDownTask extends TimerTask {
    	public ClearDownTask() {

    	}
		@Override
		public void run() {
            clearDown();
		}
    }

	public BigDataStorage(ISecurityServiceSettings _securityServiceSettings, IPluginContext context) {
        this.securityServiceSettings = _securityServiceSettings;
		this.context = context;
		File dataDir = new File(context.getDataDirectory());
		this.down = dataDir;
		this.timer = new Timer();
		timer.schedule(new ClearDownTask(), 60 * 1000L, 15 * 60 * 1000L);
		this.poster = new ElasticPoster();
		this.poster.setElasticURL(this.context.getChannelProperties().getProperty(ELASTIC_PROP));
	}

    public boolean init(HTTPManager _httpMgr, HTTPConfig _httpConfig) {
        this.httpMgr = _httpMgr;
        this.httpConfig = _httpConfig;

        String temp = context.getChannelProperties().getProperty(ELASTIC_PROP);
        if(temp != null) {
            elasticUrl = temp;
        }
        
        this.poster.start();

        try {
            return isConnected();
        } catch (Throwable t) {
            return false;
        }
        
    }
    
    public void stop() {
    	poster.stop();
    }

    URL getElasticConnectionCheckUrl(String elasticUrl) {
        URL u = null;
        try {
            u = new URL(elasticUrl);
            String host = u.getHost();
            int port = u.getPort();
            if (port < 1) {
                port = 80;
            }
            String protocol = u.getProtocol();
            return new URL(protocol + "://" + host + ":" + port);
        } catch (Throwable t) {
            if (BIGDATA_LOGS > 7) {
                t.printStackTrace();
            }
            return u;
        }
    }

    public boolean isConnected() throws Throwable {
        boolean retVal = false;
        HTTPRequest req = null;
        int reqReply = -1;
        try {
            if (elasticUrl == null) {
                debug(DETAILED_INFO, "isConnected() :: elasticUrl - " + elasticUrl);
                return retVal;
            }

            URL elasticUrlToconnect = getElasticConnectionCheckUrl(elasticUrl);
            HttpURLConnection elasticUrlConnection = (HttpURLConnection)elasticUrlToconnect.openConnection();

//            req = httpMgr.create(elasticUrlToconnect);
//            do {
//                if(req != null) {
//                    debug(DETAILED_INFO, "isConnected() :: Request is NOW physically created to - " + elasticUrlToconnect);
//                } else {
//                    debug(DETAILED_INFO, "isConnected() :: Request is NOT created to since it was NULL");
//                }
//
//                //connecting...
//                req.connect(false);
//                debug(DETAILED_INFO, "isConnected() :: Keep alive is true...");
//                req.addField("Keep-Alive","true");
//                req.addField("Accept", "*/*");
//                req.addField("Content-type","application/x-www-form-urlencoded");
//                req.addField("authorization", "Basic ");
//
////                debug(DETAILED_INFO, "isConnected() :: Doing post without any content!");
////                req.post(0);
//
//                //trigger HTTP GET
//                req.get();
//                reqReply = req.reply();
//            } while (reqReply == HTTP_RETRY);

            int httpRetCode = elasticUrlConnection.getResponseCode();
            debug(DETAILED_INFO, "isConnected() :: httpRetCode - " + httpRetCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(elasticUrlConnection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            switch(httpRetCode) {
                case HTTP_OK:
                    debug(DETAILED_INFO, "isConnected() :: HTTP reply OK, so Elastic Search seemed to have accepted it, printing out reply");
                    retVal = true;
                    break;
                default:
                    debug(DETAILED_INFO, "isConnected() :: HTTP reply returned: " + reqReply);
                    break;
            }

        } catch(Throwable t) {
            if (BIGDATA_LOGS > 7) {
                t.printStackTrace();
            }
            throw t;
        } finally {
//            if (req != null) {
//                req.close();
//                req = null;
//            }
            debug(DETAILED_INFO, "isConnected() :: Request finished transferred...");
        }
        debug(DETAILED_INFO, "isConnected() :: retVal - " + retVal);
        return retVal;
    }

    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans) {
        return insertScanDetails(securityComplianceBeans, null);
    }

    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine) {
        return insertScanDetails(securityComplianceBeans, null, new HashMap<String, String>());
    }
    
    class ElasticMissedReportsFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if(name.startsWith(REPORT_START_NAME)) {
				return true;
			}
			return false;
		}
    	
    }

    protected void clearDown() {
        boolean connected = false;
        try {
            connected = isConnected();
        } catch (Throwable t) {
            connected = false;
        }
        if (!connected) {
            securityServiceSettings.getPluginContext().log(LOG_PLUGIN_DOWNREPORTS_SKIP_BIGDATA, LOG_MAJOR);
            return;
        }
    	long pass = 0, fail = 0;
    	securityServiceSettings.getPluginContext().log(LOG_PLUGIN_DOWNREPORTS_START_BIGDATA, LOG_INFO);
        try {
            if (down != null) {
                File[] downFiles = down.listFiles(new ElasticMissedReportsFilter());
                if (downFiles != null) {
                    for (File f : downFiles) {
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        try {
                            FileInputStream in = new FileInputStream(f);
                            while (in.available() > 0) {
                                bout.write(in.read());
                            }
                            JSONObject record = new JSONObject(new String(bout.toByteArray()));
                            bout.close();
                            in.close();
                            synchronized (lock) {
                                if (poster.submit(record)) {
                                    pass++;
                                    //clear the successful insertion of failed record from queue
                                    f.delete();
                                    if (EXTRA_LOGS) {
                                        String logMessage = "[" + record.getString("machineName") + "(" + record.getString("targetName") + ")"
                                                + " -- " + record.getString("contentId") + " -- " + record.getString("profileID") + " -- " + "]";
                                        securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_INSERTED_BIGDATA, LOG_AUDIT, logMessage + "." + record.getString("ruleID"));
                                    }
                                } else {
                                    fail++;
                                }
                            }
                        } catch (Throwable e) {
                            if (ERROR) {
                                e.printStackTrace();
                            }
                            fail++;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            if (EXTRA_LOGS) {
                t.printStackTrace();
            }
            //we failed to handle something in this timer tick... lets process reports in the next run...
        }
    	securityServiceSettings.getPluginContext().log(LOG_PLUGIN_DOWNREPORTS_STOP_BIGDATA, LOG_INFO, "passed: "+pass+", failed: "+fail);
    }

    protected boolean storeInDown(JSONObject obj) {
    	String fName = REPORT_START_NAME + obj.getString("machineName") + "_" + obj.getString("ruleID") + "_" + obj.getLong("@timestamp");
        fName = fName.replaceAll(":", ".");
    	File f = new File(down, fName);
    	try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write(obj.toString()); writer.flush(); writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return f.exists();
    }

    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine, HashMap<String, String> moreInfo) {
        boolean allInserted = true;
        boolean success = false;
        int failed = 0;
        int queued = 0;
        debug(INFO, "insertScanDetails() :: securityComplianceBeans - " + securityComplianceBeans);
        debug(INFO, "insertScanDetails() :: securityComplianceForMachine - " + securityComplianceForMachine);

        String error = "";
        try {
            for (SecurityComplianceBean securityComplianceBean : securityComplianceBeans) {
                boolean isElasticReachable = false;
                try {
                    isElasticReachable = isConnected();
                } catch (Throwable t) {
                    //only if we get a proper false reply from isConnected(), we know that the http request to elastic server failed...
                    //any exceptions means, we were not able to create a http request, and lets move ahead assuming server is up...
                    if(ERROR) {
                        t.printStackTrace();
                    }
                    isElasticReachable = true;
                }
                debug(INFO, "insertScanDetails() :: isElasticReachable - " + isElasticReachable);

                String contentId = securityComplianceBean.getContentId();
                String contentFileName = securityComplianceBean.getContentFileName();
                String contentTitle = securityComplianceBean.getContentTitle();
                String contentTargetOS = securityComplianceBean.getContentTargetOS();
                String profileID = securityComplianceBean.getProfileID();
                String profileTitle = securityComplianceBean.getProfileTitle();
                String scannedBy = securityComplianceBean.getScannedBy();
                String targetName = securityComplianceBean.getTargetName();
                String overallCompliance = securityComplianceBean.getOverallCompliance();
                String individualCompliance = securityComplianceBean.getIndividualCompliance();
                String machineName = securityComplianceBean.getMachineName();
                if ((securityComplianceForMachine != null) && (machineName == null)) {
                    machineName = securityComplianceForMachine;
                }
                long startTime = securityComplianceBean.getStartTime();
                long finishTime = securityComplianceBean.getFinishTime();
                long lastPolicyUpdateTime = securityComplianceBean.getLastPolicyUpdateTime();
                HashMap<String, String> rulesCompliance = securityComplianceBean.getRulesCompliance();
                HashMap<String, String[]> ruleAndGroupMap = securityComplianceBean.getRuleAndGroupMap();

                String logMessage = "[" + machineName + "(" + securityComplianceBean.getTargetName() + ")"
                        + " -- " + securityComplianceBean.getContentId() + " -- " + securityComplianceBean.getProfileID() + " -- " + "]";

                boolean currentInserted = true, currentQueued = false;
                if ((rulesCompliance == null) || (rulesCompliance.size() < 1)) {
                    //nothing to save in elastic... treat it as success...
                    currentInserted = true;
                } else {
                    for (String key : rulesCompliance.keySet()) {
                    	synchronized(lock) {
                    		String val = rulesCompliance.get(key);
                            JSONObject record = new JSONObject();
                            record.put("ruleID", key);
                            record.put("compliance", val);
                            record.put("@timestamp", lastPolicyUpdateTime);
                            record.put("startTime", startTime);
                            record.put("finishTime", finishTime);
                            record.put("machineName", machineName);
                            record.put("overallCompliance", overallCompliance);
                            record.put("targetName", targetName);
                            record.put("scannedBy", scannedBy);
                            record.put("profileTitle", profileTitle);
                            record.put("profileID", profileID);
                            record.put("groupTitle", ruleAndGroupMap.get(key)[1]);
                            record.put("groupID", ruleAndGroupMap.get(key)[0]);
                            record.put("contentTargetOS", contentTargetOS);
                            record.put("contentTitle", contentTitle);
                            record.put("contentFileName", contentFileName);
                            record.put("contentId", contentId);
                            boolean currentRuleResult = false;
                            try {
                                debug(INFO, "insertScanDetails() :: record - " + record);
                                currentRuleResult = isElasticReachable ? poster.submit(record) : false;
                                if (currentRuleResult) {
                                    moreInfo.put("result." + logMessage + "." + key, "true");
                                } else {
                                    error += logMessage + "." + key + " --> Failed to Insert;";
                                    moreInfo.put("error." + logMessage + "." + key, "Failed to Insert");
                                    moreInfo.put("result." + logMessage + "." + key, "false");
                                }
                            } catch (Throwable t) {
                                if(ERROR) {
                                    t.printStackTrace();
                                }
                                error += logMessage + "." + key + " --> " + t.getMessage() + ";";
                                moreInfo.put("result." + logMessage + "." + key, "false");
                                moreInfo.put("error." + logMessage + "." + key, t.getMessage());
                                currentRuleResult = false;
                            }
                            currentInserted = currentInserted ? currentRuleResult : false;
                            if (!currentRuleResult) {
                                failed++;
                                if (storeInDown(record)) {
                                    currentQueued = true;
                                    queued++;
                                    if (EXTRA_LOGS) {
                                        securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED_BIGDATA, LOG_AUDIT, logMessage + "." + key);
                                    }
                                } else {
                                    error += logMessage + "." + key + " --> Failed to Queue;";
                                    if (EXTRA_LOGS) {
                                        securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_NOT_QUEUED_BIGDATA, LOG_MAJOR, logMessage + "." + key);
                                    }
                                }
                            } else {
                                if (EXTRA_LOGS) {
                                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_INSERTED_BIGDATA, LOG_AUDIT, logMessage + "." + key);
                                }
                            }
                    	}
                    }
                }

                debug(INFO, "insertScanDetails() :: currentInserted - " + currentInserted + " ,currentQueued - " + currentQueued);
                if (currentInserted) {
                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_INSERTED_BIGDATA, LOG_AUDIT, logMessage);
                } else if (currentQueued) {
                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_QUEUED_BIGDATA, LOG_AUDIT, logMessage);
                }

                allInserted = allInserted ? currentInserted : false;
            }
        } catch(Throwable t) {
            error += t.getMessage() + ";";
            if(ERROR) {
                t.printStackTrace();
            }
            allInserted = false;
        } finally {
            moreInfo.put("error", error);

            debug(INFO, "insertScanDetails() :: allInserted - " + allInserted + " ,failed - " + failed + " ,queued - " + queued);

            //we treat it as success, if we either inserted all the records or atleast queued all failed ones...
            success = allInserted && (failed == queued);

            moreInfo.put("result", "" + allInserted);
            moreInfo.put("success", "" + success);
        }

        return success;
    }

    public void debug(boolean debugType, String msg) {
        if (debugType || (BIGDATA_LOGS >= 3)) {
            securityServiceSettings.getPluginContext().logToConsole("BigDataStorage.java -- " + msg);
        }
    }

}

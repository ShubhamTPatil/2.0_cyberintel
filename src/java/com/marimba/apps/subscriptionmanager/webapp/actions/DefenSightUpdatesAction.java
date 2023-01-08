// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.compliance.util.DefinitionUpdateHandler;
import com.marimba.apps.securitymgr.compliance.util.VdefCarTransferHandler;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.forms.DefenSightUpdatesForm;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.apps.subscriptionmanager.beans.SCAPContentDetails;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.securitymgr.view.SecurityUpdateDetailsBean;
import com.marimba.tools.config.*;
import com.marimba.tools.util.*;
import com.marimba.intf.castanet.*;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.util.*;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * DefenSight Updates Action
 * w.r.t update sync operation task
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */
public class DefenSightUpdatesAction extends AbstractAction {
    public static int DEBUG = DebugFlag.getDebug("SECURITY/MGR");
    private Locale locale;


    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        debug("execute()...");
        init(request);
        locale = request.getLocale();
        String view = "success";
        String action = request.getParameter("action");
        debug("execute(), action - " + action);
        DefenSightUpdatesForm updatesForm = new DefenSightUpdatesForm();
        Object bean = request.getSession().getAttribute(IWebAppsConstants.INITERROR_KEY);

        if ((bean != null) && bean instanceof Exception) {
            debug("execute()...critical exception found");
            //remove initerror from the session because it has served its purpose
            request.getSession().removeAttribute(IWebAppsConstants.INITERROR_KEY);
            view = "initerror";
        } else {
            if ("dosync".equals(action)) {
                boolean forceSync = "force".equals(request.getParameter("sync"));
                String contentsParam = request.getParameter("contents");
                String xmls = "";
                if (contentsParam != null) {
                    if ("all".equals(contentsParam)) {
                        debug("execute(), sync content - all");
                        xmls = "";
                    } else {
                        String contents[] = contentsParam.split(":_:");
                        HashMap<String, String> securityInfoXmls = getSecurityInfoXmls();
                        debug("execute(), securityInfoXmls - " + securityInfoXmls);
                        for (String content : contents) {
                            if ((content == null) || (content.trim().length() < 1)) {
                                continue;
                            }
                            debug("execute(), sync content - " + content);
                            xmls += securityInfoXmls.get(content) + ";";
                        }
                    }
                }
                triggerContentUpdate(xmls, forceSync);
            }
            updatesForm.initialize(main);
            request.setAttribute("updates", getUpdates());
            request.setAttribute("scapUpdateAvailable", updatesForm.getProperty("scapUpdateAvailable"));
            request.setAttribute("scapUpdateStatus", updatesForm.getProperty("scapUpdateStatus"));
            if ("inprogress".equals(updatesForm.getProperty("scapUpdateStatus"))) {
                view = "inprogress";
            }
            request.setAttribute("scapUpdateTime", updatesForm.getProperty("scapUpdateTime"));
            request.setAttribute("scapUpdateInProgressStatus", updatesForm.getProperty("scapUpdateInProgressStatus"));
            String securityInfoPath = main.getConfig().getProperty("subscriptionmanager.securityinfo.url");
            if ((null != securityInfoPath) && (securityInfoPath.trim().length() > 0) && securityInfoPath.endsWith("/vDef")) {
                //vDef configured...
            } else {
                view = "vDeferror";
            }
        }
        debug("execute(), updatesForm.getProps() - " + updatesForm.getProps());
        return mapping.findForward(view);
    }

    private void triggerContentUpdate(String xmls, boolean forceSync) {
        debug("triggerContentUpdate(), xmls - " + xmls + ", forceSync - " + forceSync);
        String securityInfoUrl = SCAPUtils.getSCAPUtils().getSecurityInfoUrl();
        debug("triggerContentUpdate(), securityInfoUrl - " + securityInfoUrl);
        IChannel securityInfoChannel = SCAPUtils.getSCAPUtils().getChannel(securityInfoUrl);
        debug("triggerContentUpdate(), securityInfoChannel - " + securityInfoChannel);
        if (null == securityInfoUrl || securityInfoUrl.trim().isEmpty()) {
            debug("triggerContentUpdate(), vDef channel URL missing...");
            return;
        }
        File tempDbConfigFile = null;
        try {
            tempDbConfigFile = new File(main.getDataDirectory(), "tempDbConfig.txt");
            if (tempDbConfigFile.exists()) {
                tempDbConfigFile.delete();
            }
            tempDbConfigFile.createNewFile();
            IConfig dbCfg = new ConfigPrefs(tempDbConfigFile, null, null);
            dbCfg.setProperty("db.connection.class", main.getConfig().getProperty("subscriptionmanager.db.class"));
            dbCfg.setProperty("db.connection.url", main.getConfig().getProperty("subscriptionmanager.db.url"));
            dbCfg.setProperty("db.connection.user", main.getConfig().getProperty("subscriptionmanager.db.username"));
            dbCfg.setProperty("db.connection.pwd", Password.decode(main.getConfig().getProperty("subscriptionmanager.db.password")));
            dbCfg.setProperty("db.connection.type", Password.decode(main.getConfig().getProperty("subscriptionmanager.db.type")));
            dbCfg.setProperty("db.thread.max", main.getConfig().getProperty("subscriptionmanager.db.thread.max"));
            dbCfg.setProperty("db.thread.min", main.getConfig().getProperty("subscriptionmanager.db.thread.min"));
            ((ConfigPrefs) dbCfg).close();
            ILauncher launcher = (ILauncher) main.getFeatures().getChild("launcher");
            IConfig tunerConfig = (IConfig) main.getFeatures().getChild("tunerConfig");
            if (null == securityInfoChannel) {
                debug("triggerContentUpdate(), vDef channel not found...");
                tempDbConfigFile.delete();
                return;
            }
            if (null != tunerConfig) {
                tunerConfig.setProperty("marimba.securityinfo.sync.status." + securityInfoChannel.getURL(), "inprogress");
            }
            if ((xmls == null) || (xmls.trim().length() < 1)) {
                if (forceSync) {
                    launcher.start(securityInfoChannel.getURL(), new String[]{"dbimport", "-force", "-dbConfig", tempDbConfigFile.getAbsolutePath()}, false);
                } else {
                    launcher.start(securityInfoChannel.getURL(), new String[]{"dbimport", "-dbConfig", tempDbConfigFile.getAbsolutePath()}, false);
                }
            } else {
                if (forceSync) {
                    launcher.start(securityInfoChannel.getURL(), new String[]{"dbimport", "-force", "-xml", xmls, "-dbConfig", tempDbConfigFile.getAbsolutePath()}, false);
                } else {
                    launcher.start(securityInfoChannel.getURL(), new String[]{"dbimport", "-xml", xmls, "-dbConfig", tempDbConfigFile.getAbsolutePath()}, false);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
        }
    }

    HashMap<String, String> getSecurityInfoXmls() {
        HashMap<String, String> securityInfoXmls = new HashMap<String, String>();
        String securityInfoUrl = SCAPUtils.getSCAPUtils().getSecurityInfoUrl();
        debug("getSecurityInfoXmls(), securityInfoUrl - " + securityInfoUrl);
        IChannel securityInfoChannel = SCAPUtils.getSCAPUtils().getChannel(securityInfoUrl);
        debug("getSecurityInfoXmls(), securityInfoChannel - " + securityInfoChannel);
        String[] xmlChecksumDetailsArr = securityInfoChannel.getPropertyPairs();
        if (xmlChecksumDetailsArr != null) {
            for (int i = 0; i < xmlChecksumDetailsArr.length; i += 2) {
                String propName = xmlChecksumDetailsArr[i];
                String propValue = xmlChecksumDetailsArr[i + 1];
                if (propName.startsWith("cs.")) {
                    securityInfoXmls.put(new File(propName.substring(3)).getName(), propName.substring(3));
                }
            }
        }
        debug("getSecurityInfoXmls(), securityInfoXmls - " + securityInfoXmls);
        return securityInfoXmls;
    }

    ArrayList<SecurityUpdateDetailsBean> getUpdates() {
        ArrayList<SecurityUpdateDetailsBean> securityUpdateDetailsList = new ArrayList<SecurityUpdateDetailsBean>();
        HashMap<String, ArrayList<SecurityUpdateDetailsBean>> securityUpdateDetailsMap = new HashMap<String, ArrayList<SecurityUpdateDetailsBean>>();
        securityUpdateDetailsMap.put("insync", new ArrayList<SecurityUpdateDetailsBean>());
        securityUpdateDetailsMap.put("failedupdate", new ArrayList<SecurityUpdateDetailsBean>());
        securityUpdateDetailsMap.put("deletedupdate", new ArrayList<SecurityUpdateDetailsBean>());
        securityUpdateDetailsMap.put("newupdate", new ArrayList<SecurityUpdateDetailsBean>());
        securityUpdateDetailsMap.put("existingupdate", new ArrayList<SecurityUpdateDetailsBean>());
        securityUpdateDetailsMap.put("inprogress", new ArrayList<SecurityUpdateDetailsBean>());
        HashMap<String, String> securityInfoXmls = new HashMap<String, String>();
        HashMap<String, String> xmlChecksumDetails = new HashMap<String, String>();
        HashMap<String, String> xmlTimestampDetails = new HashMap<String, String>();
        HashMap<String, String> xmlFailureDetails = new HashMap<String, String>();
        Map<String, String> standardWindowsDetailsMap = new HashMap<String, String>();
        Map<String, String> standardNonwindowsDetailsMap = new HashMap<String, String>();
        Map<String, String> customDetailsMap = new HashMap<String, String>();
        Map<String, String> customWindowsDetailsMap = new HashMap<String, String>();
        Map<String, String> customNonwindowsDetailsMap = new HashMap<String, String>();
        try {
            String securityInfoUrl = SCAPUtils.getSCAPUtils().getSecurityInfoUrl();
            debug("getUpdates(), securityInfoUrl - " + securityInfoUrl);
            IChannel securityInfoChannel = SCAPUtils.getSCAPUtils().getChannel(securityInfoUrl);
            debug("getUpdates(), securityInfoChannel - " + securityInfoChannel);
            standardWindowsDetailsMap = (Map<String, String>) context.getAttribute("usgcbcontentdetailsmap");
            standardNonwindowsDetailsMap = (Map<String, String>) context.getAttribute("scapcontentdetailsmap");
            customDetailsMap = (Map<String, String>) context.getAttribute("customcontentdetailsmap");
            String inProgressXml = securityInfoChannel.getProperty("securityinfo.sync.inprogress.xml");
            debug("getUpdates(), inProgressXml - " + inProgressXml);
            String inProgressCount = securityInfoChannel.getProperty("securityinfo.sync.inprogress.count");
            debug("getUpdates(), inProgressCount - " + inProgressCount);
            String[] xmlChecksumDetailsArr = securityInfoChannel.getPropertyPairs();
            if (xmlChecksumDetails != null) {
                for (int i = 0; i < xmlChecksumDetailsArr.length; i += 2) {
                    String propName = xmlChecksumDetailsArr[i];
                    String propValue = xmlChecksumDetailsArr[i + 1];
                    if (propName.startsWith("cs.")) {
                        securityInfoXmls.put(new File(propName.substring(3)).getName(), propName.substring(3));
                        xmlChecksumDetails.put(new File(propName.substring(3)).getName(), propValue);
                    } else if (propName.startsWith("time.")) {
                        xmlTimestampDetails.put(new File(propName.substring(5)).getName(), propValue);
                    } else if (propName.startsWith("error.")) {
                        xmlFailureDetails.put(new File(propName.substring(9)).getName(), propValue);
                    }
                }
            }
            debug("getUpdates(), xmlChecksumDetails - " + xmlChecksumDetails);
            for (String fileName : xmlChecksumDetails.keySet()) {
                SecurityUpdateDetailsBean securityUpdateDetailsBean = null;
                try {
                    String checksumDetails = xmlChecksumDetails.get(fileName);
                    String channelChecksum, dbChecksum;
                    String title = null, status = null, platform = null, target = null, profileTitles = "", profileIds = "", error = "";
                    if ((checksumDetails != null) && (checksumDetails.indexOf(";") > -1)) {
                        channelChecksum = checksumDetails.split(";")[0];
                        dbChecksum = checksumDetails.split(";")[1];
                        if (channelChecksum.equals(dbChecksum) && (xmlFailureDetails.get(fileName) == null)) {
                            status = "insync";
                        } else {
                            if ((xmlFailureDetails.get(fileName) != null)) {
                                error = xmlFailureDetails.get(fileName);
                                status = "failedupdate";
                            } else {
                                if ((dbChecksum == null) || ("empty".equals(dbChecksum)) || ("".equals(dbChecksum.trim()))) {
                                    status = "newupdate";
                                } else {
                                    if ("deleted".equals(channelChecksum)) {
                                        status = "deletedupdate";
                                    } else {
                                        status = "existingupdate";
                                    }
                                }
                            }
                        }
                    } else {
                        channelChecksum = "empty";
                        dbChecksum = "empty";
                        status = "newupdate";
                    }
                    if ((inProgressXml != null) && fileName.equals(new File(inProgressXml).getName())) {
                        status = "inprogress";
                    }
                    if ("deleted".equals(channelChecksum)) {
                        HashMap<String, String> contentDetails = new GetContentDetails(main, fileName).getContentDetails();
                        title = contentDetails.get("title");
                        target = contentDetails.get("os");
                        platform = "nonwindows".equalsIgnoreCase(target) ? "Non-Windows" : "Windows";
                    } else {
                        if ((standardWindowsDetailsMap != null) && (standardWindowsDetailsMap.get(fileName) != null)) {
                            title = standardWindowsDetailsMap.get(fileName);
                            platform = "Windows";
                            target = "windows";
                        } else if ((standardNonwindowsDetailsMap != null) && (standardNonwindowsDetailsMap.get(fileName) != null)) {
                            title = standardNonwindowsDetailsMap.get(fileName);
                            platform = "Non-Windows";
                            target = "nonwindows";
                        } else if ((customDetailsMap != null) && (customDetailsMap.get(fileName) != null)) {
                            title = customDetailsMap.get(fileName);
                            target = "custom";
                            if (securityInfoXmls.get(fileName) != null) {
                                if ("windows".equals(new File(securityInfoXmls.get(fileName)).getParentFile().getName())) {
                                    platform = "Windows";
                                } else {
                                    platform = "Non-Windows";
                                }
                            } else {
                                platform = "Windows";
                            }
                        } else {
                        }
                        HashMap<String, String> profilesForScapContent = com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils.getSCAPUtils().getProfilesForScapContent(fileName);
                        for (String profileTitle : profilesForScapContent.values()) {
                            profileTitles += profileTitle + ";";
                        }
                        for (String profileId : profilesForScapContent.keySet()) {
                            profileIds += profileId + ";";
                        }
                    }
                    securityUpdateDetailsBean = new SecurityUpdateDetailsBean();
                    securityUpdateDetailsBean.setFileName(fileName);
                    securityUpdateDetailsBean.setTitle(title);
                    securityUpdateDetailsBean.setStatus(status);
                    securityUpdateDetailsBean.setPlatform(platform);
                    String assessmentType = SCAPUtils.getSCAPUtils().getAssessmentType(fileName);
                    securityUpdateDetailsBean.setAssessmentType(assessmentType);
                    if ((xmlTimestampDetails.get(fileName) != null)) {
                        try {
                            securityUpdateDetailsBean.setUpdated(new Date(Long.parseLong(xmlTimestampDetails.get(fileName))).toString());
                        } catch (Throwable t) {
                            securityUpdateDetailsBean.setUpdated("");
                        }
                    } else {
                        securityUpdateDetailsBean.setUpdated("");
                    }
                    securityUpdateDetailsBean.setTarget(target);
                    securityUpdateDetailsBean.setProfileTitles(profileTitles);
                    securityUpdateDetailsBean.setProfileIds(profileIds);
                    securityUpdateDetailsBean.setError(error);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (securityUpdateDetailsBean != null) {
                    securityUpdateDetailsMap.get(securityUpdateDetailsBean.getStatus()).add(securityUpdateDetailsBean);
                }
            }
            securityUpdateDetailsList.addAll(securityUpdateDetailsMap.get("inprogress"));
            securityUpdateDetailsList.addAll(securityUpdateDetailsMap.get("failedupdate"));
            securityUpdateDetailsList.addAll(securityUpdateDetailsMap.get("deletedupdate"));
            securityUpdateDetailsList.addAll(securityUpdateDetailsMap.get("newupdate"));
            securityUpdateDetailsList.addAll(securityUpdateDetailsMap.get("existingupdate"));
            securityUpdateDetailsList.addAll(securityUpdateDetailsMap.get("insync"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        debug("getUpdates(), securityUpdateDetailsList - " + securityUpdateDetailsList);
        return securityUpdateDetailsList;
    }

    void reInitializeScapUtils() {
        Thread scapThread = new Thread() {
            public void run() {
                SCAPUtils.getSCAPUtils().readStandardSCAPContents("scap");
            }
        };
        scapThread.start();
        Thread usgcbThread = new Thread() {
            public void run() {
                SCAPUtils.getSCAPUtils().readStandardSCAPContents("usgcb");
            }
        };
        usgcbThread.start();
        Thread customThread = new Thread() {
            public void run() {
                SCAPUtils.getSCAPUtils().readStandardSCAPContents("custom");
            }
        };
        customThread.start();
    }

    public static class GetContentDetails extends DatabaseAccess {
        SubscriptionMain main = null;
        HashMap<String, String> contentDetails = new HashMap<String, String>();

        public GetContentDetails(SubscriptionMain main, String xmlFileName) {
            debug("GetContentDetails(), xmlFileName - " + xmlFileName);
            GetContentData result = new GetContentData(main, xmlFileName);
            debug("GetContentDetails(), result - " + result);
            try {
                runQuery(result);
                contentDetails = result.getContentDetails();
                debug("GetContentDetails(), contentDetails - " + contentDetails);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public HashMap<String, String> getContentDetails() {
            return contentDetails;
        }
    }

    static class GetContentData extends QueryExecutor {
        String xmlFileName = null;
        HashMap<String, String> contentDetails = new HashMap<String, String>();

        GetContentData(SubscriptionMain main, String xmlFileName) {
            super(main);
            debug("GetContentData(), xmlFileName - " + xmlFileName);
            this.xmlFileName = xmlFileName;
        }

        protected void execute(IStatementPool pool) throws SQLException {
            debug("GetContentData.execute(), pool - " + pool);
            String sql = "select * from security_xccdf_content where content_file_name='" + xmlFileName + "'";
            debug("GetContentData.execute(), sql - " + sql);
            PreparedStatement st = pool.getConnection().prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            try {
                while (rs.next()) {
                    String contentId = "" + rs.getInt("id");
                    String contentName = rs.getString("content_name");
                    String contentTitle = rs.getString("content_title");
                    String contentDesc = rs.getString("content_desc");
                    String contentFileName = rs.getString("content_file_name");
                    String contentTargetOs = rs.getString("content_target_os");
                    contentDetails.put("id", contentId);
                    contentDetails.put("name", contentName);
                    contentDetails.put("title", contentTitle);
                    contentDetails.put("desc", contentDesc);
                    contentDetails.put("fname", contentFileName);
                    contentDetails.put("os", contentTargetOs);
                }
                debug("GetContentData.execute(), contentDetails - " + contentDetails);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                rs.close();
            }
        }

        public HashMap<String, String> getContentDetails() {
            return contentDetails;
        }
    }

    public static void debug(String str) {
        if (DEBUG >= 5) {
            System.out.println("VDeskUpdatesAction.java :: [" + new Date().toString() + "] ==> " + str);
        }
    }
}
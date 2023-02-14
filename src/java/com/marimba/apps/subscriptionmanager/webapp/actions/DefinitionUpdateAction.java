// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Date;
import java.text.ParseException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;

import com.marimba.apps.securitymgr.compliance.util.ChannelCopier;
import com.marimba.apps.securitymgr.compliance.util.Tools;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.tools.config.*;

import com.marimba.tools.util.Props;
import com.sun.jmx.snmp.tasks.Task;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.marimba.intf.util.IConfig;
import com.marimba.intf.msf.*;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.txlisting.*;
import com.marimba.tools.txlisting.TransmitterListing;
import com.marimba.tools.util.Password;
import com.marimba.tools.util.Props;

import com.marimba.tools.config.*;
import com.marimba.apps.securitymgr.compliance.util.DefinitionUpdateHandler;
import com.marimba.apps.subscriptionmanager.webapp.forms.DefinitionUpdateForm;

/**
 * DefinitionUpdateAction
 *  w.r.t handle vDef and CVE JSON update operations
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class DefinitionUpdateAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {


    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {

        return new DefinitionUpdateAction.DefinitionUpdateTask(mapping, form, request, response);

    }

    protected class DefinitionUpdateTask extends SubscriptionDelayedTask {
        DefinitionUpdateForm definitionUpdateForm;
        private ChannelCopier copier;
        static final String PSWDSTR                 = "******";
        static final String BASE64                  = "base64:";
        HttpServletRequest request;
        HttpServletResponse response;
        Locale locale;
        ActionMapping mapping;
        String action;
        String actionString;
        ConfigProps config = null;

        DefinitionUpdateTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
            this.definitionUpdateForm = (DefinitionUpdateForm) form;
            this.request = request;
            this.mapping = mapping;
            this.response = response;
            this.locale = request.getLocale();
        }


        public void execute() {
            init(request);
            actionString = request.getParameter("action");
            String definitionUpdateResponse = null;

            try {
                if (isEmpty(action)) {
                    initDefinitionsUpdateConfig();
                    loadFormData(getDefinitionsUpdateConfig(), definitionUpdateForm);
                }
                action = definitionUpdateForm.getAction();
                System.out.println("DebugInfo: DefinitionUpdate - Action: " + action);

                if ("update_vdef".equals(action)) {
                    String  masterTxUrl = definitionUpdateForm.getPublishTxUrl();
                    String  pubUser = definitionUpdateForm.getPublishUserName();
                    String  pubPwd = definitionUpdateForm.getPublishPassword();
                    String  chstoreUser = definitionUpdateForm.getChannelStoreUserName();
                    String  chstorePwd = definitionUpdateForm.getChannelStorePassword();

                    System.out.println("DebugInfo: Master Tx Url: " + masterTxUrl);
                    System.out.println("DebugInfo: Publish UserName: " + pubUser);
                    System.out.println("DebugInfo: Publish Password: " + pubPwd);

                    initDefinitionsUpdateConfig();
                    ConfigProps config = getDefinitionsUpdateConfig();
                    if (config != null) {
                        config.setProperty("destination.mastertx.url", masterTxUrl);
                        config.setProperty("publish.tx.user", pubUser);
                        config.setProperty("publish.tx.password", pubPwd);
                        config.save();
                    }
                    String srcUrl = config.getProperty("products.mastertx.url");
                    String dstUrl = masterTxUrl + "/DefenSight/vDef";
                    boolean copyFailed = executeVdefChannelCopy(srcUrl, dstUrl, pubUser, pubPwd);
                    if (!copyFailed) {
                        long currentTimestampVal = System.currentTimeMillis();
                        java.util.Date date = new java.util.Date(currentTimestampVal);
                        if (locale == null) {
                            locale = Locale.getDefault();
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm:ss z", locale);
                        String dateVal = dateFormat.format(date);
                        config.setProperty("vdefchannel.lastcopied.timestamp", dateVal);

                        System.out.println("LogInfo: vdefchannel.lastcopied.timestamp ==> "+dateVal);
                        System.out.println("LogInfo: Copy vDef channel from src=" +srcUrl +
                                " to dst=" + dstUrl + " : succeeded");

                        definitionUpdateForm.setVdefLastUpdated(dateVal);
                        config.save();
                        config.close();
                        System.out.println("vDef channel copy operation succeeded from Products Tx..");
                    } else {
                        System.out.println("vDef channel copy operation failed from Products Tx..");
                    }
                    setFormData(definitionUpdateForm);

                    forward = mapping.findForward("view");

                } else {
                    System.out.println("START - fetch definitions update metadata ");
                    DefinitionUpdateHandler definitionUpdateHandler = new DefinitionUpdateHandler(main);
                    definitionUpdateResponse = definitionUpdateHandler.getMetaData();
                    definitionUpdateForm.setDefinationUpdateResponse(definitionUpdateResponse);
                    System.out.println("END - fetch definitions update metadata "+definitionUpdateResponse.toString());
                    forward = mapping.findForward("view");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            forward = mapping.findForward("view");
        }


        private ConfigProps getDefinitionsUpdateConfig() {
            return config;
        }

        private ChannelCopier getCopier(String user, String pwd) {
            String certId = null;
            String certPwd = null;
            IApplicationContext iApplicationContext = (IApplicationContext) main.getFeatures().getChild("context");
            ChannelCopier copier = new ChannelCopier(main, user, pwd, certId, certPwd, iApplicationContext);
            return copier;
        }

        private boolean executeVdefChannelCopy(String srcUrl, String dstUrl, String pubUser, String pubPwd) {
            boolean failed = true;
            try {
                URL src = new URL(srcUrl);
                URL dst = new URL(dstUrl);
                Props props = new Props();
                String user = pubUser;
                String pwd = pubPwd;
                copier = getCopier(user, pwd);
                if (copier == null) {
                    System.out.println("LogInfo: Unable to get the copier instance...");
                    return failed;
                }
                if (copier.copyChannel(props, src, dst) == 0) {
                    failed = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                failed = true;
            }
            return failed;
        }

        private void loadFormData(ConfigProps config, DefinitionUpdateForm defnForm) {
            if (config != null) {
                defnForm.setPublishTxUrl(config.getProperty("destination.mastertx.url"));
                defnForm.setPublishUserName(config.getProperty("publish.tx.user"));
                defnForm.setPublishPassword(config.getProperty("publish.tx.password"));
                defnForm.setVdefLastUpdated(config.getProperty("vdefchannel.lastcopied.timestamp"));
            }
        }

        private void initDefinitionsUpdateConfig() {
            File rootDir = main.getDataDirectory();
            System.out.println("Root dir :" + rootDir.getAbsolutePath());
            if (rootDir != null && rootDir.isDirectory()) {
                File configFile = new File(rootDir, "definitions_update_config.txt");
                try {
                    if (!configFile.exists()) {
                        config = new ConfigProps(configFile);
                        config.setProperty("products.mastertx.url", "http://marimbastaging.harman.com/Clarinet/m9004f/Current/vDef");
                        config.setProperty("destination.mastertx.url", "");
                        config.setProperty("publish.tx.user", "");
                        config.setProperty("publish.tx.password", "");
                        config.setProperty("channelstore.username", "");
                        config.setProperty("channelstore.password", "");
                        if (!config.save()) {
                            throw new Exception("Failed to save mitigate configurations");
                        }
                        config.close();
                    } else {
                        config = new ConfigProps(configFile);
                    }
                } catch (Exception ioe) {
                    ioe.printStackTrace();
                    // REMIND, log something here
                    config = null;
                }
            }
        }

        private String encode(String passwd) {
            return BASE64 + Password.encode(passwd);
        }

        private boolean isEncoded(String passwd) {
            return (passwd != null && passwd.startsWith(BASE64));
        }

        private void setFormData(DefinitionUpdateForm definitionUpdateForm) {
            definitionUpdateForm.setPublishTxUrl(definitionUpdateForm.getPublishTxUrl());
            definitionUpdateForm.setPublishUserName(definitionUpdateForm.getPublishUserName());
            definitionUpdateForm.setPublishUserName(definitionUpdateForm.getPublishPassword());
            definitionUpdateForm.setVdefLastUpdated(definitionUpdateForm.getVdefLastUpdated());
        }

        public String getWaitMessage() {
            locale = (locale == null) ? Locale.getDefault():locale;
            if ("update_vdef".equals(action)) {
                return resources.getMessage(locale, "page.definitions_update.vdef_copy_operation.waitForCompletion.PleaseWait");
            } else if ("cvejson_download".equals(action)) {
                return resources.getMessage(locale, "page.definitions_update.cvejson_download_operation.waitForCompletion.PleaseWait");
            } else if ("cvejson_csvupdate".equals(action)) {
                return resources.getMessage(locale, "page.definitions_update.cvejson_csvupdate_operation.waitForCompletion.PleaseWait");
            }
            return resources.getMessage(locale, "page.waitForCompletion.PleaseWait");
        }

        /**
         *
         * @param response
         * @param jsonData
         * @throws Exception
         */
        protected void sendJSONResponse(HttpServletResponse response, String jsonData) throws Exception {
            PrintWriter out = response.getWriter();
            out.print(jsonData);
            out.flush();
        }

    }
}

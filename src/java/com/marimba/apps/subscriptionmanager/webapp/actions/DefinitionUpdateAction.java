// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.*;
import java.net.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import java.text.ParseException;

import com.marimba.apps.securitymgr.compliance.DashboardHandler;
import  com.marimba.apps.subscriptionmanager.webapp.util.defensight.CVEDataInsertionUtil;
import com.marimba.apps.securitymgr.compliance.util.*;
import com.marimba.intf.application.*;
import com.marimba.intf.castanet.*;
import com.marimba.intf.util.IConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ICveUpdateConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;

import com.marimba.apps.securitymgr.utils.json.JSONArray;
import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.IWorkspace;
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
import com.marimba.apps.subscriptionmanager.webapp.forms.DefinitionUpdateForm;

/**
 * DefinitionUpdateAction w.r.t handle vDef and CVE JSON update operations
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class DefinitionUpdateAction extends AbstractAction implements IWebAppConstants,
    ISubscriptionConstants, ICveUpdateConstants {

  protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) {

    return new DefinitionUpdateAction.DefinitionUpdateTask(mapping, form, request, response);

  }

  protected class DefinitionUpdateTask extends SubscriptionDelayedTask {

    DefinitionUpdateForm definitionUpdateForm;
    private ChannelCopier copier;
    private IWorkspace ws;
    static final String PSWDSTR = "******";
    static final String BASE64 = "base64:";

    static final String CVE_CREATE_SQL = "create-tables.sql";
    static final String CVE_UPDATE_SQL = "update-tables.sql";
    static final String CVE_RUN_SQL = "run.sql";
    static final String cvejsonZipFile = "circl-cve-search-expanded.json.gz";
    static final String cvejsonFile = "circl-cve-search-expanded.json";

    HttpServletRequest request;
    HttpServletResponse response;
    Locale locale;
    ActionMapping mapping;
    String action;
    String actionString;
    String cveDownloderChPath;
    ConfigProps config = null;
    IConfig tunerConfig = null;
    CveUpdateUtil cveupdateObj = null;

    DefinitionUpdateTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,
        HttpServletResponse response) {
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

        action = definitionUpdateForm.getAction();
        System.out.println("DebugInfo: DefinitionUpdate - Action: " + action);

        if (isEmpty(action)) {
          initDefinitionsUpdateConfig();
          loadFormData(getDefinitionsUpdateConfig(), definitionUpdateForm);
        }


        if ("getCveUpdateStatus".equals(action)) {
          initDefinitionsUpdateConfig();
          config = getDefinitionsUpdateConfig();

          String status = config.getProperty("cvejsonupdate.process.status");
          String error = config.getProperty("cvejsonupdate.process.error");
          status = isNull(status) ? "0" : status;

          JSONObject json = new JSONObject();
          json.put("status", Integer.valueOf(status));
          json.put("error", error);
          //System.out.println("Response : " + json.toString());
          sendJSONResponse(response, json.toString());

        } else if ("checkThreadStatus".equals(action)) {

          Set<Thread> threads = Thread.getAllStackTraces().keySet();
          for (Thread t : threads) {
            if (t.getName() == "cveJsonUpdateThread") {
              JSONObject json = new JSONObject();
              json.put("cveJsonUpdateThread", true);
              sendJSONResponse(response, json.toString());

              return;
            }
          }

          JSONObject json = new JSONObject();
          json.put("cveJsonUpdateThread", false);
          sendJSONResponse(response, json.toString());

          return;

        } else if ("update_vdef".equals(action)) {
          String masterTxUrl = definitionUpdateForm.getPublishTxUrl();
          String pubUser = definitionUpdateForm.getPublishUserName();
          String pubPwd = definitionUpdateForm.getPublishPassword();
          String chstoreUser = definitionUpdateForm.getChannelStoreUserName();
          String chstorePwd = definitionUpdateForm.getChannelStorePassword();

          initDefinitionsUpdateConfig();
          ConfigProps config = getDefinitionsUpdateConfig();

          if (pubPwd != null && (!pubPwd.startsWith(PSWDSTR))) {
            pubPwd = encode(pubPwd);
          } else {
            pubPwd = config.getProperty("publish.tx.password");
          }

          if (chstorePwd != null && (!chstorePwd.startsWith(PSWDSTR))) {
            chstorePwd = encode(chstorePwd);
          } else {
            chstorePwd = config.getProperty("channelstore.authenticate.password");
          }

          if (config != null) {
            config.setProperty("destination.mastertx.url", masterTxUrl);
            config.setProperty("publish.tx.user", pubUser);
            config.setProperty("publish.tx.password", pubPwd);
            config.setProperty("channelstore.authenticate.user", chstoreUser);
            config.setProperty("channelstore.authenticate.password", chstorePwd);
            config.save();
          }

          // channel store credentials authenticate validation on products tx.
          boolean csAuthStatus = false;
          String errArg = "";

          try {
            IApplicationContext context = (IApplicationContext) main.getFeatures()
                .getChild("context");
            ChannelStoreAuthenticateEngine cse = new ChannelStoreAuthenticateEngine();
            cse.init(context);
            chstoreUser = config.getProperty("channelstore.authenticate.user");
            chstorePwd = config.getProperty("channelstore.authenticate.password");

            if (isEncoded(chstorePwd)) {
              chstorePwd = Password.decode(chstorePwd);
            }
            try {
              csAuthStatus = cse.authenticateUser(chstoreUser, chstorePwd);
              System.out.println("LogInfo: Channel Store Authentication Status: " + csAuthStatus);
            } catch (Exception ex) {
              System.out.println(
                  "LogInfo: Error Occurred while validate CS credentials - " + ex.getMessage());
              errArg = getString(locale,
                  "page.definitions_update.error.invalid.channelstore.creds");
              ex.printStackTrace();
            }

            if (!csAuthStatus) {
              definitionUpdateForm.setCveJsonUpdateError(
                  "Channel Store Authentication Failed - Invalid Credentials");
              errArg = getString(locale,
                  "page.definitions_update.error.invalid.channelstore.creds");
              config.setProperty("channelstore.authentication.succeeded", "false");
              System.out.println(
                  "LogInfo: Channel Store Authentication failed - " + "user rejected!");
            }
            if (csAuthStatus) {
              config.setProperty("channelstore.authentication.succeeded", "true");
              System.out.println(
                  "LogInfo: Channel Store Authentication succeeded - " + "user validated!");
            }
            config.save();
          } catch (Exception ex) {
            ex.printStackTrace();
          }

          String srcUrl = config.getProperty("products.mastertx.url");
          String dstUrl = masterTxUrl + "/DefenSight/vDef";
          vDefCopyUtil vdefcopyObj = new vDefCopyUtil();
          vdefcopyObj.init(main, config);
          if (isEncoded(pubPwd)) {
            pubPwd = Password.decode(pubPwd);
          }

          boolean copyFailed = true;
          if (csAuthStatus) {
            copyFailed = vdefcopyObj.executeVdefChannelCopy(srcUrl, dstUrl, pubUser, pubPwd);
            errArg = "";
          }

          if (!copyFailed) {
            long currentTimestampVal = System.currentTimeMillis();
            java.util.Date date = new java.util.Date(currentTimestampVal);
            if (locale == null) {
              locale = Locale.getDefault();
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm:ss z", locale);
            String dateVal = dateFormat.format(date);
            config.setProperty("vdefchannel.lastcopied.timestamp", dateVal);
            config.setProperty("vdefchannel.copy.error", "");

            System.out.println("LogInfo: vdefchannel.lastcopied.timestamp ==> " + dateVal);
            System.out.println("LogInfo: Copy vDef channel from src=" + srcUrl + " to dst=" + dstUrl
                + " : succeeded");

            definitionUpdateForm.setVdefLastUpdated(dateVal);
            config.save();
            config.close();
            System.out.println("LogInfo: vDef channel copy operation succeeded from Products Tx..");
            definitionUpdateForm.setvDefError("");
          } else {
            System.out.println("LogInfo: vDef channel copy operation failed from Products Tx.");
            config.setProperty("vdefchannel.copy.error",
                getString(locale, "page.definition_update.copy.operation.failed", errArg));
            config.save();
            config.close();
          }
          loadFormData(getDefinitionsUpdateConfig(), definitionUpdateForm);
          //setFormData(definitionUpdateForm);
          forward = mapping.findForward("view");

        } else if ("update_cvejson".equals(action)) {

          Runnable update_cvejson_runnable = new Runnable() {

            public void run() {

              Thread thread = Thread.currentThread();
              System.out.println(
                  "START THREAD: RunnableJob is being run by " + thread.getName() + " ("
                      + thread.getId() + ")");
              initDefinitionsUpdateConfig();
              cveupdateObj = new CveUpdateUtil(main, getDefinitionsUpdateConfig());

              ConfigProps config = getDefinitionsUpdateConfig();

              int updateCvejsonStartStep = definitionUpdateForm.getUpdateCvejsonStartStep();
              //System.out.println("updateCvejsonStartStep = " + updateCvejsonStartStep);
              String cveStorageDir = definitionUpdateForm.getCveStorageDir();

              Path path = Paths.get(cveStorageDir);
              if (!Files.exists(path)) {
                try {
                  Files.createDirectory(path);
                  setUpdateCveStatus(config, "0");
                } catch (Exception e) {
                  System.out.println(
                      "An error occurred while creating the directory: " + e.getMessage());
                  setUpdateCveStatus(config, "0", "CVE download location is invalid");
                  return;
                }
              }
              
              tunerConfig = (IConfig) features.getChild("tunerConfig");
              Tools.setTunerConfig(tunerConfig);
              String tunerInstallDir = tunerConfig.getProperty("marimba.tuner.install.dir");

              if (updateCvejsonStartStep < 2) {

                config.setProperty("cvejsonupdate.process.thread", "" + thread.getId());
                setUpdateCveStatus(config, "1");

                // String actionString = "cvejson_download";
                initDefinitionsUpdateConfig();
                config = getDefinitionsUpdateConfig();

                String urlStr = config.getProperty("defensight.cvejson.downloadurl");
                urlStr = (isNull(urlStr))
                    ? "https://cve.circl.lu/static/circl-cve-search-expanded.json.gz" : urlStr;

                try {
                  if (!isNull(cveStorageDir)) {
                    File cvejsonDir = new File(cveStorageDir);
                    if (!cvejsonDir.exists()) {
                      cvejsonDir.mkdirs();
                    }
                    config.setProperty("defensight.cvejson.storagedir.location", cveStorageDir);
                    config.save();
                    config.close();
                  }

                  CveUpdateUtil cveupdateObj = new CveUpdateUtil(main,
                      getDefinitionsUpdateConfig());
                  boolean downloadfailed = cveupdateObj.downloadCVEJSON(urlStr, 5, cvejsonZipFile);
                  if (!downloadfailed) {
                    System.out.println("CVE JSON Zip File Download Succeeded...");
                  } else {
                    System.out.println("CVE JSON Zip File Download Failed..");
                    setUpdateCveStatus(config, "1", "CVE JSON Zip File Download Failed..");
                  }
                } catch (Exception ex) {
                  ex.printStackTrace();
                  setUpdateCveStatus(config, "1", "CVE JSON Zip File Download Failed..");
                }

              }

              if (updateCvejsonStartStep < 3) {

                setUpdateCveStatus(config, "2");

                try {

                  File jsonZipFile = new File(cveStorageDir, cvejsonZipFile);
                  System.out.println(
                      "DebugInfo: CVE JSON ZipFile Path ==> " + jsonZipFile.getCanonicalPath());
                  String cveDir = getDefinitionsUpdateConfig().getProperty(
                      "defensight.cvejson.storagedir.location");
                  tunerConfig.setProperty("cvedownloader.storage.directory", cveStorageDir);
                  File unzipDst = new File(cveDir, cvejsonFile);

                  actionString = "cvejson_unzip";
                  Tools.gunzip(jsonZipFile, unzipDst);

                } catch (Exception ex) {
                  ex.printStackTrace();
                  setUpdateCveStatus(config, "2", "Failed to unzip..");
                }
              }
              
              
              if(updateCvejsonStartStep < 4) {
            	  
            	  setUpdateCveStatus(config, "3");
            	  
            	  try {
	            	  
	                  String cveDir = getDefinitionsUpdateConfig().getProperty(
	                          "defensight.cvejson.storagedir.location");
	              	
	                  //Read the data from json file and insert it into DB
	                  CVEDataInsertionUtil cveDataInsertionUtil = new CVEDataInsertionUtil();
	                  cveDataInsertionUtil.insertBulkData(main, prepareJsonFilePath(cveDir, cvejsonFile));
	        	  } catch (Exception ex) {
	                  ex.printStackTrace();
	                  setUpdateCveStatus(config, "3", "Failed to insert data..");
	        	  }
              }


              if (updateCvejsonStartStep < 5) {

            	setUpdateCveStatus(config, "4");

                try {

                  IApplicationContext iappContext = (IApplicationContext) main.getFeatures()
                      .getChild("context");

                  // make sure with insert JSON to DB insert confirm by above code
                  // (cve_info with child tables)
                  boolean dbInsertDone = true;
                  File scriptDir = new File(cveStorageDir, "sqlscripts");
                  boolean fileStatus = cveupdateObj.makeCveSchemaSqlScripts(scriptDir);
                  System.out.println(
                      "DebugInfo: CVE SQL <update.sql> created and updated successfully..."
                          + fileStatus);
                  
                  if(!fileStatus) {
                	  setUpdateCveStatus(config, "4", "Failed to create scripts...");
                	  return;
                  }
                                   
                  if (dbInsertDone) {
                    System.out.println(
                        "DebugInfo: CVE JSON update bulk insertion into DefenSight Database - SUCCEEDED");

                    String sqlscriptsDirPath = scriptDir.getCanonicalPath() + "\\" + CVE_UPDATE_SQL;
                    RunSQLScript rsScript = new RunSQLScript(sqlscriptsDirPath, main);
                    if (rsScript.getStatus()) {
                      System.out.println(
                          "DebugInfo: CVE JSON update-tables.sql into DefenSight Database - SUCCEEDED");
                    } else {
                  	  setUpdateCveStatus(config, "4", "Failed to update tables...");
                  	  return;
                    }

                    long currentTimestampVal = System.currentTimeMillis();
                    java.util.Date date = new java.util.Date(currentTimestampVal);
                    if (locale == null) {
                      locale = Locale.getDefault();
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy, HH:mm:ss z",
                        locale);
                    String dateVal = dateFormat.format(date);

                    config = getDefinitionsUpdateConfig();
                    config.setProperty("defensight.cvejson.lastupdated.timestamp", dateVal);
                    setUpdateCveStatus(config, "5");
                    System.out.println(
                        "LogInfo: defensight.cvejson.lastupdated.timestamp ==> " + dateVal);
                    definitionUpdateForm.setCveJsonLastUpdated(dateVal);
                  } else {
                    System.out.println(
                        "DebugInfo: CVE JSON update into DefenSight Database - FAILED");
                    
                    setUpdateCveStatus(config, "4", "CVE JSON update into DefenSight Database - FAILED");
                  }
                } catch (Exception ex) {
                  ex.printStackTrace();
                  setUpdateCveStatus(config, "4", "CVE JSON update into DefenSight Database - FAILED");
                }

                setFormData(definitionUpdateForm);
                forward = mapping.findForward("view");
                System.out.println(
                    "END THREAD: RunnableJob is being run by " + thread.getName() + " ("
                        + thread.getId() + ")");
              }}

          };

          Set<Thread> threads = Thread.getAllStackTraces().keySet();
          for (Thread t : threads) {
            if (t.getName() == "cveJsonUpdateThread") {
              System.out.printf("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority",
                  "isDaemon");
              System.out.printf("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(),
                  t.getPriority(), t.isDaemon());
              JSONObject json = new JSONObject();
              json.put("cveJsonUpdateThread", true);
              sendJSONResponse(response, json.toString());

              return;
            }
          }

          System.out.println("Creating thread with name cveJsonUpdateThread");
          Thread cveJsonUpdateThread = new Thread(update_cvejson_runnable);
          cveJsonUpdateThread.setName("cveJsonUpdateThread");
          cveJsonUpdateThread.start();

          JSONObject json = new JSONObject();
          json.put("cveJsonUpdateThread", false);
          sendJSONResponse(response, json.toString());

          return;

        } else {

          System.out.println("START - fetch definitions update metadata ");
          DefinitionUpdateHandler definitionUpdateHandler = new DefinitionUpdateHandler(main);
          definitionUpdateResponse = definitionUpdateHandler.getMetaData();
          definitionUpdateForm.setDefinationUpdateResponse(definitionUpdateResponse);
          System.out.println(
              "END - fetch definitions update metadata " + definitionUpdateResponse.toString());
          forward = mapping.findForward("view");
        }

      } catch (Exception ex) {
        ex.printStackTrace();
      }
      forward = mapping.findForward("view");
    }
    
    private void setUpdateCveStatus(ConfigProps config, String status, String error) {
		config.setProperty("cvejsonupdate.process.status", status);
	    config.setProperty("cvejsonupdate.process.error", error);
	    config.save();
	    config.close();
	    loadFormData(config, definitionUpdateForm);
    }
    
    private void setUpdateCveStatus(ConfigProps config, String status) {
		config.setProperty("cvejsonupdate.process.status", status);
	    config.setProperty("cvejsonupdate.process.error", "");
	    config.save();
	    config.close();
	    loadFormData(config, definitionUpdateForm);
    }

    private String prepareJsonFilePath(String cveDir, String cvejsonFile) {
      String jsonFilePath = cveDir + "\\" + cvejsonFile;
      return jsonFilePath;
    }
    private ConfigProps getDefinitionsUpdateConfig() {
      return config;
    }

    private void loadFormData(ConfigProps config, DefinitionUpdateForm defnForm) {
      if (config != null) {
        String txPwd = config.getProperty("publish.tx.password");
        if (isEncoded(txPwd)) {
          txPwd = txPwd.substring(BASE64.length());
          txPwd = Password.decode(txPwd);
        }
        String chStorePwd = config.getProperty("channelstore.authenticate.password");
        if (isEncoded(chStorePwd)) {
          chStorePwd = chStorePwd.substring(BASE64.length());
          chStorePwd = Password.decode(chStorePwd);
        }
        defnForm.setPublishTxUrl(config.getProperty("destination.mastertx.url"));
        defnForm.setPublishUserName(config.getProperty("publish.tx.user"));
        defnForm.setPublishPassword(PSWDSTR);

        defnForm.setChannelStoreUserName(config.getProperty("channelstore.authenticate.user"));
        defnForm.setChannelStorePassword(PSWDSTR);

        defnForm.setVdefLastUpdated(config.getProperty("vdefchannel.lastcopied.timestamp"));
        defnForm.setvDefError(config.getProperty("vdefchannel.copy.error"));

        defnForm.setCveStorageDir(config.getProperty("defensight.cvejson.storagedir.location"));
        defnForm.setCveJsonLastUpdated(
            config.getProperty("defensight.cvejson.lastupdated.timestamp"));

        int stepUpdate = isNull(config.getProperty("cvejsonupdate.process.status")) ? 0
            : Integer.valueOf(config.getProperty("cvejsonupdate.process.status"));
        defnForm.setCveJsonUpdateStep(stepUpdate);
        defnForm.setCveJsonUpdateError(config.getProperty("cvejsonupdate.process.error"));

        boolean isThreadRunning = false;
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        for (Thread t : threads) {
          if (t.getName() == "cveJsonUpdateThread") {
            isThreadRunning = true;
            break;
          }
        }
        defnForm.setCveJsonUpdateThreadRunning(isThreadRunning);
      }
    }

    private void initDefinitionsUpdateConfig() {
      File rootDir = main.getDataDirectory();
      if (rootDir != null && rootDir.isDirectory()) {
        File configFile = new File(rootDir, "definitions_update_config.txt");
        try {
          String cvedownloaderUrl = main.getConfig()
              .getProperty("subscriptionmanager.cvedownloader.url");

          if (!configFile.exists()) {
            config = new ConfigProps(configFile);
            config.setProperty("products.mastertx.url",
                "http://marimbastaging.harman.com/Clarinet/m9004f/Current/vDef");
            config.setProperty("destination.mastertx.url", "");
            config.setProperty("publish.tx.user", "");
            config.setProperty("publish.tx.password", "");
            config.setProperty("channelstore.authenticate.user", "");
            config.setProperty("channelstore.authenticate.password", "");
            config.setProperty("defensight.cvejson.downloadurl",
                "https://cve.circl.lu/static/circl-cve-search-expanded.json.gz");
            config.setProperty("defensight.cvedownloaderchannel.location", cvedownloaderUrl);
            config.setProperty("vdefchannel.lastcopied.timestamp", "Not Updated");
            config.setProperty("defensight.cvejson.lastupdated.timestamp", "Not Updated");
            config.setProperty("cvejsonupdate.process.status", "0");
            config.setProperty("cvejsonupdate.process.error", "");
            config.setProperty("vdefchannel.copy.error", "");
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
      definitionUpdateForm.setCveStorageDir(definitionUpdateForm.getCveStorageDir());
    }

    public String getWaitMessage() {
      locale = (locale == null) ? Locale.getDefault() : locale;
      if ("update_vdef".equals(action)) {
        return resources.getMessage(locale,
            "page.definitions_update.vdef_copy_operation.waitForCompletion.PleaseWait");
      } /*
       * else if ("cvejson_download".equals(actionString)) { return
       * resources.getMessage(locale,
       * "page.definitions_update.cvejson_download_operation.waitForCompletion.PleaseWait"
       * ); } else if ("cvejson_unzip".equals(actionString)) { return
       * resources.getMessage(locale,
       * "page.definitions_update.cvejson_unzip_operation.waitForCompletion.PleaseWait"
       * ); } else if ("cvejson_csvgenerate".equals(actionString)) { return
       * resources.getMessage(locale,
       * "page.definitions_update.cvejson_csvupdate_operation.waitForCompletion.PleaseWait"
       * ); } else if ("csvdata_sqldbupdate".equals(actionString)) { return
       * resources.getMessage(locale,
       * "page.definitions_update.cvejsoncsv_into_dbupdate_operation.waitForCompletion.PleaseWait"
       * ); }
       */
      return resources.getMessage(locale, "page.waitForCompletion.PleaseWait");
    }

    /**
     * @param response
     * @param jsonData
     * @throws Exception
     */
    protected void sendJSONResponse(HttpServletResponse response, String jsonData)
        throws Exception {
      PrintWriter out = response.getWriter();
      out.print(jsonData);
      out.flush();
    }

  }
}

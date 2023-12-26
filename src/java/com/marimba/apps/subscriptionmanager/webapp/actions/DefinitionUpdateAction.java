// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.intf.util.IDirectory;
import com.marimba.tools.util.DebugFlag;
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
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.CVEDataInsertionUtil;
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
import com.marimba.apps.securitymgr.compliance.util.CveUpdateUtil;

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

  protected static int DEBUG = DebugFlag.getDebug("DEFEN/ERROR");

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
    ConfigProps config = null;
    IConfig tunerConfig = null;
    CveUpdateUtil cveupdateObj = null;

    boolean isBulkInserted = false;

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
        info("DefinitionUpdate - Action:" + action);

        // Initialize tuner config
        tunerConfig = getTunerConfig();
        if (tunerConfig == null) {
          tunerConfig = (IConfig) main.getFeatures().getChild("tunerConfig");
        }

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

          String prouctMastertxURL = tunerConfig.getProperty("products.mastertx.url");

          if (config != null) {
            config.setProperty("destination.mastertx.url", masterTxUrl);
            if (!isNull(prouctMastertxURL)) {
              config.setProperty("products.mastertx.url", prouctMastertxURL);
            }
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
              info("LogInfo: Channel Store Authentication Status: " + csAuthStatus);
            } catch (Exception ex) {
              error("Error Occurred while validate CS credentials" + ex.getMessage());
              errArg = getString(locale,
                  "page.definitions_update.error.invalid.channelstore.creds");
              if (DEBUG >= 5) {
                ex.printStackTrace();
              }
            }

            if (!csAuthStatus) {
              definitionUpdateForm.setCveJsonUpdateError(
                  "Channel Store Authentication Failed - Invalid Credentials");
              errArg = getString(locale,
                  "page.definitions_update.error.invalid.channelstore.creds");
              config.setProperty("channelstore.authentication.succeeded", "false");
              error("Channel Store Authentication failed");
            }
            if (csAuthStatus) {
              config.setProperty("channelstore.authentication.succeeded", "true");
              info("Channel Store Authentication succeeded");
            }
            config.save();
          } catch (Exception ex) {
            error(ex.getMessage());
            if (DEBUG >= 5) {
              ex.printStackTrace();
            }
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

            info("vDef channel last copied timestamp :" + dateVal);
            info("Copy vDef channel from " + srcUrl + " to " + dstUrl + " is succeed");

            definitionUpdateForm.setVdefLastUpdated(dateVal);
            config.save();
            config.close();

            info("vDef channel copy operation succeeded from Products Transmitter");
            definitionUpdateForm.setvDefError("");
          } else {
            error("vDef channel copy operation failed from Products Transmitter");
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
              info("START THREAD: RunnableJob is being run by " + thread.getName() + " ("
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
                  setUpdateCveStatus(config, "0", true);
                } catch (Exception e) {
                  error("An error occurred while creating the directory:" + e.getMessage());
                  setUpdateCveStatus(config, "0", "CVE download location is invalid", true);
                  return;
                }
              }

              tunerConfig = (IConfig) features.getChild("tunerConfig");
              Tools.setTunerConfig(tunerConfig);

              if (updateCvejsonStartStep < 2) {

                config.setProperty("cvejsonupdate.process.thread", "" + thread.getId());
                setUpdateCveStatus(config, "1", true);

                initDefinitionsUpdateConfig();
                config = getDefinitionsUpdateConfig();

                info("Downloading the CVE JSON Zip File.");
                String urlStr = config.getProperty("defensight.cvejson.downloadurl");
                urlStr = (isNull(urlStr))
                    ? "https://cve.circl.lu/static/circl-cve-search-expanded.json.gz" : urlStr;
                try {
                  if (!isNull(cveStorageDir)) {
                    File cvejsonDir = new File(cveStorageDir);
                    if (!cvejsonDir.exists()) {
                      cvejsonDir.mkdirs();
                    }
                  }

                  config.setProperty("defensight.cvejson.storagedir.location", cveStorageDir);
                  config.save();
                  config.close();

                  CveUpdateUtil cveupdateObj = new CveUpdateUtil(main,
                      getDefinitionsUpdateConfig());

                /*  boolean downloadStatus = cveupdateObj.downloadCVEJSON(urlStr, 5, cvejsonZipFile);
                  if (!downloadStatus) {
                    info("CVE JSON Zip File Download Succeeded");
                  } else {
                    setUpdateCveStatus(config, "1", "Failed to connect, please try after sometime",
                        true);
                    return;
                  }*/
                } catch (Exception ex) {
                  error(ex.getMessage());
                  if (DEBUG >= 5) {
                    ex.printStackTrace();
                  }
                  setUpdateCveStatus(config, "1", "Failed to connect, please try after sometime",
                      true);
                }

              }

              if (updateCvejsonStartStep < 3) {

                setUpdateCveStatus(config, "2", true);

                try {

                  File jsonZipFile = new File(cveStorageDir, cvejsonZipFile);

                  info("Downloaded CVE JSON Zip file at " + jsonZipFile.getCanonicalPath());
                  String cveDir = getDefinitionsUpdateConfig().getProperty(
                      "defensight.cvejson.storagedir.location");
                  File unzipDst = new File(cveDir, cvejsonFile);

                  actionString = "cvejson_unzip";
                  info("Unzipping the CVE JSON Zip file");
                  // Tools.gunzip(jsonZipFile, unzipDst);
                  info("File Unzipped successfully");

                } catch (Exception ex) {
                  error(ex.getMessage());
                  if (DEBUG >= 5) {
                    ex.printStackTrace();
                  }
                  setUpdateCveStatus(config, "2", "Failed to unzip..", true);
                }
              }

              if (updateCvejsonStartStep < 4) {
                setUpdateCveStatus(config, "3", true);
                try {

                  String cveDir = getDefinitionsUpdateConfig().getProperty(
                      "defensight.cvejson.storagedir.location");

                  String cvejsonFilePath = prepareJsonFilePath(cveDir, cvejsonFile);
                  File cveJsonFile = new File(cvejsonFilePath);

                  //if (!cveJsonFile.exists()) {
                  // setUpdateCveStatus(config, "3", "Failed to insert data..", true);
                  // }

                  //Read the data from json file and insert it into DB
                  info("Started process of Data insertion into DB.");
                  CVEDataInsertionUtil cveDataInsertionUtil = new CVEDataInsertionUtil();

                  isBulkInserted = cveDataInsertionUtil.insertBulkData(main, cvejsonFilePath,
                      config);

                  if (!isBulkInserted) {
                    error("CVE JSON update into DefenSight Database - FAILED");
                    setUpdateCveStatus(config, "3", "Failed to insert data..", false);
                    return;
                  }

                } catch (Exception ex) {
                  error("CVE JSON update into DefenSight Database - FAILED,  Error Message : "
                      + ex.getMessage());

                  if (DEBUG >= 5) {
                    ex.printStackTrace();
                  }
                  setUpdateCveStatus(config, "3", "Failed to insert data..", false);
                }
                info("Data insertion into Database : SUCCEED");
              }

              if (updateCvejsonStartStep < 5) {

                info("Updating the severity status");

                setUpdateCveStatus(config, "4", false);
                try {
                  IApplicationContext iappContext = (IApplicationContext) main.getFeatures()
                      .getChild("context");
                  Path sqlScript = Paths.get(cveStorageDir + "\\sqlscripts");
                  if (!Files.exists(sqlScript)) {
                    try {
                      Files.createDirectory(sqlScript);
                    } catch (IOException e) {
                      throw new RuntimeException(e);
                    }
                  }
                  File scriptDir = new File(cveStorageDir, "sqlscripts");
                  boolean fileStatus = cveupdateObj.makeCveSchemaSqlScripts(scriptDir);

                  if (!fileStatus) {
                    setUpdateCveStatus(config, "4", "Failed to create scripts...", false);
                    return;
                  }

                  String sqlscriptsDirPath = scriptDir.getCanonicalPath() + "\\" + CVE_UPDATE_SQL;

                  com.marimba.apps.securitymgr.compliance.util.RunSQLScript rsScript = new com.marimba.apps.securitymgr.compliance.util.RunSQLScript(
                      sqlscriptsDirPath, main);
                  if (rsScript.getStatus()) {
                    info("Severity status update : SUCCEED");
                  } else {
                    error("Severity status update : FAILED");
                    setUpdateCveStatus(config, "4", "Failed to update tables...", false);
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
                  config.setProperty("cvejsonupdate.process.message",
                      "CVE SQL scripts created and updated successfully.");
                  setUpdateCveStatus(config, "5", true);

                  info("CVE json last updated timestamp : " + dateVal);

                  definitionUpdateForm.setCveJsonLastUpdated(dateVal);

                } catch (Exception ex) {
                  if (DEBUG >= 5) {
                    ex.printStackTrace();
                  }
                  setUpdateCveStatus(config, "4", "Failed to update tables...", false);
                  error(ex.getMessage());
                }

                setFormData(definitionUpdateForm);
                forward = mapping.findForward("view");
                info("END THREAD: RunnableJob is being run by " + thread.getName() + " ("
                    + thread.getId() + ")");
              }
            }

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

          info("Creating thread with name cveJsonUpdateThread");
          Thread cveJsonUpdateThread = new Thread(update_cvejson_runnable);
          cveJsonUpdateThread.setName("cveJsonUpdateThread");
          cveJsonUpdateThread.start();

          JSONObject json = new JSONObject();
          json.put("cveJsonUpdateThread", false);
          sendJSONResponse(response, json.toString());

          return;

        } else {

          info("START - fetch definitions update metadata");
          DefinitionUpdateHandler definitionUpdateHandler = new DefinitionUpdateHandler(main);
          definitionUpdateResponse = definitionUpdateHandler.getMetaData();
          definitionUpdateForm.setDefinationUpdateResponse(definitionUpdateResponse);
          info("END - fetch definitions update metadata " + definitionUpdateResponse.toString());
          forward = mapping.findForward("view");
        }

      } catch (Exception ex) {
        error(ex.getMessage());
        if (DEBUG >= 5) {
          ex.printStackTrace();
        }

      }
      forward = mapping.findForward("view");
    }

    private void setUpdateCveStatus(ConfigProps config, String status, String error,
        boolean forceUpdate) {
      config.setProperty("cvejsonupdate.process.status", status);
      config.setProperty("cvejsonupdate.process.error", error);
      config.setProperty("cvejsonupdate.forceCveUpdate", String.valueOf(forceUpdate));
      config.save();
      config.close();
      loadFormData(config, definitionUpdateForm);
    }

    private void setUpdateCveStatus(ConfigProps config, String status, boolean forceUpdate) {
      config.setProperty("cvejsonupdate.process.status", status);
      config.setProperty("cvejsonupdate.process.error", "");
      config.setProperty("cvejsonupdate.forceCveUpdate", String.valueOf(forceUpdate));
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
        defnForm.setForceUpdate(
            Boolean.parseBoolean(config.getProperty("cvejsonupdate.forceCveUpdate")));
      }
    }


    private void initDefinitionsUpdateConfig() {
      File rootDir = main.getDataDirectory();
      if (rootDir != null && rootDir.isDirectory()) {
        File configFile = new File(rootDir, "definitions_update_config.txt");
        try {
          String productMasterUrl = tunerConfig.getProperty("products.mastertx.url");
          if (!configFile.exists()) {
            config = new ConfigProps(configFile);
            if (!isNull(productMasterUrl)) {
              config.setProperty("products.mastertx.url", productMasterUrl);
            }
            config.setProperty("destination.mastertx.url", "");
            config.setProperty("publish.tx.user", "");
            config.setProperty("publish.tx.password", "");
            config.setProperty("channelstore.authenticate.user", "");
            config.setProperty("channelstore.authenticate.password", "");
            config.setProperty("defensight.cvejson.downloadurl",
                "https://cve.circl.lu/static/circl-cve-search-expanded.json.gz");
            config.setProperty("vdefchannel.lastcopied.timestamp", "Not Updated");
            config.setProperty("defensight.cvejson.lastupdated.timestamp", "Not Updated");
            config.setProperty("cvejsonupdate.process.status", "0");
            config.setProperty("cvejsonupdate.process.error", "");
            config.setProperty("vdefchannel.copy.error", "");
            config.setProperty("cvejsonupdate.forceCveUpdate", String.valueOf(true));
            config.setProperty("defensight.cve.api.url",
                "https://services.nvd.nist.gov/rest/json/cves/2.0");
            config.setProperty("defensight.cve.api.key", "7ca09635-d4da-4f18-99ca-285b82bfc6b7");
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
          info("Unable to create definitions_update_config.txt configuration file");
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
      }
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

  public void error(String message) {
    System.err.println("ERROR : DefinitionUpdateAction : " + message);
  }

  public void info(String message) {
    System.out.println("INFO : DefinitionUpdateAction : " + message);
  }
}

// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.*;
import java.net.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

import java.text.ParseException;

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

import com.marimba.apps.securitymgr.compliance.util.ChannelCopier;
import com.marimba.apps.securitymgr.compliance.util.Tools;
import com.marimba.apps.securitymgr.utils.json.JSONArray;
import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.securitymgr.compliance.util.RunSQLScript;
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
import com.marimba.apps.securitymgr.compliance.util.DefinitionUpdateHandler;
import com.marimba.apps.subscriptionmanager.webapp.forms.DefinitionUpdateForm;

/**
 * DefinitionUpdateAction w.r.t handle vDef and CVE JSON update operations
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class DefinitionUpdateAction extends AbstractAction
		implements IWebAppConstants, ISubscriptionConstants, ICveUpdateConstants {

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
					String message = config.getProperty("cvejsonupdate.process.message");

					JSONObject json = new JSONObject();
					json.put("status", Integer.valueOf(status));
					json.put("error", error);
					json.put("message", message);
					//System.out.println("Response : " + json.toString());
					sendJSONResponse(response, json.toString());

				} else if ("checkThreadStatus".equals(action)) {

					Set<Thread> threads = Thread.getAllStackTraces().keySet();
					for (Thread t : threads) {
						if (t.getName() == "cveJsonUpdateThread") {
							/*
							 * System.out.printf("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State",
							 * "Priority", "isDaemon"); System.out.printf("%-15s \t %-15s \t %-15d \t %s\n",
							 * t.getName(), t.getState(), t.getPriority(), t.isDaemon());
							 */
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

					System.out.println("DebugInfo: Master Tx Url: " + masterTxUrl);
					System.out.println("DebugInfo: Publish UserName: " + pubUser);
					System.out.println("DebugInfo: Publish Password: " + pubPwd);

					pubPwd = encode(pubPwd);
					chstorePwd = encode(chstorePwd);

					initDefinitionsUpdateConfig();
					ConfigProps config = getDefinitionsUpdateConfig();
					if (config != null) {
						config.setProperty("destination.mastertx.url", masterTxUrl);
						config.setProperty("publish.tx.user", pubUser);
						config.setProperty("publish.tx.password", pubPwd);
						config.setProperty("channelstore.authenticate.user", chstoreUser);
						config.setProperty("channelstore.authenticate.password", chstorePwd);
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
						config.setProperty("vdefchannel.copy.error", "");

						System.out.println("LogInfo: vdefchannel.lastcopied.timestamp ==> " + dateVal);
						System.out.println(
								"LogInfo: Copy vDef channel from src=" + srcUrl + " to dst=" + dstUrl + " : succeeded");

						definitionUpdateForm.setVdefLastUpdated(dateVal);
						config.save();
						config.close();
						System.out.println("vDef channel copy operation succeeded from Products Tx..");
						definitionUpdateForm.setvDefError("");
					} else {
						System.out.println("vDef channel copy operation failed from Products Tx..");
						config.setProperty("vdefchannel.copy.error", "vDef channel copy operation failed.");
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
							System.out.println("START THREAD: RunnableJob is being run by " + thread.getName() + " ("
									+ thread.getId() + ")");
							initDefinitionsUpdateConfig();
							ConfigProps config = getDefinitionsUpdateConfig();

							int updateCvejsonStartStep = definitionUpdateForm.getUpdateCvejsonStartStep();

							//System.out.println("updateCvejsonStartStep = " + updateCvejsonStartStep);

							String publishTxUrl = definitionUpdateForm.getPublishTxUrl();
							String cveStorageDir = definitionUpdateForm.getCveStorageDir();

							tunerConfig = (IConfig) features.getChild("tunerConfig");
							Tools.setTunerConfig(tunerConfig);
							String tunerInstallDir = tunerConfig.getProperty("marimba.tuner.install.dir");

							if (updateCvejsonStartStep < 2) {

								config.setProperty("cvejsonupdate.process.thread", "" + thread.getId());
								config.setProperty("cvejsonupdate.process.status", "1");
								config.setProperty("cvejsonupdate.process.message",
										"CVE JSON Zip File Downloading at " + cveStorageDir);
								config.setProperty("cvejsonupdate.process.error", "");
								config.save();

								// String actionString = "cvejson_download";
								initDefinitionsUpdateConfig();
								config = getDefinitionsUpdateConfig();

								String urlStr = config.getProperty("defensight.cvejson.downloadurl");
								urlStr = (isNull(urlStr))
										? "https://cve.circl.lu/static/circl-cve-search-expanded.json.gz"
										: urlStr;

								System.out.println("DebugInfo: publishTxUrl ==> " + publishTxUrl);
								System.out.println("DebugInfo: cveStorageDir ==> " + cveStorageDir);

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

									boolean downloadfailed = downloadCVEJSON(urlStr, 5, cvejsonZipFile);
									if (!downloadfailed) {
										System.out.println("CVE JSON Zip File Download Succeeded...");
									} else {
										System.out.println("CVE JSON Zip File Download Failed..");
										config.setProperty("cvejsonupdate.process.message", "");
										config.setProperty("cvejsonupdate.process.error",
												"CVE JSON Zip File Download Failed..");
										config.save();
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									config.setProperty("cvejsonupdate.process.message", "");
									config.setProperty("cvejsonupdate.process.error", ex.getMessage());
									config.save();
									config.close();
								}

							}

							if (updateCvejsonStartStep < 3) {

								config.setProperty("cvejsonupdate.process.status", "2");
								config.setProperty("cvejsonupdate.process.message", "Unzipping " + cvejsonZipFile);
								config.setProperty("cvejsonupdate.process.error", "");
								config.save();

								try {

									File jsonZipFile = new File(cveStorageDir, cvejsonZipFile);
									System.out.println(
											"DebugInfo: CVE JSON ZipFile Path ==> " + jsonZipFile.getCanonicalPath());
									String cveDir = getDefinitionsUpdateConfig()
											.getProperty("defensight.cvejson.storagedir.location");
									tunerConfig.setProperty("cvedownloader.storage.directory", cveStorageDir);
									File unzipDst = new File(cveDir, cvejsonFile);

									actionString = "cvejson_unzip";
									Tools.gunzip(jsonZipFile, unzipDst);

								} catch (Exception ex) {
									ex.printStackTrace();
									config.setProperty("cvejsonupdate.process.error", ex.getMessage());
									config.setProperty("cvejsonupdate.process.message", "");
									config.save();
									config.close();
								}
							}

							if (updateCvejsonStartStep < 6) {

								config.setProperty("cvejsonupdate.process.status", "3");
								config.setProperty("cvejsonupdate.process.message",
										"Subscribing CVE downloader Channel");
								config.setProperty("cvejsonupdate.process.error", "");
								config.save();

								try {

									IApplicationContext iappContext = (IApplicationContext) main.getFeatures()
											.getChild("context");

									String cveDownloaderChannel = fetchCVEDownloaderChannelUrl(iappContext);
									System.out.println(
											"DebugInfo: CVE DOWNLOADER Channel URL ==> " + cveDownloaderChannel);

									if (isNull(cveDownloaderChannel)) {
										System.out.println(
												"DebugInfo: CVE DOWNLOADER Channel URL not found.. Going to subscribe from Master Transmitter");

										config.setProperty("cvejsonupdate.process.message",
												"CVE downloader Channel URL not found. Going to subscribe from Master Transmitter");
										config.setProperty("cvejsonupdate.process.error", "");
										config.save();

                                        String cvedownloaderUrl = main.getConfig().getProperty("subscriptionmanager.cvedownloader.url");
                                        IChannel ch = subscribeCveDownloaderChannel(cvedownloaderUrl);
                                        if (ch != null) {
                                            String subscribedUrl = ch.getURL();
                                            System.out.println("DebugInfo: CVE DOWNLOADER Channel subscribedUrl succeeded - " + subscribedUrl);
                                            config.setProperty("defensight.cvedownloaderchannel.location",  subscribedUrl);
                                            config.setProperty("cvejsonupdate.process.message", "");
                                            config.setProperty("cvejsonupdate.process.error", "");
                                            config.save();
                                        }

										String cvedownloadChUrl = config
												.getProperty("defensight.cvedownloaderchannel.location");

										if (isNull(cvedownloadChUrl)) {
											System.out.println(
													"DebugInfo: defensight.cvedownloaderchannel.location not present in the properties file.");

											config.setProperty("cvejsonupdate.process.message", "");
											config.setProperty("cvejsonupdate.process.error",
													"Not able to subscribe the CVE downloader channel from Master Transmitter.<br/>Please subscribe it manually and then try again.");
											config.save();

											return;
										}

										ch = subscribeCveDownloaderChannel(cvedownloadChUrl);
										System.out.println("ch.getURL() = " + ch.getURL());

										if (ch != null) {
											String subscribedUrl = ch.getURL();
											cveDownloaderChannel = subscribedUrl;
											System.out.println(
													"DebugInfo: CVE DOWNLOADER Channel subscribedUrl succeeded - "
															+ subscribedUrl);
										} else {
											System.out.println(
													"DebugInfo: CVE downloader Channel not present on Master Transmitter.");

											config.setProperty("cvejsonupdate.process.message", "");
											config.setProperty("cvejsonupdate.process.error",
													"CVE downloader Channel not present on Master Transmitter.");
											config.save();

											return;
										}
									}

									actionString = "cvejson_csvgenerate";
									config.setProperty("cvejsonupdate.process.status", "4");
									config.setProperty("cvejsonupdate.process.message",
											"Generating CSV files from CVE JSON.");
									config.setProperty("cvejsonupdate.process.error", "");
									config.save();

									boolean csvStatus = generateCveJsonToCsvFiles(tunerInstallDir,
											cveDownloaderChannel);

									File scriptDir = null;
									if (csvStatus) {
										System.out.println("DebugInfo: CVE JSON into CSV files generation succeeded - "
												+ csvStatus);

										config.setProperty("cvejsonupdate.process.message",
												"Generation of CSV files from CVE JSON succeeded.");
										config.save();

										File csvFilesDir = new File(cveStorageDir, "csv_data");
										if (!csvFilesDir.exists())
											csvFilesDir.mkdirs();

										fetchCVEDownloaderChannelUrl(iappContext);

										if (cveDownloderChPath == null) {
											config.setProperty("cvejsonupdate.process.message", "");
											config.setProperty("cvejsonupdate.process.error",
													"CVE downloader channel path is null");
											config.save();
											return;
										}
										File cvedownloaderCsvData = new File(cveDownloderChPath);

										Tools.copyDir(cvedownloaderCsvData, csvFilesDir);

										scriptDir = new File(cveStorageDir, "sqlscripts");
										if (!scriptDir.exists())
											scriptDir.mkdirs();

										boolean fileStatus = makeCveSchemaSqlScripts(scriptDir);
										System.out.println(
												"DebugInfo: CVE SQL scripts created and updated successfully..."
														+ fileStatus);

										config.setProperty("cvejsonupdate.process.message",
												"CVE SQL scripts created and updated successfully.");
										config.save();

										int csvfilesCnt = csvFilesDir.list().length;
										System.out.println("DebugInfo: Number of CSV files: " + csvfilesCnt);
										String csvFilesDirPath = csvFilesDir.getCanonicalPath();
										updateScriptFile(scriptDir, csvFilesDirPath, csvfilesCnt);

									} else {
										System.out.println(
												"DebugInfo: CVE JSON into CSV files generation failed - " + csvStatus);
										config.setProperty("cvejsonupdate.process.error",
												"CVE JSON into CSV files generation failed - " + csvStatus);
										config.setProperty("cvejsonupdate.process.message", "");
										config.save();
										config.close();
									}

									config.setProperty("cvejsonupdate.process.status", "5");
									config.setProperty("cvejsonupdate.process.message",
											"Updating CVE JSON into DefenSight Database");
									config.setProperty("cvejsonupdate.process.error", "");
									config.save();

									actionString = "csvdata_sqldbupdate";
									String sqlscriptsDirPath = scriptDir.getCanonicalPath() + "\\" + CVE_CREATE_SQL;
									System.out
											.println("DebugInfo: SQL scripts directory path ==> " + sqlscriptsDirPath);
									RunSQLScript rsScript = new RunSQLScript(sqlscriptsDirPath, main);

									if (rsScript.getStatus()) {
										System.out.println(
												"DebugInfo: CVE JSON create-tables.sql into DefenSight Database - SUCCEEDED");
									}

									sqlscriptsDirPath = scriptDir.getCanonicalPath() + "\\" + CVE_RUN_SQL;
									rsScript = new RunSQLScript(sqlscriptsDirPath, main);

									if (rsScript.getStatus()) {
										System.out.println(
												"DebugInfo: CVE JSON update bulk insertion into DefenSight Database - SUCCEEDED");

										sqlscriptsDirPath = scriptDir.getCanonicalPath() + "\\" + CVE_UPDATE_SQL;
										rsScript = new RunSQLScript(sqlscriptsDirPath, main);
										if (rsScript.getStatus()) {
											System.out.println(
													"DebugInfo: CVE JSON update-tables.sql into DefenSight Database - SUCCEEDED");
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
										config.setProperty("cvejsonupdate.process.status", "6");
										config.setProperty("cvejsonupdate.process.message", "Completed");
										config.setProperty("cvejsonupdate.process.error", "");
										config.setProperty("defensight.cvejson.lastupdated.timestamp", dateVal);
										config.save();
										System.out.println(
												"LogInfo: defensight.cvejson.lastupdated.timestamp ==> " + dateVal);
										definitionUpdateForm.setCveJsonLastUpdated(dateVal);
									} else {
										System.out.println(
												"DebugInfo: CVE JSON update into DefenSight Database - FAILED");
										config.setProperty("cvejsonupdate.process.error",
												"CVE JSON update into DefenSight Database - FAILED");
										config.setProperty("cvejsonupdate.process.message", "");
										config.save();
										config.close();
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									config.setProperty("cvejsonupdate.process.error", ex.getMessage());
									config.save();
									config.close();
								}

								setFormData(definitionUpdateForm);
								forward = mapping.findForward("view");
								System.out.println("END THREAD: RunnableJob is being run by " + thread.getName() + " ("
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
					System.out
							.println("END - fetch definitions update metadata " + definitionUpdateResponse.toString());
					forward = mapping.findForward("view");
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
			forward = mapping.findForward("view");
		}

		private boolean makeCveSchemaSqlScripts(File cvestorageDir) {
			boolean fileStatus = true;
			try {
				File createScriptFile = new File(cvestorageDir, CVE_CREATE_SQL);
				FileOutputStream fout = new FileOutputStream(createScriptFile);
				String scriptContent = CREATE_TABLE_SQL;
				byte[] bytesArray = scriptContent.getBytes();
				fout.write(bytesArray);
				fout.flush();
				fout.close();
				System.out.println(CVE_CREATE_SQL + " - File written successfully... ");

				File updateScriptFile = new File(cvestorageDir, CVE_UPDATE_SQL);
				fout = new FileOutputStream(updateScriptFile);
				scriptContent = UPDATE_TABLE_SQL;
				bytesArray = scriptContent.getBytes();
				fout.write(bytesArray);
				fout.flush();
				fout.close();
				System.out.println(UPDATE_TABLE_SQL + " - File written successfully... ");

			} catch (Exception ex) {
				fileStatus = false;
				ex.printStackTrace();
				System.out.println("Failed to create cve setup sql script files: " + ex.getMessage());
			}
			return fileStatus;
		}

		// To update bulk insertion sql script for csv files
		private void updateScriptFile(File scriptDir, String csvDirPath, int csvFilesCnt) {
			try {

				String dataSourceName = main.getProperty("subscriptionmanager.db.name");

				File runSqlFile = new File(scriptDir, "run.sql");
				BufferedWriter bout = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(runSqlFile), "UTF-8"));

				String permString = "Use master;";
				bout.write(permString);
				bout.newLine();
				bout.write("GRANT ADMINISTER BULK OPERATIONS TO inventory;");
				bout.newLine();
				permString = "Use " + dataSourceName + ";";
				bout.write(permString);
				bout.newLine();

				String scriptContent = "BULK INSERT vendor_info FROM '" + csvDirPath
						+ "\\csv-vendor-0.csv' WITH (FIELDTERMINATOR = ',', ROWTERMINATOR = '\\n');";
				bout.write(scriptContent);
				csvFilesCnt--;

				bout.newLine();
				scriptContent = "BULK INSERT product_INFO FROM '" + csvDirPath
						+ "\\csv-product-0.csv' WITH (FIELDTERMINATOR = ',', ROWTERMINATOR = '\\n');";
				bout.write(scriptContent);
				csvFilesCnt--;
				bout.newLine();

				for (int idx = 0; idx < csvFilesCnt; idx++) {
					scriptContent = "BULK INSERT product_cve_info FROM '" + csvDirPath + "\\csv-product-cve-info-" + idx
							+ ".csv' WITH (FIELDTERMINATOR = ',', ROWTERMINATOR = '\\n');";
					bout.write(scriptContent);
					bout.newLine();
				}

				permString = "Use master;";
				bout.write(permString);
				bout.newLine();
				bout.write("REVOKE ADMINISTER BULK OPERATIONS FROM inventory;");
				bout.newLine();
				permString = "Use " + dataSourceName + ";";
				bout.write(permString);
				bout.newLine();

				bout.flush();
				bout.close();
				System.out.println("SQL script <run.sql> file updated successfully... ");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private IChannel subscribeCveDownloaderChannel(String chUrl) throws Exception {
			IChannel cvedownloadCh = null;
			IApplicationContext context = (IApplicationContext) main.getFeatures().getChild("context");
			ws = (IWorkspace) context.getFeature("workspace");
			if (chUrl != null) {

				// Removed try catch
				cvedownloadCh = ws.getChannel(chUrl);
				if (cvedownloadCh == null) {
					cvedownloadCh = ws.getChannelCreate(chUrl, false, null);
				} else {
					return cvedownloadCh;
				}

			}
			return cvedownloadCh;
		}

		// Method to download cve jsoz zip file from cve site
		private boolean downloadCVEJSON(String urlStr, int timeoutMinutes, String resultFile) {
			boolean downloadFailed = true;
			System.out.println(" Timeout in minutes  - " + timeoutMinutes);
			int TIMEOUT_VALUE = timeoutMinutes * 1000 * 60;
			try {

				URL testUrl = new URL(urlStr);
				StringBuilder answer = new StringBuilder(100000);

				URLConnection testConnection = testUrl.openConnection();
				testConnection.setConnectTimeout(TIMEOUT_VALUE);
				testConnection.setReadTimeout(TIMEOUT_VALUE);
				testConnection.setRequestProperty("http.protocol.single-cookie-header", "true");
				testConnection.connect();

				InputStream ip = null;

				try {
					ip = testConnection.getInputStream();
					copyFile(ip, resultFile);
					downloadFailed = false;
				} catch (IOException ioe) {
					ioe.printStackTrace();
					downloadFailed = true;
				}
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
				downloadFailed = true;
				System.out.println("More than " + TIMEOUT_VALUE + " elapsed.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return downloadFailed;
		}

		private void copyFile(InputStream source, String fileName) throws IOException {
			byte[] buff = new byte[1024];
			int len = -1;
			OutputStream out = null;
			String rootDir = getDefinitionsUpdateConfig().getProperty("defensight.cvejson.storagedir.location");
			try {
				File fileDir = new File(rootDir);
				if (!fileDir.exists()) {
					fileDir.mkdirs();
				}

				File fos = new File(rootDir, fileName);

				// open the outstream to the file and creates the parent directories if
				// necessary
				out = new FileOutputStream(fos);

				// copy from the source to the destination
				while ((len = source.read(buff)) > 0) {
					out.write(buff, 0, len);
				}
			} finally {
				source.close();
				if (out != null) {
					out.close();
				}
			}
		}

		private boolean generateCveJsonToCsvFiles(String runChannelDir, String cveDownloderChUrl) {
			boolean resultCLI = true;
			Vector args = new Vector();
			args.addElement("runchannel.exe");
			args.addElement(cveDownloderChUrl);
			args.addElement("-prepareCsv2");

			String cmdLineStr = Tools.getCommandline(args);
			// System.out.println("DebugInfo: CVE-Downloader CLI String ==> " + cmdLineStr);
			try {
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", cmdLineStr)
						.directory(new File(runChannelDir));

				builder.redirectErrorStream(true);
				Process process = builder.start();
				BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while (true) {
					line = r.readLine();
					if (line == null) {
						break;
					}
					if (line != null && line.indexOf("Total time taken") != -1) {
						resultCLI = true;
					}
					System.out.println(" Command execution result :: " + line);
				}

				int exitCode = process.waitFor();
				System.out.println("CLI Process Exited with code : " + exitCode);

			} catch (Exception ex) {
				resultCLI = false;
				ex.printStackTrace();
			}
			return resultCLI;
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
				defnForm.setVdefLastUpdated(config.getProperty("vdefchannel.lastcopied.timestamp"));
				defnForm.setvDefError(config.getProperty("vdefchannel.copy.error"));
				
				defnForm.setCveStorageDir(config.getProperty("defensight.cvejson.storagedir.location"));
				defnForm.setCveJsonLastUpdated(config.getProperty("defensight.cvejson.lastupdated.timestamp"));

				defnForm.setCveJsonUpdateStep(Integer.valueOf(config.getProperty("cvejsonupdate.process.status")));
				defnForm.setCveJsonUpdateMsg(config.getProperty("cvejsonupdate.process.message"));
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

		private String fetchCVEDownloaderChannelUrl(IApplicationContext context) throws Exception {
			String cveDownloaderChUrl = "";
			File file = new File(context.getDataDirectory());
			String s = file.getParent();
			String s1 = s.substring(0, s.indexOf("ch."));
			String s2 = (new StringBuilder()).append(s1).append("map.txt").toString();

			System.out.println("s1 = " + s1);
			System.out.println("s2 = " + s2);

			// Removed Try Catch
			BufferedReader bufferedreader = new BufferedReader(new FileReader(new File(s2)));

			String s3;
			do {
				s3 = bufferedreader.readLine();
				System.out.println("s3 = " + s3);
				if (s3 == null)
					break;
			} while (!s3.contains("CVEDownloader"));

			if (!isNull(s3)) {
				String s4 = s3.substring(0, s3.indexOf("="));
				String s5 = s3.substring(s3.indexOf("=") + 1, s3.length());
				String s6 = (new StringBuilder()).append(s1).append("\\").append(s5).append("\\data\\cve-save-csv")
						.toString();
				cveDownloderChPath = s6;
				System.out.println("DebugInfo: FilePath of CVE Downloader channel path: " + cveDownloderChPath);
				cveDownloaderChUrl = s4;
			}

			return cveDownloaderChUrl;
		}

		private void initDefinitionsUpdateConfig() {
			File rootDir = main.getDataDirectory();
			if (rootDir != null && rootDir.isDirectory()) {
				File configFile = new File(rootDir, "definitions_update_config.txt");
				try {
					String cvedownloaderUrl = main.getConfig().getProperty("subscriptionmanager.cvedownloader.url");

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
						config.setProperty("cvejsonupdate.process.message",
								"Please start the CVE Definitions update process.");
						config.setProperty("vdefchannel.copy.error","");

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

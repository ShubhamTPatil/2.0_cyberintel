// Copyright 2018-21, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.servlets;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.securitymgr.utils.json.JSONArray;
import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.subscriptionmanager.beans.OvalDetailBean;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.tools.config.*;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.intf.db.IStatementPool;
import com.marimba.oval.util.xml.profiles.OVALProfile;
import com.marimba.oval.util.xml.profiles.OVALProfileDefinition;
import org.mapdb.DB;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MachineOverallDownloadServlet extends BasicServlet {
    private String CVEID_OS_MAPPING_FILE = "cveids_os_mapping_settings.txt";
    private ConfigProps config;
    private int rulesSize = 0;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
        debug(5, "doPost()...");
        String commandAction = request.getParameter("command");
        String fileFormat = request.getParameter("file_format");
        String reportTypeStr = "Excel";
        if(null != commandAction && commandAction.equals("isreportrunning")) {
        	JSONObject jsonResult = new JSONObject();
        	try {
        		jsonResult.put("status", isRunningReport());
        	} catch(Throwable e) {
        		e.printStackTrace();
        		jsonResult.put("status", false);
        	}
        	PrintWriter out = response.getWriter();
            out.println(jsonResult.toString());
            out.flush();
        } else {
	        if (request.getParameter("file_format") == null) {
	            fileFormat = "xls";
	        }
	        File vaDefinitionResultsDir = createReportDirectory();
	        DownloadProcessor downloadProcessor = new DownloadProcessor(request, vaDefinitionResultsDir);
	
	        Properties lastReportDirProps = new Properties();
	        lastReportDirProps.setProperty("last.report.dir", vaDefinitionResultsDir.getAbsolutePath());
	        lastReportDirProps.setProperty("status", "started");
	        updateLastReport(vaDefinitionResultsDir, lastReportDirProps);
	
	        JSONObject jsonResult = new JSONObject();
	
	        try {
	            new Thread(downloadProcessor).start();
	            jsonResult.put("result", true);
	
                if ("html".equalsIgnoreCase(fileFormat)) {
                    reportTypeStr = "HTML";
                } else if ("pdf".equalsIgnoreCase(fileFormat)) {
                    reportTypeStr = "PDF";
                } else {
                    reportTypeStr = "Excel";
                }

	            String reportsPath, statusPath = null;
	            reportsPath = vaDefinitionResultsDir.getAbsolutePath();
	            statusPath = vaDefinitionResultsDir.getAbsolutePath() + File.separator + "status.txt";
	
	            jsonResult.put("message", reportTypeStr + "  report generation is triggered successfully, and will be available at " + reportsPath + " in some time." +
	                    "\n\nAlso, please refer to " + statusPath + " file for more information on this report");
	            jsonResult.put("report.file", reportsPath);
	            jsonResult.put("report.info", statusPath);
	        } catch (Throwable ex) {
	            ex.printStackTrace();
	            jsonResult.put("result", false);
	            jsonResult.put("message", "Download of " + reportTypeStr + " report failed. Reason - " + ex.getMessage());
	        }
	        PrintWriter out = response.getWriter();
	        out.println(jsonResult.toString());
	        out.flush();
        }
    }

    private boolean isRunningReport() {
    	try {
    		File lastReportFile = new File(main.getDataDirectory() + File.separator + "temp", "lastreport.txt");
    		if(lastReportFile.exists() && lastReportFile.canRead()) {
        		FileReader reader=new FileReader(lastReportFile);  
        	    Properties lastReportProp=new Properties();  
        	    lastReportProp.load(reader);
        	    String reportStatus = lastReportProp.getProperty("status");
        	    if(null != reportStatus && reportStatus.equals("started")) {
        	    	return true;
        	    }
    		}
    	} catch(Throwable e) {
    		if (DEBUG >= 5) {
    			e.printStackTrace();
    		}
    	}
    	return false;
    }

    int getRulesSize() {
        return rulesSize;
    }

    private String getWhitelistCVE(String OS) {
    	String whiteListCveIds = "";
        try {
    		initConfig();
    		if(null != config) {
                String sizeStr = config.getProperty("os.size");
                int size = (sizeStr !=null) ? Integer.parseInt(sizeStr) : 0;
                for(int i = 0; i < size; i++) {
                    String osName = config.getProperty("profile.os" + i + ".name");
                    if (OS != null && OS.startsWith(osName)) {
                       whiteListCveIds = config.getProperty("profile.os" + i + ".name");
                       break; 
                    }
                }
    		}
    	} catch(Throwable e) {
    		if (DEBUG >= 5) {
    			e.printStackTrace();
    		}
    	}
    	return whiteListCveIds;
    }

    private void initConfig() {
	File rootDir = main.getDataDirectory();
        config = new ConfigProps(new File(rootDir, CVEID_OS_MAPPING_FILE));
    }

    class DownloadProcessor implements Runnable {
        HttpServletRequest request = null;
        File vaDefinitionResultsDir = null;
        String selectionType = null;
        String displayPath = null;
        String filteredResults = null;
        String filteredSeverity = null;
        String filteredColumns = null;
        String selectedRows = null;
        boolean isSingleReport = false;
        String fileFormat = null;
        String cveList = null;


        DownloadProcessor(HttpServletRequest request, File vaDefinitionResultsDir) {
            this.request = request;
            debug(5, "DownloadProcessor.run(), request - " + request);

            this.vaDefinitionResultsDir = vaDefinitionResultsDir;
            debug(5, "DownloadProcessor.run(), vaDefinitionResultsDir - " + vaDefinitionResultsDir.getAbsolutePath());

            this.selectionType = request.getParameter("selection_type");
            debug(5, "DownloadProcessor.run(), selectionType - " + selectionType);

            this.displayPath = request.getParameter("display_path");
            debug(5, "DownloadProcessor(), displayPath - " + displayPath);

            this.filteredResults = request.getParameter("selected_results");
            debug(5, "DownloadProcessor(), filteredResults - " + filteredResults);

            this.filteredSeverity = request.getParameter("selected_severity");
            debug(5, "DownloadProcessor(), filteredSeverity - " + filteredSeverity);

            this.filteredColumns = request.getParameter("selected_columns");
            debug(5, "DownloadProcessor(), filteredColumns - " + filteredColumns);

            this.selectedRows = request.getParameter("selected_rows");
            debug(5, "DownloadProcessor(), selectedRows - " + selectedRows);

            this.isSingleReport = "true".equals(request.getParameter("single_report"));
            debug(5, "DownloadProcessor(), isSingleReport - " + isSingleReport);

            this.fileFormat = request.getParameter("file_format");
            debug(5, "DownloadProcessor(), fileFormat - " + fileFormat);
            if (this.fileFormat == null) {
                this.fileFormat = "xls";
            }
        }

        public void run() {
            if (isSingleReport) {
            	forceDebug("Starting Single report generation - " + displayPath);
                Properties prop = new Properties();
                OutputStream output;
                File vaDefinitionResultsFile = null;
                try {
                    prop.setProperty("status", "in-progress");
                    prop.setProperty("type", selectionType);
                    prop.setProperty("filter.results", filteredResults);
                    prop.setProperty("filter.severity", filteredSeverity);
                    prop.setProperty("filter.columns", filteredColumns);
                 //   prop.setProperty("filter.whitelistcveid", cveList);

                    List<File> files = new ArrayList<File>(2);
                    StringBuilder strBuilder = new StringBuilder();
                    long currentTime = System.currentTimeMillis();
                    strBuilder.append("va-definition-results").append("_").append(fileFormat).append("_").append(currentTime).append(".").append(fileFormat.toLowerCase());
                    String fileName = strBuilder.toString();
                    vaDefinitionResultsFile = new File(vaDefinitionResultsDir, fileName);
                    try {
                        if (!vaDefinitionResultsFile.createNewFile()) vaDefinitionResultsFile = null;
                    } catch (IOException e) {
                    	if(DEBUG >= 5) {
                    		e.printStackTrace();
                    	}
                    }
                    debug(5, "DownloadProcessor.run(), vaDefinitionResultsFile - " + vaDefinitionResultsFile.getAbsolutePath());

                    output = new FileOutputStream(new File(vaDefinitionResultsDir, "status.txt"));
                    prop.setProperty("report", vaDefinitionResultsFile.getAbsolutePath());

                    String queryCondition = null;

                    String query = "select DISTINCT machine_name, content_id, content_name, content_title, profile_id, profile_name, profile_title, rules_compliance,finished_at from inv_security_oval_compliance";

                    DownloadQueryRunner downloadQueryRunner;

                    if (displayPath.contains("Configuration Assessment")) {
                        debug(5, "DownloadProcessor.run(), create DownloadQueryRunner for Configuration Assessment");
                        if ("0".equals(selectionType)) {
                            queryCondition = getQueryCondition("sxc.content_id", selectedRows);
                        }
                        debug(5, "DownloadProcessor.run(), queryCondition - " + queryCondition);
                        prop.setProperty("criteria", (queryCondition == null) ? "NONE" : queryCondition);

                        try {
                            prop.store(output, null);
                        } catch (Throwable t) {

                        } finally {
                            if (output != null) {
                                try {
                                    output.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            output = null;
                        }

                        downloadQueryRunner = new DownloadQueryRunner(queryCondition, isSingleReport);
                    } else {
                        debug(5, "DownloadProcessor.run(), create DownloadQueryRunner for Vulnerability Assessment");

                        queryCondition = getQueryCondition(null, selectedRows);
                        debug(5, "DownloadProcessor.run(), queryCondition - " + queryCondition);

                        prop.setProperty("criteria", (queryCondition == null) ? "NONE" : queryCondition);

                        try {
                            prop.store(output, null);
                        } catch (Throwable t) {

                        } finally {
                            if (output != null) {
                                try {
                                    output.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            output = null;
                        }

                        debug(5, "DownloadProcessor.run(), query - " + query);
                        downloadQueryRunner = new DownloadQueryRunner(queryCondition, query, filteredColumns, filteredResults, filteredSeverity, isSingleReport);
                    }
                    debug(7, "DownloadProcessor.run(), downloadQueryRunner - " + downloadQueryRunner);

                    prop = new Properties();
                    prop.setProperty("criteria", (queryCondition == null) ? "NONE" : queryCondition);

                    JSONObject wholeObj = downloadQueryRunner.getWholeResult();
                    debug(6, "DownloadProcessor.run(), wholeObj - " + wholeObj);
                    if (null != vaDefinitionResultsFile) {
                        try {
                            Map<String, String> dataMap = new HashMap<String, String>(4);
                            dataMap.put("ranat", "" + new Date(System.currentTimeMillis()));
                            dataMap.put("rantime", "");
                            dataMap.put("path", displayPath);
                            if ("pdf".equalsIgnoreCase(fileFormat)) {
                                dataMap.put("pdfpagesize", "A1L");
                            } else if ("html".equalsIgnoreCase(fileFormat)) {
                                dataMap.put("htmlpagesize", "A1L");
                            }

                            if (displayPath.contains("Configuration Assessment")) {
                                dataMap.put("target", displayPath);
                                boolean result = "pdf".equalsIgnoreCase(fileFormat) ?
                                        populatePDFFile(vaDefinitionResultsFile, dataMap, wholeObj) : populateExcelFile(vaDefinitionResultsFile, dataMap, wholeObj);
                                if (result) files.add(vaDefinitionResultsFile);
                            } else {
                                dataMap.put("target", "Vulnerability Assessment: Evaluation results for each Definition");
                                boolean result = false;
                                if ("html".equalsIgnoreCase(fileFormat)) {
                                    result = populateHTMLFile(vaDefinitionResultsFile, dataMap, (JSONObject) wholeObj.get("CVEResult"));
                                } else if ("pdf".equalsIgnoreCase(fileFormat)) {
                                    result = generatePDFFile(vaDefinitionResultsFile, dataMap, (JSONObject) wholeObj.get("CVEResult"));
                                } else {
                                    result = populateExcelFile(vaDefinitionResultsFile, dataMap, (JSONObject) wholeObj.get("CVEResult"));
                                }
                                if (result) files.add(vaDefinitionResultsFile);
                            }
                            if (files.size() > 0) {
                                debug(5, "DownloadProcessor.run(), Files created successfully - " + files);
                                prop.setProperty("status", "completed");
                            } else {
                                debug(0, "DownloadProcessor.run(), Unable to populate content in respective file");
                                prop.setProperty("status", "error [Failed to populate excel report file with scan results]");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            prop.setProperty("status", "error [" + ex.getMessage() + "]");
                        }
                    } else {
                        prop.setProperty("status", "error [Failed to create excel report file]");
                        debug(0, "DownloadProcessor.run(), Unable to create file");
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    prop.setProperty("status", "error [" + t.getMessage() + "]");
                } finally {
                    try {
                        prop.setProperty("report", vaDefinitionResultsFile.getAbsolutePath());
                        prop.setProperty("type", selectionType);
                        prop.setProperty("filter.results", filteredResults);
                        prop.setProperty("filter.severity", filteredSeverity);
                        prop.setProperty("filter.columns", filteredColumns);
                     //   prop.setProperty("filter.whitelistcveid", cveList);
                        output = new FileOutputStream(new File(vaDefinitionResultsDir, "status.txt"));
                        prop.store(output, null);
                        output.close();
                        Properties lastReportDirProps = new Properties();
                        lastReportDirProps.setProperty("last.report.dir", vaDefinitionResultsDir.getAbsolutePath());
                        lastReportDirProps.setProperty("status", "completed");
                        updateLastReport(vaDefinitionResultsDir, lastReportDirProps);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                forceDebug("Completed Single report generation - " + displayPath);
			} else {
				forceDebug("Starting multiple report generation - " + displayPath);
				Properties prop = new Properties();
				OutputStream output;
				boolean isCompleted = false;
				try {
					debug(5, "DownloadProcessor.run() - calling multiple report generation");
					prop.setProperty("status", "in-progress");
					prop.setProperty("type", selectionType);
					prop.setProperty("filter.results", filteredResults);
					prop.setProperty("filter.severity", filteredSeverity);
					prop.setProperty("filter.columns", filteredColumns);
				//	prop.setProperty("filter.whitelistcveid", cveList);

					List<File> files = new ArrayList<File>(2);

					output = new FileOutputStream(new File(vaDefinitionResultsDir, "status.txt"));
					prop.setProperty("report", vaDefinitionResultsDir.getAbsolutePath());

					String queryCondition = null;

					String query = "select DISTINCT machine_name, content_id, content_name, content_title, profile_id, profile_name, profile_title, rules_compliance,finished_at from inv_security_oval_compliance";

					DownloadQueryRunner downloadQueryRunner;

                    if (displayPath.contains("Configuration Assessment")) {
						debug(5, "DownloadProcessor.run(), create DownloadQueryRunner for Configuration Assessment");
						if ("0".equals(selectionType)) {
							queryCondition = getQueryCondition("sxc.content_id", selectedRows);
						}
						debug(5, "DownloadProcessor.run(), queryCondition - " + queryCondition);
						prop.setProperty("criteria", (queryCondition == null) ? "NONE" : queryCondition);

						try {
							prop.store(output, null);
						} catch (Throwable t) {

						} finally {
							if (output != null) {
								try {
									output.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							output = null;
						}

						downloadQueryRunner = new DownloadQueryRunner(queryCondition, isSingleReport);
					} else {
						debug(5, "DownloadProcessor.run(), create DownloadQueryRunner for Vulnerability Assessment");

						queryCondition = getQueryCondition(null, selectedRows);
						debug(5, "DownloadProcessor.run(), queryCondition - " + queryCondition);

						prop.setProperty("criteria", (queryCondition == null) ? "NONE" : queryCondition);

						try {
							prop.store(output, null);
						} catch (Throwable t) {

						} finally {
							if (output != null) {
								try {
									output.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							output = null;
						}

						debug(5, "DownloadProcessor.run(), query - " + query);
						downloadQueryRunner = new DownloadQueryRunner(queryCondition, query, filteredColumns, filteredResults, filteredSeverity, isSingleReport);
					}
					debug(7, "DownloadProcessor.run(), downloadQueryRunner - " + downloadQueryRunner);

					prop = new Properties();
					prop.setProperty("criteria", (queryCondition == null) ? "NONE" : queryCondition);
					
					Map<String, JSONArray> individualContentMap = downloadQueryRunner.getIndividaulContentsMapDetails();
					int count = 1;
					for (Entry<String, JSONArray> contentsEntry : individualContentMap.entrySet()) {
						count = count + 1;
						List<File> multiFfiles = new ArrayList<File>(2);
						OutputStream multiOutput = null;
						JSONObject OverallwholeResult = new JSONObject();
						JSONObject wholeResult = new JSONObject();
						wholeResult.put("header", downloadQueryRunner.getHeaderArray());
						wholeResult.put("body", contentsEntry.getValue());
						OverallwholeResult.put("CVEResult", wholeResult);

                        long currentTime = System.currentTimeMillis();
						StringBuilder strBuilder = new StringBuilder();
						strBuilder.append("va-definition-results").append("_").append(fileFormat).append("_").append(currentTime).append(".").append(fileFormat.toLowerCase());
						String fileName = strBuilder.toString();
						File vaDefinitionResultsFile1 = new File(vaDefinitionResultsDir, fileName);
						try {
							if (!vaDefinitionResultsFile1.exists()) {
								vaDefinitionResultsFile1.createNewFile();
							} else {
								// to handle the situation like file is already exists
								long currentTime2 = System.currentTimeMillis();
								String timeStr = Long.toString(currentTime2);
								if(currentTime2 == currentTime) {
									timeStr = timeStr + "_" +  Integer.toString(count);
								}
								strBuilder = new StringBuilder();
								strBuilder.append("va-definition-results").append("_").append(fileFormat).append("_").append(timeStr).append(".").append(fileFormat.toLowerCase());
								fileName = strBuilder.toString();
								vaDefinitionResultsFile1 = new File(vaDefinitionResultsDir, fileName);
								if (!vaDefinitionResultsFile1.exists()) {
									vaDefinitionResultsFile1.createNewFile();
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							vaDefinitionResultsFile1 = null;
						}

						if (null != vaDefinitionResultsFile1) {
							debug(5, "DownloadProcessor.run(), va-definition file path - " + vaDefinitionResultsFile1.getAbsolutePath());
							try {
								Map<String, String> dataMap = new HashMap<String, String>(4);
								dataMap.put("ranat", "" + new Date(System.currentTimeMillis()));
								dataMap.put("rantime", "");
								dataMap.put("path", displayPath);
                                if ("pdf".equalsIgnoreCase(fileFormat)) {
                                    dataMap.put("pdfpagesize", "A1L");
                                } else if ("html".equalsIgnoreCase(fileFormat)) {
                                    dataMap.put("htmlpagesize", "A1L");
                                }

								if (displayPath.contains("Configuration Assessment")) {
									dataMap.put("target", displayPath);
									boolean result = "pdf".equalsIgnoreCase(fileFormat)
											? populatePDFFile(vaDefinitionResultsFile1, dataMap, OverallwholeResult)
											: populateExcelFile(vaDefinitionResultsFile1, dataMap, OverallwholeResult);
									if (result)
										multiFfiles.add(vaDefinitionResultsFile1);
								} else {
									dataMap.put("target", "Vulnerability Assessment: Evaluation results for each Definition");
                                    boolean result = false;
                                    if ("html".equalsIgnoreCase(fileFormat)) {
                                        result = populateHTMLFile(vaDefinitionResultsFile1, dataMap, (JSONObject) OverallwholeResult.get("CVEResult"));
                                    } else if ("pdf".equalsIgnoreCase(fileFormat)) {
                                        result = generatePDFFile(vaDefinitionResultsFile1, dataMap, (JSONObject) OverallwholeResult.get("CVEResult"));
                                    } else {
                                        result = populateExcelFile(vaDefinitionResultsFile1, dataMap, (JSONObject) OverallwholeResult.get("CVEResult"));
                                    }

									if (result)
										multiFfiles.add(vaDefinitionResultsFile1);
								}
								if (multiFfiles.size() > 0) {
									debug(5, "DownloadProcessor.run(), Files created successfully - " + multiFfiles);
								} else {
									debug(0, "DownloadProcessor.run(), Unable to populate content in respective file");
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						} else {
							debug(0, "DownloadProcessor.run(), Unable to create file");
						}
					}

                    isCompleted = true;
				} catch (Throwable t) {
					t.printStackTrace();
					prop.setProperty("status", "error [" + t.getMessage() + "]");
					isCompleted = false;
				} finally {
					try {
						if(isCompleted) {
							prop.setProperty("status", "completed");
						}
						prop.setProperty("report", vaDefinitionResultsDir.getAbsolutePath());
						prop.setProperty("type", selectionType);
						prop.setProperty("filter.results", filteredResults);
						prop.setProperty("filter.severity", filteredSeverity);
						prop.setProperty("filter.columns", filteredColumns);
					//	prop.setProperty("filter.whitelistcveid", cveList);
						output = new FileOutputStream(new File(vaDefinitionResultsDir, "status.txt"));
						prop.store(output, null);
						output.close();
				        Properties lastReportDirProps = new Properties();
				        lastReportDirProps.setProperty("last.report.dir", vaDefinitionResultsDir.getAbsolutePath());
				        lastReportDirProps.setProperty("status", "completed");
				        updateLastReport(vaDefinitionResultsDir, lastReportDirProps);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				forceDebug("Completed multiple report generation - " + displayPath);
			}
		}
    }

    private String getQueryCondition(String contentColumnString, String _selectedRows) {
        String selectedRows = _selectedRows;
        String queryCondition = "";
        String contentString = "content_id";
        if (contentColumnString != null) {
            contentString = contentColumnString;
        }
        try {
            JSONArray jsonArray = new JSONArray(selectedRows);

            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String machineName = jsonObject.getString("machine_name");
                String contentId = jsonObject.getString("contentId");
                String profileId = jsonObject.getString("profileId");
                if (i > 0) queryCondition += "or";
                queryCondition += "(machine_name='" + machineName +"' and " +contentString+"="+ contentId + " and profile_id="+ profileId +")";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryCondition;
    }

    class DownloadQueryRunner extends DatabaseAccess {
        String queryCondition;
        String filteredColumns, filteredResults, filteredSeverity;
        JSONObject wholeResult = new JSONObject();
        Map<String, JSONArray> individualContentsMap = new HashMap<String, JSONArray>();
        JSONArray headerArray = new JSONArray();
        Map<String, JSONObject> ruleResult = null;
        Map<Integer, String> contentInfoMap = new HashMap<Integer, String>();
        Map<Integer, String> profileInfoMap = new HashMap<Integer, String>();
        Map<String, String> scanTimeMap = new LinkedHashMap<String, String>();
        Map<String, List<String>> cveidsOsMap = new LinkedHashMap<String, List<String>>();
        Map<String, List<OvalDetailBean>> ruleDetails = null;
        Integer contentIdSet[];
        String lastScanTime = null;

        String query = "select DISTINCT machine_name,content_id, profile_id, rules_compliance,finished_at from inv_security_oval_compliance";
        boolean isSingleReport = false;
        List<String> whiteListCVE = new ArrayList<String>();
//        public DownloadQueryRunner(String queryCondition, String query) {
//            this.query = query;
//            this.queryCondition = queryCondition;
//            try {
//                debug(5, "DownloadQueryRunner :: query - " + query);
//                debug(5, "DownloadQueryRunner :: queryCondition - " + queryCondition);
//                GetRuleCompliance getRuleCompliance = new GetRuleCompliance();
//                debug(5, "DownloadQueryRunner :: getRuleCompliance - " + getRuleCompliance);
//                runQuery(getRuleCompliance);
//                debug(5, "DownloadQueryRunner :: runQuery done");
//                ruleResult = getRuleCompliance.getRuleResult();
//                debug(6, "DownloadQueryRunner :: ruleResult - " + ruleResult);
//                contentIdSet = getRuleCompliance.getContentIdSet();
//                debug(5, "DownloadQueryRunner :: contentIdSet - " + contentIdSet);
//                contentInfoMap = getRuleCompliance.getContentInfoMap();
//                debug(5, "DownloadQueryRunner :: contentInfoMap - " + contentInfoMap);
//                profileInfoMap = getRuleCompliance.getProfileInfoMap();
//                debug(5, "DownloadQueryRunner :: profileInfoMap - " + profileInfoMap);
//                wholeResult = getFinalresult(ruleResult, contentInfoMap, profileInfoMap);
//                debug(6, "DownloadQueryRunner :: wholeResult - " + wholeResult);
//                debug(5, "DownloadQueryRunner :: Processing result done");
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }

        public DownloadQueryRunner(String queryCondition, String query, String filteredColumns, String filteredResult, String filteredSeverity, boolean isSingleReport) {
            this.query = query;
            this.queryCondition = queryCondition;
            this.filteredColumns = filteredColumns;
            this.filteredResults = filteredResult;
            this.filteredSeverity = filteredSeverity;
            this.isSingleReport = isSingleReport;
            try {
                debug(5, "DownloadQueryRunner :: query - " + query);
                debug(5, "DownloadQueryRunner :: queryCondition - " + queryCondition);
                List<String> filteredResultsList = new ArrayList<String>(Arrays.asList(filteredResult.split(",")));
                List<String> filteredSeverityList = new ArrayList<String>(Arrays.asList(filteredSeverity.split(",")));
                List<String> filteredColumnsList = new ArrayList<String>(Arrays.asList(filteredColumns.split(",")));
                Map<String, List<String>> filtersMap = new HashMap();
                filtersMap.put("filteredColumns", filteredColumnsList);
                filtersMap.put("filteredResult", filteredResultsList);
                filtersMap.put("filteredSeverity", filteredSeverityList);
                GetRuleCompliance getRuleCompliance = new GetRuleCompliance();
                debug(5, "DownloadQueryRunner :: getRuleCompliance - " + getRuleCompliance);
                runQuery(getRuleCompliance);
                debug(5, "DownloadQueryRunner :: runQuery done");
                ruleResult = getRuleCompliance.getRuleResult();
                debug(6, "DownloadQueryRunner :: ruleResult - " + ruleResult);
                lastScanTime = getRuleCompliance.getLastScanTime();
                scanTimeMap = getRuleCompliance.getScanTimeMap(); 
                cveidsOsMap = getRuleCompliance.getCveIdsOSmap();
                debug(6, "DownloadQueryRunner :: lastScanTime - " + lastScanTime);
                contentIdSet = getRuleCompliance.getContentIdSet();
                debug(5, "DownloadQueryRunner :: contentIdSet - " + contentIdSet);
                contentInfoMap = getRuleCompliance.getContentInfoMap();
                debug(5, "DownloadQueryRunner :: contentInfoMap - " + contentInfoMap);
                profileInfoMap = getRuleCompliance.getProfileInfoMap();
                debug(5, "DownloadQueryRunner :: profileInfoMap - " + profileInfoMap);
                wholeResult = getFinalResult(ruleResult, contentInfoMap, profileInfoMap, scanTimeMap, cveidsOsMap, filtersMap);
                debug(6, "DownloadQueryRunner :: wholeResult - " + wholeResult);
                debug(5, "DownloadQueryRunner :: Processing result done");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public DownloadQueryRunner(String queryCondition, boolean isSingleReport) {
        	this.isSingleReport = isSingleReport;
            GetXCCDFRuleCompliance getXCCDFRuleCompliance = new GetXCCDFRuleCompliance(queryCondition, isSingleReport);
            try {
                runQuery(getXCCDFRuleCompliance);
                wholeResult = getXCCDFRuleCompliance.getXCCDFRuleCompliance();
                if(!isSingleReport) {
                	individualContentsMap = getXCCDFRuleCompliance.getRulesMap();
                	headerArray = getXCCDFRuleCompliance.getHeaderArray();
                }
                debug(6, "DownloadQueryRunner :: wholeResult - " + wholeResult);
                debug(5, "DownloadQueryRunner :: Processing result done");
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        public JSONObject getWholeResult() {
            return wholeResult;
        }
        public Map<String, JSONArray> getIndividaulContentsMapDetails() {
        	return this.individualContentsMap;
        }
        public JSONArray getHeaderArray() {
        	return this.headerArray;
        }
        class GetRuleCompliance extends QueryExecutor {
            Set<Integer> contentIdSet = new HashSet<Integer>();
            GetRuleCompliance() {
                super(main);
            }
            Map<String, JSONObject> map = new HashMap<String, JSONObject>();
            java.sql.Date lastScanTime = null;
            Map<Integer, String> contentInfoMap = new HashMap<Integer, String>();
            Map<Integer, String> profileInfoMap = new HashMap<Integer, String>();
            Map<String, String> scanTimeMap = new LinkedHashMap<String, String>();
            Map<String, List<String>> cveidsOSmap = new LinkedHashMap<String, List<String>>();

            public List<String> fetchCveIdList(String contentTitle) {
              List<String> cveidsList = new ArrayList<String>();
              String osName = "";
              String osTitle = contentTitle.substring(0, contentTitle.indexOf("OVAL") - 1);
              debug(1, "DebugInfo: contentTitle: " + contentTitle + ", --> " + osTitle);
              try {
                File rootDir = main.getDataDirectory();
                ConfigProps config = new ConfigProps(new File(rootDir, CVEID_OS_MAPPING_FILE));
                if(null != config) {
                    String sizeStr = config.getProperty("os.size");
                    int size = (sizeStr !=null) ? Integer.parseInt(sizeStr) : 0;
                    for(int i = 1; i <= size; i++) {
                        osName = config.getProperty("profile.os" + i + ".name");
                        if (osName != null && (contentTitle.startsWith(osName) && osTitle.equalsIgnoreCase(osName))) {
                           String cveIds = config.getProperty("profile.os" + i + ".cveids");
                            String[] cveArray = cveIds.split(",");
                            if(null != cveArray) {
                                for(String cve:cveArray) {
                                    cve = cve.toUpperCase().trim();
                                    cveidsList.add(cve);
                                }
                            }
                           break;
                        }
                    }
                }
            } catch(Throwable e) {
                if (DEBUG >= 5) {
                    e.printStackTrace();
                }
            }
               debug(1, "DebugInfo: fetCveIdsList() - cveIds: " + cveidsList  + " for OS: " + osName);
               return cveidsList;
            }

            protected void execute(IStatementPool pool) throws SQLException {

                if (queryCondition != null) query = query + " where " + queryCondition;
                debug("GetRuleCompliance :: execute(), query: " + query);
                PreparedStatement st = pool.getConnection().prepareStatement(query);
                ResultSet rs = st.executeQuery();
                try {
                    int rowCount = 0;
                    while (rs.next()) {
                        rowCount++;
                        byte[] rulesCompliance = rs.getBytes("rules_compliance");
                        String machineName = rs.getString("machine_name");
                        int contentId = rs.getInt("content_id");
                        int profileId = rs.getInt("profile_id");
                        String contentName = rs.getString("content_name");
                        String contentTitle = rs.getString("content_title");
                        String profileName = rs.getString("profile_name");
                        String profileTitle = rs.getString("profile_title");
                        java.sql.Timestamp lastUpdatedTimeStamp = rs.getTimestamp("finished_at");
                        lastScanTime = new java.sql.Date(lastUpdatedTimeStamp.getTime());
                        scanTimeMap.put(machineName, formatLastScanTime(lastScanTime));
                        List<String> cveidList = fetchCveIdList(contentTitle);
                        cveidsOSmap.put(machineName, cveidList);
                        contentInfoMap.put(contentId, contentName + ":_:" + contentTitle);
                        profileInfoMap.put(profileId, profileName + ":_:" + profileTitle);
                        contentIdSet.add(contentId);
                        String machinePlusContentId = machineName + ":_:" + contentId + ":_:"+ profileId;
                        ByteArrayInputStream bInput = new ByteArrayInputStream(rulesCompliance);
                        StringBuilder sb = new StringBuilder();
                        int num;
                        while ((num = bInput.read()) != -1) {
                            sb.append((char) num);
                        }
                        bInput.close();
                        bInput = null;
                        rulesCompliance = null;
                        try {
                            JSONObject jsonObject = new JSONObject(sb.toString());
                            sb.setLength(0);
                            
                            JSONObject jsonObject1 = jsonObject.has("rules_compliance") ? jsonObject.getJSONObject("rules_compliance") : null;
                            if (jsonObject1 == null) continue;
                            if(null != map.get(machinePlusContentId)) {
                            	// some case machine has different rules compliance from different collection but other data are same. In this case should be handle by vInspector side
                            	// as of now, to show all reports on multiple reports mode
                            	if(!isSingleReport) {
                            		machinePlusContentId = machinePlusContentId + "_MULTIREPORT_DUPLICATE";
                            	}
                            }
                            map.put(machinePlusContentId, jsonObject1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    debug("GetRuleCompliance :: execute(), size of contents map : " + map.size());
                } catch (SQLException sex) {
                    sex.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    rs.close();
                    st.close();
                }
            }
            public Map<String, JSONObject> getRuleResult() {
                return map;
            }

            public String getLastScanTime() {
            	if( lastScanTime != null ){
                    return WebUtil.getComplianceDateFormat().format( lastScanTime );
                }
                return "";
            }

            public String formatLastScanTime(java.sql.Date lastScanTime) {
            	if( lastScanTime != null ){
                    return WebUtil.getComplianceDateFormat().format( lastScanTime );
                }
                return "";
            }

            public Integer[] getContentIdSet() {
                return contentIdSet.toArray(new Integer[]{});
            }

            public Map<Integer, String> getContentInfoMap() {
                return contentInfoMap;
            }

            public Map<Integer, String> getProfileInfoMap() {
                return profileInfoMap;
            }

            public Map<String, String> getScanTimeMap() {
                return scanTimeMap;
            }

            public Map<String, List<String>> getCveIdsOSmap() {
                return cveidsOSmap;
            }
        }

        class GetXCCDFRuleCompliance extends QueryExecutor {

			JSONArray resultJsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            String queryCondition;
            Map<String, JSONArray> rulesMap = new HashMap<String, JSONArray>();
            JSONArray headerArray = new JSONArray();
            boolean isSingleReport = false;
            GetXCCDFRuleCompliance(String queryCondition, boolean isSingleReport) {
                super(main);
                this.queryCondition = queryCondition;
                this.isSingleReport = isSingleReport;
            }
            public Map<String, JSONArray> getRulesMap() {
				return rulesMap;
			}

			public void setRulesMap(Map<String, JSONArray> rulesMap) {
				this.rulesMap = rulesMap;
			}

			public JSONArray getHeaderArray() {
				return headerArray;
			}

			public void setHeaderArray(JSONArray headerArray) {
				this.headerArray = headerArray;
			}

            protected void execute(IStatementPool pool) throws SQLException {
                String sql = "select * from inv_security_xccdf_compliance sxc, inv_security_xccdf_rule_details sxrd  \n" +
                        "where sxc.content_id =sxrd.content_id ";
                if (queryCondition != null) sql = sql + " and ( " + queryCondition +")";
                debug("GetXCCDFRuleCompliance :: execute(), sql - " + sql);
                Map<String, JSONObject> map = new HashMap<String, JSONObject>();
                PreparedStatement st = pool.getConnection().prepareStatement(sql);
                ResultSet rs = st.executeQuery();
                try {
                    while (rs.next()) {
                        byte[] rulesCompliance = rs.getBytes("rules_compliance");
                        String machineName = rs.getString("machine_name");
                        int contentId = rs.getInt("content_id");

                        String machinePlusContentId = machineName + ":_:" + contentId;
                        if (map.get(machinePlusContentId) == null) {
                            ByteArrayInputStream bInput = new ByteArrayInputStream(rulesCompliance);
                            StringBuilder sb = new StringBuilder();
                            int num;
                            while ((num = bInput.read()) != -1) {
                                sb.append((char) num);
                            }
                            try {
                                JSONObject jsonObject = new JSONObject(sb.toString());
                                JSONObject jsonObject1 = jsonObject.has("rules_compliance") ? jsonObject.getJSONObject("rules_compliance") : null;
                                if (jsonObject1 == null) continue;
                                map.put(machinePlusContentId, jsonObject1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            bInput.close();
                            bInput = null;
                        }
                        rulesCompliance = null;
                        JSONObject jsonObject = map.get(machinePlusContentId);
                        String machineDomain = rs.getString("machine_domain");
                        String contentTitle = rs.getString("content_title");
                        String profileTitle = rs.getString("profile_title");
                        String ruleName = rs.getString("rule_name");
                        String ruleTitle = rs.getString("rule_title");
                        String severity = rs.getString("rule_severity");
                        String fix = rs.getString("rule_fix_script");
                        String status = null;
                        if (jsonObject != null && jsonObject.has(ruleName)) {
                            status = jsonObject.getString(ruleName);
                        } else {
                            continue;
                        }
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.put(machineName);
                        jsonArray.put(machineDomain == null ? "" : machineDomain);
                        jsonArray.put(contentTitle == null ? "" : contentTitle);
                        jsonArray.put(profileTitle == null ? "" : profileTitle);
                        jsonArray.put(ruleName == null ? "" : ruleName);
                        jsonArray.put(ruleTitle == null ? "" : ruleTitle);
                        jsonArray.put(severity == null ? "" : severity);
                        jsonArray.put(status == null ? "" : status);
                        jsonArray.put(fix == null ? "" : fix);
                        resultJsonArray.put(jsonArray);
                        if(!isSingleReport) {
	                        if(null != rulesMap.get(machinePlusContentId)) {
	                        	JSONArray rulesArray = rulesMap.get(machinePlusContentId);
	                        	rulesArray.put(jsonArray);
	                        	rulesMap.put(machinePlusContentId, rulesArray);
	                        } else {
	                        	rulesMap.put(machinePlusContentId, jsonArray);
	                        }
                        }
                    }
                } catch (SQLException sex) {
                    sex.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    rs.close();
                    st.close();
                }
                jsonObject.put("header", getRuleColumns());
                if(!isSingleReport) {
                	headerArray = getRuleColumns();
                }
                jsonObject.put("body", resultJsonArray);
            }
            public JSONObject getXCCDFRuleCompliance() {
                return jsonObject;
            }
            
            private JSONArray getRuleColumns() {
                JSONArray jsonArray = new JSONArray();
                jsonArray.put("Machine Name");
                jsonArray.put("Machine Domain");
                jsonArray.put("Content Title");
                jsonArray.put("profileTitle");
                jsonArray.put("Rule Name");
                jsonArray.put("Rule Title");
                jsonArray.put("Severity");
                jsonArray.put("Status");
                jsonArray.put("Fix");
                jsonArray.put("LastScanTime");
                return jsonArray;
            }
        }

        private JSONArray getCVEColumns() {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("Machine");
            jsonArray.put("Content");
            jsonArray.put("Profile");
            jsonArray.put("Reference");
            jsonArray.put("Reference URL");
//            jsonArray.put("CVSS Version");
            jsonArray.put("CVSS Score");
//            jsonArray.put("CVSS Base Score");
//            jsonArray.put("CVSS Impact Score");
//            jsonArray.put("CVSS Exploit Score");
            jsonArray.put("Severity");
            jsonArray.put("Definition ID");
            jsonArray.put("Definition Title");
            jsonArray.put("Definition Description");
            jsonArray.put("Definition Class");
            jsonArray.put("Result");
            jsonArray.put("Solution");
            jsonArray.put("LastScanTime");
            return jsonArray;
        }

        private JSONArray getCVEColumns(List<String> filteredColumns) {
            JSONArray jsonArray = new JSONArray();
            if (filteredColumns.contains("machineName")) {jsonArray.put("Machine");}
            if (filteredColumns.contains("contentTitle")) {jsonArray.put("Content");}
            if (filteredColumns.contains("profileTitle")) {jsonArray.put("Profile");}
            if (filteredColumns.contains("referenceName")) {jsonArray.put("Reference");}
            if (filteredColumns.contains("referenceURL")) {jsonArray.put("Reference URL");}
            if (filteredColumns.contains("cvssScore")) {jsonArray.put("CVSS Score");}
            if (filteredColumns.contains("severity")) {jsonArray.put("Severity");}
            if (filteredColumns.contains("definitionName")) {jsonArray.put("Definition ID");}
            if (filteredColumns.contains("definitionTitle")) {jsonArray.put("Definition Title");}
            if (filteredColumns.contains("definitionDesc")) {jsonArray.put("Definition Description");}
            if (filteredColumns.contains("definitionClass")) {jsonArray.put("Definition Class");}
            if (filteredColumns.contains("definitionResult")) {jsonArray.put("Result");}
            if (filteredColumns.contains("solution")) {jsonArray.put("Solution");}
            jsonArray.put("LastScanTime");
            return jsonArray;
        }

        private JSONArray getDefinitionColumns() {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put("Machine");
            jsonArray.put("Content");
            jsonArray.put("Profile");
            jsonArray.put("Reference");
            jsonArray.put("Reference URL");
            jsonArray.put("Severity");
            jsonArray.put("Definition ID");
            jsonArray.put("Definition Title");
            jsonArray.put("Definition Description");
            jsonArray.put("Definition Class");
            jsonArray.put("Result");
            return jsonArray;
        }

//        public JSONObject getFinalresult(Map<String, JSONObject> ruleResult, Map<Integer, String> contentInfoMap, Map<Integer, String> profileInfoMap) {
//            return getFinalresult(ruleResult, contentInfoMap, profileInfoMap, null);
//        }

        public JSONObject getFinalResult(Map<String, JSONObject> ruleResult, Map<Integer, String> contentInfoMap, Map<Integer, String> profileInfoMap,
                                         Map<String, String> scanTimeInfoMap, Map<String, List<String>> cveidsOsMap, Map<String, List<String>> filtersMap) {
            debug(6, "DownloadQueryRunner :: getFinalresult(), ruleResult - " + ruleResult);
            if (ruleResult != null) {
                debug(5, "DownloadQueryRunner :: getFinalresult(), ruleResult.size() - " + ruleResult.size());
            } else {
                debug(5, "DownloadQueryRunner :: getFinalresult(), ruleResult NULL");
            }
            debug(5, "DownloadQueryRunner :: getFinalresult(), contentInfoMap - " + contentInfoMap);
            debug(5, "DownloadQueryRunner :: getFinalresult(), profileInfoMap - " + profileInfoMap);
            debug(5, "DownloadQueryRunner :: getFinalresult(), scanTimeInfoMap - " + scanTimeInfoMap);
            debug(5, "DownloadQueryRunner :: getFinalresult(), filtersMap - " + filtersMap);
            JSONObject wholeCVEResult = new JSONObject();
            
            Map<String, List<OvalDetailBean>> ovalDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalDefinitionDetails();
            if (ovalDefinitionDetails == null) {
                SCAPUtils.getSCAPUtils().loadOvalDefinitionDetails(main);
                ovalDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalDefinitionDetails();
            }
            if(null != ovalDefinitionDetails) {
            	debug(5, "DownloadQueryRunner :: getFinalresult(), ovalDefinitionDetails.size() - " + ovalDefinitionDetails.size());
        	}

            debug(7, "DownloadQueryRunner :: getFinalresult(), ovalDefinitionDetails - " + ovalDefinitionDetails);

            Map<String, List<OvalDetailBean>> ovalCVEDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalCVEDefinitionDetails();
            if (ovalCVEDefinitionDetails == null) {
                SCAPUtils.getSCAPUtils().loadOvalCVEDefinitionDetails(main);
                ovalCVEDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalCVEDefinitionDetails();
            }
            if(null != ovalCVEDefinitionDetails) {
            	debug(5, "DownloadQueryRunner :: getFinalresult(), ovalCVEDefinitionDetails.size() - " + ovalCVEDefinitionDetails.size());
            }

            debug(7, "DownloadQueryRunner :: getFinalresult(), ovalCVEDefinitionDetails - " + ovalCVEDefinitionDetails);

            JSONArray  ovalCVEDefinitionDetailsArray = new JSONArray();
            int count = 0;

            for (Map.Entry<String, JSONObject> entry : ruleResult.entrySet()) {
                String machinePlusContentId = entry.getKey();
                if(!isSingleReport) {
                    int index = machinePlusContentId.indexOf("_MULTIREPORT_DUPLICATE");
                    if(index > 0) {
                    	machinePlusContentId = machinePlusContentId.substring(0, index);
                    }
                }
                debug(5, "DownloadQueryRunner :: getFinalresult(), machinePlusContentId - " + machinePlusContentId);
                String contentIdPlusProfileId = machinePlusContentId.substring(machinePlusContentId.indexOf(":_:") + 3);
                String machineName = machinePlusContentId.substring(0, machinePlusContentId.indexOf(":_:"));
                String contentId = contentIdPlusProfileId.substring(0, contentIdPlusProfileId.indexOf(":_:"));
                String profileId = contentIdPlusProfileId.substring(contentIdPlusProfileId.indexOf(":_:") + 3);
                String contentName = contentInfoMap.get(Integer.parseInt(contentId)).substring(0, contentInfoMap.get(Integer.parseInt(contentId)).indexOf(":_:"));
                String profileName = profileInfoMap.get(Integer.parseInt(profileId)).substring(0, profileInfoMap.get(Integer.parseInt(profileId)).indexOf(":_:"));
                String contentTitle = contentInfoMap.get(Integer.parseInt(contentId)).substring(contentInfoMap.get(Integer.parseInt(contentId)).indexOf(":_:") + 3);
                String profileTitle = profileInfoMap.get(Integer.parseInt(profileId)).substring(profileInfoMap.get(Integer.parseInt(profileId)).indexOf(":_:") + 3);
                JSONObject rules = entry.getValue();

                count = count + 1;
                debug(6, "DownloadQueryRunner :: getFinalresult(), rules - " + rules);

                Iterator<String> iterator = rules.keys();
                JSONArray  detailsArrayForMultipleReport = new JSONArray();

                rulesSize = rules.length();

                this.whiteListCVE = cveidsOsMap.get(machineName);
                debug(1, "DebugInfo: whiteListCVE-IDs - "+whiteListCVE + " for the machineName: " + machineName);

                while (iterator.hasNext()) {
                    String ruleDefinition = iterator.next();
                    debug(5, "DownloadQueryRunner :: getFinalresult(), ruleDefinition - " + ruleDefinition);

                    String rResult = rules.getString(ruleDefinition);
                    debug(5, "DownloadQueryRunner :: getFinalresult(), rResult - " + rResult);

                    List<OvalDetailBean> list = ovalCVEDefinitionDetails.get(contentIdPlusProfileId + ":_:" + ruleDefinition);
                    debug(6, "DownloadQueryRunner :: getFinalresult(), list - " + list);
                    if (list != null) {
                        debug(5, "DownloadQueryRunner :: getFinalresult(), list.size() - " + list.size());
                    } else {
                        debug(5, "DownloadQueryRunner :: getFinalresult(), list NULL");
                    }

                    if (list != null) {
                        OvalDetailBean finalOvalDetailBean = new OvalDetailBean();
                        finalOvalDetailBean = list.get(0);
                        finalOvalDetailBean.setMachineName(machineName);
                        finalOvalDetailBean.setDefinitionResult(rResult);
                        finalOvalDetailBean.setLastScanTime(scanTimeInfoMap.get(machineName));

                        if(filtersMap != null) {
                            if ((filtersMap.get("filteredSeverity").contains(finalOvalDetailBean.getSeverity().toLowerCase())) ||
                                    (filtersMap.get("filteredSeverity").contains("unknown") && "".equals(finalOvalDetailBean.getSeverity().trim()))) {
                                //severity filter allows this rule... lets export it to excel...
                            } else {
                                continue;
                            }
                            if (filtersMap.get("filteredResult").contains(finalOvalDetailBean.getDefinitionResult())) {
                                //result filter allows this rule... lets export it to excel...
                            } else if (filtersMap.get("filteredResult").contains("OTHERS")) {
                                if ("NON-VULNERABLE".equals(finalOvalDetailBean.getDefinitionResult())
                                        || "VULNERABLE".equals(finalOvalDetailBean.getDefinitionResult())
                                        || "NOT-INSTALLED".equals(finalOvalDetailBean.getDefinitionResult())
                                        || "INSTALLED".equals(finalOvalDetailBean.getDefinitionResult())) {
                                    continue;
                                } else {
                                    //result filter allows this rule... lets export it to excel...
                                }
                            } else {
                                continue;
                            }
                        }
                        boolean isSkipFirstCVE = false;
                        if(null != finalOvalDetailBean.getReferenceName()) {
                        	isSkipFirstCVE = isSkippedCVEID(finalOvalDetailBean.getReferenceName(), whiteListCVE);
                        }
                        debug(5, "DownloadQueryRunner :: getFinalresult() isSkipFirstCVE - " + isSkipFirstCVE);
                        if (list.size() > 1) {
                            for (int i = 1; i < list.size(); i++) {
                                OvalDetailBean currentOvalDetailBean = list.get(i);
                                boolean isSkippedCVEID = false;
                                if ((currentOvalDetailBean.getReferenceName() != null) && (currentOvalDetailBean.getReferenceName().trim().length() > 0)) {
                                    if ((finalOvalDetailBean.getReferenceName() != null) && (finalOvalDetailBean.getReferenceName().trim().length() > 0)) {
                                        if (!finalOvalDetailBean.getReferenceName().equalsIgnoreCase(currentOvalDetailBean.getReferenceName())) {
                                        	isSkippedCVEID = isSkippedCVEID(currentOvalDetailBean.getReferenceName(), whiteListCVE);
                                        	if(!isSkippedCVEID) {
                                        		if(!isSkipFirstCVE) {
                                        			finalOvalDetailBean.setReferenceName(finalOvalDetailBean.getReferenceName() + ", " + currentOvalDetailBean.getReferenceName());
                                        		} else {
                                        			finalOvalDetailBean.setReferenceName(currentOvalDetailBean.getReferenceName());
                                        		}
                                        	} else {
                                        		if(isSkipFirstCVE) {
                                        			finalOvalDetailBean.setReferenceName("");
                                        		} 
                                        	}
                                        }
                                    } else {
                                    	isSkippedCVEID = isSkippedCVEID(finalOvalDetailBean.getReferenceName(), whiteListCVE);
                                    	if(!isSkippedCVEID) {
	                                        finalOvalDetailBean.setReferenceName(currentOvalDetailBean.getReferenceName());
                                    	}
                                    }
                                }
                                if ((currentOvalDetailBean.getReferenceURL() != null) && (currentOvalDetailBean.getReferenceURL().trim().length() > 0)) {
                                    if ((finalOvalDetailBean.getReferenceURL() != null) && (finalOvalDetailBean.getReferenceURL().trim().length() > 0)) {
                                        if (!finalOvalDetailBean.getReferenceURL().equalsIgnoreCase(currentOvalDetailBean.getReferenceURL())) {
                                        	if(!isSkippedCVEID) {
                                        		if(!isSkipFirstCVE) {
                                        			finalOvalDetailBean.setReferenceURL(finalOvalDetailBean.getReferenceURL() + ", " + currentOvalDetailBean.getReferenceURL());
                                        		} else {
                                        			finalOvalDetailBean.setReferenceURL(currentOvalDetailBean.getReferenceURL());
                                        		}
                                        	}
                                        }
                                    } else {
                                    	if(!isSkippedCVEID) {
                                    		finalOvalDetailBean.setReferenceURL(currentOvalDetailBean.getReferenceURL());
                                    	}
                                    }
                                }
                                if ((currentOvalDetailBean.getSolution() != null) && (currentOvalDetailBean.getSolution().trim().length() > 0)) {
                                    if ((finalOvalDetailBean.getSolution() != null) && (finalOvalDetailBean.getSolution().trim().length() > 0)) {
                                        if (!finalOvalDetailBean.getSolution().equalsIgnoreCase(currentOvalDetailBean.getSolution())) {
                                        	if(!isSkippedCVEID) {
                                        		if(!isSkipFirstCVE) {
                                        			finalOvalDetailBean.setSolution(finalOvalDetailBean.getSolution() + ", " + currentOvalDetailBean.getSolution());
                                        		} else {
                                        			finalOvalDetailBean.setSolution(currentOvalDetailBean.getSolution());
                                        		}
                                        	}
                                        }
                                    } else {
                                    	if(!isSkippedCVEID) {
                                    		finalOvalDetailBean.setSolution(currentOvalDetailBean.getSolution());
                                    	}
                                    }
                                }
                                isSkipFirstCVE = false;
                                finalOvalDetailBean.setLastScanTime(scanTimeInfoMap.get(machineName));
                            }
                        }
                        boolean isEntrySkipped = false;
                        if(list.size() == 1) {
                            if(null != finalOvalDetailBean) {
                            	debug(5, "DownloadQueryRunner :: getFinalresult() entering into single entry final getReferenceName() : " + finalOvalDetailBean.getReferenceName());
                            }
                            isEntrySkipped = isSkippedCVEID(finalOvalDetailBean.getReferenceName(), whiteListCVE);
                        } else {
                        	if(null == finalOvalDetailBean.getReferenceName() || "".equals(finalOvalDetailBean.getReferenceName().trim())) {
                        		debug(5, "DownloadQueryRunner :: getFinalresult() no entry for getReferenceName() so skip the entry ");
                        		isEntrySkipped = true;
                        	}
                        }
                        debug(5, "DownloadQueryRunner :: getFinalresult() isEntrySkipped - " + isEntrySkipped);
                        if(!isEntrySkipped) {
	                        if (filtersMap != null) {
	                            ovalCVEDefinitionDetailsArray.put(finalOvalDetailBean.getCVEJSONArray(filtersMap.get("filteredColumns")));
	                            if(!isSingleReport ) {
	                            	detailsArrayForMultipleReport.put(finalOvalDetailBean.getCVEJSONArray(filtersMap.get("filteredColumns")));
	                            }
	                        } else {
	                            ovalCVEDefinitionDetailsArray.put(finalOvalDetailBean.getCVEJSONArray());
	                            if(!isSingleReport ) {
	                            	detailsArrayForMultipleReport.put(finalOvalDetailBean.getCVEJSONArray());
	                            }
	                        }
                        }
                    } else {
                        //we don't have any reference associated with this definition, in OVAL xml...
                        OvalDetailBean finalOvalDetailBean = null;
                        if (ovalDefinitionDetails != null) {
                            if (ovalDefinitionDetails.get(contentIdPlusProfileId + ":_:" + ruleDefinition) != null) {
                                finalOvalDetailBean = ovalDefinitionDetails.get(contentIdPlusProfileId + ":_:" + ruleDefinition).get(0);
                            }
                        }

                        if (finalOvalDetailBean == null) {
                            finalOvalDetailBean = new OvalDetailBean();
                            finalOvalDetailBean.setContentId(contentId);
                            finalOvalDetailBean.setContentTitle(contentTitle);
                            finalOvalDetailBean.setProfileTitle(profileTitle);
                            finalOvalDetailBean.setReferenceName("");
                            finalOvalDetailBean.setReferenceURL("");
                            finalOvalDetailBean.setCvssScore("");
                            finalOvalDetailBean.setCvssBaseCore("");
                            finalOvalDetailBean.setCvssImpactScore("");
                            finalOvalDetailBean.setCvssExploitScore("");
                            finalOvalDetailBean.setSeverity("");
                            finalOvalDetailBean.setDefinitionName(ruleDefinition);
                            OVALProfileDefinition ovalProfileDefinition = SCAPUtils.getSCAPUtils().getDefinition(contentName, profileName, ruleDefinition);
                            if (ovalProfileDefinition == null) {
                                finalOvalDetailBean.setDefinitionTitle("");
                                finalOvalDetailBean.setDefinitionDesc("");
                                finalOvalDetailBean.setDefinitionClass("");
                            } else {
                                finalOvalDetailBean.setDefinitionTitle(ovalProfileDefinition.getTitle());
                                finalOvalDetailBean.setDefinitionDesc(ovalProfileDefinition.getDescription());
                                finalOvalDetailBean.setDefinitionClass(ovalProfileDefinition.getClassType());
                            }
                        }

                        finalOvalDetailBean.setMachineName(machineName);
                        finalOvalDetailBean.setDefinitionResult(rResult);
                        finalOvalDetailBean.setSolution("None Reported");
                        finalOvalDetailBean.setLastScanTime(scanTimeInfoMap.get(machineName));

                        if ((finalOvalDetailBean.getReferenceName() == null) ||
                                (finalOvalDetailBean.getReferenceName().trim().length() < 1) ||
                                   (finalOvalDetailBean.getReferenceName().indexOf("cpe") > -1)) {
                            //skip  rules with references to cpe...
                            continue;
                        }


                        // To ignore rule definition if solution as empty and severity, cvs score as empty
                        if ("None Reported".equalsIgnoreCase(finalOvalDetailBean.getSolution())) {
                            if (isNull(finalOvalDetailBean.getSeverity()))
                                finalOvalDetailBean.setSeverity("None Reported");
                            if (isNull(finalOvalDetailBean.getCvssScore()))
                                finalOvalDetailBean.setCvssScore("None Reported");
                        }


                        if(filtersMap != null) {
                            if ((filtersMap.get("filteredSeverity").contains(finalOvalDetailBean.getSeverity().toLowerCase())) ||
                                (finalOvalDetailBean.getSeverity().equalsIgnoreCase("Important")) ||  // RHEL-7 rules count diff fix btw scan and excel report
                                      (filtersMap.get("filteredSeverity").contains("unknown") && "".equals(finalOvalDetailBean.getSeverity().trim()))) {
                                //severity filter allows this rule... lets export it to excel...
                            } else {
                                continue;
                            }
                            if (filtersMap.get("filteredResult").contains(finalOvalDetailBean.getDefinitionResult())) {
                                //result filter allows this rule... lets export it to excel...
                            } else {
                                continue;
                            }

                            boolean isEntrySkipped = isSkippedCVEID(finalOvalDetailBean.getReferenceName(), whiteListCVE);
                            if (!isEntrySkipped) {
                                ovalCVEDefinitionDetailsArray.put(finalOvalDetailBean.getCVEJSONArray(filtersMap.get("filteredColumns")));
                            }
                            if(!isSingleReport ) {
                                if (!isEntrySkipped)
                                    detailsArrayForMultipleReport.put(finalOvalDetailBean.getCVEJSONArray(filtersMap.get("filteredColumns")));
                            }
                        } else {
                            ovalCVEDefinitionDetailsArray.put(finalOvalDetailBean.getCVEJSONArray());
                            if(!isSingleReport ) {
                            	detailsArrayForMultipleReport.put(finalOvalDetailBean.getCVEJSONArray());
                            }
                        }
                    }
                }
                if(!isSingleReport ) {
	                try {
	                	String contentKey = machineName + "::" + profileTitle + "::" + contentTitle;
	                	if(null != individualContentsMap.get(contentKey)) {
	                		contentKey = contentKey + "_MULTIREPORT_DUPLICATE";
	                	} 
	                	individualContentsMap.put(contentKey, detailsArrayForMultipleReport);
	                } catch(Throwable t) {
	                	t.printStackTrace();
	                }
              }
            }

            if(filtersMap != null) {
                wholeCVEResult.put("header", getCVEColumns(filtersMap.get("filteredColumns")));
            } else {
                wholeCVEResult.put("header", getCVEColumns());
            }
            if(!isSingleReport ) {
            	headerArray = wholeCVEResult.getJSONArray("header");
            }

            debug(7, "DownloadQueryRunner :: getFinalresult(), ovalCVEDefinitionDetailsArray - " + ovalCVEDefinitionDetailsArray);
            wholeCVEResult.put("body", ovalCVEDefinitionDetailsArray);

            debug(7, "DownloadQueryRunner :: getFinalresult(), wholeCVEResult - " + wholeCVEResult);
            wholeResult.put("CVEResult", wholeCVEResult);

            debug(6, "DownloadQueryRunner :: getFinalresult(), wholeResult - " + wholeResult);
            return wholeResult;
        }
    }


    private boolean isNull(String s) {
        return s == null || s.trim().length() == 0;
    }

    private boolean isSkippedCVEID(String referenceName, List<String>whiteListCVE) {
    	boolean isSkipped = false;
    	try {
            String cveId = referenceName;
            debug(1, "DebugInfo: isSkippedCVEID() referenceName ==> " + referenceName);
            if(null != cveId) {
            	cveId = cveId.toUpperCase().trim();
            	if(whiteListCVE.contains(cveId)) {
            		isSkipped = true;
            		debug(5, "DownloadQueryRunner :: isSkippedCVEID() whitelist cve id is available so needs to be skip  - " + cveId);
            	} else if ("".equals(cveId)) {
                    isSkipped = true;
                }
        	}
    	} catch(Exception ed) {
    		if (DEBUG >= 5) {
    			ed.printStackTrace();
    		}
    	}
    	return isSkipped;
    }

    private byte[] zipFiles(File files[]) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        byte bytes[] = new byte[2048];

        for (File file : files) {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            zos.putNextEntry(new ZipEntry(file.getName()));

            int bytesRead;
            while ((bytesRead = bis.read(bytes)) != -1) {
                zos.write(bytes, 0, bytesRead);
            }
            zos.closeEntry();
            bis.close();
            fis.close();
        }
        zos.flush();
        baos.flush();
        zos.close();
        baos.close();

        return baos.toByteArray();
    }

    private void forceDebug(String msg) {
        System.out.println("MachineOverallDownloadServlet.java :: [" + new Date().toString() + "] ==> " + msg);
    }

    private void debug(String msg) {
        if (DEBUG >= 5) {
            System.out.println("MachineOverallDownloadServlet.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }

    private void debug(int level, String msg) {
        if (DEBUG >= level) {
            System.out.println("MachineOverallDownloadServlet.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }

    public void destroy() {
        DB ovalDB = SCAPUtils.getSCAPUtils().getOvalDB();
        if (ovalDB != null) {
            try {
                ovalDB.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
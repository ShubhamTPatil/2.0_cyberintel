package com.marimba.apps.securitymgr.servlets;
// Copyright 2019, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File: //depot/ws/products/securitypolicymanager/9.0.02hf_ga/src/java/com/marimba/apps/securitymgr/servlets/ReportGenerationStatusServlet.java $, $Revision: #2 $, $Date: 2020/08/18 $

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.securitymgr.utils.json.JSONArray;
import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.subscriptionmanager.beans.OvalDetailBean;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReportGenerationStatusServlet extends BasicServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
        debug(5, "doPost()...");
        String machineCountStr = "1";
        Properties lastReportDirProps = new Properties();
        try {
            FileInputStream in = new FileInputStream(main.getDataDirectory() + File.separator + "temp" + File.separator + "lastreport.txt");
            lastReportDirProps.load(in);
            in.close();
            machineCountStr = lastReportDirProps.getProperty("current.va-definition-results.machineCount", "1");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int machineCount = 1;
        try {
            machineCount = Integer.parseInt(machineCountStr);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String resultsDirPath = main.getDataDirectory().getAbsolutePath() + "persist" + File.separator + "temp";
        resultsDirPath = lastReportDirProps.getProperty("last.report.dir", main.getDataDirectory().getAbsolutePath() + "persist" + File.separator + "temp");

forceDebug("ReportGenerationStatusServlet doPost() -> resultsDirPath = " + resultsDirPath);
        File resultsDir = new File(resultsDirPath);
        JSONObject jsonResult = new JSONObject();
        if (resultsDir.exists()) {
            try {
                String[] statusList = resultsDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".txt");
                    }
                });
                jsonResult.put("result", true);
                JSONArray reportStatusArray = new JSONArray();
                jsonResult.put("message", "Report generation is triggered successfully, and will be available at " + resultsDirPath + " in some time." +
                        "\n\nAlso, please refer to status file(s) for more information.");
                for (int i = 0; i < statusList.length; i++) {
                    JSONObject statusObject = new JSONObject();
                    Properties properties = new Properties();
                    FileInputStream in = new FileInputStream(new File(resultsDir.getAbsolutePath() + File.separator + statusList[i]));
                    String reportIndexStr = statusList[i].replace("status.","").replace(".txt", "");
                    properties.load(in);
                    in.close();
                    statusObject.put("machine", properties.getProperty("machine"));
                    statusObject.put("status", properties.getProperty("report." + reportIndexStr + ".status",properties.getProperty("status")));
                    File reportFile = new File(resultsDir.getAbsolutePath(), properties.getProperty("report.file"));
                    if (reportFile.exists()) {
                        statusObject.put("report-size", String.valueOf(reportFile.length()));
                    }
                    reportStatusArray.put(statusObject);
                }
                jsonResult.put("report.status.array", reportStatusArray);
            } catch (Throwable ex) {
            	if (DEBUG >= 5) {
            		ex.printStackTrace();
            	}
                jsonResult.put("result", false);
                jsonResult.put("message", "Status of excel report tracking failed. Reason - " + ex.getMessage());
            }
        }
        PrintWriter out = response.getWriter();
//forceDebug("ReportGenerationStatusServlet doPost() " + jsonResult.toString());
        out.println(jsonResult.toString());
        out.flush();
    }

    private void forceDebug(String msg) {
        System.out.println("ReportGenerationStatusServlet.java :: [" + new Date().toString() + "] ==> " + msg);
    }

    private void debug(String msg) {
        if (DEBUG >= 5) {
            System.out.println("ReportGenerationStatusServlet.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }

    private void debug(int level, String msg) {
        if (DEBUG >= level) {
            System.out.println("ReportGenerationStatusServlet.java :: [" + new Date().toString() + "] ==> " + msg);
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
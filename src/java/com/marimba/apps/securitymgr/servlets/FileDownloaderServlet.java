// Copyright 2018, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.servlets;

import com.marimba.apps.securitymgr.utils.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: May 19, 2017
 * Time: 2:58:12 PM
 * To change this template use File | Settings | File Templates.
 */

public class FileDownloaderServlet extends BasicServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("FileDownloaderServlet.doGet");
        this.doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);

        String docType = request.getParameter("type");
        String jsonData = request.getParameter("data");

        if (docType == null || docType.trim().isEmpty() || null == jsonData || jsonData.trim().isEmpty()) {
            resultObject.put("result", false);
            resultObject.put("message", "Unable to read json data from request.");
            PrintWriter pout = response.getWriter();
            pout.println(resultObject.toString());
            pout.flush();
            return;
        }
        String path = request.getParameter("path");
        String ranat = request.getParameter("ranat");
        String target = request.getParameter("target");
        String rantime = request.getParameter("rantime");
        String clientTime = request.getParameter("time");

        JSONObject wholeObj = new JSONObject(jsonData);
        File file = createFile("report", docType);
        OutputStream out = response.getOutputStream();
        if (null != file) {
            try {
                boolean contentPopulated = false;
                String mimeType = "application/octet-stream";
                Map<String, String> dataMap = new HashMap<String, String>(4);
                dataMap.put("target", target);
                dataMap.put("ranat", ranat);
                dataMap.put("rantime", rantime);
                dataMap.put("path", path);
                if ("pdf".equals(docType)) {
                    mimeType = "application/pdf";
                    contentPopulated = populatePDFFile(file, dataMap, wholeObj);
                }
                if ("xls".equals(docType)) {
                    contentPopulated = populateExcelFile(file, dataMap, wholeObj);
                    mimeType = "application/vnd.ms-excel";
                }
                if (contentPopulated) {
                    InputStream in = new FileInputStream(file);

                    response.setContentType(mimeType);
                    response.setContentLength((int) file.length());
                    String formatedFileName = target + "_" + getFormatedTime(clientTime) + "." + docType;
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + formatedFileName + "\"");
                    response.setHeader("Set-Cookie", "fileDownload=true; path=/");
                    response.setHeader("fileName", file.getName());
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                } else {
                    debug("Unable to populate content in respective file");
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        } else {
            debug("Unable to create file");
        }
        if (null != file) file.delete();
        out.flush();
    }


    private void debug(String msg) {
        System.out.println("FileDownloaderServlet: " + msg);
    }
}

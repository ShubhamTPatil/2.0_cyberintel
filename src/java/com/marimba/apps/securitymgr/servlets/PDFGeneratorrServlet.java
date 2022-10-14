// Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.securitymgr.servlets;

import com.marimba.pdf.PDFGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * PDFGeneratorrServlet, Generate PDF files from the content we supplied (content can be either in normal text or HTML)
 *
 * @author	Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 *
 */

public class PDFGeneratorrServlet extends BasicServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        debug("doGet() called");
        this.doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        debug("doPost() called");
        super.doPost(request, response);
        OutputStream out = response.getOutputStream();

        String targetName = "sample_target";
        String htmlContent = request.getParameter("html");
        if (null == htmlContent || htmlContent.trim().isEmpty()) htmlContent = "Empty Content";
        debug("HTML Content: " + htmlContent);

        File file = createFile("report");
        OutputStream outPdfFile = new FileOutputStream(file);
        try {
            PDFGenerator generator = new PDFGenerator(file.getAbsolutePath(), htmlContent);
            generator.generateFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            outPdfFile.close();
        }
        InputStream in = new FileInputStream(file);

        String formatedFileName = targetName + ".pdf";
        response.setContentType("application/pdf");
        response.setContentLength((int) file.length());
        response.setHeader("fileName", file.getName());
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + formatedFileName + "\"");

        int length;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        in.close();
    }

    protected File createFile(String filePrefix) {
        File dataDir = (File) context.getAttribute("com.marimba.servlet.context.data");
        StringBuilder strBuilder = new StringBuilder();
        long currentTime = System.currentTimeMillis();
        strBuilder.append(filePrefix).append("_").append(currentTime).append(".pdf");
        String fileName = strBuilder.toString();
        File file = new File(dataDir + File.separator + "temp" + File.separator + currentTime + File.separator + fileName);
        try {
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) file = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void debug(String msg) {
        System.out.println("PDFGeneratorrServlet: " + msg);
    }
}

// Copyright 2018-20, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.servlets;

import com.marimba.apps.securitymgr.utils.json.JSONArray;
import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.intf.castanet.IFile;
import com.marimba.pdf.PDFGenerator;
import com.marimba.tools.util.DebugFlag;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CreationHelper;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: May 19, 2017
 * Time: 2:59:40 PM
 * To change this template use File | Settings | File Templates.
 */

public class BasicServlet extends HttpServlet {
    public static final int DEBUG = DebugFlag.getDebug("SECURITY");
    static final int BUFFER_SIZE = 4096;
    protected SubscriptionMain main;
    protected ServletContext context;
    protected JSONObject resultObject;
    protected HttpServletRequest request;

    public void init(ServletConfig servletConfig) throws ServletException {
        this.context = servletConfig.getServletContext();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.resultObject = new JSONObject();
        response.setContentType("application/json; charset=utf-8");
        response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        this.request = request;
        this.main = TenantHelper.getTenantSubMain(context, request);
    }

    protected File createFile(String filePrefix, String fileType) {
        File dataDir = main.getDataDirectory();
        StringBuilder strBuilder = new StringBuilder();
        long currentTime = System.currentTimeMillis();
        strBuilder.append(filePrefix).append("_").append(fileType).append("_").append(currentTime).append(".").append(fileType.toLowerCase());
        String fileName = strBuilder.toString();
        File file = new File(dataDir + File.separator + "temp" + File.separator + currentTime + File.separator + fileName);
        try {
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) file = null;
        } catch (IOException e) {
        	if(DEBUG >= 5) {
        		e.printStackTrace();
        	}
        }
        return file;
    }
    protected File createReportDirectory() {
        File dataDir = main.getDataDirectory();
        long currentTime = System.currentTimeMillis();
        File file = new File(dataDir + File.separator + "temp" + File.separator + currentTime);
        try {
            file.mkdirs();
        } catch (Exception e) {
        	if(DEBUG >= 5) {
        		e.printStackTrace();
        	}
        }
        return file;
    }
    protected void updateLastReport(File vaDefinitionResultsDir, Properties lastReportDirProps) {
    	try {
    		FileOutputStream lastReportDirPropsOut = new FileOutputStream(vaDefinitionResultsDir.getParentFile().getAbsolutePath() + File.separator + "lastreport.txt");
            lastReportDirProps.store(lastReportDirPropsOut,"");
            lastReportDirPropsOut.flush();
            lastReportDirPropsOut.close();
    	} catch(Throwable e) {
    		if (DEBUG >= 5) {
    			e.printStackTrace();
    		}
    	}
    }

    protected boolean generatePDFFile(File file, Map<String, String> dataMap, JSONObject wholeObj) {
        debug(5, "generatePDFFile(), file - " + file);
        debug(5, "generatePDFFile(), dataMap - " + dataMap);
        debug(5, "generatePDFFile(), wholeObj - " + wholeObj);
        IFile channelFile = main.getChannel().getFile("/templates/report-mail.vm");
        if (null == channelFile) return false;
        String templateStr = "";
        try {
            templateStr = IOUtils.toString(channelFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JSONArray headerData = wholeObj.getJSONArray("header");
        JSONArray bodyData = wholeObj.getJSONArray("body");

        boolean pdfFileGenerated = false;
        try {

            Document document = new Document(PageSize.LETTER);
            PdfWriter pdfWriter = PdfWriter.getInstance
                 (document, new FileOutputStream(new File(file.getAbsolutePath())));

              document.setPageSize(PageSize.A1);
              document.setMargins(50, 45, 50, 40);
              document.setMarginMirroring(false);

               document.open();

                document.addAuthor("Harman - Marimba");
                document.addCreator("Harman - Marimba");
                document.addSubject("VScan Report File");

                document.addCreationDate();
                document.addTitle("Harman - Marimba VDesk Application Report");

            setPdfTitleContent(dataMap, document);
            List<String> headerList = getPdfHeaderContent(headerData);
            setPdfTableBodyContent(bodyData, headerList, document);

            document.close();
            pdfWriter.close();
            pdfFileGenerated = true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return pdfFileGenerated;
    }
    
    protected boolean populatePDFFile(File file, Map<String, String> dataMap, JSONObject wholeObj) {
        debug(5, "populatePDFFile(), file - " + file);
        debug(5, "populatePDFFile(), dataMap - " + dataMap);
        debug(5, "populatePDFFile(), wholeObj - " + wholeObj);
        IFile channelFile = main.getChannel().getFile("/templates/report-mail.vm");
        if (null == channelFile) return false;
        String templateStr = "";
        try {
            templateStr = IOUtils.toString(channelFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JSONArray headerData = wholeObj.getJSONArray("header");
        JSONArray bodyData = wholeObj.getJSONArray("body");

        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("titleContent", getTitleContent(dataMap));
        valuesMap.put("headerContent", getHeaderContent(headerData));
        valuesMap.put("bodyContent", getBodyContent(bodyData));
        String resolvedString = new StrSubstitutor(valuesMap).replace(templateStr);

        debug(5, "populatePDFFile(), resolvedString - " + resolvedString);
        PDFGenerator pdfGenerator = new PDFGenerator(file.getAbsolutePath(), resolvedString);
        String pdfPageSize = dataMap.get("pdfpagesize");
        if (null != pdfPageSize && !pdfPageSize.trim().isEmpty()) {
            pdfGenerator.setPageSize(pdfPageSize);
        }
        boolean fileGenerated = pdfGenerator.generateFile();
        debug(5, "populatePDFFile(), file.getAbsolutePath() - " + file.getAbsolutePath());
        debug(5, "populatePDFFile(), fileGenerated - " + fileGenerated);
        return fileGenerated;
    }

    protected boolean populateHTMLFile(File file, Map<String, String> dataMap, JSONObject wholeObj) {
        debug(5, "populateHTMLFile(), file - " + file);
        debug(5, "populateHTMLFile(), dataMap - " + dataMap);
        debug(5, "populateHTMLFile(), wholeObj - " + wholeObj);
        IFile channelFile = main.getChannel().getFile("/templates/report-mail.vm");
        if (null == channelFile) return false;
        String templateStr = "";
        try {
            templateStr = IOUtils.toString(channelFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        JSONArray headerData = wholeObj.getJSONArray("header");
        JSONArray bodyData = wholeObj.getJSONArray("body");

        Map<String, String> valuesMap = new HashMap<String, String>();
        valuesMap.put("titleContent", getTitleContent(dataMap));
        valuesMap.put("headerContent", getHeaderContent(headerData));
        valuesMap.put("bodyContent", getHtmlTableBodyContent(bodyData));
        String resolvedString = new StrSubstitutor(valuesMap).replace(templateStr);
        debug(5, "populateHTMLFile(), resolvedString - " + resolvedString);
        boolean fileGenerated = false;
        try {
            FileOutputStream fout = new FileOutputStream(new File(file.getAbsolutePath()));
            fout.write(resolvedString.getBytes());
            fout.close();
            fileGenerated = true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        debug(5, "populateHTMLFile(), file.getAbsolutePath() - " + file.getAbsolutePath());
        debug(5, "populateHTMLFile(), fileGenerated - " + fileGenerated);
        return fileGenerated;
    }

    protected boolean populateExcelFile(File file, Map<String, String> dataMap, JSONObject wholeObj) {
        debug(5, "populateExcelFile(), file - " + file);
        debug(5, "populateExcelFile(), dataMap - " + dataMap);
        debug(5, "populateExcelFile(), wholeObj - " + wholeObj);
        JSONArray headerData = wholeObj.getJSONArray("header");
        JSONArray bodyData = wholeObj.getJSONArray("body");

        List<String> headerList = getHeaderColumns(headerData);
        List<List<String>> bodyList = getBodyColumns(bodyData);
        debug(5, "populateExcelFile(), headerList.size() - " + headerList.size());
        debug(5, "populateExcelFile(), bodyList.size() - " + bodyList.size());

        int MAX_ENTRIES_PER_SHEET = main.getExcelReportEntriesPerSheet();
        HSSFWorkbook workBook = new HSSFWorkbook();
        HSSFSheet sheet = workBook.createSheet((bodyList.size() > MAX_ENTRIES_PER_SHEET) ? ("Entries 1-" + MAX_ENTRIES_PER_SHEET): ("Entries 1-" + bodyList.size()));
        CreationHelper ch = workBook.getCreationHelper();
        HSSFCellStyle headerStyle = workBook.createCellStyle();
        HSSFFont font = workBook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        headerStyle.setFont(font);

        // For headings alone

        HSSFRow rows0 = sheet.createRow(0);
        HSSFCell cell0 = rows0.createCell(0);

        cell0.setCellValue(dataMap.get("target"));
        cell0.setCellStyle(headerStyle);

        HSSFRow rows1 = sheet.createRow(1);
        HSSFCell cell1 = rows1.createCell(0);

        cell1.setCellValue("Report Generated at: " + dataMap.get("ranat"));
        cell1.setCellStyle(headerStyle);

        HSSFRow rowhead = sheet.createRow(3);
        for (int i = 0; i < headerList.size(); i++) {
            HSSFCell cell = rowhead.createCell(i);
            cell.setCellValue(headerList.get(i));
            cell.setCellStyle(headerStyle);
        }
        int serialNo = 4;
        int sheetId = 1;
        for (List<String> rowContent : bodyList) {
            HSSFRow rows = sheet.createRow(serialNo);
            for (int k = 0; k < rowContent.size(); k++) {
                String content = rowContent.get(k);
                if (content != null && content.length() >= 32766) {
                    content = content.substring(0, 32750) + ".......";
                }
                rows.createCell(k).setCellValue(ch.createRichTextString(content));
            }

            boolean createNewSheet = false;
            if ((sheetId == 1) && (serialNo >= (MAX_ENTRIES_PER_SHEET + 3))) {
                //we are done with adding max allowed rows in sheet number 1 now, but we still have few more rows to process...
                createNewSheet = true;
            } else if ((sheetId > 1) && (serialNo >= MAX_ENTRIES_PER_SHEET)) {
                //we are done with adding max allowed rows in sheet number >=2 now, but we still have few more rows to process...
                createNewSheet = true;
            }
            if ((sheetId * MAX_ENTRIES_PER_SHEET) == (bodyList.size())) {
                //we have covered all rules with current sheet... no need to create a new sheet...
                createNewSheet = false;
            }

            if (createNewSheet) {
                int start = (MAX_ENTRIES_PER_SHEET * sheetId) + 1;
                int end = -1;
                if ((bodyList.size() - (MAX_ENTRIES_PER_SHEET * sheetId)) > MAX_ENTRIES_PER_SHEET) {
                    end = (MAX_ENTRIES_PER_SHEET * (sheetId  + 1));
                } else {
                    end = bodyList.size();
                }
                if (start == end) {
                    sheet = workBook.createSheet("Entry " + start);
                } else {
                    sheet = workBook.createSheet("Entries " + (start + "-" + end));
                }
                sheetId++;
                serialNo = 0;
                rowhead = sheet.createRow(serialNo);
                for (int i = 0; i < headerList.size(); i++) {
                    HSSFCell cell = rowhead.createCell(i);
                    cell.setCellValue(headerList.get(i));
                    cell.setCellStyle(headerStyle);
                }
            }
            serialNo++;
        }
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            workBook.write(fileOut);
            fileOut.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
            return false;
        }
        debug(5, "populateExcelFile(), file.getAbsolutePath() - " + file.getAbsolutePath());
        return true;
    }

    private String getTitleContent(Map<String, String> dataMap) {
        StringBuilder str = new StringBuilder(dataMap.size() * 2);
        str.append("<tr><td><b>").append(dataMap.get("target")).append("</b></td></tr>");
        str.append("<tr><td>&nbsp;</td></tr>");
        str.append("<tr><td>Report Generated at: ").append(dataMap.get("ranat")).append("</td></tr>");
        return str.toString();
    }

    private void setPdfTitleContent(Map<String, String> dataMap, Document document) throws DocumentException{
        Paragraph p1 = new Paragraph();
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12,Font.BOLD, BaseColor.BLACK);
        p1.add(new Chunk((String)dataMap.get("target"), font));
        p1.add(Chunk.NEWLINE);
        p1.add(new Chunk("Report Generated at: " + (String) dataMap.get("ranat"),font));
        document.add(p1);
    }

    private String getHeaderContent(JSONArray headerData) {
        StringBuilder str = new StringBuilder(headerData.length() * 2);
        str.append("<tr>");
        for (int i = 0; i < headerData.length(); i++) {
            str.append("<th>").append(headerData.get(i)).append("</th>").append(System.lineSeparator());
        }
        str.append("</tr>");
        return str.toString();
    }

    private List<String> getPdfHeaderContent(JSONArray headerData) {
        List<String> headerVal = new ArrayList<String>();
        for (int i = 0; i < headerData.length(); i++) {
            headerVal.add(headerData.get(i).toString());
        }
        return headerVal;
    }


    private String getBodyContent(JSONArray bodyData) {
        StringBuilder str = new StringBuilder(bodyData.length() * 2);
        for (int i = 0; i < bodyData.length(); i++) {
            str.append("<tr class=\"").append(i % 2 == 0 ? "even" : "odd").append("\">");
            JSONArray bodySubData = (JSONArray) bodyData.get(i);
            for (int j = 0; j < bodySubData.length(); j++) {
                String data = bodySubData.getString(j);
                str.append("<td>").append((data == null || data.trim().isEmpty()) ? "&nbsp;" : data).append("</td>");
            }
            str.append("</tr>").append(System.lineSeparator());
        }
        return str.toString();
    }

    private String escapeHtml(String string) {
        StringBuilder escapedTxt = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char tmp = string.charAt(i);
            switch (tmp) {
            case '<':
                escapedTxt.append("&lt;");
                break;
            case '>':
                escapedTxt.append("&gt;");
                break;
            case '&':
                escapedTxt.append("&amp;");
                break;
            case '"':
                escapedTxt.append("&quot;");
                break;
            default:
                escapedTxt.append(tmp);
            }
        }
        return escapedTxt.toString();
    }

    private String getHtmlTableBodyContent(JSONArray bodyData) {
        List<List<String>> bodyList = getBodyColumns(bodyData);
        StringBuilder str = new StringBuilder(bodyData.length() * 2);
        int i = 0;
        for (List<String> rowContent : bodyList) {
            str.append("<tr class=\"").append(i % 2 == 0 ? "even" : "odd").append("\">");
            for (int k = 0; k < rowContent.size(); k++) {
                String content = rowContent.get(k);
                content = escapeHtml(content);
                if (content != null && content.length() >= 32766) {
                    content = content.substring(0, 32750) + ".......";
                }
                str.append("<td>").append((content == null || content.trim().isEmpty()) ? "&nbsp;" : content.trim()).append("</td>");
            }
            i++;
            str.append("</tr>").append(System.lineSeparator());
        }
        return str.toString();
    }

    private void setPdfTableBodyContent(JSONArray bodyData, List<String> headerList, Document document) throws DocumentException {
        List<List<String>> bodyList = getBodyColumns(bodyData);
        StringBuilder str = new StringBuilder(bodyData.length() * 2);
        int i = 0;
        for (List<String> rowContent : bodyList) {
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100); //Width 100%
            table.setSpacingBefore(10f); //Space before table
            table.setSpacingAfter(10f); //Space after table

            //Set Column widths
            float[] columnWidths = {0.60f, 2.40f};
            table.setWidths(columnWidths);

            for (int k = 0; k < rowContent.size(); k++) {
                String content = rowContent.get(k);

                String colHead = headerList.get(k);
                Font font = new Font(Font.FontFamily.TIMES_ROMAN, 11,Font.BOLD, BaseColor.BLACK);
                PdfPCell cell = new PdfPCell(new Phrase(colHead, font));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);

                if (content != null && content.length() >= 32766) {
                    content = content.substring(0, 32750) + ".......";
                }

                content = (content == null || content.trim().isEmpty()) ? "" : content;
                font = new Font(Font.FontFamily.TIMES_ROMAN, 11,Font.NORMAL, BaseColor.BLACK);
                cell = new PdfPCell(new Phrase(content, font));
                table.addCell(cell);
            }
            document.add(table);
            document.add(Chunk.NEWLINE);
        }
    }

    private List<String> getHeaderColumns(JSONArray headerData) {
        List<String> columns = new LinkedList<String>();
        for (int i = 0; i < headerData.length(); i++) columns.add((String) headerData.get(i));
        return columns;
    }

    private List<List<String>> getBodyColumns(JSONArray bodyData) {
        List<List<String>> columnsList = new LinkedList<List<String>>();
        for (int i = 0; i < bodyData.length(); i++) {
            JSONArray bodySubData = (JSONArray) bodyData.get(i);
            List<String> columnList = new LinkedList<String>();
            for (int j = 0; j < bodySubData.length(); j++) columnList.add((String) bodySubData.get(j));
            columnsList.add(columnList);
        }
        return columnsList;
    }

    public String getFormatedTime(String clientTime) {
        long dateTime = System.currentTimeMillis();
        try {
            dateTime = Long.parseLong(clientTime);
        } catch (Exception ex) {/**/}

        return new SimpleDateFormat("dd-MMM-yy_HH-mm").format(System.currentTimeMillis());
    }

    private void debug(String msg) {
        if (DEBUG >= 5) {
            System.out.println("BasicServlet.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }

    private void debug(int level, String msg) {
        if (DEBUG >= level) {
            System.out.println("BasicServlet.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }
}

// Copyright 2018, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

//$File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.servlets;

import com.marimba.apps.securitymgr.utils.json.JSONObject;
import com.marimba.intf.util.IConfig;
import com.marimba.mail.core.SimpleMailer;
import static com.marimba.mail.intf.IMailerConstants.EMAIL_PROP_BCC_MAILID;
import static com.marimba.mail.intf.IMailerConstants.EMAIL_PROP_RECEIVER_MAILIDS;
import com.marimba.tools.config.ConfigPrefix;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: May 9, 2017
 * Time: 3:13:42 PM
 * To change this template use File | Settings | File Templates.
 */

public class MailSenderServlet extends BasicServlet {

    private File file;
    private String target;
    private String docType;
    private String clientTime;
    private IConfig mailConfig;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        debug("MailSenderServlet.doGet() called forwarding to doPost()");
        this.doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
        debug("Started processing to send mail...");
        boolean mailSent = false;
        String message = "";
        JSONObject resultObject = new JSONObject();
        this.mailConfig = new ConfigPrefix("smtp.", main.getConfig());
        if (mailConfig.getPropertyPairs().length == 0) {
            resultObject.put("result", mailSent);
            resultObject.put("message", "Configure Email settings and try again.");
            PrintWriter out = response.getWriter();
            out.println(resultObject.toString());
            out.flush();
            return;
        }
        docType = request.getParameter("type");
        String jsonData = request.getParameter("data");
        if (docType == null || docType.trim().isEmpty() || null == jsonData || jsonData.trim().isEmpty()) {
            resultObject.put("result", mailSent);
            resultObject.put("message", "Unable to read json data from request.");
            PrintWriter out = response.getWriter();
            out.println(resultObject.toString());
            out.flush();
            return;
        }
        target = request.getParameter("target");
        clientTime = request.getParameter("time");
        String ranat = request.getParameter("ranat");
        String rantime = request.getParameter("rantime");
        String path = request.getParameter("path");

        JSONObject wholeObj = new JSONObject(jsonData);
        this.file = createFile("report", docType);
        try {
            if (null != file) {
                boolean contentPopulated = false;
                Map<String, String> dataMap = new HashMap<String, String>(4);
                dataMap.put("target", target);
                dataMap.put("ranat", ranat);
                dataMap.put("rantime", rantime);
                dataMap.put("path", path);
                if ("pdf".equals(docType)) contentPopulated = populatePDFFile(file, dataMap, wholeObj);
                if ("xls".equals(docType)) contentPopulated = populateExcelFile(file, dataMap, wholeObj);
                if (contentPopulated) mailSent = sendMail();
                else message = "Failed to populate file contents";
            } else {
                message = "Failed to create file in the following location";
            }
        } catch(Exception ex) {
            message = ex.getMessage();
            ex.printStackTrace();
        }
        resultObject.put("result", mailSent);
        resultObject.put("message", message);
        PrintWriter out = response.getWriter();
        out.println(resultObject.toString());
        if (null != file) file.delete();
        out.flush();
    }

    private boolean sendMail() {
        SimpleMailer mailer = new SimpleMailer(mailConfig);
        mailer.setSubject("Clarinet Report");        
        mailer.setContent(getMailContent());
        String toAddresses = mailConfig.getProperty(EMAIL_PROP_RECEIVER_MAILIDS);
        String bccddresses = mailConfig.getProperty(EMAIL_PROP_BCC_MAILID);
        mailer.setAttachFilePath(file.getAbsolutePath());
        mailer.setAttachFileDisplayName(target + "_" + getFormatedTime(clientTime) + "." + docType);
        mailer.setAttachDescription(target + "_" + getFormatedTime(clientTime));
        boolean result = mailer.sendMail(toAddresses, null, bccddresses);
        debug("Send Mail Result : " + result);
        return result;
    }

    private String getMailContent() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\">\n" +
                "<head>\n" +
                "    <style type=\"text/css\">#mrba_mail_tmplt body{margin:0;padding:0;width:100%;text-align:justify;font-size:14px;background-color:#F1F2F7;-webkit-font-smoothing:antialiased;font-family:Verdana,Arial,Helvetica,sans-serif}#mrba_mail_tmplt table{background-color:#FFF;border-collapse:collapse;mso-table-lspace:0;mso-table-rspace:0}#mrba_mail_tmplt table td{border-collapse:collapse}#mrba_mail_tmplt a{text-decoration:none;color:#1179BC;font-weight:700}#mrba_mail_tmplt p{margin:1em 0}h1,h2,h3,h4,h5,h6{color:#000!important}#mrba_mail_tmplt h1 a,h2 a,h3 a,h4 a,h5 a,h6 a{color:#00f!important}#mrba_mail_tmplt h1 a:active,h2 a:active,h3 a:active,h4 a:active,h5 a:active,h6 a:active{color:red!important}#mrba_mail_tmplt h1 a:visited,h2 a:visited,h3 a:visited,h4 a:visited,h5 a:visited,h6 a:visited{color:purple!important}@media only screen and (max-device-width:480px){a[href^=sms],a[href^=tel]{text-decoration:none;color:#000;pointer-events:none;cursor:default}.mobile_link a[href^=sms],.mobile_link a[href^=tel]{text-decoration:default;color:orange!important;pointer-events:auto;cursor:default}}@media only screen and (min-device-width:768px) and (max-device-width:1024px){a[href^=sms],a[href^=tel]{text-decoration:none;color:#00f;pointer-events:none;cursor:default}.mobile_link a[href^=sms],.mobile_link a[href^=tel]{text-decoration:default;color:orange!important;pointer-events:auto;cursor:default}}</style>\n" +
                "</head>\n" +
                "<body id=\"mrba_mail_tmplt\">\n" +
                "<table width=\"80%\" align=\"center\" style=\"border:1px solid #CCCCCC\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "    <tr><td>\n" +
                "        <table width=\"100%\" cellpadding=\"20\" cellspacing=\"20\" style=\"background-color:#1190BC;border-bottom:1px solid #DCDCDC;\">\n" +
                "            <tr><td><span style=\"font-size:20px;color:#ffffff;\"><b>Clarinet</b></span><br><br>\n" +
                "                <span style=\"color:#ffffff;\">A comprehensive and extensible framework for managing Infrastructure Security against vulnerabilities</span></td>\n" +
                "            </tr>\n" +
                "        </table>\n" +
                "    </td></tr>\n" +
                "    <tr><td>&nbsp;</td></tr> <tr><td>&nbsp;</td></tr>\n" +
                "    <tr><td>\n" +
                "        <table width=\"96%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin:20px;\" align=\"center\">\n" +
                "            <tr><td>Hi</td></tr>\n" +
                "            <tr><td>&nbsp;</td></tr>\n" +
                "            <tr><td>Please find attached the Clarinet Security Compliance report.</td></tr>\n" +
                "            <tr><td>&nbsp;</td></tr>\n" +
                "            <tr><td>Regards</td></tr>\n" +
                "            <tr><td>Clarinet Administrator</td></tr>\n" +
                "        </table>\n" +
                "    </td></tr>\n" +
                "    <tr><td>&nbsp;</td></tr> <tr><td>&nbsp;</td></tr>\n" +
                "    <tr><td align=\"center\"><span style=\"font-size: 12px;\">This is an auto generated email. Please do not reply to this email.</span></td></tr>\n" +
                "    <tr><td>&nbsp;</td></tr>\n" +
                "    <tr><td>\n" +
                "        <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid #B4B4B4;background-color: #DCDCDC;font-size: 13px;\">\n" +
                "            <tr><td>&nbsp;</td></tr>\n" +
                "            <tr><td align=\"center\">&copy; 2015-2018 HARMAN International. All Rights Reserved</td></tr>\n" +
                "            <tr><td>&nbsp;</td></tr>\n" +
                "            <tr><td align=\"center\">This message was produced and distributed by Harman International Industries, Incorporated,</td></tr>\n" +
                "            <tr><td align=\"center\">400 Atlantic Street, 15th Floor Stamford, CT 06901, USA.</td></tr>\n" +
                "            <tr><td align=\"center\">Phone: +1.203.328.3500</td></tr>\n" +
                "            <tr><td>&nbsp;</td></tr>\n" +
                "        </table>\n" +
                "    </td></tr>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";
    }

    private void debug(String msg) {
        System.out.println("MailSenderServlet: " + msg);
    }
}

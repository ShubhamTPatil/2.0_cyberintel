// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager;

import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;
import org.apache.struts.util.MessageResources;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
/**
 * Description about the class MailFormatter
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public abstract class MailFormatter implements IPolicyDiffConstants {

    private String createdBy;
    private String createdOn;
    private String targetId;
    private String targetName;
    private String targetType;
    private String createdByMailId;
    private String createdByDispName;
    private StringBuilder mailSubject;
    private StringBuilder mailHeader;
    private StringBuilder mailBody;
    private StringBuilder mailFooter;
    private boolean isPeerApprovalEnabled;

    protected MessageResources mResource;

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedBy() {
        return (createdBy != null) ? createdBy : STR_UNKNOWN;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getTargetId() {
        return (null == targetId) ? targetId : (targetId.equalsIgnoreCase("all")) ? ALL_END_POINTS : targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return (targetName.equalsIgnoreCase("all")) ? ALL_END_POINTS : targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getCreatedByDispName() {
        return createdByDispName;
    }

    public void setCreatedByDispName(String createdByDispName) {
        this.createdByDispName = createdByDispName;
    }

    public String getCreatedByMailId() {
        return createdByMailId;
    }

    public void setCreatedByMailId(String createdByMailId) {
        this.createdByMailId = createdByMailId;
    }

    public void prepare() {
        formatMailSubject();
        formatMailHeader();
        formatBodyHeader();
        formatBodyMessage();
        formatBodyFooter();
        formatMailFooter();
    }

    public StringBuilder getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(StringBuilder mailSubject) {
        this.mailSubject = mailSubject;
    }

    public StringBuilder getMailHeader() {
        return mailHeader;
    }

    public void setMailHeader(StringBuilder mailHeader) {
        this.mailHeader = mailHeader;
    }

    public StringBuilder getMailBody() {
        return mailBody;
    }

    public void setMailBody(StringBuilder mailBody) {
        this.mailBody = mailBody;
    }

    public StringBuilder getMailFooter() {
        return mailFooter;
    }

    public void setMailFooter(StringBuilder mailFooter) {
        this.mailFooter = mailFooter;
    }

    public boolean isPeerApprovalEnabled() {
        return isPeerApprovalEnabled;
    }

    public void setPeerApprovalEnabled(boolean peerApprovalEnabled) {
        isPeerApprovalEnabled = peerApprovalEnabled;
    }

    public void formatMailSubject() { }

    public void formatBodyHeader() { }

    public void formatBodyMessage() { }

    public void formatMailHeader() {
        StringBuilder mailHeader = new StringBuilder(1024);
        mailHeader.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "    <style type=\"text/css\">\n" +
                "        <!--\n" +
                "        table {\n" +
                "            font-size: 11px;\n" +
                "            font-family: Verdana, Arial, Helvetica, sans-serif;\n" +
                "        }\n" +
                "        dt {\n" +
                "            margin-top:10px;\n" +
                "            margin-left:15px;\n" +
                "        }\n" +
                "        dd {\n" +
                "            margin-top:6px;\n" +
                "        }\n" +
                "        s {\n" +
                "            background-color:yellow;\n" +
                "            text-decoration:none;\n" +
                "        }\n" +
                "        .url {\n" +
                "            padding-top:10px;\n" +
                "            padding-left:10px;\n" +
                "            padding-bottom:10px;\n" +
                "            background-color:#DCDCDC;\n" +
                "            font-family:Tahoma, serif;\n" +
                "        }\n" +
                "        a  {\n" +
                "            text-decoration:none;\n" +
                "            color:#1179BC;\n" +
                "            font-weight:bold;\n" +
                "        }\n" +
                "        -->\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n");

        setMailHeader(mailHeader);
    }

    public void formatBodyFooter() { }

    public void formatMailFooter() {
        StringBuilder mailFooter = new StringBuilder(30);
        mailFooter.append("\n</body>\n" + "</html>");
        setMailFooter(mailFooter);
    }

    public String getDate() {
        return new SimpleDateFormat(MAIL_DATE_FORMAT,Locale.US).format(Calendar.getInstance().getTime());
    }

    // Read locale based display strings from ApplicationResources.properties file
    // We need this for translating messages

    protected String getMessage(String key) {
        String val = null;
        if (null != mResource) {
            key = "mail." + key;
            val = mResource.getMessage(key);
        }
        return val;
    }
}

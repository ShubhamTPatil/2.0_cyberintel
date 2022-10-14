// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscriptionmanager.MailFormatter;
import com.marimba.tools.util.DebugFlag;
import org.apache.struts.util.MessageResources;

/**
 * Description about the class ApprovalMailFormatter
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class ApprovalMailFormatter extends MailFormatter implements IApprovalPolicyConstants {
    public int DEBUG = DebugFlag.getDebug("SUB/MAIL");

    private int approvalType;
    private String remarks;
    private String reviewedBy;
    private String reviewedOn;
    private String reviewedByDispName;

    public ApprovalMailFormatter(MessageResources mResource) {
        this.mResource = mResource;
    }

    public void setApprovalType(int approvalType) {
        this.approvalType = approvalType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getReviewedBy() {
        return (reviewedBy != null) ? reviewedBy : STR_UNKNOWN;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewedOn() {
        return reviewedOn;
    }

    public void setReviewedOn(String reviewedOn) {
        this.reviewedOn = reviewedOn;
    }

    public String getReviewedByDispName() {
        return reviewedByDispName;
    }

    public void setReviewedByDispName(String reviewedByDispName) {
        this.reviewedByDispName = reviewedByDispName;
    }

    public void prepare() {
        super.prepare();
    }

    public void formatMailSubject() {
        StringBuilder mailSubject = new StringBuilder(100);
        mailSubject.append(getMessage("approval.subject1")).append(STR_SPACE).append(getMessage("approval.subject2")).append(STR_SPACE);
        mailSubject.append(approvalType == POLICY_APPROVED ? getMessage("approval.approved") : getMessage("approval.rejected"));
        mailSubject.append(STR_SPACE).append(getMessage("approval.on")).append(STR_SPACE).append(getDate());
        setMailSubject(mailSubject);
    }

    public void formatBodyHeader() {
        StringBuilder mailBody = new StringBuilder(1024);
        mailBody.append("<table width=\"80%\" align=\"center\" style=\"border:1px solid #CCCCCC\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "    <tr>\n" +
                "        <td>\n" +
                "            <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"background-color:#1179BC;border-bottom:15px solid #DCDCDC;padding-left:20px;\">\n" +
                "                <tr><td>&nbsp;</td></tr>\n" +
                "                <tr><td>&nbsp;</td></tr>\n" +
                "                <tr>\n" +
                "                    <td> <b style=\"color:#ffffff;font-size:15px\">").append(getMessage("approval.notification")).append("</b> </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>\n" +
                "                        <b style=\"color:#ffffff\">")
                .append(getMessage("approval.reviewedby")).append(STR_SPACE)
                .append("</b>\n")
                .append("                        <span style=\"color:#ffffff\">").append(getReviewedByDispName()).append("</span>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>\n");
        mailBody.append("                        <b style=\"color:#ffffff\">")
                .append(getMessage("approval.reviewedon")).append(STR_SPACE)
                .append("</b>\n")
                .append("                        <span style=\"color:#ffffff\">").append(getReviewedOn()).append("</span>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n")
                .append("    <tr><td>&nbsp;</td></tr>");
        setMailBody(mailBody);
    }

    public void formatBodyMessage() {
        StringBuilder mailBody = getMailBody();

        mailBody.append("<tr>\n" +
                "        <td>\n" +
                "            <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px;border:1px solid #B4B4B4;\" border=\"0\">\n" +
                "                <tr>\n" +
                "                    <td style=\"background-color:#DCDCDC;font-size:15px;margin-left:10px;color:#DF5F5F;font-family:Tahoma, serif;padding-left:1px;padding-top:2px;padding-bottom:5px\" colspan=\"2\">\n" +
                "                        <b>").append(getMessage("approval.information")).append("</b>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding-left:10px\" align=\"right\">").append(getMessage("approval.targetname")).append("</td>\n" +
                "                    <td>").append(getTargetName()).append("</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding-left:10px\" align=\"right\">").append(getMessage("approval.targetid")).append("</td>\n" +
                "                    <td>").append(getTargetId()).append("</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding-left:10px\" align=\"right\">").append(getMessage("approval.createdby")).append("</td>\n" +
                "                    <td>").append(getCreatedByDispName()).append("</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding-left:10px\" align=\"right\">").append(getMessage("approval.createdon")).append("</td>\n" +
                "                    <td>").append(getCreatedOn()).append("</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding-left:10px\" align=\"right\">").append(getMessage("approval.comments")).append("</td>\n" +
                "                    <td>").append(getRemarks()).append("</td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td style=\"padding-left:10px\" align=\"right\">").append(getMessage("approval.finalstatus")).append("</td>\n" +
                "                    <td>").append(getFinalStatus()).append("</td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n");

        mailBody.append("    <tr><td>&nbsp;</td></tr>\n" + "    <tr><td>&nbsp;</td></tr>");

        setMailBody(mailBody);
    }

    public void formatBodyFooter() {
        StringBuilder mailBody = getMailBody();
        mailBody.append("<tr><td>&nbsp;</td></tr>\n" +
                "<tr><td>&nbsp;</td></tr>\n" +
                "</table>");
        setMailBody(mailBody);
    }

    private String getFinalStatus() {
        StringBuilder tmpBuff = new StringBuilder(100);
        tmpBuff.append("<b>");
        if (approvalType == POLICY_APPROVED) {
            tmpBuff.append("<font color=\"green\">").append(getMessage("approval.approved1")).append("</font>");
        } else if (approvalType == POLICY_REJECTED) {
            tmpBuff.append("<font color=\"red\">").append(getMessage("approval.rejected1")).append("</font>");
        } else {
            tmpBuff.append("<font color=\"blue\">").append(getMessage("approval.nostatus")).append("</font>");
        }
        tmpBuff.append("</b>");
        return tmpBuff.toString();
    }
}

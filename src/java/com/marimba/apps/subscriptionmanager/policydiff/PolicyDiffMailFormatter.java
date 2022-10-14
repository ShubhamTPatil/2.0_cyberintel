// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.policydiff;

import com.marimba.apps.subscriptionmanager.MailFormatter;
import com.marimba.tools.util.DebugFlag;
import org.apache.struts.util.MessageResources;

import java.util.List;
import java.util.Map;

/**
 * PolicyMailFormatter for populate diffing content which is used for send policy notification mail
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class PolicyDiffMailFormatter extends MailFormatter {
    public int DEBUG = DebugFlag.getDebug("SUB/MAIL");

    private boolean isMultiMode;
    private List<PolicyDiff> policyDiffs;

    public PolicyDiffMailFormatter(List<PolicyDiff> policyDiffs) {
        this.policyDiffs = policyDiffs;
    }

    public PolicyDiffMailFormatter(List<PolicyDiff> policyDiffs, MessageResources mResource) {
        this.policyDiffs = policyDiffs;
        this.mResource = mResource;
    }

    public void prepare() {
        super.prepare();
    }

    public boolean isMultiMode() {
        return isMultiMode;
    }

    public void setMultiMode(boolean multiMode) {
        isMultiMode = multiMode;
    }

    public void formatMailSubject() {
        StringBuilder mailSubject = new StringBuilder(100);
        StringBuilder mailSubjectTemp = new StringBuilder(100);
        mailSubject.append(getMessage("subject1")).append(" ");
        for (PolicyDiff diff : policyDiffs) {
            mailSubjectTemp.append(diff.getTargetName()).append(",");
        }
        if (!isMultiMode()) {
            mailSubject.append(getStrippedOutTargets(mailSubjectTemp.toString()))
                    .append(STR_SPACE).append(getMessage("subject2")).append(STR_SPACE).append(getDate());
        } else {
            mailSubject.append("(").append(mailSubjectTemp.toString().split(",").length).append(")")
                    .append(STR_SPACE).append(getMessage("subject3")).append(STR_SPACE).append(getDate());
        }
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
                "                    <td> <b style=\"color:#ffffff;font-size:15px\">").append(getMessage("header")).append("</b> </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>\n" +
                "                        <b style=\"color:#ffffff\">").append(getMessage("header.modifiedby")).append("</b>\n" +
                "                        <span style=\"color:#ffffff\">").append(getCreatedByDispName()).append("</span>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "                <tr>\n" +
                "                    <td>\n" +
                "                        <b style=\"color:#ffffff\">").append(getMessage("header.modifiedon")).append("</b>\n" +
                "                        <span style=\"color:#ffffff\">").append(getDate()).append("</span>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </table>\n" +
                "        </td>\n" +
                "    </tr>\n");
        setMailBody(mailBody);
    }

    public void formatBodyMessage() {
        for (PolicyDiff aPolicyDiff : policyDiffs) {
            formatBodyMessage(aPolicyDiff);
        }
    }

    private void formatBodyMessage(PolicyDiff differ) {
        boolean isEmptyPolicy = true;

        StringBuilder mailBody = getMailBody();

        mailBody.append("    <tr>\n" +
                "        <td style=\"font-size:15px;color:#DF5F5F;font-family:Tahoma, serif;padding-left:10px;padding-top:5px;padding-bottom:5px\">\n" +
                "            <b>").append(getMessage("target")).append(STR_SPACE).append("</b>\n" +
                "            <span style=\"color:#000000;\">").append(differ.getTargetName()).append("</span>\n" +
                "            <span style=\"color:#000000;font-size:12px;\">(").append(differ.getTargetId()).append(")</span>\n" +
                "        </td>\n" +
                "    </tr>\n");

        Map<String, ChannelDiffer> addedChannelsMap = differ.getAddedChannelsMap();

        // ADDED CHANNELS
        if (!addedChannelsMap.isEmpty()) {
            mailBody.append("    <tr>\n" +
                    "        <td>\n" +
                    "            <table width=\"96%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\" align=\"center\">\n" +
                    "                <tr>\n" +
                    "                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("addedchannels")).append(" (")
                    .append(addedChannelsMap.size())
                    .append(")</b></td>\n" + "                </tr>\n");


            Map<String, Map<String,String>> modifiedInfoMap;
            for(String chnlUrl : addedChannelsMap.keySet()) {
                ChannelDiffer chnlDiff = addedChannelsMap.get(chnlUrl);
                if(chnlDiff.hasChannelInfoUpdated()) {
                    modifiedInfoMap = chnlDiff.getChannelDiffInfo();
                    mailBody.append("                <tr>\n" +
                            "                    <td style=\"padding-top:5px;\">\n" +
                            "                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"border:1px solid #B4B4B4;\">\n");
                    mailBody.append("                            <tr><td class=\"url\"><a href=\"#\">")
                            .append(chnlUrl)
                            .append("</a></td></tr>\n")
                            .append("                            <tr>\n" +
                                    "                                <td>\n" +
                                    "                                    <ul>\n");

                    for (String propKey : modifiedInfoMap.keySet()) {
                        Map<String, String> interMap = modifiedInfoMap.get(propKey);
                        // <dd class="indent1"> Primary State: &nbsp;&nbsp; <s>primary</s> To subscribe_noinstall</dd>
                        mailBody.append("                                        <li>")
                                .append(propKey)
                                .append("&nbsp;:&nbsp;")
                                .append(interMap.get(KEY_NEW_VALUE))
                                .append("</li>\n");
                    }
                    mailBody.append("                                    </ul>\n" +
                            "                                </td>\n" +
                            "                            </tr>\n" +
                            "                        </table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n");
                    //add space between each box
//                    mailBody.append("<table  width=\"95%\" cellpadding=\"2\" cellspacing=\"2\"><tr><td></td></tr></table>");
                }
            }
            mailBody.append("            </table>\n" +
                    "        </td>\n" +
                    "    </tr>\n");
            isEmptyPolicy = false;
        }

        // MODIFIED CHANNELS
        Map<String, ChannelDiffer> modifiedChannelMap = differ.getModifiedChannelPropsMap();

        if (!modifiedChannelMap.isEmpty()) {
            mailBody.append("    <tr>\n" +
                    "        <td>\n" +
                    "            <table width=\"96%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\" align=\"center\">\n" +
                    "                <tr>\n" +
                    "                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("modifiedchannels")).append(" (")
                    .append(modifiedChannelMap.size())
                    .append(")</b></td>\n" + "                </tr>\n");

            for (String channelUrl : modifiedChannelMap.keySet()) {
                ChannelDiffer chnlDiffer = modifiedChannelMap.get(channelUrl);
                Map<String, Map<String,String>> modifiedInfoMap = chnlDiffer.getChannelDiffInfo();
                mailBody.append("                <tr>\n" +
                        "                    <td style=\"padding-top:5px;\">\n" +
                        "                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"border:1px solid #B4B4B4;\">\n");
                mailBody.append("                            <tr><td class=\"url\"><a href=\"#\">")
                        .append(channelUrl)
                        .append("</a></td></tr>\n");
                if (!modifiedInfoMap.keySet().isEmpty()) {
                    mailBody.append("                            <tr>\n" +
                            "                                <td>\n" +
                            "                                    <ul>\n");
                    for (String propKey : modifiedInfoMap.keySet()) {
                        Map<String, String> interMap = modifiedInfoMap.get(propKey);
                        mailBody.append("                                        <li>")
                                .append(propKey)
                                .append("&nbsp;:&nbsp;")
                                .append("<s>")
                                .append(interMap.get(KEY_OLD_VALUE))
                                .append("</s>")
                                .append("&nbsp;<b>").append(getMessage("to")).append("</b>&nbsp;")
                                .append(interMap.get(KEY_NEW_VALUE))
                                .append("</li>\n");
                    }
                    mailBody.append("                                    </ul>\n" +
                            "                                </td>\n" +
                            "                            </tr>\n");
                }
                mailBody.append(formatChannelProps(chnlDiffer));
                mailBody.append("                        </table>\n" +
                        "                    </td>\n" +
                        "                </tr>\n");

                //add space between each box
//                mailBody.append("<table  width=\"100%\" cellpadding=\"2\" cellspacing=\"2\"><tr><td></td></tr></table>");
            }
            mailBody.append("            </table>\n" +
                    "        </td>\n" +
                    "    </tr>\n");
            isEmptyPolicy = false;
        }

        // DELETED CHANNELS
        Map<String, ChannelDiffer> deletedChannelsMap = differ.getDeletedChannelsMap();
        if (!deletedChannelsMap.isEmpty()) {
            mailBody.append("    <tr>\n" +
                    "        <td>\n" +
                    "            <table width=\"96%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\" align=\"center\">\n" +
                    "                <tr>\n" +
                    "                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("removedchannels")).append(" (")
                    .append(deletedChannelsMap.size())
                    .append(")</b></td>\n" + "                </tr>\n");

            Map<String, Map<String,String>> modifiedInfoMap;
            for(String chnlUrl : deletedChannelsMap.keySet()) {
                ChannelDiffer chnlDiff = deletedChannelsMap.get(chnlUrl);
                if(chnlDiff.hasChannelInfoUpdated()) {
                    modifiedInfoMap = chnlDiff.getChannelDiffInfo();
                    mailBody.append("                <tr>\n" +
                            "                    <td style=\"padding-top:5px;\">\n" +
                            "                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"border:1px solid #B4B4B4;\">\n");
                    mailBody.append("                            <tr><td class=\"url\"><a href=\"#\">")
                            .append(chnlUrl)
                            .append("</a></td></tr>\n")
                            .append("                            <tr>\n" +
                                    "                                <td>\n" +
                                    "                                    <ul>\n");

                    for (String propKey : modifiedInfoMap.keySet()) {
                        Map<String, String> interMap = modifiedInfoMap.get(propKey);
                        // <dd class="indent1"> Primary State: &nbsp;&nbsp; <s>primary</s> To subscribe_noinstall</dd>
                        mailBody.append("                                        <li>")
                                .append(propKey)
                                .append("&nbsp;:&nbsp;")
                                .append(interMap.get(KEY_OLD_VALUE))
                                .append("</li>\n");
                    }
                    mailBody.append("                                    </ul>\n" +
                            "                                </td>\n" +
                            "                            </tr>\n" +
                            "                        </table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n");
                    //add space between each box
//                    mailBody.append("<table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\"><tr><td></td></tr></table>");
                }
            }
            mailBody.append("            </table>\n" +
                    "        </td>\n" +
                    "    </tr>\n");
            isEmptyPolicy = false;
        }

        // CAPTURE PROPERTY CHANGES, NOT RELATED TO CHANNELS
        Map<String, String> addedPropsMap = differ.getAddedPropsMap();
        Map<String, Map<String, String>> addedDummyPropsMap = differ.getAddedDummyChannelsMap();
        if (!(addedPropsMap.isEmpty() && addedDummyPropsMap.isEmpty())) {
            mailBody.append("    <tr>\n" +
                    "        <td>\n" +
                    "            <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\">\n" +
                    "                <tr>\n" +
                    "                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("addedproperties")).append("</b></td>\n" +
                    "                </tr>\n" +
                    "                <tr>\n" +
                    "                    <td>\n" +
                    "                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"border:1px solid #B4B4B4;\">\n" +
                    "                            <tr>\n" +
                    "                                <td>\n" +
                    "                                    <ul>\n");
            for(String key : addedPropsMap.keySet()) {
                mailBody.append("                                        <li>")
                        .append(key).append("&nbsp;=&nbsp;").append(addedPropsMap.get(key))
                        .append(" </li>\n");
            }

            for(String key : addedDummyPropsMap.keySet()) {
                Map<String, String> dummyMap = addedDummyPropsMap.get(key);
                for (Map.Entry<String, String> entry : dummyMap.entrySet()) {
                    mailBody.append("                                        <li>")
                            .append(entry.getKey()).append("&nbsp;=&nbsp;").append(entry.getValue()).append(" [").append(key).append("]")
                            .append(" </li>\n");
                }
            }
            mailBody.append("                                    </ul>\n" +
                    "                                </td>\n" +
                    "                            </tr>\n" +
                    "                        </table>\n");
            //add space between each box
//            mailBody.append("<table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\"><tr><td></td></tr></table>");
            mailBody.append("                    </td>\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "        </td>\n" +
                    "    </tr>\n");
            isEmptyPolicy = false;
        }

        Map<String, String> deletedPropsMap = differ.getDeletedPropsMap();
        Map<String, Map<String, String>> deletedDummyPropsMap = differ.getDeletedDummyChannelsMap();

        if (!(deletedPropsMap.isEmpty() && deletedDummyPropsMap.isEmpty())) {
            mailBody.append("    <tr>\n" +
                    "        <td>\n" +
                    "            <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\">\n" +
                    "                <tr>\n" +
                    "                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("removedproperties")).append("</b></td>\n" +
                    "                </tr>\n" +
                    "                <tr>\n" +
                    "                    <td>\n" +
                    "                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"border:1px solid #B4B4B4;\">\n" +
                    "                            <tr>\n" +
                    "                                <td>\n" +
                    "                                    <ul>\n");

            for (String key: deletedPropsMap.keySet()) {
                mailBody.append("                                        <li>")
                        .append(key).append("&nbsp;=&nbsp;").append(deletedPropsMap.get(key))
                        .append("</li>\n");
            }
            for(String key : deletedDummyPropsMap.keySet()) {
                Map<String, String> dummyMap = deletedDummyPropsMap.get(key);
                for (Map.Entry<String, String> entry : dummyMap.entrySet()) {
                    mailBody.append("                                        <li>")
                            .append(entry.getKey()).append("&nbsp;=&nbsp;").append(entry.getValue()).append(" [").append(key).append("]")
                            .append(" </li>\n");
                }
            }
            mailBody.append("                                    </ul>\n" +
                    "                                </td>\n" +
                    "                            </tr>\n" +
                    "                        </table>\n");
            //add space between each box
            mailBody.append("                    </td>\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "        </td>\n" +
                    "    </tr>\n");
            isEmptyPolicy = false;
        }

        Map<String, Map<String, String>> modifiedPropsMap = differ.getModifiedPropsMap();
        Map<String, Map<String, Map<String, String>>> modifiedDummyPropsMap = differ.getModifiedDummyChannelsMap();

        if (!(modifiedPropsMap.isEmpty() && modifiedDummyPropsMap.isEmpty())) {
            mailBody.append("    <tr>\n" +
                    "        <td>\n" +
                    "            <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\">\n" +
                    "                <tr>\n" +
                    "                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("modifiedproperties")).append("</b></td>\n" +
                    "                </tr>\n" +
                    "                <tr>\n" +
                    "                    <td>\n" +
                    "                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"border:1px solid #B4B4B4;\">\n" +
                    "                            <tr>\n" +
                    "                                <td>\n" +
                    "                                    <ul>\n");

            Map<String, String> tmpMap;
            for(String key : modifiedPropsMap.keySet()) {
                tmpMap = modifiedPropsMap.get(key);
                mailBody.append("                                        <li>")
                        .append(key).append("&nbsp;=&nbsp;")
                        .append("<s>").append(tmpMap.get(KEY_OLD_VALUE)).append("</s>")
                        .append("&nbsp;<b>").append(getMessage("to")).append("</b>&nbsp;").append(tmpMap.get(KEY_NEW_VALUE)).append("</li>\n");
            }

            for(String key : modifiedDummyPropsMap.keySet()) {
                Map<String, Map<String, String>> dummyPropsMap = modifiedDummyPropsMap.get(key);
                for(String keyInter : dummyPropsMap.keySet()) {
                    tmpMap = dummyPropsMap.get(keyInter);
                    mailBody.append("                                        <li>")
                            .append(keyInter).append("&nbsp;=&nbsp;")
                            .append("<s>").append(tmpMap.get(KEY_OLD_VALUE)).append("</s>")
                            .append("&nbsp;<b>").append(getMessage("to")).append("</b>&nbsp;")
                            .append(tmpMap.get(KEY_NEW_VALUE)).append(" [").append(key).append("]").append("</li>\n");
                }
            }
            mailBody.append("                                    </ul>\n" +
                    "                                </td>\n" +
                    "                            </tr>\n" +
                    "                        </table>\n");
            //add space between each box
            mailBody.append("                    </td>\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "        </td>\n" +
                    "    </tr>\n");
            isEmptyPolicy = false;
        }

        String blackoutProps = differ.getBlackoutProps().toString();
        if (!blackoutProps.trim().isEmpty()) {
            mailBody.append("    <tr>\n" +
                    "        <td>\n" +
                    "            <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\">\n" +
                    "                <tr>\n" +
                    "                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("blackoutproperties")).append("</b></td>\n" +
                    "                </tr>\n" +
                    "                <tr>\n" +
                    "                    <td>\n" +
                    "                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\" style=\"border:1px solid #B4B4B4;\">\n" +
                    "                            <tr>\n" +
                    "                                <td>\n" +
                    "                                    <ul>\n");
            mailBody.append("                                        <li>")
                    .append(blackoutProps)
                    .append("</li>\n");
            mailBody.append("                                    </ul>\n" +
                    "                                </td>\n" +
                    "                            </tr>\n" +
                    "                        </table>\n" +
                    "                    </td>\n" +
                    "                </tr>\n" +
                    "            </table>\n" +
                    "        </td>\n" +
                    "    </tr>\n");
            isEmptyPolicy = false;
        }

        if (isEmptyPolicy) {
            // Possible to save policy without any changes
            mailBody.append("<tr><td style=\"font-size:15px;padding-top:30px;padding-bottom:30px;padding-left:20px;\">")
                    .append("<b>").append(getMessage("nopolicyupdate")).append("</b></td></tr>");
        }
        setMailBody(mailBody);
    }

    private StringBuffer formatChannelProps(ChannelDiffer chnlDiffer) {
        StringBuffer mailBody = new StringBuffer(100);
        Map<String, String> addedPropsMap = chnlDiffer.getAddedPropsMap();
        Map<String, String> deletedPropsMap = chnlDiffer.getDeletedPropsMap();
        Map<String, Map<String, String>> modifiedPropsMap = chnlDiffer.getModifiedPropsMap();

        if (!addedPropsMap.isEmpty()) {
            mailBody.append("\n<tr>\n" +
                    "                <td>\n" +
                    "                    <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\">\n" +
                    "                        <tr>\n" +
                    "                            <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>").append(getMessage("addedchnlproperties")).append("</b></td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <td>\n" +
                    "                                <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\">\n" +
                    "                                    <tr>\n" +
                    "                                        <td>\n" +
                    "                                            <ul>\n");

            for (String propKey : addedPropsMap.keySet()) {
                mailBody.append("                                                <li>")
                        .append(propKey)
                        .append("&nbsp;=&nbsp;")
                        .append(addedPropsMap.get(propKey))
                        .append("</li>\n");
            }
            mailBody.append("                                                        </ul>\n" +
                    "                                                    </td>\n" +
                    "                                                </tr>\n" +
                    "                                            </table>\n" +
                    "                                        </td>\n" +
                    "                                    </tr>\n");
//            //add space between each box
//            mailBody.append("                        <tr><td>&nbsp;</td></tr>\n");
            mailBody.append("                    </table>\n" +
                    "                </td>\n" +
                    "            </tr>\n");
        }

        if (!deletedPropsMap.isEmpty()) {
            mailBody.append("\n<tr>\n" +
                    "                <td>\n" +
                    "                    <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\">\n" +
                    "                        <tr>\n" +
                    "                            <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>")
                    .append(getMessage("removedchnlproperties")).append("</b></td>\n" +
                    "                        </tr>\n" +
                    "                        <tr>\n" +
                    "                            <td>\n" +
                    "                                <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\">\n" +
                    "                                    <tr>\n" +
                    "                                        <td>\n" +
                    "                                            <ul>\n");
            for (String propKey : deletedPropsMap.keySet()) {
                mailBody.append("                                                <li>")
                        .append(propKey)
                        .append("&nbsp;=&nbsp;")
                        .append(deletedPropsMap.get(propKey))
                        .append("</li>\n");
            }
            mailBody.append("                                            </ul>\n" +
                    "                                        </td>\n" +
                    "                                    </tr>\n" +
                    "                                </table>\n" +
                    "                            </td>\n" +
                    "                        </tr>\n");
            //add space between each box
//            mailBody.append("                        <tr><td>&nbsp;</td></tr>\n");
            mailBody.append("                    </table>\n" +
                    "                </td>\n" +
                    "            </tr>\n");

        }
        if (!modifiedPropsMap.isEmpty()) {
            mailBody.append("\n                    <tr>\n" +
                    "                        <td>\n" +
                    "                            <table width=\"95%\" cellpadding=\"2\" cellspacing=\"2\" style=\"margin-left:20px\">\n" +
                    "                                <tr>\n" +
                    "                                    <td style=\"border-bottom:#B4B4B4 solid 1px;font-size:12px\"><b>")
                    .append(getMessage("modifiedchnlproperties")).append("</b></td>\n" +
                    "                                </tr>\n" +
                    "                                <tr>\n" +
                    "                                    <td>\n" +
                    "                                        <table width=\"100%\" cellpadding=\"2\" cellspacing=\"2\">\n" +
                    "                                            <tr>\n" +
                    "                                                <td>\n" +
                    "                                                    <ul>\n");

            for (String propKey : modifiedPropsMap.keySet()) {
                Map<String, String> interMap = modifiedPropsMap.get(propKey);
                mailBody.append("                                                        <li>")
                        .append(propKey)
                        .append("&nbsp;=&nbsp;")
                        .append("<s>")
                        .append(interMap.get(KEY_OLD_VALUE))
                        .append("</s>")
                        .append("&nbsp;<b>").append(getMessage("to")).append("</b>&nbsp;")
                        .append(interMap.get(KEY_NEW_VALUE))
                        .append("</li>\n");
            }
            mailBody.append("                                                </ul>\n" +
                    "                                            </td>\n" +
                    "                                        </tr>\n" +
                    "                                    </table>\n" +
                    "                                </td>\n" +
                    "                            </tr>\n");
            //add space between each box
//            mailBody.append("                            <tr><td>&nbsp;</td></tr>");
            mailBody.append("                        </table>\n" +
                    "                    </td>\n" +
                    "                </tr>\n");

        }
        return mailBody;
    }

    public void formatBodyFooter() {
        StringBuilder mailBody = getMailBody();
        mailBody.append("<tr><td>&nbsp;</td></tr>\n");
        if (isPeerApprovalEnabled()) {
            mailBody.append("<tr>\n")
                    .append("<td style=\"font-family:Tahoma, serif;padding-left:10px;padding-top:5px;padding-bottom:5px\">")
                    .append(getMessage("note"))
                    .append("</td>\n</tr>\n");
        }
        mailBody.append("<tr><td>&nbsp;</td></tr>\n");
        mailBody.append("</table>");
        setMailBody(mailBody);
    }

    // This method to limit the target names length which is used as a subject in mail
    private String getStrippedOutTargets(String targetNames) {
        if (null == targetNames) {
            return "";
        }
        int reqLength = 140;
        targetNames = targetNames.trim();
        if (targetNames.endsWith(",")) {
            targetNames = targetNames.substring(0, targetNames.length() - 1);
        }
        if (targetNames.length() > reqLength) {
            int final_count = 0;
            int tgt_count = targetNames.split(",").length;
            targetNames = targetNames.substring(0, reqLength);
            final_count = targetNames.split(",").length;

            String tmp_title = "";
            if (tgt_count - final_count > 0) {
                tmp_title = " and (" + (tgt_count - final_count) +") more";
            }
            targetNames = targetNames + "... " + tmp_title;
        }
        debug("Stripped out targets : " + targetNames);
        return targetNames;
    }

    private void debug(String msg) {
        if (DEBUG > 1) System.out.println("PolicyMailFormatter: " + msg);
    }
}

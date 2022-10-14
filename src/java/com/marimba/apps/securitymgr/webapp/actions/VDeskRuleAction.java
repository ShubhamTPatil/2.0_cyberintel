// Copyright 2019, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.webapp.actions;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.securitymgr.webapp.forms.VDeskQueryresultsForm;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.beans.OvalDetailBean;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.intf.db.IStatementPool;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.tools.action.DelayedAction;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: May 5, 2017
 * Time: 3:16:16 PM
 * To change this template use File | Settings | File Templates.
 */

public class VDeskRuleAction extends AbstractAction {

    private int DEBUG = DebugFlag.getDebug("SECURITY/REPORTING");
    private String MAIL_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    private JSONObject result;
    private JSONArray records;
    private SubscriptionMain main;

    public ActionForward perform(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
            ServletContext sc = servlet.getServletConfig().getServletContext();
            main = TenantHelper.getTenantSubMain(sc, request);
            String machineName=request.getParameter("machine");
            String contentId = request.getParameter("contentId");
            String queryDisplayPath = request.getParameter("queryDisplayPath");
            String action = request.getParameter("action");
            try {
                if ("rule_details".equals(action)) {
                    String xmlType = "xccdf";
                    if ((queryDisplayPath != null) && (queryDisplayPath.trim().length() > 0)) {
                        if (queryDisplayPath.startsWith("/Vulnerability Assessment/")) {
                            xmlType = "oval";
                        } else {
                            xmlType = "xccdf";
                        }
                    }
                    QueryRunner queryRunner = new QueryRunner(xmlType, machineName, contentId);
                    QueryRunner queryRunner1 = new QueryRunner(xmlType, contentId);
                    result = new JSONObject();
                    result.put("result", records);
                    result.put("machine_name", machineName);
                }

                sendJSONResponse(response, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
    }

        public String getWaitMessage() {
            return "Please Wait....";
        }


    class QueryRunner extends DatabaseAccess {
        String theQuery = "";
        Long startTime = System.currentTimeMillis(), endTime = -1L;

        public QueryRunner(String xmlType, String machineName, String contentId) {
            try {
                runQuery(new GetRuleCompliance(xmlType, machineName, contentId));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public QueryRunner(String xmlType, String contentId) {
            try {
                runQuery(new GetRuleDetails(xmlType, contentId));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public String getQueryElapsedTime() {
            return getElapsedTime(startTime, endTime);
        }
        class GetRuleDetails extends QueryExecutor {

            String contentId;
            String xmlType;

            GetRuleDetails(String xmlType, String contentId) {
                super(main);
                this.contentId = contentId;
                this.xmlType = xmlType;
            }

            protected void execute(IStatementPool pool) {
                String rules = getRules();
                rules = rules.replace("[", "");
                rules = rules.replace("]", "");
                if ("".equals(rules)) { return; }
                String sql = "SELECT sg.content_id, sg.group_name, sgr.id, sgr.rule_name, sgr.rule_title, sgr.rule_desc, rule_severity, sgrf.rule_fix_script \n" +
                        "from security_xccdf_group_rule sgr, security_xccdf_group_rule_fix sgrf, security_xccdf_group sg \n" +
                        "where content_id in (select content_id from inv_security_xccdf_compliance isc \n" +
                        "where isc.content_id = '"+contentId+"' )\n" +
                        "#rules"+
                        "and sg.id = sgr.group_id\n" +
                        "and sgr.id = sgrf.rule_id\n" +
                        "order by group_name\n";

                if ("oval".equals(xmlType)) {
                    sql = "SELECT sop.content_id, sop.profile_name, sopd.id, sopd.definition_name , " +
                            "sopd.definition_title, sopd.definition_desc, sopd.definition_severity," +
                            "sopd.profile_id, sopd.definition_class \n" +
                            "from security_oval_profile_defn sopd, security_oval_profile sop \n" +
                            "where content_id in (select content_id from inv_security_oval_compliance isoc \n" +
                            "where isoc.content_id = '"+contentId+"' )\n" +
                            "#rules"+
                            "and sop.id = sopd.profile_id\n" +
                            "order by profile_name\n";
                    String ruleCondition = ("".equals(rules)) ? "" : "and definition_name in ("+rules+")\n";
                    sql =   sql.replace("#rules", ruleCondition);
                } else {
                    String ruleCondition = ("".equals(rules)) ? "" : " and rule_name in ("+rules+")\n";
                    sql =   sql.replace("#rules", ruleCondition);
                }
                debug(5, "GetRuleDetails :: execute(), sql - " + sql);
                records = new JSONArray();
                PreparedStatement st = null;
                ResultSet rs = null;
                try {
                    st = pool.getConnection().prepareStatement(sql);
                    rs = st.executeQuery();
                    String ruleCompliance = result.has("rules_compliance") ? result.getString("rules_compliance") : null;
                    JSONObject ruleComplianceJSON = new JSONObject();
                    if (ruleCompliance != null && !"".equals(ruleCompliance))
                        ruleComplianceJSON= new JSONObject(ruleCompliance);

                    Map<String, List<OvalDetailBean>> ovalDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalDefinitionDetails();
                    if (ovalDefinitionDetails == null) {
                        SCAPUtils.getSCAPUtils().loadOvalDefinitionDetails(main);
                        ovalDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalDefinitionDetails();
                    }
                    debug(5, "GetRuleDetails :: execute(), ovalDefinitionDetails.size() - " + ovalDefinitionDetails.size());
                    debug(7, "GetRuleDetails :: execute(), ovalDefinitionDetails - " + ovalDefinitionDetails);

                    Map<String, List<OvalDetailBean>> ovalCVEDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalCVEDefinitionDetails();
                    if (ovalCVEDefinitionDetails == null) {
                        SCAPUtils.getSCAPUtils().loadOvalCVEDefinitionDetails(main);
                        ovalCVEDefinitionDetails = SCAPUtils.getSCAPUtils().getOvalCVEDefinitionDetails();
                    }
                    debug(5, "GetRuleDetails :: execute(), ovalCVEDefinitionDetails.size() - " + ovalCVEDefinitionDetails.size());
                    debug(7, "GetRuleDetails :: execute(), ovalCVEDefinitionDetails - " + ovalCVEDefinitionDetails);

                    while(rs.next()) {
                        int ruleId = rs.getInt(3);
                        String ruleName = rs.getString(4);
                        String ruleTitle = rs.getString(5);
                        String ruleDescription = rs.getString(6);
                        String ruleSeverity = rs.getString(7);
                        String status = ruleComplianceJSON.has(ruleName) ? ruleComplianceJSON.getString(ruleName) : "";

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("rule_id", ruleId);
                        jsonObject.put("rule_name", ruleName);
                        jsonObject.put("rule_title", ruleTitle);
                        jsonObject.put("rule_desc", ruleDescription);
                        jsonObject.put("rule_severity", ruleSeverity);

                        if ("oval".equals(xmlType)) {
                            int contentId = rs.getInt(1);
                            int profileId = rs.getInt(8);
                            String classType = rs.getString(9);

                            if ("fail".equals(status)) {
                                if ("inventory".equals(classType) || "compliance".equals(classType)) {
                                    status = "NOT-INSTALLED";
                                } else {
                                    status = "VULNERABLE";
                                }
                            } else if ("pass".equals(status)) {
                                if ("inventory".equals(classType) || "compliance".equals(classType)) {
                                    status = "INSTALLED";
                                } else {
                                    status = "NOT-VULNERABLE";
                                }
                            } else {
                                status = status.toUpperCase();
                            }

                            String ruleRef = "";
                            String ruleSolution = "";
                            String ruleCvss = "";
                            if (ovalCVEDefinitionDetails != null) {
                                List<OvalDetailBean> list = ovalCVEDefinitionDetails.get(contentId + ":_:" + profileId + ":_:" + ruleName);
                                if (list != null) {
                                    ruleSeverity = "";
                                    jsonObject.put("rule_severity", list.get(0).getSeverity());
                                    jsonObject.put("rule_cvss", list.get(0).getCvssScore());
                                    
                                    for (OvalDetailBean ovalDetailBean : list) {
                                        ruleRef += (("".equals(ruleRef)) ? "" : ", ");
                                        ruleRef += ovalDetailBean.getReferenceName();

                                        ruleSolution += (("".equals(ruleSolution)) ? "" : ", ");
                                        ruleSolution += ovalDetailBean.getSolution();

                                        if ((ruleSeverity == null) || (ruleSeverity.trim().length() < 1)) {
                                            ruleSeverity = ovalDetailBean.getSeverity();
                                        }

                                        if ((ruleCvss == null) || (ruleCvss.trim().length() < 1)) {
                                            ruleCvss = ovalDetailBean.getCvssScore();
                                        }
                                    }
                                }
                            }

                            jsonObject.put("rule_severity", ruleSeverity);
                            jsonObject.put("rule_cvss", ruleCvss);
                            jsonObject.put("rule_fix", ruleSolution);
                            jsonObject.put("status", status);
                            jsonObject.put("rule_ref", ruleRef);
                        } else {
                            jsonObject.put("rule_fix", getRuleFix(rs.getBytes(8)));
                            jsonObject.put("status", status);
                            jsonObject.put("rule_ref", "");
                        }
                        if(jsonObject.get("rule_ref") != null && ((String)jsonObject.get("rule_ref")).trim().length() > 0) {
                            if (((String)jsonObject.get("rule_ref")).indexOf("cpe") < 0 ) {
                                records.put(jsonObject);
                            }
                        }
                    }
                } catch(Exception sex) {
                    sex.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (st != null) st.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            String getRuleFix(byte[] fixBytes) {
                if (fixBytes == null) return "";
                StringBuilder sb = new StringBuilder();
                ByteArrayInputStream bInput = new ByteArrayInputStream(fixBytes);
                int num;
                while((num = bInput.read()) != -1 ) {
                    sb.append((char) num);
                }
                System.out.println(sb.toString());
                return sb.toString();
            }

            String getRules() {
                List list = new ArrayList();
                try {

                    JSONObject jsonObject = result.has ("rules_compliance") ? new JSONObject(result.getString("rules_compliance")): null;
                    if (null != jsonObject) {
                        Iterator iterator = jsonObject.keys();
                        int i = 0;
                        while (iterator.hasNext()) {
                            String key = (String) iterator.next();
                            list.add("'" + key + "'");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return list.toString();
            }
        }

        class GetRuleCompliance extends QueryExecutor {
            String machineName;
            String contentId;
            String xmlType;

            GetRuleCompliance(String xmlType, String machineName, String contentId) {
                super(main);
                this.machineName = machineName;
                this.contentId = contentId;
                this.xmlType = xmlType;
            }

            protected void execute(IStatementPool pool) throws SQLException {
                String sql = "select rules_compliance from inv_security_" + xmlType + "_compliance where machine_name='"+ machineName + "' and content_id='"+contentId+"'";
                debug(5, "GetRuleCompliance :: execute(), theQuery - " + theQuery);
                PreparedStatement st = pool.getConnection().prepareStatement(sql);
                ResultSet rs = st.executeQuery();
                StringBuilder sb = new StringBuilder();
                try {
                    while(rs.next()) {
                        try {
                            byte[] jsonBytes = rs.getBytes("rules_compliance");
                            if (jsonBytes == null) {
                                result = new JSONObject();
                                return;
                            }
                            ByteArrayInputStream bInput = new ByteArrayInputStream(jsonBytes);
                            int num;
                            while((num = bInput.read()) != -1 ) {
                                sb.append((char) num);
                            }
                            result = new JSONObject(sb.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch(SQLException sex) {
                    sex.printStackTrace();
                } finally {
                    rs.close();
                    st.close();
                }
            }
        }
    }


    private String getElapsedTime(Long startTime, Long endTime) {
        if (endTime == -1) endTime = System.currentTimeMillis();

        long milliseconds = endTime - startTime;
        int hours   = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60)) / 1000;
        return String.format("%02d h : %02d m : %02d s", hours, minutes, seconds) + " (" + milliseconds + " ms)";
    }

    public String getColumnName(MessageResources messageResource, String columnName) {
        String key = "sql.column.name." + columnName;
        String value = messageResource == null ? null : messageResource.getMessage(key);
        return (value == null ) ? columnName: value;
    }

    private void debug0(String msg) {
        System.out.println("VDeskRuleAction.java :: [" + new Date().toString() + "] ==> " + msg);
    }

    private void debug(int level, String msg) {
        if (DEBUG >= level) {
            System.out.println("VDeskRuleAction.java :: [" + new Date().toString() + "] ==> " + msg);
        }
    }

    public void sendJSONResponse(HttpServletResponse response, JSONObject jsonObject) throws Exception {
        PrintWriter out = response.getWriter();
        out.println(jsonObject.toString());
        out.flush();
    }
}
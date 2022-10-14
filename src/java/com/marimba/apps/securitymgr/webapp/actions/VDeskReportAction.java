// Copyright 2019, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.webapp.actions;

import com.marimba.apps.securitymgr.query.DirNode;
import com.marimba.apps.securitymgr.query.QueryListing;
import com.marimba.apps.securitymgr.view.ArgBean;
import com.marimba.apps.securitymgr.view.QueryBean;
import com.marimba.apps.securitymgr.webapp.forms.VDeskReportForm;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.tools.util.DebugFlag;
import com.marimba.tools.xml.XMLParser;
import com.marimba.webapps.common.Resources;
import com.marimba.webapps.tools.action.DelayedAction;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

public class VDeskReportAction extends DelayedAction implements IAppConstants {

    int DEBUG = DebugFlag.getDebug("SECURITY/REPORTING");

    public VDeskReportAction.Task create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                         HttpServletResponse response) throws IOException, ServletException {
        return new MainReportTask((VDeskReportForm) form, mapping, request, response);
    }

    public ActionForward done(DelayedAction.Task task, ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        MainReportTask reportTask = (MainReportTask) task;
        if (reportTask.errors != null && !reportTask.errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, reportTask.errors);
        }
        return reportTask.forward;
    }

    private class MainReportTask extends Task {
        String action;
        Locale locale;
        HttpSession sess;
        ActionErrors errors;
        VDeskReportForm reportForm;
        ActionMapping mapping;
        ActionForward forward;
        HttpServletRequest request;
        HttpServletResponse response;
        ArgBean[] args = null;

        MainReportTask(VDeskReportForm reportForm, ActionMapping mapping, HttpServletRequest request, HttpServletResponse response) {
            this.request = request;
            this.response = response;
            this.mapping = mapping;
            this.reportForm = reportForm;
            this.sess = request.getSession();
            this.locale = request.getLocale();
            this.errors = (ActionErrors)request.getAttribute(Globals.ERROR_KEY);
            if (this.errors == null) this.errors = new ActionErrors();
        }

        public void execute() {
            action = reportForm.getAction();
            String path = reportForm.getPath();
            if ("left".equals(action)) {
                forward = mapping.findForward("query_folder");
                return;
            }
            QueryListing queryListing = getQueryListing(sess);
            if ("list_queries".equals(action)) {
                if (null == path || "".equals(path)) {
                    createResponse(response, constructFirstJsonObject());
                } else {
                    DirNode node = (queryListing.getDirNode(path) != null) ? queryListing.getDirNode(path) : queryListing.getCurrentDirNode();
                    createResponse(response, constructJsonObject(node.getChildNode()));
                }
                return;
            }
            if ("selected_query".equals(action)) {
                if (null == path || "".equals(path.trim())) {
                    path = "/Configuration Assessment/Machines assessed in last 24 hours";
                }
                if ("/Configuration Assessment/".equals(path)) {
                    path = "/Configuration Assessment/Machines assessed in last 24 hours";
                }
                if ("/Vulnerability Assessment/".equals(path)) {
                    path = "/Vulnerability Assessment/Machines assessed in last 24 hours";
                }
                DirNode node = queryListing.queryMap.get(path);
                reportForm.setArgs(getArgs(node, request));
                reportForm.setSql(node.getValue("atlas.node.sql"));
                reportForm.setDesc(node.getValue("atlas.node.desc"));
                reportForm.setPath(node.getName());
                setRequestAttribute("atlas.form", reportForm);
                if ("s".equals(node.getValue("atlas.query.type"))) {
                    action = "result";
                } else {
                    forward = mapping.findForward("query_form");
                    return;
                }
            }
            if ("result".equals(action)) {
                DirNode node = queryListing.queryMap.get(reportForm.getPath());
                reportForm.setArgs(getArgs(node, request));
                String sql = node.getValue("atlas.node.sql");
                for(ArgBean argBean: reportForm.getArgs()) {
                    for(String value: argBean.getValues()) {
                        value = argBean.getSqlValue(value, 0);
                        int i = 0;
                        String str= "$"+ ++i;
                        if (value.indexOf(",") > -1) {
                            String modifiedValue = "";
                            String argCriteria = node.getValue("atlas.node.sql.arg" + i + ".criteria");
                            try {
                                if ((argCriteria != null) && (argCriteria.trim().length() > 0)) {
                                    String[] valueArr = value.split(",");
                                    if (valueArr[0].startsWith("'")) {
                                        //remove quotes at the beginning
                                        valueArr[0] = valueArr[0].substring(1);
                                    }
                                    if (valueArr[valueArr.length - 1].endsWith("'")) {
                                        //remove trailing quotes
                                        valueArr[valueArr.length - 1] = valueArr[valueArr.length - 1].substring(0, valueArr[valueArr.length - 1].length() - 1);
                                    }
                                    for (String valueArrElem : valueArr) {
                                        if (modifiedValue.length() > 0) {
                                            modifiedValue += " or ";
                                        }
                                        modifiedValue += "(" + argCriteria.replace(str, "'" + valueArrElem + "'") + ")";
                                    }
                                    if (modifiedValue.length() > 0) {
                                        modifiedValue = "(" + modifiedValue + ")";
                                    } else {
                                        modifiedValue = "";
                                    }
                                }
                            } catch (Throwable t) {
                                t.printStackTrace();
                                modifiedValue = "";
                            }
                            if (modifiedValue.length() > 0) {
                                sql = sql.replace(argCriteria, modifiedValue);
                            } else {
                                sql = sql.replace(str, value);
                            }
                        } else {
                            sql = sql.replace(str, value);
                        }
                    }
                }
                setRequestAttribute("action", action);
                QueryBean queryBean = new QueryBean();
                queryBean.setDisplayPath(reportForm.getPath());
                queryBean.setQueryTarget(node.getName().split("/")[2]);
                queryBean.setQueryName(node.getName());
                request.getUserPrincipal().getName();
                queryBean.setQueryUserName(request.getUserPrincipal().getName());
                queryBean.setQuery(sql);
                reportForm.setAction(null);
                sess.setAttribute("SESS_QUERY_BEAN", queryBean);
                forward = mapping.findForward("query_result");
                return;
            }

            forward = mapping.findForward("view");
        }

        public String getWaitMessage() {
            return "Please Wait....";
        }

        private QueryListing getQueryListing(HttpSession session) {

            if (session.getAttribute("query_listing") != null) {
                return (QueryListing)session.getAttribute("query_listing");
            }
            QueryListing queryListing = null;
            XMLParser theXMLParser = new XMLParser();
            theXMLParser.setUTF(true);
            try {
                InputStream xmlInputStream = new Resources().getResourceAsStream("SecurityComplianceReports.xml");
                queryListing = new QueryListing();
                theXMLParser.parse(xmlInputStream, queryListing);
                sess.setAttribute("query_listing", queryListing);
            } catch (Exception e ) {
                e.printStackTrace();
            }
            return queryListing;
        }
    }

    public ArgBean[] getArgs(DirNode node, HttpServletRequest request){
        int numArg = getInteger(node.getValue("atlas.args"), 0);

        ArgBean[] args = new ArgBean[numArg];
        boolean fine = true;

        for (int i = 0; i < numArg; i++) {
            String id = node.getValue(ARG_ID_PREFIX + i);
            String name = node.getValue(ARG_NAME_PREFIX + i);
            String type = node.getValue(ARG_TYPE_PREFIX + i);
            args[i] = new ArgBean(id, name, type, false);
            try {
                args[i].getValue(i, request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return args;
    }

    protected int getInteger(String v, int def) {
        try {
            def = Integer.parseInt(v);
        } catch (Exception e) {
        }
        return def;
    }

    private Collection<Map<String, Object>> constructFirstJsonObject() {
        Collection<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();

        Map<String, Object> aMap = new LinkedHashMap<String, Object>();
        String type = "folder";
        Map<String, Boolean> statemap = new HashMap<String, Boolean>();
        statemap.put("opened", false);
        aMap.put("id", "/Configuration Assessment/");
        aMap.put("text", "Configuration Assessment");
        //aMap.put("icon", type);
        aMap.put("children", true);
        aMap.put("state", statemap);

        contents.add(aMap);

        aMap = new LinkedHashMap<String, Object>();
        type = "folder";
        statemap = new HashMap<String, Boolean>();
        statemap.put("opened", false);
        aMap.put("id", "/Vulnerability Assessment/");
        aMap.put("text", "Vulnerability Assessment");
        //aMap.put("icon", type);
        aMap.put("children", true);
        aMap.put("state", statemap);

        contents.add(aMap);

        System.out.println("constructJsonObject contents to view(" + contents.size() + "): " + contents);
        return contents;
    }

    private Collection<Map<String, Object>> constructJsonObject(Map<String, DirNode> childNodes) {
        Collection<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
        for (DirNode node : childNodes.values()) {
            Map<String, Object> aMap = new LinkedHashMap<String, Object>();
            String type = node.getType();
            Map<String, Boolean> statemap = new HashMap<String, Boolean>();
            statemap.put("opened", false);
            aMap.put("id", node.getName());
            aMap.put("text", node.getDisplayName());
            //aMap.put("icon", type);

            aMap.put("children", "directory".equals(type));
            aMap.put("state", statemap);

            contents.add(aMap);
        }
        System.out.println("constructJsonObject contents to view(" + contents.size() + "): " + contents);
        return contents;
    }

    private void createResponse(HttpServletResponse response, Collection<Map<String, Object>> jsonMap) {
          try {
            PrintWriter out = response.getWriter();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contents", jsonMap);

            out.println(jsonObject);
            out.flush();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
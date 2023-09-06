// Copyright 2019, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.webapp.actions;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.securitymgr.view.QueryBean;
import com.marimba.apps.securitymgr.webapp.forms.VDeskQueryresultsForm;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.intf.db.IStatementPool;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.tools.action.DelayedAction;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: May 5, 2017
 * Time: 3:16:16 PM
 * To change this template use File | Settings | File Templates.
 */

public class VDeskQueryresultsAction extends DelayedAction {

    private int DEBUG = DebugFlag.getDebug("SECURITY/REPORTING");
    private String MAIL_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

    private SubscriptionMain main;
    private List<String> columnsList = null;
    private List<List<String>> valuesList = null;

    public Task create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        return new QueryResultsTask((VDeskQueryresultsForm) form, mapping, request);
    }

    public ActionForward done(DelayedAction.Task task, ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        QueryResultsTask resultsTask = (QueryResultsTask) task;
        if (resultsTask.errors != null && !resultsTask.errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, resultsTask.errors);
        }
        return resultsTask.forward;
    }

    private class QueryResultsTask extends Task {
        String action;
        Locale locale;
        HttpSession sess;
        ActionErrors errors;
        ActionMapping mapping;
        ActionForward forward;
        HttpServletRequest request;
        VDeskQueryresultsForm reportForm;

        QueryResultsTask(VDeskQueryresultsForm reportForm, ActionMapping mapping, HttpServletRequest request) {
            this.request = request;
            this.mapping = mapping;
            this.reportForm = reportForm;
            this.sess = request.getSession();
            this.locale = request.getLocale();
            this.action = reportForm.getAction();
            this.errors = (ActionErrors)request.getAttribute(Globals.ERROR_KEY);
            if (this.errors == null) this.errors = new ActionErrors();
        }

        public void execute() {
            ServletContext sc = servlet.getServletConfig().getServletContext();
            main = TenantHelper.getTenantSubMain(sc, request);
            forward = mapping.findForward("view");
            if (null == action || "".equals(action) || "result".equals(action)) {
                //populateTestQueryBean(); // For testing purpose only
                QueryBean queryBean = (QueryBean) sess.getAttribute("SESS_QUERY_BEAN");
                reportForm.setAddViewDetails("/Configuration Assessment/Machine Level Compliance".equals(queryBean.getDisplayPath())
                        || "/Vulnerability Assessment/Machine Level Compliance".equals(queryBean.getDisplayPath()));
                reportForm.setIsVA("/Vulnerability Assessment/Machine Level Compliance".equals(queryBean.getDisplayPath()));
                if (null != queryBean) {
                    QueryRunner queryRunner = new QueryRunner(queryBean.getQuery());
                    reportForm.setQueryTarget(queryBean.getQueryTarget());
                    reportForm.setQueryRanAt(getGMTtime(System.currentTimeMillis()));
                    reportForm.setQueryRanTime(queryRunner.getQueryElapsedTime());
                } else {
                    reportForm.setQueryTarget("N/A");
                    reportForm.setQueryRanAt("N/A");
                    reportForm.setQueryRanTime("N/A");
                }
                reportForm.setSql(queryBean.getQuery());
                reportForm.setDisplayPath(queryBean.getDisplayPath());
                reportForm.setColumnsList(columnsList);
                reportForm.setValuesList(valuesList);
            }
        }

        public String getWaitMessage() {
            return "Please Wait....";
        }

        private void populateTestQueryBean() {
            QueryBean queryBean = new QueryBean();
            queryBean.setQueryTarget("hicbel62925");
            queryBean.setQueryName("Top5 Vulnerabilities");
            queryBean.setQueryUserName("puser");
            queryBean.setQuery("select name, domain, type, location, model, scantime, status, description, wmi_status, number_of_processors from inv_machine");
            sess.setAttribute("SESS_QUERY_BEAN", queryBean);
        }
    }

    class QueryRunner extends DatabaseAccess {
        String theQuery = "";
        Long startTime = System.currentTimeMillis(), endTime = -1L;


        public QueryRunner(String theQuery) {
            this.theQuery = theQuery;
            try {
                runQuery(new QueryLocalExecutor());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public String getQueryElapsedTime() {
            return getElapsedTime(startTime, endTime);
        }

        class QueryLocalExecutor extends QueryExecutor {

            QueryLocalExecutor() {
                super(main);
            }

            protected void execute(IStatementPool pool) throws SQLException {

                debug("Query Str: " + theQuery);
                columnsList = new LinkedList<String>();
                valuesList = new LinkedList<List<String>>();
                PreparedStatement st = pool.getConnection().prepareStatement(theQuery);
                ResultSet rs = st.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                debug("Column count: " + columnCount);
                MessageResources messageResource = main.getAppResources();

                int ruleComplianceColumnIndex = -1;               
                for (int i = 1; i <= columnCount; i++ ) {
                    String columnName = rsmd.getColumnName(i);
                    columnName = (columnName == null || "".equals(columnName) ? "No Column Name" : columnName);
                    columnName = getColumnName(messageResource, columnName);
                    if ("rules_compliance".equals(columnName)) {
                        columnName = "CVSS";
                        ruleComplianceColumnIndex = i;
                    }                   
                    columnsList.add(columnName);
                }
                try {
                    while(rs.next()) {
                        List<String> valueList = new LinkedList<String>();
                        for (int i = 1; i <= columnCount; i++ ) {                        	                        	String columnName = rsmd.getColumnName(i);
                            String value = rs.getString(i);
                            if (ruleComplianceColumnIndex != -1 && ruleComplianceColumnIndex == i) {
                                value = getCVSS(rs.getBytes(i));
                            }                                                      if ("Last Scan Time".equals(columnName)) {                                value = formatDate(value);                            }                            
                            valueList.add(null == value ? "" : value);
                        }
                        valuesList.add(valueList);
                    }
                } catch(SQLException sex) {
                    sex.printStackTrace();
                } finally {
                    rs.close();
                    st.close();
                }
                debug("Columns List ("+columnsList.size()+"): " + columnsList);
                debug("Values total List ("+valuesList.size()+"): " + valuesList);
                endTime = System.currentTimeMillis();
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

    public String getGMTtime(long dateTime) {
        DateFormat gmtFormat = new SimpleDateFormat(MAIL_DATE_FORMAT);
        TimeZone gmtTime = TimeZone.getTimeZone("GMT");
        gmtFormat.setTimeZone(gmtTime);
        return gmtFormat.format(dateTime);
    }  //Formating "Last Scan Time" from yyyy-MM-dd HH:mm:ss.S  to yyyy-MM-dd HH:mm:ss    public String formatDate(String inputDateStr) {        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");        try {                        Date date = inputDateFormat.parse(inputDateStr);                        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");                        return outputDateFormat.format(date);        }         catch (Exception e) {            e.printStackTrace();        }        return null;    }

    private String getCVSS(byte[] jsonBytes) {
        StringBuilder sb = new StringBuilder();

        int num;
        String cvss = "0/0";
        if (jsonBytes == null) return  cvss;
        ByteArrayInputStream bInput = new ByteArrayInputStream(jsonBytes);
        while( (num = bInput.read()) != -1 ) {
            sb.append((char) num);
        }
        int total = 0;
        int passCount =0;
        try {
            JSONObject jsonObject = new JSONObject(sb.toString());
            if (jsonBytes == null || jsonObject.length() == 0) return  cvss;
            JSONObject jsonObject1 = jsonObject.getJSONObject("rules_compliance");
            Iterator iterator = jsonObject1.keys();
            while (iterator.hasNext()) {
                total ++;
                String key = (String)iterator.next();
                if ("pass".equalsIgnoreCase(jsonObject1.getString(key))) {
                    passCount++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(passCount)+ "/" +String.valueOf(total);
    }

    private void debug(String msg) {
        if (DEBUG >= 5) {
            System.out.println("VDeskQueryresultsAction: " + msg);           
        }
    }
}

//m/marimba/apps/securitymgr/webapp/actions/VDeskRuleAction.java $, $Revision: #5 $, $Date: 202package com.marimba.apps.subscriptionmanager.webapp.actions;import com.marimba.apps.securitymgr.db.DatabaseAccess;import com.marimba.apps.securitymgr.db.QueryExecutor;import com.marimba.apps.subscriptionmanager.SubscriptionMain;import com.marimba.apps.subscriptionmanager.TenantHelper;import com.marimba.intf.db.IStatementPool;import com.marimba.tools.util.DebugFlag;import org.apache.struts.action.ActionForm;import org.apache.struts.action.ActionForward;import org.apache.struts.action.ActionMapping;import org.json.JSONArray;import org.json.JSONException;import org.json.JSONObject;import javax.servlet.ServletContext;import javax.servlet.ServletException;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import java.io.IOException;import java.io.PrintWriter;import java.sql.PreparedStatement;import java.sql.ResultSet;import java.sql.SQLException;import com.marimba.apps.subscription.common.ISubscriptionConstants;import com.marimba.apps.subscription.common.intf.IUser;import com.marimba.tools.config.*;import com.marimba.intf.msf.*;import java.util.*;/** * @author Yogesh Pawar * @Description:This is class is repsonsbile for handle the ajax call to display the * top vulnerablity table on dashboard.it is writing the json response. * @Date : 20/07/2023 */public class TopVulnerableDashboardAction extends AbstractAction {    public static boolean DEBUG = DebugFlag.getDebug("SECURITY") >= 5;    private JSONArray topVulnerableDetails;    private SubscriptionMain main;    private int totalRecords;    public ActionForward perform(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)            throws IOException, ServletException {        ServletContext sc = servlet.getServletConfig().getServletContext();        main = TenantHelper.getTenantSubMain(sc, request);        //Reading the request parameter        String pageSize = request.getParameter("pageSize");        String filter = request.getParameter("filter");        String draw = request.getParameter("draw");        String pageNumber = request.getParameter("page");        String search = request.getParameter("search");        String sortColumn = request.getParameter("order[0][column]");        String sortOrder = request.getParameter("order[0][dir]");        try {            int offset = (Integer.parseInt(pageNumber) - 1) * Integer.parseInt(pageSize);            new QueryRunner(Integer.parseInt(pageSize), offset, filter, search, sortColumn, sortOrder);            JSONObject result = new JSONObject();            result.put("draw", draw);            result.put("recordsFiltered", totalRecords);            result.put("recordsTotal", totalRecords);            result.put("data", topVulnerableDetails);            sendJSONResponse(response, result);        } catch (Exception exception) {            debug(exception.getMessage());        }        return null;    }    class QueryRunner extends DatabaseAccess {        public QueryRunner(int pageSize, int pageNumber, String filter, String search, String sortColumn, String sortOrder) {            try {                runQuery(new GetTopVulnerableDetails(pageSize, pageNumber, filter, search, sortColumn, sortOrder));            } catch (Exception ex) {                ex.printStackTrace();            }        }        class GetTopVulnerableDetails extends QueryExecutor {            int pageNumber;            int pageSize;            String filter;            String search;            String sortColumn;            String sortOrder;            GetTopVulnerableDetails(int pageSize, int pageNumber, String filter, String search, String sortColumn, String sortOrder) {                super(main);                this.pageNumber = pageNumber;                this.pageSize = pageSize;                this.filter = filter;                this.search = search;                this.sortColumn = sortColumn;                this.sortOrder = sortOrder;            }            protected void execute(IStatementPool pool) throws SQLException {                getTotalCount(pool);                String fetchTopVulQuery = prepareQuery();                topVulnerableDetails = new JSONArray();                PreparedStatement preparedStatement = pool.getConnection().prepareStatement(fetchTopVulQuery);                int paramIndex = 1;                if (filter != null && !filter.isEmpty()) {                    preparedStatement.setString(paramIndex++, filter);                }                if (search != null && !search.isEmpty()) {                    preparedStatement.setString(paramIndex++, "%" + search.trim() + "%");                    preparedStatement.setString(paramIndex++, "%" + search.trim() + "%");                }                preparedStatement.setInt(paramIndex++, pageNumber);                preparedStatement.setInt(paramIndex, pageSize);                ResultSet rs = preparedStatement.executeQuery();                try {                    fetchAndGenerateTopVulJSON(rs);/*                    if (totalRecords <= 0) {                        getTotalCount(pool);                    }*/                } catch (Exception sqlException) {                    debug(sqlException.getMessage());                } finally {                    rs.close();                    preparedStatement.close();                }            }            private void getTotalCount(IStatementPool pool) {                String fetchCountQuery = "SELECT COUNT(*) FROM ds_derived_top_vuln";                if (filter != null && !filter.isEmpty()) {                    fetchCountQuery += " WHERE status = ?";                }                if (search != null && !search.isEmpty()) {                    if (fetchCountQuery.contains("WHERE")) {                        fetchCountQuery += " AND (patch_id LIKE ? OR cve_id LIKE ?)";                    } else {                        fetchCountQuery += " WHERE (patch_id LIKE ? OR cve_id LIKE ?)";                    }                }                // made check if records exists on security_oval_compliance                if (fetchCountQuery.indexOf("WHERE") != -1) {                    fetchCountQuery += " AND exists (select count(machine_name) " +                            " from inv_security_oval_compliance " +                            " group by machine_name having count(machine_name) > 0)";                } else {                    fetchCountQuery += " where exists (select count(machine_name) " +                            " from inv_security_oval_compliance " +                            " group by machine_name having count(machine_name) > 0)";                }                try {                    PreparedStatement prepareStatement = pool.getConnection().prepareStatement(fetchCountQuery);                    int paramIndex = 1;                    if (filter != null && !filter.isEmpty()) {                        prepareStatement.setString(paramIndex++, filter);                    }                    if (search != null && !search.isEmpty()) {                        prepareStatement.setString(paramIndex++, "%" + search.trim() + "%");                        prepareStatement.setString(paramIndex, "%" + search.trim() + "%");                    }                    ResultSet rs = prepareStatement.executeQuery();                    while (rs.next()) {                        totalRecords = rs.getInt(1);                    }                } catch (SQLException sqlException) {                    debug(sqlException.getMessage());                }            }            private String prepareQuery() {                try {                    String fetchTopVulQuery = "SELECT cve_id, severity, affected_machines, patch_id, status, risk_score FROM ds_derived_top_vuln";                    if (filter != null && !filter.isEmpty()) {                        fetchTopVulQuery += " WHERE status = ?";                    }                    if (search != null && !search.isEmpty()) {                        if (fetchTopVulQuery.contains("WHERE")) {                            fetchTopVulQuery += " AND (patch_id LIKE ? OR cve_id LIKE ?)";                        } else {                            fetchTopVulQuery += " WHERE (patch_id LIKE ? OR cve_id LIKE ?)";                        }                    }                    // made check if records exists on security_oval_compliance                    if (fetchTopVulQuery.indexOf("WHERE") != -1) {                        fetchTopVulQuery += " AND exists (select count(machine_name) " +                                " from inv_security_oval_compliance " +                                " group by machine_name having count(machine_name) > 0)";                    } else {                        fetchTopVulQuery += " where exists (select count(machine_name) " +                                " from inv_security_oval_compliance " +                                " group by machine_name having count(machine_name) > 0)";                    }                    if (sortOrder != null && !sortOrder.isEmpty() && sortColumn != null && !sortColumn.isEmpty() && !sortColumn.equals("0")) {                        fetchTopVulQuery += " ORDER BY " + sortColumn + " " + sortOrder.toUpperCase() + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";                    } else {                        fetchTopVulQuery += " ORDER BY risk_score DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";                    }                    return fetchTopVulQuery;                } catch (Exception exception) {                    debug(exception.getMessage());                }                return null;            }            private void fetchAndGenerateTopVulJSON(ResultSet rs) {                try {                    while (rs.next()) {                        String cveID = rs.getString(1);                        String severity = rs.getString(2);                        String affectedMachines = rs.getString(3);                        String patchId = rs.getString(4);                        String status = rs.getString(5);                        Integer riskScore = rs.getInt(6);                        JSONObject topVulJson = new JSONObject();                        topVulJson.put("cveId", cveID);                        topVulJson.put("severity", severity);                        topVulJson.put("affectedMachines", affectedMachines);                        topVulJson.put("patchId", patchId);                        topVulJson.put("status", status);                        topVulJson.put("riskScore", riskScore);                        topVulnerableDetails.put(topVulJson);                    }                } catch (Exception exception) {                    debug(exception.getMessage());                }            }        }    }    public void sendJSONResponse(HttpServletResponse response, JSONObject jsonObject) throws Exception {        PrintWriter out = response.getWriter();        out.println(jsonObject.toString());        out.flush();    }    public void setDebug(boolean _DEBUG) {        DEBUG = _DEBUG;    }    public static void debug(String str) {        if (DEBUG) {            System.out.println("TopVulnerableDashboardAction.java :: [" + new Date().toString() + "] ==> " + str);        }    }}
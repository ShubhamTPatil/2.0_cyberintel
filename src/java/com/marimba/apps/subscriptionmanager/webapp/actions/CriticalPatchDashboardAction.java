//m/marimba/apps/securitymgr/webapp/actions/VDeskRuleAction.java $, $Revision: #5 $, $Date: 202
package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.intf.db.IStatementPool;
import com.marimba.tools.util.DebugFlag;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.tools.config.*;
import com.marimba.intf.msf.*;

import java.util.*;

/**
 * @author Shaik ReshmaBegum
 * @Description:This class is responsible for handle the ajax call to display the 
 * critical patches table on dashboard.it is writing the json response.
 * @Date : 20/12/2023
 */

public class CriticalPatchDashboardAction extends AbstractAction {

  private int DEBUG = DebugFlag.getDebug("DEFEN/DASHBOARD");

  private JSONArray criticalPatchDetails;

  private SubscriptionMain main;

  private int totalRecords;

  public ActionForward perform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response)

      throws IOException, ServletException {

    ServletContext sc = servlet.getServletConfig().getServletContext();

    main = TenantHelper.getTenantSubMain(sc, request);

    //Reading the request parameter
    String pageSize = request.getParameter("pageSize");
    String filter = request.getParameter("filter");
    String draw = request.getParameter("draw");
    String pageNumber = request.getParameter("page");
    String search = request.getParameter("search");
    String sortColumn = request.getParameter("order[0][column]");
    String sortOrder = request.getParameter("order[0][dir]");

    try {
      int offset = (Integer.parseInt(pageNumber) - 1) * Integer.parseInt(pageSize);
      new QueryRunner(Integer.parseInt(pageSize), offset, filter, search, sortColumn, sortOrder);
      JSONObject result = new JSONObject();
      result.put("draw", draw);
      result.put("recordsFiltered", totalRecords);
      result.put("recordsTotal", totalRecords);
      result.put("data", criticalPatchDetails);
      sendJSONResponse(response, result);

    } catch (Exception exception) {
    	if (DEBUG >= 5) {
            debugError(exception);
          }
          error(exception.getMessage());
          }

    
    return null;

  }


  class QueryRunner extends DatabaseAccess {

    public QueryRunner(int pageSize, int pageNumber, String filter, String search,
        String sortColumn, String sortOrder) {

      try {

        runQuery(new GetcriticalPatchDetails(pageSize, pageNumber, filter, search, sortColumn,
            sortOrder));

      } catch (Exception ex) {

        ex.printStackTrace();

      }

    }

    class GetcriticalPatchDetails extends QueryExecutor {

      int pageNumber;

      int pageSize;

      String filter;

      String search;
      String sortColumn;

      String sortOrder;


      GetcriticalPatchDetails(int pageSize, int pageNumber, String filter, String search,
          String sortColumn, String sortOrder) {

        super(main);

        this.pageNumber = pageNumber;

        this.pageSize = pageSize;

        this.filter = filter;

        this.search = search;

        this.sortColumn = sortColumn;

        this.sortOrder = sortOrder;

      }


      protected void execute(IStatementPool pool) throws SQLException {

        getTotalCount(pool);

        String fetchCritPatchQuery = prepareQuery();

        criticalPatchDetails = new JSONArray();

        PreparedStatement preparedStatement = pool.getConnection()
            .prepareStatement(fetchCritPatchQuery);

        int paramIndex = 1;
        if (filter != null && !filter.isEmpty()) {
          preparedStatement.setString(paramIndex++, filter);
        }
        if (search != null && !search.isEmpty()) {
          preparedStatement.setString(paramIndex++, "%" + search.trim() + "%");
          preparedStatement.setString(paramIndex++, "%" + search.trim() + "%");
        }
        preparedStatement.setInt(paramIndex++, pageNumber);
        preparedStatement.setInt(paramIndex, pageSize);

        ResultSet rs = preparedStatement.executeQuery();

        try {
          fetchAndGenerateCritPatchJSON(rs);

        } catch (Exception sqlException) {
          if (DEBUG >= 3) {
        	  debugError(sqlException);
            }
            info(sqlException.getMessage());
        }

        finally {
          rs.close();
          preparedStatement.close();
        }

      }

      private void getTotalCount(IStatementPool pool) {
        String fetchCountQuery = "SELECT COUNT(*) FROM ds_derived_crit_patches";

        if (filter != null && !filter.isEmpty()) {
          fetchCountQuery += " WHERE status = ?";
        }

        if (search != null && !search.isEmpty()) {
          if (fetchCountQuery.contains("WHERE")) {
            fetchCountQuery += " AND (patch_id LIKE ? OR cve_id LIKE ?)";
          } else {
            fetchCountQuery += " WHERE (patch_id LIKE ? OR cve_id LIKE ?)";
          }
        }

        // made check if records exists on security_oval_compliance
        if (fetchCountQuery.indexOf("WHERE") != -1) {
          fetchCountQuery += " AND exists (select count(machine_name) " +
              " from inv_security_oval_compliance " +
              " group by machine_name having count(machine_name) > 0)";
        } else {
          fetchCountQuery += " where exists (select count(machine_name) " +
              " from inv_security_oval_compliance " +
              " group by machine_name having count(machine_name) > 0)";
        }

        try {
          PreparedStatement prepareStatement = pool.getConnection()
              .prepareStatement(fetchCountQuery);

          int paramIndex = 1;
          if (filter != null && !filter.isEmpty()) {
            prepareStatement.setString(paramIndex++, filter);
          }
          if (search != null && !search.isEmpty()) {
            prepareStatement.setString(paramIndex++, "%" + search.trim() + "%");
            prepareStatement.setString(paramIndex, "%" + search.trim() + "%");
          }

          ResultSet rs = prepareStatement.executeQuery();
          while (rs.next()) {
            totalRecords = rs.getInt(1);
          }
        } catch (SQLException sqlException) {
          if (DEBUG >= 5) {
            debugError(sqlException);
          }
          error(sqlException.getMessage());
        }
      }

      private String prepareQuery() {
        try {
          String fetchCritPatchQuery = "SELECT cve_id, patch_id, severity, affected_machines, status FROM ds_derived_crit_patches";

          if (filter != null && !filter.isEmpty()) {
            fetchCritPatchQuery += " WHERE status = ?";
          }

          if (search != null && !search.isEmpty()) {
            if (fetchCritPatchQuery.contains("WHERE")) {
              fetchCritPatchQuery += " AND (patch_id LIKE ? OR cve_id LIKE ?)";
            } else {
              fetchCritPatchQuery += " WHERE (patch_id LIKE ? OR cve_id LIKE ?)";
            }
          }

          // made check if records exists on security_oval_compliance
          if (fetchCritPatchQuery.indexOf("WHERE") != -1) {
            fetchCritPatchQuery += " AND exists (select count(machine_name) " +
                " from inv_security_oval_compliance " +
                " group by machine_name having count(machine_name) > 0)";
          } else {
            fetchCritPatchQuery += " where exists (select count(machine_name) " +
                " from inv_security_oval_compliance " +
                " group by machine_name having count(machine_name) > 0)";
          }

          if (sortOrder != null && !sortOrder.isEmpty() && sortColumn != null
              && !sortColumn.isEmpty() && !sortColumn.equals("0")) {
            fetchCritPatchQuery += " ORDER BY " + sortColumn + " " + sortOrder.toUpperCase()
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
          } else {
            fetchCritPatchQuery += " ORDER BY cve_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
          }

          return fetchCritPatchQuery;
        } catch (Exception exception) {
          if (DEBUG >= 3) {
                debugError(exception);
          }
            info(exception.getMessage());
       }
        
        return null;
      }

      private void fetchAndGenerateCritPatchJSON(ResultSet rs) {
        try {

          while (rs.next()) {

            String cveID = rs.getString(1);

            String patchId = rs.getString(2);

            String severity = rs.getString(3);

            String affectedMachines = rs.getString(4);

            String status = rs.getString(5);

            JSONObject critPatchJson = new JSONObject();

            critPatchJson.put("cveId", cveID);
            critPatchJson.put("patchId", patchId);
            critPatchJson.put("severity", severity);
            critPatchJson.put("affectedMachines", affectedMachines);
            critPatchJson.put("status", status);
            criticalPatchDetails.put(critPatchJson);

          }

        } catch (Exception exception) {
        	if (DEBUG >= 5) {
                debugError(exception);
              }
              error(exception.getMessage());
        }
      }

    }

  }

  public void sendJSONResponse(HttpServletResponse response, JSONObject jsonObject)
      throws Exception {

    PrintWriter out = response.getWriter();

    out.println(jsonObject.toString());

    out.flush();

  }

  public void debugError(Exception exception) {
    exception.printStackTrace();
  }

}
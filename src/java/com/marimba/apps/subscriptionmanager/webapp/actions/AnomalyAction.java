// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.User;
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.AnomalyUtil;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.intf.util.IDirectory;
import com.marimba.tools.util.DebugFlag;
import java.io.IOException;
import java.io.PrintWriter;
import com.marimba.intf.util.IConfig;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;


/**
 * Set the context and forwads on to the target view page. If 'forward' request parameter is set,
 * the action will forward to that page or action. This action may switch to single select mode
 * depending on the caller specified by the 'src' request parameter.
 *
 * @author Bhavesh Pandya
 * @version 1.0, 09/04/2023
 */
public final class AnomalyAction
    extends AbstractAction {

  /**
   * REMIND
   *
   * @param mapping REMIND
   * @param form REMIND
   * @param request REMIND
   * @param response REMIND
   * @return REMIND
   * @throws IOException REMIND
   * @throws ServletException REMIND
   */

  public final static int DEBUG = DebugFlag.getDebug("DEFENSIGHT/ACTION");
  public ActionForward execute(ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException,
      ServletException, Exception {
    init(request);
    SubscriptionMain main = null;
    String reqTenantName = TenantHelper.getTenantName(request);
    main = TenantHelper.getTenantSubMain(context, request.getSession(), reqTenantName);
    IDirectory features = main.getFeatures();
    IConfig tunerConfig = (IConfig) features.getChild("tunerConfig");
    AnomalyUtil anomalyUtil = new AnomalyUtil();
    String action = request.getParameter("action");
    if (action == null) {
      return (mapping.findForward("success"));
    }
    if (action.equals("heatmapData")) {
      String fetchAllData = anomalyUtil.fetchDataForChart(tunerConfig, action);
      sendJSONResponse(response, fetchAllData);
    }
    return (mapping.findForward("success"));
  }
  protected void sendJSONResponse(HttpServletResponse response, String jsonData) throws Exception {
    PrintWriter out = response.getWriter();
    out.println(jsonData);
    out.flush();
  }
  private void debugInfo(String msg) {
    if (DEBUG >= 5) {
      System.out.println("DEBUG:AnomalyAction : " + msg);
    }
  }
  private void log(String msg) {
    if (DEBUG >= 3) {
      System.out.println("LogInfo:AnomalyAction : " + msg);
    }
  }

}

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.intf.util.IConfig;
import com.marimba.tools.net.HTTPConfig;
import com.marimba.castanet.http.HTTPManager;
import com.marimba.tools.config.ConfigUtil;
import com.marimba.apps.securitymgr.utils.ElasticSecurityMgmt;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.io.IOException;

import com.marimba.intf.util.IObserver;

import com.marimba.webapps.tools.util.PropsBean;

public class CheckElasticServerAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants {
	
	ActionForward forward;
	
    public ActionForward perform(ActionMapping mapping, ActionForm form, HttpServletRequest  request,HttpServletResponse response)throws IOException,ServletException {

    init(request);
    String elasticUrl = request.getParameter("elasticurl");
    try {
    	JSONObject result = new JSONObject();
    	
    	if (null == elasticUrl || elasticUrl.trim().isEmpty()) {
	        System.out.println("Elastic server URL is null");
	        result.put("elasticstatus", "Not Configured");
	        sendJSONResponse(response, result);
	        return mapping.findForward("success");
	    }
	    
    	elasticUrl = (elasticUrl.endsWith("/")) ? elasticUrl + "_search" : elasticUrl + "/_search";
    	//System.out.println("features -"+ features);
    	//System.out.println("features.getChild(\"tunerConfig\") - "+ features.getChild("tunerConfig"));

	    IConfig tunerConfig = (IConfig) features.getChild("tunerConfig");
	    HTTPConfig httpConfig = new HTTPConfig(new ConfigUtil(tunerConfig));
	    HTTPManager httpManager = new HTTPManager(httpConfig);
	    ElasticSecurityMgmt securityMgmt = new ElasticSecurityMgmt(httpManager, httpConfig, elasticUrl);
		
	    boolean elasticup = securityMgmt.isElasticUP();
	    String elasticServerStatus = "";
	    elasticServerStatus = (elasticup == false)? "Down" : "Up";
	    System.out.println("elasticserver status in Plugin: "+elasticServerStatus);
	    
	    result.put("elasticstatus", elasticServerStatus);
	    sendJSONResponse(response, result);
	    return mapping.findForward("success");
    } catch (Exception e){ e.printStackTrace(); }
    	
    	return mapping.findForward("success");
    }
    public void sendJSONResponse(HttpServletResponse response, JSONObject jsonObject)throws Exception {
        PrintWriter out = response.getWriter();
        out.println(jsonObject.toString());
        out.flush();
    }
}


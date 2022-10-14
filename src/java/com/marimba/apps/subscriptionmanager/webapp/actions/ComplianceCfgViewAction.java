// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.users.UserManager;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.core.ConfigManager;
import com.marimba.apps.subscriptionmanager.compliance.view.ConfigBean;

import com.marimba.webapps.intf.SystemException;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;

import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;

/**
 * For compliance config related actions
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
public class ComplianceCfgViewAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {

    public ActionForward perform( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest  request,
                                  HttpServletResponse response )throws IOException, ServletException {
        init(request);

        IMapProperty props = ( IMapProperty )form;
        String configOption = ( String )props.getValue( "configOption" );
        props.setValue( "configOption", null );
	    HttpSession session = request.getSession();
	    ServletContext ctx = session.getServletContext();
	    TenantAttributes tenantAttr = UserManager.getTenantAttr(tenant.getName());
	    ComplianceMain cm = tenantAttr.getCompMain();
        ActionForward forward = null;
        try {
            IUser user = GUIUtils.getUser( request );
            if ( "save".equals( configOption ) ) {
                ConfigBean cb = new ConfigBean();
                cb.setCacheListMax((String)props.getValue( "cache_list_max" ));
                //cb.setCacheListExt((String)props.getValue( "cache_list_ext" ));
                cb.setCacheObjMax((String)props.getValue( "cache_obj_max" ));
                //cb.setCacheObjExt((String)props.getValue( "cache_obj_ext" ));
                cb.setWaitTime((String)props.getValue( "wait_time" ));
                cb.setCheckinLimit((String)props.getValue( "checkin_limit" ));
                cb.setCollectCompEnabled( (String)props.getValue( "coll_comp" ) );
                cm.getConfig().setConfig( cb, user );

                form.reset( mapping, request);

                main.logAuditInfo(LOG_AUDIT_COMPLIANCESETTINGS, LOG_AUDIT,  "Policy Manager",
                        "Collection of compliance data " + ("enable".equals(cb.getCollectCompEnabled()) ? "Enabled, " : "Disabled, ")
                        + "Maximum cache time for list results " + cb.getCacheListMax() + " seconds, "
                        + "Maximum cache time for non-list results " + cb.getCacheObjMax() + " seconds, "
                        + "Time to wait for a query result " + cb.getWaitTime() + " seconds, "
                        + "Time limit for checked-in status " + cb.getCheckInLimit() + " seconds"
                        , request, COMPLIANCE_CONF);

                forward = mapping.findForward("save");
            } else {
                ConfigBean cb = new ConfigBean();
                cm.getConfig().populate( cb, user );
                request.setAttribute("compCfg", cb);
                forward = mapping.findForward("load");
            }
        }catch( Exception exp ){
            exp.printStackTrace();
            throw new GUIException( exp );
        }
        return forward;
    }
}

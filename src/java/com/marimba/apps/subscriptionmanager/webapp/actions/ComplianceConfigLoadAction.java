package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Collection;
import java.net.InetAddress;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.webapp.forms.SetComplianceForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.IUser;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IProperty;

/**
 * User: Jean Ro
 * Date: Jul 27, 2004
 * Time: 5:18:45 PM
 */
public class ComplianceConfigLoadAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        try {
            SetComplianceForm configForm = (SetComplianceForm) form;
            ServletContext context = servlet.getServletConfig().getServletContext();
            SubscriptionMain smmain = TenantHelper.getTenantSubMain(context, request);
            smmain.refreshSubscriptionConfig(smmain.getAdminUser().getBrowseConn());
            IProperty config = smmain.getSubscriptionConfig();

            //fetch the host name
            String hostName = config.getProperty(CALC_COMP_SPM_HOSTNAME);
            if(hostName != null) {
                hostName = hostName.trim();
                if(hostName.length() == 0) {
                    hostName = null;
                }
            }

            configForm.setHostName(hostName);
            String calcCompEnabled = CALC_COMP_SPM_DISABLE;
            if(hostName != null) {
                if(InetAddress.getLocalHost().getHostName().equals(hostName.trim())) {
                    calcCompEnabled = CALC_COMP_SPM_ENABLE;
                }
            }
            configForm.setCalcCompEnabled(calcCompEnabled);

            IUser user = GUIUtils.getUser(request);
            String collectCompEnabled = isCollectingComplianceData(user);
            configForm.setCollectCompEnabled(collectCompEnabled);

            // set the target option
            String cacheOption = config.getProperty(COMPLIANCE_GROUP2CACHE);
            configForm.setCacheOption(cacheOption);
            String isRecompute = ( String )request.getParameter( "recompute" );
            ArrayList targetList = null;
            if(SPECIFIED_ONLY.equalsIgnoreCase(cacheOption)) {
                targetList = LDAPUtils.getTargetList(config);
                configForm.setTargetlist(targetList);
                // adding new target to in the list of targets to recompute compliance
                if( targetList != null && isRecompute!=null && isRecompute.equals( "true" ) ){
                    addRecomputeTarget( targetList, request );
                }
                if(targetList == null || targetList.size() == 0) {
                    configForm.setAddblankrow("true");
                }
            } else if( isRecompute != null && isRecompute.equals( "true" ) ){
                configForm.setCacheOption( SPECIFIED_ONLY );
                targetList = new ArrayList(1);
                targetList.add( "all" );
                addRecomputeTarget( targetList, request );
                configForm.setTargetlist(targetList);
            }

            if( targetList != null ){
                int tlSize = targetList.size();
                configForm.setTargetsize(Integer.toString(tlSize));
                request.getSession().setAttribute("tarraysize", Integer.toString(tlSize));
            }

            // set the report refresh schedule
	    configForm.setSchedule("tn_tx_", config.getProperty(COMPLIANCE_REPORT_CACHE_SCHED));
            configForm.setClearData(false);

            configForm.setReadOnlySchedule(config.getProperty(COMPLIANCE_REPORT_CACHE_SCHED));

        } catch (Exception e) {
            throw new GUIException(e);
        }

        return (mapping.findForward("success"));
    }

    private void addRecomputeTarget( ArrayList targetList, HttpServletRequest request ){
        Target target = null;
        try {
            target = (Target)GUIUtils.getFromSession(request, IWebAppConstants.SESSION_PC_TARGET);
        } catch (SystemException e) {
            e.printStackTrace();
        }
        if( !targetList.contains( target.getId() ) ){
            targetList.add( target.getId() );
        }
    }

	String isCollectingComplianceData(IUser  user){
		String retval = "disable";
		try {
			ISubscription sub = ObjectManager.openSubForRead(TYPE_ALL,TYPE_ALL, user);
			String shouldCollectData = sub.getProperty(PROP_TUNER_KEYWORD, COLLECT_INVENTORY_DATA);
			if ("true".equalsIgnoreCase(shouldCollectData)){
				retval = "enable";
			}
		} catch( SystemException sys){
		}
		return retval;
	}


}

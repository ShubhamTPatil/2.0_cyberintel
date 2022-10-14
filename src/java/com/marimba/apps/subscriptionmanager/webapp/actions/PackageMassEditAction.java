// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.webapps.tools.action.DelayedAction;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.actions.common.GenericDistributionAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageMassEditForm;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.distribution.PackageBean;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This action handles mass edit related operations
 *
 * @author Marie Antoine
 */

public class PackageMassEditAction extends AbstractAction {

    protected DelayedAction.Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
		return new MassEditTask(mapping, form, request, response);
	}

	protected class MassEditTask
	        extends SubscriptionDelayedTask {
		MassEditTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
			super(mapping, form, request, response);
		}


		public void execute() {
            try {
                PackageMassEditForm massForm = (PackageMassEditForm) form;

                String action = massForm.getAction();
                if ("save".equals(action)) {
                    saveState(request, massForm);

                    request.setAttribute("parent.reload","true");
                    forward = mapping.findForward("view");
                }
                else if ("view".equals(action)) {
                    request.setAttribute("parent.reload","");
                    forward = mapping.findForward("view");
                    massForm.initStateInc(request);
                    //Remove old session variable so current policy will be stored
                    request.getSession().removeAttribute(SESSION_SELECT_PACKAGES);
                }
                else {
                    request.setAttribute("wait.message","Loading packages");
                    forward = mapping.findForward("wait");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                guiException = new GUIException(new CriticalException(ex.toString()));
                forward = mapping.findForward("failure");
			}
        }

        private void saveState(HttpServletRequest request, PackageMassEditForm form) {
            DistributionBean distBean = getDistributionBean(request);
            boolean selected = false;
            if( distBean != null) {
                //Collect selected checkbox values
                Map selMap = form.getProps();
                Set selSet = selMap.keySet();
                Iterator selIt = selSet.iterator();
                String s;
                while (selIt.hasNext()) {
                    s = (String) selIt.next();
                    if(null != s && s.startsWith("pkgurl")) {

                        String selPkg = s.substring("pkgurl#".length());
                        String url = (String) form.getValue("pkgurl#"+ selPkg);

                        String priState = (String)form.getValue("state#"+ selPkg);
                        String priStateIncons = (String)form.getValue("stateInc#"+ selPkg);
                        String secState = (String)form.getValue("secState#"+ selPkg);
                        String secStateIncons = (String)form.getValue("secStateInc#"+ selPkg);
                        String wowDep = (String)form.getValue("wowDep#"+ selPkg);
                        String exempt = (String)form.getValue("exemptBo#"+ selPkg);

                        if(url != null) {
                            Channel ch = distBean.getChannel(url);
                            if(ch != null) {
                                if("true".equals(wowDep)) {
                                    ch.setWowEnabled(true);
                                } else {
                                    ch.setWowEnabled(false);
                                }

                                if("true".equals(exempt)) {
                                    ch.setExemptFromBlackout(true);
                                } else {
                                    ch.setExemptFromBlackout(false);
                                }

                                if (priStateIncons != null) {
                                     if ("true".equals(priStateIncons)) {
                                         ch.setState(INCONSISTENT);
                                    } else {
                                         if(priState != null) {
                                            ch.setState(priState);
                                         }
                                     }
                                } else {
                                    if(priState != null) {
                                        ch.setState(priState);
                                    }
                                }

                                if (secStateIncons != null) {
                                     if ("true".equals(secStateIncons)) {
                                         ch.setSecState(INCONSISTENT);
                                    } else {
                                         if(secState != null) {
                                            ch.setSecState(secState);
                                         }
                                     }
                                } else {
                                    if(secState != null) {
                                        ch.setSecState(secState);
                                    }
                                }
                            }

                            //Atlease one package is selected
                            selected = true;
                        }
                    }
                }

                if(selected) {
                    //Ask for resetting results
                    request.getSession().setAttribute(SESSION_PERSIST_RESETRESULTS, "true");
                }
            }
        }
    }
}

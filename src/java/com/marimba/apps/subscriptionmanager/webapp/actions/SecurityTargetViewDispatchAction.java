package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.securitymgr.utils.SecurityLDAPUtils;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.*;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.beans.SecurityTargetDetailsBean;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.intf.msf.*;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.SecurityTargetDetailsForm;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class SecurityTargetViewDispatchAction extends AbstractAction {
	String forwardAction = "success";
	SecurityTargetDetailsForm targetDetailsForm;
	HttpSession session;
	boolean primaryAdmin = false;
	IUser user;
	protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new  SecurityTargetViewDispatchActionTask(mapping, form, request, response);
    }

    protected class SecurityTargetViewDispatchActionTask extends SubscriptionDelayedTask {
    	SecurityTargetViewDispatchActionTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
            session = request.getSession();
        }
        public String getWaitMessage() {
            return getString(locale, "page.security.processing.request");
        }

    	public void execute(){
	    	try {
	    		System.out.println("Security Target view dispatcher action called..");
	    		init(request);
	    		user = (IUser) session.getAttribute(SESSION_SMUSER);
	    		
	    		targetDetailsForm = (SecurityTargetDetailsForm) form;

	    		String name = request.getParameter("name");
	    		String type = request.getParameter("type");
	    		String id = request.getParameter("id");

	    		if(null != session.getAttribute("context")) {
	    			String curContext = (String) session.getAttribute("context");
	    			if("compTarget".equals(curContext)) {
	    				session.setAttribute("viewcompliance", "true");
                        forwardAction = "viewcompliance";
	    			} else if("copyAdd".equals(curContext)) {
	    				if(null != session.getAttribute("viewcompliance")) {
	    					session.removeAttribute("viewcompliance");
	    				}
	    				forwardAction = "copytarget";
	    			} else {
	    				if(null != session.getAttribute("viewcompliance")) {
	    					session.removeAttribute("viewcompliance");
	    				}
                        forwardAction = "success";
	    			}
	    		} else {
    				if(null != session.getAttribute("viewcompliance")) {
    					session.removeAttribute("viewcompliance");
    				}
                    forwardAction = "success";
	    		}
	    		if(null != name && null != type && null != id) {
	    			try {
	    				String targetType = LDAPUtils.objClassToTargetType(type, main.getLDAPVarsMap());
	    				if(null != targetType) {
	    					type = targetType;
	    				}
	    			} catch(Exception ed) {
	    				ed.printStackTrace();
	    			}
    	    		
	    			session.setAttribute(SELECTED_TARGET_ID, id);
	    			session.setAttribute(SELECTED_TARGET_NAME, name);
	    			session.setAttribute(SELECTED_TARGET_TYPE, type);
	    			ArrayList targetsList = new ArrayList(DEF_COLL_SIZE);
		    		Target availableTarget = new Target(name, type, id);
		    		targetsList.add(availableTarget);
		    		session.setAttribute(MAIN_PAGE_TARGET, targetsList);
		    		session.setAttribute( "target", availableTarget );
		    		
	    		}
	    			if(null != session.getAttribute(SELECTED_TARGET_ID)) {
	    				name = (String) session.getAttribute(SELECTED_TARGET_NAME);
	    				type = (String) session.getAttribute(SELECTED_TARGET_TYPE);
	    				id = (String) session.getAttribute(SELECTED_TARGET_ID);
	    				
	    				System.out.println("selected name :" + name);
	    	    		System.out.println("type :" + type);
	    	    		System.out.println("id :" + id);

	    	    		targetDetailsForm.setTargetId(id);
	    	    		targetDetailsForm.setTargetName(name);
	    	    		targetDetailsForm.setTargetType(type);
    	    		
	    	    		getAssignedPolicy(name, type, id);
	    			} else {
	    				System.out.println("No target selected");
	    			}
	    	} catch(Exception ex) { 
	    		ex.printStackTrace();
				if(ex instanceof GUIException) {
		            guiException = new GUIException(new CriticalException(ex, ((GUIException)ex).getMessageKey()));
		        } else if(ex instanceof SystemException) {
		            guiException = new GUIException(new CriticalException(ex, ((SystemException)ex).getKey()));
		        } else {
		            guiException = new GUIException(new CriticalException(ex.toString()));
		        }
			}

            session.setAttribute("hasContent", "" + SCAPUtils.getSCAPUtils().hasSyncedContent());
			forward = mapping.findForward(forwardAction);
    	}
    	private void getAssignedPolicy(String name, String type, String id) {
    		List<SecurityTargetDetailsBean> policyBean = SecurityLDAPUtils.getAssignedPolicy(name, type, id, main, user);
			if(policyBean.size() > 0) {
				targetDetailsForm.setAssignSecurityDetailsBean(policyBean);
			} else {
				targetDetailsForm.setAssignSecurityDetailsBean(null);
			}
    	}
    }
}

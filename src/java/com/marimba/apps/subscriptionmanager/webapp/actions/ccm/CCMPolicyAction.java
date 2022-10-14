// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions.ccm;


import com.marimba.apps.subscriptionmanager.arsystem.*;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.intf.msf.arsys.ARManagerException;
import com.marimba.intf.msf.arsys.IARConstants;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * @author Devendra Vamathevan
 * @version 1.0, 5/12/2004
 */

public final class CCMPolicyAction extends AbstractAction {
	protected void init(HttpServletRequest request) {
		super.init(request);
	}

	protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

		return new ExtractFromARTask(mapping, form, request, response);
	}

	protected class ExtractFromARTask extends SubscriptionDelayedTask {
		ExtractFromARTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
			super(mapping, form, request, response);
		}


		public void execute() {
			forward = null;
			CCMPolicyAction.this.init(request);
			//init app and user if this page is called directly
			initApp(request);
			initUser(session, main);
            doQueryAR();
			if (forward == null){
				forward = mapping.findForward("success");
			}
		}

		public void doQueryAR() {
            ARLogMgr arLogMgr = null;
			try {
                IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
                session.setAttribute(IARTaskConstants.AR_TASK_ID, "-1");
                ARRequestProcessor arRequestProcessor = new ARRequestProcessor(main, request.getParameterMap(), user, null);
                arLogMgr = arRequestProcessor.getARObjectSource().getLogManager();
		    	List targets = arRequestProcessor.getTargets();
                ArrayList targetsDisplay = new ArrayList(DEF_COLL_SIZE);
                ArrayList targetsACL = new ArrayList(DEF_COLL_SIZE);
                session.setAttribute(IARTaskConstants.AR_TASK_ID, arRequestProcessor.getTaskID());
				session.setAttribute(IARTaskConstants.AR_CHANGE_ID, arRequestProcessor.getChangeID());
                arLogMgr.log(ARTaskLogConstants.LOG_AR_TASK_ID, LOG_AUDIT, arRequestProcessor.getTaskID(), null, COMPLIANCE_POLICY);
                arLogMgr.log(ARTaskLogConstants.LOG_AR_CHANGE_ID, LOG_AUDIT, arRequestProcessor.getChangeID(), null, COMPLIANCE_POLICY);
                Iterator iter = targets.iterator();
                String type = null;
                boolean permissionExists = true;
                while(iter.hasNext()) {
                    Target tg = (Target)iter.next();
                    try {
                        type = LDAPUtils.objClassToTargetType(tg.getType(),LDAPVarsMap);
                    }catch(SubInternalException sie) {
                        //ignore the exception if the target type is resolved.
                    }
                    Target tg1 = new Target(tg.getName(), type, tg.getID());
                    permissionExists = hasPolicyWritePermission(tg1, user);
                    if(permissionExists) {
                        targetsDisplay.add(tg1);
                    } else {
                        targetsACL.add(tg1);
                    }
                }

                HashMap hm = new HashMap();
                hm.put(IARTaskConstants.AR_TASK_ID, new String[] {arRequestProcessor.getTaskID()});
                hm.put(IARTaskConstants.AR_USER, getString(locale, "ar.worklog.modifieduser")+user.getName());
                ARWorklog workLog = new ARWorklog(hm, tenant);

                if(!permissionExists) {
                    session.setAttribute(IARTaskConstants.ACL_TARGETS, targetsACL);
                    guiException = new GUIException(new CriticalException(AR_NO_ACL_PERMISSION));
                    Iterator iter_ACL = targetsACL.iterator();
                    while(iter_ACL.hasNext()){
                        Target aclTarget = (Target)iter_ACL.next();
                        workLog.write(aclTarget.getId());
                    }
                    workLog.close(hm, IARConstants.TMS_RETURN_CODE_ERROR, getString(locale, "ar.worklog.noaclwritepermission"));
                    arLogMgr.log(ARTaskLogConstants.LOG_AR_NO_ACLWRITE_PERMISSION, LOG_MAJOR, user.getName(), guiException);
                    forward = mapping.findForward("aclwrite");
                    return;
                }
				//By Default - set the return page type to the target view.
                //The channel is saved in Subscription only, if it from target view or package view.
                session.setAttribute(SESSION_RETURN_PAGETYPE, TARGET_VIEW);

				session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
                // session variable to load the channels into the distribution bean for the first 
                // time when we loads the page.
                session.setAttribute(IARTaskConstants.AR_TARGET_REFRESH, "true");

                arLogMgr.log(ARTaskLogConstants.LOG_AR_TARGETS, LOG_AUDIT, targetsDisplay.toString(), null, COMPLIANCE_POLICY);

				if (targetsDisplay == null || targetsDisplay.size() < 1) {
					session.removeAttribute(SESSION_MULTITGBOOL);
					session.removeAttribute(MAIN_PAGE_TARGET);
					session.removeAttribute(MAIN_PAGE_M_TARGETS);
					guiException = new GUIException(new CriticalException(AR_TARGETCHANNEL_POLICY));
					workLog.write(getString(locale, "ar.worklog.notarget"));
					workLog.close(hm, IARConstants.TMS_RETURN_CODE_ERROR, getString(locale, "ar.worklog.policyfailed"));
                    arLogMgr.log(ARTaskLogConstants.LOG_AR_TARGETS_NOT_FOUND, LOG_MAJOR, "Target Not Present", guiException);
					forward = mapping.findForward("failure");
					return;
				} else if (targetsDisplay.size() == 1) {
					// Remove from previous
					session.removeAttribute(SESSION_MULTITGBOOL);
					session.removeAttribute(MAIN_PAGE_M_TARGETS);
					session.setAttribute("context", "targetDetailsAdd");
					session.setAttribute(MAIN_PAGE_TARGET, targetsDisplay);
				} else {
					// multiple targets
					session.removeAttribute(MAIN_PAGE_TARGET);
					session.setAttribute(SESSION_MULTITGBOOL, "true");
					session.setAttribute("context", "targetDetailsAddMulti");
					session.setAttribute(MAIN_PAGE_M_TARGETS, targetsDisplay);
				}

				List chns = arRequestProcessor.getChannelList();
				if (chns.size() < 1) {
					guiException = new GUIException(new CriticalException(AR_TARGETCHANNEL_POLICY));
					workLog.write(getString(locale, "ar.worklog.nochannel"));
					workLog.close(hm, IARConstants.TMS_RETURN_CODE_ERROR, getString(locale, "ar.worklog.policyfailed"));
                    arLogMgr.log(ARTaskLogConstants.LOG_AR_CHANNELS_NOT_FOUND, LOG_MAJOR, "Channel Not Present", guiException);
					forward = mapping.findForward("failure");
					return;
				} else {
					session.setAttribute(IARTaskConstants.AR_CHANNELS, chns);
                    arLogMgr.log(ARTaskLogConstants.LOG_AR_CHANNELS, LOG_AUDIT, chns.toString(), null, COMPLIANCE_POLICY);
				}

			} catch (ARManagerException err) {
				session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
				session.removeAttribute(MAIN_PAGE_M_TARGETS);
				session.removeAttribute(MAIN_PAGE_TARGET);
				guiException = new GUIException(AR_CONNECTION_ERROR, err);
				forward = mapping.findForward("failure");
				err.printStackTrace();

			} catch (CriticalException cerr) {
				session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
				session.removeAttribute(MAIN_PAGE_M_TARGETS);
				session.removeAttribute(MAIN_PAGE_TARGET);
				guiException = new GUIException(cerr);
				forward = mapping.findForward("failure");
				cerr.printStackTrace();

			} catch (SystemException err) {
				session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
				session.removeAttribute(MAIN_PAGE_M_TARGETS);
				session.removeAttribute(MAIN_PAGE_TARGET);
				guiException = new GUIException(new CriticalException(err,err.getKey()));
				forward = mapping.findForward("failure");
				err.printStackTrace();

			}
		}

        public boolean hasPolicyWritePermission(Target tgt, IUser user) throws SystemException{
            return ObjectManager.checkSubPerm( user, tgt.getId(), tgt.getType(), IAclConstants.WRITE_ACTION );
        }

	}
}


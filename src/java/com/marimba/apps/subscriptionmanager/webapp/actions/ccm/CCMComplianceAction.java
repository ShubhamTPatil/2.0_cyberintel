// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions.ccm;

import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.arsystem.*;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.intf.msf.arsys.ARManagerException;
import com.marimba.intf.msf.arsys.IARConstants;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;


/**
 * This class handles displaying the target compliance information which is invoked from AR.
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 11/23/2005
 */

public final class CCMComplianceAction extends AbstractAction {
    protected void init(HttpServletRequest request) {
        super.init(request);
    }

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return new CCMComplianceAction.CCMTargetComplianceTask(mapping, form, request, response);
    }

    protected class CCMTargetComplianceTask extends SubscriptionDelayedTask {
        CCMTargetComplianceTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        super(mapping, form, request, response);
    }


    public void execute() {
        forward = null;
        String returnString = "failure";
        CCMComplianceAction.this.init(request);
        //init app and user if this page is called directly
        initApp(request);
        initUser(session, main);

        returnString = doQueryfromAR();
        if (forward == null){
            forward = mapping.findForward(returnString);
        }
    }
    public String doQueryfromAR() {
        String viewType = null;
        ARLogMgr arLogMgr = null;
        try {
            clearSessionVars(session);
            IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
            session.setAttribute(IARTaskConstants.AR_TASK_ID, "-1");
            ARRequestProcessor arRequestProcessor = new ARRequestProcessor(main, request.getParameterMap(), user, null);
            arLogMgr = arRequestProcessor.getARObjectSource().getLogManager();
            List targets = arRequestProcessor.getTargets();
            ArrayList displayTargets = new ArrayList(DEF_COLL_SIZE);
            ArrayList targetsList = new ArrayList(DEF_COLL_SIZE);

            arLogMgr.log(ARTaskLogConstants.LOG_AR_TASK_ID, LOG_AUDIT, arRequestProcessor.getTaskID(), null, COMPLIANCE_TARGET);
            arLogMgr.log(ARTaskLogConstants.LOG_AR_CHANGE_ID, LOG_AUDIT, arRequestProcessor.getChangeID(), null, COMPLIANCE_TARGET);

            Iterator targetIter = targets.iterator();
            while(targetIter.hasNext()) {
                Target tgt = (Target)targetIter.next();
                PropsBean pbean = targetToPropsBean(tgt);
                displayTargets.add(pbean);
                Target tg = new Target(tgt.getName(), (String)pbean.getValue("type"), tgt.getId());
                targetsList.add(tg);
            }
            arLogMgr.log(ARTaskLogConstants.LOG_AR_TARGETS, LOG_AUDIT, targetsList.toString(), null, COMPLIANCE_TARGET);
            List channels = arRequestProcessor.getChannelList();
            ArrayList displayChannels = new ArrayList(DEF_COLL_SIZE);

            Iterator chIter = channels.iterator();
            while(chIter.hasNext())  {
                Channel ch = (Channel) chIter.next();
                PropsBean pbean = channelToPropsBean(ch);
                displayChannels.add(pbean);
            }
            arLogMgr.log(ARTaskLogConstants.LOG_AR_CHANNELS, LOG_AUDIT, displayChannels.toString(), null, COMPLIANCE_TARGET);
            session.setAttribute(IARTaskConstants.AR_TASK_ID, arRequestProcessor.getTaskID());
            session.setAttribute(IARTaskConstants.AR_CHANGE_ID, arRequestProcessor.getChangeID());
            session.setAttribute(IARTaskConstants.AR_COMPLIANCE_TARGET_DISPLAY, displayTargets);
            session.setAttribute(IARTaskConstants.AR_COMPLIANCE_CHANNEL_DISPLAY, displayChannels);

            HashMap hm = new HashMap();
            hm.put(IARTaskConstants.AR_TASK_ID, new String[] {arRequestProcessor.getTaskID()});
            hm.put(IARTaskConstants.AR_USER, getString(locale, "ar.worklog.modifieduser")+user.getName());
            ARWorklog workLog = new ARWorklog(hm, tenant);

            if ( (displayTargets == null || displayTargets.size() == 0) &&
                    (displayChannels == null || displayChannels.size() == 0) ) {
                session.removeAttribute(SESSION_MULTITGBOOL);
                session.removeAttribute(MAIN_PAGE_TARGET);
                session.removeAttribute(MAIN_PAGE_M_TARGETS);
                guiException = new GUIException(new CriticalException(AR_TARGETCHANNEL_NOTPRESENT));
                arLogMgr.log(ARTaskLogConstants.LOG_AR_TARGET_CHANNELS_NOT_FOUND, LOG_MAJOR, "Target/Channel Not Present", guiException);
                workLog.write(getString(locale, "ar.worklog.notargetchannel"));
                workLog.close(hm, IARConstants.TMS_RETURN_CODE_ERROR, getString(locale, "ar.worklog.policyfailed"));
                forward = mapping.findForward("failure");
                return null;
            } else if (targetsList.size() == 1) {
                // Remove from previous
                session.removeAttribute(SESSION_MULTITGBOOL);
                session.removeAttribute(MAIN_PAGE_M_TARGETS);
                session.setAttribute("context", "targetDetailsAdd");
                session.setAttribute(MAIN_PAGE_TARGET, targetsList);
            } else {
                // multiple targets
                session.removeAttribute(MAIN_PAGE_TARGET);
                session.setAttribute(SESSION_MULTITGBOOL, "true");
                session.setAttribute("context", "targetDetailsAddMulti");
                session.setAttribute(MAIN_PAGE_M_TARGETS, targetsList);
            }

            if( (displayTargets != null) && (displayTargets.size() > 0) && (displayChannels != null) && (displayChannels.size() > 0) ) {
                viewType = "bothview";
            } else if(displayTargets != null && displayTargets.size() > 0) {
                viewType = "targetview";
            } else if(displayChannels != null && displayChannels.size() > 0) {
                viewType = "packageview";
            }
            session.setAttribute(IARTaskConstants.AR_VIEW_TYPE, viewType);
            return viewType;

        } catch (ARManagerException err) {
            session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
            session.removeAttribute(MAIN_PAGE_M_TARGETS);
            session.removeAttribute(MAIN_PAGE_TARGET);
            guiException = new GUIException(new CriticalException("ARManagerException"));
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
        return null;
    }

        /**
         * Convert a Target object to a PropsBean object to display in GUI
         * @param target
         * @return PropsBean
         */
        private PropsBean targetToPropsBean(Target target) {
            String allEndpoints = null;
            allEndpoints = resources.getMessage(Locale.getDefault(), "page.global.All");
            PropsBean bean = new PropsBean();
            bean.setValue(GUIConstants.TYPE, target.getType());
            String type = target.getType();
            try {
                type = LDAPUtils.objClassToTargetType(type, LDAPVarsMap);
            } catch(SubInternalException sie) {
                //ignore the exception if the target type is already resolved
            }
            bean.setValue("type",type);
            bean.setValue(GUIConstants.DISPLAYNAME, TYPE_ALL.equals(target.getType())?allEndpoints:target.getName());
            bean.setValue("dn", target.getId());
            return bean;
        }

        /**
         * Convert a channel object to a PropsBean object to display in GUI
         * @param channel
         * @return PropsBean
         */
        private PropsBean channelToPropsBean(Channel channel) {
            PropsBean bean = new PropsBean();
            bean.setValue("url", channel.getUrl());
            bean.setValue("type", channel.getType());
            bean.setValue("title", channel.getTitle());
            return bean;
        }

        private void clearSessionVars(HttpSession session) {
            session.removeAttribute(IARTaskConstants.AR_COMPLIANCE_TARGET_DISPLAY);
            session.removeAttribute(IARTaskConstants.AR_COMPLIANCE_CHANNEL_DISPLAY);
            session.removeAttribute(IARTaskConstants.AR_VIEW_TYPE);
            session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
            session.removeAttribute(MAIN_PAGE_M_TARGETS);
            session.removeAttribute(MAIN_PAGE_TARGET);
            session.removeAttribute(IARTaskConstants.AR_TASK_ID);
            session.removeAttribute(IARTaskConstants.AR_CHANGE_ID);
            session.removeAttribute("target_compliance_ar");
            session.removeAttribute("package_compliance_ar");
            session.removeAttribute("target");
        }

    }
}

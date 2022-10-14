// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.webapp.actions.ccm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.*;
import java.util.*;

import com.marimba.apps.subscriptionmanager.arsystem.*;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.*;
import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.intf.msf.arsys.*;
import com.marimba.webapps.intf.GUIException;
import com.marimba.intf.msf.*;
import com.marimba.webapps.intf.*;

/**
 * @author $Author
 * @version $version
 */

public final class CCMVerifyAction extends AbstractAction {
    VerificationTaskService taskService = null;

	protected void init(HttpServletRequest request) {
		super.init(request);
        taskService = (VerificationTaskService) tenant.getManager(IARTaskConstants.VERIFY_TASK_SERVICE);
	}

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {

        return new VerifyTask(mapping, form, request, response);
    }

    protected class VerifyTask extends SubscriptionDelayedTask {
        VerifyTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
        }


    public void execute() {
        CCMVerifyAction.this.init(request);
        //init app and user if called directly
        initApp(request);
        initUser(session, main);
        String returnCode = "true";
        ARLogMgr arLogMgr = null;
        try {
            clearSessionVars(session);
            session.setAttribute(IARTaskConstants.AR_TASK_ID, "-1");
            ARRequestProcessor arRequestProcessor = taskService.handleRequest(request);
            ARObjectSource arObjSource = arRequestProcessor.getARObjectSource();
            arLogMgr = arObjSource.getLogManager();
            arLogMgr.log(ARTaskLogConstants.LOG_AR_TASK_ID, LOG_AUDIT, arRequestProcessor.getTaskID(), null, COMPLIANCE_VERIFY);
            arLogMgr.log(ARTaskLogConstants.LOG_AR_CHANGE_ID, LOG_AUDIT, arRequestProcessor.getChangeID(), null, COMPLIANCE_VERIFY);
            List targets = arRequestProcessor.getTargets();
            List channels = arRequestProcessor.getChannelList();
            List targetsDisplay = setTargetType(targets);
            arLogMgr.log(ARTaskLogConstants.LOG_AR_TARGETS, LOG_AUDIT, targetsDisplay.toString(), null, COMPLIANCE_VERIFY);
            arLogMgr.log(ARTaskLogConstants.LOG_AR_CHANNELS, LOG_AUDIT, channels.toString(), null, COMPLIANCE_VERIFY);
            returnCode = arObjSource.doVerifyTaskAction();
            session.setAttribute(IARTaskConstants.AR_COMP_PERCENTAGE, arObjSource.getCompliancePercentage());
            session.setAttribute(IARTaskConstants.AR_EXPIRY_TIME, arObjSource.getTimeoutWindow());
            session.setAttribute(IARTaskConstants.AR_SCHEDULE, arObjSource.getSchedule());
            if( (targetsDisplay !=null) && (targetsDisplay.size() > 0) ) {
                session.setAttribute(IARTaskConstants.AR_TARGETS_DISPLAY, targetsDisplay);
            }
            if( (channels !=null) && (channels.size() > 0) ) {
                session.setAttribute(IARTaskConstants.AR_CHANNELS_DISPLAY, channels);
            }
            session.setAttribute(IARTaskConstants.AR_TASK_ID, arRequestProcessor.getTaskID());
            session.setAttribute(IARTaskConstants.RETURN_CODE, returnCode);
            if("true".equals(returnCode)) {
                arLogMgr.log(ARTaskLogConstants.LOG_AR_VERIFYTASK_CREATE_OK, LOG_AUDIT, returnCode, null, COMPLIANCE_VERIFY);
            } else {
                arLogMgr.log(ARTaskLogConstants.LOG_AR_VERIFYTASK_CREATE_ERROR, LOG_MAJOR, returnCode);
            }

        } catch (ARManagerException err) {
            guiException = new GUIException(AR_CONNECTION_ERROR, err);
            forward = mapping.findForward("failure");
            err.printStackTrace();
        } catch (CriticalException cerr) {
            guiException = new GUIException(cerr);
            forward = mapping.findForward("failure");
            cerr.printStackTrace();
        } catch (SystemException err) {
            guiException = new GUIException(new CriticalException(err,err.getKey()));
            forward = mapping.findForward("failure");
            err.printStackTrace();
        }
        forward =  mapping.findForward("info");
    }

    private List setTargetType(List targets) {
        List targetsDisplay = new ArrayList(DEF_COLL_SIZE);
        Iterator targetsIter = targets.iterator();
        String type = null;
        while(targetsIter.hasNext()) {
            Target target = (Target)targetsIter.next();
            try {
                type = LDAPUtils.objClassToTargetType(target.getType(),LDAPVarsMap);
            }catch(SubInternalException sie) {
                //ignore the exception if the target type is resolved.
            }
            Target displayTarget = new Target(target.getName(), type, target.getID());
            targetsDisplay.add(displayTarget);
        }
        return targetsDisplay;
    }

    private void clearSessionVars(HttpSession session) {
        session.removeAttribute(IARTaskConstants.AR_COMP_PERCENTAGE);
        session.removeAttribute(IARTaskConstants.AR_EXPIRY_TIME);
        session.removeAttribute(IARTaskConstants.AR_SCHEDULE);
        session.removeAttribute(IARTaskConstants.AR_TARGETS_DISPLAY);
        session.removeAttribute(IARTaskConstants.AR_CHANNELS_DISPLAY);
        session.removeAttribute(IARTaskConstants.RETURN_CODE);
        session.removeAttribute(IARTaskConstants.AR_TASK_ID);
    }
    }
}


// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffLogFormatter;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsForm;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.WebAppUtils;

import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.ADD_OPERATION;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.MODIFY_OPERATION;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.POLICY_PENDING;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_NEW_SUB;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_OLD_SUB;

/**
 * This action is used from the delete preview page when user confirms the deletion of selected targets from a single select mode.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */
public final class PackageDeleteSaveAction
    extends AbstractAction {
    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws IOException REMIND
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException, 
                   ServletException {
        init(request);

        if (!(form instanceof IMapProperty)) {
            InternalException ie = new InternalException(GUIUTILS_INTERNAL_WRONGARG, "DistDeleteAction", form.toString());

            throw new GUIException(DIST_DELETE_ERROR, ie);
        }
        IMapProperty formbean = (IMapProperty) form;
        HttpSession   session = request.getSession();

        String           channelSessionVar = MAIN_PAGE_PACKAGE;

        if (session.getAttribute(SESSION_MULTIPKGBOOL) != null) {
            channelSessionVar = MAIN_PAGE_M_PKGS;
        }

        ArrayList channels = new ArrayList(DEF_COLL_SIZE);

        channels = (ArrayList) session.getAttribute(channelSessionVar);

        // Get the targets and packages which are selected
        List targets = (List) session.getAttribute(SESSION_TGS_TODELETE);

        // Get the session variable for select all.
        String value = (String)session.getAttribute(SESSION_ALL);

        // Get the value of the radio button in which user choose
        // whether to delete the policy entirely to only the packages.
        Map<String, Map<String, ISubscription>> subsMap = new LinkedHashMap<String, Map<String, ISubscription>>(10);
        Vector           deleteSubTargets = new Vector(5);

        try {
            for (int i = 0; i < targets.size(); i++) {
                Target        tgt = (Target) targets.get(i);
                Map<String, ISubscription> subMap = new HashMap<String, ISubscription>(2);
                ISubscription sub = ObjectManager.getSubscription(tgt.getId(), tgt.getType(), GUIUtils.getUser(request));
                Subscription oldSub = new Subscription((Subscription) sub);
                main.logAuditInfo(LOG_AUDIT_SUB_OBJ_CREATED, LOG_AUDIT, "Policy Manager", tgt.getID(), request, DEL_TARGETS);

                if(DEBUG) {
                    System.out.println("PackageDeleteSaveAction: logging which channels were deleted");
                }
                for (int j = 0; j < channels.size(); j++) {
                    Channel selectedChn = (Channel) channels.get(j);
                    //Channel subch = sub.getChannel(selectedChn.getUrl());
                    sub.removeChannel(selectedChn.getUrl());
                    GUIUtils.log(servlet, request, LOG_SUBSCRIPTION_PKGDELETE, selectedChn.getUrl(), tgt.toString());
                }
                main.logAuditInfo(LOG_AUDIT_PACKAGEVIEW_DELETE, LOG_AUDIT, "Policy Manager", tgt.getId(), request, DEL_TARGETS);
                // check if this policy is empty (no channels,
                // properties, or blackout schedule)
                // add it to the list of subscriptions to be saved
                subMap.put(KEY_NEW_SUB, sub);
                subMap.put(KEY_OLD_SUB, oldSub);
                subsMap.put(tgt.getId(), subMap);
            }

            // The subscription are saved outside of the loop because
            // an error could occur during the save of one of them and
            // we do not want to save any of them if there is a problem
            transactSave(request, subsMap, deleteSubTargets, targets);


        } catch (Exception e) {
            throw new GUIException(DIST_DELETE_ERROR, e);
        } finally {
            session.removeAttribute(SESSION_PKGS_TODELETE);
            session.removeAttribute(SESSION_DISPLAY_DELETE_PROPS);
            if("true".equals(value)) {
                session.removeAttribute(MAIN_PAGE_PACKAGE);
                session.removeAttribute(MAIN_PAGE_M_PKGS);
            }
            session.removeAttribute(SESSION_ALL);
            session.removeAttribute((String)formbean.getValue(SESSION_PERSIST_SELECTED));
            session.removeAttribute(SESSION_PERSIST_RESETRESULTS);
            ((PackageDetailsForm) form).clearCheckedItems();
        }
        return (mapping.findForward("success"));
    }

    protected void transactSave(HttpServletRequest request,
                                Map<String, Map<String, ISubscription>> modifySubs,
                                Vector             deleteSubTargets,
                                List               targets)
            throws SystemException {

        String isMultiTgt = (String)request.getSession().getAttribute(SESSION_MULTITGBOOL);
        String        targetList = "";
        String modifiedUserDN = null;
        boolean isPeerPolicyEnabled = main.isPeerApprovalEnabled();
        IUser user = GUIUtils.getUser(request);
        try {
            modifiedUserDN = main.resolveUserDN(user.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }

        List<PolicyDiff> policyDiffs = new ArrayList<PolicyDiff>();

        for (String targetId : modifySubs.keySet()) {
            Map<String, ISubscription> tmpMap = modifySubs.get(targetId);
            ISubscription sub = tmpMap.get(KEY_NEW_SUB);
            ISubscription oldSub = tmpMap.get(KEY_OLD_SUB);
            if (!isPeerPolicyEnabled) {
                sub.save();
                int size = deleteSubTargets.size();
                for (int i = 0; i < size; i++) {
                    Target tgt = (Target) deleteSubTargets.get(i);
                    ObjectManager.deleteSubscription(tgt.getId(), tgt.getType(), GUIUtils.getUser(request));
                }
            }
            targetList = "(" + sub.getTargetID() + ")" + targetList;

            if(main.isPeerApprovalEnabled()) {
                PolicyDiffLogFormatter logDiff = new PolicyDiffLogFormatter(oldSub, sub, main);
                logDiff.prepareDiff();

                PolicyDiff policyDiff = new PolicyDiff(oldSub, sub, main);
                boolean hasPolicyExists = ObjectManager.existsSubscription(oldSub.getTargetID(), oldSub.getTargetType(), user);
                if(hasPolicyExists) {
                    if (DEBUG) System.out.println("Old subscription is already exists");
                    policyDiff.setPolicyAction(MODIFY_OPERATION);
                } else {
                    policyDiff.setPolicyAction(ADD_OPERATION);
                }
                if(null != modifiedUserDN) {
                    policyDiff.setUser(modifiedUserDN);
                }
                // set default policy status as pending
                policyDiff.setPolicyStatus(POLICY_PENDING);
                if(policyDiff.hasDiff()) {
                    policyDiffs.add(policyDiff);
                }
            }
        }
        if(main.isPeerApprovalEnabled()) {
            if(policyDiffs.size() > 0) {
                main.storeApprovalPolicy2DB(policyDiffs);
                main.setPolicyChange(true);
            } else {
                main.setPolicyChange(false);
            }
        }
        if(main.isEmailFeatureEnabled() && policyDiffs.size() > 0) {
            PolicyDiffMailFormatter mailFormatter = new PolicyDiffMailFormatter(policyDiffs, resources);
            String userName = main.getDisplayName(modifiedUserDN);
            mailFormatter.setCreatedByDispName((null == userName || userName.trim().isEmpty()) ? user.getName() : userName);
            mailFormatter.setMultiMode("true".equalsIgnoreCase(isMultiTgt));
            mailFormatter.setPeerApprovalEnabled(main.isPeerApprovalEnabled());
            mailFormatter.prepare();
            main.sendMail(mailFormatter);
        }
    }
}

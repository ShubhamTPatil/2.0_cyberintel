// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import static com.marimba.apps.subscriptionmanager.approval.IApprovalPolicyConstants.*;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_NEW_SUB;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.KEY_OLD_SUB;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiff;
import com.marimba.apps.subscriptionmanager.policydiff.PolicyDiffMailFormatter;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.*;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * This action is used from the delete preview page when user confirms the deletion of selected packages from a single select mode.  It will provides an option
 * to the user whether to delete the entire subscription when all packages in the target are selected.
 *
 * @author Theen-Theen Tan
 * @version 1.12, 09/03/2002
 */
public final class DistDeleteSaveAction extends AbstractAction {

    private IUser currentUser;
    private boolean isMultiMode;
    private StringBuffer targetIds;
    private StringBuffer targetNames;

    private Map<String, Map<String, ISubscription>> subObjMap;

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
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!(form instanceof IMapProperty)) {
            InternalException ie = new InternalException(GUIUTILS_INTERNAL_WRONGARG, "DistDeleteAction", form.toString());

            throw new GUIException(DIST_DELETE_ERROR, ie);
        }
        super.init(request);
        HttpSession session = request.getSession();
        this.isMultiMode = "true".equalsIgnoreCase((String) session.getAttribute(SESSION_MULTITGBOOL));
        this.targetIds = new StringBuffer(50);
        this.targetNames = new StringBuffer(50);

        String all = request.getParameter("all");

        // Get the targets and packages which are selected
        List targets = (List) session.getAttribute(SESSION_TGS_TODELETE);
        this.subObjMap = new HashMap<String, Map<String, ISubscription>>(targets.size());
        // Get the value of the radio button in which user choose
        // whether to delete the policy entirely to only the packages.
        boolean selectAll = "true".equals(all);
        List<ISubscription> modifySubs = new Vector<ISubscription>(5);
        List<Target> deleteSubTargets = new Vector<Target>(5);

        //main is needed for logging purposes
        try {
            this.currentUser = GUIUtils.getUser(request);
            // Check that the policy exists in storage
            for (Object target : targets) {
                Target tgt = (Target) target;
                if (!ObjectManager.existsSubscription(tgt.getId(), tgt.getType(), currentUser)) {
                    throw new SubKnownException(DIST_DELETE_NOSUB, tgt.getName());
                }
            }

            if (selectAll) {
                for (Object target : targets) {
                    Target tgt = (Target) target;
                    ISubscription sub = ObjectManager.getSubscription(tgt.getId(), tgt.getType(), currentUser);
                    removeWindowsProfiles(sub);
                    removeNonWindowsProfiles(sub);
                    removeCustomProfiles(sub);
                    sub.save();
                    main.logAuditInfo(LOG_AUDIT_DELETE_SUBSCRIPTION, LOG_AUDIT, "vDesk", tgt.getId(), request, SUB_DELETE);
                }
            }
            session.removeAttribute(SESSION_PKGS_TODELETE);
            session.removeAttribute(SESSION_DISPLAY_DELETE_PROPS);
        } catch (Exception ex) {
            if(ex instanceof GUIException) {
                throw new GUIException(new CriticalException(((GUIException)ex).getMessageKey(),ex.toString()));
            } else if(ex instanceof SystemException) {
                throw new GUIException(new CriticalException(((SystemException)ex).getKey(),ex.toString()));
            } else {
                throw new GUIException(DIST_DELETE_ERROR,ex);
            }
        }

        return (mapping.findForward("success"));
    }
    private void removeWindowsProfiles(ISubscription sub) {
    	String type = "windows";
        setValue(sub, USGCB_SECURITY_OPTION, null, type);
        setValue(sub, USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID, null, type);
        setValue(sub, USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, type);
        setValue(sub, USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, type);
        setValue(sub, USGCB_SECURITY_TEMPLATE_NAME, null, type);
        setValue(sub, USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID, null, type);
        setValue(sub, USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, type);
        setValue(sub, USGCB_SECURITY_SCAP_REMEDIATION_ENABLED, null, type);
    }
    private void removeNonWindowsProfiles(ISubscription sub) {
    	String type = "nonwindows";
        setValue(sub, SCAP_SECURITY_OPTION, null, type);
        setValue(sub, SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID, null, type);
        setValue(sub, SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, type);
        setValue(sub, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, type);
        setValue(sub, SCAP_SECURITY_TEMPLATE_NAME, null, type);
        setValue(sub, SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID, null, type);
        setValue(sub, SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, type);
        setValue(sub, SCAP_SECURITY_SCAP_REMEDIATION_ENABLED, null, type);
    }
    private void removeCustomProfiles(ISubscription sub) {
    	String type = "custom";
        setValue(sub, CUSTOM_SECURITY_OPTION, null, type);
        setValue(sub, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID, null, type);
        setValue(sub, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME, null, type);
        setValue(sub, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE, null, type);
        setValue(sub, CUSTOM_SECURITY_TEMPLATE_NAME, null, type);
        setValue(sub, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID, null, type);
        setValue(sub, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE, null, type);
        setValue(sub, CUSTOM_SECURITY_SCAP_REMEDIATION_ENABLED, null, type);
    }
    private void setValue(ISubscription sub, String key, String value, String type) {
    	try {
	        if ("windows".equals(type)) {
	            sub.setProperty(PROP_USGCB_SECURITY_KEYWORD, key, value);
	        }
	        if ("nonwindows".equals(type)) {
	            sub.setProperty(PROP_SCAP_SECURITY_KEYWORD, key, value);
	        }
	        if ("custom".equals(type)) {
	            sub.setProperty(PROP_CUSTOM_SECURITY_KEYWORD, key, value);
	        }
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
    }
    /* REMIND: this should do a transactional save.  Meaning, if there is a problem
     * with one of the subscriptions for the target, save out the old ones for the previous
     */
    protected void transactSave(HttpServletRequest request, List<ISubscription> modifySubs,
                                List<Target> deleteSubTargets, List targets) throws SystemException {
        String targetList = "";
        boolean isEnabledPeerPolicy = main.isPeerApprovalEnabled();
        for (ISubscription sub : modifySubs) {
            if(!isEnabledPeerPolicy) {
                sub.save();
            }
            addNewSub(sub.getTargetID(), sub);
            targetList = "(" + sub.getTargetID() + ")" + targetList;
        }
        for (Target tgt : deleteSubTargets) {
            if(!isEnabledPeerPolicy) {
                ObjectManager.deleteSubscription(tgt.getId(), tgt.getType(), currentUser);
            }
            addNewSub(tgt.getId(), null);
            GUIUtils.log(servlet, request, LOG_DELETE_SUBSCRIPTION, tgt.getId());
        }
        for(Object target : targets) {
            main.updatePendingPolicySessionVar(request, (Target)target);
        }
    }

    private void addOldSub(String targetId, ISubscription oldSub) {
        targetId = targetId.toUpperCase();
        Map<String, ISubscription> localMap;
        if (null != subObjMap.get(targetId)) {
            localMap = subObjMap.get(targetId);
            localMap.put(KEY_OLD_SUB, oldSub);
        } else {
            localMap = new HashMap<String, ISubscription>(2);
            localMap.put(KEY_OLD_SUB, oldSub);
        }

        subObjMap.put(targetId, localMap);
    }

    private void addNewSub(String targetId, ISubscription newSub) {
        targetId = targetId.toUpperCase();
        Map<String, ISubscription> localMap;
        if (null != subObjMap.get(targetId)) {
            localMap = subObjMap.get(targetId);
            localMap.put(KEY_NEW_SUB, newSub);
        } else {
            localMap = new HashMap<String, ISubscription>(2);
            localMap.put(KEY_NEW_SUB, newSub);
        }

        subObjMap.put(targetId, localMap);
    }

    private void sendMail() {

        if (subObjMap.isEmpty()) {
            System.out.println("Failed to send mail, Policy diff object is empty");
            return;
        }

        List<PolicyDiff> policyDiffs = getPolicyDiff();

        debug("Subscription objects map to find diff : " + subObjMap);
        PolicyDiffMailFormatter mailFormatter = new PolicyDiffMailFormatter(policyDiffs, resources);

        String userName = null;
        try {
            userName = main.getDisplayName(main.resolveUserDN(currentUser.getName()));
        } catch (SystemException e) {
            if (DEBUG) e.printStackTrace();
        }

        String tmp_id = targetIds.toString();
        String tmp_name = targetNames.toString();
        mailFormatter.setMultiMode(isMultiMode);
        mailFormatter.setTargetId((tmp_id.lastIndexOf(",") != -1) ? tmp_id.substring(0, tmp_id.length() - 1) : tmp_id);
        mailFormatter.setTargetName((tmp_name.lastIndexOf(",") != -1) ? tmp_name.substring(0, tmp_name.length() - 1) : tmp_name);
        mailFormatter.setCreatedByDispName((null == userName || userName.trim().isEmpty()) ? currentUser.getName() : userName);
        mailFormatter.setPeerApprovalEnabled(main.isPeerApprovalEnabled());
        mailFormatter.prepare();

        main.sendMail(mailFormatter);
    }

    private void storeDeletedPolicy2DB() {
        if (!subObjMap.isEmpty()) {
            List<PolicyDiff> policyDiffs = getPolicyDiff();
            debug("Policy diff's size : " + policyDiffs.size());
            if(policyDiffs.size() > 0) {
                main.storeApprovalPolicy2DB(policyDiffs);
            } else {
                System.out.println("DistDeleteSaveAction : Failed to store policy diff in database for approval");
            }
        }
    }

    private List<PolicyDiff> getPolicyDiff() {
        List<PolicyDiff> policyDiffs = new ArrayList<PolicyDiff>(subObjMap.size());
        String modifiedUserDN = null;
        try {
            modifiedUserDN = main.resolveUserDN(currentUser.getName());
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
        if(null == modifiedUserDN) {
            return policyDiffs;
        }
        debug("Subscription object size : " + subObjMap.size());
        for (String key : subObjMap.keySet()) {
            Map<String, ISubscription> subMap = subObjMap.get(key);
            if (null != subMap && !subMap.isEmpty()) {
                ISubscription oldSub = subMap.get(KEY_OLD_SUB);
                ISubscription newSub = subMap.get(KEY_NEW_SUB);
                PolicyDiff subDiff = new PolicyDiff(oldSub, newSub, main);
                if(null != newSub) {
                    subDiff.setPolicyAction(MODIFY_OPERATION);
                } else {
                    subDiff.setPolicyAction(DELETE_OPERATION);
                }
                subDiff.setPolicyStatus(POLICY_PENDING);
                subDiff.setUser(modifiedUserDN);
                policyDiffs.add(subDiff);
            }
        }
        return policyDiffs;
    }

    private void debug(String msg) {
        if (DEBUG) System.out.println("DistDeleteSaveAction: " + msg);
    }
}

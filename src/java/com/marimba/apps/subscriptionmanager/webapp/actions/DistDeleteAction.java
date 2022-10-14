// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsMultiForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPPolicyHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;

import com.marimba.webapps.intf.*;

/**
 * This action is used from single select mode to delete packages that have been selected.  Additionally, it will set a session variable which determines if
 * all of the packages were selected.  This is needed for the preview page.
 *
 * @author Angela Saval
 * @version 1.19, 09/09/2002
 */
public class DistDeleteAction
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
    	try {
    		init(request);
    	} catch(Exception ed) {
    		
    	}
        /*Add in the targets from the session variable used from the target schedule details.
          We must first determine which set of targets we are dealing with. This is done\
          by looking at the boolean which is set when the user is in multiple select mode
        */
        HttpSession session = request.getSession();

        List   targets = getSelectedTargets(session);
        boolean     hasMultiTargets = false;

        if ((targets != null) && (targets.size() > 1)) {
            hasMultiTargets = true;
        }
        if(null == targets) {
        	String targetId = (String)session.getAttribute(SELECTED_TARGET_ID);
        	String targetName = (String) session.getAttribute(SELECTED_TARGET_NAME);
        	String targetType = (String) session.getAttribute(SELECTED_TARGET_TYPE);
            if(null != targetId && null != targetName && null != targetType) {
	    		Target availableTarget = new Target(targetName, targetType, targetId);
	    		targets = new ArrayList(DEF_COLL_SIZE);
	    		targets.add(availableTarget);
	    		session.setAttribute(MAIN_PAGE_TARGET, targets);
            }
        }
        session.setAttribute(SESSION_TGS_TODELETE, targets);

        try {
            synchronized (targets) {
                for (int i = 0; i < targets.size(); i++) {
                    Target tgt = (Target) targets.get(i);
                    LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(request),main.getSubBase(), main.getUsersInLDAP(), main.getDirType());
                    policyFinder.addTarget(tgt.getId(),tgt.getType());
                    boolean hasPolicy = false;
                    if(ISubscriptionConstants.TYPE_ALL.equals(tgt.getType()) ||
                        LDAPVarsMap.get("TARGET_ALL").equals(tgt.getType())) {
                        hasPolicy = policyFinder.hasPolicies("all");
                    } else {
                        hasPolicy = policyFinder.hasPolicies(tgt.getId());
                    }
                    if (!hasPolicy) {
                        //throw new SubKnownException(DIST_DELETE_NOSUB, tgt.getName());
                    	System.out.println("There is no policy has assigned for the target");
                    	return (mapping.findForward("failure"));
                    }
                }
            }

                if (!hasMultiTargets) {
                    // figure out if there are any properties that might need
                    // to be deleted.
                    Target        tgt = (Target) targets.get(0);
                    ISubscription sub = ObjectManager.getSubscription(tgt.getId(), tgt.getType(), GUIUtils.getUser(request));

                    if (DEBUG) {
                        System.out.println("sub.hasProperties()= " + sub.hasProperties());

                    }

                    // Display the option to "delete all properties"
                    // (tuner properties, package properties, blackout schedule)
                    // if all packages in the subscription were selected and
                    // *only if there exist properties to delete*
                    // If there is no properties in the subscription, we will
                    // automatically delete the entire subscription if all packages
                    // are being deleted (see DistDeleteSaveAction)
                    if (sub.hasProperties()) {
                        if (DEBUG) {
                            System.out.println("set DISPLAY_DELETE_PROPS to true");
                        }

                        session.setAttribute(SESSION_DISPLAY_DELETE_PROPS, "true");
                    } else {
                        if (DEBUG) {
                            System.out.println("set DISPLAY_DELETE_PROPS to false");
                        }

                        session.setAttribute(SESSION_DISPLAY_DELETE_PROPS, "false");
                    }
                } else {
                    // for multi-select mode, do not display delete props option
                    session.setAttribute(SESSION_DISPLAY_DELETE_PROPS, "false");
                }
        } catch (SystemException se) {
            throw new GUIException(se);
        } finally {
        }

        return (mapping.findForward("success"));
    }

    /**
     * Gets the packages which delete from the subscription
     *
     * @param session REMIND
     * @param formbean REMIND
     *
     * @return REMIND
     */
    protected ArrayList getPackages(HttpSession   session,
                                    IMapProperty formbean) {
        ArrayList pkgSel = (ArrayList) session.getAttribute(SESSION_PKGS_FROMTGS_RS);

        if (pkgSel == null) {
            return null;
        }

	ArrayList pkgs = new ArrayList(pkgSel.size());
        TargetChannelMap tempmap;

        for (Iterator ite = pkgSel.iterator(); ite.hasNext();) {
            tempmap = (TargetChannelMap) ite.next();
            pkgs.add(tempmap.getChannel());

            if (DEBUG) {
                System.out.println("DistDeleteAction: pkg to delete = " + tempmap.getChannel().getUrl());
            }
        }

        return pkgs;
    }

    /**
     * From all the packages that is listed, obtain the total which are directly assigned to the targets.
     *
     * @param session REMIND
     * @param pkgs REMIND
     *
     * @return REMIND
     */
    protected int getDirectPkgsSize(HttpSession session,
                                    ArrayList   pkgs) {
        ArrayList        allpkgs = (ArrayList) session.getAttribute(SESSION_PKGS_FROMTGS_RS);
        int              nDirect = 0;
        TargetChannelMap tempmap = null;

        for (int i = 0; i < allpkgs.size(); i++) {
            tempmap = (TargetChannelMap) allpkgs.get(i);

            if ("true".equals(tempmap.getIsSelectedTarget())) {
                nDirect++;
            }
        }

        return nDirect;
    }

    protected void cleanup(ActionForm form) {
        if (form instanceof TargetDetailsForm) {
            ((TargetDetailsForm) form).clearCheckedItems();
        } else {
            ((TargetDetailsMultiForm) form).clearCheckedItems();
        }
    }
}

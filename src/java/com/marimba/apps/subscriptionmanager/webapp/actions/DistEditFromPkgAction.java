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

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;

import com.marimba.webapps.intf.*;

/**
 * This action is used from the single select mode of the package details page This follows similar logic to the target details page in DistEditAction
 *
 * @author Rahul Ravulur
 * @version 1.13, 10/07/2002
 */
public final class DistEditFromPkgAction
    extends DistEditAction {
    String channelSessionVar = MAIN_PAGE_PACKAGE; // session variable that holds the packages that are selected.


    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                         return new  DistEditFromPkgTask(mapping, form, request, response);
        }

        protected class DistEditFromPkgTask
                   extends SubscriptionDelayedTask {
            DistEditFromPkgTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                      super(mapping, form, request, response);
        }
        public void execute() {
         try {
            if (!(form instanceof IMapProperty)) {
                InternalException ie = new InternalException(GUIUTILS_INTERNAL_WRONGARG, "DistEditAction", form.toString());

                throw new GUIException(ie);
            }

            IMapProperty formbean = (IMapProperty) form;

            if (DEBUG) {
                System.out.println("Distribution Edit Action called");
            }

            // Creating a new distribution bean?
            // so that we do not append to one already present
            DistributionBean distributionBean = new DistributionBean();
            HttpSession      session = request.getSession();

            if (DEBUG) {
                System.out.println("DistEditFromPkgAction: targets are being set for bean");
            }

            // now figure out the target list
            PersistifyChecksAction.SelectedRecords tgSel =
                    (PersistifyChecksAction.SelectedRecords)session.getAttribute(
                            (String)formbean.getValue(SESSION_PERSIST_SELECTED));
            TargetChannelMap sampleMap = null;

            if (tgSel != null) {
                if (DEBUG) {
                    System.out.println(" The selected target size is " + tgSel.getSelectionCount());
                }
                ArrayList targetList = new ArrayList();
                ArrayList targetMapList = new ArrayList(tgSel.getTargetChannelMaps());

                for (Iterator ite = targetMapList.iterator(); ite.hasNext();) {

                    TargetChannelMap tchmap = (TargetChannelMap) ite.next();

                    // Add to selected list only if TargetChannelMap doesn't belong to a different domain / namespace
                    if(tchmap.getIsSelectedTarget().equals("true")) {
                        targetList.add(tchmap.getTarget());
                    }
                }

                if (targetList.size() > 0) {
                    distributionBean.setSelectedTargets(targetList);

                    // designate that we are editing an existing policy assignment
                    // so that the tab is highlighted correctly in the banner
                    distributionBean.setType(EDIT);
                }

                if (targetList.size() == 1) {
            // The user selected one target.  Blackout, priority, and advanced properties can be set
            // Here we load in the necessary information.
            try {
                Target target = (Target) targetList.get(0);
                ISubscription sub = ObjectManager.createSubscription(target.getId(), target.getType(),
                                         GUIUtils.getUser(request));
                distributionBean.setTransmitterProps(getTloginBean(sub));
                distributionBean.setTunerProps(getTunerProps(sub));
                clearSessionVar(request);
                GUIUtils.setToSession(request, PAGE_TCHPROPS_CHANNELS, sub.getChannels());
                GUIUtils.setToSession(request, PAGE_TCHPROPS_SUB, sub);
                GUIUtils.setToSession(request, PAGE_TCHPROPS_SUB_COPY, new Subscription((Subscription) sub));
            } catch (SystemException se) {
                throw new GUIException(se);
            } finally {
                GUIUtils.removeFromSession( request, "multi_trgts_pkg" );
                session.removeAttribute((String) formbean.getValue(SESSION_PERSIST_SELECTED));
                ((PackageDetailsForm) form).clearCheckedItems();
            }
            }else{
                GUIUtils.setToSession(request, "multi_trgts_pkg", "true");
            }

                // figure out channel schedule inconsistencies
                String initSchedule = null;
                String secSchedule = null;
                String updateSchedule = null;
                String verRepairSchedule = null;

                if (!targetMapList.isEmpty()) {
                    sampleMap         = (TargetChannelMap) targetMapList.get(0);
                    initSchedule      = sampleMap.getInitSchedule();
                    secSchedule       = sampleMap.getSecSchedule();
                    updateSchedule    = sampleMap.getUpdateSchedule();
                    verRepairSchedule = sampleMap.getVerRepairSchedule();
                }

                /*Set the initial values for the schedules.
                 *This is derived from the first package in the list.
                 */

                // iterate through the list of targets that are represented
                // as TargetChannelMaps. Since the Edit Assignment interrogates the Packages
                // for schedule information, we need to get the schedule information from the targets
                // and set it into the packages.
                TargetChannelMap tempMap;
                Hashtable        channelTable;
                Enumeration      urls;
                Channel          ch1;
                int minOrder = ISubscriptionConstants.ORDER;

                int size = targetMapList.size();
                for (int i = 0; i<size; i++) {
                    tempMap = (TargetChannelMap) targetMapList.get(i);

                    filterChannels(tempMap, targetMapList, size, i);

                    if (DEBUG) {
                        System.out.println(" DistEditFromPkgComparing TargetChannelMap:: " + tempMap.getName());
                        System.out.println(" number of packages in the map = " + tempMap.getChannels().size());
                        System.out.println(" target = " + tempMap.getTarget());
                    }

                    channelTable = tempMap.getChannels();
                    Hashtable consTable = new Hashtable(channelTable);
                    consTable.putAll(tempMap.getMiscChannels());
                    urls = consTable.keys();
                    while (urls.hasMoreElements()) {
                        ch1 = (Channel) consTable.get(urls.nextElement());
                        distributionBean.addChannel(ch1);
                        distributionBean.setInconsistentStates(ch1.getInconsistentStates());
                        // set the starting priority to the min (highest) priority.
                        if (ch1.getOrder() < minOrder) {
                            minOrder = ch1.getOrder();
                        }
                    }

                    Enumeration miscChnUrls = tempMap.getMiscChannels().keys();
                    while(miscChnUrls.hasMoreElements()) {
                        String miscChnUrl = (String)miscChnUrls.nextElement();
                        distributionBean.addMiscChannelUrl(miscChnUrl);
                    }

                }

                // if the prioties have already been initialized, set starting priorty to
                // the min priority else set it to 1
                if (minOrder == ISubscriptionConstants.ORDER) {
                    distributionBean.setStartingPriority(1);
                } else {
                    distributionBean.setStartingPriority(minOrder);
                }
            }

            if (sampleMap == null) {
                // In the case that no targets is selected, we add in the packages
                // from the session variable that stores the dummy channel objects
                ArrayList pkglist = null;

                if (session.getAttribute(SESSION_MULTIPKGBOOL) == null) {
                    pkglist = (ArrayList) session.getAttribute(MAIN_PAGE_PACKAGE);
                } else {
                    pkglist = (ArrayList) session.getAttribute(MAIN_PAGE_M_PKGS);
                }

                if (pkglist != null) {
                    for (Iterator ite = pkglist.iterator(); ite.hasNext();) {
                        Channel ch1 = (Channel) ite.next();
                        ch1.setOrder(DistAsgValidateAction.MAX_INSTALL_PRIORITY);
                        distributionBean.addChannel(ch1);
                        distributionBean.setInconsistentStates(ch1.getInconsistentStates());
                    }
                }
            }

	session.setAttribute("pkg_view_edit_pkgs", distributionBean.getChannels());
	
            try {
                setDistributionBean(distributionBean, request);
            } catch (SystemException se) {
                throw new GUIException(se);
            } finally {
                session.removeAttribute((String) formbean.getValue(SESSION_PERSIST_SELECTED));
                ((PackageDetailsForm) form).clearCheckedItems();
                if(null != session.getAttribute(SESSION_OSM_TEMPLATE_RESULT)) {
                    session.removeAttribute(SESSION_OSM_TEMPLATE_RESULT);
                }
            }
            session.setAttribute(ISubscriptionConstants.IS_FROM_PKG_VIEW, ISubscriptionConstants.IS_FROM_PKG_VIEW);
           forward = mapping.findForward("success");
           }catch (Exception ex) {
                guiException = new GUIException(new CriticalException(ex.toString()));
                forward = mapping.findForward("failure");
          }
    }
  }

    /**
     * For each miscellaneous channel in the targetMap, check for a similar channel in the TargetChannelMap instances
     * found in the targetMapList. If a channel is not found, then the channel should be removed. Hence distribution
     * assignment page would display only the common channels.
     *
     * @param targetMap Contains the miscellaneous Channel instances for introspection
     * @param targetMapList List of TargetChannelMap instances
     * @param size Size of targetMapList. Not re-calculated.
     * @param currentIndex Index in targetMapList to skip
     */
    private void filterChannels(TargetChannelMap targetMap, ArrayList targetMapList, int size, int currentIndex) {
        List chUrlsForRemoval = new ArrayList();
        Enumeration channelUrls = targetMap.getMiscChannels().keys();
        while(channelUrls.hasMoreElements()) {

            String channelUrl = (String) channelUrls.nextElement();

            // Iterate the targetMapList to check for the channelUrl entry
            for(int i=0; i<size; i++) {
                if(i == currentIndex) {
                    // Skip the current instance
                    continue;
                }
                TargetChannelMap tgChMap = (TargetChannelMap) targetMapList.get(i);
                if(tgChMap.getMiscChannels().get(channelUrl) == null) {
                    chUrlsForRemoval.add(channelUrl);
                }
            }

        }

        // Iterate the list chUrlsForRemoval and remove from TargetChannelMap
        int remListSize = chUrlsForRemoval.size();
        for(int i=0; i<remListSize; i++) {
            targetMap.removeMiscChannel((String)chUrlsForRemoval.get(i));
        }
    }

    /* Used to determine if the schedules are inconsistent
     *
     */
    boolean equalScheduleStrings(String sch1,
                                 String sch2) {
        if (DEBUG2) {
            System.out.println("DistEditFromPkgAction:: comparing sch1  " + sch1);
            System.out.println("with sch2		   " + sch2);
        }

        if ((sch1 == null) && (sch2 != null)) {
            return false;
        }

        if ((sch1 != null) && (sch2 == null)) {
            return false;
        }

        if ((sch1 == null) && (sch2 == null)) {
            return true;
        }

        //They are both not null after this point.  compare the strings
        if (sch1.equals(sch2)) {
            /*Check to see if one of the values is inconsistent already.  This
             *can happen if sch1 or sch2 is inconsistent when selected. In which case, false
             *should be returned
             */
            if (DEBUG2) {
                System.out.println("DistEditAction: schedule strings are equals.  checking inconsistent");
            }

            if (ISubscriptionConstants.INCONSISTENT.equals(sch1) || ISubscriptionConstants.INCONSISTENT.equals(sch2)) {
                if (DEBUG2) {
                    System.out.println("DistEditAction: schedule strings are equals.  one is inconsistent");
                }

                return false;
            }

            return true;
        } else {
            return false;
        }
    }
}


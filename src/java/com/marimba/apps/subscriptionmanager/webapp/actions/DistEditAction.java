// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.common.GenericDistributionAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsMultiForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * This action is used from the single select mode of the target details page
 *
 * @author Angela Saval
 * @author Sunil Ramakrishnan
 * @author Devendra Vamathevan
 */

public class DistEditAction extends GenericDistributionAction {

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        return new DistEditTask(mapping, form, request, response);
    }

    protected class DistEditTask extends SubscriptionDelayedTask {

        DistEditTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        public void execute() {
            try {
                init(form, request);
                init(request);
                IMapProperty formbean = (IMapProperty) form;
                DistributionBean distributionBean = new DistributionBean();
                List targetList = getSelectedTargets(request.getSession());

                if (targetList != null) {
                    distributionBean.setSelectedTargets(targetList);
                } else {
                	String targetId = (String)session.getAttribute(SELECTED_TARGET_ID);
                	String targetName = (String) session.getAttribute(SELECTED_TARGET_NAME);
                	String targetType = (String) session.getAttribute(SELECTED_TARGET_TYPE);
                    if(null != targetId && null != targetName && null != targetType) {
    		    		Target availableTarget = new Target(targetName, targetType, targetId);
    		    		targetList = new ArrayList(DEF_COLL_SIZE);
    		    		targetList.add(availableTarget);
    		    		session.setAttribute(MAIN_PAGE_TARGET, targetList);
    		    		distributionBean.setSelectedTargets(targetList);
                    }
                }
                

                // get the blackout and set it in the DistributionBean
                if (targetList != null && !targetList.isEmpty() && singleMode) {
                    Target target = (Target) targetList.get(0);
                    debugMessage(klass, "perform", "target = " + target);

                    try {
                        ISubscription sub = ObjectManager.openSubForWrite(target.getId(), target.getType(),
                                GUIUtils.getUser(request));
                        distributionBean.setTransmitterProps(getTloginBean(sub));
                        distributionBean.setTunerProps(getTunerProps(sub));
                        clearSessionVar(request);
                        GUIUtils.setToSession(request, PAGE_TCHPROPS_CHANNELS, sub.getChannels());
                        GUIUtils.setToSession(request, PAGE_TCHPROPS_SUB, sub);
                        GUIUtils.setToSession(request, PAGE_TCHPROPS_SUB_COPY, new Subscription((Subscription) sub));
                        String targetType = target.getType();

                        if (TYPE_USER.equalsIgnoreCase(targetType) || TYPE_USERGROUP.equalsIgnoreCase(targetType)) {
                            GUIUtils.setToSession(request, PAGE_SHOW_UCD_BUTTON, "true");
                        } else {
                            request.getSession().removeAttribute(PAGE_SHOW_UCD_BUTTON);
                        }

                    } catch (SystemException se) {
                        se.printStackTrace();
                        throw new GUIException(se);
                    }
                }
                if (!singleMode) {
                    boolean show = true;

                    for (Object obj : targetList) {
                        Target target = (Target) obj;
                        String targetType = target.getType();
                        if (!(TYPE_USER.equalsIgnoreCase(targetType) || TYPE_USERGROUP.equalsIgnoreCase(targetType))) {
                            show = false;
                            break;
                        }
                    }

                    if (show) {
                        GUIUtils.setToSession(request, PAGE_SHOW_UCD_BUTTON, "true");
                    } else {
                        request.getSession().removeAttribute(PAGE_SHOW_UCD_BUTTON);
                    }
                }

                // Get the channels from the page result set returned from the multiple selection.
                ArrayList targetMapList = (ArrayList) session.getAttribute(SESSION_PKGS_FROMTGS_RS);

                if (targetMapList != null) {
                    Channel firstch = null;
                    Channel tempch;
                    TargetChannelMap tempmap = null;

                    if (targetMapList.size() > 0) {
                        // designate that we are editing an existing policy assignment
                        // so that the tab is highlighted correctly in the banner
                        distributionBean.setType(EDIT);
                        debugMessage(klass, "perform", "distributionBean type set to EDIT");

                    }

                    int minOrder = ISubscriptionConstants.ORDER;

                    // Set the initial values for the schedules.
                    // This is derived from the first package in the list.
                    for (Object aTargetMapList : targetMapList) {
                        tempmap = (TargetChannelMap) aTargetMapList;

                        if (singleMode) {
                            // In single select mode, do not consider channels which
                            // are not directly assigned
                            if (!"true".equals(tempmap.getIsSelectedTarget())) {
                                continue;
                            }
                        }

                        tempch = tempmap.getChannel();

                        if (firstch == null) {
                            firstch = tempch;
                        }

                        distributionBean.addChannel(tempch);
                        distributionBean.addInitSchedule(tempch.getUrl(), tempch.getInitScheduleString());
                        distributionBean.addSecondarySchedule(tempch.getUrl(), tempch.getSecScheduleString());
                        distributionBean.addUpdateSchedule(tempch.getUrl(), tempch.getUpdateScheduleString());
                        distributionBean.addVerifyRepairSchedule(tempch.getUrl(), tempch.getVerRepairScheduleString());
                        distributionBean.addPostponeSchedule(tempch.getUrl(), tempch.getPostponeScheduleString());

                        /*For each channel, set whether or not the states are inconsisted.
                        *This is used for displaying the warning messages.  Once the inconsistent
                        *state flag is set true once, it will stay true.
                        */
                        distributionBean.setInconsistentStates(tempch.getInconsistentStates());

                        // set the starting priority to the min (highest) priority.
                        // consider priority of the channels if their order state is consistent
                        // in single mode the order will be consistent by default
                        if (!INCONSISTENT.equals(tempch.getOrderState())) {
                            if (tempch.getOrder() < minOrder) {
                                minOrder = tempch.getOrder();
                            }
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
                List chns = (List) session.getAttribute(IARTaskConstants.AR_CHANNELS);

                // session.setAttribute("ccm_channels",null);
                // DistributionBean distbean = getDistributionBean(request);
                String taskId =(String) session.getAttribute(IARTaskConstants.AR_TASK_ID);
                String changeID = (String)session.getAttribute(IARTaskConstants.AR_CHANGE_ID);
                String ARString = taskId + ((changeID == null)? "": '|' + changeID);
                if (chns != null) {
                    for (Object chn : chns) {
                        Channel o = (Channel) chn;
                        o.setARTaskValue(ARString);
                        distributionBean.addChannel(o);
                        //distributionBean.setARTaskValue(o.getUrl(),ARString);
                    }
                }

                if( "true".equals(main.getConfig().getProperty(IARTaskConstants.AR_TASK_ID_ENABLED_CONFIG)) ) {
                    session.setAttribute(IARTaskConstants.SESSION_AR_TASKID_ENABLED, "true");
                } else {
                    session.removeAttribute(IARTaskConstants.SESSION_AR_TASKID_ENABLED);
                }

                try {
                    setDistributionBean(distributionBean, request);
                } catch (SystemException se) {
                    throw new GUIException(se);
                } finally {
                    if (formbean.getValue(SESSION_PERSIST_SELECTED) != null) {
                        session.removeAttribute((String) formbean.getValue(SESSION_PERSIST_SELECTED));
                    }
                    if(null != session.getAttribute(SESSION_OSM_TEMPLATE_RESULT)) {
                        session.removeAttribute(SESSION_OSM_TEMPLATE_RESULT);
                    }

                    if (form instanceof TargetDetailsForm) {
                        ((TargetDetailsForm) form).clearCheckedItems();
                    } else {
                        ((TargetDetailsMultiForm) form).clearCheckedItems();
                    }
                }
                forward = mapping.findForward("success");

            } catch (Exception ex) {
                ex.printStackTrace();
                guiException = new GUIException(new CriticalException(ex.toString()));
                forward = mapping.findForward("failure");
            }
        }
    }

    protected TLoginBean getTloginBean(ISubscription sub) throws SystemException {
        Enumeration tprops = sub.getPropertyKeys(ISubscriptionConstants.PROP_TUNER_KEYWORD);

        TLoginBean tbean = new TLoginBean();
        String prop;
        String propvalue;
        String transmitter;
        int userIdx;
        int pwdIdx;
        int propLen;
        int mrbaKeychainLen = MARIMBA_KEYCHAIN.length();

        while (tprops.hasMoreElements()) {
            prop = (String) tprops.nextElement();

            if (prop.startsWith(MARIMBA_KEYCHAIN)) {
                propLen = prop.length();
                userIdx = prop.indexOf(".user", propLen - 5);

                if (userIdx > 0) {
                    transmitter = prop.substring(mrbaKeychainLen + 1, propLen - 5);
                    propvalue = sub.getProperty(PROP_TUNER_KEYWORD, prop);

                    if (!"null".equals(propvalue)) {
                        tbean.addTUser(transmitter, propvalue);
                    }

                    continue;
                }

                pwdIdx = prop.indexOf(".password", propLen - 9);

                if (pwdIdx > 0) {
                    transmitter = prop.substring(mrbaKeychainLen + 1, propLen - 9);
                    propvalue = sub.getProperty(PROP_TUNER_KEYWORD, prop);

                    if (!"null".equals(propvalue)) {
                        tbean.addTPwd(transmitter, propvalue);
                    }
                }
            }
        }
        return tbean;
    }

    protected Vector getTunerProps(ISubscription sub) throws SystemException {

        String[] tprops = sub.getPropertyPairs("");
        Vector propsVector = new Vector();
        addToPropsVector(propsVector, tprops, null);

        String[] sprops = sub.getPropertyPairs(PROP_SERVICE_KEYWORD);
        addToPropsVector(propsVector, sprops, PROP_SERVICE_KEYWORD);

        String[] chprops = sub.getPropertyPairs(PROP_CHANNEL_KEYWORD);
        addToPropsVector(propsVector, chprops, PROP_CHANNEL_KEYWORD);

        String[] allchsprops = sub.getPropertyPairs(PROP_ALL_CHANNELS_KEYWORD);
        addToPropsVector(propsVector, allchsprops, PROP_ALL_CHANNELS_KEYWORD);

        String[] deviceProps = sub.getPropertyPairs(PROP_DEVICES_KEYWORD);
        addToPropsVector(propsVector, deviceProps, PROP_DEVICES_KEYWORD);

        String[] pwrProps = sub.getPropertyPairs(PROP_POWER_KEYWORD);
        addToPropsVector(propsVector, pwrProps, PROP_POWER_KEYWORD);

        String[] secProps = sub.getPropertyPairs(PROP_SECURITY_KEYWORD);
        addToPropsVector(propsVector, secProps, PROP_SECURITY_KEYWORD);

        String[] scapProps = sub.getPropertyPairs(PROP_SCAP_SECURITY_KEYWORD);
        addToPropsVector(propsVector, scapProps, PROP_SCAP_SECURITY_KEYWORD);
        
        String[] usgcbProps = sub.getPropertyPairs(PROP_USGCB_SECURITY_KEYWORD);
        addToPropsVector(propsVector, usgcbProps, PROP_USGCB_SECURITY_KEYWORD);
        
        String[] amtProps = sub.getPropertyPairs(PROP_AMT_KEYWORD);
        addToPropsVector(propsVector, amtProps, PROP_AMT_KEYWORD);

        String[] amtAlarmClkProps = sub.getPropertyPairs(PRO_AMT_ALARMCLK_KEYWORD);
        addToPropsVector(propsVector, amtAlarmClkProps, PRO_AMT_ALARMCLK_KEYWORD);

        String[] osTemplateProps = sub.getPropertyPairs(PROP_OSM_KEYWORD);
        addToPropsVector(propsVector, osTemplateProps, PROP_OSM_KEYWORD);

        String[] pbProps = sub.getPropertyPairs(PROP_PBACKUP_KEYWORD);
        addToPropsVector(propsVector, pbProps, PROP_PBACKUP_KEYWORD);


        // add all the channel properties, including properties for
        // channels that doesn't yet exist to the vector. Iterate through all the
        // channels and for each channel through it's list of properties. At the end
        // of this the propsVector contains all the properties (tuner and channel)
        // in this subscription.
        addChannelProperties(sub.getChannels(), propsVector);
        addChannelProperties(sub.getDummyChannels(), propsVector);

        return propsVector;


    }

    void addChannelProperties(Enumeration enumChannels,
                              Vector propsVector)
            throws SystemException {
        for (; enumChannels.hasMoreElements();) {
            Channel chn = (Channel) enumChannels.nextElement();
            String[] pairs = chn.getPropertyPairs();

            for (int i = 0; i < pairs.length; i += 2) {
                debugMessage(klass, "perform", "EditTunerChPropsAction:: addinng channel property " + pairs[i]);
                if ("postponeSched".equals(pairs[i])) {
                    try {
                        chn.updatePostponeSchedule(chn.getProperty(pairs[i]));
                    } catch (Exception ex) {
                        debugMessage(klass, "addChannelProperties", "EditTunerChPropsAction:: postpone schedule can't be parsed correctly " + pairs[i]);
                    }
                } else {
                    propsVector.add(pairs[i] + "," + chn.getUrl() + "=" + chn.getProperty(pairs[i]));
                }
            }
        }
    }

    // this is a private method to load the properties
    //from a String array into a vector.
    private void addToPropsVector(Vector propsVector,
                                  String[] properties,
                                  String type) {
        for (int i = 0; i < properties.length; i++) {
            String propString = "";

            if (type != null) {
                propString = properties[i] + "," + type + "=" + properties[i++];
            } else {
                propString = properties[i] + "=" + properties[i++];
            }
            debugMessage(klass, "perform", "EditTunerChPropsAction:: loading property " + propString);
            propsVector.add(propString);
        }
    }
    protected void clearSessionVar(HttpServletRequest req) {
		if(null == req || null == req.getSession()) {
    		return;
    	}
    	HttpSession session = req.getSession();

    	if(null != session.getAttribute(SESS_MDM_ADDED_DEVICE)) {
    		session.setAttribute(SESS_MDM_ADDED_DEVICE, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_REMOVED_DEVICE)) {
    		session.setAttribute(SESS_MDM_REMOVED_DEVICE, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ADDED_POLICY)) {
    		session.setAttribute(SESS_MDM_ADDED_POLICY, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_REMOVED_POLICY)) {
    		session.setAttribute(SESS_MDM_REMOVED_POLICY, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ASSINGEDPOLICY)) {
    		session.setAttribute(SESS_MDM_ASSINGEDPOLICY, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ASSINGEDDEVICE)) {
    		session.setAttribute(SESS_MDM_ASSINGEDDEVICE, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_AVAILABLEPOLICY)) {
    		session.setAttribute(SESS_MDM_AVAILABLEPOLICY, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_AVAILABLEDEVICE)) {
    		session.setAttribute(SESS_MDM_AVAILABLEDEVICE, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ORGIONAL_AVAILABLE_DEVICE)) {
    		session.setAttribute(SESS_MDM_ORGIONAL_AVAILABLE_DEVICE, null);
    	}
    	if(null != session.getAttribute(SESS_MDM_ORGIONAL_AVAILABLE_POLICY)) {
    		session.setAttribute(SESS_MDM_ORGIONAL_AVAILABLE_POLICY, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ORGIONAL_ASS_DEVICE)) {
    		session.setAttribute(SESS_MDM_ORGIONAL_ASS_DEVICE, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ORGIONAL_ASS_POLICY)) {
    		session.setAttribute(SESS_MDM_ORGIONAL_ASS_POLICY, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ASSINGEDGROUP)) {
    		session.setAttribute(SESS_MDM_ASSINGEDGROUP, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_ORGIONAL_AVAILABLE_GROUP)) {
    		session.setAttribute(SESS_MDM_ORGIONAL_AVAILABLE_GROUP, null);
    	} 
    	if(null != session.getAttribute(SESS_MDM_AVAILABLEGROUP)) {
    		session.setAttribute(SESS_MDM_AVAILABLEGROUP, null);
    	}
    }
}

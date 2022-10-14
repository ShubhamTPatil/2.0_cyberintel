// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.system;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.dao.LDAPSubscription;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.ChannelComparator;
import com.marimba.apps.subscriptionmanager.webapp.util.Utils;
import com.marimba.tools.util.Props;
import com.marimba.intf.castanet.IChannel;
import com.marimba.webapps.intf.SystemException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

/**
 * This bean captures the state of the current policy before the tranactional save.
 * This bean will be stored in the session of the user.  It will be used when creating/editing a
 * policy from the targets view or application view. The trascactional save will save the state
 * of this bean to LDAP.
 * It is similar to the Subscribtion object, except it handles multiple targets.
 * Before these methods are called, verification should have already taken place in the forms.
 *
 * @author Angela Saval
 * @author Theen-Theen Tan
 * @author Sunil Ramakrishnan
 */
public class DistributionBean implements ISubscriptionConstants, IAppConstants, IWebAppConstants, IWebAppsConstants {

    static ChannelComparator comp = new ChannelComparator(CH_INSTALL_PRIORITY_KEY);
    private String type = NEW;
    private ArrayList targets = new ArrayList(100); /* this is going to be a representations
    * of the targets using the distinguished
    * name of the target.
    */
    private String defaultState = ISubscriptionConstants.STATE_SUBSCRIBE; /* Used if the application
    * does not have its state
    * set
    */

    private Hashtable channels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable initStateChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable initSecStateChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable initOrderStateChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable initBlackOutStateChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable initSchedChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable secondSchedChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable updateSchedChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable verifyRepairSchedChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable postponeSchedChannels = new Hashtable(DEF_COLL_SIZE);
    private Hashtable modifiedChannelOrders = new Hashtable(DEF_COLL_SIZE);
    private Hashtable preChannelOrders = new Hashtable(DEF_COLL_SIZE);

    private HashMap ARTaskid = new HashMap(DEF_COLL_SIZE);

    private String initSched = null;
    private String secSched = null;
    private String updateSched = null;
    private String verRepairSched = null;
    private String postponeSched = null;
    private String initSchedInitValue = null;
    private String secSchedInitValue = null;
    private String updateSchedInitValue = null;
    private String verRepairSchedInitValue = null;
    private String postponeSchedInitValue = null;
    private String blackOut = null;
    private boolean serviceExemptFromBlackout = false;
    private boolean patchServiceExemptFromBlackout = false;
    private String serviceSchedule = null;
    private String rebootSchedule = null;
    boolean inconsistentStates = false;
    boolean inconsistentScheds = false;
    private Vector tunerProps;
    private int startingPriority;
    private TLoginBean tbean;
    private Vector powerProps = new Vector(DEF_COLL_SIZE);
    private Vector<String> securityProps = new Vector<String>(DEF_COLL_SIZE);
    private Vector<String> scapSecurityProps = new Vector<String>(DEF_COLL_SIZE);
    private Vector<String> usgcbSecurityProps = new Vector<String>(DEF_COLL_SIZE);
    private Vector<String> customSecurityProps = new Vector<String>(DEF_COLL_SIZE);
    private Vector<String> deviceProps = new Vector<String>(DEF_COLL_SIZE);
    private Vector amtProps = new Vector(DEF_COLL_SIZE);
    private Vector osTemplateProps = new Vector(DEF_COLL_SIZE);
    private Vector personalBackupProps = new Vector(DEF_COLL_SIZE);
    private Vector amtAlarmClkProps = new Vector(DEF_COLL_SIZE);
    private Props powerProfiles, securityProfiles, scapProfiles, usgcbProfiles, customSecurityPolicies;
    Props config;
    private boolean wowforInit = false;
    private boolean wowforSec = false;
    private boolean wowforUpdate = false;
    private boolean wowforRepair = false;
    private Map<String, String> LDAPVarsMap;
    private SubscriptionMain main;

    /**
     * Urls of the Channels which are not selected by the user. Instead automatically added when the user
     * selected a target in Pkg View to navigate to Target View. This channel urls will be used after
     * transactSave is called for removing from the Channels list.
     */
    private List miscChannelUrls;

    public DistributionBean() {
    }

    public DistributionBean(HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(context, request);
        this.main = main;
        File rootDir = main.getDataDirectory();

        // load power profiles
        File configFile = new File(rootDir, "PowerSettings.txt");
        this.LDAPVarsMap = main.getLDAPVarsMap();
        config = new Props(configFile);
        config.load();
        this.setPowerProfiles(config);

        // load security profiles
        File secCfgFile = new File(rootDir, "desktop-security-settings.txt");
        Props secConfig = new Props(secCfgFile);
        secConfig.load();
        this.setSecurityProfiles(secConfig);

        // load scap(non-windows) security profiles
        File scapCfgFile = new File(rootDir, "scap-security-settings.txt");
        Props scapConfig = new Props(scapCfgFile);
        scapConfig.load();
        this.setScapProfiles(scapConfig);

        // load scap(windows) security profiles
        File usgcbCfgFile = new File(rootDir, "usgcb-security-settings.txt");
        Props usgcbConfig = new Props(usgcbCfgFile);
        usgcbConfig.load();
        this.setUsgcbProfiles(usgcbConfig);

        // load custom security profiles
        File customCfgFile = new File(rootDir, "custom-security-settings.txt");
        Props customConfig = new Props(customCfgFile);
        customConfig.load();
        this.setCustomSecurityPolicies(customConfig);
    }

    /**
     * This method returns the type of this distribution- NEW if we are creating a new distribution
     * EDIT if we are editing a distribution
     *
     * @param type is the type we are setting it to
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * This method gets the type of this distribution NEW if we are creating a new distribution policy
     * EDIT if we are editing an existing distribution policy
     *
     * @return REMIND
     */
    public String getType() {
        return this.type;
    }

    /**
     * This method takes the targets in the ADD_REMOVE_SELECTED_PAGE_TARGETS variable and sets them
     * to the targets variable in the Distribution bean.
     *
     * @param targetsVec is the vector of selected targets that the user has picked using the GUI page:
     *                   Add/Remove Targets page
     */
    public void setSelectedTargets(List targetsVec) {
        if (targetsVec == null) {
            //REMIND: need to figure out how to deal with the verfication in the system beans
        }

        this.targets = (ArrayList) (new ArrayList(targetsVec)).clone();
    }

    /**
     * REMIND
     *
     * @param packagesVec REMIND
     */
    public void setSelectedPackages(ArrayList packagesVec) {
        if (packagesVec == null) {
            //REMIND:RCR need to figure out how to deal with the verfication in the system beans
            return;
        }

        for (int i = 0; i < packagesVec.size(); i++) {
            Channel ch = (Channel) packagesVec.get(i);
            channels.put(ch.getUrl(), ch);
        }
    }

    /**
     * REMIND
     *
     * @param target REMIND
     */
    public void removeTarget(String target) {
        targets.remove(target);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ArrayList getTargets() {
        return targets;
    }

    /**
     * REMIND
     *
     * @param url   REMIND
     * @param title REMIND
     */
    public void setChannel(String url,
                           String title, String ptype, Map<String, String> LDAPVarsMap, String tenantName, IChannel channel) {
        // append to the end of the list.
        String state = defaultState;
        if (ptype != null) {
            state = state + "," + ptype;
        }


        if (null == channels.get(url)) {
            Channel app = null;
            try {
                app = LDAPSubscription.createContent(url, state, null, LDAPVarsMap, tenantName, channel);
                app.setTitle(title);
            } catch (SystemException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
            channels.put(url, app);
        }

        //If it is already in the list, we do not alter anything
    }

    /**
     * REMIND
     *
     * @param url REMIND
     * @return REMIND
     */
    public Channel getChannel(String url) {
        return (Channel) channels.get(url);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public ArrayList getChannels() {
        ArrayList result = new ArrayList(channels.values());
        // Only sorting by "order"
        Collections.sort(result, comp);
        return result;
    }

    /**
     * @return all channel that are of type patch
     */
    public ArrayList getPatchChannels() {
        ArrayList result = getChannelsByType(CONTENT_TYPE_PATCHGROUP);
        // Only sorting by "order"
        Collections.sort(result, comp);
        return result;
    }


    /**
     * @return all channels that are of type application
     */

    public ArrayList getApplicationChannels() {
        ArrayList result = getChannelsByType(CONTENT_TYPE_APPLICATION);
        // Only sorting by "order"
        Collections.sort(result, comp);
        return result;
    }

    /**
     *
     * @return all channels that are of type application
     */
    public ArrayList getModifiedChannelOrders() {
        Collection allChannels = modifiedChannelOrders.values();
        ArrayList result = new ArrayList();
        for (Iterator iterator = allChannels.iterator(); iterator.hasNext();) {
            Channel c = (Channel) iterator.next();
            result.add(c);
        }
        // Only sorting by "order"
        Collections.sort(result, comp);
        return result;
    }
    public void removeModifiedChannelOrders() {
        modifiedChannelOrders.clear();
    }
    /**
     * @param chType selects the type of channel to return,
     *               the two types currently supported are
     *               CONTENT_TYPE_PATCHGROUP and CONTENT_TYPE_APPLICATION
     * @return returns only channels that of type chType
     */
    public final ArrayList getChannelsByType(String chType) {
        Collection allChannels = channels.values();
        ArrayList result = new ArrayList();
        for (Iterator iterator = allChannels.iterator(); iterator.hasNext();) {
            Channel c = (Channel) iterator.next();
            if (chType.equals(c.getType())) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * @return the channels hashtable
     */
    public Hashtable getChannelsTable() {
        return channels;
    }

    /**
     * In order to display the radio buttons correctly, we need to know if the channel had inconsistate
     *
     * @param url   REMIND
     * @param state REMIND
     */
    public void setChInitStateValue(String url,
                                    String state) {
        if (DEBUG) {
            System.out.println("DistributionBean: setChInitStateValue for " + url + ", state = " + state);
        }

        //This is so that if the state is not defined, at least there will be an
        //empty value string.  Otherwise, the conflict resolution becomes confused
        if (state == null) {
            state = "";
        }

        if ((state != null) && (url != null)) {
            initStateChannels.put(url, state);
        }
    }

    /**
     * In order to display the radio buttons correctly, we need to know if the channel had inconsistent state when it was set.  This method allows for the JSP
     * page to retrieve the this information
     *
     * @param url REMIND
     * @return REMIND
     */
    public String getChInitStateValue(String url) {
        if (url != null) {
            return (String) initStateChannels.get(url);
        } else {
            return "";
        }
    }

    /**
     * In order to display the radio buttons correctly, we need to know if the channel had inconsistate
     *
     * @param url   REMIND
     * @param state REMIND
     */
    public void setChInitOrderStateValue(String url, String state) {
        if (DEBUG) {
            System.out.println("DistributionBean: setChInitOrderStateValue for " + url + ", state = " + state);
            System.out.println("Distribution Bean - order url = " + url);
            System.out.println("Distribution Beab - order state value = " + state);
        }

        if (state == null) {
            state = "";
        }

        if ((state != null) && (url != null)) {
            initOrderStateChannels.put(url, state);
        }
    }

    public String getChInitOrderStateValue(String url) {
        if (url != null) {
            return (String) initOrderStateChannels.get(url);
        }

        return "";
    }

    /**
     * In order to display the radio buttons correctly, we need to know if the channel had inconsistate
     *
     * @param url   REMIND
     * @param state REMIND
     */
    public void setChInitBlackOutStateValue(String url, String state) {
        if (DEBUG) {
            System.out.println("DistributionBean: setChInitBlackOutStateValue for " + url + ", state = " + state);
            System.out.println("Distribution Bean - Blackout url = " + url);
            System.out.println("Distribution Beab - Blackout state value = " + state);
        }

        if (state == null) {
            state = "";
        }

        if ((state != null) && (url != null)) {
            initBlackOutStateChannels.put(url, state);
        }
    }

    public String getChInitBlackOutStateValue(String url) {
        if (url != null) {
            return (String) initBlackOutStateChannels.get(url);
        }

        return "";
    }

    /**
     * In order to display the radio buttons correctly, we need to know if the channel had inconsistate
     *
     * @param url   REMIND
     * @param state REMIND
     */
    public void setChInitSecStateValue(String url,
                                       String state) {
        if (DEBUG) {
            System.out.println("DistributionBean: setChSecStateValue for " + url + ", state = " + state);
            System.out.println("Distribution Bean - sec url = " + url);
            System.out.println("Distribution Beab - sec state value = " + state);
        }

        if (state == null) {
            state = "";
        }

        if ((state != null) && (url != null)) {
            initSecStateChannels.put(url, state);
        }
    }

    /**
     * In order to display the radio buttons correctly, we need to know if the channel had inconsistent state when it was set.  This method allows for the JSP
     * page to retrieve the this information
     *
     * @param url REMIND
     * @return REMIND
     */
    public String getChInitSecStateValue(String url) {
        if (url != null) {
            return (String) initSecStateChannels.get(url);
        }

        return "";
    }

    /**
     * REMIND
     *
     * @param url REMIND
     * @return REMIND
     */
    public String getChInitInconsistentStates(String url) {
        if (url != null) {
            if (ISubscriptionConstants.INCONSISTENT.equals(initSecStateChannels.get(url))
                    || ISubscriptionConstants.INCONSISTENT.equals(initStateChannels.get(url))) {
                return "true";
            } else {
                return "false";
            }
        }
        return "false";
    }

    /**
     * Add a url of a channel. This is used in DistEditFromPkgAction class for adding the list of channels
     * which is not selected by the user.
     *
     * @param miscChnUrl URL of the miscellaneous channel
     */
    public void addMiscChannelUrl(String miscChnUrl) {
        if (miscChannelUrls == null) {
            miscChannelUrls = new ArrayList();
        }
        miscChannelUrls.add(miscChnUrl);
    }

    /**
     * REMIND
     *
     * @param newch REMIND
     */
    public void addChannel(Channel newch) {
        if (DEBUG) {
            System.out.println("DistributionBean: adding channel = " + newch.getUrl());
        }

        if (newch != null) {
            Object ch = channels.get(newch.getUrl());

            if (ch != null) {
                //this channel has already been added, and therefore we should
                //deal with the inconsistent values
                try {
                    updateConsistency((Channel) ch, newch);
                } catch (SystemException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                }
            }

            setChInitStateValue(newch.getUrl(), newch.getState());
            setChInitSecStateValue(newch.getUrl(), newch.getSecState());
            setChInitOrderStateValue(newch.getUrl(), newch.getOrderState());
            setChInitBlackOutStateValue(newch.getUrl(), newch.getBlackOutState());

            setARTaskValue(newch.getUrl(),newch.getARTaskValue());
            channels.put(newch.getUrl(), newch);
        }
    }

    void updateConsistency(Channel oldch,
                           Channel newch) throws SystemException {
        // if old channel is inconsistent or new channel state has been changed
        // set value as inconsistent
        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getState())) {
            if (!Utils.channelAttrCompare(newch.getState(), oldch.getState())) {
                newch.setState(ISubscriptionConstants.INCONSISTENT);

                if (DEBUG) {
                    System.out.println("DistributionBean: initState inconsistent for " + newch.getUrl());
                }
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its state
            newch.setState(ISubscriptionConstants.INCONSISTENT);
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getSecState())) {
            if (DEBUG) {
                System.out.println("DistributionBean.updateConsistency: oldch sec state = " + oldch.getSecState());
                System.out.println("DistributionBean.updateConsistency: newch sec state = " + newch.getSecState());
            }

            if (!Utils.channelAttrCompare(newch.getSecState(), oldch.getSecState())) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting secondary inconsistent for " + newch.getUrl());
                }

                newch.setSecState(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its state
            newch.setSecState(ISubscriptionConstants.INCONSISTENT);
        }
        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getInitScheduleString())) {
            if (!Utils.channelAttrCompare(newch.getInitScheduleString(), oldch.getInitScheduleString())) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting the InitSchedule to inconsistent for " + newch.getUrl());
                }
                newch.setInitSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its InitSchedule
            newch.setInitSchedule(ISubscriptionConstants.INCONSISTENT);
        }
        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getSecScheduleString())) {
            if (!Utils.channelAttrCompare(newch.getSecScheduleString(), oldch.getSecScheduleString())) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting the SecSchedule to inconsistent for " + newch.getUrl());
                }
                newch.setSecSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its SecSchedule
            newch.setSecSchedule(ISubscriptionConstants.INCONSISTENT);
        }
        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getUpdateScheduleString())) {
            if (!Utils.channelAttrCompare(newch.getUpdateScheduleString(), oldch.getUpdateScheduleString())) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting the UpdateSchedule to inconsistent for " + newch.getUrl());
                }
                newch.setUpdateSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its UpdateSchedule
            newch.setUpdateSchedule(ISubscriptionConstants.INCONSISTENT);
        }
        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getVerRepairScheduleString())) {
            if (!Utils.channelAttrCompare(newch.getVerRepairScheduleString(), oldch.getVerRepairScheduleString())) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting the VerRepairSchedule to inconsistent for " + newch.getUrl());
                }
                newch.setVerRepairSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its VerRepairSchedule
            newch.setVerRepairSchedule(ISubscriptionConstants.INCONSISTENT);
        }
        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getPostponeScheduleString())) {
            if (!Utils.channelAttrCompare(newch.getPostponeScheduleString(), oldch.getPostponeScheduleString())) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting the postponeSchedule to inconsistent for " + newch.getUrl());
                }
                newch.setPostponeSchedule(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its VerRepairSchedule
            newch.setPostponeSchedule(ISubscriptionConstants.INCONSISTENT);
        }
        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getOrderState())) {
            if ( newch.getOrder() != oldch.getOrder() ) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting the OrderState to inconsistent for " + newch.getUrl());
                }
                newch.setOrderState(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its OrderState
            newch.setOrderState(ISubscriptionConstants.INCONSISTENT);
        }

        if (!ISubscriptionConstants.INCONSISTENT.equals(oldch.getBlackOutState())) {
            if ( newch.isExemptFromBlackout() != oldch.isExemptFromBlackout() ) {
                if (DEBUG) {
                    System.out.println("DistributionBean: setting the BlackOutState to inconsistent for " + newch.getUrl());
                }
                newch.setBlackOutState(ISubscriptionConstants.INCONSISTENT);
            }
        } else {
            //we want to be sure that the new channel has inconsistent set for its BlackOutState
            newch.setBlackOutState(ISubscriptionConstants.INCONSISTENT);
        }
    }

    /**
     * REMIND
     *
     * @param url REMIND
     */
    public void removeChannel(String url) {
        channels.remove(url);
    }

    /**
     * Fetch the miscellaneous channel URLs list.
     */
    public List getMiscChannelUrls() {
        return miscChannelUrls;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getInitSchedule() {
        return initSched;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSecSchedule() {
        return secSched;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getUpdateSchedule() {
        return updateSched;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getVerRepairSchedule() {
        return verRepairSched;
    }

    public String getPostponeSchedule() {
        return postponeSched;
    }

    /* Obtains the initial values that were set for the schedule on the distribution bean
      *
      */
    public String getInitScheduleInitValue() {
        return initSchedInitValue;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSecScheduleInitValue() {
        return secSchedInitValue;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getUpdateScheduleInitValue() {
        return updateSchedInitValue;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getVerRepairScheduleInitValue() {
        return verRepairSchedInitValue;
    }

    public String getPostponeScheduleInitValue() {
        return postponeSchedInitValue;
    }

    /**
     * Used to get the initial schedule value that is set when the packages are edited from the target details area.
     *
     * @param sch REMIND
     */
    public void setInitScheduleInitValue(String sch) {
        initSchedInitValue = sch;
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setSecScheduleInitValue(String sch) {
        secSchedInitValue = sch;
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setUpdateScheduleInitValue(String sch) {
        updateSchedInitValue = sch;
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setVerRepairScheduleInitValue(String sch) {
        verRepairSchedInitValue = sch;
    }

    public void setPostponeScheduleInitValue(String sch) {
        postponeSchedInitValue = sch;
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setInitSchedule(String sch) {
        if ((sch != null) && (sch.length() == 0)) {
            sch = null;
        }

        initSched = sch;
//        setInconsistentScheds();
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setSecSchedule(String sch) {
        if ((sch != null) && (sch.length() == 0)) {
            sch = null;
        }

        secSched = sch;
//        setInconsistentScheds();
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setUpdateSchedule(String sch) {
        if ((sch != null) && (sch.length() == 0)) {
            sch = null;
        }

        updateSched = sch;
//        setInconsistentScheds();
    }

    /**
     * REMIND
     *
     * @param sch REMIND
     */
    public void setVerRepairSchedule(String sch) {
        if ((sch != null) && (sch.length() == 0)) {
            sch = null;
        }

        verRepairSched = sch;
//        setInconsistentScheds();
    }

    public void setPostponeSchedule(String sch) {
        if ((sch != null) && (sch.length() == 0)) {
            sch = null;
        }

        postponeSched = sch;
    }

    /**
     * REMIND
     *
     * @param value REMIND
     */
    public void setInconsistentStates(boolean value) {
        /*The inconsistent states can only be set true once.  Once it is false,
           *it will always be true.  This is because we want to use this for showing
           * and hiding the warning message
           */
        if (inconsistentStates) {
            inconsistentStates = true;
        }
    }

    /**
     * Used from the distribution edit action to set whether or not any of the states are inconsistent at the time that they were edited.
     REMIND t3: this will be needed when we offer inconsistent schedule identification on the
     Common Schedule page
     public void setInconsistentScheds() {
     if (ISubscriptionConstants.INCONSISTENT.equals(initSched) || ISubscriptionConstants.INCONSISTENT.equals(secSched) || ISubscriptionConstants.INCONSISTENT
     .equals(updateSched) || ISubscriptionConstants.INCONSISTENT
     .equals(verRepairSched)) {
     inconsistentScheds = true;
     } else {
     inconsistentScheds = false;
     }
     }
     */

    /**
     * Used to get whether or not any of the channels had inconsistent states at the time they were edited.
     *
     * @return REMIND
     */
    public boolean getInconsistentStates() {
        return inconsistentStates;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getInconsistentScheds() {
        return inconsistentScheds;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean getInconsistentSet() {
        if (inconsistentScheds || inconsistentStates) {
            return true;
        } else {
            return false;
        }
    }

    public String getBlackOut() {
        return blackOut;
    }

    public void setBlackOut(String blackOut) {
        this.blackOut = blackOut;
    }

    public boolean isServiceExemptFromBlackout() {
        return serviceExemptFromBlackout;
    }

    public void setServiceExemptFromBlackout(boolean serviceExemptFromBlackout) {
        this.serviceExemptFromBlackout = serviceExemptFromBlackout;
    }

    public boolean isPatchServiceExemptFromBlackout() {
        return patchServiceExemptFromBlackout;
    }

    public void setPatchServiceExemptFromBlackout(boolean patchServiceExemptFromBlackout) {
        this.patchServiceExemptFromBlackout = patchServiceExemptFromBlackout;
    }

    public String getServiceSchedule() {
        return serviceSchedule;
    }

    public void setServiceSchedule(String serviceSchedule) {
        this.serviceSchedule = serviceSchedule;
    }

    public String getRebootSchedule() {
        return rebootSchedule;
    }

    public void setRebootSchedule(String rebootSchedule) {
        this.rebootSchedule = rebootSchedule;
    }

    public Vector getTunerProps() {
        return tunerProps;
    }

    public void setTunerProps(Vector tunerProps) {
        this.tunerProps = tunerProps;
    }

    public Vector getPowerProps() {
        return powerProps;
    }

    public Vector<String> getScapSecurityProps() {
        return scapSecurityProps;
    }

    public void setScapSecurityProps(Vector<String> scapSecurityProps) {
        this.scapSecurityProps = scapSecurityProps;
    }

    public Vector<String> getUsgcbSecurityProps() {
        return usgcbSecurityProps;
    }

    public void setUsgcbSecurityProps(Vector<String> usgcbSecurityProps) {
        this.usgcbSecurityProps = usgcbSecurityProps;
    }

    public Vector<String> getCustomSecurityProps() {
        return customSecurityProps;
    }

    public void setCustomSecurityProps(Vector<String> customSecurityProps) {
        this.customSecurityProps = customSecurityProps;
    }

    public void setPowerProps(Vector powerProps) {
        this.powerProps = powerProps;
    }

    public Vector<String> getSecurityProps() {
        return securityProps;
    }

    public void setSecurityProps(Vector<String> securityProps) {
        this.securityProps = securityProps;
    }

    public Vector getAmtProps() {
        return amtProps;
    }

    public void setAmtProps(Vector amtProps) {
        this.amtProps = amtProps;
    }

    public Vector getOsTemplateProps() {
        return osTemplateProps;
    }

    public void setOsTemplateProps(Vector osTemplateProps) {
        this.osTemplateProps = osTemplateProps;
    }

    public Vector getPersonalBackupProps() {
        return personalBackupProps;
    }

    public void setPersonalBackupProps(Vector personalBackupProps) {
        this.personalBackupProps = personalBackupProps;
    }

    public Vector getAmtAlarmClkProps() {
        return amtAlarmClkProps;
    }

    public void setAmtAlarmClkProps(Vector amtAlarmClkProps) {
        this.amtAlarmClkProps = amtAlarmClkProps;
    }

    public Props getPowerProfiles() {
        return powerProfiles;
    }

    public void setPowerProfiles(Props powerProfiles) {
        this.powerProfiles = powerProfiles;
    }

    public Props getSecurityProfiles() {
        return securityProfiles;
    }

    public void setSecurityProfiles(Props securityProfiles) {
        this.securityProfiles = securityProfiles;
    }

    public Props getScapProfiles() {
        return scapProfiles;
    }

    public void setScapProfiles(Props scapProfiles) {
        this.scapProfiles = scapProfiles;
    }

    public Props getUsgcbProfiles() {
        return usgcbProfiles;
    }

    public void setUsgcbProfiles(Props usgcbProfiles) {
        this.usgcbProfiles = usgcbProfiles;
    }

    public Props getCustomSecurityPolicies() {
        return customSecurityPolicies;
    }

    public void setCustomSecurityPolicies(Props customSecurityPolicies) {
        this.customSecurityPolicies = customSecurityPolicies;
    }

    public int getStartingPriority() {
        return startingPriority;
    }
    public void setNewPriority(String url, Channel ch) {
        modifiedChannelOrders.put(url, ch);
    }
    public void setStartingPriority(int startingPriority) {
        this.startingPriority = startingPriority;
    }


    public void setTransmitterProps(TLoginBean tbean) {
        this.tbean = tbean;
    }

    public TLoginBean getTransmitterProps() {
        return tbean;
    }

    public void addInitSchedule(String url,String sch) {

        if((url != null)  && (sch != null) ) {
            initSchedChannels.put(url,sch);
        }
    }
    public void addSecondarySchedule(String url,String sch) {

        if((url != null)  && (sch != null) ) {
            secondSchedChannels.put(url,sch);
        }
    }
    public void addUpdateSchedule(String url,String sch) {

        if((url != null)  && (sch != null) ) {
            updateSchedChannels.put(url,sch);
        }
    }
    public void addVerifyRepairSchedule(String url,String sch) {

        if((url != null)  && (sch != null) ) {
            verifyRepairSchedChannels.put(url,sch);
        }

    }
    public void addPostponeSchedule(String url, String sch) {
        if ((url != null) && (sch != null)) {
            postponeSchedChannels.put(url, sch);
        }
    }
    public String getInitSchedule(String url) {
        return (String)initSchedChannels.get(url);

    }
    public String getSecondarySchedule(String url) {
        return (String)secondSchedChannels.get(url);

    }
    public String getUpdateSchedule(String url) {
        return (String)updateSchedChannels.get(url);

    }
    public String getVerifyRepairSchedule(String url) {
        return (String)verifyRepairSchedChannels.get(url);

    }
    public String getPostponeSchedule(String url) {
        return (String) postponeSchedChannels.get(url);
    }

    public void setARTaskValue(String url,
                               String taskid) {
        if (url != null) {
            ARTaskid.put(url, taskid);
        }
    }

    public String getARTaskValue(String url) {
        if (url != null) {
            return (String) ARTaskid.get(url);
        }
        return null;
    }

    public void clearPreState(){
        preChannelOrders.clear();
    }

    public void setPreState( ArrayList channelList ){
        Channel curapp = null;
        preChannelOrders = new Hashtable( DEF_COLL_SIZE );
        for( int index = 0; index < channelList.size(); index++ ){
            curapp = ( Channel )channelList.get( index );
            preChannelOrders.put( curapp.getUrl(), new Channel( curapp.getUrl(), curapp.getState(), curapp.getOrder() ) );
        }
    }

    public Hashtable getPreState(){
        return preChannelOrders;
    }

    public void setWowforUpdate(boolean wowforUpdate) {
        this.wowforUpdate = wowforUpdate;
    }

    public boolean getWowforUpdate() {
        return this.wowforUpdate;
    }

    public void setWowforRepair(boolean wowforRepair) {
        this.wowforRepair = wowforRepair;
    }

    public boolean getWowforRepair() {
        return this.wowforRepair;
    }

    public void setWowforInit(boolean wowforInit) {
        this.wowforInit = wowforInit;
    }

    public boolean getWowforInit() {
        return this.wowforInit;
    }
    public void setWowforSec(boolean wowforSec) {
        this.wowforSec = wowforSec;
    }

    public boolean getWowforSec() {
        return this.wowforSec;
    }

    public Vector<String> getDeviceProps() {
        return deviceProps;
    }

    public void setDeviceProps(Vector<String> deviceProps) {
        this.deviceProps = deviceProps;
    }
}



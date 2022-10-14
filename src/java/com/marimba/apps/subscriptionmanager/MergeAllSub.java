// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager;

import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;

//import com.marimba.apps.subscription.*;
import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.objects.*;
import com.marimba.apps.subscription.common.util.*;

import com.marimba.apps.subscriptionmanager.intf.*;

import com.marimba.intf.util.*;
import com.marimba.tools.ldap.*;

import com.marimba.webapps.intf.*;

/**
 * Merges the 'all' Subscriptions from various child containers into Subscription located at the Subscription Base. If there was no top level All Subscription,
 * it is created.  The 'all' Subscriptions in the sub containers are deleted after the merge. If more than one 'all' Subscription contain the same channel
 * package resolution follows the priorities based on state and schedule If more than one 'all' Subscriptions contain the same  tuner/service/subscriber/'all'
 * channel property. The value is taken from the first policy that is being merged. A preview mode is vailable.
 *
 * @author Theen-Theen Tan
 * @version 1.4, 03/17/2003
 */
public class MergeAllSub
        implements IErrorConstants,
        IAppConstants,
        ISubscriptionConstants {
    SubscriptionMain main;
    String subBase;
    LDAPConnection subConn;
    ILDAPDataSourceContext ldapCtx;
    HashMap existingAll = new HashMap(DEF_COLL_SIZE);
    StringBuffer resultBuf = new StringBuffer();
    Subscription mergeResult = null;
    boolean mergeResultCreated = false;
    Map<String, String> LDAPVars;

    /**
     * Creates a new MergeAllSub object.
     *
     * @param main REMIND
     * @param ctx REMIND
     *
     * @throws SystemException REMIND
     */
    public MergeAllSub(SubscriptionMain main,
                       ILDAPDataSourceContext ctx)
            throws SystemException {
        this.main = main;
        this.subBase = main.getSubBase();
        this.ldapCtx = ctx;
        this.subConn = ldapCtx.getSubConn();
        LDAPVars = main.getLDAPVarsMap();
    }

    /**
     * Merges the All Subscriptions from various child containers into Subscription located at the Subscription Base.
     *
     * @param preview true to preview the merge
     *
     * @throws SystemException REMIND
     */
    public void merge(boolean preview)
            throws SystemException {
        resultBuf = new StringBuffer(2054);
        mergeResult = null;
        mergeResultCreated = false;

        try {
            getExistingAll();

            // We start with the higher level domains before
            // the subdomains so that the containers at the higher
            // level will take precedence
            int level = 1;
            TreeSet existing = new TreeSet(existingAll.keySet());
            String allStr = null;
            Name allName = null;

            while (!existing.isEmpty()) {
                Iterator ite = existing.iterator();

                while (ite.hasNext()) {
                    allStr = (String) ite.next();
                    allName = subConn.getParser()
                            .parse(allStr);

                    if (allName.size() == level) {
                        if (level == 1) {
                            mergeResult = (Subscription) existingAll.get(allStr);
                            createAll();
                        } else {
                            merge(allStr);
                        }

                        ite.remove();
                    }
                }

                level++;
            }

            if (preview) {
                resultBuf.append("Previewing the merge of 'all' Subscriptions\n\n");

                if (existingAll.isEmpty()) {
                    resultBuf.append("No existing 'all' Subscriptions to merge\n");

                    return;
                }

                if (mergeResultCreated) {
                    resultBuf.append("Top Level 'all' Subscription created\n");
                }

                resultBuf.append(mergeResult.toString());
                resultBuf.append("\n");

                for (Iterator ite = existingAll.entrySet()
                        .iterator(); ite.hasNext();) {
                    Map.Entry entry = (Map.Entry) ite.next();
                    Subscription sub = (Subscription) entry.getValue();

                    if (!sub.equals(mergeResult)) {
                        resultBuf.append("Subscription " + ((String) entry.getKey()) + ',' + subBase + " will be deleted\n");
                    }
                }
            } else {
                if (existingAll.isEmpty()) {
                    resultBuf.append("No existing 'all' Subscriptions to merge\n");

                    return;
                }

                mergeResult.save();

                // Delete the subscriptions that is not in the Subscription Base
                resultBuf.append("Merge of 'all' Subscriptions\n\n");

                if (mergeResultCreated) {
                    resultBuf.append("Top Level 'all' Subscription created\n");
                }

                resultBuf.append(mergeResult.toString());
                resultBuf.append("\n");

                for (Iterator ite = existingAll.entrySet()
                        .iterator(); ite.hasNext();) {
                    Map.Entry entry = (Map.Entry) ite.next();
                    Subscription sub = (Subscription) entry.getValue();
                    String dn = (String) entry.getKey() + ',' + subBase;

                    if (!sub.equals(mergeResult)) {
                        subConn.deleteObject(dn);
                        resultBuf.append("Subscription " + dn + " deleted\n");
                    }
                }
            }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        }
    }

    /**
     * Returns output of the merge process
     *
     * @return A String message containing the result of the merge, including conflict resolution.
     */
    public String getMessage() {
        return resultBuf.toString();
    }

    /**
     * Determines if -upgrade needs to be performed.  Upgrade is needed if there's more than one copy of all_all Subscription under the Subscription container.
     *
     * @param subConn REMIND
     * @param subBase REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     * @throws CriticalException REMIND
     */
    public static void check(LDAPConnection subConn,
                             String subBase, Map<String, String> LDAPVars)
            throws SystemException {
        NamingEnumeration nenum = null;

        try {
            nenum = searchExistingAll(subConn, subBase, LDAPVars);
        } catch (NameNotFoundException nof) {
            throw new SubKnownException(SUB_LDAP_SUBNOTFOUND, subBase);
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne, ne.getMessage(), true);
        }

        int count = 0;

        try {
            while (nenum.hasMoreElements()) {
                nenum.next();
                count++;
            }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        }

        if (count > 1) {
            throw new CriticalException(LDAP_CONNECT_UPDATECONTAINER);
        }
    }

    private static NamingEnumeration searchExistingAll(LDAPConnection subConn,
                                                       String subBase, Map<String, String> LDAPVars)
            throws NamingException,
            SystemException {
        String searchStr = "(" + LDAPVars.get("TARGET_ALL") + "=true)";

        return subConn.search(searchStr, null, subBase, false);
    }

    private void getExistingAll()
            throws NamingException,
            SystemException {
        NamingEnumeration nenum = searchExistingAll(subConn, subBase, LDAPVars);
        existingAll.clear();

        while (nenum.hasMoreElements()) {
            SearchResult sr = (SearchResult) nenum.next();
            Subscription sub = new Subscription(TYPE_ALL, TYPE_ALL, ObjectManager.getSubDataSource(ldapCtx), false);
            sub.getDataSource()
                    .load(sr, sub);

            Vector chns = sub.getSortedChannels();
            existingAll.put((String) sr.getName(), sub);
        }
    }

    /**
     * Merge channels from lower level child containers to the parent
     *
     * @param all REMIND
     *
     * @throws SystemException REMIND
     */
    private void merge(String all)
            throws SystemException {
        Subscription toBeMerged = (Subscription) existingAll.get(all);
        createAll();

        for (Enumeration enumChannels = toBeMerged.getChannels(); enumChannels.hasMoreElements();) {
            Channel chn = (Channel) enumChannels.nextElement();
            Channel resChn = mergeResult.getChannel(chn.getUrl());

            if (resChn == null) {
                mergeResult.addChannel(chn);
                resultBuf.append("Channel ");
                resultBuf.append(chn);
                resultBuf.append(" added from ");
                resultBuf.append(toBeMerged.getTargetID());
                resultBuf.append("\n");
            } else {
                StringBuffer msg = new StringBuffer(1024);
                IConfig tunerConfig = (IConfig) main.getFeatures().getChild("tunerConfig");
                String value = tunerConfig.getProperty("marimba.subscription.raisedeleteprecedence");
                boolean precedence = ("true".equals(value)) ? true : false;
                if (ParserUtils.resolveConflict(resChn, chn, msg, precedence)) {
                    resultBuf.append(msg.toString());
                    resultBuf.append(", conflict won by target '");
                    resultBuf.append(toBeMerged.getTargetID());
                    resultBuf.append("' in " + all);
                    resultBuf.append("\n");
                    mergeResult.setChannel(chn);
                } else {
                    resultBuf.append("Maintained channel in merged result " + resChn);
                    resultBuf.append("\n");
                }
            }
        }

        mergeProperties(toBeMerged, PROP_TUNER_KEYWORD);
        mergeProperties(toBeMerged, PROP_SERVICE_KEYWORD);
        mergeProperties(toBeMerged, PROP_CHANNEL_KEYWORD);
        mergeProperties(toBeMerged, PROP_ALL_CHANNELS_KEYWORD);
        mergeProperties(toBeMerged, PROP_DEVICES_KEYWORD);
        mergeProperties(toBeMerged, PROP_POWER_KEYWORD);
        mergeProperties(toBeMerged, PROP_SECURITY_KEYWORD);
        mergeProperties(toBeMerged, PROP_SCAP_SECURITY_KEYWORD);
        mergeProperties(toBeMerged, PROP_USGCB_SECURITY_KEYWORD);
        mergeProperties(toBeMerged, PROP_CUSTOM_SECURITY_KEYWORD);
        mergeProperties(toBeMerged, PROP_AMT_KEYWORD);
        mergeProperties(toBeMerged, PRO_AMT_ALARMCLK_KEYWORD);
        mergeProperties(toBeMerged, PROP_OSM_KEYWORD);
        mergeProperties(toBeMerged, PROP_PBACKUP_KEYWORD);
    }

    /**
     * Creates a top level all object if there's none available
     *
     * @throws SystemException REMIND
     */
    private void createAll()
            throws SystemException {
        if (mergeResult == null) {
            mergeResult = new Subscription(TYPE_ALL, TYPE_ALL, ObjectManager.getSubDataSource(ldapCtx));
            mergeResultCreated = true;
        }
    }

    private void mergeProperties(Subscription toBeMerged,
                                 String type)
            throws SystemException {
        resultBuf.append("\n");

        for (Enumeration enumkeys = toBeMerged.getPropertyKeys(type); enumkeys.hasMoreElements();) {
            String key = (String) enumkeys.nextElement();
            String value = mergeResult.getProperty(type, key);

            if (PROP_TUNER_KEYWORD.equals(type)) {
                resultBuf.append("Tuner");
            } else if (PROP_SERVICE_KEYWORD.equals(type)) {
                resultBuf.append("Subscription Service");
            } else if (PROP_CHANNEL_KEYWORD.equals(type)) {
                resultBuf.append("Subscriber");
            } else if (PROP_ALL_CHANNELS_KEYWORD.equals(type)) {
                resultBuf.append("All channels");
            } else if (PROP_DEVICES_KEYWORD.equals(type)) {
                resultBuf.append("Devices");
            } else if (PROP_POWER_KEYWORD.equals(type)) {
                resultBuf.append("Power Setting");
            } else if (PROP_SECURITY_KEYWORD.equals(type)) {
                resultBuf.append("Desktop Security");
            } else if (PROP_SCAP_SECURITY_KEYWORD.equals(type)) {
                resultBuf.append("SCAP(Non-Windows) Security");
            } else if (PROP_USGCB_SECURITY_KEYWORD.equals(type)) {
                resultBuf.append("SCAP(Windows) Security");
            } else if (PROP_CUSTOM_SECURITY_KEYWORD.equals(type)) {
                resultBuf.append("Custom Security");
            } else if (PROP_AMT_KEYWORD.equals(type)) {
                resultBuf.append("Vpro Setting");
            } else if (PRO_AMT_ALARMCLK_KEYWORD.equals(type)) {
                resultBuf.append("vPro PC Alarm Clock Setting");
            } else if (PROP_OSM_KEYWORD.equals(type)) {
                resultBuf.append("OS Template Setting");
            } else if (PROP_PBACKUP_KEYWORD.equals(type)) {
                resultBuf.append("Personal Backup Setting");
            }

            if (value == null) {
                value = toBeMerged.getProperty(type, key);
                mergeResult.setProperty(type, key, value);
                resultBuf.append(" property added. ");
                resultBuf.append(key);
                resultBuf.append('=');
                resultBuf.append(value);
            } else {
                resultBuf.append(" property conflict ");
                resultBuf.append(key);
                resultBuf.append(".  Use value in merge result : ");
                resultBuf.append(value);
                resultBuf.append("\n");
            }
        }
    }

    /**
     * checks for the access token or creates one if one is not present
     *
     */
    public static void checkAccessToken(LDAPConnection subConn,
                                 String subBase, Map<String, String> LDAPVarsMap)
            throws SystemException {
        // create the access entry
        try {
            String filter = "(cn=" + LDAPVarsMap.get("CHECK_ACCESS_TOKEN") + ")";
            NamingEnumeration ne = subConn.search(filter, new String[]{"cn"}, subBase, true);
            while (ne.hasMore()) {
                return;
            }
            String dn = "cn=" + LDAPVarsMap.get("CHECK_ACCESS_TOKEN") + ", " + subBase;
            Vector attrs = new Vector();
            attrs.addElement("objectclass: top");
            attrs.addElement("objectclass: " + LDAPVarsMap.get("SUBSCRIPTION_CLASS"));
            attrs.addElement("cn: " + LDAPVarsMap.get("CHECK_ACCESS_TOKEN"));
            String[] attrStr = new String[attrs.size()];
            attrs.copyInto(attrStr);
            subConn.createObject(dn, attrStr, false);
        } catch (NameNotFoundException nof) {
        	if(DEBUG) {
                System.out.println("failed to create access token");
                nof.printStackTrace();
        	}
        } catch (NamingException ne) {
        	if(DEBUG) {
                System.out.println("failed to create access token");
                ne.printStackTrace();
        	}
        }
    }

}

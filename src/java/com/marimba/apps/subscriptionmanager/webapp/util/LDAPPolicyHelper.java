// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.objects.dao.*;


import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.tools.ldap.*;

/*  Helper class that obtains policy from LDAP
*   batches targets and run query when results are needed
*   adding or removing a target will results in the query being re-executed
*   otherwise a cashed result will be returned
*/
public class LDAPPolicyHelper implements ISubscriptionConstants {

    //key = target, value = type
    protected Map     targets = new HashMap();
    protected boolean isDirty = true;

    //key dn, value attributes
    protected HashMap        results;
    protected LDAPConnection conn;
    protected String         subBase;
    protected boolean browsingLDAP;
    protected String osTemplateName = null;   
    protected String dirType;
    protected Map<String, String> LDAPVarsMap;
    /**
     * Creates a new LDAPPolicyHelper object.
     *
     * @param conn REMIND
     * @param subBase REMIND
     */
    public LDAPPolicyHelper(LDAPConnection conn,
                            String subBase, boolean browsingLDAP, String dirType) {
        this.conn = conn;

        this.subBase = subBase;
        this.browsingLDAP = browsingLDAP;
        this.dirType = dirType;
        this.LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
    }

    /**
     * Adds the target to list of targets managed by this helper, Queries are lazy queries. Adding targets will not result in any ldap queries
     *
     * @param target target managed by subscription
     * @param targetType subscription target type
     *
     * @return previous value associated with specified dn, or <tt>null</tt> if there was no mapping for dn.
     */
    public Object addTarget(String target,
                            String targetType) {
        isDirty = true;

        return targets.put(target, targetType);
    }

    /**
     * Removes the target from the list of targets managed by this helper
     *
     * @param target whose mapping is to be removed from the map.
     *
     * @return previous targetType associated with specified target if there was no taregt a <tt>null</tt> is retuned
     */
    public Object removeTraget(String target) {
        isDirty = true;

        return targets.remove(target);
    }

    /**
     * Returns <tt>true</tt> there are any policies found for any of the targets managed by the helper
     *
     * @return <tt>true</tt> if any target has policies associated with it
     */
    public boolean hasPolicies() {
        runQuery();

        return !(targets.size() == 0);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Map getPolicies() {
        runQuery();

        return results;
    }

    /**
     * Returns <tt>true</tt> there are policies found for the given target
     *
     * @param dn dn to look up
     *
     * @return <tt>true</tt> if the target has a policy associated with it
     */
    public boolean hasPolicies(String dn) {
        runQuery();

        Attributes srAttrs = (Attributes) results.get(Utils.nomalize(dn));
        // no policies if no DN
        if (srAttrs == null) {
            return false;
        }

        Hashtable chnInitMap = null;

        boolean   hasAssignment = false;

        try {
//            chnInitMap = LDAPSubscription.loadChannelAttr(LDAPVarsMap.get("CHANNEL"), srAttrs);
//
//            for (Enumeration ite = chnInitMap.elements(); ite.hasMoreElements();) {
//                if (!LDAPSubscription.isNone((String) ite.nextElement())) {
//                    hasAssignment = true;
//
//                    break;
//                }
//            }
        	String[] securityTypes = {"scap", "usgcb", "custom"};
        	boolean isExistsPolicy = false;
            if (srAttrs.get(LDAPVarsMap.get("PROPERTY")) != null) {
                Attribute atr = srAttrs.get(LDAPVarsMap.get("PROPERTY"));
                int isSecurityPropertyAvailable = -1;
                for (Enumeration ite = atr.getAll(); ite.hasMoreElements();) {
                    String property = (String) ite.nextElement();
                    for(String securityType : securityTypes) {
	                    isSecurityPropertyAvailable = property.indexOf(","+securityType+"=");
	                    if(isSecurityPropertyAvailable != -1) {
	                    	isExistsPolicy = true;
	                    	continue;
	                    }
                    }
                }
            }
            if(isExistsPolicy) {
                hasAssignment = true;
            }
        } catch (NamingException ne) {
            if (IAppConstants.DEBUG) {
                ne.printStackTrace();
            }
        }

        return hasAssignment;
    }

    private void setOSTemplate(String templateName) {
        this.osTemplateName = templateName;
    }
    public String getOSTemplate() {
        return this.osTemplateName;
    }

    private String getSearchFilter() {
        StringBuffer filter = new StringBuffer(1024);

        filter.append("(|");

        Iterator it = targets.keySet()
                             .iterator();

        String dn;

        String targetType;

        while (it.hasNext()) {
            dn = (String) it.next();

            targetType = (String) targets.get(dn);

            if (ISubscriptionConstants.TYPE_ALL.equals(targetType)) {
                filter.append("(");
                filter.append(LDAPVarsMap.get("TARGET_ALL"));
                filter.append("=true)");
          } else  if (ISubscriptionConstants.GROUPS_EP.equals(targetType) ||
                    ISubscriptionConstants.USER_TXGROUP.equals(targetType) ) {
                    filter.append("(");
                    filter.append(LDAPVarsMap.get("TARGET_TX_GROUP"));
                    filter.append("=");
                    filter.append(LDAPSearchFilter.escapeComponentValue(dn.trim()) + ")");
            }  else  if (ISubscriptionConstants.PEOPLE_EP.equals(targetType) ) {
                    filter.append("(");
                    filter.append(LDAPVarsMap.get("TARGET_TX_USER"));
                    filter.append("=");
                    filter.append(LDAPSearchFilter.escapeComponentValue(dn.trim()) + ")");
             } else if (ISubscriptionConstants.TYPE_USERGROUP.equals(targetType) && !browsingLDAP) {
                 filter.append("(");
                 filter.append(LDAPVarsMap.get("TARGET_TX_GROUP"));
                 filter.append("=");
                 filter.append(LDAPSearchFilter.escapeComponentValue(dn.trim()) + ")");
             } else if (ISubscriptionConstants.TYPE_USER.equals(targetType) && !browsingLDAP) {
                 filter.append("(");
                 filter.append(LDAPVarsMap.get("TARGET_TX_USER"));
                 filter.append("=");
                 filter.append(LDAPSearchFilter.escapeComponentValue(dn.trim()) + ")");
             } else  if (ISubscriptionConstants.SITES_EP.equals(targetType) ||
                     ISubscriptionConstants.TYPE_SITE.equals(targetType) ) {
                 filter.append("(");
                 filter.append(LDAPVarsMap.get("SUBSCRIPTION_NAME"));
                 filter.append("=");
                 filter.append(LDAPSearchFilter.escapeComponentValue(dn.trim()) + ")");
             } else {
                filter.append("(");
                filter.append(LDAPVarsMap.get("TARGETDN"));
                filter.append("=");
                filter.append(LDAPSearchFilter.escapeComponentValue(dn.trim()));
                filter.append(")");
            }
        }

        filter.append(")");

        return filter.toString();
    }

    private void runQuery() {
        if (!isDirty) {
            return;
        }

        results = new HashMap();

        try {
            if (targets.size() == 0) {
                isDirty = false;

                return;
            }

            String searchFilter = getSearchFilter();
            NamingEnumeration nenum  = conn.search(searchFilter, LDAPUtils.getLDAPVarArr("policyAttributes", dirType), subBase, LDAPConstants.LDAP_SCOPE_SUBTREE);
            
            if (IAppConstants.DEBUG5) {
                System.out.println("Printing results LDAPPolicy");
            }

            String dn;

            Attributes srAttrs;

            if (nenum != null) {
                while (nenum.hasMoreElements()) {
                    SearchResult sr = (SearchResult) nenum.next();

                    if (IAppConstants.DEBUG5) {
                        System.out.println(sr);
                    }

                    srAttrs = sr.getAttributes();
                    dn = LDAPWebappUtils.getTargetDN(srAttrs, LDAPVarsMap);
                    // if dn is null it must be a all all or
                    if ( dn == null){

                    }
                    dn = LDAPSearchFilter.unEscapeComponentValue(dn);
                    dn = Utils.nomalize(dn);
                    if (IAppConstants.DEBUG5) {
                        System.out.println("unescaped " + dn);
                    }
                    results.put(dn, srAttrs);
                }
            }

            isDirty = false;
        } catch (NamingException ne) {
            if (IAppConstants.DEBUG) {
                ne.printStackTrace();
            }
        }
    }


}

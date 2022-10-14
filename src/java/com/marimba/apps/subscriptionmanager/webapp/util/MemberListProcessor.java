// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.*;
import javax.naming.directory.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.util.LDAPUtils;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IListProcessor;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;

import com.marimba.tools.ldap.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Used by setPagingResultsTag to obtain the member type when we page through members in a group, and search from LDAP.
 *
 * @author Theen-Theen Tan
 * @version 1.6, 11/21/2002
 */
public class MemberListProcessor
        implements IListProcessor {
    final static boolean DEBUG = IWebAppConstants.DEBUG;

    /**
     * REMIND
     *
     * @param listing REMIND
     * @param req REMIND
     * @param context REMIND
     *
     * @throws SystemException REMIND
     */
    public void process(List listing,
                        HttpServletRequest req,
                        ServletContext context)
            throws SystemException {

        SubscriptionMain main = TenantHelper.getTenantSubMain(context, req);
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(main.getDirType());
        String subBase = main.getSubBase();
        boolean usersInLDAP = main.getUsersInLDAP();

        String[] searchAttrs = {
                LDAPConstants.OBJECT_CLASS, LDAPVarsMap.get("CONTAINER_PREFIX"), LDAPVarsMap.get("CONTAINER_PREFIX2"), LDAPVarsMap.get("GROUP_PREFIX"), LDAPVarsMap.get("MACHINE_NAME"),
                LDAPVarsMap.get("USER_ID"), LDAPVarsMap.get("GROUP_DESCRIPTION"), LDAPConstants.DOMAIN_PREFIX
            };

        if ((listing != null) && (listing.size() > 0)) {
            // Now iterate again through the list of member dn's
            // and query LDAP for the objectclass of each one.
            int listLength = listing.size();
            LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(req), subBase, main.getUsersInLDAP(), main.getDirType());

            for (int i = 0; i < listLength; i++) {
                PropsBean entry = (PropsBean) listing.get(i);

                // the dn and objectclass are use to check validity (inCurrentEP)
                String dn = (String) entry.getValue("dn");
                String cn = dn;

                String objectclass = null;
                String objectClass = (String) entry.getValue(LDAPConstants.OBJECT_CLASS);

                if ( IWebAppConstants.INVALID_COLLECTION.equals(objectClass)
                        || IWebAppConstants.INVALID_LINK.equals(objectClass) || ISubscriptionConstants.MDM_TYPE_DEVICE_GROUP.equals(objectClass) || ISubscriptionConstants.TYPE_DEVICE.equals(objectClass)) {
                    continue;
                }

                LDAPConnection conn = main.getLDAPEnv()
                        .getGroupConn(dn, GUIUtils.getUser(req));

                // set appropriate attributes for each member
                try {
                    cn = LDAPWebappUtils.getSubConn(req).getParser().getCN(dn);
                    Attributes elementAttrs = conn.getAttributes((String) entry.getValue(LDAPVarsMap.get("DN")), searchAttrs);

                    if (elementAttrs.size() > 0) {
                        objectclass = LDAPUtils.getObjectClass(elementAttrs, LDAPVarsMap, main.getTenantName(), main.getChannel());
                    }

	                if (objectclass == null){
		                continue;
	                }

                    entry.setValue(LDAPConstants.OBJECT_CLASS, objectclass);
                    try {
                        String type = LDAPUtils.objClassToTargetType(objectclass, LDAPVarsMap);
                        if ( !usersInLDAP && ISubscriptionConstants.TYPE_USER.equals(type) ){
                            type = "user_inactive";
                        }
                        entry.setValue("type",type );

                    } catch(SubInternalException sie) { }
                    LDAPWebappUtils.setDisplayName(entry, elementAttrs, main.getDirType(), main.getTenantName(), main.getChannel());
                    LDAPWebappUtils.setTargetAbleProperty(entry, usersInLDAP, LDAPVarsMap.get("USER_CLASS"));
                    LDAPWebappUtils.setExpandAbleProperty(entry, LDAPVarsMap);
                    policyFinder.addTarget(dn, objectclass);

                } catch (PartialResultException pexception) {
                    // for active directory, if the entry is not resolved, we know
                    // it is located in another domain- thus, use external type
                    if (LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
                        objectclass = LDAPVarsMap.get("EXTERNAL_CLASS");
                        entry.setValue("type","external_target" );
                        entry.setProperty(LDAPConstants.DISPLAY_NAME, cn);
                    } else {
                        // we are using IPlanet and should throw a SystemException
                        if (DEBUG) {
                            System.out.println("LDAPBrowseGroup: caught PartialResultException in IPlanet");
                            pexception.printStackTrace();
                        }

                        LDAPUtils.classifyLDAPException(pexception);
                    }
                } catch (NamingException nexception) {
                    if (DEBUG) {
                        System.out.println("LDAPBrowseGroup: caught NamingException");
                        nexception.printStackTrace();
                    }

                    if (nexception instanceof NameNotFoundException) {
                        objectclass = IWebAppConstants.INVALID_NONEXIST;
                        entry.setValue("type","invalid_target" );
                        entry.setProperty(LDAPConstants.DISPLAY_NAME, cn);
                    } else {
                        LDAPUtils.classifyLDAPException(nexception);
                    }
                }

                // set the object class for the member entry
                entry.setValue(LDAPConstants.OBJECT_CLASS, objectclass);
            }
            LDAPWebappUtils.setDirectTargetProperty(listing, policyFinder, LDAPVarsMap);

        }
    }

    /**
     * REMIND
     *
     * @param req REMIND
     *
     * @return REMIND
     */
    public LDAPBean getLDAPBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        LDAPBean ldapBean = (LDAPBean) session.getAttribute(IWebAppConstants.SESSION_LDAP);

        if (null == ldapBean) {
            ldapBean = new LDAPBean();
        }

        return ldapBean;
    }
}

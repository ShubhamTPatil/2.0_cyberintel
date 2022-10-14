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

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IListProcessor;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;

import com.marimba.tools.ldap.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * Used by setPagingResultsTag to obtain the member isDirectTarget check type when we page through users/user groups sourced from the Tx.
 *
 * @author Theen-Theen Tan
 * @version 1.4, 11/21/2002
 */
public class TxMemberListProcessor
    implements IListProcessor {
    /**
     * REMIND
     *
     * @param listing REMIND
     * @param req REMIND
     * @param context REMIND
     *
     * @throws SystemException REMIND
     */
    public void process(List               listing,
                        HttpServletRequest req,
                        ServletContext     context)
        throws SystemException {

        SubscriptionMain main = TenantHelper.getTenantSubMain(context, req);
        String           subBase = LDAPWebappUtils.getSubBaseWithNamespace(req, main);

        if ((listing != null) && (listing.size() > 0)) {
            // Now iterate again through the list of member dn's
            // and query LDAP for the objectclass of each one.
            int              listLength = listing.size();
            String           type = ISubscriptionConstants.TYPE_USER;
            LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(req), subBase, main.getUsersInLDAP(), main.getDirType());
	        //CacheComplianceList cachedComplianceList = new CacheComplianceList();
            Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
            for (int i = 0; i < listLength; i++) {
                PropsBean entry = (PropsBean) listing.get(i);

                // set appropriate attributes for each member
                //determine if a subscription has been directly assigned to this
                boolean group = LDAPVarsMap.get("TARGET_TX_GROUP").equals(entry.getValue(LDAPConstants.OBJECT_CLASS));

                type = (group  ? ISubscriptionConstants.TYPE_USERGROUP : ISubscriptionConstants.PEOPLE_EP);
                policyFinder.addTarget((String) entry.getValue(ISubscriptionConstants.ESCAPED_NAME), type);
	            //cachedComplianceList.addTarget((String) entry.getValue(ISubscriptionConstants.ESCAPED_NAME));
            }

            LDAPWebappUtils.setDirectTargetProperty(listing, policyFinder, LDAPVarsMap);
        }
    }
}

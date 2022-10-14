// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IListProcessor;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Used by setPagingResultsTag to obtain the sites list from SBRP.
 *
 * @author   Tamilselvan Teivasekamani
 * @version  $Revision$,  $Date$
 *
 */

public class SiteListProcessor implements IListProcessor {

    public void process(List listing, HttpServletRequest req, ServletContext context) throws SystemException {

        SubscriptionMain main = TenantHelper.getTenantSubMain(context, req);
        String subBase = LDAPWebappUtils.getSubBaseWithNamespace(req, main);

        if ((listing != null) && (listing.size() > 0)) {
            // Now iterate again through the list of member dn's
            // and query LDAP for the objectclass of each one.
            int listLength = listing.size();
            String type = ISubscriptionConstants.TYPE_USER;
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

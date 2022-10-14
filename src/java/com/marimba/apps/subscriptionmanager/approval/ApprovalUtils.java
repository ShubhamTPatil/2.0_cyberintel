//Copyright 1996-2014, BMC Software Inc. All Rights Reserved.
//Confidential and Proprietary Information of BMC Software Inc.
//Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
//6,381,631, and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import javax.servlet.http.HttpServletRequest;
import com.marimba.intf.msf.*;
import com.marimba.tools.util.QuotedTokenizer;
import com.marimba.tools.config.ConfigProps;
import com.marimba.webapps.intf.*;

import java.util.List;
import java.util.Set;
import java.util.Vector;
/**
 * Peer Approval Utils
 *
 * @author	Selvaraj Jegatheesan
 * @version 	$Revision$, $Date$
 */
public class ApprovalUtils {
	public ApprovalUtils() {
		
	}
    private static boolean compareGroup(Set<String> ldapGroups, String approverGroup, SubscriptionMain main) {
        List<String> approverDNList = getApproverGroupDNList(approverGroup, main);
        debug("LDAP groups (" + ldapGroups.size() + "): " + ldapGroups);
        debug("Approver groups (" + approverDNList.size() + "): " + approverDNList);

        for (String approverDN: approverDNList) {
            approverDN = unescapeGroupName(approverDN);
            for (String ldapGroup : ldapGroups) {
                ldapGroup = unescapeGroupName(ldapGroup);
                if (approverDN.equalsIgnoreCase(ldapGroup)) {
                    debug("LDAP groups contains approver group: " + approverDN);
                    return true;
                }
            }
        }

        return false;
    }

    private static List<String> getApproverGroupList(String approverGroup) {
        // populate approver groups
        List<String> approverGroups = new Vector<String>(10);
        QuotedTokenizer tok = new QuotedTokenizer(approverGroup, ", ", '\\');

        while (tok.hasMoreTokens()) {
            approverGroups.add(tok.nextToken().trim());
        }
        return approverGroups;
    }

    private static List<String> getApproverGroupDNList(String approverGroupName, SubscriptionMain main) {
        List<String> approverGroupsDN = new Vector<String>(10);

        for (String anApproverGroup : getApproverGroupList(approverGroupName)) {
            try {
                approverGroupsDN.add(main.resolveGroupDN(anApproverGroup));
            } catch (com.marimba.webapps.intf.SystemException e) {
                debug("Failed to resolve approver group : " + anApproverGroup);
            }
        }
        return approverGroupsDN;
    }
    // user is member of approver group then get all policies and approve or reject policy except own policy
    // user is not member of approver group then get user policies and can't approve or reject any policy
    // This is user specific
    public static boolean hasApproverPermission(IUserPrincipal user, SubscriptionMain main) {
        boolean permissionStatus = false;
        try {
            ConfigProps config = main.getConfig();
            Set<String> ldapGroups = getGroupMemberships(user);
            permissionStatus = compareGroup(ldapGroups,  config.getProperty(IWebAppConstants.PEER_APPROVAL_TO_ADDRESS), main);
        } catch(Exception ex) {
            debug("Failed to find out Approver permission for the logged user");
        }
        return permissionStatus;
    }
    // user is member of approver group then get all policies and approve or reject policy except own policy
    // user is not member of approver group then get user policies and can't approve or reject any policy
    // This is httpservletrequest specific

    public static boolean hasApproverPermission(HttpServletRequest request, SubscriptionMain main) {
        boolean permissionStatus = false;
        try {
        	ConfigProps config = main.getConfig();
            Set<String> ldapGroups = getGroupMemberships(request);
            permissionStatus = compareGroup(ldapGroups,  config.getProperty(IWebAppConstants.PEER_APPROVAL_TO_ADDRESS), main);
        } catch(Exception ex) {
            debug("Failed to find out Approver permission for the logged user");
        }
        return permissionStatus;
    }
    // Retrieves the groups the user belongs to.  The set is String of DNs.
    private static Set<String> getGroupMemberships(IUserPrincipal user) {
        return user.getGroupMemberships();
    }

    // Retrieves the groups the user belongs to.  The set is String of DNs.
    private static Set<String> getGroupMemberships(HttpServletRequest request) {
        IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
        return user.getGroupMemberships();
    }

    public static String unescapeGroupName(String groupName) {
        if (groupName == null) {
            return null;
        }

        int i = 0;
        StringBuffer buf = new StringBuffer();

        while (i < groupName.length()) {
            char c = groupName.charAt(i++);

            if (c != '\\') {
                buf.append(c);
            } else {
                if (i < groupName.length()) {
                    buf.append(groupName.charAt(i++));
                }
            }
        }

        return buf.toString();
    }
    private static void debug(String msg) {
        if(IAppConstants.DEBUG5) System.out.println("ApprovalUtils : " + msg);
    }
}

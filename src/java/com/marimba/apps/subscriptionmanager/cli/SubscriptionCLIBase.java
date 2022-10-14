// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.MergeAllSub;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.tools.gui.StringResources;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPSearchFilter;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.logs.ILogConstants;
import com.marimba.intf.util.IProperty;
import com.marimba.io.FastInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

public class SubscriptionCLIBase implements ISubscriptionConstants, IAppConstants, IErrorConstants {
    private SubscriptionMain subsMain;
    private CLIUser cliUser;
    private StringResources resources;
    private Hashtable objectConversionMapping;
    private String msg;
    private TreeMap	         cliUsageGrps = new TreeMap();
    private StringBuffer commStr;
    
    public SubscriptionCLIBase() {
    	commStr   = new StringBuffer(2056);
		objectConversionMapping = new Hashtable();
		objectConversionMapping.put("machines", "computers");
		objectConversionMapping.put("computers", "machines");
		objectConversionMapping.put("people", "users");
		objectConversionMapping.put("users", "people");
	}
    public void setSubscriptionMain(SubscriptionMain subsMain) {
        this.subsMain = subsMain;
    }
    public void setCLIUser(CLIUser cliUser) {
        this.cliUser = cliUser;
    }
    public void setResources(StringResources  resources) {
    	this.resources = resources;
    }
    public void setNameSpace(String ns) {
        cliUser.setProperty(IUser.PROP_NAMESPACE, subsMain.upgradeNamespace(ns));
    }
    public void setMessage(String msg) {
    	this.msg = msg;
    }
    public String getMessage(){
    	return msg;
    }
    public void checkAclInit() throws SystemException {
        // will throw an exception if state of ACLs is not success
        subsMain.checkAclsInitialized();
    }
    /**
     * Resolves the target's DN for this subscription.  Only does the resolution if the targetID is not given as a DN. Expects targetName and targetType not be
     * null
     *
     * @param targetName REMIND
     * @param targetType REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public String resolveTargetDN(String targetName,
                           String targetType)
        throws SystemException {
        boolean useTxUsers = !subsMain.getUsersInLDAP();

        if (TYPE_EXTERNAL.equals(targetType) || TYPE_DOMAIN.equals(targetType)) {
            throw new SubKnownException(SUB_LDAP_NODNDOMAIN);
        }

        if (TYPE_ALL.equals(targetType) || TYPE_SITE.equals(targetType) || 
        		(useTxUsers && (TYPE_USER.equals(targetType) || TYPE_USERGROUP.equals(targetType)))) {
            // It is ok to not have a full DN for type All, or when
            // sourcing user/usergroup from the Transmitter
            return targetName;
        }

        String[] dns = resolveTargetDN2(targetName, targetType);

        if (dns == null) {
            // Checks whether any conversions has to be done for this object
            if (objectConversionMapping.containsKey(targetName)) {
                targetName = (String)objectConversionMapping.get(targetName);
                dns = resolveTargetDN2(targetName, targetType);
                if (dns == null) {
                    printMessage(resources.getString("cmdline.usednandtype"));
                	//setMessage(resources.getString("cmdline.usednandtype"));
                    throw new SubKnownException(SUB_LDAP_RESOLVETARGETDN, targetName);
                }
            }
            else {
                printMessage(resources.getString("cmdline.usednandtype"));
            	//setMessage(resources.getString("cmdline.usednandtype"));
                throw new SubKnownException(SUB_LDAP_RESOLVETARGETDN, targetName);
            }
        }

        // We disallow the access of subscription with
        // potentiall many target DN's under the same base.
        if (dns.length > 1) {
            throw new SubKnownException(SUB_LDAP_MULTIPLETARGETDN, targetName);
        }

        return dns [0];
    }

    /**
     * This method should not be called usergroup/users when sourcing user from Tx
     *
     * @param target REMIND
     * @param targetType REMIND
     *
     * @return REMIND
     *
     * @throws SubKnownException REMIND
     */
    private String[] resolveTargetDN2(String target,
                                      String targetType)
        throws SubKnownException {
        IProperty subConfig = subsMain.getSubscriptionConfig();
        IProperty mrbaConfig = subsMain.getMarimbaConfig();
        String dirType = subsMain.getDirType();
        // Shouldn't use DOMAINASDN
        String    base = subsMain.getLDAPConfig().getProperty(LDAPConstants.PROP_DOMAINASDN);
        String[]  dns = null;
        target = LDAPSearchFilter.escapeComponentValue(target);

        if ("true".equals(subsMain.getLDAPConfig().getProperty(LDAPConstants.PROP_NOAUTODISCOVER))) {
            base = subsMain.getLDAPConfig().getProperty(LDAPConstants.PROP_BASEDN);
        }

        try {
            if (targetType.equals(TYPE_MACHINE)) {
                String attr = subConfig.getProperty(LDAPConstants.CONFIG_MACHINENAMEATTR);
                String machineClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);
                String query = "(&(" + attr + "=" + target + ")"+LDAPUtils.createSearchFilter(LDAPUtils.getLDAPVarString("OBJECT_CLASS", dirType), machineClass)+")";
                dns = cliUser.getBrowseConn()
                             .searchAndReturnDNs(query, base, false);
            } else if (targetType.equals(TYPE_USER)) {
                String attr = subConfig.getProperty(LDAPConstants.CONFIG_USERIDATTR);
                String userClass = subConfig.getProperty(LDAPConstants.CONFIG_USERCLASS);
                String machineClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);
                String notMrbaMcClause = "(!(objectclass=" + machineClass + "))";
                String query = "(&(" + attr + "=" + target + ")(objectclass=" + userClass + ")" + notMrbaMcClause + ")";
                dns = cliUser.getBrowseConn()
                             .searchAndReturnDNs(query, base, false);
            } else if (targetType.equals(TYPE_CONTAINER)) {
                String query = "(&(cn=" + target + ")(|(objectclass=organizationalunit)(objectclass=container)))";
                dns = cliUser.getBrowseConn()
                             .searchAndReturnDNs(query, base, false);
                if(dns == null) {
                    query = "(&(ou=" + target + ")(|(objectclass=organizationalunit)(objectclass=container)))";
                    dns = cliUser.getBrowseConn()
                                 .searchAndReturnDNs(query, base, false);
                }
            } else if (targetType.equals(TYPE_MACHINEGROUP) || targetType.equals(TYPE_USERGROUP) || targetType.equals(TYPE_COLLECTION)) {
                String groupClass = subConfig.getProperty(LDAPConstants.CONFIG_GROUPCLASS);

                // group class can be comma separated list
                StringTokenizer st = new StringTokenizer(groupClass, ",");
                String          searchStr = "";
                int             items = 0;

                while (st.hasMoreTokens()) {
                    searchStr += ("(objectclass=" + st.nextToken() + ")");
                    items++;
                }

                if (items > 1) {
                    searchStr = "(|" + searchStr + ")";
                }

                String query = "(&(cn=" + target + ")" + searchStr + ")";
                dns = cliUser.getBrowseConn()
                             .searchAndReturnDNs(query, base, false);
            }
        } catch (LDAPException le) {
            throw new SubKnownException(SUB_LDAP_RESOLVETARGETDN, target);
        }

        return dns;
    }
    /**
     * Resolves the target's type for this Subscription. Expects the targetID to be full DN.
     *
     * @param targetID REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public String resolveTargetType(String targetID)
        throws SystemException {
        boolean  useTxUsers = !subsMain.getUsersInLDAP();
        String dirType = subsMain.getDirType();
    	Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        String[] searchAttrs = { LDAPVarsMap.get("DN"), LDAPVarsMap.get("OBJECT_CLASS") };

        // TargetType not given. The "machinegroup" type
        // that is only available for iPlanet, and the "usergroup" type
        // will be resolved to "group" type
        if (DEBUG) {
            System.out.print("Resolving Target Type " + targetID);
        }

        if (ISubscriptionConstants.TYPE_ALL.equals(targetID)) {
            return ISubscriptionConstants.TYPE_ALL;
        }

        String targetType = null;

        try {
            Attributes srAttrs = cliUser.getBrowseConn()
                                        .getAttributes(targetID, searchAttrs);
            String     objclass = LDAPUtils.getObjectClass(srAttrs, LDAPVarsMap, 
            		subsMain.getTenantName(), subsMain.getChannel());
            targetType = LDAPUtils.objClassToTargetType(objclass, LDAPVarsMap);

            // When we are sourcing users from transmitter,
            // if a targetDN has objclass group/groupofuniquenames/groupofnames
            // it is a machinegroup object.
            if (useTxUsers && TYPE_USERGROUP.equals(targetType)) {
                targetType = TYPE_MACHINEGROUP;
            }

            if (DEBUG) {
                System.out.print(" " + targetType);
                System.out.println(" " + new Boolean(useTxUsers));
            }
        } catch (NameNotFoundException se) {
            // The target has been deleted from LDAP.
            // User should provide both DN and type so we do not need to resolve the target
            // in order to operate on the Subscription
            printMessage(resources.getString("cmdline.usednandtype"));
            //setMessage(resources.getString("cmdline.usednandtype"));
            throw new SubKnownException(SUB_LDAP_TARGETDELETED, targetID);
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        }
        return targetType;
    }
    
 
        /**
     * Print the output string and append it for display later
     *
     * @param msg String to be displayed
     */
    private void printMessage(String msg) {
        commStr.append(msg);
        commStr.append("\n");
    }
    public String getOutputStr() {
        if(commStr != null) {
            return commStr.toString();
        } else {
            return "";
        }
    }
    public boolean isValidUrl(String s) {
      try {
		new URL(s);
      }
      catch(MalformedURLException malformedurlexception) {

		return false;
      }
      return true;
   }

}

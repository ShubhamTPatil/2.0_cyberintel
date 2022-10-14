// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.intf.plugin.IRequest;
import com.marimba.intf.util.IProperty;
import com.marimba.str.StrString;
import com.marimba.tools.ldap.*;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.SystemException;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.*;

/**
 * LDAP Utility methods used by the plugin.
 */
class PluginLDAPUtils
        implements ISubscriptionConstants,
                   LogConstants,
                   IPluginDebug,
                   IErrorConstants,
                   LDAPConstants {

    private ISubsPluginContext ctx;

    /**
     * Instantiate the data soure utilities.
     *
     * @param ctx Subscription Plugin context.
     */
    public PluginLDAPUtils(ISubsPluginContext ctx) {
        this.ctx = ctx;
        ctx.registerPluginLDAPUtils(this);
    }

    /**
     * This will obtain the user and machine data.  specifically, the following will be obtained for both the machine
     * and user. It will first obtain to obtain the dn from the request data. however, if that is not possible,
     * it will attempt to resolve the machine and user.
     *
     * @param request
     * @param subConfig
     * @param ldapCfg
     * @param taskDurationLogger REMIND
     * @param usersInLdap REMIND
     * @return String[] - a list of DNs for the user and machine
     * @throws SystemException    REMIND
     * @throws LDAPLocalException REMIND
     * @throws NamingException    REMIND
     * @throws LDAPException      REMIND
     */
    String[][] getUserAndMachineDNs(IRequest request,
                                    Props subConfig,
                                    Props mrbaConfig,
                                    IProperty ldapCfg,
                                    TaskDurationLogger taskDurationLogger,
                                    boolean usersInLdap,
                                    boolean resolveFromProfile)
            throws SystemException,
            LDAPLocalException,
            NamingException,
            LDAPException {

		String threadName = Thread.currentThread ().getName ();
		if (DETAILED_INFO) {
			System.out.println(threadName + " - User and Machine DN are getting identified");
		}

        //Attempt to get the user's fully qualified DN from the request.  This
        //will prevent the need to resolve the DN.
        Object profileObj = null;
        String userdn = null;
        String machinedn = null;
        String machineDomain = null;
        String userDomain = null;
        String[] userDNs = null;
        String[] machineDNs = null;
        String[][] results = new String[2][];
        results[0] = null;
        results[1] = null;

        float clientVersion = getClientVersion(request);
        boolean reporterRequest = isSubscriptionReporterRequest(request);

        if((clientVersion > 7.0) && reporterRequest){
            profileObj = request.getProfileAttribute(MACHINEDN_PROFILE_RPTR_ATTR);
        } else {
            profileObj = request.getProfileAttribute(MACHINEDN_PROFILE_ATTR);
        }

        boolean overideClientDN = "true".equals(subConfig.getProperty(LDAPConstants.CONFIG_OVERIDECLIENTDN));
        if ((profileObj != null) && ctx.getPluginLDAPEnv().getLdapConnUtils().isADWithAutoDiscovery(ldapCfg) && !overideClientDN) {
            machinedn = profileObj.toString();

            if (HIGH_PRIORITY_INFO) {
                ctx.logToConsole("PluginLDAPUtils: machine = " + machinedn);
            }

            // if sourcing from transmitter we only need the full DN for the machine
            // we dont need user information so return
            if (!usersInLdap && (machinedn != null)) {
                results[0] = new String[]{machinedn};
                return results;
            }
        }

        if (ctx.getPluginLDAPEnv().getLdapConnUtils().isADWithAutoDiscovery(ldapCfg)) {
            if((clientVersion > 7.0) && reporterRequest){
                profileObj = request.getProfileAttribute(USERDN_PROFILE_RPTR_ATTR);
            } else {
                profileObj = request.getProfileAttribute(USERDN_PROFILE_ATTR);
            }
            // get the userdn only if the sourcing users from AD
            // get userdn if we chose not to overide
            if ((profileObj != null) && usersInLdap &&
                    !overideClientDN) {
                userdn = profileObj.toString();

                if (HIGH_PRIORITY_INFO) {
                    ctx.logToConsole("PluginLDAPUtils: userdn = AD:" + userdn);
                }
            }

            // if the dn is null, then we need to figure it out before we query for
            // the full DN.  However, this will only be done if it is indicated in the
            // configuration object that we should resolve it.
            if ((userdn != null) && (machinedn != null)) {
                if (HIGH_PRIORITY_INFO) {
                    ctx.logToConsole("PluginLDAPUtils: able to obtain the userdn and machine dn without a query");
                    ctx.logToConsole("PluginLDAPUtils: -- userdn = " + userdn);
                    ctx.logToConsole("PluginLDAPUtils: -- machinedn = " + machinedn);
                }
                if(resolveFromProfile) {
                    results[0] = new String[]{machinedn};
                    results[1] = new String[]{userdn};
                    return results;
                } else {
                    // Assigning it to null and it can resolve the machine name
                    // instead of getting dn from the profile.
                    // This case will occur if the MachineDn from the profile is deleted in LDAP.
                    machinedn = null;
                    machineDomain = ldapCfg.getProperty(PROP_DOMAINPATH);
                }
            }

            // The prew2k*domain attributes will only be sent back by 5.1 clients
            // Needed to support user and client machine not in the same domain
            //
            // Multi-domain subscription only works in AD.
            // Since the service is not aware which Directory type we are in,
            // it would send up values for the domains.  We only use these
            // values if we are in AD.
            profileObj = request.getProfileAttribute(MACHINEDOMAIN_PROFILE_ATTR);

            if (profileObj != null) {
                machineDomain = profileObj.toString();
            }

            profileObj = request.getProfileAttribute(USERDOMAIN_PROFILE_ATTR);

            if (profileObj != null) {
                userDomain = profileObj.toString();
            }

            if (HIGH_PRIORITY_INFO) {
                ctx.logToConsole("PluginLDAPUtils: not able to obtain both userdn and machinedn");
                ctx.logToConsole("PluginLDAPUtils: -- userdn = " + userdn);
                ctx.logToConsole("PluginLDAPUtils: -- machinedn = " + machinedn);
            }

            // the domain could not be determined, look at the
            // ldapCfg property to see if i should
            // use the plugin as a default for resolving the DN
            if (usersInLdap && "true".equals(subConfig.getProperty(LDAPConstants.CONFIG_USEDNFROMCLIENTONLY))) {
                if (DETAILED_INFO) {
                    ctx.logToConsole("LDAPDatasourceUtils: true = " + LDAPConstants.CONFIG_USEDNFROMCLIENTONLY);
                }

                //resolution of the DN should not be done and the request should fail.
                String errString = "";

                if ((userdn == null) || (userDomain == null)) {
                    errString = "User DN and domain are not available.";
                }

                if ((machinedn == null) || (machineDomain == null)) {
                    errString = errString + "Machine DN and domain are not available.";
                }

                if (ERROR) {
                    ctx.logToConsole("PluginLDAPUtils Error string =" + errString);
                }

                ctx.log(LOG_ERROR_NO_DN_FROM_CLIENT, LOG_MAJOR, errString);
                throw new SystemException(PLUGIN_GET_DATA_FAILED);
            }

            // We have to determine if we should look up a dn.  The user is checked to be null
            // or not because it would be null if the transmitter isn't trusted.  In which case,
            // we would not need to do a domain resolution.
            // First establish which domain should be used.  we will try to use the same domain
            // for both if one if the DNs exist
            if (machinedn != null) {
                machineDomain = LDAPConnUtils.getDomainFromDN(ctx.getPluginLDAPEnv().
                                                                  getDefaultConnection(), machinedn);
            } else {
                if (machineDomain != null) {

                    taskDurationLogger.taskStarted(PluginTasks.RESOLVE_MACHINE_DOMAIN);
                    machineDomain = resolveDNSDomain(machineDomain, ldapCfg);
                    taskDurationLogger.taskStopped(PluginTasks.RESOLVE_MACHINE_DOMAIN);

                    if (HIGH_PRIORITY_INFO) {
                        ctx.logToConsole("PluginLDAPUtils machine domain: machine " + machineDomain);
                    }
                }
            }

            if (userdn != null) {
                userDomain = LDAPConnUtils.getDomainFromDN(ctx.getPluginLDAPEnv().getDefaultConnection(), userdn);
            } else {
                if (userDomain != null) {
                    taskDurationLogger.taskStarted(PluginTasks.RESOLVE_USER_DOMAIN);
                    userDomain = resolveDNSDomain(userDomain, ldapCfg);
                    taskDurationLogger.taskStopped(PluginTasks.RESOLVE_USER_DOMAIN);
                }

                // First attempt to use the machine domain, if it is not null.  We know that
                // if the machine domain is not null at this point that the user domain must
                // be null because of an earlier check for resolving DNs.
                if (machineDomain != null) {
                    if (userDomain == null) {
                        userDomain = machineDomain;
                    }

                    if (DETAILED_INFO) {
                        ctx.logToConsole("PluginLDAPUtils: setting userdomain to the machineDomain.");
                        ctx.logToConsole("   This should happen if userdn was null above");
                    }
                } else if (userDomain != null) {
                    machineDomain = userDomain;

                    if (DETAILED_INFO) {
                        ctx.logToConsole("PluginLDAPUtils: setting machinedomain to the userDomain.");
                        ctx.logToConsole("   This should happen if machinedn was null above");
                    }
                } else {
                    //they are both null and we should use the current domain
                    userDomain = ldapCfg.getProperty(PROP_DOMAINPATH);
                    machineDomain = ldapCfg.getProperty(PROP_DOMAINPATH);

                    if (INFO) {
                        ctx.logToConsole("PluginLDAPUtils: using the current domain of the plugin");
                    }
                }

                if (HIGH_PRIORITY_INFO) {
                    ctx.logToConsole("PluginLDAPUtils: machineDomain = " + machineDomain);
                    ctx.logToConsole("PluginLDAPUtils: userdomain = " + userDomain);
                }
            }
        }

        // Now we will have to resolve the machine and user DN if possible
        // obtain the user and machine name
        String user = null;
        if((clientVersion > 7.0) && reporterRequest){
            user = (String)request.getProfileAttribute(USERNAME_PROFILE_RPTR_ATTR);
        } else {
            user = request.getUserID();
        }

        String machineName = null;

        if((clientVersion > 7.0) && reporterRequest){
            profileObj = request.getProfileAttribute(MACHINENAME_PROFILE_RPTR_ATTR);
        } else {
            profileObj = request.getProfileAttribute(MACHINENAME_PROFILE_ATTR);
        }

        if (profileObj != null) {
            machineName = profileObj.toString()
                    .trim();
        }

        if (DETAILED_INFO) {
            ctx.logToConsole("PluginLDAPUtils: user from profile = " + user);
            ctx.logToConsole("PluginLDAPUtils: machineName from profile = " + machineName);
            ctx.logToConsole("PluginLDAPUtils: machineDomain = " + machineDomain);
            ctx.logToConsole("PluginLDAPUtils: userDomain = " + userDomain);
        }

        // We have determined which domain to use for resolution and perform the
        // query against the GC.  The GC is used so that user from cross domain
        // can be determined.  Please note that if there is more than one instance
        // of a user with the same common name within a domain, that all will be
        // returned in the listing of userDNs.  if this behavior is not desired,
        // then the customer must ensure that the endpoints send up their fqdn.
        // Additionally, the property of LDAPConstants.CONFIG_USEDNFROMCLIENTONLY must
        // be set to true

        if (usersInLdap) {
            if ((user != null) && (userdn == null)) {

                taskDurationLogger.taskStarted(PluginTasks.RESOLVE_USER);
                userDNs = getDNsfromGC("user", user, userDomain, ldapCfg, subConfig, mrbaConfig);
                taskDurationLogger.taskStopped(PluginTasks.RESOLVE_USER);

                if (INFO) {
                    ctx.logToConsole("PluginLDAPUtils: userdn list found = " + userDNs);
                }
            } else if (userdn != null) {
                userDNs = new String[1];
                userDNs[0] = userdn;
            } else {
                if (WARNING) {
                    ctx.logToConsole("PluginLDAPUtils: no userdns found");
                }

                userDNs = null;
            }
        }

        // Now determine what the machine distinguished name should be.
        if ((machineName != null) && (machinedn == null)) {
            //at this point, it is guaranteed that the userDomain is not null,
            //so we set the machine domain to whatever the user domain is
            taskDurationLogger.taskStarted(PluginTasks.RESOLVE_MACHINE);
            machineDNs = getDNsfromGC("machine", machineName, machineDomain, ldapCfg, subConfig, mrbaConfig);
            taskDurationLogger.taskStopped(PluginTasks.RESOLVE_MACHINE);
        } else if (machinedn != null) {
            machineDNs = new String[1];
            machineDNs[0] = machinedn;
        } else {
            if (WARNING) {
                ctx.logToConsole("PluginLDAPUtils: no machinedns found");
            }

            machineDNs = null;
        }

        int arrayLength = 0;

        if (DETAILED_INFO) {
            if (machineDNs != null) {
                ctx.logToConsole("PluginLDAPUtils: machineDNs length = " + machineDNs.length);
            }

            if (userDNs != null) {
                ctx.logToConsole("PluginLDAPUtils: userDNs length = " + userDNs.length);
            }
        }

        if (machineDNs != null) {
            arrayLength += machineDNs.length;
        }

        if (userDNs != null) {
            arrayLength += userDNs.length;
        }

        if (arrayLength != 0) {
            if (machineDNs != null) {
                results[0] = machineDNs;
            }

            if (userDNs != null) {
                results[1] = userDNs;
            }
        } else {
		// if machinename is null do not error it the firstpass of the double update
  	        if ( machineName != null){
                  String src = request.getRemoteAddress()
                          .getHostAddress();
                  if (ctx.getPluginLDAPEnv().getLdapConnUtils().isADWithAutoDiscovery(ldapCfg)) {
                      String clientInfo = user + " , " + machineName + " , " + userDomain + " , " + machineDomain;
                      ctx.log(LOG_ERROR_USER_AND_MACHINE_NOT_FOUND_AD, LOG_MAJOR, src, "", clientInfo);
                  } else {
                      String clientInfo = user + " , " + machineName;
                      ctx.log(LOG_ERROR_USER_AND_MACHINE_NOT_FOUND_GEN, LOG_MAJOR, src, "", clientInfo);
                  }
  	        }

            return null;
        }

        return results;
    }

    /**
     * Looks into the partitions directory on in the Configuration Context of a local DC to obtain
     * a Win9x or NT domain name into DNS format, or if it is a Unix end point, verify that the
     * domain sent up is an AD domain.
     *
     * @param netbiosname ntdomain is the Win9x or NT domain (e.g. mixed), or a DNS format domain name
     *                    from a Unix client
     * @return the DNS format of the domain (e.g. mixed.qa.marimba.com)
     */
    private String resolveDNSDomain(String netbiosname, IProperty ldapCfg) throws SystemException, LDAPLocalException, LDAPException, NamingException {

        LDAPConnection dcDir = ctx.getPluginLDAPEnv().getConnection(TYPE_DC, ldapCfg.getProperty(PROP_DOMAINPATH));
        String configNamingContext = ctx.getPluginLDAPEnv().getLdapConnUtils().getConfigNamingContext(dcDir);
        String searchBase = LDAPConstants.AD_PARTITIONS_PREFIX + configNamingContext;
        String filter = null;
        String[] dnsRoot = null;

        // If the service is a Unix machine, a DNS format will be sent up.
        // We still need to check whether the domain is valid in AD.
        // Otherwise we return null, and the calling code will use the
        // Plugin's current domain.
        if (netbiosname.indexOf('.') != -1) {
            filter = LDAPConstants.AD_DNSROOT + "=" + LDAPSearchFilter.escapeComponentValue(netbiosname);
            dnsRoot = dcDir.searchAndReturnDNsWrapper(filter, searchBase, false);
            if (dnsRoot == null) {
                return null;
            } else {
                return netbiosname;
            }
        }

        filter = LDAPConstants.AD_NETBIOSNAME + "=" + LDAPSearchFilter.escapeComponentValue(netbiosname);
        dnsRoot = dcDir.searchAndList(filter, AD_DNSROOT, null, false, searchBase, false);

        if (dnsRoot.length == 0) {
            ctx.log(LOG_ERROR_LDAP_NODOMAINDNS, LOG_AUDIT, netbiosname);

            // In the case of a work group, the workgroup's name will be sent up but we will not be able to
            // find the workgroup in AD.  We return null, and subsequent logic will set the search domain to
            // the plugin's current domain
            return null;
        } else {
            return dnsRoot[0];
        }
    }

    /**
     * This obtains the DN listing for the specific name and the domain specified.  It is used to resolve the user and machine names that were not sent up from
     * the client endpoint. A connection is made to the gC so that user and machines from across the forest can obtain their policies.
     *
     * @param type      this is either "user" or "machine".  It is used to determine the attribute     and object class that will need to be used for searching
     * @param name      This is the name of the user or machine.
     * @param domain    This is the domain that should be used as the search base. Input in DNS format.  The PROP_BASEDN value is used as the search base if we
     *                  are in Netscape
     * @param ldapCfg   This contains the credentials for the ldap configuration
     * @param subConfig This contains which attribute and class should be used during the query
     * @return REMIND
     * @throws LDAPLocalException REMIND
     * @throws NamingException    REMIND
     * @throws LDAPException      REMIND
     */
    private String[] getDNsfromGC(String type,
                          String name,
                          String domain,
                          IProperty ldapCfg,
                          Props subConfig,
                          Props mrbaConfig)
            throws LDAPLocalException,
            NamingException,
            LDAPException {
        LDAPConnection dir = ctx.getPluginLDAPEnv().getConnection(TYPE_GC, domain);

        //Now look up the filter that should be used
        String attr = LDAPConstants.CONFIG_MACHINENAMEATTR;
        String lclass = LDAPConstants.CONFIG_MACHINECLASS;

        if ("user".equals(type)) {
            attr = LDAPConstants.CONFIG_USERIDATTR;
            lclass = LDAPConstants.CONFIG_USERCLASS;
        }

        StringBuffer filter = new StringBuffer();
        filter.append("(&");
        filter.append("(").append(subConfig.getProperty(attr)).append("=").append(LDAPSearchFilter.escapeComponentValue(name)).append(")");
        filter.append(getObjectFilter(type, ldapCfg, mrbaConfig, subConfig));
        filter.append(getMrbaComputerFilter(type, ldapCfg, mrbaConfig));
        filter.append(")");

        if (ctx.getPluginLDAPEnv().getLdapConnUtils().isADWithAutoDiscovery(ldapCfg)) {
            domain = LDAPConnUtils.getDNFromDomain(domain);
        } else {
            domain = ldapCfg.getProperty(PROP_BASEDN);
        }

        if (DETAILED_INFO) {
            ctx.logToConsole("PluginLDAPUtils: getDNsFromGC search base  = " + domain);
        }

        return dir.searchAndReturnDNs(filter.toString(), domain, false);
    }

    private String getObjectFilter(String type, IProperty ldapCfg, Props mrbaConfig, Props subConfig) {
        StringBuffer searchFilter = new StringBuffer();
        if ("machine".equals(type)) {
            searchFilter.append(getMachineFilter(mrbaConfig, ldapCfg));
        } else if ("user".equals(type)) {
            searchFilter.append(getUserFilter(subConfig, ldapCfg));
        }
        return searchFilter.toString();
    }

    private String getMrbaComputerFilter(String type, IProperty ldapCfg, Props mrbaConfig) {
        StringBuffer mrbaCompFilter = new StringBuffer();
        if(!VENDOR_NS.equals(ldapCfg.getProperty(PROP_VENDOR))) {
            if("user".equals(type)) {
                mrbaCompFilter.append("(!");
                mrbaCompFilter.append(getMachineFilter(mrbaConfig, ldapCfg));
                mrbaCompFilter.append(")");
            }
        }
        return mrbaCompFilter.toString();
    }

    private String getMachineFilter(Props mrbaConfig, IProperty ldapCfg){
        // Ex:- (objectclass=computer)
        String machineClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);
        return LDAPUtils.createSearchFilter(LDAPUtils.getLDAPVarString("OBJECT_CLASS", ldapCfg.getProperty(LDAPConstants.PROP_VENDOR)), machineClass);
    }

    private String getUserFilter(Props subConfig, IProperty ldapCfg){
        // Ex:- (objectclass=user)
    	String userClass = subConfig.getProperty(LDAPConstants.CONFIG_USERCLASS);
    	return LDAPUtils.createSearchFilter(LDAPUtils.getLDAPVarString("OBJECT_CLASS", ldapCfg.getProperty(LDAPConstants.PROP_VENDOR)), userClass);
    }

    String resolveTargetType(String targetID, String dirType) {
    	Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        String[] searchAttrs = { LDAPVarsMap.get("DN"), LDAPVarsMap.get("OBJECT_CLASS") };

        // TargetType not given. The "machinegroup" type
        // that is only available for iPlanet, and the "usergroup" type
        // will be resolved to "group" type
        if (INFO) {
            ctx.logToConsole("Resolving Target Type for a DN from Subscription Service " + targetID);
        }

        if (ISubscriptionConstants.TYPE_ALL.equals(targetID)) {
            return ISubscriptionConstants.TYPE_ALL;
        }

        String targetType = null;

        try {
            Attributes srAttrs = ctx.getPluginLDAPEnv().getDefaultConnection().getAttributes(targetID, searchAttrs);
            String     objclass = LDAPUtils.getObjectClass(srAttrs, LDAPVarsMap, null, null);
            targetType = LDAPUtils.objClassToTargetType(objclass, LDAPVarsMap);

            if (DETAILED_INFO) {
                ctx.logToConsole(" " + targetType);
            }
        } catch (NameNotFoundException se) {
            // The target has been deleted from LDAP.
            // User should provide both DN and type so we do not need to resolve the target
            // in order to operate on the Subscription
            if(WARNING) {
                ctx.logToConsole("target " + targetID + " is not found in LDAP. " + se.getMessage());
            }
        } catch (Exception ne) {
            if(ERROR) {
                ctx.logToConsole("target " + targetID + " object class retrieval failed. " + ne.getMessage());
            }
        }

        return targetType;
    }

    String getCanonicalName(String dn) {
        try {
            return ctx.getPluginLDAPEnv().getDefaultConnection().getParser().getCanonicalName(dn);
        } catch (NamingException ex) {
	    // Deals with sourcing users from Tx
            return dn.toLowerCase();
        }
    }

    /**
     * Read attribute values into a Hashtable with channel url as the key.
     *
     * @param srAttrs Atributes
     * @param attrname Attribute name
     * @return Hashtable loaded with attributes
     * @throws NamingException
     */
    HashMap readAttrtoHashMap(Attributes srAttrs,
                              String attrname)
            throws NamingException {
        HashMap map = new HashMap(10);
        Attribute attr = srAttrs.get(attrname);

        if (attr != null) {
            NamingEnumeration allValues = attr.getAll();

            while (allValues.hasMoreElements()) {
                Object value = allValues.next();

                if (value instanceof String) {
                    String strValue = (String) value;
                    int ind = strValue.indexOf("=");

                    if (ind != -1) {
                        String url = strValue.substring(0, ind);
                        String val = strValue.substring(ind + 1);
                        map.put(new StrString(url), new StrString(val));
                    }
                }
            }
        }

        return map;
    }

    List getValue(Attributes srAttrs,
                    String name)
            throws NamingException {

		String threadName = Thread.currentThread().getName();
		if (DETAILED_INFO) {
			ctx.logToConsole("[PluginLDAPUtils.getValue()]" + threadName + ", User and Machine DN are getting identified");
		}

        Attribute nameAttrs = srAttrs.get(name);
        List retvalue = new ArrayList();

        if (nameAttrs != null) {
            NamingEnumeration allValues = nameAttrs.getAll();

            while (allValues.hasMoreElements()) {
                Object value = allValues.next();

                if (value instanceof String) {
                    if (!"".equals(value)) {
                        retvalue.add(value);
                    }
                }
            }
        }

        return retvalue;
    }

    public boolean isSubscriptionReporterRequest(IRequest request) {
        Object subsReporterReq = request.getProfileAttribute(SUBSCRIPTION_REPORTER_REQUEST);
        return ("true".equals(subsReporterReq)) ? true : false;
    }

    public float getClientVersion(IRequest request) {
        // Boolean to indicate that the Subscription Service version is 5.0 or above
        // The schedule info and secondary state is not stored in
        // the config.xml if version of Subscription Service requesting an update
        // is less than 5.0
        Object profileObj = request.getProfileAttribute(SERVICE_VERSION_PROFILE_ATTR);

        if (profileObj != null) {
            String vers = profileObj.toString();

            if (vers != null) {
                int mjrInd = vers.indexOf(".");
                int mnrInd = vers.indexOf(".", mjrInd+1);
                if (mjrInd != -1) {
                    if(mnrInd != -1){
                        return parseVersionVal(vers, mnrInd);
                    } else if(mjrInd < vers.length()-1){
                        return parseVersionVal(vers, vers.length());
                    }
                    return parseVersionVal(vers, mjrInd);
                }
            }
        }
        return 4;
    }

    public String getStrClientVersion(IRequest request) {
        String version = "";
        Object profileObj = request.getProfileAttribute(SERVICE_VERSION_PROFILE_ATTR);

        if (profileObj != null) {
            version = profileObj.toString();
        }

        return version;
    }

    private float parseVersionVal(String value, int ind){
        if(value != null && ind != -1){
            String version = value.substring(0, ind);
            float m = Float.parseFloat(version);
            return m;
        }
        return -1f;
    }
}

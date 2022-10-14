// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.util.PluginUtils;
import com.marimba.intf.castanet.IFile;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.util.Props;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Loads the published plugin properties.
 *
 * @version $Revision$, $Date$
 */
class PublishedProperties
        implements IProperty,
                   LDAPConstants,
                   LogConstants,
                   IPluginDebug,
                   ISubscriptionConstants {

    private ISubsPluginContext ctx;

    /**
     * These are the channel properties that are published with the plugin.
     */
    IProperty publishedProps;

    PublishedProperties(ISubsPluginContext ctx) {
        this.ctx = ctx;
        publishedProps = ctx.getPluginContext().getChannelProperties();
        ctx.registerPublishedPluginProps(this);
    }

    /**
     * Read the LDAP server properties for initialization LDAP.
     *
     * @return LDAP properties
     */
    Props readLDAPServerProps() {

        //Temporary variable that store the published attributes for connecting
        //to the ldap server.
        // These variable are stored using different constants depending on if the
        // were published or were specified in the mapping file.
        Props ldapCfg = new Props();

        // Only read the LDAP mapping file if we are not AD autodisover mode
        // Name of the matching Transmitter entry from the mapping file
        HostInfo hostInfo = new HostInfo();
        findHostName(hostInfo);

        // Parse the ldapservers.txt Tx/LDAP Servers mapping file
        IFile mapfile = ctx.getPluginContext().getChannelFile("ldapservers.txt");


        if (mapfile != null) {
            if(!validateMappingFile(mapfile)) {
                return null;
            }

            // Load the Ldap Server mapping file into a Props object
            Props pser = new Props(mapfile, -1);

            if (!pser.load()) {
                ctx.log(LOG_ERROR_READING_MAP_FILE, LOG_MAJOR);

                return null;
            }

            String[] pairs = pser.getPropertyPairs();

            parseServerMappings(pairs, hostInfo, ldapCfg);
            ctx.log(LOG_MAPPED_TX_NAME, LOG_AUDIT, hostInfo.getTxfound());
        }

        Props propsobj = trimProps(publishedProps);
        overrideWithPubPluginProps(propsobj, ldapCfg, hostInfo);
        return ldapCfg;
    }

    private void overrideWithPubPluginProps(Props propsobj, Props ldapCfg, HostInfo hostInfo) {
        boolean autoDiscover = !"true".equals(propsobj.getProperty("subscriptionplugin." + LDAPConstants.PROP_NOAUTODISCOVER));
        // By default set the Vendor and the NOAUTODISCOVER properties.
        // This is so that each mapping won't need to set them.
        if (LDAPConstants.VENDOR_AD.equals(propsobj.getProperty("subscriptionplugin.vendor")) && !autoDiscover) {
             ldapCfg.setProperty(LDAPConstants.PROP_NOAUTODISCOVER, propsobj.getProperty("subscriptionplugin.noautodiscover"));
         }

        // If no Transmitter is found in the mapping file which corresponds
        // to the Transmitter on which this plugin is running or no LDAP
        // Server hostname is specified for this Transmitter then use the
        // Default LDAP Server name and bind params from the published Plugin properties

        if (hostInfo.getTxfound() == null || (hostInfo.getHost() == null) || "".equals(hostInfo.getHost().trim())) {
            //Now set it into the ldapCfg that gets passed into the ldap environment.  These are the
            //properties that got set during publish time

            if (LDAPConstants.VENDOR_AD.equals(propsobj.getProperty("subscriptionplugin.vendor")) && autoDiscover) {
                // Used by AD with auto-discovery
                ldapCfg.setProperty(LDAPConstants.PROP_USERRDN, propsobj.getProperty("subscriptionplugin.binddn"));
                ldapCfg.setProperty(LDAPConstants.PROP_PASSWORD, propsobj.getProperty("subscriptionplugin.bindpasswd"));
                ldapCfg.setProperty(LDAPConstants.PROP_USESSL, propsobj.getProperty("subscriptionplugin.usessl"));
                ldapCfg.setProperty(LDAPConstants.PROP_AUTHMETHOD, propsobj.getProperty("subscriptionplugin.authmethod"));
                ldapCfg.setProperty(LDAPConstants.PROP_POOLSIZE, propsobj.getProperty("subscriptionplugin.poolsize"));
                ldapCfg.setProperty(LDAPConstants.PROP_LASTGOODHOST_EXPTIME, propsobj.getProperty("subscriptionplugin.lastgoodhostexptime"));
                ldapCfg.setProperty(LDAPConstants.PROP_ALLOW_PROVISION, propsobj.getProperty(ISubscriptionConstants.PROVISION_ALLOW));
                ldapCfg.setProperty(LDAPConstants.PROP_AD_DOMAIN, propsobj.getProperty("subscriptionplugin.admanagementdomain"));
                ldapCfg.setProperty(LDAPConstants.PROP_AD_DNSSERVER, propsobj.getProperty("subscriptionplugin.srvdnsserver"));
                ldapCfg.setProperty(LDAPConstants.PROP_CONN_TIMEOUT, propsobj.getProperty("subscriptionplugin.connectiontimeout"));
                ldapCfg.setProperty(LDAPConstants.PROP_QUERY_TIMEOUT, propsobj.getProperty("subscriptionplugin.querytimeout"));
                ldapCfg.setProperty(LDAPConstants.PROP_AD_PREFERREDDCS, propsobj.getProperty("subscriptionplugin.preferreddcs"));
                ldapCfg.setProperty(LDAPConstants.PROP_AD_SITE, propsobj.getProperty("subscriptionplugin.adsite"));
            } else {
                // Used by both AD with no autodiscovery and iPlanet
                ldapCfg.setProperty(LDAPConstants.PROP_HOST, propsobj.getProperty("subscriptionplugin.ldaphost"));
                ldapCfg.setProperty(LDAPConstants.PROP_BASEDN, propsobj.getProperty("subscriptionplugin.basedn"));
                ldapCfg.setProperty(LDAPConstants.PROP_USERRDN, propsobj.getProperty("subscriptionplugin.binddn"));
                ldapCfg.setProperty(LDAPConstants.PROP_PASSWORD, propsobj.getProperty("subscriptionplugin.bindpasswd"));
                ldapCfg.setProperty(LDAPConstants.PROP_USESSL, propsobj.getProperty("subscriptionplugin.usessl"));
                ldapCfg.setProperty(LDAPConstants.PROP_AUTHMETHOD, propsobj.getProperty("subscriptionplugin.authmethod"));
                ldapCfg.setProperty(LDAPConstants.PROP_POOLSIZE, propsobj.getProperty("subscriptionplugin.poolsize"));
                ldapCfg.setProperty(LDAPConstants.PROP_LASTGOODHOST_EXPTIME, propsobj.getProperty("subscriptionplugin.lastgoodhostexptime"));
                ldapCfg.setProperty(LDAPConstants.PROP_ALLOW_PROVISION, propsobj.getProperty(ISubscriptionConstants.PROVISION_ALLOW));
            }
        }

        //only one vendor can be set for all of the repeaters in the system. This means that
        //the property published will be the only one used.
        ldapCfg.setProperty(LDAPConstants.PROP_VENDOR, propsobj.getProperty("subscriptionplugin.vendor"));
        ldapCfg.setProperty(LDAPConstants.CONFIG_SCHEMA_VERSION, propsobj.getProperty("subscriptionplugin.configSchemaVersion"));
        if (DETAILED_INFO) {
            ctx.logToConsole("LDAP Config read from ldapservers.txt file or plugin properties");
            LDAPUtils.printLDAPConfig(ldapCfg);
        }
    }

    private void findHostName(HostInfo hostInfo) {
        // Find the Transmitter name so that the mapping can be determined
        try {

            InetAddress addr = InetAddress.getLocalHost();
            hostInfo.setDnshostname(addr.getHostName());

            // Due to JDK bug 4148388 the host name returned by
            // getHostName is not fully qualified.
            // Therefore strip off the domain for all platforms to
            // make this consistent
            int i = hostInfo.getDnshostname().indexOf('.');
            String hostname = (i == -1) ? hostInfo.getDnshostname() : hostInfo.getDnshostname().substring(0, i);
            hostInfo.setHostname(hostname);

        } catch (UnknownHostException e) {
            hostInfo.setHostname("unknown_host");
            hostInfo.setDnshostname("unknown_host");
        }
    }


    /**
     * Search for matching Transmitter name in the Ldap mapping file. To return values conveniantly
     * from this method, HostInfo inner class is used.
     */
    private void parseServerMappings(String pairs[], HostInfo hostInfo, Props ldapCfg) {
        for (int i = 0; i < pairs.length; i += 2) {
            String key = pairs[i].trim();
            String value = null;

            if (pairs[i + 1] != null) {
                value = pairs[i + 1].trim();
            }

            int ind = key.indexOf(PROP_DELIM);
            String txname = key.substring(0, ind)
                    .trim();

            // Compare the Transmitter name/IP address of the host machine
            // with the entries in the mapping file to seacrh
            // a matching entry
           try {
               if (hostInfo.getTxfound() == null) {
                   //to fix the defect 45401  addtional check added to see whether the repeater has more than one ipaddress
                   InetAddress[] ipAddresses = InetAddress.getAllByName(hostInfo.getHostname());
                   for ( int count =0; count < ipAddresses.length ; count++) {
                        String address = ipAddresses[count].getHostAddress();
                        if(txname.equals(address)) {
                            hostInfo.setTxfound(txname);
                        }
                   }
               }
           } catch (UnknownHostException e) {
              if(ERROR) {
                  ctx.logToConsole("Not able to retrieve the Ipaddresses of the repeater", e);
              }
           }

            if (hostInfo.getTxfound() == null) {
                if (txname.equalsIgnoreCase(hostInfo.getDnshostname()) || txname.equalsIgnoreCase(hostInfo.getHostname())) {
                    hostInfo.setTxfound(txname);
                } else {
                    continue;
                }
            }

            // Get the attributes for the matching Transmitter
            // such as basedn , binddn , password, usessl and authentication type(simple or kerberos).
            //The keys are defined in ISubscriptionConstants and are the properties
            //used in the mapping file to define the appropriate connection
            //values
            if (hostInfo.getTxfound().equalsIgnoreCase(txname)) {
                String lprop = key.substring(ind + 1)
                        .trim();

                if (SERVER_KEY.equalsIgnoreCase(lprop)) {
                    hostInfo.setHost(value);
                    ldapCfg.setProperty(LDAPConstants.PROP_HOST, value);
                } else if (BASEDN_KEY.equalsIgnoreCase(lprop)) {
                    ldapCfg.setProperty(LDAPConstants.PROP_BASEDN, value);
                } else if (BINDDN_KEY.equalsIgnoreCase(lprop)) {
                    ldapCfg.setProperty(LDAPConstants.PROP_USERRDN, value);
                } else if (PASSWD_KEY.equalsIgnoreCase(lprop)) {
                    ldapCfg.setProperty(LDAPConstants.PROP_PASSWORD, value);
                } else if (USESSL_KEY.equalsIgnoreCase(lprop)) {
                    ldapCfg.setProperty(LDAPConstants.PROP_USESSL, value);
                } else if (AUTHMETH_KEY.equals(lprop)) {
                    ldapCfg.setProperty(LDAPConstants.PROP_AUTHMETHOD, value);
                } else if (POOLSIZE_KEY.equals(lprop)) {
                    ldapCfg.setProperty(LDAPConstants.PROP_POOLSIZE, value);
                } else if (NOAUTODISCOVER_KEY.equalsIgnoreCase(lprop)) {
                    ldapCfg.setProperty(LDAPConstants.PROP_NOAUTODISCOVER, value);
                }
            }
        }
    }

    private boolean validateMappingFile(IFile mapfile) {
        PluginUtils putils = new PluginUtils();

        try {
            // Validate the syntax of the Server mapping file
            putils.validateLDAPMappingFile(mapfile.getInputStream());

            if (putils.errorCode != PluginUtils.VALID_FILE) {

                if (putils.errorCode == PluginUtils.INVALID_TX_NAME) {
                    String err = putils.errorLineNumber + " : " + putils.errorLine;
                    ctx.log(LOG_ERROR_INVALID_TX_NAME, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.INVALID_SERVER) {
                    String err = putils.errorLineNumber + " : " + putils.errorLine;
                    ctx.log(LOG_ERROR_INVALID_SERVER, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.INVALID_USESSL) {
                    String err = putils.errorLineNumber + " : " + putils.errorLine;
                    ctx.log(LOG_ERROR_INVALID_USESSL, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.INVALID_AUTHMETH) {
                    String err = putils.errorLineNumber + " : " + putils.errorLine;
                    ctx.log(LOG_ERROR_INVALID_AUTHTYPE, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.INVALID_POOLSIZE) {
                    String err = putils.errorLineNumber + " : " + putils.errorLine;
                    ctx.log(LOG_ERROR_INVALID_POOLSIZE, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.INVALID_KEY) {
                    String err = putils.errorLineNumber + " : " + putils.errorLine;
                    ctx.log(LOG_ERROR_INVALID_KEY, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.DUPLICATE_KEY) {
                    String err = putils.errorLineNumber + " : " + putils.errorLine;
                    ctx.log(LOG_ERROR_DUPLICATE_KEY, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.MISSING_BASEDN) {
                    String err = putils.errorLine;
                    ctx.log(LOG_ERROR_MISSING_BASEDN, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.MISSING_BINDDN) {
                    String err = putils.errorLine;
                    ctx.log(LOG_ERROR_MISSING_BINDDN, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.MISSING_PASSWORD) {
                    String err = putils.errorLine;
                    ctx.log(LOG_ERROR_MISSING_PASSWORD, LOG_MAJOR, err);
                } else if (putils.errorCode == PluginUtils.MISSING_PASSWORD) {
                    String err = putils.errorLine;
                    ctx.log(LOG_ERROR_MISSING_PASSWORD, LOG_MAJOR, err);
                }
                return false;

            } else {

                return true;

            }
        } catch (IOException ioe) {
            ctx.log(LOG_ERROR_READING_MAP_FILE, LOG_MAJOR, ioe);

            return false;
        }
    }

    /**
     * This trims out the extra white spaces that exist in the properties.
     *
     * @param props Properties to trim
     * @return Trimmed properties
     */
    private Props trimProps(IProperty props) {
        String[] pairs = props.getPropertyPairs();
        String key = null;
        String value = null;
        Props propsobj = new Props();

        for (int i = 0; i < pairs.length; i += 2) {
            key = pairs[i].trim();
            value = null;

            if (pairs[i + 1] != null) {
                value = pairs[i + 1].trim();
            }

            //reset it back to the props object
            propsobj.setProperty(key, value);
        }

        return propsobj;
    }

    public String getProperty(String key) {
        return publishedProps.getProperty(key);
    }

    public String[] getPropertyPairs() {
        return publishedProps.getPropertyPairs();
    }

    private static class HostInfo {
        String hostname;
        String dnshostname;
        String host;

        String txfound;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getDnshostname() {
            return dnshostname;
        }

        public void setDnshostname(String dnshostname) {
            this.dnshostname = dnshostname;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getTxfound() {
            return txfound;
        }

        public void setTxfound(String txfound) {
            this.txfound = txfound;
        }

    }

}

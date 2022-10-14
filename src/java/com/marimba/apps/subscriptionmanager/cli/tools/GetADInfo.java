// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.tools;

import java.util.*;
import javax.naming.*;

import com.marimba.tools.util.*;
import com.marimba.intf.util.*;

import com.marimba.tools.ldap.*;
import com.marimba.tools.dns.*;

/**
 * Channel for testing and listing information needed bu SPM and plugins againts AD.
 *
 * @author Devendra Vamathevan
 * @version %I%, %G%
 */

public class GetADInfo {

    IProperty config = null;
    String domain;
    String site;
    String pwd;
    String[] dnsHosts;
    LDAPConnection directory = null;
    String[] dcRec = null;
    boolean stdout = false;
    boolean useSSL = false;
    String forestRoot;


    public GetADInfo(IProperty config) {
        this.config = config;
    }


    public void execute() {

       channelLog("*");
       channelLog("********** Starting GetDirectoryInfo Active Directory **********");
       channelLog("*");

        //parse args

        stdout = "true".equals(config.getProperty("stdout"));


// get dnshosts
        channelLog("-udn  = " + config.getProperty("-udn"));
        dnsHosts = LDAPConnUtils.getInstance().getSRVDNSHosts();
        channelLog("DNS Servers: ");
        channelLog(dnsHosts);
        useSSL = "true".equals(config.getProperty("-ssl"));

        domain = LDAPConnUtils.getInstance().getCurrentDomain();
        if (domain == null) {
            channelLog("!!!!! Failed to obtain domain = null ");
        } else {
            channelLog("Current domain = " + domain);
        }

        String forestRoot = LDAPConnUtils.getInstance().getForestRoot(null, config.getProperty("bdn"),
                config.getProperty("-udn"),
                config.getProperty("-pwd"),
                null, null,
                false, 10, -1);

        if (forestRoot == null) {
            channelLog("!!!!! Failed to obtain forest root = null ");
        } else {
            channelLog("Forest root = " + forestRoot);
        }

        channelLog("Connection using SSL? " + useSSL);
        String[] hrec = LDAPConnection.getHostRecs(LDAPConstants.TYPE_DC, dnsHosts, (String) null, domain, forestRoot, false, useSSL, null, null);
        if (hrec != null) {
            channelLog("Getting DC records " + hrec.length + "record found");
        } else {
            channelLog("Getting DC records is null ");
        }
        channelLog(hrec);

        hrec = LDAPConnection.getHostRecs(LDAPConstants.TYPE_GC, dnsHosts, (String) null, domain, forestRoot, false, useSSL, null, null);
        if (hrec != null) {
            channelLog("Getting DC records " + hrec.length + "record found");
        } else {
            channelLog("Getting DC records is null ");
        }
        channelLog(hrec);

        try {

            directory = LDAPConnection.
                    createLDAPConnectionToGC(forestRoot, null, domain, config.getProperty("bdn"),
                            config.getProperty("-udn"), config.getProperty("-pwd"), null, "simple", false, 1, -1, null);
        } catch (Exception ex) {
            channelLog("!!!!! failed to make connection to GC ");
            ex.printStackTrace();
            return;
        }
        channelLog("Connected to GC ");

        dumpADInfo();

    }


    public String[] getLookupRecs(boolean useSiteInfo,
                                  boolean isGC) {

        if (dcRec == null) {
            dcRec = listDomainTrees(true);
        }

        String[] retval = new String[dcRec.length];
        String prefix = LDAPConstants.PREFIX_SRV_TCP;

        if (useSiteInfo) {
            prefix += site + "." + LDAPConstants.PREFIX_SITES;
        }
        if (isGC) {
            prefix += LDAPConstants.PREFIX_SRV_GC;
            return new String[]{prefix + forestRoot};
        } else {
            prefix += LDAPConstants.PREFIX_SRV_DC;
        }
        for (int i = 0; i < dcRec.length; i++) {
            retval[i] = prefix + dcRec[i];
        }
        return retval;

    }

    public void getSrvRecs(String[] srvRecs) {
        DNS dns = new DNS(dnsHosts);
        String[] recs = null;

        for (int i = 0; i < srvRecs.length; i++) {
            try {
                recs = dns.lookupService(srvRecs[i]);
            } catch (java.io.IOException ioex) {
                channelLog("!!!!! DNS lookup failed with exception = " + srvRecs[i]);
                ioex.printStackTrace();
            }
            if (recs == null || recs.length <= 0) {
                channelLog("!!!!! No records found for " + srvRecs[i]);
            } else {
                channelLog(recs.length + " # records found for " + srvRecs[i]);
                channelLog(recs);
                for (int j = 0; j < recs.length; j++) {
                    createLDAPConnection(recs[j]);
                }
            }
        }
    }


    public void dumpADInfo
            () {
        try {

            site = LDAPConnUtils.getInstance().getCurrentSite(domain,
                    config.getProperty("bdn"),
                    config.getProperty("-udn"),
                    config.getProperty("-pwd"),
                    null,
                    "simple",
                    false, 1, -1);
            if (site == null) {
                channelLog("!!!!! Failed to obtain site = null ");
            } else {
                channelLog("Current site   = " + site);
            }
            listForestRoot();
            channelLog("Domain roots = ");
            channelLog(listDomainTrees(false));
            channelLog("Domains = : ");
            channelLog(listDomainTrees(true));
            channelLog("Lookup dc records with site");
            getSrvRecs(getLookupRecs(true, false));
            channelLog("Lookup dc records without site");
            getSrvRecs(getLookupRecs(false, false));
            channelLog("Lookup gc records with site");
            getSrvRecs(getLookupRecs(true, true));
            channelLog("Lookup gc records without site");
            getSrvRecs(getLookupRecs(false, true));
            dumpSubConfig();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public String[] listDomainTrees
            (boolean alldomains) {
        Vector domainTrees = new Vector(10);
        try {
            if (alldomains) {
                LDAPConnUtils.getInstance().searchAndReturnDomains(domainTrees,
                        config.getProperty("bdn"),
                        config.getProperty("-udn"),
                        config.getProperty("-pwd"),
                        null, null,
                        false, 10, -1);
            } else {
                LDAPConnUtils.getInstance().searchAndReturnRootDomainTrees(domainTrees,
                        config.getProperty("bdn"),
                        config.getProperty("-udn"),
                        config.getProperty("-pwd"),
                        null, null,
                        false, 10, -1);
            }
        } catch (LDAPLocalException lle) {
            lle.printStackTrace();
        } catch (NamingException lde) {
            lde.printStackTrace();
        }

        String[] retval = new String[domainTrees.size()];
        int count = 0;
        for (Enumeration enumDns = domainTrees.elements(); enumDns.hasMoreElements();) {
            retval[count++] = LDAPConnUtils.getDomainFromDN(directory, enumDns.nextElement().toString());
        }
        return retval;
    }

    public void listForestRoot
            () {
        try {

            String forestRoot = LDAPConnUtils.getInstance().getForestRoot(null, config.getProperty("bdn"),
                    config.getProperty("-udn"),
                    config.getProperty("-pwd"),
                    null, null,
                    false, 10, -1);
            if (domain == null) {
                channelLog("!!!!! Failed to obtain forest root ");
            } else {
                channelLog("Forest root    = " + forestRoot);

            }
        } catch (LDAPLocalException lle) {
            lle.printStackTrace();
        }

    }


    public String getConfigDN
            () throws LDAPException, LDAPLocalException, NamingException {
        // Search for an entry that contains Subscription Config as the name.
        // This signifies the configuration object for Subscription
        String searchFilter = "(&(cn=Subscription Config)" +
                "(objectclass=marimbaCom1996-Castanet-SubscriptionSubscription))";
        String[] searchBases = listDomainTrees(false);
        String retval = null;

        if (searchBases != null) {
            for (int i = 0; i < searchBases.length; i++) {
                String base = LDAPConnUtils.getDNFromDomain(searchBases[i]);
                String[] configDNs = directory.searchAndReturnDNs(searchFilter, base, false);

                if (configDNs != null) {
                    if (retval == null) {
                        retval = configDNs[0];
                    }
                    if (configDNs.length > 1) {
                        channelLog("!!!!! Multiple Subcription configs found ");
                    }
                    for (int j = 0; j < configDNs.length; j++) {
                        channelLog(configDNs[j]);
                    }
                }
            }
        }
        return retval;
    }

    void dumpSubConfig
            () throws Exception {
        Props subConfig;
        try {
            String configDN = getConfigDN();
            channelLog("Using config DN = " + configDN);
            IProperty iprop = (IProperty) directory.getConfig(configDN, "marimbaCom1996-Castanet-SubscriptionConfig");
            if (iprop instanceof Props) {
                subConfig = (Props) iprop;
            } else {
                subConfig = null;
            }

            if (subConfig == null) {
                // could not find config
                channelLog("!!!!! LDAP_CONNECT_SUBCONFIG_NOTFOUND");
            } else {
                channelLog("Listing Subscription Config properties");
                String[] cValues = subConfig.getPropertyPairs();
                for (int j = 0; j < cValues.length; j += 2) {
                    channelLog("    " + cValues[j] + " = " + cValues[j + 1]);
                }
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
        }
    }

    public void createLDAPConnection
            (String
            host) {
        String bdn = "";
        try {
            int index = host.indexOf(':');
            if (index != -1) {
                bdn = host.substring(0, index);
            } else {
                bdn = host;
            }
            bdn = LDAPConnUtils.getDNFromDomain(bdn);
            LDAPConnection conn = LDAPConnection.createLDAPConnection(host, bdn,
                    config.getProperty("-udn"),
                    config.getProperty("-pwd"),
                    null,
                    "simple",
                    false, 1, -1, null);
            conn.poke();
            conn.unbind();
        } catch (LDAPLocalException le) {
            channelLog("!!!!! Failed connecting to host = " + host + "bdn = " + bdn + " le");
            le.printStackTrace();
            return;
        } catch (NamingException ne) {
            channelLog("!!!!! Failed connecting to host = " + host + "bdn = " + bdn + " ne");
        }
        channelLog("     Connected to host = " + host + " bdn = " + bdn);
    }

	    public static boolean readArgs(String[] argv, IConfig config) {
        if (argv.length > 0) {
            parseArgs(argv, config);
        }
        switchPasswords(config);
        return usage(config);
    }

    public static boolean parseArgs(String[] argv, IConfig config) {
        int argc = argv.length;
        if (argc > 0) {
            for (int i = 0; i < argc; i++) {
                if (argv[i].startsWith("-")) {
                    // REMIND do checking here and print usage
                    String arg = argv[i].substring(1, argv[i].length());
                    if (arg.equalsIgnoreCase("stdout")) {
                        config.setProperty("stdout", "true");
                        continue;
                    }
                    config.setProperty(arg, argv[++i]);
                }
            }
        }
        return true;
    }


    public static void switchPasswords(IConfig config) {
        String sPwd = config.getProperty("pwd");
        // if simple password present encode and store as encoded password
        // also wipe out the simple password
        if (sPwd != null) {
            config.setProperty("pwd", Password.encode(sPwd));
            config.setProperty("pwd", null);
        }
    }

    public static boolean usage(IConfig config) {
        if ( config.getProperty("udn") == null ||
                (config.getProperty("pwd") == null && config.getProperty("pwd") == null)) {
            System.out.println("usage : ");
            System.out.println(" -udn bindDN -pwd password");
            return false;
        }
        return true;

    }

    public static String array2String(String[] stringArray) throws LDAPLocalException {
        StringBuffer sb = new StringBuffer(256);
        for (int i = 0; i < stringArray.length; i++) {
            sb.append("\n");
            sb.append(stringArray[i]);
        }
        return sb.toString();
    }

    public static void channelLog(String[] statement) {
        System.out.println(array2String(statement));
    }

    public static void channelLog(String statement) {
        System.out.println(statement);
    }

}



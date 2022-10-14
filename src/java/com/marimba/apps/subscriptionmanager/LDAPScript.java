// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.webapps.intf.SystemException;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConnUtils;

/**
 * Export LDAP install scripts dialog This file was created using some code from ExportInstallScript.java from 4.7
 *
 * @author Damodar Hegde
 * @author Simon Wynn
 * @version 1.14, 12/19/2002
 */
public class LDAPScript
        implements ISubscriptionConstants,
        IErrorConstants,
        IAppConstants {
    static final String[] directives = new String[]{"#mrbaversion", "#mrbainclude"};
    SubscriptionMain main;

    /**
     * Constructor
     *
     * @param main SubscriptionMain object used to get the servlet context
     */
    public LDAPScript(SubscriptionMain main) {
        this.main = main;
    }

    /**
     * Top level method to generate the LDAP Installation script for Active Directory and iPlanet directory
     *
     * @param filename File to which the generated script will be written to
     * @param schemafilename File to which the generated schema will be written to
     * @param dirtype Type of the directory Active Directory or iPlanet
     * @param basedn Basedn used for creating the directory objects
     * @param schemabasedn Schema Base for creating schema attrubutes / classes
     * @param usetxusers Boolean indicating whether to source users from a transmitter or the LDAP server
     * @param usegc Boolean indicating whether to use the global catalog for Active Directory
     * @param updatefrom REMIND
     *
     * @throws SystemException REMIND
     */
    public void generateInstallScript(String filename,
                                      String schemafilename,
                                      String dirtype,
                                      String basedn,
                                      String schemabasedn,
                                      boolean usetxusers,
                                      boolean usegc,
                                      String updatefrom,
                                      CLIUser cliUser)
            throws SystemException {
        if (LDAPVars.ACTIVE_DIRECTORY.equals(dirtype)) {
            generateADScript(filename, schemafilename, dirtype, basedn, schemabasedn, usetxusers, usegc, cliUser);
        } else if (LDAPVars.NETSCAPE_DIRECTORY.equals(dirtype)) {
            generateIPlanetScript(filename, dirtype, basedn, usetxusers, updatefrom);
        } else if (LDAPVars.ADAM.equals(dirtype)) {
            generateADAM(filename, dirtype, basedn, usetxusers, updatefrom);
        }
    }

    private void generateADAM(String filename, String dirtype, String basedn, boolean usetxusers, String hostname) throws SystemException {

        BufferedWriter bw = null;
        try {
            File outfile = new File(filename);
            FileWriter fw = new FileWriter(outfile);
            bw = new BufferedWriter(fw);
            generateIncludeADAMScript("install-adam.ldif", bw, dirtype, basedn, usetxusers);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

    /**
     * Generate the LDAP Installation script for iPlanet directory server
     *
     * @param filename File to which the generated script will be written to
     * @param dirtype Type of the directory Active Directory or iPlanet
     * @param basedn Basedn used for creating the directory objects
     * @param usetxusers Boolean indicating whether to source users from a transmitter or the LDAP server
     * @param updatefrom REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    void generateIPlanetScript(String filename,
                               String dirtype,
                               String basedn,
                               boolean usetxusers,
                               String updatefrom)
            throws SystemException {
        boolean firstTime = false;
        InputStream is = null;
        BufferedWriter bw = null;

        // Generate the install script
        try {
            File outfile = new File(filename);

            // Read the input template file
            is = main.context.getResourceAsStream("scripts/install/install-ns-tp.ldif");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            FileWriter fw = new FileWriter(outfile);
            bw = new BufferedWriter(fw);

            // replace all $ entries with the specified values
            String line = null;
            boolean skip = false;

            while ((line = br.readLine()) != null) {
                String[] directive = getDirective(line);
                boolean upgrade = updatefrom != null;

                if (directive != null) {
                    if (directive[0].equals("#mrbaversion")) {
                        if ((directive[1]).startsWith("4.7") && upgrade) {
                            skip = true;
                            if(DEBUG5) {
                                System.out.println(upgrade + directive[1] + " " + skip);
                            }

                            continue;
                        } else {
                            skip = false;
                            if(DEBUG5) {
                                System.out.println(upgrade + directive[1] + " " + skip);
                            }

                            continue;
                        }
                    } else if (directive[0].equals("#mrbainclude")) {
                        if (skip) {
                            continue;
                        }

                        if(DEBUG5) {
                            System.out.println("including " + directive[1]);
                        }
                        bw.newLine();
                        generateIncludeScript(directive[1], bw, dirtype, basedn, usetxusers);
                        bw.write(LDAPVars.getPropertyPrefix(dirtype) + ":  " + "marimba.subscriptionplugin.usetransmitterusers=" + usetxusers);

                        continue;
                    }
                }

                if (!skip) {
                    StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                    String outLine = "";

                    while (st.hasMoreTokens()) {
                        String word = st.nextToken();

                        if ("$SUFFIX".equals(word)) {
                            word = basedn;
                        }

                        outLine += word;
                    }

                    if (firstTime) {
                        bw.newLine();
                    } else {
                        firstTime = true;
                    }

                    bw.write(outLine);
                } else {
                	if(DEBUG5) {
                        System.out.println(line);
                	}
                }
            }

            bw.newLine();
        } catch (IOException e) {
            throw new SubKnownException(e, LDAPSCRIPT_CANTSAVEIPLANETSCRIPT);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (is != null) {
                    is.close();
                }
            } catch (IOException ie) {
                throw new SubKnownException(LDAPSCRIPT_CANTSAVEIPLANETSCRIPT, ie.getMessage());
            }
        }
    }

    void generateIncludeScript(String filename,
                               BufferedWriter bw,
                               String dirtype,
                               String basedn,
                               boolean usetxusers)
            throws SystemException,
            IOException {
        String cfgfile = "scripts/install/" + filename;
        InputStream iscfg = main.context.getResourceAsStream(cfgfile);
        BufferedReader brcfg = new BufferedReader(new InputStreamReader(iscfg));
        String linecfg = null;

        while ((linecfg = brcfg.readLine()) != null) {
            StringTokenizer st1 = new StringTokenizer(linecfg, ":");
            st1.nextToken();

            String str = st1.nextToken();
            StringTokenizer st2 = new StringTokenizer(str, " ,", true);
            String propLine = "";

            while (st2.hasMoreTokens()) {
                String word = st2.nextToken();

                if ("$SUFFIX".equals(word)) {
                    word = basedn;
                }

                propLine += word;
            }

            bw.write(LDAPVars.getPropertyPrefix(dirtype) + ": " + propLine);
            bw.newLine();
        }
    }

    void generateIncludeADAMScript(String filename,
                                   BufferedWriter bw,
                                   String dirtype,
                                   String basedn,
                                   boolean usetxusers)
            throws SystemException,
            IOException {
        String cfgfile = "scripts/install/" + filename;
        InputStream iscfg = main.context.getResourceAsStream(cfgfile);
        BufferedReader brcfg = new BufferedReader(new InputStreamReader(iscfg));
        String linecfg = null;

        while ((linecfg = brcfg.readLine()) != null) {
            int index;
            while ((index = linecfg.indexOf("$SUFFIX")) != -1) {
                linecfg = linecfg.substring(0, index) + basedn +
                        linecfg.substring(index + "$SUFFIX".length());
            }
            bw.write(linecfg);
            bw.newLine();
        }

    }

    /**
     * THis is used for distributed mode of collections so that collection containers and machine containers can be created for each domain as needed
     *
     * @param filename REMIND
     * @param basedn REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public void generateCollectionScript(String filename,
                                         String basedn)
            throws SystemException {
        boolean firstTime = false;
        InputStream is = null;
        BufferedWriter bw = null;

// Generate the install script
        try {
            File outfile = new File(filename);

// Read the input template file
            is = main.context.getResourceAsStream("scripts/install/install-ad-collections.ldif");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            FileWriter fw = new FileWriter(outfile);
            bw = new BufferedWriter(fw);

// replace all $ entries with the specified values
            String line = null;

            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                String outLine = "";

                while (st.hasMoreTokens()) {
                    String word = st.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = basedn;
                    }

                    outLine += word;
                }

                if (firstTime) {
                    bw.newLine();
                } else {
                    firstTime = true;
                }

                bw.write(outLine);
            }
        } catch (IOException e) {
            throw new SubKnownException(e, LDAPSCRIPT_CANTSAVECOLLSCRIPT);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (is != null) {
                    is.close();
                }
            } catch (IOException ie) {
                throw new SubKnownException(LDAPSCRIPT_CANTSAVECOLLSCRIPT, ie.getMessage());
            }
        }
    }

    /**
     * Generate the LDAP Installation script for Active Directory
     *
     * @param filename File to which the generated script will be written to
     * @param schemafilename File to which the generated schema will be written to
     * @param dirtype Type of the directory Active Directory or iPlanet
     * @param basedn Basedn used for creating the directory objects
     * @param schemabasedn Schema Base for creating schema attrubutes / classes
     * @param usetxusers Boolean indicating whether to source users from a transmitter or the LDAP server
     * @param usegc Boolean indicating whether to use the global catalog for Active Directory
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    void generateADScript(String filename,
                          String schemafilename,
                          String dirtype,
                          String basedn,
                          String schemabasedn,
                          boolean usetxusers,
                          boolean usegc,
                          CLIUser cliUser)
            throws SystemException {
        boolean firstTime = false;
        InputStream is_schema = null;
        InputStream is_entries = null;
        BufferedWriter bw = null;

// Generate the install script
        try {

// Write the schema entries in the schema out file
            is_schema = main.context.getResourceAsStream("scripts/install/install-ad-schema-tp.ldif");

            File outfile = new File(schemafilename);
            FileWriter fw = new FileWriter(outfile);
            bw = new BufferedWriter(fw);

            BufferedReader br = new BufferedReader(new InputStreamReader(is_schema));

// replace $SUFFIX entries with the specified schema base dn value
            String line = null;

            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                String outLine = "";

                while (st.hasMoreTokens()) {
                    String word = st.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = schemabasedn;
                    }

                    outLine += word;
                }

                if (firstTime) {
                    bw.newLine();
                } else {
                    firstTime = true;
                }

                bw.write(outLine);
            }

// If the schema out file is not same as the LDAP entries out file
// then close the schema file and open the LDAP entries out file.
            if (!filename.equals(schemafilename)) {
                bw.newLine();
                bw.close();
                outfile = new File(filename);
                fw = new FileWriter(outfile);
                bw = new BufferedWriter(fw);
            }

// Write the LDAP entries outfile
            is_entries = main.context.getResourceAsStream("scripts/install/install-ad-entries-tp.ldif");
            br = new BufferedReader(new InputStreamReader(is_entries));

// replace all $ entries with the specified values
            line = null;

            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                String outLine = "";

                while (st.hasMoreTokens()) {
                    String word = st.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = basedn;
                    }

                    outLine += word;
                }

                bw.newLine();
                bw.write(outLine);
            }

// Write the domain container to ou=subscripiton, for all domains
            is_entries = main.context.getResourceAsStream("scripts/install/install-ad-domain-container-tp.ldif");
            br = new BufferedReader(new InputStreamReader(is_entries));

// replace all $ entries with the specified values
            LDAPConnection con = cliUser.getCollConn();
            Vector domainList = null;
            int domainListSize = 0;

            try{

                domainList =  con.searchAndReturnRootDomainTrees(LDAPConnUtils.getInstance().getConfigNamingContext(con));
            } catch(Exception e) {

            }

            if(domainList!=null) {
                domainListSize = domainList.size();
            }
            for(int j = 0; j < domainListSize; j++) {
                line = null;
                while ((line = br.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                    String outLine = "";
                    while (st.hasMoreTokens()) {
                        String word = st.nextToken();

                        if ("$DOMAINNAME".equals(word)) {
                            word = convertBaseDnToDomain(domainList.get(j).toString());
                        } else if ("$SUFFIX".equals(word)) {
                            word = basedn;
                        }

                        outLine += word;
                    }

                    bw.newLine();
                    bw.write(outLine);
                }
            }


 // Write the domain container to ou=subscripiton, for all domains - END

// Write the LDAP entries outfile
            is_entries = main.context.getResourceAsStream("scripts/install/install-ad-entries2-tp.ldif");
            br = new BufferedReader(new InputStreamReader(is_entries));

// replace all $ entries with the specified values
            line = null;

            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                String outLine = "";

                while (st.hasMoreTokens()) {
                    String word = st.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = basedn;
                    }

                    outLine += word;
                }

                bw.newLine();
                bw.write(outLine);
            }
 // Write the LDAP entries outfile - END

// now write the config properties
            bw.newLine();
            bw.write(LDAPVars.getPropertyPrefix(dirtype) + ": " + "marimba.subscriptionplugin.usetransmitterusers=");

            if (usetxusers) {
                bw.write("true");
            } else {
                bw.write("false");
            }

            bw.newLine();

            String cfgfile = "scripts/install/config-ad-tp.ldif";

            InputStream iscfg = main.context.getResourceAsStream(cfgfile);
            BufferedReader brcfg = new BufferedReader(new InputStreamReader(iscfg));
            String linecfg = null;

            while ((linecfg = brcfg.readLine()) != null) {
                StringTokenizer st1 = new StringTokenizer(linecfg, ":");
                st1.nextToken();

                String str = st1.nextToken();
                StringTokenizer st2 = new StringTokenizer(str, " ,", true);
                String propLine = "";

                while (st2.hasMoreTokens()) {
                    String word = st2.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = basedn;
                    }

                    propLine += word;
                }

                bw.write(LDAPVars.getPropertyPrefix(dirtype) + ": " + propLine);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new SubKnownException(LDAPSCRIPT_CANTSAVEADSCRIPT, e.getMessage());
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (is_schema != null) {
                    is_schema.close();
                }

                if (is_entries != null) {
                    is_entries.close();
                }
            } catch (IOException ie) {
                throw new SubKnownException(LDAPSCRIPT_CANTSAVEADSCRIPT, ie.getMessage());
            }
        }
    }

    /**
     * Generate the scripts for updating the LDAP directory schema for Active directory and iPlanet Directory
     *
     * @param filename File to which the generated script will be written to
     * @param schemafilename File to which the generated schema will be written to
     * @param dirtype Type of the directory Active Directory or iPlanet
     * @param basedn Basedn used for creating the directory objects
     * @param schemabasedn Schema Base for creating schema attrubutes / classes from a transmitter or the LDAP server
     * @param usegc Boolean indicating whether to use the global catalog for Active Directory
     *
     * @throws SystemException REMIND
     */
    public void generateUpdateScript(String filename,
                                     String schemafilename,
                                     String dirtype,
                                     String basedn,
                                     String schemabasedn,
                                     boolean usegc)
            throws SystemException {
        if (LDAPVars.ACTIVE_DIRECTORY.equals(dirtype)) {
            generateADUpdateScript(filename, schemafilename, dirtype, basedn, schemabasedn, usegc);
        } else if (LDAPVars.NETSCAPE_DIRECTORY.equals(dirtype)) {
            generateIPlanetUpdateScript(filename, basedn);
        }
    }

    /**
     * REMIND
     *
     * @param filename REMIND
     * @param schemafilename REMIND
     * @param dirtype REMIND
     * @param basedn REMIND
     * @param schemabasedn REMIND
     * @param usegc REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public void generateADUpdateScript(String filename,
                                       String schemafilename,
                                       String dirtype,
                                       String basedn,
                                       String schemabasedn,
                                       boolean usegc)
            throws SystemException {
        boolean firstTime = false;

// Generate the update script
        InputStream is = null;
        InputStream is_entries = null;
        BufferedWriter bw = null;

// Generate the schema update file
        try {
            File outfile = new File(schemafilename);

// Read the input template files
            is = main.context.getResourceAsStream("scripts/install/update-ad-schema51-tp.ldif");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            FileWriter fw = new FileWriter(outfile);
            bw = new BufferedWriter(fw);

// replace all $ entries with the specified values
            String line = null;

            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                String outLine = "";

                while (st.hasMoreTokens()) {
                    String word = st.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = schemabasedn;
                    }

                    outLine += word;
                }

                if (firstTime) {
                    bw.newLine();
                } else {
                    firstTime = true;
                }

                bw.write(outLine);
            }

            bw.newLine();

// If the schema out file is not same as the LDAP entries out file
// then close the schema file and open the LDAP entries out file.
            if (!filename.equals(schemafilename)) {
                bw.newLine();
                bw.close();
                outfile = new File(filename);
                fw = new FileWriter(outfile);
                bw = new BufferedWriter(fw);
            }

// Write the LDAP entries outfile
            is_entries = main.context.getResourceAsStream("scripts/install/update-ad-entries51-tp.ldif");
            br = new BufferedReader(new InputStreamReader(is_entries));

// replace all $ entries with the specified values
            line = null;

            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                String outLine = "";

                while (st.hasMoreTokens()) {
                    String word = st.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = basedn;
                    }

                    outLine += word;
                }

                bw.newLine();
                bw.write(outLine);
            }

            if (usegc) {
                bw.newLine();
                bw.write(LDAPVars.getPropertyPrefix(dirtype) + ": " + "marimba.subscriptionplugin.useglobalcatalog=true");
                bw.newLine();
                bw.write(LDAPVars.getPropertyPrefix(dirtype) + ": " + "marimba.subscriptionplugin.globalcatalogbase=" + schemabasedn);
            }

            bw.newLine();
            bw.write("-");
            bw.newLine();
        } catch (IOException e) {
            throw new SubKnownException(e, LDAPSCRIPT_CANTSAVEUPDATESCRIPT);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (is != null) {
                    is.close();
                }
            } catch (IOException ie) {
                throw new SubKnownException(ie, LDAPSCRIPT_CANTSAVEUPDATESCRIPT);
            }
        }
    }

    /**
     * REMIND
     *
     * @param filename REMIND
     * @param basedn REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public void generateIPlanetUpdateScript(String filename,
                                            String basedn)
            throws SystemException {
        boolean firstTime = false;

// Generate the update script
        InputStream is = null;
        BufferedWriter bw = null;

// Generate the schema update file
        try {
            File outfile = new File(filename);

// Read the input template files
            is = main.context.getResourceAsStream("scripts/install/update-ns51-tp.ldif");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            FileWriter fw = new FileWriter(outfile);
            bw = new BufferedWriter(fw);

// replace all $ entries with the specified values
            String line = null;
            boolean skip = false;
            boolean upgrade = false;

            while ((line = br.readLine()) != null) {
                String[] directive = getDirective(line);

                if (directive != null) {
                    if (directive[0].equals("#mrbaversion")) {
                        if ((directive[1]).equals("4.7") && upgrade) {
                            skip = true;

                            continue;
                        } else {
                            skip = false;

                            continue;
                        }
                    } else if (directive[0].equals("#mrbainclude")) {
                        ;
                    }
                }

                if (!skip) {
                    StringTokenizer st = new StringTokenizer(line, " /\",=_", true);
                    String outLine = "";

                    while (st.hasMoreTokens()) {
                        String word = st.nextToken();

                        if ("$SUFFIX".equals(word)) {
                            word = basedn;
                        }

                        outLine += word;
                    }

                    if (firstTime) {
                        bw.newLine();
                    } else {
                        firstTime = true;
                    }

                    bw.write(outLine);
                }
            }
             bw.newLine();
             bw.write("-");
             bw.newLine();
        } catch (IOException e) {
            throw new SubKnownException(e, LDAPSCRIPT_CANTSAVEUPDATESCRIPT);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (is != null) {
                    is.close();
                }
            } catch (IOException ie) {
                throw new SubKnownException(ie, LDAPSCRIPT_CANTSAVEUPDATESCRIPT);
            }
        }
    }

    /**
     * REMIND
     *
     * @param line REMIND
     *
     * @return REMIND
     */
    public String[] getDirective(String line) {
        if ((line.length() > 0) && (line.charAt(0) == '#')) {
            for (int i = 0; i < directives.length; i++) {
                if (line.startsWith(directives[i])) {
                    //returnstoken and command
                    return new String[]{directives[i], line.substring(directives[i].length() + 1)};
                }
            }
        }

        return null;
    }

   /**
     * This converts DC=example,DC=com to example.com
     *
     * @param String DC=example,DC=com
     *
     * @return String domain name
     */
    public String convertBaseDnToDomain(String currentDomain) {
        currentDomain = currentDomain.substring(2);
        StringTokenizer domainSt = new StringTokenizer(currentDomain,",DC=");
        currentDomain = null;

        while(domainSt.hasMoreTokens()) {
            if(currentDomain == null) {
                currentDomain = domainSt.nextToken();
                if(DEBUG5) {
                    System.out.println("currentDomain:" + currentDomain);
                }
            }
            else {
                currentDomain = (new StringBuffer(currentDomain + "." + domainSt.nextToken())).toString();
                if(DEBUG5) {
                    System.out.println("currentDomain:" + currentDomain);
                }
            }
        }

        return currentDomain;
    }
}

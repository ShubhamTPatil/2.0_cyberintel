// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.intf.util.IProperty;
import com.marimba.webapps.intf.SystemException;
import com.marimba.tools.ldap.LDAPConstants;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.StringTokenizer;

/**
 * Export LDAP install scripts dialog This file was created using some code from ExportInstallScript.java from 4.7
 *
 * @author Theen-Theen Tan
 * @version 1.1, 12/19/2002
 */
public class GUILDAPScript
        extends LDAPScript {
    public static String FILEPATH = "filePath";
    public static String DIRTYPE = "dirType";
    public static String BASEDN = "baseDN";
    public static String USETX = "useTx";
    public static String EPUSER = "epUser";
    public static String EPGROUP = "epGroup";
    public static String EPMACHINE = "epMachine";
    public static String EPMACHINEIMPORT = "epMachineImport";
    public static String EPCOLLECTION = "epCollection";
    public static String EPSUBSCRIPTION = "epSubscription";
    public static String USEGC = "useGC";
    public static String USEGCBASEDN = "useGCBaseDN";
    public static String USEGCFILEPATH = "useGCFilePath";
    static String[] epPropNames = new String[6];
    static String[] adPropNames = new String[5];

    static {
        epPropNames[0] = LDAPConstants.CONFIG_MACHINEIMPORTBASE;

        epPropNames[1] = LDAPConstants.CONFIG_COLLECTIONBASE;

        epPropNames[5] = LDAPConstants.CONFIG_SUBSCRIPTIONBASE;

        adPropNames[0] = LDAPConstants.CONFIG_USERCLASS;

        adPropNames[1] = LDAPConstants.CONFIG_USERIDATTR;

        adPropNames[2] = LDAPConstants.CONFIG_GROUPCLASS;

        adPropNames[3] = LDAPConstants.CONFIG_GROUPMEMBERATTR;

        adPropNames[4] = LDAPConstants.CONFIG_GROUPNAMEATTR;

        // Machine and Machine class name can't be obtained from CMS
        // so we use the default that is provided in the script
    }

    IProperty input;

    /**
     * Constructor
     *
     * @param main SubscriptionMain object used to get the servlet context
     */
    public GUILDAPScript(SubscriptionMain main) {
        super(main);
    }

    /**
     * REMIND
     *
     * @param input REMIND
     *
     * @throws SystemException REMIND
     */
    public void generateInstallScript(IProperty input)
            throws SystemException {
        this.input = input;

        if (LDAPVars.ACTIVE_DIRECTORY.equals(input.getProperty(DIRTYPE))) {
            generateADScript(input.getProperty(FILEPATH), input.getProperty(USEGCFILEPATH), LDAPVars.ACTIVE_DIRECTORY, input.getProperty(BASEDN), input
                    .getProperty(USEGCBASEDN), "true"
                    .equals(input
                    .getProperty(USETX)), "true"
                    .equals(input
                    .getProperty(USEGC)),null);
        } else if (LDAPVars.NETSCAPE_DIRECTORY.equals(input.getProperty(DIRTYPE))) {
            generateIPlanetScript(input.getProperty(FILEPATH), LDAPVars.NETSCAPE_DIRECTORY, input.getProperty(BASEDN), "true".equals(input.getProperty(USETX)), null);
        }
    }

    /**
     * REMIND
     *
     * @param input REMIND
     *
     * @throws SystemException REMIND
     */
    public void generateUpdateScript(IProperty input)
            throws SystemException {
        this.input = input;

        if (LDAPVars.ACTIVE_DIRECTORY.equals(input.getProperty(DIRTYPE))) {
            generateADUpdateScript(input.getProperty(FILEPATH), input.getProperty(USEGCFILEPATH), LDAPVars.ACTIVE_DIRECTORY, input.getProperty(BASEDN), input
                    .getProperty(USEGCBASEDN), "true"
                    .equals(input
                    .getProperty(USEGC)));
        } else if (LDAPVars.NETSCAPE_DIRECTORY.equals(input.getProperty(DIRTYPE))) {
            generateIPlanetUpdateScript(input.getProperty(FILEPATH), input.getProperty(BASEDN));
        }
    }

    /**
     * While generating the Subscription Configuration properties, we - set the entry points information to the ones set by the users in the GUI. - set the
     * objectclass/userclass information to the ones already set in CMS.
     *
     * @param br REMIND
     * @param bw REMIND
     * @param basedn REMIND
     * @param dirtype REMIND
     *
     * @throws IOException REMIND
     */
    void replaceConfig(BufferedReader br,
                       BufferedWriter bw,
                       String basedn,
                       String dirtype)
            throws IOException {
        String linecfg = null;

        while ((linecfg = br.readLine()) != null) {
            StringTokenizer st1 = new StringTokenizer(linecfg, ":");

            String propLine = null;

            st1.nextToken();

            String str = st1.nextToken();

            String propertyName = str.substring(0, str.indexOf('='));

            if (isEP(propertyName)) {
                // This is an entry point information.  The User may have changed
                // this in the GUI.
                propLine = getEPValue(propertyName);
            } else if (isObjectClass(propertyName)) {
                // This is an object class information.  The User may have changed
                // this in the GUI.
                propLine = getObjectClass(propertyName);
            }

            if (propLine == null) {
                propLine = "";

                StringTokenizer st2 = new StringTokenizer(str, " ,", true);

                while (st2.hasMoreTokens()) {
                    String word = st2.nextToken();

                    if ("$SUFFIX".equals(word)) {
                        word = basedn;
                    }

                    propLine += word;
                }
            }

            bw.write(LDAPVars.getPropertyPrefix(dirtype) + ":" + propLine);

            bw.newLine();
        }
    }

    static boolean isEP(String propName) {
        for (int i = 0; i < epPropNames.length; i++) {
            if (epPropNames[i].equals(propName.trim())) {
                return true;
            }
        }

        return false;
    }

    String getEPValue(String propName) {
        String propValue = null;

        if (LDAPConstants.CONFIG_MACHINEIMPORTBASE.equals(propName)) {
            propValue = input.getProperty(EPMACHINEIMPORT);
        } else if (LDAPConstants.CONFIG_COLLECTIONBASE.equals(propName)) {
            propValue = input.getProperty(EPCOLLECTION);
        } else if (LDAPConstants.CONFIG_SUBSCRIPTIONBASE.equals(propName)) {
            propValue = input.getProperty(EPSUBSCRIPTION);
        }

        return propValue;
    }

    static boolean isObjectClass(String propName) {
        for (int i = 0; i < adPropNames.length; i++) {
            if (adPropNames[i].equals(propName.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtain object class settings from CMS
     *
     * @param propName REMIND
     *
     * @return REMIND
     */
    String getObjectClass(String propName) {
        String propValue = null;

        if (main.hasLDAP()) {
            if (LDAPConstants.CONFIG_USERCLASS.equals(propName.trim())) {
                propValue = main.getLDAPProperty("userClass");
            } else if (LDAPConstants.CONFIG_USERIDATTR.equals(propName.trim())) {
                propValue = main.getLDAPProperty("userIDAttr");
            } else if (LDAPConstants.CONFIG_GROUPCLASS.equals(propName.trim())) {
                propValue = main.getLDAPProperty("groupClass");
            } else if (LDAPConstants.CONFIG_GROUPNAMEATTR.equals(propName.trim())) {
                propValue = main.getLDAPProperty("groupNameAttr");
            } else if (LDAPConstants.CONFIG_GROUPMEMBERATTR.equals(propName.trim())) {
                propValue = main.getLDAPProperty("groupMemberAttr");
            }
        }

        if (propValue != null) {
            propValue = propName + "=" + propValue;
        }

        return propValue;
    }
}

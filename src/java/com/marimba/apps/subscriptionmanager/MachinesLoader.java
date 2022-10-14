// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.webapps.intf.SystemException;

import javax.naming.NamingException;
import javax.naming.directory.AttributeInUseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Loads machines and groups into the iPlanet LDAP directory server
 *
 * @author Simon Wynn
 * @author Damodar Hegde
 * @version 1.7, 12/19/2002
 */
public class MachinesLoader
        implements IAppConstants,
        IErrorConstants {
	File macFile;
	LDAPConnection directory;
	String ldapMachineClass;
	String ldapGroupClass;
	String ldapMemberAttr;
	String ldapMachineNameAttr;
	String ldapMachineImportBase;
	String ldapMachineImportGroupBase;
	String ldapGroupNameAttr;
	String machinesOU;

	/**
	 * Creates a new MachinesLoader object.
	 *
	 * @param main      REMIND
	 * @param macFile   REMIND
	 * @param directory REMIND
	 */
	public MachinesLoader(SubscriptionMain main,
	                      File macFile,
	                      LDAPConnection directory) throws NamingException {
		this.macFile = macFile;
		this.directory = directory;

		IProperty subConfig = main.getSubscriptionConfig();
                IProperty mrbaConfig = main.getMarimbaConfig();
		String ldapGroupClasses = subConfig.getProperty("marimba.subscriptionplugin.groupclass");

		// pick the first itme out of a possibly comma separated list
		StringTokenizer st = new StringTokenizer(ldapGroupClasses, ",");

		while (st.hasMoreTokens()) {
			ldapGroupClass = st.nextToken();

			break;
		}

		ldapMemberAttr = subConfig.getProperty("marimba.subscriptionplugin.groupmemberattr");

		// pick the first itme out of a possibly comma separated list
		st = new StringTokenizer(ldapMemberAttr, ",");
		while (st.hasMoreTokens()) {
			ldapMemberAttr = st.nextToken();

			break;
		}

		ldapMachineClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);
		ldapMachineNameAttr = subConfig.getProperty("marimba.subscriptionplugin.machinenameattr");
		if (LDAPConstants.VENDOR_NS.equals(directory.getVendor())) {
			ldapMachineImportBase = subConfig.getProperty("marimba.subscriptionplugin.machinebase");
			if (ldapMachineImportBase == null) {
				ldapMachineImportBase = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINEIMPORTBASE);
			}
		} else {
			ldapMachineImportBase = mrbaConfig.getProperty(LDAPConstants.CONFIG_COLLECTIONMACHINEBASE);
		}
		ldapMachineImportGroupBase = subConfig.getProperty("marimba.subscriptionplugin.machineimportgroupbase");
		ldapGroupNameAttr = subConfig.getProperty("marimba.subscriptionplugin.groupnameattr");

		// derive the 'ou' for machines
		st = new StringTokenizer(ldapMachineImportBase);
		st.nextToken("=");
		machinesOU = st.nextToken(",")
		        .replace('=', ' ')
		        .trim();
	}

	/**
	 * Import specified file containing machine and group  entries into an LDAP server
	 *
	 * @throws SystemException   REMIND
	 * @throws SubKnownException REMIND
	 */
	public void importFile()
	        throws SystemException {
		try {
			// step through the machines.txt fileand create machine and group entries
			// in the locations defined by the subscription config
			String line;
			BufferedReader in = new BufferedReader(new FileReader(macFile));

			while ((line = in.readLine()) != null) {
				line = line.trim()
				        .toLowerCase();

				if (line.startsWith("#") || (line.length() == 0)) {
					continue;
				}

				int index;
				String groups;

				if ((index = line.indexOf(":")) != -1) {
					// strip of groups
					groups = line.substring(index + 1);
					line = line.substring(0, index);

					String machineDN = insertMachine(line);

					if (machineDN != null) {
						insertGroups(groups, machineDN);
					}
				} else {
					insertMachine(line);
				}
			}
		} catch (IOException ioe) {
			throw new SubKnownException(ioe, IMPORTMACHINES_INPUTFILE);
		}
	}

	/**
	 * Insert a machine into the LDAP directory
	 *
	 * @param name the CN of the machine
	 * @return the DN of the inserted machine
	 * @throws SystemException   REMIND
	 * @throws SubKnownException REMIND
	 */
	String insertMachine(String name)
	        throws SystemException {
		if (DEBUG) {
			debug("Insert machine: " + name);
		}

		// create the machine entry
		String dn = ldapMachineNameAttr + "=" + name + ", " + ldapMachineImportBase;
		try {
			Vector attrs = new Vector();
			if (LDAPConstants.VENDOR_NS.equals(directory.getVendor())) {
				attrs.addElement("objectclass: device");
				attrs.addElement("ou: " + machinesOU);
			}
			attrs.addElement("objectclass: " + ldapMachineClass);
			attrs.addElement("description: Imported from file '" + macFile.getName() + "'");

			attrs.addElement(ldapMachineNameAttr + ": " + name);

			String[] attrStr = new String[attrs.size()];
			attrs.copyInto(attrStr);


			directory.createObject(dn, attrStr, false);
		} catch (NamingException ne) {
			if(DEBUG) {
				System.out.println("failed to insert " + dn + " " + ne.getMessage());
				ne.printStackTrace();
			}
		}

		return dn;
	}

	/**
	 * Insert groups into the LDAP directory and add 'machine' to all these groups.
	 *
	 * @param groups  comma separated list of groups
	 * @param machine a machine that should be member of specified groups
	 * @throws SystemException   REMIND
	 * @throws SubKnownException REMIND
	 */
	void insertGroups(String groups,
	                  String machine)
	        throws SystemException {
		if (DEBUG) {
			debug("Insert groups: " + groups + " " + machine);
		}

		StringTokenizer st = new StringTokenizer(groups, ",");


		String machineGroupContainer;
		machineGroupContainer = ldapMachineImportGroupBase != null ? ldapMachineImportGroupBase : ldapMachineImportBase;
		String dn = "";

		while (st.hasMoreTokens()) {
			try {
				String group = st.nextToken()
				        .trim();

				dn = ldapGroupNameAttr + "=" + group + ", " + machineGroupContainer;

				// first try to find an existing group
				String searchStr = "(&(" + ldapMachineNameAttr + "=" + group + ")(objectclass=" + ldapGroupClass + "))";
				String[] dns = directory.searchAndReturnDNs(searchStr, machineGroupContainer, false);

				if (dns == null) {
					// create a new group
					Vector attrs = new Vector();
					attrs.addElement("objectclass: top");
					attrs.addElement("objectclass: " + ldapGroupClass);
					attrs.addElement(ldapGroupNameAttr + ": " + group);
					attrs.addElement(ldapMemberAttr + ": " + machine);
					attrs.addElement("description: Imported from file '" + macFile.getName() + "'");

					String[] attrStr = new String[attrs.size()];
					attrs.copyInto(attrStr);
					directory.createObject(dn, attrStr, false);
				} else {
					// add to the existing group
					String[] attrStr = new String[1];
					attrStr[0] = (ldapMemberAttr + ": " + machine);
					directory.addAttributes(dn, attrStr);
				}
			} catch (AttributeInUseException aiu) {
				debug("failed to insert machine " + machine + " into " + dn + " machine present ");
			} catch (LDAPException le) {
				debug("failed to insert machine " + machine + " into " + dn + " " + le.getMessage());

			} catch (NamingException ne) {
				debug("failed to insert machine " + machine + " into " + dn + " " + ne.getMessage());

			}
		}
	}

	/**
	 * Implement IDebug
	 *
	 * @param str REMIND
	 */
	public void debug(String str) {
		if (DEBUG) {
			System.out.println(str);
		}
	}
}

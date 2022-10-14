// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
//
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.intf;

/**
 * Constants referenced by the GUI pages
 *
 * @author Devendra Vamathevan
 * @author Marie Antoine (Tony)

 * @version 1.69, 06/24/2003
 */
public interface GUIConstants {
    // display name and type of the target
    public static final String DISPLAYNAME = "displayname";
    public static final String TYPE = "objectclass";
    public static final String HASPOLICIES = "haspolicies";
	public static final String COMPLIANCECACHED = "compliancecached";
    public static final String OSTEMPLATENAME = "ostemplatename";

    // is set when the plus sign is targetable
    public static final String ISTARGETABLE = "istargetable";

    // is set when the plus sign is expanadable
    public static final String EXPANDABLE = "expandable";

    //Reboot schedule key
    public static final String REBOOT_SCHEDULE_AT = "marimba.reboot.schedule.at";
    public static final String DIRECTORY_TYPE = "directorytype";
}

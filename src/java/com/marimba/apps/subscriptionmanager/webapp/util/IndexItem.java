// Copyright 1997-2003, Marimba Inc. All Rights Reserved. 
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// %Z%%M%, %I%, %G%
package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.webapps.tools.util.*;

/**
 * An index-page menu item
 *
 * @author	Johan Eriksson
 * @version 	%I%, %G%
 */
public class IndexItem extends ProtectedLinkBean {
    
    private String descr;
    
    public IndexItem(String role, String name, String href, String descr) {
	super(role, name, href);
	this.descr = descr;
    }

    public String getDescription() {
	return descr;
    }
}

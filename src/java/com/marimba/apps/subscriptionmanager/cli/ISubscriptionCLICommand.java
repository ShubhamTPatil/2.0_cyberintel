// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.cli;

import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletContext;

import com.marimba.webapps.intf.SystemException;

/**
 * Interface for the subscription CLI commands.
 *
 * @author	Narayanan A R
 * @version 	$Revision$, $Date$
 */
public interface ISubscriptionCLICommand {
    boolean execute(ServletContext context, Hashtable args) throws SystemException;
    boolean execute(Hashtable args) throws SystemException;
    boolean execute(String file) throws SystemException, IOException;
    boolean execute() throws SystemException; 
    String getOutputStr();
    void printMessage(String msg);
}

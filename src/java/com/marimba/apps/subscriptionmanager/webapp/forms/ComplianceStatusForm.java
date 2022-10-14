// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.webapps.intf.IMapProperty;

import org.apache.struts.action.ActionMapping;

/**
 * This form is used for storing the state of the Compliance right hand pane.
 */
public class ComplianceStatusForm
        extends AbstractForm
        implements ISubscriptionConstants,
        IMapProperty {

    //HashMap checkedItems = new HashMap(DEF_COLL_SIZE);

    /**
     * REMIND
     */
	 /*
    public void clearCheckedItems() {
        checkedItems.clear();
    }
	*/
    /**
     * REMIND
     *
     * @return REMIND
     */
	 /*
    public HashMap getCheckedItems() {
        return checkedItems;
    }
	*/
    /**
     * Reset all bean properties to their default state.  This method is
     * called before the properties are repopulated by the controller servlet.
     * <p>
     * Clears check boxes
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        //this.clearCheckedItems();
        super.reset(mapping, request);
    }


}

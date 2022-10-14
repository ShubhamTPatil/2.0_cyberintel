// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;




import org.apache.struts.action.ActionMapping;
import java.util.*;
import javax.servlet.http.*;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.webapps.intf.IMapProperty;

/**
 * Form class for copy operation
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */

public class CopyEditForm
        extends AbstractForm
        implements ISubscriptionConstants,
        IMapProperty {
    boolean initialized = false;
    HashMap checkedItems = new HashMap(DEF_COLL_SIZE);
    protected static final String copyitemprefix = "copyresult_";
    protected static final String rhsItems = "target_rhs_list";

    //documented in the interface for IMapProperty
    public void setValue(String property,
                    Object value) {
        if (property.startsWith(copyitemprefix)) {
            checkedItems.put(property, value);
        } else {
            props.put(property, value);
        }
    }

    // documented in the interface for IMapProperty
    public Object getValue(String property) {
        if (property.startsWith(copyitemprefix)) {
            return checkedItems.get(property);
        } else {
            return props.get(property);
        }
    }

    /**
     * Clearing the List
     */
    public void clearCheckedItems() {
        checkedItems.clear();
    }

    /**
     * Selected targets in the form
     *
     * @return HashMap set of target bean
     */
    public HashMap getCheckedItems() {
        return checkedItems;
    }

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
        this.clearCheckedItems();
        super.reset(mapping, request);
    }

    public void removeSelectedtargets(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(rhsItems);
    }

    public void clearPagingVars(HttpServletRequest request,ArrayList targets) {
        request.getSession().removeAttribute("target_rhs_list");
	    request.getSession().removeAttribute("context");
        request.getSession().removeAttribute("policy_exists");
        request.getSession().removeAttribute("copy_preview");
        request.getSession().removeAttribute(PAGE_TCHPROPS_CHANNELS);
        request.getSession().removeAttribute(PAGE_TCHPROPS_SUB);
        request.getSession().removeAttribute(PAGE_TCHPROPS_SUB_COPY);
        clearCheckedItems();
        for(int i=0;i<targets.size();i++){
            Target tg = (Target)targets.get(i);
            request.getSession().removeAttribute(tg.getName());
        }
    }

}

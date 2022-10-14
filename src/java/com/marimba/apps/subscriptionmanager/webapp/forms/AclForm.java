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
 * This form is used for storing the state of the page since
 * there are not form elements on the target details page.
 */
public class AclForm
        extends AbstractForm
        implements ISubscriptionConstants,
        IMapProperty {
    boolean initialized = false;
    HashMap checkedItems = new HashMap(DEF_COLL_SIZE);

    /**
     * REMIND
     */

    //documented in the interface for IMapProperty
    public void setValue(String property,
                    Object value) {
        if (property.startsWith(SESSION_PKGS_FROMTGS_PREFIX)) {
            checkedItems.put(property, value);
        } else {
            props.put(property, value);
        }
    }

    // documented in the interface for IMapProperty
    public Object getValue(String property) {
        if (property.startsWith(SESSION_PKGS_FROMTGS_PREFIX)) {
            return checkedItems.get(property);
        } else {
            return props.get(property);
        }
    }

    /**
     * REMIND
     */
    public void clearCheckedItems() {
        checkedItems.clear();
    }

    /**
     * REMIND
     *
     * @return REMIND
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


    //dumps contents of bean for debugging
    public void dump() {
        Set set = checkedItems.keySet();
        Iterator it = set.iterator();
        System.out.println("checked item size " + checkedItems.size());

        String s;

        while (it.hasNext()) {
            s = (String) it.next();
            System.out.print(s);
            System.out.print("=");
            String[] o;
            try {
                o = (String[]) checkedItems.get(s);
                for (int i = 0; i < o.length; i++) {
                    System.out.println(o[i]);
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
            System.out.println(checkedItems.get(s));
        }

        System.out.println("Contents of props");
        set = props.keySet();
        it = set.iterator();
        System.out.println("props item size " + props.size());
        while (it.hasNext()) {
            s = (String) it.next();
            System.out.print(s);
            System.out.print("=");
            System.out.println(props.get(s));
        }
    }

}
// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.util.IProperty;
import com.marimba.webapps.intf.IMapProperty;
import org.apache.struts.validator.ValidatorForm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This form is merely used for storing the state of the page since there are not form elements on the target details page.
 */

public class AbstractForm extends ValidatorForm implements ISubscriptionConstants, IWebAppConstants,
        IProperty, IMapProperty, IErrorConstants {

    Map props = new HashMap();
    private String forward;
    private boolean cloudEnabled = false;


    /**
     * REMIND
     */
    public void initialize() {
    }

    public void setProps(Map map) {
        this.props = (HashMap) map;
    }

    public Map getProps() {
        return this.props;
    }

    //documented in the interface for IMapProperty
    public Object getValue(String property) {
        return props.get(property);
    }

    //documented in the interface for IMapProperty
    public void setValue(String property,
                         Object value) {
        if (value instanceof String[]) {
            props.put(property, ((String[]) value) [0]);
        } else {
            props.put(property, value);
        }
    }

    /**
     * Implements IProperty
     *
     * @return REMIND
     */
    public String[] getPropertyPairs() {
        return null;
    }

    /**
     * REMIND
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public String getProperty(String property) {
        Object value = props.get(property);

        if (value instanceof String[]) {
            String[] valueArray = (String[]) value;

            if (valueArray.length > 0) {
                return ((String[]) value) [0];
            }
        } else {
            return (String) value;
        }

        return "";
    }

    public void setForward(String forward) {
        setValue("forward",forward);
        this.forward = forward;
    }

    public String getForward() {
        getValue("forward");
        return this.forward;
    }


    //dumps contents of bean for debugging
    public void dump() {
        Set set = props.keySet();
        Iterator it = set.iterator();

        String s;

        while (it.hasNext()) {
            s = (String) it.next();
            System.out.print(s);
            System.out.print("=");
            String[] o;
            try {
                o = (String[]) props.get(s);
                for (int i = 0; i < o.length; i++) {
                    System.out.println(o[i]);
                }
            } catch (Exception e) {
                // e.printStackTrace();
                System.out.println(props.get(s));
            }

        }
    }

    public boolean isCloudEnabled() {
        return cloudEnabled;
    }

    public void setCloudEnabled(boolean cloudEnabled) {
        this.cloudEnabled = cloudEnabled;
    }
}

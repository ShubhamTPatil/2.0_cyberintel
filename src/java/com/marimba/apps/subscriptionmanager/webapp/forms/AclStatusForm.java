// Copyright 1997-2003, Marimba Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// %Z%%M%, %I%, %G%
package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.Iterator;

/**
 * Form for the fields on the ACL status page.
 *
 * @author Kumaravel Ayyakkannu
 * @version 0, 11/16/2005
 */
public class AclStatusForm extends AbstractForm {

    String aclStatus;

    public void setAclStatus(String aclStatus) {
        this.aclStatus = aclStatus;
    }
    public String getAclStatus() {
      return aclStatus;
    }
   /**
     * We have to override the AbstractForm's set method, since it distinguishes between
     * String arrays and other values.  We need to store the values in their native format
     * for the checkbox to work.
     * @param property
     * @param value
     */
    public void setValue(String property,
                    Object value) {
        props.put(property, value);
    }
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping      mapping,
                      HttpServletRequest request) {
        Set set=props.keySet();
        Iterator iter=set.iterator();
        while(iter.hasNext()){
            Object obj=iter.next();
            props.put(obj,"false");
        }
    }

}

// Copyright 2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

/**
 * Form for the performance page.
 *
 * @author Theen-Theen Tan
 * @version 1.3, 02/27/2003
 */
public class PerformanceForm
    extends AbstractForm {
    /**
     * We have to override the AbstractForm's set method, since it distinguishes between
     * String arrays and other values.  We need to store the values in their native format
     * for the checkbox to work.
     * @param property
     * @param value
     */ 
    public void set(String property,
                    Object value) {
        props.put(property, value);
    }
}

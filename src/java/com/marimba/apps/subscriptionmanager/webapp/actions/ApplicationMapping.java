// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionMapping;

/**
 * Implementation of <strong>ActionMapping</strong> for the Struts example application.  It defines the following custom properties:
 * 
 * <ul>
 * <li>
 * <b>failure</b> - The context-relative URI to which this request should be forwarded if a validation error occurs on the input information (typically goes
 * back to the input form).
 * </li>
 * <li>
 * <b>success</b> - The context-relative URI to which this request should be forwarded if the requested action is successfully completed.
 * </li>
 * </ul>
 * 
 *
 * @author Craig R. McClanahan
 * @version $Revision$ $Date$
 */
public final class ApplicationMapping
    extends ActionMapping {
    // --------------------------------------------------- Instance Variables

    /** The failure URI for this mapping. */
    private String failure = null;

    /** The success URI for this mapping. */
    private String success = null;

    // ----------------------------------------------------------- Properties

    /**
     * Return the failure URI for this mapping.
     *
     * @return REMIND
     */
    public String getFailure() {
        return (this.failure);
    }

    /**
     * Set the failure URI for this mapping.
     *
     * @param failure The failure URI for this mapping
     */
    public void setFailure(String failure) {
        this.failure = failure;
    }

    /**
     * Return the success URI for this mapping.
     *
     * @return REMIND
     */
    public String getSuccess() {
        return (this.success);
    }

    /**
     * Set the success URI for this mapping.
     *
     * @param success The success URI for this mapping
     */
    public void setSuccess(String success) {
        this.success = success;
    }
}

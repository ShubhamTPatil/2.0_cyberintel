// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.intf;

import org.apache.struts.upload.FormFile;

/**
 * This interface should be implemented by forms that will perform a file upload using the html:form tag provided by Struts.  UploadAction would obtain the
 * FormFile object from Struts by using this interface.
 *
 * @author Theen-Theen Tan
 * @version 1.0, 07/15/2002
 *
 * @see com.marimba.apps.subscriptionmanager.webapps.common.UploadAction
 */
public interface IUploadForm {
    /**
     * Retrieve a representation of the file the user has uploaded
     *
     * @return REMIND
     */
    public FormFile getUploadFile();

    /**
     * Set a representation of the file the user has uploaded
     */
    public void setUploadFile(FormFile file);
}

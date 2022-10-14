// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.validations;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.upload.FormFile;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.UserCentricDeploymentForm;
import com.marimba.webapps.tools.util.KnownActionError;

/**
 * Validate User Centric Deployment Options
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$,  $Date$
 */
public class UserCentricDeployValidation implements IErrorConstants, IWebAppConstants {
	public static boolean validateAllSettings(java.lang.Object bean, ActionErrors errors,
            HttpServletRequest request) {
        String action = request.getParameter("action");
        UserCentricDeploymentForm userForm = (UserCentricDeploymentForm) bean;

        if (null != action && "fileUploadType".equals(action)) { // Do not worry about other action like load
        	request.setAttribute("deviceIdentType", "fileUploadType");
            FormFile file = (FormFile)userForm.getFile();

	        if(file.getFileSize() == 0 ) {
	        	errors.add("fileUploadError", new KnownActionError(USER_MAPPING_FILE_REQUIRED, ""));
	        	return errors.isEmpty();
	        }
	        if(!"application/vnd.ms-excel".equals(file.getContentType())) {
	        	errors.add("fileUploadError", new KnownActionError(USER_MAPPING_FILE_INVALID, file.getContentType()));
	        	return errors.isEmpty();
	        }
        }
        return errors.isEmpty();
	}
}

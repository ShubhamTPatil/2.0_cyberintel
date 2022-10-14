// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.common;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marimba.apps.subscriptionmanager.intf.IUploadForm;

/**
 * This a file upload.  Expects the form to have a  and file attributes and puts them in the request for the subsequent page to use
 *
 * @author Theen-Theen Tan
 * @version 1.0, 07/15/2002
 */
public class UploadAction
    extends Action {
    final boolean T3DEBUG = true;

    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response) {
        if (!(form instanceof IUploadForm)) {
            // REMIND t3: error in this case
        }

        IUploadForm theForm = (IUploadForm) form;

        //retrieve the text data
        //String text = theForm.getTheText();
        //retrieve the file representation
        FormFile file = theForm.getUploadFile();

        //retrieve the file name
        String fileName = file.getFileName();

        if (T3DEBUG) {
            System.out.println("UploadAction fileName : " + file.getFileName());
        }

        // place the data into the request for retrieval from display.jsp
        // request.setAttribute("text", text);
        request.setAttribute("fileName", fileName);

        //destroy the temporary file created
        file.destroy();

        //return a forward to display.jsp
        //return mapping.findForward("display");
        return new ActionForward("/install/install_script.jsp", true);
    }
}

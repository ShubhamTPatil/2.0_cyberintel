// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions.patch;

import java.io.IOException;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.net.URL;

import javax.servlet.http.*;
import javax.servlet.*;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchManagerHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.util.IConfig;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.tools.util.URLDecoder;
import com.marimba.tools.util.URLUTF8Encoder;
import com.marimba.tools.util.URLUTF8Decoder;

/**
 * Given a patch group URL as a request parameter IWebAppConstants., obtain a list of patches in the patch group, and store the results in
 *
 * @author Theen-Theen Tan
 * @version $Revision$, $Date$
 */
public class GetPatchesAction extends AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest req,
                                 HttpServletResponse response)
            throws IOException, ServletException {

    	String patchGroupUrl = req.getParameter(IWebAppConstants.REQUEST_PATCH_GROUPURL);
        String title = req.getParameter("title");
        
        try {
        	init(req);
            List patchList = PatchManagerHelper.getPatches((IUserPrincipal) req.getUserPrincipal(), patchGroupUrl, tenant);
            if(DEBUG2) {
                System.out.println("Patch Group URL:" + patchGroupUrl);
                System.out.println("Fetched patches size:" + patchList.size());
            }
            GUIUtils.setToSession(req, REQUEST_PATCH_GROUPURL, patchGroupUrl);
            GUIUtils.setToSession(req, "title", title);
            GUIUtils.setToSession(req, IWebAppConstants.SESSION_PATCH_PATCHES, patchList);
        } catch (SystemException e) {
            throw new GUIException(e);
        }
        return mapping.findForward("success");
    }


}

// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditPatchForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelComparator;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.webapps.intf.*;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class handles the situation when a user selects a target from the add targets page in the Package View.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005
 */

public final class AddTargetRemoveAction
        extends AbstractAction {
    final static boolean DEBUG = IAppConstants.DEBUG;
    static TargetChannelComparator comp = new TargetChannelComparator(ISubscriptionConstants.TARGET_DIRECTLYASSIGNED_KEY);

    /**
     * REMIND
     *
     * @param mapping  REMIND
     * @param form     REMIND
     * @param request  REMIND
     * @param response REMIND
     * @return REMIND
     * @throws IOException      REMIND
     * @throws ServletException REMIND
     * @throws GUIException     REMIND
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {

        if (!(form instanceof IMapProperty)) {
            InternalException ie = new InternalException(GUIUTILS_INTERNAL_WRONGARG, "PackageDeleteAction", form.toString());
            throw new GUIException(ie);
        }
        IMapProperty formbean = (IMapProperty) form;
        HttpSession session = request.getSession();
        String action = request.getParameter("type");
        String targetsSessionVar = null;
        boolean packageRemove = false;
        boolean patchRemove = false;

        if("package".equals(action)) {
            packageRemove = true;
            targetsSessionVar = ADD_REMOVE_PACKAGE;
        } else if("patch".equals(action)){
            packageRemove = false;
            targetsSessionVar = ADD_REMOVE_PATCH;
        }

        ArrayList tchannelmap = (ArrayList)session.getAttribute(targetsSessionVar);

        if(tchannelmap == null) {
            tchannelmap = new ArrayList(DEF_COLL_SIZE);
        }

        PersistifyChecksAction.SelectedRecords tgSel =
                (PersistifyChecksAction.SelectedRecords)session.getAttribute(
                        (String)formbean.getValue(SESSION_PERSIST_SELECTED));
        ArrayList removeList = new ArrayList(DEF_COLL_SIZE);
        if (tgSel != null) {
            ArrayList targetMapList = new ArrayList(tgSel.getTargetChannelMaps());
            for(int i=0;i<tchannelmap.size();i++){
                TargetChannelMap tchmap = (TargetChannelMap)tchannelmap.get(i);
                for(int j=0;j<targetMapList.size();j++){
                    TargetChannelMap tcmap = (TargetChannelMap)targetMapList.get(j);
                    if(tchmap.equals(tcmap)){
                        removeList.add(tchmap);
                    }
                }
            }
        }
       for(int k=0;k<removeList.size();k++){
           TargetChannelMap tmap = (TargetChannelMap) removeList.get(k);
           tchannelmap.remove(tmap);
       }
        if (DEBUG) {
            Iterator ite = tchannelmap.iterator();
            while(ite.hasNext()){
                TargetChannelMap tmap = (TargetChannelMap)ite.next();
                Target tg = tmap.getTarget();
                System.out.println("Target Name in the list after performing the remove action ::: "+tg.getName());
            }
        }
        try{
            Collections.sort(tchannelmap, comp);
            // Set new Target list to session var
            GUIUtils.setToSession(request, targetsSessionVar, (Object) tchannelmap);
            GUIUtils.setToSession(request, SESSION_PERSIST_RESETRESULTS, "true");
        } catch (KnownException ke) {
            throw new GUIException(ke);
        } catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        } finally {
            if(packageRemove) {
                ((AddTargetEditForm) form).clearPagingVars(request);
            } else if(patchRemove) {
                ((AddTargetEditPatchForm) form).clearPagingVars(request);
            }
        }
        return (mapping.findForward("success"));
    }
}
// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Carries out validation of target assignment before the policy is saved.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/11/2005
 */
public final class AddTargetValidateAction extends AbstractAction {

    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession();
        List pkgtchmap = new ArrayList(DEF_COLL_SIZE);
        List patchtcmap = new ArrayList(DEF_COLL_SIZE);
        List Channels = new ArrayList(DEF_COLL_SIZE);

        // perform any validation here before the preview page is shown
        try {

            if(session.getAttribute(ADD_REMOVE_PACKAGE) != null) {
                pkgtchmap = (ArrayList) session.getAttribute(ADD_REMOVE_PACKAGE);
            }

            if(session.getAttribute(ADD_REMOVE_PATCH) != null) {
                patchtcmap = (ArrayList) session.getAttribute(ADD_REMOVE_PATCH);
            }

            if (session.getAttribute(SESSION_MULTIPKGBOOL) != null) {
                Channels = (ArrayList) session.getAttribute(MAIN_PAGE_M_PKGS);
            } else {
                Channels = (ArrayList) session.getAttribute(MAIN_PAGE_PACKAGE);
            }

            if ( (pkgtchmap != null) || (pkgtchmap.size() > 0) ) {
                validateSecSchedule(pkgtchmap,Channels);
            }

            if ( (pkgtchmap == null || pkgtchmap.size() == 0) && (patchtcmap == null || patchtcmap.size() == 0)) {
                throw new KnownException(IErrorConstants.ADD_TARGET_SELECT_ONE_TARGET);
            }

        } catch (KnownException ke) {
            throw new GUIException(ke);
        } catch (SystemException se) {
            throw new GUIException(se);
        }

        return (mapping.findForward("success"));
    }

    void validateSecSchedule(List tchmaps, List channels) throws SystemException {
        StringBuffer badChannels = null;

        for (int i = 0; i < tchmaps.size(); i++) {
            TargetChannelMap tcmap = (TargetChannelMap) tchmaps.get(i);
            Channel subch = tcmap.getChannel();
            String secState = tcmap.getSecState();
            String secSchedule = tcmap.getSecSchedule();
            Target tg = tcmap.getTarget();
            if ((secState != null) && (secState.length() > 0) && (!secState.equals(INCONSISTENT)) && (secSchedule == null) ) {

                if (badChannels == null) {
                    badChannels = new StringBuffer(tg.getName());
                } else {
                    badChannels.append(", "  + tg.getName());
                }
            }
        }

        if (badChannels != null) {
            throw new KnownException(IErrorConstants.ADD_TARGET_SAVE_NOSECSCHED, badChannels.toString());
        }
    }
}

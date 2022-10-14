// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
//%Z%%M%, %I%, %G%

package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.IOException;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.webapp.actions.common.CopyBaseAction;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * This class handles the situation when a user selects a target from the confirmation
 * page to forward the target view page.
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */

public final class CopyReturnAction
        extends AbstractAction {

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
                ArrayList setTarget = new ArrayList(DEF_COLL_SIZE);
                String name = request.getParameter("name");

                init(request);

                DistributionBean copyBean = getDistributionBeanCopy(request);
                ArrayList targets = copyBean.getTargets();
                Iterator iter = targets.iterator();
                try{
                    while(iter.hasNext()){
                        Target target = (Target)iter.next();
                        if(name.equals(target.getId())){
                            //Iam setting the session varible main_page_target to the user clicked target
                            setTarget.add(target);
                            GUIUtils.setToSession(request, MAIN_PAGE_TARGET, setTarget);
                        }
                    }
                }catch(SystemException se){
                    GUIException guie = new GUIException(se);
                    throw guie;
                } finally {
                    removeDistributionBean(request);
                    removeDistributionBeanCopy(request);
                }
                return (mapping.findForward("success"));
        }
}

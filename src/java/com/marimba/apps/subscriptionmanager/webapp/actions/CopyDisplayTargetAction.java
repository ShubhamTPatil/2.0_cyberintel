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
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * This class handles the situation where the targets to copy in the preview page
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */

public final class CopyDisplayTargetAction
        extends CopyBaseAction {

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param request The non-HTTP request we are processing
     * @param response The non-HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {

            //contains list selected from the left hand pane
            ArrayList existList= new ArrayList(DEF_COLL_SIZE);
            ArrayList newList = new ArrayList(DEF_COLL_SIZE);
            init(request);
            existList = (ArrayList)super.getRhsItems(request);
            Iterator iter = existList.iterator();
            while(iter.hasNext()){
                PropsBean pbean = (PropsBean)iter.next();
                Target targets = super.propsBeanToTarget(pbean);
                newList.add(targets);
                main.updatePendingPolicySessionVar(request, targets);
            }
            try{
                DistributionBean copybean = new DistributionBean();
                copybean.setSelectedTargets(newList);
                setDistributionBeanCopy(copybean, request);
            }catch(SystemException se){
                GUIException guie = new GUIException(se);
                throw guie;
            }
            return (mapping.findForward("success"));
       }
}

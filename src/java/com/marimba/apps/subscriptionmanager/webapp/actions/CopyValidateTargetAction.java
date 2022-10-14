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
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPPolicyHelper;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;

import com.marimba.tools.ldap.LDAPLocalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;

import org.apache.struts.action.*;

/**
 * This class handles the situation when a user selects a target from the copy assignmnt page.
 * This page will look up whether the target holds the subscription object.
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */

public final class CopyValidateTargetAction
        extends AbstractAction {


    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws IOException REMIND
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {

        //contains list selected from the left hand pane
        ArrayList targets = new ArrayList(DEF_COLL_SIZE);
        init(request);

        DistributionBean copyBean = getDistributionBeanCopy(request);
        targets = copyBean.getTargets();
        String forwardName = "success";
        int count =0;
        try{
            for(int i=0;i<targets.size();i++){
                Target tg = (Target)targets.get(i);

                LDAPPolicyHelper policyFinder= new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(request),main.getSubBase(), main.getUsersInLDAP(), main.getDirType());
                policyFinder.addTarget(tg.getId(),tg.getType());
                boolean hasPolicy = policyFinder.hasPolicies(tg.getId());
                Boolean value = new Boolean(hasPolicy);
                if(!hasPolicy){
                    count = count+1;
                    if(DEBUG){
                        System.out.println("Targets which has subscription object"+tg.getName()+"and it's value is "+hasPolicy);
                    }
                }
                GUIUtils.setToSession(request,tg.getName(),value.toString());
            }
            if(targets.size() != count ){
                GUIUtils.setToSession(request,"policy_exists","true");
            }
            if(DEBUG){
                System.out.println("CopyValidateTargetAction forwarding to =="+forwardName);
            }
        }catch (LDAPLocalException localexc) {
             throw new GUIException(localexc.getRootException());
        } catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        }
        return (mapping.findForward(forwardName));
    }
}

// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
//%Z%%M%, %I%, %G%
// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
//%Z%%M%, %I%, %G%

package com.marimba.apps.subscriptionmanager.webapp.actions;

import java.io.IOException;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marimba.apps.subscriptionmanager.webapp.actions.common.CopyBaseAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.CopyEditForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPPolicyHelper;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.tools.ldap.LDAPLocalException;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.tools.util.PropsBean;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * This class handles adding new targets to list
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */

public final class CopyAddAction
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

        ArrayList addList = new ArrayList(DEF_COLL_SIZE);
        ArrayList newlist = new ArrayList(DEF_COLL_SIZE);

        if(DEBUG) {
            System.out.println("In COPYAddAction.perform");
        }

        super.init(request);

        String action = request.getParameter("action");
        String forwardName = "success";
        try {
            ServletContext sc = servlet.getServletConfig().getServletContext();
            SubscriptionMain main = TenantHelper.getTenantSubMain(sc, request);
            DistributionBean distributionBean = getDistributionBean(request);
            DistributionBean copyBean = getDistributionBeanCopy(request);
            ArrayList from_target = distributionBean.getTargets();
            Iterator from_ite = from_target.iterator();
            PropsBean itemExists = null;
            while(from_ite.hasNext()) {
                Target tg = (Target) from_ite.next();
                itemExists = super.targetToPropsBean(tg);
            }

            addList = (ArrayList)super.getRhsItems(request);

            if(action == null) {
                if( (addList == null) || (addList.size() == 0) ) {
                    PropsBean itemToAdd = super.getPropsBean(request);

                    //setting the property policy
                    checkPolicy(itemToAdd,request);
                    newlist.add(itemToAdd);
                    //Manually checking the dn equals inorder to clear the error page in right hand pane
                    if(itemExists.getProperty("dn").equals(itemToAdd.getProperty("dn"))){
                        throw new KnownException(IErrorConstants.COPY_FROM_ITEM_ALREADY_EXISTS);
                    } else {
//                    if(newlist.size() <= 0){
//                        throw new KnownException(IErrorConstants.COPY_NO_ITEM_EXISTS);
//                    }
                    super.setRhsItems(request,newlist);
                    }

                }else{
                    PropsBean itemToAdd = super.getPropsBean(request);
                    newlist.addAll(addList);

                    //setting the property policy
                    checkPolicy(itemToAdd,request);
                    //validating to targets
                    validateItemNotExists(main, itemToAdd, newlist,itemExists);
                    newlist.add(itemToAdd);

                    super.setRhsItems(request,newlist);
                    }
                    GUIUtils.setToSession(request, "context", "copyAdd");
            } else if ("remove".equals(action)) {
                //removes an entry from the addlist
                ArrayList removeList = (ArrayList)super.getRhsItems(request);
                super.removeSelectedItems((CopyEditForm)form, removeList, false);
                ((CopyEditForm) form).clearCheckedItems();
                GUIUtils.setToSession(request, "context", "copyAdd");
                forwardName = "success";
            } else if ("back".equals(action)) {
                GUIUtils.removeFromSession(request,"policy_exists");
                ArrayList targets = copyBean.getTargets();
                Iterator sess_ite = targets.iterator();
                while(sess_ite.hasNext()){
                    Target tg = (Target)sess_ite.next();
                    GUIUtils.removeFromSession(request,tg.getName());
                }
                GUIUtils.setToSession(request, "context", "copyAdd");
                forwardName = "bothpanes";
            } else if ("save".equals(action)) {

                ArrayList saveList = new ArrayList(DEF_COLL_SIZE);
                saveList = (ArrayList)super.getRhsItems(request);
                // check and create the all_all dn if it doesn't exist
                if(saveList == null || saveList.size() == 0){
                    throw new KnownException(IErrorConstants.COPY_NO_ITEM_EXISTS);
                }
                if(DEBUG){
                    System.out.println("Size of the arrayList ==="+saveList.size());
                }
                IUser user = GUIUtils.getUser(request);
                Iterator iter = addList.iterator();
                while(iter.hasNext()) {
                    PropsBean addItem = (PropsBean) iter.next();
                    //checkCreateAllAll(addItem, user);
                }

                super.setRhsItems(request, saveList);
                GUIUtils.setToSession(request,"copy_preview","true");
                GUIUtils.removeFromSession(request, "context");
                forwardName = "copysave";
            }
        } catch (LDAPLocalException localexc) {
            throw new GUIException(localexc.getRootException());
        } catch (NamingException nexc) {
            try {
            	if(DEBUG) {
                    nexc.printStackTrace();
            	}
                LDAPUtils.classifyLDAPException(nexc, null, true);
            } catch (Exception sysexc) {
                throw new GUIException(sysexc);
            }
        }  catch (KnownException ke) {
            throw new GUIException(ke);

        }catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        }
        if(DEBUG) {
            System.out.println("forwardname = " + forwardName + " path = " + mapping.findForward(forwardName).getPath());
        }
        return (mapping.findForward(forwardName));
    }

    /**
     * Setting the bean property policy to true for existing policy and vice versa.
     *
     * @param pbean PropsBean object
     * @param request HttpServletRequest object
     */
    private void checkPolicy(PropsBean pbean,HttpServletRequest request) throws SystemException{
        Target tg = (Target)super.propsBeanToTarget(pbean);
        LDAPPolicyHelper policyFinder= new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(request),main.getSubBase(), main.getUsersInLDAP(), main.getDirType());
        policyFinder.addTarget(tg.getId(),tg.getType());
        boolean hasPolicy = policyFinder.hasPolicies(tg.getId());
        if(hasPolicy) {
            pbean.setProperty("policy","true");
        }else {
            pbean.setProperty("policy","false");
        }
    }

    /**
     * Comparing the dns in the existing target beans list with the new target bean
     *
     * @param main SubscriptionMain object
     * @param item New target bean
     * @param items list of items to process
     * @param from_item bean from the distribution
     */
    private void validateItemNotExists(SubscriptionMain main, PropsBean item, ArrayList items,PropsBean from_item) throws KnownException, NamingException {
        // compare the dns
        ArrayList exists = new ArrayList(DEF_COLL_SIZE);
        exists.addAll(items);
        if(DEBUG){
            System.out.println("exists size "+exists.size());
        }
        Iterator iter = exists.iterator();
        String dn = item.getProperty("dn");
        if(DEBUG){
            System.out.println("dn = " + dn);
        }
        LDAPConnection conn = main.getAdminUser().getBrowseConn();
        Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
        while(iter.hasNext()) {
            PropsBean bean = (PropsBean) iter.next();

            if(DEBUG){
                System.out.println("exist dn = " + bean.getProperty("dn"));
            }
            String fromDn = from_item.getProperty("dn");
            String addDn = dn;
            String existDn = bean.getProperty("dn");

            if(!main.getUsersInLDAP()) {
                fromDn = from_item.getProperty("dn");
                addDn = dn;
                existDn = bean.getProperty("dn");
            } else {
                fromDn = LDAPWebappUtils.escapeDN(conn, from_item, LDAPVarsMap.get("TARGET_ALL"));
                addDn = LDAPWebappUtils.escapeDN(conn, item, LDAPVarsMap.get("TARGET_ALL"));
                existDn = LDAPWebappUtils.escapeDN(conn, bean, LDAPVarsMap.get("TARGET_ALL"));
            }

            if(DEBUG){
                System.out.println("from dn = " + fromDn);
                System.out.println("add dn = " + addDn);
                System.out.println("exist dn = " + existDn);
            }

            if(addDn.equals(fromDn)){
                throw new KnownException(IErrorConstants.COPY_FROM_ITEM_ALREADY_EXISTS);
            } else if(addDn.equals(existDn)) {
                throw new KnownException(IErrorConstants.COPY_ITEM_ALREADY_EXISTS);
            }
        }
    }
}

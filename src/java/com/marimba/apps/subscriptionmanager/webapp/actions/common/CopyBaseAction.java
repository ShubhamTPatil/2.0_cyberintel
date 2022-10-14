// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions.common;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Target;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.forms.CopyEditForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;



/**
 * Base class contains helper methods that extract values from request
 *
 * @author  Jayaprakash Paramasivam
 * @version 1.0, 31/12/2004
 */

public class CopyBaseAction
        extends AbstractAction
        implements IWebAppConstants,
        ISubscriptionConstants {
    protected static final String targetrhsItems = COPY_RHS_LIST; //"target_rhs_list";
    protected static final String lineItemPrefix = "copyresult_line_item_";
    protected static final int lineItemPrefixLength = lineItemPrefix.length();

    private String allEndpoints;

    /**
     *
     * @param request  request The HTTP request we are processing
     * @param targetName
     * @throws GUIException
     */
    protected void setRhsItems(HttpServletRequest request, String targetName, Object value) throws GUIException {
        try {
            GUIUtils.setToSession(request, targetName, value);
        } catch (SystemException se) {
            se.printStackTrace();
            GUIException guie = new GUIException(se);
            throw guie;
        }
    }

    /**
     *  helper that extracts the target list from the session
     *
     * @param  request request The HTTP request we are processing
     *
     * @return Araylist containining targets that are listed on the page
     *
     * @throws GUIException
     */
    protected List getRhsItems(HttpServletRequest request, String targetName) throws GUIException {
        try {
            //get stored values
           if(GUIUtils.getFromSession(request, targetName) == null) {
               return null;
           }
            return (List) GUIUtils.getFromSession(request, targetName);
        } catch (SystemException se) {
            se.printStackTrace();

            GUIException guie = new GUIException(se);
            throw guie;
        }
    }

    /**
     * Convert a Target object to a PropsBean object
     * @param target
     * @return
     */
    protected PropsBean targetToPropsBean(Target target) {
        allEndpoints = resources.getMessage(Locale.getDefault(), "page.global.All");
        PropsBean bean = new PropsBean();
        bean.setValue(GUIConstants.TYPE, target.getType());
        bean.setValue(GUIConstants.DISPLAYNAME, TYPE_ALL.equals(target.getType())?allEndpoints:target.getName());
        bean.setValue("dn", target.getId());
        return bean;
    }

    /**
     * Convert a PropsBean object to a Target object
     * @param bean
     * @return
     */
    protected Target propsBeanToTarget(PropsBean bean) {
        allEndpoints = resources.getMessage(Locale.getDefault(), "page.global.All");
        String type = (String) bean.getValue(GUIConstants.TYPE);
        String displayName = TYPE_ALL.equals(type)?allEndpoints:(String) bean.getValue(GUIConstants.DISPLAYNAME);
        String dn = (String) bean.getValue("dn");

        return new Target(displayName, type, dn);
    }

    /**
     *  helper that extracts the target list from the session
     *
     * @param  request request The HTTP request we are processing
     * @throws GUIException
     */
    protected void setRhsItems(HttpServletRequest request, Object value) throws GUIException {
        setRhsItems(request, targetrhsItems, value);
    }

    /**
     *  helper that sets the target list to the session
     *
     * @param  request request The HTTP request we are processing
     * @return Araylist containining targets to list on the page
     * @throws GUIException
     */
    protected List getRhsItems(HttpServletRequest request) throws GUIException {
        return getRhsItems(request, targetrhsItems);
    }

    /**
     * method assumes that the check boxes are numbered sequentially exacts the indexes from the checkbox name and uses it to index into targetList
     *
     * @param copyForm form containing the selected checkboxes
     * @param targetList results display on the page
     *
     * @return ArrayList containing the a subset of selected target from targetList
     */
    protected ArrayList getSelectedItems(CopyEditForm copyForm, List targetList) {

        HashMap checkedItems = copyForm.getCheckedItems();
        Iterator it = checkedItems.keySet()
                .iterator();
        String boxName;
        int itemNo;
        ArrayList selected = new ArrayList(checkedItems.size());

        while (it.hasNext()) {
            boxName = (String) it.next();
            if(DEBUG) {
                System.out.println("boxname" + boxName);
            }
            if (boxName.startsWith(lineItemPrefix)) {
                String val = boxName.substring(lineItemPrefixLength);
                if(!val.equalsIgnoreCase("all")) {
                    itemNo = Integer.parseInt(val);
                    selected.add(targetList.get(itemNo));
                }
            }
        }

        return selected;
    }

    /**
     *  removes elements selected items from the targetlist
     *
     * @param  copyForm form containing the selected checkboxes
     * @param  targetList results display on the page
     * @param preview only do not remove;
     *
     * @return list of items that will be removed
     */
    protected ArrayList removeSelectedItems(CopyEditForm copyForm,
                                            List targetList,
                                            boolean preview) {
        HashMap checkedItems = copyForm.getCheckedItems();
        Iterator it = checkedItems.keySet()
                .iterator();
        String boxName;
        ArrayList selected = new ArrayList(checkedItems.size());
        ArrayList removeList = new ArrayList(checkedItems.size());

        while (it.hasNext()) {
            boxName = (String) it.next();
            if (boxName.startsWith(lineItemPrefix)) {
                String val = boxName.substring(lineItemPrefixLength);
                if(!val.equalsIgnoreCase("all")) {
                    selected.add(new Integer(val));
                }
            }
        }

        Object[] sortedSelection = selected.toArray();
        Arrays.sort(sortedSelection);
        for (int i = sortedSelection.length - 1; i > -1; i--) {
            int ind = ((Integer) sortedSelection[i]).intValue();
            removeList.add(targetList.get(ind));
            if (!preview) {
                targetList.remove(targetList.get(ind));
            }
        }
        return removeList;
    }

    /**
     *  Extracts values related to a target from a query sting
     *  and creates a prop bean
     * @param request current HTTP request
     * @return prop bean containing name type & id
     */
    protected PropsBean getPropsBean(HttpServletRequest request) throws GUIException {
        String targetName = request.getParameter("name");
        String targetType = request.getParameter("type");
        String targetID = request.getParameter("id");
        allEndpoints = resources.getMessage(Locale.getDefault(), "page.global.All");
        try {
            targetType = LDAPUtils.objClassToTargetType(targetType, LDAPVarsMap);
        } catch (SubInternalException se) {
            // the target type may already have been converted, so we ignore this exception
        }
        LDAPBean ldapBean = getLDAPBean(request);
        // If sourcing users from Transmitter, and we are not browsing user
        // group from the Transmitter, the default Target type will be
        // machinegroup
        if (!main.getUsersInLDAP() && TYPE_USERGROUP.equals(targetType) && "ldap".equals(ldapBean.getEntryPoint())) {
            targetType = TYPE_MACHINEGROUP;
        }

        PropsBean bean = null;
        if (targetName != null) {
            bean = new PropsBean();
            bean.setProperty(GUIConstants.DISPLAYNAME, TYPE_ALL.equals(targetType)?allEndpoints:targetName);
            bean.setProperty(GUIConstants.TYPE, targetType);
            bean.setProperty("dn", targetID);
        }
        return bean;
    }
}

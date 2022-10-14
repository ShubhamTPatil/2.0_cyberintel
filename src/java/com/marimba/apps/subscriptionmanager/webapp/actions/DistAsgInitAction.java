// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * An interm action to initialize DistAsgForm. This action is needed because
 * the DistEditAction needs to be associated with TargetDetailsForm.  And we need to
 * initialize the DistAsgForm before we generate the JSP page.
 *
 * @author Theen-Theen Tan
 * @author Sunil Ramakrishnan
 * @version 1.3, 07/17/2002
 */

public final class DistAsgInitAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        init(request);

        // when the user selects the 'New Assignment' tab
        // they should always start with a cleared distribution bean
        if ("true".equals(request.getParameter("selectedTab"))) {
            DistributionBean distbean = new DistributionBean();

            // by default, new distribution beans are of type NEW
            // designating that this is a NEW policy assignment
            // - for the tab to be highlighted correctly in the banner
            try {
                setDistributionBean(distbean, request);
            } catch (SystemException se) {
                throw new GUIException(se);
            } finally {
                // clear out any checked boxes
                HttpSession session = request.getSession();
                session.removeAttribute(SESSION_TGS_FROMPKGS_SELECTED);
                session.removeAttribute(SESSION_PKGS_FROMTGS_SELECTED);
            }
        }

        // check if user clicked preview
        String action = request.getParameter("action");
        if (action == null || "back".equals(action) || "reload".equals(action)) {
            ((DistAsgForm) form).initialize(getResources(), getLocale(request), request);
            ((DistAsgForm) form).setDeActivateWoW(!Boolean.valueOf(tenant.getConfig().getProperty(ENABLE_WOW_FEATURE)));
            ((DistAsgForm) form).clearPagingVars(request);
        } else if ("saveucd".equals(action)) {
            HttpSession sess = request.getSession();
            if (null != sess.getAttribute("selectedUCDTmplts")) {
                Set<String> selectedTmpltNames = (Set<String>) sess.getAttribute("selectedUCDTmplts");
                if (null != selectedTmpltNames) {
                    IMapProperty formbean = (IMapProperty) form;
                    GenericPagingBean pageBean = (GenericPagingBean) sess.getAttribute((String)formbean.getValue(SESSION_PERSIST_BEANNAME));
                    applyUCDTemplate(pageBean, formbean, selectedTmpltNames);
                }
            } else {
                System.out.println("Failed to set UCD templates...");
            }
        } else {
            if ("preview".equals(action)) {
                return mapping.findForward("preview");
            }
        }
        return (mapping.findForward("success"));
    }

    private void applyUCDTemplate(GenericPagingBean pageBean, IMapProperty formbean, Set<String> ucdTmpltList) {
        String cbNamePrefix = (String)formbean.getValue(SESSION_PERSIST_PREFIX);
        Channel chnl;
        for(int i = pageBean.getStartIndex(); i < pageBean.getEndIndex(); i++ ) {
            if("true".equals(getFormValue(formbean, cbNamePrefix + i))) {
                chnl = (Channel) pageBean.getResults().get(i);
                String strTmpList = getSetAsString(ucdTmpltList);
                chnl.setUcdTemplates(strTmpList.isEmpty() ? null : strTmpList);
            }
        }
    }

    private String getSetAsString(Set<String> strList) {
        String tmpStr = "";
        for (String aStr : strList) {
        	if(aStr.startsWith("StaticLevel_")) {
        		tmpStr += aStr;
        	} else {
        		tmpStr += aStr + "," ;
        	}
        }
        if(tmpStr.equalsIgnoreCase("StaticLevel_")) tmpStr = "";
        if (tmpStr.endsWith(",")) {
            tmpStr = tmpStr.substring(0, tmpStr.length() - 1);
        }
        return tmpStr;
    }

    private Set<String> getStringAsSet(String str) {
        Set<String> tmpSet = new HashSet<String>();
        tmpSet.addAll(Arrays.asList(str.split(",")));
        return tmpSet;
    }

    private String getFormValue(IMapProperty formbean, String key) {
        Object value = formbean.getValue(key);
        return (value instanceof String[]) ? ((String[])value)[0] : (String)value;
    }
}

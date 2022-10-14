// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import com.marimba.apps.subscriptionmanager.webapp.actions.PersistifyChecksAction;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.intf.IPersistifyCheck;
import com.marimba.webapps.intf.IMapProperty;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Tag library for appyling the persisted targets selection, while paging, to the given form bean.
 *
 * @author Narayanan A R
 * @version $Id$
 */
public class SetPersistedRecords extends TagSupport {
    private String selectedTargetsVarName;
    private String formName;
    private String pagingBeanName;

    public void setSelectedTargetsVarName(String selectedTargetsVarName) {
        this.selectedTargetsVarName = selectedTargetsVarName;
    }

    public String getSelectedTargetsVarName() {
        return selectedTargetsVarName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormName() {
        return formName;
    }

    public void setPagingBeanName(String pagingBeanName) {
        this.pagingBeanName = pagingBeanName;
    }

    public String getPagingBeanName() {
        return pagingBeanName;
    }

    public int doStartTag() throws JspException {
        HttpSession session = pageContext.getSession();
        PersistifyChecksAction.SelectedRecords selectedRecords =
                (PersistifyChecksAction.SelectedRecords)session.getAttribute(selectedTargetsVarName);
        if(selectedRecords != null) {
            IMapProperty formBean = (IMapProperty)session.getAttribute(formName);
            String cbNamePrefix = (String)formBean.getValue(IWebAppConstants.SESSION_PERSIST_PREFIX);
            if(selectedRecords.isAllRecordsSelected()) {
                formBean.setValue(cbNamePrefix + "all", "true");
            }
            GenericPagingBean pageBean = (GenericPagingBean)session.getAttribute(pagingBeanName);
            for(int i=pageBean.getStartIndex(); i<pageBean.getEndIndex(); i++) {
                if(selectedRecords.isRecordSelected(i)) {
                    formBean.setValue(cbNamePrefix + i, "true");
                }
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }
}

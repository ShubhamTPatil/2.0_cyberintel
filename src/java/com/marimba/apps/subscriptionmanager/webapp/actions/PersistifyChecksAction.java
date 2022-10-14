// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IPersistifyCheck;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.webapps.intf.IMapProperty;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This action is responsible for retrieving a form and persistifying the values of the checkboxes.
 *
 * @author Theen-Theen
 * @author Rahul Ravulur
 * @author Narayanan A R
 * @version 1.4, 12/02/2002
 */
public class PersistifyChecksAction
    extends AbstractAction
    implements IWebAppConstants,
                   ISubscriptionConstants {
    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
                     return new  PersistChkTask(mapping, form, request, response);
    }

    protected class PersistChkTask extends SubscriptionDelayedTask {

        private IMapProperty formbean;
        private String cbNamePrefix;

        PersistChkTask(ActionMapping mapping, ActionForm form,
                       HttpServletRequest request, HttpServletResponse response) {
                  super(mapping, form, request, response);
            formbean = (IMapProperty) form;
            cbNamePrefix = (String)formbean.getValue(SESSION_PERSIST_PREFIX);
        }

        public void execute() {
            GenericPagingBean pageBean = (GenericPagingBean)
                    session.getAttribute((String)formbean.getValue(SESSION_PERSIST_BEANNAME));

            SelectedRecords selectedRecords =
                    (SelectedRecords)session.getAttribute((String)formbean.getValue(SESSION_PERSIST_SELECTED));
            if(selectedRecords == null) {
                selectedRecords = new SelectedRecords(pageBean);
                session.setAttribute((String)formbean.getValue(SESSION_PERSIST_SELECTED), selectedRecords);
            }

            if(isAllTargetsSelected()) {
                selectedRecords.selectAll();
            } else if(isClearAll()) {
                selectedRecords.deselectAll();
            } else {
                handleSelection(pageBean, selectedRecords);
            }

            forward = getReturnPage(request);
        }

        private void handleSelection(GenericPagingBean pageBean, SelectedRecords selectedRecords) {
            for(int i=pageBean.getStartIndex(); i<pageBean.getEndIndex(); i++ ) {
                if("true".equals(getFormValue(formbean, cbNamePrefix + i))) {
                    selectedRecords.selectRecord(i);
                } else {
                    selectedRecords.deselectRecord(i);
                }
            }
        }

        private boolean isAllTargetsSelected() {
            return "true".equals(getFormValue(formbean, cbNamePrefix + "all"));
        }

        private boolean isClearAll() {
            return "true".equals(getFormValue(formbean, "clear_all"));
        }
    }

    protected ActionForward getReturnPage(HttpServletRequest request) {
        String fwd = request.getQueryString();

        // strip out /sm if it exists
        String context = request.getContextPath();

        if (DEBUG) {
            System.out.println("PersistifyChecksAction forwarding to " + fwd);
        }

        if (fwd.startsWith(context)) {
            return getForward(fwd.substring(context.length()), null);
        }

	return getForward(fwd, null);
    }

    protected static ActionForward getForward(String forward, String src) {
        int idx = forward.indexOf('?');
        if (idx == -1) {
            return new ActionForward(forward + ((src == null) ? "" : "?src=" + src),true);
        } else {
            return new ActionForward(forward.substring(0, idx + 1) + ((src == null) ? "" : "src=" + src)+ "&" + encodeQueryStr(forward.substring(idx + 1)), true);
        }
    }

    private String getFormValue(IMapProperty formbean, String key) {
        Object value = formbean.getValue(key);
        return (value instanceof String[]) ? ((String[])value)[0] : (String)value;
    }

    /**
     * Abstracts the selected targets/channels across pages.
     */
    public static class SelectedRecords {

        /**
         * Represents the selected targets across pages in the form
         * of <name=index><value=TargetChannelMap> pair.
         */
        private Map selections;
        private GenericPagingBean pageBean;

        private boolean isAllTargetsSelected;

        private SelectedRecords(GenericPagingBean pageBean) {
            SelectedRecords.this.pageBean = pageBean;

            isAllTargetsSelected = false;
            selections = new HashMap();
        }

        private void selectRecord(int index) {
            selections.put(String.valueOf(index), pageBean.getResults().get(index));
        }

        private void deselectRecord(int index) {
            isAllTargetsSelected = false;
            selections.remove(String.valueOf(index));
        }

        private void deselectAll() {
            isAllTargetsSelected = false;
            selections.clear();
        }

        private void selectAll() {
            isAllTargetsSelected = true;
            int size = pageBean.getResults().size();
            for(int i=0; i<size; i++) {
                Object obj = pageBean.getResults().get(i);
                // Defect: SW00329648
                // For Channel objects store url as key and sort it
                if (obj instanceof Channel) {
                    Channel chnl = (Channel) obj;
                    selections.put(chnl.getUrl(), chnl);
                } else {
                    selections.put(String.valueOf(i), obj);
                }
            }
        }

        public int getSelectionCount() {
            return selections.size();
        }

        public boolean isAllRecordsSelected() {
            return isAllTargetsSelected;
        }

        public boolean isRecordSelected(int index) {
            return selections.containsKey(String.valueOf(index));
        }

        public Collection getTargetChannelMaps() {
            selections = new TreeMap(selections);
            return selections.values();
        }

    }
}

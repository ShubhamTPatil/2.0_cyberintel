package com.marimba.apps.subscriptionmanager.webapp.actions.patch;

import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchManagerHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.GUIException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


/**
 * An interm action to initialize DistAsgForm. This action is needed because
 * the DistEditAction needs to be associated with TargetDetailsForm.  And we need to
 * initialize the DistAsgForm before we generate the JSP page.
 *
 * @author Devendra Vamathevan
 * @version 1.3, 12/29/2003
 */
public final class RunSimulationAction extends AbstractAction {

    /**
     *
     * @param request
     * @return vetor containg a list of patch bundles that are in herited
     */
    protected Vector getInheritedPatchBundles(HttpServletRequest request) {
        Vector patchBundles = new Vector();
        HttpSession session = request.getSession();
        // Get the channels from the page result set returned from the multiple selection.
        ArrayList targetMapList = (ArrayList) session.getAttribute(SESSION_PKGS_FROMTGS_RS);
        Vector excluded = new Vector();
        if (targetMapList != null) {
            Channel tempch;
            TargetChannelMap tempmap = null;
            //iterate and select only patch channels that are inherited
            for (int i = 0; i < targetMapList.size(); i++) {
                tempmap = (TargetChannelMap) targetMapList.get(i);
                // ignore channels that that are assigned to this target
                // we will get the updated list from the distribution bean
                if ("true".equals(tempmap.getIsSelectedTarget())) {
                    continue;
                }
                // also ignore channles that are not patchBundles
                tempch = tempmap.getChannel();
                if (!CONTENT_TYPE_PATCHGROUP.equals(tempch.getType())) {
                    continue;
                }
                if (STATE_EXCLUDE.equals(tempch.getState())) {
                    excluded.add(tempch.getUrl());
                } else {
                    patchBundles.add(tempch.getUrl());
                }
            }
        }
        if (DEBUG) {
            System.out.println("# of inherited patch bundles (actual) = " + patchBundles.size());
            for (Iterator iterator = patchBundles.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());

            }
            System.out.println("# of excluded patch bundles = " + excluded.size());
            for (Iterator iterator = excluded.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());

            }
        }
        for (Iterator iterator = excluded.iterator(); iterator.hasNext();) {
            patchBundles.remove(iterator.next());
        }
        if (DEBUG) {
            System.out.println("# of inherited patch bundles (reconciled) = " + patchBundles.size());
            for (Iterator iterator = patchBundles.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());

            }
        }
        return patchBundles;
    }

    /**
     *
     * @param distbean
     *  @param patchBundles
     *  @return  return the patch channel that was passed in with channels
     *   assigned to the current target
     */
    Vector getChannelsAssigened2Target(DistributionBean distbean, Vector patchBundles) {
        ArrayList channels = distbean.getPatchChannels();
        for (int i = 0; i < channels.size(); i++) {
            Channel assigendCh = (Channel) channels.get(i);
            if (STATE_EXCLUDE.equals(assigendCh.getState())) {
                // should be fine because
                // they an be only one state per url
                patchBundles.remove(assigendCh.getUrl());
            } else {
                patchBundles.add(assigendCh.getUrl());
            }
        }
        if (DEBUG) {
            System.out.println("# of patch bundles (reconciled) = " + patchBundles.size());
            for (Iterator iterator = patchBundles.iterator(); iterator.hasNext();) {
                System.out.println(iterator.next());

            }
        }
        return patchBundles;
    }

    Collection getSimulationResults(HttpServletRequest req, String machineDN, Vector patchBundleUrls)
            throws SystemException {
    	init(req);
        return PatchManagerHelper.simulate((IUserPrincipal) req.getUserPrincipal(), machineDN, patchBundleUrls, tenant);
    }

    Collection testData(HttpServletRequest req, String machineDN, Vector patchBundleUrls) throws SystemException {
        String[] data = {"Buffer Overrun in Workstation Service Could Allow Code Execution",
                         "Q824146", "Install", "Prerequisite for patch Q824559"};
        Vector table = new Vector();
        IConfig row = null;
        for (int i = 0; i < 10; i++) {
            row = new ConfigProps(new Props(), null);
            row.setProperty("Description", data[0]);
            row.setProperty("ID", data[1]);
            row.setProperty("Action", data[2]);
            row.setProperty("Notes", data[3]);
            table.add(row);
        }
        Vector retval = new Vector();
        retval.add(table);
        Vector logTable = new Vector();
        for (int i = 0; i < 20; i++) {
            logTable.add("Generated log data row = " + i);
        }
        retval.add(logTable);
        return retval;
    }

     protected Task createTask(ActionMapping mapping,
                    ActionForm form,
                    HttpServletRequest request,
                    HttpServletResponse response) {
        return new SimulationTask(mapping, form, request, response);
    }


    class SimulationTask

            extends SubscriptionDelayedTask {
        SimulationTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
        }

        /**
         * REMIND
         */
        public void execute() {

            String action = request.getParameter("action");
            if ("close".equals(action)) {
                try {
                    GUIUtils.removeFromSession(request, "simulate_rs");
                    GUIUtils.removeFromSession(request, "log_rs");
                } catch (SubInternalException e) {
                    e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                }
                forward = mapping.findForward("done");
                return ;
            } else {
                if ("logs".equals(action)) {
                    forward = mapping.findForward("logs");
                    return;
                }
            }

            boolean useFakeData = false;
            if (DEBUG) {
                ServletContext sc = servlet.getServletConfig().getServletContext();
                SubscriptionMain main = TenantHelper.getTenantSubMain(sc, request);
                useFakeData = LDAPWebappUtils.getTunerProperty(main, "marimba.subscription.usefakedata") != null;
            }

            Vector patchBundleUrls = getInheritedPatchBundles(request);
            DistributionBean distbean = getDistributionBean(request);
            getChannelsAssigened2Target(distbean, patchBundleUrls);
            ArrayList targetList = distbean.getTargets();
            //simulation works on a single target only
            Target target = (Target) targetList.get(0);
            String targetName = target.getName();
            Collection simulationResults;
            try {
                if (useFakeData) {
                    simulationResults = testData(request, targetName, patchBundleUrls);
                } else {
                    simulationResults = getSimulationResults(request, targetName, patchBundleUrls);
                }
                Vector v = (Vector) simulationResults;
                GUIUtils.setToSession(request, "simulate_rs", v.elementAt(0));
                GUIUtils.setToSession(request, "log_rs", v.elementAt(1));

            } catch (SystemException e) {
                guiException = new GUIException(e);
            }

            forward = mapping.findForward("success");

        }


    }
}


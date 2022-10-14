package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.castanet.ILauncher;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.castanet.IActive;
import com.marimba.intf.util.*;
import com.marimba.tools.config.*;
import com.marimba.tools.util.*;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nrao
 * Date: May 12, 2005
 * Time: 4:49:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ComplianceTgtViewAction extends AbstractAction implements IWebAppConstants, ISubscriptionConstants, ComplianceConstants {

    protected boolean useURIMapping(){
        return true;
    }

    /**
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     */

    protected Task createTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
        return new ComplianceTgtViewAction.CompTgtViewTask(mapping, form, request, response);
    }

    protected class CompTgtViewTask extends SubscriptionDelayedTask {
        PackageComplianceForm pkgForm;
        CompTgtViewTask(ActionMapping mapping, ActionForm form, HttpServletRequest request,HttpServletResponse response) {
            super(mapping, form, request, response);
            this.pkgForm = (PackageComplianceForm) form;
        }

        public void execute() {

            init(request);
            pkgForm.initialize(main);
//            GUIUtils.initForm(request, mapping);

            try {
                GUIUtils.setToSession(request, "context", "compTarget");
            } catch (SystemException e) {
                e.printStackTrace();
            }

            String view = request.getParameter( "view" );
            String results = request.getParameter( "results" );
            String taskid = (String)session.getAttribute(IARTaskConstants.AR_TASK_ID);
            String action = request.getParameter("action");
            if ("install".equals(action) || "retry".equals(action)) {
                triggerSCAPUpdate();
            }

            if( view == null ){
                view = "target";
            }

            if(DEBUG) System.out.println( "view is "+view );

            HttpSession session = request.getSession();
            Target target = null;
            try {
                target = getTarget(request, main);

                if( target == null ){
                    target = ( Target )session.getAttribute( "target" );
                } else {
                    session.setAttribute( "target", target );
                }

                if(target == null && (taskid != null)) {
                    List targetList = null;
                    if(session.getAttribute(MAIN_PAGE_TARGET) != null) {
                        targetList = (List)session.getAttribute(MAIN_PAGE_TARGET);
                    } else {
                        targetList = (List)session.getAttribute(MAIN_PAGE_M_TARGETS);
                    }
                    // if found multiple targets only getting the first target in the list.
                    if (targetList != null && !targetList.isEmpty() ) {
                        target = (Target) targetList.get(0);
                    }
                }

                List display_results = null;
                if( display_results != null){
                    session.removeAttribute( IWebAppConstants.SESSION_POLICIES_DETAILS );
                    GUIUtils.setToSession( request, IWebAppConstants.POLICIES_FORTGTNAME, target.getName() );
                    GUIUtils.setToSession( request, IWebAppConstants.POLICIES_DETAILS_FORTGT, display_results );
                } else {
                    // will be executed when there is no selected target
                    session.removeAttribute( IWebAppConstants.SESSION_COMP_HASPOLICIES );
                    session.removeAttribute( "results" );
                    session.removeAttribute( IWebAppConstants.SESSION_POLICY_LASTUPDATED );
                    session.removeAttribute(IWebAppConstants.SESSION_POLICIES_DETAILS);
                    session.removeAttribute(IWebAppConstants.POLICIES_FORTGTNAME);
                    session.removeAttribute(IWebAppConstants.POLICIES_DETAILS_FORTGT);
                    session.removeAttribute(IWebAppConstants.COMP_SUMMARY_RESULT);
                    session.removeAttribute(IWebAppConstants.SESSION_COMP_LASTCALCULATED);
                    session.removeAttribute(IWebAppConstants.NO_ACL_PERMISSION);
                }
                session.setAttribute( "view", view );
                pkgForm.initialize(main);
                forward = mapping.findForward("success");
            } catch (SystemException se) {
                guiException = new GUIException( se );
                forward = mapping.findForward("failure");
            } catch ( Exception exp ){
                exp.printStackTrace();
                guiException = new GUIException( ( new CriticalException ( exp.getMessage() ) ) );
                forward = mapping.findForward("failure");
            }
            Object bean = session.getAttribute(IWebAppsConstants.INITERROR_KEY);

            if ((bean != null) && bean instanceof Exception) {
                //remove initerror from the session because it has served its purpose
                if (DEBUG) {
                    System.out.println("ComplianceTgtViewAction: critical exception found");
                }
                session.removeAttribute(IWebAppsConstants.INITERROR_KEY);
                forward = mapping.findForward("failure");
            }
        }

        public String getWaitMessage() {
            return getString("page.global.processing");
        }
    }

    private void triggerSCAPUpdate() {
        String scapInfoChnlUrl = main.getConfig().getProperty("subscriptionmanager.securityinfo.url");

        if (null == scapInfoChnlUrl || scapInfoChnlUrl.trim().isEmpty()) {
            System.out.println("vDef channel URL is not specified");
            return;
        }
        File tempDbConfigFile = null;

        try {
            tempDbConfigFile = new File(main.getDataDirectory(), "tempDbConfig.txt");
            if (tempDbConfigFile.exists()) {
                tempDbConfigFile.delete();
            }
            tempDbConfigFile.createNewFile();

            IConfig dbCfg = new ConfigPrefs(tempDbConfigFile, null, null);
            dbCfg.setProperty("db.connection.class", main.getConfig().getProperty("subscriptionmanager.db.class"));
            dbCfg.setProperty("db.connection.url", main.getConfig().getProperty("subscriptionmanager.db.url"));
            dbCfg.setProperty("db.connection.user", main.getConfig().getProperty("subscriptionmanager.db.username"));
            dbCfg.setProperty("db.connection.pwd", Password.decode(main.getConfig().getProperty("subscriptionmanager.db.password")));
            dbCfg.setProperty("db.connection.type", Password.decode(main.getConfig().getProperty("subscriptionmanager.db.type")));
            dbCfg.setProperty("db.thread.max", main.getConfig().getProperty("subscriptionmanager.db.thread.max"));
            dbCfg.setProperty("db.thread.min", main.getConfig().getProperty("subscriptionmanager.db.thread.min"));
            ((ConfigPrefs) dbCfg).close();

            ILauncher launcher = (ILauncher) main.getFeatures().getChild("launcher");
            IWorkspace workspace = (IWorkspace) main.getFeatures().getChild("workspace");
            IConfig tunerConfig = (IConfig) main.getFeatures().getChild("tunerConfig");
            IChannel channel = workspace.getChannel(scapInfoChnlUrl);
            if (null == channel) {
                System.out.println("Unable to get vDef channel from the URL : " + scapInfoChnlUrl);
                tempDbConfigFile.delete();
                return;
            }
            System.out.println("Sending request to vDef channel for update");
            if (null != tunerConfig) {
                tunerConfig.setProperty("marimba.securityinfo.sync.status." + channel.getURL(), "inprogress");
            }
            launcher.start(channel.getURL(), new String[]{"dbimport", "-dbConfig", tempDbConfigFile.getAbsolutePath()}, false);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // todo: Tamil, delete tempDbConfigFile once 'insync' status received...
        }
    }
}

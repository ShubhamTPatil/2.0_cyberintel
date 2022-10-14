// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.BlackoutForm;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.PatchUtils;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * BlackoutSaveAction interprets the blackout form elements and saves them in the
 * DistributionBean object.
 *
 * @author Michele Lin
 * @author Sunil Ramakrishnan
 */
public final class BlackoutSaveAction extends AbstractAction {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        if (DEBUG) {
            System.out.println("BlackoutSaveAction called");
        }
        init(request);
        // save the changes to the DistributionBean
        saveState(request, (BlackoutForm) form);

        // and forward to the next action
        String forward = (String)((AbstractForm) form).getValue("forward");
        return (new ActionForward(forward, true));
    }

    /**
     * Save the changes in the form to the DistributionBean.
     */
    private void saveState(HttpServletRequest request, BlackoutForm form) throws GUIException {
        // noBlackout is the value of the radio box used to reset schedule
        String noBlackout = (String) ((BlackoutForm) form).getValue("noBlackout");
        String status;
        try {
            status=(String)GUIUtils.getFromSession(request, "pm_configured");
        }catch(Exception e){
            throw new GUIException(e);
        }

        if (DEBUG3) {
            System.out.println("noBlackout = " + noBlackout);
        }

        // String blackout represents the result
        String blackout = "";
        String separater = "+";
        String weekDays = "";

        if ("true".equals(noBlackout)) {
            // user has chosen to clear or not set a blackout schedule
            blackout = NOBLACKOUT;
        } else {
            /*
            //The black out was set, we need to determine if
            String[] exemptBlackout =  (String[]) ((BlackoutForm) form).get("exemptBlackout");
            String exempt = "exempt";
            if ("false".equals(exemptBlackout[0])) {
            exempt = "notexempt";
            }
            sb.append(exempt + "|");
            */
            SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getTimeInstance(DateFormat.SHORT, request.getLocale());
            SimpleDateFormat mrbadf = new SimpleDateFormat(IWebAppConstants.MRBA_TIMEFORMAT, java.util.Locale.US);
            String fromTime = (String) ((BlackoutForm) form).getValue("fromTime");
            String toTime = (String) ((BlackoutForm) form).getValue("toTime");

            if ("true".equals(form.getValue("sunday"))) {
                weekDays = "sun" + separater ;
            }
            if ("true".equals(form.getValue("monday"))) {
                weekDays = weekDays + "mon" + separater ;
            }
            if("true".equals(form.getValue("tuesday"))) {
                weekDays = weekDays + "tue" + separater ;
            }
            if("true".equals(form.getValue("wednesday"))) {
                weekDays = weekDays + "wed" + separater ;
            }
            if("true".equals(form.getValue("thursday")) ) {
                weekDays = weekDays + "thu" + separater ;
            }
            if("true".equals(form.getValue("friday") )) {
                weekDays = weekDays + "fri" + separater ;
            }
            if("true".equals(form.getValue("saturday") )) {
                weekDays = weekDays + "sat" + separater ;
            }

            if(weekDays.length() > 3) {
                weekDays = weekDays.substring(0,(weekDays.lastIndexOf("+")));
            }
            //New Format: BETWEEN 10:00AM and 9:00PM on mon+thu+fri+sat+sun
            //Old Format: anytime on mon+thu+fri+sat+sun BLACKOUT 10:00AM-9:00PM
            try {
            	blackout = "between " + mrbadf.format(sdf.parse(fromTime)) + " and " + 
            	mrbadf.format(sdf.parse(toTime)) + " on " + weekDays.trim();
            	
                form.setValue("blackout",blackout);
                String blkoutPriority = String.valueOf(form.getValue("blackoutPriority"));
                if(NOTAVBLE.equals(blkoutPriority) || "".equals(blkoutPriority) || null == blkoutPriority) {
                    blkoutPriority = null;
                }
                if( null != blkoutPriority ) {
                    int priority = Integer.parseInt(blkoutPriority);
                    blackout = blackout + PROP_DELIM + Integer.toString(priority);
                }
            } catch (ParseException e) {
                // leave this because we made sure that the time will be in correct format
            }

        }
        if (DEBUG3) {
            System.out.println("blackout to be saved= " + blackout);
        }
        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            // save the blackout info from the DistribitionBean to the Subscription object
            // Here no need to stripOutExistingPriority for old and new blackout schedule
            //blackout = stripOutExistingPriority(sub.getBlackOut(), blackout);

            sub.setBlackOut(blackout);

            // save the service exempt from blackout property from the DistribitionBean to the Subscription object
            if (NOBLACKOUT.equals(blackout)) {
                sub.setProperty(PROP_SERVICE_KEYWORD, "update.schedule.blackoutexempt", null);
                sub.setProperty(PROP_SERVICE_KEYWORD, "start.schedule.blackoutexempt", null);
                if("true".equals(status)){
                    setPatchProperties(sub, request, null);
                }
            } else {
                // If priority for this property is already defined, keep the priority and the value alone
                String policySchedule = String.valueOf(form.getValue("serviceExemptFromBlackout"));
                policySchedule = stripOutExistingPriority(sub.getProperty(PROP_SERVICE_KEYWORD, "update.schedule.blackoutexempt"),
                        policySchedule);

                sub.setProperty(PROP_SERVICE_KEYWORD, "update.schedule.blackoutexempt", policySchedule);
                sub.setProperty(PROP_SERVICE_KEYWORD, "start.schedule.blackoutexempt",policySchedule);

                if("true".equals(status)){
                    setPatchProperties(sub, request, String.valueOf(form.getValue("patchServiceExemptFromBlackout")));
                }

            }
            // save the blackout string to DistributionBean
            DistributionBean distributionBean = getDistributionBean(request);
            distributionBean.setBlackOut(blackout);
            distributionBean.setServiceExemptFromBlackout(Boolean.valueOf(form.getValue("serviceExemptFromBlackout").toString()));
            if("true".equals(status)){
                distributionBean.setPatchServiceExemptFromBlackout(Boolean.valueOf(form.getValue("patchServiceExemptFromBlackout").toString()));
            }
        } catch (SystemException e) {
            throw new GUIException(e);
        }
        try {
            if(status != null) {
                GUIUtils.removeFromSession(request, "pm_configured");
            }
        }catch(Exception e) {
            throw new GUIException(e);
        }
    }

    private void setPatchProperties(ISubscription sub, HttpServletRequest request, String value) throws SystemException {
        //if (GUIUtils.getMain(servlet).hasCapability(IAppConstants.CAPABILITIES_PATCH)) {
        String patchServiceUrl = PatchUtils.getPatchServiceUrl(request, tenant);
        if (patchServiceUrl == null) {
            throw new CriticalException(IErrorConstants.PATCH_SERVICEURLNOTCONFIGURED);
        }
        String update_sch_oldValue = PatchUtils.getPatchServiceProperty(sub, "update.schedule.blackoutexempt");
        String start_sch_oldValue = PatchUtils.getPatchServiceProperty(sub, "update.schedule.blackoutexempt");

        PatchUtils.setPatchServiceProperty(sub, patchServiceUrl, "update.schedule.blackoutexempt", stripOutExistingPriority(update_sch_oldValue, value));
        PatchUtils.setPatchServiceProperty(sub, patchServiceUrl, "start.schedule.blackoutexempt", stripOutExistingPriority(start_sch_oldValue, value));
        //}
    }
}
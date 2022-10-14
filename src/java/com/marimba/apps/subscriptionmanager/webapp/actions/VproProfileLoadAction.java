// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import com.marimba.tools.util.Password;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.VproProfileForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;

/**
 * This action is called when visiting the Vpro Profile Settings page. It reads the Vpro Profile
 * Configuration Settings and populates the form for display.
 *
 * @author Selvaraj Jegatheesan
 */
public class VproProfileLoadAction extends AbstractAction {
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {

        ISubscription sub = null;
        try {
            sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            String enableVproProfile = getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_AMT));
            if(VALID.equals(enableVproProfile) ){
                setSessionValue(VALID, (VproProfileForm)form, sub);
            } else {
                setDefaultValue(INVALID, (VproProfileForm)form);
            }

        } catch (SystemException e) {
            throw new GUIException(e);
        }
        // check if user clicked preview
        String action = request.getParameter("action");
        if ("preview".equals(action)) {
            return mapping.findForward("preview");
        }
        return (mapping.findForward("success"));
    }
    private void setSessionValue(String enableVproProfile, VproProfileForm vproForm, ISubscription sub) throws SystemException {
        String certFile = null;
        String prvtFile = null;

        vproForm.setValue(ENABLEVPRO,enableVproProfile);
        vproForm.setValue(VPRO_SETTING,enableVproProfile);
        vproForm.setValue(VPRO_NONE,enableVproProfile);
        vproForm.setValue(PRVSN_PRIORITY, getPriorityValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_AMT)));
        vproForm.setValue(WEBUI,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_WEBUI)));
        vproForm.setValue(SERIALLAN,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_SERIAL_LAN)));
        vproForm.setValue(IDEREDIRECT,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_IDE_REDIRECT)));
        String kvmRedirect =getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_KVM_REDIRECT));
        vproForm.setValue(KVMREDIRECT, kvmRedirect);
        if(VALID.equals(kvmRedirect)) {
            String kvmPwd =getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, KVM_PWD));
            kvmPwd = Password.decode(kvmPwd);
            vproForm.setValue(KVMPWD,kvmPwd);
            vproForm.setValue(KVMCFRMPWD,kvmPwd);
            String kvmSession =getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_KVM_SESSION));
            vproForm.setValue(KVMSESSION, kvmSession);
            if(VALID.equals(kvmSession)) {
                vproForm.setValue(TIMEOUTUSERCONSENT,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, USER_CONSENT_TIMEOUT)));
            }
        }
        vproForm.setValue(POWERSTATE,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, SYSTEM_STATE)));
        vproForm.setValue(TIMEOUTIDLE,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, IDEL_TIMEOUT)));
        String adminPwd = getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, AMT_ADMIN_PWD));
        adminPwd = Password.decode(adminPwd);
        vproForm.setValue(ADMINPWD,adminPwd);
        vproForm.setValue(ADMINCFRMPWD,adminPwd);
        vproForm.setValue(PINGREG,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, ENABLE_PING_REG)));
        vproForm.setValue(FORCE_APPLY,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, AMT_FORCE_APPLY)));
        certFile = getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, CERT_FILE_PATH));
        prvtFile = getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, PVT_FILE_PATH));
        vproForm.setValue(FQDNSETTINGS, getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, FQDN_SETTINGS)));
        if(null != certFile && null!= prvtFile) {
            vproForm.setValue("enableTLS", VALID);
            vproForm.setValue(PRVTFILE,prvtFile);
            vproForm.setValue(CERTFILE,certFile);
        } else {
            vproForm.setValue("enableTLS", INVALID);    
        }
        vproForm.setValue(IP_SETTINGS,getTChPropValue(sub.getProperty(PROP_AMT_KEYWORD, AMT_IP_SETTING)));

    }
    private void setDefaultValue(String enableVproProfile, VproProfileForm vproForm) throws SystemException  {
        vproForm.setValue(ENABLEVPRO,enableVproProfile);
        vproForm.setValue(VPRO_NONE,enableVproProfile);
        vproForm.setValue(VPRO_SETTING,enableVproProfile);
    }
}

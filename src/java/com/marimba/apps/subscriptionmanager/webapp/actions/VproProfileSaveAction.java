package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscriptionmanager.webapp.forms.VproProfileForm;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.tools.util.Password;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;

import java.util.Vector;
import java.util.Enumeration;

/**
 * This action is used to store the Vpro Profile settings to Distribution bean
 *
 * @author Selvaraj Jegatheesan
 */

public class VproProfileSaveAction extends AbstractAction{
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws ServletException {

        if (DEBUG) {
            System.out.println("VproProfileSaveAction called");
        }

        // save the changes to the DistributionBean
        saveState(request, (VproProfileForm) form);

        // and forward to the next action
        String forward = (String)((AbstractForm) form).getValue("forward");
        return (new ActionForward(forward, true));
    }

    /**
     * Save the changes in the form to the DistributionBean.
     */
    private void saveState(HttpServletRequest request, VproProfileForm form) throws GUIException {

        // Vpro Provisioning Profile Settings Constants
        String enableVpro = null;
        String webUI = null;
        String serialLAN = null;
        String ideRedirect = null;
        String kvmRedirect = null;
        String kvmPwd = null;
        String kvmSession = null;
        String timeoutUserconsent = null;
        String timeoutidle = null;
        String powerState = null;
        String adminPwd = null;
        String pingReq = null;
        String forceApply = null;
        String enableTLS = null;
        String prvtfile = null;
        String certfile = null;
        String priority = null;
        String fqdnSettings = null;
        Vector amtProps = new Vector();

        try {
            ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
            DistributionBean distBean = getDistributionBean(request);

            enableVpro = (String) ((VproProfileForm) form).getValue(ENABLEVPRO);
            if(VALID.equals(enableVpro)) {
                priority = ((String) ((VproProfileForm) form).getValue(PRVSN_PRIORITY)).trim();
                if(NOTAVBLE.equals(priority) || "".equals(priority)) {
                    priority = null;
                }
                setValue(sub, ENABLE_AMT, enableVpro, amtProps, priority);

                webUI = (String) ((VproProfileForm) form).getValue(WEBUI);
                if("on".equals(webUI)) {
                    webUI = VALID;
                } else {
                    webUI = INVALID;
                }
                setValue(sub, ENABLE_WEBUI, webUI, amtProps, priority);

                serialLAN = (String) ((VproProfileForm) form).getValue(SERIALLAN);
                if("on".equals(serialLAN)){
                    serialLAN = VALID;
                } else {
                    serialLAN = INVALID;
                }
                setValue(sub, ENABLE_SERIAL_LAN, serialLAN, amtProps, priority);

                ideRedirect = (String) ((VproProfileForm) form).getValue(IDEREDIRECT);
                if("on".equals(ideRedirect)) {
                    ideRedirect = VALID;
                } else {
                    ideRedirect = INVALID;
                }

                setValue(sub, ENABLE_IDE_REDIRECT, ideRedirect, amtProps, priority);

                kvmRedirect = (String) ((VproProfileForm) form).getValue(KVMREDIRECT);
                if("on".equals(kvmRedirect)) {
                    kvmRedirect = VALID;
                } else {
                    kvmRedirect = INVALID;
                }
                setValue(sub, ENABLE_KVM_REDIRECT, kvmRedirect, amtProps, priority);

                if(VALID.equals(kvmRedirect)) {
                    kvmPwd = (String) ((VproProfileForm) form).getValue(KVMPWD);
                    kvmPwd = Password.encode(kvmPwd);
                    setValue(sub, KVM_PWD, kvmPwd, amtProps, priority);

                    kvmSession = (String) ((VproProfileForm) form).getValue(KVMSESSION);
                    if("on".equals(kvmSession)) {
                        kvmSession = VALID;
                    } else {
                        kvmSession = INVALID;
                    }
                    setValue(sub, ENABLE_KVM_SESSION, kvmSession, amtProps, priority);
                    if(VALID.equals(kvmSession)) {
                        timeoutUserconsent = (String) ((VproProfileForm) form).getValue(TIMEOUTUSERCONSENT);
                        setValue(sub, USER_CONSENT_TIMEOUT, timeoutUserconsent, amtProps, priority);
                    } else {
                        setValue(sub, USER_CONSENT_TIMEOUT, null, amtProps, priority);
                    }
                } else {
                    setValue(sub, KVM_PWD, null, amtProps, priority);
                    setValue(sub, ENABLE_KVM_SESSION, null, amtProps, priority);
                    setValue(sub, USER_CONSENT_TIMEOUT, null, amtProps, priority);
                }

                powerState = (String) ((VproProfileForm) form).getValue(POWERSTATE);
                setValue(sub, SYSTEM_STATE, powerState, amtProps, priority);

                if(PWR_HST_S0.equals(powerState)) {
                    timeoutidle = null;
                } else {
                    timeoutidle = (String) ((VproProfileForm) form).getValue(TIMEOUTIDLE );
                }
                
                setValue(sub, IDEL_TIMEOUT, timeoutidle, amtProps, priority);

                fqdnSettings = (String) ((VproProfileForm) form).getValue(FQDNSETTINGS);
                setValue(sub, FQDN_SETTINGS, fqdnSettings, amtProps, priority);                
                
                adminPwd = (String) ((VproProfileForm) form).getValue(ADMINPWD);
                adminPwd = Password.encode(adminPwd);
                setValue(sub, AMT_ADMIN_PWD, adminPwd, amtProps, priority);

                pingReq = (String) ((VproProfileForm) form).getValue(PINGREG);
                if("on".equals(pingReq)) {
                    pingReq = VALID;
                } else {
                    pingReq = INVALID;
                }
                setValue(sub, ENABLE_PING_REG, pingReq, amtProps, priority);

                forceApply = (String) ((VproProfileForm) form).getValue(FORCE_APPLY);
                if("on".equals(forceApply)) {
                    forceApply = VALID;
                } else {
                    forceApply = INVALID;
                }
                setValue(sub, AMT_FORCE_APPLY, forceApply, amtProps, priority);

                enableTLS = (String) ((VproProfileForm) form).getValue("enableTLS");
                if("on".equals(enableTLS)) {
                    prvtfile = (String) ((VproProfileForm) form).getValue(PRVTFILE);
                    if("".equals(prvtfile) || null == prvtfile) {
                        setValue(sub, PVT_FILE_PATH, null, amtProps, priority);
                    } else {
                        setValue(sub, PVT_FILE_PATH, prvtfile, amtProps, priority);
                    }

                    certfile = (String) ((VproProfileForm) form).getValue(CERTFILE);
                    if("".equals(certfile) || null == certfile) {
                        setValue(sub, CERT_FILE_PATH, null, amtProps, priority);
                    } else {
                        setValue(sub, CERT_FILE_PATH, certfile, amtProps, priority);
                    }
                } else {
                    setValue(sub, PVT_FILE_PATH, null, amtProps, priority);
                    setValue(sub, CERT_FILE_PATH, null, amtProps, priority);
                }
                String ip_setting = (String) ((VproProfileForm) form).getValue("ip_settings");
                setValue(sub, AMT_IP_SETTING, ip_setting, amtProps, priority);

            } else {
                setValue(sub, ENABLE_AMT, null, amtProps, priority);
                setValue(sub, ENABLE_WEBUI, null, amtProps, priority);
                setValue(sub, ENABLE_SERIAL_LAN, null, amtProps, priority);
                setValue(sub, ENABLE_IDE_REDIRECT, null, amtProps, priority);
                setValue(sub, ENABLE_KVM_REDIRECT, null, amtProps, priority);
                setValue(sub, KVM_PWD, null, amtProps, priority);
                setValue(sub, ENABLE_KVM_SESSION, null, amtProps, priority);
                setValue(sub, USER_CONSENT_TIMEOUT, null, amtProps, priority);
                setValue(sub, IDEL_TIMEOUT, null, amtProps, priority);
                setValue(sub, SYSTEM_STATE, null, amtProps, priority);
                setValue(sub, AMT_ADMIN_PWD, null, amtProps, priority);
                setValue(sub, ENABLE_PING_REG, null, amtProps, priority);
                setValue(sub, PVT_FILE_PATH, null, amtProps, priority);
                setValue(sub, CERT_FILE_PATH, null, amtProps, priority);
                setValue(sub, AMT_FORCE_APPLY, null, amtProps, priority);
                setValue(sub, AMT_IP_SETTING, null, amtProps,priority);
                setValue(sub, FQDN_SETTINGS,null,amtProps,priority);
            }
            distBean.setAmtProps(amtProps);
        } catch (SystemException e) {
            throw new GUIException(e);
        }

    }
    private void setValue(ISubscription sub, String key,String value,Vector amtProps,String priority) throws SystemException{
        if (value != null) {
            if(null != priority && !("".equals(priority)) && !(NOTAPP.equals(priority)))  {
                value = value + PROP_DELIM + Integer.parseInt(priority);
            }
        }
        sub.setProperty(PROP_AMT_KEYWORD, key, value);
        amtProps.add(PROP_AMT_KEYWORD + PROP_DELIM + key + "=" + value);
    }
}

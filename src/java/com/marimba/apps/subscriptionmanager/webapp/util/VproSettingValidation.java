// Copyright 1997-2010, BMC, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import org.apache.commons.validator.*;

import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.VproProfileForm;

import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.util.*;

/**
 * Validation for Vpro Provisioning Settings All form validation methods should be written such that null is a possible input.
 *
 * @author Selvaraj Jegatheesan
 * @version 1.0, 08/16/2010
 */

public class VproSettingValidation
        implements IWebAppConstants,
        IErrorConstants{

    /**
     * Validates the hour and minutes fields for the blackout schedule. namely, checking that each field is present (required), an integer, and its range.
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true is there is.
     */
    public static boolean validateVpro(java.lang.Object                      bean,
                                           ValidatorAction                       va,
                                           Field                                 field,
                                           org.apache.struts.action.ActionErrors errors,
                                           javax.servlet.http.HttpServletRequest request,
                                           javax.servlet.ServletContext          application) {
        int MIN_INTEGER = 1;
        int MAX_INTEGER = 1440;
        int validInteger = 0;
        int priorityMin = 1;
        int priorityMax = 99999;

        String enableVpro = (String) ((VproProfileForm) bean).getValue("enableVpro");
        if("true".equals(enableVpro)) {
            String priority = ((String) ((VproProfileForm) bean).getValue("vProPrvsnPriority")).trim();
            if(!((NOTAPP.equals(priority)) || null == priority || "".equals(priority))) {
                try {
                    int vProPrvnPriority = Integer.parseInt(priority);
                    if(vProPrvnPriority == 0) {
                        errors.add("vProPrvsnPriority", new KnownActionError(VALIDATION_VPRO_PRIORITY_INVALID, priority ));
                        return false;
                    }
                    if(!((vProPrvnPriority >= priorityMin) && (vProPrvnPriority<= priorityMax))) {
                        errors.add("vProPrvsnPriority", new KnownActionError(VALIDATION_VPRO_PRIORITY_INVALID, priority ));
                        return false;
                    }
                } catch(NumberFormatException ne) {
                    errors.add("vProPrvsnPriority", new KnownActionError(VALIDATION_VPRO_PRIORITY_INVALID, priority ));
                    return false;
                }
            }
            String kvmRedirect = ((String) ((VproProfileForm) bean).getValue("kvmRedirect")).trim();
            if("on".equals(kvmRedirect)) {
                String kvmPwd = ((String) ((VproProfileForm) bean).getValue("kvmPwd")).trim();
                if(null == kvmPwd || "".equals(kvmPwd)) {
                    errors.add("kvmPwd", new KnownActionError(VALIDATION_VPRO_KVM_SESSION_PWD_REQUIRED));
                    return false;
                } else {
                    if(!ValidationUtil.checkPasswordStrength(kvmPwd, "true")) {
                        errors.add("kvmPwd", new KnownActionError(VALIDATION_VPRO_KVM_SESSION_PWD_INVALID));
                        return false;
                    }
                    String kvmCfrmPwd = ((String) ((VproProfileForm) bean).getValue("kvmCfrmPwd")).trim();
                    if(!kvmCfrmPwd.equals(kvmPwd)) {
                        errors.add("kvmCfrmPwd", new KnownActionError(VALIDATION_VPRO_KVM_SESSION_CFRMPWD_INVALID));
                        return false;
                    }
                }

                String kvmSession = ((String) ((VproProfileForm) bean).getValue("kvmSession")).trim();
                if("on".equals(kvmSession)) {
                    String timeoutuserconsent = ((String) ((VproProfileForm) bean).getValue("timeoutuserconsent")).trim();
                    if(null == timeoutuserconsent || "".equals(timeoutuserconsent)) {
                        errors.add("timeoutuserconsent", new KnownActionError(VALIDATION_VPRO_TIMEOUT_USERCONSENT_REQUIRED));
                        return false;
                    } else {
                        try {
                            validInteger = Integer.parseInt(timeoutuserconsent);
                        } catch(NumberFormatException numExp) {
                            errors.add("timeoutuserconsent", new KnownActionError(VALIDATION_VPRO_TIMEOUT_USERCONSENT_INTEGER_REQUIRED));
                            return false;
                        }
                        if(!((validInteger >= MIN_INTEGER) && (validInteger<= MAX_INTEGER))) {
                            errors.add("timeoutuserconsent", new KnownActionError(VALIDATION_VPRO_TIMEOUT_USERCONSENT_RANGE));
                            return false;
                        }
                    }
                }

            }
            String powerState = ((String) ((VproProfileForm) bean).getValue("powerState")).trim();
            if(!("Host is on(S0)".equals(powerState))) {
                String timeoutidle = ((String) ((VproProfileForm) bean).getValue("timeoutidle")).trim();
                if(null == timeoutidle || "".equals(timeoutidle)) {
                    errors.add("timeoutidle", new KnownActionError(VALIDATION_VPRO_TIMEOUT_IDLE_REQUIRED));
                    return false;
                } else {
                    try {
                        validInteger = Integer.parseInt(timeoutidle);
                    } catch(NumberFormatException numExp) {
                        errors.add("timeoutidle", new KnownActionError(VALIDATION_VPRO_TIMEOUT_IDLE_INTEGER_REQUIRED));
                        return false;
                    }
                    if(!((validInteger >= MIN_INTEGER) && (validInteger<= MAX_INTEGER))) {
                        errors.add("timeoutidle", new KnownActionError(VALIDATION_VPRO_TIMEOUT_IDLE_RANGE));
                        return false;
                    }
                }
            }

            String adminPwd = ((String) ((VproProfileForm) bean).getValue("adminPwd")).trim();
            if(null == adminPwd || "".equals(adminPwd)) {
                errors.add("adminPwd", new KnownActionError(VALIDATION_VPRO_ADMINUSER_PWD_REQUIRED));
                return false;
            } else {
                if(!ValidationUtil.checkPasswordStrength(adminPwd, "false")) {
                        errors.add("adminPwd", new KnownActionError(VALIDATION_VPRO_ADMINUSER_PWD_INVALID));
                        return false;
                }
                String adminCfrmPwd = ((String) ((VproProfileForm) bean).getValue("adminCfrmPwd")).trim();
                if(!adminCfrmPwd.equals(adminPwd)) {
                    errors.add("adminCfrmPwd", new KnownActionError(VALIDATION_VPRO_ADMINUSER_CFRMPWD_INVALID));
                    return false;
                }
            }
            String enableTLS = ((String) ((VproProfileForm) bean).getValue("enableTLS")).trim();
            if("on".equals(enableTLS)) {

                String certfile = ((String) ((VproProfileForm) bean).getValue("certfile")).trim();
                if(null == certfile || "".equals(certfile)) {
                    errors.add("certfile", new KnownActionError(VALIDATION_VPRO_CERTFILE_REQUIRED));
                    return false;
                }

                String prvtfile = ((String) ((VproProfileForm) bean).getValue("prvtfile")).trim();
                if(null == prvtfile || "".equals(prvtfile)) {
                    errors.add("prvtfile", new KnownActionError(VALIDATION_VPRO_PRVTFILE_REQUIRED));
                    return false;
                }
            }
        }
        return true;
    }
}

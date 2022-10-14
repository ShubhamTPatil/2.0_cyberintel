// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;

/**
 * This form is called when visiting the Vpro Profile Settings page. It is used for set and
 * get the Vpro Profile form elements
 *
 * @author Selvaraj Jegatheesan
 */

public class VproProfileForm extends AbstractForm {

    String enableVpro = null;
    String webUI = null;
    String serialLAN = null;
    String ideRedirect = null;
    String kvmRedirect = null;
    String kvmPwd = null;
    String kvmCfrmPwd  = null;
    String kvmSession = null;
    String timeoutuserconsent = null;
    String timeoutidle = null;
    String powerState = null;
    String adminPwd = null;
    String adminCfrmPwd = null;
    String pingReq = null;
    String prvtfile = null;
    String certfile = null;
    String DEFAULT_VALUE = null;
    String ip_settings = null;
    String fqdnSettings = null;
    String enableTLS = null;
    
    public void reset(ActionMapping mapping,
                          HttpServletRequest request) {
        setValue("webUI", INVALID);
        setValue("serialLAN", INVALID);
        setValue("ideRedirect", INVALID);
        setValue("kvmRedirect", INVALID);
        setValue("kvmPwd", DEFAULT_VALUE);
        setValue("kvmCfrmPwd", DEFAULT_VALUE);
        setValue("kvmSession", INVALID);
        setValue("timeoutuserconsent", DEFAULT_VALUE);
        setValue("timeoutidle", DEFAULT_VALUE);
        setValue("powerState", DEFAULT_VALUE);
        setValue("adminPwd", DEFAULT_VALUE);
        setValue("adminCfrmPwd", DEFAULT_VALUE);
        setValue("pingReq", INVALID);
        setValue("prvtfile", DEFAULT_VALUE);
        setValue("certfile", DEFAULT_VALUE);
        setValue("forceApply", INVALID);
        setValue("vProPrvsnPriority", NOTAVBLE);
        setValue("ip_settings", DEFAULT_VALUE);
        setValue("fqdnSettings", DEFAULT_VALUE);
        setValue("enableTLS", INVALID);
        
    }

    public String getFqdnSettings() {
		return fqdnSettings;
	}

	public void setFqdnSettings(String fqdnSettings) {
		this.fqdnSettings = fqdnSettings;
	}

	public String getIp_settings() {
        return ip_settings;
    }

    public void setIp_settings(String ip_settings) {
        this.ip_settings = ip_settings;
    }

    public void setEnableVpro(String enableVpro) {
        this.enableVpro = enableVpro;
    }

    public String getEnableVpro() {
        return enableVpro;
    }

    public void setWebUI(String webUI) {
        this.webUI = webUI;
    }

    public String getWebUI() {
        return webUI;
    }

    public void setSerialLAN(String serialLAN) {
        this.serialLAN = serialLAN;
    }

    public String getSerialLAN() {
        return serialLAN;
    }

    public void setIdeRedirect(String ideRedirect) {
        this.ideRedirect = ideRedirect;
    }

    public String getIdeRedirect() {
        return ideRedirect;
    }

    public void setKvmRedirect(String kvmRedirect) {
        this.kvmRedirect = kvmRedirect;
    }

    public String getKvmRedirect() {
        return kvmRedirect;
    }

    public void setKvmPwd(String kvmPwd) {
        this.kvmPwd = kvmPwd;
    }

    public String getKvmPwd() {
        return kvmPwd;
    }

    public void setKvmCfrmPwd(String kvmCfrmPwd) {
        this.kvmCfrmPwd = kvmCfrmPwd;
    }

    public String getKvmCfrmPwd() {
        return kvmCfrmPwd;
    }

    public void setKvmSession(String kvmSession) {
        this.kvmSession = kvmSession;
    }

    public String getKvmSession() {
        return kvmSession;
    }

    public void setTimeoutuserconsent(String timeoutuserconsent) {
        this.timeoutuserconsent = timeoutuserconsent;
    }

    public String getTimeoutuserconsent() {
        return timeoutuserconsent;
    }


     public void setTimeoutidle(String timeoutidle) {
        this.timeoutidle = timeoutidle;
    }

    public String getTimeoutidle() {
        return timeoutidle;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    public String getPowerState() {
        return powerState;
    }

    public void setAdminPwd(String adminPwd) {
        this.adminPwd = adminPwd;
    }

    public String getAdminPwd() {
        return adminPwd;
    }

    public void setAdminCfrmPwd(String adminCfrmPwd) {
        this.adminCfrmPwd = adminCfrmPwd;
    }

    public String getAdminCfrmPwd() {
        return adminCfrmPwd;
    }

    public void setPingReq(String pingReq) {
        this.pingReq = pingReq;
    }

    public String getPingReq() {
        return pingReq;
    }

    public void setPrvtfile(String prvtfile) {
        this.prvtfile = prvtfile;
    }

    public String getPrvtfile() {
        return prvtfile;
    }

    public void setCertfile(String certfile) {
        this.certfile = certfile;
    }

    public String getCertfile() {
        return certfile;
    }

    public String getEnableTLS() {
        return this.enableTLS;
    }

    public void setEnableTLS(String enableTLS) {
        this.enableTLS = enableTLS;
    }

}

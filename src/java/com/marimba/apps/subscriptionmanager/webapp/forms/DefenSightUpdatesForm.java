// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.securitymgr.view.SecurityUpdateDetailsBean;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.castanet.ILauncher;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.castanet.IActive;
import com.marimba.intf.util.*;
import com.marimba.webapps.intf.IMapProperty;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.List;

/**
 * DefenSight Updates Form
 * w.r.t update sync operation task
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */
public class DefenSightUpdatesForm extends AbstractForm implements IMapProperty {
    String action;
    private String vdefDefaultValue;
    private String responseMsg;
    private String definationUpdateResponse;



    public void initialize(SubscriptionMain smmain) {
        IConfig tunerConfig = (IConfig) smmain.getFeatures().getChild("tunerConfig");
        IWorkspace workspace = (IWorkspace) smmain.getFeatures().getChild("workspace");
        String securityInfoChnlUrl = smmain.getConfig().getProperty("subscriptionmanager.securityinfo.url");
        IChannel channel = workspace.getChannel(securityInfoChnlUrl);

        if (null == channel) {
            setValue("scapUpdateAvailable", "false");
            return;
        }
        String propValue = tunerConfig.getProperty("marimba.securityinfo.sync.status." + channel.getURL());
        String propValueSyncTime = tunerConfig.getProperty("marimba.securityinfo.sync.time." + channel.getURL());
        String propSyncProgressStatus = tunerConfig.getProperty("marimba.securityinfo.sync.inprogress.count." + channel.getURL());
        if (null == propValue || propValue.trim().isEmpty()) {
            setValue("scapUpdateAvailable", "false");
        } else {
            setValue("scapUpdateAvailable", "true");
            setValue("scapUpdateStatus", propValue);
            setValue("scapUpdateInProgressStatus", propSyncProgressStatus);
        }
        if (null != propValueSyncTime && !propValueSyncTime.trim().isEmpty()) {
            String lastSysnctime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Long.parseLong(propValueSyncTime));
            setValue("scapUpdateTime", lastSysnctime);
        }
    }

    public void setValue(String property,
                         Object value) {
        props.put(property, value);
    }

    public Object getValue(String property) {
        return props.get(property);
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        props.clear();
        super.reset(mapping, request);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getVdefDefaultValue() {
        return vdefDefaultValue;
    }

    public void setVdefDefaultValue(String vdefDefaultValue) {
        this.vdefDefaultValue = vdefDefaultValue;
    }

    public String getDefinationUpdateResponse() {
        return definationUpdateResponse;
    }

    public void setDefinationUpdateResponse(String definationUpdateResponse) {
        this.definationUpdateResponse = definationUpdateResponse;
    }


}
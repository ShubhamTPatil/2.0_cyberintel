// Copyright 1997-2017, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
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


public class VDeskUpdatesForm extends AbstractForm implements IMapProperty {
	String action;

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
}

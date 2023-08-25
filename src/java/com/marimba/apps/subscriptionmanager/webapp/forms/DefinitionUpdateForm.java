// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionForm;

/**
 *	  Definitions Update Form w.r.t handle definitions update
 *	  and CVE JSON operations (automated)
 *   @author Nandakumar Sankaralingam
 *   Version: $Revision$, $Date$
 *
 **/

public class DefinitionUpdateForm extends AbstractForm {

	private String responseMsg;
	private String definationUpdateResponse;

    private String action = null;
    private String publishTxUrl;
    private String publishUserName;
    private String publishPassword;
    private String channelStoreUserName;
    private String channelStorePassword;
    private String vdefLastUpdated;
    private String cveStorageDir;
    private String cveJsonLastUpdated;
    private int updateCvejsonStartStep;
    private boolean cveJsonUpdateThreadRunning;
    private int cveJsonUpdateStep;
    private String cveJsonUpdateMsg;
    private String cveJsonUpdateError;
    private String vDefError;
    private boolean forceUpdate;

    public boolean isForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public String getCveStorageDir() {
        return cveStorageDir;
    }

    public void setCveStorageDir(String cveStorageDir) {
        this.cveStorageDir = cveStorageDir;
    }


    public String getCveJsonLastUpdated() {
        return cveJsonLastUpdated;
    }

    public void setCveJsonLastUpdated(String cveJsonLastUpdated) {
        this.cveJsonLastUpdated = cveJsonLastUpdated;
    }

    public String getVdefLastUpdated() {
        return vdefLastUpdated;
    }

    public void setVdefLastUpdated(String vdefLastUpdated) {
        this.vdefLastUpdated = vdefLastUpdated;
    }

    public String getPublishTxUrl() {
        return publishTxUrl;
    }

    public void setPublishTxUrl(String publishTxUrl) {
        this.publishTxUrl = publishTxUrl;
    }

    public String getPublishUserName() {
        return publishUserName;
    }

    public void setPublishUserName(String publishUserName) {
        this.publishUserName = publishUserName;
    }

    public String getPublishPassword() {
        return publishPassword;
    }

    public void setPublishPassword(String publishPassword) {
        this.publishPassword = publishPassword;
    }

    public String getChannelStoreUserName() {
        return channelStoreUserName;
    }

    public void setChannelStoreUserName(String channelStoreUserName) {
        this.channelStoreUserName = channelStoreUserName;
    }

    public String getChannelStorePassword() {
        return channelStorePassword;
    }

    public void setChannelStorePassword(String channelStorePassword) {
        this.channelStorePassword = channelStorePassword;
    }



	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}


	public String getDefinationUpdateResponse() {
		return definationUpdateResponse;
	}

	public void setDefinationUpdateResponse(String definationUpdateResponse) {
		this.definationUpdateResponse = definationUpdateResponse;
	}
	

	public int getUpdateCvejsonStartStep() {
		return updateCvejsonStartStep;
	}

	public void setUpdateCvejsonStartStep(int updateCvejsonStartStep) {
		this.updateCvejsonStartStep = updateCvejsonStartStep;
	}

	public boolean isCveJsonUpdateThreadRunning() {
		return cveJsonUpdateThreadRunning;
	}

	public void setCveJsonUpdateThreadRunning(boolean cveJsonUpdateThreadRunning) {
		this.cveJsonUpdateThreadRunning = cveJsonUpdateThreadRunning;
	}

	public int getCveJsonUpdateStep() {
		return cveJsonUpdateStep;
	}

	public void setCveJsonUpdateStep(int cveJsonUpdateStep) {
		this.cveJsonUpdateStep = cveJsonUpdateStep;
	}

	public String getCveJsonUpdateMsg() {
		return cveJsonUpdateMsg;
	}

	public void setCveJsonUpdateMsg(String cveJsonUpdateMsg) {
		this.cveJsonUpdateMsg = cveJsonUpdateMsg;
	}

	public String getCveJsonUpdateError() {
		return cveJsonUpdateError;
	}

	public void setCveJsonUpdateError(String cveJsonUpdateError) {
		this.cveJsonUpdateError = cveJsonUpdateError;
	}

	public String getvDefError() {
		return vDefError;
	}

	public void setvDefError(String vDefError) {
		this.vDefError = vDefError;
	}

}

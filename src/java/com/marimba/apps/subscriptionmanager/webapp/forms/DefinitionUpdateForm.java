// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionForm;

/**
 * Used to handle run scan results.
 *
 * @author Inmkaklij
 * @version 1.1, 09/25/2001
 */
public class DefinitionUpdateForm extends AbstractForm {

	private String vdefDefaultValue;
	private String responseMsg;
	private String definationUpdateResponse;

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

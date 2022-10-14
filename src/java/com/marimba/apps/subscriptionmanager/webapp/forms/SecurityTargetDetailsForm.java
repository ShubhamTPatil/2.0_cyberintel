package com.marimba.apps.subscriptionmanager.webapp.forms;

import java.util.ArrayList;
import java.util.List;

import com.marimba.apps.subscriptionmanager.beans.SecurityTargetDetailsBean;

public class SecurityTargetDetailsForm extends AbstractForm {
	String targetType;
	String targetName;
	String targetId;
	List<SecurityTargetDetailsBean> assignSecurityDetailsBean = new ArrayList<SecurityTargetDetailsBean>();
	
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public List<SecurityTargetDetailsBean> getAssignSecurityDetailsBean() {
		return assignSecurityDetailsBean;
	}
	public void setAssignSecurityDetailsBean(
			List<SecurityTargetDetailsBean> assignSecurityDetailsBean) {
		this.assignSecurityDetailsBean = assignSecurityDetailsBean;
	}
}

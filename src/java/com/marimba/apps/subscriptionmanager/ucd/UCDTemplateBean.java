// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$
package com.marimba.apps.subscriptionmanager.ucd;

import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
/**
 * This class for storing UCD template bean
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$,  $Date$ 
 */
public class UCDTemplateBean extends AbstractForm {
	
	private String templateType;
	private String templateName;
	private String templateDesc;
	
	public UCDTemplateBean(String templateName, String templateDesc) {
		this.templateName = templateName;
		this.templateDesc = templateDesc;
	}
	
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getTemplateDesc() {
		return templateDesc;
	}
	public void setTemplateDesc(String templateDesc) {
		this.templateDesc = templateDesc;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}
	
	
}

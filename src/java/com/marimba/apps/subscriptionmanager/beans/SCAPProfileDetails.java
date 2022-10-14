// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import com.marimba.oval.util.xml.profiles.OVALProfileDefinition;

import java.util.ArrayList;

public class SCAPProfileDetails {
	String id;
	String title;
	String description;
	ArrayList<OVALProfileDefinition> definitions = new ArrayList<OVALProfileDefinition>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<OVALProfileDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(ArrayList<OVALProfileDefinition> definitions) {
		this.definitions = definitions;
	}
}

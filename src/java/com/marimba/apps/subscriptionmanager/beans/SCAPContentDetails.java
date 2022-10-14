// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.util.ArrayList;
import java.util.List;
import com.marimba.xccdf.util.xml.groups.*;

public class SCAPContentDetails {
	String scapType; // firefox or RHEL6 or JRE or RHEL 7
    String id;
	String fileName;
	String title;
	String description;
	String platform;
    String xccdfFileLocation;
    int countProfile = 0;
    int countGroup = 0;
    List<SCAPProfileDetails> profileDetails = new ArrayList<SCAPProfileDetails>();
    List<XCCDFGroup> groups = new ArrayList<XCCDFGroup>();

	String ovalProductVersion;
	String ovalProductName;
	String ovalModuleName;
	String ovalTimestamp;
	String ovalGeneratorSchemaVersion;
	String ovalGeneratorProductVersion;
	String ovalGeneratorProductName;
	String ovalGeneratorTimestamp;
	String ovalGeneratorContentVersion;
	int ovalTotalDefinitions;
	int ovalTotalComplianceDefinitions;
	int ovalTotalInventoryDefinitions;
	int ovalTotalMiscellaneousDefinitions;
	int ovalTotalPatchDefinitions;
	int ovalTotalVulnerabilityDefinitions;
	int ovalTotalTests;
	int ovalTotalObjects;
	int ovalTotalStates;
	int ovalTotalVariables;

    public String getScapType() {
		return scapType;
	}
	public void setScapType(String scapType) {
		this.scapType = scapType;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public int getCountProfile() {
		return countProfile;
	}
	public void setCountProfile(int countProfile) {
		this.countProfile = countProfile;
	}
	public List<SCAPProfileDetails> getProfileDetails() {
		return profileDetails;
	}
	public void setProfileDetails(List<SCAPProfileDetails> profileDetails) {
		this.profileDetails = profileDetails;
	}

    public int getCountGroup() {
        return countGroup;
    }

    public void setCountGroup(int countGroup) {
        this.countGroup = countGroup;
    }

    public List<XCCDFGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<XCCDFGroup> groups) {
        this.groups = groups;
    }

    public String getXccdfFileLocation() {
        return xccdfFileLocation;
    }

    public void setXccdfFileLocation(String xccdfFileLocation) {
        this.xccdfFileLocation = xccdfFileLocation;
    }

	public String getOvalProductVersion() {
		return ovalProductVersion;
	}

	public void setOvalProductVersion(String ovalProductVersion) {
		this.ovalProductVersion = ovalProductVersion;
	}

	public String getOvalProductName() {
		return ovalProductName;
	}

	public void setOvalProductName(String ovalProductName) {
		this.ovalProductName = ovalProductName;
	}

	public String getOvalModuleName() {
		return ovalModuleName;
	}

	public void setOvalModuleName(String ovalModuleName) {
		this.ovalModuleName = ovalModuleName;
	}

	public String getOvalTimestamp() {
		return ovalTimestamp;
	}

	public void setOvalTimestamp(String ovalTimestamp) {
		this.ovalTimestamp = ovalTimestamp;
	}

	public String getOvalGeneratorSchemaVersion() {
		return ovalGeneratorSchemaVersion;
	}

	public void setOvalGeneratorSchemaVersion(String ovalGeneratorSchemaVersion) {
		this.ovalGeneratorSchemaVersion = ovalGeneratorSchemaVersion;
	}

	public String getOvalGeneratorProductVersion() {
		return ovalGeneratorProductVersion;
	}

	public void setOvalGeneratorProductVersion(String ovalGeneratorProductVersion) {
		this.ovalGeneratorProductVersion = ovalGeneratorProductVersion;
	}

	public String getOvalGeneratorProductName() {
		return ovalGeneratorProductName;
	}

	public void setOvalGeneratorProductName(String ovalGeneratorProductName) {
		this.ovalGeneratorProductName = ovalGeneratorProductName;
	}

	public String getOvalGeneratorTimestamp() {
		return ovalGeneratorTimestamp;
	}

	public void setOvalGeneratorTimestamp(String ovalGeneratorTimestamp) {
		this.ovalGeneratorTimestamp = ovalGeneratorTimestamp;
	}

	public String getOvalGeneratorContentVersion() {
		return ovalGeneratorContentVersion;
	}

	public void setOvalGeneratorContentVersion(String ovalGeneratorContentVersion) {
		this.ovalGeneratorContentVersion = ovalGeneratorContentVersion;
	}

	public int getOvalTotalDefinitions() {
		return ovalTotalDefinitions;
	}

	public void setOvalTotalDefinitions(int ovalTotalDefinitions) {
		this.ovalTotalDefinitions = ovalTotalDefinitions;
	}

	public int getOvalTotalComplianceDefinitions() {
		return ovalTotalComplianceDefinitions;
	}

	public void setOvalTotalComplianceDefinitions(int ovalTotalComplianceDefinitions) {
		this.ovalTotalComplianceDefinitions = ovalTotalComplianceDefinitions;
	}

	public int getOvalTotalInventoryDefinitions() {
		return ovalTotalInventoryDefinitions;
	}

	public void setOvalTotalInventoryDefinitions(int ovalTotalInventoryDefinitions) {
		this.ovalTotalInventoryDefinitions = ovalTotalInventoryDefinitions;
	}

	public int getOvalTotalMiscellaneousDefinitions() {
		return ovalTotalMiscellaneousDefinitions;
	}

	public void setOvalTotalMiscellaneousDefinitions(int ovalTotalMiscellaneousDefinitions) {
		this.ovalTotalMiscellaneousDefinitions = ovalTotalMiscellaneousDefinitions;
	}

	public int getOvalTotalPatchDefinitions() {
		return ovalTotalPatchDefinitions;
	}

	public void setOvalTotalPatchDefinitions(int ovalTotalPatchDefinitions) {
		this.ovalTotalPatchDefinitions = ovalTotalPatchDefinitions;
	}

	public int getOvalTotalVulnerabilityDefinitions() {
		return ovalTotalVulnerabilityDefinitions;
	}

	public void setOvalTotalVulnerabilityDefinitions(int ovalTotalVulnerabilityDefinitions) {
		this.ovalTotalVulnerabilityDefinitions = ovalTotalVulnerabilityDefinitions;
	}

	public int getOvalTotalTests() {
		return ovalTotalTests;
	}

	public void setOvalTotalTests(int ovalTotalTests) {
		this.ovalTotalTests = ovalTotalTests;
	}

	public int getOvalTotalObjects() {
		return ovalTotalObjects;
	}

	public void setOvalTotalObjects(int ovalTotalObjects) {
		this.ovalTotalObjects = ovalTotalObjects;
	}

	public int getOvalTotalStates() {
		return ovalTotalStates;
	}

	public void setOvalTotalStates(int ovalTotalStates) {
		this.ovalTotalStates = ovalTotalStates;
	}

	public int getOvalTotalVariables() {
		return ovalTotalVariables;
	}

	public void setOvalTotalVariables(int ovalTotalVariables) {
		this.ovalTotalVariables = ovalTotalVariables;
	}

	public String toString() {
		return "SCAPContentDetails{" +
				"scapType='" + scapType + '\'' +
				", id='" + id + '\'' +
				", fileName='" + fileName + '\'' +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", platform='" + platform + '\'' +
				", xccdfFileLocation='" + xccdfFileLocation + '\'' +
				", countProfile=" + countProfile +
				", countGroup=" + countGroup +
				", profileDetails=" + profileDetails +
				", groups=" + groups +
				", ovalProductVersion='" + ovalProductVersion + '\'' +
				", ovalProductName='" + ovalProductName + '\'' +
				", ovalModuleName='" + ovalModuleName + '\'' +
				", ovalTimestamp='" + ovalTimestamp + '\'' +
				", ovalGeneratorSchemaVersion='" + ovalGeneratorSchemaVersion + '\'' +
				", ovalGeneratorProductVersion='" + ovalGeneratorProductVersion + '\'' +
				", ovalGeneratorProductName='" + ovalGeneratorProductName + '\'' +
				", ovalGeneratorTimestamp='" + ovalGeneratorTimestamp + '\'' +
				", ovalGeneratorContentVersion='" + ovalGeneratorContentVersion + '\'' +
				", ovalTotalDefinitions=" + ovalTotalDefinitions +
				", ovalTotalComplianceDefinitions=" + ovalTotalComplianceDefinitions +
				", ovalTotalInventoryDefinitions=" + ovalTotalInventoryDefinitions +
				", ovalTotalMiscellaneousDefinitions=" + ovalTotalMiscellaneousDefinitions +
				", ovalTotalPatchDefinitions=" + ovalTotalPatchDefinitions +
				", ovalTotalVulnerabilityDefinitions=" + ovalTotalVulnerabilityDefinitions +
				", ovalTotalTests=" + ovalTotalTests +
				", ovalTotalObjects=" + ovalTotalObjects +
				", ovalTotalStates=" + ovalTotalStates +
				", ovalTotalVariables=" + ovalTotalVariables +
				'}';
	}
}

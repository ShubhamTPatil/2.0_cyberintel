// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

public class SecurityTargetDetailsBean {
	String targetID;
	String targetName;
	String targetType;
	String xmlType;
	String selectedSecurityContentName;
	String selectedContentFileName;
    String selectedContentTitle;
    String selectedContentId;
    String selectedProfileId;
	String selectedProfileTitle;
	String customTemplateName;
	String assginedToName;
	String assginedToType;
	String assignedToID;
	String categoryType;
	String complaintLevel;
    String compliantCount;
    String nonCompliantCount;
    String notApplicableCount;
    String checkinCount;
    String targetOS;
    int profileCount;
    long startTime;
	long finishTime;
	long lastPolicyUpdateTime;
    String lastPolicyUpdateTimeInString;
    Collection<SecurityProfileDetailsBean> profiles;
    Map<String, String> rulesCompliance = new HashMap<String, String>();

	public String getTargetID() {
		return targetID;
	}
	public void setTargetID(String targetID) {
		this.targetID = targetID;
	}
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public String getTargetType() {
		return targetType;
	}
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	public String getXmlType() {
		return xmlType;
	}
	public void setXmlType(String xmlType) {
		this.xmlType = xmlType;
	}
	public String getSelectedSecurityContentName() {
		return selectedSecurityContentName;
	}
	public void setSelectedSecurityContentName(String selectedSecurityContentName) {
		this.selectedSecurityContentName = selectedSecurityContentName;
	}
	public String getSelectedContentFileName() {
		return selectedContentFileName;
	}
	public void setSelectedContentFileName(String selectedContentFileName) {
		this.selectedContentFileName = selectedContentFileName;
	}

    public String getSelectedContentTitle() {
        return selectedContentTitle;
    }

    public void setSelectedContentTitle(String selectedContentTitle) {
        this.selectedContentTitle = selectedContentTitle;
    }

    public String getSelectedProfileId() {
		return selectedProfileId;
	}
	public void setSelectedProfileId(String selectedProfileId) {
		this.selectedProfileId = selectedProfileId;
	}
	public String getSelectedProfileTitle() {
		return selectedProfileTitle;
	}
	public void setSelectedProfileTitle(String selectedProfileTitle) {
		this.selectedProfileTitle = selectedProfileTitle;
	}
	public String getAssginedToName() {
		return assginedToName;
	}
	public void setAssginedToName(String assginedToName) {
		this.assginedToName = assginedToName;
	}
	public String getAssginedToType() {
		return assginedToType;
	}
	public void setAssginedToType(String assginedToType) {
		this.assginedToType = assginedToType;
	}
	public String getAssignedToID() {
		return assignedToID;
	}
	public void setAssignedToID(String assignedToID) {
		this.assignedToID = assignedToID;
	}
	public String getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}
	public String getComplaintLevel() {
		return complaintLevel;
	}
	public void setComplaintLevel(String complaintLevel) {
		this.complaintLevel = complaintLevel;
	}

    public String getCheckinCount() {
        return checkinCount;
    }
    public void setCheckinCount(String checkinCount) {
        this.checkinCount = checkinCount;
    }

    public String getNonCompliantCount() {
        return nonCompliantCount;
    }
    public void setNonCompliantCount(String nonCompliantCount) {
        this.nonCompliantCount = nonCompliantCount;
    }

    public String getCompliantCount() {
        return compliantCount;
    }
    public void setCompliantCount(String compliantCount) {
        this.compliantCount = compliantCount;
    }

    public String getNotApplicableCount() {
        return notApplicableCount;
    }

    public void setNotApplicableCount(String notApplicableCount) {
        this.notApplicableCount = notApplicableCount;
    }

    public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}
	public long getLastPolicyUpdateTime() {
		return lastPolicyUpdateTime;
	}
	public void setLastPolicyUpdateTime(long lastPolicyUpdateTime) {
		this.lastPolicyUpdateTime = lastPolicyUpdateTime;
	}
	public Map<String, String> getRulesCompliance() {
		return rulesCompliance;
	}
	public void setRulesCompliance(Map<String, String> rulesCompliance) {
		this.rulesCompliance = rulesCompliance;
	}

    public int getProfileCount() {
        return profileCount;
    }

    public void setProfileCount(int profileCount) {
        this.profileCount = profileCount;
    }

    public String getTargetOS() {
        return targetOS;
    }

    public void setTargetOS(String targetOS) {
        this.targetOS = targetOS;
    }

    public String getLastPolicyUpdateTimeInString() {
        return lastPolicyUpdateTimeInString;
    }

    public void setLastPolicyUpdateTimeInString(String lastPolicyUpdateTimeInString) {
        this.lastPolicyUpdateTimeInString = lastPolicyUpdateTimeInString;
    }
    public String getSelectedContentId() {
		return selectedContentId;
	}
	public void setSelectedContentId(String selectedContentId) {
		this.selectedContentId = selectedContentId;
	}
	public Collection<SecurityProfileDetailsBean> getProfiles() {
        if (profiles == null) {
            profiles = (Collection) new ArrayList<SecurityProfileDetailsBean>();
        }
        return profiles;
    }

    public void setProfiles(Collection<SecurityProfileDetailsBean> profiles) {
        this.profiles = profiles;
    }
	public String getCustomTemplateName() {
		return customTemplateName;
	}
	public void setCustomTemplateName(String customTemplateName) {
		this.customTemplateName = customTemplateName;
	}
    
}

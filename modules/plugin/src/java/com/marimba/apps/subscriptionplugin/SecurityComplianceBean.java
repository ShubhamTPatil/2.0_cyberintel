// Copyright 2018, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionplugin;

import java.util.HashMap;

public class SecurityComplianceBean {
	private String scanType;
    private String contentType;
	private String contentId;
	private String contentTitle;
	private String contentFileName;
	private String contentTargetOS;
    private String templateName;
	private String profileID;
	private String profileTitle;
	private String scannedBy;
	private String targetName;
	private String overallCompliance;
    private String individualCompliance;
	private String machineName;
	private long startTime;
	private long finishTime;
	private long lastPolicyUpdateTime;
	private HashMap<String, String> rulesCompliance = new HashMap<String, String>();
    private HashMap<String, String[]> ruleAndGroupMap = new HashMap<String, String[]>();

	public String getScanType() {
		return scanType;
	}

	public void setScanType(String scanType) {
		this.scanType = scanType;
	}

	public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getContentTitle() {
		return contentTitle;
	}

	public void setContentTitle(String contentTitle) {
		this.contentTitle = contentTitle;
	}

	public String getContentFileName() {
		return contentFileName;
	}

	public void setContentFileName(String contentFileName) {
		this.contentFileName = contentFileName;
	}

	public String getContentTargetOS() {
		return contentTargetOS;
	}

	public void setContentTargetOS(String contentTargetOS) {
		this.contentTargetOS = contentTargetOS;
	}

	public String getProfileID() {
		return profileID;
	}

	public void setProfileID(String profileID) {
		this.profileID = profileID;
	}

	public String getProfileTitle() {
		return profileTitle;
	}

	public void setProfileTitle(String profileTitle) {
		this.profileTitle = profileTitle;
	}

	public String getScannedBy() {
		return scannedBy;
	}

	public void setScannedBy(String scannedBy) {
		this.scannedBy = scannedBy;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getOverallCompliance() {
		return overallCompliance;
	}

	public void setOverallCompliance(String overallCompliance) {
		this.overallCompliance = overallCompliance;
	}

    public String getIndividualCompliance() {
        return individualCompliance;
    }

    public void setIndividualCompliance(String individualCompliance) {
        this.individualCompliance = individualCompliance;
    }

    public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
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

	public HashMap<String, String> getRulesCompliance() {
		return rulesCompliance;
	}

	public void setRulesCompliance(HashMap<String, String> rulesCompliance) {
		this.rulesCompliance = rulesCompliance;
	}

    public void addRulesCompliance(String ruleID, String ruleCompliance) {
        this.rulesCompliance.put(ruleID, ruleCompliance);
    }

    public HashMap<String, String[]> getRuleAndGroupMap() {
        return ruleAndGroupMap;
    }

    public void setRuleAndGroupMap(HashMap<String, String[]> ruleAndGroupMap) {
        this.ruleAndGroupMap = ruleAndGroupMap;
    }

    public void addRuleAndGroupMap(String ruleName, String[] group) {
        this.ruleAndGroupMap.put(ruleName, group);
    }

	public java.lang.String toString() {
		return "SecurityComplianceBean{" +
				"scanType='" + scanType + '\'' +
				", contentType='" + contentType + '\'' +
				", contentId='" + contentId + '\'' +
				", contentTitle='" + contentTitle + '\'' +
				", contentFileName='" + contentFileName + '\'' +
				", contentTargetOS='" + contentTargetOS + '\'' +
				", templateName='" + templateName + '\'' +
				", profileID='" + profileID + '\'' +
				", profileTitle='" + profileTitle + '\'' +
				", scannedBy='" + scannedBy + '\'' +
				", targetName='" + targetName + '\'' +
				", overallCompliance='" + overallCompliance + '\'' +
				", individualCompliance='" + individualCompliance + '\'' +
				", machineName='" + machineName + '\'' +
				", startTime=" + startTime +
				", finishTime=" + finishTime +
				", lastPolicyUpdateTime=" + lastPolicyUpdateTime +
				", rulesCompliance=" + rulesCompliance +
				", ruleAndGroupMap=" + ruleAndGroupMap +
				'}';
	}
}
package com.marimba.apps.subscriptionmanager.beans;

import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: inelaiyara
 * Date: Mar 30, 2017
 * Time: 7:26:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityProfileDetailsBean {
    String targetID;
    String targetName;
    String targetType;
    String targetOS;
    String assginedToName;
    String assginedToType;
    String assignedToID;
    String categoryType;
    String complaintLevel;
    String compliantCount;
    String nonCompliantCount;
    String checkinCount;
    String contentId;
    String contentName;
    String contentFileName;
    String contentTitle;
    String contentDescription;
    int profileId;
    String profileName;
    String profileTitle;
    String profileDescription;
    String templateName;
    String startTime;
    String finishTime;
    String lastPolicyUpdateTime;
    String performedBy;

    int rulesCount;

    int passedRulesCount;
    int failedRulesCount;
    int otherRulesCount;

    String passedRulesPercentage;
    String failedRulesPercentage;
    String otherRulesPercentage;

    int failedRulesHighSeverity;
    int failedRulesMediumSeverity;
    int failedRulesLowSeverity;
    int failedRulesOtherSeverity;

    String failedRulesHighSeverityPercentage;
    String failedRulesMediumSeverityPercentage;
    String failedRulesLowSeverityPercentage;
    String failedRulesOtherSeverityPercentage;

    String rulesJSONResult;
    HashMap<String, String> rulesComplianceMap;
    HashMap<String, SecurityRuleBean> rulesMap;
    HashMap<String, Object> rulesValueMap;

    Collection<SecurityGroupBean> groups;
    Collection<SecurityRuleBean> rules;

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

    public String getTargetOS() {
        return targetOS;
    }

    public void setTargetOS(String targetOS) {
        this.targetOS = targetOS;
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

    public String getCompliantCount() {
        return compliantCount;
    }

    public void setCompliantCount(String compliantCount) {
        this.compliantCount = compliantCount;
    }

    public String getNonCompliantCount() {
        return nonCompliantCount;
    }

    public void setNonCompliantCount(String nonCompliantCount) {
        this.nonCompliantCount = nonCompliantCount;
    }

    public String getCheckinCount() {
        return checkinCount;
    }

    public void setCheckinCount(String checkinCount) {
        this.checkinCount = checkinCount;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getContentName() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public void setContentFileName(String contentFileName) {
        this.contentFileName = contentFileName;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileTitle() {
        return profileTitle;
    }

    public void setProfileTitle(String profileTitle) {
        this.profileTitle = profileTitle;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getLastPolicyUpdateTime() {
        return lastPolicyUpdateTime;
    }

    public void setLastPolicyUpdateTime(String lastPolicyUpdateTime) {
        this.lastPolicyUpdateTime = lastPolicyUpdateTime;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
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

    public int getPassedRulesCount() {
        return passedRulesCount;
    }

    public void setPassedRulesCount(int passedRulesCount) {
        this.passedRulesCount = passedRulesCount;
    }

    public int getFailedRulesCount() {
        return failedRulesCount;
    }

    public void setFailedRulesCount(int failedRulesCount) {
        this.failedRulesCount = failedRulesCount;
    }

    public int getOtherRulesCount() {
        return otherRulesCount;
    }

    public void setOtherRulesCount(int otherRulesCount) {
        this.otherRulesCount = otherRulesCount;
    }

    public String getPassedRulesPercentage() {
        if (getRulesCount() != 0) {
            passedRulesPercentage = String.format("%.2f", 100d * passedRulesCount / getRulesCount());
        } else {
            passedRulesPercentage = "0";
        }
        return passedRulesPercentage;
    }

    public void setPassedRulesPercentage(String passedRulesPercentage) {
        this.passedRulesPercentage = passedRulesPercentage;
    }

    public String getFailedRulesPercentage() {
        if (getRulesCount() != 0) {
            failedRulesPercentage = String.format("%.2f", 100d * failedRulesCount / getRulesCount());
        } else {
            failedRulesPercentage = "0";
        }
        return failedRulesPercentage;
    }

    public void setFailedRulesPercentage(String failedRulesPercentage) {
        this.failedRulesPercentage = failedRulesPercentage;
    }

    public String getOtherRulesPercentage() {
        if (getRulesCount() != 0) {
            otherRulesPercentage = String.format("%.2f", 100d * otherRulesCount / getRulesCount());
        } else {
            otherRulesPercentage = "0";
        }
        return otherRulesPercentage;
    }

    public void setOtherRulesPercentage(String otherRulesPercentage) {
        this.otherRulesPercentage = otherRulesPercentage;
    }

    public int getFailedRulesHighSeverity() {
        return failedRulesHighSeverity;
    }

    public void setFailedRulesHighSeverity(int failedRulesHighSeverity) {
        this.failedRulesHighSeverity = failedRulesHighSeverity;
    }

    public int getFailedRulesMediumSeverity() {
        return failedRulesMediumSeverity;
    }

    public void setFailedRulesMediumSeverity(int failedRulesMediumSeverity) {
        this.failedRulesMediumSeverity = failedRulesMediumSeverity;
    }

    public int getFailedRulesLowSeverity() {
        return failedRulesLowSeverity;
    }

    public void setFailedRulesLowSeverity(int failedRulesLowSeverity) {
        this.failedRulesLowSeverity = failedRulesLowSeverity;
    }

    public int getFailedRulesOtherSeverity() {
        return failedRulesOtherSeverity;
    }

    public void setFailedRulesOtherSeverity(int failedRulesOtherSeverity) {
        this.failedRulesOtherSeverity = failedRulesOtherSeverity;
    }

    public String getFailedRulesHighSeverityPercentage() {
        if (getFailedRulesCount() != 0) {
            failedRulesHighSeverityPercentage = String.format("%.2f", 100d * failedRulesHighSeverity / getFailedRulesCount());
        } else {
            failedRulesHighSeverityPercentage = "0";
        }
        return failedRulesHighSeverityPercentage;
    }

    public void setFailedRulesHighSeverityPercentage(String failedRulesHighSeverityPercentage) {
        this.failedRulesHighSeverityPercentage = failedRulesHighSeverityPercentage;
    }

    public String getFailedRulesMediumSeverityPercentage() {
        if (getFailedRulesCount() != 0) {
            failedRulesMediumSeverityPercentage = String.format("%.2f", 100d * failedRulesMediumSeverity / getFailedRulesCount());
        } else {
            failedRulesMediumSeverityPercentage = "0";
        }
        return failedRulesMediumSeverityPercentage;
    }

    public void setFailedRulesMediumSeverityPercentage(String failedRulesMediumSeverityPercentage) {
        this.failedRulesMediumSeverityPercentage = failedRulesMediumSeverityPercentage;
    }

    public String getFailedRulesLowSeverityPercentage() {
        if (getFailedRulesCount() != 0) {
            failedRulesLowSeverityPercentage = String.format("%.2f", 100d * failedRulesLowSeverity / getFailedRulesCount());
        } else {
            failedRulesLowSeverityPercentage = "0";
        }
        return failedRulesLowSeverityPercentage;
    }

    public void setFailedRulesLowSeverityPercentage(String failedRulesLowSeverityPercentage) {
        this.failedRulesLowSeverityPercentage = failedRulesLowSeverityPercentage;
    }

    public String getFailedRulesOtherSeverityPercentage() {
        if (getFailedRulesCount() != 0) {
            failedRulesOtherSeverityPercentage = String.format("%.2f", 100d * failedRulesOtherSeverity / getFailedRulesCount());
        } else {
            failedRulesOtherSeverityPercentage = "0";
        }
        return failedRulesOtherSeverityPercentage;
    }

    public void setFailedRulesOtherSeverityPercentage(String failedRulesOtherSeverityPercentage) {
        this.failedRulesOtherSeverityPercentage = failedRulesOtherSeverityPercentage;
    }

    public String getRulesJSONResult() {
        return rulesJSONResult;
    }

    public void setRulesJSONResult(String rulesJSONResult) {
        this.rulesJSONResult = rulesJSONResult;
    }

    public Collection<SecurityGroupBean> getGroups() {
        if (groups == null) {
            groups = (Collection) new ArrayList<SecurityGroupBean>();
        }
        return groups;
    }

    public void setGroups(Collection<SecurityGroupBean> groups) {
        this.groups = groups;
    }

    public HashMap<String, String> getRulesComplianceMap() {
        if (rulesComplianceMap == null) {
            rulesComplianceMap = new HashMap<String, String>();
        }
        return rulesComplianceMap;
    }

    public void setRulesComplianceMap(HashMap<String, String> rulesComplianceMap) {
        this.rulesComplianceMap = rulesComplianceMap;
    }

    public HashMap<String, SecurityRuleBean> getRulesMap() {
        if (rulesMap == null) {
            rulesMap = new HashMap<String, SecurityRuleBean>();
        }
        return rulesMap;
    }

    public void setRulesMap(HashMap<String, SecurityRuleBean> rulesMap) {
        this.rulesMap = rulesMap;
    }

    public Collection<SecurityRuleBean> getRules() {
        if (rulesMap == null) {
            return new ArrayList<SecurityRuleBean>();
        }
        return rulesMap.values();
    }

    public void setRules(Collection<SecurityRuleBean> rules) {
        this.rules = rules;
    }

    public HashMap<String, Object> getRulesValueMap() {
        return rulesValueMap;
    }

    public void setRulesValueMap(HashMap<String, Object> rulesValueMap) {
        this.rulesValueMap = rulesValueMap;
    }

    public int getRulesCount() {
        rulesCount = passedRulesCount + failedRulesCount + otherRulesCount;
        return rulesCount;
    }

    public void setRulesCount(int rulesCount) {
        this.rulesCount = rulesCount;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}

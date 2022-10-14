// Copyright 2019, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.io.Serializable;
import java.util.List;

import com.marimba.apps.securitymgr.utils.json.JSONArray;

public class OvalDetailBean implements Serializable {

	String contentTitle;
    String profileTitle;
    String referenceName;
    String cvssVersion;
    String cvssScore;
    String cvssBaseCore;
    String cvssImpactScore;
    String cvssExploitScore;
    String severity;
    String machineName;
    String definitionName;
    String definitionTitle;
    String definitionDesc;
    String referenceURL;
    String solution;
    String definitionClass;
    String definitionResult = "";
    String contentId;
    String lastScanTime;

    @Override
    public String toString() {
        return
                "OvalDetailBean:~'" + contentTitle + '\'' +
                ",'" + profileTitle + '\'' +
                ",'" + referenceName + '\'' +
                ",'" + cvssVersion + '\'' +
                ",'" + cvssScore + '\'' +
                ",'" + cvssBaseCore + '\'' +
                ",'" + cvssImpactScore + '\'' +
                ",'" + cvssExploitScore + '\'' +
                ",'" + severity + '\'' +
                ",'" + machineName + '\'' +
                ",'" + definitionName + '\'' +
                ",'" + definitionTitle + '\'' +
                ",'" + definitionDesc + '\'' +
                ",'" + solution + '\'' +
                ",'" + referenceURL + '\'' +
                ",'" + definitionClass + '\'' +
                ",'" + definitionResult + '\'' +

                '~';
    }

    public JSONArray getDefinitionJSONArray() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put( machineName== null ? "" : machineName);
        jsonArray.put( contentTitle == null ? "" : contentTitle);
        jsonArray.put( profileTitle == null ? "" : profileTitle);
        jsonArray.put( referenceName == null ? "" : referenceName);
        jsonArray.put( referenceURL== null ? "" : referenceURL);
        jsonArray.put( severity == null ? "" : severity);
        jsonArray.put( definitionName== null ? "" : definitionName);
        jsonArray.put( definitionTitle== null ? "" : definitionTitle);
        jsonArray.put( definitionDesc== null ? "" : definitionDesc);
        jsonArray.put( definitionClass== null ? "" : definitionClass);
        if ("fail".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                jsonArray.put("NOT-INSTALLED");
            } else {
                jsonArray.put("VULNERABLE");
            }
        } else if ("pass".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                jsonArray.put("INSTALLED");
            } else {
                jsonArray.put("NON-VULNERABLE");
            }
        } else {
            jsonArray.put( definitionResult== null ? "" : definitionResult.toUpperCase());
        }
        return jsonArray;
    }

    public JSONArray getCVEJSONArray() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put( machineName == null ? "" : machineName);
        jsonArray.put( contentTitle== null ? "" : contentTitle);
        jsonArray.put( profileTitle == null ? "" : profileTitle);
        jsonArray.put( referenceName == null ? "" : referenceName);
        jsonArray.put( referenceURL == null ? "" : referenceURL);
//        jsonArray.put( cvssVersion == null ? "" : cvssVersion);
        jsonArray.put( cvssScore == null ? "" : cvssScore);
//        jsonArray.put( cvssBaseCore == null ? "" : cvssBaseCore);
//        jsonArray.put( cvssImpactScore == null ? "" : cvssImpactScore);
//        jsonArray.put( cvssExploitScore == null ? "" : cvssExploitScore);
        jsonArray.put( severity == null ? "" : severity);
        jsonArray.put( definitionName == null ? "" : definitionName);
        jsonArray.put( definitionTitle == null ? "" : definitionTitle);
        jsonArray.put( definitionDesc == null ? "" : definitionDesc);
        jsonArray.put( definitionClass== null ? "" : definitionClass);
        if ("fail".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                jsonArray.put("NOT-INSTALLED");
            } else {
                jsonArray.put("VULNERABLE");
            }
        } else if ("pass".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                jsonArray.put("INSTALLED");
            } else {
                jsonArray.put("NON-VULNERABLE");
            }
        } else {
            jsonArray.put( definitionResult== null ? "" : definitionResult.toUpperCase());
        }
        jsonArray.put( solution == null ? "" : solution);
        jsonArray.put( lastScanTime == null ? "" : lastScanTime);
        return jsonArray;
    }
    
    public JSONArray getCVEJSONArray(List<String> filteredColumnslist) {
        JSONArray jsonArray = new JSONArray();
        
        if (filteredColumnslist.contains("machineName")) {jsonArray.put( machineName == null ? "" : machineName);}
        if (filteredColumnslist.contains("contentTitle")) {jsonArray.put( contentTitle== null ? "" : contentTitle);}
        if (filteredColumnslist.contains("profileTitle")) {jsonArray.put( profileTitle == null ? "" : profileTitle);}
        if (filteredColumnslist.contains("referenceName")) {jsonArray.put( referenceName == null ? "" : referenceName);}
        if (filteredColumnslist.contains("referenceURL")) {jsonArray.put( referenceURL == null ? "" : referenceURL);}
        if (filteredColumnslist.contains("cvssScore")) {jsonArray.put( cvssScore == null ? "" : cvssScore);}
        if (filteredColumnslist.contains("severity")) {jsonArray.put( severity == null ? "" : severity);}
        if (filteredColumnslist.contains("definitionName")) {jsonArray.put( definitionName == null ? "" : definitionName);}
        if (filteredColumnslist.contains("definitionTitle")) {jsonArray.put( definitionTitle == null ? "" : definitionTitle);}
        if (filteredColumnslist.contains("definitionDesc")) {jsonArray.put( definitionDesc == null ? "" : definitionDesc);}
        if (filteredColumnslist.contains("definitionClass")) {jsonArray.put( definitionClass== null ? "" : definitionClass);}
        if ("fail".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                if (filteredColumnslist.contains("definitionResult")) {jsonArray.put("NOT-INSTALLED");}
            } else {
                if (filteredColumnslist.contains("definitionResult")) {jsonArray.put("VULNERABLE");}
            }
        } else if ("pass".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                if (filteredColumnslist.contains("definitionResult")) {jsonArray.put("INSTALLED");}
            } else {
                if (filteredColumnslist.contains("definitionResult")) {jsonArray.put("NON-VULNERABLE");}
            }
        } else {
        	if (filteredColumnslist.contains("definitionResult")) {jsonArray.put( definitionResult== null ? "" : definitionResult.toUpperCase());}
        }
        if (filteredColumnslist.contains("solution")) {jsonArray.put( solution == null ? "" : solution);}
        jsonArray.put( lastScanTime == null ? "" : lastScanTime);
        return jsonArray;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getProfileTitle() {
        return profileTitle;
    }

    public void setProfileTitle(String profileTitle) {
        this.profileTitle = profileTitle;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public String getCvssVersion() {
        return cvssVersion;
    }

    public void setCvssVersion(String cvssVersion) {
        this.cvssVersion = cvssVersion;
    }

    public String getCvssScore() {
        return cvssScore;
    }

    public void setCvssScore(String cvssScore) {
        this.cvssScore = cvssScore;
    }

    public String getCvssBaseCore() {
        return cvssBaseCore;
    }

    public void setCvssBaseCore(String cvssBaseCore) {
        this.cvssBaseCore = cvssBaseCore;
    }

    public String getCvssImpactScore() {
        return cvssImpactScore;
    }

    public void setCvssImpactScore(String cvssImpactScore) {
        this.cvssImpactScore = cvssImpactScore;
    }

    public String getCvssExploitScore() {
        return cvssExploitScore;
    }

    public void setCvssExploitScore(String cvssExploitScore) {
        this.cvssExploitScore = cvssExploitScore;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public String getDefinitionTitle() {
        return definitionTitle;
    }

    public void setDefinitionTitle(String definitionTitle) {
        this.definitionTitle = definitionTitle;
    }

    public String getDefinitionDesc() {
        return definitionDesc;
    }

    public void setDefinitionDesc(String definitionDesc) {
        this.definitionDesc = definitionDesc;
    }

    public String getReferenceURL() {
        return referenceURL;
    }

    public void setReferenceURL(String referenceURL) {
        this.referenceURL = referenceURL;
    }

    public String getDefinitionClass() {
        return definitionClass;
    }

    public void setDefinitionClass(String definitionClass) {
        this.definitionClass = definitionClass;
    }

    public String getDefinitionResult() {
        if ("fail".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                return "NOT-INSTALLED";
            } else {
                return "VULNERABLE";
            }
        } else if ("pass".equalsIgnoreCase(definitionResult)) {
            if ("inventory".equalsIgnoreCase(definitionClass) || "compliance".equalsIgnoreCase(definitionClass)) {
                return "INSTALLED";
            } else {
                return "NON-VULNERABLE";
            }
        } else {
            return (definitionResult== null ? "" : definitionResult.toUpperCase());
        }
    }

    public void setDefinitionResult(String definitionResult) {
        this.definitionResult = definitionResult;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.contentId = solution;
    }
    public String getLastScanTime() {
		return lastScanTime;
	}

	public void setLastScanTime(String lastScanTime) {
		this.lastScanTime = lastScanTime;
	}

    public OvalDetailBean(String contentTitle, String profileTitle, String referenceName, String cvssVersion,
                          String cvssScore, String cvssBaseCore, String cvssImpactScore, String cvssExploitScore,
                          String severity, String definitionName, String definitionTitle,
                          String definitionDesc, String definitionClass, String referenceURL, String contentId, String solution) {
        this.contentTitle = contentTitle;
        this.profileTitle = profileTitle;
        this.referenceName = referenceName;
        this.cvssVersion = cvssVersion;
        this.cvssScore = cvssScore;
        this.cvssBaseCore = cvssBaseCore;
        this.cvssImpactScore = cvssImpactScore;
        this.cvssExploitScore = cvssExploitScore;
        this.severity = severity;
        this.definitionName = definitionName;
        this.definitionTitle = definitionTitle;
        this.definitionDesc = definitionDesc;
        this.definitionClass = definitionClass;
        this.referenceURL = referenceURL;
        this.definitionResult = definitionResult;
        this.contentId = contentId;
        this.solution = solution;
    }

    public OvalDetailBean() {
    }

}

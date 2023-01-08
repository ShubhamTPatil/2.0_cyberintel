// Copyright 1997-2018, Marimba. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.securitymgr.view;

import org.apache.commons.lang.StringEscapeUtils;

public class SecurityUpdateDetailsBean {
    String fileName;
    String title;
    String status;
    String platform;
    String updated;
    String target;
    String profileTitles;
    String profileIds;
    String error;
    String assessmentType;

    public SecurityUpdateDetailsBean() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return escapeJS(title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getProfileTitles() {
        return escapeJS(profileTitles);
    }

    public void setProfileTitles(String profileTitles) {
        this.profileTitles = profileTitles;
    }

    public String getProfileIds() {
        return escapeJS(profileIds);
    }

    public void setProfileIds(String profileIds) {
        this.profileIds = profileIds;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public static String escapeJS(String value) {
        return StringEscapeUtils.escapeJavaScript(value);
    }

    public String toString() {
        return "SecurityUpdateDetailsBean{" +
                "fileName='" + fileName + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", platform='" + platform + '\'' +
                ", assessmentType='" + assessmentType + '\'' +
                ", updated='" + updated + '\'' +
                ", target='" + target + '\'' +
                ", profileTitles='" + profileTitles + '\'' +
                ", profileIds='" + profileIds + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
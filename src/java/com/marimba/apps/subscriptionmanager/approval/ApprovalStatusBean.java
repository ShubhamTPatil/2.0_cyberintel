// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.approval;

/**
 * Description about the class ApprovalStatusBean
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class ApprovalStatusBean {

    private String status;
    private String remarks;
    private String createdBy;
    private String createdOn;
    private String reviewedBy;
    private String reviewedOn;
    private String targetId;
    private String targetName;
    private String targetType;
    private int changeId;
    private int approvalType;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = (null != createdBy) ? createdBy.toLowerCase() : createdBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = (null != reviewedBy) ? reviewedBy.toLowerCase() : reviewedBy;
    }

    public String getReviewedOn() {
        return reviewedOn;
    }

    public void setReviewedOn(String reviewedOn) {
        this.reviewedOn = reviewedOn;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
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

    public int getChangeId() {
        return changeId;
    }

    public void setChangeId(int changeId) {
        this.changeId = changeId;
    }

    public int getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(int approvalType) {
        this.approvalType = approvalType;
    }
}

// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;
import static com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants.MAIL_DATE_FORMAT;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
/**
 * This class is used to store list of Policies 
 * for peer approval storage
 *
 * @author Selvaraj Jegatheesan
 */

public class ApprovalPolicyDTO {
    private int changeId;
    private int policyAction;
    private int policyStatus;
    private String policyTargetId;
    private String policyTargetName;
    private String policyTargetType;
    private String ldapSource;
    private String arReferenceTag;
    private String softwarePath;
    private String txGroup;
    private String txUser;
    private String allTarget;
    private String blackoutSchedule;
    private boolean isBlackoutChanged;
    private String changeOwner;
    private String reviewedBy;
    private String remarks;
    private String created_on_str;
    private Timestamp reviewed_on;
    private Timestamp created_on;
    private List<ApprovalChannelDTO> policyChannels;
    private List<ApprovalChannelDTO> addChannels;
    private List<ApprovalChannelDTO> modifyChannels;
    private List<ApprovalChannelDTO> deleteChannels;
    private List<ApprovalPropertyDTO> tunerProps;
    private List<ApprovalPropertyDTO> addTunerProps;
    private List<ApprovalPropertyDTO> modifyTunerProps;
    private List<ApprovalPropertyDTO> deleteTunerProps;

    public ApprovalPolicyDTO() {
        this.policyChannels = new ArrayList<ApprovalChannelDTO>();
        this.addChannels = new ArrayList<ApprovalChannelDTO>();
        this.modifyChannels = new ArrayList<ApprovalChannelDTO>();
        this.deleteChannels = new ArrayList<ApprovalChannelDTO>();
        this.tunerProps = new ArrayList<ApprovalPropertyDTO>();
        this.addTunerProps = new ArrayList<ApprovalPropertyDTO>();
        this.modifyTunerProps = new ArrayList<ApprovalPropertyDTO>();
        this.deleteTunerProps = new ArrayList<ApprovalPropertyDTO>();
    }

    public int getChangeId() {
        return changeId;
    }
    public void setChangeId(int changeId) {
        this.changeId = changeId;
    }
    public String getPolicyTargetName() {
        return policyTargetName;
    }
    public void setPolicyTargetName(String policyTargetName) {
        this.policyTargetName = policyTargetName;
    }
    public String getPolicyTargetId() {
        return policyTargetId;
    }
    public void setPolicyTargetId(String policyTargetId) {
        this.policyTargetId = policyTargetId;
    }
    public String getPolicyTargetType() {
        return policyTargetType;
    }
    public void setPolicyTargetType(String policyTargetType) {
        this.policyTargetType = policyTargetType;
    }
    public String getLdapSource() {
        return ldapSource;
    }
    public void setLdapSource(String ldapSource) {
        this.ldapSource = ldapSource;
    }
    public int getPolicyAction() {
        return policyAction;
    }
    public void setPolicyAction(int policyAction) {
        this.policyAction = policyAction;
    }
    public int getPolicyStatus() {
        return policyStatus;
    }
    public void setPolicyStatus(int policyStatus) {
        this.policyStatus = policyStatus;
    }
    public String getArReferenceTag() {
        return arReferenceTag;
    }
    public void setArReferenceTag(String arReferenceTag) {
        this.arReferenceTag = arReferenceTag;
    }
    public String getSoftwarePath() {
        return softwarePath;
    }
    public void setSoftwarePath(String softwarePath) {
        this.softwarePath = softwarePath;
    }
    public String getTxGroup() {
        return txGroup;
    }
    public void setTxGroup(String txGroup) {
        this.txGroup = txGroup;
    }
    public String getTxUser() {
        return txUser;
    }
    public void setTxUser(String txUser) {
        this.txUser = txUser;
    }
    public String getAllTarget() {
        return allTarget;
    }
    public void setAllTarget(String allTarget) {
        this.allTarget = allTarget;
    }
    public String getBlackoutSchedule() {
        return blackoutSchedule;
    }
    public void setBlackoutSchedule(String blackoutSchedule) {
        this.blackoutSchedule = blackoutSchedule;
    }
    public String getChangeOwner() {
        return changeOwner;
    }
    public void setChangeOwner(String changeOwner) {
        this.changeOwner = changeOwner;
    }
    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Timestamp getReviewed_on() {
        return reviewed_on;
    }

    public void setReviewed_on(Timestamp reviewed_on) {
        this.reviewed_on = reviewed_on;
    }

    public Timestamp getCreated_on() {
        return created_on;
    }

    public void setCreated_on(Timestamp created_on) {
        this.created_on = created_on;
        this.created_on_str = new SimpleDateFormat(MAIL_DATE_FORMAT, Locale.US).format(created_on);
    }

    public String getCreated_on_str() {
        return created_on_str;
    }

    public List<ApprovalChannelDTO> getPolicyChannels() {
        return policyChannels;
    }
    public void setPolicyChannels(List<ApprovalChannelDTO> policyChannels) {
        this.policyChannels = policyChannels;
    }
    public List<ApprovalChannelDTO> getAddChannels() {
        return addChannels;
    }
    public void setAddChannels(List<ApprovalChannelDTO> addChannels) {
        this.addChannels = addChannels;
    }
    public List<ApprovalChannelDTO> getModifyChannels() {
        return modifyChannels;
    }
    public void setModifyChannels(List<ApprovalChannelDTO> modifyChannels) {
        this.modifyChannels = modifyChannels;
    }
    public List<ApprovalChannelDTO> getDeleteChannels() {
        return deleteChannels;
    }
    public void setDeleteChannels(List<ApprovalChannelDTO> deleteChannels) {
        this.deleteChannels = deleteChannels;
    }
    public List<ApprovalPropertyDTO> getTunerProps() {
        return tunerProps;
    }
    public void setTunerProps(List<ApprovalPropertyDTO> tunerProps) {
        this.tunerProps = tunerProps;
    }
    public List<ApprovalPropertyDTO> getAddTunerProps() {
        return addTunerProps;
    }

    public void addTunerProperty(ApprovalPropertyDTO tunerProps) {
        this.tunerProps.add(tunerProps);
        switch (tunerProps.getPropAction()) {
            case IApprovalPolicyConstants.ADD_OPERATION:
                addTunerProps.add(tunerProps);
                break;
            case IApprovalPolicyConstants.DELETE_OPERATION:
                deleteTunerProps.add(tunerProps);
                break;
            case IApprovalPolicyConstants.MODIFY_OPERATION:
                modifyTunerProps.add(tunerProps);
                break;
        }
    }

    public void addChannel(ApprovalChannelDTO channel) {
        this.policyChannels.add(channel);
        switch (channel.getChannelAction()) {
            case IApprovalPolicyConstants.ADD_OPERATION:
                addChannels.add(channel);
                break;
            case IApprovalPolicyConstants.DELETE_OPERATION:
                deleteChannels.add(channel);
                break;
            case IApprovalPolicyConstants.MODIFY_OPERATION:
                modifyChannels.add(channel);
                break;
        }
    }

    public void setAddTunerProps(List<ApprovalPropertyDTO> addTunerProps) {
        this.addTunerProps = addTunerProps;
    }
    public List<ApprovalPropertyDTO> getModifyTunerProps() {
        return modifyTunerProps;
    }
    public void setModifyTunerProps(List<ApprovalPropertyDTO> modifyTunerProps) {
        this.modifyTunerProps = modifyTunerProps;
    }
    public List<ApprovalPropertyDTO> getDeleteTunerProps() {
        return deleteTunerProps;
    }
    public void setDeleteTunerProps(List<ApprovalPropertyDTO> deleteTunerProps) {
        this.deleteTunerProps = deleteTunerProps;
    }

    public boolean isBlackoutChanged() {
        return isBlackoutChanged;
    }

    public void setBlackoutChanged(boolean isBlackoutChanged) {
        this.isBlackoutChanged = isBlackoutChanged;
    }

    public String toString() {
        return "ApprovalPolicyDTO{" +
                "changeId=" + changeId +
                ", policyAction=" + policyAction +
                ", policyStatus=" + policyStatus +
                ", policyTargetId='" + policyTargetId + '\'' +
                ", policyTargetName='" + policyTargetName + '\'' +
                ", policyTargetType='" + policyTargetType + '\'' +
                ", ldapSource='" + ldapSource + '\'' +
                ", arReferenceTag='" + arReferenceTag + '\'' +
                ", softwarePath='" + softwarePath + '\'' +
                ", txGroup='" + txGroup + '\'' +
                ", txUser='" + txUser + '\'' +
                ", allTarget='" + allTarget + '\'' +
                ", blackoutSchedule='" + blackoutSchedule + '\'' +
                ", changeOwner='" + changeOwner + '\'' +
                ", reviewedBy='" + reviewedBy + '\'' +
                ", created_on_str='" + created_on_str + '\'' +
                ", reviewed_on=" + reviewed_on +
                ", created_on=" + created_on +
                ", remarks='" + remarks + '\'' +
                ", isBlackoutChanged=" + isBlackoutChanged +
                '}';
    }
}

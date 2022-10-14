package com.marimba.apps.subscriptionmanager.webapp.forms;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sethumadhavan Vasudevan
 * Date: Oct 4, 2012
 * Time: 1:08:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class PolicyStatusForm extends AbstractForm {

    private List pendingPolicies;
    private List approvedPolicies;
    private List rejectedPolicies;
    private String searchPolicyStr = "";

    public List getPendingPolicies() {
        return pendingPolicies;
    }

    public void setPendingPolicies(List pendingPolicies) {
        this.pendingPolicies = pendingPolicies;
    }

    public List getApprovedPolicies() {
        return approvedPolicies;
    }

    public void setApprovedPolicies(List approvedPolicies) {
        this.approvedPolicies = approvedPolicies;
    }

    public List getRejectedPolicies() {
        return rejectedPolicies;
    }

    public void setRejectedPolicies(List rejectedPolicies) {
        this.rejectedPolicies = rejectedPolicies;
    }

	public String getSearchPolicyStr() {
		return searchPolicyStr;
	}

	public void setSearchPolicyStr(String searchPolicyStr) {
		this.searchPolicyStr = searchPolicyStr;
	}
    
}

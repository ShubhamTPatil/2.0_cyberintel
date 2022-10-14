package com.marimba.apps.subscriptionmanager.approval;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: svasudev
 * Date: Oct 5, 2012
 * Time: 2:08:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadDBPolicy {
    ApprovalPolicyStorage dbStorage;

    List pendingPolicy;
    List approvedPolicy;
    List rejectedPolicy;
    List<ApprovalPolicyDTO> policies;

    public List getPendingPolicy() {
        return pendingPolicy;
    }

    public List getApprovedPolicy() {
        return approvedPolicy;
    }

    public List getRejectedPolicy() {
        return rejectedPolicy;
    }

    public LoadDBPolicy(ApprovalPolicyStorage dbStorage) {
        this.dbStorage = dbStorage;
        this.pendingPolicy = new ArrayList();
        this.approvedPolicy = new ArrayList();
        this.rejectedPolicy = new ArrayList();
        this.policies = new ArrayList<ApprovalPolicyDTO>(10);
    }

    public List getPolicyByUser(String user) {
        List<ApprovalPolicyDTO> policies = new ArrayList<ApprovalPolicyDTO>(10);
        if (null != user) {
            policies = dbStorage.getPolicyByUser(user);
            if (policies.size() > 0) {
                System.out.println("Total number of policies retrieved for the user (" + user + ") :" + policies.size());
            }
        } else {
            System.out.println("Failed to get policy from database for the user specific");
        }
        return policies;
    }

    public List getAllPolicy() {
        policies.addAll(dbStorage.getAllPolicy());
        if (policies.size() > 0) {
            System.out.println("Total number of policies retrieved: " + policies.size());
        }
        return policies;
    }

    public ApprovalPolicyDTO loadPendingPolicyFromDB(String policyName, String user) {
        ApprovalPolicyDTO policyDTO = null;
        if (null != policyName && null != user) {
            policyDTO = dbStorage.getPolicyByUserAndPolicyName(policyName, user, String.valueOf(IApprovalPolicyConstants.POLICY_PENDING));
        }
        return policyDTO;
    }

    public ApprovalPolicyDTO loadPolicyByChangeId(int changeId) {
        ApprovalPolicyDTO policyDTO = null;
        policyDTO = dbStorage.getPolicyByChangeId(changeId);
        return policyDTO;
    }
    
    public List<ApprovalPolicyDTO> getPendingPolicyByTarget(String targetName, String targetId, String targetType) {
    	List<ApprovalPolicyDTO> policies = new ArrayList<ApprovalPolicyDTO>(10);
    	if(null != targetName && null != targetId && null != targetType) {
    		policies.addAll(dbStorage.getPendingPolicyByTarget(targetName, targetId, targetType));
    	}
    	return policies;
    }
}

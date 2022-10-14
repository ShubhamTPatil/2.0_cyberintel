// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscriptionmanager.AuditLogger;
import com.marimba.intf.msf.watchdir.IWatchAction;
import com.marimba.intf.msf.watchdir.IWatchContext;
import com.marimba.intf.msf.watchdir.IWatchDirectory;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.approval.*;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.msf.*;
//import com.marimba.intf.tuner.IServiceAutomation;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

import com.marimba.apps.subscription.common.intf.IObjectManager;
import com.marimba.intf.msf.policyapi.IPolicyManagement;
import com.marimba.policyapi.PolicyManagement;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class PeerApprovalHandler implements IWatchAction, IApprovalPolicyConstants, IAppConstants, ISubscriptionConstants {
    private SubscriptionMain main = null;
    private ITenant tenant;
    private IUser iuser = null;
    private LoadDBPolicy dbPolicy;
    private ApprovalPolicyDTO policy = null;
    private ApprovalPolicyStorage dbstorage = null;
//    private static PeerApprovalHandler peerApprovalHandler;

    public PeerApprovalHandler() {
        debug("PeerApprovalHandler() is invoked...");
    }

    public PeerApprovalHandler(SubscriptionMain _main) {
        debug("PeerApprovalHandler(main) is invoked...");
        this.main = _main;
    }

//        public static PeerApprovalHandler getInstance(SubscriptionMain _main) {
//        debug("PeerApprovalHandler.getInstance() is invoked...");
//        if (peerApprovalHandler == null) {
//            peerApprovalHandler = new PeerApprovalHandler(_main);
//        }
//        return peerApprovalHandler;
//    }

    public boolean handle(String filePath, String action, IWatchContext context) {
        debug("PeerApprovalHandler.handle() is invoked...");
        debug("filePath: " + filePath);
        debug("action: " + action);
        this.tenant = main.getTenant();
        this.dbPolicy = new LoadDBPolicy(main.getDBStorage());
        this.dbstorage = main.getDBStorage();
        this.dbstorage.initializeStorage();
        boolean requestSucceeded = true;
        String message = "";

        if (!IWatchDirectory.EVENT_CREATE.equals(action)) {
            return false;
        }

        String command = "";
        String sysId = "";
        String ticketId = "";
        String changeId = "";
        String state = "";
        String user = "";
        String remarks = "";

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(filePath);
            prop.load(input);

            command  = prop.getProperty("command");
            debug("command: " + command);

            sysId  = prop.getProperty("sysId");
            debug("sysId: " + sysId);

            ticketId  = prop.getProperty("ticketId");
            debug("ticketId: " + ticketId);

            changeId  = prop.getProperty("changeId");
            debug("changeId: " + changeId);

            user  = prop.getProperty("user");
            debug("user: " + user);

            state  = prop.getProperty("state");
            debug("state: " + state);

            remarks  = prop.getProperty("remarks");
            debug("remarks: " + remarks);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (null == command || command.trim().length() == 0) {
            message = "Invalid command";
            requestSucceeded = false;
        }

        if (null == changeId || changeId.trim().length() == 0) {
            message = "Invalid Policy change ID";
            requestSucceeded = false;
        }

        if (null == user || user.trim().length() == 0) {
            user = "Auto-Approved";
        }

        if (requestSucceeded) {
            int policyChangeId = 0;
            try {
                policyChangeId = Integer.parseInt(changeId);
            } catch(NumberFormatException ex) {
                debug("Failed to parse policy change id :" + changeId);
            }

//            IServiceAutomation serviceAutomation = main.getServiceAutomation();
//            debug("serviceAutomation - " + serviceAutomation);

            PolicyManagement policyAPI = (PolicyManagement) tenant.getManager(IPolicyManagement.SERVICE_NAME);
            IObjectManager objMgr = policyAPI.getObjectManager();
            try {
                iuser = objMgr.createUser(context.getUserPrincipal());
            } catch (Throwable t) {
                t.printStackTrace();
                iuser = null;
            }

            ApprovalStatusBean statusBean = null;
            statusBean = new ApprovalStatusBean();
            statusBean.setChangeId(policyChangeId);
            statusBean.setRemarks(remarks);
            statusBean.setReviewedBy(user);

            HashMap<String, String> requestDetails = new HashMap<String, String>();
            requestDetails.put("number", ticketId);
            requestDetails.put("sysId", sysId);
            requestDetails.put("state", state);
            requestDetails.put("user", user);
            requestDetails.put("comments", remarks);

            if(command.equals("approvedpolicy")) {
                debug("Performing policy approve request and update status to database");
                statusBean.setApprovalType(POLICY_APPROVED);
                debug("Policy Approved by :" + user);
                debug("Policy change Id :" + policyChangeId);
                debug("Policy Approved remarks :" + statusBean.getRemarks());
                requestSucceeded = updatePolicy(statusBean);
                requestDetails.put("approval", "approved");
                sendApprovalMail(statusBean);
            } else if(command.equals("rejectedpolicy")) {
                debug("Performing policy reject request and update status to database");
                debug("Policy rejected by : " + user);
                debug("Policy change id : " + policyChangeId);
                debug("Policy rejected remarks : " + statusBean.getRemarks());
                statusBean.setApprovalType(POLICY_REJECTED);
                requestSucceeded = updatePolicy(statusBean);
                requestDetails.put("approval", "rejected");
                sendApprovalMail(statusBean);
            }

            debug("requestDetails1 - " + requestDetails);
            for (Map.Entry<String, String> entry : requestDetails.entrySet()) {
                try {
                    requestDetails.put(entry.getKey(), entry.getValue().replaceAll(" ", "<space>"));
                } catch (Throwable t) {
                    //ignore...
                }
            }
            debug("requestDetails2 - " + requestDetails);

            //String requestId = serviceAutomation.handleRequest(serviceAutomation.PEER_APPROVAL_UPDATE_REQUEST, requestDetails);
            String requestId = null;
            debug("Change Request Id - " + requestId);
        }

        debug("message: " + message);
        debug("requestSucceeded: " + requestSucceeded);

        return requestSucceeded;
    }

    //    public boolean updatePolicy(int policyChangeId, int policyStatus, String remarks, String reviewdBy) {
    public boolean updatePolicy(ApprovalStatusBean statusBean) {
        boolean isUpdatedtoDB = false;
        int oldPolicystatus = 0;
        try {
            if(null == dbstorage){
                debug("Failed to update policy status in database due to database storage is empty");
                statusBean.setStatus("FAILED");
                return isUpdatedtoDB;
            }
            try {
            	// Used to compare old policy before update status
            	ApprovalPolicyDTO oldPolicy = null;
            	if (dbPolicy != null) {
            		oldPolicy = dbPolicy.loadPolicyByChangeId(statusBean.getChangeId());
            		oldPolicystatus = oldPolicy.getPolicyStatus();
            		debug("old policy status before approve or reject : " + oldPolicystatus);
            	}
                debug("Updating policies status to database for the target id: " + statusBean.getChangeId());
                //Update Approved policy change to Database
                isUpdatedtoDB = dbstorage.updatePolicyApprovalStatus(statusBean);
                if(isUpdatedtoDB) {
                    debug("Successfully updated policy status in database");
                    statusBean.setStatus("SUCCEED");
                } else {
                    debug("Failed to update policy status in database");
                    statusBean.setStatus("FAILED");
                }
            } catch(Exception e) {
                e.printStackTrace();
                debug("Failed to update policy status in database");
            }
            // Collect Policy information from temporary Database storage
            getPendingPolicyDetail(statusBean.getChangeId());
            if(isUpdatedtoDB && POLICY_REJECTED != statusBean.getApprovalType()) {

                if(null != policy) {
                    boolean policyExists = false;

                    //Check policy Exists on LDAP
                    policyExists = ObjectManager.existsSubscription(policy.getPolicyTargetId(), policy.getPolicyTargetType(), iuser);

                    ApprovedPolicySave approvedPolicy = new ApprovedPolicySave(policy, iuser, tenant);

                    debug("Policy is already exists for the target: " + policyExists);
                    debug("Policy action: " + policy.getPolicyAction());
                    // if change request coming from copy operation(pending_copyoperation) then 
                    // goes to else condition for removing the old policy if exists and coping new policy
                    if(policyExists && POLICY_PENDING_COPYOPERATION != oldPolicystatus) {
                    	// This is special case : suppose different user create a request to add new policy for same target. 
                    	// In Approver process, approver accept both request order wise. In this case, 
                    	// second request should be consider as modify even though policy action showing add operation     
                        if(MODIFY_OPERATION == policy.getPolicyAction() || ADD_OPERATION == policy.getPolicyAction()) {
                            approvedPolicy.updateExistingPolicy();
                        } else if(DELETE_OPERATION == policy.getPolicyAction()) {
                            approvedPolicy.DeletePolicy();
                        }
                    } else {
                    	// When doing copy operation old policy should be removed if already exists and copied new policy
                    	if(POLICY_PENDING_COPYOPERATION == oldPolicystatus) {
                    		if(policyExists) {
                    			ObjectManager.deleteSubscription(policy.getPolicyTargetId(), policy.getPolicyTargetType(), iuser);
                    		}
                    	}
                        if(ADD_OPERATION == policy.getPolicyAction()) {
                            approvedPolicy.addNewPolicy();
                        }
                    }
                } else {
                    debug("Failed to load approved policies from database");
                }

            }
            statusBean.setCreatedBy(policy.getChangeOwner());
            statusBean.setCreatedOn(policy.getCreated_on_str());
            statusBean.setTargetId(policy.getPolicyTargetId());
            statusBean.setRemarks(policy.getRemarks());
            statusBean.setTargetName(policy.getPolicyTargetName());
            statusBean.setTargetType(policy.getPolicyTargetType());
        } catch(Exception ex) {
            if(DEBUG5) {
                ex.printStackTrace();
            }
            debug("Failed to save approved policy change in LDAP");
        }
        return isUpdatedtoDB;
    }

    public void getPendingPolicyDetail(int changeID) {
        if (dbPolicy != null) {
            policy = dbPolicy.loadPolicyByChangeId(changeID);
        }
    }

    private void sendApprovalMail(ApprovalStatusBean statusBean) {
        ApprovalMailFormatter mailFormatter = new ApprovalMailFormatter(main.getAppResources());
        mailFormatter.setApprovalType(statusBean.getApprovalType());
        mailFormatter.setTargetId(statusBean.getTargetId());
        mailFormatter.setTargetName(statusBean.getTargetName());
        mailFormatter.setTargetType(statusBean.getTargetType());
        mailFormatter.setCreatedBy(statusBean.getCreatedBy());
        mailFormatter.setCreatedOn(statusBean.getCreatedOn());
        mailFormatter.setCreatedByMailId(main.getMailId(statusBean.getCreatedBy()));
        mailFormatter.setCreatedByDispName(main.getDisplayName(statusBean.getCreatedBy()));
        mailFormatter.setReviewedBy(statusBean.getReviewedBy());
        mailFormatter.setReviewedOn(statusBean.getReviewedOn());
        mailFormatter.setReviewedByDispName(statusBean.getReviewedBy());

        mailFormatter.setRemarks(statusBean.getRemarks());

        // start to prepare mail message
        mailFormatter.prepare();
        main.sendMail(mailFormatter);
    }

    private static void debug(String msg) {
        if (DEBUG5) System.out.println("PeerApprovalHandler: " + msg);
    }
}

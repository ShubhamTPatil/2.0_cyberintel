// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.approval.ajax;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.approval.*;
import com.marimba.intf.msf.*;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * Description about the class ApprovalHandlerServlet
 *
 * @author	 svasudev
 * @version  $Revision$,  $Date$
 *
 */

public class ApprovalHandlerServlet extends HttpServlet implements IWebAppsConstants, IApprovalPolicyConstants,IAppConstants {
    private IUser iuser = null;
    private LoadDBPolicy dbPolicy;
    private HttpSession session = null;
    private SubscriptionMain main = null;
    private ApprovalPolicyDTO policy = null;
    private ApprovalPolicyStorage dbstorage = null;
    private boolean isPolicyWritePerm = false;
    private boolean isSameUser = false;
    private boolean isServiceNowRequest = false;
    private boolean isRemedyForceRequest = false;
    ITenant tenant;

    public void init() throws ServletException {
        super.init();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String command = (String) request.getParameter("command");
        PrintWriter out = response.getWriter();
        ServletContext context = request.getSession().getServletContext();
        this.session = request.getSession();
        this.main = TenantHelper.getTenantSubMain(context, request);
        this.tenant = main.getTenant();
        this.dbPolicy = new LoadDBPolicy(main.getDBStorage());
        this.dbstorage = main.getDBStorage();
        boolean requestSucceeded = true;
        String message = "";
        if (null == command || command.trim().length() == 0) {
            message = "Invalid command";
            requestSucceeded = false;
        }

        String changeId = (String) request.getParameter("changeid");
        if (null == changeId || changeId.trim().length() == 0) {
            message = "Invalid Policy change ID";
            requestSucceeded = false;
        }
        if (requestSucceeded) {
            iuser = (IUser) request.getSession().getAttribute(IWebAppConstants.SESSION_SMUSER);
            String user = null;

            if (iuser != null) {
                try {
                    user = main.resolveUserDN(iuser.getName());
                } catch (SystemException se) {
                    se.printStackTrace();
                }
                if (null == user || user.trim().length() == 0) {
                    System.out.println("user is null");
                    return;
                }
                System.out.println("Logged in user is : " + user);
                int policyChangeId = 0;

                try {
                    policyChangeId = Integer.parseInt(changeId);
                } catch(NumberFormatException ex) {
                    System.out.println("Failed to parse policy change id :" + changeId);
                }
                ApprovalStatusBean statusBean = null;
                if (!command.equals(GET_POLICY_DETAILS)) {
                    statusBean = new ApprovalStatusBean();
                    statusBean.setChangeId(policyChangeId);
                    String remarks = (null == request.getParameter("remarks")) ? "" : request.getParameter("remarks") ;
                    statusBean.setRemarks(remarks);
                    statusBean.setReviewedBy(user);
                }

                if (command.equals(GET_POLICY_DETAILS)) {
                    getPendingPolicyDetail(policyChangeId);
                } else if(command.equals("approvedpolicy")) {
                    debug("Performing policy approve request and update status to database");
                    statusBean.setApprovalType(POLICY_APPROVED);
                    debug("Policy Approved by :" + user);
                    debug("Policy change Id :" + policyChangeId);
                    debug("Policy Approved remarks :" + statusBean.getRemarks());
                    updatePolicy(statusBean);
                    sendApprovalMail(statusBean);
                } else if(command.equals("rejectedpolicy")) {
                    debug("Performing policy reject request and update status to database");
                    debug("Policy rejected by : " + user);
                    debug("Policy change id : " + policyChangeId);
                    debug("Policy rejected remarks : " + statusBean.getRemarks());
                    statusBean.setApprovalType(POLICY_REJECTED);
                    updatePolicy(statusBean);
                    sendApprovalMail(statusBean);
                }
            }
            addHeader(response);
            checkPolicyWritePermission();
            checkApproverUser(user);
            checkServiceAutomationRequest();
            generatePolicyChange(response);
        }
    }

    private void generatePolicyChange(HttpServletResponse response) throws IOException {
        try {
            if(null == policy) {
                return;
            }
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            Hashtable<String, String> warningMap = new Hashtable<String, String>();
            String warningMsg = null;
            boolean policyExists = false;

            // Policy details
            JsonElement policyObject =  gson.toJsonTree(policy);
            // This property required to populate json object
            jsonObject.addProperty("success",true);
            if(!isPolicyWritePerm()) {
            	warningMsg = "writePermission";
                warningMap.put("warning", warningMsg);
            } else {
                if (isServiceNowRequest) {
                	warningMsg = "serviceNow";
                    warningMap.put("warning", warningMsg);
                }
                if (isRemedyForceRequest) {
                    warningMsg = "remedyForce";
                    warningMap.put("warning", warningMsg);
                }
                if(isSameUser) {
                    warningMsg = "sameUser";
                    warningMap.put("warning", warningMsg);
                }
            }
            if(null != warningMsg) {
                JsonElement warningObject =  gson.toJsonTree(warningMap);
                jsonObject.add("warningObject",warningObject);
            }

            jsonObject.add("policyObject",policyObject);

            PolicyChangeUIGenerator policyGen = new PolicyChangeUIGenerator(policy, main);
            // Store added Channels information to object
            JsonElement addChObject =  gson.toJsonTree(policyGen.getAddedChannel());
            jsonObject.add("addChObject",addChObject);
            // Store modified Channels information to object
            JsonElement modifyChObject =  gson.toJsonTree(policyGen.getModifiedChannel());
            jsonObject.add("modifyChObject", modifyChObject);
            // Store deleted Channels information to object
            JsonElement deleteChObject = gson.toJsonTree(policyGen.getDeletedChannel());
            jsonObject.add("deleteChObject", deleteChObject);
            // Store added tuner, channel property information to object
            JsonElement addPropObject = gson.toJsonTree(policyGen.getAddedProp());
            jsonObject.add("addPropObject", addPropObject);
            // Store modified tuner, channel property information to object
            JsonElement modifyPropObject = gson.toJsonTree(policyGen.getModifiedProp());
            jsonObject.add("modifyPropObject", modifyPropObject);
            // Store deleted tuner, channel property information to object
            JsonElement deletePropObject = gson.toJsonTree(policyGen.getDeletedProp());
            jsonObject.add("deletePropObject", deletePropObject);

            policyExists = ObjectManager.existsSubscription(policy.getPolicyTargetId(), policy.getPolicyTargetType(), iuser);
            Hashtable<String, String> policyExitsMap = new Hashtable<String, String>();
            policyExitsMap.put("policyExists", (policyExists) ? "true" : "false");
            JsonElement policyExistsObject =  gson.toJsonTree(policyExitsMap);
            jsonObject.add("policyExistsObject",policyExistsObject);
            
            response.getWriter().println(jsonObject.toString());
            if(DEBUG5) {
                System.out.println(jsonObject.toString());
            }
        } catch(Exception ex) {
            if(DEBUG5) {
                ex.printStackTrace();
            }
            System.out.println("ApprovalHandlerServlet : Failed to generate policy changes to display in approval page");
        }
    }
    private void checkPolicyWritePermission() {
        boolean status = false;
        if(null == policy) {
            setPolicyWritePerm(false);
        }
        try {
            status = ObjectManager.hasSubPerm(iuser, policy.getPolicyTargetId(), policy.getPolicyTargetType(), IAclConstants.WRITE_ACTION);
        } catch(Exception ex) {

        }
        setPolicyWritePerm(status);
    }
    private void setPolicyWritePerm(boolean status) {
        isPolicyWritePerm = status;
    }
    private boolean isPolicyWritePerm() {
        return isPolicyWritePerm;
    }
    private void checkApproverUser(String loggeduser) {
        if(null == policy || null == loggeduser) {
            setSameUser(false);
        }
        if(DEBUG5) {
            System.out.println("Check Same User : logged-in user : " + loggeduser);
            System.out.println("Check Same User : Policy change owner : " + policy.getChangeOwner());
        }
        isSameUser = false;
        if (loggeduser != null && loggeduser.equalsIgnoreCase(policy.getChangeOwner())) {
            setSameUser(true);
        }
    }

    private void checkServiceAutomationRequest() {
        if(DEBUG5) {
            System.out.println("Check Service Automation Request : main.getPeerApprovalType() : " + main.getPeerApprovalType());
        }

        if ("servicenow".equalsIgnoreCase(main.getPeerApprovalType())) {
            isServiceNowRequest = true;
        } else {
            isServiceNowRequest = false;
        }
        if ("remedyforce".equalsIgnoreCase(main.getPeerApprovalType())) {
            isRemedyForceRequest = true;
        } else {
            isRemedyForceRequest = false;
        }
    }

    public boolean isSameUser() {
        return isSameUser;
    }
    public void setSameUser(boolean isSameUser) {
        this.isSameUser = isSameUser;
    }

    public boolean isServiceNowRequest() {
        return isServiceNowRequest;
    }

    public void setIsServiceNowRequest(boolean isServiceNowRequest) {
        this.isServiceNowRequest = isServiceNowRequest;
    }

    public boolean isRemedyForceRequest() {
        return isRemedyForceRequest;
    }

    public void setIsRemedyForceRequest(boolean isRemedyForceRequest) {
        this.isRemedyForceRequest = isRemedyForceRequest;
    }

    public void addHeader(HttpServletResponse response) {
        response.setContentType("application/json; charset=utf-8");
        response.addHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "Wed, 11 Jan 1984 05:00:00 GMT");
    }

    public void getPendingPolicyDetail(String policyName, String user) {
        if (dbPolicy != null) {
            policy = dbPolicy.loadPendingPolicyFromDB(policyName, user);
        }
    }

    public void getPendingPolicyDetail(int changeID) {
        if (dbPolicy != null) {
            policy = dbPolicy.loadPolicyByChangeId(changeID);
        }
    }

    //    public boolean updatePolicy(int policyChangeId, int policyStatus, String remarks, String reviewdBy) {
    public boolean updatePolicy(ApprovalStatusBean statusBean) {
        boolean isUpdatedtoDB = false;
        int oldPolicystatus = 0;
        try {
            if(null == dbstorage){
                System.out.println("ApprovalHandlerServlet: Failed to update policy status in database due to database storage is empty");
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
                    System.out.println("ApprovalHandlerServlet: Successfully updated policy status in database");
                    statusBean.setStatus("SUCCEED");
                } else {
                    System.out.println("ApprovalHandlerServlet: Failed to update policy status in database");
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
                    System.out.println("ApprovalHandlerServlet: Failed to load approved policies from database");
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

//    public boolean updatePolicyApprovalStatus(int policyChangeId, int approvalAction, String approvalRemarks, String reviewdBy) {
//        boolean status = false;
//        if (dbstorage != null) {
//            status = dbstorage.updatePolicyApprovalStatus(policyChangeId, approvalAction, approvalRemarks, reviewdBy);
//        }
//        return status;
//    }

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
        mailFormatter.setReviewedBy(iuser.getName() + " (" + statusBean.getReviewedBy() + ")");
        mailFormatter.setReviewedOn(statusBean.getReviewedOn());
        mailFormatter.setReviewedByDispName(main.getDisplayName(statusBean.getReviewedBy()));

        mailFormatter.setRemarks(statusBean.getRemarks());

        // start to prepare mail message
        mailFormatter.prepare();
        main.sendMail(mailFormatter);
    }

    private void debug(String msg) {
        if (DEBUG5) System.out.println("ApprovalHandlerServlet: " + msg);
    }
}

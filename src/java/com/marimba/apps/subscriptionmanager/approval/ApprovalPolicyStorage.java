// Copyright 2016, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscriptionmanager.AuditLogger;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
//import com.marimba.intf.tuner.IServiceAutomation;
import com.marimba.intf.msf.IDatabaseMgr;
import com.marimba.intf.msf.ITenant;

import java.io.InputStream;
import java.util.*;

/**
 * This class is used to execute all policy transactions for peer approval policy
 *
 * @author Selvaraj Jegatheesan
 */

public class ApprovalPolicyStorage implements IAppConstants, IApprovalPolicyConstants {
    //public int DEBUG = DebugFlag.getDebug("SUB");

    private SubscriptionMain main;
    private AuditLogger logger;
    private IDatabaseMgr dbmgr;
    private DataSourceClient dataSource;
    private PolicyApprovalHandler policyChange = null;
    private List<ApprovalPolicyDTO> approvalPolicy;
    private ITenant tenant;
    private String serviceNowLatestChangeRequestId = "";
    private String remedyForceLatestChangeRequestId = "";

    public ApprovalPolicyStorage(SubscriptionMain main) {
        this.main = main;
        this.tenant = main.getTenant();
        this.logger = new AuditLogger();
        initializeStorage();
    }
    /**
     * Used to Policy Storage Initialize 
     */
    public void initializeStorage() {
        try {
            dbmgr = (IDatabaseMgr) this.tenant.getDbMgr();
            if (null != dbmgr) {
                this.dataSource = new DataSourceClient(dbmgr, dbmgr.getActive("read"), logger);
                this.dataSource.initializePolicyTransaction();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public void closeStorage() {
    	try {
    		 if(null != this.dataSource) {
    			 this.dataSource.closePolicyTransaction();
    		 }
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
    }

    public PolicyApprovalHandler getPolicyChange() {
        return policyChange;
    }

    public void setPolicyChange(PolicyApprovalHandler policyChange) {
        this.policyChange = policyChange;
    }

    public List<ApprovalPolicyDTO> getApprovalPolicy() {
        return approvalPolicy;
    }

    public void setApprovalPolicy(List<ApprovalPolicyDTO> approvalPolicy) {
        this.approvalPolicy = approvalPolicy;
    }

    public String getServiceNowLatestChangeRequestId() {
        return serviceNowLatestChangeRequestId;
    }

    public void setServiceNowLatestChangeRequestId(String serviceNowLatestChangeRequestId) {
        this.serviceNowLatestChangeRequestId = serviceNowLatestChangeRequestId;
    }

    public String getRemedyForceLatestChangeRequestId() {
        return remedyForceLatestChangeRequestId;
    }

    public void setRemedyForceLatestChangeRequestId(String remedyForceLatestChangeRequestId) {
        this.remedyForceLatestChangeRequestId = remedyForceLatestChangeRequestId;
    }

    public List<ApprovalPolicyDTO> getPolicyByUser(String user) {
    	List<ApprovalPolicyDTO> policies = new ArrayList<ApprovalPolicyDTO>(10);
    	if (null == dataSource) {
            System.out.println("ApprovalPolicyStorage : Failed to get policy by user due to datasource was empty");
            return policies;
        }
    	try {
	    	dataSource.initializePolicyTransaction();
	    	policies = dataSource.loadPolicyByUser(user); 
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	} finally {
    		dataSource.closePolicyTransaction();
    	}
    	return policies;
    }
    public boolean getBlobPolicyByTarget(String targetName, String targetId, String targetType) {
    	boolean status = false;
    	if (null == dataSource) {
            System.out.println("Get BlobPolicy : Failed to get policy by target due to datasource was empty");
            return status;
        }
    	try {
	    	dataSource.initializePolicyTransaction();
	    	status = dataSource.loadBlobPolicyByTarget(targetName, targetId, targetType, main.getDataDirectory().getAbsolutePath()); 
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	} finally {
    		dataSource.closePolicyTransaction();
    	}
    	return status;
    }
    public List<ApprovalPolicyDTO> getPendingPolicyByTarget(String targetName, String targetId, String targetType) {
    	List<ApprovalPolicyDTO> policies = new ArrayList<ApprovalPolicyDTO>(10);
    	if (null == dataSource) {
            System.out.println("ApprovalPolicyStorage : Failed to get pending policy by target due to datasource was empty");
            return policies;
        }
    	try {
    		dataSource.initializePolicyTransaction();
    		policies = dataSource.loadPendingPolicyByTarget(targetName, targetId, targetType);
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	} finally {
    		dataSource.closePolicyTransaction();
    	}
        return policies;
    }

    public List<ApprovalPolicyDTO> getAllPolicy() {
    	List<ApprovalPolicyDTO> allPolicies = new ArrayList<ApprovalPolicyDTO>(10);
    	if (null == dataSource) {
            System.out.println("ApprovalPolicyStorage : Failed to get all policies due to datasource was empty");
            return allPolicies;
        }
    	try {
    		dataSource.initializePolicyTransaction();
    		allPolicies = dataSource.loadAllPolicies();
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	} finally {
    		dataSource.closePolicyTransaction();
    	}
        return allPolicies;
    }
    
    /**
     * All Policy transactions for storing approval policy to Database
     * @throws ApprovalPolicyException
     */
    public void storeApprovalPolicy() {
        if (null == dataSource) {
            System.out.println("ApprovalPolicyStorage : Failed to store approval policy changes in database, due to data source is empty");
            return;
        }

        boolean oldCommitValue;
        for (ApprovalPolicyDTO changePolicy : approvalPolicy) {
            if (changePolicy.getPolicyChannels().isEmpty() && changePolicy.getTunerProps().isEmpty() && !changePolicy.isBlackoutChanged()) {
                debug("There is no policy change for the target : " + changePolicy.getPolicyTargetId());
                continue;
            }

            try {
                debug("Entering into policy change storage for the target : " + changePolicy.getPolicyTargetId());
                dataSource.initializePolicyTransaction();

                // Used for adding policy channel URL, channel Property
                // and tuner property to dictionary table
                addPolicyDictionary(changePolicy);
                oldCommitValue = dataSource.disableAutoCommit();
                dataSource.removeExistingPolicy(changePolicy);
                dataSource.storePolicyChangeRequest(changePolicy);
                dataSource.storePolicyChannels(changePolicy);
                dataSource.storePolicyTunerProps(changePolicy);
                dataSource.savePolicyTransaction(oldCommitValue);
                System.out.println("ApprovalPolicyStorage : Successfully stored policies in database for the target : " + changePolicy.getPolicyTargetId());

                if ("servicenow".equals(main.getPeerApprovalType()) || "remedyforce".equals(main.getPeerApprovalType())) {
                    try {
                        String addedChannelStr = "", modifiedChannelStr = "", deletedChannelStr = "", addedPropStr = "", modifiedPropStr = "", deletedPropStr = "";
                        String policyName = changePolicy.getPolicyTargetId();
                        String policyType = changePolicy.getPolicyTargetType();
                        String changeOwner = changePolicy.getChangeOwner();
                        String createdOn = changePolicy.getCreated_on().toString();
                        String blackoutSchedule = (changePolicy.getBlackoutSchedule() != null) ? changePolicy.getBlackoutSchedule() : "";

                        com.marimba.apps.subscriptionmanager.approval.PolicyChangeUIGenerator policyGen = new com.marimba.apps.subscriptionmanager.approval.PolicyChangeUIGenerator(changePolicy, main);
                        // Store added/modified/deleted Channels information to object
                        ArrayList<Hashtable<String, String>> addedChannel = (ArrayList<Hashtable<String, String>>) policyGen.getAddedChannel();
                        if ((addedChannel != null) && (addedChannel.size() > 0)) {
                            for (int i = 0; i < addedChannel.size(); i++) {
                                Hashtable<String, String> addedChannelElement = addedChannel.get(i);
                                String channelInfo = addedChannelElement.get("info");
                                channelInfo = channelInfo.replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("<br>", ",").replaceAll(" : ", "-").replaceAll(" ", "+");
                                addedChannelStr += "Channel" + (i + 1) + "[" + channelInfo + "]";
                            }
                        }
                        ArrayList<Hashtable<String, String>> modifiedChannel = (ArrayList<Hashtable<String, String>>) policyGen.getModifiedChannel();
                        if ((modifiedChannel != null) && (modifiedChannel.size() > 0)) {
                            for (int i = 0; i < modifiedChannel.size(); i++) {
                                Hashtable<String, String> modifiedChannelElement = modifiedChannel.get(i);
                                String channelInfo = modifiedChannelElement.get("info");
                                channelInfo = channelInfo.replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("<br>", ",").replaceAll(" : ", "-").replaceAll(" ", "+");
                                modifiedChannelStr += "Channel" + (i + 1) + "[" + channelInfo + "]";
                            }
                        }
                        ArrayList<Hashtable<String, String>> deletedChannel = (ArrayList<Hashtable<String, String>>) policyGen.getDeletedChannel();
                        if ((deletedChannel != null) && (deletedChannel.size() > 0)) {
                            for (int i = 0; i < deletedChannel.size(); i++) {
                                Hashtable<String, String> deletedChannelElement = deletedChannel.get(i);
                                String channelInfo = deletedChannelElement.get("info");
                                channelInfo = channelInfo.replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("<br>", ",").replaceAll(" : ", "-").replaceAll(" ", "+");
                                deletedChannelStr += "Channel" + (i + 1) + "[" + channelInfo + "]";
                            }
                        }

                        // Store added/modified/deleted tuner, channel property information to object
                        ArrayList<String> addedProp = (ArrayList<String>) policyGen.getAddedProp();
                        if ((addedProp != null) && (addedProp.size() > 0)) {
                            for (int i = 0; i < addedProp.size(); i++) {
                                String propInfo = addedProp.get(i);
                                propInfo = propInfo.replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("<br>", ",").replaceAll(" : ", "-").replaceAll(" ", "+");
                                addedPropStr += "Property" + (i + 1) + "[" + propInfo + "]";
                            }
                        }
                        ArrayList<String> modifiedProp = (ArrayList<String>) policyGen.getModifiedProp();
                        if ((modifiedProp != null) && (modifiedProp.size() > 0)) {
                            for (int i = 0; i < modifiedProp.size(); i++) {
                                String propInfo = modifiedProp.get(i);
                                propInfo = propInfo.replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("<br>", ",").replaceAll(" : ", "-").replaceAll(" ", "+");
                                modifiedPropStr += "Property" + (i + 1) + "[" + propInfo + "]";
                            }
                        }
                        ArrayList<String> deletedProp = (ArrayList<String>) policyGen.getDeletedProp();
                        if ((deletedProp != null) && (deletedProp.size() > 0)) {
                            for (int i = 0; i < deletedProp.size(); i++) {
                                String propInfo = deletedProp.get(i);
                                propInfo = propInfo.replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("<br>", ",").replaceAll(" : ", "-").replaceAll(" ", "+");
                                deletedPropStr += "Property" + (i + 1) + "[" + propInfo + "]";
                            }
                        }

//                        IServiceAutomation serviceAutomation = main.getServiceAutomation();
//                        debug("serviceAutomation - " + serviceAutomation);

                        HashMap<String, String> requestDetails = new HashMap<String, String>();
                        requestDetails.put("policyName", policyName);
                        requestDetails.put("policyType", policyType);
                        requestDetails.put("changeId", "" + changePolicy.getChangeId());
                        requestDetails.put("changeOwner", changeOwner);
                        requestDetails.put("createdOn", createdOn);
                        requestDetails.put("blackoutSchedule", blackoutSchedule);
                        requestDetails.put("addedChannel", addedChannelStr);
                        requestDetails.put("modifiedChannel", modifiedChannelStr);
                        requestDetails.put("deletedChannel", deletedChannelStr);
                        requestDetails.put("addedProp", addedPropStr);
                        requestDetails.put("modifiedProp", modifiedPropStr);
                        requestDetails.put("deletedProp", deletedPropStr);
                        debug("requestDetails1 - " + requestDetails);
                        for (Map.Entry<String, String> entry : requestDetails.entrySet()) {
                            try {
                                requestDetails.put(entry.getKey(), entry.getValue().replaceAll(" ", "<space>"));
                            } catch (Throwable t) {
                                //ignore...
                            }
                        }
                        debug("requestDetails2 - " + requestDetails);

                        //String requestId = serviceAutomation.handleRequest(serviceAutomation.PEER_APPROVAL_SUBMIT_REQUEST, requestDetails);
                        //debug("Change Request Id - " + requestId);
                        String requestId = "";

                        if ((requestId == null) || (requestId.trim().length() < 1)) {
                            dataSource.rollBackTransaction();
                        } else {
                            if (requestId.indexOf(";") > -1) {
                                if ("servicenow".equals(main.getPeerApprovalType())) {
                                    serviceNowLatestChangeRequestId = requestId.split(";")[1];
                                } else if ("remedyforce".equals(main.getPeerApprovalType())) {
                                    remedyForceLatestChangeRequestId = requestId.split(";")[1];
                                }
                            } else {
                                if ("servicenow".equals(main.getPeerApprovalType())) {
                                    serviceNowLatestChangeRequestId = requestId;
                                } else if ("remedyforce".equals(main.getPeerApprovalType())) {
                                    remedyForceLatestChangeRequestId = requestId;
                                }
                                serviceNowLatestChangeRequestId = requestId;
                            }
                        }
//                        com.marimba.apps.subscriptionmanager.ws.servicenow.PeerApprovalJaxWSClient peerApprovalJaxWSClient = new com.marimba.apps.subscriptionmanager.ws.servicenow.PeerApprovalJaxWSClient(policyName, policyType, changeOwner, createdOn, blackoutSchedule, addedChannelStr, modifiedChannelStr, deletedChannelStr, addedPropStr, modifiedPropStr, deletedPropStr);
//                        peerApprovalJaxWSClient.setWsdlLocation("http://inpulw013039a:7767/servicenow/requestwatcher/?wsdl");
//                        peerApprovalJaxWSClient.submitRequest();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
                if(DEBUG5) {
                    e.printStackTrace();
                }
                dataSource.rollBackTransaction();
            } finally {
                dataSource.closePolicyTransaction();
            }
        }
    }

    /**
     * This method called before Save Policy Transaction
     * Used for adding policy channel URL, channel Property and tuner property to dictionary table
     * @param changePolicy
     * @throws ApprovalPolicyException
     */
    public void addPolicyDictionary(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        try {
            addChannels2Dictionary(changePolicy);
            addChannelProp2Dictionary(changePolicy);
            addTunerProp2Dictionary(changePolicy);
        } catch(Exception ex) {
            throw new ApprovalPolicyException(ex.getMessage());
        }
    }
    /**
     * Add all policy channels to Dictionary table
     * @param changePolicy
     * @throws ApprovalPolicyException
     */
    private void addChannels2Dictionary(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        try {
            List<ApprovalChannelDTO> channels = changePolicy.getPolicyChannels();

            for (ApprovalChannelDTO channel : channels) {
                int channelId = 0;

                if (null != dataSource) {
                    channelId = dataSource.getChannelId(channel);
                    channel.setChannelID(channelId);
                }
            }
        } catch(ApprovalPolicyException ex) {
            throw new ApprovalPolicyException(ex.getMessage());
        }
    }
    /**
     * Add all channels property to Dictionary table
     * @param changePolicy
     * @throws ApprovalPolicyException
     */
    private void addChannelProp2Dictionary(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        try {
            List<ApprovalChannelDTO> channels = changePolicy.getPolicyChannels();
            for(ApprovalChannelDTO channel : channels) {
                for(ApprovalPropertyDTO chProps : channel.getPropertyList()) {
                    int propertyId = 0;
                    if(null != dataSource) {
                        propertyId = dataSource.getPropertyId(chProps);
                        chProps.setPropertyId(propertyId);
                    }
                }
            }
        } catch(ApprovalPolicyException ex) {
            throw new ApprovalPolicyException(ex.getMessage());
        }
    }
    /**
     * Add all tuner property to Dictionary table
     * @param changePolicy
     * @throws ApprovalPolicyException
     */
    private void addTunerProp2Dictionary(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        try {
            for(ApprovalPropertyDTO tunerProps : changePolicy.getTunerProps()) {
                int propertyId = 0;
                if(null != dataSource) {
                    propertyId = dataSource.getPropertyId(tunerProps);
                    tunerProps.setPropertyId(propertyId);
                }
            }
        } catch(ApprovalPolicyException ex) {
            throw new ApprovalPolicyException(ex.getMessage());
        }
    }

    public ApprovalPolicyDTO getPolicyByUserAndPolicyName(String policyName, String user, String status) {
    	ApprovalPolicyDTO policyDto = null;
    	if (null == dataSource) {
            System.out.println("ApprovalPolicyStorage : Failed to get policy by user and policy name due to datasource was empty");
            return policyDto;
        }
    	try {
    		dataSource.initializePolicyTransaction();
	        policyDto = dataSource.loadPolicyByUserAndPolicyName(policyName, user);
	        dataSource.loadChannel(policyDto);
	        dataSource.loadTunerProperty(policyDto);
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	} finally {
    		dataSource.closePolicyTransaction();
    	}
        return policyDto;
    }

    public ApprovalPolicyDTO getPolicyByChangeId(int changeId) {
    	ApprovalPolicyDTO policyDto = null;
    	if (null == dataSource) {
            System.out.println("ApprovalPolicyStorage : Failed to get policy by change id due to datasource was empty");
            return policyDto;
        }
    	try {
    		dataSource.initializePolicyTransaction();
	        policyDto = dataSource.loadPolicyByPolicyID(changeId);
	        dataSource.loadChannel(policyDto);
	        dataSource.loadTunerProperty(policyDto);
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	} finally {
    		dataSource.closePolicyTransaction();
    	}
        return policyDto;
    }
    public boolean updatePolicyApprovalStatus(ApprovalStatusBean statusBean) {
    	boolean isUpdated = false;
    	if (null == dataSource) {
            System.out.println("ApprovalPolicyStorage : Failed to update policy approval policy status due to datasource was empty");
            return isUpdated;
    	}
    	try {
    		dataSource.initializePolicyTransaction();
    		isUpdated = dataSource.updatePolicyApprovalStatus(statusBean);
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	} finally {
    		dataSource.closePolicyTransaction();
    	}
        return isUpdated;
    }

    private void debug(String msg) {
        if(DEBUG5) System.out.println("ApprovalPolicyStorage : " + msg);
    }
}

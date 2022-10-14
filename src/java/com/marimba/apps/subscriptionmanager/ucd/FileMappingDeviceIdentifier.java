// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.ucd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.struts.upload.FormFile;

import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.forms.UserCentricDeploymentForm;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.util.IProperty;
import com.marimba.webapps.intf.SystemException;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.ldap.*;
/**
 * This class used for mapping user - device level from bulk csv file
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$,  $Date$ 
 */
public class FileMappingDeviceIdentifier implements ISubscriptionConstants, IWebAppConstants {
	private UserCentricDeploymentForm deviceIdentifyForm;
	IUser iuser;
	SubscriptionMain main;
	
	private Map<String,ArrayList<UserDeviceLevelMappingBean>> userInfoMap;
	private Map<String, String> userLevelMap;
	private Map<String, String> formattedProps;
	private int DEFALUT_COLUMNCNT_CRITIERIA = 3; // user name, machine name and device level columns in .csv file(like 1 or 2...)
	Vector<String> errorList = new Vector<String>();
	private IProperty ldapCfg;
	String ldapType = null;
	/**
	 * File Mapping Device Identifier parameterized constructor
	 * @param deviceIdentifyForm
	 * @param iuser
	 */
	public FileMappingDeviceIdentifier(UserCentricDeploymentForm deviceIdentifyForm, IUser iuser, SubscriptionMain main) {
		this.deviceIdentifyForm = deviceIdentifyForm;
		this.iuser = iuser;
		this.main = main;
		this.ldapCfg = main.getLDAPConfig();
		this.ldapType = this.ldapCfg.getProperty(LDAPConstants.PROP_VENDOR);
		initialize();
	}
	/**
	 * Initialize File Mapping Device Identifier
	 */
	private void initialize() {
		userInfoMap = new HashMap<String, ArrayList<UserDeviceLevelMappingBean>>();
		userLevelMap = new HashMap<String, String>();
		formattedProps = new HashMap<String, String>();
	}
	/**
	 * Mapping User-Device level
	 */
	public void MappUserDeviceLevel() {
		FormFile userMappingFile = (FormFile)deviceIdentifyForm.getFile();
		boolean isParseFile = parseUserMappingFile(userMappingFile);
	    if(isParseFile && userInfoMap.size() > 0) {
	    	filterUserLevelProp();
	    	generateFormattedProperty();
	    	printUserMapInfo();
	    	assignPolicy();
	    }
	    if(errorList.size() > 0) {
	    	deviceIdentifyForm.setErrorFlag("completed");
	    	errorList.addElement("Failed to update user policy with device level mapping informations");
	    	errorList.addElement("File Name : " + userMappingFile.getFileName());
	    	deviceIdentifyForm.setErrorList(errorList);
	    }
	}
	
	/**
	 * Parse User Mapping File method used to parse user information like user name,
	 * machine name, domain name and device level
	 * @param userMappingFile FormFile
	 * @return
	 * @throws Exception
	 */
	private boolean parseUserMappingFile(FormFile userMappingFile) {
		
		String userMappingContent = null;  
		
		try {
			BufferedReader mappingBuffer = new BufferedReader(new InputStreamReader(userMappingFile.getInputStream()));
        	
            while((userMappingContent = mappingBuffer.readLine()) != null) {
            	
            	String[] userMapInfo = userMappingContent.split(PROP_DELIM);

            	int count = 0;
            	String userDN = null;
            	try {
            		if(null == userMapInfo || userMapInfo.length != DEFALUT_COLUMNCNT_CRITIERIA) {
            			debug("Invalid user mapping information " + userMappingContent);
            			debug("Failed to update user policy " + userMappingContent);
            			errorList.addElement("Invalid user mapping information " + userMappingContent);
            			errorList.addElement("Failed to update user policy " + userMappingContent);
            			
            		} else {
            			if(validateUserParameters(userMapInfo) && 
    		    				!isDupliateHighDeviceLevel(userMapInfo[0], userMapInfo[2])) {
    		    			userDN = getUserDN(userMapInfo[0]);
    		    			if(null != userDN) {
    		    				updateUserInfoMap(userMapInfo, userDN);
    		    			}
    		    		} else {
    		    			debug("Failed to update user policy " + userMappingContent);
    		    			errorList.addElement("Failed to update user policy " + userMappingContent);
    		    		}
            		}
            	} catch(ArrayIndexOutOfBoundsException  aex) {
            		debug("Failed  to prase user mapping information " + userMappingContent);
            		errorList.addElement("Failed  to prase user mapping information " + userMappingContent);
            		aex.printStackTrace();
            	}
	    	}
            
            mappingBuffer.close();  
            return true;
		 } catch(FileNotFoundException fex) {
			 debug("User mapping file is not avialable " + userMappingFile.getFileName());
			 errorList.addElement("User mapping file is not avialable " + userMappingFile.getFileName());
		 } catch(Exception ex) {
			 debug("Failed to parse user mapping file." + ex.getMessage());
			 errorList.addElement("Failed to parse user mapping file." + ex.getMessage());
			 ex.printStackTrace();
		 }
		 return false;
	}
	/**
	 * print user information for debugging purpose
	 */
	private void printUserMapInfo() {
		debug("Debug purpose printing user level information");
		for(Map.Entry<String, String> userInfo : formattedProps.entrySet()) {
			System.out.print(userInfo.getKey() + "=");
			System.out.print(userInfo.getValue());
			System.out.println();
		}
		
	}
	/**
	 * To validate user parameters from .csv file
	 * @param userInfo
	 * @return
	 */
	private boolean validateUserParameters(String[] userInfo) {
		int maxDeviceLevel = 2;
		try {
			ConfigProps subConfig = main.getConfig();
			if(null != subConfig.getProperty(MAX_UCD_DEVICELEVEL)) 
				maxDeviceLevel = Integer.parseInt(subConfig.getProperty(MAX_UCD_DEVICELEVEL));
		} catch(Exception ec) {
			//
		}
		
		if("".equals(userInfo[0].trim()) || "".equals(userInfo[1].trim()) 
				|| "".equals(userInfo[2].trim())) {
			return false;
		}
		int deviceType = 0;
		try {
			deviceType = Integer.parseInt(userInfo[2].trim());
		} catch(NumberFormatException ex) {
			debug("Device level should be specify in digits");
			errorList.addElement("Device level should be specify in digits");
			return false;
		}
		if(deviceType < 1) {
			debug("Device level should be start from 1. Default device high level is 1");
			errorList.addElement("Device level should be start from 1. Default device high level is 1");
			return false;
		}
		if(deviceType > maxDeviceLevel) {
			debug("Device level exceeds maximum device level.Device level should be start from 1 to " + maxDeviceLevel);
			errorList.addElement("Device level exceeds maximum device level.Device level should be start from 1 to " + maxDeviceLevel);
			return false;
		}
		
		return true;
	}
	/**
	 * To verify duplicate high level of device for user
	 * Because only one high level for user "1" 
	 * @param userName
	 * @param deviceLevel
	 * @return
	 */
	private boolean isDupliateHighDeviceLevel(String userName, String deviceLevel) {
		ArrayList<UserDeviceLevelMappingBean> userInfoList = userInfoMap.get(userName);
		if(null != userInfoList && userInfoList.size() > 0) {
			for(UserDeviceLevelMappingBean userBean : userInfoList) {
				if(deviceLevel.equals(userBean.getDeviceLevel()) && UCD_DEFAULT_DEVICE_LEVEL.equals(deviceLevel))  {
					debug("Dupliate device level " + UCD_DEFAULT_DEVICE_LEVEL +" entry avialable for " + userName );
					errorList.addElement("Dupliate device level " + UCD_DEFAULT_DEVICE_LEVEL +" entry avialable for " + userName );
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Get full DN for user
	 * @param userName
	 * @return
	 */
	private String getUserDN(String userName) {
		String userDN = null;
		try {
			userDN = main.resolveUserDN(userName);
		} catch(SystemException ex) {
			if(null == ex.getMessage()) {
				debug("No User found on LDAP for " + userName);
				errorList.addElement("No User found on LDAP for " + userName);
			} else {
				debug("Failed to get full userDN on LDAP for " + userName + "  " + ex.getMessage());
				errorList.addElement("Failed to get full userDN on LDAP for " + userName + "  " + ex.getMessage());
			}
			
		}
		
		return userDN;
	}
	/**
	 * To convert user information object
	 * @param userInfo
	 * @param userDN
	 * @return
	 */
	private UserDeviceLevelMappingBean convertUserInfoObject(String[] userInfo, String userDN) {
		UserDeviceLevelMappingBean userBean = new UserDeviceLevelMappingBean();
		userBean.setUserName(userInfo[0]);
		String machineName = userInfo[1];
		if(null != ldapType && !LDAPConstants.VENDOR_AD.equals(ldapType)) {
			if(machineName.indexOf(".") > 0) {
				machineName = machineName.substring(0, machineName.indexOf("."));
			}
		}
		userBean.setMachineName(machineName);
		userBean.setDeviceLevel(userInfo[2]);
		userBean.setUserDN(userDN);
		return userBean;
	}
	/**
	 * Update user information to Map
	 * @param userMapInfo
	 * @param userDN
	 */
	private void updateUserInfoMap(String[] userMapInfo, String userDN) {
		UserDeviceLevelMappingBean userMapBean = convertUserInfoObject(userMapInfo, userDN);
		ArrayList<UserDeviceLevelMappingBean> userInfoList = userInfoMap.get(userMapInfo[0]);
		if(null != userInfoList && userInfoList.size() > 0) {
			userInfoList.add(userMapBean);
		} else {
			ArrayList<UserDeviceLevelMappingBean> userList = new ArrayList<UserDeviceLevelMappingBean>();
			userList.add(userMapBean);
			userInfoMap.put(userMapInfo[0], userList);
		}
	}
	/**
	 * Filter user device ie., level user1.1=machine1.domain1
	 */
	private void filterUserLevelProp() {
		for(ArrayList<UserDeviceLevelMappingBean> userInfoList : userInfoMap.values()) {
			for(UserDeviceLevelMappingBean userBean : userInfoList) {
				String userLevelKey = userBean.getUserName() + ".devicelevel" + userBean.getDeviceLevel();
				String userLevelValue = userLevelMap.get(userLevelKey);
				if(null != userLevelValue) {
					// append mapping machines
					userLevelValue = userLevelValue + "," + userBean.getMachineName(); 
				} else {
					// add new mapping machine
					userLevelValue = userBean.getMachineName(); 
				}
				userLevelMap.put(userLevelKey, userLevelValue);
			}
		}
	}
	/**
	 * Generate user level mapping information into subscription format
	 */
	private void generateFormattedProperty() {
		for (Map.Entry<String, String> userMapEntry : userLevelMap.entrySet()) {
			String userKey =  "ucd."+ userMapEntry.getKey();
			formattedProps.put(userKey, userMapEntry.getValue());
		}
	}
	/**
	 * Assign policy 
	 */
	private void assignPolicy() {
		
		for(Map.Entry<String, ArrayList<UserDeviceLevelMappingBean>> userMap : userInfoMap.entrySet()) {
			String userName = userMap.getKey();
			String userDN = null;
			for(UserDeviceLevelMappingBean userBean : userMap.getValue()) {
				userDN = userBean.getUserDN();
				break;
			}
			if(null != userDN) {
				boolean isExists = false;
				try {
					isExists = ObjectManager.existsSubscription(userDN, "user" , iuser);
					if(isExists) {
						updatePolicy(userName, userDN);
					} else {
						createPolicy(userName, userDN);
					}
				} catch(SystemException exs) {
					debug("Failed to check existing subscription for user : " + userDN);
					errorList.addElement("Failed to check existing subscription for user : " + userDN);
				}
			}
		}
	}
	/**
	 * Create a new user policy if policy doesn't exists
	 * @param userName
	 * @param userDN
	 * @return
	 */
	private boolean createPolicy(String userName, String userDN) {
		ISubscription userPropSub = null;
		boolean status = false;
		try {
			userPropSub = ObjectManager.createSubscription(userDN, "user", iuser);
			
			if(assignProperty(userPropSub, userName)) {
				userPropSub.setTargetName(userName);
				userPropSub.save();
				debug("Successfully create a new policy for user : " + userDN); 
				status = true;
			}
		} catch(Exception ex) {
			debug("Failed to create a new policy for user : " + userDN);
			errorList.addElement("Failed to create a new policy for user : " + userDN);
			ex.printStackTrace();
			
		}
		return status;
	}
	/**
	 * Modify policy if policy already exists
	 * @param userName
	 * @param userDN
	 * @return
	 */
	private boolean updatePolicy(String userName, String userDN) {
		boolean status = false;
		ISubscription userPropSub = null;
		try {
			userPropSub = ObjectManager.openSubForWrite(userDN, "user", iuser);
			
			if(assignProperty(userPropSub, userName)) {
				userPropSub.save();
				debug("Successfully update existing policy for user : " + userDN);
				status = true;
			}
		} catch(Exception ex) {
			debug("Failed to update existing policy for user : " + userDN);
			errorList.addElement("Failed to update existing policy for user : " + userDN);
		}
		return status;
	}
	/**
	 * Assign user level property as tuner
	 * @param userPropSub
	 * @param userName
	 * @return
	 */
	private boolean assignProperty(ISubscription userPropSub, String userName) {
		boolean isPropsExists = false;
		String startStr = "ucd." + userName + ".";
		
		for(Map.Entry<String, String> userInfo : formattedProps.entrySet()) {
			if(userInfo.getKey().startsWith(startStr)) {
				isPropsExists = true;
				try {
					//String keySubStr = userInfo.getKey().replace("." + userName+".", ""); 
					userPropSub.setProperty(PROP_SERVICE_KEYWORD, userInfo.getKey(), userInfo.getValue());
				} catch(SystemException ex) {
					isPropsExists = false;
					debug("Failed to assign tuner property for user : " + userName );
					debug("key - " + userInfo.getKey() + " Value - " + userInfo.getValue());
					errorList.addElement("Failed to assign tuner property for user : " + userName );
					errorList.addElement("key - " + userInfo.getKey() + " Value - " + userInfo.getValue());
				}
			}
			System.out.print(userInfo.getKey() + "=");
			System.out.print(userInfo.getValue());
			System.out.println();
		}
		return isPropsExists;
	}
	
	private void debug(String msg) {
		System.out.println("File Uploading Idenitification Type : " + msg);
	}
}

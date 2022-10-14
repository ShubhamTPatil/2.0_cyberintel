// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.ucd.UCDTemplateBean;
import com.marimba.webapps.tools.util.KnownActionError;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.struts.upload.FormFile;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
/**
 * UserCentricDeploymentForm used to define user deployment rules 
 *
 * @author Selvaraj Jegatheesan
 * @version $Revision$,  $Date$
 */
public class UserCentricDeploymentForm extends AbstractForm implements ISubscriptionConstants {
	private FormFile file;
	private String deviceIdentType;
	private String errorFlag;
	private Vector<String> errorList;
	private String fileUploadingStatus;
	private String templateType;
	private String templateName;
	private String templateDesc;
	private String deviceLevel;
	private String maxDeviceLevel;
	private Vector<String> deviceLevelList;
	private String ramsizeCondtion;
	private String ramsizeOption;
	private String disksizeCondtion;
	private String disksizeOption;
	private List<String> osNameList = null;
	private List<String> processorNameList = null;
	private String osList;
	private String processorList;
	private Map<String, String> affectedTargets = new HashMap<String, String>();
	
	public Map<String, String> getAffectedTargets() {
		return affectedTargets;
	}

	public void setAffectedTargets(Map<String, String> affectedTargets) {
		this.affectedTargets = affectedTargets;
	}

	public FormFile getFile() {
		return file;
	}

	public void setFile(FormFile file) {
		this.file = file;
	}

	public String getDeviceIdentType() {
		return deviceIdentType;
	}

	public void setDeviceIdentType(String deviceIdentType) {
		this.deviceIdentType = deviceIdentType;
	}

	public String getErrorFlag() {
		return errorFlag;
	}

	public void setErrorFlag(String errorFlag) {
		this.errorFlag = errorFlag;
	}

	public Vector<String> getErrorList() {
		return errorList;
	}

	public void setErrorList(Vector<String> errorList) {
		this.errorList = errorList;
	}

	public String getFileUploadingStatus() {
		return fileUploadingStatus;
	}

	public void setFileUploadingStatus(String fileUploadingStatus) {
		this.fileUploadingStatus = fileUploadingStatus;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateDesc() {
		return templateDesc;
	}

	public void setTemplateDesc(String templateDesc) {
		this.templateDesc = templateDesc;
	}
	
    public String getDeviceLevel() {
		return deviceLevel;
	}

	public void setDeviceLevel(String deviceLevel) {
		this.deviceLevel = deviceLevel;
	}

	public String getMaxDeviceLevel() {
		return maxDeviceLevel;
	}

	public void setMaxDeviceLevel(String maxDeviceLevel) {
		this.maxDeviceLevel = maxDeviceLevel;
	}

	public Vector<String> getDeviceLevelList() {
		return deviceLevelList;
	}

	public void setDeviceLevelList(Vector<String> deviceLevelList) {
		this.deviceLevelList = deviceLevelList;
	}

	public String getRamsizeCondtion() {
		return ramsizeCondtion;
	}

	public void setRamsizeCondtion(String ramsizeCondtion) {
		this.ramsizeCondtion = ramsizeCondtion;
	}

	public String getRamsizeOption() {
		return ramsizeOption;
	}

	public void setRamsizeOption(String ramsizeOption) {
		this.ramsizeOption = ramsizeOption;
	}

	public String getDisksizeCondtion() {
		return disksizeCondtion;
	}

	public void setDisksizeCondtion(String disksizeCondtion) {
		this.disksizeCondtion = disksizeCondtion;
	}

	public String getDisksizeOption() {
		return disksizeOption;
	}

	public void setDisksizeOption(String disksizeOption) {
		this.disksizeOption = disksizeOption;
	}

	public List<String> getOsNameList() {
		return osNameList;
	}

	public void setOsNameList(List<String> osNameList) {
		this.osNameList = osNameList;
	}

	public List<String> getProcessorNameList() {
		return processorNameList;
	}

	public void setProcessorNameList(List<String> processorNameList) {
		this.processorNameList = processorNameList;
	}

	public String getOsList() {
		return osList;
	}

	public void setOsList(String osList) {
		this.osList = osList;
	}

	public String getProcessorList() {
		return processorList;
	}

	public void setProcessorList(String processorList) {
		this.processorList = processorList;
	}

	public void reset() {
        setTemplateName("");
        setTemplateDesc("");
        setRamsizeCondtion("greaterthan");
        setRamsizeOption("MB");
        setDisksizeCondtion("greaterthan");
        setDisksizeOption("MB");
        setDeviceLevel(UCD_DEFAULT_DEVICE_LEVEL);
        setValue(UCD_LOGIN_HISTORY, INVALID);
        setValue(UCD_WORKHRS, UCD_DEFAULT_EMPTYSTR);
        setValue(UCD_WORKDAYS, UCD_DEFAULT_EMPTYSTR);
        setValue(UCD_LOCKUNLOCK, INVALID);
        setValue(UCD_REMOTELOGINMODE, INVALID);
        setValue(UCD_DEVICECONFIG, INVALID);
        setValue(UCD_RAMSIZE, UCD_DEFAULT_EMPTYSTR);
        setValue(UCD_DISKSPACE, UCD_DEFAULT_EMPTYSTR);
        setValue(UCD_OSNAME, UCD_DEFAULT_EMPTYSTR);
        setValue(UCD_PROCESSOR, UCD_DEFAULT_EMPTYSTR);
        setOsList("");
        setProcessorList("");
    }

	public void loadProfile(HashMap<String, String> props) {
        reset();
        setTemplateName((String) props.remove(UCD_TEMPLATE_NAME));
        setDeviceLevel((String) props.remove(UCD_DEVICELEVEL));
        String desc = (String) props.remove(UCD_TEMPLATE_DESCRIPTION);
        if (desc != null) {
            setTemplateDesc(desc);
        }
        for(Entry<String, String> propSet : props.entrySet()) {
        	System.out.println(propSet.getKey() + "=" + propSet.getValue());
        	if(UCD_RAMSIZE.equals(propSet.getKey()) || UCD_DISKSPACE.equals(propSet.getKey())) {
        		String[] ramSizeStr = splitSize(propSet.getValue());
        		String cond = (propSet.getValue().indexOf("lessthan") > -1) ? "lessthan" : "greaterthan";
        		// Set value for RAMSize and Disk Size
        		setValue(propSet.getKey(), ramSizeStr[0]);
        		if(UCD_RAMSIZE.equals(propSet.getKey())) {
        			setRamsizeCondtion(cond);
            		setRamsizeOption(ramSizeStr[1]);
        		} else {
        			setDisksizeCondtion(cond);
            		setDisksizeOption(ramSizeStr[1]);
        		}
        	} else if(UCD_OSNAME.equals(propSet.getKey())) {
        		setOsList(propSet.getValue());
        	} else if(UCD_PROCESSOR.equals(propSet.getKey())) {
        		setProcessorList(propSet.getValue());
        	} else {
        		setValue(propSet.getKey(), propSet.getValue());
        	}
        }
    }
	private String[] splitSize(String sizeStr) {
		String[] sizeStrArray = new String[2];
		int lIndex = sizeStr.indexOf("greaterthan");
		int gIndex = sizeStr.indexOf("lessthan");
		if(lIndex > -1) {
			sizeStrArray[0] = sizeStr.substring(11, sizeStr.length()-2);
			sizeStrArray[1] = sizeStr.contains("GB") ? "GB" : sizeStr.contains("TB") ? "TB" : "MB";
		} else if (sizeStr.indexOf("lessthan") > -1) {
			sizeStrArray[0] = sizeStr.substring(8, sizeStr.length()-2);
			sizeStrArray[1] = sizeStr.contains("GB") ? "GB" : sizeStr.contains("TB") ? "TB" : "MB";
		} else {
			sizeStrArray[0] = sizeStr;
			sizeStrArray[1] = "MB";
		}
		return sizeStrArray;
	}
}

// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.view;

/**
 * Config bean for action/view
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.apps.subscriptionmanager.compliance.core.*;

public class ConfigBean {

    String listMax;
    String cacheListMax;
    String cacheListExt;
    String cacheObjMax;
    String cacheObjExt;
    String waitTime;
    String checkLimit;
    String collectCompEnabled;

    public ConfigBean() {
    }

    public String getListMax() {
        return listMax;
    }

    public String getCacheListMax() {
	return cacheListMax;
    }

    public String getCacheListExt() {
	return cacheListExt;
    }

    public String getCacheObjMax() {
	return cacheObjMax;
    }

    public String getCacheObjExt() {
	return cacheObjExt;
    }

    public String getWaitTime() {
	return waitTime;
    }

    public String getCheckInLimit() {
	return checkLimit;
    }

    public void setListMax(String listMax) {
        this.listMax = listMax;
    }

    public void setCacheListMax(String cacheListMax) {
	this.cacheListMax = cacheListMax;
    }

    public void setCacheListExt(String cacheListExt) {
	this.cacheListExt = cacheListExt;
    }

    public void setCacheObjMax(String cacheObjMax) {
	this.cacheObjMax = cacheObjMax;
    }

    public void setCacheObjExt(String cacheObjExt) {
	this.cacheObjExt = cacheObjExt;
    }

    public void setWaitTime(String waitTime) {
	this.waitTime = waitTime;
    }

    public void setCheckinLimit(String checkLimit) {
	this.checkLimit = checkLimit;
    }

    public void setCollectCompEnabled( String collectCompEnabled ){
        this.collectCompEnabled = collectCompEnabled;
    }

    public String getCollectCompEnabled(){
        return collectCompEnabled;
    }
}

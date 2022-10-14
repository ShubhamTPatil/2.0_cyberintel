package com.marimba.apps.subscriptionmanager.webapp.util.push;

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
import com.marimba.apps.sdm.intf.simplified.IDMDeployment;
import com.marimba.apps.sdm.intf.simplified.IDMDeploymentStatus;
import com.marimba.apps.subscriptionmanager.webapp.util.DMHelperUtils;
import com.marimba.tools.i18n.util.I18nDateFormat;

import java.util.*;
import java.text.DateFormat;
import java.net.URL;
import com.marimba.intf.msf.*;


/**
 * Created by IntelliJ IDEA.
 * This class is used to contain the deployment initiated againt the DM from SPM
 * This will be used for the GUI navigation utilities as well as internal
 * webactions
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 * $File$
 * 
 */
public class DMDeploymentView  {


    private IDMDeployment deployment;
    private IDMDeploymentStatus deploymentStatus;
    private String deploymentID;
    private ArrayList pendingTargets;
    private ArrayList failedTargets;
    private ArrayList succeededTargets;
    private ArrayList stoppedTargets;
    private String startTimeStamp;
    private ITenant tenant;

    private static final int PIXEL_COUNT = 300; // the number of pixels in the progress bar

    public DMDeploymentView(IDMDeployment deployment, ITenant tenant) {
        init(deployment, tenant);
    }


    private void init(IDMDeployment deployment, ITenant tenant) {

    	this.tenant = tenant;
        this.deployment = deployment;
        this.deploymentID = deployment.getID();

        this.deploymentStatus = deployment.getStatus();
        this.startTimeStamp =   deploymentStatus.getStartTime();

        pendingTargets = new ArrayList();
        failedTargets  = new ArrayList();
        succeededTargets = new ArrayList();
        stoppedTargets = new ArrayList();

        DMHelperUtils.updateDeploymentStatus(this, tenant.getName());
    }

    public IDMDeployment getDeployment() {
        return deployment;
    }

    public void setDeployment(IDMDeployment deployment) {
        this.deployment = deployment;
    }


    public String getDeploymentID() {
        return deploymentID;
    }


    public int getCompletedCount() {
        int value = getSucceededCount()+ getFailedCount() + getStoppedCount();
        return value;
    }


    public int getPendingCount() {
        return  pendingTargets.size();
    }


    public int getSucceededCount() {
        return succeededTargets.size();
    }


    public int getFailedCount() {
        return failedTargets.size();
    }

    public int getStoppedCount() {
        return stoppedTargets.size();
    }


    public int getTargetCount() {
        return getPendingCount() + getSucceededCount() + getFailedCount() + getStoppedCount();
    }

    public int getProgressPercent() {
        // return the percentage as integer based on the number of machine in
        // the targetcount
        float completed = this.getCompletedCount();
        float total = this.getTargetCount();
        int retval = (int)((completed/total *100.0d)+0.5d);
        return retval;
    }

    public int getProgressPercentView() {
        // return the percentage as integer based on the number of machine in
        // the targetcount
        float percent = this.getProgressPercent();
        int retval = (int) ((percent * PIXEL_COUNT /100.0d)+0.5d);
        return retval;
    }

    public String getStartTimeStamp() {

        long startTime = Long.parseLong(startTimeStamp);
        Date startDate = new java.util.Date(startTime);
        Locale locale = Locale.getDefault();
        TimeZone timeZone = TimeZone.getDefault();

        DateFormat df = I18nDateFormat.getLocaleDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
        df.setTimeZone(timeZone);
        String startTimestamp = df.format(startDate);

        return startTimestamp;
    }

    public void setStartTimeStamp(String timeStamp) {
        startTimeStamp = timeStamp;
    }


    public ArrayList getSucceededTargets() {
        return succeededTargets;
    }

     public void setSucceededTargets(ArrayList succeededTargets) {
        this.succeededTargets = succeededTargets;
    }

    public ArrayList getFailedTargets() {
        return failedTargets;
    }

    public void setFailedTargets(ArrayList failedTargets) {
        this.failedTargets = failedTargets;
    }

    public ArrayList getPendingTargets() {
        return pendingTargets;
    }

    public void setPendingTargets(ArrayList pendingTargets) {
        this.pendingTargets = pendingTargets;
    }

    public ArrayList getStoppedTargets() {
        return stoppedTargets;
    }

     public void setStoppedTargets(ArrayList stoppedTargets) {
        this.stoppedTargets = stoppedTargets;
    }

    public boolean getRetryAllowed() {
        return getSucceededCount() != getTargetCount();
    }

    public ArrayList getTargetLogs(String targeturl) {
        /*
        return dm.getTargetLogs(deployment, targeturl);
        */
        return null;
    }











}

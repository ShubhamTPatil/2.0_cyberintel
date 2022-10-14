// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.view;

import com.marimba.apps.subscriptionmanager.intf.compliance.ICompliance;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscription.common.objects.Target;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Bean to store package and policy details.
 *
 * @author  Nageswara Rao
 * @version $Revision$, $Date$
 */

public class PackagePolicyDetails implements ICompliance {
    private Date policyLastUpdated;
    private Date compLastCalculated;
    private String targetType = "";
    private String targetName = "";
    private String targetId = "Full dn of policy";
    private String primaryState = "";
    private long total = Long.MIN_VALUE;
    private long succeeded = Long.MIN_VALUE;
    private long failed = Long.MIN_VALUE;
    private long notCheckedIn = Long.MIN_VALUE;
    private boolean checkedStatus = true;
    private String packageUrl = "";
    private String packageTitle = "";
    private String packageType = "";
    private Date packageLastPublished;
    private Target target = null;

    private boolean hasCachedCompliance = false;
    private int queryState = ComplianceConstants.STATE_NOT_CALCULATE;

    public int getQueryState(){
        return queryState;
    }

    public void setQueryState( int state ){
        queryState = state;
    }

    // Get a target object out of this PackagePolicyDetails bean. This is used by ACLS
    
    public Target getTarget(){
        return new Target(targetName, targetType, targetId);
    }

    public String getPackageUrl() {
        return packageUrl;
    }



    public void setPackageUrl( String packageUrl ) {
        this.packageUrl = packageUrl;
    }

    public String getPackageTitle() {
        return packageTitle;
    }

    public void setPackageTitle( String packageTitle ) {
        this.packageTitle = packageTitle;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType( String packageType ) {
        this.packageType = packageType;
    }

    public void setPackageLastPublished( java.sql.Date packageLastPublished ){
        this.packageLastPublished = packageLastPublished;
    }

    public String getPackageLastPublished(){
        if( packageLastPublished != null ){
            return WebUtil.getComplianceDateFormat().format( packageLastPublished );
        }
        return null;
    }

    public boolean isCheckedStatus() {
        return checkedStatus;
    }

    public void setCheckedStatus(boolean checkedStatus) {
        this.checkedStatus = checkedStatus;
    }



    public void setPolicyLastUpdated(Date policyLastUpdated ){
        this.policyLastUpdated = policyLastUpdated;
    }

    public String getPolicyLastUpdated(){
        if( policyLastUpdated != null ){
            return WebUtil.getComplianceDateFormat().format( policyLastUpdated );
        }
        return null;
    }

    public void setCompLastCalculated( java.sql.Date compLastCalculated ){
        this.compLastCalculated = compLastCalculated;
    }

    public String getCompLastCalculated(){
        if( compLastCalculated != null ){
            return WebUtil.getComplianceDateFormat().format( compLastCalculated );
        }
        return null;
    }

    public void setTargetType( String targetType ){
        this.targetType = targetType;
    }

    public String getTargetType(){
        return targetType;
    }

    public void setTargetName( String targetName ){
        this.targetName = targetName;
    }

    public String getTargetName(){
        return targetName;
    }

    public void setTargetId( String targetId ){
        if( targetId.indexOf( '=' ) != -1 && targetId.indexOf( ',' ) != -1 ){
            this.setTargetName( targetId.substring( targetId.indexOf( '=' )+1, targetId.indexOf( ',' ) ) );
        } else {
            this.setTargetName( targetId );
        }
        this.targetId = targetId;
    }

    public String getTargetId(){
        return targetId;
    }

    public void setPrimaryState( String primaryState ){
        this.primaryState = primaryState;
    }

    public String getPrimaryState(){
        return primaryState;
    }

    public void setHasCachedCompliance(boolean val) {
	this.hasCachedCompliance = val;
    }

    public boolean getHasCachedCompliance() {
	return hasCachedCompliance;
    }

    public void setTotal( long total ){
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public void setSucceeded( long succeeded ){
        this.succeeded = succeeded;
    }

    public long getSucceeded() {
        return succeeded;
    }

    public void setFailed( long failed ){
        this.failed = failed;
    }

    public long getFailed() {
        return failed;
    }

    public void setNotCheckedIn( long notCheckedIn ){
        this.notCheckedIn = notCheckedIn;
    }

    public long getNotCheckedIn() {
        return notCheckedIn;
    }

    public String[] getCompNumbers(){
        String[] compNumber = { ""+succeeded, ""+failed, ""+notCheckedIn };
        return compNumber;
    }

    public String[] getCompPercentage(){
        if( succeeded > Long.MIN_VALUE && failed > Long.MIN_VALUE && notCheckedIn > Long.MIN_VALUE ){
            String[] compNumber = { ""+getPercentage( succeeded, total ), ""+getPercentage( failed, total ), ""+getPercentage( notCheckedIn, total ) };
            return compNumber;
        }
        return null;
    }

    public String getPercentage( long component, long total ){
        return ""+( component/total )*100;
    }

    public boolean getCheckedStatus(){
        return checkedStatus;
    }
}

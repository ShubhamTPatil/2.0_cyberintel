// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.view;

import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Cached report information
 *
 * @author Zheng Xia
 * @version $Revision$, $Date$
 */

public class ReportBean {

    Date startTime;
    Date endTime;
    String target="";
    String name="";
    String type="";
    int calculationId;
    String checkedStatus = "false";
    ComplianceSummaryBean reportSummary;
    boolean hasCachedCompliance = false;

    public void setReportSummary( ComplianceSummaryBean reportSummary ){
        this.reportSummary = reportSummary;
    }

    public ComplianceSummaryBean getReportSummary(){
        return reportSummary;
    }

    public void setStartTime(Date startTime) {
	    this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
	    this.endTime = endTime;
    }

    public void setTarget(String target) {
        this.target = target;
        // resolving name for domain
        String lowerTarget = target.toLowerCase();
        int sepIndex = -1;
        if( lowerTarget.startsWith( "dc=" ) ){
            sepIndex = target.indexOf( ',' );
            if( sepIndex == -1 ){
                name = target.substring( "dc=".length(), target.length() );
            } else {
                name = target.substring( "dc=".length(), sepIndex )+"."+target.substring( sepIndex+"dc=".length()+1, target.length() );
            }
        } else {
            sepIndex = target.indexOf( ',' );
            int eqlIndex = target.indexOf( '=' );
            if( sepIndex != -1 && eqlIndex != -1 ){
                name = target.substring( eqlIndex+1, sepIndex );
            } else{
                name = target;
            }
        }
    }

    public void setType( String targetType ){
        this.type = targetType;
    }

    public String getType(){
        return this.type;
    }

    public void setCalculationId(int id) {
	    this.calculationId = id;
    }

    public String getStartTime() {
        return WebUtil.getComplianceDateFormat().format( startTime );
	    //return startTime;
    }

    public String getEndTime() {
        if( endTime != null ){
            return WebUtil.getComplianceDateFormat().format( endTime );
        }
	    return null;
    }

    public String getTarget() {
	    return target;
    }

    public String getName() {
	    return name;
    }

    public int getCalculationId() {
	    return calculationId;
    }

    public void setCheckedStatus( String status ){
        this.checkedStatus = status;
    }

    public String getCheckedStatus(){
        return checkedStatus; 
    }

    public void setHasCachedCompliance(boolean val) {
	    this.hasCachedCompliance = val;
    }

    public boolean getHasCachedCompliance() {
	    return hasCachedCompliance;
    }
}

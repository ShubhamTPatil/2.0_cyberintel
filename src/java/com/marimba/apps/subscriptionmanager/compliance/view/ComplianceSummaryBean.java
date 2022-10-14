package com.marimba.apps.subscriptionmanager.compliance.view;

/**
 * Created by IntelliJ IDEA.
 * User: mkumar
 * Date: May 3, 2005
 * Time: 11:38:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class ComplianceSummaryBean implements java.io.Serializable{

    int notcheckedin;
    int compliant;
    int noncompliant;
    int notcalculated;
    int notapplicable;
    int total;
    String state;
    String info;
    long complianceCalcTime;
    String targetType;

    // Storing exception message to facilitate to display in GUI
    String exception = "";

    public void setException( String exception ){
        this.exception = exception;
    }

    public String getException(){
        return exception;
    }

    public long getComplianceCalcTime() {
        return complianceCalcTime;
    }

    public void setComplianceCalcTime(long complianceCalcTime) {
        this.complianceCalcTime = complianceCalcTime;
    }

    public ComplianceSummaryBean(){
    }

    public void setState(String state){
        this.state = state;
    }

    public String getState(){
        return this.state;
    }

    public void setInfo(String info){
        this.info = info;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getNotcheckedin() {
        return notcheckedin;
    }

    public void setNotcheckedin(int notcheckedin) {
        this.notcheckedin = notcheckedin;
    }

    public int getCompliant() {
        return compliant;
    }

    public void setCompliant(int compliant) {
        this.compliant = compliant;
    }

    public int getNotcalculated() {
	return notcalculated;
    }

    public void setNotcalculated(int notcalculated) {
	this.notcalculated = notcalculated;
    }

    public int getNoncompliant() {
        return noncompliant;
    }

    public void setNoncompliant(int noncompliant) {
        this.noncompliant = noncompliant;
    }

    public int getNotapplicable() {
        return notapplicable;
    }

    public void setNotapplicable(int notapplicable) {
        this.notapplicable = notapplicable;
    }

    public int getNotapplicablePer() {
        if (total > 0 && notapplicable >= 0 && notapplicable <= total) {
            return Math.round( ( ( float )notapplicable/total )*100 );
        } else {
            return 0;
        }
    }

    public int getNotcheckedinPer() {
	    if (total > 0 && notcheckedin >= 0 && notcheckedin <= total) {
	        return Math.round( ( ( float )notcheckedin/total )*100 );
	    } else {
	        return 0;
	    }
    }

    public int getNoncompliantPer() {
	    if (total > 0 && noncompliant >= 0 && noncompliant <= total) {
	        return Math.round( ( ( float )noncompliant/total )*100 );
        } else {
            return 0;
        }
    }

    public int getCompliantPer() {
        if (total > 0 && compliant >= 0 && compliant <= total) {
            return Math.round( ( ( float )compliant/total )*100 );
        } else {
            return 0;
        }
    }

    public int getNotcalculatedPer() {
        if (total > 0 && notcalculated >= 0 && notcalculated <= total) {
            return Math.round( ( ( float )notcalculated/total ) * 100 );
        } else {
            return 0;
        }
    }

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
    
}

// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.view;

/**
 * Result object for power compliance summary (level:number)
 *
 * @author  Venkatesh Jeyaraman
 * @version $Revision$, $Date$
 */

public class PowerSummaryBean implements java.io.Serializable{

    int powernotcheckedin;
    int powercompliant;
    int powernoncompliant;
    int powernotcalculated;
    int powertotal;
    String state;
    String info;
    long complianceCalcTime;

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

    public PowerSummaryBean(){
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

    public int getPowerTotal() {
        return powertotal;
    }

    public void setPowerTotal(int total) {
        this.powertotal = total;
    }

    public int getPowerNotcheckedin() {
        return powernotcheckedin;
    }

    public void setPowerNotcheckedin(int notcheckedin) {
        this.powernotcheckedin = notcheckedin;
    }

    public int getPowerCompliant() {
        return powercompliant;
    }

    public void setPowerCompliant(int compliant) {
        this.powercompliant = compliant;
    }

    public int getPowerNotcalculated() {
	return powernotcalculated;
    }

    public void setPowerNotcalculated(int notcalculated) {
	this.powernotcalculated = notcalculated;
    }

    public int getPowerNoncompliant() {
        return powernoncompliant;
    }

    public void setPowerNoncompliant(int noncompliant) {
        this.powernoncompliant = noncompliant;
    }

    public int getPowerNotcheckedinPer() {
	    if (powertotal > 0 && powernotcheckedin >= 0 && powernotcheckedin <= powertotal) {
	        return Math.round( ( ( float )powernotcheckedin/powertotal )*100 );
	    } else {
	        return 0;
	    }
    }

    public int getPowerNoncompliantPer() {
	    if (powertotal > 0 && powernoncompliant >= 0 && powernoncompliant <= powertotal) {
	        return Math.round( ( ( float )powernoncompliant/powertotal )*100 );
        } else {
            return 0;
        }
    }

    public int getPowerCompliantPer() {
        if (powertotal > 0 && powercompliant >= 0 && powercompliant <= powertotal) {
            return Math.round( ( ( float )powercompliant/powertotal )*100 );
        } else {
            return 0;
        }
    }

    public int getPowerNotcalculatedPer() {
        if (powertotal > 0 && powernotcalculated >= 0 && powernotcalculated <= powertotal) {
            return Math.round( ( ( float )powernotcalculated/powertotal ) * 100 );
        } else {
            return 0;
        }
    }
}

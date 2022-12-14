// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.lang.*;


/**
 *  VulnerableStatusBean
 *  w.r.t show Vulnerable Statistics Donught Chat
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */


public class VulnerableStatusBean {
    String critical;
    String high;
    String medium;
    String low;
    String moderate;

    public String getCritical() {
        return critical;
    }

    public void setCritical(String critical) {
        this.critical = critical;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getModerate() {
        return moderate;
    }

    public void setModerate(String moderate) {
        this.moderate = moderate;
    }

}



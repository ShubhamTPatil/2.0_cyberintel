// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.distribution;

/**
 * Bean to hold a package state in session
 *
 * @author Marie Antoine
 */
public class PackageBean {
    public String url;
    public String primaryState;
    public String secondaryState;
    public String wow;
    public String blackout;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrimaryState() {
        return primaryState;
    }

    public void setPrimaryState(String primaryState) {
        this.primaryState = primaryState;
    }

    public String getSecondaryState() {
        return secondaryState;
    }

    public void setSecondaryState(String secondaryState) {
        this.secondaryState = secondaryState;
    }

    public String getWow() {
        return wow;
    }

    public void setWow(String wow) {
        this.wow = wow;
    }

    public String getBlackout() {
        return blackout;
    }

    public void setBlackout(String blackout) {
        this.blackout = blackout;
    }

}

// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin;

import java.io.*;

import com.marimba.io.*;

import com.marimba.tools.util.*;

/**
 * Security Scan Report stored in the Security Scan Report Queue.
 */
public class SecurityScanReport extends ListElement {
    public File dir;				// directory
    public String fname;		       	// file name in the directory
    public int attempts;	     		// number of process attempts
    public long firstTime;			// time we first attemped to process this report
    public long nextTime;    			// when to next process this report

    public SecurityScanReport() {}

    /**
     * Create a security scan report from the specified (shared) directory, and the name.
     */
    public SecurityScanReport(File dir, String fname) {
        this.dir = dir;
        this.fname = fname;
    }

    public File getFile() {
        return new File(dir, fname);
    }

    /**
     * Get the length of the report file.
     */
    public long length() {
        return getFile().length();
    }

    public String toString() {
        return "SecurityScanReport[" + fname + "]";
    }
}

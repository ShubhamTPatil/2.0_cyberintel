// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: //depot/ws/products/securitypolicymanager/9.0.02hf_ga/src/java/com/marimba/apps/subscriptionmanager/compliance/view/ReportStatusBean.java $, $Revision: #1 $, $Date: 2020/08/14 $

package com.marimba.apps.subscriptionmanager.compliance.view;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;

import java.sql.Date;
import java.util.ArrayList;

/**
 * Report Status information
 *
 * @author Ganesh Jegadheesan
 * @version $Revision: #1 $, $Date: 2020/08/14 $
 */
public class ReportStatusBean {
    Date startTime;

    Date endTime;

    String fileName = "";

    String size = "";

    String type = "";

    String checkedStatus = "false";

    public void setType(String targetType) {
        this.type = targetType;
    }

    public String getType() {
        return this.type;
    }

    public String getStartTime() {
        return WebUtil.getComplianceDateFormat().format(startTime);
        //return startTime;
    }

    public String getEndTime() {
        if (endTime != null) {
            return WebUtil.getComplianceDateFormat().format(endTime);
        }
        return null;
    }

    public void setCheckedStatus(String status) {
        this.checkedStatus = status;
    }

    public String getCheckedStatus() {
        return checkedStatus;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String[] toStringArray() {
        ArrayList<String> array = new ArrayList<String>();
        array.add(getFileName());
        array.add(getSize());
        array.add(getType());
        array.add(getCheckedStatus());
        array.add(getStartTime());
        array.add(getEndTime());
        return array.toArray(new String[0]);
    }
}
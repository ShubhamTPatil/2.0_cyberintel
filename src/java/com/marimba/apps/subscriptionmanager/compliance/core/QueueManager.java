// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.core;

/**
 * Manager for all db operation queues for compliance
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.tools.config.ConfigUtil;
import com.marimba.apps.subscriptionmanager.compliance.queue.ReportQueue;
import com.marimba.apps.subscriptionmanager.compliance.queue.QueryQueue;

public class QueueManager {

    ComplianceMain main;

    // A slow queue for reports
    ReportQueue rptQueue;

    // A fast queue for relatively fast db operations
    QueryQueue qryQueue;

    // A queue for delete operation
    ReportQueue delQueue;

    public QueueManager(ComplianceMain main) {
	this.main = main;
    }

    public void initQueues() {
	rptQueue = new ReportQueue(main, "Compliance report", 2);
	qryQueue = new QueryQueue(main, "Compliance query", 5);
	delQueue = new ReportQueue(main, "Compliance delete", 1);
    }

    public ReportQueue getReportQueue() {
	return rptQueue;
    }

    public QueryQueue getQueryQueue() {
	return qryQueue;
    }

    public ReportQueue getDelQueue() {
	return delQueue;
    }
}

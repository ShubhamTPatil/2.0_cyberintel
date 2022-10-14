// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.queue;

/**
 * A queue for compliance reports
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.Connection;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import com.marimba.intf.db.IStatementPool;

import com.marimba.tools.util.ThreadPool;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceReport;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;

import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;

public class ReportQueue implements Runnable {

    // Thread pool for the operations
    ThreadGroup tgroup;
    ThreadPool tpool;

    // Queue
    List queue;

    // All reports in this queue, including the ones in the report state
    Hashtable reports;

    // Stop?
    boolean stop;

    // wait time
    long waitTime = 5 * 60 * 1000;

    // Compliance Main
    ComplianceMain main;

    // DbSource
    DbSourceManager dbMgr;

    public ReportQueue(ComplianceMain main, String name, int maxThread) {
	    this.main = main;
	    this.dbMgr = main.getDbManager();

	    this.tgroup = new ThreadGroup("Thread group: " + name);
	    this.tpool = new ThreadPool(tgroup, "Thread pool: " + name, maxThread, 10000);
	    this.queue = new ArrayList();
	    this.reports = new Hashtable();
	    this.stop = false;
    }

    /**
     * Fork a new thread
     */
    void forkNext() {
	    if (!stop) {
	        tpool.add(this, null, "compliance", Thread.NORM_PRIORITY);
	    }
    }

    /**
     * Add a db report/operation into queue
     */
    public IComplianceReport add(IComplianceReport rpt) {
        synchronized(reports) {
	        IComplianceReport ret = (IComplianceReport) reports.get(rpt);
	        if (ret == null) {
		        reports.put(rpt, rpt);
            } else {
		        // Already in queue
		        return ret;
            }
        }
	
	    if (!stop) {
	        rpt.setState(ComplianceConstants.STATE_IN_QUEUE);

            // Add to the queue
	        synchronized (queue) {
		        queue.add(rpt);
		        queue.notifyAll();
            }
	    
            if (tpool.countRunning() <= 0) {
		        // Let's add one when there is not running thread
		        forkNext();
            }
        }

	    return rpt;
    }

    /**
     * Doing the report here
     */
    public void run() {
	    boolean forkedNext = false;
	    try {
	        while (true) {
		        IComplianceReport report = null;
		        synchronized (queue) {
		            if (queue.size() == 0) {
			            try {
			                queue.wait(waitTime);
			            } catch (InterruptedException ie) {
			                ie.printStackTrace();
			            }
		            }
		            if (!stop && queue.size() != 0) {
			            report = (IComplianceReport) queue.remove(0);
		            }
		        }
		
		        if (report != null) {
		            forkedNext = true;
		            forkNext();

		            // Set start time
		            report.setStartTime(new Date(System.currentTimeMillis()));

		            try {
			            // get connection
			            IStatementPool spool = dbMgr.getPool();
			            if (spool == null) {
			                Exception noConn = new RuntimeException("Connection not available!");
			                report.setState(ComplianceConstants.STATE_ERROR);
			                throw noConn;
			            }
			
			            try {
			                Connection con = spool.getConnection();
			                // Do report
			                try {
				                report.execute(con);
			                } finally {
				                // Remove from the reports table
				                reports.remove(report);

				                // Set end time
				                report.setEndTime(new Date(System.currentTimeMillis()));
			                }
			            } finally {
			                // Make sure we return pool
			                dbMgr.returnPool(spool);
			            }
		            } catch (ThreadDeath d) {
			            throw d;
		            } catch (Throwable t) {
			            t.printStackTrace();
		            } finally {
			            // REMIND commit or rollback here
		            }
		        }
	        }
	    } finally {
	        if (!forkedNext) {
		        forkNext();
	        }
	    }
    }

    public void stop() {
	    stop = true;
    }

    public List getQueue() {
	    return queue;
    }
}

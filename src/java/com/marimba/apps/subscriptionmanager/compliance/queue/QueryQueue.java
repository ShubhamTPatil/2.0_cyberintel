// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.queue;

/**
 * A queue for compliance queries
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.Connection;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import com.marimba.intf.db.IStatementPool;

import com.marimba.tools.util.ThreadPool;
import com.marimba.tools.config.ConfigUtil;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.ICacheManager;

import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;

public class QueryQueue implements Runnable {

    // Thread pool for the operations
    ThreadGroup tgroup;
    ThreadPool tpool;

    // Queue
    List queue;

    // All queries in this queue, including the ones in the query state
    Hashtable queries;

    // Stop?
    boolean stop;

    // wait time
    long waitTime = 5 * 60 * 1000;

    // Compliance Main
    ComplianceMain main;

    // Cache manager
    ICacheManager cache;

    // DbSource
    // DbSourceManager dbMgr;

    public QueryQueue(ComplianceMain main, String name, int maxThread) {
        this.main = main;
        this.cache = main.getCache();
        // this.dbMgr = main.getDbManager();

        this.tgroup = new ThreadGroup("Thread group: " + name);
        this.tpool = new ThreadPool(tgroup, "Thread pool: " + name, maxThread, 10000);
        this.queue = new ArrayList();
        this.queries = new Hashtable();
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

    public List addBatch( List compQueries ){
        IComplianceQuery query = null;
        ArrayList added = new ArrayList();
        synchronized( queries ) {
            for( int index=0; index < compQueries.size(); index++ ){
                query = ( IComplianceQuery )compQueries.get( index );
                IComplianceQuery ret = ( IComplianceQuery ) queries.get( query );
                if (ret == null) {
                    queries.put( query, query );
                    added.add( query );
                }
            }
        }
        if (!stop) {
            // Add to the queue
            synchronized (queue) {
                for( int index=0; index < added.size(); index++ ){
                    query = ( IComplianceQuery )added.get( index );
                    query.setState(ComplianceConstants.STATE_IN_QUEUE);
                    queue.add( query );
                }
                queue.notifyAll();
            }

            if (tpool.countRunning() <= 0) {
                // Let's add one when there is not running thread
                forkNext();
            }
        }
        return compQueries;
    }

    /**
     * Add a db query/operation into queue
     */
    public IComplianceQuery add(IComplianceQuery dbOper) {
        synchronized(queries) {
            IComplianceQuery ret = (IComplianceQuery) queries.get(dbOper);
            if (ret == null) {
            queries.put(dbOper, dbOper);
            } else {
            // Already in queue
            return ret;
            }
        }
	
        if (!stop) {
            dbOper.setState(ComplianceConstants.STATE_IN_QUEUE);

            // Add to the queue
            synchronized (queue) {
                queue.add(dbOper);
                queue.notifyAll();
            }

            if (tpool.countRunning() <= 0) {
            // Let's add one when there is not running thread
            forkNext();
            }
        }

        return dbOper;
    }

    /**
     * Doing the query here
     */
    public void run() {
        boolean forkedNext = false;
        try {
            while (true) {
                IComplianceQuery query = null;
                synchronized (queue) {
                    if (queue.size() == 0) {
                        try {
                            queue.wait(waitTime);
                        } catch (InterruptedException ie) {
                            //ie.printStackTrace();
                        }
                    }
                    if (!stop && queue.size() != 0) {
                        query = (IComplianceQuery) queue.remove(0);
                    }
                }

                if (query != null) {
                    forkedNext = true;
                    forkNext();

                    try {
                        query.executeQuery();

                        // Put into the cache
                        cache.put(query);
                    } catch (ThreadDeath d) {
                        throw d;
                    } catch (Throwable t) {
                        //We need to log this instead of printing the trace on the console. t.printStackTrace();
                    } finally {
                        // Remove from the queries table
                        queries.remove(query);
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

    public Hashtable getQueries(){
        return queries;
    }
}

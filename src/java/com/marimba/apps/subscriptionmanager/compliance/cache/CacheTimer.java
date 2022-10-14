// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.cache;

import java.util.*;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;

/**
 * Cache clean up task
 *
 * @author  Zheng Xia
 * @version $Revision$ $Date$
 */

public class CacheTimer implements Comparator {

    public class WaitList {

	SortedSet wList;
	Map wMap;
	Map qMap;

	public WaitList() {
	    wList = new TreeSet(CacheTimer.this);
	    wMap = new HashMap();
	    qMap = new HashMap();
	}
	
	public void add(long expireTime, IComplianceQuery qry) {
	    Long l = new Long(expireTime);
	    WaitEntry entry = (WaitEntry) wMap.get(l);
	    if (entry == null) {
		entry = new WaitEntry(expireTime);
		wMap.put(l, entry);
		wList.add(entry);
	    }
	    entry.add(qry);
	    qMap.put(qry, entry);
	}
	
	public void remove(IComplianceQuery qry) {
	    WaitEntry entry = (WaitEntry) qMap.get(qry);
	    if (entry != null) {
		qMap.remove(qry);
		entry.remove(qry);
		if (entry.size() == 0) {
		    remove(entry);
		}
	    }
	}

	public WaitEntry first() {
	    return (WaitEntry) wList.first();
	}

	public int size() {
	    return wList.size();
	}

	public void remove(WaitEntry entry) {
	    if (entry != null) {
		if (entry.size() != 0) {
		    Iterator i = entry.getSet().iterator();
		    while (i.hasNext()) {
			qMap.remove(i.next());
		    }
		}
		wList.remove(entry);
		wMap.remove(entry.getExpireTime());
	    }
	}

    }

    public class WaitEntry {
	Long expireTime;
        Set set;

	public WaitEntry(long expireTime) {
	    this.expireTime = new Long(expireTime);
	    this.set = new HashSet();
	}

	public Long getExpireTime() {
	    return expireTime;
	}

	public void add(IComplianceQuery qry) {
	    set.add(qry);
	}

	public void remove(IComplianceQuery qry) {
	    set.remove(qry);
	}

	public int size() {
	    return set.size();
	}

	public Set getSet() {
	    return set;
	}
    }

    // The cache to clean
    ComplianceCacheMgr mgr;

    // Clean thread
    Thread cleanThread;
    List cleanList;

    // Wait thread
    Thread waitThread;
    WaitList waitList;

    // Stop
    boolean stop = false;

    public CacheTimer(ComplianceCacheMgr mgr) {
	this.mgr = mgr;

	cleanList = new ArrayList();
	waitList = new WaitList();
	
	// start the thread
	cleanThread = new Thread(new Cleaner());
	waitThread = new Thread(new Waiter());

	cleanThread.start();
	waitThread.start();
    }

    public void shutdown() {
	stop = true;

	synchronized (cleanList) {
	    cleanList.notifyAll();
	}

	synchronized (waitList) {
	    waitList.notifyAll();
	}
    }

    public void addExpireTask(long expireTime, IComplianceQuery qry) {
	if (qry != null) {
	    synchronized (waitList) {
		// Get rid of milli seconds
		expireTime = (expireTime - (expireTime % 1000));
		
		waitList.remove(qry);
		waitList.add(expireTime, qry);
		waitList.notifyAll();

	    }
	}
    }

    public int compare(Object obj1, Object obj2) {
	WaitEntry entry1 = (WaitEntry) obj1;
	WaitEntry entry2 = (WaitEntry) obj2;
	
	long expTime1 = entry1.getExpireTime().longValue();
	long expTime2 = entry2.getExpireTime().longValue();

	if (expTime1 > expTime2) {
	    return 1;
	} else if (expTime1 < expTime2) {
	    return -1;
	} else {
	    return 0;
	}
    }

    public class Waiter implements Runnable {
	public Waiter() {
	}

	public void run() {
	    List list = new ArrayList();
	    while (!stop) {
		synchronized (waitList) {
		    if (waitList.size() == 0) {
			try {
			    waitList.wait();
			} catch (InterruptedException ie) {
			    //Remind: log this ie.printStackTrace();
			} 
		    } else {
			long currentTime = System.currentTimeMillis();
			long waitTime = 0;
			
			while (waitList.size() > 0) {
			    WaitEntry entry =  waitList.first();
			    long expireTime = entry.getExpireTime().longValue();
			    if (expireTime <= currentTime) {
				waitList.remove(entry);
				list.addAll(entry.getSet());
			    } else {
				waitTime = expireTime - currentTime;
				break;
			    }
			}
			
			if (waitTime > 0 && list.size() == 0) {
			    try {
				waitList.wait(waitTime + 100);
			    } catch (InterruptedException ie) {
				//Remind: log this ie.printStackTrace();
			    }
			}
		    }
		}

		// Add to clean list
		if (list.size() > 0) {
		    synchronized (cleanList) {
			cleanList.addAll(list);
			cleanList.notifyAll();
		    }
		    
		    list.clear();
		}
	    }
	}
    }

    public class Cleaner implements Runnable {

	public Cleaner() {
	}

	public void run() {
	    List list = new ArrayList();
	    while (!stop) {
		synchronized (cleanList) {
		    while (cleanList.size() == 0 && !stop) {
			try {
			    cleanList.wait();
			} catch (InterruptedException ie) {
			    //Remind: log this ie.printStackTrace();
			}
		    }
		    list.addAll(cleanList);
		    cleanList.clear();
		}
		
		// Expire every element in the list
		Iterator i = list.iterator();
		while (i.hasNext()) {
		    mgr.expire((IComplianceQuery) i.next());
		}
		list.clear();
	    }
	}
    }

}

// Copyright 2018, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin;

import java.io.*;
import java.io.FileInputStream;
import java.lang.Throwable;
import java.util.*;
import java.util.Iterator;

import marimba.util.Timer;
import marimba.util.TimerClient;

import com.marimba.io.*;

import com.marimba.tools.util.List;
import com.marimba.tools.util.ListElement;

import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceConstants ;
import com.marimba.apps.subscriptionplugin.LogConstants;
import com.marimba.apps.subscriptionplugin.IPluginDebug;

/**
 * This class stores security scan reports on disk, and has methods for
 * managing those reports in a queue.
 */
public class SecurityScanReportQueue implements ISecurityServiceConstants, TimerClient, LogConstants, IPluginDebug {
    static final int RENAME_TIMEOUT = 2 * 1000; // 2 seconds
    static final int MAX_BUFFERS    = 100;
    static final int BUFFER_SIZE    = 1024;
    static final int DEBUG = 5;
    
    /**
     * This is how long to wait before before we try to
     * forward a report to a down server.
     */
    static final int DECAY_MINUTES[] = {5, 10, 20, 30, 60, 120};

    SubscriptionPlugin plugin;	// the security plugin that's in charge
    File root;			// directory for storing all the reports
    List active = new List();	// list of reports
    List delayed = new List();	// list of reports that will be retried after a delay
    int maxDuration;		// how long a report can be retried
    long maxDiskStoreSize;      // maximum disk size that can used by the plugin for queuing reports
    long currDiskStoreSize;     // current disk store size of the plugin data queue dir
    long maxFiles;		// maximun number of files plugin can store in queue
    long currFiles;		// current number of files plugin has stored in queue
    int scanReportQueueTimerInterval;		// how often the queued reports need to be processed

    int bufferCount;
    byte[][] buffers;
    
    SecurityScanReportQueue(SubscriptionPlugin plugin, File root, int maxDuration, long maxDiskStoreSize, long maxFiles, int scanReportQueueTimerInterval) throws IOException {
        this.plugin = plugin;
        this.root = root;
        this.maxDuration = maxDuration;
        this.maxDiskStoreSize = maxDiskStoreSize;
        this.maxFiles = maxFiles;
        this.scanReportQueueTimerInterval = scanReportQueueTimerInterval;
        init();
        Timer.master.add(this);
        buffers = new byte[MAX_BUFFERS][];
    }

    private byte[] getBuffer() {
        if (bufferCount > 0) {
            synchronized (buffers) {
                if (bufferCount > 0) {
                    return buffers[--bufferCount];
                }
            }
        }
        return new byte[BUFFER_SIZE];
    }

    private void returnBuffer(byte[] buffer) {
        int n = buffers.length;
        if (bufferCount < n) {
            synchronized (buffers) {
                buffers[bufferCount++] = buffer;
            }
        }
    }
    
    /**
     * Returns the number of items in the queue.
     */
    int length() {
        return active.getLength() + delayed.getLength();
    }

    /**
     * Returns the number of items in the queue.
     */
    int activeLength() {
        return active.getLength();
    }

    /**
     * Initializes the scan queue.  It loads all the existing
     * reports, and deletes any tmp files it finds along the
     * way.
     */
    void init() throws IOException {
        // load all the reports, deleting all tmp files along the way
        File[] list = root.listFiles();
        if (list == null) {
            throw new IOException("Scan queue cannot list directory: " + root);
        }
        synchronized (list) {
            try {
                Arrays.sort(list, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return Long.valueOf(((File) o1).lastModified()).compareTo(Long.valueOf(((File) o2).lastModified()));
                    }
                });
            } catch (Throwable t) {
                debug(ERROR, "init(), Arrays.sort() failed to sort queue files by modified time... retrying with QuickSort...");
                quickSortFilesByTime(list, 0, list.length);
            }
        }
        if (DETAILED_INFO) {
            debug(DETAILED_INFO, "init(), queue files sorted list:");
            for (int i=0; i < list.length; i++) {
                System.out.println("\t" + list[i].getName() + "(" + list[i].lastModified() + ")");
            }
        }

        // load all the existing reports, nuke all incomplete reports
        for (int i = 0; i < list.length; i++) {
            String f = list[i].getName();
            debug(INFO, "init(), processing - " + f);
            if (f.startsWith(PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT_QUEUE)) {
                // a report
                File inqueue = new File(root, f);
                currDiskStoreSize += inqueue.length();
                currFiles++;
                active.append(new SecurityScanReport(root, f));
            } else if (f.startsWith("T.") || f.startsWith(PLUGIN_REQUEST_COMPLIANCE_DETAILS_CLIENT)) {
                new File(root, f).delete();
            } else {
                System.out.println("WARNING: ignoring " + f + " in security scan report queue");
            }
        }
        debug(INFO, "init(), activeLength() - " + activeLength());
        debug(INFO, "init(), length() - " + length());
        if (activeLength() > 0) {
            Enumeration activeElements = active.elements();
            while (activeElements.hasMoreElements()) {
                debug(INFO, "init(), active element - " + activeElements.nextElement());
            }
        } else {
            debug(INFO, "init(), active - EMPTY");
        }
        if (length() > 0) {
            Enumeration delayedElements = delayed.elements();
            while (delayedElements.hasMoreElements()) {
                debug(INFO, "init(), delayed element - " + delayedElements.nextElement());
            }
        } else {
            debug(INFO, "init(), delayed - EMPTY");
        }
    }

    void stop() {
        Timer.master.remove(this, null);
    }

    /**
     * This copies a security scan report to disk.
     */
    public SecurityScanReport addReport(File reportFile) throws Exception {
        return addReport(reportFile, null);
    }

    public SecurityScanReport addReport(File reportFile, String newReportName) throws Exception {
        debug(INFO, "addReport(), reportFile - " + reportFile);
        debug(INFO, "addReport(), newReportName - " + newReportName);
        debug(INFO, "addReport(), currDiskStoreSize - " + currDiskStoreSize);
        debug(INFO, "addReport(), maxDiskStoreSize - " + maxDiskStoreSize);
        debug(INFO, "addReport(), currFiles - " + currFiles);
        debug(INFO, "addReport(), maxFiles - " + maxFiles);
        if (currDiskStoreSize > maxDiskStoreSize || currFiles > maxFiles) {
            // we're full
            plugin.getPluginContext().log(LOG_ERROR_DISK_FULL, LOG_MAJOR);
            return null;
        }

        // we are within storage limits, so lets add this report

        InputStream in = new FileInputStream(reportFile);

        // create a new log report
        SecurityScanReport lr = new SecurityScanReport(root, null);

        lr.fname = ((newReportName != null) && (newReportName.trim().length() > 0)) ? newReportName : reportFile.getName();
        nukeReport(lr.fname);

        File file = lr.getFile();

        // copy report to a tmp file, and rename it when we're done
        File tmp = new File(root, "T." + Thread.currentThread().hashCode());
        byte[] buffer = getBuffer();
        FastOutputStream out = new FastOutputStream(new FileOutputStream(tmp), buffer);
        Exception exception = null;

        try {
            // then write the contents
            out.write(in, -1);
        } catch (IOException e) {
            exception = e;
        } catch (Exception ex) {
            exception = ex;
        } finally {
            out.close();
            in.close();
            returnBuffer(buffer);
        }

        // Hmm - we weren't expecting this.  This should imply that it's
        // the same data.  For now we're assume that's the case.
        if (file.exists()) {
            plugin.getPluginContext().log(LOG_REPORT_EXISTS, LOG_INFO, file.getName());
            debug(INFO, "addReport(), WARNING: DELETED " + file + " SIZE = " + file.length());
            debug(INFO, "addReport(),          NEW SIZE " + tmp.length());
            file.delete();
            currFiles--;
        }

        // If we were successful, move the report into the queue.  If
        // not, then delete the report and throw an exception.
        if (exception == null) {
            synchronized (this) {
                // Synchronously rename this file into the queue and
                // add this to the active list. But first check to see
                // if we are within storage limits.
                long reportSize = tmp.length();
                if (currDiskStoreSize + reportSize < maxDiskStoreSize &&
                        currFiles < maxFiles) {
                    if (rename(tmp, file, RENAME_TIMEOUT)) {
                        currDiskStoreSize += reportSize;
                        currFiles++;
                        active.append(lr);
                    } else {
                        tmp.delete();
                        throw new IOException("Couldn't rename: " + tmp + " to " + file);
                    }
                } else {
                    // we're full
                    plugin.getPluginContext().log(LOG_ERROR_DISK_FULL, LOG_MAJOR);
                    tmp.delete();
                    return null;
                }
            }
        } else {
            tmp.delete();
            throw exception;
        }
        debug(INFO, "addReport(), activeLength() - " + activeLength());
        debug(INFO, "addReport(), length() - " + length());
        if (activeLength() > 0) {
            Enumeration activeElements = active.elements();
            while (activeElements.hasMoreElements()) {
                debug(INFO, "addReport(), active element - " + activeElements.nextElement());
            }
        } else {
            debug(INFO, "addReport(), active - EMPTY");
        }
        if (length() > 0) {
            Enumeration delayedElements = delayed.elements();
            while (delayedElements.hasMoreElements()) {
                debug(INFO, "addReport(), delayed element - " + delayedElements.nextElement());
            }
        } else {
            debug(INFO, "addReport(), delayed - EMPTY");
        }
        return lr;
    }

    /**
     * Renames one file name to another within a specified timeout period.
     */
    private boolean rename(File from, File to, int timeout) {
        // Try the rename.  It's ok if it fails if the
        // source doesn't exist.
        if (from.renameTo(to) || !from.exists()) {
            return true;
        }
        long now = System.currentTimeMillis();
        long deadline = now + timeout;
        int cnt = 0;
        while (now < deadline) {
            // try the rename
            if (from.renameTo(to)) {
                return true;
            }

            if (to.exists()) {
                to.delete();
                if (to.exists()) {
                    // REMIND: Log this.
                    System.out.println("Warning: SecurityScanReportStore: couldn't rename " + from + " to " + to + ": destination exists and cannot be deleted");
                    return false;
                }
                continue;
            }
            try {
                Thread.sleep(100);
                now = System.currentTimeMillis();
            } catch (InterruptedException e) {
                return false;
            }
        }
        // REMIND: Log this.
        System.out.println("Warning: SecurityScanReportStore: Couldn't rename " + from + " to " + to + ": there must be a sharing violation with the source");
        return false;
    }

    /**
     * Returns a saved report.  This blocks until one is available.  The
     * report is loaded from the file system, and its input stream
     * points to the first byte of the compressed log data (the header
     * has been read).
     */
    synchronized SecurityScanReport getReport(int timeout) throws InterruptedException {
        long now = System.currentTimeMillis();
        long then = now + timeout;
        while (active.length == 0 && now < then) {
            wait(then - now);
            now = System.currentTimeMillis();
        }

        if (active.length == 0) {
            return null;
        }

        debug(INFO, "getReport(), activeLength() - " + activeLength());
        debug(INFO, "getReport(), length() - " + length());
        if (activeLength() > 0) {
            Enumeration activeElements = active.elements();
            while (activeElements.hasMoreElements()) {
                debug(INFO, "getReport(), active element - " + activeElements.nextElement());
            }
        } else {
            debug(INFO, "getReport(), active - EMPTY");
        }
        if (length() > 0) {
            Enumeration delayedElements = delayed.elements();
            while (delayedElements.hasMoreElements()) {
                debug(INFO, "getReport(), delayed element - " + delayedElements.nextElement());
            }
        } else {
            debug(INFO, "getReport(), delayed - EMPTY");
        }

        SecurityScanReport lr = (SecurityScanReport) active.first;
        active.unlink(active.first);
        return lr;
    }

    /**
     * This is called when we're giving up on a report.  Either
     * we tried to forward it for a long time, or we tried to
     * insert it multiple times and it just failed.
     */
    public void nukeReport(SecurityScanReport lr) {
        if (activeLength() > 0) {
            Enumeration activeElements = active.elements();
            while (activeElements.hasMoreElements()) {
                SecurityScanReport lrCurrent = (SecurityScanReport) activeElements.nextElement();
                if (lr.fname.equals(lrCurrent.fname)) {
                    active.unlink(lrCurrent);
                    break;
                }
            }
        } else {
            //do nothing...
        }
        deleteReport(lr);
        plugin.getPluginContext().log(LOG_ERROR_GIVING_UP, LOG_AUDIT, lr.fname);
    }

    public void nukeReport(String reportFileName) {
        if (activeLength() > 0) {
            Enumeration activeElements = active.elements();
            while (activeElements.hasMoreElements()) {
                SecurityScanReport lr = (SecurityScanReport) activeElements.nextElement();
                if (lr.fname.equals(reportFileName)) {
                    active.unlink(lr);
                    break;
                }
            }
        } else {
            //do nothing...
        }
        deleteReport(reportFileName);
        plugin.getPluginContext().log(LOG_ERROR_GIVING_UP, LOG_AUDIT, reportFileName);
    }

    /**
     * Decrements the current disk store size before
     * deleting the report.
     */
    void deleteReport(SecurityScanReport lr) {
        File f = lr.getFile();
        long reportSize = f.length();
        f.delete();
        synchronized (this) {
            currDiskStoreSize -= reportSize;
            currFiles--;
        }
    }

    void deleteReport(String reportFileName) {
        File f = new File(root, reportFileName);
        if (!f.exists()) {
            return;
        }
        long reportSize = f.length();
        f.delete();
        synchronized (this) {
            currDiskStoreSize -= reportSize;
            currFiles--;
        }
    }

    /**
     * Mark a report that needs forwarding for retrying some time in the future.
     */
    void retryForward(SecurityScanReport lr) {
        retry(lr, 0);
    }

    /**
     * Retry a report.  This moves the report to the delayed queue,
     * which is examined periodically for reports that should be moved
     * back into the active queue.  If maxAttempts > 0, then that's the
     * maximum number of times this report should be forward.  If it's
     * equal to 0, then we use a time-based restriction on whether to
     * continue retrying.
     */
    void retry(SecurityScanReport lr, int maxAttempts) {
        if (maxAttempts > 0 && lr.attempts >= maxAttempts) {
            nukeReport(lr);
        }
        synchronized (delayed) {
            long now = System.currentTimeMillis();
            int cnt = lr.attempts++;
            if (cnt == 0) {
                lr.firstTime = now;
            }
            int delay = DECAY_MINUTES[Math.min(cnt, DECAY_MINUTES.length - 1)];
            lr.nextTime = now + (delay * 60 * 1000);
            if (lr.nextTime - lr.firstTime > maxDuration) {
                nukeReport(lr);
            } else {
                plugin.getPluginContext().log(LOG_ERROR_RETRY_SCHEDULED, LOG_INFO, lr.fname);
                delayed.append(lr);
            }
        }
    }

    /**
     * Process all the delayed items to see if it's time to move them
     * back onto the official queue.
     */
    public synchronized long tick(long tm, Object arg) {
        synchronized (delayed) {
            for (ListElement le = delayed.first, next = null; le != null; le = next) {
                SecurityScanReport lr = (SecurityScanReport) le;
                next = le.next;
                if (tm >= lr.nextTime) {
                    delayed.unlink(le);
                    plugin.checkFork();
                    active.append(le);
                    notify();
                }
            }
        }
        return tm + (scanReportQueueTimerInterval * 60 * 1000);
    }

    public void quickSortFilesByTime(File a[], int off, int len) {
        quickSortFilesByTime0(a, off, off + len - 1);
    }

    void quickSortFilesByTime0(File a[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        File mid;

        if (hi0 > lo0) {
            mid = a[(lo0 + hi0) / 2];

            while (lo <= hi) {
                while (lo < hi0 && a[lo].lastModified() < mid.lastModified())
                    lo += 1;
                while (hi > lo0 && a[hi].lastModified() > mid.lastModified())
                    hi -= 1;
                if (lo <= hi) {
                    File tmp = a[lo];
                    a[lo] = a[hi];
                    a[hi] = tmp;

                    lo += 1;
                    hi -= 1;
                }
            }
            if (lo0 < hi)
                quickSortFilesByTime0(a, lo0, hi);
            if (lo < hi0)
                quickSortFilesByTime0(a, lo, hi0);
        }
    }

    public void debug(boolean debugType, String msg) {
        if (debugType) {
            plugin.getPluginContext().logToConsole("SecurityScanReportQueue.java -- " + msg);
        }
    }

}

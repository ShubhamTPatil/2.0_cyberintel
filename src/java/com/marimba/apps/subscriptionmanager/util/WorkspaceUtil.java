// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.util;

import java.io.*;
import java.net.*;

import com.marimba.intf.castanet.IChannel;
import marimba.io.*;

import com.marimba.castanet.checksum.*;
import com.marimba.desktop.*;

import com.marimba.tools.util.*;

/**
 * A wrapper around a native executable.
 *
 * @author	Pritpal Singh
 * @version 	$Revision$, $Date$
 */ 
public class WorkspaceUtil {

    static final int DEBUG = 3;

    String[] argv;

    /**
     * Constructor.
     */
    public WorkspaceUtil() {
    }

    /**
     * Constructor.
     */    
    public WorkspaceUtil(String[] argv) {
        this.argv = argv;
    }

    /**
     * Copy the native executable from the channel into the data directory
     * where it can be executed. Ensure that the file in the workspace is
     * up-to-date. First, calculate the checksums of the file in the tuner
     * storage and the file in the workspace.  Then, if the checksums differ,
     * copy the file from the storage into the workspace. We could have instead
     * always copied the file to a temp location in the workspace, compared
     * checksums, and if they differed then overwritten the actual file.  The
     * benefit of that approach is that we only retrieve the inputstream from
     * the storage once, but the benefit is outweighed by the headache of having
     * to manage an additional file in the workspace and deal with exception paths
     * and cleanup.  This way is more reliable.
     */
    public boolean syncFile(URL baseURL, File destFile) {
        return (syncFile(baseURL, destFile.getName(), destFile));
    }

    /**
     * Copy the native executable from the channel into the data directory
     * where it can be executed. Ensure that the file in the workspace is
     * up-to-date. First, calculate the checksums of the file in the tuner
     * storage and the file in the workspace.  Then, if the checksums differ,
     * copy the file from the storage into the workspace. We could have instead
     * always copied the file to a temp location in the workspace, compared
     * checksums, and if they differed then overwritten the actual file.  The
     * benefit of that approach is that we only retrieve the inputstream from
     * the storage once, but the benefit is outweighed by the headache of having
     * to manage an additional file in the workspace and deal with exception paths
     * and cleanup.  This way is more reliable.
     */
    public boolean syncFile(URL baseURL, String sourceFileName, File destFile) {
        if (DEBUG > 1) {
            sdebug("syncFile");
        }

	URL srcURL;
	try {
	    srcURL = new URL(baseURL, sourceFileName);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    return false;
	}

	if (destFile.exists()) {
	    if (!destFile.isFile()) {
		// if file exists but is a directory, there's nothing we can do
		sdebug("Warning: destination file '" + destFile.toString() + "' exists but is not a file");
		return false;
	    }

	    if (compare(srcURL, destFile)) {
		// checksums match, nothing more to do
		if (DEBUG >= 5) {
		    sdebug("checksums match, don't need to copy file '" + destFile.getName() + "'");
		}
		return true;
	    }
	}

	// file does not exist or checksums don't match, so update the file
	return copy(srcURL, destFile);
    }

    /**
     * Compare two files by comparing their checksums.
     * Return true if the checksums match, false otherwise.
     */
    protected static boolean compare(URL srcURL, File destFile) {
        try {
	    ChecksumAlgorithm alg = ChecksumFactory.getAlgorithm("MD5");

	    // calculate checksum for source (storage)
	    Checksum c1 = null;
	    InputStream src = null;

	    try {
        src = srcURL.openStream();
        c1 = alg.checksum(src);
	    } catch (FileNotFoundException e) {
            if (DEBUG >= 5) {
            sdebug("Warning: could not retrieve checksum for source file '" + srcURL.getFile() + "'");    
            }
            return false;
        } finally {
        if (src != null) {
        src.close();
        }
        }

	    if (DEBUG >= 5) {
		sdebug("c1 is " + ((null == c1) ? "null" : c1.toString()));
	    }

	    if (c1 == null) {
		sdebug("Warning: could not retrieve checksum for source file '" + srcURL.getFile() + "'");
		return false;
	    }

	    // calculate checksum for destination (filesystem)
	    Checksum c2;

	    try {
		c2 = alg.checksum(destFile);
	    } catch (FileNotFoundException e) {
		// We have already determined that the file exists
		// according to Java, but if the checksum code
		// disagrees, return false and sync it anyway.
		// This is for defect 34266.
		sdebug("Warning: could not retrieve checksum for destination file '" + destFile.getName() + "'");
		return false;
	    }

	    if (DEBUG >= 5) {
		sdebug("c2 is " + ((null == c2) ? "null" : c2.toString()));
	    }

	    // compare the checksums
	    if (c2 == null || !c1.equals(c2)) {
		// they differ
		return false;
	    }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
	return true;
    }


    /**
     * Copy a file from a source to a destination, and make
     * the file executable afterwards by setting its mode bits.
     */
    protected boolean copy(URL srcURL, File destFile) {
	try {
            // Local file doesn't exist or checksums don't match, so
            // we'll copy the file from storage onto the filesystem.
            if (DEBUG > 1) {
                sdebug("checksums do not match, copying file '" + destFile.getName() + "'");
            }

// Change#1 made for defect# 41396 - allow permission for read/write/execute on the files to
// be copied from tuner-store to disk. This set of permissions will last only till the file
// gets copied out into the target directory in the channel. Thereafter a new set of permissions
// will be given (refer Change#2 made for defect# 41396)
// This is to ensure that the file has the following access permissions before the channel tries to
// update it:
// Owner -  Allow Read, Allow Write, Allow Execute
// Group -  Allow Read, Allow Write, Allow Execute
// Others - Allow Read, Allow Write, Allow Execute
            if(destFile.exists()) {
                if(OSConstants.UNIX) {
                    FileSystem.setModeBits(destFile,0x007);
                }
            }

            InputStream src = srcURL.openStream();
            try {
                FastOutputStream dst = new FastOutputStream(destFile.getAbsolutePath());
                try {
                    copy(src, dst);

                    if (DEBUG > 1) {
                        sdebug("COPYING SUCCEEDED");
                    }
                } finally {
                    dst.close();
                }
            } finally {
                src.close();
            }
        } catch (IOException e) {
            if (DEBUG > 1) {
                e.printStackTrace();
            }
            return false;
        }

// make sure the file is executable
// Change#2 made for defect# 41396 - deny world permission (read/write) for the files copied from tuner-store to disk
// Only execute permission allowed for all the users
// Owner -  Deny Read, Deny Write, Allow Execute
// Group -  Deny Read, Deny Write, Allow Execute
// Others - Deny Read, Deny Write, Allow Execute
// REMIND === Defect #42037 has been filed denoting this error in the class
//            "com.marimba.desktop.FileSystem"
        if(destFile.exists()) {
            if(OSConstants.UNIX) {
                FileSystem.setModeBits(destFile,0x004);
            }
        }

        if (DEBUG > 1) {
            // verify that we correctly set the mode bits
            int b = FileSystem.getModeBits(destFile);
            sdebug("mode bits set to " + b + " on file " + destFile.getAbsolutePath());
        }

        return true;
    }

    /**
     * Copies data from one stream to another.
     */
    public static void copy(InputStream in, OutputStream out)
        throws IOException {
    	byte[] buf = new byte[4096];
        try {
            while (true) {
                int n = in.read(buf, 0, buf.length);
                if (n <= 0) {
                    return;
                }
                out.write(buf, 0, n);
            }
        } catch (EOFException e) {
            ;
        }
    }

    public static void copyFile(IChannel channel, String sourceFilePath, String destFilePath) {
        try {
            FileOutputStream out = new FileOutputStream(destFilePath);
            InputStream in = channel.getFile(sourceFilePath).getInputStream();
            copy(in, out);
            out.flush();
            out.close();
            in.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public void debug(String str) {
        System.out.println(getClass().getName() + ": " + str);
    }

    public static void sdebug(String str) {
        System.out.println("WorkspaceUtil: " + str);
    }
}

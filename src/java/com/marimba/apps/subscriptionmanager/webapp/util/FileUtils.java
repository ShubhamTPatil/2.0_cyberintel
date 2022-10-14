//Copyright 1996-2014, BMC Software Inc. All Rights Reserved.
//Confidential and Proprietary Information of BMC Software Inc.
//Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
//6,381,631, and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import marimba.io.FastOutputStream;
import com.marimba.tools.util.Props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * File Utils
 *
 * @author	Selvaraj Jegatheesan
 * @version 	$Revision$, $Date$
 */

public class FileUtils {
	// File Utils Constructor
	public FileUtils() {
	}
	/**
	 * Move directory from source to destination folder with source folder removal option
	 * @param srcDirPath
	 * @param destDirPath
	 * @param isSrcDel
	 */
	public static boolean moveDirectory(File srcDirPath, File destDirPath, boolean isSrcDel) {
		try {
			try {
				copyFolder(srcDirPath, destDirPath, isSrcDel);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				// error, just exit
			}
		} catch (Exception ec) {
			ec.printStackTrace();
		}
		return false;
	}
	/**
	 * Copy directory from source to destination folder with source folder removal option
	 * @param srcDirPath
	 * @param destDirPath
	 * @param isSrcDel
	 * @throws IOException
	 */
	private static void copyFolder(File srcDirPath, File destDirPath, boolean isSrcDel) throws IOException {

		if (!srcDirPath.exists()) {
			System.out.println("Source Directory does not exist :"	+ srcDirPath.getAbsolutePath());
		} else {
			if (srcDirPath.isDirectory()) {
				// if directory not exists, create it
				if (!destDirPath.exists()) {
					destDirPath.mkdir();
					System.out.println("Directory copied from " + srcDirPath + "  to " + destDirPath);
				}

				// list all the directory contents
				String files[] = srcDirPath.list();

				for (String file : files) {
					// construct the src and dest file structure
					File srcFile = new File(srcDirPath, file);
					File destFile = new File(destDirPath, file);
					if (!(destDirPath.getAbsoluteFile().equals(srcFile.getAbsoluteFile()))) {
						// recursive copy
						copyFolder(srcFile, destFile, isSrcDel);
						// User can able to delete source folder
						if(isSrcDel)	srcFile.delete();
					}
				}
			} else {
				// if file, then copy it
				// Use bytes stream to support all file types
				InputStream in = new FileInputStream(srcDirPath);
				OutputStream out = new FileOutputStream(destDirPath);

				byte[] buffer = new byte[1024];

				int length;
				// copy the file content in bytes
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
				in.close();
				out.close();
				System.out.println("File copied from " + srcDirPath + " to " + destDirPath);
			}
		}
	}
	/**
     * Write File
     * @param fileProps Props
     * @param dir String
     * @param fileName String
     */
    public static void writeFile(Props channelProps, File filePath) {
        FastOutputStream fos = null;

        try {
            fos = new FastOutputStream(filePath);
            try {
            	channelProps.save(fos);
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            //
        }
    }
    public static boolean removeDirectory(File dirPath) {
    	try {
    		if (dirPath.isDirectory()) {
    	        String[] children = dirPath.list();
    	        for (int i = 0; i < children.length; i++) {
    	            boolean success = removeDirectory(new File(dirPath, children[i]));
    	            if (!success) {
    	                return false;
    	            }
    	        }
    	    }
    	    return dirPath.delete(); // The directory is empty now and can be deleted.
    	} catch(Exception ec) {
    		System.out.println("Failed to remove directory : " + dirPath.getAbsolutePath());
    		ec.printStackTrace();
    	}
    	return false;
    }
}

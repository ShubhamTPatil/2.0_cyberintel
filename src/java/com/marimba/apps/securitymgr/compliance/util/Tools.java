// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.


// $File$, $Revision$, $Date$
package com.marimba.apps.securitymgr.compliance.util;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;

import com.marimba.intf.util.IConfig;
import java.io.IOException;
import java.io.FileOutputStream;
import java.security.cert.*;
import javax.net.ssl.*;

import org.w3c.dom.*;

import com.marimba.desktop.*;

import com.marimba.intf.application.*;
import com.marimba.intf.castanet.*;
import com.marimba.castanet.checksum.*;
import com.marimba.tools.util.*;
import com.marimba.tools.util.DebugFlag;



/**
 * Miscellaneous helper functions.
 *
 * @author      Nandakumar Sankaralingam
 * @version 	$Revision$, $Date$
 */
public class Tools  {
    private static final ChecksumAlgorithm csa = ChecksumFactory.getAlgorithm("MD5");
    public static final String CHAR_ENCODING_UTF_8    = "UTF-8";
    public static final String CHAR_ENCODING_UTF_16BE = "UTF-16BE";
    public static final String CHAR_ENCODING_UTF_16LE = "UTF-16LE";
    public static final String XML_TYPE_CDATA = "CDATA";
    public static SimpleDateFormat	shvDate = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat	shvUpdateDate = new SimpleDateFormat("yyyy-MM-dd");

    final static int DEBUG = DebugFlag.getDebug("DEFENSIGHT");
    private static IConfig tunerConfig;

    public Tools() {
    }

    public static void setTunerConfig(IConfig tunerCfg) {
        tunerConfig = tunerCfg;
    }

    public static IConfig getTunerConfig() {
        return tunerConfig;
    }


    public static String getCommandline(Vector args) {
	StringBuffer command = new StringBuffer();
	Enumeration e = args.elements();
	while (e.hasMoreElements()) {
	    String arg = (String)e.nextElement();
	    if (arg.indexOf(" ") > 0) {
		command.append("\"");
		command.append(arg);
		command.append("\"");
	    } else {
		command.append(arg);
	    }
	    if (e.hasMoreElements()) {
		command.append(" ");
	    }
	}
	return command.toString();
    }


    /**
     * Delete a file or recursively delete a directory tree.
     */
    public static boolean rmDir(File dir) {
	if (!dir.exists()) {
	    return true;
	} else {
	    return FileSystem.remove(dir);
	}
    }

    public static boolean copyDir(File src, File dst) {
	return copyDir(src, dst, null);
    }

    public static boolean copyDir(File src, File dst, FilenameFilter filter) {
	String list[] = null;
	if (filter != null) {
	    list = src.list(filter);
	} else {
	    list = src.list();
	}

	if (list == null) {
	    return false;
	}

	dst.mkdirs();
	for (int i=0; i < list.length; i++) {
	    File f = new File(src, list[i]);
	    if (f.isFile()) {
		try {
		    InputStream in = new FileInputStream(f);
		    try {
			try {
			    OutputStream out = new FileOutputStream(new File(dst, list[i]));
			    try {
				copyStream(in, out, false);
			    } finally {
				out.close();
			    }
			} catch (IOException ex) {
			    ex.printStackTrace();
			    return false;
			}
		    } finally {
			in.close();
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		    return false;
		}
	    } else if (!copyDir(f, new File(dst, list[i]), filter)) {
		return false;
	    }
	}

	return true;
    }

    public static void copyStream(InputStream in, OutputStream out, boolean close) throws IOException {
	StreamCopier sc = new StreamCopier(in, out, close);
	sc.copy();
	IOException ioex = sc.getIOException();
	if (ioex != null) {
	    throw ioex;
	}
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
	StreamCopier sc = new StreamCopier(in, out);
	sc.copy();
	IOException ioex = sc.getIOException();
	if (ioex != null) {
	    throw ioex;
	}
    }

    public static Thread copyStreamInThread(InputStream in, OutputStream out) {
	StreamCopier streamCopier = new StreamCopier(in, out);
	return streamCopier.start();
    }

    public static Thread copyStreamInThread(InputStream in, OutputStream out, boolean close) {
	StreamCopier streamCopier = new StreamCopier(in, out, close);
	return streamCopier.start();
    }



    public static String getWindowsPatchId(String bulletinId, String qnumber, String patchName) {
	 StringBuffer sb = new StringBuffer();
     try {
        if (bulletinId != null) {
            sb.append(bulletinId);
            sb.append(".");
        }
        if (qnumber != null) {
            sb.append(qnumber);
            sb.append(".");
        }
        sb.append(patchName);
        sb.append(".");
        sb.append(Integer.toString(patchName.hashCode()).replace('-','_'));
     } catch (Exception e) {
         e.printStackTrace();
     }
	 return sb.toString();
    }



    /**
     * Copies data from one stream to another.
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
	byte[] buf = new byte[4096];
	int n;
	while ((n = in.read(buf, 0, buf.length)) > 0) {
	    out.write(buf, 0, n);
	}
    }

    public static void removeDirectory(File d) {
	rmDir(d);
    }

    public synchronized static File getTempFolder(File dir) {
	File tmp = new File(dir,"temp");

	// find a name that doesn't exist in the directory
	int i=0;
	File f = null;
	long prefix = System.currentTimeMillis();
	do {
	    f = new File(tmp, prefix+"_"+i);
	    i++;
	} while (f.exists());

	f.mkdirs();
	return f;
    }


    static class StreamCopier implements Runnable {
	private InputStream in;
	private OutputStream out;
	private boolean close;
	private IOException ioex;

	public StreamCopier(InputStream in, OutputStream out) {
	    this(in, out, true);
	}

	public StreamCopier(InputStream in, OutputStream out, boolean close) {
	    this.in = in;
	    this.out = out;
	    this.close = close;
	}

	public final IOException getIOException() {
	    return ioex;
	}

	public Thread start() {
	    Thread copyThread = new Thread(this);
	    copyThread.start();
	    return copyThread;
	}

	public void run() {
	    copy();
	}

	public void copy() {
	    byte[] buff = new byte[1024];
	    int len = 0;
	    try {
		while ((len = in.read(buff, 0, buff.length)) > 0) {
		    out.write(buff, 0, len);
		}
		out.flush();
	    } catch (IOException e) {
		    ioex = e;
            if (DEBUG > 4) {
		        e.printStackTrace();
            }
	    } finally {
		if (close) {
		    try {
			try {
			    in.close();
			} finally {
			    out.close();
			}
		    } catch (IOException e) {
			ioex = e;
	    	    }
		}
	    }
	}
    }


    public static final String getCharacterEncoding(byte[] buf, int size) {
	if (size == 2) {
	    byte b0 = buf[0];
	    byte b1 = buf[1];
	    if (((b0 & 0xFE) == 0xFE) && ((b1 & 0xFF) == 0xFF)) {
		return CHAR_ENCODING_UTF_16BE;
	    } else if (((b0 & 0xFF) == 0xFF) && ((b1 & 0xFE) == 0xFE)) {
		return CHAR_ENCODING_UTF_16LE;
	    }
	} else if (size == 3) {
	    byte b0 = buf[0];
	    byte b1 = buf[1];
	    byte b2 = buf[2];
	    if (((b0 & 0xEF) == 0xEF) && ((b1 & 0xBB) == 0xBB) && ((b2 & 0xBF) == 0xBF)) {
		return CHAR_ENCODING_UTF_8;
	    }
	}
	return null;
    }

    public static final String getCharacterEncoding(InputStream is) {
	try {
	    byte[] buf = new byte[3];
	    int numRead = is.read(buf, 0, 3);
	    String enc = null;
	    if (numRead == 3) {
		enc = getCharacterEncoding(buf, 3);
		if (enc == null) {
		    enc = getCharacterEncoding(buf, 2);
		}
	    } else if (numRead == 2) {
		enc = getCharacterEncoding(buf, 2);
	    }
	    return enc;
	} catch (IOException ioex) {
	    return null;
	}
    }

    /**
     * Convert a version string of the form w.x.y.z to a numeric format
     * useful for comparisons.
     */
    public static final long getVersion(String version) {
	if (version == null || version.length() == 0) {
	    return 0;
	}
	long ver = 0;
	StringTokenizer st = new StringTokenizer(version, ".");
	int shiftAmt = 48;
	while (st.hasMoreTokens() && shiftAmt >= 0) {
	    String token = st.nextToken();
	    try {
		long l = Long.parseLong(token);
		ver |= (l << shiftAmt);
		shiftAmt -= 16;
	    } catch (NumberFormatException nfe) {
		System.out.println("Can't parse version part: " + token);
	    }
	}
	return ver;
    }

    /**
     * Decode a password. If no encoding is specified, then assume it is plain text.
     */
    public static final String decodePassword(String encodedPasswd) {
	if (encodedPasswd == null) {
	    return "";
	}
	int colon = encodedPasswd.indexOf(':');
	if (colon == -1) {
	    return encodedPasswd;
	}
	return Password.decode(encodedPasswd);
    }

    public static IChecksum convertToChecksum(String checksumStr) {
	if (checksumStr == null) {
	    return null;
	}
	IChecksum cs = null;
	if (checksumStr != null) {
	    if (checksumStr.startsWith("urn:md5:")) {
		Base64InputStream b64 = new Base64InputStream(new ByteArrayInputStream(checksumStr.substring(8).getBytes()));
		DataInput in = new DataInputStream(b64);
		cs = new MD5Checksum();
		try {
		    cs.read(in);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
	return cs;
    }

    /**
     * Return the MD5 checksum of the given file.
     */
    public static final IChecksum getChecksum(File f) {
	if (!f.isFile()) {
	    return null;
	}
	IChecksum cs = null;
	try {
	    cs = csa.checksum(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return cs;
    }

    public static final String[] getPropertyPairs(Properties props) {
	String[] pairs = new String[props.size() * 2];
	Enumeration keyEnum = props.propertyNames();
	for (int i = 0; keyEnum.hasMoreElements(); i++) {
	    String key = (String)keyEnum.nextElement();
	    String value = props.getProperty(key);
	    pairs[i] = key;
	    pairs[++i] = value;
	}
	return pairs;
    }

    /**
     * Given a DOM document node, find the first child node with the given tag name.
     */
    public static final Element findChildByTagName(Node parent, String tagname) {
	NodeList nodeList = parent.getChildNodes();
	int nodeListLength = nodeList.getLength();
	for (int i = 0; i < nodeListLength; i++) {
	    Node node = nodeList.item(i);
	    if (Node.ELEMENT_NODE != node.getNodeType()) {
		continue;
	    }
	    if (tagname.equals(node.getNodeName())) {
		return (Element)node;
	    }
	}
	return null;
    }


    /**
     * Unzip a .zip file to the specified directory.
     */
    public static final void unzip(File zipFile, File dir) throws IOException {
	InputStream is = null;
	boolean success = false;
	try {
	    is = new FileInputStream(zipFile);
	    unzip(is, dir);
	    success = true;
	} finally {
	    if (!success && is != null) {
		is.close();
	    }
	}
    }

    /**
     * Unzip a file inputstream to the specified directory.
     */
    public static final void unzip(InputStream in, File dir) throws IOException {
	//
	// the input stream is a zip archive so we unzip it to the
	// destination directory.
	//
	ZipInputStream zin = null;
	try {
	    zin = new ZipInputStream(in);
	    ZipEntry entry;
	    byte b[] = new byte[512];
	    while ((entry = zin.getNextEntry()) != null) {
		if (entry.isDirectory()) {
		    new File(dir,entry.getName()).mkdirs();
		    continue;
		}
		File f = new File(dir, entry.getName().replace('/', File.separatorChar));
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(f);
		    int l;
		    while ((l = zin.read(b)) != -1) {
			out.write(b, 0, l);
		    }
		} finally {
		    if (out != null) {
			out.close();
		    }
		}
	    }
	} finally {
	    if (zin != null) {
		zin.close();
	    }
	}
    }


    /**
 * Unzip a .zip file to the specified directory with newly configured locales.
     */
    public static final void unziplocale(File zipFile, File dir, String[] newLocales) throws IOException {
        InputStream is = null;
        boolean success = false;
        try {
            is = new FileInputStream(zipFile);
            unziplocale(is, dir, newLocales);
            success = true;
        } finally {
            if (!success && is != null) {
                is.close();
            }
        }
    }


/**
 * Unzip a file inputstream to the specified directory with newly configured locales.
 */
public static final void unziplocale(InputStream in, File dir, String[] newLocales) throws IOException {
    ZipInputStream zin = null;
    Vector defaultFilesList = new Vector();
    defaultFilesList.add("pkgdir/cache_checksum.txt");
    defaultFilesList.add("pkgdir/cache_pkgcode.txt");
    defaultFilesList.add("pkgdir/cache_properties.txt");
    defaultFilesList.add("pkgdir/cache_sfn.txt");
    defaultFilesList.add("pkgdir/cache_url.txt");

    try {
        zin = new ZipInputStream(in);
        ZipEntry entry;
        byte b[] = new byte[512];
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                for (int i = 0; i < newLocales.length; i++) {
                    if (entry.getName().toString().indexOf(newLocales[i]) != -1) {
                        new File(dir, entry.getName()).mkdirs();
                    }
                }
                continue;
            }
            File f = new File(dir, entry.getName().replace('/',
                    File.separatorChar));
            FileOutputStream out = null;

            if (f.exists()) {
                continue;
            }

            if (defaultFilesList.contains(entry.getName().toString())) {
                try {
                    out = new FileOutputStream(f);
                    int l;
                    while ((l = zin.read(b)) != -1) {
                        out.write(b, 0, l);
                    }
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            } else {
                for (int i = 0; i < newLocales.length; i++) {
                    if (entry.getName().toString().indexOf(newLocales[i]) != -1) {
                        try {
                            out = new FileOutputStream(f);
                            int l;
                            while ((l = zin.read(b)) != -1) {
                                out.write(b, 0, l);
                            }
                        } finally {
                            if (out != null) {
                                out.close();
                            }
                        }

                    }
                }
            }

        }
    } finally {
        if (zin != null) {
            zin.close();
        }
    }
}


    /**
     * Zip the contents of src to a dest .zip file. If includeDir is true, then
     * the directory name specified by src will be the toplevel folder in the .zip
     * file.
     */
    public static final void zip(File src, File destFile, boolean includeDir) throws IOException {
	ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destFile));
	zipOut.setMethod(ZipOutputStream.DEFLATED);
	zipOut.setLevel(9);
	try {
	    if (src.isDirectory()) {
		if (includeDir) {
		    zip(null, src, zipOut);
		} else {
		    File[] files = src.listFiles();
		    if (files != null) {
			int filesLength = files.length;
			for (int i = 0; i < filesLength; i++) {
			    File file = files[i];
			    zip(null, file, zipOut);
			}
		    }
		}
	    } else {
		zip(null, src, zipOut);
	    }
	} finally {
	    if (zipOut != null) {
		zipOut.close();
	    }
	}
    }

    /**
     * Helper method to recursively write the contents of src to the ZIP outputstream.
     */
    private static final void zip(String dirPath, File src, ZipOutputStream zipOut) throws IOException {
	ZipEntry zipEntry = null;
	if (src.isFile()) {
	    String filePath;
	    if (dirPath != null) {
		filePath = dirPath + src.getName();
	    } else {
		filePath = src.getName();
	    }
	    zipEntry = new ZipEntry(filePath);
	    zipOut.putNextEntry(zipEntry);
	    InputStream is = null;
	    try {
		is = new FileInputStream(src);
		copyStream(is, zipOut, false);
	    } finally {
		if (is != null) {
		    is.close();
		}
	    }
	} else if (src.isDirectory()) {
	    File[] files = src.listFiles();
	    if (files != null) {
		int filesLength = files.length;
		String dirName = src.getName();
		String newDirPath;
		if (dirPath != null) {
		    dirPath += (dirName + "/");
		} else {
		    dirPath = dirName + "/";
		}
		zipEntry = new ZipEntry(dirPath);
		zipOut.putNextEntry(zipEntry);
		for (int i = 0; i < filesLength; i++) {
		    File file = files[i];
		    zip(dirPath, file, zipOut);
		}
	    }
	}
    }


     public static String getHost(String address) throws IndexOutOfBoundsException {
        String host = null;

        if (address != null) {
            host = address.substring(0, address.indexOf(':'));
        }
        return host;

    }

   public static String getPort(String address) throws IndexOutOfBoundsException, NumberFormatException {
       String port = null;

       if (address != null) {
           port = address.substring(address.indexOf(':') + 1, address.length());
           //check for valid port number
           Integer.parseInt(port);
       }
       return port;
   }

}

// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.marimba.apps.subscriptionplugin.intf.ISecurityComplianceResult;
import com.marimba.apps.subscriptionplugin.intf.ISecurityComplianceResultNode;
import com.marimba.castanet.util.CompressInputStream;
import com.marimba.castanet.util.CompressOutputStream;
import com.marimba.io.FastInputStream;
import com.marimba.io.FastOutputStream;

/**
 * A class that represents the securitycompliance result information. It's a tree structure
 * where the nodes are the names of the objects. Some nodes have properties
 * that are kept in a string table keyVal.
 *
 */
public class SecurityComplianceResult
    implements ISecurityComplianceResult {
    private static final int SECURITY_COMPLIANCE_MAGIC = 0xCafeBeef;
    private static final int SECURITY_COMPLIANCE_VERSION = 1;
    private static final int EMBEDDED_VERSION = 2;
    private static final int COMPRESS_LEVEL = 6;
    private static final float SECURITY_COMPLIANCE_REPORT_VERSION = 2.0F;
    private static final float SECURITY_COMPLIANCE_EMBED_REPORT_VERSION = 1.9F;

    SecurityComplianceResultNode root = new SecurityComplianceResultNode("root"); // the root node in the tree

    public SecurityComplianceResult(File file) {
        readText(file);
    }

    public SecurityComplianceResult() {
    }

    public SecurityComplianceResult(DataInput fis)
                  throws IOException {
        readExternal(fis);
    }

    public SecurityComplianceResultNode getRoot() {
        return root;
    }

    /**
     * The function to add an object. The name is delimited by '.' to imply the
     * heirarchy. You can add an instance identifier using ':'. So e.g.,
     * machine.security_compliance:0 = {properties} Default instance number is
     * 0.
     */
    public SecurityComplianceResultNode add(String     inString,
                       Dictionary inDictionary) {
        int i = 0;
        String[] pairs = new String[inDictionary.size() * 2];
        Enumeration e = inDictionary.keys();

        Object key = null;
        Object value = null;

        while (e.hasMoreElements()) {
            key = e.nextElement();
            value = inDictionary.get(key);
            pairs[i] = key.toString();
            pairs[i + 1] = value.toString();
            i = i + 2;
        }

        if (false) {
            for (i = 0; i < pairs.length; i++) {
                System.out.println("pairs[" + i + "]=" + pairs[i]);
            }
        }

        return add(inString, pairs);
    }

    /**
     * Adds an empty node in the tree with the given name.
     */
    public SecurityComplianceResultNode add(String name) {
        return add(name, (String[]) null, -1);
    }

    /**
     * Adds a node in the tree with the given name and key-value pairs.
     */
    public SecurityComplianceResultNode add(String   name,
                       String[] keyVal) {
        return add(name, keyVal, -1);
    }

    /**
     * Adds a node in the tree with the given name, key-value pairs, and appId.
     */
    public SecurityComplianceResultNode add(String   name,
                       String[] keyVal,
                       int      appId) {
        SecurityComplianceResultNode n = root;

        if (name == null) {
            Thread.dumpStack();
        }

        // break up the name
        StringTokenizer st = new StringTokenizer(name, ".");

        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            int id = 0;
            int ind = tok.indexOf(':');

            if (ind != -1) {
                if (appId == -1) {
                    try {
                        id = Integer.parseInt(tok.substring(ind + 1));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else {
                    id = appId;
                }

                tok = tok.substring(0, ind);
            }

            n = n.getCreate(tok, id);
        }

        n.setKeyVal(keyVal);

        return n;
    }

    /**
     * Returns the node with the give name.
     */
    public SecurityComplianceResultNode getNode(String name) {
        SecurityComplianceResultNode n = root;

        StringTokenizer st = new StringTokenizer(name, ".");

        while (st.hasMoreTokens() && (n != null)) {
            String tok = st.nextToken();
            int id = 0;
            int ind = tok.indexOf(':');

            if (ind != -1) {
                try {
                    id = Integer.parseInt(tok.substring(ind + 1));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                tok = tok.substring(0, ind);
            }

            n = n.get(tok, id);
        }

        return n;
    }

    /**
     * This method goes through the SecurityComplianceResult and returns all the nodes
     * whose names begin with the String name passed in. This is useful to get
     * all nodes belonging to a certain class, for e.g. machine.security_compliance.
     */
    public Vector getNodesByName(String name) {
        Vector nodes = new Vector();

        if (name == null) {
            return null;
        }

        if (name.equals(root.getName())) {
            nodes.addElement(root);

            return nodes;
        }

        getNodesByName(root, name, nodes);

        return nodes;
    }

    /**
     * Helper method that computes all the nodes with a given name, given a
     * root node to start with.
     */
    private void getNodesByName(SecurityComplianceResultNode rootNode,
                                String  name,
                                Vector  nodes) {
        int index = name.indexOf('.');
        String childName = null;

        if (index != -1) {
            String tempName = name.substring(0, index);
            childName = name.substring(index + 1, name.length());
            name = tempName;
        }

        for (int i = 0; i < rootNode.nChildren; i++) {
            SecurityComplianceResultNode n = rootNode.children[i];
            index = n.getName()
                     .indexOf('[');

            String nodeName = n.getName();

            if (index != -1) {
                nodeName = nodeName.substring(0, index);
            }

            if (name.equals(nodeName)) {
                if (childName == null) {
                    nodes.addElement(n);
                } else {
                    getNodesByName(n, childName, nodes);
                }
            }
        }
    }

    /**
     * Returns a string representation of the tree.
     */
    public String toString() {
        return root.toString();
    }

    /**
     * Reads in the header, given a textual version of the tree.
     */
    private String readHeader(Reader in)
                       throws IOException {
        int count = 0;
        char[] buf = new char[50];

        int ch = in.read();
OUTER: 
        while (ch >= 0) {
            switch (ch) {
                case '\n':
                case '\r':
                case ']':
                    break OUTER;

                case '[':
                    ch = in.read();

                    break;

                default:

                    if (count == buf.length) {
                        char[] copy = new char[count * 2];
                        System.arraycopy(buf, 0, copy, 0, count);
                        buf = copy;
                    }

                    buf[count++] = (char) ch;
                    ch = in.read();
            }
        }

        return (ch == -1) ? null
                          : new String(buf, 0, count);
    }

    /**
     * Writes out a textual version of the tree to the print-stream.
     */
    public boolean writeText(PrintStream inPrintStream) {
        boolean ret = false;
        PrintWriter thePrintWriter = new PrintWriter(inPrintStream);

        try {
            writeText(thePrintWriter);
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if (thePrintWriter != null) {
                thePrintWriter.flush();
                thePrintWriter.close();
            }
        }

        return ret;
    }

    /**
     * Writes out a textual version of the tree.
     */
    public void writeText(Writer out)
                   throws IOException {
        // need the header info first
        out.write("[header]");
        out.write(SecurityComplianceResultNode.LINE_SEP);

        for (int i = 0; i < root.nChildren; i++) {
            root.children[i].writeTextHeader(null, out);
        }

        out.write(SecurityComplianceResultNode.LINE_SEP);
        Hashtable ht = new Hashtable();

        for (int i = 0; i < root.nChildren; i++) {
            root.children[i].writeText(null, out, ht);
        }
    }

    /**
     * Reads in the encoded key-values pairs from the textual version of the
     * tree.
     */
    Vector readKeyValTextEncoded(Reader in)
                          throws IOException {
        Vector vec = new Vector(6);
        char[] buf = new char[100];
        int count;

        int ch = in.read();

        while (ch >= 0) {
            switch (ch) {
                case '#':

                    for (; (ch >= 0) && (ch != '\n') && (ch != '\r');
                             ch = in.read()) {
                        ;
                    }

                    continue;

                case '\n':
                case '\r':
                    ch = in.read();

                    if (ch == '[') {
                        return vec;
                    } else if (ch == -1) {
                        return vec;
                    }

                    continue;
            }

            // read the property name and the '=' following it
            count = 0;

            while ((ch >= 0) && (ch != '=')) {
                if (ch == '\\') {
                    switch (ch = in.read()) {
                        case '\r':
                        case '\n':
                            throw new IllegalArgumentException("bogus properties list");

                        case 't':
                            ch = '\t';

                            break;

                        case 'n':
                            ch = '\n';

                            break;

                        case 'r':
                            ch = '\r';

                            break;

                        case 'u': {
                            while ((ch = in.read()) == 'u') {
                                ;
                            }

                            int d = 0;
loop: 
                            for (int i = 0; i < 4; i++, ch = in.read()) {
                                switch (ch) {
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        d = ((d << 4) + ch) - '0';

                                        break;

                                    case 'a':
                                    case 'b':
                                    case 'c':
                                    case 'd':
                                    case 'e':
                                    case 'f':
                                        d = ((d << 4) + 10 + ch) - 'a';

                                        break;

                                    case 'A':
                                    case 'B':
                                    case 'C':
                                    case 'D':
                                    case 'E':
                                    case 'F':
                                        d = ((d << 4) + 10 + ch) - 'A';

                                        break;

                                    default:
                                        break loop;
                                }
                            }

                            if (count == buf.length) {
                                char[] copy = new char[count * 2];
                                System.arraycopy(buf, 0, copy, 0, count);
                                buf = copy;
                            }

                            buf[count++] = (char) d;

                            // ch contains the next character
                            continue;
                        }
                    }
                }

                if (count == buf.length) {
                    char[] copy = new char[count * 2];
                    System.arraycopy(buf, 0, copy, 0, count);
                    buf = copy;
                }

                buf[count++] = (char) ch;
                ch = in.read();
            }

            String key = new String(buf, 0, count);

            // read the value until end of line
            count = 0;

            // read the first character
            ch = in.read();

            while ((ch >= 0) && (ch != '\n') && (ch != '\r')) {
                if (ch == '\\') {
                    switch (ch = in.read()) {
                        case '\r':

                            if (((ch = in.read()) == '\n') ||
                                    (ch == ' ') ||
                                    (ch == '\t')) {
                                // fall thru to '\n' case
                            } else {
                                continue;
                            }

                        case '\n':

                            while (((ch = in.read()) == ' ') || (ch == '\t')) {
                                ;
                            }

                            continue;

                        case 't':
                            ch = '\t';

                            break;

                        case 'n':
                            ch = '\n';

                            break;

                        case 'r':
                            ch = '\r';

                            break;

                        case 'u': {
                            while ((ch = in.read()) == 'u') {
                                ;
                            }

                            int d = 0;
loop: 
                            for (int i = 0; i < 4; i++, ch = in.read()) {
                                switch (ch) {
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        d = ((d << 4) + ch) - '0';

                                        break;

                                    case 'a':
                                    case 'b':
                                    case 'c':
                                    case 'd':
                                    case 'e':
                                    case 'f':
                                        d = ((d << 4) + 10 + ch) - 'a';

                                        break;

                                    case 'A':
                                    case 'B':
                                    case 'C':
                                    case 'D':
                                    case 'E':
                                    case 'F':
                                        d = ((d << 4) + 10 + ch) - 'A';

                                        break;

                                    default:
                                        break loop;
                                }
                            }

                            if (count == buf.length) {
                                char[] copy = new char[count * 2];
                                System.arraycopy(buf, 0, copy, 0, count);
                                buf = copy;
                            }

                            buf[count++] = (char) d;

                            // ch contains the next character
                            continue;
                        }
                    }
                }

                if (count == buf.length) {
                    char[] copy = new char[count * 2];
                    System.arraycopy(buf, 0, copy, 0, count);
                    buf = copy;
                }

                buf[count++] = (char) ch;
                ch = in.read();
            }

            String val = new String(buf, 0, count);
            vec.addElement(key);
            vec.addElement(val);
        }

        return vec;
    }

    /**
     * Reads in the contents of the tree, line-by-line from the textual format.
     *
     */
    public void readText(Reader in)
                  throws IOException {
        String line;
        int i = 1;

        while ((line = readHeader(in)) != null) {
            if ((i++ % 50) == 0) {
                try {
                    Thread.currentThread()
                          .sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    return;
                }
            }

            if (line.length() > 0) {
                Vector vec = readKeyValTextEncoded(in);

                if (!line.equals("header")) {
                    String[] keyval = new String[vec.size()];
                    vec.copyInto(keyval);
                    add(line, keyval);
                }
            }
        }
    }

    /**
     * Compress and serialize the tree into a byte array.
     */
    public byte[] compress() {
        FastOutputStream cos = null;

        try {
            FastOutputStream fos = new FastOutputStream(12 * 1024);
            CompressOutputStream cc;
            cos = new FastOutputStream(cc = new CompressOutputStream(fos,
                                                                     COMPRESS_LEVEL),
                                       512);

            try {
                writeExternal(cos);
                cos.flush();
                cc.finish();

                return fos.toByteArray();
            } finally {
                cos.close();
            }
        } catch (IOException e) {
            e.printStackTrace(); // shouldn't happen
        }

        return null;
    }

    /**
     * Compress and serialize the tree into a byte array.
     */
    public byte[] deflate() {
    	ByteArrayOutputStream output = new ByteArrayOutputStream();
    	DeflaterOutputStream dos;
    	FastOutputStream fos = new FastOutputStream(dos = new DeflaterOutputStream(output), 512);
    	try {
    		try {
    	    	writeExternal(fos);
    	    	fos.flush();
    	    	dos.finish();
    	    	dos.flush();
    	    	return output.toByteArray();
    		} finally {
    	    	fos.close();
    		}
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	return null;
    }

    /**
     * Uncompress a compressed and serialized tree from a byte array.
     */
    public static SecurityComplianceResult unCompress(byte[] securitycompliance)
                                    throws IOException {
        return unCompress(securitycompliance, SECURITY_COMPLIANCE_REPORT_VERSION);
    }

    /**
     * Clear the tree and read a text (uncompressed) securitycompliance file from disk.
     */
    public void readText(File file) {
        root = new SecurityComplianceResultNode("root");

        try {
            Reader in = new BufferedReader(new FileReader(file));

            try {
                readText(in);
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write an SecurityComplianceResult to a text (uncompressed) file on disk.
     */
    public boolean writeText(File file) {
        try {
            Writer out = new BufferedWriter(new FileWriter(file));

            try {
                writeText(out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    public static SecurityComplianceResult unCompress(InputStream in, float version) throws IOException {
        if (version == SECURITY_COMPLIANCE_REPORT_VERSION) {
            CompressInputStream cis = new CompressInputStream(in);

            try {
                return new SecurityComplianceResult(new FastInputStream(cis, 512));
            } finally {
                cis.close();
            }
        } else if (version == SECURITY_COMPLIANCE_EMBED_REPORT_VERSION) {
            InflaterInputStream iis = new InflaterInputStream(in);
            try {
                return new SecurityComplianceResult(new FastInputStream(iis, 512));
            } finally {
                iis.close();
            }
        } else {
            throw new IOException("Unknown Report Version: " + version);
        }
    }
    
    /**
     * Uncompress a compressed and serialized tree from a byte array, with a
     * specified report version.
     */
    public static SecurityComplianceResult unCompress(byte[] securitycompliance, float version) throws IOException {
        if (version == SECURITY_COMPLIANCE_REPORT_VERSION) {
            FastInputStream fis = new FastInputStream(securitycompliance, 0, securitycompliance.length);
            CompressInputStream cis = new CompressInputStream(fis);

            try {
                return new SecurityComplianceResult(new FastInputStream(cis, 512));
            } finally {
                cis.close();
            }
        } else if (version == SECURITY_COMPLIANCE_EMBED_REPORT_VERSION) {
            InflaterInputStream iis = new InflaterInputStream(new FastInputStream(securitycompliance, 0, securitycompliance.length));

            try {
                return new SecurityComplianceResult(new FastInputStream(iis, 512));
            } finally {
                iis.close();
            }
        } else {
            throw new IOException("Unknown Report Version: " + version);
        }
    }

    /**
     * Serialize it ourself instead of inefficent Java. Have version and magic
     * to make this extensible.
     */
    private void writeExternal(DataOutput out)
                        throws IOException {
        out.writeInt(SECURITY_COMPLIANCE_MAGIC);
        out.writeInt(SECURITY_COMPLIANCE_VERSION);
        root.writeBinary(out);
    }

    /**
     * Read from a stream in the format we stored it in.
     */
    private void readExternal(DataInput in)
                       throws IOException {
        int magic = in.readInt();

        if (magic != SECURITY_COMPLIANCE_MAGIC) {
            throw new IOException("bad magic: " + magic);
        }

        int version = in.readInt();

        switch (version) {
            case SECURITY_COMPLIANCE_VERSION:
                root = new SecurityComplianceResultNode(in);

                break;

            case EMBEDDED_VERSION:

                SecurityComplianceResult ret = new SecurityComplianceResult();

                try {
                    Reader reader = new BufferedReader(new InputStreamReader((FastInputStream) in));

                    try {
                        ret.readText(reader);
                        root = ret.getRoot();
                    } finally {
                        reader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    return;
                }

                break;

            default:
                throw new IOException("bad version: " + version + " != " +
                                      SECURITY_COMPLIANCE_VERSION);
        }
    }

    /**
     * Delete a node from the tree by name.  Note that this cannot be used to
     * delete a single instance of an object (e.g., machine.security_compliance:1), just an
     * entire node (e.g., hardware.disk).
     */
    public void delete(String name)
                throws IllegalArgumentException {
        if ("root".equalsIgnoreCase(name)) {
            throw new IllegalArgumentException("Root node cannot be deleted");
        }

        root.delete(name);
    }

    public static void main(String[] args)
                     throws Exception {
        SecurityComplianceResult tree = new SecurityComplianceResult();
        String[] props = { "rul1", "pass" };
        String[] props2 = { "rule2", "fail" };

        tree.add("machine.security_compliance:0", props);
        System.out.println(tree.toString());

        List nodes = tree.getNodesByName("machine.security_compliance");

        for (int i = 0; i < nodes.size(); i++) {
            SecurityComplianceResultNode o = (SecurityComplianceResultNode) nodes.get(i);
            System.out.println("Node name is: " + o.getName());
        }

        byte[] comp = tree.compress();
        System.out.println("The length of the compressed piece is: " +
                           comp.length);

        try {
            FastOutputStream fos = new FastOutputStream(new File("securitycompliance"),
                                                        1024);

            try {
                fos.write(comp);
                System.out.println("Wrote out the file...");
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FastInputStream fis = new FastInputStream(new File("securitycompliance"), 1024);

            try {
                System.out.println("Length of file is: " + fis.available());
                comp = new byte[fis.available()];
                fis.readFully(comp);

                SecurityComplianceResult tt = SecurityComplianceResult.unCompress(comp);
                System.out.println("The re-read tree is: \n" + tt);
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtain the root of the tree.
     */
	public ISecurityComplianceResultNode findRoot() {
		return getRoot();
	}

	/**
     * Get the node with the given name
     */
	public ISecurityComplianceResultNode findNode(String name) {
		return getNode(name);
	}

	/**
     * Walk the tree and find all nodes whose names begin with the given
     * string. This is useful to get all nodes of a certain class. (e.g.,
     * machine.security_compliance)
     */
	public List findNodesByPrefix(String prefix) {
		return getNodesByName(prefix);
	}
}

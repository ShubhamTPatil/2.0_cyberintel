// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Writer;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import com.marimba.apps.subscriptionplugin.intf.ISecurityComplianceResultNode;

/**
 * This class represents an securitycompliance-node. These nodes are what make up the
 * securitycompliance result tree.
 *
 */
public class SecurityComplianceResultNode
    implements ISecurityComplianceResultNode {
    static final String LINE_SEP = System.getProperty("line.separator");
    protected static final byte[] hex = {
                                            (byte) '0', (byte) '1', (byte) '2',
                                            (byte) '3', (byte) '4', (byte) '5',
                                            (byte) '6', (byte) '7', (byte) '8',
                                            (byte) '9', (byte) 'A', (byte) 'B',
                                            (byte) 'C', (byte) 'D', (byte) 'E',
                                            (byte) 'F'
                                        };
    static int level = -1;
    SecurityComplianceResultNode[] children; // array of children
    int nChildren; // number of children for this node
    private Hashtable<String,Object> keyVal = new Hashtable<String,Object>(); // hashtable of key-value pairs for this node
    private String name; // the name of the node
    private int id; // the id of this node

    public SecurityComplianceResultNode(String name) {
        this(name, 0);
    }

    public SecurityComplianceResultNode(String name,
                   int    id) {
        this.name = name;
        this.id = id;
    }

    public SecurityComplianceResultNode(DataInput in)
            throws IOException {
        name = in.readUTF();
        id = in.readInt();
        nChildren = in.readInt();

        int len = (int) in.readShort();
        String key = null;
        String val = null;

        if (len != 0) {
            keyVal = new Hashtable(len / 2);

            for (int i = len; i > 0; i -= 2) {
                val = in.readUTF();
                key = in.readUTF();
                keyVal.put(key, val);
            }
        }

        children = new SecurityComplianceResultNode[nChildren];

        for (int i = nChildren; --i >= 0;) {
            children[i] = new SecurityComplianceResultNode(in);
        }
    }

    /**
     * Sets the children by copying the children from "node". The method name
     * unfortunately doesn't reflect that, but its too late now...
     */
    public void makeChild(SecurityComplianceResultNode node) {
        this.children = node.children;
        this.nChildren = node.nChildren;
    }

    /**
     * Writes out the node to the output stream, in binary format.
     */
    public void writeBinary(DataOutput out)
                     throws IOException {
        String key = null;
        String val = null;

        if (keyVal != null) {
            out.writeUTF(name);
            out.writeInt(id);
            out.writeInt(nChildren);

            out.writeShort(keyVal.size() * 2);

            for (Enumeration e = keyVal.keys(); e.hasMoreElements();) {
                key = (String) e.nextElement();
                val = (String) keyVal.get(key);
                out.writeUTF(val);
                out.writeUTF(key);
            }
        }

        // walk through and write out the children
        for (int i = nChildren; --i >= 0;) {
            children[i].writeBinary(out);
        }
    }

    /**
     * Writes out the textual header info for the node.
     */
    void writeTextHeader(String parent,
                         Writer out)
                  throws IOException {
        if (nChildren > 1) {
            String prefix;

            if (parent == null) {
                prefix = name;
            } else {
                prefix = parent + "." + name;
            }

            // go through the children
            Hashtable ht = new Hashtable(101);

            for (int i = 0; i < nChildren; i++) {
                if (children[i].keyVal != null) {
                    String key = children[i].name;
                    Integer num = (Integer) ht.get(key);

                    if (num == null) { // never seen
                        ht.put(key, new Integer(children[i].id));
                    } else if (children[i].id > num.intValue()) { // update if larger
                        ht.put(key, new Integer(children[i].id));
                    }
                }
            }

            for (Enumeration e = ht.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                int val = ((Integer) ht.get(key)).intValue();
                val++; // since 0 based

                if (val > 1) {
                    out.write(prefix + "." + key + "=" + val + LINE_SEP);
                }
            }
        }

        // walk through and write out the children
        for (int i = 0; i < nChildren; i++) {
            children[i].writeTextHeader(((parent == null) ? ""
                                                          : (parent + ".")) +
                                        name, out);
        }
    }

    /**
     * Properly encodes the text before writing it out.
     */
    private void writeTextEncoded(String s,
                                  Writer out)
                           throws IOException {
        int len = s.length();

        for (int i = 0; i < len; i++) {
            int ch = s.charAt(i);

            switch (ch) {
                case '\\':
                    out.write('\\');
                    out.write('\\');

                    break;

                case '\t':
                    out.write('\\');
                    out.write('t');

                    break;

                case '\n':
                    out.write('\\');
                    out.write('n');

                    break;

                case '\r':
                    out.write('\\');
                    out.write('r');

                    break;

                case '=':
                    out.write('\\');
                    out.write('=');

                    break;

                default:

                    if ((ch < ' ') || (ch >= 127)) {
                        out.write('\\');
                        out.write('u');
                        out.write(hex[(ch >> 12) & 0xF]);
                        out.write(hex[(ch >> 8) & 0xF]);
                        out.write(hex[(ch >> 4) & 0xF]);
                        out.write(hex[(ch >> 0) & 0xF]);
                    } else {
                        out.write(ch);
                    }
            }
        }
    }

    /**
     * Writes out the actual contents of the node in textual format.
     */
    void writeText(String parent,
                   Writer out, Hashtable ht)
            throws IOException {
        String currNodeName = name;
        if (parent != null) {
            currNodeName = parent + "." + name;
        }

        int index = 0;
        String indVal = (String) ht.get(currNodeName);
        if (indVal != null) {
            index = Integer.parseInt(indVal) + 1;
        }

        if (keyVal != null) {
            out.write("[" + currNodeName + ":" + index + "]" + LINE_SEP);
            id = index;

            String key = null;
            String val = null;

            for (Enumeration e = keyVal.keys(); e.hasMoreElements();) {
                key = (String) e.nextElement();
                val = (String) keyVal.get(key);
                writeTextEncoded(key, out);
                out.write("=");
                writeTextEncoded(val, out);
                out.write(LINE_SEP);
            }

            out.write(LINE_SEP);
            ht.put(currNodeName, id+"");
        }

        // walk through and write out the children
        for (int i = 0; i < nChildren; i++) {
            children[i].writeText((index == 0) ? currNodeName : (currNodeName + ":" + index), out, ht);
        }
    }

    /**
     * Ensures that we have enough space in the node to add children. Grows the
     * node, if needed.
     */
    private void ensureCapacity(int len) {
        if (children == null) {
            children = new SecurityComplianceResultNode[Math.max(3, len)];
        } else if (len > children.length) {
            SecurityComplianceResultNode[] tmp = new SecurityComplianceResultNode[Math.max(3 + children.length, len)];
            System.arraycopy(children, 0, tmp, 0, children.length);
            children = tmp;
        }
    }

    /**
     * Given a name and an instance id, returns the node.
     */
    public SecurityComplianceResultNode get(String nm,
                       int    id) {
        if (children != null) {
            // walk through and see if any of the children match
            for (int i = 0; i < nChildren; i++) {
                if (nm.equals(children[i].name) && (id == children[i].id)) {
                    return children[i];
                }
            }
        }

        return null;
    }

    /**
     * Given a name and an instance id, return the node. Create it if it
     * doesn't exist.
     */
    public SecurityComplianceResultNode getCreate(String nm,
                             int    id) {
        // need to see if it already exists
        SecurityComplianceResultNode ret = get(nm, id);

        if (ret != null) {
            return ret;
        }

        // need to create
        ensureCapacity(nChildren + 1);
        children[nChildren++] = new SecurityComplianceResultNode(nm, id);

        return children[nChildren - 1];
    }

    /**
     * Add a node to the tree.
     */
    public void add(SecurityComplianceResultNode node) {
        ensureCapacity(nChildren + 1);
        children[nChildren++] = node;
    }

    /**
     * This method is used to delete a named node in the tree. The named node
     * can be in the form machine.security_compliance, in which case it will go down the
     * tuner branch, locate the channel node and delete it.
     */
    public void delete(String name)
                throws IllegalArgumentException {
        if (children == null) {
            return;
        }

        String grandChild = null;

        // Now the node name is in the form tuner.channel or a simple element machine
        // check if the node requested for deletion is a compound node by looking for the
        // presence of a .
        int index = name.indexOf(".");

        if (index != -1) {
            grandChild = name.substring(index + 1, name.length());
            name = name.substring(0, index);
        }

        for (int i = 0; i < children.length; i++) {
            // strip out the node index to compare the names, ie. get machine from machine:0
            if (children[i] != null) {
                if (name.equals(children[i].name + ":" + children[i].id)) {
                    SecurityComplianceResultNode[] temp = new SecurityComplianceResultNode[children.length - 1];
                    System.arraycopy(children, 0, temp, 0, i);
                    System.arraycopy(children, i + 1, temp, i,
                                     children.length - 1 - i);

                    int tempIndex = 0;

                    for (int j = 0; j < temp.length; j++) {
                        if ((temp[j] != null) && name.startsWith(temp[j].name)) {
                            temp[j].id = tempIndex++;
                        }
                    }

                    children = temp;
                    nChildren--;

                    return;
                }

                StringTokenizer st = new StringTokenizer(children[i].name, ":",
                                                         false);


                if(st.hasMoreTokens()) {
                    String childName = st.nextToken();
                    if (name.equals(childName)) {
                        // Check if compound elements were meant to be deleted
                        // by checking for the existence of a grandChild element.
                        if (grandChild != null) {
                            // this indicates that we are down the right branch of the tree
                            children[i].delete(grandChild);

                            return;
                        }

                        // This was not a compound element and we are supposed to delete everything
                        // under this node.
                        children[i] = null;
                        nChildren--;
                    }
                }
            } else {
                nChildren--;
            }
        }

        // prune the array of the null elements
        children = shrinkArray(children);
        nChildren = children.length;
    }

    /**
     * Shrinks the children array.
     */
    SecurityComplianceResultNode[] shrinkArray(SecurityComplianceResultNode[] children) {
        int count = 0;

        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                count++;
            }
        }

        SecurityComplianceResultNode[] newChildren = new SecurityComplianceResultNode[count];
        int j = 0;

        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                newChildren[j] = children[i];
                j++;
            }
        }

        return newChildren;
    }

    /**
     * Deletes the child node at the given index.
     */
    public void delete(int ind)
                throws IllegalArgumentException {
        if ((ind < 0) || (ind >= nChildren)) {
            throw new IllegalArgumentException("ind was " + ind + " length = " +
                                               nChildren);
        }

        String name = children[ind].name;
        int id = children[ind].id;

        for (int i = 0; i < nChildren; i++) {
            SecurityComplianceResultNode child = children[i];

            if (child.name.equals(name)) {
                // is the one that needs to be deleted?
                if (ind == i) { // this is the one to delete
                    System.arraycopy(children, i + 1, children, i,
                                     nChildren - i - 1);
                    nChildren--;
                } else if (child.id > id) {
                    child.id--;
                }
            }
        }
    }

    /**
     * An overloaded delete() that finds matching attribute with the given
     * value in the given list of nodes and removes them
     */
    public void delete(String name,
                       String attributeName,
                       String value) {
        if ((null == name) || (null == attributeName) || (null == value)) {
            return;
        }

        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                if (children[i].name.equals(name)) {
                    String val = children[i].getVal(attributeName);

                    if ((null != val) && val.equals(value)) {
                        delete(children[i].name + ":" + children[i].id);
                        i--;
                    }
                }
            } else {
                continue;
            }
        }
    }

    /**
     * Get the list of key/value pairs for the node. The list is returned as a
     * String[] containing alternating keys and values.
     */
    public String[] getKeyVal() {
        if (null == keyVal) {
            return new String[0];
        }

        // REMIND: cache this for better performance
        String[] array = new String[keyVal.size() * 2];
        int i = 0;
        String key = null;
        String val = null;

        for (Enumeration e = keyVal.keys(); e.hasMoreElements();) {
            key = (String) e.nextElement();
            val = (String) keyVal.get(key);
            array[i++] = key;
            array[i++] = val;
        }

        return array;
    }

    /**
     * Set the list of key/value pairs for the node.  The list is passed as a
     * String[] containing alternating keys and values.
     */
    public void setKeyVal(String[] array) {
        // clear the list
        keyVal = new Hashtable();

        if (null != array) {
            for (int i = 0; i < array.length; i++) {
			    if(i <array.length-1 && null != array[i+1]) { 
                    keyVal.put(array[i], ((array[++i]==null)?"":array[i]));
				}
            }
        }
    }

    /**
     * Get an individual value from the list of key/value pairs for the node.
     * The key must not be null.
     */
    public String getVal(String key) {
        return ((key == null) || (keyVal == null)) ? null
                                                   : (String) keyVal.get(key);
    }

    public String getString(String key) {
        return getVal(key);
    }

    /**
     * Overloaded method that returns the string value of the given key
     */
    public String getString(String key, String defaultValue) {
        String value = getVal(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public boolean getBoolean(String key) {
        return "true".equals(getVal(key));
    }

    public int getInt(String key) {
        String val = getVal(key);

        if (val == null) {
            return Integer.MIN_VALUE;
        }

        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    public long getLong(String key) {
        String val = getVal(key);

        if (val == null) {
            return Long.MIN_VALUE;
        }

        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }

    public short getShort(String key) {
        String val = getVal(key);

        if (val == null) {
            return Short.MIN_VALUE;
        }

        try {
            return Short.parseShort(val);
        } catch (NumberFormatException e) {
            return Short.MIN_VALUE;
        }
    }

    public float getFloat(String key) {
        String val = getVal(key);

        if (val == null) {
            return Float.MIN_VALUE;
        }

        try {
            return Float.valueOf(val)
                        .floatValue();
        } catch (NumberFormatException e) {
            return Float.MIN_VALUE;
        }
    }

    /**
     * Set an individual value in the list of key/value pairs for the node. The
     * key must not be null.  If the value is null, the key/value pair is
     * deleted from the list.
     */
    public void setVal(String key,
                       String val) {
        if (null == key) {
            return;
        }

        // make sure keyVal exists before proceeding
        if (null == keyVal) {
            keyVal = new Hashtable();
        }

        if (null == val) {
            // a null value means we should delete it from the list
            keyVal.remove(key);
        } else {
            // otherwise add it to the list
            keyVal.put(key, val);
        }
    }

    /**
     * Returns the name of the node.
     */
    public String getName() {
        return name + "[" + id + "]";
    }

    public String toString() {
        StringBuffer ret = new StringBuffer(10);
        level++;

        for (int i = 0; i < level; i++) {
            ret.append("\t");
        }

        ret.append("*" + name + ":" + id + "* ");

        if (keyVal != null) {
            String key = null;
            String val = null;
            ret.append("KEYVAL = {");

            for (Enumeration e = keyVal.keys(); e.hasMoreElements();) {
                key = (String) e.nextElement();
                val = (String) keyVal.get(key);
                ret.append(key + " = " + val + ", ");
            }

            ret.append("}");
        }

        ret.append(LINE_SEP);

        if (nChildren == 0) {
            level--;

            return ret.toString();
        }

        for (int i = 0; i < nChildren; i++) {
            ret.append(children[i].toString());
        }

        level--;

        return ret.toString();
    }

    /**
     * Returns a node in the tree with the given prefix and instance number.
     */
    public static SecurityComplianceResultNode getChildNode(SecurityComplianceResultNode node,
                                       int     i,
                                       String  prefix) {
        StringTokenizer st = new StringTokenizer(prefix, ".");
        int toks = st.countTokens();

        while ((toks > 0) && (node != null)) {
            String tok = st.nextToken();

            if (toks-- == 1) {
                node = node.get(tok, i);

                return node;
            } else {
                node = node.get(tok, 0);
            }
        }

        return null;
    }

    /**
     * Get the property keys within the node.
     */
    public String[] getKeys() {
        return (String[]) this.keyVal.keySet()
                                     .toArray(new String[0]);
    }

    /**
     * Count the number of child nodes.
     */
    public int getChildrenCount() {
        return this.nChildren;
    }

    /**
     * Get the children of the node.
     */
    public ISecurityComplianceResultNode[] getChildren() {
        return this.children;
    }

    /**
     * Get the offspring under the current node with the given prefix and the
     * instance number.
     */
    public ISecurityComplianceResultNode getDescendant(String prefix,
                                        int    instanceNumber) {
        return SecurityComplianceResultNode.getChildNode(this, instanceNumber, prefix);
    }

    /**
     * Get a property by name.
     */
    public String getProperty(String name) {
        return getVal(name);
    }

    /**
     * Get an array of (key, value) pairs. The keys and values are alternated.
     * The list is not sorted.
     */
    public String[] getPropertyPairs() {
        return getKeyVal();
    }
}

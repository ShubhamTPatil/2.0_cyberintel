// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.query;

import com.marimba.str.Str;
import com.marimba.tools.util.URLUTF8Decoder;
import com.marimba.tools.xml.XMLClient;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import com.marimba.tools.util.Props;
import com.marimba.tools.xml.XMLException;
import com.marimba.tools.xml.XMLParser;

/**
 * Created with IntelliJ IDEA.
 * User: ingsantha
 * Date: 5/9/17
 * Time: 12:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryListing implements XMLClient {

    //final static int DEBUG                 = DebugFlag.getDebug("DBTREE/DIR");

    final static int TAG_TREE              = 0;
    final static int TAG_METADATA          = 1;
    final static int TAG_NODE 	           = 2;
    final static int TAG_DIRNODE           = 3;
    final static int TAG_PROPERTY          = 4;
    final static int TAG_LANGUAGE          = 5;

    final static String EMPTY              = "";
    static Hashtable XMLTAGS;
    DirNode currentDirNode = null;
    DirNode parentDirNode = null;
    String currentProperty = null;
    public Map<String, DirNode> queryMap = new LinkedHashMap<String, DirNode>();

    static {
        XMLTAGS = new Hashtable(6);
        XMLTAGS.put("tree", new Integer(TAG_TREE));
        XMLTAGS.put("metadata", new Integer(TAG_METADATA));
        XMLTAGS.put("node", new Integer(TAG_NODE));
        XMLTAGS.put("dirnode",new Integer(TAG_DIRNODE));
        XMLTAGS.put("property", new Integer(TAG_PROPERTY));
        XMLTAGS.put("language", new Integer(TAG_LANGUAGE));
    }

    public QueryListing() {

    }

    public DirNode getCurrentDirNode() {
        return currentDirNode;
    }

    public DirNode getDirNode(String nodeName) {
        return queryMap.get(nodeName);
    }

    public void nameSpace(Str str, Str str2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int resolveTag(Str tag) {
        Integer i = (Integer)XMLTAGS.get(tag.toString());
        return i == null ? TAG_UNKNOWN : i.intValue();
    }

    @Override
    public int resolveEntity(Str str) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleStartTag(int tag, Str name, Hashtable atts, boolean empty) {

        switch (tag) {
            case TAG_TREE:
                break;
            case TAG_METADATA:
                break;
            case TAG_NODE:
                String dirNodeName = currentDirNode.getName();

                currentDirNode = new DirNode();
                currentDirNode.setDisplayName(((Str)atts.get("name")).toString());
                currentDirNode.setName(dirNodeName + ((Str)atts.get("name")).toString());
                //currentDirNode.setType(((Str) atts.get("type")).toString());
                currentDirNode.setClassName(((Str)atts.get("class")).toString());
                break;
            case TAG_DIRNODE:
                currentDirNode = new DirNode();
                currentDirNode.setDisplayName(((Str)atts.get("name")).toString());
                currentDirNode.setName("/"+((Str)atts.get("name")).toString() + "/");
                currentDirNode.setType(((Str) atts.get("type")).toString());
                currentDirNode.setClassName(((Str) atts.get("class")).toString());
                parentDirNode = currentDirNode;
                break;
            case TAG_PROPERTY:
                currentProperty = ((Str)atts.get("name")).toString();
                break;
            case TAG_LANGUAGE:
                break;
        }
    }

    @Override
    public void handleData(char[] data, int length) {

        String value = new String(data, 0, length);
        if (null != currentDirNode && currentProperty != null) {
            currentDirNode.setProps(currentProperty, value);
        }
    }

    @Override
    public void handleEndTag(int tag, Str str) {
        switch (tag) {
            case TAG_TREE:
                break;
            case TAG_METADATA:
                break;
            case TAG_NODE:
                queryMap.put(currentDirNode.getName(), currentDirNode);
                parentDirNode.addNode(currentDirNode);
                currentDirNode = parentDirNode;
                break;
            case TAG_DIRNODE:
                if (currentDirNode != null) {
                    queryMap.put(currentDirNode.getName(), currentDirNode);
                }
                break;
            case TAG_PROPERTY:
                break;
            case TAG_LANGUAGE:
                break;
        }
    }

    public static void main(String args[]) throws Exception {
        XMLParser theXMLParser = new XMLParser();
        theXMLParser.setUTF(true);
        InputStream xmlInputStream = new FileInputStream("C://SecurityComplianceReports.xml");
        QueryListing queryListing = new QueryListing();
        theXMLParser.parse(xmlInputStream, queryListing);
        for (String key : queryListing.queryMap.keySet()) {
            System.out.println("Query Name"+key);
            DirNode node = queryListing.queryMap.get(key);
            System.out.println(node.getValue("atlas.node.sql"));
        }
    }
}

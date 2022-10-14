// Copyright 2019, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;

import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.*;
import com.marimba.str.*;
import com.marimba.tools.util.DebugFlag;
import com.marimba.tools.util.Props;
import com.marimba.tools.xml.XMLClient;
import com.marimba.tools.xml.XMLParser;

public class CVEInfoUtil {
    public static boolean DEBUG = DebugFlag.getDebug("SECURITY") >= 5;
    private final IApplicationContext context;
    private final Map<Integer, HashMap<String, Set<String>>> cveInfo;
    private final String dataDir;
    String shouldFetchFromCIRCL;
    IChannel vDefChannel;

    public static int CVE_INFO_NOT_SUPPORTED = 0;
    public static int CVE_INFO_SOLUTION = 1;
    public static int CVE_INFO_REFERENCE = 2;
    public static String[] CVE_INFO_TYPES = new String[] {"Not Supported", "Solution", "Reference"};

    public CVEInfoUtil(IApplicationContext context) {
        this.context = context;
        this.dataDir = this.context.getDataDirectory();
        this.shouldFetchFromCIRCL = "never";
        this.cveInfo = new HashMap<Integer, HashMap<String, Set<String>>>();
    }

    public CVEInfoUtil(String dataDirectory) {
        this.context = null;
        this.dataDir = dataDirectory;
        this.shouldFetchFromCIRCL = "never";
        this.cveInfo = new HashMap<Integer, HashMap<String, Set<String>>>();
    }

    public static void main(String[] args) {
        DEBUG = true;
        try {
            debug("main(), dataDir - " + args[0]);
            debug("main(), cveID - " + args[1]);

            CVEInfoUtil cveInfoHandler = new CVEInfoUtil(args[0]);

            Set<String> solutions = cveInfoHandler.getInfo(args[1], CVE_INFO_SOLUTION);
            debug("main(), solutions - " + solutions);

            Set<String> references = cveInfoHandler.getInfo(args[1], CVE_INFO_REFERENCE);
            debug("main(), references - " + references);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public IChannel getvDefChannel() {
        return vDefChannel;
    }

    public void setvDefChannel(IChannel vDefChannel) {
        this.vDefChannel = vDefChannel;
    }

    public String getShouldFetchFromCIRCL() {
        if ((this.shouldFetchFromCIRCL == null) || (this.shouldFetchFromCIRCL.trim().length() < 1)) {
            this.shouldFetchFromCIRCL = "never";
        }
        return shouldFetchFromCIRCL;
    }

    public void setShouldFetchFromCIRCL(String shouldFetchFromCIRCL) {
        this.shouldFetchFromCIRCL = shouldFetchFromCIRCL;
        if ((this.shouldFetchFromCIRCL == null) || (this.shouldFetchFromCIRCL.trim().length() < 1)) {
            this.shouldFetchFromCIRCL = "never";
        }
    }

    public Set<String> getInfo(String cveID, int infoType) {
        debug("getInfo(), cveID - " + cveID);
        debug("getInfo(), infoType - " + infoType);
        if ((cveID == null) || (cveID.trim().length() < 1)) {
            //invalid cveID
            return new HashSet<String>();
        }
        if ((infoType < 1) || (infoType > 2)) {
            //we don't support retrieval of the specified info for CVEs
            return new HashSet<String>();
        }

        try {
            if (!"always".equalsIgnoreCase(getShouldFetchFromCIRCL())) {
                //1. Check if the requested data is available in-memory...
                if (this.cveInfo.containsKey(infoType) && this.cveInfo.get(infoType).containsKey(cveID)) {
                    debug("getInfo(), returning from in-memory for " + cveID);
                    return this.cveInfo.get(infoType).get(cveID);
                }

                //2. if we don't have the info in-memory, check the same from info file in data dir...
                Set<String> infoFromFile = getInfoFromFile(cveID, infoType);
                if (!infoFromFile.isEmpty()) {
                    if (!cveInfo.containsKey(infoType)) {
                        cveInfo.put(infoType, new HashMap<String, Set<String>>());
                    }
                    cveInfo.get(infoType).put(cveID, infoFromFile);
                    debug("getInfo(), returning from local file for " + cveID);
                    return infoFromFile;
                }

                //3. if we don't have the info both in-memory and in-file, check the same from vDef channel...
                Set<String> infoFromVDef = getInfoFromVDef(cveID, infoType);
                if (!infoFromVDef.isEmpty()) {
                    if (!cveInfo.containsKey(infoType)) {
                        cveInfo.put(infoType, new HashMap<String, Set<String>>());
                    }
                    cveInfo.get(infoType).put(cveID, infoFromVDef);
                    debug("getInfo(), returning from vDef for " + cveID);
                    return infoFromFile;
                }
            }

            if (!"never".equalsIgnoreCase(getShouldFetchFromCIRCL())) {
                //4. if we don't have the info from in-memory, in-file and also in vDef, check the same from CIRCL portal url...
                if (getInfoFromCIRCL(cveID, infoType) && this.cveInfo.containsKey(infoType) && this.cveInfo.get(infoType).containsKey(cveID)) {
                    saveInfoToFile(cveID, infoType);
                    return this.cveInfo.get(infoType).get(cveID);
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        // we failed to get the requested info for this CVE... return empty result...
        return new HashSet<String>();
    }

    protected Set<String> getInfoFromFile(String cveID, int infoType) {
        debug("getInfoFromFile(), cveID - " + cveID);
        debug("getInfoFromFile(), infoType - " + infoType);
        if ((cveID == null) || (cveID.trim().length() < 1)) {
            //invalid cveID
            return new HashSet<String>();
        }
        if ((infoType < 1) || (infoType > 2)) {
            //we don't support retrieval of the specified info for CVEs
            return new HashSet<String>();
        }

        Set<String> cveInfoSet = new HashSet<String>();
        File cveInfoDir = new File(dataDir, "cve-info" + File.separator + CVE_INFO_TYPES[infoType]);
        File requiredCVEInfoFile = new File(cveInfoDir, cveID + ".txt");

        if (requiredCVEInfoFile.exists()) {
            try {
                Props requiredCVEInfoProps = new Props(requiredCVEInfoFile);
                requiredCVEInfoProps.load();
                String cveInfoValue = requiredCVEInfoProps.getProperty(cveID + "." + CVE_INFO_TYPES[infoType], null);
                if (cveInfoValue != null && !cveInfoValue.isEmpty()) {
                    String[] cveInfos = cveInfoValue.split(";");
                    for (String cveInfo: cveInfos){
                        cveInfoSet.add(cveInfo);
                    }
                }
                requiredCVEInfoProps.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return cveInfoSet;
    }

    protected Set<String> getInfoFromVDef(String cveID, int infoType) {
        debug("getInfoFromVDef(), cveID - " + cveID);
        debug("getInfoFromVDef(), infoType - " + infoType);
        if ((cveID == null) || (cveID.trim().length() < 1)) {
            //invalid cveID
            return new HashSet<String>();
        }
        if ((infoType < 1) || (infoType > 2)) {
            //we don't support retrieval of the specified info for CVEs
            return new HashSet<String>();
        }

        Set<String> cveInfoSet = new HashSet<String>();
        if (vDefChannel != null) {
            try {
                String cveInfoDir = dataDir + File.separator + "cve-info" + File.separator + CVE_INFO_TYPES[infoType];
                new File(cveInfoDir).mkdirs();

                copyFromChannel(new StrString("/cve-info/" + CVE_INFO_TYPES[infoType] + "/" + cveID + ".txt"), new File(cveInfoDir, cveID + ".txt"), vDefChannel);

                File requiredCVEInfoFile = new File(cveInfoDir, cveID + ".txt");

                if (requiredCVEInfoFile.exists()) {
                    Props requiredCVEInfoProps = new Props(requiredCVEInfoFile);
                    requiredCVEInfoProps.load();
                    String cveInfoValue = requiredCVEInfoProps.getProperty(cveID + "." + CVE_INFO_TYPES[infoType], null);
                    if (cveInfoValue != null && !cveInfoValue.isEmpty()) {
                        String[] cveInfos = cveInfoValue.split(";");
                        for (String cveInfo : cveInfos) {
                            cveInfoSet.add(cveInfo);
                        }
                    }
                    requiredCVEInfoProps.close();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return cveInfoSet;
    }

    protected boolean getInfoFromCIRCL(String cveID, int infoType) {
        debug("getInfoFromCIRCL(), cveID - " + cveID);
        debug("getInfoFromCIRCL(), infoType - " + infoType);
        if ((cveID == null) || (cveID.trim().length() < 1)) {
            //invalid cveID
            return false;
        }
        if ((infoType < 1) || (infoType > 2)) {
            //we don't support retrieval of the specified info for CVEs
            return false;
        }

        boolean retVal = false;
        boolean done = false;
        String url = "http://cve.circl.lu/api/cve/" + cveID;
        try {
            do {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.addRequestProperty("Connection", "close");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Clarinet vDesk");

                int httpReply = connection.getResponseCode();
                if (httpReply == HttpURLConnection.HTTP_OK) {
                    debug("getInfoFromCIRCL(), received HTTP_OK");
                    done = true;
                    retVal = true;
                    InputStream in = connection.getInputStream();
                    if (in == null) {
                        return false;
                    }
                    int bytesToRead = connection.getContentLength();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    if(bytesToRead == -1) {
                        while (in.available() > 0) {
                            bout.write(in.read());
                        }
                        in.close();
                    } else {
                        byte[] buff = new byte[1024];
                        int readUpTillNow = 0;
                        do {
                            int n = in.read(buff);
                            if (n > 0) {
                                bout.write(buff, 0, n);
                                readUpTillNow += n;
                            }
                        } while(readUpTillNow < bytesToRead);
                    }

                    String s = new String(bout.toByteArray());
                    JSONObject resp = new JSONObject(s);

                    Set<String> infoSet = new HashSet<String>();
					if (CVE_INFO_SOLUTION == infoType) {
                        if (resp.has("capec")) {
                            JSONArray capec = resp.getJSONArray("capec");
                            for (int i = 0; i < capec.length(); i++) {
                                JSONObject capecObj = capec.getJSONObject(i);
                                if (capecObj.has("solutions")) {
                                    String solutions = capecObj.getString("solutions");
                                    infoSet.add(solutions);
                                }
                            }
                        }
                    } else if (CVE_INFO_REFERENCE == infoType) {
                        if (resp.has("references")) {
                            JSONArray cpes = resp.getJSONArray("references");
                            for (int i = 0; i < cpes.length(); i++) {
                                String reference = cpes.getString(i);
                                infoSet.add(reference);
                            }
                        }
                    }

                    if (!infoSet.isEmpty()) {
                        if (!this.cveInfo.containsKey(infoType)) {
                            this.cveInfo.put(infoType, new HashMap<String, Set<String>>());
                        }
                        this.cveInfo.get(infoType).put(cveID, infoSet);
                    }
                } else if (httpReply == HttpURLConnection.HTTP_MOVED_PERM
                        || httpReply == HttpURLConnection.HTTP_MOVED_TEMP) {
                    debug("getInfoFromCIRCL(), received HTTP_MOVED_PERM || HTTP_MOVED_TEMP");
                    url = connection.getHeaderField("Location");
                    connection.getInputStream().close();
                } else {
                    debug("getInfoFromCIRCL(), received " + httpReply);
                    done = true;
                    retVal = false;
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    InputStream in = connection.getErrorStream();
                    if (in != null) {
                        while (in.available() > 0) {
                            bout.write(in.read());
                        }
                        in.close();
                    }
                    String errorResponse = new String(bout.toByteArray());
                    debug("getInfoFromCIRCL(), received " + httpReply + ", errorResponse - " + errorResponse);
                }
            } while (!done);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }

        return retVal;
    }

    protected void saveInfoToFile(String cveID, int infoType) {
        debug("saveInfoToFile(), cveID - " + cveID);
        debug("saveInfoToFile(), infoType - " + infoType);
        if ((cveID == null) || (cveID.trim().length() < 1)) {
            //invalid cveID
            return;
        }
        if ((infoType < 1) || (infoType > 2)) {
            //we don't support retrieval of the specified info for CVEs
            return ;
        }

        Set<String> cveInfoSet = this.cveInfo.get(infoType).get(cveID);
        File cveInfoDir = new File(dataDir, "cve-info" + File.separator + CVE_INFO_TYPES[infoType]);
        if (!cveInfoDir.exists()) {
            cveInfoDir.mkdirs();
        }
        try {
            Props cveInfoProps = new Props(new File(cveInfoDir, cveID + ".txt"));
            cveInfoProps.load();

            StringBuilder cveInfoBuilder = new StringBuilder();
            for (String cpe : cveInfoSet) {
                cveInfoBuilder.append(cpe);
                cveInfoBuilder.append(";");
            }

            cveInfoProps.setProperty(cveID + "." + CVE_INFO_TYPES[infoType], cveInfoBuilder.toString());
            cveInfoProps.save();
            cveInfoProps.close();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void copyFromChannel(Str path, File dest, IChannel src) throws IOException {
        IIndex index = src.getIndex();
        INode node = index.lookup(path);

        if (dest.isDirectory() &&!dest.exists()) {
            dest.mkdirs();
        }

        if (node != null) {
            if (node.isDirectory()) {
                INode[] children = node.getChildren();
                int len = node.countChildren();

                for (int i = 0; i < len; i++) {
                    INode child = children[i];
                    File file = new File(dest, child.getName().toString());

                    if (child.isDirectory()) {
                        file.mkdirs();
                    }

                    Str p = (path.endsWith("/"))
                            ? path.append(child.getName())
                            : path.append('/').append(child.getName());

                    copyFromChannel(p, file, src);
                }
            } else {
                copyFile(src, path, dest);
            }
        }
    }

    void copyFile(IChannel src, Str path, File dest) throws IOException {
        IFile file = src.getFile(path.toString());
        InputStream in = file.getInputStream();
        OutputStream out = new FileOutputStream(dest);
        byte[] buf = new byte[2048];

        try {
            int len = 0;

            while ((len = in.read(buf, 0, buf.length)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                try { in.close(); } catch (Exception ex) {}
            }
        }
    }

    public static void debug(String str) {
        if (DEBUG) {
            System.out.println("CVEInfoUtil.java :: [" + new Date().toString() + "] ==> " + str);
        }
    }
}

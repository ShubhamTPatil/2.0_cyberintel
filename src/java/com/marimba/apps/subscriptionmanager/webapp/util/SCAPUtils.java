// Copyright 2019, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.beans.OvalDetailBean;
import com.marimba.apps.subscriptionmanager.beans.SCAPContentDetails;
import com.marimba.apps.subscriptionmanager.beans.SCAPProfileDetails;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.*;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.util.IConfig;
import com.marimba.oval.util.OVALHandler;
import com.marimba.oval.util.xml.profiles.OVALProfile;
import com.marimba.oval.util.xml.profiles.OVALProfileDefinition;
import com.marimba.str.Str;
import com.marimba.str.StrString;
import com.marimba.tools.util.DebugFlag;
import com.marimba.tools.util.URLUTF8Decoder;
import com.marimba.xccdf.util.XCCDFException;
import com.marimba.xccdf.util.XCCDFHandler;
import com.marimba.xccdf.util.xml.groups.XCCDFGroup;
import com.marimba.xccdf.util.xml.profiles.XCCDFProfile;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SCAPUtils {
    public static boolean DEBUG = DebugFlag.getDebug("SECURITY") >= 5;
    static SCAPUtils scapUtils = null;
    private Map<String, List<OvalDetailBean>>  ovalCVEDefinitionDetails = null;
    private Map<String, List<OvalDetailBean>>  ovalCVEDefinitionDetailsPersist = null;
    private Map<String, List<OvalDetailBean>>  ovalDefinitionDetails = null;
    private Map<String, List<OvalDetailBean>>  ovalDefinitionDetailsPersist = null;

    private List<String> osInfoDetails = new ArrayList<String>();

    String[] supportedScapContents = null;
    String[] supportedUsgcbContents = null;
    String[] customSecurityContents = null;
    String[] customSecurityWindowsContents = null;
    String[] customSecurityNonWindowsContents = null;
    String[] supportedScapContentsXML = null;
    String[] supportedUsgcbContentsXML = null;
    String[] customSecurityContentsXML = null;
    String[] customSecurityWindowsContentsXML = null;
    String[] customSecurityNonWindowsContentsXML = null;
    Map<String, String> xmlContentTypeMap = new HashMap<String, String>();
    Map<String, String> xmlContentTypeMapPersist = null;
    Map<String, ArrayList<OVALProfileDefinition>> contentProfileDefinitionsMap = new HashMap<String, ArrayList<OVALProfileDefinition>>();
    Map<String, ArrayList<OVALProfileDefinition>> contentProfileDefinitionsMapPersist = null;

    String securityManagerUrl;
    String securityInfoUrl;

    String securityManagerCveInfoDir = null;
    String securityManagerXCCDFDir = null;
    String securityManagerOVALDir = null;
    String dataDir = null;

    ServletContext context;
    SubscriptionMain main;

    IApplicationContext iApplicationContext;
    ILauncher launcher;
    IWorkspace workspace;
    IConfig cmsTunerConfig;
    IChannel securityInfoChannel;
    XCCDFHandler xccdfHandler;
    OVALHandler ovalHandler;
    protected DB ovalDB = null;

    private CVEInfoUtil cveInfoUtil;

    public DB getOvalDB() {
        return ovalDB;
    }

    public static SCAPUtils getSCAPUtils() {
        if (scapUtils != null) {
            return scapUtils;
        }

        return scapUtils = new SCAPUtils();
    }

    public void setDebug(boolean _DEBUG) {
        DEBUG = _DEBUG;
    }

    public void setManagerChannelURL(String securityManagerChannelURL) {
        this.securityManagerUrl = securityManagerChannelURL;
    }

    public void init(ServletContext _context, IApplicationContext _iApplicationContext, SubscriptionMain _main) {
        try {
            debug("init(), _context - " + _context);
            debug("init(), _iApplicationContext - " + _iApplicationContext);
            debug("init(), _main - " + _main);
            this.main = _main;
            this.dataDir = main.getDataDirectory().getAbsolutePath();
            debug("init(), dataDir - " + dataDir);

            this.context = _context;

            this.iApplicationContext = _iApplicationContext;
            this.launcher = (ILauncher) iApplicationContext.getFeature("launcher");
            this.workspace = (IWorkspace) iApplicationContext.getFeature("workspace");
            this.cmsTunerConfig = (IConfig) iApplicationContext.getFeature("config");

            securityInfoUrl = getSecurityInfoChannelURL(securityManagerUrl);
            debug("init(), vDeskUrl - " + securityManagerUrl);
            debug("init(), vDefUrl - " + securityInfoUrl);

            securityInfoChannel = getChannel(securityInfoUrl);

            this.cveInfoUtil = new CVEInfoUtil(dataDir);
            this.cveInfoUtil.setvDefChannel(securityInfoChannel);
            this.cveInfoUtil.setShouldFetchFromCIRCL(cmsTunerConfig.getProperty("marimba.vdesk.circl.cvesearch"));
            debug("init(), cveInfoUtil - " + cveInfoUtil);

            IChannel securityInfoChannel = getSecurityInfoChannelCreated();
            debug("init(), vDefChannel - " + securityInfoChannel);
            if (securityInfoChannel != null) {
                File ovalDBFile = new File(dataDir + File.separator + "vDesk.db");
                if (ovalDBFile.exists()) {
                    ovalDBFile.delete();
                }
                this.ovalDB = DBMaker.fileDB(ovalDBFile).closeOnJvmShutdown().transactionEnable().make();
                securityManagerCveInfoDir = dataDir + File.separator + "cve-info";
                securityManagerXCCDFDir = dataDir + File.separator + "xccdf";
                securityManagerOVALDir = dataDir + File.separator + "oval";
                debug("init(), securityManagerCveInfoDir - " + securityManagerCveInfoDir);
                debug("init(), vDeskXCCDFDir - " + securityManagerXCCDFDir);
                debug("init(), vDeskOVALDir - " + securityManagerOVALDir);

                new File(securityManagerCveInfoDir).mkdirs();
                new File(securityManagerXCCDFDir).mkdirs();
                new File(securityManagerOVALDir).mkdirs();

                String securityManagerXCCDFWindowsLocation = securityManagerXCCDFDir + File.separator + "windows";
                String securityManagerXCCDFNonWindowsLocation = securityManagerXCCDFDir + File.separator + "nonwindows";
                String securityManagerXCCDFCustomWindowsLocation = securityManagerXCCDFDir + File.separator + "custom" + File.separator + "windows";
                String securityManagerXCCDFCustomNonWindowsLocation = securityManagerXCCDFDir + File.separator + "custom" + File.separator + "nonwindows";

                // oval definition
                String securityManagerOVALWindowsLocation = securityManagerOVALDir + File.separator + "windows";
                String securityManagerOVALNonWindowsLocation = securityManagerOVALDir + File.separator + "nonwindows";

                String metadataLocation = dataDir + File.separator + "metadata.properties";

                deleteDir(new File(securityManagerXCCDFWindowsLocation));
                deleteDir(new File(securityManagerXCCDFNonWindowsLocation));
                deleteDir(new File(securityManagerXCCDFCustomWindowsLocation));
                deleteDir(new File(securityManagerXCCDFCustomNonWindowsLocation));

                // oval definition
                deleteDir(new File(securityManagerOVALWindowsLocation));
                deleteDir(new File(securityManagerOVALNonWindowsLocation));

                // delete metadata file of all definitions
                deleteFile(new File(dataDir + File.separator + "metadata.properties"));

                new File(securityManagerXCCDFWindowsLocation).mkdirs();
                new File(securityManagerXCCDFNonWindowsLocation).mkdirs();
                new File(securityManagerXCCDFCustomWindowsLocation).mkdirs();
                new File(securityManagerXCCDFCustomNonWindowsLocation).mkdirs();
                // oval definition
                new File(securityManagerOVALWindowsLocation).mkdirs();
                new File(securityManagerOVALNonWindowsLocation).mkdirs();

                copyFromChannel(new StrString("/cve-info"), new File(securityManagerCveInfoDir), securityInfoChannel);
                copyFromChannel(new StrString("/xccdf/windows"), new File(securityManagerXCCDFWindowsLocation), securityInfoChannel);
                copyFromChannel(new StrString("/xccdf/nonwindows"), new File(securityManagerXCCDFNonWindowsLocation), securityInfoChannel);
                copyFromChannel(new StrString("/xccdf/custom/windows"), new File(securityManagerXCCDFCustomWindowsLocation), securityInfoChannel);
                copyFromChannel(new StrString("/xccdf/custom/nonwindows"), new File(securityManagerXCCDFCustomNonWindowsLocation), securityInfoChannel);
                // oval definition
                copyFromChannel(new StrString("/oval/windows"), new File(securityManagerOVALWindowsLocation), securityInfoChannel);
                copyFromChannel(new StrString("/oval/nonwindows"), new File(securityManagerOVALNonWindowsLocation), securityInfoChannel);

                // metadata file for all definitions
                copyFromChannel(new StrString("/metadata.properties"), new File(metadataLocation), securityInfoChannel);

                InputStream is = null;
                IFile file = securityInfoChannel.getFile("/xccdf/windows/metadata.properties");
                String[] scapContents1 = null;
                String[] scapContentsXML1 = null;
                if (file != null) {
                    is = file.getInputStream();
                    debug("init(), is1 - " + is);
                    if (null != is) {
                        Properties props = new Properties();
                        props.load(is);
                        ArrayList propertyList = Collections.list(props.propertyNames());

                        scapContents1 = new String[propertyList.size()];
                        scapContentsXML1 = new String[propertyList.size()];
                        debug("init(), props - " + props);
                        for (int i = 0; i < propertyList.size(); i++) {
                            String key = (String) propertyList.get(i);
                            scapContents1[i] = key;
                            scapContentsXML1[i] = props.getProperty(key);
                        }
                        is.close();
                    }
                }
                file = securityInfoChannel.getFile("/oval/windows/metadata.properties");
                String[] scapContents2 = null;
                String[] scapContentsXML2 = null;
                if (file != null) {
                    is = file.getInputStream();
                    debug("init(), is1 - " + is);
                    if (null != is) {
                        Properties props = new Properties();
                        props.load(is);
                        ArrayList propertyList = Collections.list(props.propertyNames());

                        scapContents2 = new String[propertyList.size()];
                        scapContentsXML2 = new String[propertyList.size()];
                        debug("init(), props - " + props);
                        for (int i = 0; i < propertyList.size(); i++) {
                            String key = (String) propertyList.get(i);
                            scapContents2[i] = key;
                            scapContentsXML2[i] = props.getProperty(key);
                        }
                        is.close();
                    }
                }
                supportedUsgcbContents = combine(scapContents1, scapContents2);
                supportedUsgcbContentsXML = combine(scapContentsXML1, scapContentsXML2);
                if (DEBUG) {
                    for(int i=0; (supportedUsgcbContents != null) && (i < supportedUsgcbContents.length); i++) {
                        System.out.println("SCAP Windows Content " + (i + 1) + " - " + supportedUsgcbContents[i] + "(" + supportedUsgcbContentsXML[i] + ")");
                    }
                }

                file = securityInfoChannel.getFile("/xccdf/nonwindows/metadata.properties");
                String[] scapContents = null;
                String[] scapContentsXML = null;
                if (file != null) {
                    is = file.getInputStream();
                    debug("init(), is2 - " + is);
                    if (null != is) {
                        Properties props = new Properties();
                        props.load(is);
                        ArrayList propertyList = Collections.list(props.propertyNames());

                        scapContents = new String[propertyList.size()];
                        scapContentsXML = new String[propertyList.size()];
                        debug("init(), props2 - " + props);
                        for (int i = 0; i < propertyList.size(); i++) {
                            String key = (String) propertyList.get(i);
                            scapContents[i] = key;
                            scapContentsXML[i] = props.getProperty(key);
                        }
                        is.close();
                    }
                }
                file = securityInfoChannel.getFile("/oval/nonwindows/metadata.properties");
                String[] scapOvalContents = null;
                String[] scapOvalContentsXML = null;
                if (file != null) {
                    is = file.getInputStream();
                    debug("init(), is2 - " + is);
                    if (null != is) {
                        Properties props = new Properties();
                        props.load(is);
                        ArrayList propertyList = Collections.list(props.propertyNames());
                        scapOvalContents = new String[propertyList.size()];
                        scapOvalContentsXML = new String[propertyList.size()];

                        debug("init(), props2 - " + props);
                        for (int i = 0; i < propertyList.size(); i++) {
                            String key = (String) propertyList.get(i);
                            scapOvalContents[i] = key;
                            scapOvalContentsXML[i] = props.getProperty(key);
                        }
                        is.close();
                    }
                }
                supportedScapContents = combine(scapContents, scapOvalContents);
                supportedScapContentsXML = combine(scapContentsXML, scapOvalContentsXML);

                if (DEBUG) {
                    for(int i=0; (supportedScapContents != null) && (i < supportedScapContents.length); i++) {
                        System.out.println("SCAP Non-Windows Content " + (i + 1) + " - " + supportedScapContents[i] + "(" + supportedScapContentsXML[i] + ")");
                    }
                }

                file = securityInfoChannel.getFile("/xccdf/custom/windows/metadata.properties");
                if (file != null) {
                    is = file.getInputStream();
                    debug("init(), is3 - " + is);
                    if (null != is) {
                        Properties props = new Properties();
                        props.load(is);
                        ArrayList propertyList = Collections.list(props.propertyNames());

                        customSecurityWindowsContents = new String[propertyList.size()];
                        customSecurityWindowsContentsXML = new String[propertyList.size()];
                        debug("init(), props3 - " + props);
                        for (int i = 0; i < propertyList.size(); i++) {
                            String key = (String) propertyList.get(i);
                            customSecurityWindowsContents[i] = key;
                            customSecurityWindowsContentsXML[i] = props.getProperty(key);
                        }
                        is.close();
                    }
                }

                file = securityInfoChannel.getFile("/xccdf/custom/nonwindows/metadata.properties");
                if (file != null) {
                    is = file.getInputStream();
                    debug("init(), is4 - " + is);
                    if (null != is) {
                        Properties props = new Properties();
                        props.load(is);
                        ArrayList propertyList = Collections.list(props.propertyNames());

                        customSecurityNonWindowsContents = new String[propertyList.size()];
                        customSecurityNonWindowsContentsXML = new String[propertyList.size()];
                        debug("init(), props4 - " + props);
                        for (int i = 0; i < propertyList.size(); i++) {
                            String key = (String) propertyList.get(i);
                            customSecurityNonWindowsContents[i] = key;
                            customSecurityNonWindowsContentsXML[i] = props.getProperty(key);
                        }
                        is.close();
                    }
                }

                int customSecurityContentsSize = ((customSecurityWindowsContents == null) ? 0 : customSecurityWindowsContents.length) +
                        ((customSecurityNonWindowsContents == null) ? 0 : customSecurityNonWindowsContents.length);
                debug("init(), customSecurityContentsSize - " + customSecurityContentsSize);
                if (DEBUG) {
                    for(int i=0; (customSecurityWindowsContents != null) && (i < customSecurityWindowsContents.length); i++) {
                        System.out.println("Custom Security Windows Content " + (i + 1) + " - " + customSecurityWindowsContents[i] + "(" + customSecurityWindowsContentsXML[i] + ")");
                    }
                    for(int i=0; (customSecurityNonWindowsContents != null) && (i < customSecurityNonWindowsContents.length); i++) {
                        System.out.println("Custom Security Non-Windows Content " + (i + 1) + " - " + customSecurityNonWindowsContents[i] + "(" + customSecurityNonWindowsContentsXML[i] + ")");
                    }
                }
                if (customSecurityContentsSize > 0) {
                    if ((customSecurityWindowsContents == null) || (customSecurityWindowsContents.length < 1)) {
                        customSecurityContents = customSecurityNonWindowsContents;
                        customSecurityContentsXML = customSecurityNonWindowsContentsXML;
                    } else if ((customSecurityNonWindowsContents == null) || (customSecurityNonWindowsContents.length < 1)) {
                        customSecurityContents = customSecurityWindowsContents;
                        customSecurityContentsXML = customSecurityWindowsContentsXML;
                    } else {
                        customSecurityContents = new String[customSecurityContentsSize];
                        customSecurityContentsXML = new String[customSecurityContentsSize];

                        for(int i=0; i < customSecurityWindowsContents.length; i++) {
                            customSecurityContents[i] = customSecurityWindowsContents[i];
                            customSecurityContentsXML[i] = customSecurityWindowsContentsXML[i];
                        }

                        for(int i=0, k=customSecurityWindowsContents.length; k < customSecurityContentsSize && i<customSecurityNonWindowsContents.length; i++, k++) {
                            customSecurityContents[k] = customSecurityNonWindowsContents[i];
                            customSecurityContentsXML[k] = customSecurityNonWindowsContentsXML[i];
                        }
                    }
                }
                ovalDB.commit();
            }

            xccdfHandler = new XCCDFHandler();
            ovalHandler = new OVALHandler();

            loadContentTypeMetadata(main.getDataDirectory().getAbsolutePath());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    private void deleteFile(File deleteFile) {
        try {
            if(null != deleteFile) {
                deleteFile.deleteOnExit();
            }
        } catch(Exception ed) {

        }
    }
    private static String[] combine(String[] firstStrArray, String[] secondStrArray){
        if(null == firstStrArray && null == secondStrArray) {
            return null;
        }
        if(null == firstStrArray) {
            return secondStrArray;
        }
        if(null == secondStrArray) {
            return firstStrArray;
        }
        int length = firstStrArray.length + secondStrArray.length;

        String[] result = new String[length];
        System.arraycopy(firstStrArray, 0, result, 0, firstStrArray.length);
        System.arraycopy(secondStrArray, 0, result, firstStrArray.length, secondStrArray.length);

        return result;
    }

    public List<String> getOsInfoDetails(SubscriptionMain subscriptionMain) {
        String query = "select distinct substring(content_title, 1, charindex('OVAL',content_title)-2) OS_Name  \n" +
                "FROM inv_sec_oval_defn_cve_details order by OS_Name";
        OSInfoQueryRunner runner = new OSInfoQueryRunner(subscriptionMain, query);
        osInfoDetails = runner.getOsInfoBeanList();
        return osInfoDetails;
    }

    public String list(String target, String xml, String profile, String format, String template) throws XCCDFException {
        debug("list(), target - " + target);
        debug("list(), xml - " + xml);
        debug("list(), profile - " + profile);
        debug("list(), format - " + format);
        debug("list(), template - " + template);
        String xmlFolder = securityManagerXCCDFDir + File.separator + target;
        debug("list(), xmlFolder - " + xmlFolder);

        String xmlContentType = getXMLContentType(xml);
        debug("list(), xmlContentType - " + xmlContentType);

        if(null != xmlContentType && "oval".equals(xmlContentType)) {
            xmlFolder = securityManagerOVALDir + File.separator + target;
        }

        if(null != target && "custom".equals(target)) {
            if (isCustomWindowsContentsXML(xml)) {
                xmlFolder += File.separator + "windows";
            } else if (isCustomNonWindowsContentsXML(xml)) {
                xmlFolder += File.separator + "nonwindows";
            }
        }

        String xmlFile = xmlFolder + File.separator + xml;
        String xmlNameNoExt = xml.substring(0, xml.length() - 4);
        String htmlName = xmlNameNoExt + ".html";
        try {
            String templatePath = null;
            if ((template != null) && (template.trim().length() > 0)) {
                if("custom".equals(target)) {
                    templatePath = new File(dataDir + File.separator + "customtemplates" + File.separator + template).getAbsolutePath();
                } else if("windows".equals(target)) {
                    templatePath = new File(dataDir + File.separator + "usgcbtemplates" + File.separator + template).getAbsolutePath();
                } else if("nonwindows".equals(target)) {
                    templatePath = new File(dataDir + File.separator + "scaptemplates" + File.separator + template).getAbsolutePath();
                }
            }
            debug("list(), templatePath - " + templatePath);
            int result = 0;
            if(null != xmlContentType && "oval".equals(xmlContentType)) {
                result = ovalHandler.list(xmlFile, format, templatePath, "/spm/images/", "/spm/includes/latest/", true);
            } else {
                result = xccdfHandler.list(xmlFile, profile, format, templatePath, "/spm/images/", "/spm/includes/latest/", true);
            }
            if (0 == result) {
                String htmlFile = xmlFolder + File.separator + htmlName;
                debug("list(), htmlFile - " + htmlFile);
                debug("list(), templatePath - " + templatePath);

                File customizationFile = null;
                String customizationFileName = null;
                String customizationFileNameNoExt = null;
                if ((templatePath != null) && (templatePath.trim().length() > 0)) {
                    customizationFile = new File(templatePath);
                    customizationFileName = customizationFile.getName();
                    customizationFileNameNoExt = customizationFileName.substring(0, customizationFileName.length() - 11);
                    htmlName = xmlNameNoExt + "_" + customizationFileNameNoExt + ".html";
                    htmlFile = xmlFolder + File.separator + htmlName;
                    debug("list(), customizationFileNameNoExt - " + customizationFileNameNoExt);
                    debug("list(), htmlFile - " + htmlFile);
                    debug("list(), new File(htmlFile).exists() - " + new File(htmlFile).exists());
                    if (new File(htmlFile).exists()) {
                        return htmlFile;
                    } else {
                        htmlFile = xmlFolder + File.separator + htmlName;
                    }
                }
                return new File(htmlFile).exists() ? htmlFile : null;
            } else {
                return null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public HashMap<String, Object> getValues(String xml, String profileName) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        try {
            if (xccdfHandler != null) {
                values = xccdfHandler.getValues(xml, profileName, true);
            }
        } catch (Exception ex) {
            if (true) {
                ex.printStackTrace();
            }
        }
        return values;
    }

    public java.util.List<SCAPContentDetails> readStandardSCAPContents(String type) {
        java.util.List<SCAPContentDetails> scapDetails = new ArrayList<SCAPContentDetails>();
        Map<String, String> scapContentsMap = new HashMap<String, String>();

        String platform = null;
        try {
            String[] scapTypes = null;
            String[] scapXMLs = null;
            String xccdfLocation = null;
            if (null == type || "scap".equals(type)) {
                scapTypes = getSupportedSCAPContents();
                scapXMLs = getSupportedSCAPContentsXML();
                xccdfLocation = securityManagerXCCDFDir + File.separator + "nonwindows";
                platform = "nonwindows";
            } else if(null != type && "usgcb".equals(type)) {
                scapTypes = getSupportedUSGCBContents();
                scapXMLs = getSupportedUSGCBContentsXML();
                xccdfLocation = securityManagerXCCDFDir + File.separator + "windows";
                platform = "windows";
            } else if(null != type && "custom".equals(type)) {
                scapTypes = getSupportedCustomContents();
                scapXMLs = getSupportedCustomContentsXML();
                xccdfLocation = securityManagerXCCDFDir + File.separator + "custom";
            }

            if (null != context && null != scapTypes && scapTypes.length > 0) {
                for (int i=0; i < scapTypes.length; i++) {
                    String scapType = scapTypes[i];
                    try {
                        if("xccdf".equals(getXMLContentType(scapXMLs[i]))) {
                            scapType = "xccdf";
                            String currentXccdfLocation = "";
                            if(null != type && "custom".equals(type)) {
                                if ((customSecurityWindowsContentsXML != null) && (customSecurityWindowsContentsXML.length > i) && scapXMLs[i].equals(customSecurityWindowsContentsXML[i])) {
                                    currentXccdfLocation = xccdfLocation + File.separator + "windows";
                                    platform = "windows";
                                } else {
                                    if (customSecurityWindowsContentsXML == null) {
                                        if ((customSecurityNonWindowsContentsXML.length > i) && scapXMLs[i].equals(customSecurityNonWindowsContentsXML[i])) {
                                            currentXccdfLocation = xccdfLocation + File.separator + "nonwindows";
                                        }
                                    } else {
                                        if ((customSecurityNonWindowsContentsXML.length > (i - customSecurityWindowsContentsXML.length)) && scapXMLs[i].equals(customSecurityNonWindowsContentsXML[i - customSecurityWindowsContentsXML.length])) {
                                            currentXccdfLocation = xccdfLocation + File.separator + "nonwindows";
                                        }
                                    }
                                    platform = "nonwindows";
                                }
                            } else {
                                currentXccdfLocation = xccdfLocation;
                            }
                            debug("readStandardSCAPContents(), loading scap content for type - " + scapType + " from " + currentXccdfLocation + File.separator + scapXMLs[i]);
                            HashMap<String, Object> xccdfInfo = xccdfHandler.getXccdfInfo(currentXccdfLocation + File.separator + scapXMLs[i], true);
                            debug("readStandardSCAPContents(), xccdfInfo - " + xccdfInfo);

                            String id = (String) xccdfInfo.get("xml.id");
                            String fileName = new File((String) xccdfInfo.get("xml")).getName();
                            String title = (String) xccdfInfo.get("xml.title");
                            String description = (String) xccdfInfo.get("xml.description");
                            //					String platform = props.getProperty(scapType+ ".platform");
                            String countProfileStr = "" + (Integer) xccdfInfo.get("xml.xccdf.profiles.info.count");

                            debug("readStandardSCAPContents(), SCAP Content Id :" + id);
                            debug("readStandardSCAPContents(), SCAP Content File Name :" + fileName);
                            debug("readStandardSCAPContents(), SCAP Content title :" + title);
                            debug("readStandardSCAPContents(), SCAP Content description :" + description);
                            debug("readStandardSCAPContents(), SCAP Content Platform :" + platform);
                            debug("readStandardSCAPContents(), SCAP Content Profile count :" + countProfileStr);
                            debug("readStandardSCAPContents(), SCAP Content XCCDF File location : " + currentXccdfLocation);

                            SCAPContentDetails scapContents = new SCAPContentDetails();
                            scapContents.setId(id);
                            scapContents.setScapType(scapType);
                            scapContents.setFileName(fileName);
                            scapContents.setTitle(title);
                            scapContents.setPlatform(platform);
                            scapContents.setDescription(description);
                            scapContents.setXccdfFileLocation(currentXccdfLocation);
                            // show SCAP content in dropdown in UI
                            scapContentsMap.put(fileName, title);
                            int countProfiles = 0;
                            try {
                                if (null != countProfileStr) {
                                    countProfiles = Integer.parseInt(countProfileStr);
                                }
                            } catch(Exception ex) {
                                //
                            }
                            scapContents.setCountProfile(countProfiles);

                            if (countProfiles > 0) {
                                ArrayList<XCCDFProfile> xccdfProfiles = (ArrayList<XCCDFProfile>) xccdfInfo.get("xml.xccdf.profiles.info");
                                java.util.List<SCAPProfileDetails> listProfileDetails = new ArrayList<SCAPProfileDetails>();
                                Map<String, String> scapProfilesMap = new HashMap<String, String>();
                                for (XCCDFProfile xccdfProfile : xccdfProfiles) {
                                    String profileId = xccdfProfile.getId();
                                    String profileTitle = xccdfProfile.getTitle();
                                    String profileDescription = xccdfProfile.getDescription();

                                    debug("readStandardSCAPContents(), SCAP Profile Id :" + profileId);
                                    debug("readStandardSCAPContents(), SCAP Profile Title :" + profileTitle);
                                    debug("readStandardSCAPContents(), SCAP Profile Description :" + profileDescription);
                                    if (null != profileId && null != profileTitle && null != profileDescription) {
                                        SCAPProfileDetails profileDetails = new SCAPProfileDetails();
                                        profileDetails.setId(profileId);
                                        profileDetails.setTitle(profileTitle);
                                        profileDetails.setDescription(profileDescription);
                                        listProfileDetails.add(profileDetails);

                                        // show scap profile details map
                                        scapProfilesMap.put(profileId , profileTitle);
                                    }
                                }
                                if (listProfileDetails.size() > 0) {
                                    context.setAttribute(fileName, scapProfilesMap);
                                    scapContents.setProfileDetails(listProfileDetails);
                                } else {
                                    scapContents.setProfileDetails(null);
                                }
                                scapDetails.add(scapContents);
                            }

                            Integer groupsCount = (Integer) xccdfInfo.get("xml.xccdf.groups.info.count");
                            if (groupsCount > 0) {
                                scapContents.setCountGroup(countProfiles);
                                ArrayList<XCCDFGroup> xccdfGroups = (ArrayList<XCCDFGroup>) xccdfInfo.get("xml.xccdf.groups.info");
                                scapContents.setGroups(xccdfGroups);
                            }
                            xccdfInfo.clear();
                            xccdfInfo = null;
                        } else if("oval".equals(getXMLContentType(scapXMLs[i]))) {
                            // oval definition
                            scapType = "oval";
                            String[] ovalTypes = null;
                            String[] ovalXMLs = null;
                            String ovalLocation = null;

                            if (null == type || "scap".equals(type)) {
                                ovalTypes = getSupportedSCAPContents();
                                ovalXMLs = getSupportedSCAPContentsXML();
                                ovalLocation = securityManagerOVALDir + File.separator + "nonwindows";
                                platform = "nonwindows";
                            } else if(null != type && "usgcb".equals(type)) {
                                ovalTypes = getSupportedUSGCBContents();
                                ovalXMLs = getSupportedUSGCBContentsXML();
                                ovalLocation = securityManagerOVALDir + File.separator + "windows";
                                platform = "windows";
                            } else if(null != type && "custom".equals(type)) {
                                ovalTypes = getSupportedCustomContents();
                                ovalXMLs = getSupportedCustomContentsXML();
                                ovalLocation = securityManagerOVALDir + File.separator + "custom";
                            }

                            String ovalType = ovalTypes[i];
                            String currentOVALLocation = "";
                            if(null != type && "custom".equals(type)) {
                                if ((customSecurityWindowsContentsXML != null) && (customSecurityWindowsContentsXML.length > i) && scapXMLs[i].equals(customSecurityWindowsContentsXML[i])) {
                                    currentOVALLocation = ovalLocation + File.separator + "windows";
                                    platform = "windows";
                                } else {
                                    if (customSecurityWindowsContentsXML == null) {
                                        if ((customSecurityNonWindowsContentsXML.length > i) && scapXMLs[i].equals(customSecurityNonWindowsContentsXML[i])) {
                                            currentOVALLocation = ovalLocation + File.separator + "nonwindows";
                                        }
                                    } else {
                                        if ((customSecurityNonWindowsContentsXML.length > (i - customSecurityWindowsContentsXML.length)) && scapXMLs[i].equals(customSecurityNonWindowsContentsXML[i - customSecurityWindowsContentsXML.length])) {
                                            currentOVALLocation = ovalLocation + File.separator + "nonwindows";
                                        }
                                    }
                                    platform = "nonwindows";
                                }
                            } else {
                                currentOVALLocation = ovalLocation;
                            }
                            debug("readStandardSCAPContents(), loading scap oval content for type - " + ovalType + " from " + currentOVALLocation	+ File.separator + scapXMLs[i]);
                            HashMap<String, Object> ovalInfo = ovalHandler.getOvalInfo(currentOVALLocation + File.separator + scapXMLs[i], true);
                            debug("readStandardSCAPContents(), ovalInfo - " + ovalInfo);
                            String id = (String) ovalInfo.get("xml.id");
                            String fileName = new File((String) ovalInfo.get("xml")).getName();
                            String title = (String) ovalInfo.get("xml.title");
                            String description = (String) ovalInfo.get("xml.description");
                            // String platform = props.getProperty(scapType+
                            // ".platform");
                            String countProfileStr = "" + (Integer) ovalInfo.get("xml.oval.profiles.info.count");
                            SCAPContentDetails scapContents = new SCAPContentDetails();
                            scapContents.setId(id);
                            scapContents.setScapType(ovalType);
                            scapContents.setFileName(fileName);
                            scapContents.setTitle(title);
                            scapContents.setPlatform(platform);
                            scapContents.setDescription(description);
                            scapContents.setXccdfFileLocation(currentOVALLocation);
                            // show SCAP content in dropdown in UI
                            scapContentsMap.put(fileName, title);

                            int countProfiles = 0;
                            try {
                                if (null != countProfileStr) {
                                    countProfiles = Integer.parseInt(countProfileStr);
                                }
                            } catch (Exception ex) {
                                //
                            }
                            scapContents.setCountProfile(countProfiles);

                            OVALProfile ovalProfile = (OVALProfile) ovalInfo.get("xml.oval.profiles.info");
                            java.util.List<SCAPProfileDetails> listProfileDetails = new ArrayList<SCAPProfileDetails>();
                            Map<String, String> scapProfilesMap = new HashMap<String, String>();
                            String profileId = ovalProfile.getId();
                            String profileTitle = ovalProfile.getTitle();
                            String profileDescription = ovalProfile.getDescription();
                            ArrayList<OVALProfileDefinition> ovalProfileDefinition = ovalProfile.getDefinitions();
                            //contentProfileDefinitionsMap.put(id + ":_:" + profileId, ovalProfileDefinition);
                            contentProfileDefinitionsMapPersist.put(id + ":_:" + profileId, ovalProfileDefinition);

                            debug("readStandardSCAPContents(), SCAP OVAL Profile Id :" + profileId);
                            debug("readStandardSCAPContents(), SCAP OVAL Profile Title :" + profileTitle);
                            debug("readStandardSCAPContents(), SCAP OVAL Profile Description :"	+ profileDescription);
                            if (null != profileId && null != profileTitle && null != profileDescription) {
                                SCAPProfileDetails profileDetails = new SCAPProfileDetails();
                                profileDetails.setId(profileId);
                                profileDetails.setTitle(profileTitle);
                                profileDetails.setDescription(profileDescription);
                                profileDetails.setDefinitions(ovalProfileDefinition);
                                listProfileDetails.add(profileDetails);

                                // show scap profile details map
                                scapProfilesMap.put(profileId, profileTitle);
                            }
                            if (listProfileDetails.size() > 0) {
                                context.setAttribute(fileName, scapProfilesMap);
                                scapContents.setProfileDetails(listProfileDetails);
                            } else {
                                scapContents.setProfileDetails(null);
                            }

                            //add oval details...
                            String ovalProductVersion = (ovalInfo.get("xml.oval.product_version") != null) ? ((String) ovalInfo.get("xml.oval.product_version")).trim() : "";
                            String ovalProductName = (ovalInfo.get("xml.oval.product_name") != null) ? ((String) ovalInfo.get("xml.oval.product_name")).trim() : "";
                            String ovalModuleName = (ovalInfo.get("xml.oval.module_name") != null) ? ((String) ovalInfo.get("xml.oval.module_name")).trim() : "";
                            String ovalTimestamp = (ovalInfo.get("xml.oval.timestamp") != null) ? ((String) ovalInfo.get("xml.oval.timestamp")).trim() : "";
                            String ovalGeneratorSchemaVersion = (ovalInfo.get("xml.oval.generator.schema_version") != null) ? ((String) ovalInfo.get("xml.oval.generator.schema_version")).trim() : "";
                            String ovalGeneratorProductVersion = (ovalInfo.get("xml.oval.generator.product_version") != null) ? ((String) ovalInfo.get("xml.oval.generator.product_version")).trim() : "";
                            String ovalGeneratorProductName = (ovalInfo.get("xml.oval.generator.product_name") != null) ? ((String) ovalInfo.get("xml.oval.generator.product_name")).trim() : "";
                            String ovalGeneratorTimestamp = (ovalInfo.get("xml.oval.generator.timestamp") != null) ? ((String) ovalInfo.get("xml.oval.generator.timestamp")).trim() : "";
                            String ovalGeneratorContentVersion = (ovalInfo.get("xml.oval.generator.content_version") != null) ? ((String) ovalInfo.get("xml.oval.generator.content_version")).trim() : "";
                            int ovalTotalDefinitions = (ovalInfo.get("xml.oval.profiles.definitions.count") != null) ? ((Integer) ovalInfo.get("xml.oval.profiles.definitions.count")) : -1;
                            int ovalTotalComplianceDefinitions = (ovalInfo.get("xml.oval.profiles.definitions.compliance.count") != null) ? ((Integer) ovalInfo.get("xml.oval.profiles.definitions.compliance.count")) : -1;
                            int ovalTotalInventoryDefinitions = (ovalInfo.get("xml.oval.profiles.definitions.inventory.count") != null) ? ((Integer) ovalInfo.get("xml.oval.profiles.definitions.inventory.count")) : -1;
                            int ovalTotalMiscellaneousDefinitions = (ovalInfo.get("xml.oval.profiles.definitions.miscellaneous.count") != null) ? ((Integer) ovalInfo.get("xml.oval.profiles.definitions.miscellaneous.count")) : -1;
                            int ovalTotalPatchDefinitions = (ovalInfo.get("xml.oval.profiles.definitions.patch.count") != null) ? ((Integer) ovalInfo.get("xml.oval.profiles.definitions.patch.count")) : -1;
                            int ovalTotalVulnerabilityDefinitions = (ovalInfo.get("xml.oval.profiles.definitions.vulnerability.count") != null) ? ((Integer) ovalInfo.get("xml.oval.profiles.definitions.vulnerability.count")) : -1;
                            int ovalTotalTests = (ovalInfo.get("xml.oval.generator.tests.count") != null) ? ((Integer) ovalInfo.get("xml.oval.generator.tests.count")) : -1;
                            int ovalTotalObjects = (ovalInfo.get("xml.oval.generator.objects.count") != null) ? ((Integer) ovalInfo.get("xml.oval.generator.objects.count")) : -1;
                            int ovalTotalStates = (ovalInfo.get("xml.oval.generator.states.count") != null) ? ((Integer) ovalInfo.get("xml.oval.generator.states.count")) : -1;
                            int ovalTotalVariables = (ovalInfo.get("xml.oval.generator.variables.count") != null) ? ((Integer) ovalInfo.get("xml.oval.generator.variables.count")) : -1;
                            scapContents.setOvalProductVersion(ovalProductVersion);
                            scapContents.setOvalProductName(ovalProductName);
                            scapContents.setOvalModuleName(ovalModuleName);
                            scapContents.setOvalTimestamp(ovalTimestamp);
                            scapContents.setOvalGeneratorSchemaVersion(ovalGeneratorSchemaVersion);
                            scapContents.setOvalGeneratorProductName(ovalGeneratorProductName);
                            scapContents.setOvalGeneratorProductVersion(ovalGeneratorProductVersion);
                            scapContents.setOvalGeneratorTimestamp(ovalGeneratorTimestamp);
                            scapContents.setOvalGeneratorContentVersion(ovalGeneratorContentVersion);
                            scapContents.setOvalTotalDefinitions(ovalTotalDefinitions);
                            scapContents.setOvalTotalDefinitions(ovalTotalComplianceDefinitions);
                            scapContents.setOvalTotalDefinitions(ovalTotalInventoryDefinitions);
                            scapContents.setOvalTotalDefinitions(ovalTotalMiscellaneousDefinitions);
                            scapContents.setOvalTotalDefinitions(ovalTotalPatchDefinitions);
                            scapContents.setOvalTotalDefinitions(ovalTotalVulnerabilityDefinitions);
                            scapContents.setOvalTotalTests(ovalTotalTests);
                            scapContents.setOvalTotalObjects(ovalTotalObjects);
                            scapContents.setOvalTotalStates(ovalTotalStates);
                            scapContents.setOvalTotalVariables(ovalTotalVariables);

                            scapDetails.add(scapContents);
                            ovalInfo.clear();
                            ovalInfo = null;
                            // ovalProfile.clear();
                            ovalProfile = null;
                        }
                    } catch (Exception ex) {
                        debug("Error while loading contents from " + scapXMLs[i] + ". Message : " + ex.getMessage());
                        continue;
                    }
                }
            }
        } catch(Exception ed) {
            ed.printStackTrace();
        }
        if (null != type && "usgcb".equals(type)) {
            if(scapDetails.size() > 0) {
                context.setAttribute("usgcbcontentdetails", scapDetails);
                context.setAttribute("usgcbcontentdetailsmap", scapContentsMap);
                return scapDetails;
            } else {
                if (null != context.getAttribute("usgcbcontentdetails")) {
                    context.removeAttribute("usgcbcontentdetails");
                }
                return null;
            }
        } else if (null != type && "scap".equals(type)) {
            if (scapDetails.size() > 0) {
                context.setAttribute("scapcontentdetails", scapDetails);
                context.setAttribute("scapcontentdetailsmap", scapContentsMap);
                return scapDetails;
            } else {
                if (null != context.getAttribute("scapcontentdetails")) {
                    context.removeAttribute("scapcontentdetails");
                }
                return null;
            }
        } else if (null != type && "custom".equals(type)) {
            if (scapDetails.size() > 0) {
                context.setAttribute("customcontentdetails", scapDetails);
                context.setAttribute("customcontentdetailsmap", scapContentsMap);
                return scapDetails;
            } else {
                if (null != context.getAttribute("customcontentdetails")) {
                    context.removeAttribute("customcontentdetails");
                }
                return null;
            }
        } else {
            return null;
        }
    }

    public String getSessionMapValue(HttpSession session, String inputKey, String sessionName) {
        Map<String, String> scapContent = (Map<String, String>) session.getAttribute(sessionName);
        if(null != scapContent) {
            return scapContent.get(inputKey);
        }
        return null;
    }

    public HashMap<String, String> getProfilesForScapContent(String contentName) {
        HashMap<String, String> scapContent = new HashMap<String, String>();
        if (context != null) {
            scapContent = (HashMap<String, String>) context.getAttribute(contentName);
            if(null == scapContent) {
                scapContent = new HashMap<String, String>();
            }
        }
        return scapContent;
    }

    public void deleteProfileForScapContent(String contentName, String profileName) {
        HashMap<String, String> scapContent = new HashMap<String, String>();
        if (context != null) {
            scapContent = (HashMap<String, String>) context.getAttribute(contentName);
            if(null != scapContent) {
                scapContent.remove(profileName);
            }
        }
    }

    public SCAPContentDetails getScapContentDetails(String contentMapName, String contentFileName) {
        java.util.List<SCAPContentDetails> scapDetails = (ArrayList<SCAPContentDetails>)(context.getAttribute(contentMapName));
        for (SCAPContentDetails scapDetail : scapDetails) {
            if (scapDetail.getFileName().equals(contentFileName)) {
                return scapDetail;
            }
        }
        return null;
    }

    public String getScapContentId(HttpSession session, String contentMapName, String contentFileName) {
        java.util.List<SCAPContentDetails> scapDetails = (ArrayList<SCAPContentDetails>)(context.getAttribute(contentMapName));
        for (SCAPContentDetails scapDetail : scapDetails) {
            if (scapDetail.getFileName().equals(contentFileName)) {
                return scapDetail.getId();
            }
        }
        return null;
    }

    public String getScapContentPlatform(HttpSession session, String contentMapName, String contentFileName) {
        java.util.List<SCAPContentDetails> scapDetails = (ArrayList<SCAPContentDetails>)(context.getAttribute(contentMapName));
        for (SCAPContentDetails scapDetail : scapDetails) {
            if (scapDetail.getFileName().equals(contentFileName)) {
                return scapDetail.getPlatform();
            }
        }
        return null;
    }

    public String getScapContentIdFromTitle(HttpSession session, String contentMapName, String title) {
        java.util.List<SCAPContentDetails> scapDetails = (ArrayList<SCAPContentDetails>)(context.getAttribute(contentMapName));
        for (SCAPContentDetails scapDetail : scapDetails) {
            if (scapDetail.getTitle().equals(title)) {
                return scapDetail.getId();
            }
        }
        return null;
    }
    public String[] getSupportedSCAPContents() {
        return supportedScapContents;
    }

    public String[] getSupportedSCAPContentsXML() {
        return supportedScapContentsXML;
    }

    public String[] getSupportedUSGCBContents() {
        return supportedUsgcbContents;
    }

    public String[] getSupportedUSGCBContentsXML() {
        return supportedUsgcbContentsXML;
    }

    public String[] getSupportedCustomContents() {
        return customSecurityContents;
    }

    public String[] getSupportedCustomContentsXML() {
        return customSecurityContentsXML;
    }

    public String[] getSupportedCustomWindowsContents() {
        return customSecurityWindowsContents;
    }

    public String[] getSupportedCustomWindowsContentsXML() {
        return customSecurityWindowsContentsXML;
    }

    public String[] getSupportedCustomNonWindowsContents() {
        return customSecurityNonWindowsContents;
    }

    public String[] getSupportedCustomNonWindowsContentsXML() {
        return customSecurityNonWindowsContentsXML;
    }

    public boolean isCustomWindowsContentsXML(String xmlName) {
        if (xmlName == null) {
            return false;
        }
        if (customSecurityWindowsContentsXML != null) {
            return (Arrays.asList(customSecurityWindowsContentsXML).contains(xmlName));
        }
        return false;
    }

    public boolean isCustomNonWindowsContentsXML(String xmlName) {
        if (xmlName == null) {
            return false;
        }
        if (customSecurityNonWindowsContentsXML != null) {
            return (Arrays.asList(customSecurityNonWindowsContentsXML).contains(xmlName));
        }
        return false;
    }

    public String getSecurityManagerUrl() {
        return securityManagerUrl;
    }

    public String getSecurityInfoUrl() {
        return securityInfoUrl;
    }

    // Try to get the Security Info channel at the location next to Security Manager channel.
    String getSecurityInfoChannelURL(String securityManagerURL) {
        String securityInfoPath = main.getConfig().getProperty("subscriptionmanager.securityinfo.url");
        if((null != securityInfoPath) && (securityInfoPath.trim().length() > 0) && securityInfoPath.endsWith("/vDef")) {
            return securityInfoPath;
        } else {
            securityInfoPath = cmsTunerConfig.getProperty("marimba.securityinfo.url");
            if((null != securityInfoPath) && (securityInfoPath.trim().length() > 0) && securityInfoPath.endsWith("/vDef")) {
                return securityInfoPath;
            } else {
                securityInfoPath = "";
            }
        }
        if(null != securityManagerURL && securityManagerURL.length() > 0) {
            int tmp_int = securityManagerURL.lastIndexOf('/');
            securityInfoPath = securityManagerURL.substring(0, tmp_int);
        }
        return securityInfoPath + "/vDef";
    }

    public IChannel getSecurityInfoChannelCreated() {
        Long SUBSCRIBE_INTERVAL = 5 * 60L; // Time to wait for Security Info channel to subscribe
        Long START_INTERVAL = 2 * 60L; // Time to wait for Security Info channel to start

        boolean doSubscribe = false;
        if (null == securityInfoChannel) {
            debug("getSecurityInfoChannelCreated(), vDef channel is not in workspace, subscribing now");
            doSubscribe = true;
        } else {
            int channel_status = securityInfoChannel.getChannelStatus();
            if (IChannel.CH_UNSUBSCRIBED == channel_status || IChannel.CH_REMOVED == channel_status) {
                debug("getSecurityInfoChannelCreated(), vDef channel is removed or unsubscribed, subscribing now");
                doSubscribe = true;
            }
        }

        if (doSubscribe) {
            try {
                launcher.subscribe(securityInfoUrl);
            } catch(Exception ed) {
                debug("getSecurityInfoChannelCreated(), Problem on subscribe channel. Check whether channel is available or not : " + securityInfoUrl);
            }

            //Wait for some random time to check the channel created
            debug("getSecurityInfoChannelCreated(), Waiting for vDef channel to subscribe");
            int i = 0;
            while (true) {
                try {
                    Thread.sleep(5 * 1000);
                    securityInfoChannel = getChannel(securityInfoUrl);
                    i += 5;
                    if ((i >= SUBSCRIBE_INTERVAL) || (null != securityInfoChannel)) {
                        break;
                    }
                } catch (Exception ex) {
                    //Ignore the exception
                }
            }
        }

        if (null != securityInfoChannel) {
            IActive active = launcher.start(securityInfoUrl, null, false);
            waitFor(active, START_INTERVAL * 1000);
        }
        return securityInfoChannel;
    }

    /**
     * Wait for the IActive to die.
     */
    private boolean waitFor(IActive active, long timeout) {
        Thread timer = null;
        try {
            if (timeout > 0) {
                timer = new Thread(new Timer(timeout, Thread.currentThread()));
                timer.start();
            }
            while (active.getApplicationStatus() != IActive.APP_DEAD) {
                Thread.sleep(1000);
            }
            if (timeout > 0) {
                timer.interrupt();
            }
        } catch (InterruptedException e) {
            // bad news: we�ve timed out!
            active.kill();
            return false;
        }
        return true;
    }

    class Timer implements Runnable {
        private long to;
        private Thread client;

        public Timer(long to, Thread client) {
            this.to = to;
            this.client = client;
        }

        public void run() {
            try {
                Thread.sleep(to);
            } catch (InterruptedException e) {
                // the client thread will interrupt us when done
                return;
            }
            // the client thread hasn�t awakened yet, so interrupt it.
            client.interrupt();
        }
    }

    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
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

    public IChannel getChannel(String url) {
        IChannel channel = workspace.getChannel(url);
        if (channel == null) {
            channel = workspace.getChannel(URLUTF8Decoder.decode(url));
        }
        return channel;
    }

    public boolean hasSyncedContent() {
        try {
            IChannel securityInfoChannel = getChannel(securityInfoUrl);
            String[] xmlChecksumDetailsArr = securityInfoChannel.getPropertyPairs();
            if (xmlChecksumDetailsArr != null) {
                for (int i = 0; i < xmlChecksumDetailsArr.length; i += 2) {
                    String propName = xmlChecksumDetailsArr[i];
                    String propValue = xmlChecksumDetailsArr[i + 1];
                    if (propName.startsWith("cs.")) {
                        if ((propValue != null) && (propValue.indexOf(";") > -1)) {
                            String exisingChannelChecksum = propValue.split(";")[0];
                            String exisingDbChecksum = propValue.split(";")[1];
                            if (exisingChannelChecksum.equals(exisingDbChecksum)) {
                                debug("hasSyncedContent(), returning true...");
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        debug("hasSyncedContent(), returning false...");
        return false;
    }

    public boolean hasSyncedContentFor(String type, String platform) {
        try {
            IChannel securityInfoChannel = getChannel(securityInfoUrl);
            String[] xmlChecksumDetailsArr = securityInfoChannel.getPropertyPairs();
            if (xmlChecksumDetailsArr != null) {
                for (int i = 0; i < xmlChecksumDetailsArr.length; i += 2) {
                    String propName = xmlChecksumDetailsArr[i];
                    String propValue = xmlChecksumDetailsArr[i + 1];
                    if (propName.startsWith("cs.")) {
                        String xmlPath = propName.substring(3);
                        String xmlPlatform = new File(xmlPath).getParentFile().getName();
                        boolean isCustom = "custom".equals(new File(xmlPath).getParentFile().getParentFile().getName());
                        if ((propValue != null) && (propValue.indexOf(";") > -1)) {
                            String exisingChannelChecksum = propValue.split(";")[0];
                            String exisingDbChecksum = propValue.split(";")[1];
                            if (exisingChannelChecksum.equals(exisingDbChecksum)) {
                                if ("scap".equals(type) && "nonwindows".equals(xmlPlatform) && (!isCustom)) {
                                    debug("hasSyncedContentFor(), returning scap true...");
                                    return true;
                                } else if ("usgcb".equals(type) && "windows".equals(xmlPlatform) && (!isCustom)) {
                                    debug("hasSyncedContentFor(), returning usgcb true...");
                                    return true;
                                } else if ("custom".equals(type) && xmlPlatform.equals(platform) && (isCustom)) {
                                    debug("hasSyncedContentFor(), returning custom true...");
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        debug("hasSyncedContentFor(), returning false...");
        return false;
    }

    public void loadDbData(final SubscriptionMain subscriptionMain) {
        final Thread loadOvalCVEDefinitionDetailsThread = new Thread() {public void run() {
            try {
                loadOvalCVEDefinitionDetails(subscriptionMain);
            } catch (Exception ex) {/**/}
        }};
        final Thread loadOvalDefinitionDetailsThread = new Thread() {public void run() {
            try {
                loadOvalDefinitionDetails(subscriptionMain);
            } catch (Exception ex) {/**/}
        }};
        loadOvalCVEDefinitionDetailsThread.start();
        loadOvalDefinitionDetailsThread.start();
        context.setAttribute("load_dbdata_status", "started");
        new Thread() {
            public void run() {
                while (true) {
                    if (!loadOvalCVEDefinitionDetailsThread.isAlive() && !loadOvalDefinitionDetailsThread.isAlive()) {
                        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ==> loadDbData(), loadOvalDefinitionDetailsThread & loadOvalDefinitionDetailsThread completed...");

                        if ((ovalDefinitionDetails != null) && (ovalCVEDefinitionDetails != null)) {
                            Map tmp = new HashMap(ovalDefinitionDetails);
                            tmp.keySet().removeAll(ovalCVEDefinitionDetails.keySet());
                            ovalCVEDefinitionDetails.putAll(tmp);
                        }

                        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ==> loadDbData(), loadOvalDefinitionDetailsThread & loadOvalDefinitionDetailsThread results merged...");
                        context.setAttribute("load_dbdata_status", "done");
                        break;
                    }
                    try {
                        Thread.sleep(5000L); // Check status each after 5 SECONDS
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }}.start();
    }

    public void loadOvalCVEDefinitionDetails(SubscriptionMain subscriptionMain) {
        long startTime = System.currentTimeMillis();
        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ========> loadOvalCVEDefinitionDetails(), started at - " + new Date(startTime));
        String query = "SELECT content_title, content_id, profile_id, profile_title, " +
                "inv_sec_oval_defn_cve_details.reference_name, definition_name,\n" +
                "definition_title, definition_desc, definition_class, reference_url, cvss_version, cvss_score, cvss_base_score, " +
                "cvss_impact_score, cvss_exploit_score, severity,definition_severity, repository_id \n" +
                "FROM inv_sec_oval_defn_cve_details where reference_name not like 'cpe%'";
        DownloadQueryRunner runner = new DownloadQueryRunner(subscriptionMain, query, true);
        ovalCVEDefinitionDetails = runner.getOvalDetailBeanList();
        ovalCVEDefinitionDetailsPersist = ovalDB.hashMap("ovalCVEDefinitionDetails", Serializer.STRING, Serializer.JAVA).createOrOpen();
        for (String key : ovalCVEDefinitionDetails.keySet()) {
            ovalCVEDefinitionDetailsPersist.put(key, ovalCVEDefinitionDetails.get(key));
        }
        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ==========> loadOvalCVEDefinitionDetails(), query - " + query);
        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ==> loadOvalCVEDefinitionDetails(), ovalCVEDefinitionDetails - " + ovalCVEDefinitionDetailsPersist.size());
        ovalCVEDefinitionDetails.clear();
        ovalDB.commit();
        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ==========> loadOvalCVEDefinitionDetails(), completed at - " + new Date() + ". Time taken: " + getElapsedTime(startTime));
    }


    public void loadOvalDefinitionDetails(SubscriptionMain subscriptionMain) {
        long startTime = System.currentTimeMillis();
        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] =======================> loadOvalDefinitionDetails(), started at - " + new Date(startTime));
        String query = "select * from inv_security_oval_defn_details where reference_name not like 'cpe%' ";
        DownloadQueryRunner runner = new DownloadQueryRunner(subscriptionMain, query, false);
        ovalDefinitionDetails = runner.getOvalDetailBeanList();
        debug("loadOvalDefinitionDetails(), ovalDefinitionDetails - " + ovalDefinitionDetails.size());
        ovalDefinitionDetailsPersist = ovalDB.hashMap("ovalDefinitionDetails", Serializer.STRING, Serializer.JAVA).createOrOpen();
        for (String key : ovalDefinitionDetails.keySet()) {
            ovalDefinitionDetailsPersist.put(key, ovalDefinitionDetails.get(key));
        }
        ovalDefinitionDetails.clear();
        ovalDB.commit();
        System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ===============> loadOvalDefinitionDetails(), completed at - " + new Date() + ". Time taken: " + getElapsedTime(startTime));
    }


    class OSInfoQueryRunner extends DatabaseAccess {
        List<String>  osInfoBeanList;

        public OSInfoQueryRunner(SubscriptionMain subscriptionMain, String query) {
            try {
                GetOSDetails getOSDetails = new GetOSDetails(subscriptionMain, query);
                runQuery(getOSDetails);
                osInfoBeanList = getOSDetails.getOsInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public List<String> getOsInfoBeanList() {
            return osInfoBeanList;
        }
    }

    class DownloadQueryRunner extends DatabaseAccess {

        Map<String, List<OvalDetailBean>>  ovalDetailBeanList;
        public DownloadQueryRunner(SubscriptionMain subscriptionMain, String query, boolean includeCveInfo) {
            try {
                GetRuleDetails getRuleDetails = new GetRuleDetails(subscriptionMain, query, includeCveInfo);
                runQuery(getRuleDetails);
                ovalDetailBeanList = getRuleDetails.getRuleDefinition();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Map<String, List<OvalDetailBean>> getOvalDetailBeanList() {
            return ovalDetailBeanList;
        }
    }

    class GetOSDetails extends QueryExecutor {
        String query = null;
        List<String> osList = new ArrayList<String>();

        public GetOSDetails(SubscriptionMain subscriptionMain, String query) {
            super(subscriptionMain);
            this.query = query;
        }

        protected void execute(IStatementPool pool) throws SQLException {
            PreparedStatement st = pool.getConnection().prepareStatement(query);
            ResultSet rs = st.executeQuery();
            try {
                while (rs.next()) {
                    String osName = rs.getString("OS_Name");
                    osList.add(osName);
                }
            } catch (SQLException sex) {
                sex.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                rs.close();
                st.close();
            }
        }

        public List<String> getOsInfo() {
            return osList;
        }
    }

    class GetRuleDetails extends QueryExecutor {
        String query = null;
        String contentIds;
        boolean includeCveInfo;
        public GetRuleDetails(SubscriptionMain subscriptionMain, String query, boolean includeCveInfo) {
            super(subscriptionMain);
            this.query = query;
            this.contentIds = contentIds;
            this.includeCveInfo = includeCveInfo;
        }
        Map<String, List<OvalDetailBean>> map = new HashMap<String, List<OvalDetailBean>>();
        Map<String, List<OvalDetailBean>> mapPersist = null;

        protected void execute(IStatementPool pool) throws SQLException {
            mapPersist = ovalDB.hashMap("GetRuleDetails.map", Serializer.STRING, Serializer.JAVA).createOrOpen();
            Properties cveInfoProps = main.getCVEInfoProps();
            PreparedStatement st = pool.getConnection().prepareStatement(query);
            ResultSet rs = st.executeQuery();
            try {
                while (rs.next()) {
                    String contentTitle = rs.getString("content_title");
                    String contentId = rs.getString("content_id");
                    String profileId = rs.getString("profile_id");
                    String profileTitle = rs.getString("profile_title");
                    String referenceName = rs.getString("reference_name");
                    String definitionName = rs.getString("definition_name");
                    String definitionTitle = rs.getString("definition_title");
                    String definitionDesc = rs.getString("definition_desc");
                    String definitionClass = rs.getString("definition_class");
                    String referenceURL = rs.getString("reference_url");
                    String cveName = null;
                    String cvssVersion = null;
                    String cvssScore = null;
                    String cvssBaseScore = null;
                    String cvssImpactScore = null;
                    String cvssExploitScore = null;
                    String cveSeverity = null;
                    String definitionSeverity = null;
                    String solution = null;

                    if (includeCveInfo) {
                        cveName = rs.getString("reference_name");
                        cvssVersion = rs.getString("cvss_version");
                        cvssScore = rs.getString("cvss_score");
                        cvssBaseScore = rs.getString("cvss_base_score");
                        cvssImpactScore = rs.getString("cvss_impact_score");
                        cvssExploitScore = rs.getString("cvss_exploit_score");
                        cveSeverity = rs.getString("severity");
                        solution = rs.getString("repository_id");
                    }
                    definitionSeverity = rs.getString("definition_severity");

                    String severity = getSeverity(cvssScore, cveSeverity, definitionSeverity, cveInfoProps, cveName);

                    solution = includeCveInfo ? getCveInfo(cveName, solution, CVEInfoUtil.CVE_INFO_SOLUTION) : solution;
                    if ((solution == null) || (solution.trim().length() < 1)) {
                        solution = "None Reported";
                    }

                    referenceURL = includeCveInfo ? getCveInfo(cveName, referenceURL, CVEInfoUtil.CVE_INFO_REFERENCE) : referenceURL;
                    if ((referenceURL == null) || (referenceURL.trim().length() < 1)) {
                        referenceURL = "None Reported";
                    }

                    OvalDetailBean ovalDetailBean = new OvalDetailBean(contentTitle, profileTitle, referenceName,
                            cvssVersion, cvssScore, cvssBaseScore, cvssImpactScore, cvssExploitScore, severity,
                            definitionName, definitionTitle, definitionDesc, definitionClass, referenceURL, contentId, solution);
                    String key = contentId +":_:" + profileId + ":_:" + definitionName;
                    List<OvalDetailBean> list = null;
                    if (map.get(key) != null) {
                        list = map.get(key);
                    } else {
                        list = new ArrayList<OvalDetailBean>();
                    }
                    list.add(ovalDetailBean);
                    map.put(key, list);
                }
                for (String key : map.keySet()) {
                    mapPersist.put(key, map.get(key));
                }
                map.clear();
                ovalDB.commit();
            } catch (SQLException sex) {
                sex.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                rs.close();
                st.close();
            }
        }
        public Map<String, List<OvalDetailBean>> getRuleDefinition() {
            return mapPersist;
        }
    }

    public Map<String, List<OvalDetailBean>> getOvalCVEDefinitionDetails() {
        return ovalCVEDefinitionDetailsPersist;
    }

    public Map<String, List<OvalDetailBean>> getOvalDefinitionDetails() {
        return ovalDefinitionDetailsPersist;
    }

    public HashMap<String, String> getAllXmlContentSyncStatus() {
        HashMap<String, String> status = new HashMap<String, String>();
        try {
            IChannel securityInfoChannel = getChannel(securityInfoUrl);
            String[] xmlChecksumDetailsArr = securityInfoChannel.getPropertyPairs();
            if (xmlChecksumDetailsArr != null) {
                for (int i = 0; i < xmlChecksumDetailsArr.length; i += 2) {
                    String propName = xmlChecksumDetailsArr[i];
                    String propValue = xmlChecksumDetailsArr[i + 1];
                    if (propName.startsWith("cs.")) {
                        if ((propValue != null) && (propValue.indexOf(";") > -1)) {
                            status.put(new File(propName.substring(3)).getName(), propValue);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        debug("getAllXmlContentSyncStatus(), status - " + status);
        return status;
    }

    public boolean isInSync(String xmlFileName) {
        boolean result = false;
        try {
            HashMap<String, String> status = getAllXmlContentSyncStatus();
            if ((status != null) && (status.size() > 0)) {
                String xmlPropValue = status.get(xmlFileName);
                if ((xmlPropValue != null) && (xmlPropValue.indexOf(";") > -1)) {
                    String exisingChannelChecksum = xmlPropValue.split(";")[0];
                    String exisingDbChecksum = xmlPropValue.split(";")[1];
                    result = exisingChannelChecksum.equals(exisingDbChecksum);
                }
            } else {
                result = false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        debug("isInSync(), xmlFileName - " + xmlFileName + " ,result - " + result);
        return result;
    }

    public boolean isSyncedOnce(String xmlFileName) {
        boolean result = false;
        try {
            HashMap<String, String> status = getAllXmlContentSyncStatus();
            if ((status != null) && (status.size() > 0)) {
                String xmlPropValue = status.get(xmlFileName);
                if ((xmlPropValue != null) && (xmlPropValue.indexOf(";") > -1)) {
                    String exisingChannelChecksum = xmlPropValue.split(";")[0];
                    String exisingDbChecksum = xmlPropValue.split(";")[1];
                    result = (exisingChannelChecksum != null) && (!"empty".equals(exisingChannelChecksum))
                            && (exisingDbChecksum != null) && (!"empty".equals(exisingDbChecksum));
                }
            } else {
                result = false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        debug("isSyncedOnce(), xmlFileName - " + xmlFileName + " ,result - " + result);
        return result;
    }
    private void loadContentTypeMetadata(String PropertiesPath) {
        String contentType = "xccdf"; // default
        InputStream inStream = null;
        try {
            if(null != PropertiesPath) {
                Properties prop = new Properties();
                inStream = new FileInputStream(new File(PropertiesPath + File.separator + "metadata.properties"));
                prop.load(inStream);

                for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                    String requiredKey = (String) entry.getKey();
                    String fileName = (String) entry.getValue();
                    if(requiredKey.startsWith("oval")) {
                        contentType = "oval";
                    } else {
                        contentType = "xccdf";
                    }
                    xmlContentTypeMap.put(fileName, contentType);
                }
                xmlContentTypeMapPersist = ovalDB.hashMap("xmlContentTypeMap", Serializer.STRING, Serializer.STRING).createOrOpen();
                contentProfileDefinitionsMapPersist = ovalDB.hashMap("contentProfileDefinitionsMap", Serializer.STRING, Serializer.JAVA).createOrOpen();
                for (String key : xmlContentTypeMap.keySet()) {
                    xmlContentTypeMapPersist.put(key, xmlContentTypeMap.get(key));
                }
                xmlContentTypeMap.clear();
            }
        } catch(Exception ed) {

        } finally {
            if(null != inStream) {
                try {
                    inStream.close();
                } catch(Exception e){}
            }
        }
    }

    public String getXMLContentType(String fileName) {
        String contentType = "xccdf"; // default
        if(null != xmlContentTypeMapPersist && xmlContentTypeMapPersist.size() > 0) {
            String getContentType = xmlContentTypeMapPersist.get(fileName);
            if(null != getContentType) contentType = getContentType;
        }
        return contentType;
    }

    public ArrayList<OVALProfileDefinition> getDefinitions(String contentName, String profileName) {
        if (contentProfileDefinitionsMap == null) return null;

        return contentProfileDefinitionsMapPersist.get(contentName + ":_:" + profileName);
    }

    public OVALProfileDefinition getDefinition(String contentName, String profileName, String definitionId) {
        if (contentProfileDefinitionsMap == null) return null;

        ArrayList<OVALProfileDefinition> allDefinitions = getDefinitions(contentName, profileName);

        if (allDefinitions == null) return null;

        OVALProfileDefinition requiredOVALProfileDefinition = null;
        for (OVALProfileDefinition ovalProfileDefinition : allDefinitions) {
            if (ovalProfileDefinition.getId().equals(definitionId)) {
                requiredOVALProfileDefinition = ovalProfileDefinition;
                break;
            }
        }

        return requiredOVALProfileDefinition;
    }

    public String getAssessmentType(String fileName) {
        String xmlContentType = getXMLContentType(fileName);
        String assessmentType  = "Configuration";
        if(null != xmlContentType && "oval".equalsIgnoreCase(xmlContentType)) {
            if (fileName.startsWith("oval.vulnerability")) {
                assessmentType = "Vulnerability";
            } else if (fileName.startsWith("oval.patch")) {
                assessmentType = "Patch";
            } else if (fileName.startsWith("oval.compliance")) {
                assessmentType = "Compliance";
            } else if (fileName.startsWith("oval.inventory")) {
                assessmentType = "Inventory";
            } else if (fileName.startsWith("oval.miscellaneous")) {
                assessmentType = "Miscellaneous";
            } else {
                assessmentType = "Vulnerability";
            }
        }
        return assessmentType;
    }

    public static String getElapsedTime(Long startTime) {
        return getElapsedTime (startTime, System.currentTimeMillis());
    }

    public static String getElapsedTime(Long startTime, Long endTime) {
        if (endTime == -1) endTime = System.currentTimeMillis();

        long milliseconds = endTime - startTime;
        int hours   = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60)) / 1000;
        return String.format("%02d h : %02d m : %02d s", hours, minutes, seconds) + " (" + milliseconds + " ms)";
    }

    public void destory() {
        if (null != ovalCVEDefinitionDetails) ovalCVEDefinitionDetails.clear();
        if (null != ovalDefinitionDetails) ovalDefinitionDetails.clear();
    }

    private String getSeverity(String cvssScore, String cveSeverity, String definitionSeverity, Properties cveInfoProps, String cveName) {
        debug("getSeverity(), cvssScore - " + cvssScore + ", cveSeverity - " + cveSeverity + ", definitionSeverity - " + definitionSeverity + ", cveName - " + cveName);

        String finalSeverity = "";

        try {
            // 1. if we can get the severity from cvss_score, use it...
            if ((cvssScore != null) && (cvssScore.trim().length() > 0)) {
                try {
                    float cvssScoreFloat = Float.parseFloat(cvssScore);
                    if (cvssScoreFloat < 4.0) {
                        finalSeverity = "Low";
                    } else if (cvssScoreFloat < 7.0) {
                        finalSeverity = "Medium";
                    } else if (cvssScoreFloat < 9.0) {
                        finalSeverity = "High";
                    } else {
                        finalSeverity = "Critical";
                    }
                } catch (Throwable t) {
                    finalSeverity = "";
                }
            }

            if ((finalSeverity == null) || (finalSeverity.trim().length() < 1)) {
                // 2. if we can't get the severity from cvss_score table, but can get the severity from cve_info table, use it...
                if ((cveSeverity != null) && (cveSeverity.trim().length() > 0)) {
                    finalSeverity = cveSeverity;
                }
            }

            if ((finalSeverity == null) || (finalSeverity.trim().length() < 1)) {
                // 3. if we can't get the severity from both cvss_score and cve_info table, but can find it in the cve map file use it...
                if (cveInfoProps != null) {
                    if ((cveName != null) && cveInfoProps.containsKey(cveName)) {
                        finalSeverity = (String) cveInfoProps.get(cveName);
                        if ((finalSeverity != null) && (finalSeverity.indexOf(",") > -1)) {
                            finalSeverity = finalSeverity.substring(0, finalSeverity.indexOf(","));
                        } else {
                            finalSeverity = null;
                        }
                    }
                }
            }

            if ((finalSeverity == null) || (finalSeverity.trim().length() < 1)) {
                // 4. if we can't get the severity from cvss_score, cve_info table as well as from the cve map file, we will use the severity of the definition instead...
                if ((definitionSeverity != null) && (definitionSeverity.trim().length() > 0)) {
                    finalSeverity = definitionSeverity;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (finalSeverity == null) {
            finalSeverity = "";
        }

        debug("getSeverity(), finalSeverity - " + finalSeverity);
        return finalSeverity;
    }

    private String getCveInfo(String cveName, String infoFromDbData, int infoType) {
        debug("getCveInfo(), cveName - " + cveName + ", infoFromDbData - " + infoFromDbData + ", infoType - " + infoType);

        String finalCVEInfo = "";
        StringBuilder cveInfoStringBuilder = new StringBuilder();
        int i = 0;
        if ((infoFromDbData != null) && (infoFromDbData.trim().length() > 0)) {
            cveInfoStringBuilder.append(CVEInfoUtil.CVE_INFO_TYPES[infoType] + " " + ++i);
            cveInfoStringBuilder.append(": ");
            cveInfoStringBuilder.append(infoFromDbData);
            cveInfoStringBuilder.append(" ");
        }

        Set<String> cveInfoSet = cveInfoUtil.getInfo(cveName, infoType);
        if (cveInfoSet != null && !cveInfoSet.isEmpty()) {
            for (String cveInfo : cveInfoSet) {
                cveInfoStringBuilder.append(CVEInfoUtil.CVE_INFO_TYPES[infoType] + " " + ++i);
                cveInfoStringBuilder.append(": ");
                if (cveInfo != null && !cveInfo.trim().isEmpty()) {
                    try {
                        String formattedSolution = cveInfo.trim();
                        formattedSolution = formattedSolution.replace("\n", " ").trim();
                        cveInfoStringBuilder.append(formattedSolution);
                    } catch(Exception e) {
                        cveInfoStringBuilder.append(cveInfo);
                    }
                }
                cveInfoStringBuilder.append(" ");
            }

            finalCVEInfo = cveInfoStringBuilder.toString().replace(",",";").replace("\n","");
        } else {
            if (i == 0) {
                finalCVEInfo = "None Reported";
            } else {
                finalCVEInfo = cveInfoStringBuilder.toString();
            }
        }

        if ((finalCVEInfo == null) || (finalCVEInfo.trim().length() < 1)) {
            finalCVEInfo = "None Reported";
        }

        debug("getCveInfo(), finalCVEInfo - " + finalCVEInfo);
        return finalCVEInfo;
    }

    public static void debug(String str) {
        if (DEBUG) {
            System.out.println("SCAPUtils.java :: [" + new Date().toString() + "] ==> " + str);
        }
    }
}
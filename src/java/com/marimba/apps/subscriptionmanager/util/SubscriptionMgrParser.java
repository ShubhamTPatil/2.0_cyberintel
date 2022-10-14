package com.marimba.apps.subscriptionmanager.util;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import static com.marimba.apps.subscription.common.ISubscriptionConstants.*;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.xml.XMLOutputStream;
import com.marimba.tools.xml.XMLClient;
import com.marimba.tools.xml.XMLException;
import com.marimba.str.StrBytes;
import com.marimba.str.Str;
import com.marimba.webapps.tools.util.KnownActionError;

import java.util.Hashtable;
import java.util.Stack;
import java.util.List;
import java.util.Dictionary;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.File;


/**
 * Created by IntelliJ IDEA.
 * User: svasudev
 * Date: Mar 6, 2013
 * Time: 9:15:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionMgrParser implements XMLClient {

    final static int POWERPROFILES=0;
    final static int POWERPROFILE=1;
    static boolean DEBUG = false;
    static Hashtable tags = new Hashtable(5, 1);
    static Hashtable tagNames = new Hashtable(5, 1);
    private boolean parseError = false;
    private boolean tagHandled = false;
    private Stack stack = new Stack();
    XMLOutputStream xos;
    SubscriptionMain main;
    XMLException xmlex;
    final String[] validValues = {"1","2","3","5","10","15","20","25","30","45","60","120","180","240","300","360"};
    static {
        add("POWERPROFILES", POWERPROFILES);
        add(POWERPROFILES, "POWERPROFILES");
        add("POWERPROFILE", POWERPROFILE);
        add(POWERPROFILE, "POWERPROFILE");

    }

    public SubscriptionMgrParser(SubscriptionMain main) {
        this.main = main;
    }

    public SubscriptionMgrParser(SubscriptionMain main, XMLOutputStream xos) {
        this.xos = xos;
        this.main = main;
    }

    static void add(String nm, int tag) {
        tags.put(new StrBytes(nm), new Integer(tag));
    }

    static void add(int tag, String name) {
        tagNames.put(new Integer(tag), name);
    }

    public void nameSpace(Str name, Str href) {

    }

    public int resolveEntity(Str name) {
        return 0;
    }

    public void handleStartTag(int tag, Str name, Hashtable atts, boolean empty) {
        if (parseError) {
            return;
        }

        if (DEBUG) {
            System.out.println((empty ? "saw self-contained tag: "
                    : "saw start tag: ") + name);
            System.out.println(name + " atts: " + atts.toString());
        }

        if (!empty) {
            stack.push(new Integer(tag));
        }
        try {
            switch (tag) {
                case POWERPROFILE:
                    tagHandled = true;
                    handlePowerScheme(atts);
                    break;
            }
        } catch (XMLException xmlex) {
            parseError = true;
            this.xmlex = xmlex;
        }
    }

    public Exception errorOccoured() {
        if (parseError) {
            return xmlex;
        } else if (!tagHandled) {
            return new Exception("Invalid XML file. <POWERPROFILE> tag is not configured in importing xml file.");
        } else {
            return null;
        }
    }


    void handlePowerScheme(Dictionary atts) throws XMLException {
        ConfigProps props = new ConfigProps(new File(main.getDataDirectory(), "PowerSettings.txt"));
        String availableProfiles = props.getProperty(POWER_PROFILE_NAME);
        String profileName = ((Str) atts.get("schemeName")).toString();
        String profiles = "";
        String profileNamePrefix = "";
        if(profileName != null && profileName != "") {
            profileNamePrefix = profileName + ".";


            if (((profileName == null) || (profileName.trim().length() < 1))) {
                throw new XMLException("Invalid profile name " + profileName);
            } else if (DEFAULT_SCHEME_8_0.equals(profileName)) {
                throw new XMLException("Profile name cannot set as default profile name " + profileName);
            } else {
                // Remove multiple space in between two words.
                profileName = profileName.replaceAll("\\s+", " ");

                if (!Pattern.matches("[\\w\\s+]+", profileName)) {
                    throw new XMLException("Not a valid profile name, " + profileName);
                }
            }
            profiles = ((availableProfiles != null) &&
                    ((availableProfiles.startsWith(profileName + ",")) ||
                            (availableProfiles.endsWith("," + profileName)) ||
                            (availableProfiles.equals(profileName)) ||
                            (availableProfiles.indexOf(","+ profileName+ ",") > -1)))
                    ?  availableProfiles : ((availableProfiles == null) ? profileName : availableProfiles + "," + profileName);
            props.setProperty(POWER_PROFILE_NAME, profiles);
        }

        String description = getValue(atts, "description");
        if (description != null && description.trim().length() > 0) {
            props.setProperty(profileNamePrefix + POWER_PROFILE_DESC, description);
        }

        String standbyAC = getValue(atts, "standbyAC");
        if (isValid(standbyAC)) {
            props.setProperty(profileNamePrefix + STANDBY_IDLETIME_PROP, standbyAC);
        } else {
            throw new XMLException("Standby AC timeout is invalid for profile " + profileName+ ". Timeout Value: " + standbyAC);
        }

        String standbyDC = getValue(atts, "standbyDc");
        if (isValid(standbyDC)) {
            props.setProperty(profileNamePrefix + STANDBY_IDLETIME_DC_PROP, standbyDC);
        } else {
            throw new XMLException("Standby DC timeout is invalid for profile " + profileName+ ". Timeout Value: " + standbyDC);
        }

        String hibernateAC = getValue(atts, "hibernateAC");
        if (isValid(hibernateAC)) {
            props.setProperty(profileNamePrefix + HIBER_IDLETIME_PROP, hibernateAC);
        } else {
            throw new XMLException("Hibernate AC timeout is invalid for profile " + profileName + ". Timeout Value: " + hibernateAC);
        }

        String hibernateDC = getValue(atts, "hibernateDC");
        if (isValid(hibernateDC)) {
            props.setProperty(profileNamePrefix + HIBER_IDLETIME_DC_PROP, hibernateDC);
        } else {
            throw new XMLException("Hibernate DC timeout is invalid for profile " + profileName + ". Timeout Value: " + hibernateDC);
        }

        String monitorAC = getValue(atts, "monitorAC");
        if (isValid(monitorAC)) {
            props.setProperty(profileNamePrefix + MONITOR_IDLETIME_PROP, monitorAC);
        } else {
            throw new XMLException("Monitor AC timeout is invalid for profile " + profileName + ". Timeout Value: " + monitorAC);
        }

        String monitorDC = getValue(atts, "monitorDC");
        if (isValid(monitorDC)) {
            props.setProperty(profileNamePrefix + MONITOR_IDLETIME_DC_PROP, monitorDC);
        } else {
            throw new XMLException("Monitor DC timeout is invalid for profile " + profileName + ". Timeout Value: " + monitorDC);
        }

        String hardDiskAC = getValue(atts, "hardDiskAC");
        if (isValid(hardDiskAC)) {
            props.setProperty(profileNamePrefix + DISK_IDLETIME_PROP, hardDiskAC);
        } else {
            throw new XMLException("Hard disk AC timeout is invalid for profile " + profileName + ". Timeout Value: " + hardDiskAC);
        }


        String hardDiskDC = getValue(atts, "hardDiskDC");
        if (isValid(hardDiskDC)) {
            props.setProperty(profileNamePrefix + DISK_IDLETIME_DC_PROP, hardDiskDC);
        } else {
            throw new XMLException("Hard disk DC timeout is invalid for profile " + profileName + ". Timeout Value: " + hardDiskDC);
        }

        String promptPassword = getValue(atts, "promptPassword");
        if (promptPassword != null && promptPassword.trim().length() > 0) {
            if ("true".equals(promptPassword) || "false".equals(promptPassword)) {
                props.setProperty(profileNamePrefix + PROMPT_PASSWORD_PROP, promptPassword);
            } else {
                throw new XMLException("Prompt for Password is invalid for profile " + profileName + ". PromptPassword Value: " + promptPassword);
            }
        }

        String forceApply = getValue(atts, "forceApply");
        if (forceApply != null && forceApply.trim().length() > 0) {
            if ("true".equals(forceApply) || "false".equals(forceApply)) {
                props.setProperty(profileNamePrefix + FORCE_APPLY_PROP, forceApply);
            } else {
                throw new XMLException("Apply Power Options Properties Every Time Policy Service Runs value is invalid for profile " + profileName + ". Force Update : " + forceApply);
            }

        }

        String hibernateEnabled= getValue(atts, "hibernateEnabled");
        if (hibernateEnabled != null && hibernateEnabled.trim().length() > 0) {
            if ("true".equals(hibernateEnabled) || "false".equals(hibernateEnabled)) {
                props.setProperty(profileNamePrefix + HIBERNATE_PROP, hibernateEnabled);
            } else {
                throw new XMLException("Enable Hibernate value is invalid for profile " + profileName + ". Enable Hibernate Value: " + hibernateEnabled);
            }
        }
        props.save();
    }


    private String getValue(Dictionary atts, String key) {
        String value = null;
        if (atts.get(key) != null) {
            value = ((Str)atts.get(key)).toString();
        }
        return value;
    }

    private boolean isValid (String s) {
        if (s == null) {
            return false;
        }
        if (s.trim().isEmpty()) {
            return true;
        }
        if ("never".equals(s)) {
            return true;
        }

        Pattern pattern = Pattern.compile("^[0-9]{1,3}$");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            for (int i = 0; i < validValues.length; i++) {
                if (validValues[i].equals(s)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void handleData(char data[], int length) {

    }

    public void handleEndTag(int tag, Str name) {
        stack.pop();
    }

    public void setXMLOutputStream(XMLOutputStream xos) {
        this.xos = xos;
    }

    public int resolveTag(Str name) {
        int pos;

        if ((pos = name.indexOf(':')) != -1) {
            name = name.substring(pos + 2);
        }

        Integer i = (Integer) tags.get(name);

        return (i != null) ? i.intValue() : TAG_UNKNOWN;
    }

    /**
     * DOCUMENT ME
     *
     * @param tag DOCUMENT ME
     *
     * @return DOCUMENT ME
     */
    public static final String resolveTag(int tag) {
        return (String) tagNames.get(new Integer(tag));
    }

    void writePowerPoliciesHelper(List selected) throws IOException {
        xos.writeTag(resolveTag(POWERPROFILES), null);
        writePowerScheme(selected);
        xos.writeTagFinish(resolveTag(POWERPROFILES));
    }

    void writePowerScheme(List selected) throws IOException {
        ConfigProps config = new ConfigProps(new File(main.getDataDirectory()
                .getAbsolutePath(), "PowerSettings.txt"));
        String profileNames = config
                .getProperty(POWER_PROFILE_NAME);
        if (profileNames != null && profileNames.trim().length() > 0) {
            String profileName[] = profileNames.split(",");
            for (int i = 0; i < profileName.length; i++) {
                if (profileName[i] != null
                        && profileName[i].trim().length() > 0) {
                    if (selected != null && selected.contains("profile_sel_" + profileName[i])) {
                        Hashtable ht = new Hashtable(12, 1);

                        ht.put("schemeName", profileName[i]);

                        String description = config.getProperty(profileName[i] + "." + POWER_PROFILE_DESC);
                        if (description == null) {
                            description = "";
                        }
                        ht.put("description", description);

                        String standbyAC = config.getProperty(profileName[i] + "." + STANDBY_IDLETIME_PROP);
                        if (standbyAC == null) {
                            standbyAC = "";
                        }
                        ht.put("standbyAC", standbyAC);

                        String standbyDC = config.getProperty(profileName[i] + "." + STANDBY_IDLETIME_DC_PROP);
                        if (standbyDC == null) {
                            standbyDC = "";
                        }
                        ht.put("standbyDc",standbyDC);

                        String hibernateAC = config.getProperty(profileName[i] + "." + HIBER_IDLETIME_PROP);
                        if (hibernateAC == null) {
                            hibernateAC = "";
                        }
                        ht.put("hibernateAC", hibernateAC);

                        String hibernateDC = config.getProperty(profileName[i] + "." + HIBER_IDLETIME_DC_PROP);
                        if (hibernateDC == null) {
                            hibernateDC = "";
                        }
                        ht.put("hibernateDC", hibernateDC);

                        String monitorAC = config.getProperty(profileName[i] + "." + MONITOR_IDLETIME_PROP);
                        if (monitorAC == null) {
                            monitorAC = "";
                        }
                        ht.put("monitorAC", monitorAC);

                        String monitorDC = config.getProperty(profileName[i] + "." + MONITOR_IDLETIME_DC_PROP);
                        if (monitorDC == null) {
                            monitorDC = "";
                        }
                        ht.put("monitorDC", monitorDC);

                        String hardDiskAC = config.getProperty(profileName[i] + "." + DISK_IDLETIME_PROP);
                        if (hardDiskAC == null) {
                            hardDiskAC = "";
                        }
                        ht.put("hardDiskAC", hardDiskAC);

                        String hardDiskDC = config.getProperty(profileName[i] + "." + DISK_IDLETIME_DC_PROP);
                        if (hardDiskDC == null) {
                            hardDiskDC = "";
                        }
                        ht.put("hardDiskDC", hardDiskDC);

                        String promptPassword = config.getProperty(profileName[i] + "." + PROMPT_PASSWORD_PROP);
                        if (promptPassword == null) {
                            promptPassword = "";
                        }
                        ht.put("promptPassword", promptPassword);

                        String forceApply = config.getProperty(profileName[i] + "." + FORCE_APPLY_PROP);
                        if (forceApply == null) {
                            forceApply = "";
                        }
                        ht.put("forceApply", forceApply);

                        String hibernateEnabled= config.getProperty(profileName[i] + "." + HIBERNATE_PROP);
                        if (hibernateEnabled == null) {
                            hibernateEnabled = "";
                        }
                        ht.put("hibernateEnabled", hibernateEnabled);

                        xos.writeTag(resolveTag(POWERPROFILE),
                                ht, true);
                    }
                }
            }
        }
    }

    public void serializeSelectedPowerConfig(List selected) throws IOException {
        try {
            writePowerPoliciesHelper(selected);
        } finally {
            xos.flush();
            xos.close();
        }
    }
}

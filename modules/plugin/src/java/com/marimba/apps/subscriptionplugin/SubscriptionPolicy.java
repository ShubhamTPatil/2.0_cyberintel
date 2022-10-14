// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.util.ProtectProperties;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.intf.util.IConfig;
import com.marimba.str.StrString;
import com.marimba.str.Str;
import com.marimba.tools.util.Props;
import com.marimba.intf.castanet.IFile;
import com.marimba.tools.xml.XMLOutputStream;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConnection.Connection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPException;

import com.marimba.intf.plugin.IRequest;
import com.marimba.intf.plugin.IPlugin;
import com.marimba.intf.castanet.IIndex;
import com.marimba.intf.castanet.INode;
import com.marimba.intf.transmitter.ITWorkspace;
import com.marimba.intf.transmitter.IServerAdmin;
import com.marimba.intf.transmitter.extension.ITLocalDepot;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import java.util.*;
import java.io.*;

import marimba.io.FastOutputStream;

/**
 * Responsible for fetching policy objects from LDAP for the given
 * search string. The policy sent to the end point is constructed here. This class
 * needs to be instantiated per client request.
 */

class SubscriptionPolicy implements IPluginConstants, LDAPConstants, ISubscriptionConstants, IPluginDebug {
    private ISubsPluginContext ctx;
    private IRequest request;
    private TaskDurationLogger taskDurationLogger;

    private int[] epDevLvl;
    private String[] epTemplates = null;
    private String devLevelType;
    private float clientVersion;
    private boolean version_5;
    private boolean version_6;
    private boolean version_7;
    private boolean version_8_1;
    private boolean version_8_3;

    // safety token to pevent accidental deletion of packages at the endpoint
    // if binddn deos not have read access
    private boolean readAccess;
    private Vector channels = null;
    private List<String> scapCustomTemplates = new ArrayList<String>();
    private List<String> usgcbCustomTemplates = new ArrayList<String>();
    private List<String> customCustomTemplates = new ArrayList<String>();

    SubscriptionPolicy(ISubsPluginContext ctx,
                       IRequest request,
                       TaskDurationLogger taskDurationLogger) {
        this.ctx = ctx;
        this.request = request;
        this.taskDurationLogger = taskDurationLogger;

        clientVersion = getClientVersion(request);
        epDevLvl = getEndpointDeviceLevel(request);
        devLevelType = getEndpontDeviceLevelType(request);
        if(null != devLevelType && DYNAMIC_DEVICE_TYPE.equals(devLevelType)) {
        	epTemplates = getEndpontApplicableTemplates(request);
        }
        version_5 = clientVersion >= 5.0;
        version_6 = clientVersion >= 6.0;
        version_7 = clientVersion >= 7.0;
        version_8_1 = clientVersion >= 8.1;
        version_8_3 = clientVersion >= 8.3;

        readAccess = false;
    }

    private float getClientVersion(IRequest request) {
        return ctx.getPluginLDAPUtils().getClientVersion(request);
    }

    private int[] getEndpointDeviceLevel(IRequest request) {
        String epDevLvl = (String) request.getProfileAttribute(DEVICE_LEVEL_PROFILE_ATTR);

        if (null != epDevLvl) {
            String[] devLvlArr = epDevLvl.split(",");
            int[] devLvlIntArr = new int[devLvlArr.length];
            int i = 0;

            for (String devLvl : devLvlArr) {
                try {
                    devLvlIntArr[i++] = Integer.parseInt(devLvl);
                } catch (NumberFormatException numForEx) {

                }
            }

            return devLvlIntArr;
        } else {
            return null;
        }
    }
    private String[] getEndpontApplicableTemplates(IRequest request) {
        String epTemplates = (String) request.getProfileAttribute(APPLICABLE_TEMPLATES_PROFILE_ATTR);
        if(null != epTemplates) {
        	return epTemplates.split(",");
        } else {
        	return null;
        }
    }
    private String getEndpontDeviceLevelType(IRequest request) {
        String epDevLvl = (String) request.getProfileAttribute(DEVICE_LEVEL_TYPE_PROFILE_ATTR);
        return epDevLvl;
    }
    private int[] getChannelDeviceLevel(String assignedTemplates) {
        if (DETAILED_INFO) {
            ctx.logToConsole("Device Level will be fetched for the templates : " + assignedTemplates);
        }

        String[] templatesArr = assignedTemplates.split(",");
        Props ucdProps = getUcdTempProps ();
        int[] devLvlIntArr = new int[templatesArr.length];
        // Handle Static Device Level
        if(null != assignedTemplates && assignedTemplates.startsWith(PREFIX_STATIC_LEVEL)) {
            String sDevLevel = assignedTemplates.substring(12);
            String[] sLevelArr = sDevLevel.split(",");
            int count = 0;
            for(String devLevel : sLevelArr) {
                int devLevelInt = -1;
                try {
                    devLevelInt = Integer.parseInt(devLevel);
                } catch(Exception ex) {
                    //
                }
                devLvlIntArr[count++] = devLevelInt;
            }
        } else {
            for (String tempName : templatesArr) {
                String devLvl = null;
                int devLvlInt = -1;
                int i =0;
                if(null != tempName && !"".equals(tempName.trim())) {
                    if(null != ucdProps && null != ucdProps.getProperty (tempName+".marimba.subscription.ucd.devicelevel")) {
                        devLvl = ucdProps.getProperty (tempName+".marimba.subscription.ucd.devicelevel");
                        if (DETAILED_INFO) {
                            ctx.logToConsole("Device Level for template '" + tempName + "' is : " + devLvl);
                        }
                        if (devLvl != null && !"".equals(devLvl.trim())) {
                            try {
                                devLvlInt = Integer.parseInt(devLvl);
                            } catch (NumberFormatException numForEx) {

                            }
                        }
                        devLvlIntArr[i++] = devLvlInt;
                    }
                }
            }
        }
        if (DETAILED_INFO) {
            StringBuilder prtMsg = new StringBuilder("Device Levels Identified are [");

            for (int devLvl : devLvlIntArr) {
                prtMsg.append(devLvl);
                prtMsg.append(", ");
            }

            ctx.logToConsole(prtMsg.append("]").toString ());
        }

        return devLvlIntArr;

    }
    private boolean isChannelApplicableForThisDevice(String[] epTemplatesArr, String[] chTemplatesArr) {
        // iterate through channel's template name and check it against endpoint's applicable templates

    	if (DETAILED_INFO) {
            StringBuilder prtMsg = new StringBuilder("Channel Templates array : ");

            for (String template : chTemplatesArr) {
                prtMsg.append(template);
                prtMsg.append(", ");
            }

            ctx.logToConsole(prtMsg.append("]").toString ());
        }

        if (DETAILED_INFO) {
            StringBuilder prtMsg = new StringBuilder("Endpoint Templates array : ");

            for (String epTemplate : epTemplatesArr) {
                prtMsg.append(epTemplate);
                prtMsg.append(", ");
            }

            ctx.logToConsole(prtMsg.append("]").toString ());
        }
        
        for (String template : chTemplatesArr) {
            for (String epTemplate : epTemplatesArr) {
                if (template.equalsIgnoreCase(epTemplate)) {
                    return true;
                }
            }
        }

        return false;
    }
    private boolean isChannelApplicableForThisDevice(int[] epDvcLvlArr, int[] chDvcLvlArr) {
        // iterate through channel's device levels and check it against endpoint's device level
    	
        if (DETAILED_INFO) {
            StringBuilder prtMsg = new StringBuilder("Channel Device Level array : ");

            for (int devLvl : chDvcLvlArr) {
                prtMsg.append(devLvl);
                prtMsg.append(", ");
            }

            ctx.logToConsole(prtMsg.append("]").toString ());
        }

        if (DETAILED_INFO) {
            StringBuilder prtMsg = new StringBuilder("Endpoint Device Level array : ");

            for (int epDvcLvl : epDvcLvlArr) {
                prtMsg.append(epDvcLvl);
                prtMsg.append(", ");
            }

            ctx.logToConsole(prtMsg.append("]").toString ());
        }
        
        for (int chDvcLvl : chDvcLvlArr) {
            for (int epDvcLvl : epDvcLvlArr) {
                if (chDvcLvl == epDvcLvl) {
                    return true;
                }
            }
        }

        return false;
    }

    synchronized void readSubscriptionPolicies(TargetResHelper targetDNs, String siteName) throws NamingException, LDAPException, IOException, RuntimeException {

        if(null != siteName) {
			targetDNs.add(siteName, "site");
		}

		String threadName = Thread.currentThread().getName();
		if (DETAILED_INFO) {
			ctx.logToConsole("SubscriptionPolicy.java: " + threadName + ", Policy Search happens");
		}

		if (null != siteName) {
			targetDNs.add(siteName, "site");
		}

        // Parameters written in config.xml, subscription.txt, and tunerproperties.txt
        Vector cprops = null;

        // Following parameters are written only in config.xml file
        Vector targetdns = null;
        Vector softwares = null;
        Long modifytime = null;
        Map chOrders = null;
        Map channels2 = null;
        Map chinitsched = null;
        Map chsecsched = null;
        Map chupdsched = null;
        Map chversched = null;
        Map chexemptbo = null;
        Map chwowdep = null;
        Map chUcdTemp = null;
        Map chARTaskId = null;

        // Creating the outermost Node of config.xml.
        Node configNode = new Node(CONFIG, "CONFIG", null);

        if (version_5) {
            configNode.setProperty(new StrString("PLUGIN_VERSION"),
                    new StrString(ctx.getPublishedPluginProps().getProperty("channel.version")));

            IConfig tunerConfig = (IConfig) ctx.getPluginContext().getFeature("config");
            if (null != tunerConfig) {
                String hostName = tunerConfig.getProperty("marimba.security.token.host");
                hostName = (hostName == null) ? "unknown" : hostName;
                configNode.setProperty(new StrString("HOST_NAME"), new StrString(hostName));

                String ipAddress = tunerConfig.getProperty("runtime.host.addr");
                ipAddress = (ipAddress == null) ? "0.0.0.0" : ipAddress;
                configNode.setProperty(new StrString("IP_ADDRESS"), new StrString(ipAddress));
            }
        }

        // Creating the Props objects for subscription.txt and
        // tuner-properties.txt. These files are created only when
        // when requesting Subscription Service client is 4.6 or less
        Props subp = new Props();
        Props tunerp = new Props();

        // Vector to store target names for creating
        // marimba.subscription.targets property used only for clients
        // 4.6 and less
        Vector target_arr = new Vector();
        LDAPConnection dir = ctx.getPluginLDAPEnv().getDefaultConnection();
        Connection c = dir.getConnection();
        String dirType = ctx.getPluginLDAPEnv().getProperty(PROP_VENDOR);
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        String searchStr = formSearchQuery(targetDNs, siteName, LDAPVarsMap);

        try {
            if (!(searchStr.length() == 0)) {
                String[] attrs = getSearchAttributes(version_5, version_6, dirType);

                // get all subscriptions for the DNs in the list.
                taskDurationLogger.taskStarted(PluginTasks.RESOLVE_SUBSCRIPTIONS);
                // build up the search
                String searchBase = ctx.getSubsMrbaConfigMgr().getSubConfig().getProperty("marimba.subscriptionplugin.securitybase");
                SearchControls constraints = new SearchControls();
                constraints.setCountLimit(dir.limit);
                constraints.setSearchScope(LDAPConstants.SUBTREE_SCOPE);
                if (attrs != null) {
                    constraints.setReturningAttributes(attrs);
                }

					if (DETAILED_INFO) {
						for (int i=0; i<attrs.length; i++) {
							ctx.logToConsole("SubscriptionPolicy.java: ThreadName : " + threadName +
									", Policy Search: attr : " + attrs[i]);
						}
					}

				NamingEnumeration nenum = dir.searchWrapper(c, searchStr, searchBase, constraints);
				long startTime = System.currentTimeMillis ();
				System.out.println(threadName + " - Policy Search: Time taken (in ms) : " +
							(System.currentTimeMillis () - startTime) + ", using connection : " +
							c.getProperty ("connUniqueId") + ", for query : " + searchStr );
                taskDurationLogger.taskStopped(PluginTasks.RESOLVE_SUBSCRIPTIONS);

                // Read the Subscription search results and create the
                // Subscription manifest files

                while ((nenum != null) && nenum.hasMoreElements()) {
                    SearchResult sr = (SearchResult) nenum.next();
                    Attributes srAttrs = sr.getAttributes();

                    channels = new Vector();
                    cprops = new Vector();
                    targetdns = new Vector();
                    softwares = new Vector();

                    String subname = null;

                    if (srAttrs != null) {
                        List svec = ctx.getPluginLDAPUtils().getValue(srAttrs, "cn");

                        if (svec.size() <= 0) {
                            continue;
                        }

                        if (LDAPVarsMap.get("CHECK_ACCESS_TOKEN").equals(svec.get(0))) {
                            readAccess = true;
                            continue;
                        }

                        subname = (String) svec.get(0);

                        if (!target_arr.contains(subname)) {
                            target_arr.addElement(subname);
                        }

                        // Creating the Subscription Target node.
                        Hashtable targetAttrs = null;
                        Node subTarget = new Node(SUB_TARGET, "SUB_TARGET", targetAttrs);
                        String targetName = null;
                        String targetType = null;
                        StrString blackout;
                        StrString blkoutPriority = new StrString(MAX_PROPERTY_PRIORITY);
                        String arTaskId;

                        // Read the target dn attributes into a vector
                        Attribute tAttrs = srAttrs.get(LDAPVarsMap.get("TARGETDN"));

                        if (tAttrs != null) {
                            NamingEnumeration allValues = tAttrs.getAll();

                            while (allValues.hasMoreElements()) {
                                Object value = allValues.next();
                                if (value instanceof String) {
                                    targetName = ctx.getPluginLDAPUtils().getCanonicalName((String) value);
                                    targetdns.addElement(targetName);
                                }
                            }
                        }

                        // If we are obtaining 4.7 Subscriptions, we have TARGETDN attribute set,
                        // but not TARGETTYPE
                        // If we are obtaining 5.0 and above subscriptions,
                        // we would have both set, unless we are sourcing users from Tx.  In which
                        // case, we use the Subsription CN to obtain the target's name and type
                        List tname = ctx.getPluginLDAPUtils().getValue(srAttrs, LDAPVarsMap.get("TARGETTYPE"));

                        if (tname.size() > 0) {
                            targetType = (String) tname.get(0);
                        }

                        int i = subname.lastIndexOf(DELIM);

                        if ("site".equals(targetType)) {
                            targetName = subname;
                        }

                        if (targetType == null || targetName == null) {
                            if (i != -1) {
                                // We get the target name and type from the Subscription CN
                                // <target name>_<target type>
                                // This is also what we use for the all_all target
                                //all_all should be represented as all_all and not all to be consistent with acl
                                if ("all_all".equals(subname)) {
                                    targetName = subname;
                                } else {
                                    targetName = subname.substring(0, i);
                                }

                                if (targetType == null) {
                                    targetType = subname.substring(i + 1);
                                }
                                if(HIGH_PRIORITY_INFO) {
                                    ctx.logToConsole("SubscriptionPolicy: TargetName " + targetName + "; TargetType " + targetType);
                                }
                            }
                        }

                        targetName = ctx.getPluginLDAPUtils().getCanonicalName(targetName);
                        System.out.println(threadName + " -SubscriptionPolicy using connection : " +
    							c.getProperty ("connUniqueId") + ", targetName=" + targetName + "; targetType=" + targetType);

                        if(HIGH_PRIORITY_INFO) {
                            ctx.logToConsole("SubscriptionPolicy: targetName=" + targetName + "; targetType=" + targetType);
                        }

                        // Setting the attributes and their values of the SUB_TARGET Node
                        subTarget.setProperty(new StrString("TARGET_NAME"), new StrString(targetName));
                        subTarget.setProperty(new StrString("TARGET_TYPE"), new StrString(targetType));

                        // Write Blackout schedule in config.xml
                        if (version_5) {
                            List blkshd = ctx.getPluginLDAPUtils().getValue(srAttrs, LDAPVarsMap.get("BLACKOUTSCHED"));

                            if (blkshd.size() > 0) {
                                blackout = new StrString((String) blkshd.get(0));

                                if (blackout != null) {
                                    // from 7.5.00.002 and 8.0.00, blackout string has been changed.
                                    // plug-in should return the right string to service.
                                    // service channels earlier to 7.5.002 and 8.0.00 dont understand the new string
                                    // to support the backward compatibility, return the blackout schedule alone
                                    // otherwise return anytime on mon+tue+... BLACKOUT 10:00AM-5:00PM
                                    if (clientVersion < 8) {
                                        if (isNewBlackoutRequired()) {
                                            String blkOut = blackout.toString();
                                            int index = blkOut.indexOf("BLACKOUT");
                                            if (index != -1) {
                                                blkOut = blkOut.substring(index + "BLACKOUT ".length());
                                                blackout = new StrString(blkOut);
                                            }
                                        }
                                    }
                                    String blkOut = blackout.toString();
                                    String blkPriority = blkoutPriority.toString();
                                    int prtyIndex = blkOut.lastIndexOf(",");
                                    if(prtyIndex != -1) {
                                        blkPriority = blkOut.substring(prtyIndex+1,blkOut.length());
                                        blkOut = blkOut.substring(0,prtyIndex);
                                        blackout = new StrString(blkOut);
                                    }

                                    subTarget.setProperty(new StrString("BLACKOUTSCHED"), blackout);
                                    subTarget.setProperty(new StrString("ORDER"), new StrString(blkPriority));
                                }
                            }
                        }

                        if (version_6) {
                            modifytime = new Long(ctx.getPluginLDAPEnv().getLDAPEnv().getLastModified(srAttrs));
                        }


                        // Read the channels/states attributes into a vector
                        Attribute channelAttrs = srAttrs.get(LDAPVarsMap.get("CHANNEL"));

                        if (channelAttrs != null) {
                            NamingEnumeration allValues = channelAttrs.getAll();

                            while (allValues.hasMoreElements()) {
                                Object value = allValues.next();

                                if (value instanceof String) {
                                    channels.addElement(value);

                                    if (DETAILED_INFO) {
                                        ctx.logToConsole("SubscriptionPolicy: channel = " + value);
                                    }
                                }
                            }
                        }

                        // Read subscription properties
                        Attribute propertyAttrs = srAttrs.get(LDAPVarsMap.get("PROPERTY"));

                        if (propertyAttrs != null) {
                            NamingEnumeration allValues = propertyAttrs.getAll();

                            while (allValues.hasMoreElements()) {
                                Object value = allValues.next();

                                if (value instanceof String) {
                                    cprops.addElement(value);
                                }
                            }
                        }

                        // Get the attributes from the LDAP for the target
                        // and save them in a Hashtable with channel url
                        // as the key and attribute value as the value
                        chOrders = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELORDER"));
                        channels2 = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELSEC"));

                        if (version_5) {
                            // Schedules are read only when the Subscription Service client is 5.0+
                            chinitsched = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELINITSCHED"));
                            chsecsched = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELSECSCHED"));
                            chupdsched = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELUPDATESCHED"));
                            chversched = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELVERREPAIRSCHED"));
                        }

                        if (version_6) {
                            chexemptbo = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT"));
                        }

                        if (version_7) {
                            chARTaskId = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("AR_REFERENCE_TAG"));
                            chwowdep = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELWOWENABLED"));
                        }

                        if(version_8_1) {
                            Attribute unInstallers = srAttrs.get(LDAPVarsMap.get("UNINSTALLSTR"));

                            if (unInstallers != null) {
                                NamingEnumeration allValues = unInstallers.getAll();

                                while (allValues.hasMoreElements()) {
                                    Object value = allValues.next();

                                    if (value instanceof String) {
                                        softwares.addElement(value);
                                    }
                                }
                            }
                        }

                        if(version_8_3) {
                            chUcdTemp = ctx.getPluginLDAPUtils().readAttrtoHashMap(srAttrs, LDAPVarsMap.get("CHANNELUCDTEMPLATE"));
                        }

                        // if there are no targets check to see if we have read access.
                        // If shouldSendXMLSubscriptionManifest is true then create the Nodes for
                        // config.xml ( version 4.7 or greater)
                        // else create the Props for the text files
                        // (4.6 or less)
                        if (shouldSendXML(request)) {
                            if (version_8_3 && epDevLvl != null && epDevLvl.length > 0) {
                                // filter channels based on endpoint's device level
                                filterChannelsWithDeviceLevel(channels, cprops, chOrders, channels2, chinitsched, chsecsched,
                                        chupdsched, chversched, chexemptbo, chwowdep, chUcdTemp);
                            }

                            createNodes(targetdns, channels, cprops, softwares, modifytime, chOrders, channels2, chinitsched,
                                    chsecsched, chupdsched, chversched, chexemptbo, chwowdep, chUcdTemp, chARTaskId,
                                    subTarget, isSubscriptionReporterRequest(request), clientVersion);
                            configNode.addNode(subTarget);
                        } else {
                            createProps(channels, channels2, cprops, subp, tunerp);
                        }
                    }
                }
            }
        } catch (LDAPException ldapEx) {
			if (HIGH_PRIORITY_INFO) {
				ctx.logToConsole("SubscriptionPolicy.java: " + threadName + ", Exception occured while policy search : " + ldapEx.getMessage ());
				ldapEx.printStackTrace ();
			}

            String exMsg = ldapEx.toString();

            if (null != exMsg && (exMsg.indexOf(CTX_NULL_EXC_MSG) != -1)) {
                if (!c.nuked) {
                    dir.releaseConnection(c);
                }
            } else {
                throw ldapEx;
            }
		} catch (RuntimeException runEx) {
			runEx.printStackTrace ();
			throw runEx;
        } catch (Exception ex) {
            ctx.logToConsole ("Exception while constructing policies : " + ex.getMessage ());

            if (DETAILED_INFO) {
                ex.printStackTrace ();
            }

            throw new RuntimeException(ex.getMessage ());
		} finally {
            if (null != c && !c.nuked) {
                dir.releaseConnection(c);
            }
        }
        // If shouldSendXMLSubscriptionManifest is true then insert config.xml to the index
        // else insert the text files.
        if (shouldSendXML(request)) {
            if (version_6) {
                addTargetsToNode(configNode, targetDNs.getTargetsMap());
            }
            addFiles(configNode, request.getChannelIndex());
        } else {
            // Write target Channel Property
            writeTargetChannelProp(tunerp, target_arr);
            addFiles(subp, tunerp, request.getChannelIndex());
        }

        Props ucdProps = getUcdTempProps ();
        if(null != ucdProps) {
            addUCDTemplateFile(ucdProps, request.getChannelIndex());
        }
        // append scap custom template
        if(scapCustomTemplates.size() > 0) {
        	for(String customTemplateName : scapCustomTemplates) {
        		
        		String templateName = customTemplateName+".properties";
        		
        		Props templateProps = getTempalteProps(templateName);
        		if(null != templateProps) {
        			ctx.logToConsole("Bundle SCAP Custom profile with Plugin :" + templateName);
        			appendTemplateFile(templateProps, request.getChannelIndex(), templateName);
                }
        	}
        }
        // append scap(windows) custom template
        if(usgcbCustomTemplates.size() > 0) {
        	for(String customTemplateName : usgcbCustomTemplates) {
        		
        		String templateName = customTemplateName+".properties";
        		
        		Props templateProps = getTempalteProps(templateName);
        		if(null != templateProps) {
        			ctx.logToConsole("Bundle SCAP(Windows) Custom profile with Plugin :" + templateName);
        			appendTemplateFile(templateProps, request.getChannelIndex(), templateName);
                }
        	}
        }
        // append custom scanner custom template
        if(customCustomTemplates.size() > 0) {
            for(String customTemplateName : customCustomTemplates) {

                String templateName = customTemplateName+".properties";

                Props templateProps = getTempalteProps(templateName);
                if(null != templateProps) {
                    ctx.logToConsole("Bundle Custom Scanner Custom profile with Plugin :" + templateName);
                    appendTemplateFile(templateProps, request.getChannelIndex(), templateName);
                }
            }
        }
        // append cve filter props for vulnerability assessment
        Props cveFilterProps = getCveFiltersProps();
        if (null != cveFilterProps) {
            /** Creating an OutputStreams to create the txt file */
            FastOutputStream fosCveFilter = new FastOutputStream(4096);
            cveFilterProps.save(fosCveFilter);

            addCveFilterFile("cve_filters", fosCveFilter, request.getChannelIndex());

            String cveFilterPropsKeys[] = cveFilterProps.getKeys();
            for (String cveFiltersPropsKey : cveFilterPropsKeys) {
                String cveFilterFile = cveFilterProps.getProperty(cveFiltersPropsKey);

                InputStream is = null;
                FastOutputStream os = new FastOutputStream(4096);
                try {
                    is = getCveFiltersFile(cveFilterFile);
                    if (null != is) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        //read from is to buffer
                        while((bytesRead = is.read(buffer)) !=-1){
                            os.write(buffer, 0, bytesRead);
                        }
                        addCveFilterFile("cve_filters_" + cveFilterFile, os, request.getChannelIndex());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if(null != is) {
                        try {
                            is.close();
                        } catch(Exception e){}
                    }
                    if(null != os) {
                        try {
                            os.flush();
                            os.close();
                        } catch(Exception f){}
                    }
                }
            }
        }
        // append cve info props for vulnerability assessment
        Props cveInfoProps = getCveInfoProps();
        if (null != cveInfoProps) {
            /** Creating an OutputStreams to create the txt file */
            FastOutputStream fosCveInfo = new FastOutputStream(4096);
            cveInfoProps.save(fosCveInfo);

            addCveInfoFile("cve_info", fosCveInfo, request.getChannelIndex());
        }
    }

    private Props getUcdTempProps () {
        try {
            IFile ucdTempFile = ctx.getPluginContext().getChannelFile(UCD_TEMPLATE_FILENAME);

            if(ucdTempFile != null) {
                Props ucdTempProps = new Props(ucdTempFile, -1);

                if (!ucdTempProps.load()) {
                    return null;
                }

                return ucdTempProps;
            }
        } catch(Exception ex) {
            ctx.logToConsole ("Unable to read ucdtemplate.txt file : " + ex.getMessage ());

            if (DETAILED_INFO) {
                ex.printStackTrace ();
            }
        }

        return null;
    }
    private Props getTempalteProps (String templateFileName) {
        try {
            IFile templateTempFile = ctx.getPluginContext().getChannelFile(templateFileName);

            if(templateTempFile != null) {
                Props templateTempProps = new Props(templateTempFile, -1);

                if (!templateTempProps.load()) {
                    return null;
                }

                return templateTempProps;
            }
        } catch(Exception ex) {
            ctx.logToConsole ("Unable to read  " + templateFileName + "file : " + ex.getMessage ());

            if (DETAILED_INFO) {
                ex.printStackTrace ();
            }
        }

        return null;
    }

    private Props getCveFiltersProps() {
        try {
            IFile cveFiltersFile = ctx.getPluginContext().getChannelFile("cve_filters/cve_filters.txt");
            if (cveFiltersFile != null) {
                Props cveFiltersProps = new Props(cveFiltersFile, -1);

                if (!cveFiltersProps.load()) {
                    return null;
                }

                return cveFiltersProps;
            }
        } catch(Exception ex) {
            ctx.logToConsole ("Unable to read cve_filters/cve_filters.txt file : " + ex.getMessage ());

            if (DETAILED_INFO) {
                ex.printStackTrace ();
            }
        }

        return null;
    }

    private Props getCveInfoProps() {
        try {
            IFile cveInfoFile = ctx.getPluginContext().getChannelFile("cve_info.properties");
            if (cveInfoFile != null) {
                Props cveInfoProps = new Props(cveInfoFile, -1);

                if (!cveInfoProps.load()) {
                    return null;
                }

                return cveInfoProps;
            }
        } catch(Exception ex) {
            ctx.logToConsole ("Unable to read cve_info.properties file : " + ex.getMessage ());

            if (DETAILED_INFO) {
                ex.printStackTrace ();
            }
        }

        return null;
    }

    private InputStream getCveFiltersFile(String fileName) {
        try {
            IFile cveFiltersFile = ctx.getPluginContext().getChannelFile("cve_filters/" + fileName);
            if (cveFiltersFile != null) {
                return cveFiltersFile.getInputStream();
            }
        } catch(Exception ex) {
            ctx.logToConsole ("Unable to read cve_filters/" + fileName + " file : " + ex.getMessage ());

            if (DETAILED_INFO) {
                ex.printStackTrace ();
            }
        }

        return null;
    }

    private boolean isNewBlackoutRequired() {
        String version = "";
        version = ctx.getPluginLDAPUtils().getStrClientVersion(request);

        if (version.startsWith("7.5")) {
            // Following check is for the 7.5.00 patch releases since we support this from 7.5.002 onwards.
            String patchVersion = version.substring(version.lastIndexOf(".") + 1);

            if (Integer.parseInt(patchVersion) > 1) {
                return false;
            }
        }

        return true;
    }

    private String formSearchQuery(TargetResHelper targetDNs, String siteName, Map<String, String> LDAPVarsMap) {
        // Include the 'All' target to the query . This target contains
        // TARGET_ALL attribute set to true
        String searchStr = targetDNs.getSearchString();
        String searchStrForSites = formSearchQueryForSite(siteName, LDAPVarsMap);
        StringBuffer searchStrBuffer = new StringBuffer();
        if (searchStr.length() == 0) {
            // only search for 'all' subscriptions if the search string is
            // empty
            searchStrBuffer.append("(|(");
            searchStrBuffer.append(LDAPVarsMap.get("TARGET_ALL"));
            searchStrBuffer.append("=true)(cn=");
            searchStrBuffer.append(LDAPVarsMap.get("CHECK_ACCESS_TOKEN"));
            searchStrBuffer.append("))");

            searchStr = searchStrBuffer.toString();
        } else {
            searchStrBuffer.append("(|");
            searchStrBuffer.append(searchStr);
            searchStrBuffer.append(searchStrForSites);
            searchStrBuffer.append("(");
            searchStrBuffer.append(LDAPVarsMap.get("TARGET_ALL"));
            searchStrBuffer.append("=true)(cn=");
            searchStrBuffer.append(LDAPVarsMap.get("CHECK_ACCESS_TOKEN"));
            searchStrBuffer.append("))");

            searchStr = searchStrBuffer.toString();
        }

        return searchStr;
    }

    private String formSearchQueryForSite(String siteName, Map<String, String> LDAPVarsMap) {
        if (siteName == null || siteName.trim().length() == 0) {
            return "";
        }

        StringBuilder query = new StringBuilder();
        //(&(cn=SiteA)(marimbaCom1996-Castanet-SubscriptionTargetType=site))

        query.append("(&(cn=");
        query.append(siteName);
        query.append(")(");
        query.append(LDAPVarsMap.get("TARGETTYPE"));
        query.append("=site))");

        return query.toString();
    }

    /**
     * Adding the subscription.txt and tuner-properties.txt to the index
     *
     * @param subp REMIND
     * @param tunerp REMIND
     * @param index REMIND
     */
    private void addFiles(Props subp,
                          Props tunerp,
                          IIndex index) {
        /** Creating an OutputStreams to create the txt files */
        FastOutputStream fosSub = new FastOutputStream(1024);
        FastOutputStream fosProp = new FastOutputStream(1024);

        subp.save(fosSub);
        tunerp.save(fosProp);

        /** Adding the prop files to the channel index */
        byte[] subBytes = fosSub.toByteArray();
        byte[] propBytes = fosProp.toByteArray();
        INode subNode = ctx.getPluginContext().makeNode(subBytes, null, IPlugin.PLUGIN_NODE_IMMEDIATE );
        INode propNode = ctx.getPluginContext().makeNode(propBytes, null, IPlugin.PLUGIN_NODE_IMMEDIATE );
        Str subFile = new StrString(SUBSCRIPTION_FILE);
        Str propFile = new StrString(TUNERPROPS_FILE);
        index.insert(subFile, subNode);
        index.insert(propFile, propNode);
    }

    private void addUCDTemplateFile(Props ucdProps, IIndex index) {
        /** Creating an OutputStreams to create the txt file */
        FastOutputStream fosUCD = new FastOutputStream(1024);
        ucdProps.save(fosUCD);

        /** Adding the prop files to the channel index */
        byte[] ucdBytes = fosUCD.toByteArray();
        INode UCDNode = ctx.getPluginContext().makeNode(ucdBytes, null,
                IPlugin.PLUGIN_NODE_IMMEDIATE);

        Str ucdFile = new StrString(UCD_TEMPLATE_FILENAME);
        index.insert(ucdFile, UCDNode);
    }

    private void appendTemplateFile(Props templateProps, IIndex index, String templateName) {
        /** Creating an OutputStreams to create the template file */
        FastOutputStream fosTemplate = new FastOutputStream(1024);
        templateProps.save(fosTemplate);

        /** Adding the prop files to the channel index */
        byte[] templateBytes = fosTemplate.toByteArray();
        INode templateNode = ctx.getPluginContext().makeNode(templateBytes, null, IPlugin.PLUGIN_NODE_IMMEDIATE);

        Str file = new StrString(templateName);
        index.insert(file, templateNode);
    }

    private void addCveFilterFile(String cveFilterName, FastOutputStream fosCveFilter, IIndex index) {
        /** Adding the prop files to the channel index */
        byte[] cveFilterBytes = fosCveFilter.toByteArray();
        INode cveFilterNode = ctx.getPluginContext().makeNode(cveFilterBytes, null, IPlugin.PLUGIN_NODE_IMMEDIATE);

        Str cveFilterFile = new StrString(cveFilterName);
        index.insert(cveFilterFile, cveFilterNode);
    }

    private void addCveInfoFile(String cveInfoName, FastOutputStream fosCveInfo, IIndex index) {
        /** Adding the prop files to the channel index */
        byte[] cveInfoBytes = fosCveInfo.toByteArray();
        INode cveInfoNode = ctx.getPluginContext().makeNode(cveInfoBytes, null, IPlugin.PLUGIN_NODE_IMMEDIATE);

        Str cveInfoFile = new StrString(cveInfoName);
        index.insert(cveInfoFile, cveInfoNode);
    }

    private boolean isSubscriptionReporterRequest(IRequest request) {
        return ctx.getPluginLDAPUtils().isSubscriptionReporterRequest(request);
    }

    /**
     * Create the Props objects for channel subscriptions and  propterty subscriptions
     *
     * @param channels REMIND
     * @param channels2 REMIND
     * @param cprops REMIND
     * @param subp REMIND
     * @param tunerp REMIND
     */
    private void createProps(Vector channels,
                             Map channels2,
                             Vector cprops,
                             Props subp,
                             Props tunerp) {
        int count = 0;
        String keyStr = null;
        String valueStr = null;

        // Write the channel urls/states
        for (int j = 0; j < channels.size(); j++) {
            String lineStr = (String) channels.elementAt(j);

            // skip all 'none' states to save space in subscription.txt file
            if (lineStr.endsWith(ISubscriptionConstants.STATE_NONE)) {
                if (DETAILED_INFO) {
                    ctx.logToConsole("Skipping 'none' state");
                }

                continue;
            }

            int i = lineStr.indexOf("=");

            if (i != -1) {
                keyStr = lineStr.substring(0, i);

                // If secondary state is sepcified save secondary state
                // in the subscription.txt
                StrString st = (StrString) channels2.get(keyStr);

                if ((st == null) || (st.length() == 0)) {
                    valueStr = lineStr.substring(i + 1);
                } else {
                    valueStr = st.toString();
                }

                keyStr += ("|" + count++);

                if (DETAILED_INFO) {
                    ctx.logToConsole(lineStr);
                }
            }

            if (keyStr != null) {
                subp.setProperty(keyStr, valueStr);
            }

            keyStr = null;
            valueStr = null;
        }

        // Write the proprties
        for (int k = 0; k < cprops.size(); k++) {
            String lineStr = (String) cprops.elementAt(k);

            //Do not send keychain entries if Subscription Service version <4.7
            // i.e GETXML_PROFILE_ATTR is not set
            if (lineStr != null) {
                if (ProtectProperties.isProtectedProperty(lineStr)) {
                    continue;
                }
            }

            int i = lineStr.indexOf("=");

            if (i != -1) {
                keyStr = lineStr.substring(0, i) + "|" + count++;
                valueStr = lineStr.substring(i + 1);

                if (DETAILED_INFO) {
                    ctx.logToConsole(lineStr);
                }
            }

            if (keyStr != null) {
                tunerp.setProperty(keyStr, valueStr);
            }

            keyStr = null;
            valueStr = null;
        }
    }

    private boolean shouldSendXML(IRequest request) {
        // Boolean to indicate if the subscription manifest should be sent in an XML file
        Object getXml = request.getProfileAttribute(GETXML_PROFILE_ATTR);
        return ("true".equals(getXml)) ? true : false;
    }

    /**
     *
     * @param configNode
     * @param tgtMap
     */
    private void addTargetsToNode(Node configNode, Map tgtMap) {
        String key, val;

        Node tgtsnode = new Node(TARGET_MEMBERSHIP, "TARGET_MEMBERSHIP", null);
        for (Iterator ite = tgtMap.keySet().iterator(); ite.hasNext();) {
            key = (String) ite.next();
            val = (String) tgtMap.get(key);
            Node tnode = new Node(TARGET, "TARGET", null);

            tnode.setProperty(new StrString("NAME"), new StrString(ctx.getPluginLDAPUtils().getCanonicalName(key)));
            tnode.setProperty(new StrString("TYPE"), new StrString(val));
            tgtsnode.addNode(tnode);
        }

        Node tnode = new Node(TARGET, "TARGET", null);
        tnode.setProperty(new StrString("NAME"), new StrString("all_all"));
        tnode.setProperty(new StrString("TYPE"), new StrString("all"));
        tgtsnode.addNode(tnode);
        configNode.addNode(tgtsnode);
    }

    /**
     * Adding the config.xml to the inClientIndex
     *
     * @param inConfigurationNode REMIND
     * @param inClientIndex REMIND
     *
     * @throws IOException REMIND
     */
    private void addFiles(Node inConfigurationNode, IIndex inClientIndex) throws IOException {
        /** Creating an XMLOutputStream to create the XML file. */
        FastOutputStream fosCon = new FastOutputStream(2048); // REMIND: gcg: What's the average, largish config.xml we normally see? Preventing re-allocations would be good.
        XMLOutputStream xout = new XMLOutputStream(fosCon);

        try {
            inConfigurationNode.write(xout);
        } finally {
            xout.close();
        }

        byte[] cnfgBytes = fosCon.toByteArray();

        /** Adding the XML file to the channel inClientIndex */
        INode cnfgNode = ctx.getPluginContext().makeNode(cnfgBytes, null, IPlugin.PLUGIN_NODE_IMMEDIATE );
        Str cnfgID = new StrString(CONFIGXML_FILE);

        if(isSubscriptionReporterRequest(request)){
            float clientVers = ctx.getPluginLDAPUtils().getClientVersion(request);
            if(clientVers > 7.0){
                cnfgID = new StrString(REPORTERXML_FILE);
            }
        }

        inClientIndex.insert(cnfgID, cnfgNode);
        fosCon.close();
    }

    /**
     * Write the marimba.subscription.targets property in tuner-properrties.txt
     */
    private void writeTargetChannelProp(Props tunerp, Vector target_arr) {
        int count = target_arr.size();

        for (int i = 0; i < count; i++) {
            tunerp.setProperty(TARGET_CHANNEL_PROP + i, (String) target_arr.elementAt(i));
        }

        tunerp.setProperty(TARGET_CHANNEL_TOTAL, String.valueOf(count));
    }

    private String[] getSearchAttributes(boolean version_5, boolean version_6, String dirType) {
        String[] attrs = null;
        int attrcount = 7;

        if (version_5) {
            attrcount = 12;
        }

        if (version_6) {
            attrcount = 15;
        }

        if (version_7) {
            attrcount = 16;
        }

        if (version_8_1) {
            attrcount = 17;
        }

        if (version_8_3) {
            attrcount = 18;
        }
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        // Read following attributes from the Subscription objects
        attrs = new String[attrcount];
        attrs[0] = LDAPVarsMap.get("CHANNEL");
        attrs[1] = LDAPVarsMap.get("PROPERTY");
        attrs[2] = LDAPVarsMap.get("CHANNELORDER");
        attrs[3] = "cn";
        attrs[4] = LDAPVarsMap.get("TARGETDN");
        attrs[5] = LDAPVarsMap.get("TARGETTYPE");
        attrs[6] = LDAPVarsMap.get("CHANNELSEC");

        // Read schedule attributes only if Subscription Service client is 5.0
        if (version_5) {
            attrs[7] = LDAPVarsMap.get("BLACKOUTSCHED");
            attrs[8] = LDAPVarsMap.get("CHANNELINITSCHED");
            attrs[9] = LDAPVarsMap.get("CHANNELSECSCHED");
            attrs[10] = LDAPVarsMap.get("CHANNELUPDATESCHED");
            attrs[11] = LDAPVarsMap.get("CHANNELVERREPAIRSCHED");
        }

        if (version_6) {
            attrs[12] = LDAPVarsMap.get("OP_MODIFYTIMESTAMP");
            attrs[13] = LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT");
            attrs[14] = LDAPVarsMap.get("AR_REFERENCE_TAG");
        }

        if (version_7) {
            attrs[15] = LDAPVarsMap.get("CHANNELWOWENABLED");
        }

        if(version_8_1) {
            attrs[16] = LDAPVarsMap.get("UNINSTALLSTR");
        }

        if(version_8_3) {
            attrs[17] = LDAPVarsMap.get("CHANNELUCDTEMPLATE");
        }

        return attrs;
    }

    private void filterChannelsWithDeviceLevel(Vector channels, Vector cprops, Map chOrders, Map channels2, Map chinitsched,
                                               Map chsecsched, Map chupdsched, Map chversched, Map chexemptbo,
                                               Map chwowdep, Map chUcdTemp) {
        Iterator chUrls = chUcdTemp.keySet().iterator();

        while (chUrls.hasNext ()) {
            StrString chUrlKey = (StrString) chUrls.next();
            StrString devLvlVal = (StrString) chUcdTemp.get(chUrlKey);

            if (devLvlVal != null) {
                boolean isSameDeviceType = compareDeviceType(devLvlVal.toString());
                boolean includeChnl = false;
                if(isSameDeviceType) {
                	// if Device type is dynamic then compare config. template to endpoint applicable template
                	if(null != devLevelType && DYNAMIC_DEVICE_TYPE.equals(devLevelType)) {
                		String[] chTemplatesArr = (devLvlVal.toString()).split(",");
                		includeChnl = isChannelApplicableForThisDevice (epTemplates, chTemplatesArr);
                	} else {
                		int[] chDvcLvlArr = getChannelDeviceLevel(devLvlVal.toString());
                        includeChnl = isChannelApplicableForThisDevice (epDevLvl, chDvcLvlArr);
                	}
                } else {
                    if (DETAILED_INFO) {
                        ctx.logToConsole("SubscriptionPolicy.filterChannelsWithDeviceLevel() conflict device type : true");
                        ctx.logToConsole("Device level of " + chUrlKey.toString() + " : " + devLvlVal.toString());
                        ctx.logToConsole("Endpoint device level type : " + devLevelType);
                    }
                    includeChnl = true;
                }
                if (DETAILED_INFO) {
                    ctx.logToConsole("SubscriptionPolicy.filterChannelsWithDeviceLevel() includeChnl : " + includeChnl);
                }

                if (!includeChnl) {
                    if (DETAILED_INFO) {
                        ctx.logToConsole("Channel to be removed: " + chUrlKey);
                    }

                    int idx = -1;
                    for (int i=0; i < channels.size(); i++) {
                        if (DETAILED_INFO) {
                            ctx.logToConsole("Processing channel : " + channels.get(i));
                        }
                        String element = (String) channels.get(i);
                        String[] chnAndState = element.split ("=");

                        if (chnAndState[0] != null && chnAndState[0].equals (chUrlKey.toString ())) {
                            idx = i;
                        }
                    }

                    if (idx != -1) {
                        channels.remove(idx);
                    }

                    idx = -1;
                    for (int i=0; i < cprops.size(); i++) {
                        if (DETAILED_INFO) {
                            ctx.logToConsole("Processing property : " + cprops.get(i));
                        }
                        String element = (String) cprops.get(i);
                        String[] chnAndState = element.split ("=");

                        if (chnAndState[0] != null && chnAndState[0].equals (chUrlKey.toString ())) {
                            idx = i;
                        }
                    }

                    if (idx != -1) {
                        cprops.remove(idx);
                    }

                    chOrders.remove(chUrlKey);
                    channels2.remove(chUrlKey);
                    chinitsched.remove(chUrlKey);
                    chsecsched.remove(chUrlKey);
                    chupdsched.remove(chUrlKey);
                    chversched.remove(chUrlKey);
                    chexemptbo.remove(chUrlKey);
                    chwowdep.remove(chUrlKey);
                }
            }
        }
    }
    
    private boolean compareDeviceType(String chDevLevel) {
        if(chDevLevel.startsWith(PREFIX_STATIC_LEVEL) && null != devLevelType && devLevelType.equalsIgnoreCase(STATIC_DEVICE_TYPE)) {
            return true;
        }
        if(!chDevLevel.startsWith(PREFIX_STATIC_LEVEL) && null != devLevelType && !devLevelType.equalsIgnoreCase(STATIC_DEVICE_TYPE)) {
            return true;
        }
        if(chDevLevel.startsWith(PREFIX_STATIC_LEVEL) && null != devLevelType && devLevelType.equalsIgnoreCase(CUSTOM_DEVICE_TYPE)) {
            return true;
        }
        return false;
    }
    /**
     * Crete the CHANNELS and the PROPS nodes  for the config.xml file
     *
     * @param targetdns REMIND
     * @param channels REMIND
     * @param properties REMIND
     * @param modifytime REMIND

     * @param chOrders REMIND
     * @param channels2 REMIND
     * @param chinitsched REMIND
     * @param chsecsched REMIND
     * @param chupdsched REMIND
     * @param chversched REMIND
     * @param subTarget REMIND
     * @param isSubsReporterRequest REMIND
     * @param clientVersion REMIND
     */
    private void createNodes(Vector targetdns, Vector channels, Vector properties, Vector softwares, Long modifytime,
                             Map chOrders, Map channels2, Map chinitsched, Map chsecsched, Map chupdsched, Map chversched,
                             Map chexemptbo, Map chwowdep, Map chUcdTemp, Map chARTaskId, Node subTarget,
                             boolean isSubsReporterRequest, float clientVersion) {
        /** Outer nodes of channels and properties */
        Node chs = new Node(CHANNELS, "CHANNELS", null);
        Node props = new Node(PROPS, "PROPS", null);
        Node unInstallers = new Node(UNINSTALLERS, "UNINSTALLERS", null);
        StrString keyStr = null;
        StrString valueStr = null;
        StrString state1Str = null;
        StrString orderStr = null;
        StrString state2 = null;
        StrString sched1 = null;
        StrString sched2 = null;
        StrString schedupd = null;
        StrString schedver = null;
        StrString exbo = null;
        StrString wowdep = null;
        StrString ucdTemp = null;
        StrString exAR = null;

        // Store target DNs
        for (int i = 0; i < targetdns.size(); i++) {
            String lineStr = (String) targetdns.elementAt(i);
            Node ch = new Node(TARGET_DN, "TARGET_DN", null);
            ch.setProperty(new StrString("NAME"), new StrString(lineStr));
            subTarget.addNode(ch);
        }

        if (clientVersion >= 6.0) {
            if (modifytime.longValue() != LDAPEnv.NEVER) {
                Node tnode = new Node(TIMESTAMP, "TIMESTAMP", null);
                tnode.setProperty(new StrString("VALUE"), new StrString(modifytime.toString()));
                subTarget.addNode(tnode);
            }
        }

//        for (int j = 0; j < channels.size(); j++) {
//            String lineStr = (String) channels.elementAt(j);
//
//            // skip all 'none' states to save space in the file
//            if (lineStr.endsWith(ISubscriptionConstants.STATE_NONE)) {
//                if (DETAILED_INFO) {
//                    ctx.logToConsole("Skipping 'none' state");
//                }
//
//                continue;
//            }

            /**
             * Creating the channel Node. Also check for the presence of attributes such as install order, primary/secondary/update  and verifyrepair schedules
             * for this particular channel and save  them in the corresponding hashtable. If present then add it  as an attribute of the CHANNEL Node
             */
//            Hashtable chAttrs = null;
//            Node ch = new Node(CHANNEL, "CHANNEL", chAttrs);
//            int i = lineStr.indexOf("=");
//
//            if (i != -1) {
//                keyStr = new StrString(lineStr.substring(0, i));
//                String state1 = lineStr.substring(i + 1);
//
//                int idx = state1.indexOf(',');
//                if (idx > 0) {
//                    state1Str = new StrString(state1.substring(0, idx));
//                } else {
//                    state1Str = new StrString(state1);
//                }
//                orderStr = (StrString) chOrders.get(keyStr);
//                state2 = (StrString) channels2.get(keyStr);
//
//                if (clientVersion >= 5.0) {
//                    sched1 = (StrString) chinitsched.get(keyStr);
//                    sched2 = (StrString) chsecsched.get(keyStr);
//                    schedupd = (StrString) chupdsched.get(keyStr);
//                    schedver = (StrString) chversched.get(keyStr);
//                }
//
//                if (clientVersion >= 6.0) {
//                    exbo = (StrString) chexemptbo.get(keyStr);
//                }
//
//                if (clientVersion >= 7.0) {
//                    exAR = (StrString) chARTaskId.get(keyStr);
//                    wowdep = (StrString) chwowdep.get(keyStr);
//                }
//
//                if (clientVersion >= 8.3) {
//                    ucdTemp = (StrString) chUcdTemp.get(keyStr);
//                }
//
//                if (DETAILED_INFO) {
//                    ctx.logToConsole(lineStr);
//                }
//            }
//
//            if (keyStr != null) {
//                ch.setProperty(new StrString("URL"), keyStr);
//
//                if (clientVersion >= 5.0) {
//                    // Write the secondary schedule only when the Sub. Service
//                    // client is 5.0
//                    ch.setProperty(new StrString("STATE"), state1Str);
//
//                    if ((state2 != null) && (state2.length() != 0)) {
//                        ch.setProperty(new StrString("STATE2"), state2);
//                    }
//                } else {
//                    if ((state2 != null) && (state2.length() != 0)) {
//                        ch.setProperty(new StrString("STATE"), state2);
//                    } else {
//                        ch.setProperty(new StrString("STATE"), state1Str);
//                    }
//                }
//            }
//
//            if ((orderStr != null) && (orderStr.length() != 0)) {
//                ch.setProperty(new StrString("ORDER"), orderStr);
//            }
//
//            if (clientVersion >= 5.0) {
//                // Schedules are written only when the
//                // Sub. Service client is 5.0
//                if ((sched1 != null) && (sched1.length() != 0)) {
//                    ch.setProperty(new StrString("SCHED"), sched1);
//                }
//
//                if ((sched2 != null) && (sched2.length() != 0)) {
//                    ch.setProperty(new StrString("SCHED2"), sched2);
//                }
//
//                if ((schedupd != null) && (schedupd.length() != 0)) {
//                    ch.setProperty(new StrString("UPDATESCHED"), schedupd);
//                }
//
//                if ((schedver != null) && (schedver.length() != 0)) {
//                    ch.setProperty(new StrString("VERREPAIRSCHED"), schedver);
//                }
//            }
//
//            if (clientVersion >= 6.0) {
//                if ((exbo != null) && (exbo.length() != 0)) {
//                    ch.setProperty(new StrString("EXEMPTBLACKOUT"), exbo);
//                }
//            }
//
//            if (clientVersion >= 7.0) {
//                if ((exAR != null) && (exAR.length() != 0)) {
//                    ch.setProperty(new StrString("ARTASKID"), exAR);
//                }
//                if ((wowdep != null) && (wowdep.length() != 0)) {
//                    ch.setProperty(new StrString("WOWDEPLOYMENT"), wowdep);
//                }
//            }
//
//            if (clientVersion >= 8.3) {
//                if ((ucdTemp != null) && (ucdTemp.length() != 0)) {
//                    ch.setProperty(new StrString("DEVICE_LEVEL"), ucdTemp);
//                }
//            }
//
//
//            keyStr = null;
//            state1Str = null;
//            orderStr = null;
//
//            // Adding the CHANNEL node to the CHANNELS node
//            chs.addNode(ch);
//        }
        for (int k = 0; k < properties.size(); k++) {
            String lineStr = (String) properties.elementAt(k);
            // Do not send keychain entries to Subscription Reporter
            if (isSubsReporterRequest) {
                if (lineStr != null) {
                    if (ProtectProperties.isProtectedProperty(lineStr)) {
                        continue;
                    }
                }
            }

            /** Creating the property Node */
            Hashtable propAttrs = null;
            Node prop = new Node(PROP, "PROP", propAttrs);
            String key = null;
            String value = null;
            int i = lineStr.indexOf("=");

            if (i != -1) {
                key = lineStr.substring(0, i);
                value = (lineStr.substring(i + 1));

                if (DETAILED_INFO) {
                    ctx.logToConsole(lineStr);
                }
            }

            if (key != null) {
                StrString typeStr = null;
                StrString priorityStr = null;

                i = key.indexOf(",");

                if (i != -1) {
                    keyStr = new StrString(key.substring(0, i));
                    typeStr = new StrString(key.substring(i + 1));
                } else {
                    keyStr = new StrString(key);
                    typeStr = new StrString("");
                }

                if (value != null) {
                    i = value.lastIndexOf(",");

                    if (i != -1) {
                        valueStr = new StrString(value.substring(0, i));
                        priorityStr = new StrString(value.substring(i + 1));
                        // if the priority is not an interger, then we should concatenate that to actual value.
                        // this may occur when we give a property like marimba.subscription.values=x,y
                        try {
                            Integer.parseInt(priorityStr.toString());
                        } catch (NumberFormatException nfe) {
                            valueStr = new StrString(value);
                            priorityStr = new StrString(MAX_PROPERTY_PRIORITY);
                        }
                    } else {
                        valueStr = new StrString(value);
                        priorityStr = new StrString(MAX_PROPERTY_PRIORITY);
                    }
                }

                if(null != typeStr && "scap".equals(typeStr.toString()) && null != keyStr && SCAP_SECURITY_TEMPLATE_NAME.equals(keyStr.toString())) {
                	String selectedTemplateNames = (valueStr == null) ? null : valueStr.toString();
                	ctx.logToConsole("Adding templates " + selectedTemplateNames);
                	if(null != selectedTemplateNames) {
                		String[] values = selectedTemplateNames.split(";");
                        for (String propValue : values) {
                        	scapCustomTemplates.add(propValue);
                        }
                	}
                }
                if(null != typeStr && "usgcb".equals(typeStr.toString()) && null != keyStr && USGCB_SECURITY_TEMPLATE_NAME.equals(keyStr.toString())) {
                	String selectedTemplateNames = (valueStr == null) ? null : valueStr.toString();
                	ctx.logToConsole("Adding templates " + selectedTemplateNames);
                	if(null != selectedTemplateNames) {
                		String[] values = selectedTemplateNames.split(";");
                        for (String propValue : values) {
                        	usgcbCustomTemplates.add(propValue);
                        }
                	}
                }
                if(null != typeStr && "custom".equals(typeStr.toString()) && null != keyStr && CUSTOM_SECURITY_TEMPLATE_NAME.equals(keyStr.toString())) {
                	String selectedTemplateNames = (valueStr == null) ? null : valueStr.toString();
                	ctx.logToConsole("Adding templates " + selectedTemplateNames);
                	if(null != selectedTemplateNames) {
                		String[] values = selectedTemplateNames.split(";");
                        for (String propValue : values) {
                        	customCustomTemplates.add(propValue);
                        }
                	}
                }
                if((null != typeStr && "scap".equals(typeStr.toString())) || (null != typeStr && "usgcb".equals(typeStr.toString())) 
                		|| (null != typeStr && "custom".equals(typeStr.toString()))) {
	                prop.setProperty(new StrString("TYPE"), typeStr);
	                prop.setProperty(new StrString("KEY"), keyStr);
	                prop.setProperty(new StrString("VALUE"), valueStr);
	                prop.setProperty(new StrString("ORDER"), priorityStr);
                }
            }
            keyStr = null;
            valueStr = null;

            // Adding the PROP node to the PROPS node
            props.addNode(prop);
        }
//        if (clientVersion >= 8.1) {
//            for (int k = 0; k < softwares.size(); k++) {
//                String lineStr = (String) softwares.elementAt(k);
//
//                /** Creating software Node */
//                Hashtable sftAttrs = null;
//                Node software = new Node(SOFTWARE, "SOFTWARE", sftAttrs);
//                StrString key = null;
//                StrString value = null;
//                int i = lineStr.indexOf("=");
//
//                if (i != -1) {
//                    key = new StrString(lineStr.substring(0, i));
//                    value = new StrString((lineStr.substring(i + 1)));
//
//                    if (DETAILED_INFO) {
//                        ctx.logToConsole(lineStr);
//                    }
//                }
//
//                if (key != null) {
//                    software.setProperty(new StrString("TITLE"), key);
//                    software.setProperty(new StrString("PATH"), value);
//                }
//
//                // Adding the SOFTWARE node to the UNINSTALLERS node
//                unInstallers.addNode(software);
//            }
//        }
        // Adding the CHANNELS and the PROPS node to the SUB_TARGET node
        //subTarget.addNode(chs);
        subTarget.addNode(props);

//        if (clientVersion >= 8.1) {
//            subTarget.addNode(unInstallers);
//        }
    }

    public boolean isNoReadAccess() {
        return ((channels == null || channels.size() == 0) && !readAccess);
    }
}

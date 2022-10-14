// Copyright 2018, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.utils;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.TargetResHelper;
import com.marimba.apps.subscription.common.intf.ILDAPDataSourceContext;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.beans.SecurityTargetDetailsBean;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils;
import com.marimba.intf.admin.IUserDirectory;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPLocalException;
import com.marimba.webapps.intf.SystemException;

import javax.naming.NamingException;
import java.util.*;

public class SecurityLDAPUtils implements ISubscriptionConstants {

    public static List<SecurityTargetDetailsBean> getAssignedPolicy(String name, String type, String id, SubscriptionMain main, IUser user) {
        return getAssignedPolicy(null, name, type, id, main, user);
    }

    public static List<SecurityTargetDetailsBean> getAssignedPolicy(String xmlType, String name, String type, String id, SubscriptionMain main, IUser user) {
        List<SecurityTargetDetailsBean> policyBean = new ArrayList<SecurityTargetDetailsBean>();
        try {
            List<Target> targetList = getAssingedTargetDetails(id, name, type, main, user);
            for(Target assignedTarget : targetList) {
                debug("Get Assigned Policy: targetList.getName() = " + assignedTarget.getName());
                debug("Get Assigned Policy: targetList.getType() = " + assignedTarget.getType());
                debug("Get Assigned Policy: targetList.getId() = " + assignedTarget.getId());

                ISubscription sub = ObjectManager.openSubForRead(assignedTarget.getId(), assignedTarget.getType(), user);

                // loading scap security policy details
                String scapContentTile = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
                if(null != scapContentTile && !"".equals(scapContentTile)) {
                    String contentFilesNamesProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
                    String contentTitlesProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
                    String contentIdsProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID));
                    String profileIdsProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID));
                    String profileTitlesProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
                    String customTemplateName = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_TEMPLATE_NAME));
                    Map<String, String> contentTitlesMap = null;
                    Map<String, String> standardMap = null;
                    Map<String, String> customizeMap = null;
                    
                    customizeMap = getCustomContentsMap(contentTitlesProps);
                    standardMap = getStandardContentsMap(contentTitlesProps);
                    Map<String, Map<String, String>> listAssignments = new HashMap<String, Map<String, String>>();
                    if(customizeMap.size() > 0) {
                    	listAssignments.put("customize", customizeMap);
                    }
                    if(standardMap.size() > 0) {
                    	listAssignments.put("standard", standardMap);
                    }
                    for(String scapOption : listAssignments.keySet()) {
                    	contentTitlesMap = listAssignments.get(scapOption);
	                    for (String contentTitle : contentTitlesMap.keySet()) {
	                        if(null != contentTitle) {
	                            String templateName = "";
	                            Map<String, Map<String, String>> profilesMap = getProfilesMap(contentTitle, profileIdsProps, profileTitlesProps);
	                            Map<String, String> profileMap = profilesMap.get(contentTitle);
	                            Map<String, Map<String, String>> contentsMap = getContentsMap(contentTitle, contentIdsProps, contentFilesNamesProps);
	                            Map<String, String> contentMap = contentsMap.get(contentTitle);
	                            String contentId = null;
                                String contentFileName = null;
	                            if (null != contentMap) {
	                                contentId = contentMap.get("contentId");
                                    contentFileName = contentMap.get("contentFileName");
	                            }
	                            String selectedContentName = contentTitle;
	                            if("customize".equalsIgnoreCase(scapOption)) {
	                                templateName = contentTitle;
	                                selectedContentName = contentTitlesMap.get(contentTitle);
	                            }
	                            debug("Content title :" + selectedContentName);
	                            String profileId = profileMap.get("profileId");
	                            debug("Profile Id :" + profileId);
	                            debug("contentId :" + contentId);
                                debug("contentFileName :" + contentFileName);
	                            debug("Policy available: targetList.getId() = " + assignedTarget.getId());
                                debug("xmlTypeRequired :" + xmlType + ", xmlTypeActual :" + SCAPUtils.getSCAPUtils().getXMLContentType(contentFileName));
                                if (((xmlType != null)) && (!xmlType.equals(SCAPUtils.getSCAPUtils().getXMLContentType(contentFileName)))) {
                                    continue;
                                }
                                SecurityTargetDetailsBean scapDetilsBean = new SecurityTargetDetailsBean();
	                            scapDetilsBean.setSelectedSecurityContentName(selectedContentName);
	                            scapDetilsBean.setSelectedProfileId(profileId);
	                            scapDetilsBean.setCategoryType("scap");
	                            if (null != contentId) {
	                                scapDetilsBean.setSelectedContentId(contentId);
	                            }
                                if (null != contentFileName) {
                                    scapDetilsBean.setSelectedContentFileName(contentFileName);
                                }
	                            scapDetilsBean.setCustomTemplateName(templateName);
	                            scapDetilsBean.setAssginedToName(assignedTarget.getName());
	                            scapDetilsBean.setAssignedToID(assignedTarget.getId());
	                            scapDetilsBean.setAssginedToType(assignedTarget.getType());
	                            scapDetilsBean.setTargetID(id);
	                            scapDetilsBean.setTargetName(name);
	                            scapDetilsBean.setTargetType(type);
                                scapDetilsBean.setXmlType(xmlType);
	                            policyBean.add(scapDetilsBean);
	                        }
	                    }
	                }
                }

                // loading scap(WINDOWS) security policy details
                String usgcbContentTitle = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
                if(null != usgcbContentTitle && !"".equals(usgcbContentTitle)) {
                    String contentFilesNamesProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
                    String contentTitlesProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
                    String contentIdsProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID));
                    String profileIdsProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID));
                    String profileTitlesProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
                    String customTemplateName = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_TEMPLATE_NAME));
                    Map<String, String> contentTitlesMap = null;
                    Map<String, String> standardMap = null;
                    Map<String, String> customizeMap = null;
                    
                    customizeMap = getCustomContentsMap(contentTitlesProps);
                    standardMap = getStandardContentsMap(contentTitlesProps);
                    Map<String, Map<String, String>> listAssignments = new HashMap<String, Map<String, String>>();
                    if(customizeMap.size() > 0) {
                    	listAssignments.put("customize", customizeMap);
                    }
                    if(standardMap.size() > 0) {
                    	listAssignments.put("standard", standardMap);
                    }
                    for(String usgcbOption : listAssignments.keySet()) {
                    	contentTitlesMap = listAssignments.get(usgcbOption);
	                    for (String contentTitle : contentTitlesMap.keySet()) {
	                        if(null != contentTitle) {
	                            String templateName = "";
	                            Map<String, Map<String, String>> profilesMap = getProfilesMap(contentTitle, profileIdsProps, profileTitlesProps);
	                            Map<String, String> profileMap = profilesMap.get(contentTitle);
	                            Map<String, Map<String, String>> contentsMap = getContentsMap(contentTitle, contentIdsProps, contentFilesNamesProps);
	                            Map<String, String> contentMap = contentsMap.get(contentTitle);
	                            String contentId = null;
                                String contentFileName = null;
                                if (null != contentMap) {
                                    contentId = contentMap.get("contentId");
                                    contentFileName = contentMap.get("contentFileName");
                                }
	                            String selectedContentName = contentTitle;
	                            if("customize".equalsIgnoreCase(usgcbOption)) {
	                                templateName = contentTitle;
	                                selectedContentName = contentTitlesMap.get(contentTitle);
	                            }
	                            debug("Content title :" + selectedContentName);
	                            String profileId = profileMap.get("profileId");
	                            debug("Profile Id :" + profileId);
	                            debug("contentId :" + contentId);
                                debug("contentFileName :" + contentFileName);
	                            debug("Policy available: targetList.getId() = " + assignedTarget.getId());
                                debug("xmlTypeRequired :" + xmlType + ", xmlTypeActual :" + SCAPUtils.getSCAPUtils().getXMLContentType(contentFileName));
                                if (((xmlType != null)) && (!xmlType.equals(SCAPUtils.getSCAPUtils().getXMLContentType(contentFileName)))) {
                                    continue;
                                }
	                            SecurityTargetDetailsBean scapDetilsBean = new SecurityTargetDetailsBean();
	                            scapDetilsBean.setSelectedSecurityContentName(selectedContentName);
	                            scapDetilsBean.setSelectedProfileId(profileId);
	                            scapDetilsBean.setCategoryType("usgcb");
	                            if (null != contentId) {
	                                scapDetilsBean.setSelectedContentId(contentId);
	                            }
                                if (null != contentFileName) {
                                    scapDetilsBean.setSelectedContentFileName(contentFileName);
                                }
	                            scapDetilsBean.setCustomTemplateName(templateName);
	                            scapDetilsBean.setAssginedToName(assignedTarget.getName());
	                            scapDetilsBean.setAssignedToID(assignedTarget.getId());
	                            scapDetilsBean.setAssginedToType(assignedTarget.getType());
	                            scapDetilsBean.setTargetID(id);
	                            scapDetilsBean.setTargetName(name);
	                            scapDetilsBean.setTargetType(type);
                                scapDetilsBean.setXmlType(xmlType);
	                            policyBean.add(scapDetilsBean);
	                        }
	                    }
	                }
                }
                // loading custom security policy details
                String customContentTitle = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
                if(null != customContentTitle && !"".equals(customContentTitle)) {
                    String contentFilesNamesProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_FILENAME));
                    String contentTitlesProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_TITLE));
                    String contentIdsProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID));
                    String profileIdsProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID));
                    String profileTitlesProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
                    String customTemplateName = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_TEMPLATE_NAME));
                    Map<String, String> contentTitlesMap = null;
                    Map<String, String> standardMap = null;
                    Map<String, String> customizeMap = null;
                    
                    customizeMap = getCustomContentsMap(contentTitlesProps);
                    standardMap = getStandardContentsMap(contentTitlesProps);
                    Map<String, Map<String, String>> listAssignments = new HashMap<String, Map<String, String>>();
                    if(customizeMap.size() > 0) {
                    	listAssignments.put("customize", customizeMap);
                    }
                    if(standardMap.size() > 0) {
                    	listAssignments.put("standard", standardMap);
                    }
                    for(String customOption : listAssignments.keySet()) {
                    	contentTitlesMap = listAssignments.get(customOption);
	                    for (String contentTitle : contentTitlesMap.keySet()) {
	                        if(null != contentTitle) {
	                            String templateName = "";
	                            Map<String, Map<String, String>> profilesMap = getProfilesMap(contentTitle, profileIdsProps, profileTitlesProps);
	                            Map<String, String> profileMap = profilesMap.get(contentTitle);
	                            Map<String, Map<String, String>> contentsMap = getContentsMap(contentTitle, contentIdsProps, contentFilesNamesProps);
	                            Map<String, String> contentMap = contentsMap.get(contentTitle);
	                            String contentId = null;
                                String contentFileName = null;
                                if (null != contentMap) {
                                    contentId = contentMap.get("contentId");
                                    contentFileName = contentMap.get("contentFileName");
                                }
	                            String selectedContentName = contentTitle;
	                            if("customize".equalsIgnoreCase(customOption)) {
	                                templateName = contentTitle;
	                                selectedContentName = contentTitlesMap.get(contentTitle);
	                            }
	                            debug("Content title :" + selectedContentName);
	                            String profileId = profileMap.get("profileId");
	                            debug("Profile Id :" + profileId);
	                            debug("contentId :" + contentId);
                                debug("contentFileName :" + contentFileName);
	                            debug("Policy available: targetList.getId() = " + assignedTarget.getId());
                                debug("xmlTypeRequired :" + xmlType + ", xmlTypeActual :" + SCAPUtils.getSCAPUtils().getXMLContentType(contentFileName));
                                if (((xmlType != null)) && (!xmlType.equals(SCAPUtils.getSCAPUtils().getXMLContentType(contentFileName)))) {
                                    continue;
                                }
	                            SecurityTargetDetailsBean scapDetilsBean = new SecurityTargetDetailsBean();
	                            scapDetilsBean.setSelectedSecurityContentName(selectedContentName);
	                            scapDetilsBean.setSelectedProfileId(profileId);
	                            scapDetilsBean.setCategoryType("custom");
	                            if (null != contentId) {
	                                scapDetilsBean.setSelectedContentId(contentId);
	                            }
                                if (null != contentFileName) {
                                    scapDetilsBean.setSelectedContentFileName(contentFileName);
                                }
	                            scapDetilsBean.setCustomTemplateName(templateName);
	                            scapDetilsBean.setAssginedToName(assignedTarget.getName());
	                            scapDetilsBean.setAssignedToID(assignedTarget.getId());
	                            scapDetilsBean.setAssginedToType(assignedTarget.getType());
	                            scapDetilsBean.setTargetID(id);
	                            scapDetilsBean.setTargetName(name);
	                            scapDetilsBean.setTargetType(type);
                                scapDetilsBean.setXmlType(xmlType);
	                            policyBean.add(scapDetilsBean);
	                        }
	                    }
                    }
                }

            }
        } catch(Exception ed) {
            ed.printStackTrace();
        }
        return policyBean;
    }
    private static String getPropertyValue( String probandval ){

        int indexVal;

        if( null != probandval ) {
            indexVal = probandval.lastIndexOf( PROP_DELIM );
            if( indexVal != -1) {
                probandval = probandval.substring( 0, indexVal );
            }
        }
        else {
            probandval = "";
        }
        return probandval;
    }
    public static List<Target> getAssingedTargetDetails(String targetId, String targetName, String targetType, SubscriptionMain main, IUser user) {
        List<Target> targetList = new ArrayList<Target>();
        try {
            boolean browsingLDAP = main.getUsersInLDAP();
            TargetResHelper targetDNs = getGroupMembershipTargets(targetId, targetType, main, browsingLDAP, user);
            if(null != targetDNs) {
                ILDAPDataSourceContext ldapCtx = main.getLDAPDataSourceContext(user);
                LDAPConnection browseConn = ldapCtx.getBrowseConn();
                Map targetDNMap = targetDNs.getTargetsMap();
                for (Object o : targetDNMap.keySet()) {
                    String id = (String) o;
                    String type = (String) targetDNMap.get(id);
                    debug("Target Id :" + id);
                    String name = LDAPUtils.getCnFromDn(id);
                    debug("Target Name :" + name);
                    type = LDAPUtils.getObjectClass(id, browseConn, main.getLDAPVarsMap(), main.getTenantName(), main.getChannel());
                    debug("Target object class Type :" + type);
                    try {
                        type = LDAPUtils.objClassToTargetType(type, main.getLDAPVarsMap());                        
                    } catch (Exception ed) {
                    	type = TYPE_CONTAINER; // set default target type
                    }
                    debug("Target type :" + type);
                    if (null != id && null != name && null != type) {
                        Target tgt = new Target(name, type, id);
                        targetList.add(tgt);
                    }
                }
            }
            // adding AllEndpoints all_all
            Target tgt = new Target("All Endpoints", "all", "all");
            targetList.add(tgt);

            // loading selected target
            //if (!TYPE_ALL.equals(targetType) && !TYPE_DOMAIN.equals(targetType) && !TYPE_SITE.equals(targetType)) {
            if (!TYPE_ALL.equals(targetType)) {
                Target seltgt = new Target(targetName, targetType, targetId);
                targetList.add(seltgt);
            }

        } catch(Exception ed) {
            ed.printStackTrace();
        }
        return targetList;
    }
    static TargetResHelper getGroupMembershipTargets(String targetId, String targetType, SubscriptionMain main, boolean browsingLDAP, IUser user) throws Exception {
        TargetResHelper targetDNs = null;
        try {
            ILDAPDataSourceContext ldapCtx = main.getLDAPDataSourceContext(user);
            IProperty subConfig = main.getSubscriptionConfig();
            String childContainer = main.getSubBaseWithNamespace(ldapCtx);

            IUserDirectory userDirectory = null;

//				if (!browsingLDAP && (TYPE_USER.equals(targetType) || TYPE_USERGROUP.equals(targetType))) {
//					try {
//						userDirectory = main.getUserDirectoryTx((HttpServletRequest) pageContext.getRequest());
//					} catch (Exception ed1) {
//						ed1.printStackTrace();
//					}
//				}


            // do group and resolution if its mot a domain and its not target type ALL
            if (!TYPE_ALL.equals(targetType) && !TYPE_DOMAIN.equals(targetType) && !TYPE_SITE.equals(targetType)) {
                try {
                    targetDNs = getGroupsAndContainers(targetId, targetType, subConfig, userDirectory, ldapCtx, childContainer, browsingLDAP, main);
                } catch (LDAPException e) {
                    //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        } catch(Exception ed) {
            // ed.printStackTrace();
        }
        return targetDNs;
    }

    static TargetResHelper getGroupsAndContainers(String targetID, String targetType,
                                                  IProperty subConfig, IUserDirectory userDirectory,
                                                  ILDAPDataSourceContext ldapCtx, String childContainer,
                                                  boolean usersInLdap, SubscriptionMain main) throws LDAPException, LDAPLocalException,
            NamingException {
        Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
        TargetResHelper targetDNs = new TargetResHelper(usersInLdap, LDAPVarsMap);
        LDAPConnection subConn = ldapCtx.getSubConn();
        // get all DNs for the user and machineName
        String[] memberOfattr = new String[1];
        memberOfattr[0] = LDAPVarsMap.get("MEMBEROF");

        String[] resultDNs = null;

        if (usersInLdap	|| (!usersInLdap && (TYPE_MACHINEGROUP.equals(targetType) || TYPE_COLLECTION.equals(targetType)))) {
            // A search for the group ID should only be done in LDAP if
            // users are not sourced from the transmitter
            resultDNs = new String[1];
            resultDNs[0] = targetID;
            debug("targetID = " + targetID);
        }

        long starttime = System.currentTimeMillis();
        LDAPEnv ldapEnv = main.getLDAPEnv();

        // Resolve group membership
        // get the domain only for the child container
        // String domainDN =
        // LDAPConnUtils.getDomainDNFromDN(ldapCtx.getSubConn(),
        // childContainer);

        // Convert into the DNS format
        // String subDomain =
        // LDAPConnUtils.getDomainFromDN(ldapCtx.getSubConn(), domainDN);

        Set groupDNs = ldapEnv.getGroupMembership(resultDNs, ldapCtx, ldapCtx.getConnectionCache());
        // We default to USERGROUP for all groups from LDAP
        for (Iterator ite = groupDNs.iterator(); ite.hasNext();) {
            String dn = (String) ite.next();
            targetDNs.add(dn, ISubscriptionConstants.TYPE_USERGROUP);
        }

        debug("Finish resolving group membership");

        if (usersInLdap	|| (!usersInLdap && (TYPE_MACHINEGROUP.equals(targetType) || TYPE_COLLECTION.equals(targetType)))) {
            Map containersMap;
            HashSet iSet = new HashSet(targetDNs.getTargetsSet());
            iSet.add(targetID);
            containersMap = ldapEnv.getEnclosingContainers(subConn, iSet);
            debug("containerersMap.size() " + containersMap.size());
            targetDNs.addAll(containersMap);
        }

        debug("group resolution results " + targetDNs.getTargetsMap().size());

        // add all user/group targets that point to user/groups
        // as seen from the Transmitter, i.e. via IUserDirectory
//		if ((!usersInLdap) && (userDirectory != null)) {
//			if (TYPE_USER.equals(targetType)) {
//				DEBUGTIME(starttime, "Start search for user from Tx "
//						+ targetID);
//				targetDNs.add(targetID, TYPE_USER);
//
//				String[] groups = userDirectory.listGroups(LDAPSearchFilter
//						.escapeComponentValue(targetID));
//
//				if (groups != null) {
//					if (groups.length > 0) {
//						for (int i = 0; i != groups.length; i++) {
//							targetDNs.add(groups[i], TYPE_USERGROUP);
//						}
//					}
//				}
//			} else if (TYPE_USERGROUP.equals(targetType)) {
//				targetDNs.add(targetID, TYPE_USERGROUP);
//			}
//		}

        debug("number of groups and containers for search = " + targetDNs.getTargetsMap().size());
        return targetDNs;
    }
    private static Map<String, String> getContentsMap(String propValues) {
        Map<String, String> contentMap = new HashMap<String, String>();
        if (null == propValues || propValues.trim().isEmpty()) return contentMap;
        String[] values = propValues.split(";");
        for (String propValue : values) {
            String[] tmpArr = propValue.split("::");
            if (tmpArr.length != 2) continue;
            String contentTitle = tmpArr[0];
            String contentFileName = tmpArr[1];
            contentMap.put(contentTitle, contentFileName);
        }
        debug("contentMap = " + contentMap);
        return contentMap;
    }
    private static Map<String, String> getCustomContentsMap(String propValues) {
        Map<String, String> contentMap = new HashMap<String, String>();
        if (null == propValues || propValues.trim().isEmpty()) return contentMap;
        String[] values = propValues.split(";");
        for (String propValue : values) {
            String[] tmpArr = propValue.split("::");
            if (tmpArr.length != 2) continue;
            String templateName = tmpArr[0];
            String contentTitle = tmpArr[1];
            contentMap.put(templateName, contentTitle);
        }
        debug("customize contentMap = " + contentMap);
        return contentMap;
    }
    private static Map<String, String> getStandardContentsMap(String propValues) {
        Map<String, String> contentMap = new HashMap<String, String>();
        if (null == propValues || propValues.trim().isEmpty()) return contentMap;
        String[] values = propValues.split(";");
        for (String propValue : values) {
            String[] tmpArr = propValue.split("::");
            if (tmpArr.length != 2) {
	            String contentTitle = propValue;
	            String contentTitleStr = propValue;
	            contentMap.put(contentTitle, contentTitleStr);
            }
        }
        debug("contentMap = " + contentMap);
        return contentMap;
    }
    private static Map<String, Map<String, String>> getContentsMap(String contentTitle, String contentIdsProps, String contentFileNamesProps) {
        Map<String, Map<String, String>> contentsMap = new HashMap<String, Map<String, String>>();
        String contentId = null;
        String contentFileName = null;

        String[] contentIds = contentIdsProps.split(";");
        for (String aContentId : contentIds) {
            String[] tmpArr = aContentId.split("::");
            if (tmpArr.length != 2) continue;
            if (tmpArr[0].equals(contentTitle)) {
                contentId = tmpArr[1];
                break;
            }
        }
        String[] contentFileNames = contentFileNamesProps.split(";");
        for (String aContentFileName : contentFileNames) {
            String[] tmpArr = aContentFileName.split("::");
            if (tmpArr.length != 2) continue;
            if (tmpArr[0].equals(contentTitle)) {
                contentFileName = tmpArr[1];
                break;
            }
        }
        if (null != contentId && null != contentFileName) {
            Map<String, String> details = new HashMap<String, String>();
            details.put("contentFileName", contentFileName);
            details.put("contentId", contentId);
            contentsMap.put(contentTitle, details);
        }
        debug("contentsMap = " + contentsMap);
        return contentsMap;
    }

    private static Map<String, Map<String, String>> getProfilesMap(String contenantTitle, String profileIdProps, String profileTitleProps) {
        Map<String, Map<String, String>> profilesMap = new HashMap<String, Map<String, String>>();
        String profileId = null;
        String profileTitle = null;

        String[] profileIds = profileIdProps.split(";");
        for (String aProfileId : profileIds) {
            String[] tmpArr = aProfileId.split("::");
            if (tmpArr.length != 2) continue;
            if (tmpArr[0].equals(contenantTitle)) {
                profileId = tmpArr[1];
                break;
            }
        }
        String[] profileTitles = profileTitleProps.split(";");
        for (String aProfileTitle : profileTitles) {
            String[] tmpArr = aProfileTitle.split("::");
            if (tmpArr.length != 2) continue;
            if (tmpArr[0].equals(contenantTitle)) {
                profileTitle = tmpArr[1];
                break;
            }
        }
        if (null != profileId && null != profileTitle) {
            Map<String, String> details = new HashMap<String, String>();
            details.put("profileTitle", profileTitle);
            details.put("profileId", profileId);
            profilesMap.put(contenantTitle, details);
        }
        debug("profilesMap = " + profilesMap);
        return profilesMap;
    }


    public static Map<String, String> getContentDetailsOfTarget(Target target, SubscriptionMain main, IUser user) {
        Map<String, String> contentDetails = new TreeMap<String, String>(); // Key -> content title, Value -> content id
        try {
            List<Target> targetList = getAssingedTargetDetails(target.getId(), target.getName(), target.getType(), main, user);
            for(Target assignedTarget : targetList) {
                debug("Get Assigned Policy: targetList.getName() = " + assignedTarget.getName());
                debug("Get Assigned Policy: targetList.getType() = " + assignedTarget.getType());
                debug("Get Assigned Policy: targetList.getId() = " + assignedTarget.getId());
                ISubscription sub = ObjectManager.openSubForRead(assignedTarget.getId(), assignedTarget.getType(), user);
                String scapContentFilesNamesProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDCONTENT_ID));
                String usgcbpContentFilesNamesProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDCONTENT_ID));
                String customContentFilesNamesProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDCONTENT_ID));
                contentDetails.putAll(getContentsMap(scapContentFilesNamesProps));
                contentDetails.putAll(getContentsMap(usgcbpContentFilesNamesProps));
                contentDetails.putAll(getContentsMap(customContentFilesNamesProps));
            }
        } catch (SystemException syex) {
            syex.printStackTrace();
        }
        return contentDetails;
    }

    public static Map<String, String> getProfileDetailsOfContent(Target target, SubscriptionMain main, IUser user, String contentTitle) {
        Map<String, String> profileDetails = new TreeMap<String, String>(); // Key -> profile title, Value -> profile id
        try {
            List<Target> targetList = getAssingedTargetDetails(target.getId(), target.getName(), target.getType(), main, user);
            for(Target assignedTarget : targetList) {
                debug("Get Assigned Policy: targetList.getName() = " + assignedTarget.getName());
                debug("Get Assigned Policy: targetList.getType() = " + assignedTarget.getType());
                debug("Get Assigned Policy: targetList.getId() = " + assignedTarget.getId());
                ISubscription sub = ObjectManager.openSubForRead(assignedTarget.getId(), assignedTarget.getType(), user);
                String scapProfileIdProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDPROFILE_ID));
                String scapProfileTitleProps = getPropertyValue(sub.getProperty(PROP_SCAP_SECURITY_KEYWORD, SCAP_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
                String usgcbpProfileIdProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID));
                String usgcbpProfileTitleProps = getPropertyValue(sub.getProperty(PROP_USGCB_SECURITY_KEYWORD, USGCB_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
                String customProfileIdProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_ID));
                String customProfileTitleProps = getPropertyValue(sub.getProperty(PROP_CUSTOM_SECURITY_KEYWORD, CUSTOM_SECURITY_SCAP_SELECTEDPROFILE_TITLE));
                Map<String, Map<String, String>> usgcbProfiles = getProfilesMap(contentTitle, usgcbpProfileIdProps, usgcbpProfileTitleProps);
                Map<String, Map<String, String>> scapProfiles = getProfilesMap(contentTitle, scapProfileIdProps, scapProfileTitleProps);
                Map<String, Map<String, String>> customProfiles = getProfilesMap(contentTitle, customProfileIdProps,customProfileTitleProps);
                if (!usgcbProfiles.isEmpty()) {
                    Map<String, String> details = usgcbProfiles.get(contentTitle);
                    profileDetails.put(details.get("profileId"), details.get("profileTitle"));
                }
                if (!scapProfiles.isEmpty()) {
                    Map<String, String> details = scapProfiles.get(contentTitle);
                    profileDetails.put(details.get("profileId"), details.get("profileTitle"));
                }
                if (!customProfiles.isEmpty()) {
                    Map<String, String> details = customProfiles.get(contentTitle);
                    profileDetails.put(details.get("profileId"), details.get("profileTitle"));
                }
            }
        } catch (SystemException syex) {
            syex.printStackTrace();
        }
        return profileDetails;
    }

    private static void debug(String msg) {
        if (IAppConstants.DEBUG) System.out.println("SecurityLDAPUtils: " + msg);
    }
}
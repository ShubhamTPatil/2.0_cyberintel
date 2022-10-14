// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPEnvAD;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.objects.dao.LDAPSubscription;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.users.User;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import javax.naming.NameAlreadyBoundException;
import com.marimba.intf.admin.IUserDirectory;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.intf.msf.acl.IAclConstants;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.ldap.*;
import com.marimba.tools.regex.Matcher;
import com.marimba.tools.util.QuotedTokenizer;
import com.marimba.webapps.intf.InternalException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.PropsBean;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.Collator;
import java.util.*;

/**
 * REMIND
 *
 * @author Theen-Theen Tan
 * @version $Revision$, $Date$
 */
public class LDAPWebappUtils implements ISubscriptionConstants, IErrorConstants, IWebAppConstants {
    // This version of parseLDAPResults is used by LDAPBrowOUAction when
    // paging through LDAP.   MemberListProcessor deals with browsing group
    // members, and search.  TxMemberListProcessor deals with listing users from
    // the Tx.
    // This is used with paged results
    // subBase is the subbase with prepended namespace.
    //   ex. ou=Namespace1, ou=Subscriptions, dc=marimba,dc=com
    // subBasePlain does not have prepended namespace
    //   ex. ou=Subscriptions,dc=marimba,dc=com
	static String[]  containerAttrs = { "showInAdvancedViewOnly: FALSE", "objectclass: top", "objectclass: organizationalUnit" };
    public static Vector parseLDAPResults(HttpServletRequest req,
                                          List list,
                                          String searchBase,
                                          SubscriptionMain main, ITenant tenant)
            throws SystemException {
        Vector results = new Vector(100);

        if (DEBUG2) {
            System.out.println("in parseLDAPResults(List, ..)");
        }

        boolean usersInLDAP = main.getUsersInLDAP();
        try {
            if (!list.isEmpty()) {
                LDAPConnection conn = getBrowseConn(req);

                // parser is used for dn comparison to determine
                // if container should be hidden or not.
                LDAPName ldapName = conn.getParser();
                String subBase = main.getSubBase();
                searchBase = searchBase.trim();
                String vendor = main.getLDAPProperty(LDAPConstants.PROP_VENDOR);
                boolean isVendorAD = (LDAPConstants.VENDOR_AD.equals(vendor) ||
                        LDAPConstants.VENDOR_AD.equals(main.getLDAPProperty(vendor)));
            	Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(vendor);
                LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(req), subBase, main.getUsersInLDAP(), main.getDirType());

                int size = list.size();

                if (DEBUG3) {
                    System.out.println("list.size()= " + size);
                }

                for (int i = 0; i < size; i++) {
                    SearchResult sr = (SearchResult) list.get(i);
                    Attributes srAttrs = sr.getAttributes();

                    if (DEBUG3) {
                        System.out.println("srAttrs " + i + " = " + srAttrs);
                    }

                    String objectclass = null;

                    if (srAttrs.size() > 0) {
                        objectclass = LDAPUtils.getObjectClass(srAttrs, LDAPVarsMap, main.getTenantName(), main.getChannel());
                    }

                    if (objectclass == null) {
                        // the objectclass of the result was invalid, skip it
                        continue;
                    }

                    objectclass = objectclass.toLowerCase();

                    PropsBean entry = new PropsBean();
                    String dn = LDAPName.unescapeJNDISearchResultName(sr.getName().trim()) + "," + searchBase;
                    Vector desc = null;
                    desc = LDAPUtils.getValue(srAttrs, LDAPVarsMap.get("DESCRIPTION"), main.getTenantName(), main.getChannel());

                    // set the objectclass, displayname, desc and dn for each element
                    entry.setValue(LDAPConstants.OBJECT_CLASS, objectclass);
                    String type = LDAPUtils.objClassToTargetType(objectclass, LDAPVarsMap);
                    if (!usersInLDAP && TYPE_USER.equals(type)) {
                        type = "user_inactive";
                    }
                    entry.setValue("type", type);
                    entry.setValue(LDAPVarsMap.get("DN"), dn);
                    LDAPWebappUtils.setDisplayName(entry, srAttrs, main.getDirType(), main.getTenantName(), main.getChannel());
                    LDAPWebappUtils.setTargetAbleProperty(entry, usersInLDAP, LDAPVarsMap.get("USER_CLASS"));
                    LDAPWebappUtils.setExpandAbleProperty(entry, LDAPVarsMap);

                    if ((desc != null) && (desc.size() > 0)) {
                        entry.setValue(LDAPVarsMap.get("DESCRIPTION"), desc.lastElement());
                    }
                    boolean isHidden = false;

                    if (LDAPVarsMap.get("CONTAINER_CLASS").equals(objectclass) ||
                            LDAPVarsMap.get("CONTAINER_CLASS2").equals(objectclass) ||
                            LDAPConstants.DOMAIN_CLASS.equals(objectclass)) {
                        Name dnName = ldapName.parse(dn);

                        // hide certain folders
                        Name[] hiddenNames = main.getLDAPEnv().getHiddenEntries(conn);
                        for (int ind = 0; ind < hiddenNames.length; ind++) {
                            if (dnName.equals(hiddenNames[ind])) {
                                isHidden = true;
                            }
                        }
                    }
                    // need to get hidden property from msf.txt
                    IConfig tenantConfig = tenant.getConfig();
                    if(null != tenantConfig.getProperty("marimba.ldap.cloud.hideentries")) {
                    	String[] cloudHideEntries = getTokenizerProperty(tenantConfig.getProperty("marimba.ldap.cloud.hideentries"));
                    		if(null != cloudHideEntries) {
                    			Name inputDnName = ldapName.parse(dn);
                    			for(String cloudHideEntrie : cloudHideEntries) {
                    				Name hideName = ldapName.parse(cloudHideEntrie);
                    				if (inputDnName.equals(hideName)) {
                                        isHidden = true;
                                    }
                    		}
                    	}
                    }

                    if (isVendorAD && LDAPVarsMap.get("GROUP_CLASS").equals(objectclass)) {
                        try {
                            int groupType = Integer.parseInt(getBrowseConn(req).getAttrs(dn, "grouptype")[0]);
                            entry.setValue("scope", getGroupText(LDAPEnvAD.getGroupScope(groupType)));
                        } catch (NumberFormatException nfe) {
                            if (DEBUG) {
                                nfe.printStackTrace();
                            }
                        } catch (LDAPException le) {
                            if (DEBUG) {
                                le.printStackTrace();
                            }
                        }
                    }

                    // write scope
                    // only add entry if it isn't a Subscription obj or container
                    if (!isHidden) {
                        policyFinder.addTarget(dn, objectclass);
                        // insert domains at the top because
                        // data that we get is sorted by cn
                        if (LDAPConstants.DOMAIN_CLASS.equals(objectclass)) {
                            int currentIdx = 0;
                            int rsize = results.size();
                            String objClass;
                            String dispName;
                            PropsBean tentry;

                            while (currentIdx < rsize) {
                                tentry = (PropsBean) results.elementAt(currentIdx);
                                objClass = (String) tentry.getValue(LDAPConstants.OBJECT_CLASS);
                                dispName = (String) tentry.getValue(LDAPConstants.DISPLAY_NAME);

                                if (!objClass.equals(LDAPConstants.DOMAIN_CLASS) || (dispName.compareTo((String) entry.getValue(LDAPConstants.DISPLAY_NAME)) > 0)) {
                                    break;
                                }

                                currentIdx++;
                            }

                            results.insertElementAt(entry, currentIdx);
                        } else {
                            results.addElement(entry);
                        }
                    }
                }

                LDAPWebappUtils.setDirectTargetProperty(results, policyFinder, LDAPVarsMap);

            }
        } catch (NamingException ne) {
            if (DEBUG) {
                ne.printStackTrace();
            }

            LDAPUtils.classifyLDAPException(ne);
        }

        if (DEBUG3) {
            PropsBean entry;

            for (int i = 0; i < results.size(); i++) {
                entry = (PropsBean) results.elementAt(i);
                dumpProperties(entry, "parse LDAP results");
            }

            System.out.println("=============== end entry ================");
            System.out.println("result size is = " + results.size());
        }

        return results;
    }
    public static String[] getTokenizerProperty(String propertyValue) {
    	try {
    		if (propertyValue == null || "".equals(propertyValue.trim())) {
                return null;
            }
	    	Vector p = new Vector();
	        QuotedTokenizer tok = new QuotedTokenizer(propertyValue, ", ", '\\');
	        while (tok.hasMoreTokens()) {
	            p.addElement(tok.nextToken());
	        }
	        String[] propsList = new String[p.size()];
	        p.copyInto(propsList);
	        return propsList;
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
        return null;
    }
    public static String createContainerObject(LDAPConnection subConn, String dn, Map<String, String> LDAPVarsMap) {
    	
    	try {
            subConn.createObject(dn, containerAttrs, false);
            return dn;
    	} catch(NameAlreadyBoundException ed) {
    		return dn;
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
    }
    public static String getRelativeDN(String dn) {
    	if(null == dn || "".equals(dn.trim())) {
    		return null;
    	}
    	String relativeDN = null;
    	
    	try {
    		int index = dn.indexOf(",");
    		relativeDN = dn.substring(index+1);
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
    	return relativeDN;
    }
    /**
     * This method parses the results returned by a LDAP query. The results are interrogated for target type and a targetchannelmap object is created per
     * result set entry. The method also has logic to determine the target type if the target type attribute is not set (as in the case of pre-5.0 objects).
     * The displayname of the objects is also determined by looking at specific properties (additional comments in the method below) returns a vector
     * containing the targetChannelMap results.
     *
     * @param conn              REMIND
     * @param subConn           REMIND
     * @param searchResultsList REMIND
     * @param channelList       REMIND
     * @param subBase           REMIND
     * @param childContainer    REMIND
     * @return REMIND
     * @throws NamingException REMIND
     * @throws SystemException REMIND
     */
    public static Vector<TargetChannelMap> parseLDAPTargetResults(LDAPConnection conn,
                                                                  LDAPConnection subConn,
                                                                  List searchResultsList,
                                                                  List channelList,
                                                                  String subBase,
                                                                  String childContainer,
                                                                  String allEndpoints, Map<String, String> LDAPVarsMap)
            throws NamingException,
            SystemException {
        Vector<TargetChannelMap> results = new Vector<TargetChannelMap>(100);


        if (!searchResultsList.isEmpty()) {
            int size = searchResultsList.size();
            for (int i = 0; i < size; i++) {
                SearchResult sr = (SearchResult) searchResultsList.get(i);
                Attributes srAttrs = sr.getAttributes();
                // this is the subscription object name
                String targetDN = getTargetDN(srAttrs, LDAPVarsMap);
                String targetTypeAtt = LDAPVarsMap.get("TARGETTYPE");
                String subsNameAtt = LDAPVarsMap.get("SUBSCRIPTION_NAME");
                String collAtt = LDAPVarsMap.get("COLLECTION_CLASS");
                String targetType = getTargetType(srAttrs, targetTypeAtt);
                boolean sourceTx = isFromSourceTx(srAttrs, LDAPVarsMap);
                if (!TYPE_ALL.equals(targetDN) && !sourceTx && (!targetType.equalsIgnoreCase(TYPE_SITE) || !targetType.equalsIgnoreCase(MDM_TYPE_DEVICE_GROUP) || !targetType.equalsIgnoreCase(TYPE_DEVICE))) {
                    if (isValidTgt(conn,targetDN,srAttrs)){
                        continue;
                    }
                }
                targetType = getTargetType(conn, srAttrs, targetDN, targetTypeAtt, subsNameAtt, collAtt, LDAPVarsMap);
                String displayName = getTargetName(srAttrs, targetType, conn, allEndpoints, LDAPVarsMap);
                //targetDN is the displayname if users/groups are sourced from tx.
                if ("".equals(targetDN)) {
                    // The Target ID for a TXUSER or TXGROUP
                    // is an escaped version of the displayName
                    targetDN = LDAPName.escapeComponentValue(displayName);
                }

                // when the target is of external type
                // it is DESIRED to not show the name, but the entire DN in that case.
                if (LDAPVars.EXTERNAL_CLASS.equals(targetType)) {
                    displayName = targetDN;
                }

                String subName = LDAPName.unescapeJNDISearchResultName(sr.getName().trim());
                String resultChildContainer = null;
                Name subDN = subConn.getParser().parse(subName);
                subDN.remove(subDN.size() - 1);
                resultChildContainer = subDN.toString();

                boolean targettable = true;

                if (!TYPE_ALL.equals(targetType) && !TYPE_DOMAIN.equals(targetType)) {
                    // Reconstruct the result DN to include the Subscription base.
                    if (resultChildContainer.length() > 0) {
                        subDN = subConn.getParser().parse(resultChildContainer + "," + subBase);
                    } else {
                        subDN = subConn.getParser().parse(subBase);
                    }

                    Name subContainerDN = subConn.getParser().parse(childContainer);
                    targettable = subDN.equals(subContainerDN);
                }

                Target target = new Target(displayName, targetType, targetDN);
                TargetChannelMap entry = new TargetChannelMap(target, resultChildContainer);
                entry.setIsSelectedTarget(new Boolean(targettable).toString());

                Hashtable<String, Channel> channelTable = new Hashtable<String, Channel>();

                for (Object aChannelList : channelList) {
                    Channel c = (Channel) aChannelList;
                    channelTable.put(c.getUrl(), c);
                }

                // add the channels to the TargetChannelMap
                addChannels(entry, srAttrs, channelTable, LDAPVarsMap, conn.currentTenantName, conn.currentChannel);

                // In addChannels, if all the channels had none states,
                // there will not be any channel in the TargetChannelMap
                if (entry.getChannel() != null) {
                    results.addElement(entry);

                    if (DEBUG) {
                        System.out.println("=============== begin================");
                        System.out.println("target type : " + targetType);
                        System.out.println("subDN : " + subName);
                        System.out.println("name : " + entry.getName());
                        System.out.println("dn : " + entry.getId());
                        System.out.println("type :" + entry.getType());
                        System.out.println("=============== end ================");
                    }
                }
            }
        }

        return results;
    }

    /**
     * Checks whether the target is a deleted target based on the directory server

     * @param conn
     * @param targetDN
     * @param srAttrs
     * @return  boolean true if it is a deleted target else false
     */
    private static boolean isValidTgt(LDAPConnection conn,
                                      String targetDN,
                                      Attributes srAttrs)  {
        try {
            boolean isDeleted = false;
            String vendor = conn.getVendor();
            LDAPName ldapName = conn.getParser();
            Name deletedContainer = ldapName.parse("CN=Deleted Objects,"+conn.getBaseDN());
            Name currentTarget = null;
            currentTarget = ldapName.parse(targetDN);
            if ((vendor.equals(LDAPConstants.VENDOR_AD)) || (vendor.equals(LDAPConstants.VENDOR_ADAM) )) {
                isDeleted = currentTarget.startsWith(deletedContainer);
            } else if (vendor.equals(LDAPConstants.VENDOR_NS)) {
                if (!isTgtExists(conn, targetDN, srAttrs)) {
                    isDeleted = true;
                }
            }
            return isDeleted;
        } catch (NamingException e) {
            return true;
        }
    }
    /**
     * Evaluates whether the  target exists in the directory server
     * @param conn
     * @param targetDN
     * @param srAttrs
     * @return  boolean true if the target exists else false
     * @throws NamingException
     */

    private static boolean isTgtExists (LDAPConnection conn,
                                        String targetDN,
                                        Attributes srAttrs)
            throws NamingException {
        try {
            return conn.exists(targetDN, srAttrs);
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static String getTargetType(Attributes srAttrs, String targetType) {
        Attribute targetTypeAttr = srAttrs.get(targetType);

        if (targetTypeAttr != null) {
            try {
                targetType = (String) targetTypeAttr.get();
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
        return targetType;
    }

    /**
     * REMIND
     *
     * @param conn     REMIND
     * @param srAttrs  REMIND
     * @param targetDN REMIND
     * @return REMIND
     * @throws SystemException   REMIND
     * @throws NamingException   REMIND
     * @throws InternalException REMIND
     */
    public static String getTargetType(LDAPConnection conn,
                                       Attributes srAttrs,
                                       String targetDN, String targetTypeAtt, String subscriptionAttr, String collAttr, Map<String, String> LDAPVarsMap )
            throws SystemException,
            NamingException {
        Attribute targetTypeAttr = srAttrs.get(targetTypeAtt);
        String targetType = "";

        if (targetTypeAttr != null) {
            targetType = (String) targetTypeAttr.get();
        }

        Attribute t = srAttrs.get(subscriptionAttr);

        if ((targetType == null) || (targetType == "")) {
            // determine target type from name
            // target type is always defined for subscrription objects in 5.0
            // in 4.7, the target type was concatenated with the cn
            // in the form cn: name_type
            // so lop off the header cn to determine the type
            if (t != null) {
                String targetTypeString = (String) t.get();
                int last = targetTypeString.lastIndexOf('_');

                if (last == -1) {
                    // REMIND:RCR be more descriptive in the exception?
                    // or should we just set it to be unknown
                    throw new InternalException(SYSTEM_INTERNAL_EXCEPTION);
                } else {
                    targetType = targetTypeString.substring(last + 1, targetTypeString.length());
                }
            }
        }

        // if the target is a machine group, then we determine
        // if it is possibly a TYPE_COLLECTION
        if (TYPE_MACHINEGROUP.equals(targetType)) {
            if (DEBUG) {
                System.out.println("LDAPWebappUtils, getTargetType in MACHINEGROUP case");
            }

            if (!"".equals(targetDN)) {
                String[] searchAttrs = new String[1];
                searchAttrs[0] = LDAPConstants.OBJECT_CLASS;

                Attributes attrs = conn.getAttributes(targetDN, searchAttrs);
                String objectclass = LDAPUtils.getObjectClass(attrs, LDAPVarsMap, conn.currentTenantName, conn.currentChannel);

                if (DEBUG) {
                    System.out.println("objectclass= " + objectclass);
                }

                if (collAttr.equals(objectclass)) {
                    targetType = TYPE_COLLECTION;

                    if (DEBUG) {
                        System.out.println("LDAPWebappUtils, getTargetType: is Collection");
                    }
                }
            }
        }

        return targetType;
    }

    /**
     * REMIND
     *
     * @param srAttrs    REMIND
     * @param targetType REMIND
     * @param conn       REMIND
     * @return REMIND
     * @throws SystemException REMIND
     * @throws NamingException REMIND
     */
    public static String getTargetName(Attributes srAttrs,
                                       String targetType,
                                       LDAPConnection conn,
                                       String allEndpoints, Map<String, String> LDAPVarsMap)
            throws SystemException,
            NamingException {
        // First attempt to obtain the name from the target dn attribute
        Attribute nameAttr = srAttrs.get(LDAPVarsMap.get("TARGETDN"));
        String displayName = "";


        if (nameAttr != null) {
            displayName = (String) nameAttr.get();
        }

        // If the target dn attribute did not contain it
        // users, usergroups are sourced from transmitter.
        if ("".equals(displayName)) {
            if (targetType.equals(ISubscriptionConstants.TYPE_USER)) {
                nameAttr = srAttrs.get(LDAPVarsMap.get("TARGET_TX_USER"));

                if (nameAttr != null) {
                    displayName = (String) nameAttr.get();
                }
            } else if (targetType.equals(ISubscriptionConstants.TYPE_USERGROUP)) {
                nameAttr = srAttrs.get(LDAPVarsMap.get("TARGET_TX_GROUP"));

                if (nameAttr != null) {
                    displayName = (String) nameAttr.get();
                }
            } else if (targetType.equals(ISubscriptionConstants.TYPE_ALL)) {
                displayName = allEndpoints;
            } else if (targetType.equals("site")) {
                nameAttr = srAttrs.get("cn");

                if (nameAttr != null) {
                    displayName = (String) nameAttr.get();
                }
            }
        } else {
            if (targetType.equals(ISubscriptionConstants.TYPE_USER) && LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
                // Only on AD can users can have CN that is different from
                // its unique identifier sAMAccountName that Subscription use
                // use to resolve its target
                // REMIND t3 we can optimize this in the future by caching
                // the all the targets in the system
                Attributes elementAttrs = conn.getAttributes(displayName, new String[]{LDAPVarsMap.get("UI_USER_ID")});

                if (elementAttrs.size() > 0) {
                    displayName = (String) elementAttrs.get(LDAPVarsMap.get("UI_USER_ID"))
                            .get();
                } else {
                    // Can't find the uid or sAMAccountName attribute
                    // for a given user dn. For Target Details page this is used for
                    // display so we are ok if it is not accurate.
                    // For Switching from package details to target details
                    // this will cause a problem if the LDAPVarsMap.get("USER_ID is not
                    // the same as the CN
                    throw new SystemException(SYSTEM_INTERNAL_UIDNOTFOUND, displayName);
                }
            } else {
                displayName = parseTargetDNString(displayName, conn);
            }
        }

        // Since this is a display name, we unescape the value obtained from LDAP
        displayName = LDAPName.unescapeComponentValue(displayName);

        if (DEBUG) {
            System.out.println("target name is " + displayName);
        }

        return displayName;
    }


    public static String getTargetDN(Attributes srAttrs, Map<String, String> LDAPVarsMap)
            throws NamingException {
        String targetDN = "";
        Attribute nameAttr = srAttrs.get(LDAPVarsMap.get("TARGETDN"));

        if (nameAttr != null) {
//            System.out.println("if condition");
            targetDN = (String) nameAttr.get();
        } else if (srAttrs.get(LDAPVarsMap.get("TARGET_ALL")) != null) {
            targetDN = ISubscriptionConstants.TYPE_ALL;
        } else if (srAttrs.get(LDAPVarsMap.get("TARGET_TX_USER")) != null) {
            nameAttr = srAttrs.get(LDAPVarsMap.get("TARGET_TX_USER"));
            targetDN = (String) nameAttr.get();
        } else if (srAttrs.get(LDAPVarsMap.get("TARGET_TX_GROUP")) != null) {
            nameAttr = srAttrs.get(LDAPVarsMap.get("TARGET_TX_GROUP"));
            targetDN = (String) nameAttr.get();
        } else if (null != srAttrs.get(LDAPVarsMap.get("SUBSCRIPTION_NAME")) && null != srAttrs.get(LDAPVarsMap.get("TARGETTYPE"))) {  // Specific for SBRP Site policy
            Attribute typeAttr = srAttrs.get(LDAPVarsMap.get("TARGETTYPE"));
            String typeValue =  (String) typeAttr.get();
            if(TYPE_SITE.equals(typeValue)) {
                nameAttr = srAttrs.get(LDAPVarsMap.get("SUBSCRIPTION_NAME"));
                targetDN = (String) nameAttr.get();
            }
        }
        return targetDN;
    }

    public static boolean isFromSourceTx(Attributes attr, Map<String, String> LDAPVarsMap) {
        if(attr.get(LDAPVarsMap.get("TARGET_TX_USER")) != null) {
            return true;
        } else if(attr.get(LDAPVarsMap.get("TARGET_TX_GROUP")) != null) {
            return true;
        }
        return false;
    }

    // since the value of the TARGETDN is always a string in the form
    // "cn=xxx,ou=xxx,o-xxx" ,parse this string to obtain the value of the
    // cn property.
    private static String parseTargetDNString(String dnString,
                                              LDAPConnection conn)
            throws SystemException {
        try {
            LDAPName ldapName = conn.getParser();
            String displayName = ldapName.getCN(dnString);

            if ((displayName != null) && !"".equals(displayName)) {
                return displayName;
            }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        }

        return dnString;
    }

    /**
     * Copied from LDAPSubscription. This interrogates the attributes list obtained and creates Channel objects by parsing them appropriately. These channels
     * are added to the TargetChannelMap. This method also returns a list of unique Channel objects  that are contained in the hashtable. REMIND::RCR this
     * should probably be reconciled such that TargetChannelMap extends ISubDataSource but then we need to create and maintain LDAPSubscription objects.
     *
     * @param entry        REMIND
     * @param srAttrs      REMIND
     * @param channelTable REMIND
     * @throws NamingException REMIND
     * @throws SystemException REMIND
     */
    public static void addChannels(TargetChannelMap entry,
                                   Attributes srAttrs,
                                   Hashtable<String, Channel> channelTable, Map<String, String> LDAPVarsMap, String tenantName, IChannel channel)
            throws NamingException,
            SystemException {

        if (srAttrs != null) {

            Hashtable chnValueMaps = LDAPSubscription.loadChannelAttrs(srAttrs, LDAPVarsMap);
            Hashtable chnInitMap = (Hashtable) chnValueMaps.get(LDAPVarsMap.get("CHANNEL"));
            for (Enumeration ite = chnInitMap.keys(); ite.hasMoreElements();) {
                String url = (String) ite.nextElement();
                String state1 = (String) chnInitMap.get(url);

                if (LDAPSubscription.isNone(state1)) {
                    continue;
                }

                Channel chn = LDAPSubscription.createContent(url, state1, chnValueMaps, LDAPVarsMap, tenantName, channel);

                if (channelTable.get(url) != null) {
                    // only update the map's channel properties if the channel we are inspecting
                    // is part of the package selection
                    entry.addChannel(chn);
                } else {
                    // Add the channel to miscellaneous channels Hashtable. This would be used
                    // in the distribution assignment page
                    entry.addMiscChannels(chn);
                }
            }
        }

    }


    /**
     * This method is used to parse a list of SearchResult objects. It creates a vector of channel objects by populating them with url and name information.
     *
     * @param list         REMIND
     * @param countPerPage REMIND
     * @param search       REMIND
     * @return REMIND
     * @throws NamingException REMIND
     */
    public static HashMap<String, PropsBean> parseLDAPPackageResults(List<SearchResult> list,
                                                                     int countPerPage,
                                                                     String search, Map<String, String> LDAPVarsMap)
            throws NamingException {
        HashMap<String, PropsBean> chnTbl = new HashMap<String, PropsBean>(list.size());
        Object[] matches;
        String url = null;
        String title = null;
        String state1 = null;

        Matcher matcher = new Matcher();
        Object[] MATCH = new Object[1];

        matcher.add(search, MATCH);
        matcher.add(search.toLowerCase(), MATCH);
        matcher.prepare();

        //     try {
        if (!list.isEmpty()) {
            int size = list.size();

            if (DEBUG) {
                System.out.println("list is not empty, size is  " + size);
                System.out.println("count per page is " + countPerPage);
            }

            for (int i = 0; (i < countPerPage) && (i < size); i++) {
                SearchResult sr = list.get(i);

                if (DEBUG) {
                    System.out.println(" Comparing for  sr " + sr.getName() + " for index " + i);
                }

                Attributes srAttrs = sr.getAttributes();
                Hashtable chnInitMap = LDAPSubscription.loadChannelAttr(LDAPVarsMap.get("CHANNEL"), srAttrs);
                Hashtable chnTitleMap = LDAPSubscription.loadChannelAttr(LDAPVarsMap.get("CHANNELTITLE"), srAttrs);

                if (DEBUG) {
                    System.out.println(" srAttrs.size()= " + srAttrs.size());
                }

                for (Enumeration ite = chnInitMap.keys(); ite.hasMoreElements();) {
                    url = (String) ite.nextElement();
                    title = (String) chnTitleMap.get(url);
                    state1 = (String) chnInitMap.get(url);

                    if (LDAPSubscription.isNone(state1)) {
                        continue;
                    }

                    if (title == null) {
                        matches = matcher.match(extractChannelName(url).toLowerCase());
                    } else {
                        matches = matcher.match(title.toLowerCase());
                    }

                    if (matches.length != 0) {
                        if (chnTbl.get(url) == null) {
                            PropsBean entry = new PropsBean();
                            entry.setValue("url", url);

                            if (title == null) {
                                entry.setValue("title", extractChannelName(url));
                            } else {
                                entry.setValue("title", title);
                            }

                            chnTbl.put(url, entry);
                        }
                    }
                }
            }
        }
        //   } catch (Exception exc) {
        //     exc.printStackTrace();
        //}

        return chnTbl;
    }

    private static String extractChannelName(String url) {
        int i = url.lastIndexOf('/');

        if (i == -1) {
            return url;
        }

        return url.substring(i + 1);
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @return REMIND
     * @throws SystemException REMIND
     */
    public static LDAPConnection getBrowseConn(HttpServletRequest req)
            throws SystemException {
        IUser user = GUIUtils.getUser(req);

        return user.getBrowseConn();
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @return REMIND
     * @throws SystemException REMIND
     */
    public static LDAPConnection getSubConn(HttpServletRequest req)
            throws SystemException {
        IUser user = GUIUtils.getUser(req);

        return user.getSubConn();
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @return REMIND
     * @throws SystemException REMIND
     */
    public static LDAPConnection getCollConn(HttpServletRequest req)
            throws SystemException {
        IUser user = GUIUtils.getUser(req);

        return user.getCollConn();
    }

    /**
     * REMIND
     *
     * @param objectclassEnum REMIND
     * @return REMIND
     */
    public static String constructSearchFilter(Enumeration<String> objectclassEnum, Map<String, String> LDAPVarsMap) {
        StringBuffer sb = new StringBuffer();
        sb.append("(|");

        if (LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            sb.append("(objectclass=" + LDAPVarsMap.get("CONTAINER_CLASS") + ")");
        }

        sb.append("(objectclass=" + LDAPVarsMap.get("CONTAINER_CLASS2") + ")");

        while (objectclassEnum.hasMoreElements()) {
            String objclass = objectclassEnum.nextElement();
            sb.append("(objectclass=" + objclass + ")");
        }

        sb.append(")");

        return sb.toString();
    }

    /**
     * REMIND
     *
     * @param objectclass REMIND
     * @return REMIND
     */
    public static String constructSearchFilter(String[] objectclass, String collcontainerPrefix, Map<String, String> LDAPVarsMap) {
        StringBuffer sb = new StringBuffer();
        sb.append("(|");

        if ("ActiveDirectory".equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            sb.append("(objectclass=" + LDAPVarsMap.get("CONTAINER_CLASS") + ")");
        }

        if (LDAPVarsMap.get("CONTAINER_PREFIX").equals(collcontainerPrefix)) {
            sb.append("(objectclass=" + LDAPVarsMap.get("CONTAINER_CLASS") + ")");
        } else {
            sb.append("(objectclass=" + LDAPVarsMap.get("CONTAINER_CLASS2") + ")");
        }


        for (int i = 0; i < objectclass.length; i++) {
            String objclass = objectclass[i];
            sb.append("(objectclass=" + objclass + ")");
        }

        sb.append(")");

        return sb.toString();
    }

    // This method takes the full dn of a group and returns the group name
    public static String parseName(LDAPConnection conn,
                                   String dn)
            throws SystemException {
        String result = dn;

        try {
            LDAPName ldapName = conn.getParser();
            result = ldapName.getCN(dn);
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        }

        return LDAPName.unescapeComponentValue(result)
                .trim();
    }

    /**
     * REMIND
     *
     * @param req  REMIND
     * @param main REMIND
     * @return REMIND
     * @throws SystemException REMIND
     */
    public static String getSubBaseWithNamespace(HttpServletRequest req,
                                                 SubscriptionMain main)
            throws SystemException {
        User user = (User) GUIUtils.getUser(req);

        return main.getSubBaseWithNamespace(user);
    }

    /**
     * Used from LDAPBrowseEPAction and LDAPSearchAction to set the listing from the transmitter to be browsed. Since we do not have an LDAP connection to the
     * transmitter, we must handle this differently from the LDAP search results
     *
     * @param epType       REMIND
     * @param txgroupName  REMIND
     * @param session      REMIND
     * @param main         REMIND
     * @param req          REMIND
     * @param searchFilter REMIND
     * @return REMIND
     * @throws SystemException REMIND
     */
    public static Vector<PropsBean> setUsersOrGroupsListTx(String epType,
                                                           String txgroupName,
                                                           HttpSession session,
                                                           SubscriptionMain main,
                                                           HttpServletRequest req,
                                                           String searchFilter, Map<String, String> LDAPVarsMap)
            throws SystemException {
        if (DEBUG) {
            System.out.println("LDAPWebapputils: calling setUsersorGroupsListTx");
            System.out.println("LDAPWebapputils: searchFilter = " + searchFilter);
        }

        //Construct a matcher to be used for the string comparison
        PatternMatcher matcher = new Perl5Matcher();
        GlobCompiler compiler = new GlobCompiler();
        Pattern pattern;
        try {
            pattern = compiler.compile(searchFilter, GlobCompiler.CASE_INSENSITIVE_MASK | GlobCompiler.READ_ONLY_MASK);
        } catch (MalformedPatternException mpe) {
            throw new KnownException(mpe, IErrorConstants.MALFORMED_PATTERN);
        }
        if (DEBUG) System.out.println("lDAPWebappUtils: epType = " + epType);

        String[] listing = null;
        Vector<PropsBean> listingv = null;
        String identifier = LDAPVarsMap.get("TARGET_TX_USER");        // used to identify this as a transmitter user/user group within the GUI

        /* Obtain the appropriate listing from the transmitter depending on which entry point is being navigated.
           When sourcing users from the transmitter, the only two supported entry points are user and user group
          */
        if (PEOPLE_EP.equals(epType) || GROUPS_EP.equals(epType)) {
            //get the user directory from the transmitter
            IUserDirectory userdir = main.getUserDirectoryTx(req);

            if (userdir == null) {
                System.out.println("LDAPBrowseEPAction: userdir is NULL");
            }

            if (userdir != null) {
                if (GROUPS_EP.equals(epType)) {
                    listing = userdir.listGroups();
                    identifier = LDAPVarsMap.get("TARGET_TX_GROUP");
                } else if (PEOPLE_EP.equals(epType)) {
                    listing = userdir.listUsers();
                } else if (USER_TXGROUP.equals(epType)) {
                    if (DEBUG) System.out.println("LDAPWebappUtils: user_txgroup");
                    listing = userdir.listUsers(txgroupName);
                }
            }

            if (listing == null || listing.length < 1) {
                if (DEBUG) System.out.println("LDAPWebappUtils: no results to return for srctx");
                return new Vector<PropsBean>(1);
            }
        } else {
        	if(SITES_EP.equals(epType)) { // SBRP Sites list
                List<String> sites =  main.getSBRPSiteHttpConnector().getSitesList();
                if(null != sites && sites.size() > 0) {
                    listing = sites.toArray(new String[sites.size()]);
                    identifier = TYPE_SITE;
                }
            }
        }
        if ((listing == null) || (listing.length < 1)) {
            if (DEBUG) {
                System.out.println("LDAPWebappUtils: no results to return for srctx");
            }

            return new Vector(1);
        }
        ArrayList<Object> sortedlist = new ArrayList<Object>(listing.length);
        LDAPPolicyHelper policyFinder = new LDAPPolicyHelper(LDAPWebappUtils.getSubConn(req), main.getSubBase(), main.getUsersInLDAP(), main.getDirType());

        for (int i = 0; i < listing.length; i++) {
            sortedlist.add(listing[i]);
            policyFinder.addTarget(listing[i], epType);

            if (DEBUG) {
                System.out.println("LDAPWebappUtils: sorted list elem " + i + "=" + listing[i]);
            }
        }

        Collator stringComp = Collator.getInstance();
        Collections.sort(sortedlist, stringComp);

        if (sortedlist.size() > 0) {
            //Convert the listing to a vector since that is what the page expects.
            listingv = new Vector<PropsBean>(10);

            PropsBean entry;

            for (Object aSortedlist : sortedlist) {
                if (matcher.contains((String) aSortedlist, pattern)) {
                    if (DEBUG) {
                        System.out.println("LDAPWebappUtils: match found for search string = " + aSortedlist);
                    }

                    entry = new PropsBean();
                    entry.setValue(LDAPConstants.OBJECT_CLASS, identifier);
                    entry.setValue(LDAPConstants.DISPLAY_NAME, aSortedlist);
                    entry.setValue(LDAPVarsMap.get("DN"), aSortedlist);
                    entry.setProperty(GUIConstants.ISTARGETABLE, "true");
                    if (identifier.equals(LDAPVarsMap.get("TARGET_TX_GROUP"))) {
                        entry.setProperty("type", "group");
                        entry.setProperty(GUIConstants.EXPANDABLE, "true");
                    } else if (identifier.equals(TYPE_SITE)) {
                        entry.setProperty("type", "network");
                    } else {
                        entry.setProperty("type", "user");
                    }
                    // listUsers in LDAPUserConnection returns the users without LDAP escaping
                    entry.setValue(ESCAPED_NAME, LDAPName.escapeComponentValue((String) aSortedlist));
                    if (policyFinder.hasPolicies((String) aSortedlist)) {
                        entry.setValue(GUIConstants.HASPOLICIES, "true");
                    }
                    listingv.addElement(entry);
                }
            }

            session.setAttribute(PAGE_GEN_RS, listingv);
        }

        return listingv;
    }


    // retuns name in dotted DNS format when given in dn format
    public static String dn2DNSName(LDAPConnection conn,
                                    String dn,
                                    boolean first)
            throws NamingException {
        LDAPName ldapName = conn.getParser();
        Name dnName = ldapName.parse(dn);
        //if first is false parse entire dn
        StringBuffer tname = new StringBuffer();

        if (!first) {
            for (int i = dnName.size() - 1; i > 0; i--) {
                tname.append(ldapName.getComponentValue(dnName, i));
                tname.append('.');
            }

            tname.append(ldapName.getComponentValue(dnName, 0));
        } else {
            tname.append(ldapName.getComponentValue(dnName, dnName.size() - 1));
        }

        if (DEBUG2) {
            System.out.println("dn2DNSName = " + tname.toString());
        }

        return tname.toString();
    }

    /**
     * REMIND
     *
     * @param props REMIND
     * @param name  REMIND
     */
    public static void dumpProperties(PropsBean props,
                                      String name) {
        System.out.println(name);
        System.out.println("---------------------------------------------".substring(0, name.length()));

        String[] properties = props.getPropertyPairs();

        if ((properties == null) || (properties.length < 1)) {
            System.out.println("No propeties to display");
        } else {
            for (int i = 0; i < (properties.length); i += 2) {
                System.out.println(properties[i] + " = " + properties[i + 1]);
            }
        }
    }

    // creates the search filter necessary to find all
    // enclosloing domains
    public static void getDomainAssignmentFilter(LDAPConnection con,
                                                 String dn,
                                                 StringBuffer searchStr, String targetDNAttr)
            throws NamingException,
            LDAPLocalException {
        if (!dn.equals("All Endpoints")) {
            LDAPName ldapName = con.getParser();
            Name dnName = ldapName.parse(dn);

            StringBuffer enclosingDomains = new StringBuffer(128);

            // automatically add dc = com since it will
            // allways be there
            enclosingDomains.append(dnName.get(0));

            String name;

            for (int i = 1; i < dnName.size(); i++) {
                name = dnName.get(i);

                if (name.startsWith("DC=") || name.startsWith("dc=")) {
                    enclosingDomains.insert(0, name + ',');
                    searchStr.append("(");
                    searchStr.append(targetDNAttr);
                    searchStr.append("=");
                    searchStr.append(LDAPSearchFilter.escapeComponentValue(enclosingDomains.toString()));
                    searchStr.append(")");
                }
            }

            if (DEBUG2) {
                System.out.println("The domain search string is: " + searchStr);
            }
        }
    }

    /**
     * REMIND
     *
     * @param searchStr REMIND
     */
    public static void appendTargetAll(StringBuffer searchStr, Map<String, String> LDAPVarsMap) {
        // Include the 'All' target to the query . This target contains
        // LDAPVarsMap.get("TARGET_ALL attribute set to true
        if (searchStr.length() == 0) {
            // only search for 'all' subscriptions if the search string is
            // empty
            searchStr.append("(&");
            LDAPWebappUtils.append2Filter(searchStr, LDAPVarsMap.get("TARGET_ALL"), "true");
            searchStr.append(")");
        } else {
            LDAPWebappUtils.append2Filter(searchStr, LDAPVarsMap.get("TARGET_ALL"), "true");
            searchStr.insert(0, "(&(|");
            searchStr.append("))");
        }
    }

    /**
     * REMIND
     *
     * @param buff  REMIND
     * @param dn    REMIND
     * @param value REMIND
     */
    public static void append2Filter(StringBuffer buff,
                                     String dn,
                                     String value) {
        buff.append("(");
        buff.append(dn);
        buff.append("=");
        buff.append(LDAPSearchFilter.escapeComponentValue(value));
        buff.append(")");
    }

    /**
     * REMIND
     *
     * @param type REMIND
     * @return REMIND
     */
    public static String getGroupText(int type) {
        switch (type) {
            case LDAPConstants.ADS_GROUP_TYPE_GLOBAL_GROUP:
                return "global";

            case LDAPConstants.ADS_GROUP_TYPE_DOMAIN_LOCAL_GROUP:
                return "local";

            case LDAPConstants.ADS_GROUP_TYPE_UNIVERSAL_GROUP:
                return "universal";

            default:
                return "";
        }
    }

    /**
     * REMIND
     *
     * @param entry   REMIND
     * @param srAttrs REMIND
     */
    public static void setDisplayName(PropsBean entry,
                                      Attributes srAttrs, String dirType, String tenantName, IChannel channel) {
        String displayName = null;
        String objectclass = entry.getProperty(LDAPConstants.OBJECT_CLASS);
        Map<String, String> classPrefixMap = LDAPUtils.getLDAPVarMap("classPrefixMap", dirType);
        String prefix = classPrefixMap.get(objectclass);

        if (prefix != null) {
            try {
                displayName = (String) LDAPUtils.getValue(srAttrs, prefix, tenantName, channel)
                        .lastElement();
            } catch (NamingException ne) {
                if (DEBUG) {
                    System.out.println("entry " + entry);
                    System.out.println("attributes " + srAttrs);
                    ne.printStackTrace();
                }
            }
        }

        if (displayName == null) {
            if (DEBUG3) {
                System.out.println("No displayname" + srAttrs + "for objectclass " + objectclass);
            }
        }

        entry.setProperty(LDAPConstants.DISPLAY_NAME, displayName);
    }


    /**
     * DOCUMENT ME!
     *
     * @param results      contains a bunch of dns to display
     * @param policyFinder contains list of to lookup
     */
    public final static void setDirectTargetProperty(Collection results,
                                                     LDAPPolicyHelper policyFinder, Map<String, String> LDAPVarsMap) {
        PropsBean entry;

        for (Iterator iter = results.iterator(); iter.hasNext();) {
            entry = (PropsBean) iter.next();
            String objectClass = (String) entry.getValue(LDAPConstants.OBJECT_CLASS);
            if ((objectClass == null || objectClass.equals(IWebAppConstants.INVALID_COLLECTION) || objectClass.equals(IWebAppConstants.INVALID_LINK))
            		|| ISubscriptionConstants.MDM_TYPE_DEVICE_GROUP.equals(objectClass) || ISubscriptionConstants.TYPE_DEVICE.equals(objectClass)) {
                continue;
            }
            if (policyFinder.hasPolicies((String) entry.getValue(LDAPVarsMap.get("DN")))) {
                entry.setValue(GUIConstants.HASPOLICIES, "true");
            }
            if((null != policyFinder.getOSTemplate()) && ("true".equals((String)entry.getValue(GUIConstants.HASPOLICIES)))) {
                entry.setValue(GUIConstants.OSTEMPLATENAME, policyFinder.getOSTemplate());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param entry the expandable value will be set based on objectclass
     */
    public final static void setExpandAbleProperty(PropsBean entry, Map<String, String> LDAPVarsMap) {
        String objectclass = entry.getProperty(LDAPConstants.OBJECT_CLASS);

        // machine class is not expandable
        if (LDAPUtils.isMachineClass(objectclass, LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            return;
        }

        // User class and all all are not expandable
        if (LDAPVarsMap.get("USER_CLASS").equalsIgnoreCase(objectclass) ||
                LDAPVarsMap.get("USER_PROXY_CLASS").equalsIgnoreCase(objectclass) ||
                LDAPVarsMap.get("TARGET_ALL").equalsIgnoreCase(objectclass) ||
                IWebAppConstants.INVALID_COLLECTION.equals(objectclass) ||
                IWebAppConstants.INVALID_LINK.equals(objectclass)) {
            return;
        } else {
            entry.setProperty(GUIConstants.EXPANDABLE, "true");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param entry      the targetable value will be set based on objectclass
     * @param userInLDAP REMIND
     */
    public final static void setTargetAbleProperty(PropsBean entry,
                                                   boolean userInLDAP, String userClassAtt) {
        String objectclass = entry.getProperty(LDAPConstants.OBJECT_CLASS);
        if ("false".equals(entry.getProperty(GUIConstants.ISTARGETABLE))) {
            return;
        }
        if (userClassAtt.equals(objectclass) && !userInLDAP) {
            return;
        } else {
            entry.setProperty(GUIConstants.ISTARGETABLE, "true");
        }
    }

    public static String getTunerProperty(SubscriptionMain sub, String key) {
        IConfig ic = (IConfig) sub.getFeatures().getChild("tunerConfig");
        return ic.getProperty(key);
    }

    public static boolean isValidProperty(String propandkey) {
        boolean valid = false;
        if ((propandkey != null) && !"".equals(propandkey)) {
            //check for key value
            String key = null;
            // value = null;
            int i = propandkey.indexOf("=");
            if (i > 0) {
                key = propandkey.substring(0, i);
            }

            if (key != null && key.length() > 0) {
                return true;
            }
        }
        return false;
    }

    public static String escapeDN(LDAPConnection conn, PropsBean props, String targetAllAttr) throws NamingException {
        return escapeDN(conn, props.getProperty("dn"), props.getProperty(GUIConstants.TYPE), targetAllAttr);
    }

    public static String escapeDN(LDAPConnection conn, Target target, String targetAllAttr) throws NamingException {

        return escapeDN(conn, target.getID(), target.getType(), targetAllAttr);
    }

    private static String escapeDN(LDAPConnection conn, String dn, String type, String targetAllAttr) throws NamingException {
        if(ISubscriptionConstants.TYPE_ALL.equals(type) ||
        		targetAllAttr.equals(type)) {
            return "all";
        }
        if(ISubscriptionConstants.TYPE_SITE.equals(type) || ISubscriptionConstants.TYPE_DEVICE.equals(type) || ISubscriptionConstants.MDM_TYPE_DEVICE_GROUP.equals(type)) {
            return dn;
        }
        dn = dn.toLowerCase();
        dn = conn.getParser().getCanonicalName(dn);
        return dn;
    }

    // ToDo: Add Comment
    public static Vector<TargetChannelMap> searchTargetaForPkg(List channellist,
                                                               String             subBase,
                                                               String             childContainer,
                                                               String             allEndpoints,
                                                               String             pagingType,
                                                               IUser              user,
                                                               boolean            primaryAdmin,
                                                               SubscriptionMain   main)
            throws AclStorageException,
            NamingException,
            LDAPLocalException,
            AclException,
            SystemException {
        return searchTargetaForPkg(channellist, subBase, childContainer, allEndpoints, pagingType, user, primaryAdmin, main, null);
    }

    /* This method search and returns the targets for the input package and used by both UI and command line module
    *
    * @param channellist - package list
    */
    public static Vector<TargetChannelMap> searchTargetaForPkg(List channellist,
                                                               String             subBase,
                                                               String             childContainer,
                                                               String             allEndpoints,
                                                               String             pagingType,
                                                               IUser              user,
                                                               boolean            primaryAdmin,
                                                               SubscriptionMain   main,
                                                               String             searchFilter)
            throws AclStorageException,
            NamingException,
            LDAPLocalException,
            AclException,
            SystemException {
        // search for the entries one level below the selected container
        if (null == searchFilter || searchFilter.trim().length() == 0) {
        	Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
            searchFilter = createSearchFilter(channellist, main, LDAPVarsMap);
        }

        java.util.ArrayList resultsList = new ArrayList();

        LDAPConnection conn = user.getBrowseConn();
        LDAPConnection subConn = user.getSubConn();
        String[] chAttrs = LDAPUtils.getLDAPVarArr("channelAttributeAsArray", main.getDirType());
        Vector<TargetChannelMap> listing = null;
        boolean             pagesExist;

        if (DEBUG) {
            System.out.println("search filter is " + searchFilter);
            System.out.println("searchAttrs is " + chAttrs);
            System.out.println("subBase is " + subBase);
        }

        LDAPPagedSearch paged = LDAPPagedSearch.getLDAPPagedSearch(conn, searchFilter, chAttrs, subBase, false, new String[] { "cn" });
        paged.setPageSize(DEFAULT_COUNT_PER_PAGE);

        // calculate number of pages
        int size = 0;
        int startIndex = 0;

        // check if the search has any results first
        if (LDAPPagedSearch.OID_PR.equals(pagingType)) {
            // AD paging does not give the total number of pages
            pagesExist = true;
        } else {
            // NS paging gives the total number of pages
            size       = paged.getSize();
            pagesExist = (size > 0);
        }

        if (DEBUG) {
            System.out.println(" pagesExist is : " + pagesExist);
            System.out.println(" size is (only applicable for VLV)       : " + size);
        }

        // retrieve the results page by page
        while (pagesExist) {
            List pageResults = paged.getPage(startIndex);
            resultsList.addAll(pageResults);

            if (DEBUG) {
                System.out.println(" startindex is " + startIndex);
                System.out.println("results list size is " + resultsList.size());
            }

            startIndex = startIndex + DEFAULT_COUNT_PER_PAGE;

            if (LDAPPagedSearch.OID_PR.equals(pagingType)) {
                pagesExist = paged.hasMoreResults() || (startIndex < paged.getSize());
            } else {
                pagesExist = (startIndex < size);
            }
        }
        Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
        listing = LDAPWebappUtils.parseLDAPTargetResults(conn, subConn, resultsList,
                channellist, subBase, childContainer, allEndpoints, LDAPVarsMap);

        if (!primaryAdmin && main.isAclsOn()) {
            filterResultsForAcls(listing, user, main);
        }

        if (DEBUG) {
            System.out.println("listing size is " + listing.size());
        }

        return listing;
    }

    /* This method filter the result targets based on the ACL permission assigned to the logged in user.
    *
    * @param channellist - package list
    */
    private static void filterResultsForAcls(Vector<TargetChannelMap> results, IUser user, SubscriptionMain main)
            throws AclStorageException,
            NamingException,
            LDAPLocalException,
            AclException {
        if (DEBUG) {
            System.out.println("User = " + user);
            System.out.println("User name = " + user.getName());
        }

        Iterator<TargetChannelMap> iter = results.iterator();

        while (iter.hasNext()) {
            TargetChannelMap targetMap = iter.next();
            if (DEBUG) {
                System.out.println("Processing target " + targetMap.getName());
            }

            int actions = main.getAclMgr().permissionExists( IAclConstants.SUBSCRIPTION_PERMISSION,
                    user, targetMap.getTarget(), null, true, main.getUsersInLDAP());

            if ((actions & IAclConstants.READ_ACTION_INT) == 0) {
                if (DEBUG) {
                    System.out.println("Removing from resultset");
                }
                iter.remove();
            }

            if ((actions & IAclConstants.WRITE_ACTION_INT) == 0) {
                // update the target map to indicate that this item can't be modified
                targetMap.setIsSelectedTarget("false");
                if (DEBUG) {
                    System.out.println("User doesn't have Subscription write permission on target");
                }
            }
        }
    }

    public static Vector<String> getRelativeDNs(String inputDN, String baseDN) {
    	Vector<String> relativeDNs = new Vector<String>();
    	if(null == inputDN || null == baseDN) {
    		return null;
    	}
    	if(inputDN.equalsIgnoreCase(baseDN)) {
    		relativeDNs.add(baseDN);
    		return relativeDNs;
    	}
    	try {
    		int index = inputDN.indexOf(baseDN);
    		if(index > 0) {
    			relativeDNs.add(baseDN);
    			String relativeDN = inputDN.substring(0, index-1);
    			String[] dns = relativeDN.split(",");
    			String outputDN = baseDN;
    			if(null != dns) {
    				for(int i=dns.length-1;i>=0;i--) {
    					outputDN = dns[i] + "," +outputDN;
    					relativeDNs.add(outputDN);
    				}
    			}
    		}
    		return relativeDNs;
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
    	return null;
    }
    /* This method create a search filter for the input package list.
    *
    * @param channellist - package list
    */
    private static String createSearchFilter(List channelList, SubscriptionMain main, Map<String, String> LDAPVarsMap) {
        // piece together the search filter that is passes to the LDAP query
        String       searchFilter = "";
        StringBuffer sb = new StringBuffer();
        boolean browseLDAP = main.getUsersInLDAP();
        sb.append("(&");
        // search filter added to display only the packages that are assigned
        // to targets browsed from ldap. Previously it displays the packages
        // includes targets that are assigned to the targets sourced from tx.
        Iterator iter = channelList.iterator();

        if (iter.hasNext() == false) {
            ;
        } else {
            while (iter.hasNext()) {
                Channel itg = (Channel) iter.next();
                String  url = itg.getUrl();
                String  escapedUrl = LDAPName.escapeComponentValue(url);

                if (DEBUG) {
                    System.out.println("channel = " + url);
                    System.out.println("escaped channel = " + escapedUrl);
                }

                sb.append("(" + LDAPVarsMap.get("CHANNEL") + "=" + escapedUrl + "\\=*)");
            }
            // search filter added to display only the packages that are assigned
            // to targets browsed from ldap. Previously it displays the packages
            // includes targets that are assigned to the targets sourced from tx.
            if(browseLDAP) {
                //(&(mrbachannel=*)(|(mrbatargetdn=*)(mrbatargetall=true)))
                //(&(channel)(|(retrieve ldap objects with targetDN property)(retrieve all_all target with property targetall)))
                sb.append("(|(" + LDAPVarsMap.get("TARGETDN") + "=*)(" + LDAPVarsMap.get("TARGETTYPE") + "=site)(" + LDAPVarsMap.get("TARGET_ALL") + "=true)))");
            } else {
                //(&(mrbachannel=*)(|(mrbatargettxuser=*)(!(mrbatargettype=user))))
                //(&(channel)(|(retrieve ldap objects with targetDN property)(retrieve all_all target with property targetall)))
                sb.append("(|(" + LDAPVarsMap.get("TARGET_TX_USER") + "=*)(" + LDAPVarsMap.get("TARGETTYPE") + "=site)(!(" + LDAPVarsMap.get("TARGETTYPE") + "=user))))");
            }
        }

        searchFilter = sb.toString();

        if (DEBUG) {
            System.out.println(" Search Filter created is " + searchFilter);
        }

        return searchFilter;
    }
    public static boolean validateTargetDN(String targetDN, HttpServletRequest request) {
    	boolean status = false;
    	try {
    		LDAPConnection conn = getBrowseConn(request);
    		NamingEnumeration nenum = LDAPUtils.validateDN(targetDN, conn);
    		if (nenum != null && nenum.hasMore()) {
    			return true;
    		}
    	} catch(Exception ed) {
    		status = false;
    	}
    	return status;
    }

}

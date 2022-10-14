// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.naming.*;
import javax.naming.directory.*;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.objects.dao.ISubDataSource;
import com.marimba.apps.subscription.common.objects.Subscription;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.objects.dao.LDAPSubscription;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.users.User;

import com.marimba.intf.msf.acl.IAclConstants;

import com.marimba.tools.ldap.*;
import com.marimba.webapps.intf.*;
import com.marimba.intf.util.IProperty;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
/**
 * This is a singleton class for the subscription object. There is one instance of this class in each channel.  It is currently being used only by the
 * SubscriptionManager. This class Contains, create, get, delete, list, methods. Also contains methods which creates a datasource object for for example,
 * either the file system, or in LDAP (Subscription Manager directory enabled). Methods are divided into two major sets. One set that deals with a multiuser
 * environment (GUI) in which there is one connection to the storage system per user, and one set that deals with a single user environment (CLI and plugin)
 * in which the connection used is passed in each invocation. Note : This is not the Singleton pattern.  It is a class with static methods. We bumped into a
 * problem in which SubscriptionMain. Since this is not an object, this class does NOT destroy itself upon program exit.  We ran into a problem in which
 * SubscriptionMain can't be destroyed because this class refers to it.
 */
public class ObjectManager
    implements ISubscriptionConstants,
                   IErrorConstants {
    final static String           LDAP_SRC_CLASS = "com.marimba.apps.subscription.common.objects.dao.LDAPSubscription";
    String                 subClassPath = LDAP_SRC_CLASS;
    IMainDataSourceContext main = null;

    static DateFormat ADformatter;
    static DateFormat NSformatter;
    String tenantName;
    static Map<String, IMainDataSourceContext> objManagerMap = new Hashtable<String, IMainDataSourceContext>();

    /**
     * Sets the data store for a subscription object.  The data source object is retrieved using getSubDataSource(String user)
     *
     * @param ctx REMIND
     * @param subClassName REMIND
     *
     * @throws SubInternalException REMIND
     *
     */
    public void init(IMainDataSourceContext ctx,
                            String                 subClassName, String tenantName)
        throws SubInternalException {
        main         = ctx;
        subClassPath = subClassName;
        this.tenantName = tenantName;
        objManagerMap.put(tenantName, main);
    }

    /**
     * Retrieves the data store for a subscription object.  The data source object is set using init()
     *
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     *
     * @return ISubDataSource a subscription data source object.
     *
     * @throws SystemException REMIND
     *
     */
    public static ISubDataSource getSubDataSource(ILDAPDataSourceContext user)
        throws SystemException {
        return getSubDataSource(user, null);
    }

    /**
     * Retrieves the data store for a subscription object.  The class of the data source  can be specified.  This method is used in the GUI in a multi-user
     * environment.
     *
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     * @param src the class name of the data source. If null, the class specified in     the init method is used
     *
     * @return ISubDataSource a subscription data source object.
     *
     * @throws SystemException REMIND
     * @throws SubInternalException REMIND
     *
     */
    public static ISubDataSource getSubDataSource(ILDAPDataSourceContext user,
                                                  String                 src)
        throws SystemException {
        try {
        	IMainDataSourceContext objMgr = getMainDataSourceContext(user.getTenantName());
            return new LDAPSubscription(user, objMgr);
        } catch (SystemException se) {
            throw se;
        } catch (Exception se) {
            throw new SubInternalException(se, SUB_INTERNAL_OBJMGR_SUBDATASOURCE);
        }
    }
    public synchronized static IMainDataSourceContext getMainDataSourceContext(String tenantName) {
    	if(null == objManagerMap) return null;
    	return objManagerMap.get(tenantName);
    }

    /**
     * Retrieves a subscription from the storage.  A new subscription object will be created in memory if it does not already exist in storage. save() should
     * be called to persistify the subscription. In a multi environment, even though there is one connection per user, all user connections share the same
     * LDAP server settings.
     *
     * @param targetid The target name for which the subscription should be created.  This should be     in distinguished name format if the object is from
     *        LDAP
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     *
     * @return the subscription object which has fields that are loaded from the LDAP attributes
     *
     * @throws SystemException REMIND
     */
    public static ISubscription createSubscription(String targetid,
                                                   String targettype,
                                                   IUser  user)
        throws SystemException {
        ISubDataSource src = getSubDataSource(user);
        Subscription   sub = new Subscription(targetid, targettype, src);

        if (sub.exists()) {
            sub.load();
        }

        return sub;
    }
    /**
     * Retrieves an existing subscription with search level from the storage Subscription object's storage identifier.
     *
     * @param subname the name of the subscription
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     *
     * @return the subscription object which has fields that are loaded from the LDAP attributes
     *
     * @throws SystemException REMIND
     */
    public static ISubscription getSubscription(String subname,
                                                IUser  user, boolean oneLevel)
        throws SystemException {
        ISubDataSource src = getSubDataSource(user);
        Subscription sub = new Subscription(subname, src, oneLevel);
        if (sub.getTargetID() == null) {
            sub = null;
        } else {
            sub.load();
        }

        return sub;
    }
    /**
     * Retrieves an existing subscription from the storage Subscription object's storage identifier.
     *
     * @param subname the name of the subscription
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     *
     * @return the subscription object which has fields that are loaded from the LDAP attributes
     *
     * @throws SystemException REMIND
     */
    public static ISubscription getSubscription(String subname,
                                                IUser  user)
        throws SystemException {
    	ISubDataSource src = getSubDataSource(user);
        Subscription sub = new Subscription(subname, src);
        if (sub.getTargetID() == null) {
            sub = null;
        } else {
            sub.load();
        }

        return sub;
    }

    /**
     * Retrieves a subscription from the storage.  The subscription must alredy exist in storage.
     *
     * @param targetid The target identifier for which the subscription should be created.  This should be     in distinguished name format if the object is
     *        from LDAP
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     *
     * @return the subscription object which has fields that are loaded from the LDAP attributes
     *
     * @exception SystemException if the subscription does not exist in LDAP
     */
    public static ISubscription getSubscription(String targetid,
                                                String targettype,
                                                IUser  user)
        throws SystemException {
        ISubDataSource src = getSubDataSource(user);
        Subscription   sub = new Subscription(targetid, targettype, src);
        sub.load();

        return sub;
    }

    /**
     * Deletes a subscription from the storage if the object exists.
     *
     * @param targetid The target identifier for which the subscription should be deleted.  This should be     in distinguished name format if the object is
     *        from LDAP
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     *
     * @throws SystemException REMIND
     */
    public static void deleteSubscription(String targetid,
                                          String targettype,
                                          IUser  user)
        throws SystemException {
        ISubDataSource src = getSubDataSource(user);
        Subscription   sub = null;
        sub = new Subscription(targetid, targettype, src);
        sub.delete();
    }

    /**
     * Deletes an existing subscription from the storage given the Subscrtiption object's storage identifier.
     *
     * @param subname Subscription Policy storage identifier.
     * @param user the user from which the connection to the datasource (for example, LDAPConnection) can be obtained from
     *
     * @throws SystemException REMIND
     */
    public static void deleteSubscription(String subname,
                                          IUser  user)
        throws SystemException {
        ISubDataSource src = getSubDataSource(user);
        Subscription   sub = null;
        sub = new Subscription(subname, src);
        sub.delete();
    }

    /**
     * Checks if a subscription exists in the storage.
     *
     * @param targetid The target name for which the subscription should be created.  This should be     in distinguished name format if the object is from
     *        LDAP
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param user the data source for the subscription.  A single user system will     user getSubDataSource(IMainDataSourceContext, String) to create a data
     *        source     to be passed into this method.
     *
     * @return true if the Subscription exists
     *
     * @throws SystemException REMIND
     */
    public static boolean existsSubscription(String targetid,
                                             String targettype,
                                             IUser  user)
        throws SystemException {
        ISubDataSource src = getSubDataSource(user);
        Subscription   sub = new Subscription(targetid, targettype, src);

        if (sub.exists()) {
            return true;
        }else if(targettype.equals(IWebAppConstants.INVALID_NONEXIST)){
        	return true;
        }

        return false;
    }

	public static String[] listSubscriptionByName(IMainDataSourceContext main,
			ILDAPDataSourceContext srcCtx) throws SystemException {
		return listSubscriptionByName(main, srcCtx, true);
	}
    /**
     * List subscriptions available in the storage
     *
     * @param main A context to object the Subscription base
     * @param srcCtx A context to obtain LDAPConnection
     *
     * @return a list of Target DNs of the subscription available in the namespace obtained from ILDAPDataSourceContext
     *
     * @throws SystemException REMIND
     */
    public static String[] listSubscriptionByName(IMainDataSourceContext main,
                                                  ILDAPDataSourceContext srcCtx, boolean oneLevel)
        throws SystemException {
        String[]       list = null;
        LDAPConnection directory = srcCtx.getSubConn();

        try {
        	Map<String, String> LDAPVarsMap = srcCtx.getLDAPVarsMap();
            String base = main.getSubBaseWithNamespace(srcCtx);
	        list = directory.searchAndList("(&(objectclass=" + LDAPVarsMap.get("SUBSCRIPTION_CLASS") + ")(" + LDAPVarsMap.get("SM_ATTR") + "=*))", LDAPVarsMap.get("SUBSCRIPTION_NAME"), null, false, base, oneLevel);
            return list;
        } catch (LDAPException le) {
            LDAPUtils.classifyLDAPException(le.getRootException());
        }

        return list;
    }

    /**
     * List subscriptions available in the storage
     *
     * @param main A context to object the Subscription base
     * @param srcCtx A context to obtain LDAPConnection
     *
     * @return a list of Target DNs of the subscription available in the namespace obtained from ILDAPDataSourceContext
     *
     * @throws SystemException REMIND
     */
    public static List listSubscriptions(IMainDataSourceContext main,
					 ILDAPDataSourceContext srcCtx) throws SystemException {
        LDAPConnection directory = srcCtx.getSubConn();
	ArrayList res = new ArrayList(100);

        try {
        	Map<String, String> LDAPVarsMap = srcCtx.getLDAPVarsMap();
        	String[] subsAttr = LDAPUtils.getLDAPVarArr("subAttrs", LDAPVarsMap.get("DIRECTORY_TYPE"));
            NamingEnumeration nenum = directory.search("(" + LDAPVarsMap.get("SM_ATTR") + "=*)", subsAttr, main.getSubBaseWithNamespace(srcCtx), false);
	    while (nenum.hasMoreElements()) {
		SearchResult sr = (SearchResult) nenum.next();
		Subscription sub = new Subscription(ObjectManager.getSubDataSource(srcCtx));
		sub.getDataSource().load(sr, sub);
		res.add(sub);
	    }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne, ne.getMessage(), true);
        }
        return res;
    }

    /**
     * REMIND
     */
    public static void destroy() {
    	objManagerMap = new Hashtable<String, IMainDataSourceContext>();
    }

    /**
     * Retrieves a Subscription if the given admin has write access to the target.
     *
     * @param targetid The id of the target to create a subscription for
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param user The principal to check for permission
     *
     * @return the Subscription if the specified admin has read permissions for the target
     *
     * @throws SystemException REMIND
     */
    public static ISubscription openSubForWrite(String targetid,
                                                String targettype,
                                                IUser  user)
        throws SystemException {
        return openSubWithPerm(user, targetid, targettype, IAclConstants.WRITE_ACTION);
    }

    /**
     * Retrieves a Subscription if the given admin has write access to the target.
     *
     * @param targetName The id of the target to create a subscription for
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param domain Domain in which the targetName should look for
     * @param user The principal to check for permission
     *
     * @return the Subscription if the specified admin has read permissions for the target
     *
     * @throws SystemException REMIND
     */
    public static ISubscription openSubForWrite(String targetName,
                                                String targettype,
                                                String domain,
                                                IUser  user,
                                                boolean isADAutoDiscover)
        throws SystemException {
    	IMainDataSourceContext objMgr = getMainDataSourceContext(user.getTenantName());
        IProperty mrbaConfig = objMgr.getMarimbaConfig();
        IProperty subConfig = objMgr.getSubscriptionConfig();

        String attr = subConfig.getProperty(LDAPConstants.CONFIG_MACHINENAMEATTR);
        String objectClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);

        if ("user".equals(targettype)) {
            attr = subConfig.getProperty(LDAPConstants.CONFIG_USERIDATTR);
            objectClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_USERCLASS);
        }

        String objectFilter = LDAPUtils.createSearchFilter(LDAPConstants.OBJECT_CLASS, objectClass);

        String query = "(&(" + attr + "=" + LDAPSearchFilter.escapeComponentValue(targetName) + ")" + objectFilter + ")";

System.out.println("[openSubForWrite] Search Query: " + query);
        try {
            String[] dns = null;
            String domainDN = null;
            LDAPConnection conn = null;
            if (isADAutoDiscover) {
                // We need a DC connection here
                conn = user.getConnectionCache().getConnection(LDAPConstants.TYPE_DC, domain);
                domainDN = LDAPConnUtils.getDNFromDomain(domain);
                dns = conn.searchAndReturnDNs(query, domainDN, false);
            } else {
                conn = user.getBrowseConn();
                domainDN = conn.getBaseDN();
                dns = conn.searchAndReturnDNs(query, domainDN, false);
            }
            if(dns == null || dns.length == 0) {
                throw new SystemException(SUB_LDAP_RESOLVETARGETDN, targetName);
            } else if(dns.length > 1) {
                throw new SystemException(SUB_LDAP_MULTIPLETARGETDN, targetName);
            }
            else {
	            return openSubWithPerm(user, dns[0], targettype, IAclConstants.WRITE_ACTION);
            }
        } catch (LDAPException lexp) {
            throw new SystemException(SUB_LDAP_RESOLVETARGETDN, targetName);
        } catch (NamingException nexp) {
            throw new SystemException(SUB_LDAP_RESOLVETARGETDN, targetName);
        }
    }

    /**
     * Retrieves a Subscription if the given admin has write access to the target (specific to target type "user").
     *
     * @param targetName The id of the target to create a subscription for
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param domain Domain in which the targetName should look for
     * @param user The principal to check for permission
     *
     * @return the Subscription if the specified admin has read permissions for the target
     *
     * @throws SystemException REMIND
     */
    public static ISubscription openSubForWriteForTargetTypeUser(String targetName,
                                                String targettype,
                                                String domain,
                                                IUser  user,
                                                boolean isADAutoDiscover)
            throws SystemException {
        IMainDataSourceContext objMgr = getMainDataSourceContext(user.getTenantName());
        IProperty mrbaConfig = objMgr.getMarimbaConfig();
        IProperty subConfig = objMgr.getSubscriptionConfig();

        // @todo - support to be extended for SunONE and ActiveDirectory
        String attr = subConfig.getProperty(LDAPConstants.CONFIG_MACHINENAMEATTR);
        String machineClass = mrbaConfig.getProperty(LDAPConstants.CONFIG_MACHINECLASS);
        String machineFilter = LDAPUtils.createSearchFilter(LDAPConstants.OBJECT_CLASS, "user");

        String query = "(&(" + attr + "=" + LDAPSearchFilter.escapeComponentValue(targetName) + ")" + machineFilter + ")";

        System.out.println("[openSubForWrite] Search Query: " + query);
        try {
            String[] dns = null;
            String domainDN = null;
            LDAPConnection conn = null;
            if (isADAutoDiscover) {
                // We need a DC connection here
                conn = user.getConnectionCache().getConnection(LDAPConstants.TYPE_DC, domain);
                domainDN = LDAPConnUtils.getDNFromDomain(domain);
                dns = conn.searchAndReturnDNs(query, domainDN, false);
            } else {
                conn = user.getBrowseConn();
                domainDN = conn.getBaseDN();
                dns = conn.searchAndReturnDNs(query, domainDN, false);
            }
            if(dns == null || dns.length == 0) {
                throw new SystemException(SUB_LDAP_RESOLVETARGETDN, targetName);
            } else if(dns.length > 1) {
                throw new SystemException(SUB_LDAP_MULTIPLETARGETDN, targetName);
            }
            else {
                return openSubWithPerm(user, dns[0], targettype, IAclConstants.WRITE_ACTION);
            }
        } catch (LDAPException lexp) {
            throw new SystemException(SUB_LDAP_RESOLVETARGETDN, targetName);
        } catch (NamingException nexp) {
            throw new SystemException(SUB_LDAP_RESOLVETARGETDN, targetName);
        }
    }

    /**
     * Retrieves a Subscription if the given principal has read access to the target.
     *
     * @param targetid The id of the target to create a subscription for
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param user The principal to check for permission
     *
     * @return if the specified admin has write permissions for the target, the subscription object which has fields that are loaded from the LDAP attributes
     *
     * @throws SystemException REMIND
     *
     */
    public static ISubscription openSubForRead(String targetid,
                                               String targettype,
                                               IUser  user)
        throws SystemException {
        return openSubWithPerm(user, targetid, targettype, IAclConstants.READ_ACTION);
    }
    /**
     * Open Subscription with search level
     * @param subname
     * @param user
     * @param oneLevel
     * @return
     * @throws SystemException
     */
	public static ISubscription openSubForReadNoError(String subname, IUser user, boolean oneLevel)
			throws SystemException {
		ISubscription sub = getSubscription(subname, user, oneLevel);

		if (sub != null) {
			if (!checkSubPerm(user, sub.getTargetID(), sub.getTargetType(),
					IAclConstants.READ_ACTION)) {
				sub = null;
			}
		}
		return sub;
	}
    /**
     * REMIND
     * Open Subscription with search default level
     * @param subname REMIND
     * @param user REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    public static ISubscription openSubForReadNoError(String subname,
                                                      IUser  user)
        throws SystemException {
        	return openSubForReadNoError(subname, user, true);
    }

    /**
     * Retrieves a Subscription if the given admin has write access to the target.
     *
     * @param targetid The id of the target to create a subscription for
     * @param targettype The type of the target.  This can be user, group, machine, collection
     * @param user The principal to check for permission
     *
     * @return the Subscription if the specified admin has read permissions for the target
     *
     * @throws SystemException REMIND
     */
    public static boolean deleteSub(String targetid,
                                    String targettype,
                                    IUser  user)
        throws SystemException {
        boolean del = hasSubPerm(user, targetid, targettype, IAclConstants.WRITE_ACTION);
        deleteSubscription(targetid, targettype, user);

        return del;
    }

    /**
     * REMIND
     *
     * @param subname REMIND
     * @param user REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    public static boolean deleteSubNoError(String subname,
                                           IUser  user)
        throws SystemException {
        if (hasSubPermNoError(user, subname, IAclConstants.WRITE_ACTION)) {
            deleteSubscription(subname, user);

            return true;
        } else {
            return false;
        }
    }

    /**
     * REMIND
     *
     * @param user REMIND
     * @param targetid REMIND
     * @param targettype REMIND
     * @param action REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public static boolean hasSubPerm(IUser  user,
                                     String targetid,
                                     String targettype,
                                     String action)
        throws SystemException {
        if (!checkSubPerm(user, targetid, targettype, action)) {
            throw new SubKnownException(ACL_NOSUBPERM, user.getProperty(LDAPConstants.PROP_USERRDN), action, targetid);
        }

        return true;
    }

    /**
     * REMIND
     *
     * @param user REMIND
     * @param subname REMIND
     * @param action REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    public static boolean hasSubPermNoError(IUser  user,
                                            String subname,
                                            String action)
        throws SystemException {
        ISubDataSource src = getSubDataSource(user);
        Subscription   sub = new Subscription(subname, src);
        if (sub.getTargetID() != null &&
            checkSubPerm(user, sub.getTargetID(), sub.getTargetType(), action)) {
            return true;
        } else {
            return false;
        }
    }

    static ISubscription openSubWithPerm(IUser  user,
                                         String targetid,
                                         String targettype,
                                         String action)
            throws SystemException {
        hasSubPerm(user, targetid, targettype, action);

        return createSubscription(targetid, targettype, user);
    }

	 public static List listSubscriptions(List targetDNS, IMainDataSourceContext main,
                                         ILDAPDataSourceContext srcCtx) throws SystemException {

		return listSubscriptions(targetDNS, null, main, srcCtx);

	 }

	public static List listSubscriptions(Date date, IMainDataSourceContext main,
                                         ILDAPDataSourceContext srcCtx) throws SystemException {

		return listSubscriptions(new ArrayList(), date, main, srcCtx);

	 }

    public static List listSubscriptions(List targetDNS, Date updateAfter, IMainDataSourceContext main,
                                         ILDAPDataSourceContext srcCtx) throws SystemException {
        LDAPConnection directory = srcCtx.getSubConn();
        ArrayList res = new ArrayList(100);
        try {
        	String vendor = directory.getVendor();
        	Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(vendor);
            LDAPName ldapname = directory.getParser();
            Iterator dns = targetDNS.iterator();
            StringBuffer sb = new StringBuffer();
            String cachedName;
            sb.append("(|");
            while ( dns.hasNext() ){

                cachedName =  (String) dns.next();
                if ( ldapname.isDN(cachedName) ) {
                    sb.append('(');
                    sb.append(LDAPVarsMap.get("TARGETDN"));
                    sb.append('=');
                    sb.append(cachedName);
                    sb.append(')');
                }  else {
                    if  ( "all_all".equalsIgnoreCase(cachedName) || "all".equalsIgnoreCase(cachedName)){
                        sb.append('(');
                        sb.append(LDAPVarsMap.get("TARGET_ALL"));
                        sb.append("=*)");
                    }
                }
            }
            if (sb.length() == 2 ) {
                sb.setLength(0);
                if (updateAfter != null){
                sb.append("(&(");
                sb.append(LDAPVarsMap.get("SM_ATTR"));
                sb.append("=*)");
	            sb.append("(");
		        sb.append(LDAPVarsMap.get("OP_MODIFYTIMESTAMP"));
		        sb.append(">=");
		        String formatedDate;
		        if (directory.getVendor().equals(LDAPConstants.VENDOR_ADAM) ||
		            directory.getVendor().equals(LDAPConstants.VENDOR_AD)  ) {
		            formatedDate = ADformatter.format(updateAfter);
		        } else {
		            formatedDate = NSformatter.format(updateAfter);
		        }
                 sb.append(formatedDate);
		         sb.append("))");
                }
            } else {
	            sb.insert(0,"(&");
	            sb.append(")(");
                sb.append(LDAPVarsMap.get("SM_ATTR"));
                sb.append("=*)");
	            if (updateAfter != null){
		            sb.append("(");
		            sb.append(LDAPVarsMap.get("OP_MODIFYTIMESTAMP"));
		            sb.append(">=");
		            String formatedDate;
		            if (directory.getVendor().equals(LDAPConstants.VENDOR_ADAM) ||
		                  directory.getVendor().equals(LDAPConstants.VENDOR_AD)  ) {
		                formatedDate = ADformatter.format(updateAfter);
		            } else {
		                formatedDate = NSformatter.format(updateAfter);
		            }
		            sb.append(formatedDate);
		            sb.append(")");
	            }
                sb.append(")");
            }
            if (sb.length() > 0) {
            	String[] subsAttr = LDAPUtils.getLDAPVarArr("subAttrs", vendor);
                NamingEnumeration nenum = directory.search(sb.toString(), subsAttr, main.getSubBaseWithNamespace(srcCtx), false);
                while (nenum.hasMoreElements()) {
                    SearchResult sr = (SearchResult) nenum.next();
                    Subscription sub = new Subscription(ObjectManager.getSubDataSource(srcCtx));
                    sub.getDataSource().load(sr, sub);
                    res.add(sub);
                }
            }
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne, ne.getMessage(), true);
        }
        return res;
    }

    /**
     * REMIND
     *
     * @param user REMIND
     * @param targetid REMIND
     * @param targettype REMIND
     * @param action REMIND
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     * @throws SubInternalException REMIND
     */
    public static boolean checkSubPerm(IUser  user,
                                       String targetid,
                                       String targettype,
                                       String action)
        throws SystemException {
//        boolean primaryAdmin = Utils.isPrimaryAdmin(((User)user).getUser());
//        IMainDataSourceContext objMgr = getMainDataSourceContext(user.getTenantName());
//        if (!primaryAdmin && objMgr.isAclsOn()) {
//            Target  target = new Target(targetid, targettype, targetid);
//
//            try {
//                if (IAclConstants.READ_ACTION == action) {
//                    return objMgr.getAclMgr().subReadPermissionExists(user, target, null, true, objMgr.getUsersInLDAP());
//                } else {
//                    return objMgr.getAclMgr().subWritePermissionExists(user, target, null, true, objMgr.getUsersInLDAP());
//                }
//            } catch (Exception ae) {
//                throw new SubInternalException(ae, ACL_MGRERROR);
//            }
//        }

        return true;
    }

	static {
		NSformatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'"); // modifytimestamp
		NSformatter.setTimeZone(TimeZone.getTimeZone("GMT"));

		ADformatter = new SimpleDateFormat("yyyyMMddHHmmss'.0Z'"); // whenchanged
		ADformatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	    }

}

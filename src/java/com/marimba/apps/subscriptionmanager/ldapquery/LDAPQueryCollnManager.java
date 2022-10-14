// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapquery;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

import javax.naming.*;
import javax.naming.directory.InvalidSearchFilterException;

import javax.servlet.ServletContext;

import com.marimba.apps.subscription.collection.CollectionContext;
import com.marimba.apps.subscription.collection.CollectionException;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.LDAPQueryInfo;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantAttributes;
import com.marimba.apps.subscriptionmanager.users.CLIUser;

import com.marimba.apps.subscriptionmanager.intf.ILDAPQueryColln;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.intf.msf.*;

import com.marimba.intf.msf.acl.IAclMgr;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.intf.msf.task.ITaskMgr;

import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IDirectory;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.db.IConnectionPool;

import com.marimba.tools.config.ConfigProps;

import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPException;
import com.marimba.tools.ldap.LDAPConnUtils;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain;
import com.marimba.tools.util.DebugFlag;

import com.marimba.webapps.intf.SystemException;

import com.marimba.apps.subscriptionmanager.cli.*;
import com.marimba.apps.subscriptionmanager.cli.commands.LdapQc;

/**
 * This class is the central point of access to perform operations on LDAP query collection objects.
 * It is used by <code>LDAPQueryTask</code> and <code>Commandline</code> classes.
 *
 * @author Narayanan. A R
 * @author Kumaravel. A
 * @version 1.2, 30/12/2004
 */
public class LDAPQueryCollnManager
    implements ILDAPQueryColln, ISubscriptionConstants {
    final static int            DEBUG = DebugFlag.getDebug("SUB/LDAPQC/MGR");
    private static final int SPM_LOG_OFFSET = 8900;
    
    LDAPQryLogMgr               logMgr;
    IAclMgr                     aclMgr;
    IAccessControlMgr           aCtrlMgr;
    ITaskMgr					taskmgr;
    IConfig                     ldapCfg;
    IConfig                     ic;
    LDAPEnv                     ldapEnv;
    String                      binddn;
    String                      password;
    IProperty                   subsCfg;
    IDirectory                  features;
    public static IServer                     server;
    LDAPConnection              globalCatalog;
    LDAPConnection              collConn;
    LDAPConnection              collConn1;
    LDAPQueryInfo               ldapQueryInfo;
    Vector                      previewResults;
    Vector                      results;
    ServletContext              context;
    ITenantManager tenantMgr;
    ITenant tenant;
    private LDAPQueryCollection ldapqc = null;
    SubscriptionMain main;
    ComplianceMain complianceMain;
    private CollectionContext collContext;

    /**
     * Constructs a new LDAP Query collection Manager object.
     *
     * @param context ServletContext
     * @param controller SusbcriptonMain
     */
    public LDAPQueryCollnManager(ServletContext   context,
                                 SubscriptionMain controller) {
        this.context        = context;
        collContext = new CollectionContext(controller.getAppLog(), SPM_LOG_OFFSET, controller.getChannel());
        features       = (IDirectory) context.getAttribute("com.marimba.servlet.context.features");
        server         = (IServer) features.getChild("server");
        ic             = (IConfig) features.getChild("tunerConfig");
        logMgr         = new LDAPQryLogMgr(controller.getAppLog(), controller);
        tenantMgr 	   = controller.getTenantManager();
        tenant		   = controller.getTenant();
        aclMgr         = tenant.getAclMgr();
        complianceMain = getComplianceMain();
        aCtrlMgr       = tenant.getAccessControlMgr();
        taskmgr		   = tenant.getTaskMgr();
        ldapCfg        = tenant.getActiveLdapCfg();
        binddn         = ldapCfg.getProperty("userrdn");
        password       = ldapCfg.getProperty("password");
        collContext.setLdapConfig(ldapCfg);
        previewResults = new Vector();
        results        = new Vector();
        main = controller;
        ldapQueryInfo  = new LDAPQueryInfo();

        initLDAP();
    }
    private ComplianceMain getComplianceMain() {
    	try {
	    	Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
			if(null == tenantsAttr) return null;
				TenantAttributes currentTenantAttr = tenantsAttr.get(tenant.getName());
				return currentTenantAttr.getCompMain();
    	} catch(Exception ec) {
    		ec.printStackTrace();
		}
		return null;
    }
    	
    public void setComplianceMain(ComplianceMain complianceMain) {
		this.complianceMain = complianceMain;
	}
	/**
     * This method returns the bind dn of the currently connected LDAP
     *
     * @return bind dn
     */
    public String getBindDn() {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.getBindDn()");
        }

        return binddn;
    }

    /**
     * This method returns the password
     *
     * @return password
     */
    public String getBindPassword() {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.getBindPassword()");
        }

        return password;
    }

    /**
     * Initializes the LDAP connection
     */
    public synchronized void initLDAP() {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager. initLDAP()");
        }

        try {
            this.ldapEnv = main.getLDAPEnv();
            this.globalCatalog = ldapEnv.createSubConfigConn();

            subsCfg = ldapEnv.getSubscriptionConfig();

            if (!validateSubConfig()) {
                logMgr.log(LOG_LDAP_NOSUBSCRIPTIONCONFIG, LOG_MAJOR, null);

                return;
            }
        } catch (SystemException e) {
            this.subsCfg = null;
            logMgr.log(LOG_LDAP_NOSUBSCRIPTIONCONFIG, LOG_MAJOR, e.toString());

            return;
        }

        setSubConfig(subsCfg);

        try {
            // get LDAP connection to Collections
            collConn = ldapEnv.createLDAPQueryCollConn(getBindDn(), getBindPassword());
        } catch (SystemException e) {
              logMgr.log(LOG_LDAP_CONNECTFAILED, LOG_MAJOR, (collConn == null) ? null : collConn.toString());

              return;
        }
    }

     public boolean isDataSourceExists() {
         if(!checkDataSource() || !isRCConfiguredAndRunning()) {
            return false;
        } else {
             return true;
         }
     }

    /**
     * Constructs and returns a LDAP query collection object
     *
     * @return LDAPQueryCollection
     */
    public LDAPQueryCollection getLDAPQueryCollection() {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.getLDAPQueryCollection()");
        }

        if (DEBUG > 4) {
            System.out.println(" Collection Name   :  " + ldapQueryInfo.getCollnName());
            System.out.println(" CreatedBy         :  " + ldapQueryInfo.getCreatedBy());
            System.out.println(" Filter            :  " + ldapQueryInfo.getFilter());
            System.out.println(" Lastrun           :  " + ldapQueryInfo.getLastrun());
            System.out.println(" Nextrun           :  " + ldapQueryInfo.getNextrun());
            System.out.println(" PageSize          :  " + ldapQueryInfo.getPageSize());
            System.out.println(" Query             :  " + ldapQueryInfo.getQuery());
            System.out.println(" Schedule          :  " + ldapQueryInfo.getSchedule());
            System.out.println(" SearchBase        :  " + ldapQueryInfo.getSearchBase());
        }

        return new LDAPQueryCollection(collContext, 
                                       logMgr, 
                                       aclMgr, 
                                       aCtrlMgr.getRoleMap(), 
                                       collConn, 
                                       ldapQueryInfo, 
                                       subsCfg, 
                                       ldapCfg, 
                                       ldapEnv, 
                                       true, complianceMain);
    }
    /**
     *  Registers the query object retrived from CommandLine or LDAPQueryTask classes.
     *
     * @param ldapQueryInfo the collection object to be processed with
     *
     * @throws NumberFormatException if the property for VLV page size is not a number in prefs.txt
     */
    public void setQuery(LDAPQueryInfo ldapQueryInfo) throws NumberFormatException {

        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.setQuery(LDAPQueryInfo ldapQueryInfo)");
        }
        this.ldapQueryInfo = ldapQueryInfo;
    }

    /**
     * This method is not exposed to users.Since it deletes the task provided by the user.
     *
     * @return true if the given query task is removed from the CMS Task Manager.
     */
    public boolean remove() throws NameNotFoundException, NoPermissionException, NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.remove()");
        }
        boolean isRemoved = false;
        try {
            ldapqc = getLDAPQueryCollection();
            if (!ldapqc.isOperationPermitted()) {
                throw new NoPermissionException("unauthorized user");
            }
            if (taskmgr.getTask("LDAPQueryTasks",ldapQueryInfo.getCollnName()) == null) {
                throw new NameNotFoundException("Object does not exist");
            }
            isRemoved = taskmgr.removeTask("LDAPQueryTasks", ldapQueryInfo.getCollnName());
        } catch (NameNotFoundException nfe) {
            if (DEBUG > 6) {
                nfe.printStackTrace();
            }
            throw nfe;
        } catch (NoPermissionException npe) {
            if (DEBUG > 6) {
                npe.printStackTrace();
            }
            throw npe;
        } catch (NamingException ne) {
            if (DEBUG > 6) {
                ne.printStackTrace();
            }
            throw ne;
        } catch (Exception e) {
            logMgr.log(LOG_LDAPQC_REMOVETASK_FAILED, LOG_MAJOR, e.toString());
        }

        return isRemoved;
    }

     /**
     * Removes the task of all the query collection objects
     */
    public int removeTaskAll() throws NameNotFoundException, NoPermissionException, NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.removeTaskAll()");
        }
        String currentUser = null;

        try {
            ldapqc = getLDAPQueryCollection();
            currentUser = ldapQueryInfo.getCreatedBy();
            if (!ldapqc.isOperationPermittedForTask(currentUser)){
                throw new NoPermissionException("unauthorized user");
            }
            Enumeration taskEnum = taskmgr.listGroup("LDAPQueryTasks");
            int count = 0;
            while (taskEnum.hasMoreElements()) {
                String cname = (String) taskEnum.nextElement();
                if (!taskmgr.removeTask("LDAPQueryTasks",cname)) {
                    logMgr.log(LOG_LDAPQC_REMOVETASK_FAILED, LOG_MAJOR, cname);
                } else {
                    count++;
                }
            }
            return count;
        } catch (NoPermissionException npe) {
            if (DEBUG > 6) {
                npe.printStackTrace();
            }
            throw npe;
        } catch (NamingException ne) {
            if (DEBUG > 6) {
                ne.printStackTrace();
            }
            throw ne;
        }
    }

    /**
     * This method adds the task provided by the user.
     *
     * @return true if the given query task is added to the CMS Task Manager.
     */
    public boolean add() throws NoPermissionException, NamingException, InvalidSearchFilterException, NameNotFoundException, InvalidNameException {
        boolean succeed = false;
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.add()");
        }
        try {
            ldapqc = getLDAPQueryCollection();
            if (!ldapqc.isOperationPermitted()) {
                throw new NoPermissionException("unauthorized user");
            }
            if (!ldapqc.exists()) {
                throw new NameNotFoundException("Object does not exist");
            }

            Vector v = ldapqc.list();
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                LDAPQueryInfo info = (LDAPQueryInfo) e.nextElement();
                succeed = add(info);
            }
        } catch (InvalidSearchFilterException isfe) {
            if (DEBUG > 6) {
                isfe.printStackTrace();
            }
            throw isfe;
        } catch (NameNotFoundException nfe){
            if (DEBUG > 6) {
                nfe.printStackTrace();
            }
            throw nfe;
        } catch (InvalidNameException ine) {
            if (DEBUG > 6) {
                ine.printStackTrace();
            }
            throw ine;
        } catch (NoPermissionException npe) {
            if (DEBUG > 6) {
                npe.printStackTrace();
            }
            throw npe;
        } catch (NamingException ne) {
            if (DEBUG > 6) {
                ne.printStackTrace();
            }
            throw ne;
        }
        return succeed;
    }

    public boolean add(LDAPQueryInfo info){
        boolean succeed = false;
        String querySchedule = info.getSchedule();
        String createdBy = info.getCreatedBy();
        String queryname = info.getCollnName();
        if (!((querySchedule).equals("null")  || "never".equals(querySchedule))) {
            IConfig taskConfig = null;
            // Check if the query is created with Schedule
            taskConfig = new ConfigProps(null);
            taskConfig.setProperty("task.schedule", querySchedule);
            taskConfig.setProperty("collnName", queryname);
            taskConfig.setProperty("createdBy", createdBy);
            succeed = taskmgr.addTask("PolicyCompliance/ldaptask", "LDAPQueryTasks", queryname, taskConfig, true);
        } else {
            logMgr.log(LOG_LDAPQC_ADDTASK_FAILED, LOG_MAJOR, null);
        }
        return succeed;
    }

    /**
     * Adds the task to all the query collection objects
     */
    public boolean addTaskAll(LdapQc ldapQc) throws NameNotFoundException, NoPermissionException, NamingException {
        boolean succeed = true;
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.addTaskAll()");
        }

        String currentUser = null;
        try {
            ldapqc = getLDAPQueryCollection();
            currentUser = ldapQueryInfo.getCreatedBy();
            if (!ldapqc.isOperationPermittedForTask(currentUser)) {
                throw new NoPermissionException("unauthorized user");
            }
            Vector v = ldapqc.listAll();
            List failedTasks = new ArrayList();
            boolean isOneSuccess = false;
            boolean isOneFail = false;
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                LDAPQueryInfo info = (LDAPQueryInfo) e.nextElement();
                succeed = add( info );
                if (succeed == true) {
                    isOneSuccess = true;
                } else if (succeed == false) {
                    isOneFail = true;
                    failedTasks.add(info.getCollnName());
                }
            }
            if (isOneSuccess && !isOneFail) {
                ldapQc.printMessage("All Tasks are added Successfully");
            } else if (isOneFail && isOneSuccess) {
                ldapQc.printMessage("Adding few tasks failed: ");
                for (int i = 0; i < failedTasks.size(); i++ ) {
                    ldapQc.printMessage("\nAdding Task for " +(String)failedTasks.get(i) + " failed");
                }
            } else {
                ldapQc.printMessage("Adding all Tasks Failed");
            }
        } catch (NoPermissionException npe) {
            if (DEBUG > 6) {
                npe.printStackTrace();
            }
            throw npe;
        } catch (NamingException ne) {
            if (DEBUG > 6) {
                ne.printStackTrace();
            }
            throw ne;
        }
        return succeed;
    }
    /**
     * Creates the query collection object.If the collection object already exits it throws a naming exception
     * If the collection object is created with a schedule, then it registers the collection ojbect with the CMS task manager
     * to run on schedule.
     *
     * @return true if the create operatio is successful
     *
     * @throws NamingException if the new Collection name is not unique.
     */
    public boolean create() throws NoPermissionException, NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.create()");
        }

        try {
            ldapqc = getLDAPQueryCollection();

            if (ldapqc.exists()) {
                throw new NamingException("Query name is not unique");
            }

            if (ldapqc.create()) {
                IConfig  taskConfig = null;

                // Check if the query is created with Schedule
                if (ldapQueryInfo.getSchedule() != null) {
                    if (!"never".equals(ldapQueryInfo.getSchedule())) {
                        taskConfig = new ConfigProps(null);
                        taskConfig.setProperty("task.schedule", ldapQueryInfo.getSchedule());
                        taskConfig.setProperty("collnName", ldapQueryInfo.getCollnName());
                        taskConfig.setProperty("createdBy", ldapQueryInfo.getCreatedBy());
                        taskmgr.addTask("PolicyCompliance/ldaptask", "LDAPQueryTasks", ldapQueryInfo.getCollnName(), taskConfig, false);
                    }
                }
            } else {
                logMgr.log(LOG_LDAPQC_CREATION_FAILED, LOG_MAJOR, null);
            }
        } catch (NoPermissionException npe) {
              throw npe;
        }
        catch (NamingException ne) {
            logMgr.log(LOG_LDAPQC_CREATION_FAILED, LOG_MAJOR, ne.toString());
            throw ne;
        }
        return true;
    }

    /**
     * Deletes the LDAP query collection object.
     * <p>
     * If the object is not present in the LDAP, throws the NameNotFoundException.
     * Checks whether the user has the appropriate permission to delete the collection. Otherwise throws System exception.
     * If the deletion is successful, then it removes its task from task manager.
     *
     * @return true if the delete operation is successful.
     *
     * @throws NameNotFoundException if the collection object does not exist
     * @throws NamingException if the user is not permitted to perform the delete operation.
     */
    public boolean delete() throws NameNotFoundException, NoPermissionException, NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.delete()");
        }

        boolean isRemoved = false;

        try {
            ldapqc = getLDAPQueryCollection();

            if (!ldapqc.exists()) {
                throw new NameNotFoundException("Object does not exists");
            }
            if(!ldapqc.isOperationPermitted()) {

                throw new NamingException("unauthorized user");
            }
            if (ldapqc.deleteObject()) {
                isRemoved = taskmgr.removeTask("LDAPQueryTasks", ldapQueryInfo.getCollnName());

                if (isRemoved) {
                    logMgr.log(LOG_LDAPQC_REMOVETASK_FAILED, LOG_MAJOR, ldapQueryInfo.getCollnName());
                }
            }
        } catch (NameNotFoundException ne) {
            if(DEBUG >6){
                ne.printStackTrace();
            }
            throw ne;
        } catch (NoPermissionException npe) {
           if(DEBUG >6){
                npe.printStackTrace();
           }
           throw npe;
        } catch (NamingException ne) {
            if(DEBUG >6){
                ne.printStackTrace();
            }
            throw ne;
        }

        return true;
    }

    /**
     * Deletes all the LDAP query collection objects.
     * <p>
     * If the object is not present in the LDAP, throws the NameNotFoundException.
     * Checks whether the user has the appropriate permission to delete the collection. Otherwise throws System exception.
     * If the deletion is successful, then it removes its task from task manager.
     *
     * @return true if the delete operation is successful.
     *
     * @throws NameNotFoundException if the collection object does not exist
     * @throws NamingException if the user is not permitted to perform the delete operation.
     */

    public int deleteAll() throws NameNotFoundException, NoPermissionException, NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.deleteAll()");
        }
        String currentUser = null;
        boolean isRemoved = false;

        try {
            ldapqc = getLDAPQueryCollection();
            currentUser =  ldapQueryInfo.getCreatedBy();

            Vector namesVector = new Vector();
            namesVector.addAll(ldapqc.listAll());

            Object arr[] = namesVector.toArray();

            int count = 0;

            if(arr.length != 0) {
                for( int i = 0;  i < arr.length; i++ ) {

                    LDAPQueryInfo colln =(LDAPQueryInfo) arr[i];
                    if(ldapqc.setColln(colln)) {
                        // check whether the user has the sufficient privillege to delete this collection.
                        if(ldapqc.isOperationPermitted(currentUser)) {
                            if(ldapqc.deleteObject()) {
                                count ++;
                                isRemoved = taskmgr.removeTask("LDAPQueryTasks", colln.getCollnName());
                                if (!isRemoved) {
                                    logMgr.log(LOG_LDAPQC_REMOVETASK_FAILED, LOG_MAJOR, colln.getCollnName());
                                }
                            }
                        }
                    }
                }
            }
            return count;
        } catch (NoPermissionException npe) {
           if(DEBUG >6){
                npe.printStackTrace();
           }
           throw npe;
        } catch (NamingException ne) {
            if(DEBUG >6){
                ne.printStackTrace();
            }
            throw ne;
        }
    }


    /**
     * Runs the collection object to create a dyanmic group of users and/or machines.
     * <p>
     * If the collection object is not present in the LDAP, it throws <code>NameNotFoundException</code>.
     * Also checks whether the logged in user has the appropriate permission to refresh the collection.
     * Otherwise throws <code>NamingException</code>. If the the LDAP query is found to be invalid,
     * it throws <code>InvalidSearchFilterException</code>.
     *
     * @return REMIND
     *
     * @throws NameNotFoundException if the collection object does not exist
     * @throws NamingException if the user is not permitted to perform the refresh operation
     * @throws InvalidSearchFilterException if LDAP query syntax is not correct
     */
    public boolean refresh() throws InvalidSearchFilterException, NameNotFoundException, InvalidNameException, NoPermissionException, NamingException, SQLException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.refresh()");
        }

        try {
            ldapqc = getLDAPQueryCollection();

            if (!ldapqc.exists()) {

                throw new NameNotFoundException("Object does not exist");
            }

            if(!ldapqc.isOperationPermitted()) {

                throw new NamingException("unauthorized user");
            }
            if (ldapqc.runQuery()) {
                if (ldapqc.commit()) {
                    logMgr.log(LOG_LDAPQC_RUN_SUCEEDED, LOG_AUDIT, null, null, LDAPQUERY_RUN);
                } else {
                    logMgr.log(LOG_LDAPQC_RUN_FAILED, LOG_CRITICAL, null);
                }
            } else {
                    logMgr.log(LOG_LDAPQC_RUN_FAILED, LOG_CRITICAL, null);
            }
        } catch (InvalidSearchFilterException isfe) {
            if(DEBUG >6){
                isfe.printStackTrace();
            }
            throw isfe;
        } catch (NameNotFoundException nfe) {
            logMgr.log(LOG_LDAPQC_INTERNAL_EXCEPTION,LOG_MAJOR,ldapQueryInfo.getCollnName());
            throw nfe;
        } catch (InvalidNameException ine) {
            if(DEBUG >6){
                ine.printStackTrace();
            }
            throw ine;
        } catch (NoPermissionException npe) {
           if(DEBUG >6){
                npe.printStackTrace();
           }
           throw npe;
        } catch (NamingException ne) {
            if(DEBUG >6){
                ne.printStackTrace();
            }
            throw ne;
        } catch(AclStorageException ase) {
            if(DEBUG >6){
                ase.printStackTrace();
            }
            return false;
 	} catch(AclException ae) {
            if(DEBUG >6){
                ae.printStackTrace();
            }
            return false;
        } catch(LDAPException le) {
            if(DEBUG >6){
                le.printStackTrace();
            }
            return false;
        } catch(CollectionException ce) {
            if(DEBUG >6){
                ce.printStackTrace();
            }
            return false;
        } catch (SQLException sql){
            throw sql;
        }
        return true;
    }

    /**
     * Modifies the collection object.
     * <p>
     * If the collection object is not present in the LDAP, it throws NameNotFoundException.
     * Also checks whether the logged in user has the appropriate permission to refresh the collection.
     * Otherwise throws a NamingException. If a schedule is modified, the existing task from
     * the task manager is removed and registers a new task with the new schedule.
     *
     * @return true  - if the collection modified successfully.
     *
     * @throws NameNotFoundException if the collection object does not exist
     * @throws NamingException if the user is not permitted to perform the modify operation.
     */
    public boolean modify() throws NameNotFoundException, NoPermissionException, NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.modify()");
        }

        boolean isRemoved = false;

        try {
            ldapqc = getLDAPQueryCollection();

            if (!ldapqc.exists()) {

                throw new NameNotFoundException("Object does not exist");
            }
            if(!ldapqc.isOperationPermitted()) {

                throw new NamingException("unauthorized user");
            }
            if (ldapqc.modify()) {
                if (ldapQueryInfo.getSchedule() != null) {
                    isRemoved = taskmgr.removeTask("LDAPQueryTasks", ldapQueryInfo.getCollnName());

                    if (!isRemoved) {
                        logMgr.log(LOG_LDAPQC_REMOVETASK_FAILED, LOG_MAJOR, ldapQueryInfo.getCollnName());
                    }

                    if (!"never".equals(ldapQueryInfo.getSchedule())) {
                        IConfig taskConfig = null;
                        // Check if the query is created with Schedule
                        taskConfig = new ConfigProps(null);
                        taskConfig.setProperty("task.schedule", ldapQueryInfo.getSchedule());
                        taskConfig.setProperty("collnName", ldapQueryInfo.getCollnName());
                        taskConfig.setProperty("createdBy", ldapQueryInfo.getCreatedBy());
                        taskmgr.addTask("PolicyCompliance/ldaptask", "LDAPQueryTasks", ldapQueryInfo.getCollnName(), taskConfig, false);
                    }
                }
            }
        } catch (NameNotFoundException nnfe) {
           if(DEBUG >6){
                nnfe.printStackTrace();
           }
           throw nnfe;
        } catch (NoPermissionException npe) {
           if(DEBUG >6){
                npe.printStackTrace();
           }
           throw npe;
        } catch (NamingException ne) {
            if(DEBUG >6){
                 ne.printStackTrace();
            }
            throw ne;
        }
        return true;
    }

    /**
     * Previews the output by executing the LDAP Query in the given collection object.
     * <p>
     * If the collection object is not present in the LDAP, it throws <code>NameNotFoundException</code>.Checks whether
     * the database has been configured. Also checks whether the logged in user has
     * the appropriate permission to preview the collection. Otherwise throws a SystemException.
     *
     * @return REMIND
     *
     * @throws NameNotFoundException if the collection object does not exist
     * @throws NamingException if the user is not permitted to perform the preview operation
     */
    public boolean preview() throws NameNotFoundException, NamingException, SQLException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.preview()");
        }

        Vector v = new Vector();

        try {
            ldapqc = getLDAPQueryCollection();
            
            if (ldapQueryInfo.getQuery() != null) {

                v = ldapqc.previewQuery();

            } else {
                if (!ldapqc.exists()) {
                    throw new NameNotFoundException("Object does not exist");
                }
                if(!ldapqc.isOperationPermitted()) {
                    throw new NamingException("unauthorized user");
                }
                v = ldapqc.previewExtsgQuery();
            }

            previewResults = null;
            setPreviewResults(v);
        } catch (InvalidSearchFilterException isf) {
            if(DEBUG >6){
                isf.printStackTrace();
            }
            throw isf;
        } catch (CommunicationException e) {
            if(DEBUG >6){
                e.printStackTrace();
            }
            return false;
        } catch(NameNotFoundException nnfe) {
            if(DEBUG >6){
                nnfe.printStackTrace();
            }
            throw nnfe;
        } catch (InvalidNameException ine) {
            if(DEBUG >6){
                ine.printStackTrace();
            }
            throw ine;
        } catch (NamingException ne) {
            if(DEBUG >6){
                ne.printStackTrace();
            }
            throw ne;
        } catch(AclStorageException ase) {
            if(DEBUG >6){
                ase.printStackTrace();
            }
            return false;
 	} catch(AclException ae) {
            if(DEBUG >6){
                ae.printStackTrace();
            }
            return false;
        } catch(LDAPException le) {
            if(DEBUG >6){
                le.printStackTrace();
            }
            return false;
        } catch (SQLException sql) {
            if(DEBUG >6){
                sql.printStackTrace();
            }
            throw sql;
        }
        return true;
    }

    /**
     * Lists all query collection objects from the LDAP query collection container.
     *
     * @return true if listing of all collection is succeeded
     *
     * @throws NamingException if search for collection object fails.
     */
    public boolean listAll() throws NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.listAll()");
        }

        Vector v = new Vector();

        try {
            ldapqc = getLDAPQueryCollection();
            v      = ldapqc.listAll();
            setResults(v);
        }
        catch (NamingException ne) {

            throw ne;
        }

        return true;
    }

    /**
     * Lists the details of a particular collection object.
     *
     * @return true if listing the details of a particular collection is succeeded
     *
     * @throws NameNotFoundException if the collection object does not exist
     * @throws NamingException if search for collection object fails
     */
    public boolean list() throws NameNotFoundException, NamingException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.list()");
        }

        Vector v = new Vector();

        try {
            ldapqc = getLDAPQueryCollection();

            if (!ldapqc.exists()) {
                throw new NameNotFoundException("Object does not exist");
            }

            v = ldapqc.list();
            setResults(v);
        } catch (NameNotFoundException nfe) {

            throw nfe;
        } catch (NamingException ne) {

            throw ne;
        }
        return true;
    }

    /**
     * Shows the preview of the ldap query collection that has been already created.
     *
     * @return true if the preview is succeeded.
     *
     * @throws NameNotFoundException
     * @throws NamingException
     */
    public boolean previewExtsgQuery() throws NameNotFoundException, InvalidNameException, NamingException, SQLException {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.previewExtsgQuery()");
        }

        Vector v = new Vector();

        try {
            ldapqc = getLDAPQueryCollection();

            if (!ldapqc.exists()) {
                throw new NameNotFoundException("Object does not exist");
            }

            v = ldapqc.previewExtsgQuery();
            setPreviewResults(v);
        } catch(NameNotFoundException nfe) {
            if(DEBUG >6){
                nfe.printStackTrace();
            }
           throw nfe;
        } catch (CommunicationException e) {
            if(DEBUG >6){
                e.printStackTrace();
            }
           return false;
        } catch (InvalidNameException ine) {
            if(DEBUG >6){
                ine.printStackTrace();
            }
           throw ine;
        }
        catch (NamingException ne) {
            if(DEBUG >6){
                ne.printStackTrace();
            }
            throw ne;
        } catch(AclStorageException ase) {
            if(DEBUG >6){
                ase.printStackTrace();
            }
            return false;
 	} catch(AclException ae) {
            if(DEBUG >6){
                ae.printStackTrace();
            }
            return false;
        } catch(LDAPException le) {
            if(DEBUG >6){
                le.printStackTrace();
            }
            return false;
        } catch(SQLException sql) {
            if(DEBUG >6){
                sql.printStackTrace();
            }
            throw sql;
        }
        return true;
    }

    /**
     * Stores the preview results in a vector to refer to it back in the <code>Commandline.java</code>
     *
     * @param results the preview results obtained from the <code>preview</code> method.
     */
    public void setPreviewResults(Vector results) {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.setPreviewResults(Vector v)");
        }

        this.previewResults = results;
    }

    /**
     * Gets the results of the previous preview operation.
     *
     * @return the results obtained from preview operation.
     */
    public Vector getPreviewResults() {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.getPreviewResults()");
        }

        return this.previewResults;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Vector getResults() {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.getResults()");
        }

        return this.results;
    }

    /**
     * REMIND
     *
     * @param v REMIND
     */
    public void setResults(Vector v) {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.setResults(Vector v)");
        }

        this.results = v;
    }

    /**
     * Set the subscription config.
     *
     * @param subsCfg REMIND
     */
    public void setSubConfig(IProperty subsCfg) {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.setSubConfig(IProperty subsCfg)");
        }

        this.subsCfg = subsCfg;
    }

    /**
     * Verifies whether the properties Ldap container base, collection schedule property are have been set in
     * subscription configuration object. Otherwise, it returns false.
     *
     *  @return true if the configurations have been set properly, returns true.
     */
    boolean validateSubConfig() {
        if (DEBUG > 4) {
            System.out.println("LDAPQueryCollnManager.validateSubConfig()");
        }

        String value = subsCfg.getProperty(LDAPConstants.CONFIG_LDAPCOLLECTIONBASE);

        if ((value == null) || (value.length() == 0)) {
            logMgr.log(LOG_LDAPQC_BASENOTSET, LOG_CRITICAL, null);

            return false;
        }

        value = subsCfg.getProperty(LDAPConstants.CONFIG_LDAP_QUERY_COLLN_SCHED);

        if ((value == null) || (value.length() == 0)) {
            logMgr.log(LOG_LDAPQC_SCHEDULENOTSET, LOG_CRITICAL, null);

            return false;
        }

        return true;
    }

    /**
     * Lists all the tasks in the CMS. This method is not exposed to users. But is used internally to list all
     * cms tasks for debugging purposes.
     *
     * @return true
     */
    public boolean listAllTasks() {
        Vector      tasks = new Vector();
        Enumeration taskEnum = taskmgr.listGroup("LDAPQueryTasks");
        while (taskEnum.hasMoreElements()) {
            String cname = (String) taskEnum.nextElement();
            tasks.add(cname);
        }

        setResults(tasks);

        return true;
    }

     /**
     * Check if database connection is on.
     *
     * @return true  if able to get the Database connection from the pool.
     */
    protected boolean checkDataSource() {

          IStatementPool sp = null;
          DbSourceManager db = null;
          try{
             db = new DbSourceManager(this.complianceMain);
             sp = db.getPool();
             return true;
          } catch(Exception se){
              logMgr.log(LOG_LAPQC_DB_CONNECTFAILED,LOG_MAJOR, se.toString());
          } finally {
              if(sp!=null) {
                  try {
                  db.returnPool(sp);
                  } catch(Exception e){
                      if(DEBUG >6){
                          e.printStackTrace();
                      }
                  }
              }
          }
       return false;
    }
    protected boolean isRCConfiguredAndRunning() {
        boolean isRCRunning = false;
        String RC_CONTEXT_PATH ="/im";
        String DEFAULT_DATASOURCE = "default.datasoruce";
        //static final String RC_CONFIG_NAME			= "reportcenter-config";
        String RC_CONFIG_NAME			= "reportcenter-config";
    	// check to make sure Report Center is running
	IWebApplicationMgr webMgr = server.getWebApplicationMgr();
	IWebApplicationInfo rcInfo = webMgr.getApplication(RC_CONTEXT_PATH);
	if (rcInfo != null) {
	    isRCRunning = rcInfo.isRunning();
	} else {
	    logMgr.log(LOG_LAPQC_DB_CONNECTFAILED, LOG_MAJOR, null);
            return false;
	}
        return isRCRunning;
    }
}
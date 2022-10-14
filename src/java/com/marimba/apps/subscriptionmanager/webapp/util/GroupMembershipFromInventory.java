package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
//import com.marimba.apps.subscriptionmanager.compliance.ComplianceEngine;
import com.marimba.apps.subscription.common.intf.IMainDataSourceContext;
import com.marimba.intf.db.IDatabaseClient;
import com.marimba.intf.msf.query.IQueryMgrContext;
import com.marimba.intf.msf.query.QueryManagerException;
import com.marimba.intf.msf.query.IQueryMgr;
import com.marimba.intf.msf.query.IQueryResult;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.IServer;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.target.ICredentials;
import com.marimba.intf.target.ITarget;
import com.marimba.intf.target.ITargetList;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.tools.ldap.LDAPConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.DriverManager;

import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

// Copyright 1997-2004, Marimba Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.


/**
 * Created by IntelliJ IDEA.
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 * $File$
 *
 * Use this class to resolve all the members of a given Group/Container.
 * Currently the type of members returned is specified by the caller
 * The Group/Container membership as resolved from the LDAP may be
 * different from the Inventory DB due to the latency issues and inventory
 * scan not being run.
 * This is currently customized for a very specific requirment.
 */
public class GroupMembershipFromInventory implements IErrorConstants {
    private String baseDN;
    private IServer sIServer;
    private GroupMembershipFromInventory groupMembershipFromInventory;
    private Logger           sLogger = Logger.getLogger(GroupMembershipFromInventory.class);
    private IMainDataSourceContext sIMainDataSourceContext;
    private ITenant tenant;

    public GroupMembershipFromInventory(IMainDataSourceContext inIMainDataSourceContext, ITenant tenant) {
        initialize(inIMainDataSourceContext, tenant);
    }

    private void initialize(IMainDataSourceContext inIMainDataSourceContext, ITenant tenant) {
        sIMainDataSourceContext = inIMainDataSourceContext;
        sIServer = (IServer) inIMainDataSourceContext.getFeatures().getChild("server");
        this.tenant = tenant; 
    }


    protected IQueryMgrContext getQueryContext(IUserPrincipal inIUserPrincipal)
            throws QueryManagerException, SystemException {


        IQueryMgrContext ret = null;

        IQueryMgr        qm = getQueryMgr();

        if (qm == null) {
            CriticalException theCriticalException = new CriticalException(PC_NOQUERYCONTEXT);
            throw theCriticalException;
        } else {
            ret = qm.createContext(inIUserPrincipal);
        }



        return ret;
    }

    protected IQueryMgr getQueryMgr() throws SystemException {
    	
        IQueryMgr ret = null;
        try {
        	ret = DbSourceManager.getQueryManager(tenant);
        } catch(Exception ec) {
            if (ret == null) {
                CriticalException theCriticalException = new CriticalException(PC_NOQUERYCONTEXT);
                throw theCriticalException;
            }
        }
        return ret;
    }
    public GroupMembershipFromInventory getInstance(IMainDataSourceContext inIMainDataSourceContext, ITenant tenant) {
         if (groupMembershipFromInventory == null) {
             groupMembershipFromInventory = new GroupMembershipFromInventory(inIMainDataSourceContext, tenant);

         }

         return groupMembershipFromInventory;
     }

    // this should not actually try to use a list of this stuff.  In the worst case, we'll be making a list of 100000 items.  We'll choke for sure.
    public String[] getTargetList(String baseDN, IUserPrincipal inIUserPrincipal) throws SQLException, QueryManagerException, SystemException {

        String[]  ret = null;
        IQueryMgrContext context = getQueryContext(inIUserPrincipal);

        baseDN = getCanonicalDNString(baseDN);

        if (context != null) {
            String theQueryString;

            theQueryString = "select distinct t3.name,t3.rpcport,t3.rpcsslport from inv_subscriptiontargets t1,inv_subscriptiontargets t2, inv_tuner t3" +
                             " where t1.machine_id = t2.machine_id" +
                             "   and t2.type = 'machine'" +
                             "   and t1.target_name ='" + baseDN + "'" +
                             "   and t1.machine_id = t3.machine_id";

            sLogger.info("SPM Push query = " + theQueryString);

            IQueryResult result = context.execute(theQueryString);

            if (result != null) {
                ret = queryResultToArray(result);
            }

        } else {
            CriticalException theCriticalException = new CriticalException(PC_NOQUERYCONTEXTREAD);
            throw theCriticalException;
        }



        return ret;
    }
       public String getCanonicalDNString(String inString) {

        String         ret = inString;
        LDAPConnection theLDAPConnection;
        theLDAPConnection = sIMainDataSourceContext.getAdminUser().getBrowseConn();

        try {
            ret = theLDAPConnection.getParser().getCanonicalName(inString);
        } catch (Exception e) {
        }

        ret = ret.toLowerCase();


        return ret;
    }

    private String[] queryResultToArray(IQueryResult resultSet ) {
        String [] ret = null;
        int count;
        try {

            if (resultSet != null) {
                resultSet.last();
                count = resultSet.getRow();
                resultSet.beforeFirst();
                ret = new String[count];

                boolean secure;

                int port, sslport;
                String targetName;
                int index = 0;
                while(resultSet != null && resultSet.next()) {

                    port = resultSet.getInt("rpcport");
                    targetName = resultSet.getString("name");
                    sslport = resultSet.getInt("rpcsslport");
                    secure = !resultSet.wasNull();

                    ret[index++] = (secure ? "https" : "http") +
                            "://" + targetName + ":" + (secure ? sslport : port);

                }
                return ret;
            }
        } catch (SQLException e) {

        }

        return ret;
    }


}

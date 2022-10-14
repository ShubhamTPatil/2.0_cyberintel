// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.service;

import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.core.*;
import com.marimba.apps.subscriptionmanager.compliance.queue.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.query.*;
import com.marimba.apps.subscriptionmanager.compliance.result.*;
import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.ITenant;
import org.apache.log4j.*;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Implement general service relate methods
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
public class Service {

    ComplianceMain main;
    ReportQueue rptQueue;
    QueryQueue qryQueue;
    ICacheManager cache;
    ConfigManager cfgMgr;
    IUserPrincipal user;
    HttpSession curSession = null;
    ITenant tenant;
    
    public Service(ComplianceMain main,IUserPrincipal _user,ITenant tenant) {
        this.main = main;
        this.tenant = tenant;
        this.user = _user;
        this.rptQueue = main.getQueueManager().getReportQueue();
        this.qryQueue = main.getQueueManager().getQueryQueue();
        this.cache = main.getCache();
        this.cfgMgr = main.getConfig();
    }

    public void setSession( HttpSession session ){
        curSession = session;
    }

    public HttpSession getSession(){
        return curSession; 
    }

    public void setPageInfo(ComplianceListQueryObject obj, int page) {
        obj.setPageSize(cfgMgr.getInt(ConfigManager.CFG_LIST_MAX));
        obj.setNumOfCachedPages(1);
        obj.setPageToGet(page);
    }

    public void setCacheInfoForObject(ComplianceQueryObject obj) {
        obj.setMaxCache(1000 * (long) cfgMgr.getInt(ConfigManager.CFG_CACHE_OBJ_MAX));
        obj.setCacheExt(1000 * (long) cfgMgr.getInt(ConfigManager.CFG_CACHE_OBJ_EXT));
    }

    public void setCacheInfoForList(ComplianceListQueryObject obj) {
        obj.setMaxCache(1000 * (long) cfgMgr.getInt(ConfigManager.CFG_CACHE_LIST_MAX));
        obj.setCacheExt(1000 * (long) cfgMgr.getInt(ConfigManager.CFG_CACHE_LIST_EXT));
    }

    public IComplianceQuery[] doBatch( IComplianceQuery[] queries, boolean expireCache, boolean doQuery, int waitSeconds ){
        IComplianceQuery query = null;
        ArrayList toAdd = new ArrayList();
        for( int index = 0; index < queries.length; index++ ){
            query = checkExpire( queries[ index ], expireCache );
            if( query == null && doQuery ){
                query = queries[ index ];
                toAdd.add( query );
            } else {
                queries[ index ] = query;
            }
        }
        qryQueue.addBatch( toAdd );
        return queries;
    }

    /**
     * Do a query for this qry object
     * Expire the cache for this object if expireCache is true
     * Do query if the object is not in the cache if doQuery is true
     * Wait seconds for the result when do a query
     */
    public IComplianceQuery doQuery(IComplianceQuery qry, boolean expireCache, boolean doQuery, int waitSeconds) {
        IComplianceQuery query = null;
        query = checkExpire( qry, expireCache );

        if ( query == null && doQuery) {
            qry = (IComplianceQuery) qryQueue.add(qry);
            qry = setWaitTime( qry, waitSeconds );
        } else if ( query != null) {
            qry = query;
        }

	    return qry;
    }

    private IComplianceQuery checkExpire( IComplianceQuery query, boolean expireCache ){
        IComplianceQuery qry = null;

        if (expireCache) {
            cache.expire( query );
        } else {
            qry = getQuery( query ); 
        }

        return qry;
    }

    private IComplianceQuery getQuery( IComplianceQuery qry ){
        IComplianceQuery query = null;
        query = ( IComplianceQuery ) cache.get( qry );
        if( query == null ){
            query = ( IComplianceQuery )qryQueue.getQueries().get( qry );
        }
        return query;
    }

    private IComplianceQuery setWaitTime( IComplianceQuery query, int waitSeconds ){
        if (waitSeconds > 0) {
            query.waitForResult(waitSeconds);
        } else {
            query.waitForResult(1000 * cfgMgr.getInt(ConfigManager.CFG_QUERY_WAIT));
        }
        return query;
    }
}

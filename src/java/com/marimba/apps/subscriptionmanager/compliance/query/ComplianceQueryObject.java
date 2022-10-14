// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.query;

/**
 * Abstract query object for all compliance related queries
 * Each subclass represents a class of compliance query
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.*;
import java.util.Locale;

import com.marimba.tools.regex.*;



import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceQuery;
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.apps.subscriptionmanager.compliance.result.ErrorResult;
import com.marimba.apps.subscriptionmanager.compliance.core.DbSourceManager;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.intf.msf.query.*;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.tools.util.Password;

public abstract class ComplianceQueryObject implements IComplianceQuery, ComplianceConstants, IAppConstants {

    // Query object state
    protected int state;

    // Query result
    protected IComplianceResult result;

    // Query start/end time
    protected long startTime;
    protected long endTime;

    // Cache time
    protected long maxCache;
    protected long cacheExt;

    protected IUserPrincipal user;
    protected Locale userLocale;
    protected String exception;
    protected ITenant tenant;

    /**
     * Wait for a specified interval of time for the result to be populated.
     * @param secondsToWait
     */
    public void waitForResult(long secondsToWait) {
        long waitTime = secondsToWait * 1000; // convert to millisecs
	    synchronized(this){
	        try{
                if (result == null) {
                    wait(waitTime);
                }
	        }catch(InterruptedException e){
		        e.printStackTrace();
	        }
	    }
    }

    public void setException( Exception exception ){
        this.exception = WebUtil.getStackTrace( exception );
    }

    public String getException(){
        return exception;
    }

    public ITenant getTenant() {
		return tenant;
	}

	public void setTenant(ITenant tenant) {
		this.tenant = tenant;
	}

	public void setResult(IComplianceResult result){
        synchronized(this){
            this.result = result;
            notifyAll();
        }
    }
    
    public void setState(int state) {
	    this.state = state;
    }

    public int getState() {
	    return state;
    }

    public IUserPrincipal getUser() {
        return user;
    }

    public void setUser(IUserPrincipal user, Locale locale) {
        this.user = user;
        setLocale( locale );
    }

    public void setUser(IUserPrincipal user) {
        this.user = user;
    }

    public void setLocale( Locale locale ){
        this.userLocale = locale;
    }

    public void setStartTime(long startTime) {
	    this.startTime = startTime;
    }

    public long getStartTime() {
	    return startTime;
    }

    public void setEndTime(long endTime) {
	    this.endTime = endTime;
    }

    public long getEndTime() {
	    return endTime;
    }

    public void setMaxCache(long maxCache) {
	    this.maxCache = maxCache;
    }

    public void setCacheExt(long cacheExt) {
	    this.cacheExt = cacheExt;
    }

    public IComplianceResult getResult() {
	    return result;
    }

    // Get the query's query path in the report center
    public abstract String getQueryPath();

    // Get the query arguments
    public abstract String[] getQueryArgs();

    // Fetch result from result set
    public abstract IComplianceResult getResult(IQueryResult rs) throws SQLException;
    

    protected IQueryResult executeQuery(IQueryNode qnode, String parameterValues[]) throws SQLException {
        if (qnode == null) {
	        throw new SQLException("Query node not found for " + this);
        }
	
        IQuery query = qnode.getQuery();

        if (query == null) {
            throw new SQLException("Query not found for " + qnode);
        }
        IQueryParameter parameters[] = query.getParameters();
        if (parameters != null) {
            if (parameterValues == null || parameterValues.length != parameters.length) {
                throw new SQLException("Missing query parameters for " +qnode.getDisplayName() );
            }
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].set(WebUtil.stripDots(parameterValues[i]));
            }
        }
        return query.execute();
    }

    protected IComplianceResult doQuery(IUserPrincipal user, ITenant tenant) throws SQLException {
        try {
            IQueryMgr queryMgr = DbSourceManager.getQueryManager(tenant);
            String pwd = Password.decode(user.getProperty("password"));
            IQueryMgrContext ctx = null;
            if( userLocale == null ){
                if( DEBUG ){
                    System.out.println( "creating non-locale queryMgr context" );
                }
                ctx = queryMgr.createContext(user.getName(), pwd );
            } else {
                if( DEBUG ){
                    System.out.println( "creating locale specific queryMgr context" );
                }
                ctx = queryMgr.createContext(user.getName(), pwd, userLocale );
            }

            String queryPath = getQueryPath();
            String[] queryArgs = getQueryArgs();
            if( DEBUG ){
                System.out.println( "queryPath "+queryPath );
                if( queryArgs != null ){
                    for( int indx = 0; indx < queryArgs.length; indx ++ ){
                        System.out.println( "queryArgs "+indx+" : "+queryArgs[indx] );
                    }
                }
            }
            IQueryNode qryNode = ctx.lookup( queryPath );
            if(DEBUG){
                System.out.println("Start time = " + System.currentTimeMillis() / 1000  + " secs before executing " +queryPath);
            }
            IQueryResult rs = executeQuery( qryNode, queryArgs );
            if(DEBUG){
                System.out.println("End time = " + System.currentTimeMillis() / 1000  + " secs after executing "+queryPath);
            }


            if (rs == null) {
                throw new SQLException();
            }

            try {
                return getResult(rs);
            } finally {
                rs.close();
            }
        } catch (QueryManagerException e) {
            e.printStackTrace();
            throw new SQLException(e.toString());
        }
    }
    
    public IComplianceResult executeQuery() {
        // Set state
        setState(STATE_IN_QUERY);

	    // Result
	    IComplianceResult res = null;
	
	    // Set start time
	    setStartTime(System.currentTimeMillis());
	    try {
	        res = doQuery(user, tenant);
	    } catch (SQLException e) {
	        e.printStackTrace();

            // Set result to be error
            res = new ErrorResult(e);
	        // Init for cache errors results for one min
	        res.initForCache(maxCache, cacheExt);
	    } finally {
	        setEndTime(System.currentTimeMillis());
	        // Set state
	        if (res != null && !(res instanceof ErrorResult)) {
		        setState(STATE_DONE);
	        } else {
                if( DEBUG ){
                    String curDate = WebUtil.getComplianceDateTimeFormat().format( new Date( System.currentTimeMillis() ) );
                    System.out.println( "Query "+getQueryPath()+" failed to execute on "+curDate );
                }
                if( res instanceof ErrorResult ){
                    ErrorResult error = (ErrorResult)res;
                    setException( error.getException() );
                }
		        setState(STATE_ERROR);
	        }
	        // Set result and notify waiter
	        setResult(res);
	    }
	    return res;
    }
}

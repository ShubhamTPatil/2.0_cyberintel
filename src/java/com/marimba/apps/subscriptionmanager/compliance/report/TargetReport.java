// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.report;

/**
 * Target based compliance report
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceReport;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.ITenant;

import java.util.Date;
import java.sql.Types;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.CallableStatement;

public class TargetReport implements IComplianceReport {

    String target;
    Date startTime;
    Date endTime;
    int state;
    String targetType;
    protected IUserPrincipal user;
    protected ITenant tenant;

    public TargetReport() {
    }

    public void setTarget(String target) {
	    this.target = (target == null) ? "" : target.toLowerCase();
    }

    public String getTarget() {
	    return target;
    }

    public int hashCode() {
	    return target.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof TargetReport)) return false;
	    if (!target.equals(((TargetReport) obj).getTarget())) return false;
	    return true;
    }

    public void setStartTime(Date date) {
	    this.startTime = date;
    }

    public void setEndTime(Date date) {
	    this.endTime = date;
    }

    public void setState(int state) {
	    this.state = state;
    }

    public Date getStartTime() {
	    return startTime;
    }

    public Date getEndTime() {
	    return endTime;
    }

    public int getState() {
	    return state;
    }

    public IUserPrincipal getUser(){
        return user;
    }

    public void setUser( IUserPrincipal curUser ){
        user = curUser;
    }

    public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	
	public ITenant getTenant() {
		return tenant;
	}

	public void setTenant(ITenant tenant) {
		this.tenant = tenant;
	}

	// Two stored procedures to call
    final static String addcacheheader = "{call AddCacheHeader(?, ?, ?)}";
    final static String calculatecompliance = "{call CalculateCompliance(?)}";
    // Site based Compliance Report 
    //final static String calculateSitecompliance = "{call CalculateSiteCompliance(?)}";
    /**
     * Execute the report
     */
    public void execute(Connection con) throws SQLException {
	    // Basically call two stored procedures
	    try {
	        int cid = -1;
	        CallableStatement cstmt = con.prepareCall(addcacheheader);
	        try {
		        cstmt.registerOutParameter(1, Types.INTEGER);
		        cstmt.setString(2, target);
                cstmt.setString( 3, user.getName() );
		        cstmt.execute();

		        // Get calculation id
		        cid = cstmt.getInt(1);
	        } finally {
		        cstmt.close();
	        }

	        if (cid != -1) {
	        	if(null != targetType && "site".equalsIgnoreCase(targetType)) {
	        		// After adding site procedure need to change procedure name also
	        		cstmt = con.prepareCall(calculatecompliance);
	        	} else {
	        		cstmt = con.prepareCall(calculatecompliance);
	        	}
		        
		        try {
		            cstmt.setInt(1, cid);
		            cstmt.execute();
		        } finally {
		            cstmt.close();
		        }
		        con.commit();
	        } else {
		        con.rollback();
	        }
	    } catch (SQLException sqle) {
	        sqle.printStackTrace();
	        con.rollback();
	        throw sqle;
	    }
    }
}

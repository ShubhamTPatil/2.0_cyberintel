// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.report;

/**
 * Delete cached report from database
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceReport;
import com.marimba.intf.msf.IUserPrincipal;

import java.util.Date;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class DeleteReport implements IComplianceReport {

    int calculationId;
    int state;
    IUserPrincipal user;
    
    public DeleteReport(int id) {
	    this.calculationId = id;
    }

    public void setState(int state) {
	    this.state = state;
    }

    public int getState() {
	    return state;
    }	
    
    public int getCalculationId() {
	    return calculationId;
    }

    public IUserPrincipal getUser(){
        return user;
    }

    public void setUser( IUserPrincipal curUser ){
        user = curUser;
    }

    public int hashCode() {
	    return calculationId;
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof DeleteReport)) return false;
	    if (calculationId != ((DeleteReport) obj).getCalculationId()) return false;

	    return true;
    }

    // First query, make sure end_time is not null before we delete this report
    final static String checkDelete = "select * from compliance_cache_header where calculation_id = ? and end_time is not null";
    final static String deleteDetail = "delete from compliance_cache_detail where calculation_id = ?";
    final static String deleteHeader = "delete from compliance_cache_header where calculation_id = ?";
    
    /**
     * Try to delete the report
     */
    public void execute(Connection con) throws SQLException {
	    // Basically call two stored procedures
        boolean autoCommit = con.getAutoCommit();
	    try {
            con.setAutoCommit(true);
	        boolean needDel = false;
	        PreparedStatement stmt = con.prepareStatement(checkDelete);
	        try {
		        stmt.setInt(1, calculationId);
		        ResultSet rs = stmt.executeQuery();
		        try {
		            needDel = rs.next();
		        } finally {
		            rs.close();
		        }
	        } finally {
		        stmt.close();
	        }

	        // Check if we can delete anything
	        if (needDel) {
                con.setAutoCommit(true);
		        stmt = con.prepareStatement(deleteDetail);
		        try {

                    stmt.setInt(1, calculationId);
		            stmt.execute();
		        } finally {
		            stmt.close();
		        }
		        stmt = con.prepareStatement(deleteHeader);
		        try {
		            stmt.setInt(1, calculationId);
		            stmt.execute();
		        } finally {
		            stmt.close();
		        }
	        }
		
	        con.commit();
            con.setAutoCommit(autoCommit);
	    } catch (SQLException sqle) {
	        sqle.printStackTrace();
	        con.rollback();
	        throw sqle;
	    }
    }

    public String getTarget() { return null; }

    public Date getStartTime() { return null; }

    public void setStartTime(Date date) {}

    public Date getEndTime() { return null; }

    public void setEndTime(Date date) {}

}

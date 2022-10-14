// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;


import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineListResult.CompliantFetcher;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineListResult.NonComplianceFetcher;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineListResult.NotCheckInFetcher;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineListResult.PowerCompliantFetcher;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineListResult.PowerNonComplianceFetcher;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineListResult.PowerNotCheckInFetcher;
import com.marimba.apps.subscriptionmanager.compliance.result.MachineListResult.RowFetcher;
import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.*;
import java.sql.Date;
import java.util.*;
/**
 * List that holds list of site machines
 *
 * @author  Selvaraj Jegatheesan
 * @version $Revision$, $Date$
 */
public class SiteMachineListResult extends ListResult {
	private String complevel;
    private RowFetcher rowFetcher;

    public void setComplianceLevel(String level) {
	this.complevel = level;
	if (ComplianceConstants.STR_LEVEL_COMPLIANT.equals(level)) {
	    rowFetcher = new CompliantFetcher();
	} else if (ComplianceConstants.STR_LEVEL_NOT_CHECK_IN.equals(level)) {
	    rowFetcher = new NotCheckInFetcher();
    } else if (ComplianceConstants.STR_LEVEL_NON_COMPLIANT.equals(level)) {
        rowFetcher = new NonComplianceFetcher();
	} else if (POWER_STR_LEVEL_COMPLIANT.equals(level)) {
	    rowFetcher = new PowerCompliantFetcher();
	} else if (POWER_STR_LEVEL_NON_COMPLIANT.equals(level)) {
	    rowFetcher = new PowerNonComplianceFetcher();
	} else {
	    rowFetcher = new PowerNotCheckInFetcher();
	}
    }

    public void fetchRow(IQueryResult rs) throws SQLException {
	rowFetcher.fetchRow(rs);
    }

    public abstract class RowFetcher {
	public abstract void fetchRow(IQueryResult rs) throws SQLException;
    }

    public class CompliantFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
	    MachineBean mb = new MachineBean(rs.getString("machine_name"));
        mb.setMachineID( rs.getString("name") );
        Timestamp scanTimeStamp = rs.getTimestamp("scantime");
        Date scanTime = new Date(scanTimeStamp.getTime());
	    mb.setLastCheckIn(scanTime);
	    list.add(mb);
	}
	
    }

    public class NonComplianceFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
	    MachineBean mb = new MachineBean(rs.getString("machine_name"));
        mb.setMachineID( rs.getString("name") );
	    mb.setEndpointState(rs.getString("endpoint_state"));
	    mb.setPolicyState(rs.getString("policy_state"));
	    mb.setUrl(rs.getString("url"));
	    Timestamp scanTimeStamp = rs.getTimestamp("scantime");
	    Date scanTime = new Date(scanTimeStamp.getTime());
	    mb.setLastCheckIn(scanTime);
	    list.add(mb);
	}
    }

    public class NotCheckInFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
	    MachineBean mb = new MachineBean(rs.getString("machine_name"));
        mb.setMachineID( rs.getString("name") );
        Timestamp scanTimeStamp = rs.getTimestamp("scantime");
	    Date scanTime = new Date(scanTimeStamp.getTime());
	    mb.setLastCheckIn(scanTime);
	    list.add(mb);	    
	}
    }

    public class PowerCompliantFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
	    MachineBean mb = new MachineBean(rs.getString("machine_name"));
        mb.setMachineID( rs.getString("target_name") );
        Timestamp scanTimeStamp = rs.getTimestamp("scantime");
	    Date scanTime = new Date(scanTimeStamp.getTime());
	    mb.setLastCheckIn(scanTime);
	    list.add(mb);
	}

    }

    public class PowerNonComplianceFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
        String machineName = rs.getString("machine_name");
        String machineID = rs.getString("target_name");
        Timestamp scanTimeStamp = rs.getTimestamp("scantime");
	    Date scanTime = new Date(scanTimeStamp.getTime());

        for (int i = 0; i < policyPowerOption.length; i++) {           
            String columnName = powerOptions[i];
            String policyVal = rs.getString(policyPowerOption[i]);
            String machineVal = rs.getString(endpointPowerOption[i]);

            if(isFailed(policyVal, machineVal)){
                MachineBean mb = new MachineBean();
                mb.setMachineID(machineID);
                mb.setMachineName(machineName);
                mb.setFailureCause(columnName);
                if (!"SchemeName".equalsIgnoreCase(columnName)) {
                    //policy_scheme_name cannot be converted to Integer.
                    if (Integer.parseInt(machineVal) == Integer.MIN_VALUE) {
                        machineVal = "";
                    }
                }
                mb.setEndpointState(machineVal);
                mb.setPolicyState(policyVal);
                mb.setLastCheckIn(scanTime);
                list.add(mb);
            }
        }
	}
        public boolean isFailed(String policy, String endpoint) throws SQLException {
            if((!PWR_UNSET_PROP_VALUE.equals(policy)) && (!endpoint.equals(policy))) {
                return true;
            }
            return false;
        }
    }

    public class PowerNotCheckInFetcher extends RowFetcher {

	public void fetchRow(IQueryResult rs) throws SQLException {
	    MachineBean mb = new MachineBean(rs.getString("machine_name"));
        mb.setMachineID( rs.getString("target_name") );
        Timestamp scanTimeStamp = rs.getTimestamp("scantime");
	    Date scanTime = new Date(scanTimeStamp.getTime());
	    mb.setLastCheckIn(scanTime);
	    list.add(mb);
	}
    }
}

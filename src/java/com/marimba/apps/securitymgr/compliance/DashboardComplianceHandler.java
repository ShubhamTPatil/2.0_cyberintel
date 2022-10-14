package com.marimba.apps.securitymgr.compliance;

import java.util.ArrayList;
import java.util.List;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;

public class DashboardComplianceHandler  implements ComplianceConstants {
	SubscriptionMain main;
	public DashboardComplianceHandler(SubscriptionMain main) {
		this.main = main;
	}
	//Controller for total number of machine
	public int getTotalNumberofMachines(String targetId) {
		int count = 0;
		try {
			count = new DashboardDetails.GetMachineDetails(main, targetId).getMachinesCount();
		} catch(Exception ed) {
			ed.printStackTrace();
		}
		return count;
	}
	
	//Controller for total number of machine reported last 24 Hrs
	public int getLast24HourScanMachinesCount(String targetId) {
		int count = 0;
		try {
			count  = new DashboardDetails.GetLast24HourMachineDetails(main, targetId).getMachinesCount();
		} catch(Exception ed) {
			ed.printStackTrace();
		}
		return count;
	}
	//Controller for total number of scurity scanners in use
	public int getSecuirtyInUseCount(String targetId) {
		int count = 0;
		try {
		count = new DashboardDetails.GetSecurityInUseDetails(main, targetId).getSecurityCount();
		} catch(Exception ed) {
			ed.printStackTrace();
		}
		return count;
	}
	
	//Controller for overall compliance
	public void getOverallCompliance() {
		
	}
	public int getOverallCompliantMachineCount(String targetId) {
		int count = 0;
		try {
			count = new DashboardDetails.GetOverallComplianceDetails(main, targetId, COMPLAINT).getCount();
		} catch(Exception ed) {
			ed.printStackTrace();
		}
		return count;
	}
	public int getOverallNonCompliantCount(String targetId) {
		int count = 0;
		try {
			count = new DashboardDetails.GetOverallComplianceDetails(main, targetId, NON_COMPLAINT).getCount();
		} catch(Exception ed) {
			ed.printStackTrace();
		}
		return count;
	}

}

// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * Single machine compliance result
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;

import java.util.TreeMap;
import java.util.Map;

import com.marimba.apps.subscriptionmanager.compliance.view.MachinePssBean;
import com.marimba.intf.msf.query.IQueryResult;


public class MachinePssComplianceResult extends ListResult {

    public void fetchRow(IQueryResult rs) throws SQLException {

        Map pssCompliance = findPssCompliance(rs);
	MachinePssBean bean = new MachinePssBean();


        bean.setPssMap(pssCompliance);
        String targetName = rs.getString("target_name");
        bean.setTargetName(targetName );
        bean.setPolicyName(rs.getString("target_name"));
        bean.setPolicyTargetType(rs.getString("target_type"));
	// Add to the list
	list.add(bean);
    }

    Map findPssCompliance(IQueryResult rs)  throws SQLException {
        String pssCompliance = null;
        Map pssMap = new TreeMap();
        boolean complianceCheck = true;

        long ldapsyncTime = rs.getDate(LDAPSYNC_POLICY_UPDATE_TIME) == null ? -1 : rs.getDate(LDAPSYNC_POLICY_UPDATE_TIME).getTime();
        long endptTime = rs.getDate(ENDPOINT_POLICY_UPDATE_TIME) == null ? -1 : rs.getDate(ENDPOINT_POLICY_UPDATE_TIME).getTime();

        if ( ldapsyncTime > endptTime) {
            pssCompliance = POWER_STR_LEVEL_NOT_CHECK_IN;
            complianceCheck = false;
        }

        /*Check for power scheme name */

        String policyPowerScheme = rs.getString("policy_scheme_name");
        String endptPowerScheme = rs.getString("endpoint_scheme_name");

        if (endptPowerScheme == null) {
            // temp code hacking for seeing the other options
            endptPowerScheme = "";
        }

        if (policyPowerScheme != null && endptPowerScheme != null) {
            pssMap.put("policy_scheme_name", policyPowerScheme);
            pssMap.put("endpoint_scheme_name", endptPowerScheme);
        } else {
            pssMap.put("policy_scheme_name", "NULL");
            pssMap.put("endpoint_scheme_name", "NULL");
        }

            if (complianceCheck) {
                if (policyPowerScheme.equals(endptPowerScheme)) {
                    pssMap.put("scheme_name", POWER_STR_LEVEL_COMPLIANT);
                } else {
                    pssMap.put("scheme_name", POWER_STR_LEVEL_NON_COMPLIANT);
                }
            } else {
                pssMap.put("scheme_name", POWER_STR_LEVEL_NOT_CHECK_IN);
            }

        for (int i = 0; i < policyPowerOption.length; i++) {

            if ("policy_scheme_name".equals(policyPowerOption[i])) {
                // skip it because we have handled before
                continue;
            }
            int policyPower = rs.getInt(policyPowerOption[i]);
            int endptPower = rs.getInt(endpointPowerOption[i]);

            // Condition to set blank value if the power
            // option is not supported at end point
            if(endptPower == Integer.MIN_VALUE){
                pssMap.put(policyPowerOption[i], new Integer(policyPower).toString());
                pssMap.put(endpointPowerOption[i], new String(""));
            } else {
                pssMap.put(policyPowerOption[i], new Integer(policyPower).toString());
                pssMap.put(endpointPowerOption[i], new Integer(endptPower).toString());
            }

            //Need to find the power setting compliance / non-copliance only if it is not "NOT-CHECKED-IN" case.
            if(complianceCheck) {
                if ((policyPower == endptPower)) {
                    pssMap.put(powercompliant[i], POWER_STR_LEVEL_COMPLIANT);
                } else {
                    pssMap.put(powercompliant[i], POWER_STR_LEVEL_NON_COMPLIANT);
                }
            } else {
                pssMap.put(powercompliant[i], POWER_STR_LEVEL_NOT_CHECK_IN);
            }
        }
        return pssMap;
    }
}

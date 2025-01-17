// Copyright 1997-2011, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;


import com.marimba.intf.msf.query.IQueryResult;

import java.sql.SQLException;

/**
 * Result Set for target list by group
 *
 * @author  Selvaraj Jegatheesan
 * @version $Revision$, $Date$
 */
public class TargetListByGroupResult extends ListResult {
    public void fetchRow(IQueryResult rs) throws SQLException {

        String targetName = rs.getString("target_name");
        targetName = getCNString(targetName);

    }
    private String getCNString(String targetName) {
        String result = null;
        if( null != targetName) {
            int indexE = targetName.indexOf('=');
            int indexH = targetName.indexOf(',');
            result = targetName.substring(indexE+1, indexH);
        }
        return result;
    }
}

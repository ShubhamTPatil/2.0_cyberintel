// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.result;

/**
 * List result for package queries
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.SQLException;
import java.sql.ResultSet;

import java.util.Collection;
import java.util.ArrayList;

import com.marimba.apps.subscriptionmanager.compliance.view.PackageBean;
import com.marimba.intf.msf.query.IQueryResult;

public class PackageListResult extends ListResult {

    /**
     * Fetch each row
     */
    public void fetchRow(IQueryResult rs) throws SQLException {
	String url = rs.getString("url");
	// REMIND, need to get the name in the future
	url = (url == null) ? "" : url;

//	int index = url.lastIndexOf("/");
//	String name = (index != -1) ? url.substring(index + 1) : url;

    String name = rs.getString("package_title");
    String content_type = rs.getString("content_type");

    // Add one more item
	PackageBean pkg = new PackageBean();
	pkg.setName(name);
	pkg.setUrl(url);
    pkg.setContent_type(content_type);

    list.add(pkg);
    }

}

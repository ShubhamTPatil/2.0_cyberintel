// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.query;

/**
 * Query package list from subscription policy table and transmitter segment table
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.sql.*;
import java.text.*;

import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.result.*;
import com.marimba.apps.subscriptionmanager.compliance.util.WebUtil;
import com.marimba.intf.msf.query.*;

public class PackageListQuery extends ComplianceListQueryObject {


    // Package title name
    String pkgName;

    // In format of MM/dd/YYYY
    String pkgTime;

    public PackageListQuery(String pkgName, String pkgTime) {
        if( pkgName != null ){
            if( pkgName.indexOf( '*' ) != -1 ){
                this.pkgName = pkgName.replace( '*', '%' );
            } else if( "".equals( pkgName.trim() ) ){
                this.pkgName = "%";
            } else {
                this.pkgName = pkgName;
            }
        } else{
            this.pkgName = "%";
        }
        //this is not yet used from GUI
	    this.pkgTime = (pkgTime == null) ? "" : pkgTime;

	    if (!"".equals(pkgTime)) {
	        try {
                WebUtil.getComplianceDateFormat( userLocale ).parse(pkgTime);
	        } catch (ParseException e) {
		        e.printStackTrace();
		        pkgTime = "";
	        }
	    }
    }

    public String getPkgName() {
	    return pkgName;
    }

    public String getPkgTime() {
	    return pkgTime;
    }

    public int hashCode() {
	    return pkgName.hashCode();
    }

    public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof PackageListQuery)) return false;

	    PackageListQuery qry = (PackageListQuery) obj;
	    if (!pkgName.equals(qry.getPkgName())) return false;
	    if (!pkgTime.equals(qry.getPkgTime())) return false;
	    if (startPage != qry.getStartPage()) return false;
	
	    return true;
    }

    public String getQueryPath() {
	    if ("".equals(pkgTime)) {
	        return QUERY_FOLDER_PATH + "ListPackagesByPackageName";
	    } else {
	        // REMIND, return something else
	        return QUERY_FOLDER_PATH + "ListPackagesByNameAndDate";
	    }
    }

    public String[] getQueryArgs() {
	    if ("".equals(pkgTime)) {
	        return new String[] {pkgName};
	    } else {
	        return new String[] {pkgName, pkgTime};
	    }
    }
    
    public IComplianceResult getResult(IQueryResult rs) throws SQLException {
        PackageListResult plr = new PackageListResult();
	    setPageInfo(plr);
	    plr.fetch(rs);
	    plr.initForCache(maxCache, cacheExt);
        return plr;
    }
}

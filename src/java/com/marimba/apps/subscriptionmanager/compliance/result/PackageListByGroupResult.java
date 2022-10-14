package com.marimba.apps.subscriptionmanager.compliance.result;

import com.marimba.apps.subscriptionmanager.compliance.view.*;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.text.SimpleDateFormat;


/**
 * Created by IntelliJ IDEA.
 * User: mkumar
 * Date: May 13, 2005
 * Time: 3:08:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageListByGroupResult extends ListResult{
    
    /**
     * Fetch each row
     */
    public void fetchRow(IQueryResult rs) throws SQLException {
    	try{
	    PackagePolicyDetails ppd = new PackagePolicyDetails();
	    ppd.setTargetId(rs.getString("target_name"));
	    ppd.setTargetType(rs.getString("type"));
	    ppd.setPackageTitle(rs.getString("Package_title"));
	    ppd.setPackageUrl(rs.getString("url"));
	    ppd.setPrimaryState(rs.getString("primary_state"));
        ppd.setPackageLastPublished(rs.getDate("lastpublished"));
        ppd.setPolicyLastUpdated(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(rs.getString("last_updated")));
        ppd.setPackageType(rs.getString("content_type"));
        list.add(ppd);
        }
    	catch(Exception e){
    			System.out.println("Failed to parse the policy last updated datetime");
    	}
    }
}

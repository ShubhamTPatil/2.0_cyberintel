package com.marimba.apps.subscriptionmanager.compliance.result;

import com.marimba.apps.subscriptionmanager.compliance.intf.IComplianceResult;
import com.marimba.intf.msf.query.IQueryResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mkumar
 * Date: May 5, 2005
 * Time: 11:24:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class PackageResult extends CachableResult{
    /**
     * Fetch each row
     */

    List pkgs;

    public void setPackages(List pkgList){
        pkgs = pkgList;
    }
    public void fetchRow(ResultSet rs) throws SQLException {

    }

    public List getPackages(){
        return pkgs;
    }



    public void fetch(IQueryResult rs) throws SQLException {

    }


}

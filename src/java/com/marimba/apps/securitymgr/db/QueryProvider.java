package com.marimba.apps.securitymgr.db;

import com.marimba.intf.db.IConnectionPool;
import static com.marimba.intf.db.IConnectionPool.*;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Mar 22, 2017
 * Time: 5:37:44 PM
 * To change this template use File | Settings | File Templates.
 */

public class QueryProvider {
    private String sqlBase = "queries";
    private static ResourceBundle queryResource;


    public static String SUFFIX_COMMON = "_common";
    public static String SUFFIX_ORACLE = "_oracle";
    public static String SUFFIX_SQLSERVER = "_sqlserver";

    private static String PSS_CHECK_DB_CONN = "check_db_connection";


    public QueryProvider(int dbType) {
        try {
            queryResource = ResourceBundle.getBundle(sqlBase, Locale.getDefault());
        } catch (Exception e) {
            debug("Exception during initializing SQL query resources");
            e.printStackTrace();
        }

        if (ORACLE == dbType || ORACLE_RAC == dbType) {
            formatOracleVars();
        }
        if (SQLSERVER == dbType) {
            formatSQLServerVars();
        }
        formatCommonVars();
    }

   public String getRawQuery(String sqlKey) {
        String sqlValue = null;
        if (queryResource != null) {
            try {
                sqlValue = queryResource.getString(sqlKey);
            } catch (MissingResourceException mre) {
                debug("Missing query resource : " + mre.getMessage());
            }
        }

        return sqlValue;
    }


    private void formatOracleVars() {
        debug("Formatting query constants for ORACLE");
    }

    private void formatSQLServerVars() {
        debug("Formatting query constants for SQLSERVER");
    }

    private void formatCommonVars() {

    }

    public static String setArgument(String query, String find, String repl){
        if(null == query || query.indexOf(find) < 0){
            return query;
        } else {
            StringBuilder sb = new StringBuilder(query);
            sb.replace(sb.indexOf(find), (sb.indexOf(find) + find.length()), repl.toLowerCase());
            return sb.toString();
        }
    }

    private static void debug(String msg) {
        System.out.println("QueryProvider: " + msg);
    }
}

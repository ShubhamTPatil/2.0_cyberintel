// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance.util;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import org.apache.ibatis.jdbc.ScriptRunner;
import com.marimba.intf.db.IStatementPool;


/**
 *  RunSQLScript Utility
 *  w.r.t execute SQL script file execution
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class RunSQLScript extends DatabaseAccess {
    String scriptPath;
    SubscriptionMain main;
    boolean status = false;
    
    public RunSQLScript() {
         // do nothing;
    }

    public RunSQLScript(String scriptFilePath, SubscriptionMain main) {
        ScriptExecutor result = new ScriptExecutor(scriptFilePath, main);
        try {
            this.scriptPath = scriptFilePath;
            this.main = main;
            runQuery(result);
            status = result.getScriptExecutionStatus();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean getStatus() {
        return status;
    }
    
    class ScriptExecutor extends QueryExecutor {
        String scriptFilePath;
        boolean scriptExecutionFailed = true;

        ScriptExecutor(String scriptFilePath, SubscriptionMain main) {
            super(main);
            this.scriptFilePath = scriptFilePath;
        }

        protected void execute(IStatementPool pool) {
            String queryStr = "select count(id) from product_cve_info";
            PreparedStatement st = null;
            ResultSet rs = null;
            java.sql.Connection conn = null;

            try {
                conn = pool.getConnection();

                //Initialize the script runner
                ScriptRunner sr = new ScriptRunner(conn);
                sr.setStopOnError(true);
                sr.setLogWriter(null);
                sr.setAutoCommit(true);

                //Creating a reader object
                Reader reader = new BufferedReader(new FileReader(scriptFilePath));

                //Running the script
                sr.runScript(reader);

                scriptExecutionFailed = false;

                st = pool.getConnection().prepareStatement(queryStr);
                rs = st.executeQuery();

                try {
                    if (rs.next()) {
                        long count = rs.getLong(1);
                        System.out.println("DebugInfo: Table - <product_cve_info> count: " + count);
                    }
                } finally {
                    st.close();
                    rs.close();
                }
                System.out.println("Script Execution Done..");
                scriptExecutionFailed = false;

            } catch (SQLException sqlEx) {
                sqlEx.printStackTrace();
                scriptExecutionFailed = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                scriptExecutionFailed = true;
            } finally {
                try {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

        }

        private String getScriptPath() {
            return scriptPath;
        }

        public boolean getScriptExecutionStatus() {
            return !scriptExecutionFailed;
        }

    }

}


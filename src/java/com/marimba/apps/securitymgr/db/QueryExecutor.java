package com.marimba.apps.securitymgr.db;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.db.IStatementPool;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Mar 22, 2017
 * Time: 4:56:10 PM
 * To change this template use File | Settings | File Templates.
 */

public class QueryExecutor extends DatabaseProvider implements Runnable {

    SQLException error;
    private DatabaseProvider dbProvider;
    public static QueryExecutor executor = null;


    public QueryExecutor(SubscriptionMain main) {
        super(main);
    }
    public void executeQuery(String query) {

    }

    public void executeUpdate(String query) {

    }

    public void run() {

        IConnectionPool cp = getConnectionPool();

        if (cp == null) {
            error = new SQLException("No database configured");
            return;
        }
        IStatementPool pool = getStmtPool();
        if (pool == null) {
            error = new SQLException("Can't get a database-connection");
            return;
        }
        try {
            try {
                execute(pool);
            } catch (SQLException sqe) {
                sqe.printStackTrace();
                this.error = sqe;
            } finally {
                shutdown();
            }
        } finally {
            cp.releaseConnection(pool);
        }
    }

    protected void execute(IStatementPool pool) throws SQLException {

    }

    /**
     * Method doQuery.
     *
     * @throws
     */
    public void doQuery() throws Exception {
        run();
        if (hasError()) {
            throw new Exception(error);
        }
    }

    protected void shutdown() {
    }

    /**
     * Method hasError.
     * @return
     */
    public boolean hasError() {
        return error != null;
    }

    public void dispose() {
        cleanUp();
    }

     protected String getQuery(String sql_key) {
       return queryProvider.getRawQuery(sql_key);
     }
}

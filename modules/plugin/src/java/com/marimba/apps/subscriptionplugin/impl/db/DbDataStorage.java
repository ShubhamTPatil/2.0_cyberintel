// Copyright 2018, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionplugin.impl.db;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.marimba.apps.subscriptionplugin.SecurityComplianceBean;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceSettings;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceConstants;
import com.marimba.apps.subscriptionplugin.intf.IDataStorage;
import com.marimba.intf.db.*;
import com.marimba.intf.plugin.IPluginContext;
import com.marimba.intf.util.*;
import com.marimba.io.FastInputStream;
import com.marimba.io.FastOutputStream;
import com.marimba.tools.util.*;
import com.marimba.tools.config.ConfigUtil;
import com.marimba.tools.db.ConnectionPool;

public class DbDataStorage implements ISecurityServiceConstants, IDatabaseClient, IDataStorage {
    private boolean DB_LOGS = DebugFlag.getDebug("SECURITY/PLUGINDB") >= 2;

    final static String DATE_FORMAT_RFC1123 = "MMM dd, yyyy hh:mm:ss a";
    final static String TUNER_RELEASE_DATE_FORMAT_ORACLE = "yyyy/mm/dd hh24:mi:ss";
    final static String TUNER_RELEASE_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    final static String CURRENT_DATE_FORMAT_ORACLE = "yyyy-mm-dd hh24:mi:ss";
    final static String UTC_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // database status codes
    int STATUS_CONNECTED = 1;
    int STATUS_NOT_CONNECTED = 2;
    int STATUS_CONNECTION_FAILED = 3;

    final static String UTC = "UTC";
    final static String ORACLE_UNICODE_SYSTEM_PROPERTY = "oracle.jdbc.defaultNChar";
    final static int DB_CONNECT_WAIT = 2 * 60 * 1000;

    IConnectionPool database;
    int maxThreads;
    boolean canRun;

    ConfigUtil config;
    ISecurityServiceSettings securityServiceSettings;

    boolean dbConnected = false;

    public DbDataStorage(ISecurityServiceSettings _securityServiceSettings, ConfigUtil cfg) {
        this.securityServiceSettings = _securityServiceSettings;
        this.config = cfg;
    }

    private IStatementPool getPool() {
        ensureConnected();
        debug(INFO, "getPool(), dbConnected - " + dbConnected);
        IStatementPool pool = database.getConnection(DB_CONNECT_WAIT);
        debug(INFO, "getPool(), pool - " + pool);
        return pool;
    }

    protected String getConvertedDate(java.util.Date date) {
        String format = UTC_DATE_FORMAT;
        DateFormat df = new SimpleDateFormat(format, Locale.US);
        df.setTimeZone(TimeZone.getTimeZone(UTC));
        return df.format(date);
    }

    private void closePool(IStatementPool pool) {
        if (pool != null) {
            database.releaseConnection(pool);
        }
    }

    private synchronized void ensureConnected() {
        if (!database.isConnected()) {
            dbConnected = false;
            database.connect();
        } else {
            dbConnected = true;
        }
        debug(INFO, "ensureConnected(), dbConnected - " + dbConnected);
    }

    // TODO: We need to escape out "'" if it is part of any string that we
    // intend to insert into nvarchar columns
    protected boolean ExecuteBulkInsertUpdate(String[] sqlStmts, IStatementPool pool) throws SQLException {
        boolean ret = false;
        ensureConnected();
        debug(INFO, "ExecuteBulkInsertUpdate(), dbConnected - " + dbConnected);
        Statement stmt = pool.createStatement();
        try {
            debug(INFO, "ExecuteBulkInsertUpdate(), for multiple statements");
            if (DETAILED_INFO) {
                debug(DETAILED_INFO, "ExecuteBulkInsertUpdate(), statements being passed...");
                if (sqlStmts != null && sqlStmts.length > 0) {
                    for (int i = 0; i < sqlStmts.length; i++) {
                        System.out.println(sqlStmts[i]);
                    }
                }
                debug(DETAILED_INFO, "ExecuteBulkInsertUpdate(), statements finished");
            }

            if (sqlStmts != null && sqlStmts.length > 0) {
                for (int i = 0; i < sqlStmts.length; i++) {
                    stmt.addBatch(sqlStmts[i]);
                }
            }
            stmt.executeBatch();
            ret = true;
        } catch (BatchUpdateException bue) {
            if (ERROR) {
                bue.printStackTrace();
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            if (ERROR) {
                ex.printStackTrace();
            }
        } finally {
            stmt.close();
        }
        return ret;
    }

    // TODO: We need to escape out "'" if it is part of any string that we
    // intend to insert into nvarchar columns
    protected boolean ExecuteInsertUpdate(String sql, IStatementPool pool)
            throws SQLException {
        boolean ret = false;
        ensureConnected();
        debug(INFO, "ExecuteInsertUpdate(), dbConnected - " + dbConnected);
        Statement stmt = pool.createStatement();
        try {
            debug(INFO, "ExecuteInsertUpdate(), " + sql);
            stmt.executeUpdate(sql);
            ret = true;
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            if (ERROR) {
                ex.printStackTrace();
            }
        } finally {
            stmt.close();
        }
        return ret;
    }

    protected Vector ExecuteSelectStatement(IStatementPool sp, String query)
            throws SQLException {
        Vector results = null;
        ResultSet table = null;
        ensureConnected();
        debug(INFO, "ExecuteSelectStatement(), dbConnected - " + dbConnected);
        if (database.getDBProduct() == IConnectionPool.ORACLE) {
            debug(INFO, "ExecuteSelectStatement(), Using an Oracle DB for query: " + query);
            PreparedStatement prep = sp.getConnection().prepareStatement(query);
            table = prep.executeQuery();
            results = new Vector();
            results.add(table);
            results.add(prep);
        } else {
            debug(INFO, "ExecuteSelectStatement(), Using an SQL Server DB for query: " + query);
            Statement stamt = sp.getConnection().createStatement();
            table = stamt.executeQuery(query);
            results = new Vector();
            results.add(table);
            results.add(stamt);

            debug(INFO, "ExecuteSelectStatement(), Added table and stamt to results");
        }

        debug(INFO, "ExecuteSelectStatement(), Returning results");

        // returning a vector as the caller will have to get the Statement
        // object to "close" it after
        // processing the ResultSet
        return results;
    }

    public boolean init() {
        try {
            database = new ConnectionPool(this, config);
            debug(INFO, "init(), Finished initializing db connection pool");

            debug(INFO, "init(), DB type is: " + database.getDBType());
            if (database == null) {
                debug(INFO, "init(), Null database!");
            } else {
                debug(INFO, "init(), Not null database!");
            }

            canRun = true;

            maxThreads = config.getInteger("db.thread.max", 30);
            debug(INFO, "init(), Max threads for DB: " + maxThreads);

			/* Don't go ahead if there's no user and password configured */
            String dbUser = config.getProperty("db.connection.user");
            String dbPwd = config.getProperty("db.connection.pwd");
            if (dbUser == null || dbPwd == null) {
                debug(INFO, "init(), No db user or pwd for database, init should be false");
                return false;
            } else {
                debug(INFO, "init(), Found a valid user and password for db!");
            }

            // Check for unicode property specific to Oracle
            if (database.getDBProduct() == IConnectionPool.ORACLE) {
                debug(INFO, "init(), Checking Unicode property for Oracle");
                // check for system property "oracle.jdbc.defaultNChar", if not
                // set to true, log a warning message
                if (!"true".equals(System.getProperty(ORACLE_UNICODE_SYSTEM_PROPERTY))) {
                    debug(INFO, "init(), System property ["
                            + ORACLE_UNICODE_SYSTEM_PROPERTY
                            + "] not set to <true>. Unicode format might not get preserved for multibyte characters.");
                }
            } else {
                debug(INFO, "init(), No need to check Unicode property as using SQL server");
            }
            return true;
        } catch (Throwable e) {
            if (ERROR) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public void notify(Object sender, int msg, Object arg) {
        switch (msg) {
            case DB_CONNECT_ERROR:
                debug(INFO, "notify(), Database Connection Failed: " + String.valueOf(arg));
                config.setProperty("status", Integer.toString(STATUS_CONNECTION_FAILED));
                dbConnected = false;
                break;

            case DB_CONNECTED:
                debug(INFO, "notify(), Database Connection Success: " + String.valueOf(arg));
                config.setProperty("status", Integer.toString(STATUS_CONNECTED));
                dbConnected = true;
                break;

            case DB_USING_DRIVER:
                debug(INFO, "notify(), Database Driver Name Success: " + String.valueOf(arg));
                break;

        }

    }

    public void initialize(DatabaseMetaData md) throws SQLException {
		/* don't need to do anything here */
    }

    public void create(IStatementPool pool) {

    }

    public boolean close() {
        if (database != null) {
            database.closeAsynchronously();
        }
        if (database != null) {
            if (OSConstants.JAVA_2) {
                for (Enumeration e = DriverManager.getDrivers(); e.hasMoreElements();) {
                    Driver d = (Driver) e.nextElement();
                    try {
                        DriverManager.deregisterDriver(d);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        debug(INFO, "close(), Database was closed successfully");

        return true;
    }

    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans) {
        return insertScanDetails(securityComplianceBeans, null);
    }

    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine) {
        return insertScanDetails(securityComplianceBeans, null, new HashMap<String, String>());
    }

    public boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine, HashMap<String, String> moreInfo) {
        boolean inserted = true;
        String scanType = null;
        debug(INFO, "insertScanDetails(), securityComplianceBeans - " + securityComplianceBeans);
        debug(INFO, "insertScanDetails(), securityComplianceForMachine - " + securityComplianceForMachine);

        String error = "";
        IStatementPool pool = null;
        try {
            ensureConnected();
            debug(INFO, "insertScanDetails(), dbConnected - " + dbConnected);
            if (!dbConnected) {
                debug(INFO, "insertScanDetails(), DB Connection Failed");
                error = "Database Connection Failed";
                inserted = false;
                return false;
            }
            ArrayList<Integer> latestContentIds = new ArrayList<Integer>();
            String latestInsertedContentId = "";
            for (SecurityComplianceBean securityComplianceBean : securityComplianceBeans) {
                boolean securityComplianceBeanInserted = false;
                scanType = securityComplianceBean.getScanType();
                String contentId = securityComplianceBean.getContentId();
                latestInsertedContentId = contentId;
                String contentFileName = securityComplianceBean.getContentFileName();
                String contentTitle = securityComplianceBean.getContentTitle();
                String contentTargetOS = securityComplianceBean.getContentTargetOS();
                String profileID = securityComplianceBean.getProfileID();
                String profileTitle = securityComplianceBean.getProfileTitle();
                String scannedBy = securityComplianceBean.getScannedBy();
                String targetName = securityComplianceBean.getTargetName();
                String overallCompliance = securityComplianceBean.getOverallCompliance();
                String individualCompliance = securityComplianceBean.getIndividualCompliance();
                String machineName = securityComplianceBean.getMachineName();
                if ((securityComplianceForMachine != null) && (machineName == null)) {
                    machineName = securityComplianceForMachine;
                }
                int machineId = getMachineId(machineName);
                if (machineId == -1) {
                    debug(INFO, "insertScanDetails(), Machine Entry missing in Database");
                    error = "Machine Entry missing in Database";
                    inserted = false;
                    return false;
                }
                long startTime = securityComplianceBean.getStartTime();
                long finishTime = securityComplianceBean.getFinishTime();
                long lastPolicyUpdateTime = securityComplianceBean.getLastPolicyUpdateTime();

                String logMessage = "[" + machineName + "(" + securityComplianceBean.getTargetName() + ")"
                        + " -- " + securityComplianceBean.getContentId() + " -- " + securityComplianceBean.getProfileID() + " -- " + "]";

                int lastInsertedScanRecordId = getMaxId("security_" + scanType + "_compliance");
                int existingComplianceEntryId = getComplianceId(scanType, contentId, profileID, machineName, targetName);
                debug(INFO, "insertScanDetails(), lastInsertedScanRecordId - " + lastInsertedScanRecordId);
                debug(INFO, "insertScanDetails(), existingComplianceEntryId - " + existingComplianceEntryId);

                pool = getPool();
                if (pool != null) {
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, machineName - " + machineName);
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, contentId - " + (((contentId == null) || (contentId.trim().length() < 1)) ? " " : contentId));
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, profileID - " + (((profileID == null) || (profileID.trim().length() < 1)) ? " " : profileID));
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, targetName - " + (((targetName == null) || (targetName.trim().length() < 1)) ? " " : targetName));
                    String addScanDetailsStmt = "{call Add" + scanType.toUpperCase() + "Compliance(?,?,?,?,?,?,?,?,?,?)}";
                    CallableStatement poolStmt = pool.getConnection().prepareCall(addScanDetailsStmt);
                    poolStmt.setString(1, machineName);
                    poolStmt.setString(2, ((contentId == null) || (contentId.trim().length() < 1)) ? " " : contentId);
                    poolStmt.setString(3, ((profileID == null) || (profileID.trim().length() < 1)) ? " " : profileID);
                    poolStmt.setString(4, ((targetName == null) || (targetName.trim().length() < 1)) ? " " : targetName);

                    String dateFormat = "MM-dd-yyyy HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                    sdf.setLenient(false);

                    java.util.Date lastPolicyUpdateDate = sdf.parse(sdf.format(new java.util.Date(lastPolicyUpdateTime)));
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, lastPolicyUpdateDate - " + lastPolicyUpdateDate.getTime());
					poolStmt.setTimestamp(5, new java.sql.Timestamp(lastPolicyUpdateDate.getTime()), TimeUtil.getGMTCalendar());
                    java.util.Date startDate = sdf.parse(sdf.format(new java.util.Date(startTime)));
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, startDate - " + startDate.getTime());
                    poolStmt.setTimestamp(6, new java.sql.Timestamp(startDate.getTime()), TimeUtil.getGMTCalendar());
                    java.util.Date finishDate = sdf.parse(sdf.format(new java.util.Date(finishTime)));
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, finishDate - " + finishDate.getTime());
                    poolStmt.setTimestamp(7, new java.sql.Timestamp(finishDate.getTime()), TimeUtil.getGMTCalendar());


                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, scannedBy - " + (((scannedBy == null) || (scannedBy.trim().length() < 1)) ? " " : scannedBy));
                    debug(DETAILED_INFO, "insertScanDetails(), call procedure with, overallCompliance - " + (((overallCompliance == null) || (overallCompliance.trim().length() < 1)) ? " " : overallCompliance));
                    poolStmt.setString(8, ((scannedBy == null) || (scannedBy.trim().length() < 1)) ? " " : scannedBy);
                    poolStmt.setString(9, ((overallCompliance == null) || (overallCompliance.trim().length() < 1)) ? " " : overallCompliance);

                    InputStream individualComplianceJSONStringStream = null;
                    int individualComplianceJSONStringLen = 0;
                    if ((individualCompliance != null) && (individualCompliance.trim().length() > 0)) {
                        individualComplianceJSONStringLen = individualCompliance.length();
                        individualComplianceJSONStringStream = new ByteArrayInputStream(individualCompliance.getBytes("UTF-8"));
                        if (database.getDBProduct() == IConnectionPool.ORACLE) {
                            poolStmt.setBlob(10, pool.getConnection().createBlob());
                            // will handle json content upload(blob) later....
                        } else {
                            poolStmt.setBinaryStream(10, individualComplianceJSONStringStream);
                        }
                    } else {
                        poolStmt.setBinaryStream(10, null);
                    }

                    poolStmt.execute();
                    poolStmt.close();

                    if ((individualCompliance != null) && (individualCompliance.trim().length() > 0)) {
                        if (database.getDBProduct() == IConnectionPool.ORACLE) {
                            PreparedStatement st = pool.getConnection().prepareStatement(
                                    "select rules_compliance from security_" + scanType + "_compliance where id='" + (lastInsertedScanRecordId + 1) + "' for update");
                            ResultSet rs = (ResultSet) st.executeQuery();
                            if (rs.next()) {
                                debug(INFO, "insertScanDetails(), rs.next()");
                                Blob blob = (Blob) rs.getBlob(1);
                                OutputStream ous = blob.setBinaryStream(0L);

                                try {
                                    byte[] inbuf = new byte[4096];
                                    while (individualComplianceJSONStringLen > 0) {
                                        int n = (individualComplianceJSONStringLen > inbuf.length) ? inbuf.length : (int) individualComplianceJSONStringLen;
                                        try {
                                            int m = n;
                                            if ((n = individualComplianceJSONStringStream.read(inbuf, 0, n)) < 0) {
                                                debug(INFO, "insertScanDetails(), Got less bytes when streaming attachment to plugin.");
                                            }
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        if (n > 0) {
                                            debug(INFO, "insertScanDetails(), inbuf now - " + inbuf.toString());
                                            ous.write(inbuf, 0, n);
                                            individualComplianceJSONStringLen -= n;
                                            debug(INFO, "insertScanDetails(), individualComplianceJSONStringLen now - " + individualComplianceJSONStringLen);
                                        }
                                    }
                                    ous.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    ous.close();
                                }
                            }
                        }
                    }
                    closePool(pool);
                }

                boolean isNewInsert = true;
                int ovalComplianceId = 0;
                int stigComplianceId = 0;

                if (existingComplianceEntryId != -1) {
                    isNewInsert = false;
                    //its an update to existing scan record....
                    securityComplianceBeanInserted = true;
                    latestContentIds.add(existingComplianceEntryId);
                    if ("oval".equals(scanType)) {
                        ovalComplianceId = existingComplianceEntryId;
                    } else if ("xccdf".equals(scanType)) {
                        stigComplianceId = existingComplianceEntryId;
                    }
                } else {
                    //new scan record inserted...
                    int maxID = getMaxId("security_" + scanType + "_compliance");
                    debug(INFO, "insertScanDetails(), maxID - " + maxID);
                    securityComplianceBeanInserted = (lastInsertedScanRecordId < maxID);

                    if ("oval".equalsIgnoreCase(scanType)) {
                        ovalComplianceId = maxID;
                    } else if ("xccdf".equalsIgnoreCase(scanType)) {
                        stigComplianceId = maxID;
                    }

                    if (!securityComplianceBeanInserted) {
                        if (!dbConnected) {
                            error += logMessage + " --> Database Connection Failed;";
                            moreInfo.put("error." + logMessage, "Database Connection Failed");
                        } else {
                            error += logMessage + " --> Failed to insert this content;";
                            moreInfo.put("error." + logMessage, "Failed to insert this content");
                        }
                    }
                    latestContentIds.add(maxID);
                }
                moreInfo.put("result." + logMessage, securityComplianceBeanInserted ? "true" : "false");
                debug(INFO, "insertScanDetails(), securityComplianceBeanInserted - " + securityComplianceBeanInserted);
                inserted = inserted ? securityComplianceBeanInserted : false;

                if ("oval".equals(scanType)) {
                    processOvalRulesComplianceDataForInsert(securityComplianceBean, machineId, ovalComplianceId, isNewInsert);
                } else if ("xccdf".equals(scanType)) {
                    processStigRulesComplianceDataForInsert(securityComplianceBean, machineId, stigComplianceId, isNewInsert);
                }

                if (securityComplianceBeanInserted) {
                    securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_INSERTED_DB, LOG_AUDIT, logMessage);
                } else {
                    if (!dbConnected) {
                        securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_NOT_INSERTED_DB, LOG_MAJOR, "Error: " + logMessage + " --> Database Connection Failed;");
                    } else {
                        securityServiceSettings.getPluginContext().log(LOG_PLUGIN_REPORT_NOT_INSERTED_DB, LOG_MAJOR, "Error: " + logMessage + " --> Failed to insert this content;");
                    }
                }
            }
            
            if (inserted && (latestContentIds != null)) {
                String result = "";
                String deleteStmt = "";
                if (latestContentIds.size() > 0) {
                    for (int latestContentId : latestContentIds) {
                        if ("".equals(result)) {
                            result += latestContentId;
                        } else {
                            result += "," + latestContentId;
                        }
                    }
                    if (result.length() > 0) {
                        deleteStmt = "delete from security_" + scanType + "_compliance "
                        		+ "where id NOT IN (" + result + ") "
                        				+ "and content_id = (select distinct id from security_oval_content where content_name = '"+latestInsertedContentId+"') "
                        						+ "and machine_id = (select id from inv_machine where name = '" + securityComplianceForMachine.replaceAll("'", "''") + "')";
                    }
                } else {
                    if ((securityComplianceForMachine != null) && (securityComplianceForMachine.trim().length() > 0)) {
                        deleteStmt = "delete from security_" + scanType + "_compliance where machine_id = (select id from inv_machine where name = '" + securityComplianceForMachine.replaceAll("'", "''") + "')";
                    }
                }
                debug(INFO, "insertScanDetails(), deleteStmt - " + deleteStmt);
                IStatementPool deleteStmtPool = null;
                try {
                    if ((deleteStmt != null) && (deleteStmt.trim().length() > 0)) {
                        deleteStmtPool = getPool();
                        if (pool != null) {
                            boolean deleteStmtResult = ExecuteInsertUpdate(deleteStmt, deleteStmtPool);
                            debug(INFO, "insertScanDetails(), deleteStmtResult - " + deleteStmtResult);
                        }
                    }
                } catch (SQLException e) {
                    if (ERROR) {
                        e.printStackTrace();
                    }
                } finally {
                    closePool(deleteStmtPool);
                }
            }
        } catch(Throwable t) {
            if (ERROR) {
                t.printStackTrace();
            }
            inserted = false;
            error += t.getMessage() + ";";
        } finally {
            closePool(pool);
            moreInfo.put("error", error);
            moreInfo.put("result", "" + inserted);
            debug(INFO, "insertScanDetails(), inserted - " + inserted);
        }
        return inserted;
    }

    private void deleteOvalRuleComplianceData(int complianceId) throws SQLException {
        IStatementPool deleteStmtPool = null;
        try {
            String deleteStmt = "delete from oval_rules_compliance_result where compliance_id = " +complianceId;
            if ((deleteStmt != null) && (deleteStmt.trim().length() > 0)) {
                deleteStmtPool = getPool();
                if (deleteStmtPool != null) {
                    boolean deleteStmtResult = ExecuteInsertUpdate(deleteStmt, deleteStmtPool);
                    debug(INFO, "Deleted rules compliance data - result status: " + deleteStmtResult);
                }
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
        } finally {
            closePool(deleteStmtPool);
        }
    }

    private void deleteStigRuleComplianceData(int complianceId) throws SQLException {
        IStatementPool deleteStmtPool = null;
        try {
            String deleteStmt = "delete from xccdf_rules_compliance_result where compliance_id = " +complianceId;
            if ((deleteStmt != null) && (deleteStmt.trim().length() > 0)) {
                deleteStmtPool = getPool();
                if (deleteStmtPool != null) {
                    boolean deleteStmtResult = ExecuteInsertUpdate(deleteStmt, deleteStmtPool);
                    debug(INFO, "Deleted rules compliance data - result status: " + deleteStmtResult);
                }
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
        } finally {
            closePool(deleteStmtPool);
        }
    }

    private void processOvalRulesComplianceDataForInsert(SecurityComplianceBean securityComplianceBean,
                                                    int machineId, int complianceId, boolean isNewInsert) throws Exception {
        IStatementPool stmtPool = null;
        try {
            stmtPool = getPool();
            if (!isNewInsert) {
                deleteOvalRuleComplianceData(complianceId);
            }

            HashMap<String, String> rulesBean = securityComplianceBean.getRulesCompliance();
            int contentId = getContentId(securityComplianceBean.getScanType(), securityComplianceBean.getContentId());

            for (Map.Entry<String, String> entry : rulesBean.entrySet()) {
                String defName = entry.getKey();
                String defResult = rulesBean.get(defName);
                debug(DETAILED_INFO, "Before OVAL rules insert defName: " + defName + ", defResult: " + defResult + " for content id: " +
                        contentId + " , machine id: "+machineId + ", compliance id: " +complianceId);
                String insertStr = "INSERT INTO oval_rules_compliance_result(machine_id, compliance_id, content_id, definition_name, " +
                        "definition_result) VALUES(?, ?, ?, ?, ?)";
                PreparedStatement stmt = stmtPool.getConnection().prepareStatement(insertStr);
                try {
                    stmt.setInt(1, machineId);
                    stmt.setInt(2, complianceId);
                    stmt.setInt(3, contentId);
                    stmt.setString(4, defName);
                    stmt.setString(5, defResult);
                    int rowInserted = stmt.executeUpdate();
                    if (rowInserted > 0)
                        debug(DETAILED_INFO, "Definition Rule: " + defName + " inserted into DB for OVAL xml: " + contentId);
                } finally {
                    stmt.close();
                    stmt = null;
                }            }
        } catch(Exception ex) {
            throw ex;
        } finally {
            closePool(stmtPool);
        }
    }

    private void processStigRulesComplianceDataForInsert(SecurityComplianceBean securityComplianceBean,
                                         int machineId, int complianceId, boolean isNewInsert) throws Exception {
        IStatementPool stmtPool = null;
        try {
            stmtPool = getPool();
            if (!isNewInsert) {
                deleteStigRuleComplianceData(complianceId);
            }

            HashMap<String, String> rulesBean = securityComplianceBean.getRulesCompliance();
            int contentId = getContentId(securityComplianceBean.getScanType(), securityComplianceBean.getContentId());

            for (Map.Entry<String, String> entry : rulesBean.entrySet()) {
                String defName = entry.getKey();
                String defResult = rulesBean.get(defName);
                debug(DETAILED_INFO, "Before STIG rules insert defName: " + defName + ", defResult: " + defResult + " for content id: " +
                        contentId + " , machine id: " + machineId + ", compliance id: " +complianceId);
                String insertStr = "INSERT INTO xccdf_rules_compliance_result(machine_id, compliance_id, content_id, definition_name, " +
                        "definition_result) VALUES(?, ?, ?, ?, ?)";
                PreparedStatement stmt = stmtPool.getConnection().prepareStatement(insertStr);
                try {
                    stmt.setInt(1, machineId);
                    stmt.setInt(2, complianceId);
                    stmt.setInt(3, contentId);
                    stmt.setString(4, defName);
                    stmt.setString(5, defResult);
                    int rowInserted = stmt.executeUpdate();
                    if (rowInserted > 0)
                        debug(DETAILED_INFO, "Definition Rule: " + defName + " inserted into DB for STIG xml: " + contentId);
                } finally {
                    stmt.close();
                    stmt = null;
                }            }
        } catch(Exception ex) {
            throw ex;
        } finally {
            closePool(stmtPool);
        }
    }

    public int getMaxId(String tableName) {
        debug(INFO, "getMaxId(), tableName - " + tableName);
        int retVal = -1;
        IStatementPool pool = null;
        try {
            pool = getPool();
            if (pool != null) {
                String maxStmt = "select MAX(id) from " + tableName;
                debug(INFO, "getMaxId(), query - " + maxStmt);
                Vector results = ExecuteSelectStatement(pool, maxStmt);
                debug(INFO, "getMaxId(), results - " + results);
                if (results != null) {

                    ResultSet table = (ResultSet) results.elementAt(0);
                    Statement stmt = (Statement) results.elementAt(1);
                    if (table.next()) {
                        debug(INFO, "getMaxId(), results next()...");
                        retVal = (Integer) table.getInt(1);
                        debug(INFO, "getMaxId(), retVal - " + retVal);
                    }
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
            retVal = -1;
        } finally {
            closePool(pool);
        }
        debug(INFO, "getMaxId(), retVal - " + retVal);

        return retVal;
    }

    public int getMachineId(String machineName) {
        debug(INFO, "getMachineId(), machineName - " + machineName);
        int retVal = -1;
        IStatementPool pool = null;
        try {
            pool = getPool();
            if (pool != null) {
                String getMachineStmt = "select id from inv_machine where name = '" + machineName + "'";
                Vector results = ExecuteSelectStatement(pool, getMachineStmt);
                if (results != null) {
                    ResultSet table = (ResultSet) results.elementAt(0);
                    Statement stmt = (Statement) results.elementAt(1);
                    if (table.next()) {
                        retVal = (Integer) table.getInt(1);
                    }
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
            retVal = -1;
        } finally {
            closePool(pool);
        }
        debug(INFO, "getMachineId(), retVal - " + retVal);

        return retVal;
    }

    // To get content id from security compliance data
    public int getContentId(String scanType, String contentName) throws SQLException {
        String sqlStr = "select distinct content_id from inv_security_" + scanType + "_compliance \n" +
                "where content_name = '" + contentName + "'";
        int contentId = -1;
        IStatementPool pool = null;
        try {
            pool = getPool();
            if (pool != null) {
                PreparedStatement stmt = pool.getConnection().prepareStatement(sqlStr);
                ResultSet rs = stmt.executeQuery();
                try {
                    if (rs.next()) {
                     contentId = rs.getInt(1);
                    }
                } finally {
                   rs.close();
                   stmt.close();
                }
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
            contentId = -1;
        } finally {
            closePool(pool);
        }
        debug(INFO, "getContentId(), retVal - " + contentId);
        return contentId;
    }

    public int getComplianceId(String scanType, String contentName, String profileName, String machineName, String assignedTargetName) {
        debug(INFO, "getComplianceId(), scanType - " + scanType);
        debug(INFO, "getComplianceId(), contentName - " + contentName);
        debug(INFO, "getComplianceId(), profileName - " + profileName);
        debug(INFO, "getComplianceId(), machineName - " + machineName);
        debug(INFO, "getComplianceId(), assignedTargetName - " + assignedTargetName);
        int retVal = -1;
        IStatementPool pool = null;
        try {
            pool = getPool();
            if (pool != null) {
                String getComplianceStmt = "select id from security_" + scanType + "_compliance sxc \n" +
                        "where machine_id = (select id from inv_machine where name = '" + machineName + "')\n" +
                        "and assigned_target_name = '" + assignedTargetName.replaceAll("'", "''") + "' \n" +
                        "and profile_id = (select id from security_" + scanType + "_profile where profile_name = '" + profileName.replaceAll("'", "''") + "' " +
                        "and content_id = (select id from security_" + scanType + "_content where content_name = '" + contentName.replaceAll("'", "''") + "'))\n";
                Vector results = ExecuteSelectStatement(pool, getComplianceStmt);
                if (results != null) {
                    ResultSet table = (ResultSet) results.elementAt(0);
                    Statement stmt = (Statement) results.elementAt(1);
                    if (table.next()) {
                        retVal = (Integer) table.getInt(1);
                    }
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
            retVal = -1;
        } finally {
            closePool(pool);
        }
        debug(INFO, "getComplianceId(), retVal - " + retVal);

        return retVal;
    }

    
    public String[] getGroupName(String scanType, String contentName, String ruleName) {
        debug(INFO, "getGroupName(), scanType - " + scanType);
        debug(INFO, "getGroupName(), contentName - " + contentName);
        debug(INFO, "getGroupName(), ruleName - " + ruleName);
        String retVal[] = new String[2];
        IStatementPool pool = null;
        try {
            if (ISecurityServiceConstants.COMPLIANCE_DETAILS_SCAN_TYPE_XCCDF.equals(scanType)) {
                pool = getPool();
                if (pool != null) {
                    String getGroupNameStmt =
                            "select group_id, group_name, group_title from security_xccdf_group_rule re, security_xccdf_content ct, security_xccdf_group gp " +
                                    "where re.rule_name='" + ruleName.replaceAll("'", "''") + "' and ct.content_name='" + contentName.replaceAll("'", "''") + "' and re.group_id=gp.id and gp.content_id=ct.id";
                    Vector results = ExecuteSelectStatement(pool, getGroupNameStmt);
                    if (results != null) {
                        ResultSet table = (ResultSet) results.elementAt(0);
                        Statement stmt = (Statement) results.elementAt(1);
                        if (table.next()) {
                            int groupId = (Integer) table.getInt(1);
                            retVal[0] = (String) table.getString(2);
                            retVal[1] = (String) table.getString(3);
                        }
                        stmt.close();
                    }
                }
            } else if (ISecurityServiceConstants.COMPLIANCE_DETAILS_SCAN_TYPE_OVAL.equals(scanType)) {
                retVal = new String[2];
                retVal[0] = "default_group";
                retVal[1] = "Default Group";
            }
        } catch (SQLException e) {
            if (ERROR) {
                e.printStackTrace();
            }
            retVal = new String[2];
        } finally {
            closePool(pool);
        }
        debug(INFO, "getGroupName(), retVal - " + retVal);

        return retVal;
    }

    public void debug(boolean debugType, String msg) {
        if (debugType || DB_LOGS) {
            securityServiceSettings.getPluginContext().logToConsole("DbDataStorage.java -- " + msg);
        }
    }

}

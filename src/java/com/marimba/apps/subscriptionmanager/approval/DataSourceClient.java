// Copyright 1997-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.approval;

import com.marimba.apps.subscriptionmanager.AuditLogger;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.db.IDatabaseClient;
import com.marimba.intf.db.IStatementPool;
import com.marimba.intf.msf.IDatabaseMgr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
/**
 * This class is used to execute all policy transactions query for peer approval policy
 *
 * @author Selvaraj Jegatheesan
 */
public class DataSourceClient implements IDatabaseClient,IAppConstants,IApprovalPolicyConstants,ISQLConstants {

    private String activeDB;
    private IDatabaseMgr dbmgr;
    private IConnectionPool connPool;
    private AuditLogger logger;
    private DataSourceClient dataSource;
    private int dbType;
    private IStatementPool pool;
    private Connection con;

    final int MAX_IN_LIMIT = 100;
    final int DB_TIME_OUT = 60 * 10000;

//    private String CHECK_DB_CONN = "check_db_connection";

    public DataSourceClient(IDatabaseMgr dbMgr, String activeDB, AuditLogger logger) {
        this.dbmgr = dbMgr;
        this.logger = logger;
        this.activeDB = activeDB;
    }
    /**
     * Initialize Policy Transaction
     * @throws ApprovalPolicyException
     */
    public void initializePolicyTransaction() throws ApprovalPolicyException {
        try {
            createConnection();
            pool = createStatementPool();
            if(null != pool) {
                con = pool.getConnection();
            } else {
                throw new ApprovalPolicyException("Database Connection is failed");
            }
        } catch(Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to initialize DataSource for storing Approval Policy - " + e.getMessage());
        }
    }
    public DataSourceClient init(IDatabaseMgr dbMgr, String activeDB, AuditLogger logger) throws ApprovalPolicyException {
        if (dataSource != null && !isDBChanged(activeDB)) {
            return dataSource;
        } else {
            dataSource = new DataSourceClient(dbmgr, activeDB, logger);
            return dataSource;
        }
    }

    /**
     * Modified AutoCommit as 'false' and return old AutoCommit value
     * @return
     */
    public boolean disableAutoCommit() {
        boolean oldValue = false;
        try {
            if(null != con) {
                oldValue = con.getAutoCommit();
                con.setAutoCommit(false);
            }
        } catch(Exception e) {
            // Need to handle
            System.out.println("DataSourceClient : Failed to set Auto Commit - > false");

        } finally {
            return oldValue;
        }
    }
    /**
     * Commit all Policy transaction and set AutoCommit value to old value
     * @param oldValue
     */
    public void savePolicyTransaction(boolean oldValue) {
        try {
            if (null != con) {
                con.commit();
            }
        } catch(Exception e) {
            // Need to handle
            System.out.println("DataSourceClient : Failed to set Auto Commit - > true");
        } finally {
            try {
                if(null != con) {
                    con.setAutoCommit(oldValue);
                }
            } catch(Exception ex) {
                //
            }
        }
    }
    /**
     * RollBack all transaction if transaction failed
     */
    public void rollBackTransaction() {
        try {
            if(null != con) {
                System.out.println("DataSourceClient : Policy transaction has been rollback");
                con.rollback();
            }
        } catch(Exception e) {
            // Need to handle
            System.out.println("DataSourceClient : Failed to rollback transaction for peer approval policy");
        }
    }
    /**
     * Close all transaction
     */
    public void closePolicyTransaction() {
        try {
            if(null != pool) {
                connPool.releaseConnection(pool);
            }
        } catch(Exception e) {
            // Need to handle
            System.out.println("DataSourceClient : Failed to close Policy Transaction");
        }
    }
    /**
     * To verify Database changed or not
     * @param newdbName
     * @return
     */
    private boolean isDBChanged(String newdbName) {
        if (newdbName != null && activeDB != null) {
            return (!newdbName.equals(activeDB));
        } else {
            return true;
        }
    }
    /**
     * Create a new connection from dbmgr connection pool
     * @throws ApprovalPolicyException
     */
    public void createConnection() throws ApprovalPolicyException {
        if (activeDB != null) {
            connPool = dbmgr.getPool(activeDB, this);
            if(null != connPool) {
                this.dbType = connPool.getDBProduct();
                debug("DB Connection successful");
            } else {
                throw new ApprovalPolicyException("Database Connection is failed");
            }
        }
    }
    /**
     * Create a new Statement pool
     * @return
     * @throws ApprovalPolicyException
     */
    public IStatementPool createStatementPool() throws ApprovalPolicyException {
        IStatementPool stmtPool = null;
        try {
            stmtPool = connPool.getConnection(DB_TIME_OUT);
            if (null == stmtPool) {
                throw new ApprovalPolicyException("Statement Pool is Empty");
            }
        } catch(Exception ex) {
            throw new ApprovalPolicyException("Statement Pool is Empty");
        }
        return stmtPool;
    }
    /**
     * Create a new statement
     * @return
     * @throws ApprovalPolicyException
     */
    private Statement createStatement(IStatementPool stmtPool) throws ApprovalPolicyException {
        Statement stmt = null;
        try {
            if (null != stmtPool) {
                stmt = stmtPool.createStatement();
            }
        } catch (SQLException e) {
            debug("Failed to create Statement - "+ e.getMessage());
        }
        return stmt;
    }
    /**
     * To verify Database Connection
     * @return
     * @throws ApprovalPolicyException
     */
    public boolean checkDBConn() throws ApprovalPolicyException {
        Statement stmt = null;
        ResultSet rslt = null;
        boolean success = false;
        IStatementPool stmtPool = null;
        try {
            String db_check_query = "select version from schema_version where type='inventory'";
            stmtPool = createStatementPool();
            if(null != stmtPool) {
                stmt = createStatement(stmtPool);
                rslt = stmt.executeQuery(db_check_query);
                success = true;
            }

        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to check DB connection - " + e.getMessage());
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                if(DEBUG5) sqle.printStackTrace();
            }
            if(null != stmtPool) {
                connPool.releaseConnection(stmtPool);
            }
        }
        debug("Database connection successful ? " + success);
        return success;
    }

    /**
     * Remove existing policy if same user modified to same policy
     * @param changePolicy
     * @throws ApprovalPolicyException
     */
    public void removeExistingPolicy(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        Statement stmt = null;
        ResultSet result = null;
        String queryStr = null;
        int change_id = 0;
        int existingPolicyAction = 0;

        try {
            if(null != con) {
                stmt = con.createStatement();
                queryStr = "select change_id,action from policy_change_request where UPPER(policy_target) = UPPER('"+ changePolicy.getPolicyTargetId() +"') and " +
                	"UPPER(ldap_source) = UPPER('"+ changePolicy.getLdapSource() +"') and UPPER(change_owner) = UPPER('"+ changePolicy.getChangeOwner() +"')" +
                	" and (status = " + POLICY_PENDING + " or status = " + POLICY_PENDING_COPYOPERATION + ")";
                debug("Query String : " + queryStr);
                result = stmt.executeQuery(queryStr);
                if(result.next()) {
                    change_id = result.getInt("change_id");
                    existingPolicyAction = result.getInt("action");
                    System.out.println("Same user has already policy entry in DB. So the current policy overwrite to existing policy");
                    queryStr = "delete from policy_change_request where change_id = '"+ change_id +"'";
                    debug("Query String : " + queryStr);
                    stmt.execute(queryStr);
                }
            }
        } catch (Exception ex) {
            if(DEBUG5) ex.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute query - " + ex.getMessage());
        } finally {
            try {
                if (result != null) result.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }

    }
    /**
     * Create Policy Change request
     * @param changePolicy
     * @throws ApprovalPolicyException
     */
    public void storePolicyChangeRequest(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        ResultSet rslt = null;
        PreparedStatement stmt = null;
        int policyAction = 0;
        String queryStr = null;

        try {
            policyAction = changePolicy.getPolicyAction();
            if(null != con) {
                if (IConnectionPool.ORACLE == dbType || IConnectionPool.ORACLE_RAC == dbType) {
                    queryStr = INSERT_POLICY_CHANGE_REQUEST_ORACLE;
                }
                if (IConnectionPool.SQLSERVER == dbType) {
                    queryStr = INSERT_POLICY_CHANGE_REQUEST_SQL;
                }
                debug("Query String : " + queryStr);

                if(null != queryStr) {
                    stmt = con.prepareStatement(queryStr);
                    stmt.setString(1, changePolicy.getPolicyTargetName());
                    stmt.setString(2, changePolicy.getPolicyTargetId());
                    stmt.setString(3, changePolicy.getPolicyTargetType());
                    stmt.setString(4, changePolicy.getLdapSource());
                    stmt.setInt(5, changePolicy.getPolicyAction());
                    stmt.setInt(6, changePolicy.getPolicyStatus());
                    stmt.setString(7, changePolicy.getArReferenceTag());
                    stmt.setString(8, changePolicy.getSoftwarePath());
                    stmt.setString(9, changePolicy.getTxGroup());
                    stmt.setString(10, changePolicy.getTxUser());
                    stmt.setString(11, changePolicy.getAllTarget());
                    stmt.setString(12, changePolicy.getBlackoutSchedule());
                    stmt.setString(13, changePolicy.getChangeOwner());
                    stmt.setString(14, changePolicy.getReviewedBy());
                    stmt.setString(15, changePolicy.getRemarks());
                    stmt.setTimestamp(16, changePolicy.getReviewed_on());
                    stmt.setTimestamp(17, changePolicy.getCreated_on());
                    stmt.executeUpdate();
                    int policyId = getPolicyChangeRequestId(changePolicy);
                    changePolicy.setChangeId(policyId);
                }
            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute query - " + e.getMessage());
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }

    }
    /**
     * Get Policy Change Request Id
     * @param changePolicy
     * @return
     * @throws ApprovalPolicyException
     */
    public int getPolicyChangeRequestId(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        Statement stmt = null;
        String queryStr = null;
        ResultSet result = null;
        int policyChangeRequestId = 0;
        try {
            queryStr = "select change_id from policy_change_request where UPPER(policy_name) = UPPER('"+changePolicy.getPolicyTargetName()+"') " +
            	"and UPPER(policy_target) = UPPER('"+changePolicy.getPolicyTargetId()+"') and UPPER(change_owner) = UPPER('"+changePolicy.getChangeOwner()+"')" +
            	" and (status = " + POLICY_PENDING + " or status = " + POLICY_PENDING_COPYOPERATION + ")";
            if(null != queryStr) {

                debug("Query String : " + queryStr);
                if(null != con) {
                    stmt = con.createStatement();
                    result = stmt.executeQuery(queryStr);
                    if(result.next()) {
                        policyChangeRequestId = result.getInt("change_id");
                    }
                }
            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute query - " + e.getMessage());
        } finally {
            try {
                if (null != result) result.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return policyChangeRequestId;
    }
    /**
     * Store all Policy Channels to DB
     * @param changePolicy
     * @throws ApprovalPolicyException
     */
    public void storePolicyChannels(ApprovalPolicyDTO changePolicy) throws ApprovalPolicyException {
        ResultSet rslt = null;
        PreparedStatement stmt = null;
        int policyAction = 0;
        String queryStr = null;
        int policyChannelId = 0;

        try {
            if(null != con) {
                if (IConnectionPool.ORACLE == dbType || IConnectionPool.ORACLE_RAC == dbType) {
                    queryStr = INSERT_POLICY_CHANNELS_ORACLE;
                }
                if (IConnectionPool.SQLSERVER == dbType) {
                    queryStr = INSERT_POLICY_CHANNELS_SQL;
                }
                debug("Query String : " + queryStr);
                stmt = con.prepareStatement(queryStr);
                if(null != queryStr) {
                    for (ApprovalChannelDTO channel : changePolicy.getPolicyChannels()) {
                        stmt.setInt(1, channel.getChannelID());
                        stmt.setInt(2, changePolicy.getChangeId());
                        stmt.setInt(3, channel.getChannelAction());
                        stmt.setString(4, channel.getPrimaryStateSchedule());
                        stmt.setString(5, channel.getSecondaryStateSchedule());
                        stmt.setString(6, channel.getUpdateSchedule());
                        stmt.setString(7, channel.getRepairSchedule());
                        stmt.setInt(8, channel.getPriority());
                        stmt.setString(9, channel.getWowStatus());
                        stmt.setString(10, channel.getExemptBlackout());
                        stmt.execute();
                        debug("Channel is added successfully : " + channel.getM_channelURL());
                        debug("Channel Property List size is: " + channel.getPropertyList().size());
                        if(channel.getPropertyList().size() > 0) {
                            policyChannelId = getPolicyChannelId(channel, changePolicy.getChangeId());
                            channel.setPolicyChannelId(policyChannelId);
                            storePolicyChannelProps(channel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute query - " + e.getMessage());
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
    }
    /**
     * Get Policy Channel Id
     * @param ch
     * @return
     * @throws ApprovalPolicyException
     */
    public int getPolicyChannelId(ApprovalChannelDTO ch, int changeId) throws ApprovalPolicyException {
        ResultSet rslt = null;
        Statement stmt = null;
        String queryStr = null;
        int policyChannelId = 0;
        try {
            if(null != con) {
                queryStr = "Select policy_channel_id from policy_channels where channel_id = "+ ch.getChannelID()+ " and change_id = "+changeId;
                debug("Query String :" + queryStr);
            }
            if(null != queryStr) {
                stmt = con.createStatement();
                rslt = stmt.executeQuery(queryStr);
                while(rslt.next()) {
                    if( policyChannelId < rslt.getInt("policy_channel_id")) {
                        policyChannelId = rslt.getInt("policy_channel_id");
                    }
                }
            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute query - " + e.getMessage());
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return policyChannelId;
    }
    /**
     * Store all policy channel properties
     * @param channel
     * @throws ApprovalPolicyException
     */
    public void storePolicyChannelProps(ApprovalChannelDTO channel) throws ApprovalPolicyException {
        ResultSet rslt = null;
        PreparedStatement stmt = null;
        String queryStr = null;
        boolean isChanged = false;

        try {
            if(null != con) {
                queryStr = "insert into policy_channel_property(policy_channel_id,property_id,property_value,property_action)values(?,?,?,?)";
                debug("Query String :" + queryStr);
            }
            if(null != queryStr && channel.getPropertyList().size() > 0) {
                stmt = con.prepareStatement(queryStr);
                for(ApprovalPropertyDTO chProp : channel.getPropertyList()) {
                    stmt.setInt(1, channel.getPolicyChannelId());
                    stmt.setInt(2, chProp.getPropertyId());
                    stmt.setString(3, chProp.getPropValue());
                    stmt.setInt(4, chProp.getPropAction());
                    stmt.addBatch();
                    isChanged = true;
                }
                if(null != stmt && isChanged) {
                    stmt.executeBatch();
                }

            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute query - " + e.getMessage());
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
    }
    /**
     * Store all Policy Tuner Properties
     * @param policyChange
     * @throws ApprovalPolicyException
     */
    public void storePolicyTunerProps(ApprovalPolicyDTO policyChange) throws ApprovalPolicyException {
        ResultSet rslt = null;
        PreparedStatement stmt = null;
        String queryStr = null;
        boolean isChanged = false;

        try {
            if(null != con) {
                queryStr = "insert into policy_tuner_property(property_id,change_id,property_value,property_action)values(?,?,?,?)";
                debug("Query String :" + queryStr);
            }
            if(null != queryStr && policyChange.getTunerProps().size() > 0) {
                stmt = con.prepareStatement(queryStr);
                for(ApprovalPropertyDTO tunerProp : policyChange.getTunerProps()) {
                    stmt.setInt(1, tunerProp.getPropertyId());
                    stmt.setInt(2, policyChange.getChangeId());
                    stmt.setString(3, tunerProp.getPropValue());
                    stmt.setInt(4, tunerProp.getPropAction());
                    stmt.addBatch();
                    isChanged = true;
                }
                if(null != stmt && isChanged) {
                    stmt.executeBatch();
                }

            }
        } catch (SQLException e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute query - " + e.getMessage());
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
    }
    /**
     * Get Policy Channel Id
     * @param ch
     * @return
     * @throws ApprovalPolicyException
     */
    public int getChannelId(ApprovalChannelDTO ch) throws ApprovalPolicyException {
        int channelId = 0;
        CallableStatement proc = null;
        ResultSet result = null;
        try {
            if(null != con) {
                debug("Calling AddPolicyChannels procedure for the channel :" + ch.getM_channelURL());
                String contentType = (null == ch.getContentType()) ? "contenttype.application" : ch.getContentType();
                String title = (null == ch.getTitle()) ? "" : ch.getTitle();
                proc = con.prepareCall("{ call AddPolicyChannels(?,?,?, ?) }");
                proc.registerOutParameter(1, Types.INTEGER);
                proc.setString(2, ch.getM_channelURL());
                proc.setString(3, contentType);
                proc.setString(4, title);
                proc.execute();
                channelId = proc.getInt(1);
                return channelId;
            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute AddPolicyChannels procedure - " + e.getMessage());
        } finally {
            try {
                if (null != proc) proc.close();
            } catch (SQLException sqle){
                debug("Failed to close procedure call - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return channelId;
    }
    /**
     * Get Tuner/Channel Property Id
     * @param prop
     * @return
     * @throws ApprovalPolicyException
     */
    public int getPropertyId(ApprovalPropertyDTO prop) throws ApprovalPolicyException {
        int propertyId = 0;
        CallableStatement proc = null;
        ResultSet result = null;
        try {
            if(null != con) {
                debug("Calling AddPolicyProperty procedure for the Property");
                proc = con.prepareCall("{ call AddPolicyProperty(?,?, ?) }");
                proc.registerOutParameter(1, Types.INTEGER);
                proc.setString(2, prop.getPropKey());
                proc.setString(3, prop.getPropType());
                proc.execute();
                propertyId = proc.getInt(1);
                return propertyId;
            }
        } catch (Exception e) {
            if(DEBUG5) e.printStackTrace();
            throw new ApprovalPolicyException("Failed to execute AddPolicyProperty procedure - " + e.getMessage());
        } finally {
            try {
                if (null != proc) proc.close();
            } catch (SQLException sqle){
                debug("Failed to close procedure call - " + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return propertyId;
    }

    public void getPoliciesByUser(String user) {

    }

    public List<ApprovalPolicyDTO> loadAllPolicies() {
        String query = "select * from policy_change_request";
        Statement stmt = null;
        ResultSet rslt = null;
        //IStatementPool stmtPool = null;
        List<ApprovalPolicyDTO> allPolicies = new ArrayList<ApprovalPolicyDTO>(10);
        try {
            if(null != query) {
                debug("Query String : " + query);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                while (rslt.next()) {
                    ApprovalPolicyDTO policy = new ApprovalPolicyDTO();
                    policy.setChangeId(rslt.getInt("change_id"));
                    policy.setPolicyTargetName(rslt.getString("policy_name"));
                    policy.setPolicyTargetId(rslt.getString("policy_target"));
                    policy.setPolicyStatus(rslt.getInt("status"));
                    policy.setPolicyTargetType(rslt.getString("policy_target_type"));
                    policy.setArReferenceTag(rslt.getString("ar_reference_tag"));
                    policy.setSoftwarePath(rslt.getString("software_path"));
                    policy.setTxGroup(rslt.getString("tx_group"));
                    policy.setTxUser(rslt.getString("tx_user"));
                    policy.setAllTarget(rslt.getString("all_target"));
                    policy.setBlackoutSchedule(rslt.getString("blackout_schedule"));
                    policy.setChangeOwner(rslt.getString("change_owner"));
                    policy.setReviewedBy(rslt.getString("reviewed_by"));
                    policy.setRemarks(rslt.getString("remarks"));
                    policy.setReviewed_on(rslt.getTimestamp("reviewed_on"));
                    policy.setCreated_on(rslt.getTimestamp("created_on"));
                    allPolicies.add(policy);
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return allPolicies;
    }
    public boolean loadBlobPolicyByTarget(String targetName, String targetId, String targetType, String dataDir) {
        boolean status = false;
        String query = "select report_file from security_reports where target_name = '{0}'";
        query = query.replace("{0}", targetId);
        Statement stmt = null;
        ResultSet rslt = null;
        //IStatementPool stmtPool = null;
        try {
            if(null != query) {
                debug("Query String : " + query);
                String dirName = dataDir + File.separator + "scapreports";
                File dirFile = new File(dirName);
                if(!dirFile.exists()) {
                	dirFile.mkdir();
                }
                String filePath = dirName + File.separator + targetId + ".html";
                System.out.println("File path :" + filePath);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                if (!rslt.next()) {
                    System.out.println("No such file stored.");
                  } else {
                	  System.out.println("Getting binary stream...");
                	  BufferedOutputStream os;
              		  FileOutputStream fos = new FileOutputStream(filePath);

                    byte[] buffer = new byte[1024];
                    InputStream is = rslt.getBinaryStream(1);
                    while (is.read(buffer) > 0) {
                      fos.write(buffer);
                    }
                    fos.close();
                  }
                File reportFile = new File(filePath);
                if(reportFile.exists()) {
                	System.out.println("report exists.. :" + reportFile.getAbsolutePath());
                	status = true;
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                sqle.printStackTrace();
            }
        }
        return status;
    }
    public List<ApprovalPolicyDTO> loadPolicyByUser(String user) {
        List<ApprovalPolicyDTO> policies = new ArrayList<ApprovalPolicyDTO>(10);
        String query = "select * from policy_change_request where UPPER(change_owner) = UPPER('{0}')";
        query = query.replace("{0}", user);
        Statement stmt = null;
        ResultSet rslt = null;
        //IStatementPool stmtPool = null;
        try {
            if(null != query) {
                debug("Query String : " + query);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                while (rslt.next()) {
                    ApprovalPolicyDTO policy = new ApprovalPolicyDTO();
                    policy.setChangeId(rslt.getInt("change_id"));
                    policy.setPolicyTargetName(rslt.getString("policy_name"));
                    policy.setPolicyTargetId(rslt.getString("policy_target"));
                    policy.setPolicyStatus(rslt.getInt("status"));
                    policy.setPolicyTargetType(rslt.getString("policy_target_type"));
                    policy.setArReferenceTag(rslt.getString("ar_reference_tag"));
                    policy.setSoftwarePath(rslt.getString("software_path"));
                    policy.setTxGroup(rslt.getString("tx_group"));
                    policy.setTxUser(rslt.getString("tx_user"));
                    policy.setAllTarget(rslt.getString("all_target"));
                    policy.setBlackoutSchedule(rslt.getString("blackout_schedule"));
                    policy.setChangeOwner(rslt.getString("change_owner"));
                    policy.setReviewedBy(rslt.getString("reviewed_by"));
                    policy.setRemarks(rslt.getString("remarks"));
                    policy.setReviewed_on(rslt.getTimestamp("reviewed_on"));
                    policy.setCreated_on(rslt.getTimestamp("created_on"));
                    policies.add(policy);
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return policies;
    }
    public List<ApprovalPolicyDTO> loadPendingPolicyByTarget(String targetName, String targetId, String targetType) {
    	
    	List<ApprovalPolicyDTO> policies = new ArrayList<ApprovalPolicyDTO>(10);
    	
    	String query =  "select * from policy_change_request where UPPER(policy_target) = UPPER('{0}')" +
    			 " and (status = " + POLICY_PENDING + " or status = " + POLICY_PENDING_COPYOPERATION + ")"; 
        if("all".equalsIgnoreCase(targetType)) {
        	targetId = targetName; // set value as "All Endpoints"
        }
    	//query = query.replace("{0}", targetName);
        query = query.replace("{0}", targetId);
        
        Statement stmt = null;
        ResultSet rslt = null;

        try {
            if(null != query) {
                debug("Query String : " + query);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                while (rslt.next()) {
                    ApprovalPolicyDTO policy = new ApprovalPolicyDTO();
                    policy.setChangeId(rslt.getInt("change_id"));
                    policy.setPolicyTargetName(rslt.getString("policy_name"));
                    policy.setPolicyTargetId(rslt.getString("policy_target"));
                    policy.setPolicyStatus(rslt.getInt("status"));
                    policy.setPolicyTargetType(rslt.getString("policy_target_type"));
                    policy.setArReferenceTag(rslt.getString("ar_reference_tag"));
                    policy.setSoftwarePath(rslt.getString("software_path"));
                    policy.setTxGroup(rslt.getString("tx_group"));
                    policy.setTxUser(rslt.getString("tx_user"));
                    policy.setAllTarget(rslt.getString("all_target"));
                    policy.setBlackoutSchedule(rslt.getString("blackout_schedule"));
                    policy.setChangeOwner(rslt.getString("change_owner"));
                    policy.setReviewedBy(rslt.getString("reviewed_by"));
                    policy.setRemarks(rslt.getString("remarks"));
                    policy.setReviewed_on(rslt.getTimestamp("reviewed_on"));
                    policy.setCreated_on(rslt.getTimestamp("created_on"));
                    policies.add(policy);
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return policies;
    }
    public void loadPolicy(String query, ApprovalPolicyDTO policy) {
        Statement stmt = null;
        ResultSet rslt = null;
        IStatementPool stmtPool = null;
        try {
            if(null != query) {
                debug("Query String : " + query);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                while (rslt.next()) {
                    policy.setChangeId(rslt.getInt("change_id"));
                    policy.setPolicyTargetName(rslt.getString("policy_name"));
                    policy.setPolicyTargetId(rslt.getString("policy_target"));
                    policy.setPolicyAction(rslt.getInt("action"));
                    policy.setPolicyStatus(rslt.getInt("status"));
                    policy.setPolicyTargetType(rslt.getString("policy_target_type"));
                    policy.setArReferenceTag(rslt.getString("ar_reference_tag"));
                    policy.setSoftwarePath(rslt.getString("software_path"));
                    policy.setTxGroup(rslt.getString("tx_group"));
                    policy.setTxUser(rslt.getString("tx_user"));
                    policy.setAllTarget(rslt.getString("all_target"));
                    policy.setBlackoutSchedule(rslt.getString("blackout_schedule"));
                    policy.setChangeOwner(rslt.getString("change_owner"));
                    policy.setReviewedBy(rslt.getString("reviewed_by"));
                    policy.setRemarks(rslt.getString("remarks"));
                    policy.setReviewed_on(rslt.getTimestamp("reviewed_on"));
                    policy.setCreated_on(rslt.getTimestamp("created_on"));
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
    }

    public void loadChannel(ApprovalPolicyDTO policy) {
        Statement stmt = null;
        ResultSet rslt = null;
        IStatementPool stmtPool = null;
        String query = "select * from policy_channel_change_request where change_id = " + policy.getChangeId();
        try {
            if(null != query) {
                debug("Query String : " + query);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                while (rslt.next()) {
                    ApprovalChannelDTO channel = new ApprovalChannelDTO();
                    channel.setChannelAction(rslt.getInt("channel_action"));
                    channel.setContentType(rslt.getString("content_type"));
                    channel.setExemptBlackout(rslt.getString("exempt_blackout"));
                    channel.setM_changeID(policy.getChangeId());
                    channel.setM_channelURL(rslt.getString("url"));
                    channel.setPolicyChannelId(rslt.getInt("policy_channel_id"));
                    channel.setPrimaryStateSchedule(rslt.getString("primary_state_schedule"));
                    channel.setPriority(rslt.getInt("priority"));
                    channel.setRepairSchedule(rslt.getString("repair_schedule"));
                    channel.setSecondaryStateSchedule(rslt.getString("secondary_state_schedule"));
                    channel.setTitle(rslt.getString("title"));
                    channel.setUpdateSchedule(rslt.getString("update_schedule"));
                    channel.setWowStatus(rslt.getString("wow_status"));
                    System.out.println("Loading channel property details");
                    loadChannelProperty(channel);
                    policy.addChannel(channel);
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
    }


    public void loadChannelProperty(ApprovalChannelDTO channel) {
        Statement stmt = null;
        ResultSet rslt = null;
        IStatementPool stmtPool = null;
        String query = "select * from policy_channel_property_detail where policy_channel_id = "
                + channel.getPolicyChannelId() + " and UPPER(property_type) = UPPER('channel')";
        try {
            if(null != query) {
                debug("Query String : " + query);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                while (rslt.next()) {
                    System.out.println("Adding channel property");
                    ApprovalPropertyDTO chProps = new ApprovalPropertyDTO();
                    chProps.setPropAction(rslt.getInt("property_action"));
                    chProps.setPropertyId(rslt.getInt("property_id"));
                    chProps.setPropKey(rslt.getString("property_name"));
                    chProps.setPropType(rslt.getString("property_type"));
                    chProps.setPropValue(rslt.getString("property_value"));
                    channel.addPropertyList(chProps);
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
    }


    public void loadTunerProperty(ApprovalPolicyDTO policy) {
        Statement stmt = null;
        ResultSet rslt = null;
        IStatementPool stmtPool = null;
        String query = "select * from policy_tuner_property_detail where change_id = " + policy.getChangeId() ;
        try {
            if(null != query) {
                debug("Query String : " + query);
                stmt = con.createStatement();
                rslt = stmt.executeQuery(query);

                while (rslt.next()) {
                    ApprovalPropertyDTO tunerProps = new ApprovalPropertyDTO();
                    tunerProps.setPropAction(rslt.getInt("property_action"));
                    tunerProps.setPropertyId(rslt.getInt("property_id"));
                    tunerProps.setPropKey(rslt.getString("property_name"));
                    tunerProps.setPropType(rslt.getString("property_type"));
                    tunerProps.setPropValue(rslt.getString("property_value"));
                    policy.addTunerProperty(tunerProps);
                }
            }
        } catch (SQLException e) {
            debug("Unable to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } catch (Exception ex) {
            debug("Unexpected exception : " + ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
    }


    public ApprovalPolicyDTO loadPolicyByUserAndPolicyName(String policyName, String user) {
        String query = "";
        ApprovalPolicyDTO policy = null;
        try {
            query = "select * from policy_change_request where UPPER(policy_name) = UPPER('" + policyName + "')" +
                    " and UPPER(change_owner) = UPPER('" + user + "')";
            policy = new ApprovalPolicyDTO();
            loadPolicy(query, policy);
        } catch (Exception ex) {
            System.out.println("Exception occoured while trying to load policy object " + ex.getMessage());
            ex.printStackTrace();
        }

        return policy;

    }

    public ApprovalPolicyDTO loadPolicyByUserPolicyNameAndState(String policyName, String user, String status) {
        String query = "";
        ApprovalPolicyDTO policy = null;
        try {
            query = "select * from policy_change_request where UPPER(policy_name) = UPPER('" + policyName + "')" +
                    " and UPPER(change_owner) = UPPER('" + user + "') " +
                    " and status = " + status ;
            policy = new ApprovalPolicyDTO();
            loadPolicy(query, policy);
        } catch (Exception ex) {
            System.out.println("Exception occoured while trying to load policy object " + ex.getMessage());
            ex.printStackTrace();
        }

        return policy;
    }

    public ApprovalPolicyDTO loadPolicyByPolicyID(int changeId) {
        String query = "";
        ApprovalPolicyDTO policy = null;
        try {
            query = "select * from policy_change_request where change_id = " + changeId;
            policy = new ApprovalPolicyDTO();
            loadPolicy(query, policy);
        } catch (Exception ex) {
            System.out.println("Exception occoured while trying to load policy object " + ex.getMessage());
            ex.printStackTrace();
        }

        return policy;
    }

    public ApprovalPolicyDTO loadPolicyByUserAndSubContainer(String user, String subContainer) {
        String query = "";
        ApprovalPolicyDTO policy = null;
        try {
            query = "select * from policy_change_request where UPPER(change_owner) = UPPER('" + user + "') and " +
                    "UPPER(ldap_source) = UPPER('" + subContainer  + "')";
            policy = new ApprovalPolicyDTO();
            loadPolicy(query, policy);
        } catch (Exception ex) {
            System.out.println("Exception occoured while trying to load policy object " + ex.getMessage());
            ex.printStackTrace();
        }

        return policy;
    }
    
    public boolean updatePolicyApprovalStatus(ApprovalStatusBean statusBean) {
        ResultSet rslt = null;
        PreparedStatement stmt = null;
        String queryStr = null;
        boolean isUpdated = false;
        Timestamp reviewdOn = null;
        try {
            System.out.println("Update Approval Policy change to DB");
            if(null != con) {
                queryStr = "update policy_change_request set status = ?,remarks = ?,reviewed_by = ?,reviewed_on=? where change_id = ?";
                debug("Query String : " + queryStr);
            }
            if(null != queryStr) {
                stmt = con.prepareStatement(queryStr);
                stmt.setInt(1, statusBean.getApprovalType());
                stmt.setString(2, statusBean.getRemarks());
                stmt.setString(3, statusBean.getReviewedBy());
                try {
                    reviewdOn = Utils.getCurrentTimeStamp();
                } catch(Exception e) {
                    System.out.println("Failed to get current time");
                }
                stmt.setTimestamp(4, reviewdOn);
                stmt.setInt(5, statusBean.getChangeId());
                stmt.executeUpdate();
                isUpdated = true;
                statusBean.setReviewedOn(new SimpleDateFormat(MAIL_DATE_FORMAT, Locale.US).format(reviewdOn));
            }
        } catch (Exception e) {
            debug("Failed to execute query : " + e.getMessage());
            if(DEBUG5) e.printStackTrace();
        } finally {
            try {
                if (rslt != null) rslt.close();
                if (null != stmt) stmt.close();
            } catch (SQLException sqle){
                debug("Failed to close result set or statement :" + sqle.getMessage());
                if(DEBUG5) sqle.printStackTrace();
            }
        }
        return isUpdated;
    }
    
    // todo - remove these methods with inputs from CMS
    public void initialize(DatabaseMetaData md) throws SQLException {}

    public void create(IStatementPool pool) {}

    public void notify(Object sender, int msg, Object arg) {}

    private void debug(String msg) {
        if(DEBUG5) {
            System.out.println("DataSourceClient : "+ msg);
        }
    }
}

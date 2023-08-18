package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.db.IStatementPool;
import com.marimba.apps.subscriptionmanager.webapp.bean.CVEFormBean;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @Author Yogesh Pawar
 * @Date: 15/08/2023 Description : Reading the json data and inserting it into the Database in
 * Batches.
 */
public class CVEDataInsertQueryRunner extends DatabaseAccess {


  public CVEDataInsertQueryRunner(SubscriptionMain main, String jsonFilePath) {
    try {

      runQuery(new InsertCveDetailsInBulk(main, jsonFilePath));

    } catch (Exception ex) {

      ex.printStackTrace();

    }

  }

  class InsertCveDetailsInBulk extends com.marimba.apps.securitymgr.db.QueryExecutor {

    String jsonDir;

    String jsonFilePath;

    private Connection connection;
    private PreparedStatement cveInfoStmt;
    private PreparedStatement cveVendorInfoStmt;
    private PreparedStatement cveProductStmt;
    private int batchCounter = 0;

    private int cveIndex = 0;

    private int productIndex = 0;

    private int vendorIndex = 0;

    private JsonFactory jsonFactory;
    private ObjectMapper objectMapper;

    private JsonParser jsonParser;
    int childBatchSize = 600000;
    int childBatchRowCounter = 0;
    int childBatchCounter = 0;


    public InsertCveDetailsInBulk(SubscriptionMain main, String jsonFilePath) {
      super(main);
      this.jsonFilePath = jsonFilePath;
      this.jsonFactory = new JsonFactory();
      this.objectMapper = new ObjectMapper();
    }


    protected void execute(IStatementPool pool) {
      int batchSize = 8000;
      int rowCount = 0;
      long startTime = System.currentTimeMillis();

      try {
        connection = pool.getConnection();
        truncateExistingData(connection);
        setupStatements(connection);
        connection.setAutoCommit(false);

        try (FileInputStream fis = new FileInputStream(jsonFilePath)) {
          jsonParser = createJsonParser(fis);

          while (jsonParser.nextToken() != null) {
            JsonNode recordNode = objectMapper.readTree(jsonParser);
            processRecordNode(recordNode);
            rowCount++;
            if (rowCount % batchSize == 0) {
              batchCounter++;
              executeBatchStatements();
              info("**** Executed Parent Batch ****", batchCounter);
              rowCount = 0;
            }
          }

          if (rowCount > 0) {
            batchCounter++;
            executeBatchStatements();
          }

          finalizeExecution();
        }

        long endTime = System.currentTimeMillis();
        printExecutionSummary(startTime, endTime);

      } catch (Exception e) {
        error(e.getMessage());
      }
    }

    /**
     * Before inserting the bulk data truncate the existing old records
     *
     * @param connection
     */
    private void truncateExistingData(Connection connection) {
      try (Statement statement = connection.createStatement()) {
        String[] tablesToTruncate = {"cve_info", "cve_product_info", "cve_vendor_info"};

        for (String table : tablesToTruncate) {
          statement.addBatch("TRUNCATE TABLE " + table);
        }

        statement.executeBatch();

        info("Truncated data from cve_info, cve_product_info, cve_vendor_info tables");

      } catch (SQLException e) {
        error(e.getMessage());
      }
    }


    private JsonParser createJsonParser(FileInputStream fis) {
      try {
        return jsonFactory.createParser(fis);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      return null;
    }

    private void setupStatements(Connection connection) throws SQLException {
      connection.setAutoCommit(false);
      cveInfoStmt = getProductCveStatement(connection);
      cveVendorInfoStmt = getVendorStatement(connection);
      cveProductStmt = getProductStatement(connection);
    }

    private void processRecordNode(JsonNode recordNode) throws SQLException {
      getCveInfo(recordNode, cveInfoStmt);
      if (recordNode.has("vulnerable_product")) {
        JsonNode vulnerableProductArray = recordNode.get("vulnerable_product");
        if (vulnerableProductArray.isArray()) {
          getVendorAndProductObjects(vulnerableProductArray);
        }
      }
    }

    private void getVendorAndProductObjects(JsonNode vulnerableProductArray) {
      getCveVendorInfo(vulnerableProductArray, cveVendorInfoStmt);
      getCveProductInfo(vulnerableProductArray, cveProductStmt);
    }

    private void executeBatchStatements() throws SQLException {
      executeBatchStatements(cveInfoStmt, cveVendorInfoStmt, cveProductStmt);
    }

    private void finalizeExecution() {
      try {
        jsonParser.close();
        cveInfoStmt.close();
        cveVendorInfoStmt.close();
        cveProductStmt.close();
        connection.commit();
        connection.close();
        info("Data inserted successfully!");
      } catch (Exception e) {
        handleError(e);
      }

    }

    private void printExecutionSummary(long startTime, long endTime) {
      info("End time  stamp :" + endTime);
      info("All Data inseration time  : " + (endTime - startTime) + " milliseconds");
    }

    public PreparedStatement getProductStatement(Connection connection) throws SQLException {

      return connection.prepareStatement(
          "INSERT INTO cve_product_info (id, name, version, cve_id) VALUES (?, ?, ?, ?)");
    }

    public PreparedStatement getVendorStatement(Connection connection2) throws SQLException {
      return connection.prepareStatement(
          "INSERT INTO cve_vendor_info (id, name, cve_id) VALUES (?, ?, ?)");
    }

    public PreparedStatement getProductCveStatement(Connection connection) throws SQLException {
      return connection.prepareStatement(
          "INSERT INTO cve_info (id, name, modified, published, severity, cvss, cvss_time, cwe, cve_id, summary) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

    }

    public void getCveInfo(JsonNode recordNode, PreparedStatement productCveStatement) {

      try {

        cveIndex++;
        ObjectMapper myObject = new ObjectMapper();
        CVEFormBean cveForm = myObject.treeToValue(recordNode, CVEFormBean.class);

        productCveStatement.setInt(1, cveIndex);
        productCveStatement.setString(2, cveForm.getCveId());

        productCveStatement.setTimestamp(3, Timestamp.valueOf(cveForm.getModified()));

        productCveStatement.setTimestamp(4, Timestamp.valueOf(cveForm.getPublished()));
        productCveStatement.setString(5, null);
        if (cveForm.getCvss() != null) {
          productCveStatement.setString(6, cveForm.getCvss());

        } else {
          productCveStatement.setString(6, "-1");

        }
        productCveStatement.setTimestamp(7, Timestamp.valueOf(cveForm.getModified()));
        productCveStatement.setString(8, cveForm.getCwe());
        productCveStatement.setString(9, cveForm.getCveId());
        productCveStatement.setString(10, cveForm.getSummary());
        // Set other parameters
        productCveStatement.addBatch();
      } catch (SQLException | JsonProcessingException | IllegalArgumentException e) {
        handleError((Exception) e);
      }
    }

    public void getCveVendorInfo(JsonNode vulnerableProductArray,
        PreparedStatement vendorStatement) {
      for (JsonNode productNode : vulnerableProductArray) {
        String cpeValue = productNode.asText();
        vendorIndex++;
        try {
          String[] cpeValues = cpeValue.split(":");
          vendorStatement.setInt(1, vendorIndex);
          vendorStatement.setString(2, cpeValues[3]);
          vendorStatement.setInt(3, cveIndex);
          vendorStatement.addBatch();
          break;
        } catch (SQLException e) {
          error(e.getMessage());
        }
      }
    }

    public void getCveProductInfo(JsonNode vulnerableProductArray,
        PreparedStatement productStatement) {
      for (JsonNode productNode : vulnerableProductArray) {
        String cpeValue = productNode.asText();
        productIndex++;
        String[] cpeValues = cpeValue.split(":");
        try {
          productStatement.setInt(1, productIndex);
          productStatement.setString(2, cpeValues[4]);
          productStatement.setString(3, cpeValues[5]);
          productStatement.setInt(4, cveIndex);
          productStatement.addBatch();
          childBatchRowCounter++;
          if (childBatchRowCounter % childBatchSize == 0) {
            executeBatchStatements(productStatement);
            childBatchCounter++;
            info("### Executed Child Batch ###", childBatchCounter);
          }
        } catch (SQLException e) {
          error(e.getMessage());
        }
      }

    }

    private void executeBatchStatements(PreparedStatement... statements) throws SQLException {
      for (PreparedStatement statement : statements) {
        statement.executeBatch();
        statement.clearBatch();
      }
    }

    public void info(String message, int counters) {
      System.out.println(message + " : " + counters);
    }

    public void info(String message) {
      System.out.println(message);

    }

    public void error(String message) {
      System.err.println(message);
    }

    private void handleError(Exception e) {
      error(e.getMessage());
    }
  }
}

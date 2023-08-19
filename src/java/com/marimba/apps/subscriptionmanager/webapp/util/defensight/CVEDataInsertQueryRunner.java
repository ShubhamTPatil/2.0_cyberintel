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
 * @Date: 15/08/2023
 * <p>
 * Description : Reading the json data and inserting it into the Database in Batches.
 * @Since 1.0v
 * @Version 1.0.0
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
    int childBatchSize = 400000;
    int childBatchRowCounter = 0;
    int childBatchCounter = 0;


    public InsertCveDetailsInBulk(SubscriptionMain main, String jsonFilePath) {
      super(main);
      this.jsonFilePath = jsonFilePath;
      this.jsonFactory = new JsonFactory();
      this.objectMapper = new ObjectMapper();
    }

    /**
     * Executes the data insertion process using the provided statement pool and processes JSON data
     * in batches.
     *
     * @param pool The statement pool to retrieve database connections.
     */
    protected void execute(IStatementPool pool) {
      int batchSize = 4000;
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
              info(" **** Executed Parent Batch ****", batchCounter);
              rowCount = 0;
            }
          }

          //if the {batchSize} and {childBatchSize} is less than threshold value and rowCount > 0
          // then execute the all batch and insert remaining data into db.
          if (rowCount > 0) {
            batchCounter++;
            executeBatchStatements();
            info("**** Executed remaining all Batch ****", batchCounter);

          }

          finalizeExecution();
        }

        long endTime = System.currentTimeMillis();
        printExecutionSummary(startTime, endTime);

      } catch (Exception e) {
        error("Error Occurred in ()" + e.getMessage());
      }
    }

    /**
     * Before inserting the bulk data truncate the existing old records
     *
     * @param connection
     */
    /**
     * Truncates the existing data from specific tables in the database.
     *
     * @param connection The database connection to be used for truncating tables.
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
        error("Error Occurred in truncateExistingData()" + e.getMessage());
      }
    }


    /**
     * Creates and returns a JSON parser for the given input stream.
     *
     * @param fis The FileInputStream representing the JSON data to be parsed.
     * @return A JSON parser for the provided input stream, or null if an error occurs.
     */
    private JsonParser createJsonParser(FileInputStream fis) {
      try {
        return jsonFactory.createParser(fis);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      return null;
    }

    /**
     * Sets up the prepared statements used for processing CVE information.
     *
     * @param connection The database connection to be used for creating prepared statements.
     * @throws SQLException If a database access error occurs while setting up the statements.
     */
    private void setupStatements(Connection connection) throws SQLException {
      // Disable auto-commit to enable transaction management
      connection.setAutoCommit(false);

      // Initialize prepared statements for CVE information
      cveInfoStmt = getProductCveStatement(connection); // Prepared statement for CVE information
      cveVendorInfoStmt = getVendorStatement(
          connection); // Prepared statement for vendor information
      cveProductStmt = getProductStatement(
          connection); // Prepared statement for product information
    }


    /**
     * Processes a JSON record node, extracting CVE information and vulnerable product details.
     *
     * @param recordNode The JSON node representing a record containing CVE and vulnerability
     *                   details.
     * @throws SQLException If a database access error occurs while processing the record.
     */
    private void processRecordNode(JsonNode recordNode) throws SQLException {
      // Extract CVE information using the given cveInfoStmt PreparedStatement
      getCveInfo(recordNode, cveInfoStmt);

      // Check if the record contains "vulnerable_product" information
      if (recordNode.has("vulnerable_product")) {
        JsonNode vulnerableProductArray = recordNode.get("vulnerable_product");
        if (vulnerableProductArray.isArray()) {
          // Process the array of vulnerable products and get vendor and product objects
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

    /**
     * once the all data inserted into db clear the all open resource and clean up the memory.
     */
    private void finalizeExecution() {
      try {
        jsonParser.close();
        cveInfoStmt.clearBatch();
        cveInfoStmt.close();
        cveVendorInfoStmt.clearBatch();
        cveVendorInfoStmt.close();
        cveProductStmt.clearBatch();
        cveProductStmt.close();
        connection.commit();
        connection.close();
        info("Data inserted successfully!");
      } catch (Exception e) {
        handleError(e);
      }

    }

    /**
     * Prints a summary of the execution, including the end timestamp and the time taken for data
     * insertion.
     *
     * @param startTime The start timestamp of the execution.
     * @param endTime   The end timestamp of the execution.
     */
    private void printExecutionSummary(long startTime, long endTime) {
      info("End timestamp: " + endTime);
      info("All Data insertion time: " + (endTime - startTime) + " milliseconds");
    }

    /**
     * Creates and returns a prepared statement for inserting product information into the
     * database.
     *
     * @param connection The database connection to be used for creating the prepared statement.
     * @return A prepared statement for inserting product information.
     * @throws SQLException If a database access error occurs.
     */
    public PreparedStatement getProductStatement(Connection connection) throws SQLException {
      return connection.prepareStatement(
          "INSERT INTO cve_product_info (id, name, version, cve_id) VALUES (?, ?, ?, ?)");
    }

    /**
     * Creates and returns a prepared statement for inserting vendor information into the database.
     *
     * @param connection The database connection to be used for creating the prepared statement.
     * @return A prepared statement for inserting vendor information.
     * @throws SQLException If a database access error occurs.
     */
    public PreparedStatement getVendorStatement(Connection connection) throws SQLException {
      return connection.prepareStatement(
          "INSERT INTO cve_vendor_info (id, name, cve_id) VALUES (?, ?, ?)");
    }

    /**
     * Creates and returns a prepared statement for inserting CVE information into the database.
     *
     * @param connection The database connection to be used for creating the prepared statement.
     * @return A prepared statement for inserting CVE information.
     * @throws SQLException If a database access error occurs.
     */
    public PreparedStatement getProductCveStatement(Connection connection) throws SQLException {
      return connection.prepareStatement(
          "INSERT INTO cve_info (id, name, modified, published, severity, cvss, cvss_time, cwe, cve_id, summary) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
    }

    /**
     * Extracts CVE information from a JSON record node and populates a prepared statement with the
     * data.
     *
     * @param recordNode          The JSON node representing a record containing CVE information.
     * @param productCveStatement The prepared statement for inserting CVE information into the
     *                            database.
     */
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

        // Set CVSS value or a default value if not present
        productCveStatement.setString(6, cveForm.getCvss() != null ? cveForm.getCvss() : "-1");
        productCveStatement.setTimestamp(7, Timestamp.valueOf(cveForm.getModified()));
        productCveStatement.setString(8, cveForm.getCwe());
        productCveStatement.setString(9, cveForm.getCveId());
        productCveStatement.setString(10, cveForm.getSummary());

        // Add the batch for execution
        productCveStatement.addBatch();
      } catch (SQLException | JsonProcessingException | IllegalArgumentException e) {
        handleError((Exception) e);
      }
    }


    /**
     * Extracts vendor information from an array of vulnerable product nodes and populates a
     * prepared statement with the data.
     *
     * @param vulnerableProductArray The JSON array containing vulnerable product information.
     * @param vendorStatement        The prepared statement for inserting vendor information into
     *                               the database.
     */
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


    /**
     * This method will read the json data for product and also once the batch size reach to
     * threshold value it will commit the transaction and clean up the memory.
     *
     * @param vulnerableProductArray
     * @param productStatement
     */
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
          /**
           * There will the large data for the product if data added into batch == {childBatchSize} then push that
           * data along wth CVE and vendor data and release to resource to free up the memory
           */
          if (childBatchRowCounter % childBatchSize == 0) {
            executeBatchStatements(productStatement, cveInfoStmt, cveVendorInfoStmt);
            childBatchCounter++;
            info("### Executed Child Batch ###", childBatchCounter);
          }
        } catch (SQLException e) {
          error(e.getMessage());
        }
      }

    }

    /**
     * execute the batches and commit the transaction s
     *
     * @param statements
     * @throws SQLException
     */
    private void executeBatchStatements(PreparedStatement... statements) throws SQLException {
      for (PreparedStatement statement : statements) {
        statement.executeBatch();
        connection.commit();
        statement.clearBatch();
      }
    }

    public void info(String message, int counters) {
      System.out.println("INFO : InsertCveDetailsInBulk : " + message + " : " + counters);
    }

    public void info(String message) {
      System.out.println("INFO : InsertCveDetailsInBulk : " + message);

    }

    public void error(String message) {
      System.err.println("ERROR : InsertCveDetailsInBulk : " + message);
    }

    private void handleError(Exception e) {
      error(e.getMessage());
    }
  }
}

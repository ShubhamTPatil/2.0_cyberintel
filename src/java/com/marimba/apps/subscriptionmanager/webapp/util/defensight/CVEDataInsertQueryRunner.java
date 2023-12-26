package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.db.IStatementPool;
import com.marimba.apps.subscriptionmanager.webapp.bean.CVEFormBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.RootCVEBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.VulnerabilitiesBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVEBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVSSMetricsV2Bean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVSSDataBean;
import com.marimba.apps.subscriptionmanager.webapp.api.RestClient;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CVSSMetricsV3Bean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.ConfigurationBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.NodeBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.CPEMatchBean;
import com.marimba.apps.subscriptionmanager.webapp.bean.cve.DescriptionBean;
import com.marimba.intf.util.IConfig;
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.ILogger;
import com.marimba.tools.config.ConfigProps;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


/**
 * @Author Yogesh Pawar
 * @Date: 15/08/2023
 * <p>
 * Description : Reading the json data and inserting it into the Database in Batches.
 * @Since 1.0v
 * @Version 1.0.0
 */
public class CVEDataInsertQueryRunner extends DatabaseAccess {


  public CVEDataInsertQueryRunner(SubscriptionMain main, String jsonFilePath, ConfigProps definitionConfig,
      LinkedHashMap<String, String> errorMap) {
    try {
      runQuery(new InsertCveDetailsInBulk(main, jsonFilePath, definitionConfig, errorMap));
    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  class InsertCveDetailsInBulk extends com.marimba.apps.securitymgr.db.QueryExecutor implements
      ILogger {

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

    private String url;

    private String apiKey;

    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    LinkedHashMap<String, String> error;

    List<String> failedEndpoints = new ArrayList<>();

    boolean isUpdate = Boolean.FALSE;

    ConfigProps definitionConfig;


    public InsertCveDetailsInBulk(SubscriptionMain main, String jsonFilePath, ConfigProps definitionConfig,
        LinkedHashMap<String, String> error) {
      super(main);
      this.jsonFilePath = jsonFilePath;
      this.jsonFactory = new JsonFactory();
      this.objectMapper = new ObjectMapper();
      this.definitionConfig = definitionConfig;
      this.error = error;
      this.url = definitionConfig.getProperty("defensight.cve.api.url");
      this.apiKey = definitionConfig.getProperty("defensight.cve.api.key");
    }


    /**
     * On the basis of total records prepare the pagination endpoints to fetch the remaining
     * records.
     *
     * @param url
     * @param totalRecords
     * @param startIndex
     * @return
     */
    private List<String> getApiEndpoint(String url, int totalRecords, int startIndex) {
      List<String> apiEndpoints = new ArrayList<>();
      int resultPerPage = 2000;

      int totalPages = (int) Math.ceil((double) totalRecords / resultPerPage);
      for (int page = 0; page < totalPages; page++) {
        String modifyUrl;
        int currentIndex = startIndex + (page) * resultPerPage;
        if (startIndex != 0) {
          modifyUrl = url + "?resultsPerPage=" + resultPerPage + "&startIndex=" + currentIndex;
        } else {
          if (isUpdate) {
            modifyUrl = url + "&resultsPerPage=" + resultPerPage + "&startIndex=" + currentIndex;
          } else {
            modifyUrl = url + "?resultsPerPage=" + resultPerPage + "&startIndex=" + currentIndex;

          }
        }
        apiEndpoints.add(modifyUrl);
      }
      return apiEndpoints;
    }

    /**
     * Executes the data insertion process using the provided statement pool and processes JSON data
     * in batches.
     *
     * @param pool The statement pool to retrieve database connections.
     */
    protected void execute(IStatementPool pool) {

      error = new LinkedHashMap<>(); //Instanciating error message
      long startTime = System.currentTimeMillis();

      try {
        List<String> allEndpoints = null;
        RootCVEBean rootCVEBean = callApi();
        connection = pool.getConnection();

        int totalResults = rootCVEBean.getTotalResults();

        HashMap<Integer, String> lastInsertIndex = getLastInsertedIndex(connection);
        Map.Entry<Integer, String> entry = lastInsertIndex.entrySet().iterator().next();
        int lastInsertedIndex = entry.getKey();
        String lastModifyDate = entry.getValue();

        if (lastInsertedIndex == 0 && lastModifyDate == null) {
          setupStatements(connection);
          setCveInformation(rootCVEBean, cveInfoStmt, cveProductStmt);
          executeBatchStatements(cveInfoStmt, cveProductStmt); //insert first 2K records.
          allEndpoints = getApiEndpoint(url, totalResults, 2000);
          asyncApiCall(8, allEndpoints);
        } else {
          if (lastInsertedIndex != 0 && lastInsertedIndex < totalResults
              || lastInsertedIndex <= totalResults) {
            if (lastInsertedIndex != totalResults) {
              int count = totalResults - lastInsertedIndex;
              setupStatements(connection);
              allEndpoints = getApiEndpoint(url, count, lastInsertedIndex);
              asyncApiCall(8,
                  allEndpoints); //Insert the all new published and remaining records from where its fails in last operation.
            }
            updateCveInformation(url,
                lastModifyDate); //check the modified records since last insertion and into DB.
          }
        }

        //if api gives 403 error or fail to send response.
        if (failedEndpoints != null && !failedEndpoints.isEmpty() && failedEndpoints.size() > 0) {
          asyncApiCall(8, failedEndpoints);
        }
      } catch (Exception e) {
        error("Error Occurred in ()" + e.getMessage());
      }
    }


    /**
     * This method will update the existing cve information.
     *
     * @param url
     * @param lstModifyDate
     */
    private void updateCveInformation(String url, String lstModifyDate) {
      try {
        isUpdate = Boolean.TRUE;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        LocalDateTime currentDateTime = LocalDateTime.now();
        String currentDate = currentDateTime.format(formatter);
        LocalDateTime lastUpdatedDate = LocalDateTime.parse(lstModifyDate, outputFormatter);
        String lastModifyDateTime = lastUpdatedDate.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));

        JsonNode response = RestClient.build(
                url + "?lastModStartDate=" + lastModifyDateTime + "&lastModEndDate=" + currentDate)
            .executeGetRequest();

        RootCVEBean cveBean = parseJsonToBean(response);

        if (cveBean.getTotalResults() < 2000) {
          setupUpdateStatement(connection);
          setUpdateCveInformation(cveBean, cveInfoStmt);
          executeBatchStatements(cveInfoStmt);
        } else {
          //update the first page = first 2000 records.
          setupUpdateStatement(connection);
          setUpdateCveInformation(cveBean, cveInfoStmt);
          executeBatchStatements(cveInfoStmt);

          url = url + "?lastModStartDate=" + lastModifyDateTime + "&lastModEndDate=" + currentDate;
          List<String> updateEndpoint = getApiEndpoint(url, cveBean.getTotalResults(), 2000);
          asyncApiCall(8, updateEndpoint);
        }
      } catch (Exception exception) {
        handleError(exception);
      }
    }

    /**
     * Check the last inserted index and date.
     *
     * @param connection
     * @return
     */
    private HashMap<Integer, String> getLastInsertedIndex(Connection connection) {
      try {
        String fetchIndexQuery = "select MAX(id) as lastInsertId, MAX(last_update) as lastUpdateDate  from cve_info;";
        PreparedStatement prepareStatement = connection.prepareStatement(fetchIndexQuery);
        ResultSet resultSet = prepareStatement.executeQuery();
        HashMap<Integer, String> lstInsertedIndexAndDate = new HashMap<>();
        while (resultSet.next()) {
          String lstDate = resultSet.getString(2);
          if (lstDate != null) {
            StringBuffer date = new StringBuffer(lstDate);
            int length = 23 - date.length();
            for (int i = 0; i < length; i++) {
              date.append("0");
            }
            lstInsertedIndexAndDate.put(resultSet.getInt(1), date.toString());
            return lstInsertedIndexAndDate;
          }
        }
        if (lstInsertedIndexAndDate.isEmpty()) {
          lstInsertedIndexAndDate.put(0, null);
          return lstInsertedIndexAndDate;
        }

      } catch (SQLException e) {
        handleError(e);
      }
      return null;
    }

    /**
     * It is accepting the batch size and list of url (total pages to be fetched.) and processing
     * the multiple  request parallel
     *
     * @param apiBatch
     * @param allEndpoints
     */
    private void asyncApiCall(int apiBatch, List<String> allEndpoints) {
      try {
        ExecutorService executorService = Executors.newFixedThreadPool(apiBatch);
        List<CompletableFuture<JsonNode>> futures = new ArrayList<>();
        for (int i = 0; i < allEndpoints.size(); i += apiBatch) {
          List<String> batch = allEndpoints.subList(i, Math.min(i + apiBatch, allEndpoints.size()));
          Thread.sleep(6000);
          for (String endPoint : batch) {
            Thread.sleep(6000);
            futures.add(CompletableFuture.supplyAsync(() -> (JsonNode) callApi(endPoint, apiKey),
                executorService));
          }

          CompletableFuture<Void> batchAllOf = CompletableFuture.allOf(
              futures.toArray(new CompletableFuture[0]));
          batchAllOf.get();

          for (CompletableFuture<JsonNode> future : futures) {
            JsonNode result = future.get();
            ObjectMapper objectMapper = new ObjectMapper();
            if (result != null) {
              RootCVEBean cveBean = objectMapper.treeToValue(result, RootCVEBean.class);
              if (cveBean != null && !cveBean.getVulnerabilities().isEmpty()) {
                if (!isUpdate) { //if it there is no data inserted in db then do the insertion operation
                  setCveInformation(cveBean, cveInfoStmt, cveProductStmt);
                  executeBatchStatements(cveInfoStmt, cveProductStmt);
                } else {
                  setUpdateCveInformation(cveBean, cveInfoStmt);
                  executeBatchStatements(cveInfoStmt);
                }
              }
            }
          }
          futures.clear();
        }
        executorService.shutdown();
      } catch (Exception exception) {
        handleError(exception);
      }
    }

    /**
     * Construct the prepared statement by reading the @{RootCVEBean} and update the records
     *
     * @param rootCVEBean
     * @param preparedStmt
     */
    private void setUpdateCveInformation(RootCVEBean rootCVEBean, PreparedStatement preparedStmt) {
      error = new LinkedHashMap<>();
      try {
        for (VulnerabilitiesBean vulnerabilitiesBean : rootCVEBean.getVulnerabilities()) {
          CVEBean cveBean = vulnerabilitiesBean.getCveBean();
          LocalDateTime dateTime = LocalDateTime.parse(cveBean.getLastModifyDate(),
              DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          dateTime.format(outputFormatter);
          preparedStmt.setTimestamp(1, Timestamp.valueOf(dateTime.format(outputFormatter)));

          LocalDateTime publishDate = LocalDateTime.parse(cveBean.getPublishDate(),
              DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          dateTime.format(outputFormatter);
          preparedStmt.setTimestamp(2, Timestamp.valueOf(publishDate));

          if (cveBean.getMetrics().getCvssMetricV2() != null) {
            for (CVSSMetricsV2Bean cvssMetricsV2Bean : cveBean.getMetrics().getCvssMetricV2()) {
              if (!cvssMetricsV2Bean.getSeverity().isEmpty()) {
                preparedStmt.setString(3, cvssMetricsV2Bean.getSeverity());
              } else {
                preparedStmt.setString(3, null);
              }
              String source = cvssMetricsV2Bean.getV2Source();
              if (!source.isEmpty() && source.equalsIgnoreCase("nvd@nist.gov")) {
                CVSSDataBean cvssDataBean = cvssMetricsV2Bean.getCvssData();
                if (!cvssDataBean.getBaseScore().isEmpty()) {
                  preparedStmt.setString(4, cvssDataBean.getBaseScore());
                } else {
                  preparedStmt.setString(4, null);
                }
              } else {
                preparedStmt.setString(4, null);
              }
              preparedStmt.setString(5, null);
            }
          } else {
            preparedStmt.setString(3, null);
            preparedStmt.setString(4, "-1");
            preparedStmt.setString(5, "-1");
          }
          if (cveBean.getMetrics().getCvssMetricV3() != null) {
            for (CVSSMetricsV3Bean cvssMetricsV3Bean : cveBean.getMetrics().getCvssMetricV3()) {
              String source = cvssMetricsV3Bean.getV3Source();
              if (!source.isEmpty() && source.equalsIgnoreCase("nvd@nist.gov")) {
                CVSSDataBean cvssDataBean = cvssMetricsV3Bean.getCvssData();
                preparedStmt.setString(5, cvssDataBean.getBaseScore());
                preparedStmt.setString(3,
                    cvssDataBean.getBaseV3Severity()); //if v3 cvss is present then override the v2 cvss severity
              } else {
                preparedStmt.setString(5, "-1");

              }
            }
          }
          preparedStmt.setTimestamp(6, Timestamp.valueOf(publishDate));

          if (cveBean.getDescription() != null) {
            for (DescriptionBean descriptionBean : cveBean.getDescription()) {
              if (descriptionBean.getLanguage().equalsIgnoreCase("en")) {
                preparedStmt.setString(7, descriptionBean.getValue());
              }
            }
          } else {
            preparedStmt.setString(7, null);
          }
          LocalDateTime currentDateTime = LocalDateTime.now();
          preparedStmt.setTimestamp(8, Timestamp.valueOf(currentDateTime.format(outputFormatter)));
          preparedStmt.setString(9, cveBean.getId()); //where cve_id = cve_id
          preparedStmt.addBatch(); //batch size will be the same  as result per page size
        }
      } catch (Exception exception) {
        error(exception.getMessage());
        error.put("ERROR", exception.getMessage());
      }
    }

    /**
     * Construct the cve_product and cve_info table statement to insert the data into DB
     *
     * @param rootCVEBean
     * @param prepearedStmt
     */
    private void setCveInformation(RootCVEBean rootCVEBean, PreparedStatement... prepearedStmt) {
      error = new LinkedHashMap<>();
      try {
        for (VulnerabilitiesBean vulnerabilitiesBean : rootCVEBean.getVulnerabilities()) {
          CVEBean cveBean = vulnerabilitiesBean.getCveBean();
          prepearedStmt[0].setString(1, cveBean.getId());
          LocalDateTime dateTime = LocalDateTime.parse(cveBean.getLastModifyDate(),
              DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          dateTime.format(outputFormatter);
          prepearedStmt[0].setTimestamp(2, Timestamp.valueOf(dateTime.format(outputFormatter)));

          LocalDateTime publishDate = LocalDateTime.parse(cveBean.getPublishDate(),
              DateTimeFormatter.ISO_LOCAL_DATE_TIME);
          dateTime.format(outputFormatter);
          prepearedStmt[0].setTimestamp(3, Timestamp.valueOf(publishDate));

          if (cveBean.getMetrics().getCvssMetricV2() != null) {
            for (CVSSMetricsV2Bean cvssMetricsV2Bean : cveBean.getMetrics().getCvssMetricV2()) {
              if (!cvssMetricsV2Bean.getSeverity().isEmpty()) {
                prepearedStmt[0].setString(4, cvssMetricsV2Bean.getSeverity());
              } else {
                prepearedStmt[0].setString(4, null);
              }
              String source = cvssMetricsV2Bean.getV2Source();
              if (!source.isEmpty() && source.equalsIgnoreCase("nvd@nist.gov")) {
                CVSSDataBean cvssDataBean = cvssMetricsV2Bean.getCvssData();
                if (!cvssDataBean.getBaseScore().isEmpty()) {
                  prepearedStmt[0].setString(5, cvssDataBean.getBaseScore());
                } else {
                  prepearedStmt[0].setString(5, null);
                }
              } else {
                prepearedStmt[0].setString(5, null);
              }
              prepearedStmt[0].setString(6, null);
            }
          } else {
            prepearedStmt[0].setString(4, null);
            prepearedStmt[0].setString(5, null);
            prepearedStmt[0].setString(6, null);
          }
          prepearedStmt[0].setTimestamp(7, Timestamp.valueOf(publishDate));

          prepearedStmt[0].setString(8,
              null); //setting null as api dose not provides the cwe value.
          prepearedStmt[0].setString(9, cveBean.getId());

          if (cveBean.getDescription() != null) {
            for (DescriptionBean descriptionBean : cveBean.getDescription()) {
              if (descriptionBean.getLanguage().equalsIgnoreCase("en")) {
                prepearedStmt[0].setString(10, descriptionBean.getValue());
              }
            }
          } else {
            prepearedStmt[0].setString(10, null);

          }
          LocalDateTime currentDateTime = LocalDateTime.now();
          prepearedStmt[0].setTimestamp(11,
              Timestamp.valueOf(currentDateTime.format(outputFormatter)));

          if (cveBean.getMetrics().getCvssMetricV3() != null) {
            for (CVSSMetricsV3Bean cvssMetricsV3Bean : cveBean.getMetrics().getCvssMetricV3()) {
              String source = cvssMetricsV3Bean.getV3Source();
              if (!source.isEmpty() && source.equalsIgnoreCase("nvd@nist.gov")) {
                CVSSDataBean cvssDataBean = cvssMetricsV3Bean.getCvssData();
                prepearedStmt[0].setString(6, cvssDataBean.getBaseScore());
                prepearedStmt[0].setString(4,
                    cvssDataBean.getBaseV3Severity()); //if v3 cvss is present then override the v2 cvss severity
              }
            }
          }

          //Insert product Statement
          if (cveBean.getConfiguration() != null) {
            for (ConfigurationBean configurationBean : cveBean.getConfiguration()) {
              if (configurationBean.getNodes() != null) {
                for (NodeBean nodeBean : configurationBean.getNodes()) {
                  if (nodeBean.getCpeMatch() != null) {
                    for (CPEMatchBean cpeMatchBean : nodeBean.getCpeMatch()) {
                      prepearedStmt[1].setString(1, cpeMatchBean.getCepCriteria());
                      prepearedStmt[1].setString(2, cpeMatchBean.getMatchCriteriaId());
                      prepearedStmt[1].setString(3, cveBean.getId());
                      prepearedStmt[1].addBatch();
                    }
                  }
                }
              }
            }
          }
          cveIndex++;
          prepearedStmt[0].addBatch();
        }
      } catch (Exception exception) {
        error(exception.getMessage());
        error.put("ERROR", exception.getMessage());
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
        handleError(ioe);
        error.put("ERROR", ioe.getMessage());

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
      cveProductStmt = getProductStatement(
          connection); // Prepared statement for product information
    }

    private void setupUpdateStatement(Connection connection) throws SQLException {
      connection.setAutoCommit(false);
      cveInfoStmt = getCVEUpdateStatement(connection);
    }

    private PreparedStatement getCVEUpdateStatement(Connection connection) throws SQLException {
      return connection.prepareStatement(
          "UPDATE cve_info SET modified=?, published=?, severity=?, v2_cvss=?, v3_cvss=?, cvss_time=?, summary=?,last_update=? WHERE cve_id=?");
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


    private void getVendorAndProductObjects(JsonNode vulnerableProductArray) throws SQLException {
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
          "INSERT INTO cve_product_info (criteria,  match_criteria_id, cve_id) VALUES (?, ?, ?)");
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
          "INSERT INTO cve_info (name, modified, published, severity, v2_cvss,v3_cvss, cvss_time, cwe, cve_id, summary,last_update ) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
        error.put("ERROR", e.getMessage());

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
          error.put("ERROR", e.getMessage());

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
        PreparedStatement productStatement) throws SQLException {
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
          handleError(e);
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

    @Override
    public void info(String message, int counters) {
      System.out.println("INFO : InsertCveDetailsInBulk : " + message + " : " + counters);
    }

    @Override
    public void info(String message) {
      System.out.println("INFO : InsertCveDetailsInBulk : " + message);

    }

    @Override
    public void error(String message) {
      System.err.println("ERROR : InsertCveDetailsInBulk : " + message);
    }

    @Override
    public void handleError(Exception e) {
      error(e.getMessage());
    }

    public int getBatchCounter() {
      return batchCounter;
    }


    private JsonNode callApi(String endpoint, String apiKey) {
      JsonNode response = null;
      try {
        response = RestClient.build(endpoint).addHeader("Authorization", "Bearer " + apiKey)
            .executeGetRequest();
        if (response == null) {
          failedEndpoints.add(endpoint);
        }
      } catch (URISyntaxException e) {
        handleError(e);
      }
      return response;
    }

    /**
     * fetch the first page from nvd.
     */
    public RootCVEBean callApi() {
      try {
        JsonNode jsonResponse = RestClient.build(url).addHeader("Authorization", "Bearer " + apiKey)
            .executeGetRequest();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.treeToValue(jsonResponse, RootCVEBean.class);
      } catch (Exception e) {
        handleError(e);
      }
      return null;
    }

    private RootCVEBean parseJsonToBean(JsonNode response) {
      try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.treeToValue(response, RootCVEBean.class);
      } catch (Exception exception) {
        handleError(exception);
      }

      return null;
    }
  }

}

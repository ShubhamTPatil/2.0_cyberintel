package com.marimba.apps.subscriptionmanager.webapp.util.defensight.anomalyReport;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.MachineLevelAnomaly;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.MachineNameList;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import com.marimba.intf.util.IConfig;

import static com.marimba.apps.subscriptionmanager.webapp.util.defensight.anomalyReport.AnomalyUtil.*;

public class MachineLevelAnomalyUtil {

  public CosmosPagedIterable<MachineLevelAnomaly> populateMachineLevelAnomaly(
      IConfig tunerConfig, String hostname, OffsetDateTime currentTime, OffsetDateTime prevTime) {

    getCosmosConnection(tunerConfig);
    CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();

    if (hostname == null) {
      return null;
    }

    debugInfo(
        "populateMachineLevelAnomaly query: \n SELECT c.winlog.event_id as event_id, c.predictions as anomaly, c.event.created as time FROM c  WHERE c.event.created <= '"
            + currentTime.toString() + "' AND c.event.created >= '" + prevTime.toString()
            + "' AND c.host.hostname = '" + hostname
            + "' AND c.predictions = 'true' AND c.event.created != null AND c.winlog.event_id != null");

    options.setPartitionKey(new PartitionKey(AnomalyConstants.PARTITION_KEY));
    ArrayList<SqlParameter> paramList = new ArrayList<SqlParameter>();

    paramList.add(new SqlParameter("@ctime", currentTime.toString()));
    paramList.add(new SqlParameter("@ptime", prevTime.toString()));
    paramList.add(new SqlParameter("@hostname", hostname));

    return executeQueryForMachineLevelAnomaly(
        new SqlQuerySpec(AnomalyConstants.Queries.MACHINE_LEVEL_ANOMALY, paramList));
  }

  /**
   * Fetch MachineNameList
   *
   * @param tunerConfig tunerConfig for properties stored in prefs.txt
   * @param prevTime    Previous Time
   * @return CosmosPagedIterable<MachineNameList>
   */
  public CosmosPagedIterable<MachineNameList> fetchMachineNameList(IConfig tunerConfig,
      OffsetDateTime prevTime) {

    ArrayList<SqlParameter> paramList = new ArrayList<SqlParameter>();
    paramList.add(new SqlParameter("@ptime", prevTime.toString()));

    debugInfo(
        "fetchMachineNameList query: \n select distinct c.host.hostname as hostname from c where c.host.hostname != null AND c.event.created >= '"
            + prevTime.toString() + "'");

    getCosmosConnection(tunerConfig);
    return executeQueryForMachineNameList(
        new SqlQuerySpec(AnomalyConstants.Queries.MACHINE_NAME_LIST, paramList));
  }

  /**
   * Execute Query For MachineLevelAnomaly
   *
   * @param querySpec Query and SQLParams: @ctime, @ptime, @hostname
   * @return CosmosPagedIterable<MachineLevelAnomaly>
   */
  private CosmosPagedIterable<MachineLevelAnomaly> executeQueryForMachineLevelAnomaly(
      SqlQuerySpec querySpec) {
    return container.queryItems(
        querySpec, new CosmosQueryRequestOptions(), MachineLevelAnomaly.class);
  }

  /**
   * Execute Query For MachineNameList
   *
   * @param querySpec Query and SQLParam: @ptime
   * @return CosmosPagedIterable<MachineNameList>
   */
  private CosmosPagedIterable<MachineNameList> executeQueryForMachineNameList(
      SqlQuerySpec querySpec) {
    return container.queryItems(querySpec, new CosmosQueryRequestOptions(), MachineNameList.class);
  }
}

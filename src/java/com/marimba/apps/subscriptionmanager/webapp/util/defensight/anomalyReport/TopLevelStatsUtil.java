package com.marimba.apps.subscriptionmanager.webapp.util.defensight.anomalyReport;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.MachineNameList;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.TopLevelStatsChartData;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.marimba.intf.util.IConfig;

import static com.marimba.apps.subscriptionmanager.webapp.util.defensight.anomalyReport.AnomalyUtil.*;

public class TopLevelStatsUtil {

  /**
   * Populate the data for Top Level Statistics chart
   *
   * @param tunerConfig
   * @param currentTime
   * @param prevTime
   * @return
   */
  public List<TopLevelStatsChartData> populateTopLevelStatsData(IConfig tunerConfig,
      CosmosPagedIterable<MachineNameList> machineList, OffsetDateTime currentTime,
      OffsetDateTime prevTime) {

    debugInfo("populateTopLevelStatsData:: AnomalyUtil.populateTopLevelStatsData getting called.");

    if(!getCosmosConnection(tunerConfig)) return null;

    HashMap<OffsetDateTime, OffsetDateTime> intervals = createEqualSixtyTimeIntervals(prevTime,
        currentTime);

    List<CompletableFuture<List<TopLevelStatsChartData>>> futures = new ArrayList<>();

    machineList.stream().forEach(machineName -> {
      CompletableFuture<List<TopLevelStatsChartData>> future = CompletableFuture.supplyAsync(() -> {
        String hostname = machineName.getHostname();

        debugInfo("populateTopLevelStatsData " + hostname
            + ":: SELECT c.host.hostname as hostname, c.predictions as anomaly, c.event.created as time FROM c  WHERE c.event.created <= '"
            + currentTime.toString() + "' AND c.event.created >= '" + prevTime.toString()
            + "' AND c.host.hostname = '" + hostname
            + "' AND (c.predictions = 'true' OR c.predictions = 'Not_Trained') AND c.event.created != null");

        ArrayList<SqlParameter> paramList = new ArrayList<>();

        paramList.add(new SqlParameter("@ctime", currentTime.toString()));
        paramList.add(new SqlParameter("@ptime", prevTime.toString()));
        paramList.add(new SqlParameter("@hostname", hostname));

        CosmosPagedIterable<TopLevelStatsChartData> result = executeQueryForTopLevelStats(
            new SqlQuerySpec(AnomalyConstants.Queries.TOP_LEVEL_STATS_FOR_HOST, paramList));

        debugInfo("populateTopLevelStatsData " + hostname + ":: Query executed");
        debugInfo("START hostname =" + hostname);

        List<TopLevelStatsChartData> dataPoints = topLevelStatsDataPoints(result, hostname,
            intervals);

        debugInfo("END hostname =" + hostname);

        return dataPoints;
      });
      futures.add(future);
    });

    debugInfo("populateTopLevelStatsData futures created for all machines.");

    CompletableFuture<Void> allOf = CompletableFuture.allOf(
        futures.toArray(new CompletableFuture[0]));

    // Create a List of results
    List<TopLevelStatsChartData> results = new ArrayList<>();

    // Use thenApply to collect the results into the List
    allOf.thenApply(v -> {
      for (CompletableFuture<List<TopLevelStatsChartData>> future : futures) {
        try {
          results.addAll(future.get());
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }
      }
      return results;
    }).join();

    return results;
  }


  /**
   * Generates 60 data points to plot the graph for Top Level Stats
   *
   * @param result   List of TopLevelStatsChartData
   * @param hostname hostname
   * @return Result List of TopLevelStatsChartData
   */
  private List<TopLevelStatsChartData> topLevelStatsDataPoints(
      CosmosPagedIterable<TopLevelStatsChartData> result,
      String hostname, HashMap<OffsetDateTime, OffsetDateTime> intervals) {

    return intervals.entrySet()
        .stream()
        .flatMap(entry -> {
          OffsetDateTime startTime = entry.getKey();
          OffsetDateTime endTime = entry.getValue();

          List<TopLevelStatsChartData> filteredData = result.stream()
              .filter(data -> {
                OffsetDateTime dataTime = OffsetDateTime.parse(data.getTime());
                return !dataTime.isBefore(startTime) && !dataTime.isAfter(endTime);
              })
              .collect(Collectors.toList());

          if (filteredData.isEmpty()) {
            return Stream.of(new TopLevelStatsChartData(hostname, endTime.toString(), "false"));
          }

          boolean hasTrue = filteredData.stream()
              .anyMatch(data -> "true".equals(data.getAnomaly()));
          boolean hasNotTrained = filteredData.stream()
              .anyMatch(data -> "Not_Trained".equals(data.getAnomaly()));

          filteredData.clear();

          if (hasTrue) {
            return Stream.of(new TopLevelStatsChartData(hostname, endTime.toString(), "true"));
          } else if (hasNotTrained) {
            return Stream.of(
                new TopLevelStatsChartData(hostname, endTime.toString(), "Not_Trained"));
          } else {
            return Stream.of(new TopLevelStatsChartData(hostname, endTime.toString(), "false"));
          }
        })
        .collect(Collectors.toList());

  }


  /**
   * Generates equal intervals between startTime and endTime. startTime < endTime
   *
   * @param startTime latest time
   * @param endTime   last time
   * @return map of intervals
   */
  public static HashMap<OffsetDateTime, OffsetDateTime> createEqualSixtyTimeIntervals(
      OffsetDateTime startTime, OffsetDateTime endTime) {
    // Check if the endTime is before the startTime
    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("endTime cannot be before startTime");
    }

    HashMap<OffsetDateTime, OffsetDateTime> timeIntervals = new HashMap<>();
    OffsetDateTime currentStart = startTime;
    OffsetDateTime currentEnd;

    // Calculate the total duration between startTime and endTime
    Duration totalDuration = Duration.between(startTime, endTime);

    // Calculate the interval size based on the total duration
    long intervalSeconds = totalDuration.getSeconds() / 60; // 60 seconds per minute

    while (currentStart.isBefore(endTime)) {
      currentEnd = currentStart.plusSeconds(intervalSeconds);

      // Ensure the currentEnd doesn't go beyond the endTime
      if (currentEnd.isAfter(endTime)) {
        currentEnd = endTime;
      }

      timeIntervals.put(currentStart, currentEnd);
      currentStart = currentEnd;
    }

    return timeIntervals;
  }


  /**
   * Execute query for Top Level Statistics Chart
   *
   * @param querySpec Query with Parameters
   * @return List of TopLevelStatsChartData
   */
  private CosmosPagedIterable<TopLevelStatsChartData> executeQueryForTopLevelStats(
      SqlQuerySpec querySpec) {
    return container.queryItems(
        querySpec, new CosmosQueryRequestOptions(), TopLevelStatsChartData.class);
  }


}

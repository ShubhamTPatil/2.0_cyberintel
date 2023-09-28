package com.marimba.apps.subscriptionmanager.webapp.bean.anomaly;

public class TopLevelStatsChartData {
    private String hostname;
    private String time;
    private String anomaly;

    public TopLevelStatsChartData() {

    }

    /**
     * Constructor
     * @param hostname
     * @param time
     * @param anomaly
     */
    public TopLevelStatsChartData(String hostname, String time, String anomaly) {
        this.hostname = hostname;
        this.time = time;
        this.anomaly = anomaly;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAnomaly() {
        return anomaly;
    }

    public void setAnomaly(String anomaly) {
        this.anomaly = anomaly;
    }
}

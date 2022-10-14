package com.marimba.apps.securitymgr.view;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: May 12, 2017
 * Time: 12:32:04 PM
 * To change this template use File | Settings | File Templates.
 */

public class QueryBean {

    private String query;
    private String queryName;
    private String displayPath;
    private String queryTarget;
    private String queryUserName;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public void setDisplayPath(String displayPath) {
        this.displayPath = displayPath;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryTarget() {
        return queryTarget;
    }

    public void setQueryTarget(String queryTarget) {
        this.queryTarget = queryTarget;
    }

    public String getQueryUserName() {
        return queryUserName;
    }

    public void setQueryUserName(String queryUserName) {
        this.queryUserName = queryUserName;
    }
}

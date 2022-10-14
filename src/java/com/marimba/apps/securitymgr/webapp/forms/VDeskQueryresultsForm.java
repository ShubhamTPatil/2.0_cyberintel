// Copyright 2019-2021, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.webapp.forms;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: May 5, 2017
 * Time: 3:16:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class VDeskQueryresultsForm extends ActionForm {

	private String action;
    private String queryRanAt;
    private String queryTarget;
    private String queryRanTime;
    private List<String> columnsList;
    private String displayPath;
    private String sql;
    private List<List<String>> valuesList;
    private boolean addViewDetails = false;
    private boolean isVA = false;
    private String reportStatus;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDisplayPath() {
        return displayPath;
    }

    public void setDisplayPath(String displayPath) {
        this.displayPath = displayPath;
    }

    public String getQueryRanAt() {
        return queryRanAt;
    }

    public void setQueryRanAt(String queryRanAt) {
        this.queryRanAt = queryRanAt;
    }

    public String getQueryTarget() {
        return queryTarget;
    }

    public void setQueryTarget(String queryTarget) {
        this.queryTarget = queryTarget;
    }

    public String getQueryRanTime() {
        return queryRanTime;
    }

    public void setQueryRanTime(String queryRanTime) {
        this.queryRanTime = queryRanTime;
    }

    public List<String> getColumnsList() {
        return columnsList;
    }

    public void setColumnsList(List<String> columnsList) {
        this.columnsList = columnsList;
    }

    public List<List<String>> getValuesList() {
        return valuesList;
    }

    public void setValuesList(List<List<String>> valuesList) {
        this.valuesList = valuesList;
    }

    public void setAddViewDetails(boolean addViewDetails) {
        this.addViewDetails = addViewDetails;
    }

    public boolean getAddViewDetails() {
        return addViewDetails;
    }

    public boolean getIsVA() {
        return isVA;
    }

    public void setIsVA(boolean _isVA) {
        isVA = _isVA;
    }
    public String getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}

    public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {

    }
}

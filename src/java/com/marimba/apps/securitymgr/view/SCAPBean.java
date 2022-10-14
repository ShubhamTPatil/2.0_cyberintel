package com.marimba.apps.securitymgr.view;

import com.marimba.apps.subscriptionmanager.webapp.util.Utils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.LinkedHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Mar 28, 2017
 * Time: 4:19:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SCAPBean {

    private String id;
    private String type;
    private String title;
    private String fileName;
    private String templateName = "";
    private String assessmentType;
    private String selected = "false";
    private String selectedProfile = "";
    private String selectedProfileTitle = "";
    private Map<String, String> profiels;
    private LinkedHashMap<String, String> sortedProfiles;

    private String complianceLevel = "";
    private int compliantCount;
    private int nonCompliantCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }

    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public String getSelectedProfileTitle() {
        return selectedProfileTitle;
    }

    public void setSelectedProfileTitle(String selectedProfileTitle) {
        this.selectedProfileTitle = selectedProfileTitle;
    }

    public Map<String, String> getProfiels() {
        return profiels;
    }

    public void setProfiels(Map<String, String> profiels) {
        this.profiels = profiels;
    }

    public LinkedHashMap<String, String> getSortedProfiles() {
        if (profiels != null) {
            sortedProfiles= Utils.sortByMapValue(profiels);
        } else {
            sortedProfiles= new LinkedHashMap<String, String>();
        }
        return sortedProfiles;
    }

    public void setSortedProfiles(LinkedHashMap<String, String> sortedProfiles) {
        this.sortedProfiles = sortedProfiles;
    }

    public String getComplianceLevel() {
        return complianceLevel;
    }

    public void setCompliantCount(int compliantCount) {
        this.compliantCount= compliantCount;
    }

    public int getCompliantCount() {
        return compliantCount;
    }

    public void setNonCompliantCount(int nonCompliantCount) {
        this.nonCompliantCount = nonCompliantCount;
    }

    public int getNonCompliantCount() {
        return nonCompliantCount;
    }

    public void setComplianceLevel(String complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

	public String getAssessmentType() {
		return assessmentType;
	}

	public void setAssessmentType(String assessmentType) {
		this.assessmentType = assessmentType;
	}
}

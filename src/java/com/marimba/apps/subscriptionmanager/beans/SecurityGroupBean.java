package com.marimba.apps.subscriptionmanager.beans;

import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: inelaiyara
 * Date: Mar 31, 2017
 * Time: 1:03:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityGroupBean {
    String id;
    String title;
    String description;
    String warning;

    int passedRulesCount;
    int failedRulesCount;
    int otherRulesCount;

    String passedRulesPercentage;
    String failedRulesPercentage;
    String otherRulesPercentage;

    ArrayList<SecurityGroupBean> groups;
    ArrayList<SecurityRuleBean> rules;

    int groupsCount;
    int rulesCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public int getPassedRulesCount() {
        return passedRulesCount;
    }

    public void setPassedRulesCount(int passedRulesCount) {
        this.passedRulesCount = passedRulesCount;
    }

    public int getFailedRulesCount() {
        return failedRulesCount;
    }

    public void setFailedRulesCount(int failedRulesCount) {
        this.failedRulesCount = failedRulesCount;
    }

    public int getOtherRulesCount() {
        return otherRulesCount;
    }

    public void setOtherRulesCount(int otherRulesCount) {
        this.otherRulesCount = otherRulesCount;
    }

    public String getPassedRulesPercentage() {
        return passedRulesPercentage;
    }

    public void setPassedRulesPercentage(String passedRulesPercentage) {
        this.passedRulesPercentage = passedRulesPercentage;
    }

    public String getFailedRulesPercentage() {
        return failedRulesPercentage;
    }

    public void setFailedRulesPercentage(String failedRulesPercentage) {
        this.failedRulesPercentage = failedRulesPercentage;
    }

    public String getOtherRulesPercentage() {
        return otherRulesPercentage;
    }

    public void setOtherRulesPercentage(String otherRulesPercentage) {
        this.otherRulesPercentage = otherRulesPercentage;
    }

    public ArrayList<SecurityGroupBean> getGroups() {
        if (groups == null) {
            groups = new ArrayList<SecurityGroupBean>();
        }
        return groups;
    }

    public void setGroups(ArrayList<SecurityGroupBean> groups) {
        this.groups = groups;
    }

    public ArrayList<SecurityRuleBean> getRules() {
        if (rules == null) {
            rules = new ArrayList<SecurityRuleBean>();
        }
        return rules;
    }

    public void setRules(ArrayList<SecurityRuleBean> rules) {
        this.rules = rules;
    }

    public int getGroupsCount() {
        if (groupsCount == 0) {
            groupsCount = getGroups().size();
        }
        return groupsCount;
    }

    public void setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
    }

    public int getRulesCount() {
        if (rulesCount == 0) {
            rulesCount = getRules().size();
        }
        return rulesCount;
    }

    public void setRulesCount(int rulesCount) {
        this.rulesCount = rulesCount;
    }
}
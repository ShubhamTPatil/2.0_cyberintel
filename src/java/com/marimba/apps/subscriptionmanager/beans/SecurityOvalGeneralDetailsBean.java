// Copyright 2018, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import com.marimba.apps.subscriptionmanager.beans.SecurityOvalDefinitionDetailsBean;

public class SecurityOvalGeneralDetailsBean {
    String productVersion;
    String productName;
    String moduleName;
    String timestamp;
    String date;
    String time;
    String generatorSchemaVersion;
    String generatorProductVersion;
    String generatorProductName;
    String generatorTimestamp;
    String generatorDate;
    String generatorTime;
    String generatorContentVersion;
    int totalPass;
    int totalFail;
    int totalError;
    int totalUnknown;
    int totalOther;
    int totalDefinitions;
    int totalComplianceDefinitions;
    int totalInventoryDefinitions;
    int totalMiscellaneousDefinitions;
    int totalPatchDefinitions;
    int totalVulnerabilityDefinitions;
    int totalTests;
    int totalObjects;
    int totalStates;
    int totalVariables;
    String systemHostName;
    String systemOperatingSystem;
    String systemOperatingSystemVersion;
    String systemArchitecture;
    Collection<SecurityOvalDefinitionDetailsBean> securityOvalDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();;
    Collection<SecurityOvalDefinitionDetailsBean> securityOvalPassDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();;
    Collection<SecurityOvalDefinitionDetailsBean> securityOvalFailDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();;
    Collection<SecurityOvalDefinitionDetailsBean> securityOvalErrorDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();;
    Collection<SecurityOvalDefinitionDetailsBean> securityOvalUnknownDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();;
    Collection<SecurityOvalDefinitionDetailsBean> securityOvalOtherDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();;

    public String getProductVersion() {
        if ((this.productVersion == null) || (this.productVersion.trim().length() < 1)) {
            return "9.0.04";
        }
        return productVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public String getProductName() {
        if ((this.productName == null) || (this.productName.trim().length() < 1)) {
            return "Marimba";
        }
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getModuleName() {
        if ((this.moduleName == null) || (this.moduleName.trim().length() < 1)) {
            return "Clarinet";
        }
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getTimestamp() {
        if ((this.timestamp == null) || (this.timestamp.trim().length() < 1)) {
            Date dateNow = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateFormatted = formatter.format(dateNow);
            dateFormatted = dateFormatted.split(" ")[0] + "T" + dateFormatted.split(" ")[1];
            this.timestamp = dateFormatted;
        }
        this.date = this.timestamp.substring(0, this.timestamp.indexOf("T"));
        this.time = this.timestamp.substring(this.timestamp.indexOf("T") + 1);;
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        getTimestamp();
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        getTimestamp();
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGeneratorSchemaVersion() {
        return generatorSchemaVersion;
    }

    public void setGeneratorSchemaVersion(String generatorSchemaVersion) {
        this.generatorSchemaVersion = generatorSchemaVersion;
    }

    public String getGeneratorProductVersion() {
        return generatorProductVersion;
    }

    public void setGeneratorProductVersion(String generatorProductVersion) {
        this.generatorProductVersion = generatorProductVersion;
    }

    public String getGeneratorProductName() {
        return generatorProductName;
    }

    public void setGeneratorProductName(String generatorProductName) {
        this.generatorProductName = generatorProductName;
    }

    public String getGeneratorTimestamp() {
        if ((this.generatorTimestamp == null) || (this.generatorTimestamp.trim().length() < 1)) {
            Date dateNow = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateFormatted = formatter.format(dateNow);
            dateFormatted = dateFormatted.split(" ")[0] + "T" + dateFormatted.split(" ")[1];
            this.generatorTimestamp = dateFormatted;
        }
        this.generatorDate = this.generatorTimestamp.substring(0, this.generatorTimestamp.indexOf("T"));
        this.generatorTime = this.generatorTimestamp.substring(this.generatorTimestamp.indexOf("T") + 1);;
        return generatorTimestamp;
    }

    public void setGeneratorTimestamp(String generatorTimestamp) {
        this.generatorTimestamp = generatorTimestamp;
    }

    public String getGeneratorDate() {
        getGeneratorTimestamp();
        return generatorDate;
    }

    public void setGeneratorDate(String generatorDate) {
        this.generatorDate = generatorDate;
    }

    public String getGeneratorTime() {
        getGeneratorTimestamp();
        return generatorTime;
    }

    public void setGeneratorTime(String generatorTime) {
        this.generatorTime = generatorTime;
    }

    public String getGeneratorContentVersion() {
        return generatorContentVersion;
    }

    public void setGeneratorContentVersion(String generatorContentVersion) {
        this.generatorContentVersion = generatorContentVersion;
    }

    public int getTotalPass() {
        return totalPass;
    }

    public void setTotalPass(int totalPass) {
        this.totalPass = totalPass;
    }

    public int getTotalFail() {
        return totalFail;
    }

    public void setTotalFail(int totalFail) {
        this.totalFail = totalFail;
    }

    public int getTotalError() {
        return totalError;
    }

    public void setTotalError(int totalError) {
        this.totalError = totalError;
    }

    public int getTotalUnknown() {
        return totalUnknown;
    }

    public void setTotalUnknown(int totalUnknown) {
        this.totalUnknown = totalUnknown;
    }

    public int getTotalOther() {
        return totalOther;
    }

    public void setTotalOther(int totalOther) {
        this.totalOther = totalOther;
    }

    public int getTotalDefinitions() {
        return totalDefinitions;
    }

    public void setTotalDefinitions(int totalDefinitions) {
        this.totalDefinitions = totalDefinitions;
    }

    public int getTotalComplianceDefinitions() {
        return totalComplianceDefinitions;
    }

    public void setTotalComplianceDefinitions(int totalComplianceDefinitions) {
        this.totalComplianceDefinitions = totalComplianceDefinitions;
    }

    public int getTotalInventoryDefinitions() {
        return totalInventoryDefinitions;
    }

    public void setTotalInventoryDefinitions(int totalInventoryDefinitions) {
        this.totalInventoryDefinitions = totalInventoryDefinitions;
    }

    public int getTotalMiscellaneousDefinitions() {
        return totalMiscellaneousDefinitions;
    }

    public void setTotalMiscellaneousDefinitions(int totalMiscellaneousDefinitions) {
        this.totalMiscellaneousDefinitions = totalMiscellaneousDefinitions;
    }

    public int getTotalPatchDefinitions() {
        return totalPatchDefinitions;
    }

    public void setTotalPatchDefinitions(int totalPatchDefinitions) {
        this.totalPatchDefinitions = totalPatchDefinitions;
    }

    public int getTotalVulnerabilityDefinitions() {
        return totalVulnerabilityDefinitions;
    }

    public void setTotalVulnerabilityDefinitions(int totalVulnerabilityDefinitions) {
        this.totalVulnerabilityDefinitions = totalVulnerabilityDefinitions;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public int getTotalObjects() {
        return totalObjects;
    }

    public void setTotalObjects(int totalObjects) {
        this.totalObjects = totalObjects;
    }

    public int getTotalStates() {
        return totalStates;
    }

    public void setTotalStates(int totalStates) {
        this.totalStates = totalStates;
    }

    public int getTotalVariables() {
        return totalVariables;
    }

    public void setTotalVariables(int totalVariables) {
        this.totalVariables = totalVariables;
    }

    public String getSystemHostName() {
        return systemHostName;
    }

    public void setSystemHostName(String systemHostName) {
        this.systemHostName = systemHostName;
    }

    public String getSystemOperatingSystem() {
        return systemOperatingSystem;
    }

    public void setSystemOperatingSystem(String systemOperatingSystem) {
        this.systemOperatingSystem = systemOperatingSystem;
    }

    public String getSystemOperatingSystemVersion() {
        return systemOperatingSystemVersion;
    }

    public void setSystemOperatingSystemVersion(String systemOperatingSystemVersion) {
        this.systemOperatingSystemVersion = systemOperatingSystemVersion;
    }

    public String getSystemArchitecture() {
        return systemArchitecture;
    }

    public void setSystemArchitecture(String systemArchitecture) {
        this.systemArchitecture = systemArchitecture;
    }

    public Collection<SecurityOvalDefinitionDetailsBean> getSecurityOvalDefinitionDetailsBeans() {
        return securityOvalDefinitionDetailsBeans;
    }

    public void setSecurityOvalDefinitionDetailsBeans(Collection<SecurityOvalDefinitionDetailsBean> securityOvalDefinitionDetailsBeans) {
        this.securityOvalDefinitionDetailsBeans = securityOvalDefinitionDetailsBeans;
    }

    public void addSecurityOvalDefinitionDetailsBeans(SecurityOvalDefinitionDetailsBean securityOvalDefinitionDetailsBean) {
        if (this.securityOvalDefinitionDetailsBeans == null) {
            this.securityOvalDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();
        }
        this.securityOvalDefinitionDetailsBeans.add(securityOvalDefinitionDetailsBean);
    }

    public Collection<SecurityOvalDefinitionDetailsBean> getSecurityOvalPassDefinitionDetailsBeans() {
        return securityOvalPassDefinitionDetailsBeans;
    }

    public void setSecurityOvalPassDefinitionDetailsBeans(Collection<SecurityOvalDefinitionDetailsBean> securityOvalPassDefinitionDetailsBeans) {
        this.securityOvalPassDefinitionDetailsBeans = securityOvalPassDefinitionDetailsBeans;
    }

    public void addSecurityOvalPassDefinitionDetailsBeans(SecurityOvalDefinitionDetailsBean securityOvalPassDefinitionDetailsBean) {
        if (this.securityOvalPassDefinitionDetailsBeans == null) {
            this.securityOvalPassDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();
        }
        this.securityOvalPassDefinitionDetailsBeans.add(securityOvalPassDefinitionDetailsBean);
    }

    public Collection<SecurityOvalDefinitionDetailsBean> getSecurityOvalFailDefinitionDetailsBeans() {
        return securityOvalFailDefinitionDetailsBeans;
    }

    public void setSecurityOvalFailDefinitionDetailsBeans(Collection<SecurityOvalDefinitionDetailsBean> securityOvalFailDefinitionDetailsBeans) {
        this.securityOvalFailDefinitionDetailsBeans = securityOvalFailDefinitionDetailsBeans;
    }

    public void addSecurityOvalFailDefinitionDetailsBeans(SecurityOvalDefinitionDetailsBean securityOvalFailDefinitionDetailsBean) {
        if (this.securityOvalFailDefinitionDetailsBeans == null) {
            this.securityOvalFailDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();
        }
        this.securityOvalFailDefinitionDetailsBeans.add(securityOvalFailDefinitionDetailsBean);
    }

    public Collection<SecurityOvalDefinitionDetailsBean> getSecurityOvalErrorDefinitionDetailsBeans() {
        return securityOvalErrorDefinitionDetailsBeans;
    }

    public void setSecurityOvalErrorDefinitionDetailsBeans(Collection<SecurityOvalDefinitionDetailsBean> securityOvalErrorDefinitionDetailsBeans) {
        this.securityOvalErrorDefinitionDetailsBeans = securityOvalErrorDefinitionDetailsBeans;
    }

    public void addSecurityOvalErrorDefinitionDetailsBeans(SecurityOvalDefinitionDetailsBean securityOvalErrorDefinitionDetailsBean) {
        if (this.securityOvalErrorDefinitionDetailsBeans == null) {
            this.securityOvalErrorDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();
        }
        this.securityOvalErrorDefinitionDetailsBeans.add(securityOvalErrorDefinitionDetailsBean);
    }

    public Collection<SecurityOvalDefinitionDetailsBean> getSecurityOvalUnknownDefinitionDetailsBeans() {
        return securityOvalUnknownDefinitionDetailsBeans;
    }

    public void setSecurityOvalUnknownDefinitionDetailsBeans(Collection<SecurityOvalDefinitionDetailsBean> securityOvalUnknownDefinitionDetailsBeans) {
        this.securityOvalUnknownDefinitionDetailsBeans = securityOvalUnknownDefinitionDetailsBeans;
    }

    public void addSecurityOvalUnknownDefinitionDetailsBeans(SecurityOvalDefinitionDetailsBean securityOvalUnknownDefinitionDetailsBean) {
        if (this.securityOvalUnknownDefinitionDetailsBeans == null) {
            this.securityOvalUnknownDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();
        }
        this.securityOvalUnknownDefinitionDetailsBeans.add(securityOvalUnknownDefinitionDetailsBean);
    }

    public Collection<SecurityOvalDefinitionDetailsBean> getSecurityOvalOtherDefinitionDetailsBeans() {
        return securityOvalOtherDefinitionDetailsBeans;
    }

    public void setSecurityOvalOtherDefinitionDetailsBeans(Collection<SecurityOvalDefinitionDetailsBean> securityOvalOtherDefinitionDetailsBeans) {
        this.securityOvalOtherDefinitionDetailsBeans = securityOvalOtherDefinitionDetailsBeans;
    }

    public void addSecurityOvalOtherDefinitionDetailsBeans(SecurityOvalDefinitionDetailsBean securityOvalOtherDefinitionDetailsBean) {
        if (this.securityOvalOtherDefinitionDetailsBeans == null) {
            this.securityOvalOtherDefinitionDetailsBeans = new ArrayList<SecurityOvalDefinitionDetailsBean>();
        }
        this.securityOvalOtherDefinitionDetailsBeans.add(securityOvalOtherDefinitionDetailsBean);
    }

    public String toString() {
        return "SecurityOvalGeneralDetailsBean{" +
                "productVersion='" + productVersion + '\'' +
                ", productName='" + productName + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", generatorSchemaVersion='" + generatorSchemaVersion + '\'' +
                ", generatorProductVersion='" + generatorProductVersion + '\'' +
                ", generatorProductName='" + generatorProductName + '\'' +
                ", generatorTimestamp='" + generatorTimestamp + '\'' +
                ", generatorDate='" + generatorDate + '\'' +
                ", generatorTime='" + generatorTime + '\'' +
                ", generatorContentVersion='" + generatorContentVersion + '\'' +
                ", totalPass=" + totalPass +
                ", totalFail=" + totalFail +
                ", totalError=" + totalError +
                ", totalUnknown=" + totalUnknown +
                ", totalOther=" + totalOther +
                ", totalDefinitions=" + totalDefinitions +
                ", totalComplianceDefinitions=" + totalComplianceDefinitions +
                ", totalInventoryDefinitions=" + totalInventoryDefinitions +
                ", totalMiscellaneousDefinitions=" + totalMiscellaneousDefinitions +
                ", totalPatchDefinitions=" + totalPatchDefinitions +
                ", totalVulnerabilityDefinitions=" + totalVulnerabilityDefinitions +
                ", totalTests=" + totalTests +
                ", totalObjects=" + totalObjects +
                ", totalStates=" + totalStates +
                ", totalVariables=" + totalVariables +
                ", systemHostName='" + systemHostName + '\'' +
                ", systemOperatingSystem='" + systemOperatingSystem + '\'' +
                ", systemOperatingSystemVersion='" + systemOperatingSystemVersion + '\'' +
                ", systemArchitecture='" + systemArchitecture + '\'' +
                ", securityOvalDefinitionDetailsBeans=" + securityOvalDefinitionDetailsBeans +
                '}';
    }
}

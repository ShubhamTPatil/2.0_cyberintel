package com.marimba.apps.subscriptionmanager.beans;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: inelaiyara
 * Date: Mar 31, 2017
 * Time: 1:03:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class SecurityRuleBean {
    String id;
    String title;
    String description;
    String warning;
    String selected;
    String severity;
    String rationale;
    String fixId;
    String fixSystem;
    String fixScript;
    ArrayList<String> reference = new ArrayList<String>();
    ArrayList<String> ident = new ArrayList<String>();
    String valueID;
    String value;
    String valueType;
    String parentId;
    String result;
    String uuid;

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

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public String getFixId() {
        return fixId;
    }

    public void setFixId(String fixId) {
        this.fixId = fixId;
    }

    public String getFixSystem() {
        return fixSystem;
    }

    public void setFixSystem(String fixSystem) {
        this.fixSystem = fixSystem;
    }

    public String getFixScript() {
        return fixScript;
    }

    public void setFixScript(String fixScript) {
        this.fixScript = fixScript;
    }

    public ArrayList<String> getReference() {
        return reference;
    }

    public void setReference(ArrayList<String> reference) {
        this.reference = reference;
    }

    public void setReference(String reference) {
        this.reference.add(reference);
    }

    public ArrayList<String> getIdent() {
        return ident;
    }

    public void setIdent(ArrayList<String> ident) {
        this.ident = ident;
    }

    public void setIdent(String ident) {
        this.ident.add(ident);
    }

    public String getValueID() {
        return valueID;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

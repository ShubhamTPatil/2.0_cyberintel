package com.marimba.apps.subscriptionmanager.beans;

public class ChangeRequestBean {
    String operationType = "";
    String key = "";
    String oldValue = "";
    String newValue = "";

    public ChangeRequestBean() {

    }

    public ChangeRequestBean(String str) {
        String[] values = str.split(",");

        operationType = values[0];
        key = values[1];
        oldValue = values[2];
        newValue = values[3];
    }

    public String getOperationType () {
        return operationType;
    }

    public void setOperationType (String operationType) {
        this.operationType = operationType;
    }

    public String getKey () {
        return key;
    }

    public void setKey (String key) {
        this.key = key;
    }

    public String getOldValue () {
        return oldValue;
    }

    public void setOldValue (String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue () {
        return newValue;
    }

    public void setNewValue (String newValue) {
        this.newValue = newValue;
    }

    public String toString() {
        return operationType + "," + key + "," + oldValue + "," + newValue;
    }
}

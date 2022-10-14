// Copyright 2018, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.beans;

import java.util.HashMap;

public class SecurityOvalDefinitionDetailsBean {
    String id;
    String result;
    String classType;
    String title;
    String refId;
    HashMap<String, String> references = new HashMap<String, String>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public HashMap<String, String> getReferences() {
        if (this.references == null) {
            this.references = new HashMap<String, String>();
        }
        return references;
    }

    public void setReferences(HashMap<String, String> references) {
        this.references = references;
    }

    public void addReference(String key, String value) {
        if (this.references == null) {
            this.references = new HashMap<String, String>();
        }
        this.references.put(key, value);
    }

    public String toString() {
        return "SecurityOvalDefinitionDetailsBean{" +
                "id='" + id + '\'' +
                ", result='" + result + '\'' +
                ", classType='" + classType + '\'' +
                ", title='" + title + '\'' +
                ", refId='" + refId + '\'' +
                ", references=" + references +
                '}';
    }
}

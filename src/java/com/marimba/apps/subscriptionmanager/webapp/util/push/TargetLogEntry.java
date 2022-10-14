package com.marimba.apps.subscriptionmanager.webapp.util.push;

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

/**
 * Class to encapsulate individual DM Target Log entries, needed for UI access
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 * $File$
 * 
 */
public class TargetLogEntry {


    private String datetime;
    private String severity;
    private String description;
    private String id;
    private String logline;


    public TargetLogEntry(String datetime, String severity, String id, String description) {
        this.datetime = datetime;
        this.severity = severity;
        this.id = id;
        this.description = description;
    }
    /**
     * Assumes that the propertyPairs contain key value pairs
     * and is even number for the length of the array.
     * dateTime, severity, id, description must be present in the propertyPairs
     * @param propertyPairs
     */
    public TargetLogEntry(String[] propertyPairs) {

        for(int index = 0; index < propertyPairs.length; index=index+2) {
            if (propertyPairs[index].equals("logtime")) {
                this.datetime = propertyPairs[index+1];
            } else if (propertyPairs[index].equals("severity")) {
                this.severity = propertyPairs[index+1];
            } else if (propertyPairs[index].equals("id")) {
                this.id = propertyPairs[index+1];
            } else if (propertyPairs[index].equals("description")) {
                this.description = propertyPairs[index+1];
            } else if (propertyPairs[index].equals("logline")) {
                this.logline = propertyPairs[index+1];
            }
        }
    }


    public String getLogline() {
        return logline;
    }


    public String getDatetime() {
        return datetime;
    }


    public String getDescription() {
        return description;
    }


    public String getId() {
        return id;
    }



    public String getSeverity() {
        return severity;
    }




}

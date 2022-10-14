// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin;

/**
 * Capturing timestamps for the plugin tasks.
 */
class PluginTasks extends TaskDurationLogger {

    static final String RESOLVE_MACHINE = "machine";
    static final String RESOLVE_USER = "user";
    static final String RESOLVE_MACHINE_DOMAIN = "machinedomain";
    static final String RESOLVE_USER_DOMAIN = "userdomain";
    static final String RESOLVE_GROUPS = "groups";
    static final String RESOLVE_CONTAINERS = "containers";
    static final String RESOLVE_SUBSCRIPTIONS = "subscriptions";
    static final String ALL_TASKS = "alltasks";

    PluginTasks() {
        createTaskInfo(RESOLVE_MACHINE);
        createTaskInfo(RESOLVE_USER);
        createTaskInfo(RESOLVE_MACHINE_DOMAIN);
        createTaskInfo(RESOLVE_USER_DOMAIN);
        createTaskInfo(RESOLVE_GROUPS);
        createTaskInfo(RESOLVE_CONTAINERS);
        createTaskInfo(RESOLVE_SUBSCRIPTIONS);
        createTaskInfo(ALL_TASKS);
    }

    String fetchTimeStampString() {
        // The format for the times is as follows
        // MahcineDN/UserDN : <machine search time> , <user search time> , <machine domain search time> ,
        //                    <user domain search time> , <group search time> , <container search time> ,
        //                    <subscriptions search time>, <total time>, connected to: <ldaphost>:<ldapport>
        // <user search time>, <user dn search time>, <machine search time>,
        // <machine dn search time> will be -1 if no search was done against the
        // directory server.
        StringBuffer timeStampString = new StringBuffer();
        timeStampString.append(getDuration(RESOLVE_MACHINE)).append(", ");
        timeStampString.append(getDuration(RESOLVE_USER)).append(", ");
        timeStampString.append(getDuration(RESOLVE_MACHINE_DOMAIN)).append(", ");
        timeStampString.append(getDuration(RESOLVE_USER_DOMAIN)).append(", ");
        timeStampString.append(getDuration(RESOLVE_GROUPS)).append(", ");
        timeStampString.append(getDuration(RESOLVE_CONTAINERS)).append(", ");
        timeStampString.append(getDuration(RESOLVE_SUBSCRIPTIONS)).append(", ");
        timeStampString.append(getDuration(ALL_TASKS));

        return timeStampString.toString();
    }

    void removeTaskInfoObjs() {
        destroyTaskInfo(RESOLVE_MACHINE);
        destroyTaskInfo(RESOLVE_USER);
        destroyTaskInfo(RESOLVE_MACHINE_DOMAIN);
        destroyTaskInfo(RESOLVE_USER_DOMAIN);
        destroyTaskInfo(RESOLVE_GROUPS);
        destroyTaskInfo(RESOLVE_CONTAINERS);
        destroyTaskInfo(RESOLVE_SUBSCRIPTIONS);
        destroyTaskInfo(ALL_TASKS);
    }
}

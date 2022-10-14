// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks the time taken for a task/functionality executed in the plugin. This class needs to
 * be created per thread instance since it is not thread safe. Eventually this class can be moved to
 * subscription common.
 */
class TaskDurationLogger {

    private Map tasks;

    TaskDurationLogger() {
        tasks = new HashMap();
    }

    void createTaskInfo(String uniqueTaskName) {
        if(!tasks.containsKey(uniqueTaskName)) {
            TaskTimeInfo task = new TaskTimeInfo(uniqueTaskName);
            tasks.put(uniqueTaskName, task);
        }
    }

    void taskStarted(String taskName) {
        TaskTimeInfo task = (TaskTimeInfo)tasks.get(taskName);
        if(task != null) {
            task.tagStartTime();
        }
    }

    void taskStopped(String taskName) {
        TaskTimeInfo task = (TaskTimeInfo)tasks.get(taskName);
        if(task != null) {
            task.tagStopTime();
        }
    }

    void destroyTaskInfo(String taskName) {
        TaskTimeInfo task = (TaskTimeInfo)tasks.get(taskName);
        if(task != null) {
            tasks.remove(taskName);
        }
    }

    long getDuration(String taskName) {
        TaskTimeInfo task = (TaskTimeInfo)tasks.get(taskName);
        if(task != null) {
            return task.getDuration();
        }
        return -1;
    }

    /**
     * Represents a task's time stamps.
     */
    private static class TaskTimeInfo {
        private String name;
        private long startTime;
        private long endTime;

        TaskTimeInfo(String name) {
            this.name = name;
            startTime = -1;
            endTime = -1;
        }

        void tagStartTime() {
            if(startTime == -1) {
                startTime = System.currentTimeMillis();
            }
        }

        void tagStopTime() {
            if(startTime != -1) {
                if(endTime == -1) {
                    endTime = System.currentTimeMillis();
                }
            }
        }

        /**
         * Calculate the time taken for the execution of the task.
         *
         * @return -1 if the task was not started and stopped.
         */
        long getDuration() {
            if(startTime != -1 && endTime != -1) {
                return endTime - startTime;
            }
            return -1;
        }

        public String toString() {
            return "[" + name + "," + startTime + "," + endTime + "]";
        }
    }
}

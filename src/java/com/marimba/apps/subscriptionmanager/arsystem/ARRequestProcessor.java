// Copyright 2005, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionmanager.arsystem;


import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.intf.msf.arsys.ARManagerException;
import com.marimba.intf.msf.arsys.IARConstants;
import com.marimba.intf.msf.task.ITaskMgr;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.tools.util.DebugFlag;

import java.util.List;
import java.util.Map;

/**
 * File extracts/updates channel information from the webservice XML Element
 *
 * @author Devendra Vamathevan
 * @version 7.0.0.0 10/29/2005
 */
public class ARRequestProcessor {

    private String taskID;
    private String changeID;
    private String urlList;
    private String targetList;
    private ARObjectSource objSource;

    // Parameters used for testing only
    private final String PARAM_URL      = "url";
    private final String PARAM_TARGET   = "target";
    private final String AR_DISABLED    = "AR Disabled";
    private final static int DEBUG = DebugFlag.getDebug("SUB/ARTASK");

    public ARRequestProcessor(SubscriptionMain main, Map parameters,  IUser user, ITaskMgr taskMgr) throws ARManagerException,
            SystemException {
        // get parameter values
        taskID =  getParameterString(parameters.get(IARConstants.PARAM_TASK_ID ));
        changeID = getParameterString(parameters.get(IARConstants.PARAM_CHANGE_ID));
        urlList =  getParameterString(parameters.get(PARAM_URL));
        targetList = getParameterString(parameters.get(PARAM_TARGET));
        if (DEBUG > 4){
            ARUtils.debug("ARRequestProcessor: TaskID " +  taskID);
            ARUtils.debug("ARRequestProcessor: changeID " + changeID);
            ARUtils.debug("ARRequestProcessor:  urlList " + urlList );
            ARUtils.debug("ARRequestProcessor:   targetList " + targetList); 
        }

        // if all parameters are  null throw exception
        if ((urlList == null) && (targetList == null) && (taskID == null)) {
            throw new CriticalException(IErrorConstants.AR_NO_TASK_ID);
        }
        boolean lazy = taskID == null;
        if (lazy) {
            taskID = AR_DISABLED;
        }
        objSource = new ARObjectSource(main, parameters, user, lazy, taskMgr);
    }


    public ARObjectSource getARObjectSource()   {
        return objSource;
    }
    
    public List getTargets() throws SystemException {
        if (null == targetList) {
            return objSource.getTargets();
        } else {
            return objSource.getTargets(targetList);
        }
    }

    public List getChannelList() throws SystemException {
        if (null == urlList) {
            return objSource.getChannels();
        } else {
            return objSource.getChannels(urlList);
        }
    }

    public String getTaskID() {
        return taskID;
    }

    public String getChangeID() {
        return changeID;
    }

    public static String getParameterString(Object parameter){
        if ( parameter == null){
           return null;
        }
        if ( parameter instanceof String []){
            return ((String[]) parameter)[0];
        }  else  if ( parameter instanceof String ){
            return (String) parameter;
        }
        return parameter.toString();
    }

}

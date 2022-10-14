// Copyright 2009, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.config.*;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.view.*;
import com.marimba.webapps.tools.util.*;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.*;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.arsys.*;
import com.marimba.intf.msf.task.*;
import com.marimba.intf.util.IConfig;

import org.w3c.dom.*;
import org.apache.struts.util.MessageResources;

import java.util.*;

/**
 * implementation class for retrieve targets and channel info from Task details
 *     and convert them to internal data structures that SPM  can understand.
 *
 * @author   Helen Wu
 * @author   Devendra Vamathevan
 * @version  $Revision$, $Date$
 *
 */

public class ARObjectSource
        implements ARTaskLogConstants, ISubscriptionConstants {

    ARChannelSource arChannelSource;
	ARTargetSource arTargetSource;
    Element taskForm;
    Element verifyTaskForm;
    ARVerifyTask verifyTask;
    ARLogMgr arLogMgr;
    MessageResources msgResources;
    private final static int DEBUG = DebugFlag.getDebug("SUB/ARTASK");

	public ARObjectSource(SubscriptionMain main, Map parameters, IUser user, boolean lazy, ITaskMgr taskMgr)
            throws ARManagerException, SystemException {
        LDAPEnv ldapenv = main.getLDAPEnv();
		arChannelSource = new ARChannelSource();
        LDAPConnection conn = user.getBrowseConn();
		arTargetSource = new ARTargetSource(ldapenv, conn);
        arLogMgr = new ARLogMgr(main, main.getAppLog());
        msgResources = main.getAppResources();
        if (!lazy){
            if (DEBUG > 4){
                ARUtils.debug("ARObject source map values ");
                Iterator keys = parameters.keySet().iterator();
                while (keys.hasNext()) {
                    Object key = keys.next();
                    ARUtils.debug(key +  " = " + parameters.get(key));
                }
            }
            taskForm = ARConnectionManager.getARContext(main.getTenant()).getTask(parameters);
            verifyTaskForm = taskForm;
            String parentTaskId = getNodeValue(taskForm, IARConstants.TAG_PRIOR_TASK_ID);
            if (parentTaskId != null && parentTaskId.length() > 0)   {
                arLogMgr.log(LOG_AR_TASK_ID, LOG_AUDIT, parentTaskId, null, AR_TASK_VERIFIER);
                Map parentTaskMap = new HashMap();
                parentTaskMap.put(IARConstants.PARAM_TASK_ID, parentTaskId);
                taskForm  = ARConnectionManager.getARContext(main.getTenant()).getTask(parentTaskMap);
            }
        }
        initVerifyTask(main, parameters, user, taskMgr);
	}


    private void initVerifyTask(SubscriptionMain main, Map parameters,  IUser user, ITaskMgr taskMgr)
            throws ARManagerException, SystemException   {
        if (taskMgr != null) {
            verifyTask = new ARVerifyTask(taskMgr, main.getProperty("ccm.verifyaction.schedule"), main.getTenant());
            verifyTask.parseParameters(parameters, user);
            ARUtils.debug("ARObjectSource: Initialized Verify Task");
        }
    }

    public String doVerifyTaskAction()   {
        if (verifyTask == null) {
            return String.valueOf(false);
        }
        ARUtils.debug("ARObjectSource: Execute Verify Action");
        return verifyTask.doVerifyAction();
    }

	public List getTargets() throws SystemException {
		return arTargetSource.getTargets(taskForm);
	}

	public List getChannels() throws SystemException {
		return arChannelSource.getChannels(taskForm);
	}

    public String getChannelsAsString() throws SystemException {
        String packages = arChannelSource.getChannelsAsString(taskForm);
        arLogMgr.log(LOG_AR_CHANNELS, LOG_AUDIT, packages, null, AR_TASK_VERIFIER);
        return packages;
    }

    public List getTargets(String targets) throws SystemException {
        arLogMgr.log(LOG_AR_TARGETS, LOG_AUDIT, targets, null, AR_TASK_VERIFIER);
		return arTargetSource.getTargets(targets);
	}

    public String getTargetsToVerify()   throws SystemException {
        String targetsToVerify = arTargetSource.getTargetsToVerify(taskForm);
        arLogMgr.log(LOG_AR_TARGETS, LOG_AUDIT, targetsToVerify, null, AR_TASK_VERIFIER);
        return targetsToVerify;
    }

	public List getChannels(String channels) throws SystemException {
		return arChannelSource.getChannels(channels);
	}

    public String getCompliancePercentage() throws SystemException {
        String perc = getNodeValue(verifyTaskForm, IARConstants.TAG_COMPLIANCE_TARGET);
        if (perc == null)   {
            perc = IARTaskConstants.DEFAULT_PERCENTAGE;
        } else if( (Integer.parseInt(perc) < 0) || (Integer.parseInt(perc) > 100) ) {
            throw new SystemException(IErrorConstants.AR_INVALID_PERCENTAGE);
        }
        ARUtils.debug("ARObjectSource: getCompliancePercentage: "+perc);
        return perc;
    }

    public String getTimeoutWindow() throws SystemException {
        String timeout = getNodeValue(verifyTaskForm, IARConstants.TAG_TASK_EXPIRY_DURATION);
        if (timeout == null)    {
            timeout = IARTaskConstants.DEFAULT_TIMEOUT;
        } else if(Integer.parseInt(timeout) < 0) {
            throw new SystemException(IErrorConstants.AR_INVALID_TIMEOUT);
        }
        ARUtils.debug("ARObjectSource: getTimeoutWindow: "+timeout);
       return timeout;
    }

    public String getSchedule() {
        String schedule = verifyTask.taskSchedule;
        ARUtils.debug("ARObjectSource: getSchedule: "+schedule);
        return schedule;
    }
  
    private String getNodeValue(Element eform, String nodeTag) {
        String param = null;

        if (eform == null ) {
			ARUtils.debug("ARConnectionManager: GetNodeValue: Empty");
            return null;
		}

        NodeList nodelist = eform.getElementsByTagNameNS(IARConstants.ARELEMENT_NAMESPACE, IARConstants.TAG_TASK_QUERY);
        ARUtils.debug("ARConnectionManager: NodeList Size: "+nodelist.getLength());

    	for (int i = 0; i < nodelist.getLength(); i++) {
            if (param != null)   {
                break;
            }
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element ci = (Element) nodelist.item(i);
                param = ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, nodeTag);
            }
		}
        ARUtils.debug(ARUtils.ElementToString(eform));
        ARUtils.debug(" return parameter" + param);
        return param;
    }

    public ARLogMgr getLogManager() {
        return arLogMgr;
    }

    public class ARVerifyTask {

        ITaskMgr taskMgr;
        long estimatedQueryRunTimeBuffer = 30; // in minutes
        String taskId;
        String closeTask;
        String targets;
        String packages;
        String endTime;
        String compliancePerc;
        String taskSchedule = IARTaskConstants.TASK_SCHEDULE_NEVER;
        String queryAs;
        String expiryTime = IARTaskConstants.DEFAULT_TIMEOUT;
        ITenant tenant;

        ARVerifyTask(ITaskMgr taskMgr, String taskSchedule, ITenant tenant)    {
            this.taskMgr = taskMgr;
            this.taskSchedule = taskSchedule;
            this.tenant = tenant;
        }

        void parseParameters(Map parameters, IUser user)
                throws CriticalException, SystemException, ARManagerException
        {
            taskId = ARRequestProcessor.getParameterString(parameters.get(IARConstants.PARAM_TASK_ID));
            closeTask = ARRequestProcessor.getParameterString(parameters.get("close"));
            if (taskId == null) {
                taskId = "AR_TEST";
                compliancePerc = ARRequestProcessor.getParameterString(parameters.get("perc"));
                endTime = ARRequestProcessor.getParameterString(parameters.get("endtime"));// in hours
                targets = ARRequestProcessor.getParameterString(parameters.get("target"));
                packages = ARRequestProcessor.getParameterString(parameters.get("url"));
            }
            else    {
                compliancePerc = getCompliancePercentage();
                endTime = getTimeoutWindow();
                targets = getTargetsToVerify();
                packages =  getChannelsAsString();
            }

            queryAs = user.getName();
            taskSchedule = getSchedule(endTime);

            arLogMgr.log(LOG_AR_COMPLIANCE_PERCENTAGE, LOG_AUDIT, compliancePerc, null, AR_TASK_VERIFIER);
            arLogMgr.log(LOG_AR_ENDTIME, LOG_AUDIT, endTime, null, AR_TASK_VERIFIER);
            arLogMgr.log(LOG_AR_TARGETS, LOG_AUDIT, targets, null, AR_TASK_VERIFIER);
            arLogMgr.log(LOG_AR_CHANNELS, LOG_AUDIT, packages, null, AR_TASK_VERIFIER);
            arLogMgr.log(LOG_AR_USER, LOG_AUDIT, queryAs, null, AR_TASK_VERIFIER);
            arLogMgr.log(LOG_AR_TASK_SCHEDULE, LOG_AUDIT, taskSchedule, null, AR_TASK_VERIFIER);
        }

        String getSchedule(String endTime)  {
            String scheduleStr = null;
            if (endTime == null)    {
                scheduleStr = IARTaskConstants.TASK_SCHEDULE_NEVER;
                return scheduleStr;
            }
            long end_time = 0;
            try {
                end_time = Long.parseLong(endTime) * 60;  // convert to minutes
            }
            catch (NumberFormatException e) {
                arLogMgr.log(LOG_AR_EXCEPTION, LOG_MINOR, taskSchedule, e);
                ARUtils.debug("ARObjectSource:ARVerifyTask: No End Time: "+taskSchedule);
                scheduleStr = IARTaskConstants.TASK_SCHEDULE_NEVER;
                return scheduleStr;
            }

            Date currentDate = new Date();
            long currentTime = currentDate.getTime();
            ARUtils.debug("ARObjectSource:ARVerifyTask: Current Time: "+currentTime);
            // give about X minutes for  compliance query to run
            long new_end_time = end_time - estimatedQueryRunTimeBuffer;
            if (new_end_time <= 0)  {
                new_end_time = end_time;
            }
            expiryTime = String.valueOf(currentTime + end_time*60*1000);
            if (new_end_time == 0)  {
                scheduleStr = IARTaskConstants.TASK_SCHEDULE_NEVER;
                return scheduleStr;
            }

            BasicBean basicBean = new BasicBean();
            if (new_end_time < 24*60)   {
                basicBean.setValue(IScheduleConstants.SCHEDULE_FREQUENCY_TYPE, "schedule_frequency_daily");
                basicBean.setValue(IScheduleConstants.SCHEDULE_INTERVAL_DAYS_TYPE, "everyXDays");
                basicBean.setValue(IScheduleConstants.SCHEDULE_INTERVAL_DAYS, "1");
                basicBean.setValue(IScheduleConstants.SCHEDULE_TIME_INTERVAL_TYPE, "each");
                basicBean.setValue(IScheduleConstants.SCHEDULE_INTERVAL_MINUTES, String.valueOf(new_end_time));
                scheduleStr = ScheduleUtil.getScheduleString(basicBean);
            }
            else    {
                scheduleStr = taskSchedule;

                if (scheduleStr == null)    {
                    Calendar cal = Calendar.getInstance();
                    basicBean.setValue(IScheduleConstants.SCHEDULE_FREQUENCY_TYPE, "schedule_frequency_daily");
                    basicBean.setValue(IScheduleConstants.SCHEDULE_INTERVAL_DAYS_TYPE, "everyXDays");
                    basicBean.setValue(IScheduleConstants.SCHEDULE_INTERVAL_DAYS, "1");
                    basicBean.setValue(IScheduleConstants.SCHEDULE_TIME_INTERVAL_TYPE, "at");

                    int remainder = (int) (new_end_time%(24*60));
                    ARUtils.debug("ARObjectSource:ARVerifyTask: Remainder: "+remainder);

                    cal.setTime(currentDate);
                    cal.add(Calendar.MINUTE, remainder);

                    basicBean.setValue(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_HOUR, String.valueOf(cal.get(Calendar.HOUR)));
                    basicBean.setValue(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_MIN, String.valueOf(cal.get(Calendar.MINUTE)));
                    int am_pm = cal.get(Calendar.AM_PM);
                    if (am_pm == Calendar.AM)   {
                        basicBean.setValue(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_AM_PM, "am");
                    }
                    else    {
                        basicBean.setValue(IScheduleConstants.SCHEDULE_TIME_INTERVAL_START_AM_PM, "pm");
                    }
                    scheduleStr = ScheduleUtil.getScheduleString(basicBean);
                }
            }

            return scheduleStr;
        }

        String doVerifyAction() {
            boolean bSuccess = false;
            String verifyTaskName = new StringBuffer().append(IARTaskConstants.VERIFY_TASK_NAME).append(taskId).toString();
            if ("true".equals(closeTask))   {
                bSuccess =  taskMgr.removeTask(IARTaskConstants.VERIFY_TASK_GROUP, verifyTaskName);
                ARUtils.debug("ARObjectSource:ARVerifyTask: TaskConfig is removed: "+bSuccess);
                return String.valueOf(bSuccess);
            }

            if (packages == null && targets == null)    {
                String errMsg = "Targets/Packages incorrectly specified.";
                ARUtils.debug("ARObjectSource:ARVerifyTask: Error: "+errMsg);
                sendWorklog(bSuccess, errMsg);
                return String.valueOf(bSuccess);
            }

            IConfig taskConfig = new ConfigProps(null);
            taskConfig.setProperty("task.schedule", taskSchedule);
            taskConfig.setProperty("task.expirationTime", expiryTime);
            taskConfig.setProperty("compliance_percentage", compliancePerc);
            taskConfig.setProperty("ar_taskid", taskId);
            taskConfig.setProperty("ar_schedule", taskSchedule);
            taskConfig.setProperty("query_as", queryAs);
            taskConfig.setProperty("targets", targets);
            taskConfig.setProperty("packages", packages);

            bSuccess = taskMgr.addTask(IARTaskConstants.VERIFY_TASK_TYPE, IARTaskConstants.VERIFY_TASK_GROUP, verifyTaskName, taskConfig, true);
            arLogMgr.log(LOG_AR_TASK_CREATION_SUCCESS, LOG_AUDIT, String.valueOf(bSuccess), null, AR_TASK_VERIFIER);

            // End of DEBUG purpose statements
            if (IARTaskConstants.TASK_SCHEDULE_NEVER.equals(taskSchedule))  {
                ITaskHandle task = taskMgr.getTask(IARTaskConstants.VERIFY_TASK_GROUP, verifyTaskName);
                task.execute();
                ARUtils.debug("ARObjectSource:ARVerifyTask: Executing Verification Task: ");
                bSuccess = taskMgr.removeTask(IARTaskConstants.VERIFY_TASK_GROUP, verifyTaskName);
                arLogMgr.log(LOG_AR_TASK_REMOVAL_SUCCESS, LOG_AUDIT, String.valueOf(bSuccess), null, AR_TASK_VERIFIER);
            }
            else if (! "AR_TEST".equals(taskId))    {
                sendWorklog(bSuccess, null);
            }
            arLogMgr.log(LOG_AR_VERIFYTASK_RETURN_MSG, LOG_AUDIT, String.valueOf(bSuccess), null, AR_TASK_VERIFIER);
            return String.valueOf(bSuccess);
         }

         private void sendWorklog(boolean bSuccess, String errMsg) {
            try    {
                Map taskMap = new HashMap();
                String[] taskIdMap= new String[1];
                taskIdMap[0] = taskId;
                taskMap.put(IARConstants.PARAM_TASK_ID, taskIdMap);
                taskMap.put(IARTaskConstants.AR_USER, msgResources.getMessage("ar.worklog.modifieduser")+queryAs);
                ARWorklog wl = new ARWorklog(taskMap, tenant);
                String retCode = IARConstants.TMS_RETURN_CODE_OK;
                if ( ! bSuccess)   {
                    retCode = IARConstants.TMS_RETURN_CODE_ERROR;
                } else {
                    wl.write(msgResources.getMessage("ar.worklog.verifytaskreturned") +retCode);
                    wl.write("\n");
                    List targetsToLog = getTargets();
                    Iterator targetsItr = targetsToLog.iterator();
                    while(targetsItr.hasNext()) {
                        Target target = (Target) targetsItr.next();
                        wl.write(msgResources.getMessage("ar.worklog.targetName") + target.getName());
                        wl.write("\n");
                        wl.write(msgResources.getMessage("page.distribution.Target_Type") +" : " + target.getType());
                        wl.write("\n");
                    }
                    if(targetsToLog == null || targetsToLog.size() < 1) {
                        wl.write(msgResources.getMessage("page.colhdr.targets"));
                        wl.write(" : ");
                        wl.write(msgResources.getMessage("page.compliance_verifytask.none"));
                        wl.write("\n");
                    }
                    List channelsToLog = getChannels();
                    Iterator channelsItr = channelsToLog.iterator();
                    while(channelsItr.hasNext()) {
                        Channel channel = (Channel) channelsItr.next();
                        wl.write(msgResources.getMessage("ar.worklog.chTitle") + channel.getTitle());
                        wl.write("\n");
                    }
                    if(channelsToLog == null || channelsToLog.size() < 1) {
                        wl.write(msgResources.getMessage("page.colhdr.pkgs"));
                        wl.write(" : ");
                        wl.write(msgResources.getMessage("page.compliance_verifytask.none"));
                        wl.write("\n");
                    }
                    wl.write(msgResources.getMessage("page.compliance_verifytask.percentage") +" : "+ compliancePerc);
                    wl.write("\n");
                    wl.write(msgResources.getMessage("page.compliance_verifytask.schedule") +" : "+ taskSchedule);
                    wl.write("\n");
                    wl.write(msgResources.getMessage("page.compliance_verifytask.expiryTime") + " : "+ endTime);
                    wl.write("\n");

                }
                if (errMsg != null) {
                    wl.write("\n");
                    wl.write(errMsg);
                }
                wl.close(taskMap, retCode, msgResources.getMessage("ar.worklog.verifytaskcreated")+retCode);
            }
            catch (Exception e) {
                arLogMgr.log(LOG_AR_EXCEPTION, LOG_MINOR, taskSchedule, e);
                e.printStackTrace();
            }
        }
        }
    }

// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMHelper;
import com.marimba.apps.subscriptionmanager.webapp.util.push.DMDeploymentView;
import com.marimba.apps.subscriptionmanager.webapp.util.Utils;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.sdm.intf.simplified.IDMDeployment;
import com.marimba.apps.sdm.intf.simplified.IDMDeploymentStatus;
import com.marimba.apps.sdm.intf.simplified.IDMStatus;

import com.marimba.tools.util.Props;
import com.marimba.tools.util.Password;

import java.util.*;
import java.io.File;




/**
 * Created by IntelliJ IDEA.
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 * $File$
 *
 */
public class DMHelperUtils implements IWebAppConstants, IErrorConstants {


    /**
     * Takes the List of targets (containing string DN's) and converts them to a hash String
     * @param targets
     * @return hashCode
     */
    public static String getHashCode(java.util.List targets) {
        Collections.sort(targets, String.CASE_INSENSITIVE_ORDER);
        String targetName = targetToNameString(targets);

        String hashCode = Password.encode("md5", targetName);
        return hashCode;
    }

    public static String getHashCode(String targetString) {
        return Password.encode("md5", targetString);
    }



  /*  public  static List targetToStringList(List targets) {
        List retList = new Vector();
        for (int i = 0; i < targets.size(); i++) {
            Target tgt = (Target) targets.get(i);
            retList.add(tgt.getId());
        }
        return retList;
    } */

    public static  String targetToNameString(java.util.List targets) {
        StringBuffer sb = new StringBuffer();
        Target tgt;
        String id;
        for (int i = 0; i < targets.size(); i++) {
            tgt = (Target) targets.get(i);
            id = tgt.getId();
            id = id.toLowerCase().trim();
            sb.append(id);
            sb.append(PUSH_DELIMITER);
        }
        // remove the last PUSH_DELIMITER
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static  String targetToNameTypeString(List targets) {
        StringBuffer sb = new StringBuffer();
        Target tgt;
        String name, type;
        for (int i = 0; i < targets.size(); i++) {
            tgt = (Target) targets.get(i);
            name = tgt.getName();
            type = tgt.getType();
            sb.append(name);
            sb.append(PUSH_DELIMITER);
            sb.append(type);
            sb.append(PUSH_DELIMITER);
        }
        // remove the last "|"
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public synchronized static void updateDeploymentStatus(DMDeploymentView deploymentView, String tenantName) {

        DMHelper dm = DMHelper.getInstance(tenantName);  // get already initalized dm
        HashMap targetStatusMap = dm.getTargetStatus(deploymentView.getDeployment());
        // separate each  target based on the status
        Collection mapKeys = targetStatusMap.keySet();
        Iterator it = mapKeys.iterator();

        ArrayList successList = new ArrayList();
        ArrayList failList    = new ArrayList();
        ArrayList pendingList = new ArrayList();
        ArrayList stoppedList = new ArrayList();

        String entry;
        String value;
        while(it.hasNext()) {
            entry = (String)it.next();
            value = (String)targetStatusMap.get(entry);

            if (value.equals("Success")) {
                successList.add(entry);
            }
            if (value.equals("Failed")) {
                failList.add(entry);
            }
            if (value.equals("Pending")) {
                pendingList.add(entry);
            }
            if (value.equals("Stopped")) {
                stoppedList.add(entry);
            }

        }
        deploymentView.setSucceededTargets(successList);
        deploymentView.setFailedTargets(failList);
        deploymentView.setPendingTargets(pendingList);
        deploymentView.setStoppedTargets(stoppedList);


      }

    public static String getActualDN(Props pushConfig, String dn) {
        String retVal = dn;
        if (dn != null && dn.equals(LAST_DEPLOYMENT_ID) ) {
            if (pushConfig != null) {
                pushConfig.load();
                retVal = pushConfig.getProperty(LAST_DEPLOYMENT_DN);
            }
        }

        return retVal;
    }

    public static String getDeploymentIDFromDN(Props pushConfig, String dn) {

        String retVal = null;
        if (pushConfig != null && dn != null) {
            pushConfig.load();
            dn = dn.toLowerCase().trim();
            retVal = pushConfig.getProperty(dn);
        }
        return retVal;

    }

    public static String[] getTargetTypeNameFromDN(Props pushConfig, String dn) {
        String[] retVal = null;
        String targetTypeNameString = null;
        String actualdn = dn;
        if (pushConfig != null) {
            pushConfig.load();
            if (dn.equals(LAST_DEPLOYMENT_ID)) {
               actualdn = pushConfig.getProperty(LAST_DEPLOYMENT_DN);
            }
            if (actualdn != null) {
                targetTypeNameString = pushConfig.getProperty(actualdn + PUSH_TARGET_NAME_TYPE_SUFFIX);
                retVal =  Utils.stringToArray(targetTypeNameString, PUSH_DELIMITER, false);
            }
        }
        return retVal;
    }

    /**
     * This function takes in the file containing the pushdeployments and find out
     * if the deployment is running, done, never ran or if another deployment is currently
     * running.  This is used to decide which Update Menu to show in the UI.
     * @param file
     * @param dn
     * @return
     */
    public synchronized static String getDeploymentStateFromDN(String file, String dn, String tenantName, boolean isPushEnabled) {
        Props pushConfig = new Props(new File(file));

        String deploymentID = getDeploymentIDFromDN(pushConfig, dn);
        String lastDeploymentID = getDeploymentIDFromDN(pushConfig, LAST_DEPLOYMENT_ID);
        DMHelper dm = DMHelper.getInstance(tenantName);
        IDMDeployment lastDeployment;
        IDMDeploymentStatus lastDeploymentStatus;

        /* handle five different cases
        */
        String deploymentState = null;

        if (!isPushEnabled) {
            return "NOT_CONFIGURED";
        }

        if( (dm == null) && (isPushEnabled) ){
            return "DEFAULT";            
        }


        try {
            if (lastDeploymentID == null) {
                // there is no last deployment allow the creation of new deployment
                deploymentState = "DEFAULT";
            } else if (deploymentID == null) {
                // case where the curent target does not have previous deployments
                // check to see if the lastDeployment is done or not
                lastDeployment = dm.getDeploymentFromDeploymentID(lastDeploymentID);
                if (lastDeployment == null) {
                    deploymentState = "DEFAULT";
                } else {
                    lastDeploymentStatus = lastDeployment.getStatus();
                    if (lastDeploymentStatus != null && lastDeploymentStatus.getStatus().equals(IDMStatus.RUNNING)) {
                        deploymentState = "OTHER_UPDATING_NOLAST";
                    } else {
                        deploymentState = "DEFAULT";
                    }
                }
            } else if (deploymentID != null) {
                lastDeployment = dm.getDeploymentFromDeploymentID(lastDeploymentID);
                if (lastDeploymentID.equals(deploymentID)) {
                    if (lastDeployment != null) {
                        lastDeploymentStatus = lastDeployment.getStatus();
                        if (lastDeploymentStatus == null) {
                            deploymentState = "DEFAULT";
                        } else if (lastDeploymentStatus.getStatus().equals(IDMStatus.RUNNING)) {
                            deploymentState = "CURRENT_UPDATING";
                        } else {
                            deploymentState = "CURRENT_DONE";
                        }
                    } else {
                        deploymentState = "DEFAULT";
                    }
                } else {
                    if (lastDeployment != null) {
                        lastDeploymentStatus = lastDeployment.getStatus();
                        IDMDeployment deployment = dm.getDeploymentFromDeploymentID(deploymentID) ;
                        IDMDeploymentStatus deploymentStatus = null;;
                        if (deployment != null) {
                            deploymentStatus = deployment.getStatus();
                        }
                        if (lastDeploymentStatus != null && lastDeploymentStatus.getStatus().equals(IDMStatus.RUNNING)) {
                            // last deployment is running but make sure that the previous deployment of the
                            // the current dn has a valid deploymentStatus before showing it in the menu
                            if (deploymentStatus == null) {
                                deploymentState = "OTHER_UPDATING_NOLAST";
                            } else {
                                deploymentState = "OTHER_UPDATING";
                            }
                        } else {
                            // the last deploymentstatus is done and check to see if the current dn had any previous
                            // deployments

                            if (deployment!= null) {

                                if (deploymentStatus != null)  {
                                    deploymentState = "CURRENT_DONE";
                                } else {
                                    deploymentState = "DEFAULT";
                                }
                            } else {
                                deploymentState = "DEFAULT";
                            }
                        }
                    } else {
                        deploymentState = "DEFAULT";
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            deploymentState = "DEFAULT";
        }
        return deploymentState;

    }


}

// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.impl.file;

import java.io.*;
import java.lang.String;
import java.lang.Throwable;
import java.nio.channels.FileChannel;
import java.util.*;

import com.marimba.apps.subscriptionplugin.SecurityComplianceBean;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceConstants;
import com.marimba.apps.subscriptionplugin.intf.IDataStorage;
import com.marimba.io.FastInputStream;
import com.marimba.io.FastOutputStream;
import com.marimba.tools.util.*;

public class FileDataStorage implements ISecurityServiceConstants, IDataStorage {
    public static int DEBUG = DebugFlag.getDebug("SECURITY/PLUGIN");

	File dataFile;
    String xmlFileLocation = null;

	public FileDataStorage(String dataFileLocation) {
        xmlFileLocation = dataFileLocation;
		dataFile = new File(xmlFileLocation, PLUGIN_REQUEST_COMPLIANCE_DETAILS_LATEST);
	}

    public boolean init() {
        try {
            if (dataFile.isDirectory()) {
                return false;
            }

            new File(xmlFileLocation).mkdirs();

            return true;
            
        } catch (Throwable t) {
            return false;
        }
    }

    public synchronized boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans) {
        return insertScanDetails(securityComplianceBeans, null);
    }

    public synchronized boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine) {
        return insertScanDetails(securityComplianceBeans, null, new HashMap<String, String>());
    }

    public synchronized boolean insertScanDetails(ArrayList<SecurityComplianceBean> securityComplianceBeans, String securityComplianceForMachine, HashMap<String, String> moreInfo) {
        boolean inserted = false;
        debug("insertScanDetails() :: dataFile - " + dataFile);
        debug("insertScanDetails() :: securityComplianceBeans - " + securityComplianceBeans);
        debug("insertScanDetails() :: securityComplianceForMachine - " + securityComplianceForMachine);

        try {
            if (!dataFile.exists()) {
                debug("insertScanDetails() :: dataFile doesn't exist");
                dataFile.createNewFile();
            }
            if (dataFile.exists()) {
                debug("insertScanDetails() :: dataFile exists now...");
                for (SecurityComplianceBean securityComplianceBean : securityComplianceBeans) {
                    String contentId = securityComplianceBean.getContentId();
                    String contentFileName = securityComplianceBean.getContentFileName();
                    String contentTitle = securityComplianceBean.getContentTitle();
                    String contentTargetOS = securityComplianceBean.getContentTargetOS();
                    String profileID = securityComplianceBean.getProfileID();
                    String profileTitle = securityComplianceBean.getProfileTitle();
                    String scannedBy = securityComplianceBean.getScannedBy();
                    String targetName = securityComplianceBean.getTargetName();
                    String overallCompliance = securityComplianceBean.getOverallCompliance();
                    String individualCompliance = securityComplianceBean.getIndividualCompliance();
                    String machineName = securityComplianceBean.getMachineName();
                    long startTime = securityComplianceBean.getStartTime();
                    long finishTime = securityComplianceBean.getFinishTime();
                    long lastPolicyUpdateTime = securityComplianceBean.getLastPolicyUpdateTime();
                    HashMap<String, String> rulesCompliance = securityComplianceBean.getRulesCompliance();

                    String resultInfo = "contentId: " + contentId +
                                        ", contentFileName: " + contentFileName +
                                        ", contentTitle: " + contentTitle +
                                        ", contentTargetOS: " + contentTargetOS +
                                        ", profileID: " + profileID +
                                        ", profileTitle: " + profileTitle +
                                        ", machineName: " + machineName +
                                        ", targetName: " + targetName;
                    String uniqueId = resultInfo + ", scannedBy: ";
                    resultInfo += ", scannedBy: " + scannedBy +
                                    ", overallCompliance: " + overallCompliance +
                                    ", startTime: " + startTime +
                                    ", finishTime: " + finishTime +
                                    ", lastPolicyUpdateTime: " + lastPolicyUpdateTime;
                    for (String ruleComplianceKey: rulesCompliance.keySet()) {
                        resultInfo += ", " + ruleComplianceKey + ": " + rulesCompliance.get(ruleComplianceKey);
                    }
                    writeToFile(dataFile.getAbsolutePath(), uniqueId, resultInfo);
                }
                inserted = true;
            } else {
                debug("insertScanDetails() :: dataFile doesn't exist even now... just copy the current client file");
                inserted = false;
            }
        } catch(Throwable t) {
            t.printStackTrace();
            inserted = false;
        } finally {
        }

        return inserted;
    }

    public void removeExistingEntry(String filePath, String uniqueId) {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        File inputFile = null;
        File tempFile = null;
        try {
            inputFile = new File(filePath);
            tempFile = new File(filePath + ".tmp");

            reader = new BufferedReader(new FileReader(inputFile));
            writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.startsWith(uniqueId)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
                inputFile.delete();
                boolean successful = tempFile.renameTo(inputFile);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void writeToFile(String filePath, String uniqueId, String content) {
        BufferedWriter bufferedWriter = null;
        File file;
        try {
            removeExistingEntry(filePath, uniqueId);

            file = new File(filePath);
            Writer writer = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(writer);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            bufferedWriter.write(content);
            bufferedWriter.flush();

            bufferedWriter.newLine();
            bufferedWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally{
            sourceChannel.close();
            destChannel.close();
        }
    }

    public void debug(String msg) {
        if (DEBUG >= 5) {
            System.out.println("Security Plugin => FileDataStorage.java -- " + msg);
        }
    }

}

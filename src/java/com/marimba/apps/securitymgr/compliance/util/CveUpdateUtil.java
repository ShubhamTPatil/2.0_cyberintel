// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance.util;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.tools.config.ConfigProps;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import com.marimba.apps.subscriptionmanager.compliance.intf.ICveUpdateConstants;

/**
 *  CveUpdateUtil - Utility Class
 *  w.r.t help CVE Update Automation workflow steps
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class CveUpdateUtil  implements  ICveUpdateConstants{
    private SubscriptionMain main;
    private ConfigProps config;
    private String cveDownloderChPath;
    private IWorkspace ws;
    static final String CVE_CREATE_SQL = "create-tables.sql";
    static final String CVE_UPDATE_SQL = "update-tables.sql";
    static final String CVE_RUN_SQL = "run.sql";


    public CveUpdateUtil(SubscriptionMain main, ConfigProps config) {
        this.main = main;
        this.config = config;
    }

    public IChannel subscribeCveDownloaderChannel(String chUrl) throws Exception {
        IChannel cvedownloadCh = null;
        IApplicationContext context = (IApplicationContext) main.getFeatures().getChild("context");
        ws = (IWorkspace) context.getFeature("workspace");
        if (chUrl != null) {

            // Removed try catch
            cvedownloadCh = ws.getChannel(chUrl);
            if (cvedownloadCh == null) {
                cvedownloadCh = ws.getChannelCreate(chUrl, false, null);
            } else {
                return cvedownloadCh;
            }

        }
        return cvedownloadCh;
    }

    // Method to download cve jsoz zip file from cve site
    public boolean downloadCVEJSON(String urlStr, int timeoutMinutes, String resultFile) {
        boolean downloadFailed = true;
        System.out.println(" Timeout in minutes  - " + timeoutMinutes);
        int TIMEOUT_VALUE = timeoutMinutes * 1000 * 60;
        try {

            URL testUrl = new URL(urlStr);
            StringBuilder answer = new StringBuilder(100000);

            URLConnection testConnection = testUrl.openConnection();
            testConnection.setConnectTimeout(TIMEOUT_VALUE);
            testConnection.setReadTimeout(TIMEOUT_VALUE);
            testConnection.setRequestProperty("http.protocol.single-cookie-header", "true");
            testConnection.connect();

            InputStream ip = null;

            try {
                ip = testConnection.getInputStream();
                copyFile(ip, resultFile);
                downloadFailed = false;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                downloadFailed = true;
            }
        } catch (SocketTimeoutException ste) {
            ste.printStackTrace();
            downloadFailed = true;
            System.out.println("More than " + TIMEOUT_VALUE + " elapsed.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return downloadFailed;
    }

    private void copyFile(InputStream source, String fileName) throws IOException {
        byte[] buff = new byte[1024];
        int len = -1;
        OutputStream out = null;
        String rootDir = config.getProperty("defensight.cvejson.storagedir.location");
        try {
            File fileDir = new File(rootDir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            File fos = new File(rootDir, fileName);

            // open the outstream to the file and creates the parent directories if
            // necessary
            out = new FileOutputStream(fos);

            // copy from the source to the destination
            while ((len = source.read(buff)) > 0) {
                out.write(buff, 0, len);
            }
        } finally {
            source.close();
            if (out != null) {
                out.close();
            }
        }
    }

    public boolean generateCveJsonToCsvFiles(String runChannelDir, String cveDownloderChUrl) {
        boolean resultCLI = true;
        Vector args = new Vector();
        args.addElement("runchannel.exe");
        args.addElement(cveDownloderChUrl);
        args.addElement("-prepareCsv2");

        String cmdLineStr = Tools.getCommandline(args);
        // System.out.println("DebugInfo: CVE-Downloader CLI String ==> " + cmdLineStr);
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", cmdLineStr)
                    .directory(new File(runChannelDir));

            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                if (line != null && line.indexOf("Total time taken") != -1) {
                    resultCLI = true;
                }
                System.out.println(" Command execution result :: " + line);
            }

            int exitCode = process.waitFor();
            System.out.println("CLI Process Exited with code : " + exitCode);

        } catch (Exception ex) {
            resultCLI = false;
            ex.printStackTrace();
        }
        return resultCLI;
    }

    public String fetchCVEDownloaderChannelUrl(IApplicationContext context) throws Exception {
        String cveDownloaderChUrl = "";
        File file = new File(context.getDataDirectory());
        String s = file.getParent();
        String s1 = s.substring(0, s.indexOf("ch."));
        String s2 = (new StringBuilder()).append(s1).append("map.txt").toString();

        // Removed Try Catch
        BufferedReader bufferedreader = new BufferedReader(new FileReader(new File(s2)));

        String s3;
        do {
            s3 = bufferedreader.readLine();
            System.out.println("s3 = " + s3);
            if (s3 == null)
                break;
        } while (!s3.contains("CVEDownloader"));

        if (!isNull(s3)) {
            String s4 = s3.substring(0, s3.indexOf("="));
            String s5 = s3.substring(s3.indexOf("=") + 1, s3.length());
            String s6 = (new StringBuilder()).append(s1).append("\\").append(s5).append("\\data\\cve-save-csv")
                    .toString();
            cveDownloderChPath = s6;
            System.out.println("DebugInfo: FilePath of CVE Downloader channel path: " + cveDownloderChPath);
            cveDownloaderChUrl = s4;
        }

        return cveDownloaderChUrl;
    }

    private boolean isNull(String s) {
        return s == null || s.trim().length() == 0;
    }

    public boolean makeCveSchemaSqlScripts(File scriptFilesDir) {
        boolean fileStatus = true;
        try {
            /*
            File createScriptFile = new File(scriptFilesDir, CVE_CREATE_SQL);
            FileOutputStream fout = new FileOutputStream(createScriptFile);
            String scriptContent = CREATE_TABLE_SQL;
            byte[] bytesArray = scriptContent.getBytes();
            fout.write(bytesArray);
            fout.flush();
            fout.close();
            System.out.println(CVE_CREATE_SQL + " - File written successfully... ");
            */
            File updateScriptFile = new File(scriptFilesDir, CVE_UPDATE_SQL);
            FileOutputStream fout = new FileOutputStream(updateScriptFile);
            String scriptContent = UPDATE_TABLE_SQL;
            byte[] bytesArray = scriptContent.getBytes();
            fout.write(bytesArray);
            fout.flush();
            fout.close();
            System.out.println(UPDATE_TABLE_SQL + " - File written successfully... ");

        } catch (Exception ex) {
            fileStatus = false;
            ex.printStackTrace();
            System.out.println("Failed to create cve setup sql script files: " + ex.getMessage());
        }
        return fileStatus;
    }

    // To update bulk insertion sql script for csv files
    public void updateScriptFile(File scriptDir, String csvDirPath, int csvFilesCnt) {
        try {

            String dataSourceName = main.getProperty("subscriptionmanager.db.name");

            File runSqlFile = new File(scriptDir, "run.sql");
            BufferedWriter bout = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(runSqlFile), "UTF-8"));

            String permString = "Use master;";
            bout.write(permString);
            bout.newLine();
            bout.write("GRANT ADMINISTER BULK OPERATIONS TO inventory;");
            bout.newLine();
            permString = "Use " + dataSourceName + ";";
            bout.write(permString);
            bout.newLine();

            String scriptContent = "BULK INSERT vendor_info FROM '" + csvDirPath
                    + "\\csv-vendor-0.csv' WITH (FIELDTERMINATOR = ',', ROWTERMINATOR = '\\n');";
            bout.write(scriptContent);
            csvFilesCnt--;

            bout.newLine();
            scriptContent = "BULK INSERT product_INFO FROM '" + csvDirPath
                    + "\\csv-product-0.csv' WITH (FIELDTERMINATOR = ',', ROWTERMINATOR = '\\n');";
            bout.write(scriptContent);
            csvFilesCnt--;
            bout.newLine();

            for (int idx = 0; idx < csvFilesCnt; idx++) {
                scriptContent = "BULK INSERT product_cve_info FROM '" + csvDirPath + "\\csv-product-cve-info-" + idx
                        + ".csv' WITH (FIELDTERMINATOR = ',', ROWTERMINATOR = '\\n');";
                bout.write(scriptContent);
                bout.newLine();
            }

            permString = "Use master;";
            bout.write(permString);
            bout.newLine();
            bout.write("REVOKE ADMINISTER BULK OPERATIONS FROM inventory;");
            bout.newLine();
            permString = "Use " + dataSourceName + ";";
            bout.write(permString);
            bout.newLine();

            bout.flush();
            bout.close();
            System.out.println("SQL script <run.sql> file updated successfully... ");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}


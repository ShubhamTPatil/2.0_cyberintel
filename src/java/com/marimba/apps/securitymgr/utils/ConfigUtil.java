package com.marimba.apps.securitymgr.utils;

import java.util.StringTokenizer;
import java.io.File;
import com.marimba.tools.util.Password;
import com.marimba.webapps.common.Resources;
/**
 * Created with IntelliJ IDEA.
 * User: ingsantha
 * Date: 3/28/17
 * Time: 1:46 PM
 * To change this template use File | Settings | File Templates.
 */

public class ConfigUtil {


    private static String DRIVER_STRING;
    private static String DRIVER_URL;
    private static String DATABASE_TYPE;
    private static boolean NETSERVICE_ENABLED;

    private static final String ORACLE_DATABASE_TYPE = "oracle";
    private static final String ORACLERAC_DATABASE_TYPE = "oracle_rac";
    private static final String SQLSERVER_DATABASE_TYPE = "sqlserver";

    private static String PLUGIN_VERSION;

    private static  Resources drivers = new Resources("driver.properties");
    private static  Resources builderQueryXml = new Resources();


    final static int MEGA = 1024 * 1024;
    final static String BASE64 = "base64:";

    public ConfigUtil() {

    }

    /**
     * This method will trim of suffixes that are prepended with an underscore (such as _beta)
     */
    private static String trimVersionStrToken(String str) {
        if (str != null) {
            int index = str.indexOf("_");
            if (index != -1) {
                str = str.substring(0, index);
            }
        }
        return str;
    }

    /**
     * check if a password has been encoded
     */
    public static boolean isEncoded(String passwd) {
        return (passwd != null && passwd.startsWith("base64:"));
    }

    /**
     * prepend "base64" to encoded passwords to tell if a password has been encoded
     */
    public static String encode(String passwd) {
        return BASE64 + Password.encode(passwd);
    }

    /**
     * form the masterAuth with subUsr and subPwd
     */
    public static String formMasterAuth(String subUsr, String subPwd) {
        return "Basic " + Password.encode(subUsr + ":" + subPwd);
    }

    /**
     * parse the masterAuth to get subUsr and subPwd
     */



    /**
     * parse the database url to get the database host, port, and sid
     */
    public static String [] parseDbUrl(String url, boolean isNetServiceEnabled) {
        String [] result = new String[4];
        String key;
        String value;
        String match;
        int index;
        int vlen;
        StringBuffer genericUrl = new StringBuffer();
        StringBuffer realUrl = new StringBuffer();
        realUrl.append(url);

        int totalSqlServerURLS = Integer.parseInt(drivers.getString("sqlserver.url.types"));
        int totalOracleServerURLS =  Integer.parseInt(drivers.getString("oracle.url.types"));

    /*
    Commenting ipv6 change.
    int startIdx = realUrl.indexOf("[");
    int endIdx = realUrl.indexOf("]");

    if (startIdx != -1 && endIdx != -1) {
        String temp = realUrl.substring(startIdx, endIdx);
        temp = temp.replaceAll (":", "||");
        realUrl.replace(startIdx, endIdx, temp);
    }*/
        findDB: {
            String findDBURL = "";
            int findDBURLIndex;
            for (int i=1; i<=totalSqlServerURLS; i++) {
                findDBURL = drivers.getString("sqlserver" + i + ".driver.url");
                findDBURLIndex = findDBURL.indexOf('<');
                if(url.startsWith(findDBURL.substring(0,findDBURLIndex))) {
                    genericUrl.append(drivers.getString("sqlserver" + i + ".driver.url"));
                    DATABASE_TYPE = SQLSERVER_DATABASE_TYPE;
                    break findDB;
                }
            }
            for (int i=1; i<=totalOracleServerURLS; i++) {
                drivers.getString("oracle" + i + ".driver.url");
                findDBURL = isNetServiceEnabled ? drivers.getString("oracle" + i + ".driver.servicename.url"):drivers.getString("oracle" + i + ".driver.url");
                findDBURLIndex = findDBURL.indexOf('<');
                findDBURLIndex = findDBURL.indexOf('<');
                if(url.startsWith(findDBURL.substring(0,findDBURLIndex))) {
                    genericUrl.append(isNetServiceEnabled ? drivers.getString("oracle" + i + ".driver.servicename.url"):drivers.getString("oracle" + i + ".driver.url"));
                    DATABASE_TYPE = ORACLE_DATABASE_TYPE;
                    break findDB;
                }
            }
        }

        index = genericUrl.indexOf("<");
        match = genericUrl.substring(0,index);
        while (true) {
            if(match.equalsIgnoreCase(realUrl.substring(0,index))) {
                genericUrl.delete(0,(index+1));
                realUrl.delete(0,index);
                index = genericUrl.indexOf(">");
                key = genericUrl.substring(0,index);
                genericUrl.delete(0,(index+1));
                index = genericUrl.indexOf("<");
                if(index == -1)
                {
                    value = realUrl.toString();
                    if("databaseHost".equalsIgnoreCase(key)) {
                        //value = value.replace("||", ":");
                        result[0] = value;
                    } else if("databasePort".equalsIgnoreCase(key)) {
                        result[1] = value;
                    } else if("databaseName".equalsIgnoreCase(key)) {
                        result[2] = value;
                    }
                    break;
                }
                match = genericUrl.substring(0,index);
                vlen = realUrl.indexOf(match);
                value = realUrl.substring(0,vlen);
                realUrl.delete(0,vlen);
                if("databaseHost".equalsIgnoreCase(key)) {
                    //value = value.replace("||", ":");
                    result[0] = value;
                } else if("databasePort".equalsIgnoreCase(key)) {
                    result[1] = value;
                } else if("databaseName".equalsIgnoreCase(key)) {
                    result[2] = value;
                }
            } else {
                break;
            }
        }

        result[3] = DATABASE_TYPE;
        return result;
    }

    public static String checkNull(String value,String bool) {
        if (bool.equals("boolean")) {
            return (value == null) ? "false" : value;
        } else {
            return (value == null) ? "" : value;
        }
    }

    // Get the plugin driver string and driver url from Report Center "driver.properties".
    public static void setPluginDBDetailsDynamically(String dbType, String host, String port, String sid) {
        StringBuffer buf = new StringBuffer();
        // The plugin version will be null only during the transmitter is OFF. So in that case there is no need
        // to identify or set database driver string and driver url
        if(PLUGIN_VERSION != null) {
            int urlversion  = checkPlugin(PLUGIN_VERSION, dbType);
            String hostStr = "<databaseHost>";
            String portStr = "<databasePort>";
            String sidStr = "<databaseName>";
            if (host != null && port != null && sid != null) {
                if (ORACLE_DATABASE_TYPE.equals(dbType)) {
                    if (getNETSERVICE_ENABLED()) {
                        buf.append(drivers.getString(ORACLE_DATABASE_TYPE + urlversion + ".driver.servicename.url"));
                    } else {
                        buf.append(drivers.getString(ORACLE_DATABASE_TYPE + urlversion + ".driver.url"));
                    }
                    DRIVER_STRING = drivers.getString(ORACLE_DATABASE_TYPE + urlversion + ".driver.string");
                    DATABASE_TYPE = ORACLE_DATABASE_TYPE;
                } else if (SQLSERVER_DATABASE_TYPE.equals(dbType)) {
                    buf.append(drivers.getString(SQLSERVER_DATABASE_TYPE + urlversion + ".driver.url"));
                    DRIVER_STRING = drivers.getString(SQLSERVER_DATABASE_TYPE + urlversion + ".driver.string");
                    DATABASE_TYPE = SQLSERVER_DATABASE_TYPE;
                }
                int index = buf.indexOf(hostStr);
                buf.replace(index,(index+hostStr.length()),host);
                index = buf.indexOf(portStr);
                buf.replace(index,(index+portStr.length()),port);
                index = buf.indexOf(sidStr);
                buf.replace(index,(index+sidStr.length()),sid);
                DRIVER_URL = buf.toString();
            }
        }
    }

    public synchronized static String getOracleRACDriverURL (String racAddrList, String racNetserviceName, String racServerType,
                                                             String ralLoadBal, String racFailoverType, String racFailoverMthd, String racFailoverRetry, String racFailoverDelay) {
        StringBuffer buf = new StringBuffer();
        String addressList = "<addresslist>" ;
        String loadbalance = "<load_balance>";
        String serverType = "<server_type>";
        String serviceName = "<service_name>";
        String failoverType ="<failover_type>";
        String failoverMethod = "<failover_method>";
        String failoverRetires = "<failover_retries>";
        String failoverDelay = "<failover_delay>";
        String protocol = "TCP";

        if (racNetserviceName != null && racAddrList != null) {

            buf.append(drivers.getString("oracle.driver.rac.url"));

            //Replace addresslist
            StringBuffer addBuf = new StringBuffer();
            StringTokenizer addTkn = new StringTokenizer(racAddrList, ";");
            while(addTkn.hasMoreTokens()) {
                String address = addTkn.nextToken().trim();
                String host = address.substring(0, address.indexOf(":")).trim();
                String port = address.substring(address.indexOf(":") + 1).trim();
                addBuf.append("(ADDRESS = ");
                addBuf.append("(PROTOCOL = "+ protocol+ ")");
                addBuf.append("(HOST = "+ host+ ")");
                addBuf.append("(PORT = "+port+"))");
            }
            int index = buf.indexOf(addressList);
            buf.replace(index, (index + addressList.toString().length()), addBuf.toString());

            //Replace Loadbalance
            String loadbalanceValue = "ON";
            if("OFF".equals(ralLoadBal)) {
                loadbalanceValue = "OFF";
            }
            index = buf.indexOf(loadbalance);
            buf.replace(index, (index + loadbalance.length()), loadbalanceValue);

            //Replace Server Type
            String serverTypeValue = "SHARED";
            if("DEDICATED".equals(racServerType)) {
                serverTypeValue = "DEDICATED";
            }
            index = buf.indexOf(serverType);
            buf.replace(index, (index + serverType.length()), serverTypeValue);

            //Replace Net ServiceName
            index = buf.indexOf(serviceName);
            buf.replace(index, (index + serviceName.length()), racNetserviceName);

            //Replace Failover Type
            String failoverTypeValue = "SELECT";
            if("SESSION".equals(racFailoverType)) {
                failoverTypeValue = "SESSION";
            }
            index = buf.indexOf(failoverType);
            buf.replace(index, (index + failoverType.length()), failoverTypeValue);

            //Replace Failover Method
            String failoverMethodValue = "BASIC";
            if("PRECONNECT".equals(racFailoverMthd)) {
                failoverMethodValue = "PRECONNECT";
            }
            index = buf.indexOf(failoverMethod);
            buf.replace(index, (index + failoverMethod.length()), failoverMethodValue);

            //Replace Failover Retryies value
            int failoverRetryValue = 20;
            if(null != racFailoverRetry) {
                try {
                    failoverRetryValue = Integer.parseInt(racFailoverRetry);
                } catch (Exception ex) {
                    //Ignore
                }
            }
            index = buf.indexOf(failoverRetires);
            buf.replace(index, (index + failoverRetires.length()), failoverRetryValue+"");

            //Replace Failover Delay value
            int failoverDelayValue = 10;
            if(null != racFailoverDelay) {
                try {
                    failoverDelayValue = Integer.parseInt(racFailoverDelay);
                } catch (Exception ex) {
                    //Ignore
                }
            }
            index = buf.indexOf(failoverDelay);
            buf.replace(index, (index + failoverDelay.length()), failoverDelayValue+"");
        }
        return buf.toString();
    }

    public static String getOracleRACDriverClass() {
        return drivers.getString("oracle.rac.driver.string");
    }

    // Get the plugin driver string and driver url for
    // Oracle-RAC from Report Center "driver.properties".
    public static void setPluginOracleRacUrlDynamically(String racAddrList, String racNetserviceName, String racServerType,
                                                        String ralLoadBal, String racFailoverType, String racFailoverMthd, String racFailoverRetry, String racFailoverDelay) {
        DRIVER_STRING = drivers.getString("oracle.rac.driver.string");
        DATABASE_TYPE = ORACLERAC_DATABASE_TYPE;

        StringBuffer buf = new StringBuffer();
        String addressList = "<addresslist>" ;
        String loadbalance = "<load_balance>";
        String serverType = "<server_type>";
        String serviceName = "<service_name>";
        String failoverType ="<failover_type>";
        String failoverMethod = "<failover_method>";
        String failoverRetires = "<failover_retries>";
        String failoverDelay = "<failover_delay>";
        String protocol = "TCP";

        if (racNetserviceName != null && racAddrList != null) {

            buf.append(drivers.getString("oracle.driver.rac.url"));

            //Replace addresslist
            StringBuffer addBuf = new StringBuffer();
            StringTokenizer addTkn = new StringTokenizer(racAddrList, ";");
            while(addTkn.hasMoreTokens()) {
                String address = addTkn.nextToken().trim();
                String host = address.substring(0, address.indexOf(":")).trim();
                String port = address.substring(address.indexOf(":") + 1).trim();
                addBuf.append("(ADDRESS = ");
                addBuf.append("(PROTOCOL = "+ protocol+ ")");
                addBuf.append("(HOST = "+ host+ ")");
                addBuf.append("(PORT = "+port+"))");
            }
            int index = buf.indexOf(addressList);
            buf.replace(index, (index + addressList.toString().length()), addBuf.toString());

            //Replace Loadbalance
            String loadbalanceValue = "ON";
            if("OFF".equals(ralLoadBal)) {
                loadbalanceValue = "OFF";
            }
            index = buf.indexOf(loadbalance);
            buf.replace(index, (index + loadbalance.length()), loadbalanceValue);

            //Replace Server Type
            String serverTypeValue = "SHARED";
            if("DEDICATED".equals(racServerType)) {
                serverTypeValue = "DEDICATED";
            }
            index = buf.indexOf(serverType);
            buf.replace(index, (index + serverType.length()), serverTypeValue);

            //Replace Net ServiceName
            index = buf.indexOf(serviceName);
            buf.replace(index, (index + serviceName.length()), racNetserviceName);

            //Replace Failover Type
            String failoverTypeValue = "SELECT";
            if("SESSION".equals(racFailoverType)) {
                failoverTypeValue = "SESSION";
            }
            index = buf.indexOf(failoverType);
            buf.replace(index, (index + failoverType.length()), failoverTypeValue);

            //Replace Failover Method
            String failoverMethodValue = "BASIC";
            if("PRECONNECT".equals(racFailoverMthd)) {
                failoverMethodValue = "PRECONNECT";
            }
            index = buf.indexOf(failoverMethod);
            buf.replace(index, (index + failoverMethod.length()), failoverMethodValue);

            //Replace Failover Retryies value
            int failoverRetryValue = 20;
            if(null != racFailoverRetry) {
                try {
                    failoverRetryValue = Integer.parseInt(racFailoverRetry);
                } catch (Exception ex) {
                    //Ignore
                }
            }
            index = buf.indexOf(failoverRetires);
            buf.replace(index, (index + failoverRetires.length()), failoverRetryValue+"");

            //Replace Failover Delay value
            int failoverDelayValue = 10;
            if(null != racFailoverDelay) {
                try {
                    failoverDelayValue = Integer.parseInt(racFailoverDelay);
                } catch (Exception ex) {
                    //Ignore
                }
            }
            index = buf.indexOf(failoverDelay);
            buf.replace(index, (index + failoverDelay.length()), failoverDelayValue+"");
        }
        DRIVER_URL = buf.toString();
    }


    //Check the plugin url integer. For RC7.5.00, it has 2 sqlserver urls and 1 oracle driver
    public static int checkPlugin(String pluginVersion, String dbType) {
        int urllimit = Integer.parseInt(drivers.getString(dbType + ".url.types").trim());

        for(int urlstart=1; urlstart <=urllimit; urlstart++) {
            String PluginVersionFromRC = drivers.getString(dbType + ".url"+urlstart+".upto.version");
            if(string2Number(pluginVersion) <= string2Number(PluginVersionFromRC)){
                return urlstart;
            }
        }
        return urllimit;
    }

    //Return the PluginVersion excluding .
    public static int string2Number(String pluginVersion){
        StringBuffer pluginbuffer = new StringBuffer();
        StringTokenizer ss = new StringTokenizer(pluginVersion,".");
        while (ss.hasMoreTokens()) {
            pluginbuffer.append(trimVersionStrToken(ss.nextToken()));
        }
        //To make the version comparison for 4 digits
        String pluginoriginal = pluginbuffer.length()>=4?pluginbuffer.substring(0,4):pluginbuffer.toString();
        return Integer.parseInt(pluginoriginal.trim());
    }

    //Return the true if given string is directory.
    public static boolean isDir(String dir){
        File tempDir = new File(dir);
        return tempDir.isDirectory();
    }

    public static void setPLUGIN_VERSION(String PLUGIN_VERSION) {
        ConfigUtil.PLUGIN_VERSION = PLUGIN_VERSION;
    }

    public static void  setNETSERVICE_ENABLED(boolean NETSERVICE_ENABLED) {
        ConfigUtil.NETSERVICE_ENABLED = NETSERVICE_ENABLED;
    }

    public synchronized static String getDRIVER_URL(String dbType, String host, String port, String sid, boolean istNETSERVICE_ENABLED) {
        StringBuffer buf = new StringBuffer();
        // The plugin version will be null only during the transmitter is OFF. So in that case there is no need
        // to identify or set database driver string and driver url
        String hostStr = "<databaseHost>";
        String portStr = "<databasePort>";
        String sidStr = "<databaseName>";
        if (host != null && port != null && sid != null) {
            if (ORACLE_DATABASE_TYPE.equals(dbType)) {
                if (istNETSERVICE_ENABLED) {
                    buf.append(drivers.getString(ORACLE_DATABASE_TYPE + 1 + ".driver.servicename.url"));
                } else {
                    buf.append(drivers.getString(ORACLE_DATABASE_TYPE + 1 + ".driver.url"));
                }
            } else if (SQLSERVER_DATABASE_TYPE.equals(dbType)) {
                buf.append(drivers.getString(SQLSERVER_DATABASE_TYPE + 1 + ".driver.url"));
            }
            int index = buf.indexOf(hostStr);
            buf.replace(index,(index+hostStr.length()),host);
            index = buf.indexOf(portStr);
            buf.replace(index,(index+portStr.length()),port);
            index = buf.indexOf(sidStr);
            buf.replace(index,(index+sidStr.length()),sid);
        }
        return buf.toString();
    }

    public synchronized static String getDRIVER_STRING(String dbType) {
        if (ORACLE_DATABASE_TYPE.equals(dbType)) {
            return DRIVER_STRING = drivers.getString(ORACLE_DATABASE_TYPE + "1" + ".driver.string");
        } else {
            return drivers.getString(SQLSERVER_DATABASE_TYPE + "1" + ".driver.string");
        }
    }

    public static boolean getNETSERVICE_ENABLED() {
        return NETSERVICE_ENABLED;
    }

    public static String getDATABASE_TYPE() {
        return DATABASE_TYPE;
    }

    public static String getORACLE_DATABASE_TYPE() {
        return ORACLE_DATABASE_TYPE;
    }

    public static String getORACLERAC_DATABASE_TYPE() {
        return ORACLERAC_DATABASE_TYPE;
    }

    public static String getSQLSERVER_DATABASE_TYPE() {
        return SQLSERVER_DATABASE_TYPE;
    }
}


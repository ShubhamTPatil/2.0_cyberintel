// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.compliance.core;

/**
 * Manager compliance related configuration
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
import java.io.File;
import java.util.HashMap;
import com.marimba.tools.config.*;

import com.marimba.apps.subscriptionmanager.compliance.view.ConfigBean;
import com.marimba.apps.subscriptionmanager.ObjectManager;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.msf.acl.IAclMgr;
import com.marimba.intf.msf.*;

public class ConfigManager {

    // Cache information for list results (in seconds)
    public final static String CFG_CACHE_LIST_MAX = "cache.list.max";
    public final static String CFG_CACHE_LIST_EXT = "cache.list.ext";

    // Cache information for other results (in seconds)
    public final static String CFG_CACHE_OBJ_MAX = "cache.obj.max";
    public final static String CFG_CACHE_OBJ_EXT = "cache.obj.ext";

    // Maximum number of items to fetch (use RC when there is more)
    public final static String CFG_LIST_MAX = "list.max";

    // Default wait time for queries
    public final static String CFG_QUERY_WAIT = "query.wait";

    // Last check in limit (in hours)
    public final static String CFG_CHECKIN_LIMIT = "checkin.limit";

    // Enable/Disable the collection of compliance data
    public final static String COLLECT_INVENTORY_DATA = ISubscriptionConstants.COLLECT_INVENTORY_DATA;

    ComplianceMain main;
    HashMap map;
    ConfigProps config;

    public ConfigManager(ComplianceMain main) {
        this.main = main;
        this.map = new HashMap();
        this.config = null;
    }

    boolean isInteger(String v) {
        try {
            Integer.parseInt(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean setConfig( ConfigBean cb, IUser user )throws SystemException {
	    // Populate with the current value
	    setString(CFG_CACHE_LIST_MAX, cb.getCacheListMax());
	    setString(CFG_CACHE_LIST_EXT, cb.getCacheListExt());
	    setString(CFG_CACHE_OBJ_MAX, cb.getCacheObjMax());
	    setString(CFG_CACHE_OBJ_EXT, cb.getCacheObjExt());
	    setString(CFG_QUERY_WAIT, cb.getWaitTime());
	    setString(CFG_CHECKIN_LIMIT, cb.getCheckInLimit());
        setString(COLLECT_INVENTORY_DATA, cb.getCollectCompEnabled() );
	    // Save the config
	    return save( user );
    }

    public void populate(ConfigBean cb, IUser user )throws SystemException {
	    if (config != null) {
            cb.setListMax(config.getProperty(CFG_LIST_MAX));
	        cb.setCacheListMax(config.getProperty(CFG_CACHE_LIST_MAX));
	        cb.setCacheListExt(config.getProperty(CFG_CACHE_LIST_EXT));
	        cb.setCacheObjMax(config.getProperty(CFG_CACHE_OBJ_MAX));
	        cb.setCacheObjExt(config.getProperty(CFG_CACHE_OBJ_EXT));
	        cb.setWaitTime(config.getProperty(CFG_QUERY_WAIT));
	        cb.setCheckinLimit(config.getProperty(CFG_CHECKIN_LIMIT));
            cb.setCollectCompEnabled( isCollCompEnabled( user )  );
	    } else {
            cb.setListMax("" + map.get(CFG_LIST_MAX));
	        cb.setCacheListMax("" + map.get(CFG_CACHE_LIST_MAX));
	        cb.setCacheListExt("" + map.get(CFG_CACHE_LIST_EXT));
	        cb.setCacheObjMax("" + map.get(CFG_CACHE_OBJ_MAX));
	        cb.setCacheObjExt("" + map.get(CFG_CACHE_OBJ_EXT));
	        cb.setWaitTime("" + map.get(CFG_QUERY_WAIT));
	        cb.setCheckinLimit("" + map.get(CFG_CHECKIN_LIMIT));
            cb.setCollectCompEnabled(  ""+map.get( COLLECT_INVENTORY_DATA ) );
	    }
    }

    void init() {
	    File rootDir = main.getRootDir();
	    if (rootDir != null && rootDir.isDirectory()) {
	        File configFile = new File(rootDir, "comp_config.txt");
	        try {
		        if (!configFile.exists()) {
		            config = new ConfigProps(configFile);
                    config.setProperty(CFG_LIST_MAX, (new Integer(500)).toString());
		            config.setProperty(CFG_CACHE_LIST_MAX, (new Integer(10 * 60)).toString());
                    config.setProperty(CFG_CACHE_LIST_EXT, (new Integer(60)).toString());
		            config.setProperty(CFG_CACHE_OBJ_MAX, (new Integer(30 * 60)).toString());
		            config.setProperty(CFG_CACHE_OBJ_EXT, (new Integer(10 * 60)).toString());
		            config.setProperty(CFG_QUERY_WAIT, (new Integer(60 * 60)).toString());
		            config.setProperty(CFG_CHECKIN_LIMIT, (new Integer(-48)).toString());
                    config.setProperty(COLLECT_INVENTORY_DATA, "disable" );
                    if (!config.save()) {
			            throw new Exception("Failed to save init configurations");
		            }
		        } else {
		            config = new ConfigProps(configFile);
		        }
	        } catch (Exception ioe) {
		        ioe.printStackTrace();
		        // REMIND, log something here
		        config = null;
	        }
	    } else {
	        // REMIND, log something here
            map.put(CFG_LIST_MAX, new Integer(500));
	        map.put(CFG_CACHE_LIST_MAX, new Integer(10 * 60));
	        map.put(CFG_CACHE_LIST_EXT, new Integer(60));
	        map.put(CFG_CACHE_OBJ_MAX, new Integer(30 * 60));
	        map.put(CFG_CACHE_OBJ_EXT, new Integer(10 * 60));
	        map.put(CFG_QUERY_WAIT, new Integer(60 * 60));
	        map.put(CFG_CHECKIN_LIMIT, new Integer(-48));
            map.put(COLLECT_INVENTORY_DATA, "disable" );
	    }
    }

    public void setInt(String key, int val) {
        if (config == null) {
            map.put(key, new Integer(val));
        } else {
            config.setProperty(key, String.valueOf(val));
        }
    }

    public int getInt(String key) {
	    return getInt(key, 0);
    }

    public int getInt(String key, int def) {
        if (config == null) {
            Object obj = map.get(key);
            if (obj != null && obj instanceof Integer) {
                return ((Integer) obj).intValue();
            } else {
                return def;
            }
        } else {
            try {
                return Integer.parseInt(config.getProperty(key));
            } catch (Exception e) {
                e.printStackTrace();
                return def;
            }
        }
    }

    public void setString(String key, String val) {
	    if (val != null) {
	        if (config == null) {
		        map.put(key, val);
	        } else {
		        config.setProperty(key, val);
	        }
	    }
    }

    public String getString(String key) {
	    if (config == null) {
	        return (String) map.get(key);
	    } else {
	        return config.getProperty(key);
	    }
    }

    public boolean save( IUser user )throws SystemException {
        saveCollectingComplianceData( user, config.getProperty( COLLECT_INVENTORY_DATA ) );
	    return config.save();
    }

    /*
     * isCollCompEnabled() used to check whether the property
     * "marimba.subscription.inventory.compliance" is enabled in all_all policy
     */
    String isCollCompEnabled( IUser user )throws SystemException{
    	IAclMgr iAclMgr = main.getTenant().getAclMgr();
        String retval = "disable";
        ISubscription sub = ObjectManager.openSubForRead( iAclMgr.getAllAllDn(), ISubscriptionConstants.TYPE_ALL, user);
        String shouldCollectData = sub.getProperty( ISubscriptionConstants.PROP_TUNER_KEYWORD, COLLECT_INVENTORY_DATA );
        if ("true".equalsIgnoreCase(shouldCollectData)){
            retval = "enable";
        }
        return retval;
    }

    /*
     * saveCollectingComplianceData() used to save the property
     * "marimba.subscription.inventory.compliance" in all_all policy
     */
    void saveCollectingComplianceData( IUser  user, String collect ) throws SystemException {
    	IAclMgr iAclMgr = main.getTenant().getAclMgr();
        String enabled = "false";
        ISubscription sub = ObjectManager.openSubForWrite( iAclMgr.getAllAllDn(), ISubscriptionConstants.TYPE_ALL, user );
        if ("enable".equalsIgnoreCase(collect)){
            enabled = "true";
        }
        sub.setProperty( ISubscriptionConstants.PROP_TUNER_KEYWORD, COLLECT_INVENTORY_DATA, enabled );
        sub.save();
    }
}

//Copyright 1996-2014, BMC Software Inc. All Rights Reserved.
//Confidential and Proprietary Information of BMC Software Inc.
//Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
//6,381,631, and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.apps.subscriptionmanager.webapp.util.FileUtils;
import com.marimba.webapps.tools.util.WebAppUtils;
import com.marimba.intf.msf.*;
import com.marimba.intf.util.IDirectory;
import com.marimba.intf.msf.AppManagerConstants.*;

import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.Props;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
/**
 * Tenant Helper
 *
 * @author	Selvaraj Jegatheesan
 * @version 	$Revision$, $Date$
 */
public class TenantHelper {
	private static IDirectory features;
	private static ITenantManager tenantMgr;
	private static IServer server;
	public TenantHelper() {
		
	}
	public static synchronized ITenant getTenantObject(ITenantManager tenantMananger, String tenantName) {
		tenantMgr = tenantMananger;
		ITenant tenant = tenantMgr.getTenant(tenantName);
		return tenant;
	}
	public static synchronized String getTenantName(HttpServletRequest request) {
		IUserPrincipal tenantUser = (IUserPrincipal) request.getUserPrincipal();
		return tenantUser.getTenantName();
	}
	
	/**
     * This method one time calling based on property value
     * upgrade from lower 8.5 to 8.5 and above
     */
    public static void migrateTenantModel(File dir, String tenantName) {
    	// marimba.subscription.migratetenantmodel=true
    	if(tenantName.equals("Default-Tenant")) {
    		System.out.println("vDesk settings migrate to Tenant Model...");
    		String srcDir = dir.getAbsolutePath();

    		// This destination path get from IWebApplicationMgr
    		String destinationPath =  srcDir + File.separator + tenantName;
    		
    		System.out.println("Source Path :" + srcDir);
    		System.out.println("Destination Path :" + destinationPath);

    		try {
    			File srcFile = new File(srcDir);
    			File destFile = new File(destinationPath);
    			FileUtils.moveDirectory(srcFile, destFile, false);
    		} catch(Exception ec) {
    			ec.printStackTrace();
    		}
    	}
    }
    private static String getChannelDataDir(String srcDir) {
    	String channelDir = null;
    	int index = srcDir.indexOf("\\data");
		if (index > 0) {
			channelDir = srcDir.substring(0, index);
		}
		return channelDir;
    }
    public static synchronized File getTenantDataFolder(File dir, String tenantName) {
    	String srcDir = dir.getAbsolutePath();
		srcDir = getChannelDataDir(srcDir);
    	System.out.println("source dir :" + srcDir);
    	String destinationPath = null;
    	if(tenantName.equals("Default-Tenant")) {
    		// it works before 8.5 behavior
    		destinationPath =  srcDir + "\\data" + File.separator  + "persist";
    	} else {
    		destinationPath =  srcDir + "\\data" + File.separator  + tenantName + "\\persist";
    	}
		
		return new File(destinationPath);
    }

	public static boolean isRequiredTenantModel(File directory, String fileName) {
		boolean isRequired = false;
		int index = directory.getAbsolutePath().indexOf("\\data");
		if (index > 0) {
			String channelDir = directory.getAbsolutePath().substring(0, index);
			System.out.println("Channel Directory :" + channelDir);
			ConfigProps channelProps = null;
			try {
				// File file = new File(channelDir);
				File channelFile = new File(channelDir, fileName);
				channelProps = new ConfigProps(channelFile);

				// channelProps.load();
				String enableMigrate = channelProps.getProperty("marimba.subscription.migratetenantmodel");
				System.out.println("Enable Migrate Tenant Model : "	+ enableMigrate);
				// Migrate tenant model if property not exists or property value
				// as true
				if ((null == enableMigrate)	|| (null != enableMigrate && enableMigrate.equalsIgnoreCase("true"))) {
					isRequired = true;
					channelProps.setProperty("marimba.subscription.migratetenantmodel", "false");
					isRequired = channelProps.save();
					System.out.println("isrequired :" + isRequired);
				}

			} catch (Exception ec) {

			} finally {
				if (null != channelProps)
					channelProps.close();
			}
		}
		return isRequired;
	}
	public static synchronized SubscriptionMain getTenantSubMain(ServletContext context, String tenantName) {
		SubscriptionMain main = null;
		try {
			Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
			TenantAttributes currentTenantAttr = tenantsAttr.get(tenantName);
			if(null != currentTenantAttr) main = currentTenantAttr.getTenantMain();
		} catch(Exception ec) {
			ec.printStackTrace();
		}
		return main;
	}
	public static synchronized SubscriptionMain getTenantSubMain(ServletContext context, HttpSession session, String tenantName) {
		SubscriptionMain main = null;
		try {
			Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
			TenantAttributes currentTenantAttr = tenantsAttr.get(tenantName);
			if(null != currentTenantAttr) main = currentTenantAttr.getTenantMain();
		} catch(Exception ec) {
			WebAppUtils.saveInitException(session, new CriticalException("error.ldap.connect.failed"));
		}
		return main;
	}
	public static synchronized void diplayTenants(ServletContext context) {
		try {
			if(null == context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES)) System.out.println("Tenant Attribute empty");
			Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
			Iterator it = tenantsAttr.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pairs = (Map.Entry)it.next();
	            String tenantName = (String)pairs.getKey();
	            TenantAttributes tenan = (TenantAttributes) pairs.getValue();
	            SubscriptionMain main = tenan.getTenantMain();
	            String path = main.getDataDirectory().getAbsolutePath();
	            System.out.println("Tenant Name = " + tenantName);
	            System.out.println("Subscription Main dir  = " + path);
	        }
		} catch(Exception ec) {
			ec.printStackTrace();
		}
	}
	public static synchronized SubscriptionMain getTenantSubMain(ServletContext context, HttpServletRequest request) {
		SubscriptionMain main = null;
		try {
			IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
            String tenantName = user.getTenantName();
            //System.out.println("Current tenant name " + tenantName);
            main = TenantHelper.getTenantSubMain(context, request.getSession(), tenantName);
		} catch(Exception ec) {
			WebAppUtils.saveInitException(request.getSession(), new CriticalException("error.ldap.connect.failed"));
		}
		return main;
	}
	public static synchronized Exception getTenantInitError(ServletContext context, String tenantName) {
		Exception ex = null;
		try {
			Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
			TenantAttributes currentTenantAttr = tenantsAttr.get(tenantName);
			ex = currentTenantAttr.getInitError();
		} catch(Exception ec) {
			//ec.printStackTrace();
		}
		return ex;
	}
	public static synchronized void deActivateTenant(ServletContext context, String tenantName, boolean folderRemoved) {
		try {
			Map<String, TenantAttributes> tenantsAttr = (Map<String, TenantAttributes>) context.getAttribute(IWebAppConstants.TENANT_ATTRUBUTES);
			if(null != tenantsAttr.get(tenantName)) {
				if(folderRemoved) {
					TenantAttributes currentTenantAttr = tenantsAttr.get(tenantName);
					File removeDir = currentTenantAttr.getTenantMain().getDataDirectory();
					FileUtils.removeDirectory(removeDir);
				}
				tenantsAttr.remove(tenantName);
				context.setAttribute(IWebAppConstants.TENANT_ATTRUBUTES, tenantsAttr);
			} 
		} catch(Exception ec) {
			System.out.println("Failed to deactivate tenant from vDesk: " + tenantName );
			ec.printStackTrace();
		}
	}
}

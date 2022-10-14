// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.intf.msf.IServer;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.patch.IPatchMgr;
import com.marimba.intf.msf.patch.IPatchMgrContext;
import com.marimba.intf.msf.*;
import com.marimba.intf.msf.patch.PatchManagerException;
import com.marimba.intf.util.IDirectory;
import com.marimba.intf.util.IProperty;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.InternalException;

import javax.servlet.ServletContext;

/**
 * @author Devendra Vamathevan
 * @author Theen-Theen Tan
 * @version $Revision$, $Date$
 */

public class PatchManagerHelper {

    //reference external constants
    //todo  find the name of the service from patch manager
    public static String PATCH_MANAGER_SERVICE_NAME = "patchmanager";
    public static String SECURITY_POLICY_MANAGER_SERVICE_NAME = "";
    public static String PATCH_SERVICE_URL = IPatchMgrContext.PATCH_SERVICE_URL;
    public static String PATCH_TRANSMITTER_URL = IPatchMgrContext.PATCH_TRANSMITTER_URL;
    public static String PATCH_FOLDER = "PatchManagement/PatchGroups";

    private IPatchMgr patchMgr = null;
    private IPatchMgr securityPolicyMgr = null;

    private synchronized static IPatchMgr getPatchManager(ITenant tenant) throws SystemException {
        if(null == tenant) {
            throw  new KnownException(IErrorConstants.PATCH_MANAGER_NOT_CONFIGURED);
        }
        IPatchMgr patchMgr = (IPatchMgr) tenant.getManager(PATCH_MANAGER_SERVICE_NAME);
        if (null == patchMgr) {
            throw  new KnownException(IErrorConstants.PATCH_MANAGER_NOT_CONFIGURED);
        }
        System.out.println("Getting Patch Manager for tenant : " + tenant.getName());
        return patchMgr;
    }
    private synchronized static IPatchMgr getSecurityPolicyManager(ITenant tenant) throws SystemException {
        if(null == tenant) {
            throw  new KnownException(IErrorConstants.SECURITY_POLICY_MANAGER_NOT_CONFIGURED);
        }
        IPatchMgr patchMgr = (IPatchMgr) tenant.getManager(SECURITY_POLICY_MANAGER_SERVICE_NAME);
        if (null == patchMgr) {
            throw  new KnownException(IErrorConstants.SECURITY_POLICY_MANAGER_NOT_CONFIGURED);
        }
        return patchMgr;
    }
    private static synchronized IPatchMgrContext getPatchManagerContext(IUserPrincipal user, ITenant tenant) throws KnownException {
        IPatchMgrContext patchMgrContext = null;
        try {
            patchMgrContext = getPatchManager(tenant).createContext(user);
        } catch (Exception e) {
            throw  new KnownException(e, IErrorConstants.PATCH_CREATECONTEXT, user.getName());
        }
        if (patchMgrContext == null) {
            throw  new KnownException(IErrorConstants.PATCH_MANAGER_NOT_CONFIGURED);
        }
        return patchMgrContext;
    }
    private static synchronized IPatchMgrContext getSecurityPolicyManagerContext(IUserPrincipal user, ITenant tenant) throws KnownException {
        IPatchMgrContext securityPolicyMgrContext = null;
        try {
            securityPolicyMgrContext = getSecurityPolicyManager(tenant).createContext(user);
        } catch (Exception e) {
            throw  new KnownException(e, IErrorConstants.SECURITY_POLICY_CREATECONTEXT, user.getName());
        }
        if (securityPolicyMgrContext == null) {
            throw  new KnownException(IErrorConstants.SECURITY_POLICY_MANAGER_NOT_CONFIGURED);
        }
        return securityPolicyMgrContext;
    }
    private static synchronized IProperty getPatchConfig(IUserPrincipal user, ITenant tenant) throws SystemException {
        return getPatchManagerContext(user, tenant).getConfig();
    }

    private static synchronized IProperty getSecurityPolicyConfig(IUserPrincipal user, ITenant tenant) throws SystemException {
        return getSecurityPolicyManagerContext(user, tenant).getConfig();
    }

    public static synchronized String getPatchServiceUrl(IUserPrincipal user, ITenant tenant) throws SystemException {
        String url = getPatchConfig(user, tenant).getProperty(PATCH_SERVICE_URL);
        if (url == null || url.length() < 1) {
            throw new KnownException(IErrorConstants.PATCH_SERVICEURLNOTCONFIGURED);
        }
        return url;
    }

    public static synchronized String getPatchTransmitterUrl(IUserPrincipal user, ITenant tenant) throws SystemException {
        if (getPatchConfig(user, tenant).getProperty(PATCH_TRANSMITTER_URL) == null) {
            return null;
        } else {
            String patchTxURL = getPatchConfig(user, tenant).getProperty(PATCH_TRANSMITTER_URL);
            patchTxURL = patchTxURL.endsWith("/") ?  patchTxURL : patchTxURL + "/";
            return patchTxURL + PATCH_FOLDER;
        }
    }

    public static synchronized List simulate(IUserPrincipal user, String machine, Vector patchBundleURLs, ITenant tenant) throws SystemException {
        try {
            if(IAppConstants.DEBUG5) {
                System.out.println("USER =" + user);
                System.out.println("machine = " + machine);
                for (Iterator iterator = patchBundleURLs.iterator(); iterator.hasNext();) {
                    System.out.println(iterator.next());
                }
            }

            List l = getPatchManagerContext(user, tenant).simulate(machine, patchBundleURLs);

            if(IAppConstants.DEBUG5) {
                if (l == null) {
                    System.out.println("l is null");
                } else {
                    System.out.println(l);
                    for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                        System.out.println(iterator.next());
                    }
                    System.out.println("after");
                }
            }

            return l;
        } catch (PatchManagerException ex) {
            ex.printStackTrace();
            throw new KnownException(ex.getRootCause(), IErrorConstants.PATCH_SIMULATE, machine, ex.getMessage());
        }
    }

    public static synchronized List getPatches(IUserPrincipal user, String bundleURL, ITenant tenant) throws SystemException {
        try {
            return getPatchManagerContext(user, tenant).getPatches(bundleURL);
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new KnownException(ex, IErrorConstants.PATCH_GETPATCHES, bundleURL, ex.getMessage());
        }
    }

}

// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.system.operation;

import java.util.*;

import com.marimba.apps.subscription.common.intf.SubKnownException;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.tools.util.PropsComparator;

/**
 * This bean captures the applications that has been selected by the user in the Select Packages page.
 *
 * @author Theen-Theen Tan
 * @version 1.5, 11/07/2001
 */
public class SelectedPkgsBean
    implements IErrorConstants {
    static PropsComparator comp = new PropsComparator("title");
    private HashMap        packages = new HashMap();

    /**
     * REMIND
     *
     * @param node REMIND
     *
     * @throws SubKnownException REMIND
     */
    public void addPackage(PropsBean node)
        throws SubKnownException {
        String url = node.getProperty("url");

	// add if it already not added
        if (packages.get(url) == null) {
	    packages.put(url, node);
        }
    }

    /**
     * REMIND
     *
     * @param url REMIND
     *
     * @return REMIND
     */
    public boolean removePackage(String url) {
        Object value = packages.remove(url);

        if (value == null) {
            return false;
        }

        return true;
    }

    /**
     * REMIND
     *
     * @param url REMIND
     *
     * @return REMIND
     */
    public PropsBean getPackage(String url) {
        try {
            return (PropsBean) packages.get(url);
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Collection getPackages() {
        ArrayList result = new ArrayList(packages.values());
        Collections.sort(result, comp);

        try {
            return result;
        } catch (Exception exc) {
            return null;
        }
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String[] getPackagesArray() {
        ArrayList result = new ArrayList(packages.values());
        Collections.sort(result, comp);

        try {
            String urls[] = new String[result.size()];
            for(  int i = 0; i <  result.size();  i++ ) {
                urls[i]  = (String)result.get(i);
            }
            return urls;
        } catch (Exception exc) {
            return null;
        }
    }


    /**
     * REMIND
     *
     * @return REMIND
     */
    public Collection getPackageUrls() {
        return packages.keySet();
    }
}

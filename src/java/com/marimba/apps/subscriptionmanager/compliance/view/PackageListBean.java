// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.view;

import java.util.List;

/**
 * Result bean for a list of packages
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
public class PackageListBean {

    boolean error;
    List list;

    public PackageListBean() {
    }

    public boolean getError() {
	return error;
    }

    public void setError(boolean error) {
	this.error = error;
    }

    public List getList() {
	return list;
    }

    public void setList(List list) {
	this.list = list;
    }

}

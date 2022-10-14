// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.Hashtable;
import java.util.List;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.CreateSubContainers;
import com.marimba.apps.subscriptionmanager.cli.SubscriptionCLICommand;
import com.marimba.webapps.intf.SystemException;

public class CreateContainer extends SubscriptionCLICommand implements ISubscriptionConstants {

	public boolean run() throws SystemException {
        boolean isFailed = false;
        checkPrimaryAdminRole(cliUser);
        isFailed = createcontainers();
        return isFailed;
    }
    /**
     * Creates child containers.  Should be ran everytime a new domain is added to the forest.
     *
     * @return REMIND
     *
     * @throws SystemException REMIND
     */
    private boolean createcontainers() throws SystemException {
        CreateSubContainers cc = new CreateSubContainers(subsMain, cliUser);
        cc.create();
        printContainerResults(cc.getContainerNew(), "concreated");
        printContainerResults(cc.getDomainSubNew(), "subcreated");
        printContainerResults(cc.getContainerExist(), "conexist");
        printContainerResults(cc.getDomainSubExist(), "subexist");
        printMessage("\n");
        printMessage(resources.getString("cmdline.containers.replicate"));
        logMsg(LOG_AUDIT_SUB_CONTAINER_CREATED, LOG_AUDIT, null, CMD_CREATE_SUB_CONTAINER);
        return false;
    }
    private void printContainerResults(List   result, String suffix) {
        printMessage(resources.getString("cmdline.containers." + suffix));

        for (int i = 0; i < result.size(); i++) {
            printMessage((String) result.get(i));
        }

        printMessage("\n");
    }

}

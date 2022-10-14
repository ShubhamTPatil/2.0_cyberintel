// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: com/marimba/apps/subscriptionmanager/ws/subscribe/ISubscribeServiceContext
// $, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws.subscribe;

import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.webservices.IWebServiceConstants;

/**
 * Constants.
 *
 * @author Nageswara Rao V
 *
 */

public interface ISubscribeServiceConstants extends IWebServiceConstants {
    boolean DEBUG = DebugFlag.getDebug("SUBS/WS") > 4;
}

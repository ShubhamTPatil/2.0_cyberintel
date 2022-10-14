// Copyright 2009, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File: com/marimba/apps/subscriptionmanager/ws/subscribe/ISubscribeServiceContext
// $, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ws.subscribe;

import com.marimba.webapps.webservices.AbsSOAPEnabledObject;
import com.marimba.apps.subscription.common.intf.LogConstants;

/**
 * Base class for all the objects capable of rendering SOAP parts for the
 * SubscribeService.
 *
 * @author Nageswara Rao V
 *
 */
public class AbsSubsSOAPEnabledObject extends AbsSOAPEnabledObject
                      implements ISubscribeServiceConstants, LogConstants {

	protected ISubscribeServiceContext subsContext;

	public AbsSubsSOAPEnabledObject(ISubscribeServiceContext subsContext) {
		super(subsContext);
		this.subsContext = subsContext;
	}
}

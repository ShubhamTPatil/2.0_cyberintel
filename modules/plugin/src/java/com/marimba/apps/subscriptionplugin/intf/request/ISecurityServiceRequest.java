// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.intf.request;

import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceConstants;
import com.marimba.apps.subscriptionplugin.intf.ISecurityServiceSettings;
import com.marimba.intf.plugin.IHttpRequest;
import com.marimba.io.*;
import java.io.*;

/**
 * Represents a request handler for requests coming to the Security Service plugin
 *
 */
public interface ISecurityServiceRequest extends ISecurityServiceConstants {
	void handleRequest(FastInputStream in, IHttpRequest request, ISecurityServiceSettings securityServiceSettings) throws IOException;
    boolean processQueueReport(File queueReport, ISecurityServiceSettings securityServiceSettings) throws IOException;
}

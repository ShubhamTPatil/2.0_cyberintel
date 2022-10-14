// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.intf;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.marimba.webapps.intf.SystemException;

/**
 * Processes a list of objects in an action or a tag. Example usage: MemberListProcessor is passed as a parameter to setPagingResultsTag in which it is used to
 * obtain the member type on subset of the group members when we page through a list of group members.
 *
 * @author Theen-Theen Tan
 * @version 1.0, 07/25/2002
 */
public interface IListProcessor {
    /**
     * Process each item in the input listing as desired.
     */
    public void process(List               listing,
                        HttpServletRequest req,
                        ServletContext     context)
        throws SystemException;
}

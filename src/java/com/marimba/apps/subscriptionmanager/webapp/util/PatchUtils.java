// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;


public class PatchUtils {
    /**
     *
     * @param req
     * @return
     * @throws KnownException
     */
    public synchronized static String getPatchServiceUrl(HttpServletRequest req, ITenant tenant) throws SystemException {
        return PatchManagerHelper.getPatchServiceUrl((IUserPrincipal) req.getUserPrincipal(), tenant);
    }

    /**
     *
     * @param req
     * @return
     * @throws KnownException
     */
    public synchronized static String getPatchTransmitterURL(HttpServletRequest req, ITenant tenant) throws SystemException {
        return PatchManagerHelper.getPatchTransmitterUrl((IUserPrincipal) req.getUserPrincipal(), tenant);
    }

    public synchronized static String getPatchServiceProperty(ISubscription sub,
                                                              String propName)
            throws SystemException {

        // get the PatchService channel reference either from
        // the list of channels or dummy channels and get
        // subscription.update property value
        Channel patchService = getPatchServiceChannel(sub);
        if (patchService != null) {
            return patchService.getProperty(propName);
        }
        return null;
    }

    public synchronized static void setPatchServiceProperty(ISubscription sub,
                                                            String pubPatchServUrl,
                                                            String propName,
                                                            String propValue)
            throws SystemException {
        // get the PatchService channel reference using channel name
        // and subscription.update property.
        Channel patchService = getPatchServiceChannel(sub);
        if (patchService != null &&
                patchService.getProperty(propName) != null) {
            if(propValue==null && patchService.getProperty(propName)!=null){
                patchService.setProperty(propName,"null");
            }else{
                patchService.setProperty(propName, propValue);
            }
        } else {
            // check whether we have the channel object created
            // for the given URL
            patchService = sub.getChannel(pubPatchServUrl);
            if(patchService == null) {
                patchService = sub.getDummyChannel(pubPatchServUrl);
                if(patchService == null) {
                    patchService = sub.createDummyChannel(pubPatchServUrl);
                }
            }

            patchService.setProperty(propName, propValue);

        }
    }

    public synchronized static Channel getPatchServiceChannel(ISubscription sub) {
        Enumeration channels = sub.getChannels();
        if (channels != null) {
            while (channels.hasMoreElements()) {
                Channel channel = (Channel) channels.nextElement();
                if (channel.getUrl().endsWith("PatchService")) {
                    return channel;
                }
            }
        }

        channels = sub.getDummyChannels();
        if (channels != null) {
            while (channels.hasMoreElements()) {
                Channel channel = (Channel) channels.nextElement();
                if (channel.getUrl().endsWith("PatchService")) {
                    return channel;
                }
            }
        }
        return null;
    }
}


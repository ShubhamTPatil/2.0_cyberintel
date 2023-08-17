// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance.util;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.application.IApplicationContext;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.util.Props;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.logs.*;
import com.marimba.intf.util.*;
import com.marimba.intf.application.*;
import com.marimba.intf.certificates.*;
import com.marimba.intf.castanet.*;
import com.marimba.castanet.copy.*;


/**
 *  vDefCopyUtil - Utility class
 *  w.r.t copy vDef channel from Products Tx by using
 *  with channel store authentication
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class vDefCopyUtil {
    private SubscriptionMain main;
    private ChannelCopier copier;
    private ConfigProps config;

    public vDefCopyUtil() {
        // do nothing
    }
    
    public void init(SubscriptionMain main, ConfigProps config) {
        this.main = main;
        this.config = config;
    }

    public boolean executeVdefChannelCopy(String srcUrl, String dstUrl, String pubUser, String pubPwd) {
        boolean failed = true;
        try {
            URL src = new URL(srcUrl);
            URL dst = new URL(dstUrl);
            Props props = new Props();
            String user = pubUser;
            String pwd = pubPwd;
            copier = getCopier(user, pwd);
            if (copier == null) {
                System.out.println("LogInfo: Unable to get the copier instance...");
                return failed;
            }
            if (copier.copyChannel(props, src, dst) == 0) {
                failed = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            failed = true;
        }
        return failed;
    }


    private ChannelCopier getCopier(String user, String pwd) {
        String certId = null;
        String certPwd = null;
        IApplicationContext iApplicationContext = (IApplicationContext) main.getFeatures().getChild("context");
        ChannelCopier copier = new ChannelCopier(main, user, pwd, certId, certPwd, iApplicationContext);
        return copier;
    }

}


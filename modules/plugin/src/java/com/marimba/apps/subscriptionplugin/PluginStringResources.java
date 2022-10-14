// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.apps.subscription.common.ISubscriptionStringResources;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;
import java.text.MessageFormat;

/**
 * Adapter class for reading the externalized strings. As of 6.0.3, we don't have strings
 * to be externalized in classes from com.marimba.apps.subscription & com.marimba.apps.subscription.common
 * package which are used by the plugin.
 *
 * When it is required, we might need to define the externalized strings in CommonErrors.properties
 */
class PluginStringResources implements ISubscriptionStringResources, IPluginDebug {

    private ISubsPluginContext ctx;
    private ResourceBundle strResources;

    PluginStringResources(ISubsPluginContext ctx) {
        this.ctx = ctx;
        strResources = ResourceBundle.getBundle("strings");
    }

    public String getMessage(String key) {
        return getMessage0(key, null);
    }

    public String getMessage(String key, Object arg0) {
        return getMessage0(key, new Object[] {arg0});
    }

    public String getMessage(String key, Object arg0, Object arg1) {
        return getMessage0(key, new Object[] {arg0, arg1});
    }

    private String getMessage0(String key, Object args[]) {
        String message = null;
        try {
            message = strResources.getString(key);
            if(args != null) {
                message = MessageFormat.format(message, args);
            }
        } catch(MissingResourceException mre) {
            if(WARNING) {
                ctx.logToConsole("Entry not found in strings properties for key: " + key +
                                   ", for locale :" + Locale.getDefault());
            }
        }
        return message;
    }
}

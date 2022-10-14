package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.castanet.IChannel;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.util.IConfig;
import com.marimba.webapps.intf.IMapProperty;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: os-nrao
 * Date: Apr 29, 2005
 * Time: 10:49:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class PackageComplianceForm extends AbstractForm implements ISubscriptionConstants, IMapProperty {

    boolean initialized = false;
    HashMap checkedItems = new HashMap(DEF_COLL_SIZE);
    /**
     * REMIND
     */
    public void initialize() {
        if (!initialized) {
            props.put(SESSION_PERSIST_PREFIX, TGRESULT_PREFIX);
            checkedItems.clear();
        }
        initialized = true;
        if (DEBUG) {
            System.out.println("PackageComplianceForm: initialize called");
        }
    }

    public void initialize(SubscriptionMain smmain) {
        initialize();
        IConfig tunerConfig = (IConfig) smmain.getFeatures().getChild("tunerConfig");
        IWorkspace workspace = (IWorkspace) smmain.getFeatures().getChild("workspace");
        String securityInfoChnlUrl = smmain.getConfig().getProperty("subscriptionmanager.securityinfo.url");
        IChannel channel = workspace.getChannel(securityInfoChnlUrl);
        if (null == channel) {
            setValue("scapUpdateAvailable", "false");
            return;
        }
        String propValue = tunerConfig.getProperty("marimba.securityinfo.sync.status." + channel.getURL());
        String propValueSyncTime = tunerConfig.getProperty("marimba.securityinfo.sync.time." + channel.getURL());

        if (null == propValue || propValue.trim().isEmpty()) {
            setValue("scapUpdateAvailable", "false");
        } else {
            setValue("scapUpdateAvailable", "true");
            setValue("scapUpdateStatus", propValue);
        }
        if (null != propValueSyncTime && !propValueSyncTime.trim().isEmpty()) {
            String lastSysnctime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Long.parseLong(propValueSyncTime));
            setValue("scapUpdateTime", lastSysnctime);
        }
        setValue("scapUpdateAvailable", "false");//just to hide this in UI...
    }

    //documented in the interface for IBeanProperty
    public void setValue(String property,
                         Object value) {
        if (DEBUG) {
            System.out.println("PackageComplianceForm: set called for " + property);
        }
        if (property.startsWith(TGRESULT_PREFIX)) {
            checkedItems.put(property, value);
        } else {
            props.put(property, value);
        }
    }
    public Object getValue(String property) {
        if (property.startsWith(TGRESULT_PREFIX)) {
            return checkedItems.get(property);
        } else {
            return props.get(property);
        }
    }

    /**
     * REMIND
     */
    public void clearCheckedItems() {
        checkedItems.clear();
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        this.clearCheckedItems();
        props.clear();
        super.reset(mapping, request);
    }
}

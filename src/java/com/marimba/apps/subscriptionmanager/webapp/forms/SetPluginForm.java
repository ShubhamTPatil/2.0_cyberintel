// Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import com.marimba.apps.securitymgr.utils.ConfigUtil;
import com.marimba.apps.subscriptionmanager.PluginPropsProcessor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.intf.db.IConnectionPool;
import com.marimba.intf.msf.IDatabaseMgr;
import com.marimba.intf.msf.ITenant;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.ldap.LDAPConnUtils;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.util.Password;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.tools.util.KnownActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

/**
 * Form for the fields on the Set Plugin page.
 *
 * @author Angela Saval
 * @version 1.27, 04/21/2003
 */
public class SetPluginForm extends AbstractForm implements LDAPConstants,IMapProperty {

    String prevPage;
    ConfigProps config;
    String PASSWORD_STR = "*****";
    String publishPasswd;
    String subscribePasswd;
    String bindpasswd;
    String bindpasswd2;
    String dbPassword;
    String customScannerPasswrod;
    String securityInfoPassword;
    Map props = new HashMap();
    private String disableModify = "false";

    //These are the properties used for saving to the ldapconfig
    public final String[] configprops = {
            "publishurl", "securityinfo.url","customscanner.url","ldaphost", "basedn", "binddn",
            "bindpasswd", "bindpasswd2", "poolsize", "usessl", "authmethod",
            "publishurl.username", "publishurl.password", "publishurl.subscribeuser", "publishurl.subscribepassword", "lastpublishedtime", "publishcounter",
            "vendor", "lastgoodhostexptime", "pallowprov", "noautodiscover","pluginStatus",
            "repeaterInsert", "elasticurl", "cveFiltersDir",
            "db.hostname", "db.port", "db.name", "db.thread.min", "db.thread.max", "db.type", "db.username", "db.password",
            "securityinfo.subscribeuser", "securityinfo.subscribepassword", "customscanner.subscribeuser", "customscanner.subscribepassword"
    };

    /**
     * This can be called to initialize the form with a set of value.  Please note,
     * this approach was necessary because the ActionServlet is NOT initialized in
     * the constructor.  Therefore, the constructor could not be used to initialize.
     * This method can be called externally to initialize the values for this form.
     */
    public void initialize(HttpServletRequest req) {
        if (getServlet() == null) {
            return;
        }

        ServletContext   sc = servlet.getServletConfig().getServletContext();
        SubscriptionMain smmain = TenantHelper.getTenantSubMain(sc, req);
        initialize(smmain);
    }

    /**
     * REMIND
     *
     * @param smmain REMIND
     */
    public void initialize(SubscriptionMain smmain) {
        if (DEBUG) {
            System.out.println("SetPluginForm: initialization  2 called");
        }

        props.clear();
        setPublishPasswd(null);
        setBindpasswd(null);
        setBindpasswd2(null);
        config = smmain.getConfig();

        //Check to see if the plugin has ever been published before. This will determine what
        //with what values the form will be auto filled. If the plugin has been published,
        //then there is a value for the plugin url since this is required field
        String pluginurl = config.getProperty("subscriptionmanager.publishurl");

        if (DEBUG) {
            System.out.println("SetPluginForm: plugin url = " + pluginurl);
        }

        if ((pluginurl == null) || "".equals(pluginurl)) {
            // publishing for the first time
            if (DEBUG) {
                System.out.println("SetPluginForm: initializing with CMS values");
            }

            initWithCMSConfig(smmain);
            props.put("repeaterInsert", "false");
            props.put("pallowprov", "false");
            props.put("pluginStatus", "enable");
        } else {
            if (DEBUG) {
                System.out.println("SetPluginForm: initializing with saved values");
            }

            initWithSavedConfig(smmain);
        }
        loadSecurityDetails(this);
        loadDBConfig(this, smmain);

        // If AD is configured using No Auto Discovery, then we need to display host and base DN.
        // Similarly for ADAM and Sun ONE.
        if (LDAPConnUtils.getInstance(smmain.getTenantName()).isADWithAutoDiscovery(smmain.getLDAPConfig())) {
            props.put("canDisplayBaseDN", "false");
        } else {
            props.put("canDisplayBaseDN", "true");
        }
        loadEmpirumPropsFromCMS(smmain);
        //get the OS license information
        /* if(smmain.hasCapability(IAppConstants.CAPABILITIES_PATCH)) {
            props.put("osCapability", "true");
        }*/
    }
    private void loadSecurityDetails(SetPluginForm form) {
    	try {
	    	String secuirtyInfoURl = config.getProperty("subscriptionmanager.securityinfo.url");
	    	if(null != secuirtyInfoURl) {
	    		form.setValue("securityInfoURL", secuirtyInfoURl);
	    	}
	        String customScannerURL = config.getProperty("subscriptionmanager.customscanner.url");
	        if(null != customScannerURL) {
	        	form.setValue("customScannerURL", customScannerURL);
	        }
    	} catch(Exception ed) {
    		
    	}
        
    }
    private void initWithCMSConfig(SubscriptionMain smmain) {

        props.put(LDAPConstants.PROP_VENDOR, smmain.getLDAPProperty(LDAPConstants.PROP_VENDOR));

        if (LDAPConnUtils.getInstance(smmain.getTenantName()).isADWithAutoDiscovery(smmain.getLDAPConfig())) {
            props.put("ldaphost", new String[] { smmain.getLDAPProperty("domainpath") });
            props.put("basedn", new String[] { smmain.getLDAPProperty("domainAsDN") });
            props.put("noautodiscover", new String[] {"false"});
        } else {
            props.put("ldaphost", new String[] { smmain.getLDAPProperty(LDAPConstants.PROP_HOST) });
            props.put("basedn", new String[] { smmain.getLDAPProperty(LDAPConstants.PROP_BASEDN) });
            if(VENDOR_AD.equals(smmain.getLDAPProperty(LDAPConstants.PROP_VENDOR))) {
                props.put("noautodiscover", new String[] {"true"});
            } else {
                props.put("noautodiscover", null);
            }
        }

        props.put("binddn", smmain.getLDAPProperty(LDAPConstants.PROP_USERRDN));
        props.put("usessl", smmain.getLDAPProperty(LDAPConstants.PROP_USESSL));
        props.put("authmethod", smmain.getLDAPProperty(LDAPConstants.PROP_AUTHMETHOD));
        props.put("bindpasswd", smmain.getLDAPProperty(LDAPConstants.PROP_PASSWORD));
        setBindpasswd(smmain.getLDAPProperty(LDAPConstants.PROP_PASSWORD));
        props.put("bindpasswd2", smmain.getLDAPProperty(LDAPConstants.PROP_PASSWORD));
        setBindpasswd2(smmain.getLDAPProperty(LDAPConstants.PROP_PASSWORD));
        if (DEBUG) {
            System.out.println("SetPluginForm: initWithCMSConfig - binddn = " + props.get("binddn"));
            System.out.println("SetPluginForm: initWithCMSConfig - usessl = " + props.get("usessl"));
        }
        props.put("poolsize", smmain.getLDAPProperty(LDAPConstants.PROP_POOLSIZE));
        props.put("lastgoodhostexptime", smmain.getLDAPProperty(LDAPConstants.PROP_LASTGOODHOST_EXPTIME));
    }

    private void initWithSavedConfig(SubscriptionMain smmain) {

        loadPublishedProps(config);
        String bindPwd = (null != props.get("bindpasswd")) ? props.get("bindpasswd").toString() : "";
        props.put("bindpasswd2", bindPwd);

        setBindpasswd2(bindPwd);

        /*Every time a new property is introduced in configuration,
          assign default value to the property to avoid null pointers
          when plug-in loads with saved configuration.*/

        if (props.get("poolsize") == null) {
            props.put("poolsize", "25");
        }

        if (props.get("pallowprov") == null) {
            props.put("pallowprov", "false");
        }

        if ("true".equals(props.get("repeaterInsert"))) {
            props.put("repeaterInsert", "true");
        } else {
            props.put("repeaterInsert", "false");
        }

        PluginPropsProcessor pluginPropsProc = new PluginPropsProcessor(smmain.getLDAPConfig(), smmain.getConfig());
        boolean isLDAPConfigModified = pluginPropsProc.isLDAPConfigModified();

        if (DEBUG) {
            System.out.println("Is LDAP config modified in CMS: " + isLDAPConfigModified);
        }

        // This occurs when CMS vendor is changed, so we reload the CMS properties
        if (isLDAPConfigModified) {
            initWithCMSConfig(smmain);
        }
        if(null != props.get("mx42.server.soapurl")) {
            props.put("empirum.settings", "true");
        } else {
            props.put("empirum.settings", "false");
        }
    }

    public void loadPublishedProps(ConfigProps config) {
        // This is used to temporarily store the property values returned from the config.
        String propval = null;

        for (int i = 0; i < configprops.length; i++) {
            propval = config.getProperty(IAppConstants.CONFIG_PREFIX_SUBSCRPTMGR + "." + configprops [i]);

            if (DEBUG) {
                if(configprops[i].indexOf("pass") == -1) {
                    System.out.println("SetPluginForm.initWithSavedConfig 2: prop = " + configprops[i] + ", propval = " + propval);
                }
            }

            props.put(configprops [i], propval);
            setPropValue(configprops [i], propval);
        }
    }

    /**
     * This set method is used to set the values for input fields in the page
     *
     * @param property REMIND
     * @param value REMIND
     */
    public void setValue(String property,
                         Object value) {
        if (DEBUG) {
            if(property != null && property.indexOf("pass") == -1) {
                System.out.println("SetpluginForm: property = " + property);
                System.out.println("SetPluginForm: value = " + value);
            }
        }

        if ("publishurl.password".equals(property)) {
            encodeAndSet("changedPublishPwd", property, value, props);

            return;
        }

        if ("publishurl.subscribepassword".equals(property)) {
            encodeAndSet("changedSubscribePwd", property, value, props);

            return;
        }

        if ("bindpasswd".equals(property) || "bindpasswd2".equals(property)) {
            encodeAndSet("changedPassword", property, value, props);

            return;
        }

        if ("db.password".equals(property) || "db.password".equals(property)) {
            encodeAndSet("changedDBPassword", property, value, props);
            return;
        }
        if ("securityinfo.subscribepassword".equals(property) || "securityinfo.subscribepassword".equals(property)) {
            encodeAndSet("changedInfoSubPassword", property, value, props);
            return;
        }
        if ("customscanner.subscribepassword".equals(property) || "customscanner.subscribepassword".equals(property)) {
            encodeAndSet("changedCustomSubPassword", property, value, props);
            return;
        }

        //REMIND: this is done because for some reason logic:equal is not working
        //for the vendor attribute.  it does not handle String[] array. but for some
        //reason it works for the ldap.jsp in cms
        if ("vendor".equals(property)) {
            if ((value != null) && value instanceof String[]) {
                value = ((String[]) value) [0];
            }
        }

        props.put(property, value);

        if( "changedPassword".equals( property ) ){
            value = getValue( property );
            if( "true".equals( value ) ){
                if( getValue( "bindpasswd" ) != null ){
                    encodeAndSet("changedPassword", "bindpasswd", getValue( "bindpasswd" ), props);
                }
                if( getValue( "bindpasswd2" ) != null ){
                    encodeAndSet("changedPassword", "bindpasswd2", getValue( "bindpasswd2" ), props);
                }
            }
        } else if( "changedPublishPwd".equals( property ) ){
            value = getValue( property );
            if( "true".equals( value ) && ( getValue( "publishurl.password" ) != null ) ){
                encodeAndSet("changedPublishPwd", "publishurl.password", getValue( "publishurl.password" ), props);
            }
        } else if( "changedDBPassword".equals( property ) ){
            value = getValue( property );
            if( "true".equals( value ) && ( getValue( "db.password" ) != null ) ){
                encodeAndSet("changedDBPassword", "db.password", getValue( "db.password" ), props);
            }
        } else if( "changedInfoSubPassword".equals( property ) ){
            value = getValue( property );
            if( "true".equals( value ) && ( getValue( "securityinfo.subscribepassword" ) != null ) ){
                encodeAndSet("changedInfoSubPassword", "securityinfo.subscribepassword", getValue( "securityinfo.subscribepassword" ), props);
            }
        } else if( "changedCustomSubPassword".equals( property ) ){
            value = getValue( property );
            if( "true".equals( value ) && ( getValue( "customscanner.subscribepassword" ) != null ) ){
                encodeAndSet("changedCustomSubPassword", "customscanner.subscribepassword", getValue( "customscanner.subscribepassword" ), props);
            }
        }
    }

    void encodeAndSet(String chproperty,
                      String property,
                      Object value,
                      Map    props) {
        /* Check to see if the password has changed.
         * If so, we need to encode it.  If not, then do not encode.
         */
        String[] pwdvalue = null;

        String[] changedPwdVal = null;

        if ((props.get(chproperty)) instanceof String[] ) {
            changedPwdVal = (String[])props.get(chproperty);
        } else {
            changedPwdVal = new String[1];
            changedPwdVal[0] = (String)props.get(chproperty);
        }
        String   changedPwdStr = "false";

        if (changedPwdVal != null) {
            changedPwdStr = ((String[]) changedPwdVal) [0];
        }

        if ("true".equals(changedPwdStr)) {
            pwdvalue    = new String[1];
            if (value instanceof String[]) {
                pwdvalue = (String [])(value);
            } else {
                pwdvalue[0] = (String) value;
            }
            pwdvalue[0] = Password.encode(pwdvalue [0]);
        } else {
            if (value instanceof String[]) {
                pwdvalue = (String [])(value);
            } else {
                pwdvalue = new String[1];
                pwdvalue[0] = (String) value;
            }
        }

        props.put(property, pwdvalue);
        setPropValue(property, pwdvalue[0]);
    }

    /**
     * This set method is used to set the values for input fields in the page
     *
     * @param property REMIND
     *
     * @return REMIND
     */
    public Object getValue(String property) {
        if((props.get(property)) instanceof String[]){
            return ((String[]) props.get(property)) [0];
        }
        return props.get(property);
    }
    private void setPropValue(String property, String value) {
        if("publishurl.password".equalsIgnoreCase(property)) {
            setPublishPasswd(Password.decode(value));
        } else if("publishurl.subscribepassword".equalsIgnoreCase(property)) {
            setSubscribePasswd(Password.decode(value));
        } else if("bindpasswd".equalsIgnoreCase(property)) {
            setBindpasswd(value);
        } else if("bindpasswd2".equalsIgnoreCase(property)) {
            setBindpasswd2(value);
        } else if("db.password".equalsIgnoreCase(property)) {
            setDBPassword(value);
        } else if("securityinfo.subscribepassword".equalsIgnoreCase(property)) {
        	setSecurityInfoPassword(Password.decode(value));
        } else if("customscanner.subscribepassword".equalsIgnoreCase(property)) {
        	setCustomScannerPassword(Password.decode(value));
        }
    }
    public void setDBPassword(String ppasswd) {
        if(null != ppasswd && !"".equals(ppasswd.trim()) && !PASSWORD_STR.equals(ppasswd)) {
            props.put("db.password", ppasswd);
            this.dbPassword = PASSWORD_STR;
        } else if(null != ppasswd && "".equals(ppasswd.trim())) {
            props.put("db.password", "");
            this.dbPassword = "";
        } else {
            this.dbPassword = "";
        }
    }
    public void setCustomScannerPassword(String ppasswd) {
        if(null != ppasswd && !"".equals(ppasswd.trim()) && !PASSWORD_STR.equals(ppasswd)) {
            props.put("customscanner.subscribepassword", ppasswd);
            this.customScannerPasswrod = PASSWORD_STR;
        } else if(null != ppasswd && "".equals(ppasswd.trim())) {
            props.put("customscanner.subscribepassword", "");
            this.customScannerPasswrod = "";
        } else {
            this.customScannerPasswrod = "";
        }
    }
    public void setSecurityInfoPassword(String ppasswd) {
        if(null != ppasswd && !"".equals(ppasswd.trim()) && !PASSWORD_STR.equals(ppasswd)) {
            props.put("securityinfo.subscribepassword", ppasswd);
            this.securityInfoPassword = PASSWORD_STR;
        } else if(null != ppasswd && "".equals(ppasswd.trim())) {
            props.put("securityinfo.subscribepassword", "");
            this.securityInfoPassword = "";
        } else {
            this.securityInfoPassword = "";
        }
    }

    public String getSecurityInfoPassword() {
        if(null != props.get("securityinfo.subscribepassword") && !"".equals(props.get("securityinfo.subscribepassword"))) {
            this.securityInfoPassword = PASSWORD_STR;
        }
        return securityInfoPassword;
    }

    public String getCustomScannerPassword() {
        if(null != props.get("customscanner.subscribepassword") && !"".equals(props.get("customscanner.subscribepassword"))) {
            this.customScannerPasswrod = PASSWORD_STR;
        }
        return customScannerPasswrod;
    }

    public String getDBPassword() {
        if(null != props.get("db.password") && !"".equals(props.get("db.password"))) {
            this.publishPasswd = PASSWORD_STR;
        }
        return publishPasswd;
    }

    public String getPublishPasswd() {
        if(null != props.get("publishurl.password") && !"".equals(props.get("publishurl.password"))) {
            this.publishPasswd = PASSWORD_STR;
        }
        return publishPasswd;
    }

    public void setPublishPasswd(String ppasswd) {
        if(null != ppasswd && !"".equals(ppasswd.trim()) && !PASSWORD_STR.equals(ppasswd)) {
            props.put("publishurl.password", ppasswd);
            this.publishPasswd = PASSWORD_STR;
        } else if(null != ppasswd && "".equals(ppasswd.trim())) {
            props.put("publishurl.password", "");
            this.publishPasswd = "";
        } else {
            this.publishPasswd = "";
        }
    }

    public String getSubscribePasswd() {
        if(null != props.get("publishurl.subscribepassword") && !"".equals(props.get("publishurl.subscribepassword"))) {
            this.subscribePasswd = PASSWORD_STR;
        }
        return subscribePasswd;
    }

    public void setSubscribePasswd(String spasswd) {
        if(null != spasswd && !"".equals(spasswd.trim()) && !PASSWORD_STR.equals(spasswd)) {
            props.put("publishurl.subscribepassword", spasswd);
            this.subscribePasswd = PASSWORD_STR;
        } else if(null != spasswd && "".equals(spasswd.trim())) {
            props.put("publishurl.subscribepassword", "");
            this.subscribePasswd = "";
        } else {
            this.subscribePasswd = "";
        }
    }

    public String getBindpasswd() {
        if(null != props.get("bindpasswd") && !"".equals(props.get("bindpasswd"))) {
            this.bindpasswd = PASSWORD_STR;
        }
        return bindpasswd;
    }

    public void setBindpasswd(String bindpasswd) {
        if(null != bindpasswd && !"".equals(bindpasswd.trim()) && !PASSWORD_STR.equals(bindpasswd)) {
            props.put("bindpasswd", bindpasswd);
            this.bindpasswd = PASSWORD_STR;
        } else {
            this.bindpasswd = "";
        }
    }

    public String getBindpasswd2() {
        if(null != props.get("bindpasswd2") && !"".equals(props.get("bindpasswd2"))) {
            this.bindpasswd2 = PASSWORD_STR;
        }
        return bindpasswd2;
    }

    public void setBindpasswd2(String bindpasswd2) {
        if(null != bindpasswd2 && !"".equals(bindpasswd2.trim()) && !PASSWORD_STR.equals(bindpasswd2)) {
            props.put("bindpasswd2", bindpasswd2);
            this.bindpasswd2 = PASSWORD_STR;
        } else {
            this.bindpasswd2 = "";
        }
    }

    /**
     * Reset all properties necessary.  This is called any time the form is accessed (Even if it already exists in the session)
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping      mapping,
                      HttpServletRequest request) {
        if (DEBUG) {
            System.out.println("SetPluginForm: reset is called. should be called for form validation as well");
        }
        String[] value = new String[1];
        value [0] = "false";
        props.put("changedPassword", value);
        props.put("changedPublishPwd", value);
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        if (DEBUG) {
            System.out.println("SetPluginForm: validate is called.");
        }
        ActionErrors errors = new ActionErrors();

        String cveFiltersDir = (props.get("cveFiltersDir") != null) ? ((String)props.get("cveFiltersDir")) : null;
        if ((cveFiltersDir != null) && (cveFiltersDir.trim().length() > 0)) {
            boolean error = false;
            File cveFilterDirObj = new File(cveFiltersDir);
            if ((cveFilterDirObj.exists() && cveFilterDirObj.isDirectory())) {
                File[] cveFilterDirFiles = cveFilterDirObj.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".txt");
                    }
                });
                if (cveFilterDirFiles.length < 1) error = true;
            } else {
                error = true;
            }

            if (error) {
                errors.add("CVE Filters Directory desc", new KnownActionError("errors.raw", cveFiltersDir + " is not a valid directory. Specify a folder location, which contains the text file(s) with list of CVE numbers."));
            }
        }

        return errors;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getPrevPage() {
        return prevPage;
    }

    /**
     * REMIND
     *
     * @param prevPage REMIND
     */
    public void setPrevPage(String prevPage) {
        this.prevPage = prevPage;
    }
    private void loadEmpirumPropsFromCMS(SubscriptionMain main) {
        IConfig config = main.getServerConfig();
        if(null != config) {
            String empirum_url = config.getProperty("mx42.server.soapurl");
            if(null != empirum_url) {
                props.put("empirum.settings", "true");
                props.put("mx42.server.soapurl", empirum_url);
                props.put("mx42.server.username", config.getProperty("mx42.server.username"));
                props.put("mx42.server.password", Password.encode(config.getProperty("mx42.server.password")));
                if(null == props.get("mx42.repeater.allow")) {
                    // There is a change to get the value from properties.txt
                    props.put("mx42.repeater.allow", "false");
                }
            } else {
                // The Empirum settings are not configured in CMS
                props.put("empirum.settings", "false");
                props.put("mx42.server.soapurl", "");
                props.put("mx42.server.username", "");
                props.put("mx42.server.password", "");
                props.put("mx42.repeater.allow", "false");
            }
        }
    }

    public String getDisableModify() {
        return disableModify;
    }

    public void setDisableModify(String disableModify) {
        this.disableModify = disableModify;
    }

    void loadDBConfig(SetPluginForm selectForm, SubscriptionMain main) {
        try {
            ITenant tenant = main.getTenant();
            String url = config.getProperty("subscriptionmanager.db.url");
            String dbType = "";
            String port = "";
            String hostName = "";
            String dbName = "";
            String userName = "";
            String password = PASSWORD_STR;
            if (null == url || "".equals(url)) {
                String readPoolName = tenant.getDbMgr().getActive(IDatabaseMgr.DEFAULT_WRITE);
                IConnectionPool connectionPool = tenant.getDbMgr().getPool(readPoolName, null);
                dbType = connectionPool.getConfigProperty("db.connection.type");
                port = String.valueOf(connectionPool.getPort());
                hostName = connectionPool.getHostName();
                dbName = connectionPool.getSID();
                userName = connectionPool.getUserName();
            } else {
                String[] dbProps = ConfigUtil.parseDbUrl(url, false);
                hostName = dbProps[0];
                port = dbProps[1];
                dbName = dbProps[2];
                userName = config.getProperty("subscriptionmanager.db.username");
                dbType = config.getProperty("subscriptionmanager.db.type");
            }
            String dbThreadMin = config.getProperty("subscriptionmanager.db.thread.min");
            String dbThreadMax = config.getProperty("subscriptionmanager.db.thread.max");
            selectForm.setValue("db.thread.min", dbThreadMin == null ? "5" : dbThreadMin);
            selectForm.setValue("db.thread.max", dbThreadMax == null ? "30" : dbThreadMax);
            selectForm.setValue("db.type", dbType);
            selectForm.setValue("db.port", port);
            selectForm.setValue("db.hostname", hostName);
            selectForm.setValue("db.name", dbName);
            selectForm.setValue("db.username", userName);
            selectForm.setValue("db.password", PASSWORD_STR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

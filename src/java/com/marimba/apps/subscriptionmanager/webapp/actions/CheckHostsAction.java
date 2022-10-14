// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm;

import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IObserver;

import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.intf.msf.*;
import com.marimba.tools.util.Props;
import com.marimba.tools.util.Password;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.SystemException;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * This action will save the plugin properties to a config file in the data directory.  These properties are then used by the publish code to set the
 * properties in the plugin.
 *
 * @author Rahul Ravulur
 * @version 1.5, 02/23/2003
 */
public final class CheckHostsAction
    extends AbstractAction {
    public static String HOSTNAME = "hostName";
    public static String STATUS = "status";

    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param request REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws java.io.IOException REMIND
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  request,
                                 HttpServletResponse response)
        throws IOException,
                   ServletException {

        if(DEBUG) {
            System.out.println("CheckHostAction: called");
        }
        ServletContext context = request.getSession().getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(context, request);
        ConfigObj        config = new ConfigObj();
        IMapProperty    formbean = (IMapProperty) form;
        SetPluginForm userForm = (SetPluginForm) form;
        
        //String[]         parameter = (String[]) formbean.getValue("publishurl");
         String      parameter=null;

        parameter = getPropertyValue(formbean,"publishurl");
        
        if (DEBUG) {
            System.out.println("SetPluginSaveAction: publishurl = " + parameter);
        }
        
        config.setProperty("subscriptionmanager.publishurl", (parameter != null) ? parameter
                                                             : null);
        
        parameter = getPropertyValue(formbean,"ldaphost");

        String[] allHosts = {  };

        // check if hosts are specified, if they aren't return
        // if they are, trim the white space.
        if (parameter != null) {
            if ((parameter).length() == 0) {
                return mapping.findForward("success");
            }

            allHosts = stringToArray(parameter, ",", false);
            allHosts = trimHosts(allHosts);
            config.setProperty("subscriptionmanager.ldaphost", arrayToString(allHosts, ','));
        } else {
            return mapping.findForward("success");
        }
        parameter = getPropertyValue(formbean,"ppasswd");
        userForm.setPublishPasswd(parameter);
        
        parameter = getPropertyValue(formbean,"basedn");
        config.setProperty("subscriptionmanager.basedn", (parameter != null) ? parameter
                                                         : null);
        parameter = getPropertyValue(formbean,"binddn");
        config.setProperty("subscriptionmanager.binddn", (parameter != null) ? parameter
                                                         : null);
        parameter = getPropertyValue(formbean,"bindpasswd");

        String changedPwd = getPropertyValue(formbean,"changedPassword");

        String pwdvalue = (parameter != null) ? parameter
                          : null;
        if ("true".equals(changedPwd)) {
            String[] chpwd = new String[1];
            chpwd [0] = "false";
            formbean.setValue("changedPassword", chpwd);
            parameter = Password.encode(parameter);
            userForm.setBindpasswd(parameter);
            userForm.setBindpasswd2(parameter);
            config.setProperty("subscriptionmanager.bindpasswd", parameter);
        } else {

            userForm.setBindpasswd(parameter);
            userForm.setBindpasswd2(parameter);
            config.setProperty("subscriptionmanager.bindpasswd", pwdvalue);
        }
        parameter = getPropertyValue(formbean, "poolsize");
        config.setProperty("subscriptionmanager.poolsize", (parameter != null) ? parameter
                                                           : null);

        parameter = getPropertyValue(formbean,"lastgoodhostexptime");
        config.setProperty("subscriptionmanager.lastgoodhostexptime", (parameter != null) ? parameter
                                                           : null);

         parameter = getPropertyValue(formbean,"usessl");
         config.setProperty("subscriptionmanager.usessl", (parameter != null) ? parameter
                                                             : "false");
         parameter = getPropertyValue(formbean,"authmethod");
         config.setProperty("subscriptionmanager.authmethod", (parameter != null) ? parameter
                                                             : LDAPConstants.AUTHMETHOD_SIMPLE);

        String vendor = getPropertyValue(formbean,"vendor");

        if ((vendor == null) || LDAPConstants.VENDOR_AD.equals(vendor)) {
            config.setProperty("subscriptionmanager.vendor", LDAPConstants.VENDOR_AD);
        } else if ( LDAPConstants.VENDOR_ADAM.equals(vendor) ) {
            config.setProperty("subscriptionmanager.vendor", LDAPConstants.VENDOR_ADAM);
        } else {
            config.setProperty("subscriptionmanager.vendor", LDAPConstants.VENDOR_NS);
        }
        HttpSession session = request.getSession();
        Hashtable   failedHosts = new Hashtable();

        try {
            failedHosts = main.verifyLDAPConnections(config, main.getTenantName());
        } catch (SystemException ex) {
            ex.printStackTrace();
            throw new GUIException(ex);
        }

        HashMap hosts = new HashMap(allHosts.length + 1);

        if (DEBUG) {
            System.out.println(" made a hashtable with size " + allHosts.length);
        }

        // first initialize all of them as successful
        for (int in = 0; in < allHosts.length; in++) {
            PropsBean p = new PropsBean();
            p.setProperty(HOSTNAME, allHosts [in]);
            p.setProperty(STATUS, TEST_SUCCESS);
            hosts.put(allHosts [in], p);

            if (DEBUG) {
                System.out.println(" putting host.." + allHosts [in]);
            }
        }

        // now set the failed ones to be failed
        Enumeration failedHostsNames = failedHosts.keys();

        while (failedHostsNames.hasMoreElements()) {
            String name = (String) failedHostsNames.nextElement();

            if (DEBUG) {
                System.out.println("attempting to retrieve... " + name);
            }

            PropsBean p = (PropsBean) hosts.get(name);
            p.setProperty(STATUS, TEST_FAILED);

            String failureDetailString = ((Integer) failedHosts.get(name)).toString();
            p.setProperty(FAILURE_DETAIL, failureDetailString);

            if (DEBUG) {
                System.out.println(" setting the hosts as failed " + name);
            }
        }

        // set this into the session
        Vector results = new Vector(hosts.values());
        session.setAttribute(TEST_RESULT, results);


            return mapping.findForward("success");
    }

    /**
     * REMIND
     *
     * @param s REMIND
     * @param delims REMIND
     * @param sort REMIND
     *
     * @return REMIND
     */
    public String[] stringToArray(String  s,
                                  String  delims,
                                  boolean sort) {
        String[] result = null;

        if ((s != null) && (s.length() != 0)) {
            StringTokenizer st = new StringTokenizer(s, delims);
            result = new String[st.countTokens()];

            for (int i = 0; i < result.length; result [i++] = st.nextToken()) {
                ;
            }

            if (sort) {
                marimba.util.QuickSort.sort(result, 0, result.length, false);
            }
        } else {
            result = new String[0];
        }

        return result;
    }

    /**
     * Convert an array of strings to a 'delim' separated list.
     *
     * @param a the array to convert
     * @param delim the character to use to separate the elements
     *
     * @return REMIND
     */
    public static String arrayToString(String[] a,
                                       char     delim) {
        StringBuffer sb = new StringBuffer();
        int          len = a.length;

        if (len > 0) {
            sb.append(a [0]);

            for (int i = 1; i < len; sb.append(delim).append(a [i++])) {
                ;
            }
        }

        return sb.toString();
    }

    // remove white space
    private String[] trimHosts(String[] hosts) {
        for (int i = 0; i < hosts.length; i++) {
            hosts [i] = hosts [i].trim();
        }

        return hosts;
    }

    /**
     * helper class to pass in the properties of the form to SubscriptionMain to validate host information.
     */
    class ConfigObj
        implements IConfig {
        Props props;

        /**
         * Creates a new ConfigObj object.
         */
        public ConfigObj() {
            this.props = new Props();
        }

        /**
         * REMIND
         *
         * @param key REMIND
         * @param value REMIND
         */
        public void setProperty(String key,
                                String value) {
            props.setProperty(key, value);
        }

        /**
         * REMIND
         *
         * @param key REMIND
         *
         * @return REMIND
         */
        public String getProperty(String key) {
            return props.getProperty(key);
        }

        /**
         * REMIND
         *
         * @return REMIND
         */
        public String[] getPropertyPairs() {
            return props.getPropertyPairs();
        }

        /**
         * REMIND
         *
         * @param obj REMIND
         * @param start REMIND
         * @param end REMIND
         */
        public void addObserver(IObserver obj,
                                int       start,
                                int       end) {
            // not implemented
        }

        /**
         * REMIND
         *
         * @param obj REMIND
         */
        public void removeObserver(IObserver obj) {
            // not implemented
        }
    }
    /**
      *
      * @param props   IMapProperty
      * @param property   name of the property you want to retrieve
      * @return
      */

     public String getPropertyValue(IMapProperty props, String property){
         if((props.getValue(property)) instanceof String[]){
             return ((String[]) props.getValue(property)) [0];
         }
         return (String)props.getValue(property);

     }
}

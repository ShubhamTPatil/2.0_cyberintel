// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;
import com.marimba.intf.logs.ILog;
import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.util.WebAppUtils;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.util.MessageResources;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Class to keep commonly used methods from within the GUI framework (ActionForms/Actions/TagLib).
 *
 * @author Angela Saval
 * @author Theen-Theen Tan
 * @version 1.13, 05/08/2002
 */
public class GUIUtils
        implements IWebAppConstants,
        IErrorConstants {
    /**
     * This obtains IWebAppMain.  This is useful because subscription main contains some common features that can be used by many actions/forms.
     *
     * @param servlet This is the servlet that is the controller. It is accessible from     any Action instantiated.
     *
     * @return REMIND
     */
    public static SubscriptionMain getMain(ActionServlet servlet) {
        ServletContext   sc = servlet.getServletConfig()
                .getServletContext();
        SubscriptionMain main = (SubscriptionMain) sc.getAttribute(IWebAppsConstants.APP_MAIN);

        return main;
    }
    
    /**
     * Utility methods that use the log methods defined in WebAppUtils.  These are just wrappers so that the main of the web application does not need to be
     * obtained each time.
     *
     * @param servlet The action servlet which controls the redirection of the pages.     This is the class WebAppController in the case of subscription.
     * @param req REMIND
     * @param logid REMIND
     *
     * @see com.marimba.webapps.tools.util.WebAppUtils
     */
    public static void log(ActionServlet      servlet,
                           HttpServletRequest req,
                           int                logid) {
        log(servlet, req, logid, null, null, null);
    }

    /**
     * REMIND
     *
     * @param servlet REMIND
     * @param req REMIND
     * @param logid REMIND
     * @param arg1 REMIND
     */
    public static void log(ActionServlet      servlet,
                           HttpServletRequest req,
                           int                logid,
                           String             arg1) {
        log(servlet, req, logid, arg1, null, null);
    }

    /**
     * REMIND
     *
     * @param servlet REMIND
     * @param req REMIND
     * @param logid REMIND
     * @param arg1 REMIND
     * @param arg2 REMIND
     */
    public static void log(ActionServlet      servlet,
                           HttpServletRequest req,
                           int                logid,
                           String             arg1,
                           String             arg2) {
        log(servlet, req, logid, arg1, arg2, null);
    }

    /**
     * REMIND
     *
     * @param servlet REMIND
     * @param req REMIND
     * @param logid REMIND
     * @param arg1 REMIND
     * @param arg2 REMIND
     * @param arg3 REMIND
     */
    public static void log(ActionServlet      servlet,
                           HttpServletRequest req,
                           int                logid,
                           String             arg1,
                           String             arg2,
                           String             arg3) {
        logInfo(servlet, req, logid, arg1, arg2, arg3);
    }
    public static void logInfo(ActionServlet      servlet,
                               HttpServletRequest req,
                               int                logid,
                               String             arg1,
                               String             arg2,
                               String             arg3) {
    	ServletContext context = req.getSession().getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(context, req);
        String           user = null;

        try {
            user = getUser(req).getName();
        } catch (SystemException se) {
            //we attempt to get the user, however if we cannot, we simply continue logging the action
        }

        log(servlet, main.getAppLog(), main.getLogResources(), logid, arg1, arg2, arg3, LOG_INFO, user, null, null);
    }

    /**
     * REMIND
     *
     * @param servlet REMIND
     * @param req REMIND
     * @param logid REMIND
     * @param arg1 REMIND
     * @param arg2 REMIND
     * @param arg3 REMIND
     * @param target REMIND
     */
    public static void log(ActionServlet      servlet,
                           HttpServletRequest req,
                           int                logid,
                           String             arg1,
                           String             arg2,
                           String             arg3,
                           String             target) {
    	ServletContext context = req.getSession().getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(context, req);
        String           user = null;

        try {
            user = getUser(req).getName();
        } catch (SystemException se) {
            //we attempt to get the user, however if we cannot, we simply continue logging the action
        }

        log(servlet, main.getAppLog(), main.getLogResources(), logid, arg1, arg2, arg3, LOG_AUDIT, user, null, target);

    }

    public static void log(ActionServlet servlet, ILog log, MessageResources logres, int logid, String arg1,
                           String arg2, String arg3, int severity, String user, Throwable exception, String target) {

        SubscriptionMain main = getMain(servlet);
        String stlogid = Integer.toString(logid);
        //Get the message from the application resources
        String msg = getMessage(logres,Locale.getDefault(), stlogid,arg1,arg2,arg3);
        if (DEBUG) {
            System.out.println("GUIUtils: log id = " + logid + ", logmsg = " + msg);
        }

        if(null != main) {
            main.log(logid, severity, "vDesk", user, msg, target);
        }
    }
    public static String getMessage(MessageResources msgres, Locale locale, String msgid,
                                    String arg1, String arg2, String arg3) {
        Object[] args = new Object[3];
        args[0] = arg1;
        if (null != arg2) {
            args[1] = arg2;
        } else {
            args[1] = "";
        }
        if (null != arg3) {
            args[2] = arg3;
        }
        return msgres.getMessage(locale,msgid,args);
    }

    /**
     * These are utility methods used within actions so that getMain doesn't always have to be called.
     *
     * @param servlet REMIND
     * @param locale REMIND
     * @param msgid REMIND
     *
     * @return REMIND
     */
    public static String getMessage(ActionServlet servlet,
                                    Locale        locale,
                                    String        msgid) {
        return getMessage(servlet, locale, msgid, null, null, null);
    }

    /**
     * REMIND
     *
     * @param servlet REMIND
     * @param locale REMIND
     * @param msgid REMIND
     * @param arg1 REMIND
     *
     * @return REMIND
     */
    public static String getMessage(ActionServlet servlet,
                                    Locale        locale,
                                    String        msgid,
                                    String        arg1) {
        return getMessage(servlet, locale, msgid, arg1, null, null);
    }

    /**
     * REMIND
     *
     * @param servlet REMIND
     * @param locale REMIND
     * @param msgid REMIND
     * @param arg1 REMIND
     * @param arg2 REMIND
     *
     * @return REMIND
     */
    public static String getMessage(ActionServlet servlet,
                                    Locale        locale,
                                    String        msgid,
                                    String        arg1,
                                    String        arg2) {
        return getMessage(servlet, locale, msgid, arg1, arg2, null);
    }

    /**
     * REMIND
     *
     * @param servlet REMIND
     * @param locale REMIND
     * @param msgid REMIND
     * @param arg1 REMIND
     * @param arg2 REMIND
     * @param arg3 REMIND
     *
     * @return REMIND
     */
    public static String getMessage(ActionServlet servlet,
                                    Locale        locale,
                                    String        msgid,
                                    String        arg1,
                                    String        arg2,
                                    String        arg3) {
        SubscriptionMain main = getMain(servlet);

        return WebAppUtils.getMessage(main.getAppResources(), locale, arg1, arg2, arg3);
    }

    /**
     * The parameter values that are returned from a request are in the form of a string array.  This is because HTTPServletRequest.getParameter(String val)
     * returns a string array.  This is a utility to do the error checks before returning the first element in the string array.
     *
     * @param bean The bean (most likely a form) which was populated with the parameter     values when the page was submitted.
     * @param field The the property to look up
     *
     * @return String the parameter value for what was submitted to the page
     */
    public static String getValueAsString(IMapProperty bean,
                                          String        field) {
        Object val = bean.getValue(field);

        if (val == null) {
            return null;
        } else if ((val instanceof String[]) && ((String[]) val) [0].trim().length() > 0) {
            return ((String[]) val) [0];
        } else if ((val instanceof String) && (((String)val).trim().length() > 0)) {
            return (String) val;
        } else {
            /* This can occur if the the bean has been initialized with some other value.
             * This is the case with checkbox.  However, a parameter value always returns
             * something of type String[] so we know that this is the default.  This method
             * is only supposed to return what was submitted, not the default, so we return null
             */
            return null;
        }
    }

    /**
     * This method sets an object or value to the session variable. For example, this method may be called from an Action handler that sets its results into
     * the session var.  setToSession(req, ldap_results, searchResultsVector);
     *
     * @param req HTTPServletRequest used to get the session.
     * @param name String name to set in session.
     * @param value Object to assign to the name in the session.
     *
     * @throws SystemException REMIND
     * @throws InternalException REMIND
     */
    public static void setToSession(HttpServletRequest req,
                                    String             name,
                                    Object             value)
            throws SystemException {
        if (value == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "GUIUtils.setToSession", (String) value);
        }

        HttpSession session = req.getSession();
        session.setAttribute(name, value);
    }

    /**
     * This method gets an object or value from the session variable. For example, this method may be called from an Action handler that gets its results from
     * a session var.
     *
     * @param req HTTPServletRequest used to get the session.
     * @param name String name to set in session.
     *
     * @return void
     *
     * @throws SystemException REMIND
     * @throws InternalException REMIND
     */
    public static Object getFromSession(HttpServletRequest req,
                                        String             name)
            throws SystemException {
        if (name == null) {
            throw new InternalException(GUIUTILS_INTERNAL_WRONGARG, "GUIUtils.getToSession", (String) name);
        }

        HttpSession session = req.getSession();

        return session.getAttribute(name);
    }

    /**
     * REMIND
     *
     * @param request REMIND
     * @param mapping REMIND
     */
    public static void initForm(HttpServletRequest request,
                                ActionMapping      mapping) {
        HttpSession session = request.getSession();

        /*The targetDetails form used by this action is stored in the session so that
         *it can be used to store the state of target details the page. For example,
         *sort order, show url, check all, etc.
         * This form must be initialized.
         */
        Object tgform = session.getAttribute(mapping.getName());

        if (tgform != null) {
            if (tgform instanceof AbstractForm) {
                ((AbstractForm) tgform).initialize();
            }
        }
    }

    /**
     * This method removes an object or value from the session variable. For example, this method may be called from an Action handler that clears a session
     * variable that is no longer used.
     *
     * @param req HTTPServletRequest used to get the session.
     * @param name String name to be removed from session.
     *
     * @throws SubInternalException REMIND
     */
    public static void removeFromSession(HttpServletRequest req,
                                         String             name)
            throws SubInternalException {
        if (name == null) {
            throw new SubInternalException(CANT_REMOVE_NULLVALUE_FROM_SESSION);
        }

        HttpSession session = req.getSession();
        session.removeAttribute(name);
    }

    /**
     * This method accesses the Subscription Manager user object from the session
     *
     * @param req ServletRequest used to get the session.
     *
     * @return void
     *
     * @exception SubInternalException can't find the user object in the session
     */
    public static IUser getUser(HttpServletRequest req)
            throws SubInternalException {
        HttpSession session = req.getSession();
        IUser       user = (IUser) session.getAttribute(SESSION_SMUSER);

        if (user == null) {
            throw new SubInternalException(GUIUTILS_INTERNAL_SMUSERNOTFOUND);
        }

        return user;
    }

    public static String getUserName(HttpServletRequest req) {
        String userName = "";
        try {
            userName =  getUser(req).getName();
        } catch (SubInternalException siex) {
            // Skip it off;
        }

        return userName;
    }

    /**
     * This method parses a string that was generated by javascript representing the multiple selected items in a select box.
     *
     * @param selectStr String of multiple targets converted to a string by the javascript on the select_exclude.jsp page.  The string will look like: "0|2|5|"
     *        representing that the 0th, 2nd, and 5th items were selected. This is parsed into an array of integers for easier handling.
     *
     * @return Integer[] of the selected items.
     */
    public static Integer[] parseSelectedStr(String selectStr) {
        Integer[] result;

        if ((selectStr == null) || "".equals(selectStr)) {
            result = new Integer[0];
        } else {
            StringTokenizer tok = new StringTokenizer(selectStr, "|");
            int             size = tok.countTokens();
            result = new Integer[size];

            int i = 0;

            while (tok.hasMoreTokens()) {
                String str = tok.nextToken();

                if (i < size) {
                    result [i] = Integer.valueOf(str);
                    i++;
                }
            }
        }

        return result;
    }

    /**
     * This method removes one or more items from a list of items that is stored in the session variable and used by various components in the GUI. It then
     * sets the changed list back into the session.
     *
     * @param req HTTPServletRequest used to get the session.
     * @param listName the name of the session variable to set the results to
     * @param list the list of targets to remove items from
     * @param selectedItems an array of Integers representing which elements should be removed from the list of targets.
     *
     * @throws GUIException REMIND
     */
    public static void removeItems(HttpServletRequest req,
                                   String             listName,
                                   ArrayList          list,
                                   Integer[]          selectedItems)
            throws GUIException {
        if (DEBUG5) {
            System.out.println("IN removeItems method---");
        }

        int size = list.size();

        if (DEBUG5) {
            System.out.println("size of list= " + size);
        }

        for (int i = selectedItems.length; i > 0;) {
            int nth = selectedItems [--i].intValue();

            if (DEBUG5) {
                System.out.println("removing nth= " + nth);
            }

            if (nth < size) {
                list.remove(nth);
            } else {
                // remind mlin: throw exception
                if(DEBUG) {
                    System.out.println("INTERNAL ERROR in method removeSelectedItems: should not have array index outside of bounds");
                }
            }
        }

        try {
            GUIUtils.setToSession(req, listName, list);
        } catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        }

        return;
    }

    /**
     * This method adds an item into a list of items.  For example, if a user selects one or more targets on the select_exclude.jsp page and selects Add to
     * Excluded List button, the targets are added to the excluded list of targets and then the excluded list if set to the session.  The removal of the
     * target from from the selected list is done by a different method.
     *
     * @param req HTTPServletRequest used to get the session.
     * @param listName the name of the session variable to set the results to
     * @param fromList take the items to be added from this list
     * @param toList add the targets to this list
     * @param selectedItems an array of Integers representing which elements should be added from the fromList of targets to the toList of targets.
     *
     * @throws GUIException REMIND
     */
    public static void addItems(HttpServletRequest req,
                                String             listName,
                                ArrayList          fromList,
                                ArrayList          toList,
                                Integer[]          selectedItems)
            throws GUIException {
        if (DEBUG5) {
            System.out.println("IN addItems method---");
        }

        int size = fromList.size();

        if (DEBUG5) {
            System.out.println("size of fromList= " + size);
        }

        for (int i = selectedItems.length; i > 0;) {
            int nth = selectedItems [--i].intValue();

            if (DEBUG5) {
                System.out.println("adding nth= " + nth);
            }

            if (nth < size) {
                toList.add(fromList.get(nth));
            } else {
                // remind mlin: throw internal error
                if(DEBUG) {
                    System.out.println("INTERNAL ERROR in method addItems: should not have array index outside of bounds");
                }
            }
        }

        try {
            GUIUtils.setToSession(req, listName, toList);
        } catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        }
    }

    /**
     * REMIND
     *
     * @param req REMIND
     * @param listName REMIND
     * @param list REMIND
     * @param item REMIND
     * @param dir REMIND
     *
     * @throws GUIException REMIND
     */
    public static void moveItem(HttpServletRequest req,
                                String             listName,
                                ArrayList          list,
                                int                item,
                                String             dir)
            throws GUIException {
        if (DEBUG5) {
            System.out.println("IN moveItem method, dir= " + dir);
        }

        if (list != null) {
            int size = list.size();

            if (DEBUG5) {
                System.out.println("size of list= " + size);
                System.out.println("item= " + item);
            }

            if ((size > 0) && (0 <= item) && (item <= size)) {
                // if it's up or down one position, perform a swap
                if ("up".equals(dir) || "down".equals(dir)) {
                    int swapPos = item;

                    if ("up".equals(dir)) {
                        swapPos--;
                    } else if ("down".equals(dir)) {
                        swapPos++;
                    }

                    // check that item and the element to swap it with are within the list
                    if (((0 <= item) && (item < size)) && ((0 <= swapPos) && (swapPos < size))) {
                        Object temp = list.get(swapPos);
                        list.set(swapPos, list.get(item));
                        list.set(item, temp);
                    } else {
                        // remind mlin throw out of bounds exception
                    }

                    // if we're moving it to the top, then insert it at the beginning
                } else if ("top".equals(dir)) {
                    Object temp = list.get(item);
                    list.remove(item);
                    list.add(0, temp);

                    // if we're moving it to the bottom, then insert it at the end
                } else if ("bottom".equals(dir)) {
                    Object temp = list.get(item);
                    list.remove(item);
                    list.add(size - 1, temp);
                }

                // set to session
                try {
                    GUIUtils.setToSession(req, listName, list);
                } catch (SystemException se) {
                    GUIException guie = new GUIException(se);
                    throw guie;
                }
            }
        }
    }
}

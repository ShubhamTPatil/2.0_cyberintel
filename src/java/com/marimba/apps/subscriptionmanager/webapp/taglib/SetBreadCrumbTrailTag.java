// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import static com.marimba.apps.subscription.common.ISubscriptionConstants.*;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.util.Crumb;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.util.URLUTF8Encoder;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.WebAppUtils;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

/**
 * Custom tag that takes a dn and sets an array representing the bread crumb trail to the session.
 *
 * @author Michele Lin
 * @version 1.29, 04/23/2003
 */
public class SetBreadCrumbTrailTag
        extends TagSupport
        implements IWebAppConstants,
        IWebAppsConstants,
        ISubscriptionConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;
    String               dn;
    String               objectClass = "domain";

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getDn() {
        return this.dn;
    }

    /**
     * REMIND
     *
     * @param dn REMIND
     */
    public void setDn(String dn) {
        this.dn = dn;
    }

    /**
     * REMIND
     *
     * @param objectClass REMIND
     */
    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getObjectClass() {
        return this.objectClass;
    }

    /**
     * Takes the dn of the container currently being viewed and set a String array of its components (bread crumb) to the session.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
            throws JspException {
    	HttpSession session = pageContext.getSession();
    	String tenantName = (String) session.getAttribute(SESSION_TENANTNAME);
    	ServletContext servletcontext = pageContext.getSession().getServletContext();
    	SubscriptionMain main = TenantHelper.getTenantSubMain(servletcontext, pageContext.getSession(), tenantName);
        //SubscriptionMain main = (SubscriptionMain) pageContext.getServletContext().getAttribute(APP_MAIN);
        Locale locale = pageContext.getRequest().getLocale();
        String home = WebAppUtils.getMessage(main.getAppResources(), locale, "page.global.TargetsHome");

        if (DEBUG) {
            System.out.println("in setBreadCrumbTrailTag doStartTag()");
        }

        LDAPBean ldapBean = (LDAPBean) pageContext.getSession()
                .getAttribute("session_ldap");
        String   baseURL = ldapBean.getBaseURL();

        // remove '/sm/servlet' from baseURL
        String           context = ((HttpServletRequest) pageContext.getRequest()).getContextPath();

        if (baseURL.startsWith(context)) {
            baseURL = baseURL.substring(context.length());
        }

        if (DEBUG) {
            System.out.println("SetBreadCrumbTrail: baseURL = " + baseURL);
        }

        if (DEBUG) {
            // dn is a tag attribute
            System.out.println("dn= " + dn);

            // baseURL is from the ldapBean stored in the session
            System.out.println("baseURL= " + baseURL);
            System.out.println("ObjectClass" + objectClass);
        }

        Crumb[] result = new Crumb[1];

        if ("group".equals(dn)) {
            // the bread crumb was set by LDAPBrowseGroupAction
            return (EVAL_BODY_INCLUDE);
        } else if ("top".equals(dn)) {
            result     = new Crumb[1];
            //result [0] = new Crumb(HOME, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLEncoder.encode(baseURL));
            result [0] = new Crumb(home, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLUTF8Encoder.encode(baseURL));
            // Symbio modified 05/19/2005

        } else if (PEOPLE_EP.equals(dn) || GROUPS_EP.equals(dn) || SITES_EP.equals(dn)) {
            result     = new Crumb[2];
            //result [0] = new Crumb(HOME, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLEncoder.encode(baseURL));
            result [0] = new Crumb(home, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLUTF8Encoder.encode(baseURL));
            // Symbio modified 05/19/2005

            //String newLink = context + "/ldapBrowseOU.do?inputDN=" + URLEncoder.encode(dn) + "&epType=" + URLEncoder.encode(dn);
            String newLink = context + "/ldapBrowseOU.do?inputDN=" + URLUTF8Encoder.encode(dn) + "&epType=" + URLUTF8Encoder.encode(dn);
            // Symbio modified 05/19/2005
            String msgID = null;
            if(PEOPLE_EP.equals(dn)) {
            	msgID = "page.global.People";
            } else if(GROUPS_EP.equals(dn)) {
            	msgID = "page.global.Groups";
            } else {
            	msgID = "page.global.Sites";
            }
            String name = WebAppUtils.getMessage(main.getAppResources(), locale, msgID);
            result [1] = new Crumb(name, "ep", newLink, msgID);

            if (DEBUG) {
                System.out.println("SetBreadCrumbTrail: in group conditional");
            }
        } else if (MDM_DEVICE_GROUPS_EP.equals(dn) || MDM_DEVICE_GROUP_EP.equals(dn) || MDM_DEVICE_EP.equals(dn)) {
            result     = new Crumb[2];
            //result [0] = new Crumb(HOME, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLEncoder.encode(baseURL));
            result [0] = new Crumb(home, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLUTF8Encoder.encode(baseURL));
            // Symbio modified 05/19/2005

            //String newLink = context + "/ldapBrowseOU.do?inputDN=" + URLEncoder.encode(dn) + "&epType=" + URLEncoder.encode(dn);
            String newLink = context + "/ldapBrowseOU.do?inputDN=" + URLUTF8Encoder.encode(dn) + "&epType=" + URLUTF8Encoder.encode(dn);
            // Symbio modified 05/19/2005
            String msgID = null;
            if(MDM_DEVICE_GROUPS_EP.equals(dn)) {
            	msgID = "page.global.DeviceGroups";
            } else if(MDM_DEVICE_GROUP_EP.equals(dn)) {
            	msgID = "page.global.DeviceGroup";
            } else if(MDM_DEVICE_EP.equals(dn)) {
            	msgID = "page.global.Device";
            } else {
            	msgID = "page.global.Sites";
            }
            String name = WebAppUtils.getMessage(main.getAppResources(), locale, msgID);
            result [1] = new Crumb(name, "ep", newLink, msgID);

            if (DEBUG) {
                System.out.println("SetBreadCrumbTrail: in device group conditional");
            }
        } else if (!"true".equals(ldapBean.getUsersInLDAP()) && ldapBean.getIsGroup()) {
            result = new Crumb[3];

            String link = context + "/txBrowseGroup.do?name=" + ldapBean.getGroup();
            //result [0] = new Crumb(HOME, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLEncoder.encode(baseURL));
            result [0] = new Crumb(home, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLUTF8Encoder.encode(baseURL));
            // Symbio modified 05/19/2005

            String msgid = "page.global.Groups";
            String name = WebAppUtils.getMessage(main.getAppResources(), locale, msgid);
            String newLink = context + "/ldapBrowseOU.do?epType=" + GROUPS_EP;

            result [1] = new Crumb(name, "ep", newLink, msgid);
            result [2] = new Crumb(ldapBean.getGroup(), "group", link);

            if (DEBUG) {
                System.out.println("SetBreadCrumbTrail: in group conditional");
            }
        } else {
            // REMIND:
            // This section should be rewritten
            // using an arraylist or some sort of grow able array since the
            // tag array code is being called everytime the user browses
            Crumb[] oldResult = (Crumb[]) pageContext.getSession().getAttribute(PAGE_BREADCRUMB);

            //recreate link using inputDN if the inputDN exist we backtracked
            // on the crumb resize array.
            String newLink = context + "/ldapBrowseOU.do?inputDN=" + URLUTF8Encoder.encode(dn);
            // Symbio modified 05/19/2005
            //session.removeAttribute("policystartlocation");
            if(null != session.getAttribute("policystartlocation")) {
            	boolean status = true;
            	session.setAttribute("policystartlocation", null);
            	
            	try {
            		LDAPConnection conn1 = LDAPWebappUtils.getBrowseConn((HttpServletRequest) pageContext.getRequest());
            		String baseDN = conn1.getBaseDN();
            		Vector<String> relativeDNs = LDAPWebappUtils.getRelativeDNs(dn, baseDN);
            		if(null != relativeDNs) {
            			oldResult = new Crumb[relativeDNs.size()+1];
                    	oldResult[0] = new Crumb(home, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLUTF8Encoder.encode(baseURL));
                    	int count = 1;
            			Iterator<String> itStr = relativeDNs.iterator();
            			while(itStr.hasNext()) {
            				String inputdn = itStr.next();
            				try {
            					String objectClass = LDAPUtils.getObjectClass(inputdn, conn1, main.getLDAPVarsMap(), main.getTenantName(), main.getChannel());
           	                    LDAPName ldapName1 = conn1.getParser();
           	                    Name     dnName1 = ldapName1.parse(dn);
           	                    boolean isContainer = false; 
           	                    if("domain".equals(objectClass)) {
           	                    	isContainer = false;
           	                    } else {
           	                    	isContainer = true;
           	                    	objectClass = "container";
           	                    }
           	                    
           	                    String   displayName1 = LDAPWebappUtils.dn2DNSName(conn1, inputdn, isContainer);
           	                    newLink = context + "/ldapBrowseOU.do?inputDN=" + URLUTF8Encoder.encode(inputdn);
           	            		oldResult[count] = new Crumb(displayName1, objectClass, newLink);
           	            		count = count + 1 ;
            				} catch(Exception ed) {
            					status = false;
            					ed.printStackTrace();
            				}
            			}
            		}
                    
            	} catch(Exception ed) {
            		ed.printStackTrace();
            		status = false;
            	}
            	if(!status) {
            		oldResult = new Crumb[2];
                	oldResult[0] = new Crumb(home, TYPE_HOME, context + "/ldapBrowseTop.do?baseURL=" + URLUTF8Encoder.encode(baseURL));
                	// link not found create crumb to add to list
                	try {
                		LDAPConnection conn2 = LDAPWebappUtils.getBrowseConn((HttpServletRequest) pageContext.getRequest());
	                    LDAPName ldapName2 = conn2.getParser();
	                    Name     dnName2 = ldapName2.parse(dn);
	                    String   displayName1 = LDAPWebappUtils.dn2DNSName(conn2, dnName2.toString(), oldResult.length != 1);
	                    newLink = context + "/ldapBrowseOU.do?inputDN=" + URLUTF8Encoder.encode(dn);
	            		oldResult[1] = new Crumb(displayName1, objectClass, newLink);
                	} catch(Exception ed) {
                		
                	}
            	}
            }
            int    newCrumbSize = oldResult.length;

            for (int i = 0; i < oldResult.length; i++) {
                if (((Crumb) oldResult [i]).getLink()
                        .equals(newLink)) {
                    newCrumbSize = i;

                    break;
                }
            }

            if (DEBUG3) {
                System.out.println("old result length" + oldResult.length);
                System.out.println("new result length" + newCrumbSize);
            }

            if (newCrumbSize == oldResult.length) {
                try {
                    // get an ldap connection that will be needed to get the dn parser
                    LDAPConnection conn = LDAPWebappUtils.getBrowseConn((HttpServletRequest) pageContext.getRequest());

                    // link not found create crumb to add to list
                    LDAPName ldapName = conn.getParser();
                    Name     dnName = ldapName.parse(dn);
                    String   displayName = LDAPWebappUtils.dn2DNSName(conn, dnName.toString(), oldResult.length != 1);

                    //special case parse entire dn when we at the root
                    result = new Crumb[oldResult.length + 1];
                    System.arraycopy(oldResult, 0, result, 0, oldResult.length);
                    if ("domain".equals(objectClass)) {
                        result [oldResult.length] = new Crumb(displayName, "domain", newLink);
                    } else {
                        result [oldResult.length] = new Crumb(displayName, "container", newLink);
                    }
                } catch (SystemException se) {
                    WebAppUtils.saveTagException(pageContext, se);
                } catch (NamingException ne) {
                    try {
                        LDAPUtils.classifyLDAPException(ne, null, false);
                    } catch (SystemException se) {
                        WebAppUtils.saveTagException(pageContext, se);
                    }
                }

                // 
            } else {
                result = new Crumb[newCrumbSize + 1];
                System.arraycopy(oldResult, 0, result, 0, newCrumbSize + 1);
            }
        }

        // Set bread crumb results to session var
        if (DEBUG) {
            System.out.println("SetBreadCrumbTrail: setting bread crumb var");
        }

        pageContext.getSession().setAttribute(PAGE_BREADCRUMB, result);

        if (DEBUG2) {
            System.out.println("result=");

            for (int i = 0; i < result.length; i++) {
                System.out.println("  [" + i + "]= " + result [i].getName() + ", " + result [i].getLink());
            }
        }

        return (EVAL_BODY_INCLUDE);
    }
    
    /**
     * Process the end of this tag.  The default implementation does nothing.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag()
            throws JspException {
        return (EVAL_PAGE);
    }
}

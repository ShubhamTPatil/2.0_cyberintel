// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import static com.marimba.apps.subscription.common.ISubscriptionConstants.*;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.webapps.tools.util.PropsBean;
import org.apache.struts.Globals;

import javax.servlet.http.*;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Map;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class ExpandURLRendererTag extends TagSupport {

    PropsBean entry = new PropsBean();
    HttpServletRequest request;

    /**
     * REMIND
     *
     * @return REMIND
     */
    public PropsBean getEntry() {
        return entry;
    }

    /**
     * REMIND
     *
     * @param entry REMIND
     */
    public void setEntry(PropsBean entry) {
        this.entry = entry;
    }

    /**
     * REMIND
     *
     * @return REMIND
     *
     * @throws JspException REMIND
     */

    public int doStartTag() throws JspException {

        StringBuilder url = new StringBuilder(256);
        String objectClass = entry.getProperty(GUIConstants.TYPE);
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        // apend application path /sm in our case
        url.append(request.getContextPath());
        HttpSession session = request.getSession();
        String dirType = (String) session.getAttribute(GUIConstants.DIRECTORY_TYPE);
        Map<String, String> LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(dirType);
        if (LDAPVarsMap.get("GROUP_CLASS").equals(objectClass) ||
        		LDAPVarsMap.get("GROUP_CLASS_UNIQUE").equals(objectClass) ||
        				LDAPVarsMap.get("COLLECTION_CLASS").equals(objectClass)) {
            url.append("/ldapBrowseGroup.do?member=");
            if (LDAPVarsMap.get("GROUP_CLASS_UNIQUE").equals(objectClass)) {
                url.append("uniqueMember");
            } else {
                url.append("member");
            }
            url.append("&inputDN=");
            //url.append(java.net.URLEncoder.encode(entry.getProperty("dn")));
            url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty("dn")));
            url.append("&objectClass=");
            //url.append(java.net.URLEncoder.encode(entry.getProperty(GUIConstants.TYPE)));
            url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty(GUIConstants.TYPE)));
            url.append("&levels=");
            url.append((String) request.getAttribute("count"));
        }  else if (LDAPVarsMap.get("TARGET_TX_GROUP").equals(objectClass)) {
            url.append("/txBrowseGroup.do?name=");
            url.append(entry.getProperty("escaped_name"));
        } else {
            if (PEOPLE_EP.equals(objectClass) ) {
                url.append("/ldapBrowseOU.do?inputDN=Users&epType=people_ep");
            } else if (GROUPS_EP.equals(objectClass) ) {
                url.append("/ldapBrowseOU.do?inputDN=User+Groups&epType=groups_ep");
            } else if (TYPE_SITE.equals(objectClass) ) {
                url.append("/ldapBrowseOU.do?inputDN=Sites&epType=sites_ep");
            } else if (TYPE_DEVICE.equals(objectClass) ) {
                url.append("/ldapBrowseOU.do?inputDN=");
                url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty("dn")));
                url.append("&epType=device_ep&id=");
                url.append(entry.getProperty("id"));
            } else if (MDM_TYPE_DEVICE_GROUPS.equals(objectClass) ) {
                url.append("/ldapBrowseOU.do?inputDN=devicegroups&epType=device_groups_ep");
            } else if (MDM_TYPE_DEVICE_GROUP.equals(objectClass) ) {
                url.append("/ldapBrowseOU.do?inputDN=");
                url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty("dn")));
                url.append("&epType=device_group_ep&id=");
                url.append(entry.getProperty("id"));
            } else {
                url.append("/ldapBrowseOU.do?inputDN=");
                //url.append(java.net.URLEncoder.encode(entry.getProperty("dn")));
                url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty("dn")));
                url.append("&objectClass=");
                //url.append(java.net.URLEncoder.encode(entry.getProperty(GUIConstants.TYPE)));
                url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty(GUIConstants.TYPE)));
            }
        }
        JspWriter writer = pageContext.getOut();

        try {
            writer.print(url.toString());
        } catch (IOException e) {
            pageContext.setAttribute(Globals.EXCEPTION_KEY, e, PageContext.REQUEST_SCOPE);
            throw new JspException(e.toString());
        }

        return SKIP_BODY;
    }

    /**
     * Process the end of this tag.  The default implementation does nothing.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag() throws JspException {
        return (EVAL_PAGE);
    }
}

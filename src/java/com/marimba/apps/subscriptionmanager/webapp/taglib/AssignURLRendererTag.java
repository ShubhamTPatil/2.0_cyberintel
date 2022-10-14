// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import static com.marimba.apps.subscription.common.LDAPVars.AD_UID;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.webapps.tools.util.PropsBean;
import org.apache.struts.Globals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * This tag renders the href and mouse over strings for browse target page
 *
 * @author Devendra Vamathevan
 * @author $Author$
 * @version 1.2, 12/14/2001
 */

public class AssignURLRendererTag extends TagSupport {
    private final char[] SPECIAL_CHAR = {'\'', '\\'};

    PropsBean entry;

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
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        StringBuilder url = new StringBuilder(256);
        url.append("<a href='");
        url.append(request.getContextPath());

        if(ISubscriptionConstants.MDM_TYPE_DEVICE_GROUP.equals(entry.getProperty(GUIConstants.TYPE)) || ISubscriptionConstants.TYPE_DEVICE.equals(entry.getProperty(GUIConstants.TYPE))) {
        	url.append("/mdmPolicyViewDispatcher.do");
        } else {
        	url.append("/securityTargetViewDispatcher.do");
        	//url.append("/targetViewDispatcher.do");
        }

        url.append("?name=");
        //url.append(java.net.URLEncoder.encode(entry.getProperty(GUIConstants.DISPLAYNAME)));
        url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty(GUIConstants.DISPLAYNAME)));

        url.append("&type=");
        url.append(entry.getProperty(GUIConstants.TYPE));
        url.append("&id=");
        StringTokenizer st =null;
        if(ISubscriptionConstants.MDM_TYPE_DEVICE_GROUP.equals(entry.getProperty(GUIConstants.TYPE)) || ISubscriptionConstants.TYPE_DEVICE.equals(entry.getProperty(GUIConstants.TYPE))) {
        	url.append(entry.getProperty("id"));
            url.append("' target='mainFrame' class='hoverLink' onmouseover=\"MakeTip('");
            url.append("<b>DN:</b>");
            st = new StringTokenizer((String) entry.getValue("dn"), ",", true);
            String t;
            boolean broken = false;

            while (st.hasMoreTokens()) {
                t = escapeJavascriptAndHTML(st.nextToken());
                url.append(t);

                if (!broken && t.equals(",") && (url.length() > 30)) {
                    url.append(" ");
                    broken = true;
                }
            }
        } else {
        	if (entry.getValue("dn") != null) {
                String strValue= (String)entry.getValue("objectclass");
                if (entry.getValue(AD_UID) != null && strValue.equals(IWebAppConstants.INVALID_NONEXIST)) {
                    url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty(AD_UID)));
                    url.append("' target='mainFrame' class='hoverLink' onmouseover=\"MakeTip('");
                    url.append("<b>DN:</b>");
                    st = new StringTokenizer((String) entry.getValue(AD_UID), ",", true);
                } else {
                    url.append(com.marimba.tools.util.URLUTF8Encoder.encode(entry.getProperty("dn")));
                    url.append("' target='mainFrame' class='hoverLink' onmouseover=\"MakeTip('");
                    url.append("<b>DN:</b>");
                    st = new StringTokenizer((String) entry.getValue("dn"), ",", true);
                }
                String t;
                boolean broken = false;

                while (st.hasMoreTokens()) {
                    t = escapeJavascriptAndHTML(st.nextToken());
                    url.append(t);

                    if (!broken && t.equals(",") && (url.length() > 30)) {
                        url.append(" ");
                        broken = true;
                    }
                }
            }
        }
        

        if (entry.getValue("description") != null) {
            url.append("<br><b>Description: </b>");
            url.append(escapeJavascriptAndHTML((String) entry.getValue("description")));
        }

        if (entry.getValue("scope") != null) {
            url.append("<br><b>Scope: </b>");
            url.append(escapeJavascriptAndHTML((String) entry.getValue("scope")));
        }
        url.append("');\" onmouseout=\"CloseTip();\">");

        JspWriter writer = pageContext.getOut();

        try {
            writer.print(url.toString());
        } catch (IOException e) {
            pageContext.setAttribute(Globals.EXCEPTION_KEY, e, PageContext.REQUEST_SCOPE);
            throw new JspException(e);
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

    private String escapeJavascriptAndHTML(String comp) {
        if (comp == null) {
            return null;
        }

        int i = 0;
        StringBuilder buf = new StringBuilder();

        while (i < comp.length()) {
            char c = comp.charAt(i);
            if (isEscape(c)) {
                // Java script error
                buf.append('\\');
                buf.append(c);
            } else {
                switch (c) {
                    case '<':
                        buf.append("&lt;");
                        break;
                    case '>':
                        buf.append("&gt;");
                        break;
                    case '&':
                        buf.append("&amp;");
                        break;
                    case '"':
                        buf.append("&quot;");
                        break;
                    default:
                        buf.append(c);
                }
            }
            i++;
        }

        return buf.toString();
    }

    private boolean isEscape(char c) {
        for (char aSPECIAL_CHAR : SPECIAL_CHAR) {
            if (c == aSPECIAL_CHAR) {
                return true;
            }
        }

        return false;
    }
}

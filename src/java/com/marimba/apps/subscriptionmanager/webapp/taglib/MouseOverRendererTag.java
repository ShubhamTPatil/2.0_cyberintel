// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import org.apache.struts.action.Action;

import java.io.IOException;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class MouseOverRendererTag
    extends TagSupport {
    PropsBean          entry = new PropsBean();
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
    public int doStartTag()
        throws JspException {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        StringBuffer       mouseOverStr = new StringBuffer(256);

        if (entry.getValue("dn") != null) {
            mouseOverStr.append("<b>DN:</b>");

            StringTokenizer st = new StringTokenizer((String) entry.getValue("dn"), ",", true);
            boolean         broken = false;
            String          t;

            while (st.hasMoreTokens()) {
                t = st.nextToken();
                mouseOverStr.append(t);

                if (!broken && t.equals(",") && (mouseOverStr.length() > 30)) {
                    mouseOverStr.append(" ");
                    broken = true;
                }
            }
        }

        if (entry.getValue("description") != null) {
            mouseOverStr.append("<br><b>Description: </b>");
            mouseOverStr.append(entry.getValue("description"));
        }

        if (entry.getValue("scope") != null) {
            mouseOverStr.append("<br><b>Scope: </b>");
            mouseOverStr.append(entry.getValue("scope"));
        }
                 mouseOverStr.append(" ");
        //if (DEBUG3){
        //System.out.println(mouseOverStr.toString());

        //}
        JspWriter writer = pageContext.getOut();

        //try {
            //writer.print(mouseOverStr.toString());
        //} catch (IOException e) {
          //  pageContext.setAttribute(Action.EXCEPTION_KEY, e, PageContext.REQUEST_SCOPE);
            //throw new JspException(e);
        //}

        return SKIP_BODY;
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

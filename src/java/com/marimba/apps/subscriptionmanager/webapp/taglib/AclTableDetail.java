// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import java.io.IOException;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.marimba.apps.subscription.common.LDAPVars;

import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;

import com.marimba.webapps.intf.SystemException;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * REMIND: dvama
 * @author dvamathevan
 */
public class AclTableDetail
    extends TagSupport {

    static final String IMAGES_SLASH_DELIM = "ImagesSlashDelim";
    String[] PRINCIPAL_TYPES = {"POLICY_READ", "POLICY_WRITE", "ACL_READ", "ACL_WRITE"};

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
        StringBuffer       url = new StringBuffer(256);

        String             ctx = null;

        JspWriter          writer = pageContext.getOut();

        try {
            ctx = (String) GUIUtils.getFromSession(request, "context");
        } catch (SystemException e) {
            e.printStackTrace(); //To change body of catch statement use Options | File Templates.
        }

        if ((ctx == null) || "AddUserView".equals(ctx)) {
            Collection c = (Collection) request.getSession()
                                               .getAttribute("display_rs");

            if (c != null) {
                Iterator it = c.iterator();
                int      iteridx = 0;

                while (it.hasNext()) {
                    try {
                        PropsBean app = (PropsBean) it.next();

                        writer.print("<tbody id = \"row1-1\" ><tr >");
                        writer.print("<td class=\"rowLevel1\" ><html:checkbox property = \"lineitem_" + iteridx + "value = \"true\" </td >");
                        writer.print("<td	class=\"rowLevel1\" ><img border = \"0\"  id = \"widget-row1-5\"  src = \"/spm/images/invisi_shim.gif\" ");
                        writer.print("width = \"11\" height = \"11\" class=\"widget\" >	<img src = \"<webapps:fullPath path='");
                        writer.print(IMAGES_SLASH_DELIM  + (String) app.getValue("objectclass") + ".gif");
                        writer.print("width = \"16\"	height = \"16\" >	<acronym style = \"border-bottom:0px;\"");
                        writer.print("title = \"displayname\" ><bean:write name = \"app\" property = \"displayname\" ");
                        writer.print("filter = \"true\" / ></acronym ></td >");

                        for (int i = 0; i < PRINCIPAL_TYPES.length; i++) {
                            writer.print("<td	align = \"center\" class=\"rowLevel1\" >");

                            if (app.getProperty(PRINCIPAL_TYPES [i]) != null) {
                                if (app.getProperty(PRINCIPAL_TYPES [i])
                                           .equals("direct")) {
                                    writer.print("<img src=\"/spm/images/check.gif\" width =\"13\" height =\"13\">");
                                } else {
                                    writer.print("<img src=\"/spm/images/check_grey.gif\" width =\"13\" height =\"13\">");
                                }
                            } else {
                                writer.print("&nbsp;");
                            }

                            writer.print("</td>");
                        }

                        writer.print(" </tr>");
                        writer.print("</tbody>");
                    } catch (IOException e) {
                        e.printStackTrace(); //To change body of catch statement use Options | File Templates.
                    }
                }
            }
        } else {
            try {
                writer.print("<tr>");
                writer.print("<td class=\"rowLevel1\" ><input type = \"checkbox\" value = \"checkbox\" id = \"box_1\" >");
                writer.print("</td >");
                writer.print("<td class=\"rowLevel1\" ><img src = \"/images/user.gif\">");
                writer.print("width = \"16\"	height = \"16\" > Rahul </td >");
                writer.print("</tr>");
            } catch (IOException e) {
                e.printStackTrace(); //To change body of catch statement use Options | File Templates.
            }
        }

        return SKIP_BODY;
    }
}

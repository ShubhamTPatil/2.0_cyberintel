<%--
     @author Michele Lin
     @author Devendra Vamathevan
     @author Theen-Theen Tan
     @version $Revision$, $Date$
--%>

<%-- This is included from ldap_nav.jsp and iterates through the list of results --%>
<%@ include file="/includes/directives.jsp" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.marimba.apps.subscription.common.LDAPVars" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="static com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants.*" %>

<% String isSearch = request.getParameter("limitSearch"); %>

<logic:iterate id="app" name="page_targets_rs" type="com.marimba.webapps.tools.util.PropsBean">
    <tr>
            <%-- REMIND t3 Shoud high light which target is being selected --%>
            <%-- tr style="background-color:#CFDCED;" --%>
        <td class="rowLevel1">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td width="18" align="right">
                        <logic:present name="app" property="<%="value("+ EXPANDABLE +")" %>" >
                            <a href="<sm:getTargetHRef entry="<%= app %>" />"><img src="/shell/common-rsrc/images/plus.gif" border="0"></a>
                        </logic:present>
                    </td>
                    <td align="left">
                        <img src='<%="/shell/common-rsrc/images/" + (String) app.getValue("type") + ".gif" %>' border="0">&nbsp;
                        <logic:present name="app" property="<%="value("+ ISTARGETABLE +")" %>">
                            <logic:equal name="app" property="<%="value("+ ISTARGETABLE +")" %>" value="true" >
                                <sm:getTargetAssignHRef entry="<%= app %>" /><bean:write name="app" property="value(displayname)" filter="true" /></a>
                            </logic:equal>
                            <logic:notEqual name="app" property="<%="value("+ ISTARGETABLE +")"%>" value="true" >
                                <target="mainFrame" class="hoverLink"><bean:write name="app" property="value(displayname)" filter="true" />
                            </logic:notEqual>
                        </logic:present>
                        <logic:notPresent name="app" property="<%="value("+ ISTARGETABLE +")" %>">
                            <target="mainFrame" class="hoverLink"><bean:write name="app" property="value(displayname)" filter="true" />
                        </logic:notPresent>
                    </td>
                    <td align="right">
                        <logic:present name="app" property="<%= "value("+ COMPLIANCECACHED +")"%>">
                            <a href="#" class="noUnderlineLink" onmouseover="MakeTip('<webapps:pageText key="cached" type="global" escape="js"/>');"
                               onmouseout="CloseTip();"><img src="images/schedule_refresh.gif" border="0"></a>
                        </logic:present>
                    </td>
                    <td align="right">
                            <%-- show OS Template --%>
                        <logic:present name="app" property="<%= "value("+ OSTEMPLATENAME +")"%>">
                            <a href="#" class="noUnderlineLink" onmouseover="MakeTip('<webapps:pageText key="assigntemplate" type="global" escape="js"/>: <bean:write name="app" property="value(ostemplatename)" filter="true" />');"
                               onmouseout="CloseTip();"><img src="/spm/images/WinLogo.gif" width="16" height="16" border="0"></a>
                        </logic:present>
                            <%-- show policy --%>
                        <logic:present name="app" property="<%= "value("+ HASPOLICIES +")"%>">
                            <a href="#" class="noUnderlineLink" onmouseover="MakeTip('<webapps:pageText key="directlyassigned" type="global" escape="js"/>');"
                               onmouseout="CloseTip()"><img src="/shell/common-rsrc/images/policy.gif" border="0"></a>
                        </logic:present>
                    </td>
                    <td>
                </tr>
            </table>
        </td>
    </tr>
</logic:iterate>

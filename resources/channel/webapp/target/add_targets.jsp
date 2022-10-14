<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>


<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="targ_vw" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<body onLoad="syncTables('FOO');" onResize="resizeDataSection('FOO_dataDiv','endOfGui',-1);">
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<%
    session.setAttribute("disablemultimode", "true");
    session.removeAttribute("session_multitgbool");
%>

<%@ include file="/includes/info.jsp" %>
<%@ include file="/includes/MultiViewChannel.jsp" %>

<html:form name="addTargetEditForm" action="/addTargetSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm" onsubmit="return false">

    <% if(null != EmpirumContext) {%>
    <webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
    <% } else { %>
    <webapps:tabs tabset="main" tab="tgtview"/>
    <% } %>

    <script>
        CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','FOO_endOfData',1);");
        CMSOnResizeHandler.addHandler("resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);");
    </script>

    <div id="pageContent">
        <div class="pageHeader"><span class="title"><webapps:pageText key="Title" /></span></div>

        <%@ include file="/includes/help.jsp" %>

        <table cellpadding="0" cellspacing="0" width="100%" >
            <tr>
                <td valign="top" align="right" width="322px">
                    <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0; margin:0; padding:0; display:block; overflow:auto">
                        <iframe src="<webapps:fullPath path="/ldapRemember.do?selectedTab=true" />" width="100%" height="100%" frameborder="0" ></iframe>
                    </div>
                    <div id="FOO_endOfGroupList"></div>
                </td>

                <td valign="middle" align="center" height="100%" width="8px">
                    <img src="/shell/common-rsrc/images/right_arrow_indicator.gif">
                </td>

                <% if (request.getParameter("page") == null) { %>
                <td valign="top" style="padding-left:0;">
                    <logic:present name="session_multipkgbool">
                        <div id="FOO_dataDiv" style="height:100px; border:0;padding:0;margin:0;display:block; overflow:auto;">
                            <iframe name="mainFrame" src="<webapps:fullPath path="/target/add_targets_details_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                        </div>
                        <div id="FOO_endOfData"></div>
                    </logic:present>

                    <logic:notPresent name="session_multipkgbool">
                        <div id="FOO_dataDiv" style="height:100px; border:0; margin:0; padding:0; display:block; overflow:auto;">
                            <iframe name="mainFrame" src="<webapps:fullPath path="/target/add_targets_details_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                        </div>
                        <div id="FOO_endOfData"></div>
                    </logic:notPresent>

                    <% } else { %>

                    <logic:present name="session_multipkgbool">
                        <div id="FOO_dataDiv" style="height:100px; border:0;padding:0;margin:0;display:block; overflow:auto;">
                            <iframe name="mainFrame" src="<webapps:fullPath path="/target/add_targets_details_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                        </div>
                        <div id="FOO_endOfData"></div>
                    </logic:present>

                    <logic:notPresent name="session_multipkgbool">
                        <div id="FOO_dataDiv" style="height:100px; border:0;padding:0;margin:0;display:block; overflow:auto;">
                            <iframe name="mainFrame" src="<webapps:fullPath path="/target/add_targets_details_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                        </div>
                        <div id="FOO_endOfData"></div>
                    </logic:notPresent>
                </td>
                <% } %>
            </tr>
        </table>

        <div id="pageNav">
            <input name="Ok" type="button" class="mainBtn" value="<webapps:pageText key="add" type="button" shared="true"/>" onClick="javascript:submitActionFromFrames(document.addTargetEditForm, '/addTargetValidate.do','2')" >
            &nbsp;
            <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/addTargetCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
        </div><!--end pageContent div-->

    </div>

    <script>
        resizeDataSection('FOO_dataDiv','FOO_endOfData',1);
        resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);
    </script>

</html:form>
</body>
</html>


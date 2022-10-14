<%@ page contentType="text/html;charset=UTF-8" %>
<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Theen-Theen Tan
	@version  $Revision$,  $Date$
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.intf.util.*,
                 com.marimba.tools.ldap.LDAPConnUtils" %>
<%@ page import="com.marimba.webapps.intf.IWebAppsConstants" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import="com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%@ include file="/includes/startHeadSection.jsp" %>
<title><webapps:pageText key="m6" type="global"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">

<%
    String textPfx = "Target";
    String singleViewPage = "/tgviewInit.do";
    String multiViewPage = "/tgviewMultiInit.do";
    String singleDetailPage = "/tgdetailsInit.do";
    String multiDetailPage = "/tgdetailsMultiInit.do";
    String multibool ="session_multitgbool";

    if (IWebAppConstants.PKG_VIEW.equals((String) session.getAttribute(IWebAppConstants.SESSION_RETURN_PAGETYPE))) {
        textPfx = "Package";
        singleViewPage = "/pkgviewInit.do?actionType=initAction";
        multiViewPage = "/pkgviewMultiInit.do";
        singleDetailPage = "/pkgdetailsInit.do";
        multiDetailPage = "/pkgdetailsMultiInit.do";
        multibool = "session_multipkgbool";
    }

    %>

<%@ include file="/includes/endHeadSection.jsp" %>

<body onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>
<div style="text-align: center">
    <div class="commonPadding">

        <div class=pageheader style="padding-bottom:50px;"><span class=title><webapps:pageText key="Title"/></span></div>

        <div class="statusMessage" id="OK" style="width:800px; margin:0 auto;">
            <h6><webapps:pageText key="Confirm"/></h6>
            <p>
                <webapps:pageText key='<%= textPfx + "Desc" %>' /><br>&nbsp;<br>
                    <html:link page="<%=singleViewPage%>"><webapps:pageText key='<%= textPfx + "View" %>'/></html:link> &nbsp;
            </p>
        </div>
    </div>
</div>
<%@ include file="/includes/footer.jsp" %>

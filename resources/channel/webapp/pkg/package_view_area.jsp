<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Rahul Ravulur
	@version  $Revision$,  $Date$
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/info.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<!--%@ include file="/includes/banner.jsp" %-->
<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="pkgview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="pkgview"/>
<% } %>

<%
    session.removeAttribute("disablemultimode");
%>
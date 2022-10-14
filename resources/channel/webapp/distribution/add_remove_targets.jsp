<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)add_remove_targets.jsp

     @author Michele Lin
     @version 1.16, 03/25/2002
--%>

<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>
    <webapps:helpContext context="spm" topic="ar_targ" />
<script>
var singleOptionElements = new Array()
var multiOptionElements = new Array("edit_btn","delete_btn")
</script>
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<%-- Remind mlin: errors will be added to their respective frames
                  But if when we change it so that errors appear
		  in the top frame, the frameset below needs to be expanded  --%>

<%-- frames --%>

<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>
<div id="pageContent">
  <div class="pageHeader"><span class="title"><webapps:pageText key="Title" /></span></div>
	<%@ include file="/includes/help.jsp" %>
    <table cellpadding="0" cellspacing="0">
    <tr>
        <td>
		 <iframe src="<webapps:fullPath path="/ldapRemember.do?selectedTab=true" />" width="310" height="550" frameborder="0"></iframe>
	    </td>
      <td valign="top" style="padding-left:40px; padding-top:24px;">
		 <iframe name="mainFrame" src='<webapps:fullPath path="/distribution/select_exclude.jsp" />' width="580" height="550" frameborder="0"></iframe>
	  </td>
    </tr>
</div>
<%@ include file="/includes/footer.jsp" %>


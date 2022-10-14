<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)waitforcompletion.jsp

     @author Angela Saval
     @version 1.8, 12/20/2001
--%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/headSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%--@ include file="/includes/banner.jsp" --%>
<%-- Javascript --%>
  <script language="JavaScript">
    function autorefresh(){
	window.location = "<webapps:fullPath path="/waitForCompletion.do" />";
    }
    setTimeout("autorefresh()", 3000);
  </script>

<%-- Body content --%>
<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<table width="100%" border="0" cellspacing="0" cellpadding="0" class="generalText" height="100%">
  <tr>
    <td align="center" valign="middle">
      <p><font size="+4"><b><webapps:pageText key="PleaseWait" /></b></font></p>
      <p><img src="/shell/common-rsrc/images/status_animation.gif" width="180" height="72">
      </p>
      <p><b><font size="3"><webapps:pageText key="RequestProcessed" /></font></b></p>
    </td>
  </tr>
</table>

<%@ include file="/includes/footer.jsp" %>

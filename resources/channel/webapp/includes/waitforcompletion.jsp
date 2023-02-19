<%@ page contentType="text/html;charset=UTF-8"%>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)waitforcompletion.jsp

     @author Angela Saval
     @version 1.8, 12/20/2001
--%>
<%@ include file="/includes/directives.jsp"%>
<html>

<head>

<title><webapps:pageText key="Title" /></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<meta http-equiv="Pragma" content="no-cache">

<meta http-equiv="Expires" content="0">

<meta http-equiv="Cache-control" content="no-cache">

<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css" />

<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>


<script language="javascript" src="/shell/common-rsrc/js/master.js"></script>

<script language="javascript" src="/spm/includes/selectoption.js"></script>

<script language="JavaScript" src="/shell/common-rsrc/js/table.js"></script>

<script language="JavaScript" src="/shell/common-rsrc/js/tableSync.js"></script>

<script language="javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>

<script language="javascript" src="/shell/common-rsrc/js/domMenu.js"></script>

<!-- These two lines of code need to be here in order for overlib to work.  They have to be placed outside the form in the head section -->

<div id="overDiv" style="position: absolute; visibility: hidden; z-index: 1000;"></div>

<script language="JavaScript" src="/shell/common-rsrc/js/overlib.js"></script>

<script language="JavaScript" src="/shell/common-rsrc/js/intersect.js"></script>



<style type="text/css">
#pageContent {
	padding-left: 10px;
	padding-right: 10px;
}
</style>

<%@ include file="/includes/common_js.jsp"%>

<%@ include file="/dashboard/header.jsp"%>

<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%--@ include file="/includes/banner.jsp" --%>
<%-- Javascript --%>
<script language="JavaScript">
	$(function() {
		$('#settings').addClass('nav-selected');
	});

	function autorefresh() {
		window.location = "<webapps:fullPath path="/waitForCompletion.do" />";
	}
	setTimeout("autorefresh()", 3000);
</script>

<%@ include file="/includes/endHeadSection.jsp"%>

<%-- Body content --%>
<%-- <% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="cfgview"/>
<% } %> --%>
<body text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">

  <table width="100%" border="0" cellspacing="0" cellpadding="0" class="generalText" height="100%">
    <tr>
      <td align="center" valign="middle">
        <p>
          <font size="+4"><b><webapps:pageText key="PleaseWait" /></b></font>
        </p>
        <p>
          <img src="/shell/common-rsrc/images/status_animation.gif" width="180" height="72">
        </p>
        <p>
          <b><font size="3"><webapps:pageText key="RequestProcessed" /></font></b>
        </p>
      </td>
    </tr>
  </table>

  <%@ include file="/includes/footer.jsp"%>
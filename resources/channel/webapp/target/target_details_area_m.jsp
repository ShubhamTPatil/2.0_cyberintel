<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)target_details_area_m.jsp

     @author Angela Saval
     @version 1.31, 05/30/2002
--%>

<%@ include file="/includes/directives.jsp" %>


<%@ page import = "com.marimba.apps.subscription.common.util.LDAPUtils" %>
<%@ page import = "com.marimba.apps.subscription.common.LDAPVars" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Channel" %>
<%@ page import = "com.marimba.webapps.intf.IMapProperty" %>

<bean:define id="pageStateAction"  value="tgdetailsmultiPageState" toScope="request" />
<bean:define id="forwardPage"  value="/target/target_details_area_m.jsp" toScope="request" />
<bean:define id="pageBeanName" value="target_pkgs_bean" toScope="request" />

<logic:present name="targetsched">
<bean:define id="pageStateAction"  value="tgscheddetailsmultiPageState" toScope="request" />
<bean:define id="forwardPage" value="/target/target_scheddetails_area_m.jsp" toScope="request" />
<% //pageStateAction ="tgscheddetailsPageState"; %>
<% //forwardPage="/target/target_scheddetails_area.jsp"; %>
</logic:present>
<% //String tgForm = "targetDetailsForm"; %>
<% //String checkAction = "tgdetailsCheckAll"; %>
<bean:define id="tgForm"  value="targetDetailsMultiForm" toScope="request" />
<bean:define id="checkAction"  value="tgdetailsMultiCheckAll" toScope="request" />

<%--<% //String pageStateAction = "tgdetailsmultiPageState"; %>--%>
<%--<% //String forwardPage="/target/target_details_area_m.jsp"; %>--%>
<%--<% //String pageBeanName = "target_pkgs_bean"; %>--%>
<%----%>
<%--<logic:present name="targetsched">--%>
<%--<% pageStateAction ="tgscheddetailsmultiPageState"; %>--%>
<%--<% forwardPage="/target/target_scheddetails_area_m.jsp"; %>--%>
<%--</logic:present>--%>
<%--<% String tgForm = "targetDetailsMultiForm"; %>--%>
<%--<% String checkAction = "tgdetailsMultiCheckAll"; %>--%>


<%@ include file="/includes/startHeadSection.jsp" %>
<%@ include file="/includes/MultiTargets.jsp" %>
    <webapps:helpContext context="sm" topic="targ_det" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<style type="text/css">
<!--
.tableRowActionsMenu {
	background-color: #F5F5F5;
	color: #000000;
	padding-top: 3px;
	padding-bottom: 3px;
	padding-right: 5px;
	padding-left: 5px;
	border-top-width: 1px;
	border-top-style: solid;
	border-top-color: #666666;
	background-repeat: repeat-x;
	background-position: left center;
	font-weight: bold;
	cursor: pointer;
}
.tableRowActionsMenuHover {
	background-color: #E5E5E5;
	color: #000000;
	padding-top: 3px;
	padding-bottom: 3px;
	padding-right: 5px;
	padding-left: 5px;
	border-top-width: 1px;
	border-top-style: solid;
	border-top-color: #666666;
	background-repeat: repeat-x;
	background-position: left center;
	font-weight: bold;
	cursor: pointer;
}
.tableRowActionsMenuActive {
	background-color: #000099;
	color: #FFFFFF;
	padding-top: 3px;
	padding-bottom: 3px;
	padding-right: 5px;
	padding-left: 5px;
	border-top-width: 1px;
	border-top-style: solid;
	border-top-color: #435d8d;
	background-repeat: no-repeat;
	font-weight: bold;
	cursor: pointer;
}
.pageMenuItem {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10px;
	font-weight: bold;
	color: #000000;
	padding-top: 3px;
	padding-right: 5px;
	padding-bottom: 3px;
	padding-left: 5px;
	cursor: pointer;
	background-color: #F5F5F5;
}
.groupItemOff {
	font-family: Verdana, Arial, Helvetica, sans-serif;
	font-size: 11px;
	font-weight: normal;
	border-top-width: 1px;
	border-top-style: solid;
	border-top-color: #CCCCCC;
	padding-top: 2px;
	padding-bottom: 2px;
	padding-left: 5px;
	padding-right: 5px;
	cursor: pointer;
}
.groupItemOn {
	font-family: Verdana, Arial, Helvetica, sans-serif;
	font-size: 11px;
	font-weight: normal;
	border-top-width: 1px;
	border-top-style: solid;
	border-top-color: #CCCCCC;
	padding-top: 2px;
	padding-bottom: 2px;
	padding-left: 5px;
	padding-right: 5px;
	color: #000000;
	background-color: #CEDDF2;
	cursor: pointer;
}
.pageMenuSpacer {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10px;
	font-weight: bold;
	color: #000000;
	padding-top: 1px;
	padding-right: 5px;
	padding-bottom: 1px;
	padding-left: 5px;
	cursor: pointer;
	background-color: #F5F5F5;
}
.buttonMenu {
	cursor: hand;
 font-family: Arial, Helvetica, sans-serif;
	font-size: 9px;
	color: #000000;
	background-color: #F0F0F5;
	padding-top: 2px;
	padding-bottom: 2px;
	padding-left: 4px;
	padding-right: 4px;
}
.buttonMenuActive {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 9px;
	background-color:#FFFFFF;
	padding-top: 2px;
	padding-bottom: 2px;
	padding-left: 4px;
	padding-right: 4px;
}
.captionLink {
 text-decoration: none;
	color: #000000;
}
A.captionLink:visited {
	color: #000000;
}
.smallCaption {
 font-family: Arial, Helvetica, sans-serif;
	font-size: 9px;
	color: #000000;
	background-color: #F0F0F5;
	padding-top: 2px;
	padding-bottom: 2px;
	padding-left: 4px;
	padding-right: 4px;
}
-->
</style>

<logic:notPresent name="targetsched">
<%@ include file="/includes/body.html" %>
<%@ include file="/includes/common_js.jsp" %>
</logic:notPresent>


<logic:present name="targetsched">
	<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
	<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>
</logic:present>
 <script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<%-- sets the return page --%>
<%-- session_return is used by distCancel --%>
<logic:notPresent name="targetsched">
	<% session.setAttribute("session_return_page",  "/tgviewMultiInit.do"); %>
</logic:notPresent>
<logic:present name="targetsched">
	<% session.setAttribute("session_return_page",  "/tgdetailsMultiInit.do"); %>
</logic:present>
<%-- session_return is used by distSave --%>
<% session.setAttribute("session_return_pagetype",  "target_view"); %>

<%-- used by target_display_single.jsp to determine where to link to Report Center' machine's details page --%>
<% pageContext.setAttribute("linktorc", "true", PageContext.PAGE_SCOPE); %>


<%-- This is the piece of code used for refreshing the paging. --%>
<webapps:empty parameter="page">
	<sm:getPkgsFromTargets stateBean="<%=tgForm%>" />
</webapps:empty>
<%-- setPagingResults tag puts the page of results to display into display_rs --%>
<sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>"
					 resultsName="page_pkgs_fromtgs_rs" />


<% String hdrTableWidth = "650"; %>
<% String dataTableWidth = "631"; %>
<% String dataSectionHeight = "200"; %>
<logic:present name="targetsched">
<% hdrTableWidth = "900"; %>
<% dataTableWidth = "882"; %>
<% dataSectionHeight = "350"; %>
</logic:present>

<logic:present name="targetsched">
<script>
CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','endOfGui','-1');");
</script>
</logic:present>

<%-- Body content --%>
<html:form name="targetDetailsMultiForm" action="/distEdit.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsMultiForm">
<%-- This present check is here to show or hide the contents on the page depending on if there are targets defined
--%>

<logic:present name="targetsched">
<div style="text-align:center">
<div style="margin-bottom:15px; margin-left:15px; margin-right:15px; ">
</logic:present>

<logic:present name="targetsched">
  <table width="100%" border="0" cellpadding="0">
    <tr valign="top">
      <td class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></td>
    </tr>
  </table>
</logic:present>

<logic:notPresent name="main_page_m_targets">
  <table width="100%" border="0" cellpadding="0" style="margin-top:20px; ">
    <tr>
      <td valign="top" class="textGeneral"><strong><webapps:pageText shared="true" type="target_details_area" key="NoTargetSelectedShort" /></strong>&nbsp;<webapps:pageText shared="true" type="target_details_area" key="NoTargetSelectedLong" /></td>
    </tr>
  </table>
</logic:notPresent>
<logic:present name="main_page_m_targets">

<logic:notPresent name="targetsched">
<div class="sectionInfo" style="width:97%">
<strong><webapps:pageText shared="true" type="target_details_area_m" key="MultipleSelectMode" />.</strong> <webapps:pageText shared="true" type="target_details_area_m" key="MultipleSelectText" />
</div>
</logic:notPresent>

<%-- Errors Display --%>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <%@ include file="/includes/usererrors.jsp" %>
</table>

<div style="width:97%;">

<table width="100%" border="0" cellspacing="0" cellpadding="2"  class="textGeneral">
  <tr>
      <td valign="bottom" class="tableTitle">
            <% int targetCount = 0; %>
            <logic:iterate id="target" name="main_page_m_targets" type="com.marimba.apps.subscription.common.objects.Target">
                <% targetCount++;%>
            </logic:iterate>
            <a href="#" onClick="javascript:parent.showMultiTargets();"> <%= targetCount%>&nbsp;<webapps:pageText key="selectedTargets" type="colhdr" shared="true"/>
            </a>


        </td>
      <td align="right" valign="bottom" nowrap>
						<%-- previous/next --%>
						<logic:notPresent name="targetsched">
							<% request.setAttribute("targetFrame", "mainFrame"); %>
						</logic:notPresent>
						<% //include file="/includes/genPrevNext.jsp" %>
						<jsp:include page="/includes/genPrevNext.jsp" />
	  </td>
  </tr>
</table>
</div>
<jsp:include page="/includes/target_details.jsp" />

</logic:present>

</div>
</div>
</html:form>

<div id="endOfGui"></div>
<logic:present name="main_page_m_targets">
		<script>
		  resizeDataSection('FOO_dataDiv','endOfGui','-1');
				<logic:present name="targetsched">
				syncTables('FOO');
				</logic:present>
		</script>
</logic:present>


</body>
</html>

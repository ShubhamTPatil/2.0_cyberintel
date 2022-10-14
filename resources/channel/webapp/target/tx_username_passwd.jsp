<%@ page contentType="text/html;charset=UTF-8" %>
<%--
// Copyright 1997-2003, Marimba Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
     @author	Angela Saval
     @author    Devendra Vamathevan
--%>


<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="tx_pword" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%@ include file="/includes/body.html" %>


<%String edittranslogin = "" ;%>

<logic:present name="session_edittranslogin">
<%edittranslogin = (String)session.getAttribute("session_edittranslogin");%>
</logic:present>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<logic:notPresent name="taskid">
    <% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>
</logic:notPresent>
<logic:present name="taskid">
    <% request.setAttribute("nomenu", "true"); %>
    <webapps:tabs tabset="bogustabname" tab="noneselected"/>
</logic:present>

<bean:define name="session_tloginbean" id="tloginbean" scope="session" type="com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean"/>
<html:form name="transmitterLoginUserForm" action="/transLoginUserSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TransLoginForm">

<div align="center">

<div style="padding-left:25px; padding-right:25px; margin-left:auto; margin-right:auto;">
  <div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
  <logic:present name="taskid">
      <div class="pageHeader">
          <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
          <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
      </div>
  </logic:present>
		<%-- Errors Display --%>
		<div style="width:100%; ">
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
				  <%@ include file="/includes/usererrors.jsp" %>
				</table>
		</div>
		<%@ include file="/includes/help.jsp" %>

    <table width="100%" border="0" cellspacing="0" cellpadding="3">
      <tr>
        <td align="right" class="col1"><webapps:pageText key="Transmitter_Port" />: </td>
        <td class="col2"><input type="text" name="value(hostname)" size="40" <logic:present name="session_edittranslogin"> value='<%=edittranslogin%>'
        </logic:present>
	     class="requiredField">
        &nbsp; <span class="inactiveText"></span>
        </td>
      </tr>
      <tr>
        <td align="right" class="col1"><webapps:pageText key="Username"/> </td>
        <td class="col2"><input type="text" name="value(username)"
	    <logic:present name="session_edittranslogin">
	    value='<%=tloginbean.getUser(edittranslogin)%>'
	    </logic:present> >
        </td>
      </tr>
      <tr>
        <td align="right" class="col1"><webapps:pageText key="Password"/> </td>
        <td class="col2"><input type="password" name="value(password)">
        </td>
      </tr>
      <tr>
        <td align="right" class="col1"><webapps:pageText key="ConfirmPassword"/> </td>
        <td class="col2"><html:password property="value(passwordConfirm)" value=""/>

        </td>
      </tr>
    </table>
    <div id="pageNav">
        <input type="submit" id="OK" class="mainBtn" value=" <webapps:pageText type="global" key="OK"/> ">
        <input type="button" name="Cancel" value=" <webapps:pageText type="global" key="Cancel" /> " onClick="javascript:send(document.transmitterLoginUserForm,'/transLoginUserCancel.do');" styleId="Cancel">
    </div>
  </div>
</div>
<!--end super div for centering-->

</html:form>


</body>
</html>


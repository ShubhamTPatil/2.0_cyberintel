<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)add_remove_top.jsp

     @author
     @version 1.6, 01/14/2002
--%>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="spm" topic="ar_targ" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/body.html" %>

<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>

<div style="text-align:center;">
  <div id="contentPadding" align="left" style="width:800px;">
    <div class="pageHeader"><span class="title"><bean:message key="page.config.Title"/></span>
    </div>
   <%@include file="/includes/help.jsp" %>
   <jsp:include page="/includes/linktable.jsp" flush="false" />
  </div>
</div>

<%@ include file="/includes/footer.jsp" %>

<% String targetAddAction = request.getContextPath() + "/addRemoveAdd.do"; %>

<%-- Javascript --%>
<script language="JavaScript">
  function redirect(submitaction) {
        var fullpath = "<html:rewrite page='" + submitaction + "' />";
	top.location = fullpath;

  }

    function loadFrame(actionDo) {
    parent.mainFrame.document.location.href = actionDo;
  }
</script>

<html:form name="ldapNavigationForm" action="/ldap_navigation.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.LDAPNavigationForm">
<table width="100%" border="0" cellpadding="5" class="generalText">
  <tr>
    <td><font class="pageTitle"><webapps:pageText key="Title" /></font></td>
  </tr>
  <%-- Errors Display --%>
  <%@ include file="/includes/usererrors.jsp" %>
</table>
</html:form>
</body>
</html:html>
<%-- no copyright since this is top frame --%>

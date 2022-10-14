<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2005, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)ar_confirma-assignment.jsp

     @author Devendra Vamathevan
     @version 1.5, 03/21/2003
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.SubscriptionMain,
                 com.marimba.tools.ldap.LDAPConnUtils" %>
<%@ page import="com.marimba.webapps.intf.IWebAppsConstants" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%
ServletContext context = config.getServletContext();
IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
SubscriptionMain main = TenantHelper.getTenantSubMain(context, request.getSession(), user.getTenantName());
%>
<%@ include file="/includes/startHeadSection.jsp" %>
<title><webapps:pageText key="m6" type="global"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">

<script language="javascript">
function clearVariables() {

    <%  String requestURI = request.getRequestURI();
        int index = requestURI.indexOf("sm/");
        requestURI = requestURI.substring(0, index); %>
        if (window != top) {
            top.location.href = "<%= requestURI %>shell/common-rsrc/login/login.jsp?logout=true";
        } else {
            window.document.location.href = "<%= requestURI %>shell/common-rsrc/login/login.jsp?logout=true";
        }
}
</script>

<%@ include file="/includes/endHeadSection.jsp" %>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()" onbeforeunload="clearVariables();" >
<%
    request.setAttribute("nomenu", "true");
    session.removeAttribute("taskid");
%>
<webapps:tabs tabset="bogustabname" tab="noneselected"/>


<DIV style="TEXT-ALIGN: center">
<DIV style="PADDING-RIGHT: 15px; PADDING-LEFT: 15px; WIDTH: 800px">
<DIV class=pageHeader><SPAN class=title><webapps:pageText key="Title"/></SPAN></DIV>
<div class="statusMessage" id="OK">
   <h6><webapps:pageText key="Confirm"/></h6>

   <p><webapps:pageText key="ARDesc" /><br>&nbsp;<br>
	<%   if (LDAPConnUtils.getInstance(main.getTenantName()).isADWithAutoDiscovery(main.getLDAPConfig())) {
    %>
   <webapps:pageText key="ADreplicate" />
    <% } %>
   </p>
</div>
</DIV>
</DIV>
<%@ include file="/includes/footer.jsp" %>

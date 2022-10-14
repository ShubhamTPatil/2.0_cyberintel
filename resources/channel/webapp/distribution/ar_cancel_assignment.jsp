<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2005, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)ar_cancel_assignment.jsp

     @author Devendra Vamathevan
     @version 1.5, 11/07/2005
--%>
<%@ include file="/includes/directives.jsp" %>
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
    session.removeAttribute("changeid");
%>
<webapps:tabs tabset="bogustabname" tab="noneselected"/>
<DIV style="TEXT-ALIGN: center">
<DIV style="PADDING-RIGHT: 15px; PADDING-LEFT: 15px; WIDTH: 800px">
<DIV class=pageHeader><SPAN class=title><webapps:pageText key="Title"/></SPAN></DIV>
<div class="statusMessage" id="warning">
   <h6><webapps:pageText key="Cancel"/></h6>

   <p><webapps:pageText key="ARDesc" /></p>
</div>    
</DIV>
</DIV>
<%@ include file="/includes/footer.jsp" %>

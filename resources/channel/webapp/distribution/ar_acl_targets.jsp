<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2005, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)ar_acl_targets.jsp

     @author Jayaprakash paramasivam
     @version 1.0, 01/13/2005
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

<p><webapps:pageText key="noaclwritepermission" /></p>
</div>
<div class="itemStatus">
    <table border="0" cellspacing="0" cellpadding="3">
        <tr>
            <td><strong><webapps:pageText key="targets" type="colhdr" shared="true" /></strong></td>
        </tr>
        <logic:iterate id="target" name='targetsACLNoWrite' type="com.marimba.apps.subscription.common.objects.Target">
        <tr>
            <td>
                <bean:define id="ID" name="target" property="id" toScope="request"/>
                <bean:define id="Name" name="target" property="name" toScope="request"/>
                <bean:define id="Type" name="target" property="type" toScope="request"/>
                <jsp:include page="/includes/target_display_single.jsp"/>
            </td>
        </tr>
        </logic:iterate>
    </table>
</div>
</DIV>
</DIV>
<%@ include file="/includes/footer.jsp" %>

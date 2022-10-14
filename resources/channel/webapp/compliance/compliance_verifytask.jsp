<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
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

<%
    request.setAttribute("nomenu", "true");
%>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()" onbeforeunload="clearVariables();">
<webapps:tabs tabset="bogustabname" tab="noneselected"/>
<div style="TEXT-ALIGN: center">
    <div style="PADDING-RIGHT: 15px; PADDING-LEFT: 15px; WIDTH: 800px">
        <div class=pageHeader><SPAN class=title><webapps:pageText key="Title"/></SPAN></div>
        <logic:present name="taskid">
            <div class="pageHeader">
                <span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span>
                <bean:write name="taskid" />
            </div>
        </logic:present>
        <logic:equal name="returnCode" value="true">
        <div class="statusMessage" id="OK">
        <h6>&nbsp;</h6>

        <p><webapps:pageText key="Info" /></p>
        </div>
        </logic:equal>
        <logic:equal name="returnCode" value="false">
        <div class="statusMessage" id="critical">
        <h6>&nbsp;</h6>

        <p><webapps:pageText key="Warning" /></p>
        </div>
        </logic:equal>         
        <div class="itemStatus">
            <table border="0" cellspacing="0" cellpadding="3">
                <tr>
                    <td><strong><webapps:pageText key="percentage" />:</strong></td>
                    <td>
                        <logic:present name="arCompPercentage"><bean:write name="arCompPercentage"/></logic:present>
                        <logic:notPresent name="arCompPercentage"><webapps:pageText key="none" /></logic:notPresent>
                    </td>
                </tr>

                <tr>
                    <td><strong><webapps:pageText key="expiryTime" />:</strong></td>
                    <td>
                        <logic:present name="arExpiryTime"><bean:write name="arExpiryTime"/>&nbsp;<webapps:pageText key="hours" /></logic:present>
                        <logic:notPresent name="arExpiryTime"><webapps:pageText key="none" /></logic:notPresent>
                    </td>
                </tr>
                <tr>
                    <td><strong><webapps:pageText key="schedule" />:</strong></td>
                    <td>
                        <logic:present name="arSchedule"><bean:write name="arSchedule"/></logic:present>
                        <logic:notPresent name="arSchedule"><webapps:pageText key="none" /></logic:notPresent>
                    </td>
                </tr>
                <tr>
                    <td><strong><webapps:pageText shared="true" type="colhdr" key="targets" />:</strong></td>
                    <td>
                    <logic:present name="arVerifyTargets">
                    <logic:iterate id="target" name='arVerifyTargets' type="com.marimba.apps.subscription.common.objects.Target">
                        <bean:define id="ID" name="target" property="id" toScope="request"/>
                        <bean:define id="Name" name="target" property="name" toScope="request"/>
                        <bean:define id="Type" name="target" property="type" toScope="request"/>
                        <jsp:include page="/includes/target_display_single.jsp"/>
                    </logic:iterate>
                    </logic:present>
                    <logic:notPresent name="arVerifyTargets"><webapps:pageText key="none" /></logic:notPresent>
                    </td>
                </tr>
                <tr>
                    <td><strong><webapps:pageText shared="true" type="colhdr" key="pkgs" />:</strong></td>
                    <td>
                    <logic:present name="arVerifyChannels">
                    <logic:iterate id="channel" name="arVerifyChannels">
                       <a href="javascript:void(0);" class="noUnderlineLink" style="cursor:help;" onmouseover="return overlib('<webapps:stringescape><bean:write name="channel" property="url" filter="false" /></webapps:stringescape>', WIDTH, '150', DELAY, '200', LEFT, OFFSETX, 50);" onmouseout="return nd();">
                        <img src="/shell/common-rsrc/images/package.gif" border="0" />
                        <bean:write name="channel" property="title" filter="true"/>
                      </a>&nbsp;
                    </logic:iterate>
                    </logic:present>
                    <logic:notPresent name="arVerifyChannels"><webapps:pageText key="none" /></logic:notPresent>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>
<%@ include file="/includes/footer.jsp" %>

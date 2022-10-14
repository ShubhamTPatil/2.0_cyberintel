<%--
    Copyright 1997-2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/8.1.00/resources/channel/webapp/pkg/package_remove_confirm.jsp

    @author : jcahill 2005/04/19
 --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants"%>
<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Target" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
    List<Target> sameUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_SAMEUSER);
    List<Target> diffUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_DIFFUSER);
%>
<%@ include file="/includes/startHeadSection.jsp" %>

<webapps:helpContext context="sm" topic="targ_vw" />

<%@ include file="/includes/endHeadSection.jsp" %>

<%@ include file="/includes/info.jsp" %>

<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">

<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="pkgview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="pkgview"/>
<% } %>

<html:form name="PackageDetailsForm" action="/packageDeleteSave.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsForm">

<div id="pageContent">

<div class="pageHeader"><span class="title"><webapps:pageText key="Title" /></span></div>
<logic:present name="session_multitgbool">
    <% if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) && (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {%>
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p>
            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            <webapps:pageText key="approval_both_Warning" />
        </p>
    </div>
    <%} else { %>
    <% if(null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) {%>
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p>
            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            <webapps:pageText key="approval_suser_Warning" />
        </p>
    </div>
    <%} else { %>
    <% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p>
            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            <webapps:pageText key="approval_duser_Warning" />
        </p>
    </div>
    <%}} %>
    <%} %>
</logic:present>
<logic:notPresent name="session_multitgbool">
    <% if(null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) {%>
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p>
            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            <webapps:pageText key="approval_suser_Warning" />
        </p>
    </div>
    <%} else { %>
    <% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p>
            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            <webapps:pageText key="approval_duser_Warning" />
        </p>
    </div>
    <%}} %>
</logic:notPresent>


<%-- Errors Display --%>

<table width="100%" border="0" cellspacing="0" cellpadding="0">

    <%@ include file="/includes/usererrors.jsp" %>

</table>

<%@ include file="/includes/help.jsp" %>
<div class="itemStatus">
    <table cellspacing="0" cellpadding="3" border="0">

        <tr>

            <td valign="top"><webapps:pageText key="pkgs" type="colhdr" shared="true"/>:
            </td>

            <td nowrap align="left">
                <logic:present name="main_page_package" >
                    <logic:iterate id="channel" name="main_page_package" type="com.marimba.apps.subscription.common.objects.Channel">
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                        </logic:equal>
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                        </logic:equal>

                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <a style="cursor:help;" target="_blank" href="/sm/getPatches.do?patchGroupUrl=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getUrl())%>&title=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getTitle()) %>" onmouseover="return overlib(wrapDN('<webapps:stringescape><bean:write name="channel" property="url" filter="false" /></webapps:stringescape>', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();">
                        </logic:equal>
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return overlib(wrapDN('<bean:write name="channel" property="url" filter="false" />', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();">
                        </logic:equal>

                        <bean:write name="channel" property="title" filter="true"/>
                        </a>&nbsp;
                    </logic:iterate>
                </logic:present>
                <logic:present name="main_page_m_packages" >
                    <logic:iterate id="channel" name="main_page_m_packages" type="com.marimba.apps.subscription.common.objects.Channel">
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                        </logic:equal>
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                        </logic:equal>

                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <a style="cursor:help;" target="_blank" href="/sm/getPatches.do?patchGroupUrl=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getUrl())%>&title=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getTitle()) %>" onmouseover="return overlib(wrapDN('<webapps:stringescape><bean:write name="channel" property="url" filter="false" /></webapps:stringescape>', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();">
                        </logic:equal>
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return overlib(wrapDN('<bean:write name="channel" property="url" filter="false" />', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();">
                        </logic:equal>

                        <bean:write name="channel" property="title" filter="true"/>
                        </a>&nbsp;
                    </logic:iterate>

                </logic:present>
            </td>

        </tr>

    </table>

</div>


<div class="itemStatus">
    <table border="0" cellspacing="0" cellpadding="3">
        <tr>
            <td><strong><webapps:pageText key="Targets" /></strong></td>
        </tr>
        <logic:iterate id="target" name='page_tgs_todelete' type="com.marimba.apps.subscription.common.objects.Target">
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

<div id="pageNav">

    <input name="Ok" type="button" class="mainBtn" value="<webapps:pageText key="remove" type="button" shared="true"/>" onClick="javascript:send(document.forms.PackageDetailsForm,'/packageDeleteSave.do');" >

    &nbsp;

    <input name="Cancel" type="button" value="<webapps:pageText key="cancel" type="button" shared="true"/>" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/packageDeleteCancel.do');">

</div>



</div><!--end pageContent div-->

</html:form>

</body>

</html>




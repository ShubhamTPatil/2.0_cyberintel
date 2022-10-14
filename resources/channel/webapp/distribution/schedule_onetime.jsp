<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Theen-Theen Tan
	@version  $Revision$,  $Date$
	@since    04/17/2002
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import="com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
    ServletContext context = config.getServletContext();
    IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();

    SubscriptionMain main = TenantHelper.getTenantSubMain(context, session, user.getTenantName());
    boolean isWoWEnabled = main.isWoWEnabled();
    boolean isWoWApplicable = main.isWoWApplicable();
%>
<title><webapps:pageText key="m6" type="global"/></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">

<style type="text/css">
    <!--
    /* These styles are used exclusively for this page*/
    .col1 {
        width: 20%;
    }
    .col2 {
        width: 80%;
    }
    -->
</style>

<script language="javascript" src="/shell/common-rsrc/js/master.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/domMenu.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/domMenu_items2.js"></script>

<%@ include file="/includes/endHeadSection.jsp" %>


<%@ page import="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm" %>
<%@ page import="com.marimba.webapps.intf.IWebAppsConstants" %>
<bean:define id="schedForm" name="scheduleEditForm" scope="session" toScope="request" type="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm"/>

<%
    //    ScheduleEditForm schedForm = (ScheduleEditForm) session.getAttribute("scheduleEditForm");
    boolean hasUpdate = !"initial".equals(schedForm.getType()) && !"secondary".equals(schedForm.getType()) && !"postpone".equals(schedForm.getType());
    boolean hasActive = true;
    boolean isService = false;
    boolean showRadio = true;
%>

<%-- Javascript --%>
<%@ include file="/distribution/schedule.js" %>
<% String action=null; %>
<logic:present name="session_dist" >
    <% action = "/distAsgSchedSave.do";%>
</logic:present>
<logic:present name="add_selected_list" >
    <% action = "/addTargetSchedSave.do";%>
</logic:present>

<html:form name="scheduleEditForm" action='<%=action%>' type="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm">
<%-- Body content --%>

<body onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
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

<div style="text-align:center;">

<div style="padding-left:15px; padding-right:15px;">

<div>
    <td>
        <div class="pageHeader"><span class="title"><webapps:pageText shared="true" type="schedule" key='<%= schedForm.getType() + "PageTitle" %>' /></span></div>
    </td>
</div>

<logic:present name="taskid">
    <div class="pageHeader">
        <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
        <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
    </div>
</logic:present>

<%-- Errors Display --%>
<div>
    <td><%@ include file="/includes/usererrors.jsp" %></td>
</div>

<div class="pageInfo">
    <table cellspacing="0" cellpadding="2" border="0">
        <tr>
            <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>
            <td><webapps:pageText key="IntroShort"/></td>
        </tr>
    </table>
</div>

<div class="itemStatus">
    <table cellspacing="0" cellpadding="2" border="0">
        <tr>
            <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>: </td>
            <logic:present name="session_dist">
                <logic:iterate id="target" name="session_dist" property="targets">
                    <td align="left">
                        <bean:define id="ID" name="target" property="id" toScope="request"/>
                        <bean:define id="Name" name="target" property="name" toScope="request"/>
                        <bean:define id="Type" name="target" property="type" toScope="request"/>
                        <jsp:include page="/includes/target_display_single.jsp"/>
                    </td>
                </logic:iterate>
            </logic:present>
            <logic:notPresent name="session_dist">
                <logic:present name="add_selected_list">
                    <logic:iterate id="target" name="add_selected_list" type="com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap" >
                        <td align="left">
                            <bean:define id="ID" name="target" property="id" toScope="request"/>
                            <bean:define id="Name" name="target" property="name" toScope="request"/>
                            <bean:define id="Type" name="target" property="type" toScope="request"/>
                            <jsp:include page="/includes/target_display_single.jsp"/>
                        </td>
                    </logic:iterate>
                </logic:present>
            </logic:notPresent>
        </tr>
    </table>
</div>

<div class="formTabs">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <colgroup width="0*"/>
        <colgroup width="100%"/>
        <colgroup width="0*"/>

        <tr>
            <td><img src="/shell/common-rsrc/images/form_corner_top_lft.gif" width="5" height="5"></td>
            <td style="height:5px; border-top:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
            <td><img src="/shell/common-rsrc/images/form_corner_top_rt.gif" width="5" height="5"></td>
        </tr>
    </table>
</div>

<div class="formContent" style="text-align:left;">
    <table cellpadding="3" cellspacing="0">
        <%
            String activeType = "START";
            String type = schedForm.getType();
            showRadio = ("postpone".equals(schedForm.getType()) || "initial".equals(schedForm.getType()));
        %>

        <jsp:include page="/distribution/schedule_active.jsp" >
            <jsp:param name="activeType" value="<%=activeType%>"/>
            <jsp:param name="showRadio" value="<%=showRadio%>"/>
            <jsp:param name="type" value="<%=type%>"/>
        </jsp:include>

        <html:hidden property="value(ACTIVATION_PERIOD_START)" />

        <%  activeType = "END";
            if (!"postpone".equals(schedForm.getType())) {
                showRadio = true;
        %>
        <jsp:include page="/distribution/schedule_active.jsp">
            <jsp:param name="activeType" value="<%=activeType%>"/>
            <jsp:param name="showRadio" value="<%=showRadio%>"/>
        </jsp:include>
        <% }%>
        <html:hidden property="value(ACTIVE_PERIOD_SEMANTICS)" />

    </table>

    <logic:equal name="schedForm" property="type" value="initial">

        <table border="0" cellpadding="3" cellspacing="0" width="100%">
            <tr valign="middle">
                <td colspan="2" style="padding-left: 65px;">
                    <% if (isWoWApplicable) { if (isWoWEnabled) {%>
                    <html:checkbox styleId="ENABLE_WOW_ON_INIT" property="value(ENABLE_WOW_ON_INIT)" disabled="false" value="true" />
                    <%} else { %>
                    <input type="checkbox" name='<%="value(ENABLE_WOW_ON_INIT)"%>' id="ENABLE_WOW_ON_INIT"
                           title='<webapps:pageText key="wowdisabled" type="info" shared="true"/>'
                           style="cursor:help" checked="true" value="true" Disabled/>
                    <%}%>
                    <webapps:pageText key='initEnableWOW' />
                    <%}%>
                </td>
            </tr>
        </table>
    </logic:equal>
    <logic:equal name="schedForm" property="type" value="secondary">
        <table border="0" cellpadding="3" cellspacing="0" width="100%">
            <tr valign="middle">
                <td colspan="2" style="padding-left: 65px;">
                    <% if (isWoWApplicable) { if (isWoWEnabled) {%>
                    <html:checkbox styleId="ENABLE_WOW_ON_SEC" property="value(ENABLE_WOW_ON_SEC)" disabled="false" value="true" />
                    <%} else { %>
                    <input type="checkbox" name='<%="value(ENABLE_WOW_ON_SEC)"%>' id="ENABLE_WOW_ON_SEC"
                           title='<webapps:pageText key="wowdisabled" type="info" shared="true"/>'
                           style="cursor:help" value="true" Disabled/>
                    <%}%>
                    <webapps:pageText key='secEnableWOW' />
                    <%}%>
                </td>
            </tr>
        </table>
    </logic:equal>

</div>

<div class="formBottom">
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
            <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
        </tr>
    </table>
</div>

<div id="pageNav">
    <input type="submit" id="OK" class="mainBtn" value=" <webapps:pageText key="OK" type="global" /> ">
    &nbsp;
    <logic:present name="session_dist">
        <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.scheduleEditForm, '/distAsgSchedSave.do?action=cancel');" >
    </logic:present>
    <logic:notPresent name="session_dist">
        <logic:present name="add_selected_list">
            <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.scheduleEditForm, '/addTargetSchedSave.do?action=cancel');" >
        </logic:present>
    </logic:notPresent>
</div>

</html:form>

</div>
</div>

<script>
    initDisabled();
</script>

<%@ include file="/includes/footer.jsp" %>
<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@version  $Revision$,  $Date$
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>

<%@ page import = "java.util.*, com.marimba.apps.subscriptionmanager.webapp.actions.PersistifyChecksAction" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Channel" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import = "com.marimba.webapps.intf.IMapProperty" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>

<jsp:useBean id="session_dist" class="com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean" scope="session"/>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<%@ include file="/includes/startHeadSection.jsp" %>

<webapps:helpContext context="spm" topic="common_sched" />

<title><webapps:pageText key="m6" type="global"/></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<%@ include file="/includes/endHeadSection.jsp" %>

<%
    // Get the channels from the page result set returned from the multiple selection.
    IMapProperty formbean = (IMapProperty) session.getAttribute("distAsgForm");
    PersistifyChecksAction.SelectedRecords pkgSel = (PersistifyChecksAction.SelectedRecords)session.getAttribute(
            (String)formbean.getValue(IWebAppConstants.SESSION_PERSIST_SELECTED));
    Collection pkgs = new ArrayList();
    if (pkgSel != null) {
        pkgs = pkgSel.getTargetChannelMaps();
    }
    pageContext.setAttribute("pkgs", pkgs);
%>

<body onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
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

<html:form name="distAsgForm" action="/distSetStates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm">

    <html:hidden property="forward" />

<div style="text-align:center;">

<div style="padding-left:15px; padding-right:15px;">

<div class="pageHeader" style="width:100%;"><span class="title"><webapps:pageText key="Title"/></span></div>

<logic:present name="taskid">
    <div class="pageHeader" style="width:100%;">
        <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
        <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
    </div>
</logic:present>

<div class="pageInfo" style="width:100%;">
    <table cellspacing="0" cellpadding="2" border="0">
        <tr>
            <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>
            <td><webapps:pageText key="IntroShort"/></td>
        </tr>
    </table>
</div>
    <%-- Errors Display --%>

<table width="99%" border="0" cellspacing="0" cellpadding="0" align="center">
    <%@ include file="/includes/usererrors.jsp" %>
</table>

<div class="itemStatus" style="width:100%;">
    <table cellspacing="0" cellpadding="3" border="0">
        <tr>
            <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>:	  </td>
            <td align="left">
                <logic:iterate id="target" name="session_dist" property="targets">
                    <% //String tgLabel="target"; %>
                    <bean:define id="ID" name="target" property="id" toScope="request"/>
                    <bean:define id="Name" name="target" property="name" toScope="request"/>
                    <bean:define id="Type" name="target" property="type" toScope="request"/>
                    <jsp:include page="/includes/target_display_single.jsp"/>
                    &nbsp;
                </logic:iterate>
            </td>
        </tr>

        <tr>
            <td valign="top"><webapps:pageText key="pkgs" type="colhdr" shared="true"/>:</td>

            <td align="left">
                <logic:iterate id="app" name="pkgs">
                    <a href="javascript:void(0);" class="noUnderlineLink" style="cursor:help;" onmouseover="return Tip('<webapps:stringescape><bean:write name="app" property="url" filter="false" /></webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();">
                        <img src="/shell/common-rsrc/images/package.gif" border="0" />
                        <bean:write name="app" property="title" filter="true"/>
                    </a>&nbsp;
                </logic:iterate>
            </td>
        </tr>
    </table>

</div>

<table width="99%" border="0" cellspacing="0" cellpadding="3" align="center">
<colgroup span="2" width="50%"/>
<tr>
    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="textWhite">
                                <strong><webapps:pageText key="priStateTitle" shared="true" type="distribution_assignment"/></strong>
                            </td>
                            <td align="right">
                                <input name="Submit3" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.distAsgForm, '/distAsgSchedEdit.do?schedType=initial');" >
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr>
                <td valign="top">
                    <logic:present name="session_dist" property="initSchedule">
                        <logic:equal name="session_dist" property="initSchedule" value="inconsistent">
                            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal>
                    </logic:present>
                    <sm:scheduleDisplay name="session_dist" property="initSchedule" type="initial" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                </td>
            </tr>
        </table>
    </td>

    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td height="21" class="textWhite">
                                <strong><webapps:pageText key="updateTitle" shared="true" type="distribution_assignment"/></strong>
                            </td>
                            <td align="right">
                                <input name="Submit33" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.distAsgForm, '/distAsgSchedEdit.do?schedType=update');" >
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr>
                <td valign="top">
                    <logic:present name="session_dist" property="updateSchedule">
                        <logic:equal name="session_dist" property="updateSchedule" value="inconsistent">
                            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal>
                    </logic:present>
                    <sm:scheduleDisplay name="session_dist" property="updateSchedule" type="udpate" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                </td>
            </tr>

        </table>
    </td>

</tr>

<tr>
    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="textWhite">
                                <strong><webapps:pageText key="secStateTitle" shared="true" type="distribution_assignment"/></strong>
                            </td>

                            <td align="right">
                                <input name="Submit342" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.distAsgForm, '/distAsgSchedEdit.do?schedType=secondary');" >
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr>
                <td valign="top">
                    <logic:present name="session_dist" property="secSchedule">
                        <logic:equal name="session_dist" property="secSchedule" value="inconsistent">
                            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal>
                    </logic:present>
                    <sm:scheduleDisplay name="session_dist" property="secSchedule" type="secondary" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                </td>
            </tr>

        </table>
    </td>

    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="textWhite">
                                <strong><webapps:pageText key="verifyTitle" shared="true" type="distribution_assignment" /></strong>
                            </td>
                            <td align="right">
                                <input name="Submit342" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.distAsgForm, '/distAsgSchedEdit.do?schedType=verrepair');" >
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr>
                <td valign="top">
                    <logic:present name="session_dist" property="verRepairSchedule">
                        <logic:equal name="session_dist" property="verRepairSchedule" value="inconsistent">
                            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal>
                    </logic:present>
                    <sm:scheduleDisplay name="session_dist" property="verRepairSchedule" type="verrepair" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                </td>
            </tr>
        </table>
    </td>

</tr>
<tr>
    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="textWhite">
                                <strong><webapps:pageText key="postponeTitle" shared="true" type="distribution_assignment" /></strong>
                            </td>
                            <td align="right">
                                <input name="Submit342" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.distAsgForm, '/distAsgSchedEdit.do?schedType=postpone');" >
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top">
                    <logic:present name="session_dist" property="postponeSchedule">
                        <logic:equal name="session_dist" property="postponeSchedule" value="inconsistent">
                            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal>
                    </logic:present>
                    <sm:scheduleDisplay name="session_dist" property="postponeSchedule" type="postpone" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>

<div id="pageNav" style="width:100%;">
    <input name="Ok" type="button" class="mainBtn" onClick="javascript:send(document.distAsgForm, '/distAsgSchedCommonSave.do');" value="<webapps:pageText key="OK" type="global" shared="true"/>">
    &nbsp;
    <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/distAsgSchedCommonSave.do?action=cancel');" value="<webapps:pageText key="Cancel" type="global" shared="true"/>">
</div>

</div>

<!--end supder div for padding-->

</div>

<!--end super div for centering-->

</html:form>

<%@ include file="/includes/footer.jsp" %>
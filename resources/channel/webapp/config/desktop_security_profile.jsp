<%--
// Copyright 2015, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

// $File$, $Revision$, $Date$

// author: Selvaraj Jegatheesan

--%>
<html>
<head>

<webapps:helpContext context="sm" topic="desktop_security_profiles"/>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>    
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" %> 

<%
 Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

    <title>
        <logic:equal name="desktopSecurityProfileForm" property="create" value="true">
            <webapps:pageText key="TitleAdd"/>
        </logic:equal>

        <logic:notEqual name="desktopSecurityProfileForm" property="create" value="true">
            <webapps:pageText key="TitleEdit"/>
        </logic:notEqual>
    </title>

    <link rel="stylesheet" type="text/css" href="/shell/common-rsrc/css/main.css" />
	<link rel="stylesheet" type="text/css" href="/shell/common-rsrc/assets/jquery/easyui/themes/default/easyui.css">
    <link rel="stylesheet" type="text/css" href="/shell/common-rsrc/assets/jquery/easyui/themes/icon.css">

    <script type="text/javascript" src="/shell/common-rsrc/assets/jquery/easyui/jquery.min.js"></script>
    <script type="text/javascript" src="/shell/common-rsrc/assets/jquery/easyui/jquery.easyui.min.js"></script>

    <style type="text/css">
        .panel-body {
            font-size: 11px;
        }
        .tabs-title {
            font-size: 11px;
        }
        .tabs-container {
            font-size: 11px;
            overflow: hidden;
            font-family:Tahoma,Verdana,Arial,Helvetica,sans-serif !important;
        }
    </style>

    <script type="text/javascript">

        $(document).ready(function() {

            $('#securitypolicytabs').tabs({
                plain: true,
                narrow: true
            });

            $('#minPwdStrengthVal').numberspinner({
                required: true,
                min: 1,
                max: 14,
                increment: 1
            });

            $('#maxPwdAgeVal').numberspinner({
                required: true,
                min: 0,
                max: 999,
                increment: 30
            });

            $('#minPwdAgeVal').numberspinner({
                required: true,
                min: 0,
                max: 999,
                increment: 30
            });

            $('#forcedLogoutTimeVal').numberspinner({
                required: true,
                min: 0,
                increment: 3600
            });

            $('#enforcePwdHistoryVal').numberspinner({
                required: true,
                min: 0,
                max: 24,
                increment: 3
            });

            $('#accountLockoutThresholdVal').numberspinner({
                required: true,
                min: 0,
                max: 999,
                increment: 5
            });

            $('#resetAccountLockoutCounterVal').numberspinner({
                required: true,
                min: 1,
                max: 99999,
                increment: 30
            });

            $('#accountLockoutCounterVal').numberspinner({
                required: true,
                min: 1,
                max: 99999,
                increment: 30
            });

        });

        function loadSoftwarePolicyTab() {
            if (document.desktopSecurityProfileForm.showAppTab.value == 'machine') {
                $('#securitypolicytabs').tabs({selected: 1});
            } else if (document.desktopSecurityProfileForm.showAppTab.value == 'password') {
                $('#securitypolicytabs').tabs({selected: 2});
            } else if (document.desktopSecurityProfileForm.showAppTab.value == 'app') {
                $('#securitypolicytabs').tabs({selected: 3});
            } else {
                $('#securitypolicytabs').tabs({selected: 0});
            }
        }

        function showAllContentsOfThePage() {
            document.getElementById("pageContents").style.display = "block";
        }

        function saveState(form, forwardaction) {
            document.desktopSecurityProfileForm.showAppTab.value == 'app'

            if(document.getElementById('forceApplyId').checked == true) {
                document.getElementById('forceApplyEnabled').value = "true";
            } else {
                document.getElementById('forceApplyEnabled').value = "false";
            }

            if(document.getElementById('immediateUpdateId').checked == true) {
                document.getElementById('immediateUpdateEnabled').value = "true";
            } else {
                document.getElementById('immediateUpdateEnabled').value = "false";
            }

            readAppsState(form);

            var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";
            form.action = fullpath;
            form.submit();
        }

        function readAppsState(form) {
            var elements = form.elements;
            var allowedList = "";
            var blockedList = "";

            for (var i = 0; i < elements.length; ++i) {
                if (elements[i].type == 'radio' && elements[i].value == "allowed" && elements[i].checked) {
                    allowedList = allowedList + elements[i].name + ";" ;
                }

                if (elements[i].type == 'radio' && elements[i].value == "blocked" && elements[i].checked) {
                    blockedList = blockedList + elements[i].name + ";";
                }
            }

            document.getElementById('allowedAsStr').value = allowedList;
            document.getElementById('blockedAsStr').value = blockedList;
        }

        /*function addApp(form, action) {
            alert("value: " + document.getElementById('selectedApp'));
            alert("value: " + document.getElementById('selectedApp').value);
        }*/

        function removeApp(form, action, app) {
            form.selected.value = app;
            saveState(form, action);
        }

        function showSpinnerForEnabledPolicies() {
            handleUserScreenTimeout();
            handleUserForceSpecificScreenSaver();
        }

        function handleUserScreenTimeout() {
            if ($( "#userScreenTimeout:checked" ).val() == "true") {
                document.getElementById("userScreenTimeoutDiv").style.display = "block";
                $('#userScreenTimeoutVal').numberspinner({ required: true, min: 60, increment: 300 })
            } else {
                document.getElementById("userScreenTimeoutDiv").style.display = "none";
                $('#userScreenTimeoutVal').numberspinner({ required: false })
            }
        }

        function handleUserForceSpecificScreenSaver() {
            if ($( "#userForceSpecificScreen:checked" ).val() == "true") {
                document.getElementById("userForceSpecificScreenDiv").style.display = "block";
                $('#userForceSpecificScreenVal').textbox({ required: true })
            } else {
                document.getElementById("userForceSpecificScreenDiv").style.display = "none";
                $('#userForceSpecificScreenVal').textbox({ required: false })
            }
        }

    </script>
</head>

<body>

<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>

<html:form name="desktopSecurityProfileForm" action="/desktopSecurityProfile.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DesktopSecurityProfileForm">
<div id="pageContents" style="display: none;">
<div align="center">
<div id="contentPadding">
    <html:hidden property="create"/>
    <html:hidden property="selected"/>
    <html:hidden property="showAppTab"/>
    <html:hidden styleId="blockedAsStr" property="blockedAsStr"/>
    <html:hidden styleId="allowedAsStr" property="allowedAsStr"/>
    <html:hidden styleId="forceApplyEnabled" property="forceApplyEnabled"/>
    <html:hidden styleId="immediateUpdateEnabled" property="immediateUpdateEnabled"/>

    <div class="pageHeader">
        <span class="title">
            <logic:equal name="desktopSecurityProfileForm" property="create" value="true">
                <webapps:pageText key="TitleAdd"/>
            </logic:equal>

            <logic:notEqual name="desktopSecurityProfileForm" property="create" value="true">
                <webapps:pageText key="TitleEdit"/>
            </logic:notEqual>
        </span>
    </div>

    <%@ include file="/includes/usererrors.jsp" %>
    <%@ include file="/includes/help.jsp" %>

    <div style="width:1000px">
        <div align="left" style="padding-left:8px;">
            <table border="0" cellspacing="0" cellpadding="3">
                <tr >
                    <td align="left"><webapps:pageText key="label.profileName"/></td>
                    <td>
                        <logic:equal name="desktopSecurityProfileForm" property="create" value="true">
                            <html:text property="name" size="30" maxlength="30" styleClass="requiredField"/>
                        </logic:equal>

                        <logic:notEqual name="desktopSecurityProfileForm" property="create" value="true">
                            <html:hidden property="name"/>
                            <b><bean:write name="desktopSecurityProfileForm" property="name"/></b>
                        </logic:notEqual>
                    </td>
                </tr>

                <tr align="left">
                    <td ><webapps:pageText key="label.profileDesc"/></td>
                    <td >
                        <html:text property="description" size="50" maxlength="100"/>
                    </td>
                </tr>

                <tr align="left">
                    <td align="right"><html:checkbox name="desktopSecurityProfileForm" styleId="forceApplyId" property="<%= "value("+ ISubscriptionConstants.DESKTOP_SECURITY_FORCE_APPLY +")" %>" value="true" /></td>
                    <td>
                        <webapps:pageText key="policyapplylabel"/>
                    </td>
                </tr>

                <tr align="left">
                    <td align="right"><html:checkbox name="desktopSecurityProfileForm" styleId="immediateUpdateId" property="<%= "value("+ ISubscriptionConstants.DESKTOP_SECURITY_IMMEDIATE_UPDATE +")" %>" value="true" /></td>
                    <td>
                        <webapps:pageText key="immediateUpdateLabel"/>
                    </td>
                </tr>

                <tr><td colspan="2"> &nbsp;</td> </tr>
            </table>
        </div>

        <logic:present scope="request" name="result">
            <bean:define id="res" name="result"/>
            <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
            <font class="greenText"><b><font class="generalText"> <%= res %> </font></b></font>
        </logic:present>

        <div id="securitypolicytabs" class="easyui-tabs" style="height:auto;border:false">

            <div title="User Access Policy" style="padding:10px">
                <%@ include file="/config/desktop_security_user_access.jsp" %>
            </div>

            <div title="Machine Access Policy" style="padding:10px">
                <%@ include file="/config/desktop_security_machine_access.jsp" %>
            </div>

            <div title="Password Policy" style="padding:10px">
                <%@ include file="/config/desktop_security_password_policy.jsp" %>
            </div>

            <div title="Software Access Policy" style="padding:10px">
                <%@ include file="/config/desktop_security_software.jsp" %>
            </div>

        </div>

        <div id="pageNav">
            <logic:equal name="desktopSecurityProfileForm" property="create" value="true">
                <input type="button" class="mainBtn" name="save" value=" <webapps:pageText key="save" type="global" /> " onClick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityProfile.do?action=save');">
            </logic:equal>

            <logic:notEqual name="desktopSecurityProfileForm" property="create" value="true">
                <input type="button" class="mainBtn" name="apply" value=" <webapps:pageText key="saveandapply" type="global" /> " onClick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityProfile.do?action=apply');">
            </logic:notEqual>

            <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=load');" >
        </div>
    </div>

</div>
</div>
</div>

</html:form>

<script>
    loadSoftwarePolicyTab();
    showAllContentsOfThePage();
    showSpinnerForEnabledPolicies();
</script>

</body>
</html>

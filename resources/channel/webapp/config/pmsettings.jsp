<%--
 Copyright 2004-2015, BMC Software Inc. All Rights Reserved.
 Confidential and Proprietary Information of BMC Software Inc.
 Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
 6,381,631, and 6,430,608. Other Patents Pending.

 $File$

 @author    Theen-Theen Tan
 @version   $Revision$, $Date$

--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="static com.marimba.apps.subscriptionmanager.intf.IWebAppConstants.*" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IARTaskConstants" %>

<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<script type="text/javascript" src="/sm/includes/jquery-1.8.2.min.js"></script>
<webapps:helpContext context="sm" topic="performance" />

<%@ include file="/includes/endHeadSection.jsp" %>

<%
    Object EmpirumContext = session.getAttribute(EMPIRUM_APP_MAIN);
%>
<%-- Body content --%>
<body onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>
<html:form name="pmSettingsForm" action="/pmSettingsSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PMSettingsForm">
<div align="center">
    <div class="commonPadding">
        <div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
        <%@ include file="/includes/help.jsp" %>
        <%@ include file="/includes/usererrors.jsp" %>
        <div style="width:90%">
            <div class="formTabs">
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_lft.gif" width="5" height="5"></td>
                        <td width="100%" style="border-top:1px solid #CCCCCC; height:5px;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="4"></td>
                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_rt.gif" width="5" height="5"></td>
                    </tr>
                </table>
            </div>
            <div class="formContent" align="left">
                <table cellpadding="5" cellspacing="5">
                   <logic:equal name="pmSettingsForm" property="cloudEnabled" value="false">
                    <tr>
                        <td><html:checkbox name="pmSettingsForm" property="<%= "value("+ PUSH_ENABLED_GUI +")" %>" value="true" /></td>
                        <td><webapps:pageText key="Push" /></td>
                    </tr>
                    <tr>
                        <td><html:checkbox name="pmSettingsForm" property="<%= "value("+ IARTaskConstants.AR_TASK_ID_ENABLE +")" %>" value="true" /></td>
                        <td><webapps:pageText key="task" /></td>
                    </tr>
                    <tr>
                        <td><html:checkbox name="pmSettingsForm" property="<%= "value("+ COMPUTER_UNASSIGN_OTHER_GP_ENABLED +")" %>" value="true" /></td>
                        <td><webapps:pageText key="enabledUnassign" /></td>
                    </tr>
                    <tr>
                        <td><html:checkbox name="pmSettingsForm" styleId="ch_box_site_based_policy" property="<%= "value("+ ENABLE_SITE_BASED_DEPLOYMENT +")" %>" value="true" /></td>
                        <td><webapps:pageText key="sitebased.policy" /></td>
                    </tr>
                    <tr id="tx_txtbox" style="display:none;">
                        <td>&nbsp;</td>
                        <td>
                            Master Transmitter URL : <html:text property="masterTxURL" size="50" styleClass="requiredField"/>
                            <webapps:errorsPresent property="masterTxURL">
                                &nbsp; <img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" border="0">
                            </webapps:errorsPresent>
                        </td>
                    </tr>
                   </logic:equal>
                     <tr>
	                     <td><html:checkbox name="pmSettingsForm" property="<%= "value("+ ENABLE_USER_CONTROLLED_DEPLOYMENT +")" %>" value="true" /></td>
                         <td><webapps:pageText key="enableUserControlled" /></td>
                    </tr>
                    <tr>
		                <td><html:checkbox name="pmSettingsForm" property="<%= "value("+ ENABLE_AUTOSCAN_FEATURE +")" %>" value="true" /></td>
                        <td><webapps:pageText key="enableAutoScan" /></td>
                    </tr>
                    <tr>
			            <td><html:checkbox name="pmSettingsForm" property="<%= "value("+ ENABLE_USER_CENTRIC_DEPLOYMENT +")" %>" value="true" /></td>
                        <td><webapps:pageText key="enableUserCentric" /></td>
                    </tr>
					<tr>
                        <td><html:checkbox name="pmSettingsForm" styleId="ch_box_policymanager_start_location" property="<%= "value("+ ENABLE_POLICY_START_LOCATION_ENABLED +")" %>" value="true" /></td>
                        <td><webapps:pageText key="start.location.enabled" /></td>
                    </tr>
                    <tr id="startlocation_txtbox" style="display:none;">
                        <td>&nbsp;</td>
                        <td>
                            <webapps:pageText key="startLocation" /> : <html:text property="startLocationPath" size="80" styleClass="requiredField"/>
                            <webapps:errorsPresent property="startLocationPath">
                                &nbsp; <img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" border="0">
                            </webapps:errorsPresent>
                        </td>
                    </tr>
                </table>
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

            <div id="pageNav" style="margin-top: 20px;">
                <input type="submit" class="mainBtn" name="save" value=" <webapps:pageText key="OK" type="global" /> ">
                <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.pmSettingsForm,'/pmSettingsCancel.do');" >
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    $('#ch_box_site_based_policy').on("click", function () {
        if (this.checked) {
            $('#tx_txtbox').show();
        } else {
            $('#tx_txtbox').hide();
        }
    });

    var siteBasedPolicy = '<bean:write name="pmSettingsForm" property="<%= "value("+ ENABLE_SITE_BASED_DEPLOYMENT +")" %>"/>';

    if (siteBasedPolicy == 'true') {
        $('#tx_txtbox').show();
    } else {
        $('#tx_txtbox').hide();
    }
	$('#ch_box_policymanager_start_location').on("click", function () {
        if (this.checked) {
            $('#startlocation_txtbox').show();
        } else {
            $('#startlocation_txtbox').hide();
        }
    });

    var startLocationEnabled = '<bean:write name="pmSettingsForm" property="<%= "value("+ ENABLE_POLICY_START_LOCATION_ENABLED +")" %>"/>';

    if (startLocationEnabled == 'true') {
        $('#startlocation_txtbox').show();
    } else {
        $('#startlocation_txtbox').hide();
    }
</script>

</html:form>

<%@ include file="/includes/footer.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001-2002, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.

     @author	Jean Ro
     @version 	1.0, 07/27/2004
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.forms.*" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="compliance_options" />

<script language="javascript">
var show_compliance_options = new Array("disable_settings","enable_settings");
var show_cache = new Array("cache_onlyall_settings","cache_all_settings","cache_list_settings");
var tn_tx_update_type = new Array("tn_tx_compliance_refresh_never_settings","tn_tx_compliance_refresh_daily_settings","tn_tx_compliance_refresh_weekly_settings","tn_tx_compliance_refresh_monthly_settings")
var tn_tx_hourType = new Array("tn_tx_hoursec2_settings","tn_tx_hoursec_settings")

function showUpdateSection(U,v,p) {
	P = (p == null) ? "" : p+"_";
	document.getElementById(P+"compliance_refresh_time_settings").style.display = (v!=P+"compliance_refresh_never") ? "" : "none";
	if(p != null && p == "app") {
	    if(document.forms[0].swUsage.value!="true") {
	        document.getElementById("app_usage").disabled = true;
		document.getElementById("app_usage_label").className = "disabledLabel";
	    }
	}
	showSection(U,v)
}

function doSubmit(actionName, targetName) {
    var F = document.setComplianceForm;
    F.action.value = actionName;
    if(targetName != null) F.targetname.value = targetName;
    F.submit();
}
</script>
<style type="text/css">
<!--
/* These styles are used exclusively for this page*/

.fieldsetPadding {
	padding-top: 5px;
	padding-bottom: 5px;
}
.col1 {
	width: 25%;
}
.col2 {
	width: 75%;
}
.spacing1 {
	margin-top:10px;
	margin-left:9px;
}
.spacing2 {
	margin-bottom:10px;
	margin-top:5px;
	margin-left:9px;
}
-->
</style>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
 
<%-- Body content --%>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>
<html:form name="setComplianceForm" action="/complianceSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.SetComplianceForm">
    <html:hidden property="action"/>
    <html:hidden property="targetname"/>
    <div style="text-align:center;">
        <div style="padding-left:15px; padding-right:15px;">
            <div class="pageHeader" style="width:700px;">
                <span class="title"><webapps:pageText key="Title"/></span>
            </div>

            <div class="contentPadding" style="width:700px;">
                <%-- Errors Display --%>
                <%@ include file="/includes/usererrors.jsp" %>
                <%@ include file="/includes/help.jsp" %>
            </div>

            <div style="width:700px; text-align:left; margin-top:20px;" class="textGeneral">
                <webapps:pageText key="enablespm.question.inventory"/>&nbsp;&nbsp;&nbsp;
                <html:select styleId="collectCompEnabledOption" property="collectCompEnabled">
                    <html:option value="enable"><webapps:pageText key="enablespm.enable"/></html:option>
                    <html:option value="disable"><webapps:pageText key="enablespm.disable"/></html:option>
                </html:select>
            </div>

            <div style="width:700px; text-align:left; margin-top:20px;" class="textGeneral">
                <webapps:pageText key="hostnameinfo.label"/>&nbsp;&nbsp;&nbsp;
                <logic:present name="setComplianceForm" property="hostName" scope="session">
                    <bean:write name="setComplianceForm" property="hostName" scope="session"/>
                </logic:present>
                <logic:notPresent name="setComplianceForm" property="hostName" scope="session">
                    <webapps:pageText key="hostnameinfo.notset"/>
                </logic:notPresent>
            </div>

            <div style="width:700px; text-align:left; margin-top:20px;" class="textGeneral">
                <webapps:pageText key="enablespm.question"/>&nbsp;&nbsp;&nbsp;
                <html:select styleId="calcCompEnabledOption" property="calcCompEnabled" onchange="showSection(show_compliance_options,options[selectedIndex].value)">
                    <html:option value="enable"><webapps:pageText key="enablespm.enable"/></html:option>
                    <html:option value="disable"><webapps:pageText key="enablespm.disable"/></html:option>
                </html:select>
            </div>

            <div id="disable_settings" class="textGeneral" style="text-align:left; width:700px; margin-top:20px; display:none;">
                <div style="width:700px; text-align:left;">
                    <h3><webapps:pageText key="refreshSched.hdr"/></h3>
                </div>
                <div class="sectionInfo" style="margin-bottom:15px; ">
                    <webapps:pageText key="refreshSched.sectInfo.disabled"/>
                </div>
                <fieldset>
                    <legend><webapps:pageText key="refreshSched.hdr"/></legend>
                    <table border="0" cellspacing="0" cellpadding="3" style="margin:10px; ">
                        <tr>
                            <td align="right"><webapps:pageText key="refresh.freq"/></td>
                            <td>
                                <logic:present name="setComplianceForm" property="readOnlySchedule" scope="session">
                                    <bean:write name="setComplianceForm" property="readOnlySchedule" scope="session"/>
                                </logic:present>
                                <logic:notPresent name="setComplianceForm" property="readOnlySchedule" scope="session">
                                    <webapps:pageText key="refreshSched.notset"/>
                                </logic:notPresent>
                            </td>
                        </tr>
                    </table>
                </fieldset>
            </div>

            <div id="enable_settings" style="margin-top:20px; display:; ">
                <div style="width:700px; text-align:left;">
                    <h3><webapps:pageText key="refreshSched.hdr"/></h3>
                </div>
                <div style="width:700px;" class="sectionInfo">
                    <webapps:pageText key="refreshSched.sectInfo.enabled"/>
                </div>
                <fieldset style="width:700px;">
                    <legend><webapps:pageText key="refreshSched.hdr"/></legend>
                    <table border="0" cellspacing="0" cellpadding="3" style="margin:10px; ">
                        <tr valign="middle">
                            <td align="right" nowrap class="col1"><webapps:pageText key="refresh.freq"/></td>
                            <td colspan="2" class="col2">
                                <html:select styleId="tn_tx" property="value(tn_tx_compliance_refresh_frequency)" onchange="showUpdateSection(tn_tx_update_type,this.options[this.selectedIndex].value,this.id)">
                                    <html:option value="tn_tx_compliance_refresh_never"><webapps:pageText key="never" type="schedule" shared="true"/></html:option>
                                    <html:option value="tn_tx_compliance_refresh_daily"><webapps:pageText key="daily" type="schedule" shared="true"/></html:option>
                                    <html:option value="tn_tx_compliance_refresh_weekly"><webapps:pageText key="weekly" type="schedule" shared="true"/></html:option>
                                    <html:option value="tn_tx_compliance_refresh_monthly"><webapps:pageText key="monthly" type="schedule" shared="true"/></html:option>
                                </html:select>
                            </td>
                        </tr>

                        <tbody id="tn_tx_compliance_refresh_never_settings" style="display:none;">
                        </tbody>

                        <tbody id="tn_tx_compliance_refresh_daily_settings" style="display:none;">
                            <tr valign="middle">
                                <td align="right" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" class="col2">&nbsp;</td>
                            </tr>
                            <tr valign="middle">
                                <td align="right" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" class="col2">
                                <webapps:errorsPresent property="tn_tx_days">
                                    <span class="textRed"><img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" align="absmiddle">
                                </webapps:errorsPresent>

                                <label id="tn_tx_days_label"><webapps:pageText key="every" type="schedule" shared="true"/></label>

                                <webapps:errorsPresent property="tn_tx_days">
                                    </span>
                                </webapps:errorsPresent>
                                <html:text property="value(tn_tx_days)" styleId="tn_tx_days" size="1" maxlength="3" styleClass="requiredField"/>
                                <label id="tn_tx_days_2_label"><webapps:pageText key="days" type="schedule" shared="true"/></label>
                                </td>
                            </tr>
                        </tbody>

                        <tbody id="tn_tx_compliance_refresh_weekly_settings" style="display:none;">
                            <tr valign="middle">
                                <td align="right" valign="middle" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" class="col2">&nbsp;</td>
                            </tr>
                            <tr valign="middle">
                                <td align="right" valign="middle" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" class="col2">
                                    <webapps:errorsPresent property="tn_tx_weeks">
                                        <span class="textRed"><img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" align="absmiddle">
                                    </webapps:errorsPresent>
                                            <label id="tn_tx_weeks_label"><webapps:pageText key="every" type="schedule" shared="true"/></label>
                                    <webapps:errorsPresent property="tn_tx_weeks">
                                        </span>
                                    </webapps:errorsPresent>
                                    <html:text property="value(tn_tx_weeks)" styleId="tn_tx_weeks" size="1" maxlength="3" styleClass="requiredField"/>
                                    <label id="tn_tx_weeks_2_label"><webapps:pageText key="weeksOn" type="schedule" shared="true"/></label>
                                </td>
                                </td>
                            </tr>
                            <tr valign="middle">
                                <td align="right" valign="top" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" class="col2">
                                    <label id="tn_tx_weeks_label"></label>
                                    <table cellspacing="0" cellpadding="0" border="0">
                                        <tr valign="top">
                                            <td nowrap> <html:checkbox property="value(tn_tx_monday)" styleId="tn_tx_monday"/>
                                                <label id="tn_tx_monday_label" for="tn_tx_monday">&nbsp;<webapps:pageText key="monday" type="schedule" shared="true"/></label>
                                                <br>
                                                <html:checkbox property="value(tn_tx_tuesday)" styleId="tn_tx_tuesday"/>
                                                <label id="tn_tx_tuesday_label" for="tn_tx_tuesday">&nbsp;<webapps:pageText key="tuesday" type="schedule" shared="true"/></label>
                                                <br>
                                                <html:checkbox property="value(tn_tx_wednesday)" styleId="tn_tx_wednesday"/>
                                                <label id="tn_tx_wednesday_label" for="tn_tx_wednesday">&nbsp;<webapps:pageText key="wednesday" type="schedule" shared="true"/></label>
                                                <br>
                                                <html:checkbox property="value(tn_tx_thursday)" styleId="tn_tx_thursday"/>
                                                <label id="tn_tx_thursday_label" for="tn_tx_thursday">&nbsp;<webapps:pageText key="thursday" type="schedule" shared="true"/></label>
                                                <br>
                                                <html:checkbox property="value(tn_tx_friday)" styleId="tn_tx_friday"/>
                                                <label id="tn_tx_friday_label" for="tn_tx_friday">&nbsp;<webapps:pageText key="friday" type="schedule" shared="true"/></label>
                                                </td>
                                                <td nowrap> <html:checkbox property="value(tn_tx_saturday)" styleId="tn_tx_saturday"/>
                                                <label id="tn_tx_saturday_label" for="tn_tx_saturday">&nbsp;<webapps:pageText key="saturday" type="schedule" shared="true"/></label>
                                                <br>
                                                <html:checkbox property="value(tn_tx_sunday)" styleId="tn_tx_sunday"/>
                                                <label id="tn_tx_sunday_label" for="tn_tx_sunday">&nbsp;<webapps:pageText key="sunday" type="schedule" shared="true"/></label>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </tbody>
                        <tbody id="tn_tx_compliance_refresh_monthly_settings" style="display:none;">
                            <tr valign="middle">
                                <td align="right" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" nowrap class="col2">&nbsp;</td>
                            </tr>
                            <tr valign="middle">
                                <td align="right" nowrap class="col1"><label id="tn_tx_sched_mo_label">&nbsp;</label></td>
                                <td colspan="2" nowrap class="col2">
                                    <webapps:errorsPresent property="tn_tx_months">
                                        <span class="textRed"><img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" align="absmiddle">
                                    </webapps:errorsPresent>
                                            <label id="tn_tx_months_label"><webapps:pageText key="every" type="schedule" shared="true"/></label>
                                    <webapps:errorsPresent property="tn_tx_months">
                                        </span>
                                    </webapps:errorsPresent>
                                    <html:text property="value(tn_tx_months)" styleId="tn_tx_months" styleClass="requiredField" size="2" maxlength="2"/>
                                    <label id="tn_tx_months_2_label"><webapps:pageText key="monthsOn" type="schedule" shared="true"/></label>
                                    <html:select property="value(tn_tx_monthDay)" styleId="tn_tx_monthDay">
                                        <html:option value="1">1st</html:option>
                                        <html:option value="2">2nd</html:option>
                                        <html:option value="3">3rd</html:option>
                                        <html:option value="4">4th</html:option>
                                        <html:option value="5">5th</html:option>
                                        <html:option value="6">6th</html:option>
                                        <html:option value="7">7th</html:option>
                                        <html:option value="8">8th</html:option>
                                        <html:option value="9">9th</html:option>
                                        <html:option value="10">10th</html:option>
                                        <html:option value="11">11th</html:option>
                                        <html:option value="12">12th</html:option>
                                        <html:option value="13">13th</html:option>
                                        <html:option value="14">14th</html:option>
                                        <html:option value="15">15th</html:option>
                                        <html:option value="16">16th</html:option>
                                        <html:option value="17">17th</html:option>
                                        <html:option value="18">18th</html:option>
                                        <html:option value="19">19th</html:option>
                                        <html:option value="20">20th</html:option>
                                        <html:option value="21">21st</html:option>
                                        <html:option value="22">22nd</html:option>
                                        <html:option value="23">23rd</html:option>
                                        <html:option value="24">24th</html:option>
                                        <html:option value="25">25th</html:option>
                                        <html:option value="26">26th</html:option>
                                        <html:option value="27">27th</html:option>
                                        <html:option value="28">28th</html:option>
                                    </html:select>
                                    <label id="tn_tx_monthDay_label"><webapps:pageText key="ofthemonth" type="schedule" shared="true"/></label>
                                </td>
                            </tr>
                        </tbody>
                        <tbody id="tn_tx_compliance_refresh_time_settings" style="display:none;">
                            <tr valign="middle">
                                <td align="right" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" class="col2">
                                    <html:select styleId="tn_tx_hourFromTo" property="value(tn_tx_hourFromTo)" onchange="showSection(tn_tx_hourType,this.options[this.selectedIndex].value)">
                                        <html:option value="tn_tx_hoursec2"><webapps:pageText key="at" type="schedule" shared="true"/></html:option>
                                        <html:option value="tn_tx_hoursec"><webapps:pageText key="from" type="schedule" shared="true"/></html:option>
                                    </html:select>
                                    <webapps:errorsPresent property="tn_tx_hourFrom">
                                        <img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" align="absmiddle">
                                    </webapps:errorsPresent>
                                    <html:text property="value(tn_tx_hourFrom)" styleId="tn_tx_hourFrom" styleClass="requiredField" size="2" maxlength="2"/>
                                    <label id="tn_tx_minFrom_label">:</label>

                                    <webapps:errorsPresent property="tn_tx_minFrom">
                                        <img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" align="absmiddle">
                                    </webapps:errorsPresent>
                                    <html:text property="value(tn_tx_minFrom)" styleId="tn_tx_minFrom" size="2" maxlength="2" styleClass="requiredField"/>

                                    <html:select property="value(tn_tx_ampmFrom)" styleId="tn_tx_ampmFrom">
                                        <html:option value="AM"><webapps:pageText key="am" type="schedule" shared="true"/></html:option>
                                        <html:option value="PM"><webapps:pageText key="pm" type="schedule" shared="true"/></html:option>
                                    </html:select>
                                    <span id="tn_tx_hoursec_settings" style="display:none">
                                    <label id="tn_tx_hourTo_label"><webapps:pageText key="to" type="schedule" shared="true"/></label>
                                    <webapps:errorsPresent property="tn_tx_hourTo">
                                        <img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" align="absmiddle">
                                    </webapps:errorsPresent>

                                    <html:text property="value(tn_tx_hourTo)" styleId="tn_tx_hourTo" styleClass="requiredField" size="2" maxlength="2"/>

                                    <label id="tn_tx_minTo_label">:</label>
                                    <webapps:errorsPresent property="tn_tx_minTo">
                                        <img src="/shell/common-rsrc/images/error_sm.gif" width="16" height="16" align="absmiddle">
                                    </webapps:errorsPresent>

                                    <html:text property="value(tn_tx_minTo)" styleId="tn_tx_minTo" styleClass="requiredField" size="2" maxlength="2"/>

                                    <html:select property="value(tn_tx_ampmTo)" styleId="tn_tx_ampmTo">
                                        <html:option value="AM"><webapps:pageText key="am" type="schedule" shared="true"/></html:option>
                                        <html:option value="PM"><webapps:pageText key="pm" type="schedule" shared="true"/></html:option>
                                    </html:select>
                                    <br>
                                    <img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="3">
                                    <br>
                                    <label id="tn_tx_frequency_label">
                                        <webapps:formatError property="tn_tx_frequency">
                                            &nbsp;
                                        </webapps:formatError>
                                    </label>
                                    <label id="tn_tx_frequency_1_label"><webapps:pageText key="Every" type="schedule" shared="true"/></label>
                                    <html:text property="value(tn_tx_frequency)" styleId="tn_tx_frequency" size="3" maxlength="3" styleClass="requiredField"/>
                                    <label id="tn_tx_frequency_2_label"><webapps:pageText key="minutes" type="schedule" shared="true"/></label>
                                    </span><span id="tn_tx_hoursec2_settings">
                                        &nbsp;
                                    </span>
                                </td>
                            </tr>
                            <tr valign="middle">
                                <td align="right" nowrap class="col1">&nbsp;</td>
                                <td colspan="2" class="col2">&nbsp;</td>
                            </tr>
                        </tbody>
                    </table>
                </fieldset>
                <div style="width:700px; text-align:left; margin-top:20px;">
                    <h3><webapps:pageText key="cachedTgt.hdr"/></h3>
                </div>
                <div style="width:700px;" class="sectionInfo"><webapps:pageText key="cachedTgt.sectInfo"/></div>
                <div style="width:700px; text-align:left; padding-top:5px;">
                    <html:select property="cacheOption" styleClass="optionalField" styleId="targetnames" onchange="showSection(show_cache,this.options[selectedIndex].value)">
                        <html:option value="cache_onlyall"><webapps:pageText key="cacheoption.one"/></html:option>
                        <html:option value="cache_list"><webapps:pageText key="cacheoption.three"/></html:option>
                    </html:select>
                    <span id="cache_onlyall_settings">&nbsp;</span>
                    <span id="cache_all_settings">&nbsp;</span>
                    <table width="100%" border="0" cellspacing="0" cellpadding="5" style="margin-top:8px; display:none;" id="cache_list_settings">
                        <colgroup width="100%"></colgroup>
                        <colgroup width="0*"></colgroup>
                        <tr>
                            <td style="border-left:none; border-bottom:1px solid #999999; ">Target DN </td>
                            <td nowrap style="border-bottom:1px solid #999999; ">&nbsp;</td>
                        </tr>

                        <logic:present name="setComplianceForm" property="targetlist" scope="session">
                            <% int curIndex = 0; %>
                            <logic:iterate id="atarget" name="setComplianceForm" property="targetlist">

                                <tbody id="refRow_<%=curIndex%>" style="display:; ">
                                        <tr>
                                        <% if(curIndex == 0) { %> <td> <%}else{ %> <td> <%}%>
                                        <input name="target_<%=curIndex%>" type="text" styleClass="requiredField" style="width:100%; " value="<bean:write name="atarget"/>"/>
                                        </td>
                                <td nowrap style="padding-left:5px; ">
                                        <input name="Del_Button_<%=curIndex%>" type="button" class="smallButtons" style="width:20px;" value="-" onclick="doSubmit('delete','target_<%=curIndex%>')" />
                                        <input name="Add_Button_<%=curIndex%>" type="button" class="smallButtons" style="width:20px;" value="+" 
							<logic:present name="setComplianceForm" property="targetsize">
				    			<bean:define name="setComplianceForm" property="targetsize" id="tsize" type="java.lang.String"/>
							
							<% if(!(Integer.toString(curIndex+1).equals(tsize))){%> 
								disabled
							<%} else { %>
								<logic:equal name="setComplianceForm" property="addblankrow" value="true">
								disabled
								</logic:equal>
							<%} %>
				    			</logic:present>
							onclick="doSubmit('add','target_<%=curIndex%>')"
						     />
                                </td>
                                </tr>
                                </tbody>
                                <% curIndex++; %>
                            </logic:iterate>

                            <logic:equal name="setComplianceForm" property="addblankrow" value="true">
                                <tbody id="refRow_<%=curIndex%>" style="display:; ">
                                    <tr>
                                        <td><input name="target_<%=curIndex%>" type="text" styleClass="requiredField" style="width:100%; "/></td>
                                        <td nowrap style="padding-left:5px; ">
                                                <input name="Del_Button_<%=curIndex%>" type="button" class="smallButtons" style="width:20px;" value="-" onclick="doSubmit('delete','target_<%=curIndex%>')" />
                                                <input name="Add_Button_<%=curIndex%>" type="button" class="smallButtons" style="width:20px;" value="+" onclick="doSubmit('add','target_<%=curIndex%>')"/>
                                        </td>
                                    </tr>
                                </tbody>
                            </logic:equal>
                        </logic:present>
                        <logic:notPresent name="setComplianceForm" property="targetlist" scope="session">
                            <tbody id="refRow_0" style="display:; ">
                                <tr>
                                    <td><html:text property="value(target_0)" styleClass="requiredField" style="width:100%; "/></td>
                                    <td nowrap style="padding-left:5px; ">
                                            <input name="Del_Button_0" type="button" class="smallButtons" style="width:20px;" value="-" disabled/>
                                            <input name="Add_Button_0" type="button" class="smallButtons" style="width:20px;" value="+" onclick="doSubmit('add','target_0')"/>
                                    </td>
                                </tr>
                            </tbody>
                        </logic:notPresent>
                    </table>
                </div>
            </div>
            <div id="pageNav" style="width:700px;">
                <input type="button" class="mainBtn" value="<webapps:pageText type="button" shared="true" key="save"/>" onClick="doSubmit('save',null)"/>
                &nbsp;
                <input type="button" value="<webapps:pageText shared ="true" type="button" key="cancel"/>" onClick="doSubmit('cancel',null)" >
            </div>
        </div><!--end supder div for padding-->
    </div><!--end super div for centering-->
</html:form>

<script language="javascript">
// set the report refresh schedule properly
var calcCompEnabledOption = document.getElementById("calcCompEnabledOption");
var refreshSchedule = document.getElementById("tn_tx");
var refreshScheduleAt = document.getElementById("tn_tx_hourFromTo");
var targetOpt = document.getElementById("targetnames");
showSection(show_compliance_options,calcCompEnabledOption.options[calcCompEnabledOption.selectedIndex].value);
showUpdateSection(tn_tx_update_type,refreshSchedule.options[refreshSchedule.selectedIndex].value,refreshSchedule.id);
showSection(tn_tx_hourType,refreshScheduleAt.options[refreshScheduleAt.selectedIndex].value);
showSection(show_cache, targetOpt.options[targetOpt.selectedIndex].value);
</script>

<%@ include file="/includes/footer.jsp" %>

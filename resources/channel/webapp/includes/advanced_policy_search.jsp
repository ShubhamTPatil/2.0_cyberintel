<%--Copyright 2004-2011, BMC Software Inc. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software Inc.
    Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
    6,381,631, and 6,430,608. Other Patents Pending.

    $File$

    @author Tamilselvan Teivasekamani
    @version $Revision$, $Date$

--%>

<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<jsp:useBean id="session_ldap" class="com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean" scope="session"/>

<link rel="stylesheet" href="/sm/includes/errorMessage.css" type="text/css" />

<style type="text/css">

    div.transparentCover {
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: none;
        z-index: 1000;
        position: absolute;
        background-color: #ffffff;
        filter: alpha( opacity = 45 );
        -moz-opacity: 0.45;
        opacity: 0.45;
    }

    div.ReportEditArea {
        width: 580px;
        display: none;
        z-index: 1000;
        position: absolute;
    }

    .dialogcontent {
        background: #ffffff;
        border-right: 1px solid #000000;
        border-left: 1px solid #000000;
        padding-top: 2px;
        padding-bottom: 2px;
    }

    .dialogheadh {
        font-size: 11px;
        font-weight: bold;
        border-right: 1px solid #0099cc;
        border-left: 1px solid #0099cc;
    }

    .corner_Top_1, .corner_Top_2, .corner_Top_3, .corner_Top_4, .corner_Btm_1, .corner_Btm_2, .corner_Btm_3, .corner_Btm_4 {
        overflow: hidden;
        display: block;
    }

    .corner_Top_1 {
        height: 1px;
        background: #0099cc;
        margin: 0 5px;
    }

    .corner_Btm_1 {
        height: 1px;
        background: #000000;
        margin: 0 5px;
    }

    .corner_Top_2, .corner_Btm_2 {
        height: 1px;
        border-right: 2px solid #000000;
        border-left: 2px solid #000000;
        margin: 0 3px;
    }

    .corner_Top_3, .corner_Btm_3 {
        height: 1px;
        border-right: 1px solid #000000;
        border-left: 1px solid #000000;
        margin: 0 2px;
    }

    .corner_Top_4, .corner_Btm_4 {
        height: 2px;
        border-right: 1px solid #000000;
        border-left: 1px solid #000000;
        margin: 0 1px;
    }

    .corner_Btm_2, .corner_Btm_3, .corner_Btm_4 {
        background: #ffffff;
    }

    .corner_Top_2, .corner_Top_3, .corner_Top_4, .dialogheadh {
        border-left: 1px solid #0099cc;
        border-right: 1px solid #0099cc;
        background: #00418C url( /shell/common-rsrc/images/header.gif ) repeat-x top;
    }

	.errorField {
	  border-color: red;
	}

</style>

<script language="JavaScript" src="/sm/includes/validation.js"></script>

<script type="text/javascript">

$(function () {
	
	console.log('advanced_policy_search');
	
});


var errMessage = ["<webapps:text key='page.adv_policy_search.invaid_date' />", "<webapps:text key='page.adv_policy_search.invalid_month' />", "<webapps:text key='page.adv_policy_search.invalid_day' />",
		  "<webapps:text key='page.adv_policy_search.invalid_year' />", "<webapps:text key='page.adv_policy_search.between_search.required_field_missing' />", "<webapps:text key='page.adv_policy_search.required' />", "<webapps:text key='page.adv_policy_search.invalid_date_range' />"];

function validateForm(){
	var errMsg = "<ul>";
	clearErrorStyle();
	if(isBlank(policyForm.elements["<%=ISubscriptionConstants.TARGET_NAME%>"].value) && isBlank(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value) && isBlank(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value)){
		if(!policyForm.elements["<%=ISubscriptionConstants.ORPHAN_POLICY%>"].checked){
			return "<li>" + errMessage[5] + "</li>";
		}
	}
	var res = validate(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"]);
	if (res != -1){
		errMsg = errMsg + "<li>" + errMessage[res] + "</li>";
		policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].className = "errorField";
	}
	res = validate(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"]);
	if (res != -1){
		errMsg = errMsg + "<li>" + errMessage[res] + "</li>";
		policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].className = "errorField";
	}
	res = validate(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"]);
	if (res != -1){
		errMsg = errMsg + "<li>" + errMessage[res] + "</li>";
		policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].className = "errorField";
	}
	res = validate(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"]);
	if (res != -1){
		errMsg = errMsg + "<li>" + errMessage[res] + "</li>";
		policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].className = "errorField";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>"].value == "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>"){
		if(isValidDateRange(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value,policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].value)){
			errMsg = errMsg + "<li>" + errMessage[6] + "</li>";
			policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].className = "errorField";
			policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].className = "errorField";
		}
	}
	if(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>"].value == "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>"){
		if(isValidDateRange(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value,policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].value)){
			errMsg = errMsg+"<li>" + errMessage[6] + "</li>";
			policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].className = "errorField";
			policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].className = "errorField";
		}
	}
	return errMsg;
}

function validate(inputField){
	if(!isBlank(inputField.value)){
		return isValidDate(inputField.value);
	}
	return -1;
}

function invokeDiv() {
        document.getElementById('transparentCover').style.display = 'block';
        var editableArea = document.getElementById('searchArea');
        editableArea.style.display = 'block';
        editableArea.style.top = "29%";
        editableArea.style.left = "27%";
        showHideCreateDateTo();
        showHideModifyDateTo();
}

function closeGlassPane() {
    document.getElementById('searchArea').style.display = '';
    document.getElementById('transparentCover').style.display = '';
}

function clearErrorStyle(){
	policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].className = "";
	policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].className = "";
	policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].className = "";
	policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].className = "";
	document.getElementById('critical').innerHTML = "";
	document.getElementById('critical').className = "";
}

function clearValues(){
	policyForm.elements["<%=ISubscriptionConstants.TARGET_NAME%>"].value = "";
	policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>"].value = "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BEFORE%>";
	policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>"].value = "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BEFORE%>";
	policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value = "";
	policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].value = "";
	policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value = "";
	policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].value = "";
	policyForm.elements["<%=ISubscriptionConstants.ORPHAN_POLICY%>"].checked = false;
	policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].style.visibility = 'hidden';
	document.getElementById('modifyDateLabel').style.visibility = 'hidden';
	policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].style.visibility = 'hidden';
	document.getElementById('createDateLabel').style.visibility = 'hidden';
	document.getElementById('createDateToFormat').style.visibility = 'hidden';
	document.getElementById('modifyDateToFormat').style.visibility = 'hidden';
	clearErrorStyle();
	parent.clearQuery();
}

function showHideCreateDateTo(){
	if(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>"].value != "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>"){
		policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].style.visibility = 'hidden';
		document.getElementById('createDateLabel').style.visibility = 'hidden';
		document.getElementById('createDateToFormat').style.visibility = 'hidden';
	}else{
		policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].style.visibility = 'visible';
		document.getElementById('createDateLabel').style.visibility = 'visible';
		document.getElementById('createDateToFormat').style.visibility = 'visible';
	}
}
function showHideModifyDateTo(){
	if(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>"].value != "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>"){
		policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].style.visibility = 'hidden';
		document.getElementById('modifyDateLabel').style.visibility = 'hidden';
		document.getElementById('modifyDateToFormat').style.visibility = 'hidden';
	}else{
		policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].style.visibility = 'visible';
		document.getElementById('modifyDateLabel').style.visibility = 'visible';
		document.getElementById('modifyDateToFormat').style.visibility = 'visible';
	}
}


function searchPolicy() {
	if(policyForm.elements["<%=ISubscriptionConstants.ORPHAN_POLICY%>"].checked){
		policyForm.elements["<%=ISubscriptionConstants.ORPHAN_POLICY%>"].value = "true";
	}else{
		policyForm.elements["<%=ISubscriptionConstants.ORPHAN_POLICY%>"].value = "false";
	}
	var errMsg = validateForm();
	if(errMsg == "<ul>"){
		clearErrorStyle();
		closeGlassPane();
    	parent.sendURL(getAllSearchAttributes(), preparQuery());
	}else{
		document.getElementById('critical').className = "errorMessage";
		document.getElementById('critical').innerHTML = "<h6><webapps:text key="page.main_view.errmesssage" /></h6><p>" + errMsg + "</ul></p>";
	}
}

var queryValue = "";
var advAttributes = "";
function getAllSearchAttributes() {
	advAttributes = "";
    prepareURL("<%=ISubscriptionConstants.TARGET_NAME%>", policyForm.elements["<%=ISubscriptionConstants.TARGET_NAME%>"].value);
    prepareURL("<%=ISubscriptionConstants.CREATE_DATE_FROM%>", policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value);
    prepareURL("<%=ISubscriptionConstants.CREATE_DATE_TO%>", policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].value);
    prepareURL("<%=ISubscriptionConstants.MODIFY_DATE_FROM%>", policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value);
    prepareURL("<%=ISubscriptionConstants.MODIFY_DATE_TO%>", policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].value);
    prepareURL("<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>", policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>"].value);
    prepareURL("<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>", policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>"].value);
    prepareURL("<%=ISubscriptionConstants.ORPHAN_POLICY%>", policyForm.elements["<%=ISubscriptionConstants.ORPHAN_POLICY%>"].value);
    return advAttributes;
}

function preparQuery(){
	queryValue = "";
	if(policyForm.elements["<%=ISubscriptionConstants.TARGET_NAME%>"].value != ""){
		queryValue = "<webapps:text key='page.ldap_nav.targetName' />" + " " + "<webapps:text key='page.ldap_nav.like' />" + " " + policyForm.elements["<%=ISubscriptionConstants.TARGET_NAME%>"].value + "* " + "<webapps:text key='page.ldap_nav.and' /> " + " ";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>"].value == "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>" && policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value != "" && policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].value != ""){
		queryValue = queryValue + "<webapps:text key='page.ldap_nav.CreateDate' />" + " " + "<webapps:text key='page.ldap_nav.between' />" + " (" + policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value+"," + policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_TO%>"].value + ") " +  "<webapps:text key='page.ldap_nav.and' />" + " ";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>"].value == "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>" && policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value != "" && policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].value != ""){
		queryValue = queryValue + "<webapps:text key='page.ldap_nav.ModifyDate' />" + " " + "<webapps:text key='page.ldap_nav.between' />" + " (" + policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value + "," + policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_TO%>"].value + ") " +  "<webapps:text key='page.ldap_nav.and' />" + " ";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>"].value == "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BEFORE%>" && policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value != "" ){
		queryValue = queryValue + "<webapps:text key='page.ldap_nav.CreateDate' />" + " " + "<webapps:text key='page.ldap_nav.before' />" + " " + policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value + " " + "<webapps:text key='page.ldap_nav.and' />" + " ";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>"].value == "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.AFTER%>" && policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value != "" ){
		queryValue = queryValue + "<webapps:text key='page.ldap_nav.CreateDate' />" + " " + "<webapps:text key='page.ldap_nav.after' />" + " " +  policyForm.elements["<%=ISubscriptionConstants.CREATE_DATE_FROM%>"].value + " " +  "<webapps:text key='page.ldap_nav.and' />" + " ";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>"].value == "<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BEFORE%>" && policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value != ""){
		queryValue = queryValue + "<webapps:text key='page.ldap_nav.ModifyDate' />" + " " + "<webapps:text key='page.ldap_nav.before' />" + " " +  policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value + " " + "<webapps:text key='page.ldap_nav.and' />" + " ";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>"].value=="<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.AFTER%>" && policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value != ""){
		queryValue = queryValue + "<webapps:text key='page.ldap_nav.ModifyDate' />" + " " + "<webapps:text key='page.ldap_nav.after' />" + " " + policyForm.elements["<%=ISubscriptionConstants.MODIFY_DATE_FROM%>"].value + " " + "<webapps:text key='page.ldap_nav.and' />" + " ";
	}
	if(policyForm.elements["<%=ISubscriptionConstants.ORPHAN_POLICY%>"].value=="true"){
		queryValue = queryValue + "<webapps:text key='page.ldap_nav.orphanPolicy' />" + " " + "<webapps:text key='page.ldap_nav.and' />" + " ";
	}
	return queryValue.substring(0, queryValue.length - 5);
}


function prepareURL(paramName,paramValue){
	if(paramValue != ""){
	if(advAttributes == "" || advAttributes == undefined){
		advAttributes = paramName + "=" + paramValue;
	}else{
		advAttributes = advAttributes + "&#;" + paramName + "=" + paramValue;
	}
  }
}


</script>

<div id="transparentCover" class="transparentCover"></div>
<div id="searchArea" class="ReportEditArea">
    <b class="corner_Top_1"> </b>
    <b class="corner_Top_2"> </b>
    <b class="corner_Top_3"> </b>
    <b class="corner_Top_4"> </b>

    <div class="dialogheadh">
        <table width="100%">
            <tr>
                <td style="color:#ffffff;font-weight:bold;padding-left:10px;" valign="top"><webapps:text key="page.ldap_nav.AdvancedPolicySearch"/></td>
                <td valign="top" align="right"><img src="/sm/images/close_top.png" onclick="closeGlassPane();"/></td>
            </tr>
        </table>
    </div>

	<html:form name="policyForm" action="ldapSearch?basicLink=policy" type="com.marimba.apps.subscriptionmanager.webapp.forms.LDAPNavigationForm">
	    <div id="contentHolder" class="dialogcontent">
	    	<table width="99%" border="0" align="center" cellspacing="0" cellpadding="2">
		    	<tr><td><div id="critical"></div></td></tr>
		 	</table>
        	<table width="99%" border="0" align="center" cellspacing="0" cellpadding="2">
		        <colgroup width="30%">
		                <col align="left"/>
		        </colgroup>
		        <colgroup width="65%"></colgroup>
		        <colgroup width="0*"></colgroup>
            	<tr>
					<td style="padding-left:10px;"><webapps:text key="page.ldap_nav.targetName"/>&nbsp;&nbsp;:</td>
					<td colspan="2"><html:text property="<%=ISubscriptionConstants.TARGET_NAME%>" size="26" /></td>
				</tr>
				<tr class="alternateRowColor">
					<td style="padding-left:10px;"><webapps:text key="page.ldap_nav.CreateDate"/>&nbsp;&nbsp;:</td>
					<td colspan="2" >
						<select name="<%=ISubscriptionConstants.CREATE_DATE_CRITERIA%>" onChange="javaScript:showHideCreateDateTo();">
						    <option value="<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BEFORE%>"><webapps:text key="page.ldap_nav.before"/></option>
						  	<option value="<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.AFTER%>"><webapps:text key="page.ldap_nav.after"/></option>
						  	<option value="<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>"><webapps:text key="page.ldap_nav.between"/></option>
						</select>
		                <html:text property="<%=ISubscriptionConstants.CREATE_DATE_FROM%>" size="11" />
		                <label id="createDateFromFormat"><webapps:text key="page.ldap_nav.dateFormat"/></label>&nbsp;&nbsp;
                        <label id="createDateLabel"><webapps:text key="page.ldap_nav.to"/>&nbsp;:</label>
						<html:text property="<%=ISubscriptionConstants.CREATE_DATE_TO%>" size="11" />
						<label id="createDateToFormat"><webapps:text key="page.ldap_nav.dateFormat"/></label>
					</td>
				</tr>
				<tr>
					<td  style="padding-left:10px;"><webapps:text key="page.ldap_nav.ModifyDate"/>&nbsp;&nbsp;:</td>
					<td  colspan="2" >
						<select name="<%=ISubscriptionConstants.MODIFY_DATE_CRITERIA%>" onChange="javaScript:showHideModifyDateTo();">
						    <option value="<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BEFORE%>"><webapps:text key="page.ldap_nav.before"/></option>
						  	<option value="<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.AFTER%>"><webapps:text key="page.ldap_nav.after"/></option>
						  	<option value="<%=com.marimba.apps.subscriptionmanager.intf.ILdapSearch.BETWEEN%>"><webapps:text key="page.ldap_nav.between"/></option>
						</select>
                        <html:text property="<%=ISubscriptionConstants.MODIFY_DATE_FROM%>" size="11"/>
                        <label id="modifyDateFromFormat"><webapps:text key="page.ldap_nav.dateFormat"/></label>&nbsp;&nbsp;
                        <label id="modifyDateLabel"><webapps:text key="page.ldap_nav.to"/>&nbsp;:</label>
                        <html:text property="<%=ISubscriptionConstants.MODIFY_DATE_TO%>" size="11" />
                        <label id="modifyDateToFormat"><webapps:text key="page.ldap_nav.dateFormat"/></label>
                    </td>
                </tr>
				<tr class="alternateRowColor">
					<td>&nbsp;</td>
					<td>
						<html:checkbox property="<%=ISubscriptionConstants.ORPHAN_POLICY%>"/><webapps:text key="page.ldap_nav.orphanPolicy"/>
					</td>
					<td>&nbsp;</td>
				</tr>
     		</table>
        <div id="pageNav">
			<input type="button" name="searchButton" onClick="javascript:searchPolicy();" value="<webapps:text key="page.ldap_nav.Go"/>"/>
			<input type="button" name="clearButton" onClick="javascript:clearValues();" value="<webapps:text key="page.ldap_nav.Clear"/>"/>
			<input type="button" name="cancelButton" onClick="javascript:closeGlassPane();" value="<webapps:text key="page.ldap_nav.Cancel"/>"/>
			&nbsp;&nbsp;
        </div>
    </div>
</html:form>

    <!-- End of ContentHolder -->
    <b class="corner_Btm_4"> </b>
    <b class="corner_Btm_3"> </b>
    <b class="corner_Btm_2"> </b>
    <b class="corner_Btm_1"> </b>
</div>
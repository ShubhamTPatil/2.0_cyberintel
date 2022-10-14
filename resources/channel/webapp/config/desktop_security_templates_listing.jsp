<%--
// Copyright 2015, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
//
// $File$, $Revision$, $Date$
//
// author: Selvaraj Jegatheesan
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>

<script>

function selectFile() {
    desktopSecurityProfileForm.file.click();
}

function saveState(form, forwardaction) {
    var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";
    form.action = fullpath;
    form.submit();
}

// this function checks/unchecks all rows when "select-all" is checked/unchecked

function setChecked(val) {
    var colForm = document.desktopSecurityProfileForm;
    var len = colForm.elements.length;
    var i = 0;

    for (i = 0; i < len; i++) {
        if (((colForm.elements[i].name).indexOf("profile_sel_")) > -1) {
            colForm.elements[i].checked = val;
        }
    }
}

// this function does the reverse of the above function if all te rows are checked, "select-all" gets checked

function setCheckedAll(val) {
    var colForm = document.desktopSecurityProfileForm;
    var len = colForm.elements.length;
    var i = 0;
    var allChecked = true;
    var rowsPresent = false;

    for (i = 0; i < len; i++) {
        if (((colForm.elements[i].name).indexOf("profile_sel_")) > -1) {
            rowsPresent = true;
            if (!colForm.elements[i].checked) {
                allChecked = false;
                break;
            }
        }
    }

    if (rowsPresent) {
        colForm.select_all_profiles.checked = allChecked;
    }
}

// This function Checks whether atleast one check box is checked or not. If atleast one checkbox is checked then remove button will be enabled

function isAnySelected() {
    var colForm = document.desktopSecurityProfileForm;
    var len = colForm.elements.length;
    var i = 0;
    var totalChecked = 0;

    for (i = 0; i < len; i++) {
        if (((colForm.elements[i].name).indexOf("profile_sel_")) > -1) {
            if (colForm.elements[i].checked) {
                totalChecked++;
                if ( totalChecked > 1 ) {
                    break;
                }
            }
        }
    }

    colForm.removeBtn.disabled = (totalChecked == 0);
    colForm.editBtn.disabled = (totalChecked != 1);
    colForm.exportbtn.disabled = (totalChecked == 0);
}

// This function Checks whether atleast one check box is checked or not. If atleast one checkbox is checked then remove button will be enabled

function processEdit() {

    var colForm = document.desktopSecurityProfileForm;
    var len = colForm.elements.length;
    var i = 0;

    for (i = 0; i < len; i++) {
        var currentElement = colForm.elements[i].name;
        var chkBoxNamePrefix = 'profile_sel_';

        if ((currentElement.indexOf(chkBoxNamePrefix)) > -1) {
            if (colForm.elements[i].checked) {
                document.desktopSecurityProfileForm.name = currentElement.substring(chkBoxNamePrefix.length());
                break;
            }
        }
    }

    saveState(document.desktopSecurityProfileForm, '/c.do?action=edit')
}

</script>

<%@ include file="/includes/startHeadSection.jsp" %>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<webapps:helpContext context="sm" topic="DesktopSecurity_Options"/>

<body>

<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>

<html:form method="post" name="desktopSecurityProfileForm" enctype="multipart/form-data" action="/desktopSecurityTemplateListing.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DesktopSecurityProfileForm" >

<html:hidden property="forward" />

<div align="center">
<div class="commonPadding">
<div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
<%@ include file="/includes/usererrors.jsp" %>
<%@ include file="/includes/help.jsp" %>

<div id="contentPadding" style="width:65%">
<%--
<logic:equal name="desktopSecurityProfileForm" property="status" value="Export-Failed">
    <div class="statusMessage" id="critical" width="90%">
        <h6><webapps:pageText key="export.warning.header"/></h6>
        <p align="left"><webapps:pageText key="export.warning.message"/>
            <bean:write name="desktopSecurityProfileForm" property="statusDesc"/>
        </p>
    </div>
</logic:equal>

<logic:equal name="desktopSecurityProfileForm" property="status" value="Import-Failed">
    <div class="statusMessage" id="critical" width="90%">
        <h6><webapps:pageText key="import.warning.header"/></h6>
        <p align="left"><webapps:pageText key="import.warning.message"/>
            <bean:write name="desktopSecurityProfileForm" property="statusDesc"/>
        </p>
    </div>
</logic:equal>

<logic:equal name="desktopSecurityProfileForm" property="status" value="Import-Succeeded">
    <div class="statusMessage" id="OK" width="90%">
        <h6><webapps:pageText key="import.success.header"/></h6>
        <p align="left"><webapps:pageText key="import.success.message"/>
        </p>
    </div>
</logic:equal>
--%>

<logic:present scope="request" name="result">
    <bean:define id="res" name="result"/>
    <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
    <font class="greenText"><b><font class="generalText"> <%= res %> </font></b></font>
</logic:present>

<div class="tableWrapper" style="width:100%;">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr valign="middle" class="smallButtons">
            <td nowrap class="tableRowActions">
                <input disabled name="removeBtn" id="removeBtn" type="button" value="<webapps:pageText key="remove" type="button" shared="true"/>" onClick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=remove');">
            </td>
            <td nowrap class="tableRowActions">
                <input disabled name="editBtn" id="editBtn" type="button" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=edit');">
            </td>
			<%--
            <td nowrap class="tableRowActions">
                <input type="button" name="importbtn" value=" <webapps:pageText key="import" type="button" /> " onClick="fileImport.style.display=''">
            </td>
            <td nowrap class="tableRowActions">
                <input type="button" name="exportbtn" disabled="true" value=" <webapps:pageText key="export" type="button" /> " onClick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=export');">
            </td>
			--%>
            <td nowrap class="tableRowActions" width="100%" align="right">
                <input name="addBtn" id="addBtn" type="button" value="<webapps:pageText key="add" type="button" shared="true"/>" onClick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=add');">
            </td>
        </tr>
		<%--
        <tr>
            <td nowrap class="tableRowActions" colspan="5">
                <div id="fileImport" style="display:none"><webapps:pageText key="import.desc"/>
                    <html:file property="file" onchange="javascript:saveState(document.desktopSecurityProfileForm,'/profilePowerOptions.do?action=import');" value=" <webapps:pageText key='export'/>"/>
                </div>
            </td>
        </tr>
		--%>
    </table>

    <div id="headerSection"class="headerSection" style="width:100%; text-align:left; overflow:hidden;" >
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <colgroup width="0*">
            </colgroup>
            <colgroup width="30%">
            </colgroup>
            <colgroup width="65%">
            </colgroup>
            <thead>
                <tr >
                    <td class="tableHeaderCell">
                        <input type="checkbox" id="select_all_profiles" name="select_all_profiles" onClick="setChecked(checked);isAnySelected();">
                    </td>
                    <td class="tableHeaderCell"><webapps:pageText key="TitleName"/></td>
                    <td class="tableHeaderCell"><webapps:pageText key="TitleDesc"/></td>
                </tr>
            </thead>
        </table>
    </div>

    <div id="dataSection" style="width:100%;">
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <colgroup width="0*">
            </colgroup>
            <colgroup width="30%">
            </colgroup>
            <colgroup width="65%">
            </colgroup>
            <tbody>
                <logic:iterate id="profile" indexId="index" name="profiles">
                    <tr <% if(index.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                        <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
                            <input type="checkbox" id="profile_sel_<bean:write name="profile" property="name"/>" name="profile_sel_<bean:write name="profile" property="name"/>" onClick="setCheckedAll(checked);isAnySelected();">
                        </td>
                        <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
                            <bean:write name="profile" property="name"/>
                        </td>
                        <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
                            <bean:write name="profile" property="description"/>
                        </td>
                    </tr>
                </logic:iterate>
            </tbody>
        </table>
    </div>
</div>


<!--<input type="file" id="file" name="file" style="display: none"-->
<!--onchange="javascript:saveState(document.desktopSecurityProfileForm,'/profilePowerOptions.do?action=import');" />-->
<div id="pageNav">
    <input type="button" class="mainBtn" name="done" value=" <webapps:pageText key="done" shared="true" type="button" /> " onClick="javascript:saveState(document.desktopSecurityProfileForm, '/config.do');">
</div>
</div>
</div>
</div>

</html:form>

</body>
</html:html>

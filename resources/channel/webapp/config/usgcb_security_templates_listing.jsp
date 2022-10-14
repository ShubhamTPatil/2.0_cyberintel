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

function saveState(form, forwardaction) {
    var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";
    form.action = fullpath;
    form.submit();
}

// this function checks/unchecks all rows when "select-all" is checked/unchecked

function setChecked(val) {
    var colForm = document.usgcbSecurityProfileForm;
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
    var colForm = document.usgcbSecurityProfileForm;
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
    var colForm = document.usgcbSecurityProfileForm;
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

</script>

<%@ include file="/includes/startHeadSection.jsp" %>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<webapps:helpContext context="spm" topic="USGCBSecurity_Options"/>

<body>

<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>

<html:form name="usgcbSecurityProfileForm" action="/usgcbSecurityTemplateListing.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.USGCBSecurityProfileForm" >

<html:hidden property="forward" />

<div align="center">
<div class="commonPadding">
<div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
<%@ include file="/includes/usererrors.jsp" %>
<%@ include file="/includes/help.jsp" %>

<div id="contentPadding" style="width:65%">

<logic:present scope="request" name="result">
    <bean:define id="res" name="result"/>
    <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
    <font class="greenText"><b><font class="generalText"> <%= res %> </font></b></font>
</logic:present>

<logic:present scope="request" name="createedit.result">
    <logic:present scope="request" name="create.result">
        <bean:define id="res" name="create.result"/>
        <logic:equal name="createedit.result" value="success">
            <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
            <font class="greenText"><b><font class="generalText"> <%= res %> </font></b></font>
        </logic:equal>
        <logic:notEqual name="createedit.result" value="success">
            <img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()">
            <font class="redText"><b><font class="generalText"> <%= res %> </font></b></font>
        </logic:notEqual>
    </logic:present>

    <logic:present scope="request" name="edit.result">
        <bean:define id="res" name="edit.result"/>
        <logic:equal name="createedit.result" value="success">
            <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
            <font class="greenText"><b><font class="generalText"> <%= res %> </font></b></font>
        </logic:equal>
        <logic:notEqual name="createedit.result" value="success">
            <img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()">
            <font class="redText"><b><font class="generalText"> <%= res %> </font></b></font>
        </logic:notEqual>
    </logic:present>
</logic:present>

<logic:present scope="request" name="remove.allSuccess">
    <logic:equal name="remove.allSuccess" value="true">
        <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
        <font class="greenText"><b><font class="generalText"> <webapps:text key="page.remove.template.success"/> </font></b></font>
    </logic:equal>
</logic:present>
<logic:notPresent scope="request" name="remove.allSuccess">
    <table width="100%" border=0><tr>
    <logic:present scope="request" name="remove.success">
        <td>
            <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
            <font class="greenText"><b><font class="generalText"> <webapps:text key="page.remove.template.success.list"/> </font></b></font>
            <ul>
                <logic:iterate name="remove.success" id="successId">
                    <li><bean:write name="successId"/></li>
                </logic:iterate>
            </ul>
        </td>
    </logic:present>
    <logic:present scope="request" name="remove.errors">
        <td>
            <img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()">
            <font class="redText"><b><font class="generalText"> <webapps:text key="page.remove.template.error.list"/> </font></b></font>
            <ul>
                <logic:iterate name="remove.errors" id="errorsId">
                    <li><bean:write name="errorsId"/></li>
                </logic:iterate>
            </ul>
        </td>
    </logic:present>
    </tr></table>
</logic:notPresent>

<div class="tableWrapper" style="width:100%;">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr valign="middle" class="smallButtons">
            <td nowrap class="tableRowActions">
                <input disabled name="removeBtn" id="removeBtn" type="button" value="<webapps:pageText key="remove" type="button" shared="true"/>" onClick="javascript:saveState(document.usgcbSecurityProfileForm, '/usgcbSecurityTemplateListing.do?action=remove');">
            </td>
            <td nowrap class="tableRowActions">
                <input disabled name="editBtn" id="editBtn" type="button" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState(document.usgcbSecurityProfileForm, '/usgcbSecurityTemplateListing.do?action=edit');">
            </td>
            <td nowrap class="tableRowActions" width="100%" align="right">
                <input name="addBtn" id="addBtn" type="button" value="<webapps:pageText key="add" type="button" shared="true"/>" onClick="javascript:saveState(document.usgcbSecurityProfileForm, '/usgcbSecurityTemplateListing.do?action=add');">
            </td>
        </tr>
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


<div id="pageNav">
    <input type="button" class="mainBtn" name="done" value=" <webapps:pageText key="done" shared="true" type="button" /> " onClick="javascript:saveState(document.usgcbSecurityProfileForm, '/config.do');">
</div>
</div>
</div>
</div>

</html:form>

</body>
</html:html>
<%--
// Copyright 2021, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$
// author: Nandakumar Sankaralingam
--%>

<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/includes/directives.jsp"%>

<%@ include file="/includes/startHeadSection.jsp"%>

<script>
	$(function() {
		$('#settings').addClass('nav-selected');
	});

	function saveState(form, forwardaction) {
		var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";
		form.action = fullpath;
		form.submit();
	}

	// this function checks/unchecks all rows when "select-all" is checked/unchecked

	function setChecked(val) {
		var colForm = document.scapSecurityOsMapCveIdsForm;
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
		var colForm = document.scapSecurityOsMapCveIdsForm;
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
		var colForm = document.scapSecurityOsMapCveIdsForm;
		var len = colForm.elements.length;
		var i = 0;
		var totalChecked = 0;

		for (i = 0; i < len; i++) {
			if (((colForm.elements[i].name).indexOf("profile_sel_")) > -1) {
				if (colForm.elements[i].checked) {
					totalChecked++;
					if (totalChecked > 1) {
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

<style>
.table>thead>tr>th {
  text-align: left;
}

.table>tbody>tr>td {
  text-align: left;
}
#OK.statusMessage h6 {
    background-color: #6c8a28;
}

#OK.statusMessage {
    border: 1px solid #6c8a28;
}

.statusMessage {
    margin: 15px 0px 15px 0px;
    font-size: 16px;
}

.statusMessage h6 {
    font-size: 15px;
    font-weight: bold;
    text-align: left;
    padding: 4px 0 4px 5px;
    margin: 0;
    color: #ffffff;
}

#critical.statusMessage h6 {
    background-color: #9e0000;
}

#critical.statusMessage {
    border: 1px solid #9e0000;
}

.statusMessage h6 {
    font-size: 15px;
    font-weight: bold;
    text-align: left;
    padding: 4px 0 4px 5px;
    margin: 0;
    color: #ffffff;
}
</style>

<%@ include file="/includes/endHeadSection.jsp"%>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<webapps:helpContext context="spm" topic="SCAPSecurity_Options" />

<body>
  <%-- 
<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>
 --%>

  <html:form name="scapSecurityOsMapCveIdsForm" action="/scapSecurityOsMappingCveIdsListing.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.scapSecurityOsMapCveIdsForm">
    <html:hidden property="forward" />

    <%@ include file="/dashboard/startMainSection.jsp"%>

    <div class="card">
      <div class="card-body">
        <br />

        <%@ include file="/includes/usererrors.jsp"%>
        <%@ include file="/includes/help.jsp"%>


        <logic:present scope="request" name="success">
          <div class="statusMessage" id="OK">
            <h6>
              <webapps:pageText key="success" type="global" />
            </h6>

            <p>
            <ul>
              <logic:iterate id="success" name="success">
                <li><%= success %></li>
              </logic:iterate>
            </ul>
            </p>
          </div>
        </logic:present>

        <logic:present scope="request" name="remove.allSuccess">
          <logic:equal name="remove.allSuccess" value="true">
            <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">
            <font class="greenText"><b><font class="generalText"> <webapps:text key="page.remove.cveidosmapping.success" />
              </font></b></font>
          </logic:equal>
        </logic:present>


        <div class="tableWrapper" style="width: 100%;">
          <table class="mb-2 mt-2" width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr valign="middle" class="smallButtons">
              <td nowrap class="tableRowActions"><input disabled class="btn btn-sm btn-outline-primary" name="removeBtn" id="removeBtn" type="button" value="<webapps:pageText key="remove" type="button" shared="true"/>" onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMappingCveIdsListing.do?action=remove');"></td>
              <td nowrap class="tableRowActions px-2"><input disabled class="btn btn-sm btn-outline-primary" name="editBtn" id="editBtn" type="button" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMappingCveIdsListing.do?action=edit');"></td>
              <td nowrap class="tableRowActions" width="100%" align="right"><input class="btn btn-sm btn-primary" name="addBtn" id="addBtn" type="button" value="<webapps:pageText key="add" type="button" shared="true"/>" onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMappingCveIdsListing.do?action=add');"></td>
            </tr>
          </table>


          <table class="table table-striped" width="100%" border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 0">
            <colgroup width="0*">
            </colgroup>
            <colgroup width="20%">
            </colgroup>
            <colgroup width="25%">
            </colgroup>
            <colgroup width="55%">
            </colgroup>
            <thead>
              <tr>
                <td class="tableHeaderCell"><input type="checkbox" id="select_all_profiles" name="select_all_profiles" onClick="setChecked(checked);isAnySelected();"></td>
                <td class="tableHeaderCell" width="20%"><webapps:pageText key="ProfileName" /></td>
                <td class="tableHeaderCell" width="25%"><webapps:pageText key="OsName" /></td>
                <td class="tableHeaderCell" width="55%"><webapps:pageText key="CveIdsList" /></td>
              </tr>
            </thead>

            <tbody>
              <logic:iterate id="profile" indexId="index" name="profiles">
                <tr <% if(index.intValue() % 2 == 1) {%> class="alternateRowColor" <%}%>>
                  <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>><input type="checkbox" id="profile_sel_<bean:write name="profile" property="name"/>" name="profile_sel_<bean:write name="profile" property="osIndex"/>" onClick="setCheckedAll(checked);isAnySelected();"></td>
                  <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>><bean:write name="profile" property="name" /></td>
                  <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>><bean:write name="profile" property="os" /></td>
                  <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>><bean:write name="profile" property="cveids" /></td>
                </tr>
              </logic:iterate>
            </tbody>
          </table>
        </div>
      </div>

    </div>



    <div class="col" style="text-align: end;">
      <input type="button" class="btn btn-sm btn-primary mainBtn" name="done" value=" <webapps:pageText key="done" shared="true" type="button" /> " onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/config.do');">
    </div>

    <%@ include file="/dashboard/endMainSection.jsp"%>
  </html:form>

</body>
</html>

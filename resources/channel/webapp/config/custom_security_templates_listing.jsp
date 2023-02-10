<%@ page contentType="text/html;charset=UTF-8"%><%@ include file="/includes/directives.jsp"%><%@ include file="/includes/startHeadSection.jsp"%><script>  $(function () {      $('#settings').addClass('nav-selected');  });	function saveState(form, forwardaction) {		var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";		form.action = fullpath;		form.submit();	}	// this function checks/unchecks all rows when "select-all" is checked/unchecked	function setChecked(val) {		var colForm = document.customSecurityProfileForm;		var len = colForm.elements.length;		var i = 0;		for (i = 0; i < len; i++) {			if (((colForm.elements[i].name).indexOf("profile_sel_")) > -1) {				colForm.elements[i].checked = val;			}		}	}	// this function does the reverse of the above function if all te rows are checked, "select-all" gets checked	function setCheckedAll(val) {		var colForm = document.customSecurityProfileForm;		var len = colForm.elements.length;		var i = 0;		var allChecked = true;		var rowsPresent = false;		for (i = 0; i < len; i++) {			if (((colForm.elements[i].name).indexOf("profile_sel_")) > -1) {				rowsPresent = true;				if (!colForm.elements[i].checked) {					allChecked = false;					break;				}			}		}		if (rowsPresent) {			colForm.select_all_profiles.checked = allChecked;		}	}	// This function Checks whether atleast one check box is checked or not. If atleast one checkbox is checked then remove button will be enabled	function isAnySelected() {		var colForm = document.customSecurityProfileForm;		var len = colForm.elements.length;		var i = 0;		var totalChecked = 0;		for (i = 0; i < len; i++) {			if (((colForm.elements[i].name).indexOf("profile_sel_")) > -1) {				if (colForm.elements[i].checked) {					totalChecked++;					if (totalChecked > 1) {						break;					}				}			}		}		colForm.removeBtn.disabled = (totalChecked == 0);		colForm.editBtn.disabled = (totalChecked != 1);		colForm.exportbtn.disabled = (totalChecked == 0);	}</script><style>.table>thead>tr>th {	text-align: left;}.table>tbody>tr>td {	text-align: left;}</style><%@ include file="/includes/endHeadSection.jsp"%><%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%><%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%><webapps:helpContext context="spm" topic="CustomSecurity_Options" /><body>  <%-- <% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="cfgview"/>
<% } %> --%>  <html:form name="customSecurityProfileForm" action="/customSecurityTemplateListing.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.customSecurityProfileForm">    <html:hidden property="forward" />    <%@ include file="/dashboard/startMainSection.jsp"%>    <%@ include file="/includes/usererrors.jsp"%>    <div class="card">      <div class="card-body">        <br />        <%@ include file="/includes/help.jsp"%>        <logic:present scope="request" name="result">          <bean:define id="res" name="result" />          <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">          <font class="greenText"><b><font class="generalText"> <%= res %>            </font></b></font>        </logic:present>        <logic:present scope="request" name="createedit.result">          <logic:present scope="request" name="create.result">            <bean:define id="res" name="create.result" />            <logic:equal name="createedit.result" value="success">              <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">              <font class="greenText"><b><font class="generalText"> <%= res %>                </font></b></font>            </logic:equal>            <logic:notEqual name="createedit.result" value="success">              <img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()">              <font class="redText"><b><font class="generalText"> <%= res %>                </font></b></font>            </logic:notEqual>          </logic:present>          <logic:present scope="request" name="edit.result">            <bean:define id="res" name="edit.result" />            <logic:equal name="createedit.result" value="success">              <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">              <font class="greenText"><b><font class="generalText"> <%= res %>                </font></b></font>            </logic:equal>            <logic:notEqual name="createedit.result" value="success">              <img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()">              <font class="redText"><b><font class="generalText"> <%= res %>                </font></b></font>            </logic:notEqual>          </logic:present>        </logic:present>        <logic:present scope="request" name="remove.allSuccess">          <logic:equal name="remove.allSuccess" value="true">            <img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()">            <font class="greenText"><b><font class="generalText"> <webapps:text key="page.remove.template.success" />              </font></b></font>          </logic:equal>        </logic:present>        <logic:notPresent scope="request" name="remove.allSuccess">          <table class="table table-borderless" width="100%" border=0>            <tr>              <logic:present scope="request" name="remove.success">                <td><img src="/shell/common-rsrc/images/check_confirm.gif" width="24" height="24" onLoad="init()"> <font class="greenText"><b><font class="generalText"> <webapps:text key="page.remove.template.success.list" />                    </font></b></font>                  <ul>                    <logic:iterate name="remove.success" id="successId">                      <li><bean:write name="successId" /></li>                    </logic:iterate>                  </ul></td>              </logic:present>              <logic:present scope="request" name="remove.errors">                <td><img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()"> <font class="redText"><b><font class="generalText"> <webapps:text key="page.remove.template.error.list" />                    </font></b></font>                  <ul>                    <logic:iterate name="remove.errors" id="errorsId">                      <li><bean:write name="errorsId" /></li>                    </logic:iterate>                  </ul></td>              </logic:present>            </tr>          </table>        </logic:notPresent>        <%
Object customContentsMapObj = session.getAttribute("customcontentdetailsmap");
if ((customContentsMapObj != null) && (((java.util.Map<String, String>)customContentsMapObj).size() > 0)) {
%>        <div class="tableWrapper" style="width: 100%;">          <table class="mb-2 mt-2" width="100%" border="0" cellspacing="0" cellpadding="0">            <tr valign="middle" class="smallButtons">              <td nowrap class="tableRowActions"><input disabled name="removeBtn" class="btn btn-sm btn-outline-primary" id="removeBtn" type="button" value="<webapps:pageText key="remove" type="button" shared="true"/>" onClick="javascript:saveState(document.customSecurityProfileForm, '/customSecurityTemplateListing.do?action=remove');"></td>              <td nowrap class="tableRowActions px-2"><input disabled name="editBtn" class="btn btn-sm btn-outline-primary" id="editBtn" type="button" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState(document.customSecurityProfileForm, '/customSecurityTemplateListing.do?action=edit');"></td>              <td nowrap class="tableRowActions" width="100%" align="right"><input name="addBtn" class="btn btn-sm btn-primary" id="addBtn" type="button" value="<webapps:pageText key="add" type="button" shared="true"/>" onClick="javascript:saveState(document.customSecurityProfileForm, '/customSecurityTemplateListing.do?action=add');"></td>            </tr>          </table>          <table class="table table-striped" width="100%" border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 0">            <colgroup width="0*">            </colgroup>            <colgroup width="30%">            </colgroup>            <colgroup width="65%">            </colgroup>            <thead>              <tr>                <th class="tableHeaderCell"><input type="checkbox" id="select_all_profiles" name="select_all_profiles" onClick="setChecked(checked);isAnySelected();"></th>                <th class="tableHeaderCell"><webapps:pageText key="TitleName" /></th>                <th class="tableHeaderCell"><webapps:pageText key="TitleDesc" /></th>              </tr>            </thead>            <tbody>              <logic:iterate id="profile" indexId="index" name="profiles">                <tr align="left" <% if(index.intValue() % 2 == 1) {%> class="alternateRowColor" <%}%>>                  <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>><input type="checkbox" id="profile_sel_<bean:write name="profile" property="name"/>" name="profile_sel_<bean:write name="profile" property="name"/>" onClick="setCheckedAll(checked);isAnySelected();"></td>                  <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>><bean:write name="profile" property="name" /></td>                  <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>><bean:write name="profile" property="description" /></td>                </tr>              </logic:iterate>            </tbody>          </table>        </div>        <%-- 
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
                    <tr align="left" <% if(index.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
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
    </div> --%>        <%
} else {
%>        <b>Marimba vDef used in your environment doesn't include any vMediate in it. <br>Please check with your Administrator on availability of any new updates for Security Info.        </b>        <%
}
%>      </div>    </div>    <div class="col" style="text-align: end;">      <input type="button" class="btn btn-sm btn-primary mainBtn" name="done" value=" <webapps:pageText key="done" shared="true" type="button" /> " onClick="javascript:saveState(document.customSecurityProfileForm, '/config.do');">    </div>    <%@ include file="/dashboard/endMainSection.jsp"%>  </html:form></body></html>
<%--
// Copyright 2021, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$
// author: Nandakumar Sankaralingam
--%>

<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/includes/directives.jsp"%>
<webapps:helpContext context="spm" topic="scap_security_profile" />
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%
 Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<%@ include file="/includes/startHeadSection.jsp"%>

<script>
	$(function() {
		$('#settings').addClass('nav-selected');
	});

	var modifiedRulesAndValues = '';
	<logic:present scope="request" name="remove.errors">
	alert("error occurred..");
	</logic:present>

	function saveState(form, forwardaction) {
		var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";
		form.action = fullpath;
		form.submit();
	}

	function loadInitialContentSettings() {
		console.log("called loadInitialContentSettings...");
	}

	function nospaces(t) {
		if (t.value.match(/\s/g)) {
			alert('<webapps:pageText key="nospace" type="global" shared="true"/>');
			t.value = t.value.replace(/\s/g, '');
		}
	}
</script>


<<style>
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

<body onload="loadInitialContentSettings();">
  <%-- 
        <% if(null != EmpirumContext) {%>
            <webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
        <% } else { %>
            <webapps:tabs tabset="main" tab="cfgview"/>
        <% } %>
 --%>

  <html:form name="scapSecurityOsMapCveIdsForm" action="/scapSecurityOsMapCveIdProfile.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.scapSecurityOsMapCveIdsForm" onsubmit="return false;">
    
  <html:hidden property="create" />
  <html:hidden property="previousOsIndex" />
  
  
      <main id="main" class="main">
      <div class="pagetitle">
        <div class="d-flex bd-highlight justify-content-center">
          <div class="p-2 flex-grow-1 bd-highlight">

            <logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">

              <span class="pagename"><webapps:pageText key="TitleAdd" /></span>
              <span data-bs-toggle="tooltip" data-bs-placement="right" title="<webapps:pageText key="TitleAdd"/>"><i class="fa-solid fa-circle-info text-primary"></i></span>

            </logic:equal>
            <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">

              <span class="pagename"><webapps:pageText key="TitleEdit" /></span>
              <span data-bs-toggle="tooltip" data-bs-placement="right" title="<webapps:pageText key="TitleEdit"/>"><i class="fa-solid fa-circle-info text-primary"></i></span>

            </logic:notEqual>

          </div>
          <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh" style="cursor: pointer;">
            <i class="fa-solid fa-arrows-rotate"></i>
          </div>
          <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right" title="Download" style="cursor: pointer;">
            <i class="fa-solid fa-download"></i>
          </div>
          <div class="p-2 bd-highlight text-primary align-self-center">
            <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home
            </a>
          </div>
        </div>

      </div>

      <section class="section dashboard">

      
        <div class="card">
          <div class="card-body">
            <br />
      

        <%@ include file="/includes/usererrors.jsp"%>
        <%@ include file="/includes/help.jsp"%>
        
        <logic:present scope="request" name="errors">
          <div class="statusMessage" id="critical">
            <h6>
              <webapps:pageText key="errors" type="global" />
            </h6>
            <p>
            <ul>
              <logic:iterate id="error" name="errors">
                <li><%= error %></li>
              </logic:iterate>
            </ul>
            </p>
          </div>
        </logic:present>

            <table border="0" cellspacing="0" cellpadding="5">
              <tr>
                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><webapps:pageText key="label.profileName" />:</td>
                <td style="padding-top: 5px; padding-bottom: 5px"><logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                    <html:text property="profileName" size="30" maxlength="30" styleClass="requiredField" onkeyup="nospaces(this)" />
                  </logic:equal> <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                    <html:hidden property="profileName" />
                    <b><bean:write name="scapSecurityOsMapCveIdsForm" property="profileName" /></b>
                  </logic:notEqual></td>
              </tr>
              <tr>
                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><webapps:pageText key="label.profileDesc" />:</td>
                <td style="padding-top: 5px; padding-bottom: 5px"><html:text property="profileDesc" size="50" maxlength="100" /></td>
              </tr>
              <tr>
                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><webapps:pageText key="label.OS" />:</td>
                <td style="padding-top: 5px; padding-bottom: 5px"><logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                    <html:select name="scapSecurityOsMapCveIdsForm" property="osName" styleClass="requiredField" styleId="os_id" onchange="">
                      <html:optionsCollection name="scapSecurityOsMapCveIdsForm" property="osList" label="osValue" value="osIndex" />
                    </html:select>
                  </logic:equal> <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                    <html:select disabled="true" name="scapSecurityOsMapCveIdsForm" property="osName" styleClass="requiredField" styleId="os_id" onchange="">
                      <html:optionsCollection name="scapSecurityOsMapCveIdsForm" property="osList" label="osValue" value="osIndex" />
                    </html:select>
                  </logic:notEqual></td>
              </tr>
              <tr>
                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><webapps:pageText key="label.cveidslist" />:</td>
                <td style="padding-top: 5px; padding-bottom: 5px"><html:textarea property="cveIds" rows="5" cols="60" styleClass="requiredField"></html:textarea></td>
              </tr>
            </table>

          <div class="col" style="text-align: end;">
            <logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
              <input type="button" class="btn btn-sm btn-primary mainBtn" name="save" value=" <webapps:pageText key="save" type="global" /> " onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMapCveIdProfile.do?action=save');">
            </logic:equal>
            <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
              <input type="button" class="mainBtn" name="apply" value=" <webapps:pageText key="saveandapply" type="global" /> " onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMapCveIdProfile.do?action=apply');">
            </logic:notEqual>
            <input type="button" class="btn btn-sm btn-outline-primary" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMapCveIdProfile.do?action=load');">
          </div>
        </div>
      
  </html:form>
</body>
</html>
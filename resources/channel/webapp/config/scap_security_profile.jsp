<%@ page contentType="text/html;charset=UTF-8"%><%@ include file="/includes/directives.jsp"%><webapps:helpContext context="spm" topic="scap_security_profile" /><%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%><%
 Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%><%@ include file="/includes/startHeadSection.jsp"%><script>$(function () {    $('#settings').addClass('nav-selected');});
            var modifiedRulesAndValues = '';
            function saveState(form, forwardaction) {
                var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";
                form.action = fullpath;
                form.modifiedRules.value = modifiedRulesAndValues;
                if ('/scapSecurityProfile.do?action=apply' == forwardaction) {
                    form.selectedSCAPProfile.value = '<%= request.getAttribute("profile.id")%>';
                } else if ('/scapSecurityProfile.do?action=load' == forwardaction) {
                } else {
                    form.selectedSCAPProfile.value = document.getElementById("selectedSCAPProfileId").value;
                }
                form.submit();
            }
            function initialContentProfileSettings() {
                switchContentProfileSettings(document.scapSecurityProfileForm.selectedSCAPContent.value);
            }
            function switchContentProfileSettings(contentname) {
                $.ajax({
                    url: '/spm/securitymgmt?command=scapcontentchange&content=' + contentname,
                    type: 'GET',
                    cache: false,
                    async: false,
                    dataType: "json",
                    contentType: 'application/json',
                    success: function (response) {
                        var profileIdsResponse = response.profilesId;
                        var profileTitlesResponse = response.profilesTitle;

                        var text = '<select id="selectedSCAPProfileId" onchange="switchProfileInfo(this.value)">';
                        for (var i=0; i < profileIdsResponse.length; i++) {
                            if (i == 0) {
                                text += '<option selected="selected" value="' + profileIdsResponse[i] + '">' + profileTitlesResponse[i] + '</option>';
                            } else {
                                text += '<option value="' + profileIdsResponse[i] + '">' + profileTitlesResponse[i] + '</option>';
                            }
                        }

                        text += '</select>';

                        var divParent = document.getElementById("selectedSCAPProfileDiv");
                        while(divParent.firstChild) {
                            divParent.removeChild(divParent.firstChild);
                        }

                        var divChild = document.createElement("div");
                        divChild.innerHTML = text;
                        divParent.appendChild(divChild);

                        document.getElementById('iframeProfileInfo').src = '/spm/securitymgmt?command=gethtml&target=nonwindows&customize=true&content=' + contentname + '&profile=' + profileIdsResponse[0];
                    },
                    fail: function(xhr, status, err) {
                        //ignore...
                    }
                });
            }
            function switchProfileInfo(profilename) {
                var guidename = document.getElementById("guide_id").value;
                var templateName = '';
                if (profilename.lastIndexOf("@"+templateName) != -1) {
                    templateName = profilename.substring(profilename.lastIndexOf("@")+1);
                    profilename = profilename.substring(0,profilename.lastIndexOf("@"));
                }
                var srcUrl = '/spm/securitymgmt?command=gethtml&target=nonwindows&customize=true&content=' + guidename + '&profile=' + profilename;
                if (templateName != '') {
                    srcUrl = srcUrl + "&template="+ templateName + ".properties";
                }
                document.getElementById('iframeProfileInfo').src = srcUrl;
            }
            function loadCustomizationInfo() {
                if (window.frames != null) {
                    var requiredIFrame = window.frames['iframeProfileInfo'];
                    if (requiredIFrame != null) {
                        var requiredIFrameDoc = requiredIFrame.contentDocument || requiredIFrame.document || requiredIFrame.contentWindow.document;
                        if (requiredIFrameDoc != null) {
                            <% String[] customization =(String[])request.getAttribute("customization");
                            if (customization != null) {
                                for(int i=0; i<customization.length; i+=2) { %>
                                    var key = '<%= customization[i] %>';
                                    var value = '<%= customization[i+1] %>	';					var item = requiredIFrameDoc.getElementById(key);					if (item != null) {						if ("checkbox" == item.type) {							if ("true" == value) {								item.checked = true;							} else {								item.checked = false;							}						} else if (("select" == item.type)								|| ("select-one" == item.type)) {							var exists = "false";							for (i = 0; i < item.length; ++i) {								if (item.options[i].value == value) {									exists = "true";									break;								}							}							if (exists == "false") {								var opt = document.createElement('option');								opt.value = value;								opt.innerHTML = value;								item.appendChild(opt);							}							item.value = value;							key = key.substring(0, key.length - 6) + "Text";							item = requiredIFrameDoc.getElementById(key);							if (item != null) {								if ("text" == item.type) {									item.value = value;								}							}						}					}<%}
                            }%>	}			}		}	}	function nospaces(t) {		if (t.value.match(/\s/g)) {			alert('<webapps:pageText key="nospace" type="global" shared="true"/>');			t.value = t.value.replace(/\s/g, '');		}	}</script><%@ include file="/includes/endHeadSection.jsp"%><body onload="initialContentProfileSettings()">  <%--         <% if(null != EmpirumContext) {%>
            <webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
        <% } else { %>
            <webapps:tabs tabset="main" tab="cfgview"/>
        <% } %> --%>  <html:form name="scapSecurityProfileForm" action="/scapSecurityProfile.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.ScapSecurityProfileForm">    <html:hidden property="create" />    <html:hidden property="modifiedRules" />    <html:hidden property="selectedSCAPProfile" />    <main id="main" class="main">      <div class="pagetitle">        <div class="d-flex bd-highlight justify-content-center">          <div class="p-2 flex-grow-1 bd-highlight">            <logic:equal name="scapSecurityProfileForm" property="create" value="true">              <span class="pagename"><webapps:pageText key="TitleAdd" /></span>              <span data-bs-toggle="tooltip" data-bs-placement="right" title="<webapps:pageText key="TitleAdd"/>"><i class="fa-solid fa-circle-info text-primary"></i></span>            </logic:equal>            <logic:notEqual name="scapSecurityProfileForm" property="create" value="true">              <span class="pagename"><webapps:pageText key="TitleEdit" /></span>              <span data-bs-toggle="tooltip" data-bs-placement="right" title="<webapps:pageText key="TitleEdit"/>"><i class="fa-solid fa-circle-info text-primary"></i></span>            </logic:notEqual>          </div>          <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh" style="cursor: pointer;">            <i class="fa-solid fa-arrows-rotate"></i>          </div>          <div class="p-2 bd-highlight text-primary align-self-center">            <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home            </a>          </div>        </div>      </div>      <section class="section dashboard">        <div class="card">          <div class="card-body">            <br />            <%@ include file="/includes/usererrors.jsp"%>            <%@ include file="/includes/help.jsp"%>            <table border="0" cellspacing="0" cellpadding="3" style="width: 100%;">              <tr>                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><webapps:pageText key="label.profileName" /></td>                <td style="padding-top: 5px; padding-bottom: 5px"><logic:equal name="scapSecurityProfileForm" property="create" value="true">                    <html:text property="name" size="30" maxlength="30" styleClass="requiredField" onkeyup="nospaces(this)" />                  </logic:equal> <logic:notEqual name="scapSecurityProfileForm" property="create" value="true">                    <html:hidden property="name" />                    <b><bean:write name="scapSecurityProfileForm" property="name" /></b>                  </logic:notEqual></td>              </tr>              <tr>                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><webapps:pageText key="label.profileDesc" /></td>                <td style="padding-top: 5px; padding-bottom: 5px"><html:text property="description" size="50" maxlength="100" styleClass="requiredField" /></td>              </tr>              <tr>                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><logic:equal name="scapSecurityProfileForm" property="create" value="true">                    <webapps:pageText key="label.scapcontent.select" />                  </logic:equal> <logic:notEqual name="scapSecurityProfileForm" property="create" value="true">                    <webapps:pageText key="label.scapcontent.standard" />                  </logic:notEqual></td>                <td style="padding-top: 5px; padding-bottom: 5px"><logic:equal name="scapSecurityProfileForm" property="create" value="true">                    <html:select property="selectedSCAPContent" styleId="guide_id" onchange="switchContentProfileSettings(this.value)">                      <html:options collection="insyncscapcontentdetailsmap" property="key" labelProperty="value" />                    </html:select>                  </logic:equal> <logic:notEqual name="scapSecurityProfileForm" property="create" value="true">                    <b><%= request.getAttribute("content.title")%></b>                  </logic:notEqual></td>              </tr>              <tr>                <td align="left" style="padding-top: 5px; padding-bottom: 5px"><logic:equal name="scapSecurityProfileForm" property="create" value="true">                    <webapps:pageText key="label.scapprofile.select" />                  </logic:equal> <logic:notEqual name="scapSecurityProfileForm" property="create" value="true">                    <webapps:pageText key="label.scapprofile.standard" />                  </logic:notEqual></td>                <td style="padding-top: 5px; padding-bottom: 5px">                  <div id="selectedSCAPProfileDiv">                    <logic:equal name="scapSecurityProfileForm" property="create" value="true">                      <html:select property="selectedSCAPProfileSelect" styleId="selectedSCAPProfileId" onchange="switchProfileInfo(this.value)">                        <html:options collection="first_scap_content_profiles" property="key" labelProperty="value" />                      </html:select>                    </logic:equal>                    <logic:notEqual name="scapSecurityProfileForm" property="create" value="true">                      <b><%= request.getAttribute("profile.title")%></b>                    </logic:notEqual>                  </div>                </td>              </tr>              <tr>                <td colspan="2"><iframe id="iframeProfileInfo" src='<bean:write name="scapSecurityProfileForm" property="initialIFramePath"/>' width="100%" height="625px" frameBorder="0"></iframe></td>              </tr>              <!--<tr>
                                    <td nowrap style="padding-left:15px;" colspan="2" align="left">
                                        <html:checkbox property="forceApplyEnabled"/>
                                        <webapps:pageText key="policyapplylabel"/>
                                    </td>
                                </tr>-->            </table>          </div>        </div>        <div class="col" style="text-align: end;">          <logic:equal name="scapSecurityProfileForm" property="create" value="true">            <input type="button" class="btn btn-sm btn-primary mainBtn" name="save" value=" <webapps:pageText key="save" type="global" /> " onClick="javascript:saveState(document.scapSecurityProfileForm, '/scapSecurityProfile.do?action=save');">          </logic:equal>          <logic:notEqual name="scapSecurityProfileForm" property="create" value="true">            <input type="button" class="btn btn-sm btn-primary mainBtn" name="apply" value=" <webapps:pageText key="saveandapply" type="global" /> " onClick="javascript:saveState(document.scapSecurityProfileForm, '/scapSecurityProfile.do?action=apply');">          </logic:notEqual>          <input type="button" class="btn btn-sm btn-outline-primary " name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:saveState(document.scapSecurityProfileForm, '/scapSecurityProfile.do?action=load');">        </div>        <%@ include file="/dashboard/endMainSection.jsp"%>  </html:form></body></html>
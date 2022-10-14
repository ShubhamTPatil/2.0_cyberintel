<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<webapps:helpContext context="spm" topic="usgcb_security_profile"/>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>    
<%
 Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<html>
    <head>
        <title>
            <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                <webapps:pageText key="TitleAdd"/>
            </logic:equal>

            <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                <webapps:pageText key="TitleEdit"/>
            </logic:notEqual>
        </title>
        <link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css" />
        <script>
            var modifiedRulesAndValues = '';
            function saveState(form, forwardaction) {
                var fullpath = "<webapps:fullPath path='" + forwardaction + "' />";
                form.action = fullpath;
                form.modifiedRules.value = modifiedRulesAndValues;
                if ('/usgcbSecurityProfile.do?action=apply' == forwardaction) {
                    form.selectedSCAPProfile.value = '<%= request.getAttribute("profile.id")%>';
                } else if ('/usgcbSecurityProfile.do?action=load' == forwardaction) {
                } else {
                    form.selectedSCAPProfile.value = document.getElementById("selectedSCAPProfileId").value;
                }
                form.submit();
            }
            function initialContentProfileSettings() {
                switchContentProfileSettings(document.usgcbSecurityProfileForm.selectedSCAPContent.value);
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

                        switchProfileInfo(profileIdsResponse[0]);
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
                var srcUrl = '/spm/securitymgmt?command=gethtml&target=windows&customize=true&content=' + guidename + '&profile=' + profilename;
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
                                    var value = '<%= customization[i+1] %>';
                                    var item = requiredIFrameDoc.getElementById(key);
                                    if (item != null) {
                                        if ("checkbox" == item.type) {
                                            if ("true" == value) {
                                                item.checked = true;
                                            } else {
                                                item.checked = false;
                                            }
                                        } else if (("select" == item.type) || ("select-one" == item.type)) {
                                            var exists = "false";
                                            for (i = 0; i < item.length; ++i){
                                                if (item.options[i].value == value){
                                                    exists = "true";
                                                    break;
                                                }
                                            }
                                            if (exists == "false") {
                                                var opt = document.createElement('option');
                                                opt.value = value;
                                                opt.innerHTML = value;
                                                item.appendChild(opt);
                                            }
                                            item.value = value;
                                            key = key.substring(0, key.length - 6) + "Text";
                                            item = requiredIFrameDoc.getElementById(key);
                                            if (item != null) {
                                                if ("text" == item.type) {
                                                    item.value = value;
                                                }
                                            }
                                        }
                                    }
                                <%}
                            }%>
                        }
                    }
                }
            }
            function nospaces(t){
                if(t.value.match(/\s/g)){
                    alert('<webapps:pageText key="nospace" type="global" shared="true"/>');
                    t.value=t.value.replace(/\s/g,'');
                }
            }
        </script>
    </head>
    <body onload="initialContentProfileSettings()">
        <% if(null != EmpirumContext) {%>
            <webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
        <% } else { %>
            <webapps:tabs tabset="main" tab="cfgview"/>
        <% } %>

        <html:form name="usgcbSecurityProfileForm" action="/usgcbSecurityProfile.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.USGCBSecurityProfileForm">
            <div align="center">
                <div id="contentPadding">
                    <html:hidden property="create"/>
                    <html:hidden property="modifiedRules"/>
					<html:hidden property="selectedSCAPProfile"/>

                    <div class="pageHeader">
                        <span class="title">
                            <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                                <webapps:pageText key="TitleAdd"/>
                            </logic:equal>
                            <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                                <webapps:pageText key="TitleEdit"/>
                            </logic:notEqual>
                        </span>
                    </div>

                    <%@ include file="/includes/usererrors.jsp" %>
                    <%@ include file="/includes/help.jsp" %>
                    <div style="width:1000px">
                        <div align="left" style="padding-left:8px;">
                            <table border="0" cellspacing="0" cellpadding="3">
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px"><webapps:pageText key="label.profileName"/></td>
                                    <td style="padding-top:5px;padding-bottom:5px">
                                        <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                                            <html:text property="name" size="30" maxlength="30" styleClass="requiredField" onkeyup="nospaces(this)"/>
                                        </logic:equal>
                                        <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                                            <html:hidden property="name"/>
                                            <b><bean:write name="usgcbSecurityProfileForm" property="name"/></b>
                                        </logic:notEqual>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px"><webapps:pageText key="label.profileDesc"/></td>
                                    <td style="padding-top:5px;padding-bottom:5px"><html:text property="description" size="50" maxlength="100" styleClass="requiredField"/></td>
                                </tr>
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px">
                                        <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                                            <webapps:pageText key="label.scapcontent.select"/>
                                        </logic:equal>
                                        <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                                            <webapps:pageText key="label.scapcontent.standard"/>
                                        </logic:notEqual>
                                    </td>
                                    <td style="padding-top:5px;padding-bottom:5px">
                                        <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                                            <html:select property="selectedSCAPContent" styleId="guide_id" onchange="switchContentProfileSettings(this.value)">
                                                <html:options collection="insyncusgcbcontentdetailsmap" property="key" labelProperty="value" />
                                            </html:select>
                                        </logic:equal>
                                        <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                                            <b><%= request.getAttribute("content.title")%></b>
                                        </logic:notEqual>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px">
                                        <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                                            <webapps:pageText key="label.scapprofile.select"/>
                                        </logic:equal>
                                        <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                                            <webapps:pageText key="label.scapprofile.standard"/>
                                        </logic:notEqual>
                                    </td>
                                    <td style="padding-top:5px;padding-bottom:5px">
                                        <div id="selectedSCAPProfileDiv">
                                            <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                                                <html:select property="selectedSCAPProfileSelect" styleId="selectedSCAPProfileId" onchange="switchProfileInfo(this.value)">
                                                    <html:options collection="first_usgcb_content_profiles" property="key" labelProperty="value" />
                                                </html:select>
                                            </logic:equal>
                                            <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                                                <b><%= request.getAttribute("profile.title")%></b>
                                            </logic:notEqual>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                         <iframe id="iframeProfileInfo" src='<bean:write name="usgcbSecurityProfileForm" property="initialIFramePath"/>' width="1100px" height="625px" frameBorder="0"></iframe>
                                    </td>
                                </tr>
                                <!--<tr>
                                    <td nowrap style="padding-left:15px;" colspan="2" align="left">
                                        <html:checkbox property="forceApplyEnabled"/>
                                        <webapps:pageText key="policyapplylabel"/>
                                    </td>-->
                                </tr>
                            </table>
                        </div>

                        <div id="pageNav" style="padding-left:110px;">
                            <logic:equal name="usgcbSecurityProfileForm" property="create" value="true">
                                <input type="button" class="mainBtn" name="save" value=" <webapps:pageText key="save" type="global" /> " onClick="javascript:saveState(document.usgcbSecurityProfileForm, '/usgcbSecurityProfile.do?action=save');">
                            </logic:equal>
                            <logic:notEqual name="usgcbSecurityProfileForm" property="create" value="true">
                                <input type="button" class="mainBtn" name="apply" value=" <webapps:pageText key="saveandapply" type="global" /> " onClick="javascript:saveState(document.usgcbSecurityProfileForm, '/usgcbSecurityProfile.do?action=apply');">
                            </logic:notEqual>
                            <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:saveState(document.usgcbSecurityProfileForm, '/usgcbSecurityProfile.do?action=load');" >
                        </div>
                    </div>
                </div>
            </div>
        </html:form>
    </body>
</html>
<%--
// Copyright 2021, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$
// author: Nandakumar Sankaralingam
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<webapps:helpContext context="spm" topic="scap_security_profile"/>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
 Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<html>
    <head>
        <title>
            <logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                <webapps:pageText key="TitleAdd"/>
            </logic:equal>

            <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                <webapps:pageText key="TitleEdit"/>
            </logic:notEqual>
        </title>
        <link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css" />
        <script>
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
                alert("called loadInitialContentSettings...");
            }

            function nospaces(t){
                if(t.value.match(/\s/g)){
                    alert('<webapps:pageText key="nospace" type="global" shared="true"/>');
                    t.value=t.value.replace(/\s/g,'');
                }
            }
        </script>
    </head>
    <body onload="loadInitialContentSettings();">
        <% if(null != EmpirumContext) {%>
            <webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
        <% } else { %>
            <webapps:tabs tabset="main" tab="cfgview"/>
        <% } %>

        <html:form name="scapSecurityOsMapCveIdsForm" action="/scapSecurityOsMapCveIdProfile.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.scapSecurityOsMapCveIdsForm" onsubmit="return false;">
            <div align="center">
                <div id="contentPadding">
                   <html:hidden property="create"/>
                   <html:hidden property="previousOsIndex"/> 
                   <div class="pageHeader">
                        <span class="title">
                            <logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                <webapps:pageText key="TitleAdd"/>
                            </logic:equal>
                            <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                <webapps:pageText key="TitleEdit"/>
                            </logic:notEqual>
                        </span>
                    </div>

                    <%@ include file="/includes/usererrors.jsp" %>
                    <%@ include file="/includes/help.jsp" %>
                   <logic:present scope="request" name="errors">
                   <div class="statusMessage" id="critical">
                     <h6><webapps:pageText key="errors" type="global"/></h6>
                     <p><ul>
                     <logic:iterate id="error" name="errors">
                     <li><%= error %></li>
                     </logic:iterate>
                     </ul></p>
                   </div>
                   </logic:present>


                    <div style="width:1000px">
                        <div align="left" style="padding-left:8px;">
                            <table border="0" cellspacing="0" cellpadding="5">
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px"><webapps:pageText key="label.profileName"/>:</td>
                                    <td style="padding-top:5px;padding-bottom:5px">
                                        <logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                            <html:text property="profileName" size="30" maxlength="30" styleClass="requiredField" onkeyup="nospaces(this)"/>
                                        </logic:equal>
                                        <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                            <html:hidden property="profileName"/>
                                            <b><bean:write name="scapSecurityOsMapCveIdsForm" property="profileName"/></b>
                                        </logic:notEqual>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px"><webapps:pageText key="label.profileDesc"/>:</td>
                                    <td style="padding-top:5px;padding-bottom:5px">
                                        <html:text property="profileDesc" size="50" maxlength="100"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px">
                                            <webapps:pageText key="label.OS"/>:
                                    </td>
                                    <td style="padding-top:5px;padding-bottom:5px">
                                        <logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                            <html:select name="scapSecurityOsMapCveIdsForm" property="osName" styleClass="requiredField" styleId="os_id" onchange="">
                                                <html:optionsCollection name="scapSecurityOsMapCveIdsForm" property="osList" label="osValue" value="osIndex" />
                                            </html:select>
                                        </logic:equal>
                                        <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                            <html:select disabled="true" name="scapSecurityOsMapCveIdsForm" property="osName" styleClass="requiredField" styleId="os_id" onchange="">
                                                <html:optionsCollection name="scapSecurityOsMapCveIdsForm" property="osList" label="osValue" value="osIndex" />
                                            </html:select>
                                        </logic:notEqual>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left" style="padding-top:5px;padding-bottom:5px">
                                            <webapps:pageText key="label.cveidslist"/>:
                                    </td>
                                    <td style="padding-top:5px;padding-bottom:5px">
                                        <html:textarea property="cveIds" rows="5" cols="60" styleClass="requiredField"></html:textarea>
                                    </td>
                                </tr>
                            </table>
                        </div>

                        <div id="pageNav" style="padding-left:110px;">
                            <logic:equal name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                <input type="button" class="mainBtn" name="save" value=" <webapps:pageText key="save" type="global" /> " onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMapCveIdProfile.do?action=save');">
                            </logic:equal>
                            <logic:notEqual name="scapSecurityOsMapCveIdsForm" property="create" value="true">
                                <input type="button" class="mainBtn" name="apply" value=" <webapps:pageText key="saveandapply" type="global" /> " onClick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMapCveIdProfile.do?action=apply');">
                            </logic:notEqual>
                            <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:saveState(document.scapSecurityOsMapCveIdsForm, '/scapSecurityOsMapCveIdProfile.do?action=load');" >
                        </div>
                    </div>
                </div>
            </div>
        </html:form>
    </body>
</html>
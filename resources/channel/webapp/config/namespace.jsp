﻿<%@ page contentType="text/html;charset=UTF-8" %>
<%--
 Copyright 2004-2012, BMC Software Inc. All Rights Reserved.
 Confidential and Proprietary Information of BMC Software Inc.
 Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
 6,381,631, and 6,430,608. Other Patents Pending.

 $File$

 @author    Theen-Theen Tan
 @version   $Revision$, $Date$

--%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.forms.NamespaceForm" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="spm" topic="namesp" />
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
<html:form name="namespaceForm" action="/namespaceSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.NamespaceForm">
<div align="center">
    <div class="commonPadding">
        <div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
        <%@ include file="/includes/usererrors.jsp" %>
        <%@ include file="/includes/help.jsp" %>
        <div style="width:75%">
            <div class="formTabs">
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_lft.gif" width="5" height="5"></td>
                        <td width="100%" style="border-top:1px solid #CCCCCC; height:5px;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="4"></td>
                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_rt.gif" width="5" height="5"></td>
                    </tr>
                </table>
            </div>
            <div class="formContent" align="left">
                <table cellpadding="5" cellspacing="0">

                    <tr>
                        <td align="right" nowrap><webapps:pageText key="CurrentContainerPrompt" /></td>
                        <td><bean:write name="namespaceForm" property="rootContainer"/></td>
                    </tr>
                    <tr>
                        <td align="right" nowrap><webapps:pageText key="Prompt" /></td>
                        <td>
                            <html:select property="namespace" value='<%= ((NamespaceForm) session.getAttribute("namespaceForm")).getNamespace() %>' >
                                <html:option value=""><webapps:pageText key="DefLoc" /></html:option>
                                <html:options property="namespaceList" />
                            </html:select>
                        </td>
                    </tr>
                    <tr>
                        <td align="right"><html:checkbox name="namespaceForm" styleId="skipSettingBox" property="value(namespace_skipsetting)" value="true" /></td>
                        <td><label for="skipSettingBox"><webapps:pageText key="SkipSetting" /></label></td>
                    </tr>
                </table>

            </div>
            <div class="formBottom">
                <table width="100%" cellpadding="0" cellspacing="0">
                    <tr>
                        <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
                        <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
                        <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
                    </tr>
                </table>
            </div>
            <div id="pageNav">
                <input type="submit" class="mainBtn" name="save" value=" <webapps:pageText key="OK" type="global" /> ">
                <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.namespaceForm, '/namespaceRedirect.do');" >
            </div>
        </div>
    </div>
</div>

</html:form>

<%@ include file="/includes/footer.jsp" %>


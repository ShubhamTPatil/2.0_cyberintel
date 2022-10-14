<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)compliance_target_nav.jsp

     @author Jayaprakash Paramasivam
     @version 1.0, 11/23/2005
--%>

?<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants,
                 com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<%@ include file="/includes/endHeadSection.jsp" %>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad = "resizeDataSection('dataSection','endOfGui');" onResize="resizeDataSection('dataSection','endOfGui');">

<bean:define id="pageBeanName"  value="target_compliance_ar" toScope="request" />
<sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>"
             resultsName="targetCompDisplay" />

<html:form name="packageComplianceForm" action="/compTarget.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">

<table width="320" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="tableTitle"><webapps:pageText shared="true" type="colhdr" key="targets" /></td>
    <td align="right" nowrap>
        <bean:define id="targetFrame" value="1" toScope="request"/>
        <jsp:include page="/includes/genPrevNext.jsp" />
    </td>
  </tr>

</table>

<div class="tableWrapper" style="width:100%;">
<table width="320" border="0" cellspacing="0" cellpadding="0">
<tr valign="middle" class="smallButtons">
<td class="tableSearchRow">&nbsp;</td>
</tr>
</table>
<div class="headerSection" style="width:100%;">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">
        <thead>
            <colgroup width="20%"></colgroup>
            <colgroup width="80%"></colgroup>
            <tr>
                <td class="tableHeaderCell">
                    <a href="#" class="columnHeading"> <webapps:pageText shared="true" type="colhdr" key="contentType" /></a>&nbsp;
                </td>
                <td class="tableHeaderCell">
                    <a href="#" class="columnHeading"> <webapps:pageText key="TargetName" /></a>&nbsp;
                </td>
            </tr>
        </thead>
    </table>
</div>
<div id="dataSection" style="height:100px; width:100%; overflow:auto;">
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <colgroup width="20%"></colgroup>
        <colgroup width="80%"></colgroup>
        <% int contentsRowCount = 0; %>
        <logic:iterate id="app" name="display_rs" type="com.marimba.webapps.tools.util.PropsBean">
            <% if (contentsRowCount % 2 == 0){ %>
            <tr>
            <% } else { %>
	        <tr class="alternateRowColor">
            <% } %>
                <td class="rowLevel1" align="center">
                    <img src='<%="/shell/common-rsrc/images/" + (String) app.getValue("type") + ".gif" %>' height="16" width="16" border=0>
                </td>
                <td class="rowLevel1" >
                    <sm:getTargetAssignHRef entry="<%= app %>" /><bean:write name="app" property="value(displayname)" filter="true" /></a>
                </td>
            </tr>
        <% contentsRowCount++;%>
        </logic:iterate>
    </table>
</div>
</div>
</html:form>
<div id="endOfGui"></div>
<script>
resizeDataSection('dataSection','endOfGui');
</script>
<%@include file="/includes/footer.jsp" %>
 <%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)compliance_package_nav.jsp

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

<bean:define id="pageBeanName"  value="package_compliance_ar" toScope="request" />
<sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>"
             resultsName="channelCompDisplay" />

<html:form name="packageComplianceForm" action="/packageCompliance.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">

<table width="320" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="tableTitle"><webapps:pageText shared="true" type="colhdr" key="pkgs" /></td>
    <td align="right" nowrap>
        <bean:define id="targetFrame" value="ldapnav" toScope="request"/>
        <jsp:include page="/includes/genPrevNext.jsp" />
    </td>
  </tr>

</table>

<div class="tableWrapper">
<table border="0" cellspacing="0" cellpadding="0">
<tr valign="middle" class="smallButtons">
<td class="tableSearchRow">&nbsp;</td>
</tr>
</table>
<div class="headerSection">
    <table border="0" cellpadding="0" cellspacing="0">
        <thead>
            <colgroup width="20%"></colgroup>
            <colgroup width="80%"></colgroup>
            <tr>
                <td class="tableHeaderCell">
                    <a href="#" class="columnHeading"> <webapps:pageText shared="true" type="colhdr" key="contentType" /></a>&nbsp;
                </td>
                <td class="tableHeaderCell">
                    <a href="#" class="columnHeading"> <webapps:pageText key="PkgName" /></a>&nbsp;
                </td>
            </tr>
        </thead>
    </table>
</div>
<div id="dataSection" style="height:100px; overflow:auto;">
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <colgroup width="17%"></colgroup>
        <colgroup width="83%"></colgroup>
        <% int contentsRowCount = 0; %>
        <logic:iterate id="app" name="display_rs" type="com.marimba.webapps.tools.util.PropsBean">
                <% if (contentsRowCount % 2 == 0){ %>
                  <tr>
                 <% } else { %>
	              <tr class="alternateRowColor">
                 <% } %>
                <td class="rowLevel1" align="center">
                    <logic:equal name="app" property="value(type)" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                        <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                    </logic:equal>
                    <logic:equal name="app" property="value(type)" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                        <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                    </logic:equal>
                </td>
                <td class="rowLevel1" >
                <% String packageViewCompliance = request.getContextPath() + "/packageCompliance.do"; %>
                    <a href="<%=packageViewCompliance %>?channelType=<%=(String) app.getValue("type")%>&channelTitle=<%= com.marimba.tools.util.URLUTF8Encoder.encode((String) app.getValue("title")) %>&channelURL=<%= com.marimba.tools.util.URLUTF8Encoder.encode((String) app.getValue("url")) %>" target="right-frame" class="hoverLink" onmouseover="return overlib('<bean:write name="app" property="value(title)" filter="true" />', WIDTH, '200', DELAY, '150');" onmouseout="return nd();" >
                    <bean:write name="app" property="value(title)" filter="true" /></a>
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
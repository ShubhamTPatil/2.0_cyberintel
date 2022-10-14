<%@ page contentType="text/html;charset=UTF-8" %>
<%--
 Copyright 2004-2012, BMC Software Inc. All Rights Reserved.
 Confidential and Proprietary Information of BMC Software Inc.
 Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
 6,381,631, and 6,430,608. Other Patents Pending.

 $File$

 @author    Theen-Theen Tan
 @version   $Revision$, $Date$

--%>

<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="spm" topic="config" />
<%@ include file="/includes/endHeadSection.jsp" %>

<%@ include file="/includes/body.html" %>

<%@ include file="/includes/info.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>

<div align="center">
    <div align="left" class="commonPadding">
        <div class="pageHeader">
            <span class="title"><bean:message key="page.config.Title"/></span>
        </div>

        <%@include file="/includes/help.jsp" %>

        <%-- Errors Display --%>
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <%@ include file="/includes/usererrors.jsp" %>
        </table>

        <logic:present scope="request" name="changeRequestMsg">
            <div class="statusMessage" id="OK" style="width:800px">
               <h6>&nbsp;</h6>

               <p><bean:write name="changeRequestMsg"/></p>
            </div>
        </logic:present>

        <jsp:include page="/includes/linktable.jsp" flush="false" />
    </div>
</div>

<%@ include file="/includes/footer.jsp" %>
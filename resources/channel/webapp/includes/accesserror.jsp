<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)internalerror.jsp

     @author Angela Saval
     @version 1.3, 01/09/2002
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/banner.jsp" %>

<div align="left" style="margin-left:20px; margin-right:20px;">
    <div class="statusMessage" id="critical">
       <h6><webapps:pageText key="AccessPage" /></h6>

       <p><a href="<webapps:fullPath path="/config/config.jsp" />"><font class="generalText"><webapps:pageText key="ReturnToPrev" /></a></p>
    </div>
</div>

<%@ include file="/includes/footer.jsp" %>

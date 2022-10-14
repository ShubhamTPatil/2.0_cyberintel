<%@ page contentType="text/html;charset=UTF-8" %>
<%--
Copyright 1997-2003, Marimba Inc. All Rights Reserved.
Confidential and Proprietary Information of Marimba, Inc.
Protected by or for use under one or more of the following patents:
U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
and 6,430,608. Other Patents Pending.

     @author Angela Saval
     @author Theen-Theen Tan
     @version $Revision$, $Date$
--%>
<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.util.LDAPUtils" %>
<%@ page import = "com.marimba.apps.subscription.common.LDAPVars" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Channel" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap" %>
<%@ page import = "com.marimba.webapps.intf.IBeanProperty" %>

<% //String pageStateAction = "tgdetailsPageState"; %>
<% //String forwardPage="/target/target_details_area.jsp"; %>
<% //String pageBeanName = "target_pkgs_bean"; %>
<bean:define id="pageStateAction"  value="tgdetailsPageState" toScope="request" />
<bean:define id="forwardPage" value="/target/target_details_area.jsp" toScope="request" />
<bean:define id="pageBeanName"  value="target_pkgs_bean" toScope="request" />

<logic:present name="targetsched">
    <bean:define id="pageStateAction"  value="tgscheddetailsPageState" toScope="request" />
    <bean:define id="forwardPage" value="/target/target_scheddetails_area.jsp" toScope="request" />
    <% //pageStateAction ="tgscheddetailsPageState"; %>
    <% //forwardPage="/target/target_scheddetails_area.jsp"; %>
</logic:present>
<% //String tgForm = "targetDetailsForm"; %>
<% //String checkAction = "tgdetailsCheckAll"; %>
<bean:define id="tgForm" value="targetDetailsForm" toScope="request" />
<bean:define id="checkAction"  value="tgdetailsCheckAll" toScope="request" />

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="targ_vw" />
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<%@ include file="/includes/endHeadSection.jsp" %>

<style type="text/css">
<!--
.tableRowActionsMenu {
    background-color: #F5F5F5;
    color: #000000;
    padding-top: 3px;
    padding-bottom: 3px;
    padding-right: 5px;
    padding-left: 5px;
    border-top-width: 1px;
    border-top-style: solid;
    border-top-color: #666666;
    background-repeat: repeat-x;
    background-position: left center;
    font-weight: bold;
    cursor: pointer;
}
.tableRowActionsMenuHover {
    background-color: #E5E5E5;
    color: #000000;
    padding-top: 3px;
    padding-bottom: 3px;
    padding-right: 5px;
    padding-left: 5px;
    border-top-width: 1px;
    border-top-style: solid;
    border-top-color: #666666;
    background-repeat: repeat-x;
    background-position: left center;
    font-weight: bold;
    cursor: pointer;
}
.tableRowActionsMenuActive {
    background-color: #000099;
    color: #FFFFFF;
    padding-top: 3px;
    padding-bottom: 3px;
    padding-right: 5px;
    padding-left: 5px;
    border-top-width: 1px;
    border-top-style: solid;
    border-top-color: #435d8d;
    background-repeat: no-repeat;
    font-weight: bold;
    cursor: pointer;
}
.pageMenuItem {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10px;
    font-weight: bold;
    color: #000000;
    padding-top: 3px;
    padding-right: 5px;
    padding-bottom: 3px;
    padding-left: 5px;
    cursor: pointer;
    background-color: #F5F5F5;
}
.groupItemOff {
    font-family: Verdana, Arial, Helvetica, sans-serif;
    font-size: 11px;
    font-weight: normal;
    border-top-width: 1px;
    border-top-style: solid;
    border-top-color: #CCCCCC;
    padding-top: 2px;
    padding-bottom: 2px;
    padding-left: 5px;
    padding-right: 5px;
    cursor: pointer;
}
.groupItemOn {
    font-family: Verdana, Arial, Helvetica, sans-serif;
    font-size: 11px;
    font-weight: normal;
    border-top-width: 1px;
    border-top-style: solid;
    border-top-color: #CCCCCC;
    padding-top: 2px;
    padding-bottom: 2px;
    padding-left: 5px;
    padding-right: 5px;
    color: #000000;
    background-color: #CEDDF2;
    cursor: pointer;
}
.pageMenuSpacer {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10px;
    font-weight: bold;
    color: #000000;
    padding-top: 1px;
    padding-right: 5px;
    padding-bottom: 1px;
    padding-left: 5px;
    cursor: pointer;
    background-color: #F5F5F5;
}
.buttonMenu {
    cursor: hand;
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9px;
    color: #000000;
    background-color: #F0F0F5;
    padding-top: 2px;
    padding-bottom: 2px;
    padding-left: 4px;
    padding-right: 4px;
}
.buttonMenuActive {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9px;
    background-color:#FFFFFF;
    padding-top: 2px;
    padding-bottom: 2px;
    padding-left: 4px;
    padding-right: 4px;
}
.captionLink {
    text-decoration: none;
    color: #000000;
}
A.captionLink:visited {
    color: #000000;
}
.smallCaption {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9px;
    color: #000000;
    background-color: #F0F0F5;
    padding-top: 2px;
    padding-bottom: 2px;
    padding-left: 4px;
    padding-right: 4px;
}
-->
</style>


<logic:present name="targetsched">
    <body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <% if(null != EmpirumContext) {%>
    <webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
    <% } else { %>
    <webapps:tabs tabset="main" tab="tgtview"/>
    <% } %>

    <% forwardPage="/viewSchedDetails.do"; %>
</logic:present>

<logic:notPresent name="targetsched">
    <logic:present name="main_page_target">
        <%@ include file="/includes/body.html" %>
    </logic:present>
    <logic:notPresent name="main_page_target">
        <body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    </logic:notPresent>
    <%@ include file="/includes/common_js.jsp" %>
</logic:notPresent>
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<%-- sets the return page --%>
<%-- session_return is used by distCancel --%>
<%-- session_return_pagetype is used by distAsgSave and distCancel --%>
<logic:notPresent name="targetsched">
    <% session.setAttribute("session_return_page",  "/tgviewInit.do"); %>
</logic:notPresent>
<logic:present name="targetsched">
    <% session.setAttribute("session_return_page",  "/tgdetailsInit.do"); %>
</logic:present>
<%-- session_return is used by distSave --%>
<% session.setAttribute("session_return_pagetype",  "target_view"); %>
<%-- used by target_display_single.jsp to determine where to link to Report Center' machine's details page --%>
<% pageContext.setAttribute("linktorc", "true", PageContext.PAGE_SCOPE); %>

<%-- Javascript --%>
<script language="JavaScript">
    function targetConfigSelect() {
        var url = document.targetDetailsForm.targetconfig.options[document.targetDetailsForm.targetconfig.selectedIndex].value;
        var fullpath = "<html:rewrite page='" + url + "' />";
        top.location = fullpath;
    }
</script>

<sm:getDistTargets init="false" />

<%-- This is the piece of code used for refreshing the paging. --%>
<webapps:empty parameter="page">
    <sm:getPkgsFromTargets stateBean="<%=tgForm%>" />
</webapps:empty>
<%-- setPagingResults tag puts the page of results to display into display_rs --%>
<sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>"
                     resultsName="page_pkgs_fromtgs_rs" />


<% String hdrTableWidth = "650"; %>
<% String dataTableWidth = "631"; %>
<% String dataSectionHeight = "200"; %>
<logic:present name="targetsched">
    <% hdrTableWidth = "900"; %>
    <% dataTableWidth = "881"; %>
    <% dataSectionHeight = "200"; %>
</logic:present>

<logic:present name="targetsched">
    <script>
        CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','endOfGui','-1');");
    </script>
</logic:present>


<html:form name="targetDetailsForm" action="/distEdit.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsForm">
    <%-- This present check is here to show or hide the contents on the page depending on if there are targets defined--%>

    <!--center table only on Details view-->
    <logic:present name="targetsched">
        <div style="text-align:center">
        <div style="margin-bottom:15px; margin-left:15px; margin-right:15px; ">
    </logic:present>

    <logic:present name="targetsched">
        <div class="pageHeader" style="width:100%; text-align:left;">
            <span class="title"><webapps:pageText key="Title"/></span>
        </div>
    </logic:present>

    <%-- Errors Display --%>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>

    <%-- There is no package assigned to the target --%>
    <logic:notPresent name="main_page_target">
        <table width="100%" border="0" cellpadding="0" style="margin-top:20px; ">
            <tr>
                <td valign="top" class="textGeneral">
                    <strong><webapps:pageText shared="true" type="target_details_area" key="NoTargetSelectedShort" /></strong>&nbsp;
                    <webapps:pageText shared="true" type="target_details_area" key="NoTargetSelectedLong" />
                </td>
            </tr>
        </table>
    </logic:notPresent>

    <%-- There are packages assigned to the target --%>

    <logic:present name="main_page_target">

        <table width="99%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td valign="bottom" class="tableTitle">
                    <logic:iterate id="target" name="main_page_target" type="com.marimba.apps.subscription.common.objects.Target">
                        <% //String tgLabel="target"; %>
                        <bean:define id="ID" name="target" property="id" toScope="request"/>
                        <bean:define id="Name" name="target" property="name" toScope="request"/>
                        <bean:define id="Type" name="target" property="type" toScope="request"/>
                        <jsp:include page="/includes/target_display_single.jsp"/>
                    </logic:iterate>
                </td>
                <td align="right" style="padding-right:4px;" nowrap>
                        <%-- previous/next --%>
                    <logic:notPresent name="targetsched">
                        <% request.setAttribute("targetFrame", "mainFrame"); %>
                    </logic:notPresent>
                    <jsp:include page="/includes/genPrevNext.jsp" />
                </td>
            </tr>
        </table>

        <jsp:include page="/includes/target_details.jsp" />
    </logic:present>


    <logic:present name="targetsched">
        </div> <!--end centering div-->
        </div> <!--end margin div-->
    </logic:present>

</html:form>

<div id="endOfGui"></div>
<logic:present name="main_page_target">
    <script>
        resizeDataSection('FOO_dataDiv','endOfGui','-1');
        <logic:present name="targetsched">
        syncTables('FOO');
        </logic:present>
    </script>
</logic:present>

</body>

</html>

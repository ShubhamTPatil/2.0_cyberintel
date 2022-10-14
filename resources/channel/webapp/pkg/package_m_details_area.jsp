<%@ page contentType="text/html;charset=UTF-8" %>
<%--
    Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
    Confidential and Proprietary Information of Marimba, Inc.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.
    $File$, $Revision$, $Date$

    @author Rahul Ravulur
    @version $Revision$, $Date$
--%>

<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap,
                   com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>

<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<%@ include file="/includes/startHeadSection.jsp" %>
<%@ include file="/includes/MultiChannels.jsp" %>
<logic:present name="packagesched">
    <body>
    <% if(null != EmpirumContext) {%>
    <webapps:tabs tabset="ldapEmpirumView" tab="pkgview"/>
    <% } else { %>
    <webapps:tabs tabset="main" tab="pkgview"/>
    <% } %>
    <webapps:helpContext context="spm" topic="pkg_det" />
</logic:present>

<script language="JavaScript">
    var singleOptionElements = new Array("edit_assign_button", "remove_assign_button");
    var multiOptionElements = new Array("edit_assign_button", "remove_assign_button");
</script>
<%@ include file="/includes/endHeadSection.jsp" %>

<logic:notPresent name="packagesched">
    <logic:present name="main_page_m_packages">
        <%@ include file="/includes/body.html" %>
    </logic:present>
    <logic:notPresent name="main_page_m_packages">
        <body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    </logic:notPresent>
    <%@ include file="/includes/common_js.jsp" %>
</logic:notPresent>


<%-- sets the return page --%>
<%-- session_return_page is used by distCancel --%>
<%-- session_return_pagetype is used by distAsgSave and distCancel --%>
<logic:notPresent name="packagesched">
    <% session.setAttribute("session_return_page",  "/pkgviewMultiInit.do"); %>
</logic:notPresent>
<logic:present name="packagesched">
    <% session.setAttribute("session_return_page",  "/pkgdetailsMultiInit.do"); %>
</logic:present>
<%-- session_return is used by distSave --%>
<% session.setAttribute("session_return_pagetype",  "pkg_view"); %>

<%-- used by target_display_single.jsp to determine where to link to Report Center' machine's details page --%>
<% pageContext.setAttribute("linktorc", "true", PageContext.PAGE_SCOPE); %>

<% String sessionpkgs = "main_page_m_packages"; %>
<% //String pageBeanName = "pkg_tglist_bean"; %>
<bean:define id="pageBeanName"  value="pkg_tglist_bean" toScope="request" />

<%-- Start of  paging logic --%>
<webapps:empty parameter="page">
    <sm:getTargetsFromPkgs pkgs="main_page_m_package" stateBean="PackageDetailsForm" />
</webapps:empty>

<sm:setPagingResults formName="PackageDetailsForm" beanName="<%= (String)pageBeanName %>" resultsName="page_targets_frompkgs_rs" />
<%-- end of paging --%>

<bean:define id="hdrTableWidth"  value="550" toScope="request" />
<bean:define id="dataTableWidth"  value="532" toScope="request" />
<bean:define id="dataSectionHeight"  value="200" toScope="request" />
<logic:present name="packagesched">
    <bean:define id="hdrTableWidth"  value="900" toScope="request" />
    <bean:define id="dataTableWidth" value="882" toScope="request" />
    <bean:define id="dataSectionHeight" value="350" toScope="request" />
</logic:present>

<jsp:useBean id="session_page" class="com.marimba.apps.subscriptionmanager.webapp.system.PagingBean" scope="session"/>

<html:form name="PackageDetailsForm" action="/distEditFromPkg.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsForm">

    <!--only when on Details view-->
    <logic:present name="packagesched">
        <div style="text-align:center;">
        <div id="pageContent" style="margin-bottom:15px; margin-left:15px; margin-right:15px; ">
    </logic:present>

    <logic:present name="packagesched">
        <div class="pageHeader" style="width:100%;">
            <span class="title"><webapps:pageText key="DetailTitle"/></span>
        </div>
    </logic:present>

    <%-- Errors Display --%>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>

    <logic:notPresent name="main_page_m_packages">
        <div style="width:100% ">
            <table border="0" cellpadding="5">
                <tr>
                    <td valign="top"><span class="textGeneral"><b><webapps:pageText shared="true" type="package_details_area" key="NoPackageSelectedShort" /></b>&nbsp;<webapps:pageText shared="true" type="package_details_area" key="NoPackageSelectedLong" /></span></td>
                </tr>
            </table>
        </div>
    </logic:notPresent>

    <logic:present name="main_page_m_packages">

        <logic:notPresent name="packagesched">
            <div class="sectionInfo" style="width:99%; margin-bottom:10px;"><strong><webapps:pageText shared="true" type="package_details_area" key="MultipleSelectMode" /></strong>
                <webapps:pageText shared="true" type="package_details_area" key="MultipleSelectText" />
            </div>
        </logic:notPresent>

        <div style="width:99%; ">
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <% String fwdURL = "/packageDetailsMultiAddAction.do"; %>
                <% String packageschedString = "false"; %>
                <logic:present name="packagesched">
                    <% fwdURL = "/pkg/package_m_scheddetails_area.jsp"; %>
                    <% packageschedString = "true"; %>
                </logic:present>
                <% int channelCount = 0; %>
                <logic:iterate id="channel" name="main_page_m_packages" type="com.marimba.apps.subscription.common.objects.Channel">
                    <% channelCount++;%>
                </logic:iterate>
                <td align="left" class="tableTitle">
                    <a href="#" onClick="javascript:parent.showMultiChannels();"> <%= channelCount%>&nbsp;<webapps:pageText key="selectedPackages" type="colhdr" shared="true"/></a>
                </td>
                <logic:present name="display_rs">
                    <%-- previous/next --%>
                    <logic:notPresent name="packagesched">
                        <bean:define toScope="request" id="submitPaging" value="true"/>
                        <%
                            request.setAttribute("formName","document.PackageDetailsForm");
                            request.setAttribute("genericPagingAction","/persistifyChecksOnPkgMultiAction.do");
                            request.setAttribute("targetFrame", "1"); %>
                    </logic:notPresent>
                    <logic:present name="packagesched">
                        <bean:define toScope="request" id="submitPaging" value="true"/>
                        <bean:define toScope="request" id="page_jsNoFrameSubmit" value="send"/>
                        <%
                            request.setAttribute("formName","document.PackageDetailsForm");
                            request.setAttribute("genericPagingAction","/persistifyChecksOnPkgMultiAction.do");
                        %>
                    </logic:present>

                    <td align="right" nowrap>
                        <jsp:include page="/includes/genPrevNext.jsp" />
                    </td>
                </logic:present>
                <logic:notPresent name="display_rs">
                    <td align="right" nowrap>&nbsp;</td>
                </logic:notPresent>
                </tr>

            </table>
        </div>
        <%-- Apply the selected targets to the form. --%>
        <sm:setPersistedRecords selectedTargetsVarName="page_targets_frompkgs_selected" formName="PackageDetailsForm"
                                pagingBeanName="pkg_tglist_bean"/>

        <jsp:include page="/pkg/package_details_body.jsp" />

    </logic:present>

    <logic:present name="packagesched">
        </div>
        </div>
    </logic:present>

</html:form>

<div id="endOfGui"></div>
<logic:present name="main_page_m_packages">
    <script>
        resizeDataSection('FOO_dataDiv','endOfGui','-1');
        <logic:present name="packagesched">
        syncTables('FOO');
        </logic:present>
    </script>
</logic:present>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" %>
<%--
    Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

     @author Rahul Ravulur
     @version $Revision$, $Date$
--%>


<%-- This is the top level page of the right hand pane in single select mode in package view. This page
     also expands to take up the entire screen in the case when package details is clicked in single select
     mode.
     It includes the following files:
     startHeadSection, endHeadSection: header files to include headers common across pages. They in between them
     have the help id.

     If the page is in full screen details mode, the session variable packageSched is set to true in the action.
     Then the page includes the banner and header files.

     If the page is being rendered when packages exist, the "main_page_package" session variable is set indicating
     that the table is to be rendered. Else, the "no packages selected" text will be shown with the subscription.gif bkgd.


--%>
<%@ include file="/includes/directives.jsp" %>


<%@ include file="/includes/startHeadSection.jsp" %>

<script language="JavaScript">
    var singleOptionElements = new Array("edit_assign_button", "remove_assign_button");
    var multiOptionElements = new Array("edit_assign_button", "remove_assign_button");
</script>
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>


<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.actions.PersistifyChecksAction" %>
<%@ page import = "com.marimba.webapps.intf.IMapProperty" %>

<% //String pageBeanName = "pkg_tglist_bean"; %>
<bean:define id="pageBeanName"  value="pkg_tglist_bean" toScope="request" />
<%-- Start of  paging logic --%>
<webapps:empty parameter="page">
    <sm:getTargetsFromPkgs pkgs="main_page_package" stateBean="PackageDetailsForm" />
</webapps:empty>
<sm:setPagingResults formName="PackageDetailsForm" beanName="<%= (String)pageBeanName %>" resultsName="page_targets_frompkgs_rs" />
<%-- end of paging --%>
<% //String hdrTableWidth = "650"; %>
<% //String dataTableWidth = "631"; %>
<% //String dataSectionHeight = "200"; %>
<bean:define id="hdrTableWidth"  value="650" toScope="request" />
<bean:define id="dataTableWidth"  value="631" toScope="request" />
<bean:define id="dataSectionHeight"  value="200" toScope="request" />
<logic:present name="packagesched">
    <bean:define id="hdrTableWidth"  value="900" toScope="request" />
    <bean:define id="dataTableWidth" value="881" toScope="request" />
    <bean:define id="dataSectionHeight" value="200" toScope="request" />


    <% //hdrTableWidth = "900"; %>
    <% //dataTableWidth = "881"; %>
    <% //dataSectionHeight = "200"; %>
</logic:present>
<% String sessionpkgs = "main_page_package"; %>

<logic:notPresent name="packagesched">
    <logic:present name="main_page_package">
        <%@ include file="/includes/body.html" %>
    </logic:present>
    <logic:notPresent name="main_page_package">
        <body>
    </logic:notPresent>
    <%@ include file="/includes/common_js.jsp" %>
</logic:notPresent>

<%-- sets the return page --%>
<%-- session_return_page is used by distCancel --%>
<%-- session_return_pagetype is used by distAsgSave and distCancel --%>
<logic:notPresent name="packagesched">
    <% session.setAttribute("session_return_page",  "/pkgviewInit.do?actionType=initAction"); %>
</logic:notPresent>
<logic:present name="packagesched">
    <% session.setAttribute("session_return_page",  "/pkgdetailsInit.do"); %>
</logic:present>
<%-- session_return is used by distSave --%>
<% session.setAttribute("session_return_pagetype",  "pkg_view"); %>

<%-- used by target_display_single.jsp to determine where to link to Report Center' machine's details page --%>
<% pageContext.setAttribute("linktorc", "true", PageContext.PAGE_SCOPE); %>

<logic:present name="packagesched">
    <body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <% if(null != EmpirumContext) {%>
    <webapps:tabs tabset="ldapEmpirumView" tab="pkgview"/>
    <% } else { %>
    <webapps:tabs tabset="main" tab="pkgview"/>
    <% } %>
    <webapps:helpContext context="spm" topic="pkg_det" />
</logic:present>

<logic:notPresent name="packagesched">
    <webapps:helpContext context="spm" topic="pkg_vw" />
</logic:notPresent>

<%@ page import="com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap" %>
<%@ page import="com.marimba.apps.subscription.common.objects.Channel" %>

<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<logic:present name="packagesched">
    <script>
        CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','endOfGui','-1');");
    </script>
</logic:present>

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

    <logic:notPresent name="main_page_package">
        <div style="width:100% ">
            <table border="0" cellpadding="0" style="margin-top:20px; ">
                <tr>
                    <td valign="top" class="textGeneral"><strong><webapps:pageText shared="true" type="package_details_area" key="NoPackageSelectedShort" /></strong>&nbsp;<webapps:pageText shared="true" type="package_details_area" key="NoPackageSelectedLong" />
                </tr>
            </table>
        </div>
    </logic:notPresent>

    <logic:present name="main_page_package">
        <div style="width:99%; text-align:left">
            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                    <logic:iterate id="channel" name="main_page_package" type="com.marimba.apps.subscription.common.objects.Channel">
                        <td class="tableTitle" valign="bottom">
                            <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                                <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                            </logic:equal>
                            <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                                <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                            </logic:equal>


                            <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                                <a class="noUnderlineLink" style="cursor:help;" target="_blank" href="/spm/getPatches.do?patchGroupUrl=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getUrl())%>&title=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getTitle()) %>" onmouseover="return MakeTip(wrapDN('<webapps:stringescape><bean:write name="channel" property="url" filter="false" /></webapps:stringescape>', 100));" onmouseout="CloseTip();">
                            </logic:equal>
                            <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                                <a class="noUnderlineLink" style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="MakeTip(wrapDN('<webapps:stringescape><bean:write name="channel" property="url"filter="false" /></webapps:stringescape>', 100));" onmouseout="CloseTip();">
                            </logic:equal>

                            <bean:write name="channel" property="title" filter="true" />
                            </a>
                        </td>
                    </logic:iterate>

                    <logic:present name="display_rs">
                        <%-- previous/next --%>
                        <logic:notPresent name="packagesched">
                            <bean:define toScope="request" id="submitPaging" value="true"/>
                            <bean:define id="targetFrame" toScope="request" value="1" />
                            <bean:define id="formName"  toScope="request" value="document.PackageDetailsForm" />
                            <bean:define id="genericPagingAction"  toScope="request" value="/persistifyChecksOnPkgAction.do" />
                        </logic:notPresent>
                        <logic:present name="packagesched">
                            <bean:define toScope="request" id="submitPaging" value="true"/>
                            <bean:define toScope="request" id="page_jsNoFrameSubmit" value="send"/>
                            <%
                                request.setAttribute("formName","document.PackageDetailsForm");
                                request.setAttribute("genericPagingAction","/persistifyChecksOnPkgAction.do");
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
        <!-- -----END------- -->
        <%-- Apply the selected targets to the form. --%>
        <sm:setPersistedRecords selectedTargetsVarName="page_targets_frompkgs_selected" formName="PackageDetailsForm"
                                pagingBeanName="pkg_tglist_bean"/>

        <%-- include the file responsible for rendering the target table --%>
        <jsp:include page="/pkg/package_details_body.jsp" />
    </logic:present> <!-- closing tag for logic:present main_page_package -->


    <logic:present name="packagesched">
        </div> <!--end pageContent-->
        </div> <!--end centering div-->
    </logic:present>

</html:form>

<div id="endOfGui"></div>
<logic:present name="main_page_package">
    <script>
        resizeDataSection('FOO_dataDiv','endOfGui','-1');
        <logic:present name="packagesched">
                            syncTables('FOO');
        </logic:present>
    </script>
</logic:present>

</logic:present>

</body>


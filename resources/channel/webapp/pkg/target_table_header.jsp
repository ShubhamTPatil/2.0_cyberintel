<%-- Copyright 2001, Marimba Inc. All Rights Reserved.

     Confidential and Proprietary Information of Marimba, Inc.

     @(#)target_table_header.jsp


     @author Rahul Ravulur

     @author Michele Lin

     @author Theen-Theen

     @version 1.16, 12/30/2002

--%>

<% String sortAction = "packageViewSort.do"; %>

<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                   com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>

<% String prefix = IWebAppConstants.TGRESULT_PREFIX; %>

<%-- String isCheckAll = request.getParameter("setas")==null?"false":request.getParameter("setas"); --%>


<%-- The rest of the declarations is needed because we are using jsp:include to

     include this page from package_details_body.jsp.  We want to jsp:include

     for dynamic include, otherwise the page still gets too long using only @include

--%>

<%@ include file="/includes/directives.jsp" %>
<bean:define id="pageBeanName" name="pageBeanName" toScope="request" />
<bean:define id="hdrTableWidth" name="hdrTableWidth" scope="request" />
<bean:define id="dataTableWidth" name="dataTableWidth" scope="request" />



<logic:equal parameter="packagesched" value="defined">
    <bean:define id="packagesched" value="defined" scope="request" />
</logic:equal>

<logic:equal parameter="multipkgbool" value="true">
    <bean:define id="multipkgbool" value="true" scope="request" />
</logic:equal>

<%

    //String pageBeanName = request.getParameter("pageBeanName");
    String genericPagingAction = "/persistifyChecksOnPkgAction.do";
    request.setAttribute("genericPagingAction", genericPagingAction);
    request.setAttribute("submitPaging", request.getParameter("submitPaging"));
    request.setAttribute("formName", request.getParameter("formName"));
    String pkgForm = request.getParameter("formName");
    String clearAndSortPath = request.getContextPath() + "/" + sortAction + "?";
    //String hdrTableWidth = request.getParameter("hdrTableWidth");
    //String dataTableWidth = request.getParameter("dataTableWidth");

%>

<% GenericPagingBean bean = (GenericPagingBean) pageContext.findAttribute((String)pageBeanName); %>
<script>

    addSpecialColumn("checkboxColumn",0.5);

    addSpecialColumn("targetName",2);

    function setClearAllFromHeader() {
        if(document.PackageDetailsForm['value(tgresult_all)'].checked) {
            document.PackageDetailsForm['value(clear_all)'].value = 'false';
        } else {
            document.PackageDetailsForm['value(clear_all)'].value = 'true';
            selectionCount = 0;
        }
    }

    function setCheckAllFromHeader() {
    <%
        int totalCount = bean.getTotal();
        out.println("var totalCount = "+totalCount);
    %>
        if(totalCount != 0) {
        <logic:present name="packagesched">
        <logic:notPresent name="session_multipkgbool" >
            send(document.PackageDetailsForm, '/persistifyChecksOnPkgAction.do?/pkg/package_scheddetails_area.jsp?page=current');
        </logic:notPresent>
        <logic:present name="session_multipkgbool">
            send(document.PackageDetailsForm, '/persistifyChecksOnPkgAction.do?/pkg/package_m_scheddetails_area.jsp?page=current');
        </logic:present>
        </logic:present>
        <logic:notPresent name="packagesched">
        <logic:notPresent name="session_multipkgbool">
            submitActionFromFrames(document.PackageDetailsForm, '/persistifyChecksOnPkgAction.do?/pkg/package_details_area.jsp?page=current');
        </logic:notPresent>
        <logic:present name="session_multipkgbool">
            submitActionFromFrames(document.PackageDetailsForm, '/persistifyChecksOnPkgAction.do?/pkg/package_m_details_area.jsp?page=current');
        </logic:present>
        </logic:notPresent>
        }
    }

</script>


<div class="tableWrapper" style="width:99%; margin-top:0;">
<table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr valign="middle" class="smallButtons">
        <td class="tableSearchRow">
            <logic:notPresent name="packagesched">
                <logic:notPresent name="session_multipkgbool">
                    <!-- <strong><webapps:pageText shared="true" type="target_details" key="Basic_View" /></strong> | <a class="textWhite" href="javascript:send(<%="document.forms."+pkgForm%>, '<%= genericPagingAction + "?/viewPackageDetails.do" + java.net.URLEncoder.encode("?page=current") %>');"><webapps:pageText shared="true" type="target_details" key="Details_View" /></a> -->
                    <strong><webapps:pageText shared="true" type="target_details" key="Basic_View" /></strong> | <a class="textWhite" href="javascript:send(<%="document.forms."+pkgForm%>, '<%= genericPagingAction + "?/viewPackageDetails.do" + com.marimba.tools.util.URLUTF8Encoder.encode("?page=current") %>');"><webapps:pageText shared="true" type="target_details" key="Details_View" /></a>
                    <!-- Symbio modified 05/19/2005 -->

                </logic:notPresent>

                <%-- submit to different action if multi-select mode --%>

                <logic:present name="session_multipkgbool">

                    <!-- <strong><webapps:pageText shared="true" type="target_details" key="Basic_View" /></strong> | <a class="textWhite" href="javascript:send(<%="document.forms."+pkgForm%>, '<%= genericPagingAction + "?/viewMultiPackageDetails.do" + java.net.URLEncoder.encode("?page=current") %>');"><webapps:pageText shared="true" type="target_details" key="Details_View" /></a> -->
                    <strong><webapps:pageText shared="true" type="target_details" key="Basic_View" /></strong> | <a class="textWhite" href="javascript:send(<%="document.forms."+pkgForm%>, '<%= genericPagingAction + "?/viewMultiPackageDetails.do" + com.marimba.tools.util.URLUTF8Encoder.encode("?page=current") %>');"><webapps:pageText shared="true" type="target_details" key="Details_View" /></a>
                    <!-- Symbio modified 05/19/2005 -->

                </logic:present>

            </logic:notPresent>

            <%-- toggle buttons for Package Details page --%>

            <logic:present name="packagesched">

                <!-- <a class="textWhite" href="javascript:send(<%="document.forms."+pkgForm%>, '<%= genericPagingAction + "?/pkg/package_frameset.jsp" + java.net.URLEncoder.encode("?page=current") %>');"><webapps:pageText shared="true" type="target_details" key="Basic_View" /></a> | <strong><webapps:pageText shared="true" type="target_details" key="Details_View" /></strong> -->
                <a class="textWhite" href="javascript:send(<%="document.forms."+pkgForm%>, '<%= genericPagingAction + "?/pkg/package_frameset.jsp" + com.marimba.tools.util.URLUTF8Encoder.encode("?page=current") %>');"><webapps:pageText shared="true" type="target_details" key="Basic_View" /></a> | <strong><webapps:pageText shared="true" type="target_details" key="Details_View" /></strong>
                <!-- Symbio modified 05/19/2005 -->

            </logic:present>

        </td>

        <td align="right" class="tableSearchRow">&nbsp;</td>

    </tr>

</table>

<table border="0" cellspacing="0" cellpadding="0" width="100%">

    <tr valign="middle" class="smallButtons">

        <td class="tableRowActions">

            <input type="button" id="edit_assign_button" name="edit_assign_button" value="<webapps:pageText key="edit" type="button" shared="true"/>"  disabled  onClick="javascript:send(PackageDetailsForm, '/persistifyChecksOnPkgAction.do?/distEditFromPkg.do');" >
            <input type="button" id="remove_assign_button" name="remove_assign_button" value="<webapps:pageText key="remove" type="button" shared="true"/>" disabled onClick="javascript:send(PackageDetailsForm, '/persistifyChecksOnPkgAction.do?/packageDelete.do');" >

        </td>

        <td align="right" class="tableRowActions"><input type="button"  id="add_assign_button" name="add_assign_button" value="<webapps:pageText key="add" type="button" shared="true"/>" onClick="javascript:send(PackageDetailsForm,'/addTargetFromPkg.do');" ></td>

    </tr>

</table>




<%-- move up if possible --%>

<% String checkAction = "pkgdetailsCheckAll"; %>

<% String forwardSortPage = "/pkg/package_details_area.jsp"; %>

<logic:present name="session_multipkgbool">

    <% forwardSortPage = "/pkg/package_m_details_area.jsp"; %>

</logic:present>

<logic:present name="packagesched">

    <% forwardSortPage="/viewPackageDetails.do"; %>

<logic:present name="session_multipkgbool">

    <% forwardSortPage = "/viewMultiPackageDetails.do"; %>

</logic:present>

</logic:present>

<% String forwardPage = forwardSortPage + "?page=current"; %>

<%-- END move up if possible --%>


<div class="headerSection" style="width:100%; overflow:hidden; text-align:left;" id="FOO_headerDiv">

<table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable">

<%-- Column group definitions depending on basic or details view --%>

<logic:notPresent name="packagesched">
    <%-- basic view --%>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
</logic:notPresent>

<logic:present name="packagesched">
    <%-- detailed view --%>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
    <colgroup width=""></colgroup>
</logic:present>

<%-- end column group definitions depending on basic or details view --%>

<tr id="FOO_headerTable_firstRow">

<!--Checkbox header column-->

<td class="tableHeaderCell" id="checkboxColumn">
    <html:hidden property="value(clear_all)"/>
    <html:checkbox property="value(tgresult_all)" value="true" styleId="tgresult_all" onclick="setClearAllFromHeader(); checkboxToggle('tgresult'); setCheckAllFromHeader()"/>
</td>


<!--Targets header column-->

<td class="tableHeaderCell" id="targetName">

    <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
        <!-- <a href='<%= clearAndSortPath + "sortorder=false&sorttype=name&lastsort=name&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"><webapps:pageText key="targets" type="colhdr" shared="true"/></a> -->
        <a href='<%= clearAndSortPath + "sortorder=false&sorttype=name&lastsort=name&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"><webapps:pageText key="targets" type="colhdr" shared="true"/></a>
        <!-- Symbio modified 05/19/2005 -->
    </logic:notPresent>

    <logic:present name="PackageDetailsForm" property="value(sortorder)">
        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
            <!-- <a href='<%= clearAndSortPath + "sortorder=false&sorttype=name&lastsort=name&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="targets" type="colhdr" shared="true"/></a> -->
            <a href='<%= clearAndSortPath + "sortorder=false&sorttype=name&lastsort=name&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="targets" type="colhdr" shared="true"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="name">

                <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
            <!-- <a href='<%=clearAndSortPath + "sortorder=true&sorttype=name&lastsort=name&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="targets" type="colhdr" shared="true"/></a> -->
            <a href='<%=clearAndSortPath + "sortorder=true&sorttype=name&lastsort=name&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="targets" type="colhdr" shared="true"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="name">

                <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

    </logic:present>

</td>


<!--Package Basic View-->

<logic:notPresent name="packagesched">

    <!--Primary State header column-->

    <td class="tableHeaderCell">

        <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
            <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=state&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"><webapps:pageText key="priState" type="colhdr" shared="true"/></a> -->
            <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=state&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"><webapps:pageText key="priState" type="colhdr" shared="true"/></a>
            <!-- Symbio modified 05/19/2005 -->
        </logic:notPresent>

        <logic:present name="PackageDetailsForm" property="value(sortorder)">
            <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
                <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=state&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="priState" type="colhdr" shared="true"/></a> -->
                <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=state&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="priState" type="colhdr" shared="true"/></a>
                <!-- Symbio modified 05/19/2005 -->
                <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="state">

                    <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />

                </logic:equal>

            </logic:equal>

            <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
                <!-- <a href='<%=clearAndSortPath + "sortorder=true&sorttype=state&lastsort=state&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="priState" type="colhdr" shared="true"/></a> -->
                <a href='<%=clearAndSortPath + "sortorder=true&sorttype=state&lastsort=state&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="priState" type="colhdr" shared="true"/></a>
                <!-- Symbio modified 05/19/2005 -->
                <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="state">

                    <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />

                </logic:equal>

            </logic:equal>

        </logic:present>

    </td>


    <!--Secondary State header column-->

    <td class="tableHeaderCell">

        <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
            <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secState&lastsort=secState&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secState" type="colhdr" shared="true"/></a> -->
            <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secState&lastsort=secState&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secState" type="colhdr" shared="true"/></a>
            <!-- Symbio modified 05/19/2005 -->
        </logic:notPresent>

        <logic:present name="PackageDetailsForm" property="value(sortorder)">
            <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
                <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secState&lastsort=secState&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secState" type="colhdr" shared="true"/></a> -->
                <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secState&lastsort=secState&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secState" type="colhdr" shared="true"/></a>
                <!-- Symbio modified 05/19/2005 -->
                <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="secState">

                    <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />

                </logic:equal>

            </logic:equal>

            <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
                <!-- <a href='<%=clearAndSortPath + "sortorder=true&sorttype=secState&lastsort=secState&forward="  + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secState" type="colhdr" shared="true"/></a> -->
                <a href='<%=clearAndSortPath + "sortorder=true&sorttype=secState&lastsort=secState&forward="  + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secState" type="colhdr" shared="true"/></a>
                <!-- Symbio modified 05/19/2005 -->
                <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="secState">

                    <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />

                </logic:equal>

            </logic:equal>

        </logic:present>

    </td>


</logic:notPresent> <!--end Package Basic view-->


<!--Package Details view-->

<logic:present name="packagesched">


<!--Primary State/Schedule header column-->

<td class="tableHeaderCell">

    <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
        <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=initSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Primary State/Schedule</a> -->
        <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=initSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="primaryschedule" shared="true" type="target_details"/></a>
        <!-- Symbio modified 05/19/2005 -->
    </logic:notPresent>

    <logic:present name="PackageDetailsForm" property="value(sortorder)">
        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
            <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=initSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Primary State/Schedule</a> -->
            <a href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=initSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="primaryschedule" shared="true" type="target_details"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="initSched">

                <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
            <!-- <a href='<%=clearAndSortPath + "sortorder=true&sorttype=state&lastsort=initSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Primary State/Schedule</a> -->
            <a href='<%=clearAndSortPath + "sortorder=true&sorttype=state&lastsort=initSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="primaryschedule" shared="true" type="target_details"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="initSched">

                <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

    </logic:present>

</td>


<!--Secondary State/Schedule header column-->

<td class="tableHeaderCell">

    <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
        <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secSched&lastsort=secSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Secondary State/Schedule</a> -->
        <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secSched&lastsort=secSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secondaryschedule" shared="true" type="target_details"/></a>
        <!-- Symbio modified 05/19/2005 -->
    </logic:notPresent>

    <logic:present name="PackageDetailsForm" property="value(sortorder)">
        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
            <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secSched&lastsort=secSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Secondary State/Schedule</a> -->
            <a href='<%=clearAndSortPath + "sortorder=false&sorttype=secSched&lastsort=secSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secondaryschedule" shared="true" type="target_details"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="secSched">

                <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
            <!-- <a href='<%=clearAndSortPath + "sortorder=true&sorttype=secSched&lastsort=secSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Secondary State/Schedule</a> -->
            <a href='<%=clearAndSortPath + "sortorder=true&sorttype=secSched&lastsort=secSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="secondaryschedule" shared="true" type="target_details"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="secSched">

                <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

    </logic:present>

</td>


<!--Update Schedule header column-->

<td class="tableHeaderCell">

    <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
        <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=updateSched&lastsort=updateSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="updateSchedule" type="colhdr" shared="true"/></a> -->
        <a href='<%=clearAndSortPath + "sortorder=false&sorttype=updateSched&lastsort=updateSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="updateSchedule" type="colhdr" shared="true"/></a>
        <!-- Symbio modified 05/19/2005 -->
    </logic:notPresent>

    <logic:present name="PackageDetailsForm" property="value(sortorder)">
        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
            <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=updateSched&lastsort=updateSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="updateSchedule" type="colhdr" shared="true"/></a> -->
            <a href='<%=clearAndSortPath + "sortorder=false&sorttype=updateSched&lastsort=updateSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="updateSchedule" type="colhdr" shared="true"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="updateSched">

                <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
            <!-- <a href='<%=clearAndSortPath + "sortorder=true&sorttype=updateSched&lastsort=updateSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="updateSchedule" type="colhdr" shared="true"/></a> -->
            <a href='<%=clearAndSortPath + "sortorder=true&sorttype=updateSched&lastsort=updateSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="updateSchedule" type="colhdr" shared="true"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="updateSched">

                <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

    </logic:present>

</td>


<!--Verify/Repair Schedule header column-->

<td class="tableHeaderCell">

    <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
        <!--<a href='<%=clearAndSortPath + "sortorder=false&sorttype=verRepairSched&lastsort=verRepairSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Verify/Repair Schedule</a> -->
        <a href='<%=clearAndSortPath + "sortorder=false&sorttype=verRepairSched&lastsort=verRepairSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> Verify/Repair Schedule</a>
        <!-- Symbio modified 05/19/2005 -->
    </logic:notPresent>

    <logic:present name="PackageDetailsForm" property="value(sortorder)">
        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
            <!-- <a href='<%=clearAndSortPath + "sortorder=false&sorttype=verRepairSched&lastsort=verRepairSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Verify/Repair Schedule</a> -->
            <a href='<%=clearAndSortPath + "sortorder=false&sorttype=verRepairSched&lastsort=verRepairSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="verifyrepair" shared="true" type="target_details"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="verRepairSched">

                <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
            <!-- <a href='<%=clearAndSortPath + "sortorder=true&sorttype=verRepairSched&lastsort=verRepairSched&forward=" + java.net.URLEncoder.encode(forwardSortPage)%>' class="columnHeading"> Verify/Repair Schedule</a> -->
            <a href='<%=clearAndSortPath + "sortorder=true&sorttype=verRepairSched&lastsort=verRepairSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="verifyrepair" shared="true" type="target_details"/></a>
            <!-- Symbio modified 05/19/2005 -->
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="verRepairSched">

                <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />

            </logic:equal>

        </logic:equal>

    </logic:present>

</td>

<!--Postpone Schedule header column-->
<td class="tableHeaderCell">
    <logic:notPresent name="PackageDetailsForm" property="value(sortorder)">
        <a href='<%=clearAndSortPath + "sortorder=false&sorttype=postponeSched&lastsort=postponeSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> Postpone Schedule</a>
    </logic:notPresent>

    <logic:present name="PackageDetailsForm" property="value(sortorder)">
        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="true">
            <a href='<%=clearAndSortPath + "sortorder=false&sorttype=postponeSched&lastsort=postponeSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="PostponeSchedule" shared="true" type="target_details"/></a>
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="postponeSched">
                <img src="/shell/common-rsrc/images/sort_up.gif" height="6" width="7" border="0" />
            </logic:equal>
        </logic:equal>

        <logic:equal name="PackageDetailsForm" property="value(sortorder)" value="false">
            <a href='<%=clearAndSortPath + "sortorder=true&sorttype=postponeSched&lastsort=postponeSched&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode(forwardSortPage)%>' class="columnHeading"> <webapps:pageText key="PostponeSchedule" shared="true" type="target_details"/></a>
            <logic:equal name="PackageDetailsForm" property="value(lastsort)" value="postponeSched">
                <img src="/shell/common-rsrc/images/sort_down.gif" height="6" width="7" border="0" />
            </logic:equal>
        </logic:equal>
    </logic:present>
</td>

</logic:present><!--end Package Details view-->


</tr>

</table>

</div><!--end headerSection-->






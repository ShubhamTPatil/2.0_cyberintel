<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Rahul Ravulur
	@author   Theen-Theen Tan
	@version  $Revision$,  $Date$
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants,
		 com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean,
		 java.util.List,
                 com.marimba.apps.subscriptionmanager.webapp.actions.PersistifyChecksAction"%>

<%@ include file="/includes/directives.jsp" %>

<%-- this file is responsible for rendering the targets (and the associated details) returned when a user selects
     to view a package in package view. This is used in single select mode.

     The tag getTargetsFromPkgs is used to query the package. It passes in the session variable main_page_package that
     indicates that the user is in single select mode.

     The target_table_header.jsp included below abstracts out the table header and it's titles so that it can be used
     in both single and multiple select mode.

     Once the targets are returned, it iterates through the result set to display them
--%>

<%-- String pageBeanName = "pkg_tglist_bean"; --%>
<bean:define id="pageBeanName" name="pageBeanName" toScope="request" />
<bean:define id="hdrTableWidth" name="hdrTableWidth" toScope="request" />
<bean:define id="dataTableWidth" name="dataTableWidth" toScope="request" />

<% int startIndex = (pageContext.findAttribute((String)pageBeanName)!=null?((GenericPagingBean) pageContext.findAttribute((String)pageBeanName)).getStartIndex():0); %>


<%String pkgsched = (String)pageContext.findAttribute("packagesched"); %>
<%String multipkgbool = (String)pageContext.findAttribute("session_multipkgbool"); %>

<%!
    int selectioncount = 0;
%>
<jsp:include page="/pkg/target_table_header.jsp" flush="true">
    <jsp:param name="formName" value="PackageDetailsForm" />
    <jsp:param name="submitPaging" value="true" />
    <jsp:param name="packagesched" value="<%=pkgsched%>" />
    <jsp:param name="multipkgbool" value="<%=multipkgbool%>" />
</jsp:include>
<%-- iterate through the result set to display target properties --%>
<%-- Set values for chkBoxCountInRemPages & selectionCount which will be used in table.js --%>
<script language="JavaScript">
    <%
         List targetsFromPkgs = (List)session.getAttribute("page_targets_frompkgs_rs");
         PersistifyChecksAction.SelectedRecords selectedRecords =
                 (PersistifyChecksAction.SelectedRecords)session.getAttribute("page_targets_frompkgs_selected");

         int noOfTargets =0;
         if(targetsFromPkgs != null && (noOfTargets=targetsFromPkgs.size()) > 0) {
             int chkBoxCountInRemPages;
             List targetsCurrPage = (List)request.getAttribute("display_rs");
             chkBoxCountInRemPages = noOfTargets - targetsCurrPage.size();
             out.println("var chkBoxCountInRemPages=" + chkBoxCountInRemPages);

             int selectionCount = 0;
             if(selectedRecords != null && (selectionCount=selectedRecords.getSelectionCount()) > 0) {
                 out.println("var selectionCount=" + selectionCount);
                 selectioncount = selectionCount;
    %>
    buttonEnable("edit_assign_button",false);
    buttonEnable("remove_assign_button",false);
    <%
            }
        }
    %>

    function setClearAllFromBody() {
        if(selectionCount == 0) {
            document.PackageDetailsForm['value(clear_all)'].value = 'true';
        } else {
            document.PackageDetailsForm['value(clear_all)'].value = 'false';
        }
    }

    <%-- Initialize clear_all hidden property value --%>
    setClearAllFromBody();

</script>

<div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto; text-align:left;" onscroll="syncScroll('FOO');" onresize="resizeDataSection('FOO_dataDiv','endOfGui',-1);">
<table cellpadding="0" cellspacing="0" border="0" id="FOO_dataTable">
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
<logic:iterate id="target" name="display_rs" type="com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap" indexId="iteridx">
<tbody id="row1-1">

<logic:equal name="iteridx" value="0">
<tr id="FOO_dataTable_firstRow">
    </logic:equal>
        <% if(iteridx.intValue() != 0) { %>
<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
<% } %>

<!-- Checkbox column -->
<td class="rowLevel1">
    <logic:equal name="target" property="isSelectedTarget" value="true">
        <html:checkbox property='<%="value(tgresult_"+ (new Integer(startIndex + iteridx.intValue())).toString() +")"%>' value="true" styleId='<%="tgresult_"+ (new Integer(startIndex + iteridx.intValue())).toString()%>' onclick='processCheckbox(this.id); setClearAllFromBody()'/>
    </logic:equal>
    <logic:notEqual name="target" property="isSelectedTarget" value="true">
        <span class="textInactive">N/A</span>
    </logic:notEqual>
</td>

<!--Targets column-->
<td class="rowLevel1">
    <% //String tgLabel="target"; %>
    <% String mouseOverStr = "<b>Target:</b>" + target.getTarget().getId();
        String subContainer = (String) target.getValue("tgmap_subcontainer");
        if ( subContainer != null &&  subContainer.length()> 0 ) {
            mouseOverStr += "<br><b>Container:</b>" + subContainer;
        }
    %>
    <bean:define id="ID" name="target" property="id" toScope="request"/>
    <bean:define id="Name" name="target" property="name" toScope="request"/>
    <bean:define id="Type" name="target" property="type" toScope="request"/>
    <jsp:include page="/includes/target_display_single.jsp"/>
</td>


<logic:notPresent name="packagesched">

    <!-- Primary State -->
    <td class="rowLevel1">
        <logic:present name="target" property="state">
        <logic:equal name="target" property="state" value="inconsistent">
            <span class="textInactive">
                </logic:equal>
                </logic:present>
                    <logic:equal name="target" property="contentType" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                        <p><webapps:pageText key='<%= target.getState() + ".patch.uppercase" %>' type="global" /></p>
                    </logic:equal>
                    <logic:equal name="target" property="contentType" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                        <p><webapps:pageText key='<%= target.getState() + ".uppercase" %>' type="global" /></p>
                    </logic:equal>
                    <logic:present name="target" property="state">
                    <logic:equal name="target" property="state" value="inconsistent">
            </span>
            </logic:equal>
            </logic:present>
    </td>

    <!--Secondary State-->
    <td class="rowLevel1">
        <logic:present name="target" property="secState">
        <logic:equal name="target" property="secState" value="inconsistent">
            <font class="textInactive">
                </logic:equal>
                </logic:present>
                    <p><webapps:pageText key='<%= target.getSecState() + ".uppercase" %>' type="global" />&nbsp;</p>
                    <logic:present name="target" property="secState">
                    <logic:equal name="target" property="secState" value="inconsistent">
            </font>
            </logic:equal>
            </logic:present>
    </td>

</logic:notPresent> <!-- closing tag for not displaying states in detail packagesched mode -->

<logic:present name="packagesched"> <!--If this is Package Details page...-->

    <!--Primary State/Schedule-->
    <td class="rowLevel1">
        <logic:present name="target" property="state">
        <logic:equal name="target" property="state" value="inconsistent">
            <span class="textInactive">
                </logic:equal>
                </logic:present>                    
                    <logic:equal name="target" property="contentType" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                        <p><webapps:pageText key='<%= target.getState() + ".patch.uppercase" %>' type="global" /></p>
                    </logic:equal>
                    <logic:equal name="target" property="contentType" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                        <p><webapps:pageText key='<%= target.getState() + ".uppercase" %>' type="global" />&nbsp;</p>
                    </logic:equal>
                    <logic:present name="target" property="state">
                    <logic:equal name="target" property="state" value="inconsistent">
            </span>
            </logic:equal>
            </logic:present>
            <logic:present name="packagesched">
                <sm:scheduleDisplay name="target" property="initSchedule" format="short" type="initial" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
            </logic:present>
    </td>

    <!--Secondary State/Schedule-->
    <td class="rowLevel1">
        <logic:present name="target" property="secState">
        <logic:equal name="target" property="secState" value="inconsistent">
            <span class="textInactive">
                </logic:equal>
                </logic:present>
                    <p><webapps:pageText key='<%= target.getSecState() + ".uppercase" %>' type="global" /></p>
                    <logic:present name="target" property="secState">
                    <logic:equal name="target" property="secState" value="inconsistent">
            </span>
            </logic:equal>
            </logic:present>
            <logic:present name="packagesched">
                <sm:scheduleDisplay name="target" property="secSchedule" format="short" type="secondary" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
            </logic:present>
    </td>

    <!--Update Schedule-->
    <td class="rowLevel1" valign="top">
        <sm:scheduleDisplay name="target" property="updateSchedule" format="short" type="update" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </td>

    <!--Verify/Repair Schedule-->
    <td class="rowLevel1" valign="top">
        <sm:scheduleDisplay name="target" property="verRepairSchedule" format="short" type="verrepair" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </td>

    <!--Postpone Schedule-->
    <td class="rowLevel1" valign="top">
        <sm:scheduleDisplay name="target" property="postponeSchedule" format="short" type="postpone" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </td>

</logic:present> <!--end package details section-->

</tr>
</tbody>
</logic:iterate>
</table>
</div><!--end dataSection-->

</div> <!--end tableWrapper-->

<%@ include file="/pkg/target_table_footer.jsp" %>

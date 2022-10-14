<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscription.common.ISubscriptionConstants,
                 com.marimba.apps.subscriptionmanager.compliance.view.PackagePolicyDetails,
                 com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                 java.util.List,
                 com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants,
                 java.text.DateFormat,org.apache.commons.lang.StringEscapeUtils" %>
<%@ include file="/includes/directives.jsp" %>

<%
    String channelTitle = request.getParameter( "channelTitle" );
    if(channelTitle == null) {
        channelTitle = (String)session.getAttribute("channelTitle");
    }
    String channelUrl = request.getParameter( "channelURL" );
    if(channelUrl == null) {
        channelUrl = (String)session.getAttribute("channelURL");
    }
    String content_type = request.getParameter( "content_type" );
    if(content_type == null) {
        content_type = (String)session.getAttribute("content_type");
    }
%>
<% int startIndex = 0; %>

<%@ include file="/includes/startHeadSection.jsp" %>
<script type="text/javascript" src="/sm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/sm/includes/complianceJsonClient.js"></script>
<script>
    addSpecialColumn("checkboxCol",0.1);
    addSpecialColumn("targetsCol",3);

    var singleOptionElements = new Array("calculate_btn");
    var multiOptionElements = new Array("calculate_btn");
    var complianceText = '<webapps:pageText shared="true" type="package_compliance" key="Compliance" escape="js"/>';

    var inQueue = '<webapps:pageText type="global" key="inQueue" escape="js"/>';
    var inQuery = '<webapps:pageText type="global" key="inQuery" escape="js"/>';
    var error = '<webapps:pageText type="global" key="error" escape="js"/>';
    var notCalculated = '<webapps:pageText type="global" key="notCalculated" escape="js"/>';
    var calc = '<webapps:pageText type="global" key="calculating" escape="js"/>';
    var waitForCalc = '<webapps:pageText type="global" key="waitForCalculating" escape="js"/>';
    var altComp = '<webapps:pageText type="global" key="GreenCompliant" escape="js"/>';
    var altNComp = '<webapps:pageText type="global" key="RedNoncompliant" escape="js"/>';
    var altNCI = '<webapps:pageText type="global" key="BlueNotchecked" escape="js"/>';
    var view = 'package';

    var hideLink = 'false';

    function getState( state ){
        if( state == STATE_IN_QUEUE ){
            return inQueue;
        } else if( state == STATE_IN_QUERY ){
            return inQuery;
        } else if( state == STATE_ERROR ){
            return error;
        } else {
            return notCalculated;
        }
    }

    var totalPolicies = 0;
    var channelUrl = '<%= StringEscapeUtils.escapeJavaScript( WebUtil.jsEncode(channelUrl) )%>';

    function shiftDisplay( displayType ){
        shiftTo( displayType, complianceText );
    }

    function getPackageUrl( elementId ){
        return channelUrl;
    }

    function getPolicy( elementId ){
        return document.getElementById( 'trgtid_'+elementId ).value;
    }

    function setCurrentElement( elementId ) {
        calculateElm = document.getElementById( 'comp_'+elementId );
        calculateTarget = document.getElementById( 'trgtid_'+elementId ).value;
        calculatePolicy = document.getElementById( 'trgtid_'+elementId ).value;
        calculateURL = channelUrl;
        return calculateElm;
    }

    function disableLink() {
    <logic:present name="taskid"> hideLink = 'true';</logic:present>
            <logic:notPresent name="taskid">hideLink = 'false';</logic:notPresent>
        return hideLink;
    }
</script>
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/body.html" %>
<html:form name="packageComplianceForm" action="/packageCompliance.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">

<logic:notPresent name="<%=IWebAppConstants.PACKAGE_DETAILS_FORPKG%>">
    <div style="width:100% ">
        <table border="0" cellpadding="0" style="margin-top:20px; ">
            <tr>
                <td valign="top" class="textGeneral"><strong><webapps:pageText shared="true" type="package_compliance" key="NoPackageSelectedShort" /></strong>&nbsp;<webapps:pageText shared="true" type="package_compliance" key="NoPackageSelectedLong" />
            </tr>
        </table>
    </div>
</logic:notPresent>

<logic:present name="<%=IWebAppConstants.PACKAGE_DETAILS_FORPKG%>">

<div>
    <div style="width:98%; ">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td class="tableTitle"><a href="javascript:void(0);" class="noUnderlineLink" style="cursor:help; color:#435d8d;" onmouseover="return overlib('<div><webapps:pageText shared="true" type="package_compliance" key="LastPublished" escape="js" />: </div>', DELAY, '200', WIDTH, '150');" onmouseout="return nd();">
    <logic:present name="content_type" scope="session">
        <logic:equal name="content_type" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" scope="session">
            <img src="/shell/common-rsrc/images/patch_group.gif" width="16" height="16" border="0" align="absmiddle">
        </logic:equal>

        <logic:equal name="content_type" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" scope="session">
            <img src="/shell/common-rsrc/images/package.gif" width="16" height="16" border="0" align="absmiddle">
        </logic:equal>
    </logic:present><%=channelTitle%> </a></td>
    <td align="right">
        <bean:define id="pageBeanName"  value="<%=IWebAppConstants.SESSION_PACKAGE_POLICIES%>" toScope="request" />
        <sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>" resultsName="<%=IWebAppConstants.PACKAGE_DETAILS_FORPKG%>" />
        <bean:define id="targetFrame" value="2" toScope="request"/>
        <jsp:include page="/includes/genPrevNext.jsp" />
    </td>
    </tr>
    </table>
</div>
<div class="tableWrapper" style="width:100%;">
<table border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr valign="middle" class="smallButtons">
        <td colspan="2" class="tableSearchRow"><input disabled name="calculate_btn" type="button" class="smallButtons" value="<webapps:pageText shared="true" type="button" key="calculate" />" id="calculate_btn" onClick="javascript:calculateSummary(document.packageComplianceForm);"></td>
    </tr>
</table>
<div class="headerSection" style="width:100%; overflow:hidden; text-align:left;" id="FOO_headerDiv">
    <table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable">
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <tr id="FOO_headerTable_firstRow">
            <td class="tableHeaderCell" id="checkboxCol"><input type="checkbox" name="checkbox" value="checkbox" id="target_sel_all" onClick="checkboxToggle('target_sel')"></td>
            <td class="tableHeaderActive">&nbsp;</td>
            <td class="tableHeaderActive" style="border-left-width:0px; " id="targetsCol" onClick="location.href='#'"><webapps:pageText shared="true" type="colhdr" key="targets" />&nbsp;</td>
            <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="package_compliance" key="PrimaryState" /></a></td>
            <td class="tableHeaderCell"><div id="compliance_type"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_targets" key="Compliance" /></a>&nbsp;&nbsp;&nbsp;<img src="/sm/images/show_percent_sel.gif" width="16" height="16" align="absmiddle">&nbsp;<a href="javascript:shiftDisplay( 'numbers' );" target="_self"><img src="/sm/images/show_numbers.gif" width="16" height="16" border="0" align="absmiddle"></a></div></td>
        </tr>
    </table>
</div>
<!--end headerSection-->
<div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');" onresize="resizeDataSection('FOO_dataDiv','endOfGui',-1);">
    <table cellpadding="0" cellspacing="0" id="FOO_dataTable">
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <colgroup width=""></colgroup>
        <!-- Adding policies information -->
        <bean:define id="resultsSize" value="<%=""+( ( List )request.getAttribute( "display_rs" ) ).size()%>" toScope="request"/>
        <logic:iterate id="policy" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.PackagePolicyDetails" indexId="iteridx">
            <tbody id='<%="row1-"+iteridx%>'>

                <logic:equal name="iteridx" value="0">
                    <tr id="FOO_dataTable_firstRow">
                </logic:equal>
                <% if(iteridx.intValue() != 0) { %>
                <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                    <% } %>
                    <!-- Checkbox column -->
                    <td class="rowLevel1">
                        <logic:equal name="policy" property="checkedStatus" value="true">
                            <input type='checkbox' name='<%="target_sel_"+(new Integer(startIndex + iteridx.intValue())).toString()%>' value="checkbox" id='<%="target_sel_"+(new Integer(startIndex + iteridx.intValue())).toString()%>' onClick="processCheckbox(this.id)"/>
                        </logic:equal>
                        <logic:notEqual name="policy" property="checkedStatus" value="true">
                            <span class="textInactive">N/A</span>
                        </logic:notEqual>
                    </td>
                    <!-- info column -->
                    <td class="rowLevel1">
                        <a href="javascript:void(0);" style="cursor:help;" onmouseover="return overlib('<div><webapps:pageText shared="true" key="policyLastUpdated" type="package_compliance" escape="js"/><webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"><bean:write name="policy" property="policyLastUpdated"/></webapps:datetime></div><logic:present name="policy" property="compLastCalculated"><div style=margin-top:5px><webapps:pageText shared="true" type="package_compliance" key="compLastCalc" escape="js"/> <webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"><bean:write name="policy" property="compLastCalculated"/></webapps:datetime></div></logic:present>', DELAY, '200', WIDTH, '250');" onmouseout="return nd();">
<img src="/shell/common-rsrc/images/info.gif" border="0" align="absmiddle"/></a>&nbsp;</td>
<!--Targets column-->
<input type='hidden' id='<%="trgtid_target_sel_"+iteridx%>' name='<%="trgtid_target_sel_"+iteridx%>' value='<%=policy.getTargetId()%>'/>
<input type='hidden' id='<%="trgttype_target_sel_"+iteridx%>' name='<%="trgttype_target_sel_"+iteridx%>' value='<%=policy.getTargetType()%>'/>
<td class="rowLevel1">
    <% String mouseOverStr = "<b>Target:</b>" + policy.getTargetId();%>
    <bean:define id="ID" name="policy" property="targetId" toScope="request"/>
    <bean:define id="Name" name="policy" property="targetName" toScope="request"/>
    <bean:define id="Type" name="policy" property="targetType" toScope="request"/>
    <jsp:include page="/includes/target_display_single.jsp"/>
</td>
<!-- Primary State -->
<td class="rowLevel1">
    <p><webapps:pageText key='<%= policy.getPrimaryState()+ ".uppercase" %>' type="global" /></p></td>
<td class="rowLevel1">
    <div id='<%="comp_target_sel_"+iteridx%>' name='<%="comp_target_sel_"+iteridx%>'>
        <script>
            calculateElm = document.getElementById('comp_target_sel_'+'<%=iteridx%>' );
            calculateTarget = '<%= WebUtil.jsEncode(policy.getTargetId()) %>';
            calculatePolicy = calculateTarget;
            calculateURL = channelUrl;
        </script>
        <logic:equal name="policy" property="hasCachedCompliance" value="false">
            <script>
                document.write(getComplianceReport('<%="target_sel_"+iteridx%>', 0, 0, 0, <%=policy.getQueryState()%>, '<%=policy.getTargetType()%>'));
            </script>
        </logic:equal>
        <logic:equal name="policy" property="hasCachedCompliance" value="true">
            <script>
                document.write(getComplianceReport('<%="target_sel_"+iteridx%>', <%= policy.getSucceeded() %>, <%= policy.getFailed() %>, <%= policy.getNotCheckedIn() %>, <%=ComplianceConstants.STATE_DONE%>, '<%=policy.getTargetType()%>'));
            </script>
        </logic:equal>
    </div>
</td>
</td>
</tr>
</tbody>
</logic:iterate>
<logic:equal name="resultsSize" value="0">
    <tr>
        <td colspan="5">
            <span class="textInactive"><webapps:pageText key="emptyList" /></span></td>
    </tr>
</logic:equal>
<script>
    totalPolicies = '<%=( ( ( List )request.getAttribute( "display_rs" ) ).size() )%>';
</script>
</table>
</div>
<!--end dataSection-->
</div>
</div>
<div id="endOfGui"></div>
<script>
    resizeDataSection('FOO_dataDiv','endOfGui');
    // setting up timer for updating pending reports on load
    setIntrRef = setInterval("updatePendingReports()", setIntrPeriod );
</script>
</logic:present>
</html:form>
<script>
    initJSONRPC();
</script>
</body>
</html>

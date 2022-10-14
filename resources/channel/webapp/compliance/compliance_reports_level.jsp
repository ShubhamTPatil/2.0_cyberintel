<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.util.WebUtil"%>

<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<script type="text/javascript" src="/sm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/sm/includes/complianceJsonClient.js"></script>

<script>

function goReports(){
    document.packageComplianceForm.submit();
}

function showQuery( qryFor, cid ){
    var qryPath = qryLibraryPrefix;
    if( qryFor == 'succeed' ){
        qryPath = getCompliaceLevelQuery( qryLibraryPrefix +qryCompliantMachinesByCId, cid );
    } else if( qryFor == 'failed' ){
        qryPath = getCompliaceLevelQuery( qryLibraryPrefix +qryNonCompliantMachinByCId, cid );
    } else if( qryFor == 'notcheckedin' ){
        qryPath = getCompliaceLevelQuery( qryLibraryPrefix +qryNotCheckInMachineByCId, cid );
    }
    top.location.href = qryPath;
}

</script>

<%@ include file="/includes/endHeadSection.jsp" %>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">

<bean:define id="compSummary" name="reportBean" property="reportSummary" type="com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean" toScope="request"/>

    <% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="compRptView"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="compRptView"/>
<% } %>
    <div align="center" style="padding-left:25px; padding-right:25px;">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>
    <div class="pageHeader">
        <span class="title">
            <logic:present name="compLevel">
                <logic:equal name="compLevel" value="success">
                    <span class="textGreen">
                        <webapps:pageText shared="true" type="compliance_reports_success" key="TableTitle"/>(<%=compSummary.getCompliantPer()%>%)
                    </span>
                </logic:equal>
                <logic:equal name="compLevel" value="failed">
                    <span class="textRed">
                        <webapps:pageText shared="true" type="compliance_reports_failed" key="TableTitle"/>(<%=compSummary.getNoncompliantPer()%>%)
                    </span>
                </logic:equal>
                <logic:equal name="compLevel" value="nci">
                    <span class="textBlue">
                        <webapps:pageText shared="true" type="compliance_reports_nci" key="TableTitle"/>(<%=compSummary.getNotcheckedinPer()%>%)
                    </span>
                </logic:equal>
            </logic:present>
        </span>
    </div>
    <div class="itemStatus">
	    <table cellspacing="0" cellpadding="2" border="0">
            <tr>
                <td valign="top">
                    <webapps:pageText shared="true" type="compliance_reports_success" key="Target"/>:</td>
                <td>
                    <span class="pageHeader">
                        <bean:define id="reportBean" name="reportBean" scope="session"/>
                        <bean:define id="ID" name="reportBean" property="target" toScope="request"/>
                        <bean:define id="Name" name="reportBean" property="name" toScope="request"/>
                        <bean:define id="Type" name="reportBean" property="type" toScope="request"/>
                        <jsp:include page="/includes/target_display_single.jsp"/>
                    </span>
                </td>
            </tr>
        </table>
    </div>
    <!-- for paging results -->
    <html:form name="packageComplianceForm" action="/compRptView.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">
        <div style="width:97%;">
            <bean:define id="pageBeanName" value="<%=IWebAppConstants.SESSION_POLICIES_DETAILS%>" toScope="request"/>
            <sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>" resultsName="<%=IWebAppConstants.COMPRPT_CACHERPTS%>"/>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td align="right" nowrap>
                        <% request.setAttribute("formName","document.packageComplianceForm"); %>
                        <jsp:include page="/includes/genPrevNext.jsp" />
                    </td>
                </tr>
            </table>
        </div>
        <logic:present name="compLevel">
            <logic:equal name="compLevel" value="success">
                <%@ include file="/compliance/compliance_reports_success.jsp" %>
            </logic:equal>
            <logic:equal name="compLevel" value="failed">
                <%@ include file="/compliance/compliance_reports_failed.jsp" %>
            </logic:equal>
            <logic:equal name="compLevel" value="nci">
                <%@ include file="/compliance/compliance_reports_nci.jsp" %>
            </logic:equal>
        </logic:present>
    </html:form>
    </div><!-- End of centering div -->
</body>
</html>

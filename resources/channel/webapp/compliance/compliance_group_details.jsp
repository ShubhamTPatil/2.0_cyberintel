<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.util.WebUtil"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<%@ include file="/includes/startHeadSection.jsp" %>
	<webapps:helpContext context="spm" topic="pc_target_view" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/body.html" %>
<script type="text/javascript" src="/spm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<script type="text/javascript" src="/spm/includes/complianceJsonClient.js"></script>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>

<script>

var singleOptionElements = new Array("calculate_btn");
var multiOptionElements = new Array("calculate_btn");
var groupId = '<%= WebUtil.jsEncode((String) session.getAttribute("comp_target_id")) %>';
var complianceText = '<webapps:pageText shared="true" type="compliance_targets" key="Compliance" escape="js"/>';

var inQueue = '<webapps:pageText type="global" key="inQueue" escape="js"/>';
var inQuery = '<webapps:pageText type="global" key="inQuery" escape="js"/>';
var error = '<webapps:pageText type="global" key="error" escape="js"/>';
var notCalculated = '<webapps:pageText type="global" key="notCalculated" escape="js"/>';
var calc = '<webapps:pageText type="global" key="calculating" escape="js"/>';
var waitForCalc = '<webapps:pageText type="global" key="waitForCalculating" escape="js"/>';
var altComp = '<webapps:pageText type="global" key="GreenCompliant" escape="js"/>';
var altNComp = '<webapps:pageText type="global" key="RedNoncompliant" escape="js"/>';
var altNCI = '<webapps:pageText type="global" key="BlueNotchecked" escape="js"/>';
var altNApp = '<webapps:pageText type="global" key="OrangeNotApplicable" escape="js"/>';
var view = 'target';

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
function MakeTip(txtToDisplay) {
	return Tip(txtToDisplay, WIDTH, '-250', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', OFFSETY, 20, BORDERCOLOR, '#333300', FADEIN, 100);
}

function CloseTip() {
    UnTip();
}
function shiftDisplay( displayType ){
    shiftTo( displayType, complianceText );
}

function getPackageUrl( elementId ){
    return document.getElementById( 'pkgurl_'+elementId ).value;
}

function getPolicy( elementId ){
    return groupId;
}

function setCurrentElement( elementId ) {
    calculateElm = document.getElementById( 'comp_'+elementId );
    calculateTarget = groupId;
    calculatePolicy = document.getElementById( 'trgtid_'+elementId ).value;;
    calculateURL = document.getElementById( 'pkgurl_'+elementId ).value;
    return calculateElm;
}

function disableLink() {
    <logic:present name="taskid"> hideLink = 'true';</logic:present>
    <logic:notPresent name="taskid"> hideLink = 'false';</logic:notPresent>
//    return hideLink;
    return 'true';
}
</script>
<html:form name="packageComplianceForm" action="/compTgtView.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.SecurityTargetDetailsForm">
<logic:present name="target">
    <bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target" toScope="request"/>
    <div style="width:100%;">
        <bean:define id="pageBeanName"  value="<%=IWebAppConstants.SESSION_POLICIES_DETAILS%>" toScope="request" />
        <sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>"
					 resultsName="<%=IWebAppConstants.POLICIES_DETAILS_FORTGT%>" />
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td valign="bottom" class="tableTitle">
                    <bean:define id="ID" name="target" property="id" toScope="request"/>
                    <bean:define id="Name" name="target" property="name" toScope="request"/>
                    <bean:define id="Type" name="target" property="type" toScope="request"/>
                    <jsp:include page="/includes/target_display_single.jsp"/>
                </td>
                <td align="right" nowrap>
                    <bean:define id="targetFrame" value="2" toScope="request"/>
                    <jsp:include page="/includes/genPrevNext.jsp" />
    	        </td>
            </tr>
        </table>
    </div>

    <logic:notEqual name="target" property="type" value="<%=ISubscriptionConstants.TYPE_MACHINE%>">
        <logic:notEqual name="target" property="type" value="<%=ISubscriptionConstants.TYPE_USER%>">
            <logic:equal name="view" value="target">
                <webapps:formtabs tabset="compTgtView" tab="confgAssmentGroupCompliance" subtab="compSecurityProfile" />
                <%@ include file="/compliance/compliance_security_targets.jsp" %>
            </logic:equal>
            <logic:equal name="view" value="overall">
                <webapps:formtabs tabset="compTgtView" tab="confgAssmentGroupCompliance" subtab="comMachine" />
                <%@ include file="/compliance/compliance_security_overall.jsp" %>
            </logic:equal>
        </logic:notEqual>
    </logic:notEqual>

    <logic:equal name="target" property="type" value="<%=ISubscriptionConstants.TYPE_MACHINE%>">
        <logic:equal name="view" value="target">
            <webapps:formtabs tabset="compTargetView" tab="confgAssmentMachineCompliance" subtab="compTarget" />
            <%@ include file="/compliance/security_machine_compliance.jsp" %>
        </logic:equal>
    </logic:equal>

    <logic:equal name="target" property="type" value="<%=ISubscriptionConstants.TYPE_USER%>">
        <%@ include file="/compliance/security_machine_compliance.jsp" %>
    </logic:equal>
</logic:present>

<logic:notPresent name="target">
	<div style="width:100% ">
	<table border="0" cellpadding="0" style="margin-top:20px; ">
		<tr>
			<td valign="top" class="textGeneral"><strong><webapps:pageText shared="true" type="compliance_target_details" key="NoTargetSelectedShort" /></strong>&nbsp;<webapps:pageText shared="true" type="compliance_target_details" key="NoTargetSelectedLong" />
		</tr>
	</table>
	</div>
</logic:notPresent>
</html:form>
</body>
</html>


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
	<webapps:helpContext context="sm" topic="pc_target_view" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/body.html" %>
<html:form name="packageComplianceForm" action="/compTgtView.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">
<logic:present name="target">
    <bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target" toScope="request"/>
    <div style="width:97%;">
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
                    <bean:define id="targetFrame" value="ldapnav" toScope="request"/>
                    <jsp:include page="/includes/genPrevNext.jsp" />
    	        </td>
            </tr>
        </table>
    </div>

    <logic:notEqual name="target" property="type" value="<%=ISubscriptionConstants.TYPE_MACHINE%>">
        <logic:notEqual name="target" property="type" value="<%=ISubscriptionConstants.TYPE_USER%>">
            <logic:equal name="view" value="target">
                <webapps:formtabs tabset="compTgtView" tab="compTgt" />
                <%@ include file="/compliance/compliance_targets.jsp" %>
            </logic:equal>
            <logic:equal name="view" value="overall">
                <webapps:formtabs tabset="compTgtView" tab="compOvrl" />
                <%@ include file="/compliance/compliance_overall.jsp" %>
            </logic:equal>
            <logic:equal name="view" value="powerprops">
                <webapps:formtabs tabset="compTgtView" tab="compProp" />
                <%@ include file="/compliance/compliance_power_group.jsp" %>
            </logic:equal>
            <logic:equal name="view" value="osdeployment">
                <webapps:formtabs tabset="compTgtView" tab="compOSM" />
                <%@ include file="/compliance/compliance_osdeployments.jsp" %>
            </logic:equal>
        </logic:notEqual>
    </logic:notEqual>

    <logic:equal name="target" property="type" value="<%=ISubscriptionConstants.TYPE_MACHINE%>">
        <logic:equal name="view" value="macPowerProps">
            <webapps:formtabs tabset="compTargetView" tab="compProp" />
            <%@ include file="/compliance/compliance_machine_pss.jsp" %>
        </logic:equal>
        <logic:equal name="view" value="target">
            <webapps:formtabs tabset="compTargetView" tab="compTarget" />
            <%@ include file="/compliance/compliance_machine.jsp" %>
        </logic:equal>
    </logic:equal>

    <logic:equal name="target" property="type" value="<%=ISubscriptionConstants.TYPE_USER%>">
        <%@ include file="/compliance/compliance_machine.jsp" %>
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


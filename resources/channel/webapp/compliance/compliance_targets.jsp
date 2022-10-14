<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants,
                   com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                   com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                   java.util.List,
                   com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants,
                   java.text.DateFormat,
                   org.apache.commons.lang.StringEscapeUtils" %>
<script type="text/javascript" src="/sm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/sm/includes/complianceJsonClient.js"></script>

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
    return hideLink;
}
</script>
<bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target"/>
<% int startIndex = 0; %>
<div id="FOO_mainContent" class="formContent" style="overflow:auto">

    <div class="sectionInfo"><webapps:pageText shared="true" type="compliance_targets" key="Info" /></div>
    <div style="margin-top:15px; margin-bottom:5px;"><webapps:pageText shared="true" type="compliance_targets" key="PolicyLastUpdated" />:
        <logic:present name="<%=IWebAppConstants.SESSION_POLICY_LASTUPDATED%>">
            <bean:write name="<%=IWebAppConstants.SESSION_POLICY_LASTUPDATED%>"/>
        </logic:present>
        <logic:notPresent name="<%=IWebAppConstants.SESSION_POLICY_LASTUPDATED%>">
            <webapps:pageText type="global" key=".uppercase"/>
        </logic:notPresent>
    </div>

    <div class="tableWrapper" style="width:99%; overflow:hidden;">
	    <table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr valign="middle" class="smallButtons">
				<td class="tableSearchRow"><input type="button" name="calculate_btn" id="calculate_btn" disabled value="<webapps:pageText key="calculate" type="button" shared="true"/>" onClick="javascript:calculateSummary(document.packageComplianceForm);"></td>
                <logic:notPresent name="taskid">
                <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
				<td align="right" class="tableSearchRow">
					<table border="0" cellspacing="0" cellpadding="0">
						<tr>
						    <td align="center" class="smallCaption" class="smallCaption" style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)" onClick="javascript:redirect('<%= "/initTargetView.do?src=tgtview&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode("/targetViewDispatcher.do?name=" + target.getName() + "&targetType=" + target.getType() + "&id=" + target.getId()) %>');"><img src="/sm/images/policy.gif" alt="View Policy" width="16" height="16" border="0"><br>
  							<webapps:pageText shared="true" type="compliance_machine" key="ViewPolicy" /></td>
						</tr>
					</table>
				</td>
                </logic:equal>
                </logic:notPresent>
			</tr>
    </table>
		<div class="headerSection" id="FOO_headerDiv">
			<table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
                <tbhead>
				<tr id="FOO_headerTable_firstRow">
					<td class="tableHeaderCell"><input name="checkbox" id="target_sel_all" type="checkbox" onClick="checkboxToggle('target_sel')" value="checkbox"></td>
					<td class="tableHeaderActive">&nbsp;</td>
					<td class="tableHeaderActive" style="border-left-width:0px; " ><webapps:pageText shared="true" type="compliance_targets" key="Package" /> </td>
					<td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_targets" key="State" /></a></td>
					<td class="tableHeaderCell"><div id="compliance_type"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_targets" key="Compliance" /></a>&nbsp;&nbsp;&nbsp;<img src="/sm/images/show_percent_sel.gif" width="16" height="16" align="absmiddle">&nbsp;<a href="javascript:shiftDisplay( 'numbers' );" target="_self"><img src="/sm/images/show_numbers.gif" width="16" height="16" border="0" align="absmiddle"></a></div></td>
					<td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_targets" key="DirectlyAssignedTo" /></a></td>
				</tr>
                </tbhead>
			</table>
		</div>
		<div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');">
      <table cellpadding="0" cellspacing="0" id="FOO_dataTable">
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
				<colgroup width=""></colgroup>
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

                <td class="rowLevel1">
                    <a href="javascript:void(0);" style="cursor:help;" onmouseover="return overlib('<div><webapps:pageText shared="true" type="compliance_targets" key="packageLastPublished" escape="js"/> <logic:notPresent name="policy" property="packageLastPublished"><webapps:pageText type="global" key="NullDate" escape="js"/></logic:notPresent><logic:present name="policy" property="packageLastPublished"><webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"><bean:write name="policy" property="packageLastPublished" /></webapps:datetime></logic:present></div><logic:present name="policy" property="compLastCalculated"><div style=margin-top:5px><webapps:pageText shared="true" type="compliance_targets" key="ComplianceLastCalculated" escape="js"/><webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"> <bean:write name="policy" property="compLastCalculated"/></webapps:datetime></div></logic:present>', DELAY, '200', WIDTH, '250');" onmouseout="return nd();">
                        <img src="/shell/common-rsrc/images/info.gif" border="0"/></a>&nbsp;</td>
                <!--Package type title and url -->
                <input type='hidden' id='<%="pkgurl_target_sel_"+iteridx%>' name='<%="pkgurl_target_sel_"+iteridx%>' value='<bean:write name="policy" property="packageUrl"/>' />
                <% String mouseOverStr = "<b>Url:</b>" + policy.getPackageUrl();%>
                <td class="rowLevel1">
                    <a href="javascript:void(0);" class="noUnderlineLink" onmouseover="return overlib('<%=org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(mouseOverStr)%>', DELAY, '200', WIDTH, '150');" onmouseout="return nd();">
                        <logic:equal name="policy" property="packageType" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                        </logic:equal>
                        <logic:equal name="policy" property="packageType" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                        </logic:equal>
                        <bean:write name="policy" property="packageTitle"/></a></td>

                <!-- Primary State -->
			    <td class="rowLevel1">
                    <p><webapps:pageText key='<%= policy.getPrimaryState()+ ".uppercase" %>' type="global" /></p>
                </td>
                <td class="rowLevel1">
                    <div id='<%="comp_target_sel_"+iteridx%>' name='<%="comp_target_sel_"+iteridx%>'>
                        <script>
                            calculateElm = document.getElementById('comp_target_sel_'+'<%=iteridx%>' );
                            calculateTarget = groupId;
                            calculatePolicy = '<%= WebUtil.jsEncode(policy.getTargetId()) %>';
                            calculateURL = '<%= StringEscapeUtils.escapeJavaScript( WebUtil.jsEncode(policy.getPackageUrl()) ) %>';
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
                <!-- target to which package is directly assigned to-->
                <td valign="top" class="rowLevel1">
                    <bean:define id="ID" name="policy" property="targetId" toScope="request"/>
                    <bean:define id="Name" name="policy" property="targetName" toScope="request"/>
                    <bean:define id="Type" name="policy" property="targetType" toScope="request"/>
                    <input type='hidden' id='<%="trgttype_target_sel_"+iteridx%>' name='<%="trgttype_target_sel_"+iteridx%>' value='<%=policy.getTargetType()%>'/>
                    <input type='hidden' id='<%="trgtid_target_sel_"+iteridx%>' name='<%="trgtid_target_sel_"+iteridx%>' value='<%=policy.getTargetId()%>'/>
                    <jsp:include page="/includes/target_display_single.jsp"/>
                </td>
            </tr>
		</tbody>
	</logic:iterate>
    <%-- Empty list message --%>
    <logic:equal name="resultsSize" value="0">
        <tr>
            <td colspan="5">
          	    <span class="textInactive"><webapps:pageText shared="true" type="target_details" key="emptyList" /></span></td>
        </tr>
    </logic:equal>

		  </table>
    </div>
	</div>

</div>

<div class="formBottom" style="width:100%;">
	<table width="100%" cellpadding="0" cellspacing="0">
		<tr>
			<td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
			<td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
			<td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
		</tr>
	</table>
</div>
<div id="endOfGui"></div>
<script>
function setActiveBg(T,s) {
	T.style.backgroundColor = (s) ? '#FDFDFD' : '';
}
</script>
<script>
resizeDataSection('FOO_dataDiv','endOfGui');
initJSONRPC();
// setting up timer for updating pending reports on load
setIntrRef = setInterval("updatePendingReports()", setIntrPeriod );
</script>

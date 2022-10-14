<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.result.PowerSummaryResult,
                 com.marimba.apps.subscriptionmanager.compliance.view.PowerSummaryBean,
                 java.util.*,
                 java.text.*,
                 com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain,
                 com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants,
                 com.marimba.apps.subscriptionmanager.compliance.core.ConfigManager"%>
<bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target"/>

<script type="text/javascript" src="/sm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/sm/includes/complianceJsonClient.js"></script>
<script>
hasPolicies = true;
calculateTarget = '<%= WebUtil.jsEncode(target.getID()) %>';
notCheckedInTimeLimit = <%=( String )request.getAttribute( ConfigManager.CFG_CHECKIN_LIMIT )%>
var altComp = '<webapps:pageText type="global" key="GreenCompliant" escape="js"/>';
var altNComp = '<webapps:pageText type="global" key="RedNoncompliant" escape="js"/>';
var altNCI = '<webapps:pageText type="global" key="BlueNotchecked" escape="js"/>';
var targetType = '<%= WebUtil.jsEncode(target.getType()) %>';

function showQuery( qryFor ){
    var qryPath = qryLibraryPrefix;
    if("site" == targetType) {
	    if( qryFor == 'powerSucceed' ){
	        qryPath = getPowerComplianceQuery(qryLibraryPrefix +qryPowerCompliantSiteMachine);
	    } else if( qryFor == 'powerFailed' ){
	        qryPath = getPowerComplianceQuery(qryLibraryPrefix +qryPowerNonCompliantSiteMachine);
	    } else if( qryFor == 'powerNotcheckedin' ){
	        qryPath = getPowerComplianceQuery(qryLibraryPrefix +qryPowerNotCheckInSiteMachine);
	    }
    } else {
	    if( qryFor == 'powerSucceed' ){
	        qryPath = getPowerComplianceQuery(qryLibraryPrefix +qryPowerCompliantMachine);
	    } else if( qryFor == 'powerFailed' ){
	        qryPath = getPowerComplianceQuery(qryLibraryPrefix +qryPowerNonCompliantMachine);
	    } else if( qryFor == 'powerNotcheckedin' ){
	        qryPath = getPowerComplianceQuery(qryLibraryPrefix +qryPowerNotCheckInMachine);
	    }
    }
    
    top.location.href = qryPath;
}
</script>
<div id="FOO_mainContent" class="formContent" style="overflow:auto; padding:5px 0px 0px 0px;">

<div class="sectionInfo" style="margin:0px 15px 10px 15px; "><webapps:pageText shared="true" type="compliance_power_group" key="Info" /></div>

<!-- power compliance table -->
<table cellspacing="0" cellpadding="0" style="width:100% ">
<colgroup span="11" width="0*"></colgroup>
<colgroup width="100%"></colgroup>

<logic:present name="<%=IWebAppConstants.SESSION_COMP_HASPOLICIES%>">
    <logic:equal name="<%=IWebAppConstants.SESSION_COMP_HASPOLICIES%>" value="false">
        <script>
            hasPolicies = false;
        </script>
    </logic:equal>
</logic:present>

<logic:notPresent name="results">
    <bean:define id="results" value="powerFailed" toScope="request"/>
</logic:notPresent>
<logic:present name="results">
    <bean:define id="results" name="results" toScope="request"/>
</logic:present>

<logic:present name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>">
    <bean:define id="compLastCalc" name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>" toScope="request"/>
</logic:present>

<tr>
    <!-- power compliance bar -->
    <td nowrap style="padding-left:15px; border-bottom:1px solid #CCCCCC; "><webapps:pageText shared="true" type="compliance_power_group" key="PowerPropsCompliance" />:
        <script>
            document.write( getComplianceStatus( <bean:write name="power_sum_result" property="powerCompliant" />,
                                                 <bean:write name="power_sum_result" property="powerNoncompliant" />,
                                                 <bean:write name="power_sum_result" property="powerNotcheckedin" />,
                                                 <bean:write name="power_sum_result" property="powerCompliantPer" />,
                                                 <bean:write name="power_sum_result" property="powerNoncompliantPer" />,
                                                 <bean:write name="power_sum_result" property="powerNotcheckedinPer" />
                                                   )
                            );
        </script>&nbsp;&nbsp;</td>
    <!-- compliance status tabs -->
    <logic:equal name="results" value="powerSucceed" scope="request" >
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px;border-top:1px solid #CCCCCC;"><span class="textGreen" style="cursor:help" onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerCompliant" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerCompliant" value="1.0"><bean:write name="power_sum_result" property="powerCompliant"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="power_sum_result" property="powerCompliantPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compProperties.do?view=powerprops&results=powerFailed" target="_self" class="textRed" onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerNoncompliant" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerNoncompliant" value="1.0"><bean:write name="power_sum_result" property="powerNoncompliant" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="power_sum_result" property="powerNoncompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compProperties.do?view=powerprops&results=powerNotcheckedin" target="_self" class="textBlue" onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerNotcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerNotcheckedin" value="1.0"><bean:write name="power_sum_result" property="powerNotcheckedin" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="power_sum_result" property="powerNotcheckedinPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
    </logic:equal>
    <logic:equal name="results" value="powerFailed" scope="request">
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compProperties.do?view=powerprops&results=powerSucceed" class="textGreen" onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerCompliant" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerCompliant" value="1.0"><bean:write name="power_sum_result" property="powerCompliant" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="power_sum_result" property="powerCompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px; border-top:1px solid #CCCCCC; "><span class="textRed" style="cursor:help " onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerNoncompliant" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerNoncompliant" value="1.0"><bean:write name="power_sum_result" property="powerNoncompliant" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="power_sum_result" property="powerNoncompliantPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compProperties.do?view=powerprops&results=powerNotcheckedin" target="_self" class="textBlue" onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerNotcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerNotcheckedin" value="1.0"><bean:write name="power_sum_result" property="powerNotcheckedin" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="power_sum_result" property="powerNotcheckedinPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
    </logic:equal>
    <logic:equal name="results" value="powerNotcheckedin" scope="request">
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compProperties.do?view=powerprops&results=powerSucceed" target="_self" class="textGreen" onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerCompliant" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerCompliant" value="1.0"><bean:write name="power_sum_result" property="powerCompliant" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="power_sum_result" property="powerCompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compProperties.do?view=powerprops&results=powerFailed" target="_self" class="textRed" onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerNoncompliant" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerNoncompliant" value="1.0"><bean:write name="power_sum_result" property="powerNoncompliant" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="power_sum_result" property="powerNoncompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px; border-top:1px solid #CCCCCC;"><span class="textBlue" style="cursor:help " onmouseover="return overlib('<webapps:stringescape><logic:equal name="power_sum_result" property="powerNotcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="power_sum_result" property="powerNotcheckedin" value="1.0"><bean:write name="power_sum_result" property="powerNotcheckedin" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="power_sum_result" property="powerNotcheckedinPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
    </logic:equal>
    <td style="padding-left:8px; border-bottom:1px solid #CCCCCC;"><a href="#" class="textGreen" style="cursor:help; " onmouseover="return overlib('<webapps:pageText shared="true" type="compliance_power_group" key="CompLastCalc" escape="js"/>:<logic:notPresent name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>"><webapps:pageText type="global" key="NullDate" escape="js"/></logic:notPresent><logic:present name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>"><webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"><bean:write name="compLastCalc" scope="request"/></webapps:datetime></logic:present>' , WIDTH, '250', DELAY, '100', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/info.gif" border="0"></a></td>&nbsp;
    <td align="right" style="border-bottom:1px solid #CCCCCC; padding-bottom:2px; padding-right:13px;">&nbsp;&nbsp;</td>
    </tr>
</table>
<logic:equal name="results" value="powerSucceed" scope="request" >
        <!-- display results -->
        <div style="margin:15px 15px 0px 15px;color:#339933;" class="tableTitle"><webapps:pageText shared="true" type="compliance_power_group" key="SuccessfulMachines" /> (<bean:write name="power_sum_result" property="powerCompliantPer" />%)</div>
            <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
                <div class="tableWrapper" style="width:99%; overflow:hidden; border-top:1px solid #666666;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
             		    <tr valign="middle" class="smallButtons">
             			    <td class="tableSearchRow">
                                <logic:notPresent name="taskid">
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" onclick='javascript:showQuery( "powerSucceed" )'>
                                    </logic:equal>
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="true">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" disabled onclick='javascript:showQuery( "powerSucceed" )'>
                                    </logic:equal>
                                </logic:notPresent>
                                <logic:present name="taskid">
                                &nbsp;
                                </logic:present>
                            </td>
                        </tr>
                    </table>
                    <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');">
                        <colgroup width="33%"></colgroup>
                        <colgroup width="33%"></colgroup>
                        <colgroup width="33%"></colgroup>
                        <table cellpadding="3" width="99%" cellspacing="0" id="FOO_dataTable">
                        <tr>
                            <logic:iterate id="machineBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                            <td class="rowLevel1"><a href="javascript:void(0);" onmouseover="return overlib('http://packageURL', DELAY, '200', WIDTH, '150');" onmouseout="return nd();"><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a> <a href="javascript:void(0);" onmouseout="return nd();"><bean:write name="machineBean" property="machineName"/></a></td>
                            <logic:greaterEqual name="iteridx" value="2">
                                <bean:define id="modulus" value="<%=""+( ( iteridx.intValue()+1 )%3 )%>"/>
                                <logic:equal name="modulus" value="0">
                                    </tr>
                                    <tr>
                                </logic:equal>
                            </logic:greaterEqual>
                            </logic:iterate>
                        </tr>
                        </table>
                    </div>
                </div>
            </div> <!--padding tableWrapper-->
</logic:equal>
<logic:equal name="results" value="powerFailed" scope="request" >

        <!-- display results -->
        <div style="margin:15px 15px 0px 15px;color:#FF0000;" class="tableTitle"><webapps:pageText shared="true" type="compliance_power_group" key="FailedMachines" /> (<bean:write name="power_sum_result" property="powerNoncompliantPer" />%)</div>
            <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
	            <div class="tableWrapper" style="width:99%; overflow:hidden;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
             		    <tr valign="middle" class="smallButtons">
             			    <td class="tableSearchRow">
                                <logic:notPresent name="taskid">
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" onclick='javascript:showQuery( "powerFailed" )'>
                                    </logic:equal>
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="true">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" disabled onclick='javascript:showQuery( "powerFailed" )'>
                                    </logic:equal>
                                </logic:notPresent>
                                <logic:present name="taskid">
                                &nbsp;
                                </logic:present>
                            </td>
                        </tr>
                    </table>
                    <div class="headerSection" id="FOO_headerDiv">
			            <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
				            <colgroup width=""></colgroup>
				            <colgroup width=""></colgroup>
				            <colgroup width=""></colgroup>
				            <colgroup width=""></colgroup>
				            <tr id="FOO_headerTable_firstRow">
					            <td class="tableHeaderCell"  align="left"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_power_group" key="Machine"/></a></td>
                                <td class="tableHeaderCell" align="left"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_power_group" key="CauseOfFailure"/></a></td>
                                <td class="tableHeaderCell" align="left"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_power_group" key="PolicyValue"/></a></td>
                                <td class="tableHeaderCell" align="left"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_power_group" key="EndPointValue"/></a></td>
					        </tr>
			            </table>
		            </div>
                    <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');" >
                        <table cellpadding="3" cellspacing="0" id="FOO_dataTable">
                            <colgroup width=""></colgroup>
                            <colgroup width=""></colgroup>
                            <colgroup width=""></colgroup>
                            <colgroup width=""></colgroup>
                            <logic:iterate id="machineBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                                <logic:equal name="iteridx" value="0">
                                    <tr id="FOO_dataTable_firstRow">
                                </logic:equal>
	         			      <% if(iteridx.intValue() != 0) { %>
				        		<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
		         		      <% } %>
                                    <td class="rowLevel1">
                                        <a href="javascript:void(0);" onmouseover="return overlib('<webapps:stringescape><bean:write name="machineBean" property="machineID" filter="false" /></webapps:stringescape>', DELAY, '200', WIDTH, '150');" onmouseout="return nd();"><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a>
                                        <a href="javascript:void(0);" onmouseover="return overlib('<webapps:stringescape><bean:write name="machineBean" property="machineID" filter="false" /></webapps:stringescape>', DELAY, '200', WIDTH, '150');" onmouseout="return nd();"><bean:write name="machineBean" property="machineName"/></a></td>
                                    <td class="rowLevel1"><webapps:pageText shared="true" type="global" key="<%=machineBean.getFailureCause()%>"/>&nbsp;</td>
                                    <td class="rowLevel1" align="right">
                                        <logic:equal name="machineBean" property="policyState" value="">
                                            <webapps:pageText type="global" key="null.uppercase"/>
                                        </logic:equal>
                                        <logic:notEqual name="machineBean" property="policyState" value="">
                                            <bean:write name="machineBean" property="policyState"/>
                                        </logic:notEqual>&nbsp;</td>
                                    <td class="rowLevel1" align="right">
                                        <logic:equal name="machineBean" property="endpointState" value="">
                                            <span class="textInactive"> N/A </span>
                                        </logic:equal>
                                        <logic:notEqual name="machineBean" property="endpointState" value="">
                                            <bean:write name="machineBean" property="endpointState"/>
                                        </logic:notEqual>&nbsp;</td>
                                </tr>
                            </logic:iterate>
		                </table>
                    </div>
	            </div>
            </div> <!--padding tableWrapper-->
</logic:equal>
<logic:equal name="results" value="powerNotcheckedin" scope="request" >
        <!-- display results -->
        <div style="margin:15px 15px 0px 15px;color:#0000FF;" class="tableTitle"><webapps:pageText shared="true" type="compliance_power_group" key="NotCheckedInMachines" /> (<bean:write name="power_sum_result" property="powerNotcheckedinPer" />%)</div>
            <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        	    <div class="tableWrapper" style="width:99%; overflow:hidden;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
             		    <tr valign="middle" class="smallButtons">
                            <td class="tableSearchRow">
                                <logic:notPresent name="taskid">
                                    <input type='button' value='<webapps:pageText key="ShowInvQry" type="button" shared="true" />' onclick='javascript:showQuery( "powerNotcheckedin" )'>
                                </logic:notPresent>
                                <logic:present name="taskid">
                                &nbsp;
                                </logic:present>
                            </td>
                        </tr>
                    </table>
        	        <div class="headerSection" id="FOO_headerDiv">
        			    <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                            <colgroup width=""></colgroup>
                            <colgroup width=""></colgroup>
                            <tr id="FOO_headerTable_firstRow">
                                <td class="tableHeaderActive" style="border-left-width:0px; " onClick="location.href='#'"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="global" key="Machine"/></a></td>
                                <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_power_group" key="LastCheckedIn" /> </a></td>
        					</tr>
        			    </table>
        		    </div>
                    <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');" >
                        <table cellpadding="3" cellspacing="0" id="FOO_dataTable">
                            <colgroup width=""></colgroup>
                            <colgroup width=""></colgroup>

                            <logic:iterate id="machineBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                                <logic:equal name="iteridx" value="0">
                                    <tr id="FOO_dataTable_firstRow">
                                </logic:equal>
	         			      <% if(iteridx.intValue() != 0) { %>
				        		<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
		         		      <% } %>
                                        <td class="rowLevel1">
                                            <a href="javascript:void(0);" onmouseover="return overlib('<webapps:stringescape><bean:write name="machineBean" property="machineID" filter="false" /></webapps:stringescape>', DELAY, '200', WIDTH, '150');" onmouseout="return nd();"><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a>
                                            <a href="javascript:void(0);" onmouseover="return overlib('<webapps:stringescape><bean:write name="machineBean" property="machineID" filter="false" /></webapps:stringescape>', DELAY, '200', WIDTH, '150');" onmouseout="return nd();"><bean:write name="machineBean" property="machineName"/></a></td>
                                        <td class="rowLevel1"><webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"><bean:write name="machineBean" property="lastCheckIn"/></webapps:datetime>&nbsp;</td>
                                    </tr>
                            </logic:iterate>
        		        </table>
                    </div>
        	    </div>
            </div> <!--padding tableWrapper-->
</logic:equal>
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
resizeDataSection('FOO_dataDiv','endOfGui');
</script>
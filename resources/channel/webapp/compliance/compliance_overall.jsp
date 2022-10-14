<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.result.ComplianceSummaryResult,
                 com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean,
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

function generateReport(){
    if( hasPolicies ){
        if( confirm( "<webapps:pageText shared="true" key="reportConfirm" type="compliance_overall" escape="js"/>" ) ){
            document.packageComplianceForm.action = '/sm/compAddRpt.do';
            document.packageComplianceForm.submit();
        }
    } else {
        alert( "<webapps:pageText shared="true" key="generateReportError" type="compliance_overall" escape="js"/>");
    }
}

function showQuery( qryFor ){

    var qryPath = qryLibraryPrefix;
    if("site" == targetType) {
	    if( qryFor == 'succeed' ){
	        qryPath = getOverallComplianceQuery( qryLibraryPrefix +qryOverallCompliantSiteMachine, notCheckedInTimeLimit );
	    } else if( qryFor == 'failed' ){
	        qryPath = getOverallComplianceQuery( qryLibraryPrefix +qryOverallNonCompliantSiteMachine, notCheckedInTimeLimit );
	    } else if( qryFor == 'notcheckedin' ){
	        qryPath = getOverallComplianceQuery( qryLibraryPrefix +qryOverallNotCheckInSiteMachine, notCheckedInTimeLimit );
	    }
    } else {
	    if( qryFor == 'succeed' ){
	        qryPath = getOverallComplianceQuery( qryLibraryPrefix +qryOverallCompliantMachine, notCheckedInTimeLimit );
	    } else if( qryFor == 'failed' ){
	        qryPath = getOverallComplianceQuery( qryLibraryPrefix +qryOverallNonCompliantMachin, notCheckedInTimeLimit );
	    } else if( qryFor == 'notcheckedin' ){
	        qryPath = getOverallComplianceQuery( qryLibraryPrefix +qryOverallNotCheckInMachine, notCheckedInTimeLimit );
	    }
    }
    
    top.location.href = qryPath;
}
</script>

<div id="FOO_mainContent" class="formContent" style="overflow:auto; padding:5px 0px 0px 0px;">

<div class="sectionInfo" style="margin:0px 15px 10px 15px; "><webapps:pageText shared="true" type="compliance_overall" key="Info" /></div>

<!-- overall compliance table -->
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
    <bean:define id="results" value="failed" toScope="request"/>
</logic:notPresent>
<logic:present name="results">
    <bean:define id="results" name="results" toScope="request"/>
</logic:present>

<logic:present name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>">
    <bean:define id="compLastCalc" name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>" toScope="request"/>
</logic:present>

<tr>
    <!-- overall compliance bar -->
    <td nowrap style="padding-left:15px; border-bottom:1px solid #CCCCCC; "><webapps:pageText shared="true" type="compliance_overall" key="OverallCompliance" />:
        <script>
            document.write( getComplianceStatus( <bean:write name="comp_sum_result" property="compliant" />,
                                                 <bean:write name="comp_sum_result" property="noncompliant" />,
                                                 <bean:write name="comp_sum_result" property="notcheckedin" />,
                                                 <bean:write name="comp_sum_result" property="compliantPer" />,
                                                 <bean:write name="comp_sum_result" property="noncompliantPer" />,
                                                 <bean:write name="comp_sum_result" property="notcheckedinPer" />
                                                )
                            );
        </script>&nbsp;&nbsp;</td>
    <!-- compliance status tabs -->
    <logic:equal name="results" value="succeed" scope="request" >
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px;border-top:1px solid #CCCCCC;"><span class="textGreen" style="cursor:help" onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="compliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="compliant" value="1.0"><bean:write name="comp_sum_result" property="compliant"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="comp_sum_result" property="compliantPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compOverall.do?view=overall&results=failed" target="_self" class="textRed" onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="noncompliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="noncompliant" value="1.0"><bean:write name="comp_sum_result" property="noncompliant" filter="false" /></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="comp_sum_result" property="noncompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compOverall.do?view=overall&results=notcheckedin" target="_self" class="textBlue" onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="notcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notcheckedin" value="1.0"><bean:write name="comp_sum_result" property="notcheckedin" filter="false" /></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="comp_sum_result" property="notcheckedinPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
    </logic:equal>
    <logic:equal name="results" value="failed" scope="request">
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compOverall.do?view=overall&results=succeed" class="textGreen" onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="compliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="compliant" value="1.0"><bean:write name="comp_sum_result" property="compliant" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="comp_sum_result" property="compliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px; border-top:1px solid #CCCCCC; "><span class="textRed" style="cursor:help " onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="noncompliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="noncompliant" value="1.0"><bean:write name="comp_sum_result" property="noncompliant" filter="false" /></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="comp_sum_result" property="noncompliantPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compOverall.do?view=overall&results=notcheckedin" target="_self" class="textBlue" onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="notcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notcheckedin" value="1.0"><bean:write name="comp_sum_result" property="notcheckedin" filter="false" /></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="comp_sum_result" property="notcheckedinPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
    </logic:equal>
    <logic:equal name="results" value="notcheckedin" scope="request">
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compOverall.do?view=overall&results=succeed" target="_self" class="textGreen" onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="compliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="compliant" value="1.0"><bean:write name="comp_sum_result" property="compliant" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="comp_sum_result" property="compliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/sm/compOverall.do?view=overall&results=failed" target="_self" class="textRed" onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="noncompliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="noncompliant" value="1.0"><bean:write name="comp_sum_result" property="noncompliant" filter="false" /></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="comp_sum_result" property="noncompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px; border-top:1px solid #CCCCCC;"><span class="textBlue" style="cursor:help " onmouseover="return overlib('<webapps:stringescape><logic:equal name="comp_sum_result" property="notcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notcheckedin" value="1.0"><bean:write name="comp_sum_result" property="notcheckedin" filter="false" /></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="comp_sum_result" property="notcheckedinPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
    </logic:equal>
    <td style="padding-left:8px; border-bottom:1px solid #CCCCCC;"><a href="#" class="textGreen" style="cursor:help; " onmouseover="return overlib('<webapps:pageText shared="true" type="compliance_overall" key="CompLastCalc" escape="js"/>:<logic:notPresent name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>"><webapps:pageText type="global" key="NullDate" escape="js"/></logic:notPresent><logic:present name="<%=IWebAppConstants.SESSION_COMP_LASTCALCULATED%>"><webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"><bean:write name="compLastCalc" scope="request"/></webapps:datetime></logic:present>' , WIDTH, '250', DELAY, '100', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/info.gif" border="0"></a></td>&nbsp;
    <td align="right" style="border-bottom:1px solid #CCCCCC; padding-bottom:2px; padding-right:13px;">
      <logic:notPresent name="taskid">
        <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
            <input type="button" value="<webapps:pageText key="generateComplianceReport" type="button" shared="true"/>" onclick="javascript:generateReport()">
        </logic:equal>
        <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="true">
            <input type="button" disabled value="<webapps:pageText key="generateComplianceReport" type="button" shared="true"/>" onclick="javascript:generateReport()">
        </logic:equal>
      </logic:notPresent>
      <input type="hidden" name="value(reportaction)" value="add">
    </td>
    </tr>
</table>
<logic:equal name="results" value="succeed" scope="request" >
        <!-- display results -->
        <div style="margin:15px 15px 0px 15px;color:#339933;" class="tableTitle"><webapps:pageText shared="true" type="compliance_overall" key="SuccessfulMachines" /> (<bean:write name="comp_sum_result" property="compliantPer" />%)</div>
            <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
                <div class="tableWrapper" style="width:99%; overflow:hidden; border-top:1px solid #666666;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
             		    <tr valign="middle" class="smallButtons">
             			    <td class="tableSearchRow">
                                <logic:notPresent name="taskid">
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" onclick='javascript:showQuery( "succeed" )'>
                                    </logic:equal>
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="true">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" disabled onclick='javascript:showQuery( "succeed" )'>
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
                            <td class="rowLevel1"><a href="javascript:void(0);" onmouseover="return overlib('http://packageURL', DELAY, '200', WIDTH, '150');" onmouseout="return nd();"><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a> <a href="javascript:void(0);" onmouseover="return overlib('<webapps:stringescape><bean:write name="machineBean" property="machineID" filter="false"/></webapps:stringescape>', DELAY, '200', WIDTH, '150', LEFT);" onmouseout="return nd();"><bean:write name="machineBean" property="machineName"/></a></td>
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
<logic:equal name="results" value="failed" scope="request" >
        <!-- display results -->
        <div style="margin:15px 15px 0px 15px;color:#FF0000;" class="tableTitle"><webapps:pageText shared="true" type="compliance_overall" key="FailedMachines" /> (<bean:write name="comp_sum_result" property="noncompliantPer" />%)</div>
            <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
	            <div class="tableWrapper" style="width:99%; overflow:hidden;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
             		    <tr valign="middle" class="smallButtons">
             			    <td class="tableSearchRow">
                                <logic:notPresent name="taskid">
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" onclick='javascript:showQuery( "failed" )'>
                                    </logic:equal>
                                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="true">
                                        <input type='button' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" disabled onclick='javascript:showQuery( "failed" )'>
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
					            <td class="tableHeaderCell" ><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="Machine"/></a></td>
					            <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="CauseOfFailure" />&nbsp;</a></td>
                                <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="PolicyState" />&nbsp;</a></td>
                                <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="EndPointState" />&nbsp;</a></td>
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
                                    <td class="rowLevel1"><bean:write name="machineBean" property="failureCause"/>&nbsp;</td>
                                    <td class="rowLevel1">
                                        <logic:equal name="machineBean" property="policyState" value="">
                                            <webapps:pageText type="global" key="null.uppercase"/>
                                        </logic:equal>
                                        <logic:notEqual name="machineBean" property="policyState" value="">
                                            <webapps:pageText type='global' shared="true" key='<%=machineBean.getPolicyState()+".uppercase"%>'/>
                                        </logic:notEqual>&nbsp;</td>
                                    <td class="rowLevel1">
                                        <logic:equal name="machineBean" property="endpointState" value="">
                                            <webapps:pageText type="global" key="null.uppercase"/>
                                        </logic:equal>
                                        <logic:notEqual name="machineBean" property="endpointState" value="">
                                            <webapps:pageText type='global' shared="true" key='<%="compliance."+machineBean.getEndpointState()%>'/>
                                        </logic:notEqual>&nbsp;</td>
                                </tr>
                            </logic:iterate>
		                </table>
                    </div>
	            </div>
            </div> <!--padding tableWrapper-->
</logic:equal>
<logic:equal name="results" value="notcheckedin" scope="request" >

        <!-- display results -->
        <div style="margin:15px 15px 0px 15px;color:#0000FF;" class="tableTitle"><webapps:pageText shared="true" type="compliance_overall" key="NotCheckedInMachines" /> (<bean:write name="comp_sum_result" property="notcheckedinPer" />%)</div>
            <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        	    <div class="tableWrapper" style="width:99%; overflow:hidden;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
             		    <tr valign="middle" class="smallButtons">
                            <td class="tableSearchRow">
                                <logic:notPresent name="taskid">
                                    <input type='button' value='<webapps:pageText key="ShowInvQry" type="button" shared="true" />' onclick='javascript:showQuery( "notcheckedin" )'>
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
                                <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="LastCheckedIn" /> </a></td>
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
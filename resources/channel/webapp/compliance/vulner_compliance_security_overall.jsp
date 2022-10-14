<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.compliance.core.ConfigManager,
                 com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 java.text.DateFormat"%>

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
    var altNApp = '<webapps:pageText type="global" key="OrangeNotApplicable" escape="js"/>';
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
        <td nowrap style="padding-left:15px; border-bottom:1px solid #CCCCCC; "><webapps:pageText shared="true" type="compliance_overall" key="OverallMachineCompliance" />:
            <script>
                document.write( getComplianceStatus( <bean:write name="comp_sum_result" property="compliant" />,
                <bean:write name="comp_sum_result" property="noncompliant" />,
                <bean:write name="comp_sum_result" property="notcheckedin" />,
                <bean:write name="comp_sum_result" property="notapplicable" />,
                <bean:write name="comp_sum_result" property="compliantPer" />,
                <bean:write name="comp_sum_result" property="noncompliantPer" />,
                <bean:write name="comp_sum_result" property="notcheckedinPer" />,
                <bean:write name="comp_sum_result" property="notapplicablePer" />
                )
                );
            </script>&nbsp;&nbsp;</td>
        <!-- compliance status tabs -->
        <logic:equal name="results" value="succeed" scope="request" >
            <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
            <td style="padding-left:8px; padding-right:8px;border-top:1px solid #CCCCCC;"><span class="textGreen" style="cursor:help" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="compliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="compliant" value="1.0"><bean:write name="comp_sum_result" property="compliant"/></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><strong><bean:write name="comp_sum_result" property="compliantPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=failed" target="_self" class="textRed" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="noncompliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="noncompliant" value="1.0"><bean:write name="comp_sum_result" property="noncompliant" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="noncompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=notcheckedin" target="_self" class="textBlue" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notcheckedin" value="1.0"><bean:write name="comp_sum_result" property="notcheckedin" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="notcheckedinPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=notapplicable" target="_self" class="textOrange" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notapplicable" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notapplicable" value="1.0"><bean:write name="comp_sum_result" property="notapplicable" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="notapplicablePer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        </logic:equal>
        <logic:equal name="results" value="failed" scope="request">
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=succeed" class="textGreen" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="compliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="compliant" value="1.0"><bean:write name="comp_sum_result" property="compliant" filter="false"/></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="compliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px; border-top:1px solid #CCCCCC; "><span class="textRed" style="cursor:help " onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="noncompliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="noncompliant" value="1.0"><bean:write name="comp_sum_result" property="noncompliant" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><strong><bean:write name="comp_sum_result" property="noncompliantPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=notcheckedin" target="_self" class="textBlue" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notcheckedin" value="1.0"><bean:write name="comp_sum_result" property="notcheckedin" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="notcheckedinPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=notapplicable" target="_self" class="textOrange" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notapplicable" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notapplicable" value="1.0"><bean:write name="comp_sum_result" property="notapplicable" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="notapplicablePer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        </logic:equal>
        <logic:equal name="results" value="notcheckedin" scope="request">
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=succeed" target="_self" class="textGreen" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="compliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="compliant" value="1.0"><bean:write name="comp_sum_result" property="compliant" filter="false"/></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="compliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=failed" target="_self" class="textRed" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="noncompliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="noncompliant" value="1.0"><bean:write name="comp_sum_result" property="noncompliant" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="noncompliantPer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
        <td style="padding-left:8px; padding-right:8px; border-top:1px solid #CCCCCC;"><span class="textBlue" style="cursor:help " onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notcheckedin" value="1.0"><bean:write name="comp_sum_result" property="notcheckedin" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><strong><bean:write name="comp_sum_result" property="notcheckedinPer" />%</strong></span></td>
        <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=notapplicable" target="_self" class="textOrange" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notapplicable" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notapplicable" value="1.0"><bean:write name="comp_sum_result" property="notapplicable" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="notapplicablePer" />%</a></td>
        <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        </logic:equal>
        <logic:equal name="results" value="notapplicable" scope="request" >
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=succeed" target="_self" class="textGreen" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="compliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="compliant" value="1.0"><bean:write name="comp_sum_result" property="compliant" filter="false"/></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="compliantPer" />%</a></td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=failed" target="_self" class="textRed" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="noncompliant" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="noncompliant" value="1.0"><bean:write name="comp_sum_result" property="noncompliant" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="noncompliantPer" />%</a></td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;"><a href="/spm/securityVulnerMachineCompliance.do?view=overall&results=notcheckedin" target="_self" class="textBlue" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notcheckedin" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notcheckedin" value="1.0"><bean:write name="comp_sum_result" property="notcheckedin" filter="false" /></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="comp_sum_result" property="notcheckedinPer" />%</a></td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
            <td style="padding-left:8px; padding-right:8px;border-top:1px solid #CCCCCC;"><span class="textOrange" style="cursor:help" onmouseover="return MakeTip('<webapps:stringescape><logic:equal name="comp_sum_result" property="notapplicable" value="1.0">1'</logic:equal><logic:notEqual name="comp_sum_result" property="notapplicable" value="1.0"><bean:write name="comp_sum_result" property="notapplicable"/></logic:notEqual></webapps:stringescape>');" onmouseout="return CloseTip();"><strong><bean:write name="comp_sum_result" property="notapplicablePer" />%</strong></span></td>
            <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
        </logic:equal>
    </tr>
</table>
<logic:equal name="results" value="succeed" scope="request" >
    <!-- display results -->
    <div style="margin:15px 15px 0px 15px;color:#339933;" class="tableTitle"><webapps:pageText shared="true" type="compliance_overall" key="SuccessfulMachines" /> (<bean:write name="comp_sum_result" property="compliantPer" />%)</div>
    <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        <div class="tableWrapper" style="width:99%; overflow:hidden; border-top:1px solid #666666;">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
            </table>
            <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');">
                <colgroup width="33%"/>
                <colgroup width="33%"/>
                <colgroup width="33%"/>
                <table cellpadding="3" width="99%" cellspacing="0" id="FOO_dataTable">
                    <tr>
                        <logic:iterate id="machineBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                            <td class="rowLevel1"><a href="javascript:void(0);" onmouseover="return MakeTip('http://packageURL');" onmouseout="return CloseTip();"><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a> <a href="javascript:void(0);" onmouseover="return MakeTip('<webapps:stringescape><bean:write name="machineBean" property="machineID" filter="false"/></webapps:stringescape>');" onmouseout="return CloseTip();"><bean:write name="machineBean" property="machineName"/></a></td>
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
    <div style="margin:15px 15px 0px 15px;color:#FF0000;" class="tableTitle"><webapps:pageText shared="true" type="compliance_overall" key="noncompliant" /> (<bean:write name="comp_sum_result" property="noncompliantPer" />%)</div>
    <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        <div class="tableWrapper" style="width:99%; overflow:hidden;">
            <div class="headerSection" id="FOO_headerDiv">
                <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                    <colgroup width=""/>
                    <colgroup width=""/>
                    <colgroup width=""/>
                    <colgroup width=""/>
                    <tr id="FOO_headerTable_firstRow">
                        <td class="tableHeaderCell" ><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="Machine"/></a></td>
                    </tr>
                </table>
            </div>
            <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');" >
                <table cellpadding="3" cellspacing="0" id="FOO_dataTable">
                    <colgroup width=""/>
                    <colgroup width=""/>
                    <colgroup width=""/>
                    <colgroup width=""/>
                    <logic:iterate id="machineBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                        <logic:equal name="iteridx" value="0">
                            <tr id="FOO_dataTable_firstRow">
                        </logic:equal>
                        <% if(iteridx.intValue() != 0) { %>
                        <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                            <% } %>
                            <td class="rowLevel1" style="padding: 7px 0 7px 5px;">
                                <%--<a style="cursor: default;" onmouseover="return MakeTip('<webapps:stringescape><bean:write name="machineBean" property="policyName" filter="false" /></webapps:stringescape> &nbsp;');" onmouseout="return CloseTip();">--%>
                                <img src="/shell/common-rsrc/images/machine.gif">&nbsp;<bean:write name="machineBean" property="machineName"/>
                                <!--</a>-->
                            </td>
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
            <div class="headerSection" id="FOO_headerDiv">
                <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                    <colgroup width="50%"/>
                    <colgroup width="49%"/>
                    <tr id="FOO_headerTable_firstRow">
                        <td class="tableHeaderActive" style="border-left-width:0px; " onClick="location.href='#'"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="global" key="Machine"/></a></td>
                        <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="LastCheckedIn"/> </a></td>
                    </tr>
                </table>
            </div>
            <div id="FOO_dataDiv" style="height:100px; width:99%; overflow:auto;" onscroll="syncScroll('FOO');" >
                <table cellpadding="3" cellspacing="0" id="FOO_dataTable">
                    <colgroup width="50%"/><colgroup width="49%"/>
                    <logic:iterate id="machineBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                        <logic:equal name="iteridx" value="0">
                            <tr id="FOO_dataTable_firstRow">
                        </logic:equal>
                        <% if(iteridx.intValue() != 0) { %>
                        <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                            <% } %>
                            <td class="rowLevel1" style="padding: 7px 0 7px 5px;">
                                <%--<a style="cursor: default;" onmouseover="return MakeTip('<webapps:stringescape><bean:write name="machineBean" property="policyName" filter="false" /></webapps:stringescape> &nbsp;');" onmouseout="return CloseTip();">--%>
                                <img src="/shell/common-rsrc/images/machine.gif">&nbsp;<bean:write name="machineBean" property="machineName"/>
                                <!--</a>-->
                            </td>
                            <td class="rowLevel1">
                                <webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body">
                                    <bean:write name="machineBean" property="lastCheckIn"/>
                                </webapps:datetime>&nbsp;
                            </td>
                        </tr>
                    </logic:iterate>
                </table>
            </div>
        </div>
    </div> <!--padding tableWrapper-->
</logic:equal>
<logic:equal name="results" value="notapplicable" scope="request" >

    <!-- display results -->
    <div style="margin:15px 15px 0px 15px;color:#0000FF;" class="tableTitle"><webapps:pageText shared="true" type="compliance_overall" key="NotApplicableMachines" /> (<bean:write name="comp_sum_result" property="notapplicablePer" />%)</div>
    <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        <div class="tableWrapper" style="width:99%; overflow:hidden;">
            <div class="headerSection" id="FOO_headerDiv">
                <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                    <colgroup width="50%"/>
                    <colgroup width="49%"/>
                    <tr id="FOO_headerTable_firstRow">
                        <td class="tableHeaderActive" style="border-left-width:0px; " onClick="location.href='#'"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="global" key="Machine"/></a></td>
                        <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_overall" key="LastCheckedIn"/> </a></td>
                    </tr>
                </table>
            </div>
            <div id="FOO_dataDiv" style="height:100px; width:99%; overflow:auto;" onscroll="syncScroll('FOO');" >
                <table cellpadding="3" cellspacing="0" id="FOO_dataTable">
                    <colgroup width="50%"/><colgroup width="49%"/>
                    <logic:iterate id="machineBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                        <logic:equal name="iteridx" value="0">
                            <tr id="FOO_dataTable_firstRow">
                        </logic:equal>
                        <% if(iteridx.intValue() != 0) { %>
                        <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                            <% } %>
                            <td class="rowLevel1" style="padding: 7px 0 7px 5px;">
                                <%--<a style="cursor: default;" onmouseover="return MakeTip('<webapps:stringescape><bean:write name="machineBean" property="policyName" filter="false" /></webapps:stringescape> &nbsp;');" onmouseout="return CloseTip();">--%>
                                <img src="/shell/common-rsrc/images/machine.gif">&nbsp;<bean:write name="machineBean" property="machineName"/>
                                <!--</a>-->
                            </td>
                            <td class="rowLevel1">
                                <webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body">
                                    <bean:write name="machineBean" property="lastCheckIn"/>
                                </webapps:datetime>&nbsp;
                            </td>
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
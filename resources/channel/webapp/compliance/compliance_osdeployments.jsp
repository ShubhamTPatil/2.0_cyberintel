<%@ include file="/includes/directives.jsp" %>

<%@ page import="java.util.*,
                 java.text.*,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                 com.marimba.apps.subscriptionmanager.compliance.core.ComplianceMain,
                 com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants,
                 com.marimba.apps.subscriptionmanager.compliance.core.ConfigManager" %>
<bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target"/>

<script type="text/javascript" src="/sm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/sm/includes/complianceJsonClient.js"></script>

<script type="text/javascript">
    hasPolicies = true;
    calculateTarget = '<%= WebUtil.jsEncode(target.getID()) %>';
    notCheckedInTimeLimit = <%=( String )request.getAttribute( ConfigManager.CFG_CHECKIN_LIMIT )%>
    var altComp = '<webapps:pageText type="global" key="GreenCompliant" escape="js"/>';
    var altNComp = '<webapps:pageText type="global" key="RedNoncompliant" escape="js"/>';
    var altNCI = '<webapps:pageText type="global" key="BlueNotchecked" escape="js"/>';

</script>
<bean:define id="resultsSize" value="<%=""+( ( List )session.getAttribute( "policy_details_fortgt" ) ).size()%>" toScope="session"/>

<logic:equal name="resultsSize" value="0">
        <bean:define id="osTemplateName" value='NOTAPPLICABLE' toScope="request"/>
</logic:equal>
<logic:notEqual name="resultsSize" value="0">
    <logic:iterate id="OSDeploymentStatusBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.OSDeploymentStatusBean" indexId="iteridx">
        <bean:define id="osTemplateName" name="OSDeploymentStatusBean" property="templateName" toScope="request"/>
    </logic:iterate>
</logic:notEqual>

<logic:notPresent name="results">
    <bean:define id="results" value="osmFailed" toScope="request"/>
</logic:notPresent>
<logic:present name="results">
    <bean:define id="results" name="results" toScope="request"/>
</logic:present>

<div id="FOO_mainContent" class="formContent" style="overflow:auto; padding:5px 0 0 0;">

<div class="sectionInfo" style="margin:10px 15px 10px 15px;"><webapps:pageText shared="true" type="compliance_osdeployments" key="Info" /></div>
<table cellspacing="0" cellpadding="0" style="width:100% ">
    <colgroup span="11" width="0*"></colgroup>
    <colgroup width="100%"></colgroup>
    <tr>
        <td nowrap style="padding-left:15px; border-bottom:1px solid #CCCCCC; "><webapps:pageText shared="true" type="compliance_osdeployments" key="osmCompliance" />:
            <script>
                document.write(getComplianceStatus(
                        <bean:write name="osm_sum_result" property="compliantCount"/>,
                        <bean:write name="osm_sum_result" property="noncompliantCount"/>,
                        <bean:write name="osm_sum_result" property="notcheckedinCount"/>,
                        <bean:write name="osm_sum_result" property="compliantPer"/>,
                        <bean:write name="osm_sum_result" property="noncompliantPer"/>,
                        <bean:write name="osm_sum_result" property="notcheckedinPer"/>
                        ));
            </script> &nbsp;&nbsp;
        </td>
        <logic:equal name="results" value="osmSucceed" scope="request" >
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
            <td style="padding-left:8px; padding-right:8px;border-top:1px solid #CCCCCC;">
                    <span class="textGreen" style="cursor:help"
                          onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="compliantCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="compliantCount" value="1.0"><bean:write name="osm_sum_result" property="compliantCount"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="osm_sum_result" property="compliantPer" />%</strong></span>
            </td>
            <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>

            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;">
                <a href="/sm/compOSDeployment.do?view=osdeployment&results=osmFailed" target="_self" class="textRed"
                   onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="noncompliantCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="noncompliantCount" value="1.0"><bean:write name="osm_sum_result" property="noncompliantCount" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="osm_sum_result" property="noncompliantPer" />%</a>
            </td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px;border-bottom:1px solid #CCCCCC;">
                <a href="/sm/compOSDeployment.do?view=osdeployment&results=osmPending" target="_self" class="textBlue"
                   onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="notcheckedinCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="notcheckedinCount" value="1.0"><bean:write name="osm_sum_result" property="notcheckedinCount" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="osm_sum_result" property="notcheckedinPer" />%</a>
            </td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        </logic:equal>

        <logic:equal name="results" value="osmFailed" scope="request" >
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;">
                <a href="/sm/compOSDeployment.do?view=osdeployment&results=osmSucceed" target="_self" class="textGreen" onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="compliantCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="compliantCount" value="1.0"><bean:write name="osm_sum_result" property="compliantCount"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="osm_sum_result" property="compliantPer" />%</a>
            </td>
            <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
            <td style="padding-left:8px; padding-right:8px;border-top:1px solid #CCCCCC;">
                    <span class="textRed" style="cursor:help"
                          onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="noncompliantCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="noncompliantCount" value="1.0"><bean:write name="osm_sum_result" property="noncompliantCount" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="osm_sum_result" property="noncompliantPer" />%</strong></span>
            </td>
            <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;">
                <a href="/sm/compOSDeployment.do?view=osdeployment&results=osmPending" target="_self" class="textBlue"
                   onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="notcheckedinCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="notcheckedinCount" value="1.0"><bean:write name="osm_sum_result" property="notcheckedinCount" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="osm_sum_result" property="notcheckedinPer" />%</a>
            </td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        </logic:equal>

        <logic:equal name="results" value="osmPending" scope="request" >
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;">
                <a href="/sm/compOSDeployment.do?view=osdeployment&results=osmSucceed" target="_self" class="textGreen" onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="compliantCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="compliantCount" value="1.0"><bean:write name="osm_sum_result" property="compliantCount"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="osm_sum_result" property="compliantPer" />%</a>
            </td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td style="padding-left:8px; padding-right:8px; border-bottom:1px solid #CCCCCC;">
                <a href="/sm/compOSDeployment.do?view=osdeployment&results=osmFailed" target="_self" class="textRed"
                   onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="noncompliantCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="noncompliantCount" value="1.0"><bean:write name="osm_sum_result" property="noncompliantCount" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><bean:write name="osm_sum_result" property="noncompliantPer" />%</a>
            </td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
            <td><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
            <td style="padding-left:8px; padding-right:8px;border-top:1px solid #CCCCCC;">
                    <span class="textBlue" style="cursor:help"
                          onmouseover="return overlib('<webapps:stringescape><logic:equal name="osm_sum_result" property="notcheckedinCount" value="1.0">1'</logic:equal><logic:notEqual name="osm_sum_result" property="notcheckedinCount" value="1.0"><bean:write name="osm_sum_result" property="notcheckedinCount" filter="false"/></logic:notEqual></webapps:stringescape>', WIDTH, '20', DELAY, '100');" onmouseout="return nd();"><strong><bean:write name="osm_sum_result" property="notcheckedinPer" />%</strong></span>
            </td>
            <td><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
            <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
        </logic:equal>
        <td style="padding-left:8px; border-bottom:1px solid #CCCCCC;">
            &nbsp;&nbsp
        </td>

        <td align="right" style="border-bottom:1px solid #CCCCCC; padding-bottom:2px; padding-right:13px;">&nbsp;&nbsp;</td>
    </tr>
</table>
<!--Displaying results here-->
<!--Display Suucess count-->
<br>
 <logic:equal name="osTemplateName" value="NOTAPPLICABLE">
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<webapps:pageText shared="true" type="compliance_osdeployments" key="templateName" /> <webapps:pageText type="global" key="null.uppercase"/>
</logic:equal>
<logic:notEqual name="osTemplateName" value="NOTAPPLICABLE">
     &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<webapps:pageText shared="true" type="compliance_osdeployments" key="templateName" /> <bean:write name="osTemplateName"/>
</logic:notEqual>
<logic:equal name="results" value="osmSucceed" scope="request" >
    <div style="margin:15px 15px 0px 15px;color:#0000FF;" class="tableTitle"><font class="textGreen"><webapps:pageText shared="true" type="compliance_osdeployments" key="succeedMachines" /> (<bean:write name="osm_sum_result" property="compliantPer" />%)</font></div>
    <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        <div class="tableWrapper" style="width:100%; overflow:hidden;">
            <div class="headerSection" id="FOO_headerDiv">
                <table style="width: 100%" border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <tr id="FOO_headerTable_firstRow">
                        <td class="tableHeaderActive" style="border-left-width:0px; " onClick="location.href='#'"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="global" key="Machine"/></a></td>
                        <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_osdeployments" key="updatedTime" /> </a></td>
                    </tr>
                </table>
            </div>
            <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');" >
                <table style="width: 100%" cellpadding="3" cellspacing="0" id="FOO_dataTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <logic:notEqual name="resultSize" value="0">
                    <logic:iterate id="OSDeploymentStatusBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.OSDeploymentStatusBean" indexId="iteridx">
                        <logic:equal name="OSDeploymentStatusBean" property="complianceLevel" value="finished">
                            <logic:equal name="iteridx" value="0">
                                <tr id="FOO_dataTable_firstRow">
                            </logic:equal>
                            <% if(iteridx.intValue() != 0) { %>
                            <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                                <% } %>
                                <td class="rowLevel1">
                                    <a href="javascript:void(0);" ><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a>
                                    <a href="javascript:void(0);"><bean:write name="OSDeploymentStatusBean" property="machineName"/></a></td>
                                <td class="rowLevel1"><bean:write name="OSDeploymentStatusBean" property="updatedTime"/>&nbsp;</td>
                            </tr>
                        </logic:equal>
                    </logic:iterate>
                    </logic:notEqual>
                </table>
            </div>
        </div>
    </div>
</logic:equal>

<!--Display Failed count-->
<logic:equal name="results" value="osmFailed" scope="request" >
    <div style="margin:15px 15px 0px 15px;color:#0000FF;" class="tableTitle"><font class="textRed"><webapps:pageText shared="true" type="compliance_osdeployments" key="failedMachines" /> (<bean:write name="osm_sum_result" property="noncompliantPer" />%)</font></div>
    <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        <div class="tableWrapper" style="width:100%; overflow:hidden;">
            <div class="headerSection" id="FOO_headerDiv">
                <table style="width: 100%" border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <tr id="FOO_headerTable_firstRow">
                        <td class="tableHeaderActive" style="border-left-width:0px; " onClick="location.href='#'"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="global" key="Machine"/></a></td>
                        <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_osdeployments" key="updatedTime" /> </a></td>
                    </tr>
                </table>
            </div>
            <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');" >
                <table style="width: 100%" cellpadding="3" cellspacing="0" id="FOO_dataTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <logic:notEqual name="resultSize" value="0">
                    <logic:iterate id="OSDeploymentStatusBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.OSDeploymentStatusBean" indexId="iteridx">
                        <logic:equal name="OSDeploymentStatusBean" property="complianceLevel" value="failed">
                            <logic:equal name="iteridx" value="0">
                                <tr id="FOO_dataTable_firstRow">
                            </logic:equal>
                            <% if(iteridx.intValue() != 0) { %>
                            <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                                <% } %>
                                <td class="rowLevel1">
                                    <a href="javascript:void(0);" ><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a>
                                    <a href="javascript:void(0);"><bean:write name="OSDeploymentStatusBean" property="machineName"/></a></td>
                                <td class="rowLevel1"><bean:write name="OSDeploymentStatusBean" property="updatedTime"/>&nbsp;</td>
                            </tr>
                        </logic:equal>
                    </logic:iterate>
                    </logic:notEqual>
                </table>
            </div>
        </div>
    </div>
</logic:equal>

<!--Display Pending count-->
<logic:equal name="results" value="osmPending" scope="request" >
    <!-- display results -->
    <div style="margin:15px 15px 0px 15px;color:#0000FF;" class="tableTitle"><font class="textBlue"><webapps:pageText shared="true" type="compliance_osdeployments" key="pendingMachines" /> (<bean:write name="osm_sum_result" property="notcheckedinPer" />%)</font></div>
    <div style="padding-left:15px; padding-right:15px; padding-bottom:15px;">
        <div class="tableWrapper" style="width:100%; overflow:hidden;">
            <div class="headerSection" id="FOO_headerDiv">
                <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <tr id="FOO_headerTable_firstRow">
                        <td class="tableHeaderActive" style="border-left-width:0px; " onClick="location.href='#'"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="global" key="Machine"/></a></td>
                        <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_osdeployments" key="updatedTime" /> </a></td>
                    </tr>
                </table>
            </div>
            <div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');" >
                <table cellpadding="3" cellspacing="0" id="FOO_dataTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <logic:notEqual name="resultSize" value="0">
                    <logic:iterate id="OSDeploymentStatusBean" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.OSDeploymentStatusBean" indexId="iteridx">
                        <logic:equal name="OSDeploymentStatusBean" property="complianceLevel" value="pending">
                            <logic:equal name="iteridx" value="0">
                                <tr id="FOO_dataTable_firstRow">
                            </logic:equal>
                            <% if(iteridx.intValue() != 0) { %>
                            <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                                <% } %>
                                <td class="rowLevel1">
                                    <a href="javascript:void(0);" ><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" border="0" align="absmiddle"></a>
                                    <a href="javascript:void(0);"><bean:write name="OSDeploymentStatusBean" property="machineName"/></a></td>
                                <td class="rowLevel1"><bean:write name="OSDeploymentStatusBean" property="updatedTime"/>&nbsp;</td>
                            </tr>
                        </logic:equal>
                    </logic:iterate>
                    </logic:notEqual>
                </table>
            </div>
        </div>
    </div> <!--padding tableWrapper-->
</logic:equal>
<!--End of results deisplay-->


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
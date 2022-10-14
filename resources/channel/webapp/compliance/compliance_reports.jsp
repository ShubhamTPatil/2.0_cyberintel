<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                 java.sql.Date,
                 java.util.List,
                 java.text.DateFormat"%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
    <webapps:helpContext context="sm" topic="pc_reports" />
<script type="text/javascript" src="/sm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/sm/includes/complianceJsonClient.js"></script>
<script>
var singleOptionElements = new Array("delete_btn");
var multiOptionElements = new Array("delete_btn");

var complianceText = '<webapps:pageText shared="true" type="compliance_reports" key="OverallCompliance" escape="js"/>';
var altComp = '<webapps:pageText type="global" key="GreenCompliant" escape="js"/>';
var altNComp = '<webapps:pageText type="global" key="RedNoncompliant" escape="js"/>';
var altNCI = '<webapps:pageText type="global" key="BlueNotchecked" escape="js"/>';

function doReports( operation ){
    //document.packageComplianceForm.calcId = calcId;
    if( operation == 'delete' ){
        document.packageComplianceForm.action = '/sm/compDelRpt.do';
        document.packageComplianceForm['value(reportaction)'].value = 'delete';
    } else {
        document.packageComplianceForm['value(reportaction)'].value = '';
    }
    document.packageComplianceForm.submit();
}

function compLevelReport( compLevel, cId ){
    document.packageComplianceForm['value(compLevel)'].value = compLevel;
    document.packageComplianceForm['value(calcId)'].value = cId;
    document.packageComplianceForm.action = '/sm/compLevelRpt.do';
    document.packageComplianceForm.submit();
}

function getReportSummary( succeed, failed, notchkdin, calcId ){
    addReport( succeed, failed, notchkdin );
    var total = succeed+failed+notchkdin;
    var sPer = getPer( succeed, total );
    var fPer = getPer( failed, total );
    var ncPer = getPer( notchkdin, total );
    var component = '';

    component = '<img src="/shell/common-rsrc/images/invisi_shim.gif" width="'+sPer+'" height="8" alt="'+altComp+'" style="background-color:#66cc66;">'+
                '<img src="/shell/common-rsrc/images/invisi_shim.gif" width="'+fPer+'" height="8" alt="'+altNComp+'" style="background-color:#ff6666;">'+
                '<img src="/shell/common-rsrc/images/invisi_shim.gif" width="'+ncPer+'" height="8" alt="'+altNCI+'" style="background-color:#3399ff;"> '+
                '<span><a target="_top" style="cursor:pointer;text-decoration:underline" class="textGreen" onclick="javascript:compLevelReport( \'success\', '+calcId+' )">'+succeed+'</a></span>&nbsp;'+
                '<span><a target="_top" style="cursor:pointer;text-decoration:underline" class="textRed" onclick="javascript:compLevelReport( \'failed\', '+calcId+' )">'+failed+'</a></span>&nbsp;'+
                '<span><a target="_top" style="cursor:pointer;text-decoration:underline" class="textBlue" onclick="javascript:compLevelReport( \'nci\', '+calcId+' )">'+notchkdin+'</a></span>';
    return component;
}

</script>
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/body.html" %>

<%@ include file="/includes/info.jsp" %>

<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="compRptView"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="compRptView"/>
<% } %>


<% int startIndex = 0; %>

    <div align="center" style="padding-left:25px; padding-right:25px;">
        <html:form name="packageComplianceForm" action="/compRptView.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">
            <!-- for paging results -->
            <div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
            <%-- Errors Display --%>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <%@ include file="/includes/usererrors.jsp" %>
            </table>
            <%@ include file="/includes/help.jsp" %>
            <div style="width:97%;">
                <bean:define id="pageBeanName" value="<%=IWebAppConstants.SESSION_POLICIES_DETAILS%>" toScope="request" />
                <sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>" resultsName="<%=IWebAppConstants.COMPRPT_CACHERPTS%>" />
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td align="right" nowrap>
                            <% request.setAttribute("formName","document.packageComplianceForm"); %>
                            <jsp:include page="/includes/genPrevNext.jsp" />
                        </td>
                    </tr>
                </table>
            </div>
            <bean:define id="totalReports" value="<%=""+( ( List )request.getAttribute( "display_rs" ) ).size()%>" toScope="request"/>
            <div class="tableWrapper" style="width:100%;">
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr valign="middle" class="smallButtons">
                        <td nowrap class="tableRowActions">
                            <input type="button" name="delete_btn" value="<webapps:pageText key="delete" type="button" shared="true" />" id="delete_btn" disabled onClick="javascript:doReports( 'delete' )">&nbsp;
                            <input type="button" name="refresh_btn" value="<webapps:pageText key="refresh" type="button" shared="true" />" id="refresh_btn" <logic:equal  name="totalReports" value="0">disabled</logic:equal> onClick="javascript:doReports( 'refresh' )"></td>
                            <input type="hidden" name="value(reportaction)" value=""/>
                    </tr>
                </table>
                <div class="headerSection" style="width:100%; text-align:left; overflow:hidden;" id="FOO_headerDiv">
                    <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>
                        <thead>
                            <tr id="FOO_headerTable_firstRow">
                                <td class="tableHeaderCell" id="checkboxCol"><input type="checkbox" name="value(target_sel_all)" id="target_sel_all" onClick="checkboxToggle('target_sel')"></td>
                                <td class="tableHeaderActive" id="targetCol"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports" key="target" /></a></td>
                                <td class="tableHeaderCell" id="complianceCol"><div id="compliance_type"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports" key="OverallCompliance" /> </a>&nbsp;&nbsp;&nbsp;</div></td>
                                <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports" key="StartTime" /></a>&nbsp;</td>
                                <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports" key="EndTime" /></a>&nbsp;</td>
                            </tr>
                        </thead>
                    </table>
                </div><!-- end of header section-->
                <div id="FOO_dataDiv" style="height:200px; width:100%; overflow:auto; text-align:left;" onscroll="syncScroll('FOO');">
                    <table border="0" cellpadding="0" cellspacing="0" id="FOO_dataTable">
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>
                        <colgroup width=""></colgroup>

                        <logic:iterate id="report" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.ReportBean" indexId="iteridx">
                            <tbody id='<%="row1-"+iteridx%>'>
                            <logic:equal name="iteridx" value="0">
                        	    <tr id="FOO_dataTable_firstRow">
                        	</logic:equal>
	         			      <% if(iteridx.intValue() != 0) { %>	
				        		<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
		         		      <% } %>
                                    <!--Check box column-->
                                    <td class="rowLevel1">
                                        <input type='checkbox' name='value(<%="target_sel_"+(new Integer(startIndex + iteridx.intValue())).toString()%>)' id='<%="target_sel_"+(new Integer(startIndex + iteridx.intValue())).toString()%>' onClick="processCheckbox(this.id)"/>
							        </td>
							        <!--Targets column-->
                                    <input type='hidden' id='<%="trgtid_target_sel_"+iteridx%>' name='<%="trgtid_target_sel_"+iteridx%>' value='<%=report.getTarget()%>'/>
                                    <input type='hidden' id='<%="trgttype_target_sel_"+iteridx%>' name='<%="trgttype_target_sel_"+iteridx%>' value='<%=report.getType()%>'/>
							        <td class="rowLevel1">
                                        <% String mouseOverStr = "<b>Target:</b>" + report.getTarget();%>
                                        <bean:define id="ID" name="report" property="target" toScope="request"/>
                                        <bean:define id="Name" name="report" property="name" toScope="request"/>
                                        <bean:define id="Type" name="report" property="type" toScope="request"/>
                                        <jsp:include page="/includes/target_display_single.jsp"/>
							        </td>
                                    <!-- Compliance summary -->
                                    <td class="rowLevel1">
                                        <div id='<%="comp_target_sel_"+iteridx%>' name='<%="comp_target_sel_"+iteridx%>' calcId='<%=report.getCalculationId()%>'>
                                            <input type="hidden" name='value(<%="comp_target_cid_"+iteridx%>)' value='<%=report.getCalculationId()%>'/>
                                            <logic:equal name="report" property="hasCachedCompliance" value="false">
                                                <span class="textInactive"><webapps:pageText shared="true" type="package_compliance" key="ComplianceNotCaluculated" /></span>
                                            </logic:equal>
                                            <logic:equal name="report" property="hasCachedCompliance" value="true">
                                            <bean:define id="summary" name="report" property="reportSummary" type="com.marimba.apps.subscriptionmanager.compliance.view.ComplianceSummaryBean"/>
                                                <script>
                                                    calculateElm = document.getElementById('comp_target_sel_'+'<%=iteridx%>' );
                                                    calculateTarget = '<%= WebUtil.jsEncode( report.getTarget() ) %>';
                                                    document.write(getReportSummary(<%= summary.getCompliant() %>, <%= summary.getNoncompliant() %>, <%= summary.getNotcheckedin() %>, <%=report.getCalculationId()%> ));
                                                </script>
                                            </logic:equal>
                                        </div>
                                    </td>

                                    <!-- start time -->
                                    <logic:present name="report" property="startTime">
                                        <td class="rowLevel1"><p>
                                            <webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body">
                                                <bean:write name="report" property="startTime" />
                                             </webapps:datetime></p>
                                         </td>
                                    </logic:present>
                                    <!-- End time -->
                                    <logic:present name="report" property="startTime">
                                        <td class="rowLevel1"><p>
                                            <webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body">
                                                <bean:write name="report" property="endTime" />
                                            </webapps:datetime></p>
                                        </td>
                                    </logic:present>
								</tr>
						</tbody>
                    </logic:iterate>
                    <input type="hidden" name="value(compLevel)" value="success"/>
                    <input type="hidden" name="value(calcId)" value="-1"/>
                    <input type="hidden" name="value(totalReports)" value="<bean:write name="totalReports"/>" />
                    </table>
                </div><!--end dataSection-->
            </div><!--end of tableWrapper-->
    </div>
    </html:form>
<div id="endOfGui"></div>

<script>
resizeDataSection('FOO_dataDiv','endOfGui', '-1' );
syncTables('FOO');
</script>

</body>
</html>

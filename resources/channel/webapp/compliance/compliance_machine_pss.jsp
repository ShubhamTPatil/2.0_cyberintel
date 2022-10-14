<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants,
                   java.util.List,
                   java.text.DateFormat,
                   com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                   com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                   java.util.Map" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants" %>

<script>
var singleOptionElements = new Array("calculate_btn");
var multiOptionElements = new Array();
</script>
<% int startIndex = 0; %>
<bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target"/>
<div class="tableWrapper" style="width:99%; overflow:hidden;">
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
		<tr valign="middle" class="smallButtons">
			<td class="tableSearchRow">&nbsp;</td>
            <logic:notPresent name="taskid">
			<td align="right" class="tableSearchRow">
                <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
				<table border="0" cellspacing="0" cellpadding="0">
					<tr>
						    <td align="center" class="smallCaption" class="smallCaption" style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)" onClick="javascript:redirect('<%= "/initTargetView.do?src=tgtview&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode("/targetViewDispatcher.do?name=" + target.getName() + "&targetType=" + target.getType() + "&id=" + target.getId()) %>');"><img src="/sm/images/policy.gif" alt="View Policy" width="16" height="16" border="0"><br>
							<webapps:pageText shared="true" type="compliance_machine" key="ViewPolicy" /></td>
					</tr>
				</table>
                </logic:equal>
			</td>
            </logic:notPresent>
		</tr>
  </table>
	<div class="headerSection" id="FOO_headerDiv">
		<table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <tr id="FOO_headerTable_firstRow">
                         <td class="tableHeaderCell" align="left"><a class="columnHeading" href="#"><webapps:pageText type='global' key='powerprops'/></a></td>
                         <td class="tableHeaderCell" align="left"><a class="columnHeading" href="#"><webapps:pageText type='global' key='policyval'/></a></td>
                         <td class="tableHeaderCell" align="left"><a class="columnHeading" href="#"><webapps:pageText type='global' key='endptval'/></a></td>
                         <td class="tableHeaderCell" align="left"><a class="columnHeading" href="#"><webapps:pageText type='global' key='DirectlyAssignedTo'/></a></td>
                    </tr>
		</table>
	</div>
	<div id="FOO_dataDiv" style="width:100%; overflow:auto;" onscroll="syncScroll('FOO');">
            <table cellpadding="0" cellspacing="0" id="FOO_dataTable">
                <colgroup width=""></colgroup>
                <colgroup width=""></colgroup>
                <colgroup width=""></colgroup>
                <colgroup width=""></colgroup>
                <bean:define id="resultsSize" value="<%=""+( ( List )request.getAttribute( "display_rs" ) ).size()%>" toScope="request"/>
                <logic:iterate id="machine_package_pss" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachinePssBean" indexId="iteridx">
                    <tbody id='<%="row1-"+iteridx%>'>
                    <logic:equal name="iteridx" value="0">
            		    <tr id="FOO_dataTable_firstRow">
            		</logic:equal>
				    <% if(iteridx.intValue() != 0) { %>
						<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
				    <% } %>
                    <%
                        Map pssMap = machine_package_pss.getPssMap();
                        String compText = "textBlue";
                        for (int i = 0; i < ComplianceConstants.policyPowerOption.length; i++) {
                            String policyPower = ((String) pssMap.get(ComplianceConstants.policyPowerOption[i]));
                            String endptPower = ((String) pssMap.get(ComplianceConstants.endpointPowerOption[i]));
                            String compliance = (String) pssMap.get(ComplianceConstants.powercompliant[i]);

                            if(ComplianceConstants.PWD_PROMPT_ENABLE.equals(ComplianceConstants.policyPowerOption[i]) || ComplianceConstants.HIBERNATE_ENABLE.equals(ComplianceConstants.policyPowerOption[i])) {
                                policyPower =  policyPower.equals("1") ? ComplianceConstants.ENABLE : ComplianceConstants.DISABLE;
                                endptPower =  endptPower.equals("1") ? ComplianceConstants.ENABLE : ComplianceConstants.DISABLE;
                            }

                            if (ComplianceConstants.POWER_STR_LEVEL_COMPLIANT.equalsIgnoreCase(compliance)) {
                                compText = "textGreen";
                            } else if (ComplianceConstants.POWER_STR_LEVEL_NON_COMPLIANT.equalsIgnoreCase(compliance)) {
                                compText = "textRed";
                            } else {
                                compText = "textBlue";
                            }

                            if (!("-1").equals(policyPower) ) {   %>


                                     <td class="rowLevel1" width="25%"><webapps:pageText type='global' key='<%=ComplianceConstants.powerOptions[i]%>'/></Td>
                                     <TD class="rowLevel1" width="25%" align="right"><span class="<%=compText%>"><b><%=policyPower%></b></span></TD>
                                     <% if (!"".equals(endptPower)) { %>
                                     <TD class="rowLevel1" width="25%" align="right"><span class="<%=compText%>"><b><%=endptPower%></b></span></TD>
                                     <% } else { %>
                                     <TD class="rowLevel1" width="25%" align="right"><span class="textInactive"> N/A </span></TD>
                                     <% } %>
                                     <td valign="top" class="rowLevel1" width="25%">
                                        <bean:define id="ID" name="machine_package_pss" property="targetName" toScope="request"/>
                                        <bean:define id="Name" name="machine_package_pss" property="policyName" toScope="request"/>
                                        <bean:define id="Type" name="machine_package_pss" property="policyTargetType" toScope="request"/>
                                        <input type='hidden' id='<%="trgtid_target_sel_"+iteridx%>' name='<%="trgtid_target_sel_"+iteridx%>' value='<%=machine_package_pss.getPolicyName()%>'/>
                                        <input type='hidden' id='<%="trgttype_target_sel_"+iteridx%>' name='<%="trgttype_target_sel_"+iteridx%>' value='<%=machine_package_pss.getPolicyTargetType()%>'/>
                                        <jsp:include page="/includes/target_display_single.jsp"/>
                                     </td>
                                </tr>

                    <%
                            }
                        }
                    %>

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
<div id="endOfGui"></div>
<script>
function setActiveBg(T,s) {
	T.style.backgroundColor = (s) ? '#FDFDFD' : '';
}
</script>
<script>
resizeDataSection('FOO_dataDiv','endOfGui');
</script>
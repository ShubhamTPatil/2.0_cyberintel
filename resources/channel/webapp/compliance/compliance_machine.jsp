<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants,
                   java.util.List,
                   java.text.DateFormat,
                   com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                   com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
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
			<colgroup width=""></colgroup>
			<tr id="FOO_headerTable_firstRow">
				<td class="tableHeaderActive">&nbsp;</td>
				<td class="tableHeaderActive" style="border-left-width:0px; "><webapps:pageText shared="true" type="compliance_machine" key="Package" /></td>
				<td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_machine" key="State" /></a></td>
				<td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_machine" key="Compliance" /></a></td>
				<td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_machine" key="DirectlyAssignedTo" /></a></td>
			</tr>
		</table>
	</div>
	<div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');">
    <table cellpadding="0" cellspacing="0" id="FOO_dataTable">
			<colgroup width=""></colgroup>
			<colgroup width=""></colgroup>
			<colgroup width=""></colgroup>
			<colgroup width=""></colgroup>
			<colgroup width=""></colgroup>
            <bean:define id="resultsSize" value="<%=""+( ( List )request.getAttribute( "display_rs" ) ).size()%>" toScope="request"/>
            <logic:iterate id="machine_package" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachinePackageBean" indexId="iteridx">
                <tbody id='<%="row1-"+iteridx%>'>
                    <logic:equal name="iteridx" value="0">
            		    <tr id="FOO_dataTable_firstRow">
            		</logic:equal>
				    <% if(iteridx.intValue() != 0) { %>
						<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
				    <% } %>
                    <td class="rowLevel1">
                        <a href="javascript:void(0);" style="cursor:help;" onmouseover="return overlib('<div><webapps:pageText key="packageLastPublished" shared="true" type="compliance_machine" escape="js"/> <logic:notPresent name="machine_package" property="pkgPublishedTime"><webapps:pageText type="global" key="NullDate" escape="js"/></logic:notPresent><logic:present name="machine_package" property="pkgPublishedTime"><webapps:stringescape><webapps:datetime dateStyle="<%=DateFormat.MEDIUM%>" timeStyle="<%=DateFormat.MEDIUM%>" inputFormatter="<%=WebUtil.getComplianceDateFormat()%>" locale="request" value="body"><bean:write name="machine_package" property="pkgPublishedTime"/></webapps:datetime></webapps:stringescape></logic:present></div>', DELAY, '200', WIDTH, '250');" onmouseout="return nd();">
                            <img src="/shell/common-rsrc/images/info.gif" width="12" height="12" border="0">
                        </a>
                    </td>
					<td class="rowLevel1">
                        <a href="javascript:void(0);" style="cursor:help;" onmouseover="return overlib('<webapps:stringescape><bean:write name="machine_package" property="url" filter="false" /></webapps:stringescape>', DELAY, '200', WIDTH, '150');" onmouseout="return nd();">
                        <logic:equal name="machine_package" property="content_type" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" align="absmiddle" />
                        </logic:equal>
                        <logic:equal name="machine_package" property="content_type" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" align="absmiddle" />
                        </logic:equal>
                        <bean:write name="machine_package" property="title" /></a>
                    </td>
                    <td class="rowLevel1">
                    <logic:equal name="machine_package" property="endPointState" value="">
                        <webapps:pageText type="global" key="null.uppercase"/>
                    </logic:equal>
                    <logic:notEqual name="machine_package" property="endPointState" value="">
                        <webapps:pageText type='global' shared="true" key='<%="compliance."+machine_package.getEndPointState()%>'/>
                    </logic:notEqual>
                    <!-- end point state-->
                    <td class="rowLevel1">
                        <logic:equal name="machine_package" property="complianceLevel" value="<%=ComplianceConstants.STR_LEVEL_COMPLIANT%>">
                            <span class="textGreen"><webapps:pageText type='global' key='<%=ComplianceConstants.STR_LEVEL_COMPLIANT%>'/></span>
                        </logic:equal>
                        <logic:equal name="machine_package" property="complianceLevel" value="<%=ComplianceConstants.STR_LEVEL_NON_COMPLIANT%>">
                            <span class="textRed"><webapps:pageText type='global' key='<%=ComplianceConstants.STR_LEVEL_NON_COMPLIANT%>'/></span>
                        </logic:equal>
                        <logic:equal name="machine_package" property="complianceLevel" value="<%=ComplianceConstants.STR_LEVEL_NOT_CHECK_IN%>">
                            <span class="textBlue"><webapps:pageText type='global' key='<%=ComplianceConstants.STR_LEVEL_NOT_CHECK_IN%>'/></span>
                        </logic:equal>
                    </td>
                    <td valign="top" class="rowLevel1">
                        <bean:define id="ID" name="machine_package" property="policy" toScope="request"/>
                        <bean:define id="Name" name="machine_package" property="policyName" toScope="request"/>
                        <bean:define id="Type" name="machine_package" property="policyTargetType" toScope="request"/>
                        <input type='hidden' id='<%="trgtid_target_sel_"+iteridx%>' name='<%="trgtid_target_sel_"+iteridx%>' value='<%=machine_package.getPolicyName()%>'/>
                        <input type='hidden' id='<%="trgttype_target_sel_"+iteridx%>' name='<%="trgttype_target_sel_"+iteridx%>' value='<%=machine_package.getPolicyTargetType()%>'/>
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
<div id="endOfGui"></div>
<script>
function setActiveBg(T,s) {
	T.style.backgroundColor = (s) ? '#FDFDFD' : '';
}
</script>
<script>
resizeDataSection('FOO_dataDiv','endOfGui');
</script>
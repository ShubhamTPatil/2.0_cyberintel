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
						    <td align="center" class="smallCaption" class="smallCaption" style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)" onClick="javascript:redirect('<%= "/initTargetView.do?src=tgtview&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode("/targetViewDispatcher.do?name=" + target.getName() + "&targetType=" + target.getType() + "&id=" + target.getId()) %>');"><img src="/spm/images/policy.gif" alt="View Policy" width="16" height="16" border="0"><br>
							<webapps:pageText shared="true" type="compliance_machine" key="ViewPolicy" /></td>
					</tr>
				</table>
                </logic:equal>
			</td>
            </logic:notPresent>
		</tr>
  </table>

	<div id="FOO_dataDiv" style="height:275px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');">
          <table cellspacing="0" cellpadding="0" width="100%" border="0">
               <thead>
               <tr class="headerSection">
                   <td class="tableHeaderCell" width="1%">&nbsp;</td>
                   <td class="tableHeaderCell" style="border-left-width:0px; " ><webapps:pageText shared="true" type="compliance_security_targets" key="assigned.policy" /> </td>
                   <td class="tableHeaderCell"><div id="compliance_type"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_targets" key="Compliance" /></a></div></td>
                   <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_targets" key="DirectlyAssignedTo" /></a></td>
               </tr>
              </thead>
        <bean:define id="resultsSize" value="<%=""+( ( List )request.getAttribute( "display_rs" ) ).size()%>" toScope="request"/>
        <logic:iterate id="policy" name="display_rs" type="com.marimba.apps.subscriptionmanager.beans.SecurityTargetDetailsBean" indexId="iteridx">
                <tbody id='<%="row1-"+iteridx%>'>
                    <logic:equal name="iteridx" value="0">
    				    <tr id="FOO_dataTable_firstRow">
    				</logic:equal>
    	       	      <% if(iteridx.intValue() != 0) { %>
    		       		<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
    		   	      <% } %>

                    <td class="rowLevel1">
                        <logic:notEqual name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_NOT_CHECK_IN%>">
                            <logic:equal name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_NOT_APPLICABLE%>">
                                &nbsp;
                            </logic:equal>
                            <logic:notEqual name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_NOT_APPLICABLE%>">
                                <logic:equal name="policy" property="xmlType" value="xccdf">
                                    <a href="#" onClick="window.parent.showProfileComplianceReport('<%=policy.getSelectedContentId()%>', 'xccdf');" >
                                        <img src="/shell/common-rsrc/images/info.gif" border="0"/></a>
                                </logic:equal>
                                <logic:equal name="policy" property="xmlType" value="oval">
                                    <a href="#" onClick="window.parent.showProfileComplianceReport('<%=policy.getSelectedContentId()%>', 'oval');" >
                                        <img src="/shell/common-rsrc/images/info.gif" border="0"/></a>
                                </logic:equal>
                            </logic:notEqual>
                        </logic:notEqual>
                        <logic:equal name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_NOT_CHECK_IN%>">
                            &nbsp;
                        </logic:equal>
                    </td>
                    <!--Package type title and url -->
                    <td class="rowLevel1">
                        <bean:write name="policy" property="selectedSecurityContentName"/>
                        <logic:notEqual name="policy" property="customTemplateName" value="">
                            (<bean:write name="policy" property="customTemplateName"/>)
                        </logic:notEqual>
                    </td>

                    <td class="rowLevel1">
                        <logic:equal name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_COMPLIANT%>">
                            <span class="textGreen"><webapps:pageText type='global' key='<%=ComplianceConstants.STR_LEVEL_COMPLIANT%>'/></span>
                        </logic:equal>
                        <logic:equal name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_NON_COMPLIANT%>">
                            <span class="textRed"><webapps:pageText type='global' key='<%=ComplianceConstants.STR_LEVEL_NON_COMPLIANT%>'/></span>
                        </logic:equal>
                        <logic:equal name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_NOT_CHECK_IN%>">
                            <span class="textBlue"><webapps:pageText type='global' key='<%=ComplianceConstants.STR_LEVEL_NOT_CHECK_IN%>'/></span>
                        </logic:equal>
                        <logic:equal name="policy" property="complaintLevel" value="<%=ComplianceConstants.STR_LEVEL_NOT_APPLICABLE%>">
                            <span class="textOrange"><webapps:pageText type='global' key='NOT-APPLICABLE'/></span>
                        </logic:equal>
                    </td>
                    <!-- target to which package is directly assigned to-->
                    <td class="rowLevel1">
                         <bean:define id="ID" name="policy" property="assignedToID" toScope="request"/>
						<bean:define id="Name" name="policy" property="assginedToName" toScope="request"/>
						<bean:define id="Type" name="policy" property="assginedToType" toScope="request"/>
						<jsp:include page="/includes/target_display_single.jsp"/>
                    </td>
                </tr>
    		</tbody>
    	</logic:iterate>
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
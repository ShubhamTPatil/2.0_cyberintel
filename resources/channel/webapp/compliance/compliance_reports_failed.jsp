<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.util.WebUtil"%>
<%@ include file="/includes/directives.jsp" %>

		<div class="tableWrapper" style="width:100%;">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
			    <tr valign="middle" class="smallButtons">
				    <td nowrap class="tableRowActions">
                        <input type='button' name='ShowQuery' value="<webapps:pageText key="ShowInvQry" type="button" shared="true" />" onclick='javascript:showQuery( "failed", "<webapps:stringescape><bean:write name="reportBean" property="calculationId" filter="false"/></webapps:stringescape>" )'>
				    </td>
	            </tr>
            </table>
		    <div class="headerSection" style="width:100%; text-align:left;" id="FOO_headerDiv">
		        <table border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
				    <colgroup width=""></colgroup>
					<colgroup width=""></colgroup>
					<colgroup width=""></colgroup>
					<colgroup width=""></colgroup>
					<thead>
					    <tr id="FOO_headerTable_firstRow">
						    <td class="tableHeaderActive" id="targetCol">
                                <a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports_failed" key="Machine"/> </a></td>
							<td class="tableHeaderCell" id="complianceCol">
                                <a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports_failed" key="FailedPackage"/> </a></td>
							<td class="tableHeaderCell">
                                <a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports_failed" key="EndpointState"/></a></td>
							<td class="tableHeaderCell">
                                <a class="columnHeading" href="#"><webapps:pageText shared="true" type="compliance_reports_failed" key="PolicyState"/></a></td>
                        </tr>
                    </thead>
				</table>
		    </div>
		    <div id="FOO_dataDiv" style="height:200px; width:100%; overflow:auto; text-align:left;" onscroll="syncScroll('FOO');">
			    <table border="0" cellpadding="0" cellspacing="0" id="FOO_dataTable">
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <colgroup width=""></colgroup>
                    <logic:iterate id="machine" name="display_rs" type="com.marimba.apps.subscriptionmanager.compliance.view.MachineBean" indexId="iteridx">
                        <tbody id='<%="row1-"+iteridx%>'>
                            <logic:equal name="iteridx" value="0">
                                <tr id="FOO_dataTable_firstRow">
                            </logic:equal>
	         			      <% if(iteridx.intValue() != 0) { %>	
				        		<tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
		         		      <% } %>
                                <td class="rowLevel1" style="border-top:0px;"><img src="/shell/common-rsrc/images/machine.gif" width="16" height="16" align="absmiddle"> <a href="#"><bean:write name="machine" property="machineName"/></a>
                                </td>
                                <td class="rowLevel1" style="border-top:0px;"><img src="/shell/common-rsrc/images/package.gif" width="16" height="16" align="absmiddle"> <bean:write name="machine" property="packageTitle"/></td>
                                <td class="rowLevel1" style="border-top:0px;">
                                <logic:equal name="machine" property="endpointState" value="">
                                    <webapps:pageText type="global" key="null.uppercase"/>
                                </logic:equal>
                                <logic:notEqual name="machine" property="endpointState" value="">
                                    <webapps:pageText type='global' shared="true" key='<%="compliance."+machine.getEndpointState()%>'/>
                                </logic:notEqual></td>
                                <td class="rowLevel1" style="border-top:0px;">
                                <logic:equal name="machine" property="policyState" value="">
                                    <webapps:pageText type="global" key="null.uppercase"/>
                                </logic:equal>
                                <logic:notEqual name="machine" property="policyState" value="">
                                    <webapps:pageText type='global' shared="true" key='<%=machine.getPolicyState()+".uppercase"%>'/>
                                </logic:notEqual></td>
                            </tr>
                        </tbody>
                    </logic:iterate>
                </table>
            </div>
            <!--end dataSection-->
        </div>
		<!--end tableWrapper-->
		<div id="pageNav">
		    <input name='done_button' type='button' class='mainBtn' accesskey='N' onClick='javascript:goReports()' value="<webapps:pageText shared="true" type="compliance_reports_failed" key="done"/>">
        </div>
    <div id="endOfGui"></div>
<script>
resizeDataSection('FOO_dataDiv','endOfGui', '-1' );
syncTables('FOO');
</script>
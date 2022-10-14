<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants,
                   com.marimba.apps.subscriptionmanager.compliance.util.WebUtil,
                   com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                   java.util.List,
                   com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants,
                   java.text.DateFormat,
                   org.apache.commons.lang.StringEscapeUtils" %>

<bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target"/>
<% int startIndex = 0; %>
<div id="FOO_mainContent" class="formContent" style="overflow:auto">

    <div class="sectionInfo"><webapps:pageText shared="true" type="compliance_security_targets" key="Info" /></div>
    <div class="tableWrapper" style="width:99%; overflow:hidden;">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr valign="middle" class="smallButtons">
                <logic:notPresent name="taskid">
                    <logic:equal name="<%=IWebAppConstants.NO_ACL_PERMISSION%>" value="false">
                        <td align="right" class="tableSearchRow">
                            <table border="0" cellspacing="0" cellpadding="0">
                                <tr>
                                    <td align="center" class="smallCaption" class="smallCaption" style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)" onClick="javascript:redirect('<%= "/initTargetView.do?src=tgtview&forward=" + com.marimba.tools.util.URLUTF8Encoder.encode("/targetViewDispatcher.do?name=" + target.getName() + "&targetType=" + target.getType() + "&id=" + target.getId()) %>');"><img src="/spm/images/policy.gif" alt="View Policy" width="16" height="16" border="0"><br>
                                        <webapps:pageText shared="true" type="compliance_machine" key="ViewPolicy" /></td>
                                </tr>
                            </table>
                        </td>
                    </logic:equal>
                </logic:notPresent>
            </tr>
        </table>
        <div id="FOO_dataDiv" style="height:275px; width:100%; overflow:auto;" onscroll="syncScroll('FOO');">
            <table cellspacing="0" cellpadding="0" width="100%" border="0">
                <thead>
                    <tr class="headerSection">
                        <td class="tableHeaderCell" style="border-left-width:0px; " ><webapps:pageText shared="true" type="compliance_security_targets" key="assigned.policy" /> </td>
                        <td class="tableHeaderCell"> <webapps:pageText shared="true" type="compliance_targets" key="Compliance"/>
                            <!--<div id="compliance_type">-->
                                <!--<a class="columnHeading" href="#">-->
                                    <!--<webapps:pageText shared="true" type="compliance_targets" key="Compliance" />-->
                                <!--</a>&nbsp;&nbsp;&nbsp;-->
                                <!--<img src="/spm/images/show_percent_sel.gif" width="16" height="16" align="absmiddle">&nbsp;<a href="javascript:shiftDisplay( 'numbers' );" target="_self"><img src="/spm/images/show_numbers.gif" width="16" height="16" border="0" align="absmiddle"></a>-->
                            <!--</div>-->
                        </td>
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

                            <!--Package type title and url -->
                            <td class="rowLevel1">
                                <bean:write name="policy" property="selectedSecurityContentName"/>
                                <logic:notEqual name="policy" property="customTemplateName" value="">
                                    (<bean:write name="policy" property="customTemplateName"/>)
                                </logic:notEqual>
                            </td>

                            <td class="rowLevel1">
                                <div id='<%="comp_target_sel_"+iteridx%>' name='<%="comp_target_sel_"+iteridx%>'>
                                    <script type="text/javascript">
                                        document.write(getComplianceReport('<%="target_sel_"+iteridx%>', <bean:write name="policy" property="compliantCount"/> , <bean:write name="policy" property="nonCompliantCount"/>, <bean:write name="policy" property="checkinCount"/>, <bean:write name="policy" property="notApplicableCount"/>,<%=ComplianceConstants.STATE_DONE%>, 'test'));
                                    </script>
                                </div>
                            </td>
                            <!-- target to which package is directly assigned to-->
                            <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
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

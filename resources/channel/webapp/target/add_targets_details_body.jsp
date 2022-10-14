<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@version  $Revision$,  $Date$
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import = "java.util.*,
                   com.marimba.apps.subscription.common.objects.Channel,
                   com.marimba.apps.subscriptionmanager.webapp.actions.PersistifyChecksAction,
                   com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<%-- Apply the persisted targets to the form. --%>
<bean:define id="pageBeanName" name="pageBeanName" toScope="request"  />

<% int startIndex = (pageContext.findAttribute((String)pageBeanName)!=null?((GenericPagingBean) pageContext.findAttribute((String)pageBeanName)).getStartIndex():0); %>

<sm:setPersistedRecords selectedTargetsVarName="session_add_pagepkgs_selected" formName="addTargetEditForm"
                        pagingBeanName="session_add_pagepkgs_bean"/>

<div class="headerSection" style="width:100%; overflow:hidden; text-align:left;" id="FOO_headerDiv">
    <table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable" width="100%">
        <colgroup width="4%"/>
        <colgroup width="3%"/>
        <colgroup width="52%"/>
        <colgroup width="20%"/>
        <colgroup width="20%"/>

        <thead>
            <tr id="FOO_headerTable_firstRow">
                <td align="center" class="tableHeaderCell" id="checkboxColumn" style="text-align: center;">
                    <html:hidden property="value(clear_all)"/>
                    <html:checkbox property="value(add_pagepkgs_item_all)" value="true" styleId="add_pagepkgs_item_all" onclick="setClearAllFromHeader(); checkboxToggle('add_pagepkgs_item'); setCheckAllFromHeader()"/>
                </td>
                <td align="center" class="tableHeaderCell"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="11" height="1"></td>
                <!-- Targets Column -->
                <td class="tableHeaderCell" id="targetName">
                    <webapps:pageText key="targets" type="colhdr" shared="true"/>
                </td>
                <!--Primary State header column-->
                <td class="tableHeaderCell">
                    <webapps:pageText key="priState" type="colhdr" shared="true"/>
                </td>

                <!--Secondary State header column-->
                <td class="tableHeaderCell">
                    <webapps:pageText key="secState" type="colhdr" shared="true"/>
                </td>

            </tr>
        </thead>
    </table>
</div>

<script language="JavaScript">
    <%
                     ArrayList list = (ArrayList)session.getAttribute("add_remove_package");
                     PersistifyChecksAction.SelectedRecords selectedChannels =
                          (PersistifyChecksAction.SelectedRecords)session.getAttribute("session_add_pagepkgs_selected");
                     int noOfChannelMap=0;
                     if(list != null && (noOfChannelMap=list.size()) > 0) {
                         int chkBoxCountInRemPages;
                         List channelsCurrPage = (List)request.getAttribute("display_rs");
                         chkBoxCountInRemPages = noOfChannelMap - channelsCurrPage.size();
                         out.println("var chkBoxCountInRemPages=" + chkBoxCountInRemPages);
                         int selectionCount = 0;
                         if(selectedChannels != null && (selectionCount=selectedChannels.getSelectionCount()) > 0) {
                             out.println("var selectionCount=" + selectionCount);
                             out.println("buttonEnable(\"remove_button\",false);");
                             out.println("buttonEnable(\"set_schedule_button\",false);");
                         }
                     }
                     %>
</script>

<div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto; text-align:left;" onscroll="syncScroll('FOO');" onresize="resizeDataSection('FOO_dataDiv','endOfGui',-1);">
<table cellpadding="0" cellspacing="0" border="0" id="FOO_dataTable" width="100%">
<colgroup width="4%"/>
<colgroup width="3%"/>
<colgroup width="52%"/>
<colgroup width="20%"/>
<colgroup width="20%"/>

<logic:present name="add_remove_package">
<% int contentsRowCount = 0; %>
<logic:iterate id="target" name="display_rs" type="com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap" indexId="iteridx">
<% iteridx = new Integer(startIndex + iteridx.intValue()); %>
<bean:define id="index" value='<%= iteridx.toString() %>' />

<tbody id='<%= "row1-" + index %>'>
    <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
        <!-- Checkbox column -->
        <td align="center" class="rowLevel1"><html:checkbox  property='<%="value(add_pagepkgs_item_" +  index + ")"%>'  value="true" styleId='<%="add_pagepkgs_item_" + index %>'  onclick="processCheckbox(this.id); setClearAllFromBody()" /></td>
        <td align="center" class="rowLevel1"><a href="javascript:toggleSection('<%= "row1-" + index %>')"><img border="0" id='<%= "widget-row1-" + index %>' src="/shell/common-rsrc/images/list_arrow_c.gif" width="11" height="11" class="widget"></a> </td>

        <!--Targets column-->
        <td class="rowLevel1">
            <bean:define id="ID" name="target" property="id" toScope="request"/>
            <bean:define id="Name" name="target" property="name" toScope="request"/>
            <bean:define id="Type" name="target" property="type" toScope="request"/>
            <jsp:include page="/includes/target_display_single.jsp"/>
        </td>

        <!-- Primary State -->
        <td class="rowLevel1">
            <% String initsecval = target.getState();
                if (initsecval !=null) {
                    pageContext.setAttribute("initSecStateValue", initsecval);
                }
            %>
            <html:select property='<%= "value(state#" + target.hashCode() +")" %>' value='<%= target.getState() %>' onchange='<%= "javascript:saveState('/target/add_targets_details_area.jsp');" %>' styleId='<%= "state#" + target.hashCode() %>'> <html:options property="states" labelProperty="statesLabel" /> </html:select>
        </td>

        <!--Secondary State-->
        <td class="rowLevel1">
            <html:select property='<%= "value(secState#" + target.hashCode() +")" %>' value='<%= target.getSecState() %>' onchange=" javascript:saveState('/target/add_targets_details_area.jsp');" styleId='<%= "secState#" + target.hashCode() %>' > <html:option value="">&nbsp;</html:option> <html:options property="states" labelProperty="statesLabel" /> </html:select>
        </td>
    </tr>
</tbody>

<tbody id='<%= "row1-" + index + "_1"%>' style="display:none;">
<tr>
<td class="rowLevel1" colspan="5">
<table width="100%" border="0" cellspacing="0" cellpadding="3">
    <colgroup span="2" width="50%"/>
    <tr>
        <td valign="top">
            <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
                <tr>
                    <td class="tableRowActions" style="height:20px;">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td class="textWhite"><strong> <webapps:pageText key="priStateTitle"/> </strong> </td>
                                <td align="right"><input name="Submit3" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveSchedule('/addTargetSchedEdit.do?schedType=initial&target=<%= index %>');" > </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td valign="top">
                        <logic:present name="target" property="initSchedule">
                            <logic:equal name="target" property="initSchedule" value="inconsistent">
                                <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16"></p>
                            </logic:equal>
                        </logic:present>
                        <sm:scheduleDisplay name="target" property="initSchedule" schedule='<%= target.getInitSchedule() %>' type="initial" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                        <p>&nbsp;</p>
                    </td>
                </tr>
            </table>
        </td>
        <td valign="top">
            <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
                <tr>
                    <td class="tableRowActions" style="height:20px;">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td height="21" class="textWhite"><strong> <webapps:pageText key="updateTitle"/> </strong> </td>
                                <td align="right"><input name="Submit33" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveSchedule('/addTargetSchedEdit.do?schedType=update&target=<%= index %>');" > </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td valign="top"><logic:present name="target" property="updateSchedule"> <logic:equal name="target" property="updateSchedule" value="inconsistent">
                        <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                            </logic:equal> </logic:present>
                                <sm:scheduleDisplay name="target" property="updateSchedule" schedule='<%= target.getUpdateSchedule()%>' type="update" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                        <p>&nbsp;</p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td valign="top">
            <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
                <tr>
                    <td class="tableRowActions" style="height:20px;">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td class="textWhite"><strong> <webapps:pageText key="secStateTitle"/> </strong> </td>
                                <td align="right"><input name="Submit342" type="button" id="secStateBtn#<%=target.hashCode()%>" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveSchedule('/addTargetSchedEdit.do?schedType=secondary&target=<%= index %>');" > </td>
                            </tr>
                            <script>
                                stateChange(<%=target.hashCode() %>, '<%=org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(target.getSecState()) %>', true);
                            </script>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td valign="top"><logic:present name="target" property="secSchedule"> <logic:equal name="target" property="secSchedule" value="inconsistent">
                        <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                            </logic:equal> </logic:present>
                                <sm:scheduleDisplay name="target" property="secSchedule" schedule='<%= target.getSecSchedule()%>' type="secondary" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                        <p>&nbsp;</p>
                    </td>
                </tr>
            </table>
        </td>
        <td valign="top">
            <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
                <tr>
                    <td class="tableRowActions" style="height:20px;">
                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td class="textWhite"><strong> <webapps:pageText key="verifyTitle"/> </strong> </td>
                                <td align="right"><input name="Submit35" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveSchedule('/addTargetSchedEdit.do?schedType=verrepair&target=<%= index %>');" > </td>
                            </tr>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td valign="top"><logic:present name="target" property="verRepairSchedule"> <logic:equal name="target" property="verRepairSchedule" value="inconsistent">
                        <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                            </logic:equal> </logic:present>
                                <sm:scheduleDisplay name="target" property="verRepairSchedule" schedule='<%= target.getVerRepairSchedule()%>' type="verrepair" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                        <p>&nbsp;</p>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</td>
</tr>
</tbody>
<% contentsRowCount++;%>
</logic:iterate>
</logic:present>
</table>
</div>


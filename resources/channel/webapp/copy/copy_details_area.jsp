<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants"%>
 <%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)copy_details_area.jsp

     @author Jayaprakash Paramasivam
     @version 1.0, 31/12/2004     
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="pol_copy" />
<%@ include file="/includes/endHeadSection.jsp" %>
<!-- String declared for getting constant-->
<%
    String rhsList = IWebAppConstants.COPY_RHS_LIST;
%>
<script language="JavaScript">
    var singleOptionElements = new Array()
    var multiOptionElements = new Array("remove_btn")
</script>
<%@ include file="/includes/body.html" %>
<html:form styleId="copyEditForm" action="/copyAdd.do" target="_top" >
<div id="pageContent">
<%-- Error handling for the user to select unique target.
--%>
    <table width="98%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>

    <Table cellpadding="0" cellspacing="0" width="98%">
        <TR>
            <TD valign=top>
                <TABLE cellSpacing=0 cellPadding=0 border=0>
                    <TBODY>
                        <TR>
                            <TD class=tableTitle><webapps:pageText key="SubTitle"/></TD>
		                </TR>
		            </TBODY>
	            </TABLE>
                <DIV class=tableWrapper style="height:260px;">
                    <TABLE cellSpacing=0 cellPadding=0 width="100%" border=0>
                        <TBODY>
                            <TR class=smallButtons vAlign=center>
                                <TD class=tableRowActions>
				                    <input type="button" disabled="true" id="remove_btn" value="<webapps:pageText key="remove" type="button" shared="true" /> " onclick="javascript:submitActionFromFrames(document.forms.copyEditForm, '/copyAdd.do?action=remove','2');" >
			                    </TD>
                                <TD class=tableRowActions align=right>&nbsp;</TD>
		                    </TR>
		                </TBODY>
		            </TABLE>
                <DIV class=headerSection style="width:100%;">
                    <TABLE cellSpacing=0 cellPadding=0 width="100%" border=0>
                        <COLGROUP width=0*></COLGROUP>
                        <COLGROUP width="100%"></COLGROUP>
                        <THEAD>
                            <TR>
                                <TD class=tableHeaderCell>
                                    <input type="checkbox" name="copyresult_line_item_all" value="checkbox" id="copyresult_line_item_all" onClick="checkboxToggle('copyresult_line_item')">
			                    </TD>
                                <TD class=tableHeaderCell><webapps:pageText key="targetName"/></TD>
		                    </TR>
		                </THEAD>
                    </TABLE>
	            </DIV><!--end headerSection-->
                <DIV id=dataSection style="OVERFLOW: auto; WIDTH: 100%; HEIGHT: 260px">
                    <TABLE cellSpacing=0 cellPadding=0 width="100%">
                        <COLGROUP width=0*></COLGROUP>
                        <COLGROUP width="100%"></COLGROUP>
                        <TBODY>
                            <TR>
			                    <TD class=rowLevel1 style="BORDER-TOP: 0px">
                                    <logic:present name='<%= rhsList %>'>
                                        <logic:iterate id="app" name='<%= rhsList %>' indexId="iteridx" type="com.marimba.webapps.tools.util.PropsBean" scope="session">
                                        <tbody id='<%="row1-" + iteridx.intValue() %>'>
                                            <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                                                <td class="rowLevel1" >
                                                    <html:checkbox  property='<%="value(copyresult_line_item_" + iteridx.intValue() +")" %>'  value="checkbox" styleId='<%="copyresult_line_item_" + iteridx.intValue() %>'  onclick="processCheckbox(this.id)">
                                                    </html:checkbox>
                                                </td>
                                                <td class="rowLevel1">
                                                    <logic:equal name="app" property="value(policy)" value="true" >
                                                        <a href="javascript:void(0);" style="cursor:help;" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText key="Warning" escape="js"/>', WIDTH, '50', DELAY, '200',ABOVE,LEFT);" onmouseout="return nd();" >
                                                            <img  src='/shell/common-rsrc/images/alert_sm.gif' width="16" height="16" border="0">
                                                        </a>
                                                    </logic:equal>
                                                    <logic:equal name="app" property="value(objectclass)" value="machinegroup" >
                                                        <img  src='/shell/common-rsrc/images/user_group.gif' width="16" height="16">
                                                    </logic:equal>
                                                    <logic:notEqual name="app" property="value(objectclass)" value="machinegroup" >
	                                                    <logic:equal name="app" property="value(objectclass)" value='<%=ISubscriptionConstants.TYPE_SITE%>' >
	                                                        <img  src='<%= "/shell/common-rsrc/images/" + "network.gif" %>' width="16" height="16">
	                                                    </logic:equal>
	                                                    <logic:notEqual name="app" property="value(objectclass)" value='<%=ISubscriptionConstants.TYPE_SITE%>' >
	                                                        <img  src='<%= "/shell/common-rsrc/images/" + (String) app.getValue("objectclass") + ".gif" %>' width="16" height="16">
	                                                    </logic:notEqual>
                                                    </logic:notEqual>
                                                    
                                                    <a href="javascript:void(0);" style="cursor:help;" class="noUnderlineLink" onmouseover="return overlib('<webapps:stringescape><bean:write name="app" property="value(dn)" filter="false" /></webapps:stringescape>', WIDTH, '50', DELAY, '200',ABOVE,LEFT);" onmouseout="return nd();" ><bean:write name="app" property="value(displayname)" filter="true" /></a>
                                                </td>
                                            </tr>
		                                </tbody>
                                        </logic:iterate>
                                  </logic:present>
                                </TD>
			                </TR>
		                </TBODY>
		            </TABLE>
	            </DIV>
	            </DIV>
            </TD>
        </TR>
    </TABLE>
</html:form>
<%@ include file="/includes/footer.jsp" %>
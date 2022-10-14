<%--
    Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

     @author Angela Saval
     @author Theen-Theen Tan
     @version $Revision$, $Date$
--%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<% //pageBeanName = "target_pkgs_bean"; %>
<bean:define id="pageStateAction" name="pageStateAction" toScope="request" />
<bean:define id="pageBeanName" name="pageBeanName" scope="request" />
<bean:define id="tgForm" name="tgForm" toScope="request" />
<%@ page import = "com.marimba.apps.subscription.common.objects.Channel,
                   com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>
<%
    try{
%>

<% int startIndex = ((GenericPagingBean) pageContext.findAttribute((String)pageBeanName)).getStartIndex(); %>

<%String tgsched =(String)pageContext.findAttribute("targetsched");%>
<%//String multitgbool =(String)pageContext.findAttribute("session_multitgbool");%>

<div class="tableWrapper" style="width:99%;">

<%@include file="/includes/target_details_tbhead.jsp" %>

<%-- iterate through the page of packages to be displayed --%>
<div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto; text-align:left;" onscroll="syncScroll('FOO');" onresize="resizeDataSection('FOO_dataDiv','endOfGui',-1);">
<table cellpadding="0" cellspacing="0" border="0" id="FOO_dataTable">

<%-- Column group definitions depending on basic or details view --%>
<%-- basic view --%>
<logic:notPresent name="targetsched">
    <%-- Single mode --%>
    <logic:notPresent name="session_multitgbool">
        <colgroup width="10%"/>
        <colgroup width="30%"/>
        <colgroup width="20%"/>
        <colgroup width="20%"/>
        <colgroup width="20%"/>
    </logic:notPresent>
    <%-- Multiple mode --%>
    <logic:present name="session_multitgbool">
        <colgroup width="10%"/>
        <colgroup width="40%"/>
        <colgroup width="25%"/>
        <colgroup width="25%"/>
    </logic:present>
</logic:notPresent>

<%-- details view --%>
<logic:present name="targetsched">
    <%-- Single mode --%>
    <logic:notPresent name="session_multitgbool">
        <colgroup width="10%"/>
        <colgroup width="20%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>
    </logic:notPresent>
    <%-- Multiple mode --%>
    <logic:present name="session_multitgbool">
        <colgroup width="5%"/>
        <colgroup width="20%"/>
        <colgroup width="15%"/>
        <colgroup width="15%"/>
        <colgroup width="15%"/>
        <colgroup width="15%"/>
        <colgroup width="15%"/>
    </logic:present>
</logic:present>
<%-- iterate through the page of packages to be displayed --%>
<logic:present name="display_rs">
<% int folderContentsRowCount = 0; %>
<logic:iterate id="app" name="display_rs" indexId="iteridx" type="com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap">
<%-- keep track if this list is empty or not. --%>
<tbody id="row1-1">
<% if (folderContentsRowCount == 0){ %>
<tr id="FOO_dataTable_firstRow">
        <% } else if (folderContentsRowCount % 2 == 0){ %>
<tr>
        <% } else { %>
<tr class="alternateRowColor">
<% } %>


    <%-- Column for showing content type  --%>
<td class="rowLevel1" align="center">
    <logic:equal name="app" property="<%="value("+ ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE +")" %>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
        <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
    </logic:equal>
    <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE+")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
        <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
    </logic:equal>
</td>

    <%-- Column for showing the packages  --%>
<logic:present name="targetsched">
    <td class="rowLevel1">
        <% String mouseOverStr = "";%>
        <% mouseOverStr = "<b>Url:</b>" + (String) app.getValue("url");
            String subContainer = (String) app.getValue("tgmap_subcontainer");
            if ( subContainer != null &&  subContainer.length()> 0 ) {
                mouseOverStr += "<br><b>Container:</b>" + subContainer;
            }
        %>
        <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE+")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
        <a target="_blank" href="/spm/getPatches.do?patchGroupUrl=<%=com.marimba.tools.util.URLUTF8Encoder.encode(app.getUrl())%>&title=<%=com.marimba.tools.util.URLUTF8Encoder.encode(app.getTitle()) %>" onmouseover="return Tip(wrapDN('<webapps:stringescape><%= mouseOverStr %></webapps:stringescape>', 100), WIDTH, '-1',BGCOLOR, '#F5F5F2',FONTCOLOR, '#000000',BORDERCOLOR, '#333300',OFFSETY, 20,FADEIN, 100);" onmouseout="return UnTip();">
            </logic:equal>

            <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE+")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
            <a href="#" class="noUnderlineLink" style="cursor:help" onmouseover="return Tip(wrapDN('<%=mouseOverStr%>', 100), WIDTH, '-1',BGCOLOR, '#F5F5F2',FONTCOLOR, '#000000',BORDERCOLOR, '#333300',OFFSETY, 20,FADEIN, 100);" onmouseout="return UnTip();">
                </logic:equal>
                <bean:write name="app" property="title" /></a>
    </td>
</logic:present>

    <%-- added by kumaravel --%>
<logic:notPresent name="targetsched">
    <td class="rowLevel1">
        <% String mouseOverStr = "";%>
        <% mouseOverStr = "<b>Url:</b>" + (String) app.getValue("url");
            String subContainer = (String) app.getValue("tgmap_subcontainer");
            if ( subContainer != null &&  subContainer.length()> 0 ) {
                mouseOverStr += "<br><b>Container:</b>" + subContainer;
            }
        %>

        <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE+")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
        <a target="_blank" href="/spm/getPatches.do?patchGroupUrl=<%=com.marimba.tools.util.URLUTF8Encoder.encode(app.getUrl())%>&title=<%=com.marimba.tools.util.URLUTF8Encoder.encode(app.getTitle()) %>" onmouseover="return Tip(wrapDN('<webapps:stringescape><%= mouseOverStr %></webapps:stringescape>', 100), WIDTH, '-1',BGCOLOR, '#F5F5F2',FONTCOLOR, '#000000',BORDERCOLOR, '#333300',OFFSETY, 20,FADEIN, 100);" onmouseout="return UnTip();">
            </logic:equal>
            <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE+")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
            <a href="#" class="noUnderlineLink" onmouseover="return Tip(wrapDN('<%=org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(mouseOverStr)%>', 100), WIDTH, '-1',BGCOLOR, '#F5F5F2',FONTCOLOR, '#000000',BORDERCOLOR, '#333300',OFFSETY, 20,FADEIN, 100);" onmouseout="return UnTip();">
                </logic:equal>
                <bean:write name="app" property="title" /></a>
    </td>
</logic:notPresent>

    <%-- Column for showing the installation priority. It only shows up on the target schedule details page --%>
<logic:present name="targetsched">
    <logic:notPresent name="session_multitgbool">
        <td class="rowLevel1">
                <%-- do not display '99999' --%>
            <logic:lessThan name="app" property="value(order)" value="<%= String.valueOf(com.marimba.apps.subscription.common.ISubscriptionConstants.ORDER) %>">
                <bean:write name="app" property="order" />
            </logic:lessThan>
            <logic:equal name="app" property="value(order)" value="<%= String.valueOf(com.marimba.apps.subscription.common.ISubscriptionConstants.ORDER) %>">
                <webapps:pageText shared="true" type="target_details" key="NotSet" />
            </logic:equal>
        </td>
    </logic:notPresent>
</logic:present>

    <%-- Primary state --%>
<td valign="top" class="rowLevel1">
    <logic:present name="app" property="value(state)">
    <logic:equal name="app" property="value(state)" value="inconsistent">
        <span class="textInactive">
            </logic:equal>
            </logic:present>

                <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE+")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                    <p><webapps:pageText key='<%= ((TargetChannelMap)app).getState() + ".patch.uppercase" %>' type="global" /></p>
                </logic:equal>
                <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE +")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                    <p><webapps:pageText key='<%= ((TargetChannelMap)app).getState() + ".uppercase" %>' type="global" /></p>
                </logic:equal>

                <logic:present name="app" property="value(state)">
                <logic:equal name="app" property="value(state)" value="inconsistent">
        </span>
    </logic:equal>
    </logic:present>

    <logic:present name="targetsched">
        <sm:scheduleDisplay name="app" property="initSchedule" format="short" type="init" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </logic:present>
</td>

    <%-- Secondary state --%>
<td valign="top" class="rowLevel1">
    <logic:present name="app" property="secState">
        <logic:equal name="app" property="secState" value="inconsistent">
            <span class="textInactive">
        </logic:equal>
        <logic:equal name="app" property="<%="value("+ISubscriptionConstants.TARGETCHANNELMAP_CONTENTTYPE +")"%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
            <p><webapps:pageText key='<%= ((TargetChannelMap)app).getSecState() + ".uppercase" %>' type="global" />&nbsp;</p>
        </logic:equal>
        <logic:equal name="app" property="value(secState)" value="inconsistent">
            </span>
        </logic:equal>
    </logic:present>
    <logic:notPresent name="app" property="value(secState)">
        <span class="textInactive"><webapps:pageText shared="true" type="target_details" key="NA" /></span>
    </logic:notPresent>
    <logic:present name="targetsched">
        <sm:scheduleDisplay name="app" property="secSchedule" format="short" type="sec" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </logic:present>
</td>


<logic:present name="targetsched">
    <%-- Update schedule.  Only shown on details page --%>
    <td valign="top" class="rowLevel1">
        <sm:scheduleDisplay name="app" property="updateSchedule" format="short" type="update" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </td>

    <%-- Verfy/Repair schedule.  Only shown on details page --%>
    <td valign="top" class="rowLevel1">
        <sm:scheduleDisplay name="app" property="verRepairSchedule" format="short" type="verrepair" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </td>

    <%-- Postpone schedule --%>
    <td valign="top" class="rowLevel1">
        <sm:scheduleDisplay name="app" property="postponeSchedule" format="short" type="postpone" activeFont="textGeneral" inactiveFont="textInactive" doubleSpace="true" />
    </td>
</logic:present> <%-- End of the target sched present check for the extra columns --%>
<logic:notPresent name="session_multitgbool">
    <td valign="top" class="rowLevel1">
        <% //String tgLabel="app"; %>
        <bean:define id="ID" name="app" property="id" toScope="request"/>
        <bean:define id="Name" name="app" property="name" toScope="request"/>
        <bean:define id="Type" name="app" property="type" toScope="request"/>
        <jsp:include page="/includes/target_display_single.jsp"/>
    </td>
</logic:notPresent>

</tr>
</tbody>
<% folderContentsRowCount++;%>
</logic:iterate>

<%-- Empty list message --%>
<logic:notPresent name="hasPackages" scope="session">
    <tr>
        <logic:notPresent name="targetsched">
            <td colspan="7">
                <span class="textInactive"><webapps:pageText shared="true" type="target_details" key="emptyList" /></span>
            </td>
        </logic:notPresent>
        <logic:present name="targetsched">
            <td colspan="7">
                <span class="textInactive"><webapps:pageText shared="true" type="target_details" key="emptyList" /></span>
            </td>
        </logic:present>
    </tr>
</logic:notPresent>

</logic:present> <%-- end display_rs --%>

</table>
</div><!--end dataSection-->
</div> <!-- end tableWrapper -->

<%
    } catch(Exception ex) { out.println(ex.toString()); }
%>

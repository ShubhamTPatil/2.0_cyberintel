<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Jayaprakash Paramasivam
	@version  $Revision$,  $Date$
	@since    31/12/2004
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.ArrayList, com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%@ include file="/includes/directives.jsp" %>
<%@ page errorPage="/includes/internalerror.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="spm" topic="pol_copy" />

<title><webapps:pageText key="m6" type="global"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">

<script language="javascript" src="/shell/common-rsrc/js/master.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/domMenu.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/domMenu_items2.js"></script>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
    session.setAttribute("disablemultimode", "true");
    session.removeAttribute("session_multitgbool");
%>
<script>
	function sendredirect(form, action) {
	    var fullpath = "<html:rewrite page='" + action + "' />";
		top.location.href = fullpath;
	}
</script>
<%-- Body content --%>
<html:form name="copyEditForm" action="/copySaveTarget.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.CopyEditForm" onsubmit="return false">
    <% if(null != EmpirumContext) {%>
    <webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
    <% } else { %>
    <webapps:tabs tabset="main" tab="tgtview"/>
    <% } %>

    <div id="pageContent" width="98%">
        <DIV class="pageHeader"><SPAN class=title><webapps:pageText key="Title" /></SPAN></DIV>
        <DIV class="pageInfo">
            <TABLE cellSpacing=0 cellPadding=2 width="98%">
                <TBODY>
                    <TR>
                        <TD vAlign=top width=0*><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></TD>
                        <TD width="100%"><webapps:pageText key="IntroShort"/></TD>
                    </TR>
                </TBODY>
            </TABLE>
        </DIV>
        <TABLE cellSpacing=0 cellPadding=0 border="0" width="98%">
            <tr>
                <td colspan="3" valign="top">
                    <div class="itemStatus">
                        <table cellspacing="0" cellpadding="3" border="0">
                            <tr>
                                <td valign="top"><webapps:pageText key="copyfrom" type="colhdr" shared="true"/>: </td>
                                <logic:iterate id="target" name="session_dist" property="targets">
                                    <td align="left">
                                        <bean:define id="ID" name="target" property="id" toScope="request"/>
                                        <bean:define id="Name" name="target" property="name" toScope="request"/>
                                        <bean:define id="Type" name="target" property="type" toScope="request"/>
                                        <jsp:include page="/includes/target_display_single.jsp"/>
                                    </td>
                                </logic:iterate>
                            </tr>
                        </table>
                    </div>
                </td>
            </tr>
            <tr>
                <td valign="top" width="322px">
                    <div id="FOO_GROUP_LISTING_iframe" style="height:360px; border:0px; margin:0px; overflow:auto;display:block;">
                        <iframe src="<webapps:fullPath path="/ldapRemember.do?selectedTab=true&isTargetView=false" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfGroupList"></div>
                </td>
                <td width="30" align="center" valign="middle">
                    <img src="/spm/images/right_arrow_indicator.gif" width="50" height="60">
                </td>
                <td valign="top" style="padding-left:10px">
                    <div id="FOO_dataDiv" style="height:360px; border:0px;padding:0px;margin:0px;display:block; overflow:auto;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/copy/copy_details_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfData"></div>
                </td>
            </tr>
            <tr>
                <td colspan="3">
                    <DIV id="pageNav">
                        <input type="button" class="mainBtn" id="preview" name="preview" value=" <webapps:pageText key="save" type="button" shared="true" /> " onclick="javascript:sendredirect(document.copyEditForm, '/copyAdd.do?action=save');">&nbsp;
                        <input type="button" name="cancel" value=" <webapps:pageText key="cancel" type="button" shared="true" /> " onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/copyCancel.do');" >
                    </DIV>
                </td>
            </tr>
        </TABLE>
    </div>
</html:form>
<%@ include file="/includes/footer.jsp" %>


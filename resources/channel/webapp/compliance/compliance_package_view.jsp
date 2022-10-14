<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
	<webapps:helpContext context="sm" topic="pc_package_view" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%@ include file="/includes/info.jsp" %>
<!--%@ include file="/includes/banner.jsp" %-->
<logic:present name="taskid">
    <% request.setAttribute("nomenu", "true"); %>
    <logic:equal name="arViewType" value="bothview">
        <webapps:tabs tabset="compViewAR" tab="compPkgViewAR"/>
    </logic:equal>
    <logic:equal name="arViewType" value="packageview">
        <webapps:tabs tabset="compPkgViewAR" tab="compPkgViewAR"/>
    </logic:equal>
</logic:present>
<logic:notPresent name="taskid">
    <% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="pcpkgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="pcpkgview"/>
<% } %>
</logic:notPresent>
<script>
CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','FOO_endOfData',0);");
CMSOnResizeHandler.addHandler("resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);");
</script>

<div align="center" style="padding-left:15px; padding-right:15px; ">
    <div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
    <logic:present name="taskid">
        <div class="pageHeader">
            <span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span>
            <bean:write name="taskid" />
        </div>
    </logic:present>
    <%-- Errors Display --%>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>

    <%@ include file="/includes/help.jsp" %>
    <table cellpadding="0" cellspacing="0" border="0" width="100%">
        <tr>
            <td valign="top" width="322px">
                <logic:present name="taskid" >
                    <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0px; margin:0px; padding:0px; overflow:auto; display:block;">
                        <iframe src="<webapps:fullPath path="/compliance/compliance_package_nav.jsp" />" width="100%" height="100%" frameborder="0" hspace="0" marginheight="0" marginwidth="0" style="padding:0px; margin:0px; border:0px;"></iframe>
                    </div>
                    <div id="FOO_endOfGroupList"></div>
                </logic:present>
                <logic:notPresent name="taskid">
                    <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0px; margin:0px; padding:0px; overflow:auto; display:block;">
                        <iframe src="<webapps:fullPath path="/compliance/package_nav.jsp" />" width="100%" height="100%" frameborder="0" hspace="0" marginheight="0" marginwidth="0" style="padding:0px; margin:0px; border:0px;"></iframe>
                    </div>
                    <div id="FOO_endOfGroupList"></div>
                </logic:notPresent>
            </td>
            <td valign="top" style="padding-left:15px;" align="left">
                <div id="FOO_dataDiv" style="height:100px; border:0px; margin:0px; padding:0px; overflow:auto; display:block;">
                    <iframe name="right-frame" src="<webapps:fullPath path="/compliance/package_compliance.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                </div>
                <div id="FOO_endOfData"></div>
            </td>
        </tr>
    </table>
</div>

<script>
    resizeDataSection('FOO_dataDiv','FOO_endOfData');
    resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList');
</script>

</body>
</html>

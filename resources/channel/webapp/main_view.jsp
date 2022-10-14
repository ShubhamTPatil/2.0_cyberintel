<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<%@ include file="/includes/startHeadSection.jsp" %>

<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/application.js"></script>
<script type="text/javascript" src="/spm/includes/easyui/jquery.easyui.min.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/icon.css">
<style type="text/css">
    .slider-arrow {
        background: #d9dada none repeat scroll 0 0; float: left;
        font-size: 25px; position: fixed; margin-left: -2px;
    }
</style>
<webapps:helpContext context="sm" topic="targ_vw" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/advanced_policy_search.jsp"%>
<%@ include file="/includes/info.jsp" %>
<%@ include file="/includes/MultiTargets.jsp" %>
<% session.removeAttribute("disablemultimode"); %>


<script type="text/javascript">
    var formObject;
    function sendURL(searchQuery, queryValue) {
        if (null != formObject) {
            formObject.elements["<%=ISubscriptionConstants.VALUE_OF_SEARCHQUERY%>"].value = searchQuery;
            formObject.elements["<%=ISubscriptionConstants.POLICY_CRITERIA%>"].value = queryValue;
            formObject.action.value = "<html:rewrite page='/ldapSearch.do?basicLink=policy'/>";
            formObject.submit();
        }
    }

    function clearQuery(){
        if(null != formObject) {
            formObject.elements["<%=ISubscriptionConstants.POLICY_CRITERIA%>"].value ="";
        }
    }
</script>
<body>

<webapps:tabs tabset="main" tab="tgtview"/>

<div id="pageContent" width="100%">
    <table width="100%" border="0" cellspacing="0" cellpadding="0"><%@ include file="/includes/usererrors.jsp" %></table>
    <%@ include file="/includes/help.jsp" %>
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td valign="top" id="td_right" style="width: 322px">
                <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0; margin:0; padding:0;">
                    <iframe name="ldapnav" src="<webapps:fullPath path="/ldapRemember.do?selectedTab=true" />" width="100%" height="100%" frameborder="0"></iframe>
                </div>
                <div id="FOO_endOfGroupList"></div>
            </td>
            <td>&nbsp;</td>
            <td valign="top" id="td_left" style="padding-left: 15px">
                <div id="FOO_dataDiv" style="height:100px; border:0;padding:0;margin:0;overflow:auto;">
                    <% if (request.getParameter("page") == null) { %>
                    <logic:present name="session_multitgbool">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/target/target_details_area_m.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                    </logic:present>
                    <logic:notPresent name="session_multitgbool">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/securityTargetViewDispatcher.do" />" width="100%" height="100%" frameborder="0"></iframe>
                    </logic:notPresent>
                    <% } else { %>
                    <logic:present name="session_multitgbool">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/target/target_details_area_m.jsp?page=current" />" width="100%" height="100%" frameborder="0"></iframe>
                    </logic:present>
                    <logic:notPresent name="session_multitgbool">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/securityTargetViewDispatcher.do" />" width="100%" height="100%" frameborder="0"></iframe>
                    </logic:notPresent>
                    <% } %>
                </div>
                <div id="FOO_endOfData">&nbsp;</div>
            </td>
        </tr>
    </table>

</div><!--end pageContent div-->

<script type="text/javascript">    
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','FOO_endOfData',0);");
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);");
    resizeDataSection('FOO_dataDiv','FOO_endOfData',0);
    resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);
</script>

</body>
</html>


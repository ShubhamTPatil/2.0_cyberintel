<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%@ page import = "com.marimba.apps.subscriptionmanager.util.Utils" %>
<%@ page import="com.marimba.intf.msf.IUserPrincipal" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="pc_target_view" />
<%@ include file="/includes/endHeadSection.jsp" %>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<style type="text/css">
    .slider-arrow {
        background: #d9dada none repeat scroll 0 0; float: left;
        font-size: 25px; position: fixed; margin-left: -2px;
    }
</style>
<script type="text/javascript">
    function invokeComplianceWindows(action) {
        document.location.href="/spm/" +action;
    }
    function showDesktopComplianceDetails() {
        alert("view desktop compliance");
    }
    function showSCAPComplianceDetails() {
        $('#propertieswindow').modal('show');
        $.ajax({
            url: '/spm/securitycompliance?command=scapcompliance',
            type: 'GET',
            cache: false,
            async: false,
            dataType: "html",
            contentType: 'text/html',
            success: function (response) {
                document.getElementById('scapcontent_id').innerHTML = response;
            },
            fail: function(xhr, status, err) {alert("Failed to Load");}
        });
    }

    function showProfileComplianceReport(contentId, xmlType) {
        var width = screen.availWidth - 200;
        var height = screen.availHeight - 200;
        var left = 100;
        var top = 50;
        var windowFeatures = "width=" + width + ",height=" + height + ",location=no,status=no,toolbar=no,resizable=no,scrollbars=yes,left=" + left + ",top=" + top + "screenX=" + left + ",screenY=" + top;
        window.open("/spm/securityProfileReport.do?contentId="+contentId+"&xmlType="+xmlType, "Compliance Details", windowFeatures);
    }
</script>

<body>
<%
    session.setAttribute("disablemultimode", "true");
    session.removeAttribute("session_multitgbool");
    IUserPrincipal findUserPrincipal = (IUserPrincipal) request.getUserPrincipal();
    boolean isPrimaryAdmin = Utils.isPrimaryAdmin(findUserPrincipal);
    boolean isSecondaryAdmin = Utils.isAdministrator(findUserPrincipal);
%>
<%@ include file="/includes/info.jsp" %>
<webapps:tabs tabset="main" tab="pctgtview"/>
<div id="pageContent" width="100%">
    <html:form name="packageComplianceForm" action="/compTgtView.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">
    <div align="center">
        <% if(isPrimaryAdmin || isSecondaryAdmin) { %>
        <logic:equal name="packageComplianceForm" property="value(scapUpdateAvailable)" value="true">
            <div align="right" style="padding-right: 20px;">
                <logic:equal name="packageComplianceForm" property="value(scapUpdateStatus)" value="pending">
                    <span class="title"><webapps:text key="dashboard.secinfo.update.available"/>&nbsp;<input type="button" value="<webapps:text key="dashboard.button.sync"/>" onclick="installUpdate()"></span>
                </logic:equal>
                <logic:equal name="packageComplianceForm" property="value(scapUpdateStatus)" value="inprogress">
                    <span class="title"><webapps:text key="dashboard.secinfo.update.inprogress"/>&nbsp;<img src="/spm/images/rel_interstitial_loading.gif"></span>
                </logic:equal>
                <logic:equal name="packageComplianceForm" property="value(scapUpdateStatus)" value="insync">
                    <logic:present name="packageComplianceForm" property="value(scapUpdateTime)">
                        <span class="title"><webapps:text key="dashboard.secinfo.update.insync"/>&nbsp;<bean:write name="packageComplianceForm" property="value(scapUpdateTime)"/></span>
                    </logic:present>
                </logic:equal>
                <logic:equal name="packageComplianceForm" property="value(scapUpdateStatus)" value="retry">
                    <span class="title"><webapps:text key="dashboard.secinfo.update.failed"/>&nbsp;<input type="button" value="<webapps:text key="dashboard.button.retry"/>" onclick="retryUpdate()"></span>
                </logic:equal>
            </div>
        </logic:equal>
        <% } %>
        <table width="100%" border="0" cellspacing="0" cellpadding="0"><%@ include file="/includes/usererrors.jsp" %></table>
        <%@ include file="/includes/help.jsp" %>
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <logic:notPresent name="taskid">
                    <td valign="top" id="td_right" style="width: 322px;">
                        <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0; margin:0; padding:0; overflow:visible; display:block;">
                            <iframe name="ldapnav" src="<webapps:fullPath path="/ldapRemember.do?selectedTab=true&isTargetView=false" />" width="100%" height="100%" frameborder="0"></iframe>
                        </div>
                        <div id="FOO_endOfGroupList">&nbsp;</div>
                    </td>
                </logic:notPresent>
                <td valign="top">&nbsp;</td>
                <td valign="top" id="td_left" style="padding-left: 15px;">
                    <div id="FOO_dataDiv" style="height:100px; border:0; margin:0; padding:0; display:block;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/securityTgtView.do" />" width="100%" height="100%" frameborder="0" hspace="0" marginheight="0" marginwidth="0" style="padding:0px; margin:0px; border:0px;"></iframe>
                    </div>
                    <div id="FOO_endOfData">&nbsp;</div>
                </td>
            </tr>
        </table>
    </div>
    <div id="propertieswindow" class="modal fade" role="dialog" data-backdrop="false" style="background-color: rgba(0, 0, 0, 0.5);">
        <div class="modal-dialog" style=width:100%;height:100%;>
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">SCAP Compliance Details</h4>
                </div>
                <div id="scapcontent_id" class="modal-body">&nbsp;</div>
                <div class="modal-footer">&nbsp;</div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">    
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','FOO_endOfData',0);");
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);");
    resizeDataSection('FOO_dataDiv','FOO_endOfData',0);
    resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);
    $('#propertieswindow').modal('hide');

    function installUpdate() {
        send(document.packageComplianceForm, '/compTgtView.do?action=install');
    }

    function retryUpdate() {
        send(document.packageComplianceForm, '/compTgtView.do?action=retry');
    }
</script>
</html:form>
</body>
</html>


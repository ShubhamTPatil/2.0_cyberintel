<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<%@ include file="/includes/endHeadSection.jsp" %>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<body>
<webapps:tabs tabset="main" tab="reports"/>

<html:form name="vDeskReportForm" action="/reports.do" type="com.marimba.apps.securitymgr.webapp.forms.VDeskReportForm">
    <div align="center">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">

        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td valign="top" width="322px">
                    <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0; margin:0; padding:0; overflow:visible; display:block;">
                        <iframe name="queries" src="<webapps:fullPath path="/reports.do?action=left" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfGroupList">&nbsp;</div>
                </td>
                <td valign="top" style="padding-left:15px;" align="left">
                    <div id="FOO_dataDiv" style="height:100px; border:0; margin:0; padding:0; display:block;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/reports.do?action=selected_query" />" width="100%" height="100%" frameborder="0" hspace="0" marginheight="0" marginwidth="0" style="padding:0px; margin:0px; border:0px;"></iframe>
                    </div>
                    <div id="FOO_endOfData">&nbsp;</div>
                </td>
            </tr>
        </table>
    </div>

    <script type="text/javascript">
        CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','FOO_endOfData',0);");
        CMSOnResizeHandler.addHandler("resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);");
        resizeDataSection('FOO_dataDiv','FOO_endOfData',0);
        resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);
    </script>
</html:form>

</body>
</html>


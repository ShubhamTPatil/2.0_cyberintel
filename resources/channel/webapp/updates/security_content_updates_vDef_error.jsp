<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/startHeadSection.jsp" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>


<script type="text/javascript" src="/spm/js/application.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/icon.css">
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-datepicker3.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
<link rel="stylesheet" type="text/css" href="/spm/css/_all-skins.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css">
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/fuelux.css">

<script type="text/javascript" src="/spm/includes/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="/spm/js/chart.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-datepicker.js"></script>
<script type="text/javascript" src="/spm/js/wizard.js"></script>

<style type="text/css">
    .slider-arrow {

        background: #d9dada none repeat scroll 0 0; float: left;

        font-size: 25px; position: fixed; margin-left: -2px;

    }
</style>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/info.jsp" %>
<body>
    <html:form name ="defenSightUpdatesForm" action="/defensightupdates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DefenSightUpdatesForm">
        <html:hidden property="action"/>
        <div style="padding-left:25px; padding-right:25px;">
            <table width="100%"><tr>
                <td>
                    <div class="pageHeader">
                        <span class="title"><bean:message key="page.security_content_updates_vDef_error.Title"/></span>
                    </div>
                </td>
            </tr></table>
            <%@include file="/includes/help.jsp" %>
            <div class="box">
                <div class="box-body">
                    <div class="row text-center"><div class="col-md-12"><div class="text-red text-bold"><img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">&nbsp;<webapps:pageText key="info"/></div></div></div>
                </div>
            </div>
        </div>
    </html:form>
</body>
</html>
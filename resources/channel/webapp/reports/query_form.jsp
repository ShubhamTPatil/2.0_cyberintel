<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/common_js.jsp" %>

<script type="text/javascript" src="/shell/common-rsrc/js/master.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/table.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/domMenu.js"></script>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">


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


<script type="text/javascript">
    function submitForm(form, pageType, formTarget) {
        form.target = formTarget;
        form.submit();
    }
</script>

<style type="text/css">
    .slider-arrow {

        background: #d9dada none repeat scroll 0 0; float: left;

        font-size: 25px; position: fixed; margin-left: -2px;

    }
</style>


<body>
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<form name="vDeskReportForm" action="/spm/reports.do" method="post">
    <logic:notPresent name="atlas.form.nopath">
        <input type="hidden" name="path" value="<bean:write name="atlas.form" property="path"/>">
        <input type="hidden" name="action" value="result">
        <div class="col-md-12">
            <div class="box box-default" style="border-left:1px solid #CCC; border-right: 1px solid #CCC;">
                <div class="box-header with-border">
                    <h3 class="box-title"><bean:write name="atlas.form" property="path"/></h3>
                </div>
                <div class="box-header with-border">
                    <div class="comment-text">
                        <strong><webapps:pageText key="desc"/></strong>
                        <bean:write name="atlas.form" property="desc"/>
                    </div>
                </div>
                <div class="box-body">
                    <div class="table-responsive">
                        <table>
                            <tr><td>
                                <table class="textSmall" width="100%" cellspacing="0" cellpadding="3" border="0">
                                    <tr><td><h3 style="margin-top: 5px;">Query Parameters</h3></td></tr>
                                    <tr><td><webapps:pageText key="query.help"/><td></tr>
                                    <tr><td>&nbsp;<td></tr>
                                    <tr><td><%@ include file="/reports/include_query_form.jsp" %></td></tr>
                                </table>
                            </td></tr>
                        </table>
                    </div>
                </div>
                <div class="box-footer">
                    <button type="button" class="btn btn-default" data-toggle="modal" data-target="#query_modal"><webapps:pageText key="show.query"/></button>
                    <button type="button" class="btn btn-info pull-right" onClick="submitForm(this.form, 'result', '')"><webapps:pageText key="viewResults" type="global"/></button>
                </div>
            </div>
        </div>
    </logic:notPresent>
    <logic:present scope="request" name="atlas.form.nopath">
        <webapps:pageText key="noquery"/>
    </logic:present>
</form>
<div id="query_modal" class="modal fade" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><webapps:pageText key="sql.query"/></h4>
            </div>
            <div class="modal-body"><p><bean:write name="atlas.form" property="sql"/></p></div>
            <div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">Close</button></div>
        </div>
    </div>
</div>
</body>
</html>
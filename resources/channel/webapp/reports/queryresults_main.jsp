<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <title>Query Results</title>
    <link rel="stylesheet" type="text/css" href="/shell/common-rsrc/css/main.css"/>
    <link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css">
    <link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
    <link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
    <link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
    <link rel="stylesheet" type="text/css" href="./css/jquery.contextmenu.min.css">

    <script type="text/javascript" src="/spm/js/jquery.min.js"></script>
    <script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
    <script type="text/javascript" src="/spm/js/datatables.min.js"></script>
    <script type="text/javascript" src="/spm/js/jquery.filedownload.js"></script>
    <script type="text/javascript" src="/spm/js/application.js"></script>
    <script type="text/javascript" src="./js/jquery.contextmenu.min.js"></script>
    <style type="text/css">
        .pagination {background-color: inherit;}
        ul.dt-button-collection.dropdown-menu {z-index: 202;}
        div.dt-button-background {z-index: 201;}
        .tooltip-inner {max-width: 700px;}
        textarea {
   			resize: none;
		}
        .bootstrap-dialog-message {overflow-wrap: anywhere;}
    </style>
    <%
        ServletContext context =  pageContext.getServletContext();
        String load_dbdata_status = (String) context.getAttribute("load_dbdata_status");
    %>
</head>
<body id="body_div" style="border: 1px solid #c3c3c3;">
<html:form action="/queryresults.do" type="com.marimba.apps.securitymgr.webapp.forms.VDeskQueryresultsForm">
<html:hidden property="action"/>
<html:hidden styleId="displayPath" property="displayPath"/>
<html:hidden styleId="reportStatus" property="reportStatus"/>
<section class="content-header"><h1><bean:write name="vDeskResultsForm" property="displayPath"/></h1></section>
<section class="content">
    <div class="row" style="display:none">
        <div class="col-sm-12">
            <div class="pageInfo" style="padding: 5px 5px 25px;">&nbsp;</div>
            <logic:messagesPresent>
                <div class="box box-danger box-solid">
                    <div class="box-header"><h3 class="box-title"><bean:message key="errors.intro"/></h3></div>
                    <div class="box-body"><ul><html:messages id="error"><li><bean:write name="error"/></li></html:messages></ul></div>
                </div>
            </logic:messagesPresent>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12">
            <table class="table no-border">
                <tr>
                    <td width="10%"><b>Report Ran at:</b>&nbsp;<bean:write name="vDeskResultsForm" property="queryRanAt"/></td>
                </tr>
            </table>
            <hr>
            <table id="results_table" class="table table-striped table-bordered" width="100%" cellspacing="0">
                <logic:present name="vDeskResultsForm" property="columnsList">
                    <thead>
                        <tr>
                            <logic:equal name="vDeskResultsForm" property="addViewDetails" value="true">
                                <th><input id="select_all" name="select_all" value="1" type="checkbox"></th>
                            </logic:equal>
                            <logic:iterate name="vDeskResultsForm" property="columnsList" id="columnsName">
                                <th><bean:write name="columnsName"/></th>
                            </logic:iterate>
                            <logic:equal name="vDeskResultsForm" property="addViewDetails" value="true">
                                <th data-orderable="false" data-searchable="false">Rules Compliance</th>
                            </logic:equal>
                        </tr>
                    </thead>
                </logic:present>
                <logic:present name="vDeskResultsForm" property="valuesList">
                    <tbody>
                        <logic:iterate name="vDeskResultsForm" property="valuesList" id="valuesListItr">
                            <tr>
                                <logic:equal name="vDeskResultsForm" property="addViewDetails" value="true">
                                    <td><input name="first_row" type="checkbox"></td>
                                </logic:equal>
                                <logic:iterate name="valuesListItr" id="valuesListRs">
                                    <td><bean:write name="valuesListRs"/></td>
                                </logic:iterate>
                                <logic:equal name="vDeskResultsForm" property="addViewDetails" value="true">
                                    <td><button id="btn_view_details" type="button" class="btn btn-default btn-view-details">View Details</button></td>
                                </logic:equal>
                            </tr>
                        </logic:iterate>
                    </tbody>
                </logic:present>
            </table>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12">
            <div id="pageNav">&nbsp;</div>
        </div>
    </div>
</section>
<div id="query_modal" class="modal fade" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"><webapps:pageText shared="true" type="query_form" key="sql.query" /></h4>
            </div>
            <div class="modal-body"><p><bean:write name="vDeskResultsForm" property="sql"/></p></div>
            <div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">Close</button></div>
        </div>
    </div>
</div>
<div class="modal fade" id="rules_status_modal" tabindex="-1" role="dialog" aria-labelledby="rules_status_modal_label" aria-hidden="true">
    <div class="modal-dialog" style="width:95%;">
        <div class="modal-content">
            <div class="modal-header bg-light-blue">
                <button type="button" class="close text-color-white" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
                <h4 class="modal-title" id="rules_status_modal_label"><b><span id="assessment_type"></span>&nbsp;<webapps:pageText key="rule.detailedresult.title"/>&nbsp;<span id="machine_name"></span></b></h4><br>
                <webapps:pageText key="rule.detailedresult.subtitle"/>
            </div>
            <div class="modal-body" id="machine_event_modal_body">
                <table class="table no-margin table-responsive table-bordered table-striped" id="rules_status_table" style="font-size:11px;">
                    <thead>
                        <tr>
                            <th nowrap><webapps:pageText key="rule.title"/></th>
                            <logic:equal name="vDeskResultsForm" property="isVA" value="true">
                                <th nowrap><webapps:pageText key="rule.cvss"/></th>
                            </logic:equal>
                            <th nowrap><webapps:pageText key="rule.severity"/></th>
                            <th nowrap><webapps:pageText key="rule.status"/></th>
                            <logic:equal name="vDeskResultsForm" property="displayPath" value="/Vulnerability Assessment/Machine Level Compliance">
                                <th nowrap><webapps:pageText key="rule.references"/></th>
                            </logic:equal>
                            <th nowrap><webapps:pageText key="rule.fix"/></th>
                        </tr>
                    </thead>
                    <tbody id="rules_status_tbody">&nbsp;</tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="reports_filter_modal" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" style="width:95%;">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title"><b><i class="fa fa-filter" aria-hidden="true" style="font-size:17px;color:#0073b7"></i>&nbsp;Report Options</b>
                </h4>
            </div>
            <div class="modal-body" style="padding-top:1px;padding-bottom:5px;">
                <div id="resultfilter" style="height:auto;">
                    <h4 class="box-title"><div style="color: #3399ff;"><b>Filter</b></div></h4>
                    <h4 style="display: inline"><i class="fa fa-info-circle text-blue" aria-hidden="true"></i></h4>&nbsp;Specify the result and severity values to be used for filtering the reports<br><br>
                    <div id="result_filter_div" style="text-align:left;">
                        <table id="result_filter_table" class="table no-border" style="width:100%">
                            <colgroup width="10%"/>
                            <colgroup width="*"/>
                            <tbody id="result_filter_tbody">
                                <tr>
                                    <td align="right"><div style="color: #031C46;font-size:16px;"><b>Results:&nbsp;</b></div></td>
                                    <td align="left" nowrap>
                                        <input type="checkbox" name="result" value="VULNERABLE"/>&nbsp;VULNERABLE&nbsp;&nbsp;
                                        <input type="checkbox" name="result" value="NOT-INSTALLED"/>&nbsp;NOT-INSTALLED&nbsp;&nbsp;
                                        <input type="checkbox" name="result" value="NON-VULNERABLE"/>&nbsp;NON-VULNERABLE&nbsp;&nbsp;
                                        <input type="checkbox" name="result" value="INSTALLED"/>&nbsp;INSTALLED&nbsp;&nbsp;
                                        <input type="checkbox" name="result" value="OTHERS"/>&nbsp;OTHERS
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div id="severity_filter_div" style="text-align:left;">
                        <table id="severity_filter_table" class="table no-border" style="width:100%">
                            <colgroup width="10%"/>
                            <colgroup width="*"/>
                            <tbody id="severity_filter_tbody">
                                <tr><td align="right"><div style="color: #031C46;font-size:16px;"><b>Severity:&nbsp;</b></div></td>
                                    <td align="left" nowrap>
                                        <input type="checkbox" name="severity" value="critical"/>&nbsp;Critical&nbsp;&nbsp;
                                        <input type="checkbox" name="severity" value="high"/>&nbsp;High&nbsp;&nbsp;
                                        <input type="checkbox" name="severity" value="medium"/>&nbsp;Medium&nbsp;&nbsp;
                                        <input type="checkbox" name="severity" value="low"/>&nbsp;Low&nbsp;&nbsp;
                                        <input type="checkbox" name="severity" value="unknown"/>&nbsp;Unknown&nbsp;&nbsp;
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
                <h4 class="box-title"><div style="color: #3399ff;"><b>Columns</b></div></h4>
                <h4 style="display: inline"><i class="fa fa-info-circle text-blue" aria-hidden="true"></i></h4>&nbsp;Specify the columns to be included in the generated reports<br><br>
                <div id="columns_filter_div" style="text-align:left;">
                    <table id="columns_filter_table" class="table no-border" style="width:100%">
                        <colgroup width="10%"/><colgroup width="12%"/><colgroup width="15%"/><colgroup width="15%"/><colgroup width="18%"/><colgroup width="*"/>
                        <tbody id="columns_filter_tbody">
                            <tr>
                                <td>&nbsp;</td>
                                <td><input type="checkbox" name="column" value="machineName"/>&nbsp;Machine&nbsp;</td>
                                <td><input type="checkbox" name="column" value="referenceName"/>&nbsp;Reference&nbsp;</td>
                                <td><input type="checkbox" name="column" value="severity"/>&nbsp;Severity&nbsp;&nbsp;</td>
                                <td><input type="checkbox" name="column" value="definitionName"/>&nbsp;Definition ID&nbsp;</td>
                                <td><input type="checkbox" name="column" value="definitionClass"/>&nbsp;Definition Class&nbsp;</td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td><input type="checkbox" name="column" value="contentTitle"/>&nbsp;Content&nbsp;</td>
                                <td><input type="checkbox" name="column" value="referenceURL"/>&nbsp;Reference URL&nbsp;</td>
                                <td><input type="checkbox" name="column" value="definitionResult"/>&nbsp;Result&nbsp;</td>
                                <td><input type="checkbox" name="column" value="definitionTitle"/>&nbsp;Definition Title&nbsp;</td>
                                <td><input type="checkbox" name="column" value="definitionDesc"/>&nbsp;Definition Description&nbsp;</td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td><input type="checkbox" name="column" value="profileTitle"/>&nbsp;Profile&nbsp;</td>
                                <td><input type="checkbox" name="column" value="cvssScore"/>&nbsp;CVSS Score&nbsp;</td>
                                <td><input type="checkbox" name="column" value="solution"/>&nbsp;Solution&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div>&nbsp;
                    Generate &nbsp; <select id="report_type_format" style="display: inline;  padding: 0;">
                    <option value="excel_format">Excel</option>
                    <option value="html_format">HTML</option>
                    <option value="pdf_format">PDF</option>
                    </select> &nbsp; report format for the selected profiles &nbsp;
                </div>
                <div style="margin-top:10px">&nbsp;
                    Generate report in a <select id="report_type" style="display: inline;  padding: 0;">
                    <option value="multiple">Multiple</option>
                    <option value="single">Single</option></select> file format for the selected profiles&nbsp;
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" title='generate_report' style="margin-left:3px;" id="btn_generate_report">Generate Report</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="reports_status_modal" tabindex="-2" role="dialog">
    <div class="modal-dialog modal-lg" style="width:95%;">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title"><b><i class="fa fa-filter" aria-hidden="true" style="font-size:17px;color:#0073b7"></i>&nbsp;Report Generation Status</b>
                </h4>
                <div align="right"><button type="button" class="close" data-dismiss="modal">&times;</button></div>
            </div>
            <div class="modal-body" style="padding-top:1px;padding-bottom:5px;">
                <div id="reportstatus" style="height:auto;">
                    <div id="report_status_div" style="text-align:left;">
                        <table id="report_status_table" border="1" style="width:100%">
                            <colgroup width="5%"/>
                            <colgroup width="20%"/>
                            <colgroup width="15%"/>
                            <colgroup width="*"/>
                            <tbody id="report_status_tbody">
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
var selected_rows = [];
var selection_type = 0;
var ruleDetailsDataTable;
var table;
var ruleDetailsDataTableSelector = '#rules_status_table';
var queryRanAt = '<bean:write name="vDeskResultsForm" property="queryRanAt"/>';
var queryTarget = '<bean:write name="vDeskResultsForm" property="queryTarget"/>';
var queryRanTime = '<bean:write name="vDeskResultsForm" property="queryRanTime"/>';
var queryDisplayPath = '<bean:write name="vDeskResultsForm" property="displayPath"/>';
var addViewDetails = '<bean:write name="vDeskResultsForm" property="addViewDetails"/>';
var selected_results = [];
var selected_severity = [];
var selected_columns = [];
var displayPath = $('#displayPath').val();
$(document).ready(function() {
    app.initApp();
    $('#rules_status_table').css({
        'font-size': '13px'
      });
    table = $('#results_table').DataTable({
        lengthMenu: [10, 25, 50],
        initComplete: function() {selectAllContextMenu(); },
        columnDefs: [{targets: 0, searchable: false, orderable: false}],
        order: [1, 'asc'],
        buttons: [{
            extend: 'collection', text: 'Export',
            buttons: [
                {text: 'PDF', action: function ( e, dt, node, config ) {downloadFile(table, 'pdf');}},
                {text: 'Excel', action: function ( e, dt, node, config ) {downloadFile(table, 'xls');}},
                {extend: 'print', title: queryTarget, exportOptions: {columns: ':visible'}},
                {extend: 'copy', exportOptions: {columns: ':visible'}},
            ]
        }, {
            extend: 'collection', text: 'Email',
            buttons: [
                {text: 'PDF', action: function (e, dt, node, config) {triggerEmail(table, 'pdf')}},
                {text: 'Excel', action: function (e, dt, node, config) {triggerEmail(table, 'xls')}}
            ]
        }, {
            extend: 'collection', text: 'Show SQL',
            action: function ( e, dt, node, config ) {$('#query_modal').modal();}
        }, {
            extend: 'collection', text: 'Generate Report',
            action: function ( e, dt, node, config ) {showFilterDialog();}
        }],
        fnDrawCallback: function(settings) {
            $('.btn-view-details').click(function() {
                if (addViewDetails) {
                    var row = $(this).closest("tr");
                    var datatableRow = table.row(row).data();
                    var machineName = datatableRow[1];
                    var contentId = datatableRow[3];
                    showMachineEvents(machineName, contentId);
                }
            });
        }
    });
    $('#results_table_wrapper > div:nth-child(1) > div:nth-child(1)').removeClass('col-sm-6');
    $('#results_table_wrapper > div:nth-child(1) > div:nth-child(2)').removeClass('col-sm-6');
    $('#results_table_wrapper > div:nth-child(1) > div:nth-child(1)').addClass('col-sm-8');
    $('#results_table_wrapper > div:nth-child(1) > div:nth-child(2)').addClass('col-sm-4');
    $('<div style="display: inline;">&nbsp;&nbsp;</div>').appendTo('#results_table_length', '.col-sm-8:eq(0)');
    table.buttons().container().appendTo('#results_table_length', '.col-sm-8:eq(0)');
    if (addViewDetails == 'true' && queryDisplayPath == '/Vulnerability Assessment/Machine Level Compliance') {
        table.columns().every( function () {
            var visible = this.visible();
            var colName = this.header().innerHTML;
            if (colName == 'Content Id' || colName == 'Profile Id') {
                this.visible(false);
            }
        });
    } else {
        $('.dt-buttons a:nth-child(4)').hide();
    }
    $('#results_table tbody').on('click', 'input[type="checkbox"]', function(e) {
        var row = $(this).closest("tr");    // Find the row
        var datatableRow = table.row(row).data();
        var machineName = datatableRow[1];
        var contentId = datatableRow[3];
        var profileId = datatableRow[4];
        if (this.checked) selected_rows.push({'machine_name': machineName, 'contentId': contentId, "profileId": profileId});
        else selected_rows.splice($.inArray(machineName, selected_rows), 1);
        var allPageRows = table.rows({page: 'all', search: 'applied'}).nodes();
        if ($(allPageRows).length === selected_rows.length) {
            $('#select_all').get(0).indeterminate = false;
            $('#select_all').prop('checked', true);
        } else {
            $('#select_all').prop('checked', false);
            $('#select_all').get(0).indeterminate = true;
        }
        e.stopPropagation();
    });
    $('#btn_generate_report').click(function() {
        var isRunningReport = document.getElementById('reportStatus').value;
        if (isRunningReport == 'true') {
            alertInfo('Please Wait...', 'Report generation in-progress. Please try after some time.');
            $('#reports_filter_modal').modal('hide');
        } else {
	        downloadOverallResult();
        }
    });
});
function showFilterDialog() {
    selected_results = [];
    selected_severity = [];
    selected_columns = [];
    $.each($("#reports_filter_modal input[type=checkbox]:checked"), function() {
        if($(this).is(':disabled')) {
        } else { $(this).prop('checked',false);}
    });
    $.each($("#result_filter_tbody input[type=checkbox]"), function() {
        $(this).prop('checked',true);
    });
    $.each($("#severity_filter_tbody input[type=checkbox]"), function() {
        $(this).prop('checked',true);
    });
    $.each($("#columns_filter_tbody input[type=checkbox]"), function() {
        $(this).prop('checked',true);
    });
    if (queryDisplayPath == '/Vulnerability Assessment/Machine Level Compliance') {
        if ('<%=load_dbdata_status%>' === 'started') {
            alertInfo('Please Wait...', 'Fetching data from database is still in progress... Please try after some time!');
            return false;
        }
    }
    isReportRunning();
    if(selected_rows.length === 0) {
        alertError ('Error', 'Select any checkbox from below table');
        return false;
    }
    $('#reports_filter_modal').modal();
}
function downloadOverallResult() {
    selected_results = [];
    selected_severity = [];
    selected_columns = [];
    $.each($("#columns_filter_tbody input[type=checkbox]"), function() {
        if($(this).prop("checked") == true){
            selected_columns.push($(this).val());
        }
    });
    $.each($("#result_filter_tbody input[type=checkbox]"), function() {
        if($(this).prop("checked") == true){
            selected_results.push($(this).val());
        }
    });
    $.each($("#severity_filter_tbody input[type=checkbox]"), function() {
        if($(this).prop("checked") == true){
            selected_severity.push($(this).val());
        }
    });
    if(selected_results.length === 0) {
        alertError ('Error', 'Please select atleast one result filter to proceed further');
        return false;
    }
    if(selected_severity.length === 0) {
        alertError ('Error', 'Please select atleast one severity filter to proceed further');
        return false;
    }
    if(selected_columns.length === 0) {
        alertError ('Error', 'Please select atleast one column filter to proceed further');
        return false;
    }
    // file format
    var fileFormatType = $('#report_type_format').val();
    var fileFormat = 'xls';
    if (fileFormatType == 'html_format') {
        fileFormat = 'html';
    } else if (fileFormatType == 'pdf_format') {
        fileFormat = 'pdf';
    } else {
        fileFormat = 'xls';
    }
    var singleReport = $('#report_type').val() == 'single' ? 'true' : 'false';
    $.ajax({
        type: 'POST', dataType: 'json', url: '/spm/machineoverallfiledownloader', httpMethod: 'POST',
        data: {selected_rows: JSON.stringify(selected_rows), selection_type: selection_type, display_path: displayPath,
            selected_results: selected_results.toString(),selected_severity: selected_severity.toString(),
            selected_columns: selected_columns.toString(), single_report: singleReport, file_format: fileFormat},
        beforeSend: function() {showSpinner('reports_filter_modal');},
        success: function (response) {
            if (response.result) alertSuccess('Generate Report - Success', response.message);
            else alertError('Generate Report - Failed', response.message);
            closeSpinnerForce('reports_filter_modal');
            $('#reports_filter_modal').modal('hide');
        },
        complete: function(jqXHR, textStatus) {closeSpinnerForce('reports_filter_modal');},
        error: function(jqXHR, textStatus, errorThrown) {
            closeSpinnerForce('reports_filter_modal');
            alertError('Generate Report - Error', 'Download of excel report failed');
        }
    });
}
function showReportGenerationStatus() {
    $.ajax({
        type: 'POST', dataType: 'json', url: '/spm/reportgenerationstatus', httpMethod: 'POST',
        data: {file_format: "xls"},
        beforeSend: function() {showSpinner('reports_status_modal');},
        success: function (response) {
            var tableBody = "<tr style=\"color: #031C46;font-size:16px;\">" +
                                    "<td align=\"left\" nowrap> <strong> S.No</strong></td>" +
                                    "<td align=\"left\" nowrap> <strong> Machine Name </strong></td>" +
                                    "<td align=\"left\" nowrap> <strong> File Size (Bytes) </strong></td>" +
                                    "<td align=\"left\" nowrap> <strong> Status </strong></td>" +
                            "</tr>";
            if (response["report.status.array"]) {
                for (i=0;i<response["report.status.array"].length;i++) {
                    tableBody += "<tr>" +
                                    "<td align=\"left\" nowrap>" + (i+1) + "</td>" +
                                     "<td align=\"left\" nowrap>" + response["report.status.array"][i].machine + "</td>" +
                                     "<td align=\"left\" nowrap>" + response["report.status.array"][i]["report-size"] + "</td>" +
                                     "<td align=\"left\" nowrap>" + response["report.status.array"][i].status + "</td>" +
                                 "</tr>";
                    $("#report_status_tbody").html(tableBody);
                }
            } else {
               // do nothing
            }
            closeSpinnerForce('reports_status_modal');
            $('#reports_status_modal').modal();
        },
        complete: function(jqXHR, textStatus) {closeSpinnerForce('reports_status_modal');},
        error: function(jqXHR, textStatus, errorThrown) {
            closeSpinnerForce('reports_status_modal');
            alertError('View Report Status - Error', 'Report Status lookup failed');
        }
    });
}
function isReportRunning() {
    $.ajax({
    	type: 'POST', dataType: 'json', url: '/spm/machineoverallfiledownloader', httpMethod: 'POST',
    	data: {command: 'isreportrunning'},
        success: function (response) {
        	document.getElementById('reportStatus').value = response.status;
        },
        error: function(jqXHR, textStatus, errorThrown) {
        }
    });
}
function applyPDFStyles(doc) {
    doc.defaultStyle.fontSize = 10;
    doc.styles.title.fontSize = 12;
    doc.styles.tableHeader.fontSize = 11;
    doc.styles.tableHeader.alignment = 'left';
    doc.styles.tableFooter.fontSize = 11;
    doc.styles.tableHeader.color = '#ffffff';
    doc.styles.tableHeader.fillColor = '#666666';
    doc.styles.tableBodyOdd.fillColor = '#ffffff';
    doc.styles.tableBodyEven.fillColor = '#e9e9e9';
    doc.styles.tableHeader.noWrap = false;
    // doc.content[0] -> title
    // doc.content[1] -> message
    // doc.content[2] -> table
    // change message
    //        doc.content[2].table.widths = Array(doc.content[2].table.body[0].length + 1).join('*').split('');
    var cols = [];
    cols[0] = {text: 'Left part', alignment: 'left', margin:[20]};
    cols[1] = {text: 'Right part', alignment: 'right', margin:[0,0,20]};
    var objFooter = {};
    objFooter['columns'] = cols;
    // doc['footer'] = objFooter;
    // doc['header'] = objFooter;
}
function downloadFile(table, docType) {
    var data = table.buttons.exportData({columns: ':visible'});
    var jsondata = JSON.stringify(data);
    var url = '/spm/filedownloader';
    showSpinner('body_div');
    $.fileDownload(url, {
        httpMethod: 'POST',
        data: {type: docType, target: queryTarget, ranat: queryRanAt, rantime: queryRanTime, path: queryDisplayPath, time: new Date().getMilliseconds(), data: jsondata},
        successCallback: function (url) {closeSpinnerForce('body_div');},
        failCallback: function (responseHtml, url) {
            alertError('Failed to download', responseHtml);
            closeSpinnerForce('body_div');
        }
    });
}
function triggerEmail(table, docType) {
    var data = table.buttons.exportData({columns: ':visible'});
    var jsondata = JSON.stringify(data);
    $.ajax({
        type: 'GET', dataType: 'json', url: '/spm/mailsender',
        data: {type: docType, target: queryTarget, ranat: queryRanAt, rantime: queryRanTime, path: queryDisplayPath, time: new Date().getMilliseconds(), data: jsondata},
        beforeSend: function() {showSpinner('body_div');},
        success: function (response) {
            if (response.result) alertSuccess('Successful', 'Email sent successfully');
            else alertError('Failed to send Email', response.message);
        },
        complete: function(jqXHR, textStatus) {closeSpinnerForce('body_div');},
        error: function(jqXHR, textStatus, errorThrown) {closeSpinnerForce('body_div');}
    });
}
function doSubmit(form, act) {
    form.action.value = act;
    form.submit();
}
function showMachineEvents(machine, contentId) {
    $.ajax({
        url: './rule_results.do', type: 'GET', dataType: 'json',
        data: {action: 'rule_details', machine : machine, contentId: contentId, queryDisplayPath: queryDisplayPath},
        beforeSend: function() {
            if ($.fn.dataTable.isDataTable(ruleDetailsDataTable)) {
                if ($.fn.dataTable.isDataTable(ruleDetailsDataTableSelector)) {
                    ruleDetailsDataTable.destroy();
                }
            }
            showSpinner('body_div');
        },
        success: function (data) {
            var details = '';
            $.each(data.result, function(key, value) {
                if ((value.rule_desc != undefined) && (value.rule_desc.trim() != '')) {
                    details += '<tr data-toggle="tooltip" title="'+value.rule_desc+'">';
                } else {
                    details += '<tr>';
                }
                details += '<td>' + value.rule_title + '</td>';
                if (queryDisplayPath == '/Vulnerability Assessment/Machine Level Compliance') {
                    details += '<td>' + value.rule_cvss + '</td>';
                }
                details += '<td>' + value.rule_severity + '</td>';
                details += '<td>' + value.status + '</td>';

                if (queryDisplayPath == '/Vulnerability Assessment/Machine Level Compliance') {
                     details += '<td>' + value.rule_ref + '</td>';
                }

                var rulFix = value.rule_fix;
                if (!rulFix && rulFix.length == 0) {
                    details += '<td> Custom </td>';
                }else{
                    details += '<td>' + rulFix + '</td>';
                }
                details += '</tr>';
            });
            if (queryDisplayPath == '/Vulnerability Assessment/Machine Level Compliance') {
                $('#assessment_type').html('Vulnerability');
            } else {
                $('#assessment_type').html('Configuration');
            }
            $('#machine_name').html(data.machine_name);
            $('#rules_status_tbody').html(details);
            ruleDetailsDataTable = $("#rules_status_table").DataTable({
                fnDrawCallback: function(settings) {
                    $('[data-toggle="tooltip"]').tooltip();
                }
            });
            $("#rules_status_modal").modal('show');
        },
        error: function (xhr, status, error) {ajaxErrorHandler(xhr, status, error);},
        complete: function() {closeSpinnerForce('body_div');}
    });
}
function updateDataTableSelectAllCtrl(table){
    var $table             = table.table().node();
    var $chkbox_all        = $('tbody input[type="checkbox"]', $table);
    var $chkbox_checked    = $('tbody input[type="checkbox"]:checked', $table);
    var chkbox_select_all  = $('thead input[name="select_all"]', $table).get(0);
    // If none of the checkboxes are checked
    if($chkbox_checked.length === 0){
        chkbox_select_all.checked = false;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = false;
        }
        // If all of the checkboxes are checked
    } else if ($chkbox_checked.length === $chkbox_all.length){
        chkbox_select_all.checked = true;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = false;
        }
        // If some of the checkboxes are checked
    } else {
        chkbox_select_all.checked = true;
        if('indeterminate' in chkbox_select_all){
            chkbox_select_all.indeterminate = true;
        }
    }
}
function selectAllContextMenu() {
    $.contextMenu({
        selector: '#select_all', trigger: 'left',
        callback: function(key, options) {
            selected_rows = [];
            var allPageRows = table.rows({page: 'all', search: 'applied'}).nodes();
            var currentPageRows = table.rows({page: 'current', search: 'applied'}).nodes();
            if ('select_page' === key) {
                $('#select_all').get(0).indeterminate = true;
            } else {
                $('#select_all').get(0).indeterminate = false;
                $('#select_all').prop('checked', 'select_all_page' === key);
            }
            if ('select_page' === key) {
                $.each(allPageRows, function(index, value) {value.querySelector("td input[type='checkbox']").checked = false;});
                $.each(currentPageRows, function(index, value) {value.querySelector("td input[type='checkbox']").checked = true;});
                selection_type = 0;
            }
            if ('select_all_page' === key) {
                $.each(allPageRows, function(index, value) {value.querySelector("td input[type='checkbox']").checked = true;});
                selection_type = 1;
            }
            if ('deselect_page' === key) {
                $.each(currentPageRows, function(index, value) {value.querySelector("td input[type='checkbox']").checked = false;});
                selection_type = 0;
            }
            if ('deselect_all_page' === key) {
                $.each(allPageRows, function(index, value) {value.querySelector("td input[type='checkbox']").checked = false;});
                selection_type = 0;
            }
            $.each(allPageRows, function(index, value) {
                if (value.querySelector("td input[type='checkbox']").checked)
                {
                    var row = $(this).closest("tr");    // Find the row
                    var datatableRow = table.row(row).data();
                    var machineName = datatableRow[1];
                    var contentId = datatableRow[3];
                    var profileId = datatableRow[4];
                    selected_rows.push({'machine_name': machineName, 'contentId': contentId, "profileId": profileId});
                }
            });
        },
        items: {
            select_page: {name: 'Select current page', icon: 'fa-check-square-o'},
            select_all_page: {name: 'Select all pages', icon: 'fa-check-square-o'},
            sep1: "---------",
            deselect_page: {name: 'Deselect current page', icon: 'fa-minus-square'},
            deselect_all_page: {name: 'Deselect all pages', icon: 'fa-minus-square'}
        }
    });
}
</script>
</html:form>
</body>
</html>
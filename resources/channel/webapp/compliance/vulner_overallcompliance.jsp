<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<html>
<head>
    <title><webapps:pageText key="Title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <meta http-equiv="Cache-control" content="no-cache">
    <link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css"/>
    <webapps:helpContext context="spm" topic="pc_target_view" />
    <%@ include file="/includes/endHeadSection.jsp" %>
<body>

<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/chart.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-datepicker.js"></script>
<script type="text/javascript" src="/spm/js/application.js"></script>
<script type="text/javascript" src="/spm/js/jquery.flot.min.js"></script>
<script type="text/javascript" src="/spm/js/jquery.flot.resize.min.js"></script>
<script type="text/javascript" src="/spm/js/jquery.flot.pie.min.js"></script>
<script type="text/javascript" src="/spm/js/es6-promise.auto.js"></script>
<script type="text/javascript" src="/spm/js/html2canvas.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-datepicker3.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<style type="text/css">
    #div_chart_donut_legend .legendLabel {padding-left: 4px;}
    #div_chart_machine_assessed_legend .legendLabel {padding-left: 4px;}
</style>

<logic:present name="target">
    <bean:define id="ID" name="target" property="id" toScope="request"/>
    <bean:define id="Name" name="target" property="name" toScope="request"/>
    <bean:define id="Type" name="target" property="type" toScope="request"/>
    <bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target" toScope="request"/>
    <logic:equal name="action1" value="">
        <div style="width:97%;">
            <bean:define id="pageBeanName"  value="<%=IWebAppConstants.SESSION_POLICIES_DETAILS%>" toScope="request" />
            <sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>" resultsName="<%=IWebAppConstants.POLICIES_DETAILS_FORTGT%>" />
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td valign="bottom" class="tableTitle">
                        <jsp:include page="/includes/target_display_single.jsp"/>
                    </td>
                </tr>
            </table>
        </div>
    </logic:equal>
</logic:present>
<logic:equal name="action1" value="">
    <webapps:formtabs tabset="compTgtView" tab="vulnerAssmentGroupCompliance" subtab="compOverview" />
</logic:equal>
<logic:notEqual name="action1" value="">
    <webapps:formtabs tabset="compOverallDashboard" tab="vulnerAssmentOverallDashboard"/>
</logic:notEqual>
<section class="content" id="report_whole_div">
<div class="row" id="error_message_div" style="display:none">
    <div class="col-lg-12 col-xs-12">
        <div class="dashboard-box">
            <div class="box-header with-border">
                <div class="box-title">
                        <span class="text-red">Warning: Please verify compliance percentage after LDAP Syn run once</span>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-lg-12 col-xs-12">
        <div class="dashboard-box">
            <div class="box-header with-border">
                <div class="box-title">
                    <logic:equal name="action1" value="">
                        <bean:write name="Name" filter="false"/><small>&nbsp;(<bean:write name="ID" filter="false"/>)</small>
                    </logic:equal>
                    <logic:notEqual name="action1" value="">Overall Dashboard</logic:notEqual>
                </div>
                <div class="box-tools pull-right">
                    <a class="btn btn-box-tool" id="report_img_save" onmouseover="MakeTip('Capture screen');" onmouseout="CloseTip();">
                        <i class="fa fa-picture-o" style="font-size:17;color:#0073b7"></i>
                    </a>
                    <a id="report_img_save_data" href="#"></a>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="row">
    <div class="col-lg-5 col-xs-5">
        <div class="dashboard-box" style="height:310px">
            <div class="box-header with-border"><div class="box-title">Machine Compliance</div></div>
            <div class="box-body">
                <div class="row text-center">
                    <div class="col-lg-8 col-xs-8 text-center" style="padding-top: 10px;">
                        <canvas height="180" width="180" id="div_chart_machine_assessed_canvas">&nbsp;</canvas>
                        <input type="text" id="div_chart_machine_assessed_percentage" value="0%" data-width="180" readonly data-height="180" data-fgcolor="#001F3F" data-readonly="true"
                               style="position: absolute; margin-top: 70px; margin-left: -105px; border: 0; font-weight: bold; font-size: 25px; padding: 0; -webkit-appearance: none; background: none;">
                        <div style="padding-top: 15px;" id="div_chart_machine_assessed_legend">
                            <table style="font-size:smaller;color:#545454"><tbody>
                                <tr><td class="legendColorBox"><div style="border:1px solid #ccc;padding:1px"><div style="width:4px;height:0;border:5px solid #efefef;overflow:hidden"></div></div></td><td class="legendLabel">TOTAL <span id="total_mac_count">()</span></td></tr>
                                <tr><td class="legendColorBox"><div style="border:1px solid #ccc;padding:1px"><div style="width:4px;height:0;border:5px solid #0073b7;overflow:hidden"></div></div></td><td class="legendLabel">REPORTED <span id="reported_mac_count">()</span></td></tr>
                            </tbody></table>
                        </div>
                    </div>
                    <div class="col-lg-4 col-xs-4" style="padding-top: 5px;">
                        <div class="text-orange"><h3><b><span class="box-title" id="compliance_percentage">0%</span></b></h3>Compliance</div>
                        <div>&nbsp;</div>
                        <div>&nbsp;</div>
                        <div class="text-blue"><b><h3><span class="box-title" id="scan_24hrs_machine_count">0</span></h3></b>Scanned in last 24 hours</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-lg-7 col-xs-7">
        <div class="dashboard-box" style="height:310px">
            <div class="box-header with-border"><div class="box-title">Security Compliance (by Operating System)</div></div>
            <div class="box-body">
                <div class="row">
                    <div class="col-lg-5 col-xs-5">
                        <div class="row text-center" style="padding-top: 5px;">
                            <div class="col-lg-12 col-xs-12">
                                <div id="div_chart_donut" style="width: 210px; height: 180px;">&nbsp;</div>
                                <div id="div_chart_donut_legend" style="padding-top: 15px;">&nbsp;</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg7 col-xs-7">
                        <div class="row">
                            <div class="progress-group">
                                <div class="col-md-2">
                                    <span class="text-blue text-center" style="padding-left:5px;font-size:40px"><i class="fa fa-windows"></i></span>
                                </div>
                                <div class="col-md-9" style="padding:12px">
                                    <span class="progress-text"><webapps:text key="page.overview.platform.windows"/></span>
                                <span class="progress-number">
                                    <span class="text-green" id="windows_compliant">0</span> /
                                    <span class="text-bold" id="windows_total">0</span>
                                    [<span id="windows_percentage_label" class="text-blue">0%</span>]
                                </span>
                                    <div class="progress sm">
                                        <div id="windows_percentage_bar" class="progress-bar progress-bar-blue" style="width: 0%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="progress-group">
                                <div class="col-md-2">
                                    <span class="text-red text-center" style="padding-left:5px;font-size:40px"><i class="fa fa-linux"></i></span>
                                </div>
                                <div class="col-md-9" style="padding:10px">
                                    <span class="progress-text"><webapps:text key="page.overview.platform.linux"/></span>
                                <span class="progress-number">
                                    <span class="text-green" id="linux_compliant">0</span> /
                                    <span class="text-bold" id="linux_total">0</span>
                                    [<span id="linux_percentage_label" class="text-blue">0%</span>]
                                </span>
                                    <div class="progress sm">
                                        <div id="linux_percentage_bar" class="progress-bar progress-bar-red" style="width: 0%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="progress-group">
                                <div class="col-md-2">
                                    <span class="text-aqua text-center" style="padding-left:5px;font-size:40px"><i class="fa fa-apple"></i></span>
                                </div>
                                <div class="col-md-9" style="padding:10px">
                                    <span class="progress-text"><webapps:text key="page.overview.platform.mac"/></span>
                                <span class="progress-number">
                                    <span class="text-green" id="mac_compliant">0</span> /
                                    <span class="text-bold" id="mac_total">0</span>
                                    [<span id="mac_percentage_label" class="text-blue">0%</span>]
                                </span>
                                    <div class="progress sm">
                                        <div id="mac_percentage_bar" class="progress-bar progress-bar-aqua" style="width: 0%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <div class="dashboard-box">
            <div class="box-header with-border">
                <div style="float: left;"><h3 class="box-title"><webapps:text key="page.overview.individual.scannerwise.compliance"/></h3></div>
                <div class="pull-right">Number of Security Scanner: &nbsp;<span id="scanner_count">&nbsp;</span></div>
            </div>
            <div class="box-body text-left" style="max-height: 260px; overflow-y: auto; overflow-x: hidden;" id="div_scannerwise_complaince_data">&nbsp;</div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <div class="dashboard-box">
            <div class="box-header with-border"><h3 class="box-title">Top 5 Vulnerabilities</h3>
                <!-- <div class="box-tools pull-right"> Elastic Server Status : &nbsp;<span id="server_status" class="text-bold"></span></div> -->

            </div>
            <div class="box-body" id="div_top5_vulnerable_serverUp">
                <div class="row">
                    <div class="col-md-12">
                        <table class="table no-border" width="100%">
                            <tr>
                                <td width="40%"><label>Security Content</label><select class="form-control" style="min-width: 100%;" id="sel_content_details">&nbsp;</select></td>
                                <td width="40%"><label>Security Profile</label><select class="form-control" style="min-width: 100%;" id="sel_profile_details">&nbsp;</select></td>
                                <td width="20%"><label>Date</label><input class="form-control" id="txt_date_picker_control" type="text" style="min-width: 100%;"></td>
                            </tr>
                            <tr>
                                <td align="center" colspan="3" width="100%"><button type="button" class="btn btn-success" id="btn_vulnerable_report" disabled="disabled">View Report</button></td>
                            </tr>
                        </table>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="dashboard-box">
                            <div class="box-header"><h3 class="box-title">Top 5 Vulnerable Definitions</h3></div>
                            <div class="box-body" id="div_top5_vulnerable_rules_data">
                                <div class="row text-center"><div class="col-md-12"><div class="text-orange text-bold">Select "Security Content" and "Security Profle" to list top 5 vulnerable definitions</div></div></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="box-body" id="div_top5_vulnerable_serverDown">
                <div class="row">
                    <div class="col-md-12">
                        <div class="dashboard-box">
                            <div class="box-body">
                                <div class="row text-center"><div class="col-md-12"><div class="text-red text-bold"><img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">&nbsp;Server Unavailable... Please try again later...</div></div></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</section>
</body>
<script type="text/javascript">
var color_compliant = ['#00A65A', '#01884B'];
var color_non_compliant = ['#DD4B39', '#C04333'];
var color_not_checkedin = ['#0073B7', '#025D92'];
var color_not_applicable = ['#FF851B', '#E66D03'];

var compliantPercentage = 0;
var pieChartOptions = {
    segmentShowStroke: false, segmentStrokeColor: "#fff", segmentStrokeWidth: 0, percentageInnerCutout: 0,
    animationSteps: 100, animationEasing: "easeOutBounce", animateRotate: false, animateScale: false, showTooltips: true
};
var doughnutOptions = {
    segmentShowStroke: false, segmentStrokeColor: "#fff", segmentStrokeWidth: 0, percentageInnerCutout: 65,
    animationSteps: 100, animationEasing: "easeOutBounce", animateRotate: false, animateScale: false, showTooltips: false
};
function SubChartdata(value, color) {
    this.value = value;
    this.color = color;
}

$(document).ready(function() {
    app.initApp();
    $('#txt_date_picker_control').datepicker({autoclose: true, endDate: "0d", startView: "0 'days'", todayBtn: true, todayHighlight: true});
    var action1 = '<bean:write name="action1"/>';
    loadScapFromDB(action1);
    getElasticServerStatus();
    $('<option>').val('select').text('Select').appendTo('#sel_profile_details');
    $('#btn_vulnerable_report').click(function() {getVulnerablityDetails();});
    $("#sel_content_details").change(function () {$('#txt_date_picker_control').val('');updateProfileDetails(this.value);});
    $("#sel_profile_details").change(function () {$('#txt_date_picker_control').val('');$("#btn_vulnerable_report").prop("disabled", 'select' === this.value || 'select' === $("#sel_content_details").val());});
});

function loadScapFromDB(secondaryAction) {
    $.ajax({
        type: "GET", dataType: "json", url: "/spm/vulnerOverallCompliance.do",
        data: {action: 'fromdb', action1: secondaryAction},
        beforeSend: function() {showSpinner('report_whole_div');},
        success: function (data) {
            var machineAssessedDetails = [];
            $('#scan_24hrs_machine_count').html(data.checkedIn24Hrs);
            $('#scanner_count').html(data.scanner_use_count);

            $('#windows_total').html(data.windows_count);
            $('#windows_compliant').html(data.windows_compliant);
            $('#windows_percentage_label').html(data.windows_compliant_percentage  + "%");
            $('#windows_percentage_bar').width(data.windows_compliant_percentage + "%");

            $('#linux_total').html(data.linux_count);
            $('#linux_compliant').html(data.linux_compliant);
            $('#linux_percentage_label').html(data.linux_compliant_percentage  + "%");
            $('#linux_percentage_bar').width(data.linux_compliant_percentage + "%");

            $('#mac_total').html(data.mac_count);
            $('#mac_compliant').html(data.mac_compliant);
            $('#mac_percentage_label').html(data.mac_compliant_percentage  + "%");
            $('#mac_percentage_bar').width(data.mac_compliant_percentage + "%");


            var machine_reported_percentage = 0;
            if (data.machine_reported_count > 0 && data.count > 0) {
                machine_reported_percentage = Math.round((data.machine_reported_count/data.count) * 100);
            }
            $('#div_chart_machine_assessed_percentage').val(machine_reported_percentage + "%");
            machineAssessedDetails.push(new SubChartdata(machine_reported_percentage, '#0073b7'));
            machineAssessedDetails.push(new SubChartdata(machine_reported_percentage > 0 ? 100 - machine_reported_percentage : 1, '#efefef'));
            new Chart($("#div_chart_machine_assessed_canvas").get(0).getContext("2d")).Doughnut(machineAssessedDetails, doughnutOptions);

            $('#total_mac_count').html('('+data.count+')');
            $('#reported_mac_count').html('('+data.machine_reported_count+')');

            if (data.count == 0 && (data.compliant > 0 || data.noncompliant > 0)) {
                $('#error_message_div').show();
            }
            if (data.compliant > 0 || data.noncompliant > 0 || data.notcheckedin > 0) {
                compliantPercentage = (!isNaN(data.count) && !isNaN(data.compliant) && data.count != 0)? Math.round((data.compliant / data.count) * 100) : 0;
                if (!isNaN(compliantPercentage)) {
                    $('#compliance_percentage').html(compliantPercentage + '%');
                    $('#compliance_percentage').closest("div").prop('class', "text-green");
                }
                var snotcheckedin = data.notcheckedin;
                if (snotcheckedin < 0) snotcheckedin = 0;

                var compliaceData = [
                    {label: "COMPLIANT", data: data.compliant, color: color_compliant[0]},
                    {label: "NON-COMPLIANT", data: data.noncompliant, color: color_non_compliant[0]},
                    {label: "NOT-CHECKED-IN", data: snotcheckedin, color: color_not_checkedin[0]},
                    {label: "NOT APPLICABLE", data: data.notapplicable, color: color_not_applicable[0]}
                ]
                plotDonutChartForComplianceData(compliaceData);
            } else {
                $('#div_chart_donut').html('<div style="height: 240px; padding-top: 110px;"><span class="text-red text-bold">No machine details found</span></div>');
            }
            populateScannerwiseComplainceData(data.scannerwise_complaince_data);
            populateContentsDetails(data.scap_content_data);
        },
        complete: function(jqXHR, textStatus) {closeSpinnerForce('report_whole_div');}
    });
}
function populateContentsDetails(scap_content_data) {
    $('<option>').val('select').text('Select').appendTo('#sel_content_details');
    if (scap_content_data != undefined || scap_content_data != '') {
        $.each(scap_content_data, function(i, item) {
            $('<option>').val(item.id).text(item.title).appendTo('#sel_content_details');
        });
    }
}

function updateProfileDetails() {
    $('#sel_profile_details').empty();
    $('<option>').val('select').text('Select').appendTo('#sel_profile_details');
    $.ajax({
        type: "GET", dataType: "json", url: "/spm/vulnerOverallCompliance.do",
        data: {action: 'getprofiledetails', contenttitle : $("#sel_content_details option:selected").text()},
        success: function (data) {
            if (data.scap_profiles_data != undefined || data.scap_profiles_data != '') {
                $.each(data.scap_profiles_data, function(i, item) {
                    $('<option>').val(item.id).text(item.title).appendTo('#sel_profile_details');
                });
            }
            $("#btn_vulnerable_report").prop("disabled", 'select' === this.value || 'select' === $("#sel_profile_details").val());
        }
    });
}

function getVulnerablityDetails() {
    var contentid = $('#sel_content_details').val();
    var profileid = $('#sel_profile_details').val();
    var date = $('#txt_date_picker_control').val();
    $.ajax({
        type: "GET", dataType: "json", url: "/spm/vulnerOverallCompliance.do",
        data: {action: 'getvulnerablitydetails', contentid : contentid, profileid : profileid, date: date},
        beforeSend: function() {showSpinner('div_top5_vulnerable_rules_data');},
        success: function (data) {populateTop5VulnerableData(data.rules);},
        complete: function(jqXHR, textStatus) {closeSpinnerForce('div_top5_vulnerable_rules_data');},
        error: function(jqXHR, textStatus, errorThrown) {closeSpinnerForce('div_top5_vulnerable_rules_data');}
    });
}
function getElasticServerStatus() {
    $.ajax({
        type: "GET", dataType: "json", url: "/spm/vulnerOverallCompliance.do",
        data: {action: 'getserverstatus'},
        success: function (data) {
            if(data.elasticstatus == "Up"){
                //$('#server_status').html('<i class="fa fa-thumbs-up text-green"></i>&nbsp;'+data.elasticstatus);
                $("#div_top5_vulnerable_serverDown").hide();
                $("#div_top5_vulnerable_serverUp").show();
            }
            else if(data.elasticstatus == "Down"){
                //$('#server_status').html('<i class="fa fa-thumbs-down text-red"></i>&nbsp;'+data.elasticstatus);
                $("#div_top5_vulnerable_serverUp").hide();
                $("#div_top5_vulnerable_serverDown").show();}
            else {//$('#server_status').html(data.elasticstatus);
                $("#div_top5_vulnerable_serverUp").hide();
                $("#div_top5_vulnerable_serverDown").show();}
        }
    });
}
function populateScannerwiseComplainceData(scannerwise_complaince_data) {
    var contentToDisplay = '';
    if (scannerwise_complaince_data != undefined || scannerwise_complaince_data != '') {
        $.each(scannerwise_complaince_data, function(i, item) {
            contentToDisplay += '<div class="row"><div class="progress-group"><div class="col-md-12"><div class="progress-text">'+ item.title + ' ('+ item.percentage +' %)'
                    + '<span style="float: right;">' + item.compliant + '/' + item.count +'</span></div>'
                    + '<div class="progress sm"><div class="progress-bar bg-green" style="width: '+ item.percentage +'%"></div></div></div></div></div>';
        });
    }
    contentToDisplay = (contentToDisplay === '') ? '<div class="row"><div class="text-red text-bold text-center">No Records found</div></div>' : contentToDisplay;
    $('#div_scannerwise_complaince_data').html(contentToDisplay);
}

function populateTop5VulnerableData(rules) {
    var contentToDisplay_rules = '';

    $.each(rules, function(i, item) {
        contentToDisplay_rules += '<div class="row"><div class="progress-group"><div class="col-md-12"><div class="progress-text">'+ item.name + ' ('+ item.percentage +' %) </div>'
                + '<div class="progress sm"><div class="progress-bar bg-red" style="width: '+ item.percentage +'%"></div></div></div></div></div>';
    });

    contentToDisplay_rules = (contentToDisplay_rules === '') ? '<div class="row text-center"><div class="col-md-12"><div class="text-red text-bold">No Records Found</div></div></div>' : contentToDisplay_rules;

    $('#div_top5_vulnerable_rules_data').html(contentToDisplay_rules);
}

function getColor(percentageVal) {
    var className = 'aqua';
    if (percentageVal < 30) className = "red";
    if (percentageVal >= 30 && percentageVal <= 70 ) className = "yellow";
    if (percentageVal > 70) className = "green";
    return className;
}

function plotDonutChartForComplianceData(donutData) {
    for (var i = 0; i < donutData.length; i++){
        donutData[i].label += ' ('+donutData[i].data+')'
    }

    $.plot("#div_chart_donut", donutData, {
        series: {
            pie: {show: true, radius: 1, innerRadius: 0.5, label: {show: true, radius: 2 / 3, formatter: labelFormatter, threshold: 0.1}}
        },
        legend: {noColumns: 1, container: $("#div_chart_donut_legend")}
    });
}

function labelFormatter(label, series) {
    return '<div style="font-size:14px; text-align:center; padding:2px; color: #fff;"><b>'+Math.round(series.percent) + "%</b></div>";
}
function MakeTip(txtToDisplay) {
    return Tip(txtToDisplay, WIDTH, '-250', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', OFFSETY, 20, BORDERCOLOR, '#333300', FADEIN, 100);
}

function CloseTip() {
    UnTip();
}
// https://codepedia.info/convert-html-to-image-in-jquery-div-or-table-to-jpg-png/
// IE Issue : http://stackoverflow.com/questions/10457608/create-screenshot-of-webpage-using-html2canvas-unable-to-initialize-properly
$('#report_img_save').click(function() {
    var element = $('#report_whole_div');
    html2canvas(element).then(function(canvas) {
        var blob;
        if (navigator.userAgent.indexOf("MSIE ") > 0 || navigator.userAgent.match(/Trident.*rv\:11\./)) {
            blob = canvas.msToBlob();
            window.navigator.msSaveBlob(blob, getFileName());
        } else {
            var imgeData = canvas.toDataURL("image/png");
            blob = imgeData.replace(/^data:image\/png/, "data:application/octet-stream");
            $("#report_img_save_data").attr("download", getFileName()).attr("href", blob);
            $("#report_img_save_data")[0].click();
        }
    });
});

function getFileName() {
    var dNow = new Date();
    var localdate= (dNow.getMonth()+1) + '-' + dNow.getDate() + '-' + dNow.getFullYear() + '_' + dNow.getHours() + '-' + dNow.getMinutes() + "-" + dNow.getSeconds();
    return 'dashboard_report_' + localdate + '.png';
}
</script>
</html>
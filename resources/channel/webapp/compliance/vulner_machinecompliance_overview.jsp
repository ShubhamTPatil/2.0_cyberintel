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
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/chart.min.js"></script>
<script type="text/javascript" src="/spm/js/application.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>

<style type="text/css">
    .legendLabel {padding-left: 4px;}
</style>

<logic:present name="target">
    <bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target" toScope="request"/>
    <div style="width:97%;">
        <bean:define id="pageBeanName"  value="<%=IWebAppConstants.SESSION_POLICIES_DETAILS%>" toScope="request"/>
        <sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>" resultsName="<%=IWebAppConstants.POLICIES_DETAILS_FORTGT%>" />
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td valign="bottom" class="tableTitle">
                    <bean:define id="ID" name="target" property="id" toScope="request"/>
                    <bean:define id="Name" name="target" property="name" toScope="request"/>
                    <bean:define id="Type" name="target" property="type" toScope="request"/>
                    <jsp:include page="/includes/target_display_single.jsp"/>
                </td>
            </tr>
        </table>
    </div>
</logic:present>
<webapps:formtabs tabset="compTargetView" tab="vulnerAssmentMachineCompliance" subtab="compTgtOverview"/>
<bean:define id="machine" name="targetDetails" scope="request"/>

<div style="padding:10px">
<div class="row">
    <div class="col-md-4">
        <div class="dashboard-box">
            <div class="box-body text-center">
                <div class="col-lg-4 col-xs-4">
                    <logic:equal name="machine" property="complaintLevel" value="COMPLIANT">
                        <span class="text-green" style="padding-left:1px;font-size:90px"><i class="fa fa-check-circle"></i></span>
                    </logic:equal>
                    <logic:equal name="machine" property="complaintLevel" value="NON-COMPLIANT">
                        <span class="text-red" style="padding-left:1px;font-size:90px"><i class="fa  fa-times-circle"></i></span>
                    </logic:equal>
                    <logic:equal name="machine" property="complaintLevel" value="NOT-CHECKED-IN">
                        <span class="text-blue" style="padding-left:1px;font-size:90px"><i class="fa fa-warning"></i></span>
                    </logic:equal>
                    <logic:equal name="machine" property="complaintLevel" value="NOT APPLICABLE">
                        <span class="text-orange" style="padding-left:1px;font-size:90px"><i class="fa fa-warning"></i></span>
                    </logic:equal>
                </div>
                <div class="col-lg-8 col-xs-8">
                    <h1 id="compliant_status" class="text-bold" style="font-size:25px;padding-top:5px"><bean:write name="machine" property="complaintLevel" /></h1>
                    <div class="row"><webapps:text key="page.overview.compliance"/></div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="dashboard-box">
            <div class="box-body text-center">
                <div class="col-lg-4 col-xs-4">
                    <span class="text-aqua" style="padding-left:1px;font-size:90px"><i class="fa fa-gears"></i></span>
                </div>
                <div class="col-lg-8 col-xs-8">
                    <h1 id="profile_count" class="text-bold" style="font-size:25px;padding-top:5px"><bean:write name="machine" property="profileCount" /></h1>
                    <div class="row"><webapps:text key="page.overview.noofprofiles"/></div>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="dashboard-box">
            <div class="box-body text-center">
                <div class="col-lg-4 col-xs-4">
                    <span class="text-teal" style="padding-left:10px;font-size:90px"><i class="fa fa-search"></i></span>
                </div>
                <div class="col-lg-8 col-xs-8">
                    <h1 id="last_scan_time" class="text-bold" style="font-size:25px;padding-top:5px"><bean:write name="machine" property="lastPolicyUpdateTimeInString" /></h1>
                    <div class="row"><webapps:text key="page.overview.scantime"/></div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-md-12">
        <div class="dashboard-box">
            <div class="box-header with-border"><h3 class="box-title"><webapps:text key="page.overview.scanner.compliance"/></h3></div>
            <div class="box-body text-center">
                <div class="col-md-3">
                    <div class="row">
                        <div class="col-md-12" id="chart_canvas"  style="padding-top:10px;">
                            <canvas id="profile_compliance_chart" style="width: 210px; height: 210px;">&nbsp;</canvas>
                        </div>
                    </div>
                    <div class="row">
                        <div style="padding-top: 15px;" id="div_chart_machine_assessed_legend">
                            <table style="font-size:smaller;color:#545454" width="80%"><tbody>
                                <tbody>
                                    <tr><td><div style="border:1px solid #ccc;padding:1px"><div style="border:5px solid #00A65A;overflow:hidden"></div></div></td><td class="legendLabel">COMPLIANT</td>
                                    <td><div style="border:1px solid #ccc;padding:1px"><div style="border:5px solid #DD4B39;overflow:hidden"></div></div></td><td class="legendLabel">NON-COMPLIANT</td></tr>
                                    <tr><td><div style="border:1px solid #ccc;padding:1px"><div style="border:5px solid #0073B7;overflow:hidden"></div></div></td><td class="legendLabel">NOT-CHECKED-IN</td>
                                    <td><div style="border:1px solid #ccc;padding:1px"><div style="border:5px solid #FF851B;overflow:hidden"></div></div></td><td class="legendLabel">NOT APPLICABLE</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="col-md-9" style="padding-top:5px;" id="rules_status">
                    <div class="progress-group">
                        <logic:iterate id="profile" name="machine" property="profiles">
                            <div class="col-md-12 text-left">
                                    <span class="progress-text" style="padding-bottom:3px">
                                        <bean:write name="profile" property="contentTitle" />
                                        <logic:equal name="profile" property="complaintLevel" value="NOT-CHECKED-IN">
                                            &nbsp;[<bean:write name="profile" property="complaintLevel" />]
                                        </logic:equal>
                                        <logic:notEqual name="profile" property="complaintLevel" value="NOT-CHECKED-IN">
                                            <logic:equal name="profile" property="complaintLevel" value="NOT APPLICABLE">
                                                &nbsp;[<bean:write name="profile" property="complaintLevel" />]
                                            </logic:equal>
                                            <logic:notEqual name="profile" property="complaintLevel" value="NOT APPLICABLE">
                                                &nbsp;[<a href="#" onclick="window.parent.showProfileComplianceReport('<bean:write name="profile" property="contentName" />', 'oval')"><bean:write name="profile" property="complaintLevel" /></a>]
                                            </logic:notEqual>
                                        </logic:notEqual>
                                    </span>
                                <div class="progress md">
                                    <div id="passed_rules_percentage" class="progress-bar progress-bar-green" style="width: <bean:write name="profile" property="passedRulesPercentage" />%;"><bean:write name="profile" property="passedRulesCount" /> pass</div>
                                    <div id="failed_rules_percentage" class="progress-bar progress-bar-red" style="width: <bean:write name="profile" property="failedRulesPercentage" />%"><bean:write name="profile" property="failedRulesCount" /> fail</div>
                                    <div id="other_rules_percentage" class="progress-bar progress-bar-gray" style="width: <bean:write name="profile" property="otherRulesPercentage" />%"><bean:write name="profile" property="otherRulesCount" /> other</div>
                                </div>
                            </div>
                        </logic:iterate>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        app.initApp();
        plotChart();
    });
    var color_compliant = ['#00A65A', '#01884B'];
    var color_non_compliant = ['#DD4B39', '#C04333'];
    var color_not_checkedin = ['#0073B7', '#025D92'];
    var color_not_applicable = ['#FF9900', '#FF9900'];

    var PieData = [];
    function SubChartdata(value, color, highlight, label){
        this.value = value;
        this.color = color;
        this.highlight = highlight;
        this.label = label;
    }
    var pieChartOptions = {
        segmentShowStroke: false, segmentStrokeColor: "#fff", segmentStrokeWidth: 0, percentageInnerCutout: 0,
        animationSteps: 100, animationEasing: "easeOutBounce", animateRotate: false, animateScale: false, showTooltips: true
    };

    function plotChart() {
        PieData.push(new SubChartdata('<bean:write name="machine" property="compliantCount"/>', color_compliant[0], color_compliant[1], "COMPLAINT"));
        PieData.push(new SubChartdata('<bean:write name="machine" property="nonCompliantCount"/>', color_non_compliant[0], color_non_compliant[1], "NON-COMPLAINT"));
        PieData.push(new SubChartdata('<bean:write name="machine" property="checkinCount"/>', color_not_checkedin[0], color_not_checkedin[1], "NOT-CHECKEDIN"));
        PieData.push(new SubChartdata('<bean:write name="machine" property="notApplicableCount"/>', color_not_applicable[0], color_not_applicable[1], "NOT APPLICABLE"));
        var pieChart = new Chart($("#profile_compliance_chart").get(0).getContext("2d"));
        pieChart.Pie(PieData, pieChartOptions)
    }
    function MakeTip(txtToDisplay) {
        return Tip(txtToDisplay, WIDTH, '-250', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', OFFSETY, 20, BORDERCOLOR, '#333300', FADEIN, 100);
    }

    function CloseTip() {
        UnTip();
    }
</script>
</body>
</html>
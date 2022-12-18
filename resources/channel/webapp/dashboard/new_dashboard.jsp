<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>

<!DOCTYPE html>
<html lang="en">
<head>


<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css"/>

<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>

<script type="text/javascript">


$(function () {

    $('.nav-selected').removeClass('nav-selected');
    $('#dashboard').addClass('nav-selected');

    var ctx1 = $("#vulStatsDonutChart");
    // label: "Severity",
    var chart1 = new Chart(ctx1, {
        type: "doughnut",
        data: {
            labels: ["Critical", "High", "Medium", "Low"],
            datasets: [
                {
                    data: <bean:write name="newDashboardForm" property="pieChartData"/>,
                    backgroundColor: [
                        "#FF5F60", "#D4733A", "#F3CC63", "#71DCEB"
                    ]
                }
            ]
        },
        options: {
            responsive: false,
            plugins: {
                title: {
                    display: false,
                    position: "top",
                    text: "Doughnut Chart",
                    fontSize: 18,
                    fontColor: "#111"
                },
                legend: {
                    display: true,
                    position: "bottom",
                    labels: {
                        fontColor: "#333",
                        fontSize: 16
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            let label = context.dataset.label || '';
                            return label + " " + context.parsed + '%';
                        }
                    }
                }
            }
        }
    });

    var vulSeverityData = '<bean:write name="newDashboardForm" property="vulnerableSeverityData"/>';
    vulSeverityData = vulSeverityData.replace(/&quot;/g,'"');
    vulSeverityData=JSON.parse(vulSeverityData);

    var ctxVulAging = $("#vulAgingScatter");
    var chartVulAgingScatter = new Chart(ctxVulAging, {
        type: 'scatter',
        data: {
            datasets: vulSeverityData,
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom'
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            let label = context.dataset.label || '';
                            return [label + ": " + context.parsed.y, 'Age: ' + context.parsed.x + ' days'];
                        }
                    }
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: "Age(Days)"
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: "Vulnerabilities"
                    }
                }
            }

        }
    });

    var topVulData = '<bean:write name="newDashboardForm" property="topVulnerableData"/>';
    topVulData = topVulData.replace(/&quot;/g,'"');
    topVulData=JSON.parse(topVulData);

    $('#topVulTable').DataTable({
        "destroy": true, // In order to reinitialize the datatable
        "pagination": true, // For Pagination
        "sorting": false, // For sorting
        "ordering": false,
        "aaData": topVulData,
        "columns": [{},
        {
            "data": "CVE-ID"
        }, {
            "data": "Severity"
        }, {
            "data": "Impacted Machines"
        }, {
            "data": "Patches"
        }],
        'columnDefs': [{
            'targets': 0,
            'searchable': true,
            'orderable': false,
            'className': 'dt-body-center',
            'render': function (data, type, full, meta) {
                return '<input type="checkbox" class="form-check-input" name="topVulCheckbox" value="' + $('<div/>').text(data).html() + '">';
            }
        }],
        'rowCallback': function (row, data, index) {
            switch (data['Severity']) {
                case 'Critical':
                    $(row).find('td:eq(2)').addClass('criticalColor');
                    break;

                case 'High':
                    $(row).find('td:eq(2)').addClass('highColor');
                    break;

                case 'Medium':
                    $(row).find('td:eq(2)').addClass('mediumColor');
                    break;

                case 'Low':
                    $(row).find('td:eq(2)').addClass('lowColor');
                    break;

                default:
                    break;
            }
        }
    });


   $('#criticalPatchesTable').DataTable({
       "destroy": true, // In order to reinitialize the datatable
       "pagination": true, // For Pagination
       "bPaginate": true,
       "sorting": false, // For sorting
       "ordering": false,
       "searching": true,
       "aaData": [
           { "Patch Name": "K84562", "Severity": "High", "Affected Machines": 2 },
           { "Patch Name": "K74365", "Severity": "High", "Affected Machines": 3 },
           { "Patch Name": "K98578", "Severity": "High", "Affected Machines": 1 },
           { "Patch Name": "543791", "Severity": "High", "Affected Machines": 7 },
           { "Patch Name": "K98352", "Severity": "High", "Affected Machines": 7 },
           { "Patch Name": "K97475", "Severity": "High", "Affected Machines": 2 },
           { "Patch Name": "K65190", "Severity": "High", "Affected Machines": 8 },
           { "Patch Name": "K06395", "Severity": "High", "Affected Machines": 3 },
           { "Patch Name": "K18536", "Severity": "High", "Affected Machines": 1 },
           { "Patch Name": "K73934", "Severity": "High", "Affected Machines": 7 },
           { "Patch Name": "K75297", "Severity": "High", "Affected Machines": 5 },
           { "Patch Name": "K07652", "Severity": "High", "Affected Machines": 3 },
           { "Patch Name": "K82049", "Severity": "High", "Affected Machines": 6 },
           { "Patch Name": "K53319", "Severity": "High", "Affected Machines": 4 },
           { "Patch Name": "K11437", "Severity": "High", "Affected Machines": 5 },
           { "Patch Name": "K77510", "Severity": "High", "Affected Machines": 1 }
       ],
       "columns": [{},
       {
           "data": "Patch Name"
       }, {
           "data": "Severity"
       }, {
           "data": "Affected Machines"
       }],
       'columnDefs': [{
           'targets': 0,
           'searchable': true,
           'orderable': false,
           'className': 'dt-body-center',
           'render': function (data, type, full, meta) {
               return '<input type="checkbox" class="form-check-input" name="criPatchCheckbox" value="' + $('<div/>').text(data).html() + '">';
           }
       }],
       'rowCallback': function (row, data, index) {
           switch (data['Severity']) {
               case 'Critical':
                   $(row).find('td:eq(2)').addClass('criticalColor');
                   break;

               case 'High':
                   $(row).find('td:eq(2)').addClass('highColor');
                   break;

               case 'Medium':
                   $(row).find('td:eq(2)').addClass('mediumColor');
                   break;

               case 'Low':
                   $(row).find('td:eq(2)').addClass('lowColor');
                   break;

               default:
                   break;
           }
       }
     });
    

});



</script>


<body>

<html:form name ="newDashboardForm" action="/newDashboard.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.NewDashboardViewForm">
<html:hidden property="action"/>

  <jsp:include page="header.jsp" />
  <jsp:include page="sidebar.jsp" />

  
 <main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename">Dashboard</span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="DefenSight Dashboard"><i
              class="fa-solid fa-circle-info text-primary"></i></span>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Refresh"><i class="fa-solid fa-arrows-rotate"></i></div>
        <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Download">
          <i class="fa-solid fa-download"></i>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center"> <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left"
              style="margin-right: 5px;"></i>Back to CMS </a>
        </div>
      </div>

    </div>

    <section class="section dashboard">
      <div class="row">
        <div class="col-lg-8">

          <div class="row">
            <div class="col-lg-12">
              <div class="row">

                <div class="col-md-2">
                  <div class="card info-card customers-card">
                    <div class="card-body">
                      <h5 class="card-title" style="margin: 0;">Total</h5>
                      <hr class="divider" />
                      <div class="row">
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa fa-laptop"></i><br />
                              <span class="text-muted small"><bean:write name="newDashboardForm" property="machinesCount"/></span><br />
                              <span class="text-muted small">Enrolled</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>


                <div class="col-md-4">
                  <div class="card info-card">
                    <div class="card-body">
                      <h5 class="card-title" style="margin: 0;">OS Types</h5>
                      <hr class="divider" />
                      <div class="row">
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa-brands fa-windows"></i><br />
                              <span class="text-muted small"><bean:write name="newDashboardForm" property="machineWindowsCount"/></span><br />
                              <span class="text-muted small">Windows</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa-brands fa-linux"></i><br />
                              <span class="text-muted small"><bean:write name="newDashboardForm" property="machineLinuxCount"/></span><br />
                              <span class="text-muted small">Linux</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa-brands fa-apple"> </i><br />
                              <span class="text-muted small"><bean:write name="newDashboardForm" property="machineMacCount"/></span><br />
                              <span class="text-muted small">Mac</span>
                            </div>
                          </div>
                        </div>
                      </div>

                    </div>
                  </div>
                </div>



                <div class="col-md-6">
                  <div class="card info-card customers-card">
                    <div class="card-body">
                      <h5 class="card-title" style="margin: 0;">Scanned Devices <span>| In last 24 hours</span></h5>
                      <hr class="divider" />

                      <div class="row">
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa fa-crosshairs"></i><br />
                              <span class="text-muted small"><bean:write name="newDashboardForm" property="vscanCount"/></span><br />
                              <span class="text-muted small">By Vulnerability</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa-regular fa-circle-check"></i><br />
                              <span class="text-muted small"><bean:write name="newDashboardForm" property="patchScanCount"/></span><br />
                              <span class="text-muted small">By Patch</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-12">
              <div class="card recent-sales overflow-auto">
                <div class="filter">
                  <a class="icon" href="#" data-bs-toggle="dropdown"><i class="fa-solid fa-sliders"></i></a>
                  <ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
                    <li class="dropdown-header text-start">
                      <h6>Filter</h6>
                    </li>
                    <li><a class="dropdown-item">Patch applied</a></li>
                    <li><a class="dropdown-item">Patch applied but not scanned</a></li>
                    <li><a class="dropdown-item">Patch not applied</a></li>
                  </ul>
                </div>
                <div class="card-body">
                  <h5 class="card-title">Top Vulnerabilities <span>| Patch applied</span>
                    <!-- Button trigger modal -->
                    <button type="button" class="btn btn-primary btn-sm" data-bs-toggle="modal"
                      data-bs-target="#topVulModal" style="margin-left: 20px;">
                      Mitigate Selected
                    </button>
                  </h5>

                  <table id="topVulTable" class="table table-borderless" style="width: 100%;">
                    <thead>
                      <tr>
                        <th><input type="checkbox" class="selectAll form-check-input" id="topVulSelectAll"></th>
                        <th scope="col">CVE-ID</th>
                        <th scope="col">Severity</th>
                        <th scope="col">Impacted Machines</th>
                        <th scope="col">Patches Available</th>
                      </tr>
                    </thead>
                    <tbody></tbody>
                  </table>
                </div>
              </div>
            </div>
            
            
            <div class="col-12">
              <div class="card overflow-auto">
                <div class="filter">
                  <a class="icon" href="#" data-bs-toggle="dropdown"><i class="fa-solid fa-sliders"></i></a>
                  <ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
                    <li class="dropdown-header text-start">
                      <h6>Filter</h6>
                    </li>
                    <li><a class="dropdown-item">Critical</a></li>
                    <li><a class="dropdown-item">High</a></li>
                    <li><a class="dropdown-item">Medium</a></li>
                    <li><a class="dropdown-item">Low</a></li>
                  </ul>
                </div>
                <div class="card-body">
                  <h5 class="card-title">Priority Patches <span>| Severity - High</span>
                    <!-- Button trigger modal -->
                    <button type="button" class="btn btn-primary btn-sm" data-bs-toggle="modal"
                    data-bs-target="#priPatchesModal" style="margin-left: 20px;">
                    Mitigate Selected
                  </button>
                  </h5>
                  <table id="criticalPatchesTable" class="table" style="width: 100%;">
                    <thead>
                      <tr>
                        <th scope="col"><input type="checkbox" id="criticalPatchesSelectAll"
                            class="selectAll form-check-input"></th>
                        <th scope="col">Patch Name</th>
                        <th scope="col">Severity</th>
                        <th scope="col">Affected Machines</th>
                      </tr>
                    </thead>
                    <tbody>

                    </tbody>
                  </table>
                </div>
              </div>
            </div>
            
          </div>



        </div>


        <div class="col-lg-4">

          <div class="card">

            <div class="card-body pb-0">
              <h5 class="card-title">Vulnerability Statistics <span>| Severity</span></h5>
              <div class="row" style="margin-bottom: 10px;">
              <bean:define id="statusBean" name="newDashboardForm" property="vulnerableStatusBean"/>
                <div align="center">
                  <h4><bean:write name="newDashboardForm" property="totalVulnerable"/></h4>
                  <h6>Total Vulnerabilities </h6>
                </div>
              </div>
              <div class="row" style="margin-bottom: 10px;">
                <div class="col">
                  <div align="center">
                    <span class="small"><bean:write name="statusBean" property="critical"/></span><br />
                    <span class="small criticalColor"><b>Critical</b></span>
                  </div>
                </div>
                <div class="col">
                  <div align="center">
                    <span class="small"><bean:write name="statusBean" property="high"/></span><br />
                    <span class="small highColor"><b>High</b></span>
                  </div>
                </div>
                <div class="col">
                  <div align="center">
                    <span class="small"><bean:write name="statusBean" property="medium"/></span><br />
                    <span class="small mediumColor"><b>Medium</b></span>
                  </div>
                </div>
                <div class="col">
                  <div align="center">
                    <span class="small"><bean:write name="statusBean" property="low"/></span><br />
                    <span class="small lowColor"><b>Low</b></span>
                  </div>
                </div>
              </div>
              <div>
                <div style="position: relative; width: 100%; margin: auto;">
                  <canvas id="vulStatsDonutChart" style="margin:auto;">
                  </canvas>
                </div>
              </div>

            </div>
          </div>
          
          
          
          <div class="card">
            <div class="card-body pb-0">
              <h5 class="card-title">Vulnerability Aging</h5>
              <div>
                <div>
                  <div style="display: block; padding: 0 10px 20px 10px;">
                    <canvas id="vulAgingScatter" style="min-height: 250px;">
                    </canvas>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          
          
          <div class="card overflow-auto">
              <div class="card-body">
                <h5 class="card-title">Machine Compliance</h5>

                <div class="row compliance">
                  <div class="col-md-4">
                    <p>Reporting</p>
                    <span> Not checked-in </span><br/>
                    <span data-bs-toggle="modal" data-bs-target="#reportingModal"
                        style="color: #FF5F60; text-decoration: underline; cursor: pointer;">
                        <bean:write name="newDashboardForm" property="reportingNotCheckedIn"/>
                      </span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="reportingNotCheckedIn" class="progress-bar" role="progressbar"
                        style="width: 15%; background-color: #FF5F60" aria-valuemax="100"></div>
                    </div>
                    <span> Not available </span><br/>
                    <span style="color: #F3CC63;"><bean:write name="newDashboardForm" property="reportingNotAvailable"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="reportingNotAvailable" class="progress-bar" role="progressbar"
                        style="width: 35%; background-color: #F3CC63" aria-valuemax="100"></div>
                    </div>
                    <span> Checked-in </span><br/>
                    <span style="color: #18db76;"><bean:write name="newDashboardForm" property="reportingCheckedIn"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="reportingCheckedIn" class="progress-bar" role="progressbar"
                        style="width: 50%; background-color: #18db76" aria-valuemax="100"></div>
                    </div>
                  </div>

                  <div class="col-md-4">
                    <p>Security</p>
                    <span> Non Compliant </span><br/> 
                    <span style="color: #FF5F60;"><bean:write name="newDashboardForm" property="securityNonCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="securityNonCompliant" class="progress-bar" role="progressbar"
                        style="width: 10%; background-color: #FF5F60" aria-valuemax="100"></div>
                    </div>
                    <span> Compliant</span> <br/>
                    <span style="color: #18db76;"><bean:write name="newDashboardForm" property="securityCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="securityCompliant" class="progress-bar" role="progressbar"
                        style="width: 90%; background-color: #18db76" aria-valuemax="100"></div>
                    </div>
                  </div>


                  <div class="col-md-4">
                    <p>Patches</p>
                    <span> Non Compliant </span><br/>
                    <span style="color: #FF5F60;"><bean:write name="newDashboardForm" property="patchNonCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="patchNonCompliant" class="progress-bar" role="progressbar"
                        style="width: 20%; background-color: #FF5F60" aria-valuemax="100"></div>
                    </div>
                    <span> Compliant </span><br/>
                    <span style="color: #18db76;"><bean:write name="newDashboardForm" property="patchCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="patchCompliant" class="progress-bar" role="progressbar"
                        style="width: 80%; background-color: #18db76" aria-valuemax="100"></div>
                    </div>
                  </div>


                </div>

              </div>
            </div>

        </div>
      </div>
    </section>

    

    <!-- Top Vulnerability Modal -->
    <div class="modal fade" id="topVulModal" tabindex="-1" aria-labelledby="topVulModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="topVulModalLabel">Apply patches or Scan machines</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <table id="topVulModalTable" class="table table-borderless" style="width: 100%;">
              <thead>
                <tr>
                  <th><input type="checkbox" class="selectAll form-check-input" id="topVulModalTableSelectAll">
                  </th>
                  <th scope="col">Impacted Machine</th>
                  <th scope="col">Status</th>
                  <th scope="col">Patch Details</th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-primary btn-sm" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-outline-primary btn-sm">Scan</button>
            <button type="button" class="btn btn-primary btn-sm">Apply Patches</button>
          </div>
        </div>
      </div>
    </div>


    <!-- Priority Patches Modal -->
    <div class="modal fade" id="priPatchesModal" tabindex="-1" aria-labelledby="priPatchesModalLabel"
      aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="priPatchesModalLabel">Apply patches or Scan machines</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <table id="priPatchesModalTable" class="table table-borderless" style="width: 100%;">
              <thead>
                <tr>
                  <th><input type="checkbox" class="selectAll form-check-input" id="priPatchesModalTableSelectAll">
                  </th>
                  <th scope="col">Impacted Machine</th>
                  <th scope="col">Status</th>
                  <th scope="col">Patch Details</th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-primary btn-sm" data-bs-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-outline-primary btn-sm">Scan</button>
            <button type="button" class="btn btn-primary btn-sm">Apply Patches</button>
          </div>
        </div>
      </div>
    </div>



    <!-- Reporting Modal -->
    <div class="modal fade" id="reportingModal" tabindex="-1" aria-labelledby="reportingModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="reportingModalLabel">Machined not checked in last 24 hours</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <table id="reportingModalTable" class="table table-borderless" style="width: 100%;">
              <thead>
                <tr>
                  <th><input type="checkbox" class="selectAll form-check-input" id="reportingModalTableSelectAll">
                  </th>
                  <th scope="col">Machine Name</th>
                  <th scope="col">Vulnerabilities</th>
                  <th scope="col">Last Scanned Vulnerability</th>
                  <th scope="col">Last Patch Applied</th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal">Close</button>
          </div>
        </div>
      </div>
    </div>
  </main>

</html:form>
</body>
</html>
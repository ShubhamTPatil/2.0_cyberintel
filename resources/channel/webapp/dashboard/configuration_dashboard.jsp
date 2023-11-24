<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Abhinav Satpute -->
--%>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>

<!DOCTYPE html>
<html lang="en">
<head>

<title>DefenSight</title>

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

<script type="text/javascript" src="/spm/js/newdashboard/chartjs.hammer.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chartjs-plugin-zoom.js"></script>

<script type="text/javascript">

$(function () {

  $('#dashboard').addClass('nav-selected');

    var pieChartData = [];
    pieChartData.push(<bean:write name="configDashboardForm" property="configProfileCompliant"/>);
    pieChartData.push(<bean:write name="configDashboardForm" property="configProfileNonCompliant"/>);


var ctx1 = $("#complianceDonutChart");
    // label: "Severity",
    var chart1 = new Chart(ctx1, {
        type: "doughnut",
        data: {
            labels: ["Compliant", "Non Compliant"],
            datasets: [
                {
                    data: pieChartData,
                    backgroundColor: [
                        "#71DCEB", "#FF5F60"
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
                    position: "bottom"
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            let label = context.dataset.label || '';
                            return label + " " + context.parsed + "" + (context.parsed == 1) ? 'Machine' : ' Machines';
                        }
                    }
                }
            }
        }
    });


  var ctx = document.getElementById('myChart').getContext('2d');

  var barChartSeverityData = '<bean:write name="configDashboardForm" property="barChartData"/>';
  barChartSeverityData = barChartSeverityData.replace(/&quot;/g,'"');
  barChartSeverityData=JSON.parse(barChartSeverityData);


  const dataChartData = {
    labels: ["Stig 1", "Stig 2", "Stig 3", "Stig 4", "Stig 5", "Stig 6"],
    datasets: [
      {
        label: "Compliant",
        backgroundColor: "rgba(255, 99, 132)",
        data: [10, 15, 13, 20, 18, 25],
        stack: "Stack 0",
      },
      {
        label: "Non-Compliant",
        backgroundColor: "rgba(201, 203, 207)",
        data: [5, 8, 10, 12, 15, 20],
        stack: "Stack 0",
      },
      {
        label: "Unknown",
        backgroundColor: "rgba(255, 205, 86)",
        data: [5, 8, 10, 12, 15, 20],
        stack: "Stack 0",
      }
    ],
  };

  //console.log(barChartSeverityData);

  const config = {
    type: "bar",
    data: barChartSeverityData,
    options: {
      plugins: {
        title: {
          display: false,
          text: "Chart.js Bar Chart - Stacked",
        },
        legend: {
          display: true,
          position: "bottom",
        },
      },
      responsive: false,
      interaction: {
        intersect: false,
      },
      scales: {
        x: {
          stacked: true,
          beginAtZero: true,
          title: {
            display: true,
            text: "Security Technical Implementation Guides (STIGs)"
          }
        },
        y: {
          stacked: true,
          title: {
            display: true,
            text: "Number of Machines"
          }
        }
      },
      onClick: handleBarChartClick,
    },
  };

  var barChart = new Chart(ctx, config);

  

  
  function showMachinesModalTable(machinesModalData) {
    
    $('#machinesModalTable').DataTable().clear();

    let machinesModalTable = $('#machinesModalTable').DataTable({
         "destroy": true, // In order to reinitialize the datatable
         "pagination": true, // For Pagination
         "bPaginate": true,
         "sorting": false, // For sorting
         "ordering": false,
         "searching": true,
         language: {
             search: "_INPUT_",
             searchPlaceholder: "Search..."
         },
         "aaData": machinesModalData,
         "columns": [
          {
            "data": "machineName",
            "title": "Machine Name"
          },
         {
            "data": "profileName",
            "title": "Profile Title"
         }, {
            "title": "Result",
            "data": "rulesCompliance"
         }, {}],
         'columnDefs': [{
             'targets': 3,
             'searchable': false,
             'orderable': false,
             'className': 'dt-body-center',
             'render': function (data, type, full, meta) {
                let machineName = '\''+full["machineName"]+'\'';
                return '<input type="button" onClick="showConfigReport('+machineName+','+full["contentId"]+','+full["profileId"]+')" class="view-details btn btn-sm btn-primary" value="View Details">';
            }
          }]
       });
      // $('#machinesModalTable').on('click','.view-details',() => {
      //   showConfigReport();
      // })
    }
  

  function handleBarChartClick(event, elements) {

    if (elements.length > 0) {
      var datasetIndex = elements[0].datasetIndex;
      var dataIndex = elements[0].index;

      //console.log("datasetIndex = " + datasetIndex);
      //console.log("dataIndex = " + dataIndex);

      var value = barChartSeverityData.datasets[datasetIndex].data[dataIndex];
      //console.log('Clicked on value:', value);
      //console.log('Clicked on:', barChartSeverityData.labels[dataIndex], ', ', barChartSeverityData.datasets[datasetIndex].label);

      $('#machinesModalLabel').html(barChartSeverityData.labels[dataIndex] + ' - ' + barChartSeverityData.datasets[datasetIndex].label);

      //[{"profileName":"xccdf_org.ssgproject.content_profile_C2S","profileId":4,"contentId":2,"rulesCompliance":"63/168","contentTitle":"Guide to the Secure Configuration of CentOS 7","profileTitle":"C2S for Red Hat Enterprise Linux 7","machineName":"vmcentos-qaendpoint","contentName":"xccdf_org.ssgproject.content_benchmark_CENTOS-7"}]

      $.ajax({
        url: './configDashboard.do',
        type: 'POST',
        dataType: 'text json',
        data: {
          action: "getMachineByContent",
          contentId: barChartSeverityData.labels[dataIndex],
          complianceType: barChartSeverityData.datasets[datasetIndex].label
        },
        beforeSend: function () { },
        complete: function (xhr, status) { },
        success: function (response) {
          //console.log(response);
          //console.log(JSON.stringify(response));

          let machinesModalData = [{
            machineName: "vmcentos-qaendpoint",
            profileTitle: "C2S for Red Hat Enterprise Linux 7",
            result: "63/168",
            profileId: "4",
            contentId: "2"
          }];

          showMachinesModalTable(response);

          var machinesModal = new bootstrap.Modal(document.getElementById('machinesModal'), {
            keyboard: false
          })
          machinesModal.show();
        }
      });

    }

    //http://localhost:8888/spm/rule_results.do?action=rule_details&machine=vmcentos-qaendpoint&contentId=2&profileId=4&queryDisplayPath=%2FConfiguration%20Assessment%2FMachine%20Level%20Compliance

    /*
    action: rule_details
    machine: vmcentos-qaendpoint
    contentId: 2
    profileId: 4
    queryDisplayPath: /Configuration Assessment/Machine Level Compliance
    */
  }


  //Line Chart
  var ctxLineChart = document.getElementById('lineChart').getContext('2d');

  var chartData = {
    labels: ["Jun 23","Jul 23", "Aug 23", "Sept 23", "Oct 23", "Nov 23" ],
    datasets: [
      {
        label: 'Profile 1',
        borderColor: 'red',
        data: [25, 21, 17, 12, 9, 5]
      },
      {
        label: 'Profile 2',
        borderColor: 'blue',
        data: [50, 38, 31, 25, 19, 9]
      }
    ]
  };

  var lineChartConfig = {
    type: 'line',
    data: chartData,
    options: {
      responsive: false,
      plugins: {
        zoom: {
          zoom: {
            wheel: {
              enabled: false,
            },
            pinch: {
              enabled: false,
            },
            mode: 'xy'
          },
          pan: {
            enabled: false,
            mode: 'xy'
          }
        }
      }
    }
  };

  var myChart = new Chart(ctxLineChart, lineChartConfig);


  });

  function setConfigReportData(data) {

      $('#configReportModalTable').DataTable().clear();

      let configReportModalTable = $('#configReportModalTable').DataTable({
        "destroy": true, // In order to reinitialize the datatable
        "pagination": true, // For Pagination
        "bPaginate": true,
        "sorting": false, // For sorting
        "ordering": false,
        "searching": true,
        language: {
          search: "_INPUT_",
          searchPlaceholder: "Search..."
        },
        "aaData": data,
        "columns": [
          {
            "title": "Title",
            "data": "rule_title",
          },
          {
            "title": "Severity",
            "data": "rule_severity"
          }, {
            "title": "Status",
            "data": "status"
          }, {
            "title": "Solution",
            "data": "rule_fix"
          }],
        'columnDefs': [{
          'targets': 0,
          'className': 'dt-body-left',
        }, {
          'targets': 3,
          'className': 'dt-body-left',
        }]
      });

    }

  function showConfigReport(machineName, contentId, profileId) {
    $.ajax({
        url: './rule_results.do',
        type: 'POST',
        dataType: 'text json',
        data: {
          action: "rule_details",
          machine: machineName,
          contentId: contentId,
          profileId: profileId,
          queryDisplayPath: "/Configuration Assessment/Machine Level Compliance"
        },
        beforeSend: function() {},
        complete: function (xhr, status) {},
        success: function (response) {
          //console.log(response);
          //console.log(JSON.stringify(response));

          $('#configReportModalLabel').html("Configuration assessment results for "+machineName);
          setConfigReportData(response['result']);

          var configReportModal = new bootstrap.Modal(document.getElementById('configReportModal'), {
            keyboard: false
          });
          configReportModal.show();
    }});
  }


</script>

<body>
<html:form name ="configDashboardForm" action="/configDashboard.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.ConfigDashboardViewForm">
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
        <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i></div>
        <div class="p-2 bd-highlight text-primary align-self-center"> <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left"
              style="margin-right: 5px;"></i>CMS Home</a>
        </div>
      </div>

    </div>

    <section class="section dashboard">

      
      <nav style="background-color: #fff;">
        <div class="nav nav-tabs nav-title" id="nav-tab" role="tablist">
          <a href="/spm/newDashboard.do" class="nav-link">Vulnerability Assessment</a>
          <button class="nav-link active" id="nav-home-tab" data-bs-toggle="tab" data-bs-target="#nav-home"
            type="button" role="tab" aria-controls="nav-home" aria-selected="true"
            style="background-color: #fff; z-index:1;">Configuration Assessment</button>
        </div>
      </nav>

      <br/>

      <div class="tab-content" id="nav-tabContent">

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
                                <span class="text-muted small"><bean:write name="configDashboardForm" property="machinesCount"/></span><br />
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
                                <span class="text-muted small"><bean:write name="configDashboardForm" property="machineWindowsCount"/></span><br />
                                <span class="text-muted small">Windows</span>
                              </div>
                            </div>
                          </div>
                          <div class="col">
                            <div class="row d-flex justify-content-center">
                              <div align="center">
                                <i class="fa-brands fa-linux"></i><br />
                                <span class="text-muted small"><bean:write name="configDashboardForm" property="machineLinuxCount"/></span><br />
                                <span class="text-muted small">Linux</span>
                              </div>
                            </div>
                          </div>
                          <div class="col">
                            <div class="row d-flex justify-content-center">
                              <div align="center">
                                <i class="fa-brands fa-apple"> </i><br />
                                <span class="text-muted small"><bean:write name="configDashboardForm" property="machineMacCount"/></span><br />
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
                                <span class="text-muted small"><bean:write name="configDashboardForm" property="configScanCount"/></span><br />
                                <span class="text-muted small">Configuration Scan</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-lg-12">
            
                <div class="card">
                  <div class="card-body pb-0">
                    <h5 class="card-title">Failed Rules for different Profiles</h5>
                    <div>
                      <div>
                        <div style="display: block; padding: 0 10px 16px 10px;">
                          <canvas id="myChart" style="width: 100%"></canvas>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
              </div>

            </div>
          </div>


          <div class="col-lg-4">
            <div class="card">
              <div class="card-body pb-0">
                <h5 class="card-title">Overall Compliance Summary</h5>
                <hr class="divider" />
                
                <div class="row" style="margin-bottom: 10px;">
                  <div class="col-sm-12">
                    <div align="center">
                      <span class="small"><bean:write name="configDashboardForm" property="configProfileCompliant"/></span><br />
                      <span class="small lowColor"><b>Compliant Machines</b></span>
                    </div>
                  </div>
                  <div class="col-sm-12">
                    <div align="center">
                      <span class="small"><bean:write name="configDashboardForm" property="configProfileNonCompliant"/></span><br />
                      <span class="small criticalColor"><b>Non-Compliant Machines</b></span>
                    </div>
                  </div>
                </div>
                <br/>
                <div>
                <div style="position: relative; width: 100%; margin: auto;">
                  <canvas id="complianceDonutChart" style="margin:auto; min-height: 130px;">
                  </canvas>
                </div>
                </div>
                <br/>
              </div>
            </div>
          </div>


        </div>


        <div class="row">
          <div class="col-lg-12">
            <div class="card">
              <div class="card-body pb-0">
                <h5 class="card-title">History Trend</h5>
                <div style="position: relative; width: 100%; margin: auto;">
                  <canvas id="lineChart" style="width: 100%"></canvas>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Modal -->
    <div class="modal fade" id="machinesModal" tabindex="-1" aria-labelledby="machinesModalLabel" aria-hidden="true">
      <div class="modal-dialog" style="max-width:800px;">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="machinesModalLabel"></h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <table id="machinesModalTable" class="table table-borderless" style="width: 100%;">
              <thead>
                <tr>
                  <th scope="col">Machine Name</th>
                  <th scope="col">Profile Title</th>
                  <th scope="col">Result</th>
                  <th scope="col"></th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-primary btn-sm" data-bs-dismiss="modal">Cancel</button>
          </div>
        </div>
      </div>
    </div>


    <!-- Modal -->
    <div class="modal fade" id="configReportModal" tabindex="-1" aria-labelledby="configReportModalLabel" aria-hidden="true">
      <div class="modal-dialog" style="max-width:90%;">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="configReportModalLabel"></h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <table id="configReportModalTable" class="table table-striped table-bordered" style="width: 100%;">
              <thead>
                <tr>
                  <th scope="col">Title</th>
                  <th scope="col">Severity</th>
                  <th scope="col">Status</th>
                  <th scope="col">Solution</th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-outline-primary btn-sm" data-bs-dismiss="modal">Cancel</button>
          </div>
        </div>
      </div>
    </div>

  </main>
</body>
</html>

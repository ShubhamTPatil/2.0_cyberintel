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

  /*
  const data = {
    labels: ["January", "February", "March", "April", "May", "June"],
    datasets: [
      {
        label: "Dataset 1",
        backgroundColor: "rgba(255, 99, 132, 0.2)",
        borderColor: "rgb(255, 99, 132)",
        borderWidth: 1,
        data: [10, 15, 13, 20, 18, 25],
        stack: "Stack 0",
      },
      {
        label: "Dataset 2",
        backgroundColor: "rgba(201, 203, 207, 0.2)",
        borderColor: "rgb(201, 203, 207)",
        borderWidth: 1,
        data: [5, 8, 10, 12, 15, 20],
        stack: "Stack 0",
      },
      {
        label: "Dataset 3",
        backgroundColor: "rgba(255, 205, 86, 0.2)",
        borderColor: "rgb(255, 205, 86)",
        borderWidth: 1,
        data: [5, 8, 10, 12, 15, 20],
        stack: "Stack 0",
      }
    ],
  }; */

  console.log(barChartSeverityData);

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
        }
      },
      responsive: false,
      interaction: {
        intersect: false,
      },
      scales: {
        x: {
          stacked: true,
        },
        y: {
          stacked: true,
        },
      },
    },
  };

  var barChart = new Chart(ctx, config);



  /* Line Chart
  var ctxLineChart = document.getElementById('lineChart').getContext('2d');

  var chartData = {
    labels: ["January", "February", "March", "April", "May", "June"],
    datasets: [
      {
        label: 'Dataset 1',
        borderColor: 'red',
        data: [10, 15, 13, 20, 18, 25]
      },
      {
        label: 'Dataset 2',
        borderColor: 'blue',
        data: [5, 8, 10, 12, 15, 20]
      }
    ]
  };

  var lineChartConfig = {
    type: 'line',
    data: chartData,
    options: {
      responsive: false,
      scales: {
        y: {
          min: 0,
          max: 30
        }
      },
      plugins: {
        zoom: {
          zoom: {
            wheel: {
              enabled: true,
            },
            pinch: {
              enabled: true,
            },
            mode: 'xy'
          },
          pan: {
            enabled: true,
            mode: 'xy'
          }
        }
      }
    }
  };

  var myChart = new Chart(ctxLineChart, lineChartConfig);
  */

  });


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

        <!--
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
        -->

      </div>


    </section>
  </main>
</body>
</html>

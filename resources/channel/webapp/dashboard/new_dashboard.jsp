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

<!--
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/jquery.dataTables.min.css"/>

<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/jquery.dataTables.min.js"></script>
-->

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet">

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-icons/1.10.2/font/bootstrap-icons.min.css"
  integrity="sha512-YFENbnqHbCRmJt5d+9lHimyEMt8LKSNTMLSaHjvsclnZGICeY/0KYEeiHwD1Ux4Tcao0h60tdcMv+0GljvWyHg=="
  crossorigin="anonymous" referrerpolicy="no-referrer" />

<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.2.1/css/all.min.css"
  integrity="sha512-MV7K8+y+gLIBoVD59lQIYicR65iaqukzvf/nwasF0nqhPay5w/9lJmVM2hMDcnK1OnMGCdVK+iQrJ7lzPJQd1w=="
  crossorigin="anonymous" referrerpolicy="no-referrer" />

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.13.1/css/jquery.dataTables.min.css">

<script src="https://code.jquery.com/jquery-3.6.1.slim.min.js"
  integrity="sha256-w8CvhFs7iHNVUtnSP0YKEg00p9Ih13rlL9zGqvLdePA=" crossorigin="anonymous"></script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>

<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.0.1/chart.umd.js"
  integrity="sha512-gQhCDsnnnUfaRzD8k1L5llCCV6O9HN09zClIzzeJ8OJ9MpGmIlCxm+pdCkqTwqJ4JcjbojFr79rl2F1mzcoLMQ=="
  crossorigin="anonymous" referrerpolicy="no-referrer"></script>

<script type="text/javascript" charset="utf8"
  src="https://cdn.datatables.net/1.13.1/js/jquery.dataTables.min.js"></script>


<style type="text/css">
  .sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    bottom: 0;
    width: 300px;
    z-index: 996;
    transition: all 0.3s;
    padding: 20px;
    overflow-y: auto;
    box-shadow: 0px 0px 20px rgb(1 41 112 / 10%);
    background-color: #fff;
  }

  .sidebar-nav {
    padding: 0;
    margin: 0;
    list-style: none
  }

  .sidebar-nav li {
    padding: 0;
    margin: 0;
    list-style: none;
  }

  .sidebar-nav .nav-item {
    margin-bottom: 5px;
  }

  .sidebar-nav .nav-link {
    display: flex;
    align-items: center;
    font-size: 15px;
    font-weight: 600;
    color: rgb(126, 126, 126);
    transition: 0.3;
    background: #fff;
    padding: 10px 15px;
    border-radius: 4px;
  }

  .sidebar-nav .nav-link:hover {
    color: #4154f1;
    background: #f6f9ff;
  }

  .sidebar-nav .nav-selected {
    color: #4154f1;
    background: #f6f9ff;
  }




  #main {
    margin-top: 60px;
    padding: 20px 30px;
    transition: all 0.3s;
    margin-left: 85px;
  }

  .filter {
    position: absolute;
    right: 0px;
    top: 15px;
  }

  .filter .icon {
    color: #aab7cf;
    padding-right: 20px;
    padding-bottom: 5px;
    transition: 0.3s;
    font-size: 16px;
  }

  #vulStatsDonutChart {
    padding: 10px;
  }

  .card {
    margin-bottom: 30px;
    border: none;
    border-radius: 5px;
    box-shadow: 0px 0 30px rgb(1 41 112 / 10%);
  }

  .card-title {
    padding: 20px 0 15px 0;
    font-size: 18px;
    font-weight: 500;
    color: #012970;
    font-family: "Poppins", sans-serif;
  }

  .card-title span {
    color: #899bbd;
    font-size: 14px;
    font-weight: 400;
  }

  hr.divider {
    margin: 0 0 10px 0;
    border-width: 2px;
  }

  .card-body {
    flex: 1 1 auto;
    padding: 0rem 1rem 1rem 1rem;
  }
</style>
</head>

<script type="text/javascript">
  $(function () {
    var ctx1 = $("#vulStatsDonutChart");
    // label: "Severity",
    var chart1 = new Chart(ctx1, {
      type: "doughnut",
      data: {
        labels: ["Critical", "High", "Medium", "Low"],
        datasets: [
          {
            data: [10, 50, 25, 70],
            backgroundColor: [
              "#FF5F60", "#D4733A", "#F3CC63", "#71DCEB"
            ],
            borderColor: [
              "#ff3939",
              "#ff7221",
              "#ffcc40",
              "#1ce4ff"
            ],
            borderWidth: [1, 1, 1, 1]
          }
        ]
      },
      options: {
        responsive: true,
        title: {
          display: true,
          position: "top",
          text: "Doughnut Chart",
          fontSize: 18,
          fontColor: "#111"
        },
        legend: {
          display: false,
          position: "bottom",
          labels: {
            fontColor: "#333",
            fontSize: 16
          }
        }
      }
    });

    $('#topVulTable').DataTable({
      "destroy": true, // In order to reinitialize the datatable
      "pagination": true, // For Pagination
      "sorting": false, // For sorting
      "ordering": false,
      "checkboxes": {
        "selectRow": true
      },
      "aaData": [
        {
          "CVE-ID": "123456",
          "Severity": "Critical",
          "Affected Target": 1234,
          "Patches": "123456"
        },
        {
          "CVE-ID": "123456",
          "Severity": "Critical",
          "Affected Target": 1234,
          "Patches": "123456"
        }
      ],
      "columns": [{},
      {
        "data": "CVE-ID"
      }, {
        "data": "Severity"
      }, {
        "data": "Affected Target"
      }, {
        "data": "Patches"
      }],
      'columnDefs': [{
        'targets': 0,
        'searchable': false,
        'orderable': false,
        'className': 'dt-body-center',
        'render': function (data, type, full, meta) {
          return '<input type="checkbox" name="id[]" value="' + $('<div/>').text(data).html() + '">';
        }
      }]
    });

  });
</script>

<body>

<html:form name ="newDashboardForm" action="/newDashboard.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.NewDashboardViewForm">
<html:hidden property="action"/>

  <aside id="sidebar" class="sidebar" style="height:100%; width: 85px;">
    <ul class="sidebar-nav" id="sidebar-nav">
      <li class="nav-item"> <a class="nav-link nav-selected" href="#"> <i class="bi bi-grid"></i> </a></li>
      <li class="nav-item">
        <a class="nav-link collapsed" data-bs-target="#forms-nav" data-bs-toggle="collapse" href="#"> <i
            class="bi bi-box-arrow-down"></i> </a>
      </li>
      <li class="nav-item">
        <a class="nav-link collapsed" data-bs-target="#components-nav" data-bs-toggle="collapse" href="#"> <i
            class="bi bi-menu-button-wide"></i> </a>
      </li>
    </ul>
  </aside>

  <main id="main" class="main">
    <div class="pagetitle">
      Dashboard
    </div>
    <section class="section dashboard">
      <div class="row">
        <div class="col-lg-8">

          <div class="row">
            <div class="col-12">
              <div class="row">
                <div class="col-2">
                  <div class="card info-card customers-card">
                    <div class="card-body">
                      <h5 class="card-title" style="margin: 0;">
                        <span>Total</span>
                      </h5>
                      <hr class="divider" />
                      <div class="row">
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="bi bi-laptop"></i><br />
                              <span class="text-muted small">201</span><br />
                              <span class="text-muted small">Enrolled</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>


                <div class="col-4">
                  <div class="card info-card">
                    <div class="card-body">
                      <h5 class="card-title" style="margin: 0;">
                        <span>OS Types</span>
                      </h5>
                      <hr class="divider" />
                      <div class="row">
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa-brands fa-windows"></i><br />
                              <span class="text-muted small">51</span><br />
                              <span class="text-muted small">Windows</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="fa-brands fa-linux"></i><br />
                              <span class="text-muted small">0</span><br />
                              <span class="text-muted small">Linux</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="bi bi-apple"> </i><br />
                              <span class="text-muted small">0</span><br />
                              <span class="text-muted small">Mac</span>
                            </div>
                          </div>
                        </div>
                      </div>

                    </div>
                  </div>
                </div>



                <div class="col">
                  <div class="card info-card customers-card">
                    <div class="card-body">
                      <h5 class="card-title" style="margin: 0;">
                        <span>Scanned Devices</span>
                      </h5>
                      <hr class="divider" />

                      <div class="row">
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="bi bi-phone"></i><br />
                              <span class="text-muted small">15</span><br />
                              <span class="text-muted small">Mobile</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="bi bi-wifi"></i><br />
                              <span class="text-muted small">19</span><br />
                              <span class="text-muted small">IOT</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="bi bi-server"> </i><br />
                              <span class="text-muted small">50</span><br />
                              <span class="text-muted small">Server</span>
                            </div>
                          </div>
                        </div>
                        <div class="col">
                          <div class="row d-flex justify-content-center">
                            <div align="center">
                              <i class="bi bi-laptop"></i><br />
                              <span class="text-muted small">0</span><br />
                              <span class="text-muted small">Laptop/Desktop</span>
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
                  <a class="icon" href="#" data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></a>
                  <ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
                    <li class="dropdown-header text-start">
                      <h6>Filter</h6>
                    </li>
                    <li><a class="dropdown-item">Severity</a></li>
                    <li><a class="dropdown-item">Age</a></li>
                  </ul>
                </div>
                <div class="card-body">
                  <h5 class="card-title">Top Vulnerabilities <span>| Severity</span>
                    <a href="#"><button type="button" class="btn btn-primary" style="margin-left: 10px;">Mitigate
                        Selected</button></a>
                  </h5>
                  <!-- <table class="table table-borderless datatable"> -->
                  <table id="topVulTable" class="table table-borderless">
                    <thead>
                      <tr>
                        <th><input type="checkbox" name="select_all" value="1" id="example-select-all"></th>
                        <th scope="col">CVE-ID</th>
                        <th scope="col">Severity</th>
                        <th scope="col">Affected Target</th>
                        <th scope="col">Patches</th>
                      </tr>
                    </thead>
                    <tbody>

                      <!-- <tr *ngFor="let row of topVulnerabilities">
                        <td scope="row"><input type="checkbox" class="form-check-input" /></td>
                        <td><a class="text-primary">{{ row.id }}</a></td>
                        <td><span>{{row.severity}}</span></td>
                        <td><span>{{row.affctedTarget}}</span></td>
                        <td><a class="text-primary">{{row.patches}} patches <i class="bi bi-arrow-down"></i></a></td>
                      </tr> -->

                    </tbody>
                  </table>
                </div>
              </div>
            </div>

          </div>



        </div>
        <div class="col-lg-4">

          <div class="card">
            <div class="filter">
              <a class="icon" href="#" data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></a>
              <ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
                <li class="dropdown-header text-start">
                  <h6>Filter</h6>
                </li>
                <li><a class="dropdown-item">Severity</a></li>
                <li><a class="dropdown-item">Age</a></li>
              </ul>
            </div>
            <div class="card-body pb-0">
              <h5 class="card-title">Vulnerability statistics <span>| Severity</span></h5>
              <div class="row" style="margin-bottom: 10px;">
                <div align="center">
                  <h4>500</h4>
                  <h6>Total Vulnerabilities </h6>
                </div>
              </div>
              <!-- <div id="trafficChart" style="min-height: 350px;" class="echart"></div> -->
              <!-- <div echarts [options]="chartOption" class="echart"></div> -->
              <div class="row" style="margin-bottom: 10px;">
                <div class="col">
                  <div align="center">
                    <span class="small">100</span><br />
                    <span class="small" style="color:#FF5F60"><b>Critical</b></span>
                  </div>
                </div>
                <div class="col">
                  <div align="center">
                    <span class="small">100</span><br />
                    <span class="small" style="color:#D4733A"><b>High</b></span>
                  </div>
                </div>
                <div class="col">
                  <div align="center">
                    <span class="small">100</span><br />
                    <span class="small" style="color:#F3CC63"><b>Medium</b></span>
                  </div>
                </div>
                <div class="col">
                  <div align="center">
                    <span class="small">200</span><br />
                    <span class="small" style="color:#71DCEB"><b>Low</b></span>
                  </div>
                </div>
              </div>
              <div>
                <div style="display: block" style="padding: 0 10px 20px 10px">
                  <canvas id="vulStatsDonutChart" style="max-height: 400px;">
                  </canvas>
                </div>
              </div>

            </div>
          </div>


        </div>
      </div>
    </section>

    </div>
</html:form>
</body>



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

<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>


<style type="text/css">
  body {
    font-family: "Open Sans", sans-serif;
    background: #f6f9ff;
    color: #444444;
  }

  @media (max-width: 1199px) {
    .toggle-sidebar .sidebar {
      left: 0;
    }
  }

  @media (min-width: 1200px) {

    .toggle-sidebar #main,
    .toggle-sidebar #footer {
      margin-left: 0;
    }

    .toggle-sidebar .sidebar {
      left: -300px;
    }
  }



  .header {
    transition: all 0.5s;
    z-index: 997;
    height: 60px;
    box-shadow: 0px 2px 20px rgb(1 41 112 / 10%);
    background-color: #fff;
    padding-left: 20px;
  }

  .header .toggle-sidebar-btn {
    font-size: 32px;
    padding-left: 10px;
    cursor: pointer;
    color: #012970;
  }

  .header-nav .nav-profile i {
    max-height: 36px;
  }

  .header-nav>ul {
    margin: 0;
    padding: 0;
  }

  .header-nav ul {
    list-style: none;
  }

  .header-nav .nav-profile {
    color: #012970;
  }

  .header-nav .nav-profile span {
    font-size: 14px;
    font-weight: 600;
  }

  .dropdown-menu {
    border-radius: 4px;
    padding: 10px 0;
    -webkit-animation-name: dropdown-animate;
    animation-name: dropdown-animate;
    -webkit-animation-duration: 0.2s;
    animation-duration: 0.2s;
    -webkit-animation-fill-mode: both;
    animation-fill-mode: both;
    border: 0;
    box-shadow: 0 5px 30px 0 rgb(82 63 105 / 20%);
  }

  .header-nav .profile .dropdown-item {
    font-size: 14px;
    padding: 10px 15px;
    transition: 0.3s;
  }

  .header-nav .profile .dropdown-item i {
    margin-right: 10px;
    font-size: 18px;
    line-height: 0;
  }

  .dropdown-menu .dropdown-item i {
    margin-right: 10px;
    font-size: 18px;
    line-height: 0;
  }

  .header-nav .profile {
    min-width: 240px;
    padding-bottom: 0;
  }

  .logo img {
    max-height: 26px;
    margin-right: 6px;
  }



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

  .pagetitle {
    margin-bottom: 10px;
  }

  .pagetitle h1 {
    font-size: 24px;
    margin-bottom: 0;
    font-weight: 600;
    color: #012970;
  }

  h1,
  h2,
  h3,
  h4,
  h5,
  h6 {
    font-family: "Nunito", sans-serif;
  }

  .table {
    font-size: small;
  }

  .table>thead>tr>th {
    text-align: center;
  }

  .table>tbody>tr>td {
    text-align: center;
  }

  .table>thead {
    background-color: #f6f6fe;
  }

  #criticalPatchesTable_filter>label>input {
    margin: 0;
    width: 100%;
  }
</style>


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
          }
        }
      }
    });


    var ctxVulAging = $("#vulAgingScatter");
    var chartVulAgingScatter = new Chart(ctxVulAging, {
      type: 'scatter',
      data: {
        datasets: [{
          label: 'Critical',
          data: [{
            y: 100,
            x: 4
          }],
          backgroundColor: '#FF5F60',
          pointRadius: 5,
          pointHoverRadius: 7
        },
        {
          label: 'High',
          data: [{
            y: 800,
            x: 10
          }],
          backgroundColor: '#D4733A',
          pointRadius: 5,
          pointHoverRadius: 7
        },
        {
          label: 'Medium',
          data: [{
            y: 400,
            x: 2
          }],
          backgroundColor: '#F3CC63',
          pointRadius: 5,
          pointHoverRadius: 7
        },
        {
          label: 'Low',
          data: [{
            x: 3,
            y: 300
          }],
          backgroundColor: '#71DCEB',
          pointRadius: 5,
          pointHoverRadius: 7
        }],
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


    $('#topVulTable').DataTable({
      "destroy": true, // In order to reinitialize the datatable
      "pagination": true, // For Pagination
      "sorting": false, // For sorting
      "ordering": false,
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
          return '<input type="checkbox" class="form-check-input" name="topVulCheckbox" value="' + $('<div/>').text(data).html() + '">';
        }
      }]
    });


    $('#criticalPatchesTable').DataTable({
      "destroy": true, // In order to reinitialize the datatable
      "pagination": true, // For Pagination
      "bPaginate": true,
      "sorting": false, // For sorting
      "ordering": false,
      "searching": true,

      "language": {
        "search": "_INPUT_",
        "searchPlaceholder": "Search..."
      },
      "aaData": [
        {
          "Machine": "Critical",
          "Patches": "Patches 2"
        },
        {
          "Machine": "Critical",
          "Patches": "Patches 2"
        }
      ],
      "columns": [{},
      {
        "data": "Machine"
      }, {
        "data": "Patches"
      }],
      'columnDefs': [{
        'targets': 0,
        'searchable': true,
        'orderable': false,
        'className': 'dt-body-center',
        'render': function (data, type, full, meta) {
          return '<input type="checkbox" class="form-check-input" name="criPatchCheckbox" value="' + $('<div/>').text(data).html() + '">';
        }
      }]
    });

    $("input[name='topVulCheckbox']").change(function () {

      let length = $('input[name="topVulCheckbox"]').length;

      if ($('input[name="topVulCheckbox"]:checked').length != length) {
        $('#topVulSelectAll').prop('checked', false);
      } else {
        $('#topVulSelectAll').prop('checked', true);
      }
    });


    $("input[name='criPatchCheckbox']").change(function () {

      let length = $('input[name="criPatchCheckbox"]').length;

      if ($('input[name="criPatchCheckbox"]:checked').length != length) {
        $('#criticalPatchesSelectAll').prop('checked', false);
      } else {
        $('#criticalPatchesSelectAll').prop('checked', true);
      }
    });

    $("#vulMitigate").click(function() {
      $("#vulMitigateModal").modal('show');
    });

  });

  function sidebarToggle() {
    $('body').toggleClass('toggle-sidebar');
  };

  function selectAllTopVul() {
    if ($('#topVulSelectAll').is(':checked'))
      $("input[name='topVulCheckbox']").prop('checked', true);
    else
      $("input[name='topVulCheckbox']").prop('checked', false);
  };

  function selectAllCriticalPatches() {
    if ($('#criticalPatchesSelectAll').is(':checked'))
      $("input[name='criPatchCheckbox']").prop('checked', true);
    else
      $("input[name='criPatchCheckbox']").prop('checked', false);
  };


</script>

<body>

<html:form name ="newDashboardForm" action="/newDashboard.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.NewDashboardViewForm">
<html:hidden property="action"/>

  <header id="header" class="header fixed-top d-flex align-items-center">
    <div class="d-flex align-items-center justify-content-between">
      <a href="#" class="logo d-flex align-items-center">
        <img
          src="/spm/images/harman_defensight_logo.png"
          style="max-height: 40px;" alt="">
      </a> 
      <i onclick="sidebarToggle()" class="fa-solid fa-bars toggle-sidebar-btn"></i>
    </div>
    <nav class="header-nav ms-auto">
      <ul class="d-flex align-items-center"> 
        <li class="nav-item dropdown pe-3">
          <a class="nav-link nav-profile d-flex align-items-center pe-0" href="#" data-bs-toggle="dropdown">
            <i class="fa-regular fa-user rounded-circle"></i>
            <!-- <img src="assets/img/profile-img.jpg" alt="Profile" class="rounded-circle">  -->
            <span class="d-none d-md-block dropdown-toggle ps-2">Admin</span> </a>
          <ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow profile">
            <li> <a class="dropdown-item d-flex align-items-center"> <i class="fa-regular fa-user"></i> <span>My
                  Profile</span> </a></li>
            <li>
              <hr class="dropdown-divider">
            </li>
            <li> <a class="dropdown-item d-flex align-items-center"> <i class="fa-solid fa-gear"></i> <span>Account
                  Settings</span> </a></li>
            <li>
              <hr class="dropdown-divider">
            </li>
            <li> <a class="dropdown-item d-flex align-items-center"> <i class="fa-regular fa-circle-question"></i> <span>Need
                  Help?</span> </a></li>
            <li>
              <hr class="dropdown-divider">
            </li>
            <li> <a class="dropdown-item d-flex align-items-center"> <i class="fa-solid fa-arrow-right-from-bracket"></i> <span>Sign
                  Out</span> </a></li>
          </ul>
        </li>
      </ul>
    </nav>
  </header>

  <aside id="sidebar" class="sidebar" style="height:100%; width: 85px;">
    <ul class="sidebar-nav" id="sidebar-nav">
      <li class="nav-item"> <a class="nav-link nav-selected" href="#" title="Dashboard"> <i class="fa-solid fa-gauge"></i> </a>
      </li>
      <li class="nav-item">
        <a class="nav-link collapsed" data-bs-target="#forms-nav" data-bs-toggle="collapse" href="#" title="Automation">
          <i class="fa-solid fa-recycle"></i> </a>
      </li>
      <li class="nav-item">
        <a class="nav-link collapsed" data-bs-target="#components-nav" data-bs-toggle="collapse" href="#">
          <i class="fa-brands fa-searchengin"></i>
        </a>
      </li>
    </ul>
  </aside>
  <main id="main" class="main">
    <div class="pagetitle">
      <h1>Dashboard</h1>
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
                    <li><a class="dropdown-item">Severity</a></li>
                    <li><a class="dropdown-item">Age</a></li>
                  </ul>
                </div>
                <div class="card-body">
                  <h5 class="card-title">Top Vulnerabilities <span>| By Severity</span>
                    <!-- Button trigger modal -->
                    <button type="button" id="vulMitigate" class="btn btn-primary" style="margin-left: 10px;"
                      data-toggle="modal" data-target="#exampleModalCenter">Mitigate Selected</button>
                  </h5>

                  <table id="topVulTable" class="table table-borderless" style="width: 100%;">
                    <thead>
                      <tr>
                        <th><input type="checkbox" class="form-check-input" id="topVulSelectAll"
                            onclick="selectAllTopVul()"></th>
                        <th scope="col">CVE-ID</th>
                        <th scope="col">Severity</th>
                        <th scope="col">Impacted Machines</th>
                        <th scope="col">Patches Available</th>
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
            <div class="filter">
              <a class="icon" href="#" data-bs-toggle="dropdown"><i class="fa-solid fa-sliders"></i></a>
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
              <h5 class="card-title">Critical Patches <span>| Severity - High</span></h5>

              <table id="criticalPatchesTable" class="table" style="width: 100%;">
                <thead>
                  <tr>
                    <th scope="col"><input type="checkbox" id="criticalPatchesSelectAll"
                        onclick="selectAllCriticalPatches()" class="form-check-input"></th>
                    <th scope="col">Machine</th>
                    <th scope="col">Patches</th>
                  </tr>
                </thead>
                <tbody>

                </tbody>
              </table>
            </div>
          </div>


        </div>
      </div>
    </section>

    </div>


    <!-- Modal -->
    <div id="vulMitigateModal" class="modal fade" id="exampleModalCenter" tabindex="-1" role="dialog"
      aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="exampleModalLongTitle">Modal title</h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            ...
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            <button type="button" class="btn btn-primary">Save changes</button>
          </div>
        </div>
      </div>
    </div>

</html:form>
</body>
</html>
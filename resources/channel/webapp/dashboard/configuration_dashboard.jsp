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

<!-- Export report as a file -->
<script type="text/javascript" src="https://cdn.datatables.net/buttons/2.4.2/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/pdfmake.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.53/vfs_fonts.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/2.4.2/js/buttons.html5.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/buttons/2.4.2/js/buttons.print.min.js"></script>

<script type="text/javascript">

let configReportModalTable;

$(function () {

  $('#dashboard').addClass('nav-selected');

  configReportModalTable = $('#configReportModalTable').DataTable({});

    var pieChartData = [];
    pieChartData.push(<bean:write name="configDashboardForm" property="configProfileCompliant"/>);
    pieChartData.push(<bean:write name="configDashboardForm" property="configProfileNonCompliant"/>);
    pieChartData.push(<bean:write name="configDashboardForm" property="configProfileNotApplicable"/>);
    pieChartData.push(<bean:write name="configDashboardForm" property="configProfileUnknown"/>);


var ctx1 = $("#complianceDonutChart");
    // label: "Severity",
    var chart1 = new Chart(ctx1, {
        type: "doughnut",
        data: {
            labels: ["Compliant", "Non-Compliant", 'Not Applicable', 'Unknown'],
            datasets: [
                {
                    data: pieChartData,
                    backgroundColor: [
                        "#71DCEB", "#FF5F60", "#F3CC63", "#D4733A"
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
                    display: false,
                    position: "bottom",
                    labels: {
                      boxWidth: 20, // Set the desired width of the legend
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            let label = context.dataset.label || '';
                            return (' ' + label + '' +context.parsed + ' Machines');
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
          labels: {
            boxWidth: 20, // Set the desired width of the legend
          }
        },
      },
      responsive: false,
      interaction: {
        intersect: false,
      },
      scales: {
        x: {
          stacked: true,
          grid: {
            display: false,
          },
          beginAtZero: true,
          title: {
            display: true,
            text: "Security Technical Implementation Guides (STIGs)"
          },
          ticks: {
            display: false
          }
        },
        y: {
          stacked: true,
          grid: {
            display: false,
          },
          title: {
            display: true,
            text: "Number of Machines"
          },
          ticks: {
            precision: 0
          }
        }
      },
      onClick: handleBarChartClick,
      barThickness: 40,
    },
  };

  var barChart = new Chart(ctx, config);

  
  function showMachinesModalTable(machinesModalData, contentTitle) {

    $('#machinesModalTable').DataTable().clear();

    let machinesModalTable = $('#machinesModalTable').DataTable({
      "destroy": true, // In order to reinitialize the datatable
      "pagination": true, // For Pagination
      "bPaginate": true,
      "sorting": true, // For sorting
      "ordering": true,
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
        'targets': 0,
        'className': 'text-nowrap'
      },{
        'targets': 3,
        'searchable': false,
        'orderable': false,
        'className': 'dt-body-center',
        'render': function (data, type, full, meta) {
          let machineName = '\'' + full["machineName"] + '\'';
          let profileTitle = '\'' + full["profileName"] + '\'';
          let finishedAt = '\'' + full["finishedAt"] + '\'';
          contentTitle = '\'' + contentTitle + '\'';
          return '<input type="button" onClick="showConfigReport(' + machineName + ',' + full["contentId"] + ',' + contentTitle + ',' + full["profileId"] + ',' + profileTitle + ',' + finishedAt +')" class="view-details btn btn-sm btn-primary" value="View Details">';
        }
      }]
    });
    // $('#machinesModalTable').on('click','.view-details',() => {
    //   showConfigReport();
    // })
/*
    //Get the column index for the Status column to be used in the method below ($.fn.dataTable.ext.search.push)
    //This tells datatables what column to filter on when a user selects a value from the dropdown.
    //It's important that the text used here (Status) is the same for used in the header of the column to filter
    var statusIndex = 0;
    $("#machinesModalTable th").each(function (i) {
      if ($($(this)).html() == "Profile Title") {
        statusIndex = i; return false;
      }
    });

    //Use the built in datatables API to filter the existing rows by the Category column
    $.fn.dataTable.ext.search.push(
      function (settings, data, dataIndex) {
        var selectedItem = $('#machinesModalFilter').val()
        var status = data[statusIndex];
        if (selectedItem === "" || status.includes(selectedItem)) {
          return true;
        }
        return false;
      }
    );
*/
    //Set the change event for the Status Filter dropdown to redraw the datatable each time
    //a user selects a new filter.
    $("#machinesModalFilter").change(function (e) {

      machinesModalTable.clear();

      if($('#machinesModalFilter').val() === "") {
        machinesModalTable.rows.add(machinesModalData);
      } else {
        let newData = machinesModalData.filter(record => record['profileName'] === $('#machinesModalFilter').val());
        machinesModalTable.rows.add(newData);
      }

      machinesModalTable.draw();
    });

  }

  function handleBarChartClick(event, elements) {

    if (elements.length > 0) {
      var datasetIndex = elements[0].datasetIndex;
      var dataIndex = elements[0].index;

      //console.log("datasetIndex = " + datasetIndex);
      //console.log("dataIndex = " + dataIndex);

      var value = barChartSeverityData.datasets[datasetIndex].data[dataIndex];
      //console.log('Clicked on value:', value);
      //console.log('Clicked on updated:', barChartSeverityData.labels[dataIndex], ', ', barChartSeverityData.datasets[datasetIndex].label);

      //[{"profileName":"xccdf_org.ssgproject.content_profile_C2S","profileId":4,"contentId":2,"rulesCompliance":"63/168","contentTitle":"Guide to the Secure Configuration of CentOS 7","profileTitle":"C2S for Red Hat Enterprise Linux 7","machineName":"vmcentos-qaendpoint","contentName":"xccdf_org.ssgproject.content_benchmark_CENTOS-7"}]

      let contId = barChartSeverityData.contentIds[dataIndex];
      let compType = barChartSeverityData.datasets[datasetIndex].label;
      let contentTitle = barChartSeverityData.labels[dataIndex];

      $('#machinesModalLabel').html(contentTitle + ' - <b>' + compType + '</b>');

      $.ajax({
        url: './configDashboard.do',
        type: 'POST',
        dataType: 'text json',
        data: {
          action: "getMachineByContent",
          contentId: contId,
          complianceType: compType
        },
        beforeSend: function () { },
        complete: function (xhr, status) { },
        success: function (response) {
          //console.log(response);
          //console.log(JSON.stringify(response));

          showMachinesModalTable(response,contentTitle);

          $('#machinesModalFilter')
            .html($("<option></option>")
              .attr("value", "")
              .text("Show All"));

          let profileFilterData = new Set();
          response.forEach(x => {
            profileFilterData.add(x['profileName']);
          });

          profileFilterData.forEach(profileName => {
            $('#machinesModalFilter')
              .append($("<option></option>")
                .attr("value", profileName)
                .text(profileName));
          })

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
   var contentLineChartCtx = document.getElementById('contentLineChart').getContext('2d');

var contentLineChartData = {
  labels: ["Jun 23","Jul 23", "Aug 23", "Sept 23", "Oct 23", "Nov 23" ],
  datasets: [
    {
      label: 'Windows 10 STIG',
      data: [25, 21, 17, 12, 9, 5]
    },
    {
      label: 'RHEL 8 STIG',
      data: [50, 38, 31, 25, 19, 9]
    }
  ]
};

var contentLineChartConfig = {
  type: 'line',
  data: contentLineChartData,
  options: {
    tension: 0.4,
    responsive: false,
    scales: {
      x: {
        title: {
          display: true,
          text: "Finished scan time"
        }
      },
      y: {
        title: {
          display: true,
          text: "Number of failed rules"
        }
      },
    },
    plugins: {
      legend: {
          display: true,
          position: "bottom",
          labels: {
            boxWidth: 20, // Set the desired width of the legend
          }
      },
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

var contentLineChart = new Chart(contentLineChartCtx, contentLineChartConfig);


  //Line Chart
  var profileLineChartCtx = document.getElementById('profileLineChart').getContext('2d');

  var profileLineChartData = {
    labels: ["Jun 23","Jul 23", "Aug 23", "Sept 23", "Oct 23", "Nov 23" ],
    datasets: [
      {
        label: 'CAT I Only',
        data: [25, 21, 17, 12, 9, 5]
      },
      {
        label: 'I - Mission Critical Classified',
        data: [50, 38, 31, 25, 19, 9]
      }
    ]
  };

  var profileLineChartConfig = {
    type: 'line',
    data: profileLineChartData,
    options: {
      tension: 0.4,
      responsive: false,
      scales: {
        x: {
          title: {
            display: true,
            text: "Finished scan time"
          }
        },
        y: {
          title: {
            display: true,
            text: "Number of failed rules"
          }
        },
      },
      plugins: {
        legend: {
            display: true,
            position: "bottom",
            labels: {
              boxWidth: 20, // Set the desired width of the legend
            }
        },
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

  var profileLineChart = new Chart(profileLineChartCtx, profileLineChartConfig);


  });

  function setConfigReportData(machineName, data, contentTitle, profileTitle, finishedAt) {

    configReportModalTable.destroy();

    configReportModalTable = $('#configReportModalTable').DataTable({
        destroy: true,
        dom: 'Bfrtip',
        buttons: [
          /*{
            extend: 'excelHtml5',
            title: machineName+'_ConfigurationReport_'+finishedAt,
            className: 'btn btn-sm btn-primary'
          },
          {
            extend: 'csvHtml5',
            title: machineName+'_ConfigurationReport_'+finishedAt,
            className: 'btn btn-sm btn-primary'
          },*/
          {
            extend: 'pdfHtml5',
            text: 'Export',
            title: machineName+'_ConfigurationReport_'+finishedAt,
            className: 'btn btn-sm btn-outline-primary float-start',
            customize: function(doc) {
              
              doc.content[0].text = "Configuration Scan Report";

              let text = "Machine Name: "+machineName+"\n"
                +"Scan Type: Configuration\n"
                +"Content Title: "+ contentTitle +"\n"
                +"Profile Title: "+ profileTitle +"\n"
                +"Scan Finished At: "+ finishedAt +"\n";

              let margin = [0,0,0,12];

              let updatedText = {text: text, margin: margin};

              doc.content.splice(1,0,updatedText);
            }
          },
        ],
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

      
    // console.log('setConfigReportData');
    // console.log(data);

    // console.log(JSON.stringify(data));

    // configReportModalTable.clear();
    // configReportModalTable.rows.add(data);
    // configReportModalTable.draw();

  }

  function showConfigReport(machineName, contentId, contentTitle, profileId, profileTitle, finishedAt) {
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

          $('#configReportModalLabel').html("Configuration assessment results for <b>"+machineName+"</b>");
          setConfigReportData(machineName, response['result'], contentTitle, profileTitle, finishedAt);

          $('#configReportStatusFilter')
            .html($("<option></option>")
              .attr("value", "")
              .text("Status"));

          let configReportStatusFilterData = new Set();
          response['result'].forEach(x => {
            configReportStatusFilterData.add(x['status']);
          });

          configReportStatusFilterData.forEach(status => {
            $('#configReportStatusFilter')
              .append($("<option></option>")
                .attr("value", status)
                .text(status));
          })

          var configReportModal = new bootstrap.Modal(document.getElementById('configReportModal'), {
            keyboard: false
          });
          configReportModal.show();

          $("#configReportStatusFilter").change(function (e) {
            if($('#configReportStatusFilter').val() === "") {
              setConfigReportData(machineName, response['result'], contentTitle, profileTitle, finishedAt);
            } else {
              let data = response['result'].filter(x => x.status === $('#configReportStatusFilter').val());
              setConfigReportData(machineName, data, contentTitle, profileTitle, finishedAt);
            }
          })
          
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
  
  
                  <div class="col-md-5">
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
  
  
                  <div class="col-md-5">
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
                                <span class="text-muted small">By Configuration</span>
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
                    <h5 class="card-title">STIGs Compliance Overview</h5>
                    <div>
                      <div>
                        <div style="display: block; padding: 0 10px 16px 10px;">
                          <canvas id="myChart" height="250" style="width: 100%"></canvas>
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
                  <div class="col-1"></div>
                  <div class="col-5">
                    <div align="center">
                      <span class="small"><bean:write name="configDashboardForm" property="configProfileCompliant"/></span><br />
                      <span class="small lowColor"><b>Compliant</b></span>
                    </div>
                  </div>
                  <div class="col-5">
                    <div align="center">
                      <span class="small"><bean:write name="configDashboardForm" property="configProfileNonCompliant"/></span><br />
                      <span class="small criticalColor"><b>Non-Compliant</b></span>
                    </div>
                  </div>
                  <div class="col-1"></div>
                  <div class="col-1"></div>
                  <div class="col-5">
                    <div align="center">
                      <span class="small"><bean:write name="configDashboardForm" property="configProfileNotApplicable"/></span><br />
                      <span class="small mediumColor"><b>Not Applicable</b></span>
                    </div>
                  </div>
                  <div class="col-5">
                    <div align="center">
                      <span class="small"><bean:write name="configDashboardForm" property="configProfileUnknown"/></span><br />
                      <span class="small highColor"><b>Unknown</b></span>
                    </div>
                  </div>
                  <div class="col-1"></div>
                </div>
                <br/>
                <div>
                <div style="position: relative; width: 100%; margin: auto;">
                  <canvas id="complianceDonutChart" height="285" style="margin:auto; width: 100%;">
                  </canvas>
                </div>
                </div>
                <br/>
              </div>
            </div>
          </div>
        </div>

        <div class="row">
          <div class="col-lg-6">
            <div class="card">
              <div class="card-body pb-0">
                <h5 class="card-title">Historical Compliance for STIGs</h5>
                <div style="position: relative; width: 100%; margin: auto;">
                  <canvas id="contentLineChart" height="300" style="width: 100%"></canvas>
                </div>
              </div>
            </div>
          </div>

          <div class="col-lg-6">
            <div class="card">
              <div class="card-body pb-0">
                <h5 class="card-title">Historical Compliance for Profiles</h5>
                <div style="position: relative; width: 100%; margin: auto;">
                  <canvas id="profileLineChart" height="300" style="width: 100%"></canvas>
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
            <div class="row">
              <div class="col-1">
                <label for="machinesModalFilter" class="col-form-label">Profile:&nbsp;</label>
              </div>
              <div class="col-11">
                <select id="machinesModalFilter" class="form-select form-select-sm"></select>
              </div>
            </div>
            <br />
            <table id="machinesModalTable" class="table table-striped table-bordered" style="width: 100%;">
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
            <div style="margin-left:15px; float:right;">
              <select id="configReportStatusFilter" class="form-select form-select-sm" style="width:fit-content; float:left;">
              </select>
            </div>

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

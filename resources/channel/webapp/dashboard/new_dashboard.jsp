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

<script type="text/javascript">

var topVulDataTable;
$(function () {

    $('#dashboard').addClass('nav-selected');
    
    var sum = 0;
    for(var x of <bean:write name="newDashboardForm" property="pieChartData"/>){ 
	    sum += x;
    }
    var pieChartData = [];
    
    for(var x of <bean:write name="newDashboardForm" property="pieChartData"/>){ 
	    pieChartData.push( Math.round((x/sum) *100) );
    }

    var ctx1 = $("#vulStatsDonutChart");
    // label: "Severity",
    var chart1 = new Chart(ctx1, {
        type: "doughnut",
        data: {
            labels: ["Critical", "High", "Medium", "Low"],
            datasets: [
                {
                    data: pieChartData,
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
                    display: false,
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
                    position: 'bottom',
                    labels: {
                      boxWidth: 20, // Set the desired width of the legend
                    }
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

   topVulDataTable = $('#topVulTable').DataTable({
       "destroy": true, // In order to reinitialize the datatable
       "pagination": true, // For Pagination
       "server": true,
       "serverSide": true,
       "processing": true,
       "searching":false,
       "ajax": {
           "url": './topVulDashboard.do',
           "data": function(req) {
               req.page = req.start / req.length + 1,
               req.pageSize = req.length
               req.filter = $('#topVulFilter').find(":selected").val();
               req.search = $('#topVulSearch').val();
           }
       },
       'fnDrawCallback': function(oSettings) {
           $('#topVulTable_filter').each(function() {
               $(this).append($('#topVulMitigateButton'));
           });
       },
       language: {
           search: "_INPUT_",
           searchPlaceholder: "Search..."
       },
       "columns": [{},
           {
               "title": "CVE-ID",
               "data": "cveId"
           },
           {
               "title": "Severity",
               "data": "severity"
           },
           {
               "title": "Impacted Machines",
               "data": "affectedMachines"
           },
           {
               "title": "Patches",
               "data": "patchId"
           },
           {
               "title": "Status",
               "data": "status"
           },
           {
               "title": "Risk Score",
               "data": "riskScore"
           }
       ],
       'columnDefs': [{
               'targets': 0,
               'searchable': false,
               'orderable': false,
               'className': 'dt-body-center',
               'render': function(data, type, full, meta) {
                   return '<input type="checkbox" class="form-check-input" name="topVulCheckbox" value="' + full["patchId"] + '">';
               }
           },
           {
               'targets': 4,
               'className': 'dt-body-left',
               'render': function(data, type, full, meta) {
                   const splitArray = data.split(".");
                   let displayValue = "";
                   if (splitArray.length == 1)
                       displayValue = splitArray[0]
                   else
                       displayValue = splitArray[0] + '.' + splitArray[1];
                   return '<span data-bs-toggle="tooltip" data-bs-placement="right" title=' + data + ' cursor:context-menu;">' + displayValue + '</span>';
               }
           }, {
               'targets': 1,
               'className': 'dt-body-left',
           }, {
               'targets': 2,
               'className': 'dt-body-left',
           }, {
               'targets': 5,
               'className': 'dt-body-left',
           }, {
               'targets': 6,
               'className': 'dt-body-left',
           }
       ],
       'rowCallback': function(row, data, index) {
           switch (data['severity']) {
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

  $("#btnTopVulSearch").click(function(event) {
      topVulDataTable.draw();
  });

  	//Get the column index for the Status column to be used in the method below ($.fn.dataTable.ext.search.push)
    //This tells datatables what column to filter on when a user selects a value from the dropdown.
    //It's important that the text used here (Status) is the same for used in the header of the column to filter
    var statusIndex = 0;
    $("#topVulTable th").each(function (i) {
      if ($($(this)).html() == "Status") {
    	  statusIndex = i; return false;
      }
    });

    //Use the built in datatables API to filter the existing rows by the Category column
    $.fn.dataTable.ext.search.push(
      function (settings, data, dataIndex) {
        var selectedItem = $('#topVulFilter').val()
        var status = data[statusIndex];
        if (selectedItem === "" || status.includes(selectedItem)) {
          return true;
        }
        return false;
      }
    );

    //Set the change event for the Status Filter dropdown to redraw the datatable each time
    //a user selects a new filter.
    $("#topVulFilter").change(function (e) {
    	topVulDataTable.draw();
    });

    $("#topVulModal").on("hidden.bs.modal", function () {
        $('#topVulModalTable').DataTable().clear();
    });

    var notCheckedInInfo;
    var notCheckedInInfo2;
    $('#reportingModelId').click(function () {
        $.ajax({
            url: './newDashboard.do',
            type: 'POST',
            dataType: 'text json',
            data: {action: 'notcheckedin_info'},
            beforeSend: function() {},
            complete: function (xhr, status) {},
            success: function (response) {
              notCheckedInInfo = response;
              notCheckedInInfo2 = JSON.stringify(notCheckedInInfo);
              notCheckedInInfo2 = JSON.parse(notCheckedInInfo2);
              populateNotCheckedInInfo(notCheckedInInfo2);
        }});

     });


    var topVulMitigateInfo;
    var topVulMitigateInfo2;

    $('#topVulMitigateButton').click(function () {
        let patchesArray = [];
        $('input[name=topVulCheckbox]:checked').each(function () {
            patchesArray.push($(this).val());
        })

        console.log("Selected Patches for Mitigation: "+patchesArray);

        var queryStr = "?patchids=" + patchesArray;
        $.ajax({
            url: './newDashboard.do' + queryStr,
            type: 'POST',
            dataType: 'text json',
            data: {action: 'mitigate'},
            beforeSend: function() {},
            complete: function (xhr, status) {},
            success: function (response) {
              topVulMitigateInfo = response;
              topVulMitigateInfo2 = JSON.stringify(topVulMitigateInfo);
              topVulMitigateInfo2 = JSON.parse(topVulMitigateInfo2);
              createMitigateTable(topVulMitigateInfo2);
        }});

    });  // onClick topVulMitigateButton()
    
    $('#criPatchesMitigateButton').click(function () {
        let patchesArray = [];
        $('input[name=criPatchCheckbox]:checked').each(function () {
            patchesArray.push($(this).val());
        })
        console.log(patchesArray);
        alert("Selected Patches for Mitigation: "+patchesArray);

        var queryStr = "?patchids=" + patchesArray;
        $.ajax({
            url: './newDashboard.do' + queryStr,
            type: 'POST',
            dataType: 'text json',
            data: {action: 'mitigate'},
            beforeSend: function() {},
            complete: function (xhr, status) {},
            success: function (response) {
              topVulMitigateInfo = response;
              topVulMitigateInfo2 = JSON.stringify(topVulMitigateInfo);
              topVulMitigateInfo2 = JSON.parse(topVulMitigateInfo2);
              createMitigateTable(topVulMitigateInfo2);
        }});
        
    });


    $('#topVulMitApplyPatches').click(function () {
        let patchgroups = [];
        $('input[name=topVulMitCheck]:checked').each(function () {
            patchgroups.push($(this).val());
        })
        // console.log(patchgroups);
        if (patchgroups.length == 0) {
          console.log("No machines and patch groups selected for mitigate operation..");
          return;
        }
        console.log("Selected Patch Groups with Machines for Mitigation: "+patchgroups);
        var queryStr = "?machinepatchgroups=" + patchgroups;
        $.ajax({
            url: './newDashboard.do' + queryStr,
            type: 'POST',
            dataType: 'text json',
            data: {action: 'apply_patches'},
            beforeSend: function() { $('#applyPatchesRes').text("Patches Deployment has been initiated...");},
            complete: function (xhr, status) {},
            success: function (response) {
            	//console.log(response);
            	$('#applyPatchesRes').text(response);
        }});

    });


    var prtyPatchesData = '<bean:write name="newDashboardForm" property="priorityPatchesData"/>';
    prtyPatchesData = prtyPatchesData.replace(/&quot;/g,'"');
    prtyPatchesData=JSON.parse(prtyPatchesData);

   $('#criticalPatchesTable').DataTable({
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
       "aaData": prtyPatchesData,
       "columns": [{},
        {
           "data": "CVE-ID"
        },
       {
           "data": "Patch Name"
       }, {
           "data": "Severity"
       }, {
           "data": "Affected Machines"
       }, {
           "data": "Status"
       }],
       'columnDefs': [{
           'targets': 0,
           'searchable': true,
           'orderable': false,
           'className': 'dt-body-center',
           'render': function (data, type, full, meta) {
               return '<input type="checkbox" class="form-check-input" name="criPatchCheckbox" value="' + full["Patch Name"] + '">';
           }
       },
       {
           'targets': 2,
           'className': 'dt-body-left',
           'render': function (data, type, full, meta) {
               const splitArray = data.split(".");
               let displayValue = "";
               if(splitArray.length == 1)
                   displayValue = splitArray[0]
               else
                   displayValue = splitArray[0]+'.'+splitArray[1];
               return '<span data-bs-toggle="tooltip" data-bs-placement="right" title='+data+' cursor:context-menu;">'+displayValue+'</span>';
           }
       }, {
           'targets': 2,
           'className': 'dt-body-left',
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
   
   var reportingNotCheckedIn = <bean:write name="newDashboardForm" property="reportingNotCheckedIn"/>;
   var reportingCheckedIn = <bean:write name="newDashboardForm" property="reportingCheckedIn"/>;
   var reportingSum = reportingNotCheckedIn + reportingCheckedIn;
   $('#reportingNotCheckedIn').css("width",(reportingNotCheckedIn/reportingSum)*100+"%");
   $('#reportingCheckedIn').css("width",(reportingCheckedIn/reportingSum)*100+"%");
   
   
   var securityNonCompliant = <bean:write name="newDashboardForm" property="securityNonCompliant"/>;
   var securityCompliant = <bean:write name="newDashboardForm" property="securityCompliant"/>;
   var securitySum = securityNonCompliant + securityCompliant;
   $('#securityNonCompliant').css("width",(securityNonCompliant/securitySum)*100+"%");
   $('#securityCompliant').css("width",(securityCompliant/securitySum)*100+"%");
   
   
   var patchNonCompliant = <bean:write name="newDashboardForm" property="patchNonCompliant"/>;
   var patchCompliant = <bean:write name="newDashboardForm" property="patchCompliant"/>;
   var patchSum = patchNonCompliant + patchCompliant;
   $('#patchNonCompliant').css("width",(patchNonCompliant/patchSum)*100+"%");
   $('#patchCompliant').css("width",(patchCompliant/patchSum)*100+"%");
   
});

function populateNotCheckedInInfo(notCheckedInData) {
   $('#reportingModalTable').DataTable({
       "destroy": true, // In order to reinitialize the datatable
       "pagination": true, // For Pagination
       "sorting": false, // For sorting
       "ordering": false,
       "searching": false,
       "bFilter": false,
       "language": {
           "search": "_INPUT_",
           "searchPlaceholder": "Search..."
       },

       "aaData": notCheckedInData,
       "columns": [
       {
           "data": "Machine Name"
       }, {
           "data": "Vulnerable Last ScanTime"
       }]
   });
}

function createMitigateTable(aaData) {
    $('#topVulModalTable').DataTable({
        "destroy": true, // In order to reinitialize the datatable
        "pagination": true, // For Pagination
        "sorting": false, // For sorting
        "ordering": false,
        "searching": false,
        language: {
            search: "_INPUT_",
            searchPlaceholder: "Search..."
        },
        "aaData": aaData,
        "columns": [{},
        {
            "data": "Impacted Machine"
        }, {
            "data": "Status"
        }, {
            "data": "Patch Details"
        }],
        'columnDefs': [{
            'targets': 0,
            'searchable': true,
            'orderable': false,
            'className': 'dt-body-center',
            'render': function (data, type, full, meta) {
                var disabled = full["Status"] === "Pass" ? "disabled" : "";
                return '<input type="checkbox" class="form-check-input" name="topVulMitCheck" value="' + full["Impacted Machine"]+ "@" +full["Patch Details"] + '" ' + disabled + '>';
            }
        }],
        'rowCallback': function (row, data, index) {
            switch (data['Status']) {
                case 'Pass':
                    $(row).find('td:eq(2)').addClass('text-success');
                    break;

                case 'Failed':
                    $(row).find('td:eq(2)').addClass('text-danger');
                    break;

                default:
                    break;
            }
        }
    });

}

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
        <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i></div>
        <div class="p-2 bd-highlight text-primary align-self-center"> <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left"
              style="margin-right: 5px;"></i>CMS Home</a>
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
            
            <div class="col-6">
            
	            <div class="card">
		            <div class="card-body pb-0">
		              <h5 class="card-title">Vulnerability Aging</h5>
		              <div>
		                <div>
		                  <div style="display: block; padding: 0 10px 16px 10px;">
		                    <canvas id="vulAgingScatter" style="min-height: 250px;">
		                    </canvas>
		                  </div>
		                </div>
		              </div>
		            </div>
		          </div>
            	
            	
            </div>
            
            <div class="col-6">
            
            <div class="card overflow-auto">
              <div class="card-body" style="padding-bottom: 1.4rem;">
                <h5 class="card-title">Machine Compliance</h5>

                <div class="row compliance">
                  <div class="col-md-4">
                    <p>Reporting</p>
                    <span> Not checked-in </span><br/>
                    <span id="reportingModelId" data-bs-toggle="modal" data-bs-target="#reportingModal"
                        style="color: #FF5F60; text-decoration: underline; cursor: pointer;">
                        <bean:write name="newDashboardForm" property="reportingNotCheckedIn"/>
                      </span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="reportingNotCheckedIn" class="progress-bar" role="progressbar"
                        style="width:0; background-color: #FF5F60" aria-valuemax="100"></div>
                    </div>
                    
                    <span> Checked-in </span><br/>
                    <span style="color: #18db76;"><bean:write name="newDashboardForm" property="reportingCheckedIn"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="reportingCheckedIn" class="progress-bar" role="progressbar"
                        style="width:0; background-color: #18db76" aria-valuemax="100"></div>
                    </div>
                  </div>
                  <div class="col-md-4">
                    <p>Security</p>
                    <span> Non Compliant </span><br/> 
                    <span style="color: #FF5F60;"><bean:write name="newDashboardForm" property="securityNonCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="securityNonCompliant" class="progress-bar" role="progressbar"
                        style="width: 0%; background-color: #FF5F60" aria-valuemax="100"></div>
                    </div>
                    
                    <span> Compliant</span> <br/>
                    <span style="color: #18db76;"><bean:write name="newDashboardForm" property="securityCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="securityCompliant" class="progress-bar" role="progressbar"
                        style="width: 0%; background-color: #18db76" aria-valuemax="100"></div>
                    </div>
                  </div>


                  <div class="col-md-4">
                    <p>Patches</p>
                    <span> Non Compliant </span><br/>
                    <span style="color: #FF5F60;"><bean:write name="newDashboardForm" property="patchNonCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="patchNonCompliant" class="progress-bar" role="progressbar"
                        style="width: 0%; background-color: #FF5F60" aria-valuemax="100"></div>
                    </div>
                    
                    <span> Compliant </span><br/>
                    <span style="color: #18db76;"><bean:write name="newDashboardForm" property="patchCompliant"/></span>
                    <div class="progress" style="margin-bottom:10px;">
                      <div id="patchCompliant" class="progress-bar" role="progressbar"
                        style="width: 0%; background-color: #18db76" aria-valuemax="100"></div>
                    </div>
                  </div>


                </div>

              </div>
            </div>
            
            
            
            </div>

            <div class="col-12">
              <div class="card recent-sales overflow-auto">
                <div class="filter" style="margin-right:15px;">
                  <select id="topVulFilter" class="form-select form-select-sm">
                    <option value="">Show All</option>
                    <option value="Patch Assigned">Patch Assigned</option>
                    <option value="Patch Applied, Reboot required">Patch Applied, Reboot required</option>
                    <option value="Patch Not Applied">Patch Not Applied</option>
                    <option value="Patch Failed">Patch Failed</option>
                    <option value="Patch Not Available">Patch Not Available</option>
                  </select>
                </div>
                <div class="card-body">
                  <h5 class="card-title">Top Vulnerabilities </h5>

                   <div class="input-group" style="margin-bottom: 10px;">
                                       <input type="search" class="form-control" placeholder="Search CVE-ID or Patch ID" id="topVulSearch">
                                       <button type="button" id="btnTopVulSearch" class="btn btn-primary btn-sm">
                                           <i class="fas fa-search"></i>
                                       </button>

                                   <button type="button" id="topVulMitigateButton" class="btn btn-primary btn-sm" data-bs-toggle="modal"
                                       data-bs-target="#topVulModal" style="margin-left: 10px;">
                                       Mitigate Selected
                                   </button>
                   				</div>

                  <table id="topVulTable" class="table table-borderless" style="width: 100%;">
                    <thead>
                      <tr>
                        <th><input type="checkbox" class="selectAll form-check-input" id="topVulSelectAll"></th>
                        <th scope="col" style="text-align: left;">CVE-ID</th>
                        <th scope="col" style="text-align: left;">Severity</th>
                        <th scope="col">Impacted Machines</th>
                        <th scope="col" style="text-align: left;">Patches Available</th>
                        <th scope="col" style="text-align: left;">Status</th>
                        <th scope="col" style="text-align: left;">Risk Score</th>
                      </tr>
                    </thead>
                    <tbody></tbody>
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
                  <canvas id="vulStatsDonutChart" style="margin:auto; min-height: 130px;">
                  </canvas>
                </div>
              </div>

            </div>
          </div>
          
           <div class="col-12">
              <div class="card overflow-auto">
                 <div class="filter" style="margin-right:10px;">
                   <select id="critPatchFilter" class="form-select form-select-sm">
                    <option value="">Show All</option>
                    <option value="Patch Assigned">Patch Assigned</option>
                    <option value="Patch Applied, Reboot required">Patch Applied, Reboot required</option>
                    <option value="Patch Not Applied">Patch Not Applied</option>
                    <option value="Patch Failed">Patch Failed</option>
                    <option value="Patch Not Available">Patch Not Available</option>
                   </select>
                 </div>

                <div class="card-body">
                  <h5 class="card-title">Critical Patches </h5>
                    <!-- Button trigger modal -->
                  <div class="input-group" style="margin-bottom: 10px;margin-left:-20px">
                    <button type="button" id="criPatchesMitigateButton" class="btn btn-primary btn-sm" data-bs-toggle="modal"
                    data-bs-target="#topVulModal" style="margin-left: 20px;">
                    Mitigate Selected
                   </button>
                  </div>

                  <table id="criticalPatchesTable" class="table" style="width: 100%;">
                    <thead>
                      <tr>
                        <th scope="col"><input type="checkbox" id="criticalPatchesSelectAll"
                            class="selectAll form-check-input"></th>
                        <th scope="col" style="text-align: left;">CVE-ID</th>
                        <th scope="col" style="text-align: left;">Patch Name</th>
                        <th scope="col" style="text-align: left;">Severity</th>
                        <th scope="col">Affected Machines</th>
                        <th scope="col" style="text-align: left;">Status</th>
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

    

    <!-- Top Vulnerability Modal -->
    <div class="modal fade" id="topVulModal" tabindex="-1" aria-labelledby="topVulModalLabel" aria-hidden="true">
      <div class="modal-dialog" style="max-width:800px;">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="topVulModalLabel">Apply Patches</h5>
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
          	<span id="applyPatchesRes"></span>
            <button type="button" class="btn btn-outline-primary btn-sm" data-bs-dismiss="modal">Cancel</button>
            <button type="button" id="topVulMitApplyPatches" class="btn btn-primary btn-sm">Apply Patches</button>
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
                  <th scope="col">Machine Name</th>
                  <th scope="col">Last Vulnerability Scan Date</th>
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
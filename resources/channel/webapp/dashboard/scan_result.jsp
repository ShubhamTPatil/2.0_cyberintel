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

<style>
    .table>thead>tr>th {
        text-align: left;
        vertical-align: middle;
    }

    .table>tbody>tr>td {
        text-align: left;
        vertical-align: middle;
    }
</style>

<script type="text/javascript">

$(function () {

	$('#scanResult').addClass('nav-selected');
	
	var resp = '<bean:write name="newScanResultsForm" property="scanResultsJson"/>';
	resp = (resp).replace(/&quot;/g, '"');
    console.log("resp = "+resp);
	console.log("json ="+JSON.parse(resp));
    var jsonData = JSON.parse(resp);
    var data = jsonData['data'];
	console.log(data);
	
        let vulAssessmentIndex = 0;
	    
	    $('#vulAssessmentTable').DataTable({
	        "destroy": true, // In order to reinitialize the datatable
	        "pagination": true, // For Pagination
	        "bPaginate": true,
	        "sorting": false, // For sorting
	        "ordering": false,
	        "searching": true,
	        "aaData": data['machineList'],
	        "columns": [{},
	        {
	            "data": "machineName"
	        }, {
	            "data": "os"
	        }, {
	            "data": "securityDefinition"
	        }, {
	            "data": "profile"
	        }, {
	            "data": "scanStaus"
	        }, {
	            "data": "machineLastScan"
	        }],
	        'columnDefs': [{
	            'targets': 0,
	            'searchable': true,
	            'orderable': false,
	            'className': 'dt-body-center',
	            'render': function (data, type, full, meta) {
	                return '<input type="checkbox" class="form-check-input" name="vulAssessmentCheckbox" value="' + vulAssessmentIndex++ + '">';
	            }
	        }]
	    });

	    
	    let configAssessmentTable = 0;
	    
	    $('#configAssessmentTable').DataTable({
	        "destroy": true, // In order to reinitialize the datatable
	        "pagination": true, // For Pagination
	        "bPaginate": true,
	        "sorting": false, // For sorting
	        "ordering": false,
	        "searching": true,
	        "aaData": data['machineList2'],
	        "columns": [{},
	        {
	            "data": "machineName"
	        }, {
	            "data": "os"
	        }, {
	            "data": "securityDefinition"
	        }, {
	            "data": "profile"
	        }, {
	            "data": "scanStaus"
	        }, {
	            "data": "machineLastScan"
	        }],
	        'columnDefs': [{
	            'targets': 0,
	            'searchable': true,
	            'orderable': false,
	            'className': 'dt-body-center',
	            'render': function (data, type, full, meta) {
	                return '<input type="checkbox" class="form-check-input" name="configAssessmentCheckbox" value="' + configAssessmentTable++ + '">';
	            }
	        }]
	    });
});

</script>
</head>


<body>


  <jsp:include page="header.jsp" />
  <jsp:include page="sidebar.jsp" />

  <form name="newScanResultsForm" id="form_id" action="/spm/upcoming.do" method="post">
 <main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename">Scan Result</span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="Scan Result"><i
              class="fa-solid fa-circle-info text-primary"></i></span>
        </div>
        <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i></div>
        <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Download" style="cursor: pointer;">
          <i class="fa-solid fa-download"></i>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center">
        <a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home</a>
        </div>
      </div>

    </div>

    <section class="section dashboard">

            <div class="card">
                <nav>
                    <div class="nav nav-tabs" id="nav-tab" role="tablist">
                        <button class="nav-link active" id="nav-home-tab" data-bs-toggle="tab"
                            data-bs-target="#nav-home" type="button" role="tab" aria-controls="nav-home"
                            aria-selected="true">Vulnerability
                            Assessment</button>
                        <button class="nav-link" id="nav-profile-tab" data-bs-toggle="tab" data-bs-target="#nav-profile"
                            type="button" role="tab" aria-controls="nav-profile" aria-selected="false">Configuration
                            Assessment</button>
                    </div>
                </nav>
                <div class="card-body">
                    <div class="tab-content" id="nav-tabContent">
                        <div class="tab-pane fade show active" id="nav-home" role="tabpanel"
                            aria-labelledby="nav-home-tab">

                            <br>

                            <div class="row" style="font-size: small;">
                                <div class="col-3">
                                    <h6>Profile</h6>
                                    <select class="form-select" aria-label="Default select example" style="font-size: small;">
                                        <option selected>Default Profile</option>
                                    </select>
                                </div>
                                <div class="col-3">
                                    <h6>Status</h6>
                                    <select class="form-select" aria-label="Default select example" style="font-size: small;">
                                        <option selected>All</option>
                                        <option value="1">Compliant</option>
                                        <option value="2">Non-Compliant</option>
                                        <option value="3">Not Checked-in</option>
                                        <option value="3">Not Applicable</option>
                                    </select>
                                </div>
                                <div class="col text-primary" style="align-self: center; text-align: end; cursor: pointer;">
                                    <i class="fa-solid fa-file-export"></i> Export Details
                                </div>
                            </div>

                            <br>

                            <table id="vulAssessmentTable" class="table" style="width: 100%;">
                                <thead>
                                    <tr>
                                        <th><input type="checkbox" class="selectAll form-check-input"
                                                id="vulAssessmentSelectAll">
                                        </th>
                                        <th scope="col">Machine Name</th>
                                        <th scope="col">Operating System</th>
                                        <th scope="col">Security Definition</th>
                                        <th scope="col">Profile</th>
                                        <th scope="col">Compliance</th>
                                        <th scope="col">Last Scan Time (GMT)</th>
                                        <!-- <th scope="col">Scan Status</th> -->
                                    </tr>
                                </thead>
                                <tbody>


                                </tbody>
                            </table>

                        </div>

                        <div class="tab-pane fade" id="nav-profile" role="tabpanel" aria-labelledby="nav-profile-tab">

                            <br>

                            <div class="row">
                                <div class="col-3">
                                    <h6>Profile</h6>
                                    <select class="form-select" aria-label="Default select example" style="font-size: small;">
                                        <option selected>CAT 1 Only</option>
                                        <option value="1">I - Mission Critical Classified</option>
                                        <option value="2">I - Mission Critical Public</option>
                                        <option value="3">I - Mission Critical Sensitive</option>
                                        <option value="3">I - Mission Support Classified</option>
                                        <option value="3">I - Mission Support Public</option>
                                    </select>
                                </div>
                                <div class="col-3">
                                    <h6>Status</h6>
                                    <select class="form-select" aria-label="Default select example" style="font-size: small;">
                                        <option selected>All</option>
                                        <option value="1">Compliant</option>
                                        <option value="2">Non-Compliant</option>
                                        <option value="3">Not Checked-in</option>
                                        <option value="3">Not Applicable</option>
                                    </select>
                                </div>
                                <div class="col text-primary" style="align-self: center; text-align: end; cursor: pointer;">
                                    <i class="fa-solid fa-file-export"></i> Export Details
                                </div>
                            </div>

                            <br>

							<div class="table-responsive">
	                            <table id="configAssessmentTable" class="table" style="width: 100%;">
	                                <thead>
	                                    <tr>
	                                        <th><input type="checkbox" class="selectAll form-check-input"
	                                                id="configAssessmentSelectAll">
	                                        </th>
	                                        <th scope="col">Machine Name</th>
	                                        <th scope="col">Operating System</th>
	                                        <th scope="col">Security Definition</th>
	                                        <th scope="col">Profile</th>
	                                        <th scope="col">Compliance</th>
	                                        <th scope="col">Last Scan Time (GMT)</th>
	                                        <!-- <th scope="col">Scan Status</th> -->
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


        </section>
 </main>
 <input id="hostIds" type="hidden" name="endDevicesArr" value="" />
 <input id="actionId" type="hidden" name="actionId" value="" />
</form>

</body>
</html>
    
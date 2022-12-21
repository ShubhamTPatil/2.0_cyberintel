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

	$('.nav-selected').removeClass('nav-selected');
	$('#scanResult').addClass('nav-selected');
	
    let vulAssesmentData = [
	        {
	            "Machine Name": "vm-rhel-clarinet",
	            "Operating System": "Linux CentOS Linux release 7.9.2009",
	            "Security Definition": "Windows",
	            "Profile": "Default Profile",
	            "Compliance": "312/513",
	            "Last Scan": "2022-21-12 05:19:03:0",
	            "Scan Status": "Compliant"
	        },

	        {
	            "Machine Name": "defensight-linux1",
	            "Operating System": "Linux Red Hat Enterprise Linux Server release 7.9",
	            "Security Definition": "Red Hat Enterprise Linux 7 OVAL definitions",
	            "Profile": "Default Profile",
	            "Compliance": "412/513",
	            "Last Scan": "2022-21-12 15:19:03:0",
	            "Scan Status": "Not Compliant"
	        },

	        {
	            "Machine Name": "defensight-linux1",
	            "Operating System": "Linux CentOS Linux release 7.9",
	            "Security Definition": "CentOS Linux 7 OVAL Patch definitions",
	            "Profile": "Default Profile",
	            "Compliance": "89/95",
	            "Last Scan": "2022-09-21 12:01:03:0",
	            "Scan Status": "Compliant"
	        },

	        {
	            "Machine Name": "Win-10-VM",
	            "Operating System": "	Microsoft Windows 10 Enterprise",
	            "Security Definition": "Microsoft Windows 10 OVAL Patch Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "300/513",
	            "Last Scan": "2022-09-08 15:19:03:0",
	            "Scan Status": "Compliant"
	        },

	        {
	            "Machine Name": "Win-10-VM",
	            "Operating System": "	Microsoft Windows 10 Enterprise",
	            "Security Definition": "Microsoft Windows 10 OVAL Vulnerability Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "200/513",
	            "Last Scan": "2022-09-20 12:29:04:0",
	            "Scan Status": "Not Compliant"
	        },

	        {
	            "Machine Name": "defensight-qa1",
	            "Operating System": "	Microsoft Windows 10 Pro",
	            "Security Definition": "Microsoft Windows 10 OVAL Vulnerability Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "500/513",
	            "Last Scan": "2022-09-21 12:28:04:0",
	            "Scan Status": "Compliant"
	        },

	        {
	            "Machine Name": "defensight-qa2",
	            "Operating System": "	Microsoft Windows 10 Enterprise",
	            "Security Definition": "Microsoft Windows 10 OVAL Vulnerability Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "510/513",
	            "Last Scan": "2022-09-21 05:14:03:0",
	            "Scan Status": "Compliant"
	        },

	        {
	            "Machine Name": "defensight-qa2",
	            "Operating System": "Microsoft Windows 10 Enterprise",
	            "Security Definition": "Microsoft Windows 10 OVAL Patch Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "400/513",
	            "Last Scan": "2022-09-18 10:19:03:0",
	            "Scan Status": "Not Compliant"
	        },

	        {
	            "Machine Name": "vm-tuner2",
	            "Operating System": "Microsoft Windows Server 2012 Datacenter",
	            "Security Definition": "Microsoft Windows Server 2012 OVAL Vulnerability Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "300/513",
	            "Last Scan": "2022-09-08 15:19:03:0",
	            "Scan Status": "Compliant"
	        },

	        {
	            "Machine Name": "reverseproxy",
	            "Operating System": "Microsoft Windows 2019 Enterprise",
	            "Security Definition": "Microsoft Windows Server 2019 OVAL Vulnerability Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "400/513",
	            "Last Scan": "2022-09-20 04:19:12:0",
	            "Scan Status": "Not Compliant"
	        },

	        {
	            "Machine Name": "reverseproxy",
	            "Operating System": "Microsoft Windows 2019 Enterprise",
	            "Security Definition": "Microsoft Windows Server 2019 OVAL Patch Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "10/23",
	            "Last Scan": "2022-09-20 04:19:12:0",
	            "Scan Status": "Not Compliant"
	        },

	        {
	            "Machine Name": "vm-master-trans",
	            "Operating System": "Microsoft Windows Server 2019 Standard",
	            "Security Definition": "Microsoft Windows Server 2019 OVAL Vulnerability Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "300/513",
	            "Last Scan": "2022-19-12 15:19:03:0",
	            "Scan Status": "Not Compliant"
	        },

	        {
	            "Machine Name": "vm-master-trans",
	            "Operating System": "Microsoft Windows Server 2019 Standard",
	            "Security Definition": "Microsoft Windows Server 2019 OVAL Patch Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "10/15",
	            "Last Scan": "2022-19-12 15:19:03:0",
	            "Scan Status": "Not Compliant"
	        },

	        {
	            "Machine Name": "vm-tuner",
	            "Operating System": "Microsoft Windows Server 2016 Datacenter",
	            "Security Definition": "Microsoft Windows Server 2016 OVAL Vulnerability Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "300/513",
	            "Last Scan": "2022-09-08 15:19:03:0",
	            "Scan Status": "Compliant"
	        },
	        {
	            "Machine Name": "vm-tuner",
	            "Operating System": "Microsoft Windows Server 2016 Datacenter",
	            "Security Definition": "Microsoft Windows Server 2016 OVAL Patch Definitions",
	            "Profile": "Default Profile",
	            "Compliance": "8/13",
	            "Last Scan": "2022-09-08 15:19:03:0",
	            "Scan Status": "Compliant"
	        }
	    ];

	    let vulAssessmentIndex = 0;
	    
	    $('#vulAssessmentTable').DataTable({
	        "destroy": true, // In order to reinitialize the datatable
	        "pagination": true, // For Pagination
	        "bPaginate": true,
	        "sorting": false, // For sorting
	        "ordering": false,
	        "searching": true,
	        "aaData": vulAssesmentData,
	        "columns": [{},
	        {
	            "data": "Machine Name"
	        }, {
	            "data": "Operating System"
	        }, {
	            "data": "Security Definition"
	        }, {
	            "data": "Profile"
	        }, {
	            "data": "Compliance"
	        }, {
	            "data": "Last Scan"
	        }, {
	            "data": "Scan Status"
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
	        "aaData": vulAssesmentData,
	        "columns": [{},
	        {
	            "data": "Machine Name"
	        }, {
	            "data": "Operating System"
	        }, {
	            "data": "Security Definition"
	        }, {
	            "data": "Profile"
	        }, {
	            "data": "Compliance"
	        }, {
	            "data": "Last Scan"
	        }, {
	            "data": "Scan Status"
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
                                        <th scope="col">Operating SYstem</th>
                                        <th scope="col">Security Definition</th>
                                        <th scope="col">Profile</th>
                                        <th scope="col">Compliance</th>
                                        <th scope="col">Last Scan</th>
                                        <th scope="col">Scan Status</th>
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

                            <table id="configAssessmentTable" class="table" style="width: 100%;">
                                <thead>
                                    <tr>
                                        <th><input type="checkbox" class="selectAll form-check-input"
                                                id="configAssessmentSelectAll">
                                        </th>
                                        <th scope="col">Machine Name</th>
                                        <th scope="col">Operating SYstem</th>
                                        <th scope="col">Security Definition</th>
                                        <th scope="col">Profile</th>
                                        <th scope="col">Compliance</th>
                                        <th scope="col">Last Scan</th>
                                        <th scope="col">Scan Status</th>
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
 </main>

</body>
</html>
    
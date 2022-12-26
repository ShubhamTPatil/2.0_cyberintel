<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>
<%@ page
	import="com.marimba.apps.subscription.common.ISubscriptionConstants"
	contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<!DOCTYPE html>
<html lang="en">
<head>


<link rel="stylesheet" type="text/css"
	href="/spm/css/newdashboard/bootstrap.min.css" />
<link rel="stylesheet" type="text/css"
	href="/spm/css/newdashboard/bootstrap-icons.min.css" />
<link rel="stylesheet" type="text/css"
	href="/spm/css/newdashboard/all.min.css" />
<link rel="stylesheet" type="text/css"
	href="/spm/css/newdashboard/datatables.min.css" />
<link rel="stylesheet" type="text/css"
	href="/spm/css/newdashboard/style.css" />

<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript"
	src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript"
	src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>

<script type="text/javascript">

    $(function () {

      $('.nav-selected').removeClass('nav-selected');
      $('#runScan').addClass('nav-selected');
      
      var resp = '<bean:write name="newRunScanForm" property="runScanJson"/>';

      resp = (resp).replace(/&quot;/g, '"');

      //console.log("resp = "+resp);
	  //console.log("json ="+JSON.parse(resp));
      
      var jsonData = JSON.parse(resp);
      var data = jsonData['data'];
	  
      //console.log(data);

      $('#cveDate').text(data['cVELastUpdated']);
      $('#vulDate').text(data['vulDefLastUpdated']);
      $('#secDate').text(data['secDefLastUpdated']);

      $('#runScanTable').DataTable({
          "destroy": true, // In order to reinitialize the datatable
          "pagination": true, // For Pagination
          "bPaginate": true,
          "sorting": false, // For sorting
          "ordering": false,
          "searching": true,
          'fnDrawCallback': function (oSettings) {
              $('.dataTables_filter').each(function () {
                  $(this).append($('#runScanButton'));
              });
          },
          language: {
              search: "_INPUT_",
              searchPlaceholder: "Search..."
          },
          "aaData": data['machineList'],
          "columns": [{},
          {
              "data": "machineName"
          }, {
              "data": "machineLastScan"
          }],
          'columnDefs': [{
              'targets': 0,
              'searchable': true,
              'orderable': false,
              'className': 'dt-body-left',
              'render': function (data, type, full, meta) {
                  return '<input type="checkbox" class="form-check-input" name="runScanCheckbox" value="' + full['machineName'] + '">';
              }
          }]
      });

      $('#runScanButton').click(function () {
		  let array = [];
          $('input[name=runScanCheckbox]:checked').each(function () {
              array.push($(this).val());
          })
		alert("Endpoint scan has been initiated");
          //console.log(array);
		  console.log(array.toString());
		  document.getElementById("hostIds").value = array.toString();
		  
		  $("#form_id").submit();
      });




    });

  </script>
  <script>
	//showMsg();
	const myTimeout = setTimeout(closeMsg, 5000);
          
	function showMsg(){
		 document.getElementById("showMsg").click();
	}
	function closeMsg() {
		 document.getElementById("closeMsg").click();
	}
  </script>
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
</head>
<body>

		<jsp:include page="header.jsp" />
		<jsp:include page="sidebar.jsp" />

		<form name="newRunScanForm" id="form_id" action="/spm/runscancli.do" method="post">
		<main id="main" class="main">
		<div class="pagetitle">

			<div class="d-flex bd-highlight justify-content-center">
				<div class="p-2 flex-grow-1 bd-highlight">
					<span class="pagename">Run Scan</span> <span
						data-bs-toggle="tooltip" data-bs-placement="right"
						title="Run Scan"><i
						class="fa-solid fa-circle-info text-primary"></i></span>
				</div>
				<div class="refresh p-2 bd-highlight text-primary align-self-center"
					data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh"
					style="cursor: pointer;">
					<i class="fa-solid fa-arrows-rotate"></i>
				</div>
				<div class="p-2 bd-highlight text-primary align-self-center"
					data-bs-toggle="tooltip" data-bs-placement="right" title="Download"
					style="cursor: pointer;">
					<i class="fa-solid fa-download"></i>
				</div>
				<div class="p-2 bd-highlight text-primary align-self-center">
					<a href="/shell/dashboard.do"><i
						class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>
						CMS Home </a>
				</div>
			</div>
		</div>
		
		
		 <section class="section dashboard">

            <div class="card">
                <div class="card-body">
                    <br />
					<div class="p-2 mb-2 text-dark" style="font-size: medium; background-color:#d9edf7;">
                        <i class="fa-solid fa-circle-dot text-primary" style="font-size: small;"></i>&nbsp; CVE last
                        updated (<span id="cveDate"></span>)
                    </div>

                    <div class="p-2 mb-2 text-dark" style="font-size: medium; background-color:#d9edf7;">
                        <i class="fa-solid fa-circle-dot text-primary" style="font-size: small;"></i>&nbsp;
                        Vulnerabilities Definitions last updated (<span id="vulDate"></span>)
                    </div>

                    <div class="p-2 mb-2 text-dark" style="font-size: medium; background-color:#d9edf7;">
                        <i class="fa-solid fa-circle-dot text-primary" style="font-size: small;"></i>&nbsp; Security
                        Definitions last updated (<span id="secDate"></span>)
                    </div>
                      
                    
                    <h5 class="card-title" style="font-weight: bold;"> Select the machine to Scan </h5>
                    <button id="runScanButton" type="button" class="btn btn-sm btn-primary" style="margin-left:20px;">SCAN</button>
                    <table id="runScanTable" class="table">
                        <thead>
                            <tr>
                                <th scope="col"><input type="checkbox" id="runScanSelectAll"
                                        class="selectAll form-check-input"></th>
                                <th scope="col">Machine Name</th>
                                <th scope="col">Last Scan Time (UTC)</th>
                            </tr>
                        </thead>
                        <tbody>

                        </tbody>
                    </table>
                </div>
            </div>

        </section>
		
<button type="button" id="showMsg" class="btn btn-primary" data-toggle="modal" onclick="showMsg()" data-target="#exampleModal" style="display:none"></button>
<!-- Modal -->
<div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" id="closeMsg" class="close" data-dismiss="modal" onclick="closeMsg()" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        Endpoint scan has been initiated
      </div>
    </div>
  </div>
</div>
		</main>
		<input id="hostIds" type="hidden" name="endDevicesArr" value="" />
	</form>
	
</body>
</html>
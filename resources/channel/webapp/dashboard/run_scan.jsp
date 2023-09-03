<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>
<%@ page
	import="com.marimba.apps.subscription.common.ISubscriptionConstants"
	contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
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

    $(".refresh").click(function (e) {
            window.location.href = "/spm/runscan.do";
        });

    var objYog=[];

      $('#runScan').addClass('nav-selected');

     var responseMsg = '<bean:write name="newRunScanForm" property="responseMsg"/>';

     //alert('updated code for responseMsg');
     //alert(responseMsg);
     if(responseMsg != 'undefined' && responseMsg != null && responseMsg.trim() != ""){

        var alertModal = new bootstrap.Modal(document.getElementById('alertModal'), {
                              				  keyboard: false });
                    $('#alertMessage').html('Scan is initiated for machines : </br>'+responseMsg);
                    alertModal.show();

     //$('#responseMsgSectionId').show();
     // $('#responseMsgId').text(responseMsg);
     }else{

       //$('#responseMsgSectionId').hide();
     }

      var resp = '<bean:write name="newRunScanForm" property="runScanJson"/>';

      resp = (resp).replace(/&quot;/g, '"');

      //console.log("resp = "+resp);
	  //console.log("json ="+JSON.parse(resp));


      var jsonData = JSON.parse(resp);
      var data = jsonData['data'];


	 // alert('Updated Data');
      console.log(data);

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
          },
          {
              "data": "machineLastScan"
          },
          {
               "data": "scanStatus"
          }],
          'columnDefs': [{
              'targets': 0,
              'searchable': true,
              'orderable': false,
              'className': 'dt-body-left',
              'render': function (data, type, full, meta) {
                  var disabled = full["scanStatus"] === "Ready to Scan" ? "" : "disabled";
                  return '<input type="checkbox" class="form-check-input" name="runScanCheckbox" value="' + full['machineName'] + '" ' + disabled + '>';
              }
          }],
          'rowCallback': function (row, data, index) {
               switch (data['scanStatus']) {
                   case 'In Progress':
                       $(row).find('td:eq(3)').addClass('text-success');
                       break;

                   case 'Failed to Connect':
                       $(row).find('td:eq(3)').addClass('text-danger');
                       break;

                   case 'Authentication Failed':
                       $(row).find('td:eq(3)').addClass('text-primary');
                       break;

                   case 'Idle':
                       $(row).find('td:eq(3)').addClass('text-muted');
                       break;

                   default:
                       break;
               }
           }
      });


      //Ajax Call onload

          //alert('aSyncCall before this upload');
          var machineNameList = [];

          objYog =  data['machineList'];
          console.log('updated objYog::'+objYog);

          console.log(objYog);
          for(let arr of objYog){
            machineNameList.push(arr.machineName);
            console.log(arr.machineName)
           }
      			//asyncCall(machineNameList);

    });

    function doSubmit(frm, action) {

		  let array = [];
          $('input[name=runScanCheckbox]:checked').each(function () {
              array.push($(this).val());
          })
		  console.log(array.toString());

       // alert('New updated code updated');
        if(typeof array != "undefined" && array != null
           			&& array.length > 0){
           			//alert('New updated code 1');
            document.getElementById("hostIds").value = array.toString();
            frm.action.value = action;
            frm.submit();


        }else{

        //alert('New updated code 2');
            var alertModal = new bootstrap.Modal(document.getElementById('alertModal'), {
                      				  keyboard: false });
            $('#alertMessage').html('Please select at least one Machine to Scan.');
            alertModal.show();
        }
    }



    //Ajax call to fetch status for every 5 min
    function checkAfter10Seconds(timeout, machineNameList) {

    		console.log("timeout in run jsp = "+timeout);
    		console.log(new Date());

         // alert('Before ajax call in checkAfter10Seconds update');
    			  return new Promise(resolve => {
    		    setTimeout(() => {
    			    	$.ajax({
    							url : '../mim/remoteadmin/tuneradmin/securityScan.do',
    							type : 'POST',
    							dataType : 'text json',
    							data : {
    								action : "scanStatusCheck",
    								machineList:machineNameList
    							},
    							beforeSend : function() {
    							},
    							complete : function(xhr, status) {},
    							success : function(response) {
    						//	alert('In Success');
    						//	alert('After getting response::'+response);
    								resolve(response);
    							},
    							error : function(xhr,status,error){
    							 // alert('Getting some error::'+error);
    							  //  alert('xhr error::'+xhr.responseText);
    							  //  alert('status::'+status);

    							  console.log(error);
    							  console.log(xhr.responseText);
    							  console.log(status);
    							}
    						});

    		    }, timeout);
    		  });
    		}


        async function asyncCall(machineNameList) {
       // alert('start of asyncCall'+machineNameList);
        			let timeout = 500;

        		  while(1) {
          		  const result = await checkAfter10Seconds(timeout, machineNameList);
          			timeout = 30000;

          			// Expected output: "resolved"

          			if(typeof result != "undefined" && typeof result.status != "undefined" && result.status != null) {

          			//alert('Response got it::'+result);

          				//$('#smartwizard').smartWizard("goToStep", result.status);

          		  }

        			}
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

		<html:form name="newRunScanForm" action="/runscan.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm" onsubmit="return false;">
		<html:hidden property="action" />

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
                    <%-- <button id="runScanButton" type="button" class="btn btn-sm btn-primary" style="margin-left:20px;">SCAN</button> --%>
                    <input type="button" id="runScanButton" class="btn btn-sm btn-primary" style="margin-left:20px;" value="SCAN" onclick="doSubmit(this.form, 'runscan_machines')">
                    <table id="runScanTable" class="table">
                        <thead>
                            <tr>
                                <th scope="col"><input type="checkbox" id="runScanSelectAll"
                                        class="selectAll form-check-input"></th>
                                <th scope="col">Machine Name</th>
                                <th scope="col">Last Scan Time (UTC)</th>
                                 <th scope="col">Scan Status</th>
                            </tr>
                        </thead>
                        <tbody>

                        </tbody>
                    </table>
                </div>
            </div>

        </section>
		</main>

		<div class="modal fade" id="alertModal" tabindex="-1" aria-labelledby="alertModalLabel" aria-hidden="true">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title" id="alertModalLabel">Message</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body">
                <div id="alertMessage"></div>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal">OK</button>
              </div>
            </div>
          </div>
        </div>

		<input id="hostIds" type="hidden" name="endDevicesArr" value="" />
    </html:form>
	
</body>
</html>
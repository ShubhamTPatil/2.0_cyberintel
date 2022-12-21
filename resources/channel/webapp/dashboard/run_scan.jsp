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
	$(document).ready(function() {
		$('.nav-selected').removeClass('nav-selected');
		$('#runScan').addClass('nav-selected');
		$("#myTable").dataTable();
	});
	function toggle(source) {
		var checkboxes = document.querySelectorAll('input[type="checkbox"]');
		for (var i = 0; i < checkboxes.length; i++) {
			if (checkboxes[i] != source)
				checkboxes[i].checked = source.checked;
		}
	}

	function updateTable() {
		var tbodyRow, tbodycol1, tbodycol2, chkBox;
		var resp = '<bean:write name="newRunScanForm" property="runScanJson"/>';
		resp = (resp).replace(/&quot;/g, '"');
		var data2 = JSON.parse(resp);
		var obj = (data2.data);
		console.log("Incoming JSON .. " + data2.data);
		document.getElementById("CVEDate").innerText = "CVE last updated ("
				+ data2.data.cVELastUpdated + ")";
		document.getElementById("VulDate").innerText = "Vulnerabilities Definitions last updated ("
				+ data2.data.vulDefLastUpdated + ")";
		document.getElementById("SecDate").innerText = "Security Definitions last updated ("
				+ data2.data.secDefLastUpdated + ")";
		console.log(data2.data.MachineList);
		//Populate Host Table
		var DataTableBody = document.getElementById("myTableBody");
		for (var i = 0; i < data2.data.machineList.length; i++) {

			console.log(data2.data.machineList.machineName);
			tbodyRow = "tbodyRow" + i;
			tbodyRow = document.createElement('tr');
			tbodyRow.id = "tbodyRow" + i;
			DataTableBody.appendChild(tbodyRow);

			tbodycol1 = "tbodycol1" + i;
			tbodycol1 = document.createElement('td');

			tbodycol1.style.width = "50%";
			tbodycol1.align = "left";
			tbodycol1.style.textAlign = "left";

			tbodycol1.id = "tbodycol1" + i;
			chkBox = '<input type="checkbox" name= "chkB" value='+data2.data.machineList[i].machineName+'>&nbsp;'
					+ data2.data.machineList[i].machineName;
			tbodycol1.innerHTML = chkBox;
			tbodyRow.appendChild(tbodycol1);

			tbodycol2 = "tbodycol2" + i;
			tbodycol2 = document.createElement('td');

			tbodycol2.style.width = "50%";
			tbodycol2.align = "left";
			tbodycol2.style.textAlign = "left";

			tbodycol2.id = "tbodycol2" + i;
			tbodycol2.innerHTML = data2.data.machineList[i].machineLastScan;
			tbodyRow.appendChild(tbodycol2);

		}
	}
	function runCliScan() {
		// Form submission
		getCheckedBoxes("chkB");
/* 		document.getElementById("form_id").action = "/runscancli.do";
		document.getElementById("form_id").type = "com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm"; */
		$("#form_id").submit();
	}

	var checkboxesChecked;
	// Pass the checkbox name to the function
	function getCheckedBoxes(chkboxName) {
		var checkboxes = document.getElementsByName(chkboxName);
		var checkboxesChecked = [];
		// loop over them all
		for (var i = 0; i < checkboxes.length; i++) {
			// And stick the checked ones onto an array...
			if (checkboxes[i].checked) {
				checkboxesChecked.push(checkboxes[i].value);
			}
			//checkboxesChecked.push(checkboxes[i].value);
		}

		// Return the array if it is non-empty, or null
		//return checkboxesChecked.length > 0 ? checkboxesChecked : null;
		checkboxesChecked = checkboxesChecked;
		console.log("chk1 : " + checkboxesChecked);
		console.log("chk2 : " + checkboxesChecked.toString());
		document.getElementById("hostIds").value = checkboxesChecked.toString();
	}
</script>
<style>
.nav-title {
    font-size: 13px;
    font-weight: bold;
    color: black;
    font-family: "Poppins", sans-serif;
}
</style>
<script type="text/javascript">

    $(function () {

      $('.nav-selected').removeClass('nav-selected');
      $('#definitionsUpdate').addClass('nav-selected');


    });

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

		<form name="newRunScanForm" id="form_id" action="/runscancli.do" method="post">
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
			<div class="row">
				<div class="col-3">
					<span class="nav-title" id="CVEDate"></span>
				</div>
				<div class="col">
					<span class="nav-title" id="VulDate"></span>
				</div>
				<div class="col">
					<span class="nav-title" id="SecDate"></span>
				</div>
			</div>
			</div></div>
			<br/>
			<div class="card">
			<div class="card-body">
			<div class="row">
				<div class="col-12">
					<div class="d-grid gap-2 d-md-flex justify-content-md-end">
					<button type="submit" class="btn btn-sm btn-primary" onclick="runCliScan()">SCAN</button>
					</div>
				</div>
				
			</div>
				
				<br/>
				<table id="myTable" class="table table-borderless" style="width: 100%;box-shadow: 1px 3px 3px #3333333d!important;">
					<thead>
						<tr>
							<th scope="col" style="text-align: left;">Machine Name</th>
							<th scope="col" style="text-align: left;">Last Scan
								Time(UTC)</th>
						</tr>
					</thead>
					<tbody id="myTableBody">

					</tbody>
				</table>
			</div>
			</div>
		</section>
		</main>
		<input id="hostIds" type="hidden" name="endDevicesArr" value="" />
	</form>
	<script type="text/javascript">
		updateTable();
	</script>
</body>
</html>
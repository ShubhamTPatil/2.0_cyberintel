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
<%@ page import = "java.util.*,com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm" %>
<jsp:useBean id="session_dist" class="com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm"  type="com.marimba.apps.subscriptionmanager.webapp.forms.RunScanForm" scope="session"/>
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

$(document).ready(function(){
                $("#myTable").dataTable();
});
function toggle(source) {
    var checkboxes = document.querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i] != source)
            checkboxes[i].checked = source.checked;
    }
}
</script>
<%  
String JSONResp= "";
JSONResp=session_dist.getRunScanJson();  
out.print("JSON is "+JSONResp);  
%>
<script type="text/javascript">
function updateTable(){
	var tbodyRow,tbodycol1,tbodycol2,chkBox;
	var JSONResp="<%=JSONResp%>";
			var data2=JSON.parse(JSONResp);
			var obj = (data2.data);	
			console.log(data2);
	    	document.getElementById("CVEDate").innerText="CVE last updated("+data2.data.cVELastUpdated+")";
	    	document.getElementById("VulDate").innerText="Vulnerabilities Definitions last upadted("+data2.data.vulDefLastUpdated+")";
	    	document.getElementById("SecDate").innerText="Security Definitions last updated("+data2.data.secDefLastUpdated+")";
	    	console.log(data2.data.MachineList);
	    	//Populate Host Table
	    	var DataTableBody=document.getElementById("myTableBody");
	    	for(var i=0;i<data2.data.machineList.length;i++){
	    	
	    	console.log(data2.data.machineList.machineName);
	    	tbodyRow="tbodyRow"+i;
			tbodyRow = document.createElement('tr');
			tbodyRow.id="tbodyRow"+i;
			DataTableBody.appendChild(tbodyRow);
			
			tbodycol1="tbodycol1"+i;
			tbodycol1 = document.createElement('td');
			tbodycol1.id="tbodycol1"+i;
			chkBox='<input type="checkbox" name= "chkB" value='+data2.data.machineList[i].machineName+'>&nbsp;'+data2.data.machineList[i].machineName;
			tbodycol1.innerHTML=chkBox;
			tbodyRow.appendChild(tbodycol1);
			
			tbodycol2="tbodycol2"+i;
			tbodycol2 = document.createElement('td');
			tbodycol2.id="tbodycol2"+i;
			tbodycol2.innerHTML=data2.data.machineList[i].machineLastScan;
			tbodyRow.appendChild(tbodycol2);
			
			
	    	}
		 }
}
 </script>
<!-- Start - Populate Host Table -->
<script>updateTable();</script>
</head>
<body>


	<html:form name="newRunScanForm" action="/newRunScan.do">

		<html:hidden property="action" value="runScan" />


		<jsp:include page="header.jsp" />
		<jsp:include page="sidebar.jsp" />


		<main id="main" class="main">
		<div class="pagetitle">

			<div class="d-flex bd-highlight justify-content-center">
				<div class="p-2 flex-grow-1 bd-highlight">
					<span class="pagename">Run Scan</span> <span
						data-bs-toggle="tooltip" data-bs-placement="right"
						title="Run Scan"><i
						class="fa-solid fa-circle-info text-primary"></i></span>
				</div>
				<div class="p-2 bd-highlight text-primary align-self-center"
					data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh">
					<i class="fa-solid fa-arrows-rotate"></i>
				</div>
				<div class="p-2 bd-highlight text-primary align-self-center"
					data-bs-toggle="tooltip" data-bs-placement="right" title="Download">
					<i class="fa-solid fa-download"></i>
				</div>
				<div class="p-2 bd-highlight text-primary align-self-center">
					<a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>
					CMS Home
					</a>
				</div>
			</div>

		</div>

		<section class="section dashboard">
			<div class="row">
				<div class="col-md-12">&nbsp;</div>
			</div>
			<div class="row">
				<div class="col-md-3">
					<h5 id="CVEDate"></h5>
				</div>
				<div class="col-md-5">
					<h5 id="VulDate"></h5>
				</div>
				<div class="col-md-4">
					<h5 id="SecDate"></h5>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">&nbsp;</div>
			</div>
			<div class="row">
				<div class="col-md-6"></div>
				<div class="col-md-offset-2 col-md-2">
					<button type="button" class="btn btn-light btn-md"
						style="width: 125px !important;">CANCEL</button>
				</div>
				<div class="col-md-2">
					<button type="button" class="btn btn-primary btn-md"
						style="width: 125px !important;">SCAN</button>
				</div>
			</div>

			<div class="row">
				<div class="col-sm-12">
					<h5>
						<input type="checkbox" onclick='toggle(this)'>&nbsp;
						Select / Deselect All the machine(s) to scan
					</h5>
				</div>
			</div>
			<div class="row">
				<div class="col-md-12">&nbsp;</div>
			</div>
			<div class="row"
				style="box-shadow: 1px 1px 1px #3333333d !important;">
				<div class="col-md-12">
					<table id="myTable" class="table-responsive">
						<thead>
							<tr>
								<th>Machine Name</th>
								<th>Last Scan Time(UTC)</th>
							</tr>
						</thead>
						<tbody id="myTableBody">

						</tbody>
					</table>
				</div>
			</div>
		</section>
		</main>
	</html:form>
</body>
</html>

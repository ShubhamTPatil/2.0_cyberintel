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


<script src="https://code.jquery.com/jquery-3.6.1.min.js"
    integrity="sha256-o88AwQnZB+VDvE9tvIXrMQaPlFFSUTR+nldQm1LuPXQ=" crossorigin="anonymous"></script>
<!-- <script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>-->
<script type="text/javascript"
	src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript"
	src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>


<script type="text/javascript">

$(function () {
	
	$('#vdefUpdateNow').click(function () {
		alert("VDEF FILE TRANSFER HAS BEEN TRIGGERED..");
        $.ajax({
        	type: 'POST', dataType: 'json text', url: '/spm/vdefTransfer.do',
        	data: {action: 'vdef'},        	
            success: function (response) {
            	 //$("#report_status_tbody").html(response.status);
            	 console.log(response);
            	 console.log(JSON.stringify(response));
            	 //document.getElementById('reportStatus').value = response.message;
            	
            }});

     });    
})
</script>

<script type="text/javascript">

	$(function() {

		$('.nav-selected').removeClass('nav-selected');
		$('#definitionsUpdate').addClass('nav-selected');

		let defUpdateData = [
				{
					"SecurityProfileName" : "SCAP-compliant definitions for Desktop Security policies manipulation",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of CentOS 6",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of CentOS 7",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Google Chromium",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Google Chromium",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Debian 8",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Firefox",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Java Runtime Environment",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Red Hat OpenStack Platform 7",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Red Hat Enterprise Linux 6",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Red Hat Enterprise Linux 7",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Scientific Linux 6",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "Guide to the Secure Configuration of Scientific Linux 7",
					"AssessmentType" : "Configuration",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},

				{
					"SecurityProfileName" : "File content for OVAL file suspicious-cyberesi-oval.xml",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Adobe Acrobat Reader DC Classic Track Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Adobe Acrobat Reader DC Continuous Track Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft DotNet Framework 4.0 STIG",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Google Chrome Current Windows STIG",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Internet Explorer 11 Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Office System 2013 STIG",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Office System 2016 Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Outlook 2013 STIG",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Publisher 2016 Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows Server 2008 R2 Domain Controller Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows Server 2008 R2 Member Server Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows Defender Antivirus Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows Server 2016 Security Technical Implementation Guide Windows Server 2016 Standalone Server Windows Server 2016 Domain Controller Windows Server 2016 Member Server",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows 10 Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows 2008 Domain Controller Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows 2008 Member Server Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows Server 2012/2012 R2 Domain Controller Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows Server 2012/2012 R2 Member Server Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Windows Firewall with Advanced Security Security Technical Implementation Guide",
					"AssessmentType" : "Configuration",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Red Hat Enterprise Linux 3 OVAL definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Red Hat Enterprise Linux 4 OVAL definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Red Hat Enterprise Linux 5 OVAL definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Red Hat Enterprise Linux 6 OVAL definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Red Hat Enterprise Linux 7 OVAL definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 4 OVAL Patch definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 3 OVAL Patch definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 5 OVAL Patch definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 6 OVAL Patch definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 7 OVAL Patch definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 3 OVAL Vulnerability definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 4 OVAL Vulnerability definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 5 OVAL Vulnerability definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 6 OVAL Vulnerability definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "CentOS Linux 7 OVAL Vulnerability definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Non-Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 10 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 7 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 8 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 8.1 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2003 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2008 R2 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2008 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2012 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2012 R2 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2019 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Storage Server 2012 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2016 OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Vista OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows XP OVAL Patch Definitions",
					"AssessmentType" : "Patch",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 10 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 7 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 8.1 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows 8 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2003 R2 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2003 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2008 R2 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2008 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2012 R2 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2012 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2016 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Server 2019 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Storage Server 2012 OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows Vista OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				},
				{
					"SecurityProfileName" : "Microsoft Windows XP OVAL Vulnerability Definitions",
					"AssessmentType" : "Vulnerability",
					"Platform" : "Windows",
					"LastUpdated" : "2022-12-18 10:40:20.0",
					"Status" : "Sync Required"
				} ];

		let defUpdateIndex = 0;

		$('#defUpdateTable')
				.DataTable(
						{
							"destroy" : true, // In order to reinitialize the datatable
							"pagination" : true, // For Pagination
							"bPaginate" : true,
							"sorting" : false, // For sorting
							"ordering" : false,
							"searching" : true,
							"aaData" : defUpdateData,
							"columns" : [ {}, {
								"data" : "SecurityProfileName"
							}, {
								"data" : "AssessmentType"
							}, {
								"data" : "Platform"
							}, {
								"data" : "LastUpdated"
							}, {
								"data" : "Status"
							} ],
							'columnDefs' : [ {
								'targets' : 0,
								'searchable' : true,
								'orderable' : false,
								'className' : 'dt-body-left',
								'render' : function(data, type, full, meta) {
									return '<input type="checkbox" class="form-check-input" name="vulAssessmentCheckbox" value="' + defUpdateIndex++ + '">';
								}
							} ]
						});

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

	<html:form name="vdefTransferForm" id="def_Update_formId"
		action="/spm/vdefTransfer.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.VdefTransferForm">	
		<html:hidden property="action"/>
		
		
	<main id="main" class="main">
	<div class="pagetitle">

		<div class="d-flex bd-highlight justify-content-center">
			<div class="p-2 flex-grow-1 bd-highlight">
				<span class="pagename">Definitions Update</span> <span
					data-bs-toggle="tooltip" data-bs-placement="right"
					title="Definitions Update"><i
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
					class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS
					Home</a>
			</div>
		</div>

	</div>


	<section class="section dashboard">

		<div class="card">
			<nav>
				<div class="nav nav-tabs nav-title" id="nav-tab" role="tablist">
					<button class="nav-link active" id="nav-home-tab"
						data-bs-toggle="tab" data-bs-target="#nav-home" type="button"
						role="tab" aria-controls="nav-home" aria-selected="true">CVE
						INFORMATION</button>
					<button class="nav-link" id="nav-profile-tab" data-bs-toggle="tab"
						data-bs-target="#nav-profile" type="button" role="tab"
						aria-controls="nav-profile" aria-selected="false">UPDATES</button>
				</div>
			</nav>
			<div class="card-body">
				<div class="tab-content" id="nav-tabContent">
					<div class="tab-pane fade show active" id="nav-home"
						role="tabpanel" aria-labelledby="nav-home-tab">
						<br />

						<div class="p-2 mb-2 text-dark"
							style="font-size: medium; background-color: #d9edf7;">
							<i class="fa-solid fa-circle-info text-primary"></i> CVE
							Definitions and Vulnerability Definitions are need to be updated
							every month.
						</div>

						<br />

						<div class="row"
							style="box-shadow: 1px 3px 3px #3333333d !important; padding-bottom: 20px;">

							<div class="col">
								<span style="font-weight: bold;">CVE Definitions last
									updated on (29/09/2022)</span><br /> <span>(Please ensure all
									information is upto date for accurate results)</span>
							</div>

							<div class="col">
								<div class="d-grid gap-2 d-md-flex justify-content-md-end">
									<button type="button" class="btn btn-sm btn-secondary"
										style="background-color: #d3d3d333; color: darkgray;">CANCEL</button>
									<div class="col-md-2">
										<button id="cveUpdateNow" type="button" class="btn btn-primary btn-md">UPDATE NOW</button>
									</div>
								</div>
							</div>
						</div>



						<br /> <br />

						<div class="row"
							style="box-shadow: 1px 3px 3px #3333333d !important; padding-bottom: 20px;">

							<div class="col">
								<span style="font-weight: bold;">Vulnerability
									Definitions last updated on (29/09/2022)</span><br /> <span>(Please
									ensure all information is upto date for accurate results)</span>
							</div>

							<div class="col">
								<div class="d-grid gap-2 d-md-flex justify-content-md-end">
									<button type="button" class="btn btn-sm btn-secondary"
										style="background-color: #d3d3d333; color: darkgray;">CANCEL</button>
									<button id="vdefUpdateNow" type="button"
													class="btn btn-primary btn-md">UPDATE NOW</button>
								</div>								
							</div>
						</div>
						
						<div class="modal fade" id="reports_status_modal" tabindex="-2" role="dialog">
						    <div class="modal-dialog modal-lg" style="width:95%;">
						        <div class="modal-content">
						            <div class="modal-header">
						                <h4 class="modal-title"><b><i class="fa fa-filter" aria-hidden="true" style="font-size:17px;color:#0073b7"></i>&nbsp;File Transfer Status</b>
						                </h4>
						                <div align="right"><button type="button" class="close" data-dismiss="modal">&times;</button></div>
						            </div>
						            <div class="modal-body" style="padding-top:1px;padding-bottom:5px;">
						                <div id="reportstatus" style="height:auto;">
						                    <div id="report_status_div" style="text-align:left;">
						                        <table id="report_status_table" border="1" style="width:100%">
						                            <colgroup width="5%"/>
						                            <colgroup width="20%"/>
						                            <colgroup width="15%"/>
						                            <colgroup width="*"/>
						                            <tbody id="report_status_tbody">
						                            </tbody>
						                        </table>
						                    </div>
						                </div>
						            </div>
						            <div class="modal-footer">
						                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
						            </div>
						        </div>
						    </div>
						</div>
																	
						<br /> <br />

						<div class="row">
							<div class="col-sm-3">
								<div class="progress"
									style="height: 8px !important; - -bs-progress-bar-bg: #1976d2 !important;">
									<div class="progress-bar active" name="CVEprogress"
										role="progressbar" aria-valuenow="80" aria-valuemin="0"
										aria-valuemax="100" style="width: 100%"></div>
								</div>
							</div>
							<div class="col-sm-9"></div>
						</div>
						<div class="row">
							<div class="col-sm-3">
								<span style="font-size: 10px !important;">Json file
									download is completed</span>
							</div>
							<div class="col-sm-9"></div>
						</div>
						<br />
						<div class="row">
							<div class="col-sm-3">
								<div class="progress"
									style="height: 8px !important; - -bs-progress-bar-bg: #1976d2 !important;">
									<div class="progress-bar active" name="CVEprogress"
										role="progressbar" aria-valuenow="80" aria-valuemin="0"
										aria-valuemax="100" style="width: 80%"></div>
								</div>
							</div>
							<div class="col-sm-9"></div>
						</div>
						<div class="row">
							<div class="col-sm-3">
								<span style="font-size: 10px !important;">File insertion
									to database is in progress</span>
							</div>
						</div>
					</div>
					<div class="tab-pane fade" id="nav-profile" role="tabpanel"
						aria-labelledby="nav-profile-tab">

						<br />
						<div>From this page, you can manage the available security
							content updates. You can synchronize the security content,
							monitor the progress of sync operations, or can view the details
							of a security content.</div>
						<br />


						<div class="p-3 mb-2 text-dark"
							style="font-size: medium; background-color: #d9edf7;">
							<i class="fa-solid fa-circle-info text-primary"></i> New security
							definitions are available.
						</div>

						<br />

						<div class="row">
							<div class="col-md-2">

								<h6>Status</h6>
								<select class="form-select" aria-label="Default select example"
									style="font-size: small;">
									<option selected>All</option>
									<option value="1">Sync Required</option>
									<option value="2">Synced</option>
								</select>

							</div>
							<div class="col-md-6"></div>
							<div class="col-md-4">
								<div class="d-grid gap-2 d-md-flex justify-content-md-end">
									<button type="button" class="btn btn-sm btn-outline-primary">VIEW
										DETAILS</button>
									<button type="button" class="btn btn-sm btn-primary">RE-SYNC</button>
								</div>
							</div>
						</div>

						<br>
						<table id="defUpdateTable" class="table" style="width: 100%;">
							<thead>
								<tr>
									<th><input type="checkbox"
										class="selectAll form-check-input" id="defUpdateSelectAll">
									</th>
									<th scope="col">Security Profile Name</th>
									<th scope="col">Assessment Type</th>
									<th scope="col">Platform</th>
									<th scope="col">Last Updated</th>
									<th scope="col">Status</th>
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
	</html:form>

</body>

</html>
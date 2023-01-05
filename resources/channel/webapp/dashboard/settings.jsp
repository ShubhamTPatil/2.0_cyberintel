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


<script type="text/javascript">

$(function () {
    $('#settings').addClass('nav-selected');
});

</script>

<style type="text/css">
    .settings svg {
        font-size: 55px;
    }
</style>

</head>
<body>


  <jsp:include page="header.jsp" />
  <jsp:include page="sidebar.jsp" />

  
 <main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename">Settings</span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="Settings"><i
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

    <section class="section dashboard settings">

        <div class="row">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body">
                        <p class="card-text">
                        <div class="row">
                            <br>
                            <div class="col-sm-2 align-self-center text-center">
                                <i class="fa-solid fa-plug"></i>
                            </div>
                            <div class="col-sm-10">
                                <a href="/spm/pluginEdit.do">Plug-in</a><br>
                                Configure the vinspector plug-in. The plug-in updates inspector on the endpoints.
                            </div>
                        </div>
                        </p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body">
                        <p class="card-text">
                        <div class="row">
                            <br>
                            <div class="col-sm-2 align-self-center text-center">
                                <i class="fa-solid fa-network-wired"></i>
                            </div>
                            <div class="col-sm-10">
                                <a href="/spm/namespaceLoad.do">Child Container</a><br>
                                Select the child container in which you would like to save policies.
                            </div>
                        </div>
                        </p>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body" style="min-height: 150px;">
                        <p class="card-text">
                        <div class="row">
                            <br>
                            <div class="col-sm-2 align-self-center text-center">
                                <i class="fa-regular fa-envelope"></i>
                            </div>
                            <div class="col-sm-10">
                                <a href="/spm/mailconfig.do">Email-Notification</a><br>
                                Specify the mail server setting needed by the application to send e-mail notifications.
                            </div>
                        </div>
                        </p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body" style="min-height: 150px;">
                        <p class="card-text">
                        <div class="row">
                            <br>
                            <div class="col-sm-2 align-self-center text-center">
                                <i class="fa-solid fa-shield-halved"></i>
                            </div>
                            <div class="col-sm-10">
                                <a href="/spm/scapSecurityTemplateListing.do?action=load">Custom SCAP security content definition for Non Windows</a><br>
                                Define custom SCAP content to be processed at endpoints, to validate the security compliance
                                (Applicable only for Non-Windows).
                            </div>
                        </div>
                        </p>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body">
                        <p class="card-text">
                        <div class="row">
                            <br>
                            <div class="col-sm-2 align-self-center text-center">
                                <i class="fa-brands fa-windows"></i>
                            </div>
                            <div class="col-sm-10">
                                <a href="/spm/usgcbSecurityTemplateListing.do?action=load">Custom SCAP security content definition for Windows</a><br>
                                Define custom SCAP content to be processed at endpoints, to validate the security compliance
                                (Applicable only for Windows).
                            </div>
                        </div>
                        </p>
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body">
                        <p class="card-text">
                        <div class="row">
                            <br>
                            <div class="col-sm-2 align-self-center text-center">
                                <i class="fa-solid fa-building-shield"></i>
                            </div>
                            <div class="col-sm-10">
                                <a href="/spm/usgcbSecurityTemplateListing.do?action=load">Custom security content definition</a><br>
                                Define custom SCAP content to be processed at endpoints, to validate the security compliance
                                (Applicable only for Operating System).
                            </div>
                        </div>
                        </p>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-body">
                        <p class="card-text">
                        <div class="row">
                            <br>
                            <div class="col-sm-2 align-self-center text-center">
                                <i class="fa-regular fa-file-lines"></i>
                            </div>
                            <div class="col-sm-10">
                                <a href="/spm/scapSecurityOsMappingCveIdsListing.do?action=load">Configure CVE-IDs Mapping for OS through Profile</a><br>
                                Define CVE-IDs mapping applicable for any Operating system w.r.t exclude CVE IDs while
                                generating report.
                            </div>
                        </div>
                        </p>
                    </div>
                </div>
            </div>
        </div>
      
    </section>


 </main>

</body>
</html>
    
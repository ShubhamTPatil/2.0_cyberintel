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

<script type="text/javascript">

$(function () {

    $('#dashboard').addClass('nav-selected');

});


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
                                <span class="text-muted small"><bean:write name="configDashboardForm" property="configScanCount"/></span><br />
                                <span class="text-muted small">Configuration Scan</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>


    </section>
  </main>
</body>
</html>

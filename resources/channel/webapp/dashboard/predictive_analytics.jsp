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

<title>Predictive Analytics</title>

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

    $('#predictiveAnalytics').addClass('nav-selected');

});

</script>

</head>
<body style="overflow:hidden;">


  <jsp:include page="header.jsp" />
  <jsp:include page="sidebar.jsp" />

  
 <main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename">Predictive Analytics</span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="DefenSight Predictive Analytics"><i
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

            <div class="row">
                <div class="col-3">
                    <div class="card" style="height:95%">
                        <div class="card-body">
                            <h5 class="card-title">Select Network Segment</h5>
                            <div id="zoneRadio">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="zoneRadio" id="1" checked/>
                                    <label class="form-check-label" for="1">Network Segment 1</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="zoneRadio" id="2" />
                                    <label class="form-check-label" for="2">Network Segment 2</label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="zoneRadio" id="3" />
                                    <label class="form-check-label" for="3">Network Segment 3</label>
                                </div>
                            </div>
                            <br />
                            <button type="button" id="start" class="btn btn-sm btn-primary"
                                style="position:absolute; bottom: 20px;">Start Network Trace</button>
                        </div>
                    </div>
                </div>
                <div class="col">
                    <div class="card" style="height:95%">
                        <div class="card-body" style="padding-top:20px;">
                            <img src="/spm/images/botnet.png" alt="Botnet" style="width:100%">
                        </div>
                    </div>
                </div>
            </div>

        </section>
 </main>

</body>
</html>
    
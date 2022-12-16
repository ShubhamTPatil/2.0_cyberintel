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



<body>


  <jsp:include page="header.jsp" />
  <jsp:include page="sidebar.jsp" />

  
 <main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename">Run Scan</span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="Run Scan"><i
              class="fa-solid fa-circle-info text-primary"></i></span>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Refresh"><i class="fa-solid fa-arrows-rotate"></i></div>
        <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Download">
          <i class="fa-solid fa-download"></i>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center">
        <a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home</a>
        </div>
      </div>

    </div>

    <section class="section dashboard">
    </section>
 </main>

</body>
</html>
    
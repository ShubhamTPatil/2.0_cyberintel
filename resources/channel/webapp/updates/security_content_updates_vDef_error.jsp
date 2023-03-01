<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8"%>
<%@ include file="/includes/startHeadSection.jsp"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm"%>

<!DOCTYPE html>
<html lang="en">

<head>
<title>DefenSight-Definitions Updates</title>

<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css" />

<script type="text/javascript" src="/shell/common-rsrc/js/master.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>

<script type="text/javascript">
	$(function() {
		$('#definitionsUpdate').addClass('nav-selected');
	});
</script>

</head>

<%@ include file="/includes/endHeadSection.jsp"%>
<%@ include file="/includes/info.jsp"%>

<body>

  <jsp:include page="../dashboard/header.jsp" />
  <jsp:include page="../dashboard/sidebar.jsp" />

  <html:form name="defenSightUpdatesForm" action="/defensightupdates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DefenSightUpdatesForm">
    <html:hidden property="action" />

    <main id="main" class="main">
      <div class="pagetitle">

        <div class="d-flex bd-highlight justify-content-center">
          <div class="p-2 flex-grow-1 bd-highlight">
            <span class="pagename">Definitions Update</span> <span data-bs-toggle="tooltip" data-bs-placement="right" title="Definitions Update"><i class="fa-solid fa-circle-info text-primary"></i></span>
          </div>
          <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh" style="cursor: pointer;">
            <i class="fa-solid fa-arrows-rotate"></i>
          </div>
          <div class="p-2 bd-highlight text-primary align-self-center">
            <a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home</a>
          </div>
        </div>

      </div>


      <section class="section dashboard">

        <div class="card">

          <nav>
            <div class="nav nav-tabs nav-title" id="nav-tab" role="tablist">
              <a href="/spm/definitionupdate.do" class="nav-link">CVE INFORMATION</a>
              <button type="button" class="nav-link active" id="updatesTabButton" data-bs-toggle="tab" data-bs-target="#nav-profile" role="tab" aria-controls="nav-profile" aria-selected="false">UPDATES</button>
            </div>
          </nav>

          <div class="card-body">
            <br />

            <table width="100%">
              <tr>
                <td>
                  <div class="pageHeader">
                    <span class="title"><bean:message key="page.security_content_updates_vDef_error.Title" /></span>
                  </div>
                </td>
              </tr>
            </table>
            <%@include file="/includes/help.jsp"%>
            <div class="box">
              <div class="box-body">
                <div class="row text-center">
                  <div class="col-md-12">
                    <div class="text-red text-bold">
                      <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">&nbsp;
                      <webapps:pageText key="info" />
                    </div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </section>
    </main>
  </html:form>
</body>
</html>
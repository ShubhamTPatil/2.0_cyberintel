<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm"%>

<!DOCTYPE html>
<html lang="en">

<head>
<title>Updates</title>

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


<%@ include file="/includes/endHeadSection.jsp"%>
<%@ include file="/includes/info.jsp"%>
<body>

  <jsp:include page="../dashboard/header.jsp" />
  <jsp:include page="../dashboard/sidebar.jsp" />

  <html:form name="vDeskUpdatesForm" action="/updates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.VDeskUpdatesForm">
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
                <td colspan=2><logic:equal name="scapUpdateAvailable" value="true">
                    <div align="right">
                      <logic:equal name="scapUpdateStatus" value="pending">
                        <b><font face="Arial" size="3" color="black"><span class="title" style="background-color: #FFFF00"><webapps:text key="dashboard.secinfo.update.available" />&nbsp;</span></font></b>
                      </logic:equal>
                      <logic:equal name="scapUpdateStatus" value="inprogress">
                        <span id="overallInProgressSpan" class="title"> <webapps:text key="dashboard.secinfo.update.inprogress" />&nbsp;<img src="/spm/images/rel_interstitial_loading.gif"> <logic:present name="scapUpdateInProgressStatus">
                            <br>
                            <bean:write name="scapUpdateInProgressStatus" />
                          </logic:present>
                        </span>
                      </logic:equal>
                      <logic:equal name="scapUpdateStatus" value="insync">
                        <logic:present name="scapUpdateTime">
                          <b><font face="Arial" size="3" color="green"><span class="title"><webapps:text key="dashboard.secinfo.update.insync" />&nbsp;<bean:write name="scapUpdateTime" /></span></font></b>
                        </logic:present>
                      </logic:equal>
                      <logic:equal name="scapUpdateStatus" value="retry">
                        <b><font face="Arial" size="3" color="red"><span class="title"><webapps:text key="dashboard.secinfo.update.failed" />&nbsp;</span></font></b>
                      </logic:equal>
                    </div>
                  </logic:equal></td>
              </tr>
            </table>
            <%@include file="/includes/help.jsp"%>
            <div class="table-responsive">
              <table id="security_updates_table" class="table table-bordered table-striped dataTable no-footer" style="margin-top: 1rem !important;">
                <thead>
                  <tr>
                    <th style="width: 56%;"><webapps:pageText key="securitycontent" /></th>
                    <th style="width: 12%;">Assessment Type</th>
                    <th style="width: 12%;"><webapps:pageText key="platform" /></th>
                    <th style="width: 20%;"><webapps:pageText key="updated" /></th>
                    <th style="width: 12%;"><webapps:pageText key="status" /></th>
                  </tr>
                </thead>
                <logic:iterate id="update" name="updates" type="com.marimba.apps.securitymgr.view.SecurityUpdateDetailsBean">
                  <tr>
                    <td valign="center" style="text-align: left;"><bean:write name="update" property="title" /></td>
                    <td nowrap valign="center"><bean:write name="update" property="assessmentType" /></td>
                    <td nowrap valign="center"><bean:write name="update" property="platform" /></td>
                    <td nowrap valign="center"><bean:write name="update" property="updated" /></td>
                    <logic:equal name="update" property="status" value="inprogress">
                      <td align="center" valign="center"><div style="display: none">1.inprogress</div> <img title='<webapps:pageText key="status.inprogress"/>' src="/spm/images/inprogress.gif" width="32" height="32"></td>
                    </logic:equal>
                    <logic:equal name="update" property="status" value="failedupdate">
                      <td align="center" valign="center"><div style="display: none">2.failedupdate</div> <img title='<webapps:pageText key="status.failedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/failedupdate.png" width="32" height="32"></td>
                    </logic:equal>
                    <logic:equal name="update" property="status" value="deletedupdate">
                      <td align="center" valign="center"><div style="display: none">3.deletedupdate</div> <img title='<webapps:pageText key="status.deletedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/deletedupdate.png" width="32" height="32"></td>
                    </logic:equal>
                    <logic:equal name="update" property="status" value="newupdate">
                      <td align="center" valign="center"><div style="display: none">4.newupdate</div> <img title='<webapps:pageText key="status.newupdate"/>' src="/spm/images/newupdate.png" width="32" height="32"></td>
                    </logic:equal>
                    <logic:equal name="update" property="status" value="existingupdate">
                      <td align="center" valign="center"><div style="display: none">5.existingupdate</div> <img title='<webapps:pageText key="status.existingupdate"/>' src="/spm/images/existingupdate.png" width="32" height="32"></td>
                    </logic:equal>
                    <logic:equal name="update" property="status" value="insync">
                      <td align="center" valign="center"><div style="display: none">6.insync</div> <img title='<webapps:pageText key="status.insync"/>' src="/spm/images/insync.png" width="32" height="32"></td>
                    </logic:equal>
                  </tr>
                </logic:iterate>
                <tbody>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>
    </main>


    <div class="modal fade" id="viewprofiledetailswindow" tabindex="-1" aria-labelledby="viewprofiledetailswindowLable" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-bs-dismiss="modal">&times;</button>
            <h4 class="modal-title text-left">Security Content Details</h4>
          </div>
          <table width="100%">
            <tr>
              <td align="right" valign="center"><b>Guide:&nbsp;</b></td>
              <td align="left" valign="center"><b><div id="viewprofiledetailswindowGuide"></div></b></td>
            </tr>
            <tr>
              <td align="right" valign="center"><b>Profiles:&nbsp;</b></td>
              <td align="left" valign="center"><div id="viewprofiledetailswindowProfiles"></div></td>
            </tr>
          </table>
          <div id="viewProfileInfoDiv" class="modal-body">
            <iframe id="iframeViewProfileInfo" src="/spm/scap_profile_details_template.html" width="100%" height="550px" frameBorder="0"></iframe>
          </div>
        </div>
        <div class="modal-footer"></div>
      </div>
    </div>
    <script type="text/javascript">
					var contentsDataTable;
					var rows_selected_contents = [];
					$(document).ready(function() {
						populateContentsDataTable('#security_updates_table');
					});
					function populateContentsDataTable(tableSelector) {
						contentsDataTable = $('#security_updates_table')
								.dataTable({
									"bPaginate" : false,
									"aoColumns" : [ {
										"bSortable" : true,
										"bSearchable" : true
									}, {
										"bSortable" : true,
										"bSearchable" : true
									}, {
										"bSortable" : true,
										"bSearchable" : true
									}, {
										"bSortable" : true,
										"bSearchable" : true
									}, {
										"bSortable" : true,
										"bSearchable" : true
									} ],
									"order" : [ [ 3, "asc" ] ]
								});
				<%
				String syncStatus = "";
				if (request.getAttribute("scapUpdateStatus") != null) {
				    syncStatus = (String)request.getAttribute("scapUpdateStatus");
				}
				if ("inprogress".equals(syncStatus)) {
				%>
					setInterval(function() {
							reload();
						}, 10000);
				<% } %>
					}
					function reload() {
						vDeskUpdatesForm.action = "/spm/updates.do?action=reload";
						vDeskUpdatesForm.submit();
					}
				</script>
  </html:form>
</body>
</html>
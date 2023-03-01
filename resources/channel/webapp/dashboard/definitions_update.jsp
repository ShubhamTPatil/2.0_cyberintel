<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>

<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>

<!DOCTYPE html>
<html lang="en">

<head>
<title>DefenSight-Definitions Updates</title>

<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css" />

<script language="javascript" src="/shell/common-rsrc/js/master.js"></script>
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

	function doSubmit(frm, action) {
		frm.action.value = action;
		frm.submit();
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

  <html:form name="definitionUpdateForm" action="/definitionupdate.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DefinitionUpdateForm" onsubmit="return false;">

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
              <button class="nav-link active" id="nav-home-tab" data-bs-toggle="tab" data-bs-target="#nav-home" type="button" role="tab" aria-controls="nav-home" aria-selected="true">CVE INFORMATION</button>
              <a href="/spm/updates.do" class="nav-link">UPDATES</a>
              <!-- <button type="button" class="nav-link" id="updatesTabButton" data-bs-toggle="tab" data-bs-target="#nav-profile" role="tab" aria-controls="nav-profile" aria-selected="false">UPDATES</button> -->
              
            </div>
          </nav>

          <div class="card-body">
            <div class="tab-content" id="nav-tabContent">
              <div class="tab-pane fade show active" id="nav-home" role="tabpanel" aria-labelledby="nav-home-tab">
                <br />

                <div class="p-2 mb-2 text-dark" style="font-size: medium; background-color: #d9edf7;">
                  <i class="fa-solid fa-circle-info text-primary"></i> CVE Definitions and Vulnerability Definitions are need to be updated every month.
                </div>

                <div class="card">
                  <div class="card-body">
                    <div class="row" style="box-shadow: 1px 3px 3px #3333333d !important; padding-bottom: 20px;">
                      <table border="0" cellspacing="1" cellpadding="5">
                        <tr>
                          <td align="right" valign="top"><webapps:pageText key="cvejson.storagedir" /></td>
                          <td valign="top"><webapps:errorsPresent property="cveStorageDir">
                              <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                            </webapps:errorsPresent> <html:text name="definitionUpdateForm" property="cveStorageDir" size="30" styleClass="requiredField" /></td>
                        </tr>
                      </table>
                    </div>
                  </div>
                </div>

                <br />
                <div class="row" style="box-shadow: 1px 3px 3px #3333333d !important; padding-bottom: 20px;">

                  <div class="col">
                    <span style="font-weight: bold;">CVE Definitions last updated on <bean:write name="definitionUpdateForm" property="cveJsonLastUpdated" filter="false" />
                    </span><br /> <span>(Please ensure all information is upto date for accurate results)</span>
                  </div>

                  <div class="col">
                    <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                      <button type="button" class="btn btn-sm btn-secondary" style="background-color: #d3d3d333; color: darkgray;">CANCEL</button>
                      <input type="button" id="cveUpdateNow" onclick="doSubmit(this.form, 'update_cvejson')" class="btn btn-sm btn-primary" value="UPDATE NOW">
                    </div>
                  </div>
                </div>
                <br />

                <div class="card">
                  <div class="card-body">
                    <div class="card-title">
                      <webapps:pageText key="vdefupdateinfo.header" />
                    </div>
                    <webapps:pageText key="vdefSecInfo" />
                    <br /> <br />
                    <div class="row" style="box-shadow: 1px 3px 3px #3333333d !important; padding-bottom: 20px;">
                      <table border="0" cellspacing="1" cellpadding="5">
                        <tr>
                          <td align="right" valign="top"><webapps:pageText key="mastertxurl" /></td>
                          <td valign="top"><webapps:errorsPresent property="publishTxUrl">
                              <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                            </webapps:errorsPresent> <html:text name="definitionUpdateForm" property="publishTxUrl" size="30" maxlength="110" styleClass="requiredField" /></td>
                        </tr>
                        <tr>
                          <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="PublishUserName" /></span></td>
                          <td valign="top"><webapps:errorsPresent property="publishUserName">
                              <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                            </webapps:errorsPresent> <html:text name="definitionUpdateForm" property="publishUserName" size="25" styleClass="optionalField" /></td>
                        </tr>
                        <tr>
                          <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="PublishPassword" /></span></td>
                          <td valign="top"><webapps:errorsPresent property="publishPassword">
                              <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                            </webapps:errorsPresent> <html:password name="definitionUpdateForm" property="publishPassword" size="25" styleId="ppasswd" styleClass="optionalField" /></td>
                        </tr>
                        <tr>
                          <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="ChannelStoreUserName" /></span></td>
                          <td valign="top"><webapps:errorsPresent property="channelStoreUserName">
                              <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                            </webapps:errorsPresent> <html:text name="definitionUpdateForm" property="channelStoreUserName" size="25" styleClass="optionalField" /></td>
                        </tr>
                        <tr>
                          <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="ChannelStorePassword" /></span></td>
                          <td valign="top"><webapps:errorsPresent property="channelStorePassword">
                              <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                            </webapps:errorsPresent> <html:password name="definitionUpdateForm" property="channelStorePassword" size="25" styleId="spasswd" styleClass="optionalField" /></td>
                        </tr>
                      </table>
                    </div>
                  </div>
                </div>

                <div class="row" style="box-shadow: 1px 3px 3px #3333333d !important; padding-bottom: 20px;">
                  <div class="col">
                    <span style="font-weight: bold;">Vulnerability Definitions last updated on <bean:write name="definitionUpdateForm" property="vdefLastUpdated" filter="false" />
                    </span> <br /> <span>(Please ensure all information is upto date for accurate results)</span>
                  </div>
                  <div class="col">
                    <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                      <button type="button" class="btn btn-sm btn-secondary" style="background-color: #d3d3d333; color: darkgray;">CANCEL</button>
                      <input type="button" id="vdefpublish" onclick="doSubmit(this.form, 'update_vdef')" class="btn btn-sm btn-primary" value="UPDATE NOW">
                    </div>
                  </div>
                </div>

              </div>

              <!-- <div class="tab-pane fade" id="nav-profile" role="tabpanel" aria-labelledby="nav-profile-tab">
              
                
              </div> -->


            </div>
          </div>
        </div>
      </section>


      <!-- VDef Update Message Info Modal -->
      <div class="modal fade" id="updateSuccessModal" tabindex="-1" aria-labelledby="updateSuccessModalLable" aria-hidden="true">
        <div class="modal-dialog" style="max-width: 800px;">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="updateSuccessMessage"></h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal">Ok</button>
            </div>
          </div>
        </div>
      </div>


      <div class="modal fade" id="reports_status_modal" tabindex="-1" aria-labelledby="reports_status_modal_label" aria-hidden="true">
        <div class="modal-dialog modal-lg" style="width: 95%;">
          <div class="modal-content">
            <div class="modal-header">
              <h4 class="modal-title">
                <b><i class="fa fa-filter" aria-hidden="true" style="font-size: 17px; color: #0073b7"></i>&nbsp;File Transfer Status</b>
              </h4>
              <div align="right">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
              </div>
            </div>
            <div class="modal-body" style="padding-top: 1px; padding-bottom: 5px;">
              <div id="reportstatus" style="height: auto;">
                <div id="report_status_div" style="text-align: left;">
                  <table id="report_status_table" border="1" style="width: 100%">
                    <colgroup width="5%" />
                    <colgroup width="20%" />
                    <colgroup width="15%" />
                    <colgroup width="*" />
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

    </main>

  </html:form>

</body>

</html>
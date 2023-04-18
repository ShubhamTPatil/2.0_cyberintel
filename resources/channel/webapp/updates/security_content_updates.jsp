<%--// Copyright 2022-2023, Harman International. All Rights Reserved.// Confidential and Proprietary Information of Harman International.<!-- Author: Nandakumar Sankaralingam -->--%><%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8"%><%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%><%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%><%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%><%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%><!DOCTYPE html><html lang="en"><head><title>Updates</title><link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css" /><link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css" /><link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css" /><link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css" /><link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css" /><script type="text/javascript" src="/shell/common-rsrc/js/master.js"></script><script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script><script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script><script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script><script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script><script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script><script type="text/javascript" src="/spm/js/newdashboard/common.js"></script><script type="text/javascript">  $(function() {    $('#definitionsUpdate').addClass('nav-selected');  });</script></head><body>  <jsp:include page="../dashboard/header.jsp" />  <jsp:include page="../dashboard/sidebar.jsp" />  <html:form name ="vDeskUpdatesForm" action="/updates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.VDeskUpdatesForm">    <html:hidden property="action" />    <main id="main" class="main">      <div class="pagetitle">        <div class="d-flex bd-highlight justify-content-center">          <div class="p-2 flex-grow-1 bd-highlight">            <span class="pagename">Definitions Update</span> <span data-bs-toggle="tooltip" data-bs-placement="right" title="Definitions Update"><i class="fa-solid fa-circle-info text-primary"></i></span>          </div>          <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh" style="cursor: pointer;">            <i class="fa-solid fa-arrows-rotate"></i>          </div>          <div class="p-2 bd-highlight text-primary align-self-center">            <a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home</a>          </div>        </div>      </div>      <section class="section dashboard">          <nav style="background-color: #fff;">            <div class="nav nav-tabs nav-title" id="nav-tab" role="tablist">              <a href="/spm/definitionupdate.do" class="nav-link">CVE and Vulnerabilities Definitions Updates</a>              <button type="button" class="nav-link active" id="updatesTabButton" data-bs-toggle="tab" data-bs-target="#nav-profile" role="tab" aria-controls="nav-profile" aria-selected="false" style="background-color: #fff; z-index:1;">Repository Updates</button>            </div>          </nav>        <div class="card">          <div class="card-body">            <br />            <table width="100%">              <tr>                <td colspan=2>                  <logic:equal name="scapUpdateAvailable" value="true">                    <div align="right">                      <logic:equal name="scapUpdateStatus" value="pending">                        <b><font face="Arial" size="3" color="black"><span class="title" style="background-color: #FFFF00">&nbsp;<webapps:text key="dashboard.secinfo.update.available" />&nbsp;</span></font></b>                      </logic:equal>                      <logic:equal name="scapUpdateStatus" value="inprogress">                        <span id="overallInProgressSpan" class="title">&nbsp;<webapps:text key="dashboard.secinfo.update.inprogress" />&nbsp;<img src="/spm/images/rel_interstitial_loading.gif"> <logic:present name="scapUpdateInProgressStatus">                            <br>                            <bean:write name="scapUpdateInProgressStatus" />                          </logic:present>                        </span>                      </logic:equal>                      <logic:equal name="scapUpdateStatus" value="insync">                        <logic:present name="scapUpdateTime">                          <b><font face="Arial" size="3" color="green"><span class="title">&nbsp;<webapps:text key="dashboard.secinfo.update.insync" />&nbsp;<bean:write name="scapUpdateTime" /></span></font></b>                        </logic:present>                      </logic:equal>                      <logic:equal name="scapUpdateStatus" value="retry">                        <b><font face="Arial" size="3" color="red"><span class="title">&nbsp;<webapps:text key="dashboard.secinfo.update.failed" />&nbsp;</span></font></b>                      </logic:equal>                    </div>                  </logic:equal></td>              </tr>            </table>            <%@include file="/includes/help.jsp"%>            <div class="table-responsive">              <table id="security_updates_table" class="table table-bordered table-striped dataTable no-footer" style="margin-top: 1rem!important;">                <thead>                  <tr>                    <th style="width: 4%; vertical-align: middle; text-align: center"><input type="checkbox" id="chk_content_all" onclick="selectAllRow()"></th>                    <th style="width: 41%; vertical-align: middle;"><webapps:pageText key="securitycontent" /></th>                    <th style="width: 12%; vertical-align: middle;">Assessment Type</th>                    <th style="width: 12%; vertical-align: middle;"><webapps:pageText key="platform" /></th>                    <th style="width: 19%; vertical-align: middle;"><webapps:pageText key="updated" /></th>                    <th style="width: 7%; vertical-align: middle;"><webapps:pageText key="status" /></th>                    <th style="width: 17%; vertical-align: middle;"><webapps:pageText key="actions" /></th>                  </tr>                </thead>                <logic:iterate id="update" name="updates" type="com.marimba.apps.securitymgr.view.SecurityUpdateDetailsBean">                  <tr>                    <td align="center" valign="center"><input type="checkbox" id="chk_content_<bean:write name="update" property="fileName"/>" onclick="selectRow()"></td>                    <td valign="center" style="text-align: left;"><bean:write name="update" property="title"/></td>                    <td nowrap valign="center"><bean:write name="update" property="assessmentType" /></td>                    <td nowrap valign="center"><bean:write name="update" property="platform" /></td>                    <td nowrap valign="center"><bean:write name="update" property="updated" /></td>                    <logic:equal name="update" property="status" value="inprogress">                      <td align="center" valign="center"><div style="display: none">1.inprogress</div> <img title='<webapps:pageText key="status.inprogress"/>' src="/spm/images/inprogress.gif" width="32" height="32"></td>                      <td align="center" valign="center"></td>                    </logic:equal>                    <logic:equal name="update" property="status" value="failedupdate">                      <td align="center" valign="center"><div style="display: none">2.failedupdate</div> <img title='<webapps:pageText key="status.failedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/failedupdate.png" width="32" height="32"></td>                      <td nowrap valign="center">                        <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" class="btn btn-sm btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)">                          <i class='fa fa-refresh'></i>&nbsp;                          <webapps:pageText key="resync" />                        </button> <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->                      </td>                    </logic:equal>                    <logic:equal name="update" property="status" value="deletedupdate">                      <td align="center" valign="center"><div style="display: none">3.deletedupdate</div> <img title='<webapps:pageText key="status.deletedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/deletedupdate.png" width="32" height="32"></td>                      <td nowrap valign="center">                        <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" class="btn btn-sm btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)">                          <i class='fa fa-trash'></i>&nbsp;                          <webapps:pageText key="remove" />                        </button> <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->                      </td>                    </logic:equal>                    <logic:equal name="update" property="status" value="newupdate">                      <td align="center" valign="center"><div style="display: none">4.newupdate</div> <img title='<webapps:pageText key="status.newupdate"/>' src="/spm/images/newupdate.png" width="32" height="32"></td>                      <td nowrap valign="center">                        <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" class="btn btn-sm btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)">                          <i class='fa fa-refresh'></i>&nbsp;                          <webapps:pageText key="sync" />                        </button> <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->                      </td>                    </logic:equal>                    <logic:equal name="update" property="status" value="existingupdate">                      <td align="center" valign="center"><div style="display: none">5.existingupdate</div> <img title='<webapps:pageText key="status.existingupdate"/>' src="/spm/images/existingupdate.png" width="32" height="32"></td>                      <td nowrap valign="center">                        <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" class="btn btn-sm btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)">                          <i class='fa fa-refresh'></i>&nbsp;                          <webapps:pageText key="sync" />                        </button> <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->                      </td>                    </logic:equal>                    <logic:equal name="update" property="status" value="insync">                      <td align="center" valign="center"><div style="display: none">6.insync</div> <img title='<webapps:pageText key="status.insync"/>' src="/spm/images/insync.png" width="32" height="32"></td>                      <td nowrap valign="center">                        <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" class="btn btn-sm btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)">                          <i class='fa fa-refresh'></i>&nbsp;                          <webapps:pageText key="resync" />                        </button>                        <button type="button" id="detailsBtn_chk_content_<bean:write name="update" property="fileName"/>" class="btn btn-sm btn-info" onclick="showProfileDetails(&apos;<bean:write name='update' property='fileName'/>&apos;, &apos;<bean:write name='update' property='title'/>&apos;, &apos;<bean:write name='update' property='profileIds'/>&apos;, &apos;<bean:write name='update' property='profileTitles'/>&apos;, &apos;<bean:write name='update' property='target'/>&apos;)">                          <i class='fa fa-search'></i>&nbsp;                          <webapps:pageText key="view" />                        </button> <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-download'></i>&nbsp;<webapps:pageText key="download"/></button> -->                      </td>                    </logic:equal>                  </tr>                </logic:iterate>                <tbody>                </tbody>              </table>            </div>          </div>        </div>      </section>    </main>    <div class="modal fade" id="viewprofiledetailswindow" tabindex="-1" aria-labelledby="viewprofiledetailswindowLable" aria-hidden="true">      <div class="modal-dialog modal-lg">        <div class="modal-content">          <div class="modal-header">            <button type="button" class="close" data-bs-dismiss="modal">&times;</button>            <h4 class="modal-title text-left">Security Content Details</h4>          </div>          <table width="100%">            <tr>              <td align="right" valign="center"><b>Guide:&nbsp;</b></td>              <td align="left" valign="center"><b><div id="viewprofiledetailswindowGuide"></div></b></td>            </tr>            <tr>              <td align="right" valign="center"><b>Profiles:&nbsp;</b></td>              <td align="left" valign="center"><div id="viewprofiledetailswindowProfiles"></div></td>            </tr>          </table>          <div id="viewProfileInfoDiv" class="modal-body">            <iframe id="iframeViewProfileInfo" src="/spm/scap_profile_details_template.html" width="100%" height="550px" frameBorder="0"></iframe>          </div>        </div>        <div class="modal-footer"></div>      </div>    </div>    <style>.toolbar {	float: left;}</style>    <script type="text/javascript">            var contentsDataTable;            var rows_selected_contents = [];            $(document).ready(function() {                populateContentsDataTable('#security_updates_table');                var divText = '<div class="btn-group">' +                               '<button type="button" id="syncBtn" disabled class="btn btn-info" onclick="doDiffSyncSelectedXml()">' +                               '<i class="fa fa-refresh"></i>&nbsp;<webapps:pageText key="sync"/>&nbsp;</button>' +                               '<button type="button" id="syncBtnToggle" disabled class="btn btn-info dropdown-toggle dropdown-toggle-split" data-bs-toggle="dropdown" aria-expanded="false">' +                               '<span class="visually-hidden">Toggle Dropdown</span>' +                               '</button>' +                               '<div class="dropdown-menu">' +                               '<a href="#" class="dropdown-item" id="diffSync" onclick="doDiffSyncSelectedXml();return true;"><i class="fa fa-refresh"></i>&nbsp;<webapps:pageText key="sync"/></a>' +                               '<a href="#" class="dropdown-item" id="forceSync" onclick="doForceSyncSelectedXml();return false;"><i class="fa fa-refresh"></i>&nbsp;<webapps:pageText key="forcesync"/></a>' +                               '</div>' +                            '</div>';                $("div.toolbar").html(divText);            });            function populateContentsDataTable(tableSelector) {                contentsDataTable = $('#security_updates_table').dataTable(                    {                        "dom": '<"toolbar">frtip',                        "bPaginate": false,                        "aoColumns": [                            { "bSortable": false, "bSearchable": false },							{ "bSortable": true, "bSearchable": true },                            { "bSortable": true, "bSearchable": true },                            { "bSortable": true, "bSearchable": true },                            { "bSortable": true, "bSearchable": true },                            { "bSortable": true, "bSearchable": true },                            { "bSortable": false, "bSearchable": false }                        ],                        "order": [[ 4, "asc" ]]                    }                );                <%				String syncStatus = "";				if (request.getAttribute("scapUpdateStatus") != null) {				    syncStatus = (String)request.getAttribute("scapUpdateStatus");				}				if ("inprogress".equals(syncStatus)) {				%>                    setInterval(function() {                        reload();                    }, 10000);                <% } else {%>                    $('#syncBtn').prop("disabled", true);                    $('#syncBtnToggle').prop("disabled", true);				<% } %>            }            function getSelectedContentsData() {                var keyValues = new Array();                var table = document.getElementById('security_updates_table');                var rowCount = table.rows.length;                var i = 1;                for (var r = 1; r < rowCount; r++) {                    var isSelected = table.rows[r].cells[0].childNodes[0].checked;                    if (isSelected) {                        keyValues[i] = table.rows[r].cells[0].childNodes[0].id.split('chk_content_')[1];                        i++;                    }                }                return keyValues.join(":_:");            }            function selectAllRow() {                try {                    var table = document.getElementById('security_updates_table');                    var rowCount = table.rows.length;                    var row = table.rows[0];                    var selectAllChkbox = row.cells[0].childNodes[0];                    if (selectAllChkbox.checked == true) {                        $('#syncBtn').prop("disabled", false);                        $('#syncBtnToggle').prop("disabled", false);                        for(var i=1; i < rowCount; i++) {                            row = table.rows[i];                            var chkbox = row.cells[0].childNodes[0];                            if (null != chkbox) {                                chkbox.checked = true;                            }                        }                        if (rowCount == 2) {                            toggleActionsForAllRow("true");                        } else {                            toggleActionsForAllRow("false");                        }                    } else {                        $('#syncBtn').prop("disabled", true);                        $('#syncBtnToggle').prop("disabled", true);                        for(var i=1; i < rowCount; i++) {                            row = table.rows[i];                            var chkbox = row.cells[0].childNodes[0];                            if (null != chkbox) {                                chkbox.checked = false;                            }                        }                        toggleActionsForAllRow("true");                    }                } catch(e) {                    alert(e);                }            }            function selectRow() {                var table = document.getElementById('security_updates_table');                var row = table.rows[0];                var selectAllChkbox = row.cells[0].childNodes[0];                var totalSelected=0;                var rowCount = table.rows.length;                for(var i=1; i < rowCount; i++) {                    row = table.rows[i];                    var chkbox = row.cells[0].childNodes[0];                    if (null != chkbox && chkbox.checked == true) {                        totalSelected++;                    }                }                if (totalSelected == (rowCount - 1)) {                    selectAllChkbox.checked = true;                } else {                    selectAllChkbox.checked = false;                }                if (totalSelected > 0) {                    toggleActionsForAllRow("false");                    if (totalSelected == 1) {                        for(var j=1; j < rowCount; j++) {                            var tableRow = table.rows[j];                            var tableRowChkbox = tableRow.cells[0].childNodes[0];                            if (null != tableRowChkbox && tableRowChkbox.checked == true) {                                var tableRowSyncButton = document.getElementById('syncBtn_' + tableRowChkbox.id);                                var tableRowDetailsButton = document.getElementById('detailsBtn_' + tableRowChkbox.id);                                if (null != tableRowSyncButton) {                                    tableRowSyncButton.disabled = false;                                }                                if (null != tableRowDetailsButton) {                                    tableRowDetailsButton.disabled = false;                                }                            }                        }                    }                    $('#syncBtn').prop("disabled", false);                    $('#syncBtnToggle').prop("disabled", false);                } else {                    toggleActionsForAllRow("true");                    $('#syncBtn').prop("disabled", true);                    $('#syncBtnToggle').prop("disabled", true);                }            }            function toggleActionsForAllRow(enable) {                var table = document.getElementById('security_updates_table');                var row = table.rows[0];                var rowCount = table.rows.length;                for(var j=1; j < rowCount; j++) {                    row = table.rows[j];                    var chkbox = row.cells[0].childNodes[0];                    var syncButton = document.getElementById('syncBtn_' + chkbox.id);                    if (null != syncButton) {                        if ("true" == enable) {                            syncButton.disabled = false;                        } else {                            syncButton.disabled = true;                        }                    }                    var detailsButton = document.getElementById('detailsBtn_' + chkbox.id);                    if (null != detailsButton) {                        if ("true" == enable) {                            detailsButton.disabled = false;                        } else {                            detailsButton.disabled = true;                        }                    }                }            }            function doDiffSyncSelectedXml() {                var isInProgressSync = document.getElementById('overallInProgressSpan');                if (isInProgressSync) {                    alert('Please wait while the active Sync operation is completed.');                    return;                }                var submitaction = '';                if (document.getElementById("chk_content_all").checked) {                    submitaction = '/updates.do?action=dosync&contents=all&sync=diff';                } else {                    submitaction = '/updates.do?action=dosync&contents=' + getSelectedContentsData() + '&sync=diff';                }                vDeskUpdatesForm.action = "/spm" + submitaction + "";                vDeskUpdatesForm.submit();            }            function doForceSyncSelectedXml() {                var isInProgressSync = document.getElementById('overallInProgressSpan');                if (isInProgressSync) {                    alert('Please wait while the active Sync operation is completed.');                    return;                }                var submitaction = '';                if (document.getElementById("chk_content_all").checked) {                    submitaction = '/updates.do?action=dosync&contents=all&sync=force';                } else {                    submitaction = '/updates.do?action=dosync&contents=' + getSelectedContentsData() + '&sync=force';                }                vDeskUpdatesForm.action = "/spm" + submitaction + "";                vDeskUpdatesForm.submit();            }            function doSyncXml(xml) {            		vDeskUpdatesForm.action = "/spm/updates.do?action=dosync&contents=" + xml + ":_:&sync=force";                vDeskUpdatesForm.submit();            }            function reload() {            		vDeskUpdatesForm.action = "/spm/updates.do?action=reload";            		vDeskUpdatesForm.submit();            }            function showProfileDetails(fileName, contentTitle, profileIds, profileTitles, targetType) {                var viewprofiledetailswindowGuideDiv = document.getElementById('viewprofiledetailswindowGuide');                while(viewprofiledetailswindowGuideDiv.firstChild){                    viewprofiledetailswindowGuideDiv.removeChild(viewprofiledetailswindowGuideDiv.firstChild);                }                viewprofiledetailswindowGuideDiv.innerHTML = contentTitle;                var viewprofiledetailswindowProfilesDiv = document.getElementById('viewprofiledetailswindowProfiles');                while(viewprofiledetailswindowProfilesDiv.firstChild){                    viewprofiledetailswindowProfilesDiv.removeChild(viewprofiledetailswindowProfilesDiv.firstChild);                }                var text = '<select id="selectedSCAPProfile" onchange="switchProfile(&apos;' + fileName + '&apos;, this.value,&apos;' + targetType + '&apos;)">';                var profileIds_array = profileIds.split(';');                var profileTitles_array = profileTitles.split(';');                for (var i=0; i < profileIds_array.length - 1; i++) {                    text += '<option value="' + profileIds_array[i] + '">' + profileTitles_array[i] + '</option>';                }                viewprofiledetailswindowProfilesDiv.innerHTML = text;                var srcUrl = '/spm/securitymgmt?command=gethtml&target='+ targetType + '&content=' + fileName + '&profile=' + profileIds_array[0];                $("#iframeViewProfileInfo").contents().find("body").html('<div class="loader"></div>');                document.getElementById('iframeViewProfileInfo').src = srcUrl;                $('#viewprofiledetailswindow').modal('show');            }            function switchProfile(fileName, profileId, targetType) {                var srcUrl = '/spm/securitymgmt?command=gethtml&target='+ targetType + '&content=' + fileName + '&profile=' + profileId;                $("#iframeViewProfileInfo").contents().find("body").html('<div class="loader"></div>');                document.getElementById('iframeViewProfileInfo').src = srcUrl;            }        </script>  </html:form></body></html>
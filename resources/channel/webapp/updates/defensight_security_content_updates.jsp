<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>


<script type="text/javascript" src="/spm/js/application.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/icon.css">
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-datepicker3.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
<link rel="stylesheet" type="text/css" href="/spm/css/_all-skins.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css">
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/fuelux.css">

<script type="text/javascript" src="/spm/includes/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="/spm/js/chart.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-datepicker.js"></script>
<script type="text/javascript" src="/spm/js/wizard.js"></script>

<style type="text/css">
    .slider-arrow {

        background: #d9dada none repeat scroll 0 0; float: left;

        font-size: 25px; position: fixed; margin-left: -2px;

    }
</style>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/info.jsp" %>


 <body>
    <html:form name ="defenSightUpdatesForm" action="/defensightupdates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DefenSightUpdatesForm">
        <html:hidden property="action"/>
        <div style="padding-left:25px; padding-right:25px;">
            <table width="100%"><tr>
                <td>
                    <div class="pageHeader">
                        <span class="title"><bean:message key="page.defensight_security_content_updates.Title"/></span>
                    </div>
                </td>
                <td>
                    <logic:equal name="scapUpdateAvailable" value="true">
                        <div align="right" style="padding-right: 20px;">
                            <logic:equal name="scapUpdateStatus" value="pending">
                                <b><font face="Arial" size="3" color="black"><span class="title" style="background-color: #FFFF00"><webapps:text key="dashboard.secinfo.update.available"/>&nbsp;</span></font></b>
                            </logic:equal>
                            <logic:equal name="scapUpdateStatus" value="inprogress">
                                <span id="overallInProgressSpan" class="title">
                                    <webapps:text key="dashboard.secinfo.update.inprogress"/>&nbsp;<img src="/spm/images/rel_interstitial_loading.gif">
                                    <logic:present name="scapUpdateInProgressStatus">
                                        <br><bean:write name="scapUpdateInProgressStatus"/>
                                    </logic:present>
                                </span>
                            </logic:equal>
                            <logic:equal name="scapUpdateStatus" value="insync">
                                <logic:present name="scapUpdateTime">
                                    <b><font face="Arial" size="3" color="green"><span class="title"><webapps:text key="dashboard.secinfo.update.insync"/>&nbsp;<bean:write name="scapUpdateTime"/></span></font></b>
                                </logic:present>
                            </logic:equal>
                            <logic:equal name="scapUpdateStatus" value="retry">
                                <b><font face="Arial" size="3" color="red"><span class="title"><webapps:text key="dashboard.secinfo.update.failed"/>&nbsp;</span></font></b>
                            </logic:equal>
                        </div>
                    </logic:equal>
                </td>
            </tr></table>
            <%@include file="/includes/help.jsp" %>
            <div class="box">
                <div class="box-body">
                    <table id="security_updates_table" class="table table-bordered table-striped dataTable no-footer" style="width: 97%;margin-left:55px;">
                        <thead>
                            <tr>
                                <th style="width: 4%;vertical-align: middle;text-align: center"><input type="checkbox" id="chk_content_all" onclick="selectAllRow()"></th>
                                <th style="width: 41%;vertical-align: middle;"><webapps:pageText key="securitycontent"/></th>
                                <th style="width: 12%;vertical-align: middle;">Assessment Type</th>
								<th style="width: 12%;vertical-align: middle;"><webapps:pageText key="platform"/></th>
                                <th style="width: 19%;vertical-align: middle;"><webapps:pageText key="updated"/></th>
                                <th style="width: 7%;vertical-align: middle;"><webapps:pageText key="status"/></th>
                                <th style="width: 17%;vertical-align: middle;"><webapps:pageText key="actions"/></th>
                            </tr>
                        </thead>
                            <logic:iterate id="update" name="updates" type="com.marimba.apps.securitymgr.view.SecurityUpdateDetailsBean">
                                <tr>
                                    <td align="center" valign="center"><input type="checkbox" id="chk_content_<bean:write name="update" property="fileName"/>" onclick="selectRow()"></td>
                                    <td valign="center"><bean:write name="update" property="title"/></td>
									<td nowrap valign="center"><bean:write name="update" property="assessmentType"/></td>
                                    <td nowrap valign="center"><bean:write name="update" property="platform"/></td>
                                    <td nowrap valign="center"><bean:write name="update" property="updated"/></td>
                                    <logic:equal name="update" property="status" value="inprogress">
                                        <td align="center" valign="center"><div style="display:none">1.inprogress</div><img title='<webapps:pageText key="status.inprogress"/>' src="/spm/images/inprogress.gif" width="32" height="32"></td>
                                        <td align="center" valign="center">
                                        </td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="failedupdate">
                                        <td align="center" valign="center"><div style="display:none">2.failedupdate</div><img title='<webapps:pageText key="status.failedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/failedupdate.png" width="32" height="32"></td>
                                        <td nowrap valign="center">
                                            <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" style="width:99px;" class="btn btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)"><i class='fa fa-refresh'></i>&nbsp;<webapps:pageText key="resync"/></button>
                                            <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->
                                        </td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="deletedupdate">
                                        <td align="center" valign="center"><div style="display:none">3.deletedupdate</div><img title='<webapps:pageText key="status.deletedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/deletedupdate.png" width="32" height="32"></td>
                                        <td nowrap valign="center">
                                            <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" style="width:99px;" class="btn btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)"><i class='fa fa-trash'></i>&nbsp;<webapps:pageText key="remove"/></button>
                                            <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->
                                        </td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="newupdate">
                                        <td align="center" valign="center"><div style="display:none">4.newupdate</div><img title='<webapps:pageText key="status.newupdate"/>' src="/spm/images/newupdate.png" width="32" height="32"></td>
                                        <td nowrap valign="center">
                                            <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" style="width:99px;" class="btn btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)"><i class='fa fa-refresh'></i>&nbsp;<webapps:pageText key="sync"/></button>
                                            <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->
                                        </td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="existingupdate">
                                        <td align="center" valign="center"><div style="display:none">5.existingupdate</div><img title='<webapps:pageText key="status.existingupdate"/>' src="/spm/images/existingupdate.png" width="32" height="32"></td>
                                        <td nowrap valign="center">
                                            <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" style="width:99px;" class="btn btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)"><i class='fa fa-refresh'></i>&nbsp;<webapps:pageText key="sync"/></button>
                                            <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-eye'></i>&nbsp;<webapps:pageText key="diff"/></button> -->
                                        </td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="insync">
                                        <td align="center" valign="center"><div style="display:none">6.insync</div><img title='<webapps:pageText key="status.insync"/>' src="/spm/images/insync.png" width="32" height="32"></td>
                                        <td nowrap valign="center">
                                            <button type="button" id="syncBtn_chk_content_<bean:write name="update" property="fileName"/>" style="width:99px;" class="btn btn-info" onclick="doSyncXml(&apos;<bean:write name='update' property='fileName'/>&apos;)"><i class='fa fa-refresh'></i>&nbsp;<webapps:pageText key="resync"/></button>
                                            <button type="button" id="detailsBtn_chk_content_<bean:write name="update" property="fileName"/>" style="width:99px;" class="btn btn-info" onclick="showProfileDetails(&apos;<bean:write name='update' property='fileName'/>&apos;, &apos;<bean:write name='update' property='title'/>&apos;, &apos;<bean:write name='update' property='profileIds'/>&apos;, &apos;<bean:write name='update' property='profileTitles'/>&apos;, &apos;<bean:write name='update' property='target'/>&apos;)"><i class='fa fa-search'></i>&nbsp;<webapps:pageText key="view"/></button>
                                            <!-- <button type="button" style="width:99px;" class="btn btn-info"><i class='fa fa-download'></i>&nbsp;<webapps:pageText key="download"/></button> -->
                                        </td>
                                    </logic:equal>
                                </tr>
                            </logic:iterate>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div id="viewprofiledetailswindow" class="modal fade" role="dialog" data-backdrop="false" style="background-color: rgba(0, 0, 0, 0.5);">
            <div class="modal-dialog" style=width:80%;height:100%;>
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title text-left">Security Content Details</h4>
                    </div>
                    <table width="100%">
                        <tr>
                            <td width="30%" align="right" valign="center"><b>Guide:&nbsp;</b></td>
                            <td width="70%" align="left" valign="center"><b><div id="viewprofiledetailswindowGuide"></div></b></td>
                        </tr>
                        <tr>
                            <td width="30%" align="right" valign="center"><b>Profiles:&nbsp;</b></td>
                            <td width="70%" align="left" valign="center"><div id="viewprofiledetailswindowProfiles"></div></td>
                        </tr>
                    </table>
                    <div id="viewProfileInfoDiv" class="modal-body">
                        <iframe id="iframeViewProfileInfo" src="/spm/scap_profile_details_template.html" width="100%" height="550px" frameBorder="0"></iframe>
                    </div>
                </div>
                <div class="modal-footer">
                </div>
            </div>
        </div>
        <style>
        .toolbar {
            float: left;
        }
        </style>
        <script type="text/javascript">
            var contentsDataTable;
            var rows_selected_contents = [];
            $(document).ready(function() {
                populateContentsDataTable('#security_updates_table');
                var divText = '<div class="btn-group" style="margin-left:54px;">' +
                               '<button type="button" id="syncBtn" disabled class="btn btn-info" style="height:35px;" onclick="doDiffSyncSelectedXml()">' +
                               '<i class="fa fa-refresh"></i>&nbsp;<webapps:pageText key="sync"/>&nbsp;</button>' +
                               '<button type="button" id="syncBtnToggle" disabled class="btn btn-info dropdown-toggle" data-toggle="dropdown" style="height:35px;">' +
                               '<span class="caret"></span><span class="sr-only">Toggle Dropdown</span>' +
                               '</button>' +
                               '<ul class="dropdown-menu" role="menu">' +
                               '<li><a href="#" id="diffSync" onclick="doDiffSyncSelectedXml();return true;"><i class="fa fa-refresh"></i>&nbsp;<webapps:pageText key="sync"/></a></li>' +
                               '<li><a href="#" id="forceSync" onclick="doForceSyncSelectedXml();return false;"><i class="fa fa-refresh"></i>&nbsp;<webapps:pageText key="forcesync"/></a></li>' +
                               '</ul>' +
                            '</div>';
                $("div.toolbar").html(divText);
            });
            function populateContentsDataTable(tableSelector) {
                contentsDataTable = $('#security_updates_table').dataTable(
                    {
                        "dom": '<"toolbar">frtip',
                        "bPaginate": false,
                        "aoColumns": [
                            { "bSortable": false, "bSearchable": false },
							{ "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": false, "bSearchable": false }
                        ],
                        "order": [[ 4, "asc" ]]
                    }
                );
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
                <% } else {%>
                    $('#syncBtn').prop("disabled", true);
                    $('#syncBtnToggle').prop("disabled", true);
				<% } %>
            }
            function getSelectedContentsData() {
                var keyValues = new Array();
                var table = document.getElementById('security_updates_table');
                var rowCount = table.rows.length;
                var i = 1;
                for (var r = 1; r < rowCount; r++) {
                    var isSelected = table.rows[r].cells[0].childNodes[0].checked;
                    if (isSelected) {
                        keyValues[i] = table.rows[r].cells[0].childNodes[0].id.split('chk_content_')[1];
                        i++;
                    }
                }
                return keyValues.join(":_:");
            }
            function selectAllRow() {
                try {
                    var table = document.getElementById('security_updates_table');
                    var rowCount = table.rows.length;
                    var row = table.rows[0];
                    var selectAllChkbox = row.cells[0].childNodes[0];
                    if (selectAllChkbox.checked == true) {
                        $('#syncBtn').prop("disabled", false);
                        $('#syncBtnToggle').prop("disabled", false);
                        for(var i=1; i < rowCount; i++) {
                            row = table.rows[i];
                            var chkbox = row.cells[0].childNodes[0];
                            if (null != chkbox) {
                                chkbox.checked = true;
                            }
                        }
                        if (rowCount == 2) {
                            toggleActionsForAllRow("true");
                        } else {
                            toggleActionsForAllRow("false");
                        }
                    } else {
                        $('#syncBtn').prop("disabled", true);
                        $('#syncBtnToggle').prop("disabled", true);
                        for(var i=1; i < rowCount; i++) {
                            row = table.rows[i];
                            var chkbox = row.cells[0].childNodes[0];
                            if (null != chkbox) {
                                chkbox.checked = false;
                            }
                        }
                        toggleActionsForAllRow("true");
                    }
                } catch(e) {
                    alert(e);
                }
            }
            function selectRow() {
                var table = document.getElementById('security_updates_table');
                var row = table.rows[0];
                var selectAllChkbox = row.cells[0].childNodes[0];
                var totalSelected=0;
                var rowCount = table.rows.length;
                for(var i=1; i < rowCount; i++) {
                    row = table.rows[i];
                    var chkbox = row.cells[0].childNodes[0];
                    if (null != chkbox && chkbox.checked == true) {
                        totalSelected++;
                    }
                }
                if (totalSelected == (rowCount - 1)) {
                    selectAllChkbox.checked = true;
                } else {
                    selectAllChkbox.checked = false;
                }
                if (totalSelected > 0) {
                    toggleActionsForAllRow("false");
                    if (totalSelected == 1) {
                        for(var j=1; j < rowCount; j++) {
                            var tableRow = table.rows[j];
                            var tableRowChkbox = tableRow.cells[0].childNodes[0];
                            if (null != tableRowChkbox && tableRowChkbox.checked == true) {
                                var tableRowSyncButton = document.getElementById('syncBtn_' + tableRowChkbox.id);
                                var tableRowDetailsButton = document.getElementById('detailsBtn_' + tableRowChkbox.id);
                                if (null != tableRowSyncButton) {
                                    tableRowSyncButton.disabled = false;
                                }
                                if (null != tableRowDetailsButton) {
                                    tableRowDetailsButton.disabled = false;
                                }
                            }
                        }
                    }
                    $('#syncBtn').prop("disabled", false);
                    $('#syncBtnToggle').prop("disabled", false);
                } else {
                    toggleActionsForAllRow("true");
                    $('#syncBtn').prop("disabled", true);
                    $('#syncBtnToggle').prop("disabled", true);
                }
            }
            function toggleActionsForAllRow(enable) {
                var table = document.getElementById('security_updates_table');
                var row = table.rows[0];
                var rowCount = table.rows.length;
                for(var j=1; j < rowCount; j++) {
                    row = table.rows[j];
                    var chkbox = row.cells[0].childNodes[0];
                    var syncButton = document.getElementById('syncBtn_' + chkbox.id);
                    if (null != syncButton) {
                        if ("true" == enable) {
                            syncButton.disabled = false;
                        } else {
                            syncButton.disabled = true;
                        }
                    }
                    var detailsButton = document.getElementById('detailsBtn_' + chkbox.id);
                    if (null != detailsButton) {
                        if ("true" == enable) {
                            detailsButton.disabled = false;
                        } else {
                            detailsButton.disabled = true;
                        }
                    }
                }
            }
            function doDiffSyncSelectedXml() {
                var isInProgressSync = document.getElementById('overallInProgressSpan');
                if (isInProgressSync) {
                    alert('Please wait while the active Sync operation is completed.');
                    return;
                }
                var submitaction = '';
                if (document.getElementById("chk_content_all").checked) {
                    submitaction = '/updates.do?action=dosync&contents=all&sync=diff';
                } else {
                    submitaction = '/updates.do?action=dosync&contents=' + getSelectedContentsData() + '&sync=diff';
                }
                defenSightUpdatesForm.action = "/spm" + submitaction + "";
                defenSightUpdatesForm.submit();
            }
            function doForceSyncSelectedXml() {
                var isInProgressSync = document.getElementById('overallInProgressSpan');
                if (isInProgressSync) {
                    alert('Please wait while the active Sync operation is completed.');
                    return;
                }
                var submitaction = '';
                if (document.getElementById("chk_content_all").checked) {
                    submitaction = '/updates.do?action=dosync&contents=all&sync=force';
                } else {
                    submitaction = '/updates.do?action=dosync&contents=' + getSelectedContentsData() + '&sync=force';
                }
                defenSightUpdatesForm.action = "/spm" + submitaction + "";
                defenSightUpdatesForm.submit();
            }
            function doSyncXml(xml) {
                defenSightUpdatesForm.action = "/spm/updates.do?action=dosync&contents=" + xml + ":_:&sync=force";
                defenSightUpdatesForm.submit();
            }
            function reload() {
                defenSightUpdatesForm.action = "/spm/updates.do?action=reload";
                defenSightUpdatesForm.submit();
            }
            function showProfileDetails(fileName, contentTitle, profileIds, profileTitles, targetType) {
                var viewprofiledetailswindowGuideDiv = document.getElementById('viewprofiledetailswindowGuide');
                while(viewprofiledetailswindowGuideDiv.firstChild){
                    viewprofiledetailswindowGuideDiv.removeChild(viewprofiledetailswindowGuideDiv.firstChild);
                }
                viewprofiledetailswindowGuideDiv.innerHTML = contentTitle;
                var viewprofiledetailswindowProfilesDiv = document.getElementById('viewprofiledetailswindowProfiles');
                while(viewprofiledetailswindowProfilesDiv.firstChild){
                    viewprofiledetailswindowProfilesDiv.removeChild(viewprofiledetailswindowProfilesDiv.firstChild);
                }
                var text = '<select id="selectedSCAPProfile" onchange="switchProfile(&apos;' + fileName + '&apos;, this.value,&apos;' + targetType + '&apos;)">';
                var profileIds_array = profileIds.split(';');
                var profileTitles_array = profileTitles.split(';');
                for (var i=0; i < profileIds_array.length - 1; i++) {
                    text += '<option value="' + profileIds_array[i] + '">' + profileTitles_array[i] + '</option>';
                }
                viewprofiledetailswindowProfilesDiv.innerHTML = text;
                var srcUrl = '/spm/securitymgmt?command=gethtml&target='+ targetType + '&content=' + fileName + '&profile=' + profileIds_array[0];
                $("#iframeViewProfileInfo").contents().find("body").html('<div class="loader"></div>');
                document.getElementById('iframeViewProfileInfo').src = srcUrl;
                $('#viewprofiledetailswindow').modal('show');
            }
            function switchProfile(fileName, profileId, targetType) {
                var srcUrl = '/spm/securitymgmt?command=gethtml&target='+ targetType + '&content=' + fileName + '&profile=' + profileId;
                $("#iframeViewProfileInfo").contents().find("body").html('<div class="loader"></div>');
                document.getElementById('iframeViewProfileInfo').src = srcUrl;
            }
        </script>
    </html:form>
</body>
</html>
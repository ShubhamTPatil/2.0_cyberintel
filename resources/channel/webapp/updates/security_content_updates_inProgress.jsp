<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>

<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/icon.css">
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-datepicker3.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
<link rel="stylesheet" type="text/css" href="/spm/css/_all-skins.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css">
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/fuelux.css">

<script type="text/javascript" src="/spm/includes/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/chart.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-datepicker.js"></script>
<script type="text/javascript" src="/spm/js/application.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/wizard.js"></script>


<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/info.jsp" %>

<body>
    <webapps:tabs tabset="main" tab="updates"/>
    <html:form name ="vDeskUpdatesForm" action="/updates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.VDeskUpdatesForm">
        <html:hidden property="action"/>

        <div style="padding-left:25px; padding-right:25px;">
            <table width="100%"><tr>
                <td>
                    <div class="pageHeader">
                        <span class="title"><bean:message key="page.security_content_updates_inProgress.Title"/></span>
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
                    <table id="security_updates_table" class="table table-bordered table-striped dataTable no-footer" style="width: 100%;">
                        <thead>
                            <tr>
                                <th style="width: 56%;"><webapps:pageText key="securitycontent"/></th>
                                <th style="width: 12%;">Assessment Type</th>
                                <th style="width: 12%;"><webapps:pageText key="platform"/></th>
                                <th style="width: 20%;"><webapps:pageText key="updated"/></th>
                                <th style="width: 12%;"><webapps:pageText key="status"/></th>
                            </tr>
                        </thead>
                            <logic:iterate id="update" name="updates" type="com.marimba.apps.securitymgr.view.SecurityUpdateDetailsBean">
                                <tr>
                                    <td valign="center"><bean:write name="update" property="title"/></td>
                                    <td nowrap valign="center"><bean:write name="update" property="assessmentType"/></td>
                                    <td nowrap valign="center"><bean:write name="update" property="platform"/></td>
                                    <td nowrap valign="center"><bean:write name="update" property="updated"/></td>
                                    <logic:equal name="update" property="status" value="inprogress">
                                        <td align="center" valign="center"><div style="display:none">1.inprogress</div><img title='<webapps:pageText key="status.inprogress"/>' src="/spm/images/inprogress.gif" width="32" height="32"></td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="failedupdate">
                                        <td align="center" valign="center"><div style="display:none">2.failedupdate</div><img title='<webapps:pageText key="status.failedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/failedupdate.png" width="32" height="32"></td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="deletedupdate">
                                        <td align="center" valign="center"><div style="display:none">3.deletedupdate</div><img title='<webapps:pageText key="status.deletedupdate"/>&nbsp;<bean:write name="update" property="error"/>' src="/spm/images/deletedupdate.png" width="32" height="32"></td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="newupdate">
                                        <td align="center" valign="center"><div style="display:none">4.newupdate</div><img title='<webapps:pageText key="status.newupdate"/>' src="/spm/images/newupdate.png" width="32" height="32"></td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="existingupdate">
                                        <td align="center" valign="center"><div style="display:none">5.existingupdate</div><img title='<webapps:pageText key="status.existingupdate"/>' src="/spm/images/existingupdate.png" width="32" height="32"></td>
                                    </logic:equal>
                                    <logic:equal name="update" property="status" value="insync">
                                        <td align="center" valign="center"><div style="display:none">6.insync</div><img title='<webapps:pageText key="status.insync"/>' src="/spm/images/insync.png" width="32" height="32"></td>
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

        <script type="text/javascript">
            var contentsDataTable;
            var rows_selected_contents = [];

            $(document).ready(function() {
                populateContentsDataTable('#security_updates_table');
            });

            function populateContentsDataTable(tableSelector) {
                contentsDataTable = $('#security_updates_table').dataTable(
                    {
                        "bPaginate": false,
                        "aoColumns": [
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true },
                            { "bSortable": true, "bSearchable": true }
                        ],
                        "order": [[ 3, "asc" ]]
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
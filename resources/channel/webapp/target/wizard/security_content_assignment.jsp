<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import="java.util.*" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
    Object profilesMap = session.getAttribute("usgcbprofilespropsmap");
    ServletContext context = config.getServletContext();
    IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
    SubscriptionMain main = TenantHelper.getTenantSubMain(context, session, user.getTenantName());
	boolean isPluginConfigured = main.isPluginConfigured();
%>
<webapps:helpContext context="spm" topic="usgcb" />

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap-glyphicons.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
<link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/fuelux.css">

<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/application.js"></script>
<script type="text/javascript" src="/spm/js/wizard.js"></script>
<style>
    .loader {
        position: fixed;
        left: 0px;
        top: 0px;
        width: 100%;
        height: 100%;
        z-index: 9999;
        background: url('/spm/images/page-loader.gif') 50% 50% no-repeat rgb(249,249,249);
    }
</style>
</head>

<body  class="no-js fuelux">
<html:form name="securityContentAssignmentForm" action="/securityContentAssignment.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.SecurityContentAssignmentForm">
<html:hidden property="action"/>
<html:hidden property="selectedWindowsProfiles" />
<html:hidden property="selectedNonWindowsProfiles"/>
<html:hidden property="customizedProfiles"/>
<html:hidden property="priority"/>
<html:hidden property="remediateEnabled"/>

<logic:notPresent name="taskid">
    <webapps:tabs tabset="main" tab="tgtview"/>
</logic:notPresent>
<logic:present name="taskid">
    <% request.setAttribute("nomenu", "true"); %>
    <webapps:tabs tabset="bogustabname" tab="noneselected"/>
</logic:present>
<div align="center">
<div style="padding-left:25px; padding-right:25px;">
   
<div class="pageHeader">
    <table cellspacing="0" cellpadding="3" border="0">
        <tr>
			<td>
			<span class="title"><webapps:pageText key="policy" type="pgtitle" shared="true"/> : &nbsp;</span>
			</td>
            <logic:iterate id="target" name="session_dist" property="targets">
                <td align="left">
                    <bean:define id="ID" name="target" property="id" toScope="request"/>
                    <bean:define id="Name" name="target" property="name" toScope="request"/>
                    <bean:define id="Type" name="target" property="type" toScope="request"/>
                    <jsp:include page="/includes/target_display_single.jsp"/>
                </td>
            </logic:iterate>
        </tr>
    </table>
</div>
<%-- Errors Display --%>
<div style="width:100%; ">
    <logic:present scope="request" name="error">
        <div align="left" style="margin-left:20px; margin-right:20px;">
          <div class="statusMessage" id="critical">
            <h6><webapps:pageText key="error" type="global"/></h6>
             <p>
                 <img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()">
                 <font class="redText"><font class="generalText"> <bean:write name="error" scope="request"/> </font></font>
             </p>
          </div>
       </div>
    </logic:present>
</div>

<div id="securityContentAssignmentWizard" data-initialize="wizard" class="wizard">
    <div class="steps-container">
        <ul class="steps">
            <li data-step="1" class="active" id="link_step1"><span class="badge badge-info">1</span>Introduction<span class="chevron"></span></li>
            <li data-step="2" id="link_step2"><span class="badge">2</span>Select Windows Content<span class="chevron"></span></li>
            <li data-step="3" id="link_step3"><span class="badge">3</span>Select Non-Windows Content<span class="chevron"></span></li>
            <li data-step="4" id="link_step4"><span class="badge">4</span>Customize<span class="chevron"></span></li>
            <li data-step="5" id="link_step5"><span class="badge">5</span>Preview<span class="chevron"></span></li>
        </ul>
    </div>
    <div class="actions">
        <button type="button" id="cancelBtn" class="btn btn-mini btn-cancel bg-yellow-gradient">Cancel&nbsp;<i class="fa fa-close"></i></button>
        <button type="button" id="prevBtn" class="btn btn-mini btn-prev bg-aqua-gradient"><i class="fa fa-hand-o-left">&nbsp;</i>Back</button>
        <button type="button" id="nextBtn" class="btn btn-mini btn-next bg-aqua-gradient">Next&nbsp;<i class="fa fa-hand-o-right">&nbsp;</i></button>
    </div>
</div>

<div class="step-content">
    <div class="step-pane active sample-pane alert" id="step1" name="stepsData" data-step="1" style="display:block;height:403px;border:1px solid #cccccc;background-color:white;">
        <%@ include file="/target/wizard/assignment_intro.jsp" %>
    </div>
    <div class="step-pane sample-pane alert" id="step2" name="stepsData" data-step="2" style="display:none;height:auto;border:1px solid #cccccc;background-color:white;">
        <%@ include file="/target/wizard/assignment_windows.jsp" %>
    </div>
    <div class="step-pane sample-pane alert" id="step3" name="stepsData" data-step="3" style="display:none;height:auto;min-height:403px;border:1px solid #cccccc;background-color:white;">
        <%@ include file="/target/wizard/assignment_nonwindows.jsp" %>
    </div>
    <div class="step-pane sample-pane alert" id="step4" name="stepsData" data-step="4" style="display:none;height:auto;min-height:403px;border:1px solid #cccccc;background-color:white;">
      <%@ include file="/target/wizard/assignment_customize.jsp" %>
    </div>
    <div class="step-pane sample-pane alert" id="step5" name="stepsData" data-step="5" style="display:none;height:auto;min-height:403px;border:1px solid #cccccc;background-color:white;">
        <%@ include file="/target/wizard/assignment_preview.jsp" %>
    </div>
</div>
</div>

<div id="viewprofiledetailswindow" class="modal fade" role="dialog" data-backdrop="false" style="background-color: rgba(0, 0, 0, 0.5);">
    <div class="modal-dialog" style=width:80%;height:100%;>
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title text-left">SCAP Profile Details</h4>
            </div>
            <div id="viewProfileInfoDiv" class="modal-body">
                <iframe id="iframeViewProfileInfo" src="/spm/scap_profile_details_template.html" width="100%" height="550px" frameBorder="0"></iframe>
            </div>
        </div>
        <div class="modal-footer">
        </div>
    </div>
</div>

<div id="editprofiledetailswindow" class="modal fade" role="dialog" data-backdrop="false" style="background-color: rgba(0, 0, 0, 0.5);">
    <div class="modal-dialog" style=width:80%;height:100%;>
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title text-left">SCAP Profile Details</h4>
				 <% if(!isPluginConfigured) { %>
	                <div align="left" style="margin-left:20px; margin-right:20px;">
			          <div class="statusMessage" id="critical">
			            <h6><webapps:pageText key="error" type="global"/></h6>
			             <p>
			                 <img src="/shell/common-rsrc/images/error.gif" width="24" height="24" onLoad="init()">
			                 <font class="redText"><font class="generalText"><webapps:pageText key="noplugin_errormsg" type="security_content_assignment" shared="true"/></font></font>
			             </p>
			          </div>
	       			</div>
                 <% } %>
            </div>
            <div id="editProfileInfoDiv" class="modal-body">
                <div clas="row">
                    <div class="col-md-8  text-left">
                        <table class="table no-border">
                            <tr>
                                <td align="left" nowrap><webapps:pageText key="profile_name" type="security_content_assignment" shared="true"/></td>
                                <td>
                                    <input type="text" size="50" id="profile_name" class="requiredField" onkeyup="nospaces(this)">
                                </td>
                            </tr>
                            <tr>
                                <td align="left" nowrap><webapps:pageText key="profile_desc" type="security_content_assignment" shared="true"/></td>
                                <td>
                                    <input type="text" size="80" id="profile_desc" class="requiredField">
                                </td>
                            </tr>
                        </table>
                     </div>
                    <div class="col-md-4 text-right">
					<% if(!isPluginConfigured) { %>
						<button type='button' class='btn btn-primary'><i class='fa fa-save'></i>&nbsp;Save</button>
					<% } else { %>
                        <button type='button' class='btn btn-primary' onclick="saveProfile()"><i class='fa fa-save'></i>&nbsp;Save</button>
					 <% } %>
                    </div>
                </div>
                <iframe id="iframeEditProfileInfo" src="/spm/scap_profile_details_template.html" width="100%" height="550px" frameBorder="0"></iframe>
            </div>
        </div>
        <div class="modal-footer">
        </div>
    </div>
</div>

<script type="text/javascript">
     var rows_selected_windows = [];
     var rows_selected_nonwindows = [];
     var customized_profiles = [];
     var customizedProfileStr = '';
     var windowsContentsDataTable;
     var nonWindowsContentsDataTable;
     var customizeContentsDataTable;
     var previewContentsDataTable;

     var selectedContentId;
     var selectedProfileId;
     var selectedTargetType;
     var modifiedRulesAndValues = '';

     $(document).ready(function() {

        $('#link_step1').click(function(){loadSection('step1');});
        $('#link_step2').click(function(){loadSection('step2');});
        $('#link_step3').click(function(){loadSection('step3');});
        $('#link_step4').click(function(){loadSection('step4');});
        $('#link_step5').click(function(){loadSection('step5');});

        $('#viewprofiledetailswindow').modal('hide');
        $('#editprofiledetailswindow').modal('hide');

        $('#securityContentAssignmentWizard').wizard();

        $('#securityContentAssignmentForm').on('submit', function(e) {
            e.preventDefault();
        });

        $('#securityContentAssignmentWizard').wizard().on('change', function(e, data) {
            alert('change, data.step - ' + data.step + ', data.direction - ' + data.direction);
            if(data.step===3 && data.direction==='next') {
                // return e.preventDefault();
            }
        });

        $('#securityContentAssignmentWizard').wizard().on('changed', function(e, data) {
            alert('changed');
        });

        $('#securityContentAssignmentWizard').wizard().on('finished', function(e, data) {
            alert('finished');
            $('#securityContentAssignmentForm').unbind('submit');
            $('#securityContentAssignmentForm').submit();
        });

        $('.btn-prev').on('click', function() {
            var stepsData = document.getElementsByName('stepsData');
            var r = 0;
            for (; r < stepsData.length; r++) {
                if ('block' == stepsData[r].style.display) {
                    stepsData[r].style.display = 'none';
                    stepsData[r-1].style.display = 'block';
                    if ('step5' == stepsData[r].id) {
                        var nextBtn = document.getElementById("nextBtn");
                        while (nextBtn.firstChild) {
                            nextBtn.removeChild(nextBtn.firstChild);
                        }
                        nextBtn.innerHTML = 'Next&nbsp;<i class="fa fa-hand-o-right">&nbsp;</i>';
                    }
                    if ('step1' == stepsData[r-1].id) {
                        $('.btn-prev').prop("disabled", true);
                        } else {
                        $('.btn-prev').prop("disabled", false);
                    }
                    break;
                }
            }
        });

        $('.btn-next').on('click', function() {
            var stepsData = document.getElementsByName('stepsData');
            var r = 0;
            for (; r < stepsData.length; r++) {
                if ('block' == stepsData[r].style.display) {
                    if ('step5' == stepsData[r].id) {
                        apply();
                    } else {
                        var valid = "true", errorDesc = "";
                        if ('step2' == stepsData[r].id) {
                            var selectedRows = getSelectedWindowsData(windowsContentsDataTable);
                            //alert(document.securityContentAssignmentForm["selectedWindowsProfiles"].value);
                        }
                        if ('step3' == stepsData[r].id) {
                            var selectedWindowsRows = getSelectedWindowsData(windowsContentsDataTable);
                            var selectedNonWindowsRows = getSelectedNonWindowsData(nonWindowsContentsDataTable);
                            if (selectedWindowsRows == '' && selectedNonWindowsRows == '') {
                                alert("Profiles are not selected, please select atleast one profile to proceed");
                                //stepsData[r].style.display = 'none';
                                $('#link_step3').click();
                                return;
                            }
                            //alert(document.securityContentAssignmentForm["selectedWindowsProfiles"].value);
                            loadCustomizeContentDataTable();
                        }
                        if ('step4' == stepsData[r].id) {
                            customizedProfileStr = getCustomizedProfiles();
                            loadPreviewContentDataTable();
                            if (valid == "true") {
                                var nextBtn = document.getElementById("nextBtn");
                                while (nextBtn.firstChild) {
                                    nextBtn.removeChild(nextBtn.firstChild);
                                }
                                nextBtn.innerHTML = 'Apply&nbsp;<i class="fa fa-check">&nbsp;</i>';
                            }
                        }

                        if (valid == "true") {
                            stepsData[r].style.display = 'none';
                            stepsData[r+1].style.display = 'block';
                        } else {
                            $('#securityContentAssignmentWizard').wizard('previous');
                        }
                    }
                    break;
                }
            }
        });
         $('.btn-cancel').on('click', function() {
             conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');
             //cancel();
         });
    });

     function loadSection(step) {
         var stepsData = document.getElementsByName('stepsData');
         var r = 0;
         for (; r < stepsData.length; r++) {
             if (step == stepsData[r].id) {
                 stepsData[r].style.display = 'block';
             } else {
                 stepsData[r].style.display = 'none';
             }
         }
     }

     function persistCheckbox(tableId) {
         $(tableId + ' tbody input[type="checkbox"]:checked').each(function() {
             var $row = $(this).closest('tr');
             if (this.checked) $row.addClass('selected');
             else $row.removeClass('selected');
         });
     }

     function populateWindowsContentsDataTable(tableSelector) {
         if (windowsContentsDataTable) windowsContentsDataTable.fnDestroy();
         windowsContentsDataTable = $(tableSelector).DataTable( {
                "bPaginate": false,
                "columnDefs": [ { "targets": 0, "orderable": false } ],
                "order": [[ 1, 'asc' ]]
        } );

         $(tableSelector + ' tbody').on('click', 'input[type="checkbox"]', function(e) {
             var $row = $(this).closest('tr');
             var data = windowsContentsDataTable.row($row).data();
             var rowId = data[0];
             var index = $.inArray(rowId, rows_selected_windows);
             if (this.checked && index === -1) {
                 rows_selected_windows.push(rowId);
             } else if (!this.checked && index !== -1){
                 rows_selected_windows.splice(index, 1);
             }
             if (this.checked) $row.addClass('selected');
             else $row.removeClass('selected');

             e.stopPropagation();
         });
         $(tableSelector).on('click', 'thead th:first-child, tbody td:first-child', function(e){
             $(this).parent().find('input[type="checkbox"]').trigger('click');
         });

         $('thead input[name="select_all"]', windowsContentsDataTable.table().container()).on('click', function(e){
             if (this.checked){
                 $(tableSelector + ' tbody input[type="checkbox"]:not(:checked)').trigger('click');
             } else {
                 $(tableSelector + ' tbody input[type="checkbox"]:checked').trigger('click');
             }
             e.stopPropagation();
         });
     }

     function populateNonWindowsContentsDataTable(tableSelector) {
         if (nonWindowsContentsDataTable) nonWindowsContentsDataTable.fnDestroy();
         nonWindowsContentsDataTable = $(tableSelector).DataTable( {
             "bPaginate": false,
             "columnDefs": [ { "targets": 0, "orderable": false } ],
             "order": [[ 1, 'asc' ]]
        } );

         $(tableSelector + ' tbody').on('click', 'input[type="checkbox"]', function(e) {
             var $row = $(this).closest('tr');
             var data = nonWindowsContentsDataTable.row($row).data();
             var rowId = data[0];
             var index = $.inArray(rowId, rows_selected_nonwindows);
             if (this.checked && index === -1) {
                 rows_selected_nonwindows.push(rowId);
             } else if (!this.checked && index !== -1){
                 rows_selected_nonwindows.splice(index, 1);
             }
             if (this.checked) $row.addClass('selected');
             else $row.removeClass('selected');

             e.stopPropagation();
         });
         $(tableSelector).on('click', 'thead th:first-child, tbody td:first-child', function(e){
             $(this).parent().find('input[type="checkbox"]').trigger('click');
         });

         $('thead input[name="select_all"]', nonWindowsContentsDataTable.table().container()).on('click', function(e){
             if (this.checked){
                 $(tableSelector + ' tbody input[type="checkbox"]:not(:checked)').trigger('click');
             } else {
                 $(tableSelector + ' tbody input[type="checkbox"]:checked').trigger('click');
             }
             e.stopPropagation();
         });
     }

     function persistCheckbox(tableId) {
         $(tableId + ' tbody input[type="checkbox"]:checked').each(function() {
             var $row = $(this).closest('tr');
             if (this.checked) $row.addClass('selected');
             else $row.removeClass('selected');
         });
     }
     /*
     function getSelectedRowData(table) {
         return $(table.rows('.selected').data()).map(function(item, value) {
             return $(value[0]).attr(id').split('chk1_')[1] + '$-$' + value[1] + '$-$' + $('#' + $(value[3]).attr('id')).val() + '$-$' + $('#' + $(value[3]).attr('id') + ' option:selected').text() + '$-$' + value[2] ;
         }).get().join(":_:");
     }
     */
     function getSelectedWindowsData(table) {
         return $(table.rows('.selected').data()).map(function(item, value) {
             return $(value[0]).attr('id').split('wchk1_')[1] + '$-$' + value[1] + '$-$' + $('#' + $(value[4]).attr('id')).val() + '$-$' + $('#' + $(value[4]).attr('id') + ' option:selected').text() + '$-$' + value[2] + '$-$' + value[3];
         }).get().join(":_:");
     }

     function getSelectedNonWindowsData(table) {
         return $(table.rows('.selected').data()).map(function(item, value) {
             return $(value[0]).attr('id').split('nwchk1_')[1] + '$-$' + value[1] + '$-$' + $('#' + $(value[4]).attr('id')).val() + '$-$' + $('#' + $(value[4]).attr('id') + ' option:selected').text() + '$-$' + value[2] + '$-$' + value[3];
         }).get().join(":_:");
     }

    function showProfileDetails(content, profile, targetType) {
        var templateName = '';
        if (profile.lastIndexOf("@"+templateName) != -1) {
            templateName = profile.substring(profile.lastIndexOf("@")+1);
            profile = profile.substring(0,profile.lastIndexOf("@"));
        }
        //alert("Template Name : " + templateName);
        var srcUrl = '/spm/securitymgmt?command=gethtml&target='+ targetType + '&content=' + content + '&profile=' + profile;
        if (templateName != '') {
            srcUrl = srcUrl + "&template="+ templateName + ".properties";
        }
        $("#iframeViewProfileInfo").contents().find("body").html('<div class="loader"></div>');
        document.getElementById('iframeViewProfileInfo').src = srcUrl;
        $('#viewprofiledetailswindow').modal('show');
    }

    function editProfileDetails(content, profileId, profileTitle, targetType) {
        modifiedRulesAndValues = '';
        var templateName = '';
        var templateDesc = '';
        if (profileId.lastIndexOf("@"+templateName) != -1) {
            templateName = profileId.substring(profileId.lastIndexOf("@")+1);
            profileId = profileId.substring(0,profileId.lastIndexOf("@"));
            templateDesc = profileTitle;
        }
        if (templateName != '') {
            $("#profile_name").val(templateName);
        } else {
            $("#profile_name").val('');
        }

        if (templateDesc != '') {
            $("#profile_desc").val(templateDesc);
        } else {
            $("#profile_desc").val('');
        }

        selectedContentId = content;
        selectedProfileId = profileId;
        selectedTargetType = targetType;
        //alert(content + " " + profile + " " + targetType )
        var srcUrl = '/spm/securitymgmt?command=gethtml&target='+ targetType + '&content=' + content + '&profile=' + profileId + '&customize=true';

        if (templateName != '') {
            srcUrl = srcUrl + "&template="+ templateName + ".properties";
        }
        //alert(srcUrl);
        $("#iframeEditProfileInfo").contents().find("body").html('<div class="loader"></div>');
        document.getElementById('iframeEditProfileInfo').src = srcUrl;
        $('#editprofiledetailswindow').modal('show');
    }

    function nospaces(t){
        if(t.value.match(/\s/g)){
            alert('<webapps:pageText key="nospace" type="global" shared="true"/>');
            t.value=t.value.replace(/\s/g,'');
        }
    }

    function saveProfile() {
        var profileName = $("#profile_name").val();
        if (profileName == '') {
            alert("Please specify Profile Name");
            return;
        }
        var profileDesc = $("#profile_desc").val();
        if (profileDesc == '') {
            alert("Please specify Profile Description");
            return;
        }
        var profilePriority = $("#profile_priority").val();
        var profileDetails = selectedContentId + ';'+ selectedProfileId + ";"+ selectedTargetType + ";" + profileName+ ";" + profileDesc + ";" + profilePriority + ";" + modifiedRulesAndValues;
        customized_profiles.push(profileDetails);
        $('#editprofiledetailswindow').modal('hide');
    }

    function getCustomizedProfiles() {
        var profiles = '';
        for (var i=0;i<customized_profiles.length; i++) {
            profiles = profiles + customized_profiles[i];
            if (i+1 < customized_profiles.length) {
                profiles = profiles + ":_:";
            }
        }
        return profiles;
    }

    function getPriorityValue() {
        var priority = $("#customize_priority").val();
        if (priority == null || priority == '') {
            priority = '99999';
        }
        return priority;
    }

    function isRemediationEnabled() {
        var isRemediateChkBoxEnabled = $("#customize_remediateEnabled").prop('checked');
        var remediateEnabled = "false";
        if (isRemediateChkBoxEnabled == true) {
            remediateEnabled = "true";
        }
        return remediateEnabled;
    }

    function apply() {
        var selectedWindowsRows = getSelectedWindowsData(windowsContentsDataTable);
        document.securityContentAssignmentForm["selectedWindowsProfiles"].value = selectedWindowsRows;
        var selectedNonWindowsRows = getSelectedNonWindowsData(nonWindowsContentsDataTable);
        document.securityContentAssignmentForm["selectedNonWindowsProfiles"].value = selectedNonWindowsRows;
        var customizedProfiles = getCustomizedProfiles();
        document.securityContentAssignmentForm["customizedProfiles"].value = customizedProfiles;

        document.securityContentAssignmentForm["priority"].value=getPriorityValue();
        document.securityContentAssignmentForm["remediateEnabled"].value=isRemediationEnabled();

        document.securityContentAssignmentForm["action"].value="save";
        document.securityContentAssignmentForm.submit();
    }

     function cancel() {
         document.securityContentAssignmentForm["action"].value="cancel";
         document.securityContentAssignmentForm.submit();
     }

</script>
</html:form>
</body>
</html>
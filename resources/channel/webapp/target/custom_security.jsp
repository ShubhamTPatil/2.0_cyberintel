<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import="java.util.*" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
    Object profilesMap = session.getAttribute("customprofilespropsmap");
%>
<webapps:helpContext context="spm" topic="custom" />
<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>

<script type="text/javascript">

    function alterProfileSettings(profileName, type) {
        var forward = '/customSecurityLoad.do?selectedprofilename='+profileName+'&type='+type;
        send(document.customSecurityForm, forward);
        return false;
    }

    function inactiveSCAPOptionParameters(optionSelected) {
        if ('exclude' == optionSelected) {
            $('#tr_custom_data_table').hide();
            $('#tr_standard_data_table').hide();
            document.customSecurityForm["remediate"].disabled = true;
     		document.getElementById('customize_priority_id').disabled=true;
			document.getElementById('standard_priority_id').disabled=true;
        } else if ('customize' == optionSelected) {
            $('#tr_custom_data_table').show();
            $('#tr_standard_data_table').hide();
            document.customSecurityForm["remediate"].disabled = false;
			document.getElementById('customize_priority_id').disabled=false;
			document.getElementById('standard_priority_id').disabled=true;
        } else if ('standard' == optionSelected) {
            $('#tr_custom_data_table').hide();
            $('#tr_standard_data_table').show();
            document.customSecurityForm["remediate"].disabled = false;
			document.getElementById('customize_priority_id').disabled=true;
			document.getElementById('standard_priority_id').disabled=false;
        } else {
            $('#tr_custom_data_table').hide();
            $('#tr_standard_data_table').hide();
            document.customSecurityForm["remediate"].disabled = true;
			document.getElementById('customize_priority_id').disabled=true;
			document.getElementById('standard_priority_id').disabled=true;
        }
    }

    function switchContentProfileSettings(contentname) {
        var selectedSCAPOptionValue = document.customSecurityForm.selectedSCAPOption.value;
        $.ajax({
            url: '/spm/securitymgmt?command=scapcontentchange&content=' + contentname,
            type: 'GET',
            cache: false,
            async: false,
            dataType: "json",
            contentType: 'application/json',
            success: function (response) {
                var profileIdsResponse = response.profilesId;
                var profileTitlesResponse = response.profilesTitle;

                var text = '<select id="selectedSCAPProfile">';
                for (var i=0; i < profileIdsResponse.length; i++) {
                    text += '<option value="' + profileIdsResponse[i] + '">' + profileTitlesResponse[i] + '</option>';
                }

                text += '</select>';
                if (selectedSCAPOptionValue == 'standard') {
                    text += '<a href="#" onclick="showStandardSCAPProfileDetails();return false;"><img id="imgStandardProfileDetails" style="display:inline;" src="/spm/images/more-details.png" title="View Profile Details"/></a>';
                } else {
                    text += '<a href="#" onclick="showStandardSCAPProfileDetails();return false;"><img id="imgStandardProfileDetails" style="display:none;" src="/spm/images/more-details.png" title="View Profile Details"/></a>';
                }

                var divParent = document.getElementById("selectedSCAPProfileDiv");
                while(divParent.firstChild) {
                    divParent.removeChild(divParent.firstChild);
                }

                var divChild = document.createElement("div");
                divChild.innerHTML = text;
                divParent.appendChild(divChild);
                if (selectedSCAPOptionValue == 'standard') {
                    document.getElementById('selectedSCAPProfile').disabled = false;
                } else {
                    document.getElementById('selectedSCAPProfile').disabled = true;
                }
            },
            fail: function(xhr, status, err) {
                //ignore...
            }
        });
    }

    function showStandardSCAPProfileDetails(content, profile) {
        document.getElementById('iframeProfileInfo').src = '/spm/securitymgmt?command=gethtml&target=custom&content=' + content + '&profile=' + profile;
        $('#standardprofiledetailswindow').modal('show');
    }

    function showCustomSCAPProfileDetails(fileName, profileid, templateName) {
        document.getElementById('iframeProfileInfo_custom').src = '/spm/securitymgmt?command=gethtml&target=custom&content=' + fileName + '&profile=' + profileid + '&template=' + templateName + '.properties';
        $('#customprofiledetailswindow').modal('show');
    }
</script>
</head>

<body>
<html:form name="customSecurityForm" action="/customSecuritySave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.CustomSecurityForm">
<html:hidden property="value(forward)" />
<html:hidden property="value(selectedSCAPProfile)" />
<html:hidden property="value(selectedScapAndProfilesCustom)"/>
<logic:notPresent name="taskid">
    <% if(null != EmpirumContext) {%>
    <webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
    <% } else { %>
    <webapps:tabs tabset="main" tab="tgtview"/>
    <% } %>
</logic:notPresent>
<logic:present name="taskid">
    <% request.setAttribute("nomenu", "true"); %>
    <webapps:tabs tabset="bogustabname" tab="noneselected"/>
</logic:present>
<div align="center">
<div style="padding-left:25px; padding-right:25px;">
<div class="pageHeader">
    <span class="title"><webapps:pageText key="policy" type="pgtitle" shared="true"/></span>
</div>
<logic:present name="taskid">
    <div class="pageHeader">
        <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
        <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
    </div>
</logic:present>
<%-- Errors Display --%>
<div style="width:100%; ">
    <table style="width:100%;" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>
</div>

<div class="pageInfo">
    <table cellspacing="0" cellpadding="2" border="0">
        <tr>
            <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>
            <td><webapps:pageText key="IntroShort"/></td>
        </tr>
    </table>
</div>

<div class="itemStatus">
    <table cellspacing="0" cellpadding="3" border="0">
        <tr>
            <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>: </td>
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

<webapps:formtabs tabset="dist" tab="custom" />
<div class="formContent" id="dataSection" style="text-align:left; overflow:auto;">
<div class="sectionInfo"><webapps:pageText key="SectionInfo"/></div>
<%
    Object customContentsMapObj = session.getAttribute("customcontentdetailsmap");
    if ((customContentsMapObj != null) && (((java.util.Map<String, String>)customContentsMapObj).size() > 0)) {
%>
<table border="0" cellspacing="0" cellpadding="3" width="90%">

<tr><td colspan="3"><webapps:pageText key="label.scapOption"/></td></tr>

<tr>
    <td width="8%" align="right" valign="top" style="padding-top:5px;padding-bottom:5px"> <html:radio property="selectedSCAPOption" value="exclude" onclick="inactiveSCAPOptionParameters(this.value)"/>&nbsp; </td>
    <td colspan="2" valign="top" style="padding-top:5px;padding-bottom:5px;padding-left:8px;"><webapps:pageText key="excludeScapOption"/></td>
</tr>

<tr>
    <td width="8%" align="right" valign="top" style="padding-top:5px;padding-bottom:5px"> <html:radio property="selectedSCAPOption" value="customize" onclick="inactiveSCAPOptionParameters(this.value)"/>&nbsp; </td>
    <td colspan="2" valign="top" style="padding-top:5px;padding-bottom:5px;padding-left:8px;"><webapps:pageText key="useProfile"/>
	&nbsp;&nbsp;&nbsp;&nbsp;<b><webapps:pageText key="priority"/></b>&nbsp;&nbsp;<html:text property="customizePriorityValue" styleId="customize_priority_id" size="5" maxlength="5" onkeypress="return restrictKeyPressPositive(event)"/> &nbsp;&nbsp;<webapps:pageText key="validpriority"/></td>
</tr>

<tr id="tr_custom_data_table">
    <td>&nbsp;</td>
    <td colspan="2" class="col3">
        <table id="custom_data_table" class="table table-bordered table-striped dataTable">
            <thead>
                <td><input name="select_all" type="checkbox"></td>
                <td>Custom Profile Name</td>
                <td>Standard SCAP Content</td>
                <td>Profile Name</td>
                <td>View</td>
            </thead>
            <tbody>
                <logic:iterate name="scapBeansListCustom" id="usgcbListCustom" scope="session" indexId="indexId">
                    <bean:define id="profile" name="usgcbListCustom" property="profiels" toScope="request"/>
                    <bean:define id="selectedProfile" name="usgcbListCustom" property="selectedProfile" toScope="request"/>
                    <tr>
                        <td>
                            <logic:equal name="usgcbListCustom" property="selected" value="true">
                                <input type="checkbox" id="chk1_<bean:write name="usgcbListCustom" property="fileName"/>" checked>
                            </logic:equal>
                            <logic:equal name="usgcbListCustom" property="selected" value="false">
                                <input type="checkbox" id="chk1_<bean:write name="usgcbListCustom" property="fileName"/>">
                            </logic:equal>
                        </td>
                        <td><bean:write name="usgcbListCustom" property="templateName"/></td>
                        <td><bean:write name="usgcbListCustom" property="title"/></td>
                        <td>
                            <select id="profilekey_custom_<%= indexId.toString() %>">
                                <logic:iterate name="profile" id="pro_file">
                                    <option value='<bean:write name="pro_file" property="key"/>'><bean:write name="pro_file" property="value"/></option>
                                </logic:iterate>
                            </select>
                        </td>
                        <td><a href="#" onclick="showCustomSCAPProfileDetails('<bean:write name="usgcbListCustom" property="fileName"/>', $('#profilekey_custom_<%= indexId.toString() %>').val(), '<bean:write name="usgcbListCustom" property="templateName"/>');return false;"><img src="/spm/images/more-details.png" title="View Profile Details"/></a></td>
                    </tr>
                </logic:iterate>
            </tbody>
        </table>
    </td>
</tr>

<tr>
    <td width="8%" align="right" valign="top" style="padding-top:5px;padding-bottom:5px"> <html:radio property="selectedSCAPOption" styleId="standardprofile_scap" value="standard" onclick="inactiveSCAPOptionParameters(this.value)"/>&nbsp; </td>
    <td colspan="2" valign="top" style="padding-top:5px;padding-bottom:5px;padding-left:8px;"><webapps:pageText key="useStandardProfile"/>
	&nbsp;&nbsp;&nbsp;&nbsp;<b><webapps:pageText key="priority"/></b>&nbsp;&nbsp;<html:text property="standardPriorityValue" styleId="standard_priority_id" size="5" maxlength="5" onkeypress="return restrictKeyPressPositive(event)"/> &nbsp;&nbsp;<webapps:pageText key="validpriority"/></td>
</tr>

<tr id="tr_standard_data_table">
    <td>&nbsp;</td>
    <td colspan="2" class="col3">
        <table id="standard_data_table" class="table table-bordered table-striped dataTable">
            <thead>
                <td><input name="select_all" type="checkbox"></td>
                <td>Standard SCAP Content</td>
                <td>Profile Name</td>
                <td>View</td>
            </thead>
            <tbody>
                <logic:iterate name="scapBeansList" id="usgcbList" scope="session" indexId="indexId">
                    <bean:define id="profile" name="usgcbList" property="profiels" toScope="request"/>
                    <bean:define id="selectedProfile" name="usgcbList" property="selectedProfile" toScope="request"/>
                    <tr>
                        <td>
                            <logic:equal name="usgcbList" property="selected" value="true">
                                <input type="checkbox" id="chk1_<bean:write name="usgcbList" property="fileName"/>" checked name="chk_box">
                            </logic:equal>
                            <logic:equal name="usgcbList" property="selected" value="false">
                                <input type="checkbox" id="chk1_<bean:write name="usgcbList" property="fileName"/>">
                            </logic:equal>
                        </td>
                        <td><bean:write name="usgcbList" property="title"/></td>
                        <td>
                            <select id="profilekey_<%= indexId.toString() %>">
                                <logic:iterate name="profile" id="pro_file">
                                    <bean:define id="currProfileKey" name="pro_file" property="key"/>
                                    <% if (currProfileKey.equals(selectedProfile)) { %>
                                    <option value="<bean:write name="pro_file" property="key"/>" selected><bean:write name="pro_file" property="value"/></option>
                                    <% } else {%>
                                    <option value="<bean:write name="pro_file" property="key"/>"><bean:write name="pro_file" property="value"/></option>
                                    <% } %>
                                </logic:iterate>
                            </select>

                        </td>
                        <td><a href="#" onclick="showStandardSCAPProfileDetails('<bean:write name="usgcbList" property="fileName"/>', $('#profilekey_<%= indexId.toString() %>').val());return false;"><img src="/spm/images/more-details.png" title="View Profile Details"/></a></td>
                    </tr>
                </logic:iterate>
            </tbody>
        </table>
    </td>
</tr>
<tr>
    <td width="8%" align="right" style="padding-bottom:5px;"><html:checkbox property="remediate"/></td>
    <td colspan="2" style="padding-top:5px;padding-bottom:5px;padding-left:8px;"><webapps:pageText key="remediate"/></td>
</tr>
<tr>
    <td width="8%">&nbsp;</td>
    <td colspan="2"><%@ include file="/target/custom_setting_options.jsp" %></td>
</tr>
</table>
<%} else {%>
<b>Marimba vDef used in your environment doesn't include any vMediate in it. <br>Please check with your Administrator on availability of any new updates for vDef.</b>
<%}%>
</div>
<!--end formContent-->
<div class="formBottom">
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
            <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
        </tr>
    </table>
</div>
<div id="pageNav">
    <%if ((customContentsMapObj != null) && (((java.util.Map<String, String>)customContentsMapObj).size() > 0)) {%>
    <input name="Submit32" type="submit" class="mainBtn" onClick="javascript:saveState('/customSecurityLoad.do?action=preview')" value="<webapps:pageText key="preview" type="button" shared="true"/>">
    &nbsp;
    <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
    </logic:present>
    <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
    </logic:notPresent>
    <%} else {%>
    <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:saveState('/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
    </logic:present>
    <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:saveState('/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
    </logic:notPresent>
    <%}%>
</div>
</div>
<!--end supder div for padding-->
</div>
<!--end super div for centering-->
</html:form>

<div id="standardprofiledetailswindow" class="modal fade" role="dialog" data-backdrop="false" style="background-color: rgba(0, 0, 0, 0.5);">
    <div class="modal-dialog" style=width:80%;height:100%;>
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">SCAP Profile Details</h4>
            </div>
            <div id="scapcontent_id" class="modal-body">
                <iframe id="iframeProfileInfo" src="/spm/scap_profile_details_template.html" width="100%" height="550px" frameBorder="0"></iframe>
            </div>
        </div>
        <div class="modal-footer">
        </div>
    </div>
</div>

<div id="customprofiledetailswindow" class="modal fade" role="dialog" data-backdrop="false" style="background-color: rgba(0, 0, 0, 0.5);">
    <div class="modal-dialog" style=width:80%;height:100%;>
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">SCAP Profile Details</h4>
            </div>
            <div id="scapcontent_id" class="modal-body">
                <iframe id="iframeProfileInfo_custom" src="/spm/scap_profile_details_template.html" width="100%" height="550px" frameBorder="0"></iframe>
            </div>
        </div>
        <div class="modal-footer">
        </div>
    </div>
</div>

<script type="text/javascript">
var rows_selected_custom = [];
var rows_selected_standard = [];
var customDataTable;
var standardDataTable;

var optionSelected = document.customSecurityForm.selectedSCAPOption.value;
populateCustomDataTable('#custom_data_table');
persistCheckbox('#custom_data_table');

populateStandardDataTable('#standard_data_table');
persistCheckbox('#standard_data_table');

inactiveSCAPOptionParameters(optionSelected);

CMSOnResizeHandler.addHandler("resizeDataSection('dataSection','pageNav');");
resizeDataSection('dataSection','pageNav');

$('#standardprofiledetailswindow').modal('hide');
$('#customprofiledetailswindow').modal('hide');

function persistCheckbox(tableId) {
    $(tableId + ' tbody input[type="checkbox"]:checked').each(function() {
        var $row = $(this).closest('tr');
        if (this.checked) $row.addClass('selected');
        else $row.removeClass('selected');
    });
}

function populateCustomDataTable(tableSelector) {
    customDataTable = $(tableSelector).DataTable();
    $(tableSelector + ' tbody').on('click', 'input[type="checkbox"]', function(e) {
        var $row = $(this).closest('tr');
        var data = customDataTable.row($row).data();
        var rowId = data[0];
        var index = $.inArray(rowId, rows_selected_custom);
        if (this.checked && index === -1) {
            rows_selected_custom.push(rowId);
        } else if (!this.checked && index !== -1){
            rows_selected_custom.splice(index, 1);
        }
        if (this.checked) $row.addClass('selected');
        else $row.removeClass('selected');

        e.stopPropagation();
    });
    $(tableSelector).on('click', 'thead th:first-child, tbody td:first-child', function(e){
        $(this).parent().find('input[type="checkbox"]').trigger('click');
    });

    $('thead input[name="select_all"]', customDataTable.table().container()).on('click', function(e){
        if (this.checked){
            $(tableSelector + ' tbody input[type="checkbox"]:not(:checked)').trigger('click');
        } else {
            $(tableSelector + ' tbody input[type="checkbox"]:checked').trigger('click');
        }
        e.stopPropagation();
    });
}

function populateStandardDataTable(tableSelector) {
    standardDataTable = $(tableSelector).DataTable();

    $(tableSelector + ' tbody').on('click', 'input[type="checkbox"]', function(e) {
        var $row = $(this).closest('tr');
        var data = standardDataTable.row($row).data();
        var rowId = data[0];        
        var index = $.inArray(rowId, rows_selected_standard);
        if (this.checked && index === -1) {
            rows_selected_standard.push(rowId);
        } else if (!this.checked && index !== -1){
            rows_selected_standard.splice(index, 1);
        }
        if (this.checked) $row.addClass('selected');
        else $row.removeClass('selected');

        e.stopPropagation();
    });
    $(tableSelector).on('click', 'thead th:first-child, tbody td:first-child', function(e){
        $(this).parent().find('input[type="checkbox"]').trigger('click');
    });

    $('thead input[name="select_all"]', standardDataTable.table().container()).on('click', function(e){
        if (this.checked){
            $(tableSelector + ' tbody input[type="checkbox"]:not(:checked)').trigger('click');
        } else {
            $(tableSelector + ' tbody input[type="checkbox"]:checked').trigger('click');
        }
        e.stopPropagation();
    });
}

function getSelectedRowData(table) {
    return $(table.rows('.selected').data()).map(function(item, value) {
        return $(value[0]).attr('id').split('chk1_')[1] + '$-$' + value[1] + '$-$' + $('#' + $(value[2]).attr('id')).val() + '$-$' + $('#' + $(value[2]).attr('id') + ' option:selected').text();
    }).get().join(":_:");
}

function getSelectedRowFromCustomDataTable(table) {
    return $(table.rows('.selected').data()).map(function(item, value) {
        return $(value[0]).attr('id').split('chk1_')[1]  + '$-$' + value[1] + '$-$' + value[2] + '$-$' + $('#' + $(value[3]).attr('id')).val() + '$-$' + $('#' + $(value[3]).attr('id') + ' option:selected').text();
    }).get().join(":_:");
}

function saveState(forwardaction) {
    document.customSecurityForm["value(forward)"].value = forwardaction;
	if(undefined != document.customSecurityForm.selectedSCAPOption) {
		var optionSelected = document.customSecurityForm.selectedSCAPOption.value;
		if ('customize' == optionSelected) {
			document.customSecurityForm["value(selectedScapAndProfilesCustom)"].value = getSelectedRowFromCustomDataTable(customDataTable);
		} else if ('standard' == optionSelected) {
			document.customSecurityForm["value(selectedScapAndProfilesCustom)"].value = getSelectedRowData(standardDataTable);
		}
	}
    send(document.customSecurityForm, '/customSecuritySave.do');
}
</script>

</body>
</html>

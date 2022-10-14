<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Tamilselvan Teivasekamani
	@version  $Revision$,  $Date$
--%>

<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ page import="com.marimba.apps.subscription.common.util.LDAPUtils" %>
<%@ page import="com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%@ page import="com.marimba.apps.subscription.common.intf.IUser" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%@ page import="java.util.Map" %>
<link rel="stylesheet" href="/sm/includes/errorMessage.css" type="text/css" />
<style type="text/css">
    div.transparentCover {
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        display: none;
        z-index: 1000;
        position: absolute;
        background-color: #ffffff;
        filter: alpha( opacity = 45 );
        -moz-opacity: 0.45;
        opacity: 0.45;
    }

    div.ReportEditArea {
        width: 550px;
        display: none;
        z-index: 1000;
        position: absolute;
    }

    .dialogcontent {
        background: #ffffff;
        border-right: 1px solid #000000;
        border-left: 1px solid #000000;
        padding-bottom: 2px;
    }

    .dialogheadh {
        font-size: 11px;
        font-weight: bold;
        border-right: 1px solid #0099cc;
        border-left: 1px solid #0099cc;
    }

    .corner_Top_1, .corner_Top_2, .corner_Top_3, .corner_Top_4, .corner_Btm_1, .corner_Btm_2, .corner_Btm_3, .corner_Btm_4 {
        overflow: hidden;
        display: block;
    }

    .corner_Top_1 {
        height: 1px;
        background: #0099cc;
        margin: 0 5px;
    }

    .corner_Btm_1 {
        height: 1px;
        background: #000000;
        margin: 0 5px;
    }

    .corner_Top_2, .corner_Btm_2 {
        height: 1px;
        border-right: 2px solid #000000;
        border-left: 2px solid #000000;
        margin: 0 3px;
    }

    .corner_Top_3, .corner_Btm_3 {
        height: 1px;
        border-right: 1px solid #000000;
        border-left: 1px solid #000000;
        margin: 0 2px;
    }

    .corner_Top_4, .corner_Btm_4 {
        height: 2px;
        border-right: 1px solid #000000;
        border-left: 1px solid #000000;
        margin: 0 1px;
    }

    .corner_Btm_2, .corner_Btm_3, .corner_Btm_4 {
        background: #ffffff;
    }

    .corner_Top_2, .corner_Top_3, .corner_Top_4, .dialogheadh {
        border-left: 1px solid #0099cc;
        border-right: 1px solid #0099cc;
        background: #00418C url( /shell/common-rsrc/images/header.gif ) repeat-x top;
    }
   .errorField {
	  border-color: red;
	}

</style>
<script language="JavaScript" src="/sm/includes/validation.js"></script>
<%
	ServletContext servContext = config.getServletContext();
	IUserPrincipal userPrincipal = (IUserPrincipal) request.getUserPrincipal();
    SubscriptionMain subsMain = TenantHelper.getTenantSubMain(servContext, request.getSession(), userPrincipal.getTenantName());
    Map<String, String> LDAPVarsMap = subsMain.getLDAPVarsMap();
%>
<script type="text/javascript">

var errMessage = ["<webapps:text key='page.adv_policy_search.invaid_date' />", "<webapps:text key='page.adv_policy_search.invalid_month' />", "<webapps:text key='page.adv_policy_search.invalid_day' />",
        		  "<webapps:text key='page.adv_policy_search.invalid_year' />", "<webapps:text key='page.adv_policy_search.required' />", "<webapps:text key='page.advanced_package_search.attribute_empty' />"];
var delete_img = "/sm/images/delete_btn.png";
var queryDelim = '&#;';
var spacer = '\u00a0';
var rowColor1 = 'alternateRowColor';
// Display strings for package state
var stage = "<webapps:pageText key='subscribe_noinstall.uppercase' type='global' />";
var exclude = "<webapps:pageText key='exclude.uppercase' type='global' />";
var install = "<webapps:pageText key='subscribe.uppercase' type='global' />";
var primary = "<webapps:pageText key='primary.uppercase' type='global' />";
var uninstall = "<webapps:pageText key='delete.uppercase' type='global' />";
var advertise = "<webapps:pageText key='available.uppercase' type='global' />";
var install_start = "<webapps:pageText key='subscribe_start.uppercase' type='global' />";
var install_persist = "<webapps:pageText key='subscribe_persist.uppercase' type='global' />";
var install_start_persist = "<webapps:pageText key='start_persist.uppercase' type='global' />";

var lbl_activation = "<webapps:text key='page.schedule.activation' />";
var lbl_expiration = "<webapps:text key='page.schedule.expiration' />";

// List item values for packages
var val_stage = '<%=ISubscriptionConstants.STATE_SUBSCRIBE_NOINSTALL%>';
var val_exclude = '<%=ISubscriptionConstants.STATE_EXCLUDE%>';
var val_install = '<%=ISubscriptionConstants.STATE_SUBSCRIBE%>';
var val_primary = '<%=ISubscriptionConstants.STATE_PRIMARY%>';
var val_uninstall = '<%=ISubscriptionConstants.STATE_DELETE%>';
var val_advertise = '<%=ISubscriptionConstants.STATE_AVAILABLE%>';
var val_install_start = '<%=ISubscriptionConstants.STATE_SUBSCRIBE_START%>';
var val_install_persist = '<%=ISubscriptionConstants.STATE_SUBSCRIBE_PERSIST%>';
var val_install_start_persist = '<%=ISubscriptionConstants.STATE_START_PERSIST%>';

// Listbox display strings for Patches
var patch_assign = "<webapps:pageText key='subscribe.patch.uppercase' type='global' />";
var patch_exclude = "<webapps:pageText key='exclude.patch.uppercase' type='global' />";

// Listbox item values for patches
var val_patch_assign = '<%=ISubscriptionConstants.STATE_SUBSCRIBE%>';
var val_patch_exclude = '<%=ISubscriptionConstants.STATE_EXCLUDE%>';

var after = "<webapps:text key='page.advanced_package_search.after' />";
var before = "<webapps:text key='page.advanced_package_search.before' />";

// Listbox dispaly items for Channel name
var contains = "<webapps:text key='page.advanced_package_search.contains' />";
var startswith = "<webapps:text key='page.advanced_package_search.startswith' />";
var endswith = "<webapps:text key='page.advanced_package_search.endswith' />";
var equalsto = "<webapps:text key='page.advanced_package_search.equalsto' />";

// Listbox display items for enable/disable
var enable = "<webapps:text key='page.advanced_package_search.enable' />";
var disable = "<webapps:text key='page.advanced_package_search.disable' />";

String.prototype.endsWith = function(str) {
    return (this.match(str+"$")==str)
}

// Method which is invoke the glasspane, if the browser doesn't support, shows a alert box
function invokeDiv() {
    if (document.getElementById && document.createElement) {
        document.getElementById('transparentCover').style.display = 'block';
        var editableArea = document.getElementById('searchArea');
        editableArea.style.display = 'block';
        editableArea.style.top = "29%";
        editableArea.style.left = "27%";
    }
    else alert('Your browser doesn\'t support the Level 1 DOM');
}

// Close the glasspane
function closeGlassPane() {
    document.getElementById('searchArea').style.display = '';
    document.getElementById('transparentCover').style.display = '';
}

// Call by Add button for creating an attribute.
function addAttribute() {
    var index_val = document.getElementById('selected_att').selectedIndex;
    constructRows(document.getElementById('selected_att').options[index_val].value);
}

// Construct the dynamic rows with elements depending on the attribute selected
function constructRows(selection) {
    var rowCount = table.rows.length;
    if ('name' == selection && isAlreadyExist('chnl_name')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        createChnlNameElement(row, rowCount);
    } else if ('blackout' == selection && isAlreadyExist('blkout')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        createBlkoutElement(row, rowCount);
    } else if ('wow' == selection && isAlreadyExist('wow_option')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        createWOWElement(row, rowCount);
    } else if ('pkg_pri_state' == selection && isAlreadyExist('pkg_primary_state')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        packPrimaryState(row, rowCount);
    } else if ('pkg_sec_state' == selection && isAlreadyExist('pkg_secondary_state')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        packSecondaryState(row, rowCount);
    } else if ('patch_state' == selection && isAlreadyExist('patch_state')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        patchState(row, rowCount);
    } else if ('remedy_state' == selection && isAlreadyExist('remedi_state')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        remediationState(row, rowCount);
    } else if ('primary_sch' == selection && isAlreadyExist('prim_sche_act')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        primarySchedule(row, rowCount);
    } else if ('secondary_sch' == selection && isAlreadyExist('sec_sche_act')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        secondarySchedule(row, rowCount);
    } else if ('update_sch' == selection && isAlreadyExist('update_sche_act')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        updateSchedule(row, rowCount);
    } else if ('verify_sch' == selection && isAlreadyExist('verify_sche_act')) {
        var row = table.insertRow(rowCount);
        row.className = (rowCount % 2) == 1 ? rowColor1 : '';
        verifySchedule(row, rowCount);
    }
}

// Create and insert the row which is containing Channel name attributes
function createChnlNameElement(row, rowIndex) {
    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.channelname'/> :   ");
    var select_elmt = document.createElement('Select');
    var input_elmt = document.createElement('input');
    var space = document.createTextNode(spacer);
    input_elmt.type = 'text';
    select_elmt.setAttribute('id', 'chnl_name');
    input_elmt.setAttribute('id', 'txt_chnl_name');
    var img_del_row = document.createElement('img');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };
    cell_0.appendChild(newText);
    cell_1.appendChild(select_elmt);
    cell_1.appendChild(space);
    cell_1.appendChild(input_elmt);
    cell_2.appendChild(img_del_row);

    var selected_val = document.getElementById('chnl_name');

    selected_val.options[0] = new Option(contains, 'contains');
    selected_val.options[1] = new Option(startswith, 'starts');
    selected_val.options[2] = new Option(endswith, 'ends');
    selected_val.options[3] = new Option(equalsto, 'equals');
}

// Create and insert the row which is containing primary state attributes
function packPrimaryState(row, rowIndex) {
    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.pkgPrimaryState'/> :   ");
    var element1 = document.createElement('Select');
    var img_del_row = document.createElement('img');
    element1.setAttribute('id', 'pkg_primary_state');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };
    cell_0.appendChild(newText);
    cell_1.appendChild(element1);
    cell_2.appendChild(img_del_row);

    var selected_val = document.getElementById('pkg_primary_state');

    selected_val.options[0] = new Option(stage, val_stage);
    selected_val.options[1] = new Option(advertise, val_advertise);
    selected_val.options[2] = new Option(install, val_install);
    selected_val.options[3] = new Option(install_start, val_install_start);
    selected_val.options[4] = new Option(install_persist, val_install_persist);
    selected_val.options[5] = new Option(install_start_persist, val_install_start_persist);
    selected_val.options[6] = new Option(exclude, val_exclude);
    selected_val.options[7] = new Option(uninstall, val_uninstall);
    selected_val.options[8] = new Option(primary, val_primary);
}

// Create and insert the row which is containing secondary state attributes
function packSecondaryState(row, rowIndex) {

    // create cell's(ie, <TD>)
    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.pkgSecondaryState'/> :   ");
    var element1 = document.createElement('Select');
    var img_del_row = document.createElement('img');
    element1.setAttribute('id', 'pkg_secondary_state');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };
    cell_0.appendChild(newText);
    cell_1.appendChild(element1);
    cell_2.appendChild(img_del_row);

    var selected_val = document.getElementById('pkg_secondary_state');

    selected_val.options[0] = new Option(stage, val_stage);
    selected_val.options[1] = new Option(advertise, val_advertise);
    selected_val.options[2] = new Option(install, val_install);
    selected_val.options[3] = new Option(install_start, val_install_start);
    selected_val.options[4] = new Option(install_persist, val_install_persist);
    selected_val.options[5] = new Option(install_start_persist, val_install_start_persist);
    selected_val.options[6] = new Option(exclude, val_exclude);
    selected_val.options[7] = new Option(uninstall, val_uninstall);
    selected_val.options[8] = new Option(primary, val_primary);
}

// Create and insert the row which is containing patch state attributes
function patchState(row, rowIndex) {

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.patchGroupState'/> :   ");
    var element1 = document.createElement('Select');
    var img_del_row = document.createElement('img');
    element1.setAttribute('id', 'patch_state');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };
    cell_0.appendChild(newText);
    cell_1.appendChild(element1);
    cell_2.appendChild(img_del_row);

    var selected_val = document.getElementById('patch_state');

    selected_val.options[0] = new Option(patch_assign, val_patch_assign);
    selected_val.options[1] = new Option(patch_exclude, val_exclude);
}

// Create and insert the row which is containing remediation state attributes
function remediationState(row, rowIndex) {

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.remediationState'/> :   ");
    var element1 = document.createElement('Select');
    var img_del_row = document.createElement('img');
    var space = document.createTextNode(spacer);
    element1.setAttribute('id', 'remedi_state');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };
    cell_0.appendChild(newText);
    cell_1.appendChild(element1);
    cell_2.appendChild(img_del_row);

    var selected_val = document.getElementById('remedi_state');

    selected_val.options[0] = new Option(patch_assign, val_patch_assign);
    selected_val.options[1] = new Option(patch_exclude, val_patch_exclude);
}

// Create and insert the row which is containing WOWElement attributes
function createWOWElement(row, rowIndex) {

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.wow'/> :   ");
    var element2 = document.createElement('Select');
    var img_del_row = document.createElement('img');
    element2.setAttribute('id', 'wow_option');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };
    cell_0.appendChild(newText);
    cell_1.appendChild(element2);
    cell_2.appendChild(img_del_row);

    var selected_val = document.getElementById('wow_option');

    selected_val.options[0] = new Option(enable, 'enabled');
    selected_val.options[1] = new Option(disable, 'disabled');

}

// Create and insert the row which is containing blackout attributes
function createBlkoutElement(row, rowIndex) {

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.blackout'/> :   ");
    var element1 = document.createElement('Select');
    var img_del_row = document.createElement('img');
    element1.setAttribute('id', 'blkout');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };
    cell_0.appendChild(newText);
    cell_1.appendChild(element1);
    cell_2.appendChild(img_del_row);

    var selected_val = document.getElementById('blkout');

    selected_val.options[0] = new Option(enable, 'enabled');
    selected_val.options[1] = new Option(disable, 'disabled');
}

// Create and insert the row which is containing primaryschedule attributes
function primarySchedule(row, rowIndex) {
    var getSelectedValue = function() {
        var index_val = document.getElementById('prim_sche_act').selectedIndex;
    }

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.primarySchedule'/> :   ");

    var lbl_box_act = document.createTextNode(lbl_activation);
    var pri_act_range = document.createElement('Select');
    var pri_act_date = document.createElement('input');
    var newLine = document.createElement('br');

    var lbl_box_Exp = document.createTextNode(lbl_expiration);
    var pri_exp_range = document.createElement('Select');
    var pri_exp_date = document.createElement('input');

    var img_del_row = document.createElement('img');
    var space = document.createTextNode(spacer);

    pri_act_date.type = 'text';
    pri_act_date.setAttribute('id', 'txt_pri_act_date');
    pri_exp_date.type = 'text';
    pri_exp_date.setAttribute('id', 'txt_pri_exp_date');

    pri_act_range.setAttribute('id', 'prim_sche_act');
    pri_exp_range.setAttribute('id', 'prim_sche_exp');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };

    cell_0.appendChild(newText);
    cell_1.appendChild(lbl_box_act);
    cell_1.appendChild(space);
    cell_1.appendChild(pri_act_range);
    cell_1.appendChild(space);
    cell_1.appendChild(pri_act_date);
    cell_1.appendChild(newLine);
    cell_1.appendChild(lbl_box_Exp);
    cell_1.appendChild(space);
    cell_1.appendChild(pri_exp_range);
    cell_1.appendChild(space);
    cell_1.appendChild(pri_exp_date);
    cell_1.appendChild(space);
    cell_2.appendChild(img_del_row);

    // Add options to the list box
    var pri_act_val = document.getElementById('prim_sche_act');
    pri_act_val.options[0] = new Option(after, 'after');
    pri_act_val.options[1] = new Option(before, 'before');

    var pri_exp_val = document.getElementById('prim_sche_exp');
    pri_exp_val.options[0] = new Option(after, 'after');
    pri_exp_val.options[1] = new Option(before, 'before');
}

// Create and insert the row which is containing secondaryschedule attributes
function secondarySchedule(row, rowIndex) {
    var getSelectedValue = function() {
        var index_val = document.getElementById('sec_sche_act').selectedIndex;
    }

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.secondarySchedule'/> :   ");

    var lbl_box_act = document.createTextNode(lbl_activation);
    var sec_act_range = document.createElement('Select');
    var sec_act_date = document.createElement('input');
    var newLine = document.createElement('br');

    var lbl_box_Exp = document.createTextNode(lbl_expiration);
    var sec_exp_range = document.createElement('Select');
    var sec_exp_date = document.createElement('input');

    var img_del_row = document.createElement('img');
    var space = document.createTextNode(spacer);

    sec_act_date.type = 'text';
    sec_act_date.setAttribute('id', 'txt_sec_act_date');
    sec_exp_date.type = 'text';
    sec_exp_date.setAttribute('id', 'txt_sec_exp_date');

    sec_act_range.setAttribute('id', 'sec_sche_act');
    sec_exp_range.setAttribute('id', 'sec_sche_exp');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };

    cell_0.appendChild(newText);
    cell_1.appendChild(lbl_box_act);
    cell_1.appendChild(space);
    cell_1.appendChild(sec_act_range);
    cell_1.appendChild(space);
    cell_1.appendChild(sec_act_date);
    cell_1.appendChild(newLine);
    cell_1.appendChild(lbl_box_Exp);
    cell_1.appendChild(space);
    cell_1.appendChild(sec_exp_range);
    cell_1.appendChild(space);
    cell_1.appendChild(sec_exp_date);
    cell_1.appendChild(space);
    cell_2.appendChild(img_del_row);

    // Add options to the list box
    var sec_act_val = document.getElementById('sec_sche_act');
    sec_act_val.options[0] = new Option(after, 'after');
    sec_act_val.options[1] = new Option(before, 'before');

    var sec_exp_val = document.getElementById('sec_sche_exp');
    sec_exp_val.options[0] = new Option(after, 'after');
    sec_exp_val.options[1] = new Option(before, 'before');
}

// Create and insert the row which is containing updateschedule attributes
function updateSchedule(row, rowIndex) {
    var getSelectedValue = function() {
        var index_val = document.getElementById('update_sche_act').selectedIndex;
    }

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.updateSchedule'/> :   ");

    var lbl_box_act = document.createTextNode(lbl_activation);
    var update_act_range = document.createElement('Select');
    var update_act_date = document.createElement('input');
    var newLine = document.createElement('br');

    var lbl_box_Exp = document.createTextNode(lbl_expiration);
    var update_exp_range = document.createElement('Select');
    var update_exp_date = document.createElement('input');

    var img_del_row = document.createElement('img');
    var space = document.createTextNode(spacer);

    update_act_date.type = 'text';
    update_act_date.setAttribute('id', 'txt_update_act_date');
    update_exp_date.type = 'text';
    update_exp_date.setAttribute('id', 'txt_update_exp_date');

    update_act_range.setAttribute('id', 'update_sche_act');
    update_exp_range.setAttribute('id', 'update_sche_exp');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };

    cell_0.appendChild(newText);
    cell_1.appendChild(lbl_box_act);
    cell_1.appendChild(space);
    cell_1.appendChild(update_act_range);
    cell_1.appendChild(space);
    cell_1.appendChild(update_act_date);
    cell_1.appendChild(newLine);
    cell_1.appendChild(lbl_box_Exp);
    cell_1.appendChild(space);
    cell_1.appendChild(update_exp_range);
    cell_1.appendChild(space);
    cell_1.appendChild(update_exp_date);
    cell_1.appendChild(space);
    cell_2.appendChild(img_del_row);

    // Add options to the list box
    var update_act_val = document.getElementById('update_sche_act');
    update_act_val.options[0] = new Option(after, 'after');
    update_act_val.options[1] = new Option(before, 'before');

    var update_exp_val = document.getElementById('update_sche_exp');
    update_exp_val.options[0] = new Option(after, 'after');
    update_exp_val.options[1] = new Option(before, 'before');
}

// Create and insert the row which is containing verifyschedule attributes
function verifySchedule(row, rowIndex) {
    var getSelectedValue = function() {
        var index_val = document.getElementById('verify_sche_act').selectedIndex;
    }

    var cell_0 = row.insertCell(0);
    var cell_1 = row.insertCell(1);
    var cell_2 = row.insertCell(2);

    var newText = document.createTextNode("<webapps:text key='page.advanced_package_search.verifySchedule'/> :   ");

    var lbl_box_act = document.createTextNode(lbl_activation);
    var verify_act_range = document.createElement('Select');
    var verify_act_date = document.createElement('input');
    var newLine = document.createElement('br');

    var lbl_box_Exp = document.createTextNode(lbl_expiration);
    var verify_exp_range = document.createElement('Select');
    var verify_exp_date = document.createElement('input');

    var img_del_row = document.createElement('img');
    var space = document.createTextNode(spacer);

    verify_act_date.type = 'text';
    verify_act_date.setAttribute('id', 'txt_verify_act_date');
    verify_exp_date.type = 'text';
    verify_exp_date.setAttribute('id', 'txt_verify_exp_date');

    verify_act_range.setAttribute('id', 'verify_sche_act');
    verify_exp_range.setAttribute('id', 'verify_sche_exp');
    img_del_row.setAttribute('src', delete_img);
    img_del_row.onclick = function () { deleteCurrentRow(this); };

    cell_0.appendChild(newText);
    cell_1.appendChild(lbl_box_act);
    cell_1.appendChild(space);
    cell_1.appendChild(verify_act_range);
    cell_1.appendChild(space);
    cell_1.appendChild(verify_act_date);
    cell_1.appendChild(newLine);
    cell_1.appendChild(lbl_box_Exp);
    cell_1.appendChild(space);
    cell_1.appendChild(verify_exp_range);
    cell_1.appendChild(space);
    cell_1.appendChild(verify_exp_date);
    cell_1.appendChild(space);
    cell_2.appendChild(img_del_row);

    // Add options to the list box
    var verify_act_val = document.getElementById('verify_sche_act');
    verify_act_val.options[0] = new Option(after, 'after');
    verify_act_val.options[1] = new Option(before, 'before');

    var verify_exp_val = document.getElementById('verify_sche_exp');
    verify_exp_val.options[0] = new Option(after, 'after');
    verify_exp_val.options[1] = new Option(before, 'before');
}

// Call by Reset button for clear the added attributes
function resetTable() {
	clearErrorStyle();
    var table_del = document.getElementById('edit_tbl');
    var rowCount_del = table_del.rows.length - 1;
    while (rowCount_del >= 0) {
        table_del.deleteRow(rowCount_del);
        rowCount_del--;
    }
}

// To delete the current row while perform delete operation
function deleteCurrentRow(obj) {
    var delRow = obj.parentNode.parentNode;
    var tbl = delRow.parentNode.parentNode;
    var rIndex = delRow.sectionRowIndex;
    var rowArray = new Array(delRow);
    for (var i = 0; i < rowArray.length; i++) {
        var rowIndex = rowArray[i].sectionRowIndex;
        rowArray[i].parentNode.deleteRow(rIndex);
    }
    reorderRows(tbl, rIndex);
}

// Re-arrange the row's color with alternative after deleted a single row
function reorderRows(tbl, startingIndex) {
    if (tbl.rows[startingIndex]) {
        var count = startingIndex + 1;
        for (var i = startingIndex; i < tbl.rows.length; i++) {
            tbl.rows[i].className = ((count-1) % 2) == 1 ? rowColor1 : '';
            count++;
        }
    }
}

function isAlreadyExist(elementID) {
    return null == document.getElementById(elementID);
}
var errMsg;
function validateForm(){
	errMsg = "<ul>";
	clearErrorStyle();
	var validSearch = false;
	if (!isAlreadyExist('chnl_name')) {
		if(isBlank(document.getElementById('txt_chnl_name').value)){
			errMsg = errMsg + "<li>" + errMessage[5] + "</li>";
			document.getElementById('txt_chnl_name').className = "errorField";
		}
		validSearch = true;
	}
	if (!isAlreadyExist('prim_sche_act')) {
		validateSchedule(document.getElementById('txt_pri_act_date'),document.getElementById('txt_pri_exp_date'));
		validSearch = true;
	}
	if (!isAlreadyExist('sec_sche_act')) {
		validateSchedule(document.getElementById('txt_sec_act_date'),document.getElementById('txt_sec_exp_date'));
		validSearch = true;
	}
	if (!isAlreadyExist('update_sche_act')) {
		validateSchedule(document.getElementById('txt_update_act_date'),document.getElementById('txt_update_exp_date'));
		validSearch = true;
	}
	if (!isAlreadyExist('verify_sche_act')) {
		validateSchedule(document.getElementById('txt_verify_act_date'),document.getElementById('txt_verify_exp_date'));
		validSearch = true;
	}
	if(errMsg == "<ul>" && !validSearch){
		if(isAlreadyExist('blkout') && isAlreadyExist('wow_option') && isAlreadyExist('pkg_primary_state') &&
			isAlreadyExist('pkg_secondary_state') && isAlreadyExist('patch_state') && isAlreadyExist('remedi_state'))
			errMsg = errMsg + "<li>" + errMessage[4] + "</li>";
	}
	return errMsg;
}

function validateSchedule(actDate,expDate){
	if(isBlank(actDate.value) && isBlank(expDate.value)){
		errMsg = errMsg + "<li>" + errMessage[5] + "</li>";
		actDate.className = "errorField";
		expDate.className = "errorField";
	}else{
		var res;
		if(!isBlank(actDate.value)){
			res = isValidDate(actDate.value);
			if (res != -1){
				errMsg = errMsg + "<li>" + errMessage[res] + "</li>";
				actDate.className = "errorField";
			}
		}
		if(!isBlank(expDate.value)){
			res = isValidDate(expDate.value);
			if (res != -1){
				errMsg = errMsg + "<li>" + errMessage[res] + "</li>";
				expDate.className = "errorField";
			}
		}
	}
}

function clearErrorStyle(){
	if (!isAlreadyExist('chnl_name')) {
		document.getElementById('txt_chnl_name').className = "";
	}
	if (!isAlreadyExist('prim_sche_act')) {
		document.getElementById('txt_pri_act_date').className = "";
		document.getElementById('txt_pri_exp_date').className = "";
	}
	if (!isAlreadyExist('sec_sche_act')) {
		document.getElementById('txt_sec_act_date').className = "";
		document.getElementById('txt_sec_exp_date').className = "";
	}
	if (!isAlreadyExist('update_sche_act')) {
		document.getElementById('txt_update_act_date').className = "";
		document.getElementById('txt_update_exp_date').className = "";
	}
	if (!isAlreadyExist('verify_sche_act')) {
		document.getElementById('txt_verify_act_date').className = "";
		document.getElementById('txt_verify_exp_date').className = "";
	}
	document.getElementById('critical').innerHTML = "";
	document.getElementById('critical').className = "";
}

function cancel(){
	clearErrorStyle();
	resetTable();
	closeGlassPane();
}
// Call by search button
function searchPackage() {
	var errMsg = validateForm();
	if(errMsg == "<ul>"){
		clearErrorStyle();
		closeGlassPane();
		parent.sendURL(getAllSearchAttributes());
	}else{
		document.getElementById('critical').className = "errorMessage";
        document.getElementById('critical').innerHTML = "<h6><webapps:text key='page.main_view.errmesssage' /></h6><p>" + errMsg + "</ul></p>";
    }

}

// Get all selected attribute values through it's ID and formating query
function getAllSearchAttributes() {
    var contentType = '';
    var adv_Attributes = '';
    var searchInText = "";
    var value = "";

    if (!isAlreadyExist('chnl_name')) {
        if ('equals' == getSelectedIndexValue('chnl_name')) {
            value = document.getElementById('txt_chnl_name').value;
            adv_Attributes = '<%=LDAPVarsMap.get("CHANNELTITLE")%>=' + 'equl' + value;
            searchInText = "Channel Name equals to " + value + " AND ";
        } else if('starts' == getSelectedIndexValue('chnl_name')) {
            value = document.getElementById('txt_chnl_name').value;
            adv_Attributes = '<%=LDAPVarsMap.get("CHANNELTITLE")%>=' + 'strt' + value;
            searchInText = "Channel Name starts with " + value + " AND ";
        }  else if('ends' == getSelectedIndexValue('chnl_name')) {
            value = document.getElementById('txt_chnl_name').value;
            adv_Attributes = '<%=LDAPVarsMap.get("CHANNELTITLE")%>=' + 'ends' + value;
            searchInText = "Channel Name ends with " + value + " AND ";
        } else {
            value = document.getElementById('txt_chnl_name').value
            adv_Attributes = '<%=LDAPVarsMap.get("CHANNELTITLE")%>=' + 'cntn' + value;
            searchInText = "Channel Name contains " + value + " AND ";
        }
        adv_Attributes = queryDelim + adv_Attributes;
    }
    if (!isAlreadyExist('pkg_primary_state')) {
        value = getSelectedIndexValue('pkg_primary_state');
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNEL")%>=' + value;
        contentType += "contenttype.application,";
        searchInText += "Primary State as " + value + " AND ";
    }
    if (!isAlreadyExist('pkg_secondary_state')) {
        value = getSelectedIndexValue('pkg_secondary_state');
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNELSEC")%>=' + value;
        searchInText += "Secondary State as " + value + " AND ";
    }
    if (!isAlreadyExist('patch_state')) {
        value = getSelectedIndexValue('patch_state');
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNEL")%>=' + value;
        contentType += "contenttype.patchgroup,";
        searchInText += "Patch State as " + value + " AND ";
    }

    if (!isAlreadyExist('wow_option')) {
        value = getSelectedIndexValue('wow_option');
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNELWOWENABLED")%>=' + value;
        searchInText += "WoW is " + value + " AND ";
    }
    if (!isAlreadyExist('blkout')) {
        value = getSelectedIndexValue('blkout');
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT")%>=' + value;
        searchInText += "Exempt from blackout is " + value + " AND ";
    }
    if (!isAlreadyExist('prim_sche_act')) {
        var pri_act_range = getSelectedIndexValue('prim_sche_act');
        var pri_act_date = document.getElementById('txt_pri_act_date').value;
        var pri_exp_range = getSelectedIndexValue('prim_sche_exp');
        var pri_exp_date = document.getElementById('txt_pri_exp_date').value;

        var final_value = pri_act_range + ' ' + pri_act_date + '-' + pri_exp_range + ' ' + pri_exp_date;
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNELINITSCHED")%>=' + final_value;
        if (pri_act_date != null && pri_act_date != "") {
            searchInText += "Primary Schedule Activation " + pri_act_range + ' ' + pri_act_date + " AND ";
        }
        if (pri_exp_date != null && pri_exp_date != "") {
            searchInText += "Primary Schedule Expiration " + pri_exp_range + ' ' + pri_exp_date + " AND ";
        }
    }
    if (!isAlreadyExist('sec_sche_act')) {
        var sec_act_range = getSelectedIndexValue('sec_sche_act');
        var sec_act_date = document.getElementById('txt_sec_act_date').value;
        var sec_exp_range = getSelectedIndexValue('sec_sche_exp');
        var sec_exp_date = document.getElementById('txt_sec_exp_date').value;

        var final_value = sec_act_range + ' ' + sec_act_date + '-' + sec_exp_range + ' ' + sec_exp_date;
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNELSECSCHED")%>=' + final_value;
        if (sec_act_date != null && sec_act_date != "") {
            searchInText += "Primary Schedule Activation " + sec_act_range + ' ' + sec_act_date + " AND ";
        }
        if (sec_exp_date != null && sec_exp_date != "") {
            searchInText += "Primary Schedule Expiration " + sec_exp_range + ' ' + sec_exp_date + " AND ";
        }
    }
    if (!isAlreadyExist('update_sche_act')) {
        var update_act_range = getSelectedIndexValue('update_sche_act');
        var update_act_date = document.getElementById('txt_update_act_date').value;
        var update_exp_range = getSelectedIndexValue('update_sche_exp');
        var update_exp_date = document.getElementById('txt_update_exp_date').value;

        var final_value = update_act_range + ' ' + update_act_date + '-' + update_exp_range + ' ' + update_exp_date;
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNELUPDATESCHED")%>=' + final_value;
        if (update_act_date != null && update_act_date != "") {
            searchInText += "Update Schedule Activation " + update_act_range + ' ' + update_act_date + " AND ";
        }
        if (update_exp_date != null && update_exp_date != "") {
            searchInText += "Update Schedule Expiration " + update_exp_range + ' ' + update_exp_date + " AND ";
        }
    }
    if (!isAlreadyExist('verify_sche_act')) {
        var verify_act_range = getSelectedIndexValue('verify_sche_act');
        var verify_act_date = document.getElementById('txt_verify_act_date').value;
        var verify_exp_range = getSelectedIndexValue('verify_sche_exp');
        var verify_exp_date = document.getElementById('txt_verify_exp_date').value;

        var final_value = verify_act_range + ' ' + verify_act_date + '-' + verify_exp_range + ' ' + verify_exp_date;
        adv_Attributes = adv_Attributes + queryDelim + '<%=LDAPVarsMap.get("CHANNELVERREPAIRSCHED")%>=' + final_value;
        if (verify_act_date != null && verify_act_date != "") {
            searchInText += "Verify Schedule Activation " + verify_act_range + ' ' + verify_act_date + " AND ";
        }
        if (verify_exp_date != null && verify_exp_date != "") {
            searchInText += "Verify Schedule Expiration " + verify_exp_range + ' ' + verify_exp_date + " AND ";
        }
    }
    // adding all the selected attributes and the search values along with the content type (package type)
    adv_Attributes = adv_Attributes + queryDelim + "contentType=" + contentType;

    // add the texts to be displayed in the textarea in the readable format.
    // this.match(suffix+"$") == suffix
    if (searchInText != null && searchInText.endsWith(" AND ")) {
        searchInText = searchInText.substr(0, searchInText.length-5);
    }
    adv_Attributes = adv_Attributes + "#$text%&" + searchInText;

    return (adv_Attributes);
}

// Common function for get the value from listbox
function getSelectedIndexValue(id) {
    var index_val = document.getElementById(id).selectedIndex;
    return document.getElementById(id).options[index_val].value;
}

</script>

<div id="transparentCover" class="transparentCover"></div>

<div id="searchArea" class="ReportEditArea">
    <b class="corner_Top_1"> </b>
    <b class="corner_Top_2"> </b>
    <b class="corner_Top_3"> </b>
    <b class="corner_Top_4"> </b>
    <div class="dialogheadh">
        <table width="99%">
            <tr>
                <td style="color:#ffffff;font-weight:bold;" valign="top"><webapps:text key="page.advanced_package_search.title"/></td>
                <td valign="top" align="right"><img src="/sm/images/close_top.png" onclick="cancel();"/></td>
            </tr>
        </table>
    </div>
    <div id="contentHolder" class="dialogcontent">
        <table width="99%" cellspacing="0" cellpadding="0" align="center">
            <tr><td colspan="3" id="critical"></td></tr>
            <tr class="tableTitle">
                <td style="padding-left:6px;" width="31%"><webapps:text key="page.advanced_package_search.addAttributes"/></td>
                <td><select id="selected_att">
                    <option value="name"><webapps:text key="page.advanced_package_search.channelname"/></option>
                    <option value="pkg_pri_state"><webapps:text key="page.advanced_package_search.pkgPrimaryState"/></option>
                    <option value="pkg_sec_state"><webapps:text key="page.advanced_package_search.pkgSecondaryState"/></option>
                    <option value="patch_state"><webapps:text key="page.advanced_package_search.patchGroupState"/></option>
                    <option value="remedy_state"><webapps:text key="page.advanced_package_search.remediationState"/></option>
                    <option value="wow"><webapps:text key="page.advanced_package_search.wow"/></option>
                    <option value="blackout"><webapps:text key="page.advanced_package_search.blackout"/></option>
                    <option value="primary_sch"><webapps:text key="page.advanced_package_search.primarySchedule"/></option>
                    <option value="secondary_sch"><webapps:text key="page.advanced_package_search.secondarySchedule"/></option>
                    <option value="update_sch"><webapps:text key="page.advanced_package_search.updateSchedule"/></option>
                    <option value="verify_sch"><webapps:text key="page.advanced_package_search.verifySchedule"/></option>
                </select>
                    &nbsp;&nbsp;&nbsp;<input type="button" value="<webapps:pageText shared="true" type="button" key="add"/>" onclick="addAttribute()"/>
                </td>
                <td align="right"><input type="button" value="<webapps:pageText shared="true" type="button" key="removeAll"/>" onclick="resetTable()"/> &nbsp;&nbsp;</td>

            </tr>
        </table>
        <!--Primary Table that the elements will be updated dynamically -->
        <table id="edit_tbl" width="99%" border="0" align="center" cellspacing="0" cellpadding="2">
            <colgroup width="30%">
                <col align="left" style="padding-left:10px;" />
            </colgroup>
            <colgroup width="65%"></colgroup>
            <colgroup width="0*"></colgroup>
        </table>
        <div id="pageNav">
            <input type="button" name="search" value="<bean:message key='page.package_nav.Search' />" onclick="searchPackage();">&nbsp;
            <input type="button" name="close" value="<webapps:pageText key='Cancel' type='global' />" onclick="cancel();">&nbsp;&nbsp;
        </div>
    </div>
    <!-- End of ContentHolder -->
    <b class="corner_Btm_4"> </b>
    <b class="corner_Btm_3"> </b>
    <b class="corner_Btm_2"> </b>
    <b class="corner_Btm_1"> </b>
</div>
<!-- End of searchArea -->

<script type="text/javascript">
    var table = document.getElementById('edit_tbl');
</script>

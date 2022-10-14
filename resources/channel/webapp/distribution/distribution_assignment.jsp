<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%@ page import = "java.util.*,
                   com.marimba.apps.subscription.common.ISubscriptionConstants,
                   com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean,
                   com.marimba.apps.subscriptionmanager.webapp.actions.PersistifyChecksAction,
                   com.marimba.apps.subscriptionmanager.SubscriptionMain,
                   com.marimba.webapps.intf.IWebAppsConstants,
                   com.marimba.tools.ldap.LDAPConnUtils,
                   com.marimba.apps.subscription.common.intf.IUser,
                   org.apache.struts.util.MessageResources" %>
<%@ page buffer="none" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Channel" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import = "com.marimba.webapps.intf.IMapProperty" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<jsp:useBean id="session_dist" class="com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean" scope="session"/>
<%
    HttpSession sess = request.getSession();
    ServletContext context = config.getServletContext();
    IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
    SubscriptionMain main = TenantHelper.getTenantSubMain(context, sess, user.getTenantName());
    MessageResources resources = main.getAppResources();
    Locale locale = request.getLocale();
    String NA = resources.getMessage(locale,"page.distribution_assignment.NA");
    boolean isWoWApplicable = main.isWoWApplicable();
%>

<%@ include file="/includes/startHeadSection.jsp" %>
<script type="text/javascript" src="/spm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/spm/includes/jquery-1.8.2.min.js"></script>
<webapps:helpContext context="spm" topic="pol_asgn" />

<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/default/easyui.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/themes/icon.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/easyui/easyui-bmc-custom.css">
<link rel="stylesheet" type="text/css" href="/spm/includes/errorMessage.css"/>

<script type="text/javascript" src="/spm/includes/easyui/jquery.easyui.min.js"></script>

<script language="javascript" type="text/javascript">
var selectedChnlsArray = new Array();

<%--
The following operation are performed in the following js function
  1) Whenever the secondary state is null we make the secondary state select box empty
  and secondary schedule edit button to disabled state.
  2) when the user removes the secondary state, change the secondary schedule edit button to disabled state.

--%>

function stateChange(key, curSecState, init) {
    var stateObj;
    var secStateObj;
    var secStateBtnObj;
    var secStateIncObj1;
    var secStateIncObj2;

    if (hasDHTML()) {
        stateObj = document.getElementById("state#" + key);
        secStateObj =  document.getElementById("secState#" + key);
        secStateBtnObj = document.getElementById("secStateBtn#" + key);
        secStateIncObj1 =  document.getElementById("secStateInc_1#" + key);
        secStateIncObj2 =  document.getElementById("secStateInc_2#" + key);
    } else {
        stateObj = eval("document.distAsgForm.state#" + key);
        secStateObj = eval("document.distAsgForm.secState#" + key);
        secStateBtnObj = eval("document.distAsgForm.secStateBtn#" + key);
        secStateIncObj1 = eval("document.distAsgForm.secStateInc_1#" + key);
        secStateIncObj2 = eval("document.distAsgForm.secStateInc_2#" + key);
    }

    if (init) {
        // If this method is called when a page is intialized, we want to
        // set the secondary stage to blank as the default if
        // there isn't already a secondary state assigned (if there's
        // a value, the widget would have set it so we do nothing).
        if (curSecState == '') {
            secStateObj.value = '';
        }
    }
    if( secStateObj.value == '' ) {
        secStateBtnObj.disabled = true;
    } else {
        secStateBtnObj.disabled = false;
    }
    if(secStateIncObj1 != null && secStateIncObj2 != null) {
        secStateIncObj1.disabled = false;
    }
}
// This function saves the form attributes to the DistributionBean,
// before forwarding to the next page.
// IMPORTANT: Always use to navigate between pages so that the changes are
// persistified in the session bean.
function saveState(forwardaction) {
    document.distAsgForm.forward.value = forwardaction;
    send(document.distAsgForm, '/persistifyChecksOnDistAsg.do?/distSetStates.do');
}
var jsonrpc;
function resultCallback(result, e) {
    // alert("The server replied: " + result);
}

function changeWowBlkoutState(url, column, value) {
    try {
        // alert(url+" , " + column + " , "+ value);
        jsonrpc = new JSONRpcClient("/spm/JSON-RPC");
        jsonrpc.DistService.changeWowBlkoutState(resultCallback, url, column, ""+value);
    } catch(e) {
        alert(e);
    }
}
function reloadPage() {
    document.distAsgForm.forward.value = '/distInit.do?action=reload';
    send(document.distAsgForm, '/distInit.do?action=reload');
}

function pageSaveState(form, forwardaction) {
    document.distAsgForm.forward.value = forwardaction;
    send(document.distAsgForm, '/persistifyChecksOnDistAsg.do?/distSetStates.do');
}

function loadActionFromSelectionPkg(selectbox, form) {
    var url = selectbox.options[selectbox.selectedIndex].value;


    if (url.length == "") {
        return;
    }
    if (form == null) {
        var fullpath = "<html:rewrite page='" + url + "' />";
        top.location = fullpath;
    } else {
        pageSaveState(form, url);
    }
}
function onlyNumbers(evt) {
    var charCode = (evt.which) ? evt.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57) && charCode != 44)
        return false;
    return true;
}
</script>

<script type="text/javascript">
    var singleOptionElements = new Array("common_schedule_btn");
    var multiOptionElements = new Array("common_schedule_btn");


    function setClearAllFromHeader(isChecked) {
        if(isChecked) {
            document.distAsgForm['value(clear_all)'].value = 'false';
        } else {
            document.distAsgForm['value(clear_all)'].value = 'true';
            selectionCount = 0;
        }
        addAllToSelectedChnlsArray(isChecked);
    }

    function setClearAllFromBody() {
        if(selectionCount == 0) {
            document.distAsgForm['value(clear_all)'].value = 'true';
        } else {
            document.distAsgForm['value(clear_all)'].value = 'false';
        }
    }

    function addAllToSelectedChnlsArray(option) {
        $('input[id^=chnl_url_]').each(function(){
            addToSelectedChnlsArray($(this).val(), option, true);
        });
    }

    function addToSelectedChnlsArray(id, isChecked, isAllSelected) {
        if (isAllSelected) {
            if (isChecked) selectedChnlsArray.push(id);
            if (!isChecked) selectedChnlsArray = new Array();
        } else {
            id = id.split('dist_pagepkgs_item_')[1];
            id = 'chnl_url_' + id;
            var chnlUrlValue = document.getElementById(id).value;
            if (isChecked) {
                selectedChnlsArray.push(chnlUrlValue);
            } else {
                for (var x in selectedChnlsArray) {
                    if (selectedChnlsArray[x] == chnlUrlValue) {
                        selectedChnlsArray.splice(x, 1);
                    }
                }
            }
        }
        if (selectedChnlsArray.length > 0) {
            $('#ucd_btn').removeAttr("disabled");
        } else {
            $('#ucd_btn').attr("disabled", "disabled");
        }
    }

</script>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<logic:present name="taskid">

    <%-- Get the packages from the taglib that is already assigned for the target
         when the user invokes from the AR System.--%>
    <webapps:empty parameter="page">
        <sm:getPkgsFromTargets stateBean="distAsgForm" />
    </webapps:empty>

    <%-- Reset the distribution bean to includes the already assigned policy
         for the target when the user invokes from AR System.--%>
    <logic:present name="arTargetRefresh">
        <sm:setApplicationChannels sessionBean="session_dist" />
    </logic:present>

</logic:present>
<%-- Set up paging for pkgs. Session beans are set up in DistEditAction, DistEditFromPkgsAction, DistInitAsgAction
 --%>

<% //String pageBeanName = IWebAppConstants.SESSION_DIST_PAGEPKGS_BEAN; %>
<bean:define id="pageBeanName"  value="<%=IWebAppConstants.SESSION_DIST_PAGEPKGS_BEAN%>" toScope="request" />
<sm:setPagingResults formName="bogusForNow" beanName="<%= (String)pageBeanName %>"
                     resultsName="session_dist"
                     property = "applicationChannels"
        />
<% int startIndex = ((GenericPagingBean) pageContext.findAttribute(pageBeanName)).getStartIndex();%>
<%-- End Set up paging for pkgs --%>
<script type="text/javascript">
    <%
        GenericPagingBean bean = (GenericPagingBean) pageContext.findAttribute(pageBeanName);
        int totalCount = bean.getTotal();
        out.println("var totalCount = "+totalCount+";");
    %>
    function setCheckAllFromHeader() {
        // Tamil : Just commenting this now since we are facing issues while making ajax call to load UCD template, we need to check other functionalities as well
        //        if(totalCount != 0) {
        //            send(document.distAsgForm, '/persistifyChecksOnDistAsg.do?/distribution/distribution_assignment.jsp?page=current');
        //        }
    }
</script>

<body>
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

<%@ include file="/distribution/package_mass_edit_layer.jsp" %>

<html:form name="distAsgForm" action="/distSetStates.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm">
<html:hidden property="forward" />
<div align="center">


<div style="padding-left:25px; padding-right:25px;">


<div class="pageHeader"><span class="title"><webapps:pageText key="policy" type="pgtitle" shared="true"/></span></div>

<logic:present name="taskid">
    <div class="pageHeader">
        <span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" />

        <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
    </div>
</logic:present>

<logic:notPresent name="taskid">
    <logic:present name="taskIDEnabled">
        <div class="pageHeader">
            <span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span>
            <html:text name="distAsgForm" property="taskid" />
            <span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span>
            <html:text name="distAsgForm" property="changeid" />
        </div>
    </logic:present>
</logic:notPresent>
    <%-- Errors Display --%>
<div style="width:100%; ">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>
</div>

<logic:present name="conflict_alert">
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p>
            <webapps:pageText key="ConflictAlert1"/>&nbsp;
            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">&nbsp;
            <webapps:pageText key="ConflictAlert2"/>
        </p>
    </div>
</logic:present>

<div class="pageInfo">
    <table cellspacing="0" cellpadding="2" border="0">
        <tr>
            <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>
            <td><webapps:pageText key="IntroShort"/></td>
        </tr>
    </table>
</div>


<logic:present name="session_multitgbool">
    <jsp:include page="/includes/target_display.jsp"/><br>
    <webapps:formtabs tabset="distmulti" tab="sec" />
</logic:present>
<logic:notPresent name="session_multitgbool">
    <logic:present name="multi_trgts_pkg">
        <logic:equal name="multi_trgts_pkg" value="true">
            <jsp:include page="/includes/target_display.jsp"/><br>
            <webapps:formtabs tabset="distmulti" tab="sec" />
        </logic:equal>
    </logic:present>
    <logic:notPresent name="multi_trgts_pkg">
        <div class="itemStatus">
            <table cellspacing="0" cellpadding="3" border="0">
                <tr>
                    <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>: </td>
                    <logic:iterate id="target" name="session_dist" property="targets">
                        <td align="left">
                            <% //String tgLabel="target"; %>
                            <bean:define id="ID" name="target" property="id" toScope="request"/>
                            <bean:define id="Name" name="target" property="name" toScope="request"/>
                            <bean:define id="Type" name="target" property="type" toScope="request"/>
                            <jsp:include page="/includes/target_display_single.jsp"/>
                        </td>
                    </logic:iterate>
                </tr>
            </table>
        </div>
        <webapps:formtabs tabset="dist" tab="windows" subtab="sec" />
    </logic:notPresent>
</logic:notPresent>

<div class="formContent" id="mainSection">
    <%-- Apply the persisted packages to the form. --%>
<sm:setPersistedRecords selectedTargetsVarName="session_dist_pagepkgs_selected" formName="distAsgForm" pagingBeanName="session_dist_pagepkgs_bean"/>

<div class="tableWrapper" style="width:100%; overflow:hidden;">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr valign="middle" class="smallButtons">
        <td align="left" class="tableRowActions">
            <input type="button" name="edit_all_btn" id="edit_all_btn" value="<webapps:pageText shared="true" type="button" key="packageMassEdit"/>" disabled onClick="javascript:showMultiEditArea();" >
            <logic:notPresent scope="session" name="is_from_package_view">
                <input type="button" value="<webapps:pageText shared="true" type="button" key="editPackageList"/>" onClick="javascript:saveState('/packageEdit.do');" >
            </logic:notPresent>
            <logic:present scope="session" name="is_from_package_view">
                <input type="button" disabled value="<webapps:pageText shared="true" type="button" key="editPackageList"/>" onClick="javascript:saveState('/packageEdit.do');" >
            </logic:present>
            <input type="button" name="common_schedule_btn" id="common_schedule_btn" disabled value="<webapps:pageText key="SetCommonSchedule"/>" onClick="javascript:saveState('/distAsgSchedCommonEdit.do?action=edit');" >
            <logic:present name="page_show_ucd_button">
                <logic:equal name="page_show_ucd_button" value="true">
                    <input type="button" id="ucd_btn" value="<webapps:pageText key="setdeviceLelvelLabel"/>" onclick="invokeUCDMaskDiv();" disabled="disabled">
                </logic:equal>
            </logic:present>
        </td>
            <%-- <bean:define id="size" value="<%=Integer.toString(session_dist.getApplicationChannels().size())%>"/> --%>
        <logic:notPresent name="multi_trgts_pkg">
            <td align="right" class="tableRowActions"> <webapps:pageText key="Startingpriority"/>&nbsp; <html:text name="session_dist" property="startingPriority" styleId="minFrom" size="5" maxlength="5" onkeypress="return restrictKeyPressPositive(event)"/>
                <logic:equal name="<%= pageBeanName %>" property="total" value="0">
                    <input type="button" disabled value="<webapps:pageText shared="true" type="policy_properties.button" key="apply"/>" onClick="javascript:saveState('/prioritySave.do?action=set_starting_priority');">
                </logic:equal>
                <logic:notEqual name="<%= pageBeanName %>" property="total" value="0">
                    <input type="button" value="<webapps:pageText shared="true" type="policy_properties.button" key="apply"/>" onClick="javascript:saveState('/prioritySave.do?action=set_starting_priority');">
                </logic:notEqual>
            </td>
            <td align="right" class="tableRowActions"> &nbsp;
                <logic:equal name="<%= pageBeanName %>" property="total" value="0">
                    <input type="button" disabled value="<webapps:pageText shared="true" type="policy_properties.button" key="update"/>" onClick="javascript:saveState('/prioritySave.do?action=modify_exising_priority');">
                </logic:equal>
                <logic:notEqual name="<%= pageBeanName %>" property="total" value="0">
                    <input type="button" value="<webapps:pageText shared="true" type="policy_properties.button" key="update"/>" onClick="javascript:saveState('/prioritySave.do?action=modify_exising_priority');">
                </logic:notEqual>
            </td>
        </logic:notPresent>
        <logic:present name="multi_trgts_pkg">
            <logic:equal name="multi_trgts_pkg" value="true">
                <td align="right" class="tableRowActions"> <webapps:pageText key="Startingpriority"/>&nbsp; <html:text name="session_dist" property="startingPriority" value="" styleId="minFrom" size="5" maxlength="5" disabled="true"/>
                    <input type="button" disabled value="<webapps:pageText shared="true" type="policy_properties.button" key="apply"/>" onClick="javascript:saveState('/prioritySave.do?action=set_starting_priority');">
                </td>
                <td align="right" class="tableRowActions"> &nbsp;
                    <input type="button" disabled value="<webapps:pageText shared="true" type="policy_properties.button" key="update"/>" onClick="javascript:saveState('/prioritySave.do?action=modify_exising_priority');">
                </td>
            </logic:equal>
        </logic:present>
        <td align="right" class="tableRowActions">
            <logic:equal name="distAsgForm" property='<%= "value("+IWebAppConstants.SESSION_DIST_PAGEPKGS_PREORDER+")" %>' value="true" >
                <input type="button" value="<webapps:pageText shared="true" type="policy_properties.button" key="revert"/>" onClick="javascript:saveState('/prioritySave.do?action=revert_exising_priority');">
            </logic:equal>
            <logic:equal name="distAsgForm" property='<%= "value("+IWebAppConstants.SESSION_DIST_PAGEPKGS_PREORDER+")" %>' value="false" >
                <input type="button" value="<webapps:pageText shared="true" type="policy_properties.button" key="revert"/>" disabled>
            </logic:equal>
        </td>

        <td align="right" class="tableRowActions" nowrap>
            <bean:define toScope="request" id="submitPaging" value="true"/>
            <bean:define toScope="request" id="page_jsNoFrameSubmit" value="pageSaveState"/>
            <% request.setAttribute("selectForm","loadActionFromSelectionPkg");
                request.setAttribute("formName","document.distAsgForm"); %>
            <jsp:include page="/includes/genPrevNext.jsp" /> &nbsp;
        </td>

    </tr>
</table>

<script language="JavaScript">
    <%
             DistributionBean distBean = (DistributionBean)session.getAttribute("session_dist");
             PersistifyChecksAction.SelectedRecords selectedChannels =
                          (PersistifyChecksAction.SelectedRecords)session.getAttribute("session_dist_pagepkgs_selected");

             int noOfChannels=0;
             if(distBean != null && (noOfChannels=distBean.getChannels().size()) > 0) {
                 int chkBoxCountInRemPages;
                 List channelsCurrPage = (List)request.getAttribute("display_rs");
                 chkBoxCountInRemPages = noOfChannels - channelsCurrPage.size();
                 out.println("var chkBoxCountInRemPages=" + chkBoxCountInRemPages);
                 int selectionCount = 0;
                 if(selectedChannels != null && (selectionCount=selectedChannels.getSelectionCount()) > 0) {
                     out.println("var selectionCount=" + selectionCount);
                     out.println("buttonEnable(\"common_schedule_btn\",false);");
                 }
             }
             %>
</script>


<div class="headerSection" style="width:100%; text-align:left;" id="FOO_headerDiv">
    <table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable" style="width: 100%;">
        <colgroup width="0"/>
        <colgroup width="0"/>
        <colgroup width="5%"/>
        <colgroup width="35%"/>
        <colgroup width="15%"/>
        <colgroup width="15%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>
        <colgroup width="10%"/>

        <tr id="FOO_headerTable_firstRow">
            <td class="tableHeaderCell">
                <html:hidden property="value(clear_all)"/>
                <html:checkbox property="value(dist_pagepkgs_item_all)" value="true" styleId="dist_pagepkgs_item_all" onclick="setClearAllFromHeader(this.checked); checkboxToggle('dist_pagepkgs_item'); setCheckAllFromHeader()"/>
            </td>
            <td align="center" class="tableHeaderCell"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="11" height="1"></td>
            <td class="tableHeaderCell"><webapps:pageText key="priority" type="colhdr" shared="true"/></td>
            <td class="tableHeaderCell"><webapps:pageText key="pkgs" type="colhdr" shared="true"/></td>
            <td nowrap class="tableHeaderCell"><webapps:pageText key="priState" type="colhdr" shared="true"/></td>
            <td nowrap class="tableHeaderCell"><webapps:pageText key="secState" type="colhdr" shared="true"/></td>
            <td class="tableHeaderCell"><webapps:pageText key="exempt" type="colhdr" shared="true"/></td>
            <td class="tableHeaderCell"><webapps:pageText key="wow" type="colhdr" shared="true"/></td>
            <td class="tableHeaderCell">Device Level</td>
        </tr>

    </table>
</div>
<!--end headerSection-->
<div id="FOO_dataDiv" style="height:100px; width:100%; overflow:auto; text-align:left;" onscroll="syncScroll('FOO');">

<table cellpadding="0" cellspacing="0" border="0" id="FOO_dataTable" style="width: 100%;">
<colgroup width="0"/>
<colgroup width="0"/>
<colgroup width="5%"/>
<colgroup width="35%"/>
<colgroup width="15%"/>
<colgroup width="15%"/>
<colgroup width="10%"/>
<colgroup width="10%"/>
<colgroup width="10%"/>

<logic:present name="session_dist" scope="session">
<% String lastIdx = Integer.toString(session_dist.getApplicationChannels().size() - 1); %>
<% int contentsRowCount = 0; %>
<logic:iterate id="app" name="display_rs" indexId="indexId">
<% indexId = new Integer(startIndex + indexId.intValue()); %>
<bean:define id="index" value='<%= indexId.toString() %>' />
<tbody id='<%= "row1-" + index %>'>
<logic:equal name="indexId" value="0">
<tr id="FOO_dataTable_firstRow">
    </logic:equal>
    <logic:notEqual name="indexId" value="0">
        <% if (contentsRowCount % 2 == 0){ %>
<tr>
        <% } else { %>
<tr class="alternateRowColor">
<% } %>
</logic:notEqual>

<td class="rowLevel1">
    <html:checkbox property='<%="value(dist_pagepkgs_item_" +  index +")" %>'  value="true" styleId='<%="dist_pagepkgs_item_" + index %>'  onclick="processCheckbox(this.id); setClearAllFromBody(); addToSelectedChnlsArray(this.id, this.checked, false);"></html:checkbox>
        <%--<input type="hidden" id=<%="chnl_url_" + index %> name='<%="chnl_url_" + index %>' value='<bean:write name='app' property='url'/>'>--%>
    <html:hidden property='<%="value(chnl_url_" + index +")" %>' value="<%= ((Channel)app).getUrl()%>" styleId='<%="chnl_url_" + index %>'/>
</td>
<td align="center" class="rowLevel1" width="0*"><a href="javascript:toggleSection('<%= "row1-" + index %>')"><img border="0" id='<%= "widget-row1-" + index %>' src="/shell/common-rsrc/images/list_arrow_c.gif" width="11" height="11" class="widget"></a> </td>
<td class="rowLevel1">
    <logic:notPresent name="multi_trgts_pkg">
        <% String orderStateVal = session_dist.getChInitOrderStateValue(((Channel)app).getUrl());
            if (orderStateVal !=null) {
                pageContext.setAttribute("oderStateValue", orderStateVal);
            } else {
                pageContext.setAttribute("oderStateValue", "");
            }
        %>
        <table width="100%" border="0" cellspacing="0" cellpadding="1">
            <tr>
                <logic:equal name="oderStateValue" value="inconsistent">
                <td width="10" valign="top">
                    <html:radio property='<%= "value(changeOrderInc#" + app.hashCode() +")" %>' value="true" styleId='<%= "changeorderInc#" + app.hashCode() %>' onclick='<%= "javascript:saveState('/distribution/distribution_assignment.jsp');" %>'/> </td>
                <td><webapps:pageText key="MaintainDiffs" /></td>
            </tr>
            <tr>
                <td valign="top"><html:radio property='<%= "value(changeOrderInc#" + app.hashCode() +")" %>' value="false" styleId='<%= "changeorderInc#" + app.hashCode() %>' onclick='<%= "javascript:saveState('/distribution/distribution_assignment.jsp');" %>'/> </td>
                </logic:equal>
                <logic:present name="distAsgForm" property='<%= "value(changeOrderInc#" + app.hashCode() +")" %>'>
                    <logic:equal name="distAsgForm" property='<%= "value(changeOrderInc#" + app.hashCode() +")" %>' value="false">
                        <logic:notEmpty name="distAsgForm" property='<%= "value(changeorder#" + app.hashCode() +")" %>'>
                            <td><html:text property='<%= "value(changeorder#" + app.hashCode() +")" %>' styleId="minFrom" size="5" maxlength="5" onkeypress="return restrictKeyPressPositive(event)" /></td>
                        </logic:notEmpty>
                        <logic:empty name="distAsgForm" property='<%= "value(changeorder#" + app.hashCode() +")" %>'>
                            <td><html:text property='<%= "value(changeorder#" + app.hashCode() +")" %>' styleId="minFrom" size="5" maxlength="5" value="<%= ((Channel)app).getOrder()==99999 ? NA : "" + ((Channel)app).getOrder()  %>" onkeypress="return restrictKeyPressPositive(event)" /></td>
                        </logic:empty>
                    </logic:equal>
                    <logic:notEqual name="distAsgForm" property='<%= "value(changeOrderInc#" + app.hashCode() +")" %>' value="false">
                        <td><html:text property='<%= "value(changeorder#" + app.hashCode() +")" %>' styleId="minFrom" size="5" maxlength="5" value="<%=NA%>" onkeypress="return restrictKeyPressPositive(event)" /></td>
                    </logic:notEqual>
                </logic:present>
                <logic:notPresent name="distAsgForm" property='<%= "value(changeOrderInc#" + app.hashCode() +")" %>'>
                    <td><html:text property='<%= "value(changeorder#" + app.hashCode() +")" %>' styleId="minFrom" size="5" maxlength="5" value="<%= ((Channel)app).getOrder()==99999 ? NA : "" + ((Channel)app).getOrder()  %>" onkeypress="return restrictKeyPressPositive(event)" /></td>
                </logic:notPresent>
            </tr>
        </table>
        <html:hidden property='<%="value(changeorderhdn#" + app.hashCode() +")" %>' value="<%= ((Channel)app).getOrder()==99999 ? "99999": "" + ((Channel)app).getOrder() %>" />
    </logic:notPresent>
    <logic:present name="multi_trgts_pkg">
        <logic:equal name="multi_trgts_pkg" value="true">
            <html:text property='<%="value(changeorderhdn#" + app.hashCode() +")" %>' styleId="minFrom" disabled="true" size="5" maxlength="5" value="" />
            <html:hidden property='<%= "value(changeOrderInc#" + app.hashCode() +")" %>' value="false" />
        </logic:equal>
    </logic:present>
</td>
    <%-- Channel URL and optional warning icon if multiple and differing states exist --%>
<td class="rowLevel1">
    <% String totvalue = session_dist.getChInitInconsistentStates(((Channel)app).getUrl());
        pageContext.setAttribute("chInitInconsistentStates",totvalue);
    %>
    <logic:equal name="chInitInconsistentStates" value="true">
        <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
    </logic:equal>
    <a href="javascript:void(0);" class="noUnderlineLink" style="cursor:help;" onmouseover="return Tip('<webapps:stringescape><bean:write name="app" property="url" filter="false" /></webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();">
        <img src="/shell/common-rsrc/images/package.gif" border="0" /> <bean:write name="app" property="title" />
    </a>
</td>
<td class="rowLevel1"><%-- Secondary state - set an attribute if the state was inconsistent originally --%>
    <% String initsecval = session_dist.getChInitSecStateValue(((Channel)app).getUrl());
        if (initsecval == null) {
            initsecval = "";
        }
        pageContext.setAttribute("initSecStateValue", initsecval);
    %>
        <%-- Primary State --%>
        <%-- If the state was inconsistent originally, then we need to give the radio buttons --%>
    <% String initval = session_dist.getChInitStateValue(((Channel)app).getUrl());
        if (initval ==null) {
            initval = "";
        }
        pageContext.setAttribute("initStateValue", initval);
    %>
    <table width="100%" border="0" cellspacing="0" cellpadding="1">
        <tr>
            <bean:define id="primary" name="app" property="state" toScope="request"/>
            <logic:equal name="initStateValue" value="inconsistent">
            <td width="10" valign="top"><input type="radio" name='<%= "value(stateInc#" + app.hashCode() +")" %>' value="true" <% if ( primary == "inconsistent") { out.println("checked=\"checked\""); }%> id='<%= "stateInc#" + app.hashCode() %>' onclick="<%= "javascript:saveState('/distribution/distribution_assignment.jsp');" %>"> </td>
            <td><webapps:pageText key="MaintainDiffs" /></td>
        </tr>
        <tr>
            <td valign="top"><input type="radio" name='<%= "value(stateInc#" + app.hashCode() +")" %>' <% if ( primary != "inconsistent") { out.println("checked=\"checked\""); }%> value="false" id='<%= "stateInc#" + app.hashCode() %>' onclick="<%= "javascript:changeWowBlkoutState('"+((Channel)app).getEscapedURL()+"', 'state', 'subscribe_noinstall' );" %>"> </td>
            </logic:equal>
            <td><html:select property='<%= "value(state#" + app.hashCode() +")" %>' value='<%= ((Channel) app).getState() %>' onchange='<%= "javascript:saveState('/distribution/distribution_assignment.jsp');" %>' styleId='<%= "state#" + app.hashCode() %>'> <html:options property="states" labelProperty="statesLabel" /> </html:select> </td>
        </tr>
    </table>
</td>
<td class="rowLevel1"><%-- Secondary State --%>
    <table width="100%" border="0" cellspacing="0" cellpadding="1">
        <tr>
                <%--The secondary state can be null.. In which case, the logic equal tag will
                    not work.  Need to check for presence
                    --%>
            <logic:present name="initSecStateValue">
            <logic:equal name="initSecStateValue" value="inconsistent">
            <%
                String secondaryState = ((Channel)app).getSecState();
                if (secondaryState == null) {
                    secondaryState = "";
                }
            %>
            <td width="10" valign="top"><input type="radio" name='<%= "value(secStateInc#" + app.hashCode() + ")" %>' value="true"  <% if ("inconsistent".equals(secondaryState)) { out.println("checked=\"checked\""); }%> id='<%= "secStateInc_1#" + app.hashCode() %>' onclick="<%= "javascript:saveState('/distribution/distribution_assignment.jsp');" %>"> </td>
            <td><webapps:pageText key="MaintainDiffs" /></td>
        </tr>
        <tr>
            <td valign="top"><input type="radio" name='<%= "value(secStateInc#" + app.hashCode() +")" %>' value="false"  <% if (("".equals(secondaryState)) ||  (!("inconsistent".equals(secondaryState)))) { out.println("checked=\"checked\""); }%> id='<%= "secStateInc_2#" + app.hashCode() %>' onclick="<%= "javascript:changeWowBlkoutState('"+((Channel)app).getEscapedURL()+"', 'secState', 'null' );" %>"> </td>
            </logic:equal>
            </logic:present>
            <td><html:select property='<%= "value(secState#" + app.hashCode() +")" %>' value='<%= ((Channel) app).getSecState() %>' onchange=" javascript:saveState('/distribution/distribution_assignment.jsp');" styleId='<%= "secState#" + app.hashCode() %>'> <html:option value="">&nbsp;</html:option> <html:options property="states" labelProperty="statesLabel" /> </html:select> </td>
        </tr>
    </table>
</td>
<bean:define id="enableWow" name="app" property="wowEnabled" toScope="request"/>
<bean:define id="exemptblck" name="app" property="exemptFromBlackout" toScope="request"/>
<%
    String wow = enableWow.toString();
    String blackout = exemptblck.toString();
%>
<td class="rowLevel1">
    <input type="checkbox" name='<%= "value(exemptBo#" + app.hashCode()+")"%>' <% if ( "true".equals(blackout)) { out.println("checked=\"checked\""); } %> value="true" id='<%= "exemptBo#" + app.hashCode() %>' onclick="<%= "javascript:changeWowBlkoutState('"+((Channel)app).getEscapedURL()+"', 'exemptBo', this.checked);" %>">
</td>
<td class="rowLevel1">
    <%
        if (isWoWApplicable) {%>
    <input type="checkbox" name='<%= "value(wowDep#" + app.hashCode()+")"%>' <% if ( "true".equals(wow)) { out.println("checked=\"checked\""); }%>
    <logic:equal name="distAsgForm" property="deActivateWoW" value="true"> Disabled title='<webapps:pageText key="wowdisabled" type="info" shared="true"/>' style="cursor:help" </logic:equal>
                                                                           value="true" id='<%= "wowDep#" + app.hashCode() %>' onclick="<%= "javascript:changeWowBlkoutState('"+ ((Channel)app).getEscapedURL()+"', 'wowDep', this.checked);" %>">
    <%} else {out.print(NA);}%>
</td>
<td class="rowLevel1">
    <%
        String ucdTmplts =  ((Channel) app).getUcdTemplates();
        out.print((null == ucdTmplts || ucdTmplts.isEmpty()) ? NA : ucdTmplts);
    %>
</td>

</tr>
</tbody>
<tbody id='<%= "row1-" + index + "_1"%>' style="display:none;">
<tr>
<td class="rowLevel1" colspan="9">
<table width="100%" border="0" cellspacing="0" cellpadding="3">
<colgroup span="2" width="50%"/>
<tr>
    <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
        <tr>
            <td class="tableRowActions" style="height:20px;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="textWhite"><strong> <webapps:pageText key="priStateTitle"/> </strong> </td>
                    <td align="right">
                        <input name="Submit3" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState('/distAsgSchedEdit.do?schedType=initial&channel=<%= index %>');">
                    </td>
                </tr>
            </table>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <logic:present name="app" property="initScheduleString">
                <logic:equal name="app" property="initScheduleString" value="inconsistent">
                <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                    </logic:equal>
                    </logic:present>
                        <sm:scheduleDisplay name="app" property="initScheduleString" schedule='<%= ((Channel) app).getInitScheduleString() %>' type="initial" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                <p>&nbsp;</p>
            </td>
        </tr>
    </table>
    </td>
    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td height="21" class="textWhite"><strong> <webapps:pageText key="updateTitle"/> </strong> </td>
                            <td align="right">
                                <input name="Submit33" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState('/distAsgSchedEdit.do?schedType=update&channel=<%= index %>');">
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top"><logic:present name="app" property="updateScheduleString"> <logic:equal name="app" property="updateScheduleString" value="inconsistent">
                    <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal> </logic:present> <sm:scheduleDisplay name="app" property="updateScheduleString" schedule='<%= ((Channel) app).getUpdateScheduleString() %>' type="update" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                    <p>&nbsp;</p>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="textWhite"><strong> <webapps:pageText key="secStateTitle"/> </strong> </td>
                            <td align="right">
                                <input name="Submit342" type="button" id="secStateBtn#<%=app.hashCode()%>" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState('/distAsgSchedEdit.do?schedType=secondary&channel=<%= index %>');" >
                            </td>
                            <script>
                                stateChange(<%= app.hashCode() %>, '<%= ((Channel) app).getSecState() %>', true);
                            </script>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top"><logic:present name="app" property="secScheduleString"> <logic:equal name="app" property="secScheduleString" value="inconsistent">
                    <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal> </logic:present> <sm:scheduleDisplay name="app" property="secScheduleString" schedule='<%= ((Channel) app).getSecScheduleString() %>' type="secondary" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                    <p>&nbsp;</p>
                </td>
            </tr>
        </table>
    </td>
    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="textWhite"><strong> <webapps:pageText key="verifyTitle"/> </strong> </td>
                            <td align="right">
                                <input name="Submit35" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState('/distAsgSchedEdit.do?schedType=verrepair&channel=<%= index %>');" >
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top"><logic:present name="app" property="verRepairScheduleString"> <logic:equal name="app" property="verRepairScheduleString" value="inconsistent">
                    <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal> </logic:present> <sm:scheduleDisplay name="app" property="verRepairScheduleString" schedule='<%= ((Channel) app).getVerRepairScheduleString() %>' type="verrepair" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                    <p>&nbsp;</p>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td valign="top">
        <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
            <tr>
                <td class="tableRowActions" style="height:20px;">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td class="textWhite"><strong> <webapps:pageText key="postponeTitle"/> </strong> </td>
                            <td align="right">
                                <input name="Submit35" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:saveState('/distAsgSchedEdit.do?schedType=postpone&channel=<%= index %>');" >
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td valign="top"><logic:present name="app" property="postponeScheduleString"> <logic:equal name="app" property="postponeScheduleString" value="inconsistent">
                    <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                        </logic:equal> </logic:present> <sm:scheduleDisplay name="app" property="postponeScheduleString" schedule='<%= ((Channel) app).getPostponeScheduleString() %>' type="postpone" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                    <p>&nbsp;</p>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</td>
</tr>
</tbody>
<% contentsRowCount++;%>
</logic:iterate>
<html:hidden property="value(hasLoaded)"  value="true"/>
</logic:present>
</table>
</div><!--end FOO_dataSection-->

</div><!--end tableWrapper-->

</div><!--end formContent-->

<div class="formBottom">
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
            <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
        </tr>
    </table>
</div>
<!--end formBottom-->
<div id="pageNav">
    <input name="Ok" type="button" class="mainBtn" accesskey="N" onClick="javascript:saveState('/distInit.do?action=preview');" value="<webapps:pageText key="preview" type="button" shared="true"/>">
    &nbsp;
    <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
    </logic:present>
    <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
    </logic:notPresent>
</div>

</div>
<!--end supder div for padding-->

</div>
<!--end super div for centering-->


<div id="ucdwindow" class="easyui-window" title='<webapps:pageText key="label.devicelevelheader"/>' data-options="modal:true,closed:true">
    <div class="easyui-layout" data-options="fit:true">
        <div data-options="region:'center'" style="padding:0, 5px;">
            <div style="padding-left:5px;"><h3><span class="title"><webapps:pageText key="label.deviceleveltitle"/></span></h3></div>
            <div style="padding-left:5px;" class="sectionInfo"><webapps:pageText key="label.devicelevelinfo"/></div>
            <div style="padding-left:60px;padding-top:5px;padding-bottom:5px;">
                <input type="radio" id="dynmaicLevelOpt" name="deviceLevelOpt" value="dynamicLevel" onclick="changeLevelSetting(this.value)"/> <webapps:pageText key="label.dynamictypetitle"/>
                <input type="radio" id="staticLevelOpt" name="deviceLevelOpt" value="staticLevel" onclick="changeLevelSetting(this.value)"/> <webapps:pageText key="label.statictypetitle"/>
            </div>
            <div id="sLevelId">
            </div>
            <div id="dLevelId">
                <div style="padding-left:5px;"><h3><span class="title"><webapps:pageText key="label.selectucd"/></span></h3></div>
                <div style="padding-left:5px;" class="sectionInfo"><webapps:pageText key="label.selectucdinfo"/></div>
                <div class="headerSection" id="templateHeaderId" style="width:100%;">
                    <div id="dataloading" class="datagrid-mask-msg" style="display: none; left: 50%; margin-left: -102px;">Processing, please wait ...</div>
                    <table cellspacing="0" cellpadding="0" width="100%">
                        <colgroup width="5%"/>
                        <colgroup width="35%"/>
                        <colgroup width="0*"/>
                        <tr>
                            <td class="tableHeaderCell"><input type="checkbox" id="ucd_check_all" onclick="checkAllUCDTemplates(this.checked);"></td>
                            <td class="tableHeaderCell"><webapps:pageText key="label.ucdname"/></td>
                            <td class="tableHeaderCell"><webapps:pageText key="label.ucddetails"/></td>
                        </tr>
                    </table>
                </div>

                <div id="templateBodyId" style="float:left; overflow:hidden;width:40%;">
                    <table id="tmplt_names" cellspacing="0" cellpadding="0" width="100%"></table>
                </div>
                <div style="float:left; overflow:hidden;padding-left:5px;padding-top:5px;border-left: 1px solid #CCCCCC;">
                    <table id="tmplt_details" cellspacing="0" cellpadding="0" width="100%"></table>
                </div>

                <div width="99%" id="ucd_error" align="center"></div>
            </div>
            <div style="border-top: 1px solid #000000;margin-top: 10px;padding-top: 5px;text-align: right;vertical-align: bottom;width: 100%;position:absolute;bottom:8px;left:0;">
                <input type="button" class="mainbtn" value='<webapps:pageText key="label.ucdsave"/>' onclick="saveUCDTemplates();" id="saveBtn">&nbsp;
                <input type="button" value="<webapps:pageText key='Cancel' type='global' />" onclick="closeUCDGlassPane();">&nbsp;&nbsp;
            </div>
        </div>
    </div>
</div>
</html:form>

<script>
var totalTemplates = 0;
function invokeUCDMaskDiv() {
    $('#ucdwindow').window({
        top:90,
        left:342,
        width:784,
        height:500,
        modal:true
    });
    $(".panel-tool-min").hide();
    $('#ucdwindow').window('open');
    loadDataFromFile();
}

function closeUCDGlassPane() {
    setClearAllFromHeader(false);
    checkboxToggle('dist_pagepkgs_item');
    setCheckAllFromHeader();
    document.getElementById("common_schedule_btn").disabled = true;
    document.getElementById("ucd_btn").disabled = true;
    $('#ucdwindow').window('close');
}
function changeLevelSetting(levelValue) {
    if("staticLevel" == levelValue) {
        document.getElementById('staticLevelOpt').checked = true;
        document.getElementById('dynmaicLevelOpt').checked = false;
        checkBoxCheck(false);
        checkAllUCDTemplates(false);
        document.getElementById('saveBtn').disabled = false;
        document.getElementById('dLevelId').style.display = "none";
        document.getElementById('sLevelId').style.display = "block";
    } else {
        document.getElementById('staticLevelOpt').checked = false;
        document.getElementById('dynmaicLevelOpt').checked = true;
        document.getElementById('staticLevel').value = "";
        if(totalTemplates == 0) document.getElementById("saveBtn").disabled = true;
        document.getElementById('sLevelId').style.display = "none";
        document.getElementById('dLevelId').style.display = "block";
    }
}
function setStaticDeviceLevel(dLevel) {
    var sLevelConstant = "StaticLevel_";
    var sLevel = dLevel.substring(sLevelConstant.length, dLevel.length);
    document.getElementById('staticLevel').value = sLevel;
    document.getElementById('staticLevelOpt').checked = true;
    document.getElementById('dynmaicLevelOpt').checked = false;
    document.getElementById('saveBtn').disabled = false;
    document.getElementById('dLevelId').style.display = "none";
    document.getElementById('sLevelId').style.display = "block";
}
function setDynamicDeviceLevel() {
    document.getElementById('staticLevel').value = "";
    document.getElementById('staticLevelOpt').checked = false;
    document.getElementById('dynmaicLevelOpt').checked = true;
    document.getElementById('sLevelId').style.display = "none";
    document.getElementById('dLevelId').style.display = "block";
    if(totalTemplates == 0) document.getElementById("saveBtn").disabled = true;
}
function loadStaticDeviceLevelOptions(maxLevel, sLevel) {
    var deviceLevelOpts = document.getElementById('sLevelId');
    var divDetails = '<div style="padding-left:5px;"><h3><span class="title"><webapps:pageText key="label.deviceheader"/></span></h3></div>';
    divDetails = divDetails + '<div style="padding-left:5px;" class="sectionInfo"><webapps:pageText key="label.devicesectioninfo"/></div>';
    divDetails = divDetails + '<div style="padding-left:130px;"><b><webapps:pageText key="sdeviceLelvelLabel"/> : </b>';
    divDetails = divDetails + "&nbsp;&nbsp;&nbsp;<select id='staticLevel' name='staticLevel' multiple><option value=''>---Select---</option>";
    var Levels = "";
    var levelList = new Array();
    if("" != sLevel) {
        Levels = sLevel.split(",");
    }
    if("" != Levels) {
        for(var count = 0;count < Levels.length;count++) {
            var dev = Levels[count];
            dev = dev.replace("StaticLevel_", "")
            levelList[count] = dev;
        }
    }
    for(var count = 1;count < maxLevel + 1;count++) {
        var isChecked = "false";
        for(var i =0;i < levelList.length;i++) {
            if(levelList[i] == count) isChecked = "true";
        }
        if("true" == isChecked) {
            divDetails = divDetails + "<option value='"+count+"' selected>"+ count + "</option>";
        } else {
            divDetails = divDetails + "<option value='"+count+"'>"+ count + "</option>";
        }
    }
    divDetails = divDetails + "</select></div>";
    deviceLevelOpts.innerHTML = divDetails;
}

function loadDataFromFile() {
    var url = 'ajax?func=loaducdtemplates&action=gettmpltnames&selectedchnls=' + selectedChnlsArray;
    var tmpltNamesTbl = '<colgroup width="5%"/><colgroup width="95%"/>';
    var contentsRowCount = 0;
    var colSpanTmpRow;
    var bgClassName = '';


    $('#tmplt_names').html("");
    togglePleaseWait(true);
    $.post(url, function(data) {
        $.each(data.all_templates, function() {totalTemplates ++});
        var sLevel = data.static_devicelevel;
        loadStaticDeviceLevelOptions(data.maxLevel, sLevel);

        var firstTime = data.firstTimeLoad;
        if (totalTemplates == 0) {
            invokeErrorDiv(true, '');
            if("" != sLevel) {
                setStaticDeviceLevel(sLevel);
            } else {
                setDynamicDeviceLevel();
            }
            togglePleaseWait(false);
            return;
        }
        $.each(data.all_templates, function(tmpltName, selected) {
            var isSelected = 'true' == selected ? "checked" : "";
            bgClassName = (contentsRowCount % 2 == 0) ? '' : ' class="alternateRowColor"';
            tmpltNamesTbl += '<tr'+bgClassName+'><td class="rowLevel1"> <input type="checkbox" id="ucd_tmp_'+tmpltName+'" onclick="checkBoxCheck(this.checked);"' +isSelected+ '> </td>'
                    + '<td class="rowLevel1"> <a class="hoverLink" href="javascript:getTemplateDetails(\'' + tmpltName + '\');">' + tmpltName + '</a></td></tr>';

            colSpanTmpRow = '';
            contentsRowCount++;
        });
        $('#tmplt_names').html(tmpltNamesTbl);
        if("" != sLevel) {
            setStaticDeviceLevel(sLevel);
        } else {
            setDynamicDeviceLevel();
        }
        togglePleaseWait(false);
    }, 'json');
}

function getTemplateDetails(tmpltName) {
    var url = '/spm/ajax?func=loaducdtemplates&action=gettmpltdetails&tmpltname=' + tmpltName;
    $('#tmplt_details').html("");
    togglePleaseWait(true);
    $.post(url, function(data) {
        $('#tmplt_details').html($.parseHTML(data.tmpltdetails));
    });
    togglePleaseWait(false);
}

function saveUCDTemplates() {
    var url = '/spm/ajax?func=loaducdtemplates&action=savetemplates&selectedtmpltsname=' + getSelectedUCDTemplates();
    togglePleaseWait(true);
    $.post(url, function(data) {
        togglePleaseWait(false);
        document.distAsgForm.forward.value = '/distInit.do?action=saveucd';
        send(document.distAsgForm, '/distInit.do?action=saveucd');
    });
}
</script>

<script>
    function checkBoxCheck(checked) {
        var isAllChecked = true;
        if (checked) {
            $('input[id^=ucd_tmp_]:checkbox').each(function() {
                if (!this.checked) {
                    return (isAllChecked = false);
                }
            });
        }
        $('#ucd_check_all').prop('checked', isAllChecked && checked);
    }

    function checkAllUCDTemplates(checked) {
        $('input[id^=ucd_tmp_]:checkbox').each(function(){
            $(this).prop('checked', checked);
        });
    }

    function getTemplateId(rawName) {
        return rawName.split('ucd_tmp_')[1];
    }

    function getSelectedUCDTemplates() {
        var arr = new Array();
        var staticLevel = document.getElementById('staticLevelOpt');
        if(staticLevel.checked) {
            var listOpts = document.getElementById('staticLevel');
            var deLevel = "";
            for(var i =0;i<listOpts.length;i++) {
                if(listOpts[i].selected && listOpts[i].value != "") {
                    if("" == deLevel) {
                        deLevel = listOpts[i].value;
                    } else {
                        deLevel = deLevel + "," + listOpts[i].value;
                    }
                }
            }
            var sLevel = "StaticLevel_" + deLevel;
            arr.push(sLevel);
        } else {
            $('input[id^=ucd_tmp_]:checkbox').each(function(){
                if (this.checked) {
                    arr.push(getTemplateId(this.id));
                }
            });
        }
        return arr;
    }

    function togglePleaseWait(show) {
        if (show) {
            $('#dataloading').show();
        } else {
            $('#dataloading').hide();
        }
    }

    function invokeErrorDiv(show, errorMsg) {
        if (show) {
            errorMsg = '<ul><li>No UCD Templates Found, Configure <a href="/spm/usercentricdeployoptions.do">User Centric Deployment</a> and try again. </li></ul>';
            $("#ucd_error").html('<div id="critical" class="errorMessage" style="padding-top:10px"><h6><webapps:text key='page.main_view.errmesssage'/></h6><p></p>' + errorMsg + '<p></p></div>');
            $('#ucd_error').show();
            $('#saveBtn').attr('disabled', true);
        } else {
            $('#ucd_error').hide();
            $("#ucd_error").html('');
            $("#ucd_error").attr('class', '');
        }
    }

</script>

<script>
    <%-- Initialize clear_all hidden property value --%>
    setClearAllFromBody();
    if(totalCount > 0) {
        document.getElementById("edit_all_btn").disabled = false;
    }
    CMSOnLoadHandler.addHandler("resizeDataSection('FOO_dataDiv','pageNav');");
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','pageNav');");
    resizeDataSection('FOO_dataDiv','pageNav');
    syncTables('FOO');

    $(function() {
        // This is to set the data table width to 100%, needs to fit the screen
        $('#FOO_headerTable').width('100%');
        $('#FOO_dataTable').width('100%');
    });

    $(document).keydown(function(e) {
        // if ESCAPE key pressed close the opened mask layer, we should do this for all of our transparent windows
        if (e.keyCode == 27) {
            closeUCDGlassPane();
        }
    });
</script>

</body>
</html>

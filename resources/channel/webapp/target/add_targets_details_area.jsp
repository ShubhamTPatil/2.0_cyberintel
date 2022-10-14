<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Target" %>
<%@ page buffer="none" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<%@ include file="/includes/MultiViewChannel.jsp" %>
<%--
 The following operation are performed in the following js function
   1) Whenever the secondary state is null we make the secondary state select box empty
   and secondary schedule edit button to disabled state.
   2) when the user removes the secondary state, change the secondary schedule edit button to disabled state
 --%>
<script language="JavaScript">
    function stateChange(key, curSecState, init) {
        var stateObj;
        var secStateObj;
        var secStateBtnObj;

        if (hasDHTML()) {
            stateObj = document.getElementById("state#" + key);
            secStateObj =  document.getElementById("secState#" + key);
            secStateBtnObj = document.getElementById("secStateBtn#" + key);
        } else {
            stateObj = eval("document.addTargetEditForm.state#" + key);
            secStateObj = eval("document.addTargetEditForm.secState#" + key);
            secStateBtnObj = eval("document.addTargetEditForm.secStateBtn#" + key);
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
    }
    // This function saves the form attributes to the DistributionBean,
    // before forwarding to the next page.
    // IMPORTANT: Always use to navigate between pages so that the changes are
    // persistified in the session bean.
    function saveState(forwardaction) {
        document.addTargetEditForm.forward.value = forwardaction;
        submitActionFromFrames(document.addTargetEditForm, '/addTargetPackageState.do?type=package','1');
    }

    function saveSchedule(forwardaction) {
        document.addTargetEditForm.forward.value = forwardaction;
        send(document.addTargetEditForm, '/addTargetPackageState.do?type=package');
    }

    function pageSaveState(form, forwardaction) {
        document.addTargetEditForm.forward.value = forwardaction;
        submitActionFromFrames(document.addTargetEditForm, '/addTargetPackageState.do?type=package','1');
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

    function setClearAllFromHeader() {
        if(document.addTargetEditForm['value(add_pagepkgs_item_all)'].checked) {
            document.addTargetEditForm['value(clear_all)'].value = 'false';
        } else {
            document.addTargetEditForm['value(clear_all)'].value = 'true';
            selectionCount = 0;
        }
    }

    function setClearAllFromBody() {
        if(selectionCount == 0) {
            document.addTargetEditForm['value(clear_all)'].value = 'true';
        } else {
            document.addTargetEditForm['value(clear_all)'].value = 'false';
        }
    }

    var singleOptionElements = new Array("remove_button", "set_schedule_button");
    var multiOptionElements = new Array("remove_button", "set_schedule_button");
    addSpecialColumn("checkboxColumn",0.5);
    addSpecialColumn("targetName",2);
</script>


<%@ include file="/includes/endHeadSection.jsp" %>

<body onLoad="syncTables('FOO');" onResize="resizeDataSection('FOO_dataDiv','endOfGui',-1);">
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>

<bean:define id="pageBeanName" value="<%=IWebAppConstants.SESSION_ADD_PAGEPKGS_BEAN%>" toScope="request" />

<sm:setPagingResults formName="bogusForNow" beanName="<%= (String)pageBeanName %>" resultsName="add_remove_package" />

<%
    GenericPagingBean bean = (GenericPagingBean) pageContext.findAttribute(pageBeanName);
%>

<logic:notPresent name="add_remove_package" >
    <%
        bean.setTotal(0);
        bean.setStartIndex(0);
        bean.setResults(new java.util.ArrayList());
    %>
</logic:notPresent>

<script language="JavaScript">
    function setCheckAllFromHeader() {
    <%
        int totalCount = bean.getTotal();
        out.println("var totalCount = "+totalCount);
    %>
        if(totalCount != 0) {
            submitActionFromFrames(document.addTargetEditForm, '/persistifyChecksOnAddTarget.do?/target/add_targets_details_area.jsp?page=current','1');
        }
    }
</script>
<%

List<Target> sameUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_SAMEUSER);
List<Target> diffUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_DIFFUSER);
%>
<html:form name="addTargetEditForm" action="/addTargetPackageState.do" target="_top" type="com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm">
    <html:hidden property="forward" />
    <%-- Errors Display --%>
<div style="width:100%; ">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>
</div>
<logic:present name="session_multitgbool">
    <% if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) && (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {%>
    <div class="statusMessage" id="warning">
	           <h6>&nbsp;</h6>
	           <p>
	           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
	           <webapps:pageText key="approval_both_Warning" />
	           </p>
	        </div>
    <%} else { %>
	    <% if(null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) {%>
	        <div class="statusMessage" id="warning">
	           <h6>&nbsp;</h6>
	           <p>
	           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
	           <webapps:pageText key="approval_suser_Warning" />
	           </p>
	        </div>
	        <%} else { %>
	        <% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
	        <div class="statusMessage" id="warning">
	           <h6>&nbsp;</h6>
	           <p>
	           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
	           <webapps:pageText key="approval_duser_Warning" />
	           </p>
	        </div>
		<%}} %>
    <%} %>
    </logic:present>
    <logic:notPresent name="session_multitgbool">
    <% if(null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) {%>
        <div class="statusMessage" id="warning">
           <h6>&nbsp;</h6>
           <p>
           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
           <webapps:pageText key="approval_suser_Warning" />
           </p>
        </div>
        <%} else { %>
        <% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
        <div class="statusMessage" id="warning">
           <h6>&nbsp;</h6>
           <p>
           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
           <webapps:pageText key="approval_duser_Warning" />
           </p>
        </div>
	<%}} %>
    </logic:notPresent>
<div style="width:100%; ">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <% int channelCount = 0; %>
        <logic:present name="main_page_m_packages">
            <logic:iterate id="channel" name="main_page_m_packages" type="com.marimba.apps.subscription.common.objects.Channel">
                <% channelCount++;%>
            </logic:iterate>
            <logic:present name="session_multipkgbool">
                <td align="left" class="tableTitle">
                    <a href="#" onClick="javascript:parent.showMultiChannels();"> <%= channelCount%>&nbsp;<webapps:pageText key="selectedPackages" type="colhdr" shared="true"/></a>
                </td>
            </logic:present>
        </logic:present>
        <logic:notPresent name="session_multipkgbool">
            <tr><td><jsp:include page="/target/add_packages_display.jsp" /></td></tr>
        </logic:notPresent>
        <tr><td><webapps:formtabs tabset="addPkgTarget" tab="pkg" /></td></tr>
    </table>
</div>

<div id="FOO_mainContent" class="formContent" style="overflow:hidden">
    <div style="width:99%; padding-right:5px;" >
        <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>
                <bean:define toScope="request" id="submitPaging" value="true"/>
                <bean:define id="targetFrame" toScope="request" value="1" />
                <bean:define id="formName"  toScope="request" value="document.addTargetEditForm" />
                <bean:define id="genericPagingAction"  toScope="request" value="/persistifyChecksOnAddTarget.do" />
                <td align="right" nowrap>
                    <jsp:include page="/includes/genPrevNext.jsp" />
                </td>
            </tr>
        </table>
    </div>

    <div class="tableWrapper" style="width:100%;">
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
            <tr valign="middle" class="smallButtons">
                <td class="tableSearchRow">
                    <input type="button" id="remove_button" name="remove_button" value="<webapps:pageText key="remove" type="button" shared="true"/>"  disabled  onClick="javascript:submitActionFromFrames(document.forms.addTargetEditForm, '/persistifyChecksOnAddTarget.do?/addTargetRemovePkg.do?type=package','2');" >
                    <input type="button" id="set_schedule_button" name="set_schedule_button" value="<webapps:pageText key="setSchedule" type="button" shared="true"/>" disabled onClick="javascript:send(document.forms.addTargetEditForm, '/persistifyChecksOnAddTarget.do?/addTargetSchedCommonEdit.do?action=edit');">
                </td>
                <td align="right" class="tableSearchRow">&nbsp;</td>
            </tr>
        </table>
        <jsp:include page="/target/add_targets_details_body.jsp"/>
    </div>
</div>
<div class="formBottom" style="width:100%;">
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
            <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
        </tr>
    </table>
</div>

<div id="endOfGui"></div>
<script>
    resizeDataSection('FOO_dataDiv','endOfGui');
</script>
</html:form>

<script language="JavaScript">
    <%-- Initialize clear_all hidden property value --%>
    setClearAllFromBody();
</script>


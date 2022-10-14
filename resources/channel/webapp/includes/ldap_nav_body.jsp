<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.LDAPVars" %>
<%@ page import = "java.util.Vector" %>
<%@ page import = "com.marimba.tools.ldap.LDAPPagedSearch" %>

<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/common_js.jsp" %>

<jsp:useBean id="session_ldap" class="com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean" scope="session"/>
<bean:define id="multimode" value="defined" toScope="request" />
<jsp:useBean id="session_page" class="com.marimba.apps.subscriptionmanager.webapp.system.PagingBean" scope="session"/>
<bean:define id="formAction" name="formAction" scope="request" />
<bean:define id="ldapPageAction" name="ldapPageAction" scope="request" toScope="request" />
<bean:define id="targetAddAction" name="targetAddAction" scope="request" toScope="request" />
<bean:define id="targetAddMultiAction" name="targetAddMultiAction" scope="request" toScope="request" />


<style type="text/css">
    <!--
    /* These styles are used exclusively for thie page*/
    .col1 {
        width: 25%;
    }
    .col2 {
        width: 75%;
    }
    .searchTabActive {
        background-color: #FFFFFF;
        border-top:1px solid #CCCCCC;
        padding-top:2px;
        padding-bottom:2px;
    }
    .searchTabInactive {
        background-color: #627EB3;
        color: #FFFFFF;
        border-top:none;
        paddoneing-top:2px;
        padding-bottom:2px;
    }
    .searchInnerTabInactive {
        background-color: #F8F8FF;
        border-top:none;
        paddoneing-top:2px;
        padding-bottom:2px;
        font-weight:normal;
    }
    .searchInnerTabActive {
        PADDING-RIGHT:5px;
        BORDER-TOP:#cccccc 1px solid;
        PADDING-LEFT:5px;
        BACKGROUND-COLOR: #ffffff;
        font-weight:bold;
    }
    -->
</style>

<%-- numEntries represents the number of results to display paging results --%>
<% int num_entries = 0; %>
<logic:present name="page_targets_rs" scope="session">
    <% num_entries = ((Vector) session.getAttribute("page_targets_rs")).size();%>
</logic:present>

<logic:present name="page_gen_rs" scope="session">
    <% num_entries = ((Vector) session.getAttribute("page_gen_rs")).size();%>
</logic:present>
<bean:define id="numEntries" toScope="request" type="java.lang.String" value="<%=""+num_entries%>"/>
<%-- javascript for the drop down box --%>
<script language="JavaScript" src="/shell/common-rsrc/js/master.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>
<script language="JavaScript" src="/shell/common-rsrc/js/table.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/domMenu.js"></script>
<script language="JavaScript" type="text/javascript">
    function submitSelected(form) {
        var index = form.elements[4].selectedIndex;
        var startIndex = form.elements[4].options[index].value;
        <!-- window.location.href = "<%= ldapPageAction %>?container=<%= java.net.URLEncoder.encode(session_ldap.getContainer()) %>&startIndex=" + startIndex; -->
        window.location.href = "<%= ldapPageAction %>?container=<%= com.marimba.tools.util.URLUTF8Encoder.encode(session_ldap.getContainer()) %>&startIndex=" + startIndex;
        <!-- Symbio modified 05/19/2005 -->
    }
</script>


<bean:define id="pageBeanName"  toScope="request" type="java.lang.String" value="gen_page_bean" />

<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>

<%-- generic paging tag --%>

<% if (!session_ldap.getUseLDAPPaging()) {
    String listProcessor = IWebAppConstants.APP_PROC_MEMBER;
    String epType = session_ldap.getEntryPoint();

    if ((!"true".equals(session_ldap.getUsersInLDAP()))
            &&(ISubscriptionConstants.PEOPLE_EP.equals(epType) || ISubscriptionConstants.GROUPS_EP.equals(epType)) ) {
        listProcessor = IWebAppConstants.APP_PROC_TXMEMBER;
    }
    if (ISubscriptionConstants.SITES_EP.equals(epType)) {
        listProcessor = IWebAppConstants.APP_PROC_SITE_MEMBER;
    }
	if (ISubscriptionConstants.MDM_DEVICE_GROUP_EP.equals(epType)) {
        listProcessor = IWebAppConstants.APP_PROC_MDM_DEVICEGROUP_MEMBER;
    }
	if (ISubscriptionConstants.MDM_DEVICE_EP.equals(epType)) {
        listProcessor = IWebAppConstants.APP_PROC_MDM_DEVICE_MEMBER;
    }

%>

<sm:setPagingResults formName="bogusForNow" beanName="<%= pageBeanName %>"

                     resultsName="page_gen_rs"

                     displayResultsName="page_targets_rs"

                     listProcessor="<%=listProcessor%>" />

<% } %>

<%-- Javascript --%>

<script language="JavaScript" type="text/javascript">

//e.keyCode         = supported by IE, Opera

//e.which           = supported by Firefox, all mozilla engine based browsers

//e.charCode        = supported by Old Netscape browser

function checkTypingInLDAPNav(submitaction,e){
    var keyCode = e.keyCode ? e.keyCode : e.which ? e.which : e.charCode;
    if (keyCode == 13) {
        if (submitaction == "send") {
            send('search');
        }
        else {
            sendDuplicate('search');
        }
    }
}

function send(action) {
    document.ldapNavigationForm.action.value = action;
    document.ldapNavigationForm.container.value = "<%= com.marimba.tools.util.URLUTF8Encoder.encode(session_ldap.getContainer()) %>";
<%-- saveAutoComplete(document.ldapNavigationForm); --%>
    document.ldapNavigationForm.submit();
}

function sendDuplicate(action) {
    document.ldapNavigationForm.searchString.value = document.ldapNavigationForm.searchString2.value;
    document.ldapNavigationForm.limitSearch.checked = document.ldapNavigationForm.limitSearch2.checked;
    send(action);
}

function setTarget(action, targetName, targetType) {
    document.ldapNavigationForm.action.value = action;
    document.ldapNavigationForm.targetName.value = targetName;
    document.ldapNavigationForm.targetType.value = targetType;
<%-- saveAutoComplete(document.ldapNavigationForm); --%>
    document.ldapNavigationForm.submit() ;
}

function showSearch(type) {
    parent.formObject = document.ldapNavigationForm;
    if(type == "basic") {
        document.ldapNavigationForm.basicLink.value = "basic";
        document.getElementById("basic_cell").className = "searchTabActive";
        document.getElementById("advanced_cell").className = "searchTabInactive";
        document.getElementById("basic_link").style.color = "#000000";
        document.getElementById("basic_link").style.textDecoration = "none";
        document.getElementById("advanced_link").style.textDecoration = "underline";
        document.getElementById("basic_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
        document.getElementById("basic_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
        document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("advanced_link").style.color = "#FFFFFF";
        document.getElementById("basic_section").style.display = "";
        document.getElementById("advanced_section").style.display = "none";
        setCookie("toggleSearchCookie","basic",nextYear);
    } else if (type == "advanced") {
        document.getElementById("advanced_cell").className = "searchTabActive";
        document.getElementById("basic_cell").className = "searchTabInactive";
        document.getElementById("basic_link").style.color = "#FFFFFF";
        document.getElementById("advanced_link").style.color = "#000000";
        document.getElementById("advanced_link").style.textDecoration = "none";
        document.getElementById("basic_link").style.textDecoration = "underline";
        document.getElementById("basic_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("basic_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
        document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
        document.getElementById("basic_section").style.display = "none";
        document.getElementById("advanced_section").style.display = "";
        setCookie("toggleSearchCookie","advanced",nextYear);
        document.ldapNavigationForm.basicLink.value = "advanced";
        if(null != document.getElementById("inner_advanced_cell")){
            document.getElementById("inner_advanced_cell").className = "searchInnerTabActive";
            document.getElementById("inner_policy_cell").className = "searchInnerTabInactive";
            document.getElementById("inner_policy_link").style.color = "#000000";
            document.getElementById("inner_advanced_link").style.color = "#000000";
            document.getElementById("inner_advanced_link").style.textDecoration = "none";
            document.getElementById("inner_policy_link").style.textDecoration = "underline";
            document.getElementById("inner_policy_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_policy_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_advanced_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
            document.getElementById("inner_advanced_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
            document.getElementById("inner_policy_section").style.display = "none";
            document.getElementById("inner_advanced_section").style.display = "";
        }
    }
    else if (type == "hide") {
        document.getElementById("advanced_cell").className = "searchTabInactive";
        document.getElementById("basic_cell").className = "searchTabInactive";
        document.getElementById("basic_link").style.color = "#FFFFFF";
        document.getElementById("advanced_link").style.color = "#FFFFFF";
        document.getElementById("advanced_link").style.textDecoration = "underline";
        document.getElementById("basic_link").style.textDecoration = "underline";
        document.getElementById("basic_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("basic_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
        document.getElementById("basic_section").style.display = "none";
        document.getElementById("advanced_section").style.display = "none";
        setCookie("toggleSearchCookie","hide",nextYear);
        if(null != document.getElementById("inner_advanced_cell")){
            document.getElementById("inner_advanced_cell").className = "searchInnerTabInactive";
            document.getElementById("inner_policy_cell").className = "searchInnerTabInactive";
            document.getElementById("inner_advanced_link").style.textDecoration = "underline";
            document.getElementById("inner_policy_link").style.textDecoration = "underline";
            document.getElementById("inner_advanced_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_advanced_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_policy_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_policy_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_advanced_section").style.display = "none";
            document.getElementById("inner_policy_section").style.display = "none";
        }
    }else if (type == "inner_policy") {
        document.getElementById("advanced_cell").className = "searchTabActive";
        document.getElementById("advanced_link").style.color = "#000000";
        document.getElementById("advanced_link").style.textDecoration = "none";
        document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
        document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
        document.getElementById("advanced_section").style.display = "";
        if(null != document.getElementById("inner_advanced_cell")){
            document.getElementById("inner_advanced_cell").className = "searchInnerTabInactive";
            document.getElementById("inner_advanced_link").style.color = "#000000";
            document.getElementById("inner_advanced_link").style.textDecoration = "underline";
            document.getElementById("inner_advanced_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_advanced_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_advanced_section").style.display = "none";
            document.ldapNavigationForm.basicLink.value = "policy";
            document.getElementById("inner_policy_cell").className = "searchInnerTabActive";
            document.getElementById("inner_policy_link").style.color = "#000000";
            document.getElementById("inner_policy_link").style.textDecoration = "none";
            document.getElementById("inner_policy_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
            document.getElementById("inner_policy_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
            document.getElementById("inner_policy_section").style.display = "";
        }
        setCookie("toggleSearchCookie","inner_policy",nextYear);
    }else if (type == "inner_advanced") {
        document.getElementById("advanced_cell").className = "searchTabActive";
        document.getElementById("advanced_link").style.color = "#000000";
        document.getElementById("advanced_link").style.textDecoration = "none";
        document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
        document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
        document.getElementById("advanced_section").style.display = "";
        document.ldapNavigationForm.basicLink.value = "advanced";
        if(null != document.getElementById("inner_advanced_cell")){
            document.getElementById("inner_advanced_cell").className = "searchInnerTabActive";
            document.getElementById("inner_advanced_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
            document.getElementById("inner_advanced_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
            document.getElementById("inner_advanced_link").style.color = "#000000";
            document.getElementById("inner_advanced_link").style.textDecoration = "none";
            document.getElementById("inner_advanced_section").style.display = "";
            document.getElementById("inner_policy_cell").className = "searchInnerTabInactive";
            document.getElementById("inner_policy_link").style.color = "#000000";
            document.getElementById("inner_policy_link").style.textDecoration = "underline";
            document.getElementById("inner_policy_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_policy_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("inner_policy_section").style.display = "none";
        }
        setCookie("toggleSearchCookie","inner_advanced",nextYear);
    }
}
</script>


<body bgcolor="white" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="resizeDataSection('dataSection','endOfGui');">
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<%-- Body content --%>
<html:form name="ldapNavigationForm" action="<%= (String)formAction %>" type="com.marimba.apps.subscriptionmanager.webapp.forms.LDAPNavigationForm">
    <html:hidden property="action" />
    <html:hidden property="container" />
    <html:hidden property="targetName" />
    <html:hidden property="targetType" />

    <table width="95%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>

    <%-- LDAP Table Title --%>
    <jsp:include page="/includes/ldap_title.jsp" />

    <%-- include the main portion of the ldap navigation table --%>
    <jsp:include page="/includes/ldap_nav.jsp" />

</html:form>
<div id="endOfGui"></div>
<script>
    resizeDataSection('dataSection','endOfGui');
</script>


</body>
</html>


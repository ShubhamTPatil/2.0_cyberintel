<%@ page import = "java.util.List,

                   com.marimba.apps.subscriptionmanager.webapp.util.DMHelperUtils" %>

<%@ page import = "java.net.URLEncoder" %>

<%@ page import = "com.marimba.apps.subscription.common.objects.Target" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import ="com.marimba.intf.msf.acl.IAclConstants" %>
<%@ page import="com.marimba.webapps.intf.IWebAppsConstants" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscription.common.intf.IUser" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.ObjectManager" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.approval.ApprovalUtils" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<logic:notPresent name="session_multitgbool">

    <jsp:include page="/includes/push_drop_down_menu.jsp"/>

</logic:notPresent>


<%-- logic:equal parameter="targetsched" value="defined">

<bean:define id="targetsched" value="defined" scope="request" />

</logic:equal>

<logic:equal parameter="session_multitgbool" value="true">

<bean:define id="session_multitgbool" value="true" scope="request" />

</logic:equal --%>
<bean:define id="pageStateAction" name="pageStateAction" toScope="request" />
<bean:define id="tgForm" name="tgForm" toScope="request" />

<%
    List<Target> sameUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_SAMEUSER);
    List<Target> diffUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_DIFFUSER);

    IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
    IUser iuser = (IUser) request.getSession().getAttribute(IWebAppConstants.SESSION_SMUSER);

    ServletContext context = config.getServletContext();

    SubscriptionMain main = TenantHelper.getTenantSubMain(context, request.getSession(), user.getTenantName());

    session.setAttribute("cmsMode", main.getCMSMode());

    boolean isApprovalPolicy = main.isPeerApprovalEnabled();
    boolean hasApproverPermission = ApprovalUtils.hasApproverPermission(user, main);
    boolean hasPolicyWritePermisssion = false;

    String pageStatePath = request.getContextPath() + "/" + pageStateAction + ".do";
    String clearAndSortPath = pageStatePath + "?";
    String targetName = "";

    try {

        Target singleTarget = (session.getAttribute("main_page_target")!=null?(Target) ((List) session.getAttribute("main_page_target")).get(0):null);

        String pushConfig = main.getPushDeploymentFileName();

        if (singleTarget != null) {
            if(isApprovalPolicy) {
                main.updatePendingPolicySessionVar(request, singleTarget);
                sameUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_SAMEUSER);
                diffUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_DIFFUSER);
            }
            targetName = 	singleTarget.getName();
            String classType = DMHelperUtils.getDeploymentStateFromDN(pushConfig, singleTarget.getId(), main.getTenantName(), main.isPushEnabled());

            if (classType != null) {  session.setAttribute("classType", classType);}
            if(isApprovalPolicy && hasApproverPermission) {
                try {
                    hasPolicyWritePermisssion = ObjectManager.hasSubPerm(iuser,	singleTarget.getId(),	singleTarget.getType(), IAclConstants.WRITE_ACTION);
                } catch (Exception ex) {

                }
            }
        }
        request.setAttribute("singleTarget", singleTarget);
%>
<%--<bean:define id="singleTargetpush" name="s1" scope="request" type="com.marimba.apps.subscription.common.objects.Target" toScope="request" />--%>


<script>

    function setActiveBg(T,s) {

        T.style.backgroundColor = (s) ? '#FDFDFD' : '';

    }
    function redirectPendingPolicy(policyName) {
        var submitaction = "/policyStatus.do?command=viewpendingpolicy&policyName="+policyName;
        var fullpath = "<html:rewrite page='" + submitaction + "' />";
        top.location.href = fullpath;
    }

    //While load the result page in the same iframe [current frame]
    //The other pages are using the existing function from common_js.js file

    function submitActionFromFrames0(form, url, frameNum) {
        form.target = parent.frames[frameNum].name;
        // Need to check whether the required iframe is available, if not set the current frame as for the target frame
        // This is used at the time of delete packages
        form.target = "";
        send(form, url);
    }

    addSpecialColumn("contentColumn",0.5);

    addSpecialColumn("packageName",2);

</script>

<%-- Table with Overview/Details options --%>


<div style="width:100% ">
<table width="100%" border="0" cellspacing="0" cellpadding="0">

<tr valign="middle" class="smallButtons">

<td class="tableSearchRow">
    <logic:notPresent name="targetsched">

        <% request.setAttribute("targetFrame", "1"); %>

        <logic:notPresent name="session_multitgbool">

            <span><strong><webapps:pageText shared="true" type="target_details" key="Basic_View" /></strong>  | 	</span>

            <a style="color: #000000;" href="javascript:send(<%="document.forms." +(String)tgForm%>,'/viewSchedDetails.do?page=current');"><webapps:pageText shared="true" type="target_details" key="Details_View" /></a>
            <% if(isApprovalPolicy) {
                if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) || (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {
                    if(hasApproverPermission) {
                        if(hasPolicyWritePermisssion) {
            %><span> |</span>&nbsp;&nbsp;<a style="color: #000000;" href="javascript:redirectPendingPolicy('<%=targetName%>');"><webapps:pageText shared="true" type="target_details" key="approvalpendingview" /></a>
            &nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>

            <%} else {
            %>&nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>
            <%}
            } else {%>
            &nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>
            <%}
            }
            }
            %>
        </logic:notPresent>

        <%-- submit to different action if multi-select mode --%>

        <logic:present name="session_multitgbool">

            <span><strong><webapps:pageText shared="true" type="target_details" key="Basic_View" /></strong>  |    	</span>

            <a style="color: #000000;" href="javascript:send(<%="document.forms."+(String)tgForm%>,'/viewSchedDetailsMulti.do?page=current');"><webapps:pageText shared="true" type="target_details" key="Details_View" /></a>
            <% if(isApprovalPolicy) {
                if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) || (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {
            %>
            &nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>
            <%}
            }
            %>
        </logic:present>

    </logic:notPresent>

    <logic:present name="targetsched">

        <a style="color: #000000;" href="javascript:send(<%="document.forms."+(String)tgForm%>,'/main_view.jsp?page=current');"><webapps:pageText shared="true" type="target_details" key="Basic_View" /></a>

        |  <span><strong><webapps:pageText shared="true" type="target_details" key="Details_View" /></strong></span>
        <logic:notPresent name="session_multitgbool">
            <% if(isApprovalPolicy) {
                if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) || (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {
                    if(hasApproverPermission) {
                        if(hasPolicyWritePermisssion) {
            %><span> |</span>&nbsp;&nbsp;<a style="color: #000000;" href="javascript:redirectPendingPolicy('<%=targetName%>');"><webapps:pageText shared="true" type="target_details" key="approvalpendingview" /></a>
            &nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>
            <%} else {
            %>&nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>
            <%}
            } else {%>
            &nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>
            <%}
            }
            }%>
        </logic:notPresent>
        <logic:present name="session_multitgbool">
            <% if(isApprovalPolicy) {
                if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) || (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {
            %>
            &nbsp;&nbsp;<a href="#" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="target_details" key="approvalpending" />', DELAY, '200', WIDTH, '125', LEFT);" onmouseout="return nd();"><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle"></a>
            <%}
            }%>
        </logic:present>

    </logic:present>

</td>

<td class="tableSearchRow" align="right">

<table border="0" cellpadding="0" cellspacing="0">

<tr>


<logic:notPresent name="<%=com.marimba.apps.subscriptionmanager.intf.IWebAppConstants.INVALID_NONEXIST%>">



    <logic:present name="aclwrite">

        <td align="center" valign="top" class="smallCaption"  style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)"

                <logic:notPresent name="session_multitgbool">

                    onClick="javascript:send(<%="document.forms." +tgForm%>,'/distEdit.do');"

                </logic:notPresent>

                <logic:present name="session_multitgbool">

                    onClick="javascript:send(<%="document.forms."+tgForm%>,'/distMultiEdit.do');"

                </logic:present>

                />

        <img src="/spm/images/edit.gif" width="16" height="16" border="0" id="edit_btn" alt="<webapps:pageText shared="true" type="target_details" key="Edit_Assignment"/>"/>

        <br>&nbsp;&nbsp;<webapps:pageText shared="true" type="target_details" key="Edit_Assignment" />&nbsp;&nbsp;
        </td>

    </logic:present>

    <logic:notPresent name="session_multitgbool">
        <!-- Push available only for single targets -->

        <logic:present name="aclwrite">

            <!-- Copy Button-->

            <td align="center" valign="top" class="smallCaption"  style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)"

                    <logic:notPresent name="targetsched">

                        onClick="javascript:submitActionFromFrames0(<%="document.forms." +tgForm%>,'/copyExist.do','1')"

                    </logic:notPresent>

                    <logic:present name="targetsched">

                        onClick="javascript:send(<%="document.forms." +tgForm%>,'/copyDetailExist.do')"

                    </logic:present>

                    />

            <img src="/spm/images/copy.gif" width="16" height="16" border="0" id="copy_btn" alt="<webapps:pageText shared="true" type="target_details" key="Copy_Assignment"/>" />

            <br>&nbsp;&nbsp;<webapps:pageText shared="true" type="target_details" key="Copy"/>&nbsp;&nbsp;

            </td>

            <!--Update Now button not applicable for cloud mode-->
            <logic:notEqual name="cmsMode" value="cloud" scope="session">
                <td align="center" style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999;" class="buttonMenu" id="file_MenuTitle" onClick="hightlightMenu(this,'#000099')" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)">
                    <logic:present name="classType">
                        <logic:equal name="classType" value="DEFAULT">
                            <jsp:include page="/push/includes/upd_cur_default.jsp" />
                        </logic:equal>

                        <logic:equal name="classType" value="CURRENT_UPDATING">
                            <jsp:include page="/push/includes/upd_cur_updating.jsp" />
                        </logic:equal>

                        <logic:equal name="classType" value="CURRENT_DONE">
                            <jsp:include page="/push/includes/upd_cur_done.jsp" />
                        </logic:equal>

                        <logic:equal name="classType" value="OTHER_UPDATING_NOLAST">
                            <jsp:include page="/push/includes/upd_other_updating_nolast.jsp" />
                        </logic:equal>

                        <logic:equal name="classType" value="OTHER_UPDATING">
                            <jsp:include page="/push/includes/upd_other_updating.jsp" />
                        </logic:equal>

                        <logic:equal name="classType" value="NOT_CONFIGURED">
                            <jsp:include page="/push/includes/upd_not_configured.jsp" />
                        </logic:equal>
                    </logic:present>
                </td>
            </logic:notEqual>

        </logic:present>

    </logic:notPresent>
</logic:notPresent>


<!--Delete button-->

<logic:present name="aclwrite">

    <td align="center" valign="top" class="smallCaption" style="border-left:1px solid #999999;border-top:1px solid #999999;border-bottom:1px solid #999999;border-right:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)"

            <logic:notPresent name="session_multitgbool">

                <logic:notPresent name="targetsched">

                    onClick="javascript:submitActionFromFrames0(<%="document.forms." +tgForm%>,'/distDelete.do','1')"

                </logic:notPresent>

                <logic:present name="targetsched">

                    onClick="javascript:send(<%="document.forms." +tgForm%>,'/distDeleteFromDetails.do')"

                </logic:present>

            </logic:notPresent>

            <logic:present name="session_multitgbool">

                <logic:notPresent name="targetsched">

                    onClick="javascript:submitActionFromFrames0(<%="document.forms."+tgForm%>,'/distDeleteMulti.do','1')"

                </logic:notPresent>

                <logic:present name="targetsched">

                    onClick="javascript:send(<%="document.forms."+tgForm%>,'/distDeleteMultiFromDetails.do')"

                </logic:present>

            </logic:present>

            />

    <img src="/shell/common-rsrc/images/trash.gif" width="16" height="16" border="0" alt="<webapps:pageText shared="true" type="target_details" key="Delete_Assignment"/>" id="delete_btn" />

    <br>&nbsp;&nbsp;<webapps:pageText shared="true" type="target_details" key="Delete" />&nbsp;&nbsp;
    </td>

</logic:present>


<td><img src="/shell/common-rsrc/images/invisi_shim.gif" width="5"></td>


<!--Compliance button-->
<logic:notPresent name="<%=com.marimba.apps.subscriptionmanager.intf.IWebAppConstants.INVALID_NONEXIST%>">
    <logic:notPresent name="aclread">

        <logic:notPresent name="session_multitgbool">

            <logic:present name="main_page_target">

                <!-- <td align="center" class="smallCaption" style="border:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)" onClick="javascript:redirect('<%= "/pcTargetView.do?src=tgtview&forward=" + java.net.URLEncoder.encode("/pcDisplayTarget.do?name=" + singleTarget.getName() + "&targetType=" + singleTarget.getType() + "&id=" + singleTarget.getId()) %>');"> -->
                <td align="center" class="smallCaption" style="border:1px solid #999999; cursor:pointer;" onmouseover="setActiveBg(this,true)" onmouseout="setActiveBg(this,false)" onClick="javascript:redirect('<%= "/compTgtView.do?src=tgtview&name=" + com.marimba.tools.util.URLUTF8Encoder.encode( singleTarget.getName() )+ "&targetType=" + singleTarget.getType() + "&id=" + com.marimba.tools.util.URLUTF8Encoder.encode( singleTarget.getId() )  %>');">
                    <!-- Symbio modified 05/19/2005 -->

                    <img src="<webapps:fullPath path="/images/compliance.gif" />" width="16" height="16" alt="<webapps:pageText shared="true" type="target_details" key="View_Compliance"/>"  ><br><webapps:pageText shared="true" type="target_details" key="Compliance"/>
                </td>

            </logic:present>

        </logic:notPresent>

    </logic:notPresent>

</logic:notPresent>
</tr>

</table>


</td>

</tr>

</table>

</div>


<div class="headerSection" style="width:100%; overflow:hidden; text-align:left;" id="FOO_headerDiv">

<table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable">

<%-- basic view --%>
<logic:notPresent name="targetsched">
    <%-- Single mode --%>
    <logic:notPresent name="session_multitgbool">
        <colgroup width="10%"></colgroup>
        <colgroup width="30%"></colgroup>
        <colgroup width="20%"></colgroup>
        <colgroup width="20%"></colgroup>
        <colgroup width="20%"></colgroup>
    </logic:notPresent>
    <%-- Multiple mode --%>
    <logic:present name="session_multitgbool">
        <colgroup width="10%"></colgroup>
        <colgroup width="40%"></colgroup>
        <colgroup width="25%"></colgroup>
        <colgroup width="25%"></colgroup>
    </logic:present>
</logic:notPresent>

<%-- details view --%>
<logic:present name="targetsched">
    <%-- Single mode --%>
    <logic:notPresent name="session_multitgbool">
        <colgroup width="10%"></colgroup>
        <colgroup width="20%"></colgroup>
        <colgroup width="10%"></colgroup>
        <colgroup width="10%"></colgroup>
        <colgroup width="10%"></colgroup>
        <colgroup width="10%"></colgroup>
        <colgroup width="10%"></colgroup>
        <colgroup width="10%"></colgroup>
        <colgroup width="10%"></colgroup>
    </logic:notPresent>
    <%-- Multiple mode --%>
    <logic:present name="session_multitgbool">
        <colgroup width="5%"></colgroup>
        <colgroup width="20%"></colgroup>
        <colgroup width="15%"></colgroup>
        <colgroup width="15%"></colgroup>
        <colgroup width="15%"></colgroup>
        <colgroup width="15%"></colgroup>
        <colgroup width="15%"></colgroup>
    </logic:present>
</logic:present>

<thead>

<tr id="FOO_headerTable_firstRow">


<%-- REMIND t3

    Write a tag to handle the sort links

    Tag : td cell active class, td cell inactive class, href class,

    bean containing sort order

    Tag decides whether it is ascending of descending

    String Property name obtained from the strings attached to the current page

    sort_up.gif and sort_down.gif is used

    --%>


<%-- Column header for content type--%>

<td class="tableHeaderCell" id="contentColumn">

    <logic:notPresent name="<%=(String)tgForm%>" property="value(sortorder)">

        <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=false&sorttype=tgmap_contenttype&lastsort=tgmap_contenttype"%>' class="columnHeading"><webapps:pageText shared="true" type="colhdr" key="contentType" /></a>

    </logic:notPresent>


    <logic:present name="<%=(String)tgForm%>" property="value(sortorder)">
        <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

            <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=false&sorttype=tgmap_contenttype&lastsort=tgmap_contenttype"%>' class="columnHeading"><webapps:pageText shared="true" type="colhdr" key="contentType" /></a>

            <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="tgmap_contenttype">

                <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

            </logic:equal>

        </logic:equal>

        <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

            <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=true&sorttype=tgmap_contenttype&lastsort=tgmap_contenttype"%>' class="columnHeading"><webapps:pageText shared="true" type="colhdr" key="contentType" /></a>

            <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="tgmap_contenttype">

                <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

            </logic:equal>

        </logic:equal>

    </logic:present>

</td>


<%-- Column header for title of the packages. --%>

<td class="tableHeaderCell" id="packageName">

    <logic:notPresent name="<%=(String)tgForm%>" property="value(sortorder)">
        <logic:notEqual name="<%=(String)tgForm%>" property="value(showurl)" value="show">

            <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=title&lastsort=title" %>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="Packages" /></a>&nbsp;<img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

        </logic:notEqual>

        <logic:equal name="<%=(String)tgForm%>" property="value(showurl)" value="show">

            <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=url&lastsort=url" %>' class="columnHeading">URL</a>&nbsp;<img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

        </logic:equal>

    </logic:notPresent>


    <logic:present name="<%=(String)tgForm%>" property="value(sortorder)">
        <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">
            <logic:notEqual name="<%=(String)tgForm%>" property="value(showurl)" value="show">

                <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=false&sorttype=title&lastsort=title" %>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="Packages" /></a>

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="title">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

                </logic:equal>

            </logic:notEqual>

            <logic:equal name="<%=(String)tgForm%>" property="value(showurl)" value="show">

                <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=url&lastsort=url"%>' class="columnHeading">URL</a>

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="url">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

        </logic:equal>

        <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">
            <logic:notEqual name="<%=(String)tgForm%>" property="value(showurl)" value="show">

                <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=title&lastsort=title"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="Packages" /></a>

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="title">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

                </logic:equal>

            </logic:notEqual>

            <logic:equal name="<%=(String)tgForm%>" property="value(showurl)" value="show">

                <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=true&sorttype=url&lastsort=url"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="URL" /></a>&nbsp;

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="url">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

        </logic:equal>

    </logic:present>

</td> <%-- End column header for title of the packages. --%>


<%-- Column Header for installation priority. Present in target details page only--%>

<logic:present name="targetsched">

    <logic:notPresent name="session_multitgbool">

        <td class="tableHeaderCell" >

            <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

                <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=false&sorttype=installPriority&lastsort=installPriority"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="InstallPriority" /></a>

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="installPriority">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

            <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

                <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=true&sorttype=installPriority&lastsort=installPriority"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="InstallPriority" /></a>&nbsp;

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="installPriority">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

        </td>

    </logic:notPresent>

</logic:present>




<logic:notPresent name="targetsched">


    <%-- This section deals with the install state.  This will sort according to the primary

                         state of the channel in the policy.  The install state column will only be displayed

                         if the target sched details request variable is not set.

                         --%>

    <td class="tableHeaderCell" >

        <logic:notPresent name="<%=(String)tgForm%>" property="value(sortorder)">

            <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=state"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="PrimaryInstallState" /></a>

        </logic:notPresent>

        <logic:present name="<%=(String)tgForm%>" property="value(sortorder)">
            <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

                <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=false&sorttype=state&lastsort=state"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="PrimaryInstallState" /></a>

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="state">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

            <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

                <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=true&sorttype=state&lastsort=state"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="PrimaryInstallState" /></a>

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="state">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

        </logic:present>

    </td>

    <%-- end install state --%>


    <%-- This section deals with the secondary state.  This will sort according to the install state

                      state of the channel in the policy.  The install state column will only be displayed

                      if the target sched details request variable is not set.

                      --%>

    <td class="tableHeaderCell" >

        <logic:notPresent name="<%=(String)tgForm%>" property="value(sortorder)">

            <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=secState&lastsort=secState"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="SecondaryInstallState" /></a>

        </logic:notPresent>


        <logic:present name="<%=(String)tgForm%>" property="value(sortorder)">
            <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

                <a class="columnHeading" href='<%=clearAndSortPath + "sortorder=false&sorttype=secState&lastsort=secState"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="SecondaryInstallState" /></a>

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="secState">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

            <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

                <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=secState&lastsort=secState"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="SecondaryInstallState" /></a>&nbsp;

                <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="secState">

                    <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

                </logic:equal>

            </logic:equal>

        </logic:present>

    </td>


</logic:notPresent>  <%-- This ends the check for targetsched --%>




<logic:present name="targetsched">


<%-- This shows the column for the primary installation on the target schedule details --%>

<td class="tableHeaderCell">

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=initSched&lastsort=initSched"%>' class="columnHeading">

            <webapps:pageText shared="true" type="target_details" key="PrimaryInstallSchedule" /></a>

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="initSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=initSched&lastsort=initSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="PrimaryInstallSchedule" /></a>

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="initSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

</td>


<%-- Column heading for the Secondary Install State --%>

<td class="tableHeaderCell">

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=secSched&lastsort=secSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="SecondaryInstallSchedule" /></a>

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="secSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=secSched&lastsort=secSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="SecondaryInstallSchedule" /></a>&nbsp;

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="secSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

</td>


<%-- Column heading for Update Schedule --%>

<td class="tableHeaderCell">

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=updateSched&lastsort=updateSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="UpdateSchedule" /></a>

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="updateSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=updateSched&lastsort=updateSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="UpdateSchedule" /></a>

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="updateSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

</td>


<%-- Column heading for the Verify Repair schedule --%>

<td class="tableHeaderCell" >

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=verRepairSched&lastsort=verRepairSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="VerifyRepairSchedule" /></a>&nbsp;

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="verRepairSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=verRepairSched&lastsort=verRepairSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="VerifyRepairSchedule" /></a>&nbsp;

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="verRepairSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

</td>

<td class="tableHeaderCell" >

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=postponeSched&lastsort=postponeSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="PostponeSchedule" /></a>&nbsp;

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="postponeSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

    <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

        <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=postponeSched&lastsort=postponeSched"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="PostponeSchedule" /></a>&nbsp;

        <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="postponeSched">

            <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

        </logic:equal>

    </logic:equal>

</td>


</logic:present> <%-- This is the end of the check to target schedule request bean --%>


<%-- If in single select mode, give the option to view the indirect channels --%>

<logic:notPresent name="session_multitgbool" >

    <td class="tableHeaderCell" >

        <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="true">

            <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=false&sorttype=directlyassigned&lastsort=directlyassigned"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="DirectlyAssignedTo" /></a>

            <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="directlyassigned">

                <img src="<webapps:fullPath path="/common-rsrc/images/sort_up.gif" />" width="7" height="6">

            </logic:equal>

        </logic:equal>

        <logic:equal name="<%=(String)tgForm%>" property="value(sortorder)" value="false">

            <a class="columnHeading" href='<%= clearAndSortPath + "sortorder=true&sorttype=directlyassigned&lastsort=directlyassigned"%>' class="columnHeading"><webapps:pageText shared="true" type="target_details" key="DirectlyAssignedTo" /></a>

            <logic:equal name="<%=(String)tgForm%>" property="value(lastsort)" value="directlyassigned">

                <img src="<webapps:fullPath path="/common-rsrc/images/sort_down.gif" />" width="7" height="6">

            </logic:equal>

        </logic:equal>

    </td>

</logic:notPresent>

</tr>

</thead>

</table>

</div><!--end headerSection-->


<%

    }

    catch(Exception ex1)

    {

        out.println("Error2:");

        out.println(ex1.toString());

    }

%>





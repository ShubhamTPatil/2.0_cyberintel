<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)copy_assignment_preview.jsp

     @author Jayaprakash Paramasivam
     @version 1.0, 31/12/2004
--%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%@ page import = "java.util.*,
                   com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Channel" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Target" %>

<jsp:useBean id="session_dist" class="com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean" scope="session"/>
<jsp:useBean id="session_copy" class="com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean" scope="session"/>
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
<webapps:helpContext context="sm" topic="policy_preview" />

<title><webapps:pageText key="m6" type="global"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">
<script language="javascript">

    <%--
      If initial install state is not 'Stage' or 'Available', secondary
      install state pull down is disabled.  The secondary states are set
      accordingly.
    --%>

    function stateChange(key, curSecState, init) {
        var stateObj;
        var secStateObj;

        if (hasDHTML()) {
            stateObj = document.getElementById("state#" + key);
            secStateObj =  document.getElementById("secState#" + key);
        } else {
            stateObj = eval("document.distAsgForm.state#" + key);
            secStateObj = eval("document.distAsgForm.secState#" + key);
        }

        if (stateObj.value == 'subscribe_noinstall' || stateObj.value == 'available') {
            secStateObj.disabled = false;
            if (init) {
                // If this method is called when a page is intialized, we want to
                // set the secondary stage to blank as the default if
                // there isn't already a secondary state assigned (if there's
                // a value, the widget would have set it so we do nothing).
                if (curSecState == '') {
                    secStateObj.value = '';
                }
            } else {
                // If initial state is not 'Stage' or 'Available' due to
                // a user switching initial state, we set the default to 'Install'.
                secStateObj.value = 'subscribe';
            }
        } else {
            secStateObj.disabled = true;
            secStateObj.value = '';
        }
    }

    // This function saves the form attributes to the DistributionBean,
    // before forwarding to the next page.
    // IMPORTANT: Always use to navigate between pages so that the changes are
    // persistified in the session bean.
    function saveState(forwardaction) {
        document.distAsgForm.forward.value = forwardaction;
        send(document.distAsgForm, '/distSetStates.do');
    }

</script>
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import="com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%@ page import="com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import="org.apache.struts.util.MessageResources" %>
<%@ page import="com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
    List<Target> sameUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_SAMEUSER);
    List<Target> diffUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_DIFFUSER);

%>


<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<% if(null != EmpirumContext) {%>
<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>

<html:form name="copyEditForm" action="/copySaveTarget.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.CopyEditForm">
    <html:hidden property="forward" />

<div style="text-align:center;">
<div style="padding-left:25px; padding-right:25px;">
    <div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
    <logic:present name="policy_exists">
        <div class="statusMessage" id="warning">
            <h6>&nbsp;</h6>

            <p><webapps:pageText key="Warningbefore"/>
                <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
                <webapps:pageText key="Warningafter"/></p>
        </div>
    </logic:present>
    <logic:present name="session_multitgbool">
    <% if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) && (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {%>
    <p>
        <webapps:pageText key="approval_both_Warning"/>
    </p>
</div>
<%} else { %>
<% if(null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) {%>
<div class="statusMessage" id="warning">
    <h6>&nbsp;</h6>
    <p>
        <webapps:pageText key="approval_suser_Warning"/>
    </p>
</div>
<%} else { %>
<% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
<div class="statusMessage" id="warning">
    <h6>&nbsp;</h6>
    <p>
        <webapps:pageText key="approval_duser_Warning"/>
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
            <webapps:pageText key="approval_suser_Warning"/>
        </p>
    </div>
    <%} else { %>
    <% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p>
            <webapps:pageText key="approval_duser_Warning"/>
        </p>
    </div>
    <%}} %>
</logic:notPresent>
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
            <td nowrap valign="top" align="right" ><webapps:pageText key="copyfrom" type="colhdr" shared="true"/>: </td>
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
    <table cellspacing="0" cellpadding="2" border="0">
        <tr></tr>
        <tr>
            <td nowrap valign="top" align="right" >&nbsp;&nbsp;&nbsp;&nbsp;<webapps:pageText key="copyto" type="colhdr" shared="true"/>: </td>
            <jsp:include page="/copy/copy_target_display.jsp" />
        </tr>
    </table>
</div>

<div valign="top" align="left" style="margin-bottom:8px; " class="tableTitle"><webapps:pageText key="Heading" /></div>
<webapps:formtabs tabset="copyPreview" tab="pkg" />
<div class="formContent">
<div class="tableWrapper" style="width:100%; overflow:hidden; margin-top:5px;">
<div class="headerSection" style="width:100%; text-align:left;" id="FOO_headerDiv">
    <table width="100%" border="0" cellpadding="0" cellspacing="0" id="FOO_headerTable">
        <colgroup width="">
        </colgroup>
        <colgroup width="">
        </colgroup>
        <colgroup width="">
        </colgroup>
        <colgroup width="">
        </colgroup>
        <colgroup width="">
        </colgroup>
        <colgroup width="">
        </colgroup>
        <colgroup width="">
        </colgroup>
        <colgroup width="">
        </colgroup>
        <thead>
            <tr>
                <td align="center" class="tableHeaderCell" width="5%"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="11" height="1"></td>
                <td align="center" class="tableHeaderCell" width="5%"><webapps:pageText key="pound" type="colhdr" shared="true"/></a></td>
                <td class="tableHeaderCell" width="20%"><webapps:pageText key="pkgs" type="colhdr" shared="true"/></a></td>
                <td nowrap class="tableHeaderCell" width="20%"><webapps:pageText key="priState" type="colhdr" shared="true"/></a></td>
                <td nowrap class="tableHeaderCell" width="20%"><webapps:pageText key="secState" type="colhdr" shared="true"/></a></td>
                <td class="tableHeaderCell" width="10%"><webapps:pageText key="exempt" type="colhdr" shared="true"/></a></td>
                <td class="tableHeaderCell" width="10%"><webapps:pageText key="wow" type="colhdr" shared="true"/></a></td>
                <td class="tableHeaderCell" width="10%"><webapps:pageText key="UCD" type="colhdr" shared="true"/></a></td>
            </tr>
        </thead>
    </table>
</div>
<!--end headerSection-->
<div id="FOO_dataDiv" style="height:120px; width:100%; overflow:auto; text-align:left;" onscroll="syncScroll('FOO');">
<table width="100%" border="0" cellpadding="0" cellspacing="0" id="FOO_dataTable">
<colgroup width="">
</colgroup>
<colgroup width="">
</colgroup>
<colgroup width="">
</colgroup>
<colgroup width="">
</colgroup>
<colgroup width="">
</colgroup>
<colgroup width="">
</colgroup>
<colgroup width="">
</colgroup>
<colgroup width="">
</colgroup>

<bean:size id="numChannels" name="session_dist" property="applicationChannels"/>
<% int contentsRowCount = 0; %>
<logic:iterate id="app" name="session_dist" property="applicationChannels" indexId="indexId">
<% indexId = new Integer(indexId.intValue() + 1); %>
<bean:define id="index" value='<%= indexId.toString() %>' />
<tbody id='<%= "row1-" + index %>'>
    <% if (contentsRowCount % 2 == 0){ %>
    <tr>
            <% } else { %>
    <tr class="alternateRowColor">
        <% } %>
        <td align="center" class="rowLevel1" width="5%">
            <a href="javascript:toggleSection('<%= "row1-" + index %>')"><img border="0" id='<%= "widget-row1-" + index %>' src="/shell/common-rsrc/images/list_arrow_c.gif" width="11" height="11" class="widget"></a>
        </td>
        <td align="center" class="rowLevel1" width="5%">
            <logic:greaterEqual name="app" property="order" value="99999">
                <webapps:pageText key="NA"/>
            </logic:greaterEqual>
            <logic:lessThan name="app" property="order" value="99999">
                <bean:write name="app" property="order" filter="true"/>
            </logic:lessThan>
        </td>
        <td class="rowLevel1" width="20%">
            <a href="javascript:void(0);" class="noUnderlineLink" style="cursor:help;" onmouseover="return Tip('<webapps:stringescape> <bean:write name="app" property="url" filter="false"/> </webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();">
                <img src="/shell/common-rsrc/images/package.gif" border="0" />
                <bean:write name="app" property="title" filter="true"/>
            </a>
        </td>
        <td class="rowLevel1" width="20%">
            <webapps:pageText key='<%= ((Channel) app).getState() + ".uppercase" %>' type="global"/>
        </td>
        <td class="rowLevel1" width="20%">
            <webapps:pageText key='<%= ((Channel) app).getSecState() + ".uppercase" %>' type="global"/>
        </td>
        <td class="rowLevel1" width="10%">
            <logic:match name="app" property="exemptFromBlackout" value="true">
                <webapps:pageText key="yes" shared="true" type="global"/>
            </logic:match>
            <logic:match name="app" property="exemptFromBlackout" value="false">
                <webapps:pageText key="no" shared="true" type="global"/>
            </logic:match>
        </td>
        <td class="rowLevel1" width="10%">
            <%if (isWoWApplicable) {%>
            <logic:match name="app" property="wowEnabled" value="true"><webapps:pageText key="yes" shared="true" type="global"/></logic:match>
            <logic:match name="app" property="wowEnabled" value="false"><webapps:pageText key="no" shared="true" type="global"/></logic:match>
            <%} else {out.print(NA);}%>

        </td>
        <td class="rowLevel1" width="10%">
            <%
                String ucdTmplts =  ((Channel) app).getUcdTemplates();
                out.print((null == ucdTmplts || ucdTmplts.isEmpty()) ? NA : ucdTmplts);
            %>
        </td>

    </tr>
</tbody>
<tbody id='<%= "row1-" + index + "_1"%>' style="display:none;">
<tr>
<td class="rowLevel1" colspan="8"><table width="100%" border="0" cellspacing="0" cellpadding="3">
<colgroup span="2" width="50%"></colgroup>
    <%-- row 1 starts --%>
<tr>
    <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
        <tr>
            <td class="tableRowActions" style="height:20px;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="textWhite"><strong><webapps:pageText key="priStateTitle"/></strong> </td>
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
    <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
        <tr>
            <td class="tableRowActions" style="height:20px;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="textWhite"><strong><webapps:pageText key="updateTitle"/></strong> </td>
                </tr>
            </table>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <logic:present name="app" property="updateScheduleString">
                <logic:equal name="app" property="updateScheduleString" value="inconsistent">
                <p><img src="/shell/common-rsrc/images/alert_sm.gif"  width="16" height="16">
                    </logic:equal>
                    </logic:present>
                        <sm:scheduleDisplay name="app" property="updateScheduleString" schedule='<%= ((Channel) app).getUpdateScheduleString() %>' type="update" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                <p>&nbsp;</p>
            </td>
        </tr>
    </table>
    </td>
</tr>
    <%-- row 2 starts --%>
<tr>
        <%-- cut --%>
    <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
        <tr>
            <td class="tableRowActions" style="height:20px;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td height="21" class="textWhite"><strong><webapps:pageText key="secStateTitle"/></strong> </td>
                </tr>
            </table>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <logic:present name="app" property="secScheduleString">
                <logic:equal name="app" property="secScheduleString" value="inconsistent">
                <p><img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                    </logic:equal>
                    </logic:present>
                        <sm:scheduleDisplay name="app" property="secScheduleString" schedule='<%= ((Channel) app).getSecScheduleString() %>' type="secondary" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                <p>&nbsp;</p>
            </td>
        </tr>
    </table>
    </td>
    <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
        <tr>
            <td class="tableRowActions" style="height:20px;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="textWhite"><strong><webapps:pageText key="verifyTitle"/></strong> </td>
                </tr>
            </table>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <logic:present name="app" property="verRepairScheduleString">
                <logic:equal name="app" property="verRepairScheduleString" value="inconsistent">
                <p><img src="/shell/common-rsrc/images/alert_sm.gif"  width="16" height="16">
                    </logic:equal>
                    </logic:present>
                        <sm:scheduleDisplay name="app" property="verRepairScheduleString" schedule='<%= ((Channel) app).getVerRepairScheduleString() %>' type="verrepair" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                <p>&nbsp;</p>
            </td>
        </tr>
    </table>
    </td>
</tr>
<tr>
    <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">
        <tr>
            <td class="tableRowActions" style="height:20px;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td class="textWhite"><strong><webapps:pageText key="postponeTitle"/></strong> </td>
                </tr>
            </table>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <logic:present name="app" property="postponeScheduleString">
                <logic:equal name="app" property="postponeScheduleString" value="inconsistent">
                <p><img src="/shell/common-rsrc/images/alert_sm.gif"  width="16" height="16">
                    </logic:equal>
                    </logic:present>
                        <sm:scheduleDisplay name="app" property="postponeScheduleString" schedule='<%= ((Channel) app).getPostponeScheduleString() %>' type="postpone" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
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
</table>
</div>
<!--end dataSection-->
</div>
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
<!--end formBottom-->
<div id="pageNav">
    <input name="copy" type="button" class="mainBtn" accesskey="N" onClick="javascript:send(document.copyEditForm, '/copySaveTarget.do');" value="<webapps:pageText key="copy" type="button" shared="true"/>">
    &nbsp;
    <input name="Submit32" type="button" onClick="javascript:send(document.copyEditForm,'/copyAdd.do?action=back');" value="<webapps:pageText key="backToEdit" type="button" shared="true"/>">
    &nbsp;
    <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/copyCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
</div>
</div>
<!--end supder div for padding-->
</div>
<!--end super div for centering-->
</html:form>
<script>
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','pageNav');");
    resizeDataSection('FOO_dataDiv','pageNav');
    syncTables('FOO');
</script>
<%@ include file="/includes/footer.jsp" %>

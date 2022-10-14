<%--
    Copyright 2012, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/8.1.00/resources/channel/webapp/target/scap_security_preview.jsp

    @author : Selvarj Jegatheesan
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean, java.util.ArrayList"%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<webapps:helpContext context="spm" topic="scap_policy_preview" />

<title><webapps:pageText key="m6" type="global"/></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">       s
<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">
<script language="JavaScript" src="/shell/common-rsrc/js/master.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>
<script language="JavaScript" src="/shell/common-rsrc/js/table.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/domMenu.js"></script>
<script language="javascript" src="/shell/common-rsrc/js/domMenu_items2.js"></script>

<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/datatables.min.js"></script>

<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/datatables.min.css"/>

<script type="text/javascript">
    // This function saves the form attributes to the DistributionBean,
    // before forwarding to the next page.
    // IMPORTANT: Always use to navigate between pages so that the changes are
    // persistified in the session bean.

    function saveState(forwardaction) {
        document.scapSecurityForm.forward.value = forwardaction;
        send(document.scapSecurityForm, '/scapSecuritySave.do');
    }
</script>

<style>

    <!--
    .col1 {
        width:30%;
    }

    .col2 {
        width:70%;
    }
    -->
</style>
</head>

<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<html:form name="scapSecurityForm" action="/scapSecuritySave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.SCAPSecurityForm">
<html:hidden property="forward" />

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
    <span class="title">
        <logic:present name="copy_preview" >
            <webapps:pageText key="copyPreview" type="pagehdr" shared="true"/>
        </logic:present>
        <logic:notPresent name="copy_preview" >
            <webapps:pageText key="Title"/>
        </logic:notPresent>
    </span>
</div>

<logic:present name="taskid">
    <div class="pageHeader">
        <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
        <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
    </div>
</logic:present>

<logic:present name="policy_exists">
    <div class="statusMessage" id="warning">
        <h6>&nbsp;</h6>
        <p><webapps:pageText key="Warningbefore"/>
            <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            <webapps:pageText key="Warningafter"/></p>
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

<%-- Errors Display --%>
<table style="width:100%;" border="0" cellspacing="0" cellpadding="0">
    <%@ include file="/includes/usererrors.jsp" %>
</table>

<div class="itemStatus">
    <table cellspacing="0" cellpadding="3" border="0">
        <tr>
            <td valign="top"><logic:present name="session_copy" ><webapps:pageText key="copyfrom" type="colhdr" shared="true"/></logic:present><logic:notPresent name="session_copy" ><webapps:pageText key="targets" type="colhdr" shared="true"/></logic:notPresent>: </td>
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
    <logic:present name="session_copy" >
        <table cellspacing="0" cellpadding="3" border="0">
            <tr>
                <td valign="top" align="right">&nbsp;&nbsp;&nbsp;&nbsp;<webapps:pageText key="copyto" type="colhdr" shared="true"/>: </td>
                <jsp:include page="/copy/copy_target_display.jsp" />
            </tr>
        </table>
    </logic:present>
</div>

<logic:present name="copy_preview">
    <div valign="top" align="left" style="margin-bottom:8px; " class="tableTitle"><webapps:pageText key="Heading" /></div>
</logic:present>

<logic:notPresent name="copy_preview">
    <webapps:formtabs tabset="distPreview" tab="nonwindows" />
</logic:notPresent>

<logic:present name="copy_preview">
    <webapps:formtabs tabset="copyPreview" tab="nonwindows" />
</logic:present>

<div class="formContent" id="mainSection" style="overflow:auto; text-align:left; padding-top:20px;">
    <table width="70%" border="0" cellspacing="0" cellpadding="4" style="padding-left:80px;">
        <tr>
            <logic:equal name="scapSecurityForm" property="selectedSCAPOption" value="exclude">
                <td nowrap style="padding-left:15px;" colspan="2"><webapps:pageText key="label.excludeprofile"/>
            </logic:equal>
        </tr>
        <logic:equal name="scapSecurityForm" property="selectedSCAPOption" value="customize">
            <tr>
                <td nowrap style="padding-left:15px;" colspan="2"><b>Selected Profile Type:</b> Custom</td>
                <td nowrap>
                    <logic:notEqual name="scapSecurityForm" property="customizePriorityValue" value="">
                        <webapps:pageText key="Priority"/> &nbsp; <bean:write name="scapSecurityForm" property="customizePriorityValue"/>
                    </logic:notEqual>
                    <logic:equal name="scapSecurityForm" property="customizePriorityValue" value="">
                        <webapps:pageText key="Priority"/> &nbsp; <webapps:pageText key="NullDate" type="global"/>
                    </logic:equal>
                </td>
            </tr>
            <tr><td colspan="3"> &nbsp; </td></tr>
            <tr>
                <td style="padding-left:15px;" colspan="3">
                    <table border="1" width="100%" style="border-collapse:collapse;">
                        <td width="50%">Content Name</td>
                        <td width="50%"><b>Profile Name</b></td>
                        <logic:iterate name="scapSecurityForm" id="preview_list_map" property="previewList">
                            <tr>
                                <td width="50%"><bean:write name="preview_list_map" property="key"/></td>
                                <td width="50%"><bean:write name="preview_list_map" property="value"/></td>
                            </tr>
                        </logic:iterate>
                    </table>
                </td>
            </tr>
        </logic:equal>
        <tr><td colspan="3"> &nbsp; </td></tr>
        <logic:equal name="scapSecurityForm" property="selectedSCAPOption" value="standard">
            <tr>
                <td nowrap style="padding-left:15px;" colspan="2"><b>Selected Profile Type:</b> Standard</td>
                <td nowrap>
                    <logic:notEqual name="scapSecurityForm" property="standardPriorityValue" value="">
                        <webapps:pageText key="Priority"/> &nbsp; <bean:write name="scapSecurityForm" property="standardPriorityValue"/>
                    </logic:notEqual>
                    <logic:equal name="scapSecurityForm" property="standardPriorityValue" value="">
                        <webapps:pageText key="Priority"/> &nbsp; <webapps:pageText key="NullDate" type="global"/>
                    </logic:equal>
                </td>
            </tr>
            <tr><td colspan="3"> &nbsp; </td></tr>
            <tr>
                <td style="padding-left:15px;" colspan="3">
                    <table border="1" width="100%" style="border-collapse:collapse;">
                        <td width="50%"><webapps:pageText key="label.standardProfileSelected"/></td>
                        <td width="50%"><b>Profile Name</b></td>
                        <logic:iterate name="scapSecurityForm" id="preview_list_map" property="previewList">
                            <tr>
                                <td width="50%"><bean:write name="preview_list_map" property="key"/></td>
                                <td width="50%"><bean:write name="preview_list_map" property="value"/></td>
                            </tr>
                        </logic:iterate>

                    </table>
                </td>
            </tr>
        </logic:equal>
        <tr><td colspan="3"> &nbsp; </td></tr>
    </table>
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
    <logic:present name="copy_preview" >
        <input name="copy" type="button" class="mainBtn" accesskey="N" onClick="javascript:send(document.scapSecurityForm, '/copySaveTarget.do');" value="<webapps:pageText key="copy" type="button" shared="true"/>">
        &nbsp;
        <input name="Submit32" type="button" onClick="javascript:send(document.scapSecurityForm,'/copyAdd.do?action=back');" value="<webapps:pageText key="backToEdit" type="button" shared="true"/>">
        &nbsp;
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/copyCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
    </logic:present>

    <logic:notPresent name="copy_preview">
        <input name="save" type="submit" class="mainBtn" accesskey="N" onClick="javascript:send(document.scapSecurityForm, '/distSave.do');" value="<webapps:pageText key="save" type="button" shared="true"/>">
        &nbsp;
        <input name="Submit32" type="submit" onClick="javascript:send(document.scapSecurityForm, '/distInit.do?action=back');" value="<webapps:pageText key="backToEdit" type="button" shared="true"/>">
        &nbsp;
        <logic:present name="taskid">
            <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
        </logic:present>
        <logic:notPresent name="taskid">
            <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
        </logic:notPresent>
    </logic:notPresent>
</div>
</div>
<!--end supder div for padding-->
</div>
<!--end super div for centering-->
</html:form>
<script>
    CMSOnResizeHandler.addHandler("resizeDataSection('mainSection','pageNav');");
    resizeDataSection('mainSection','pageNav');
</script>
</body>
</html>


<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<html>
<head>
    <webapps:helpContext context="spm" topic="security_policy_details"/>
    <title>Target View</title>
    <script type="text/javascript" src="/shell/common-rsrc/js/master.js"></script>
    <script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
    <link rel="stylesheet" type="text/css" href="/shell/common-rsrc/css/main.css"/>
    <link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css"/>

    <style type="text/css">
        .panel-body {
            font-size: 11px;
        }
        .tabs-title {
            font-size: 11px;
        }
        .tabs-container {
            font-size: 11px;
            overflow: hidden;
            font-family:Tahoma,Verdana,Arial,Helvetica,sans-serif !important;
        }
    </style>
    <script type="text/javascript">
        function sendredirect(form, action) {
            var fullpath = "<html:rewrite page='" + action + "' />";
            top.location.href = fullpath;
        }
        function viewcompliancedetails(form, action) {
            document.location.href="/spm/" +action;
        }
        function MakeTip(txtToDisplay) {
            return Tip(txtToDisplay, WIDTH, '-250', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', OFFSETY, 20, BORDERCOLOR, '#333300', FADEIN, 100);
        }

        function CloseTip() {
            UnTip();
        }

    </script>
</head>

<body>
<html:form name="securityTargetDetailsForm" action="/securityTargetViewDispatcher.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.SecurityTargetDetailsForm">
<html:hidden property="value(forward)" />

<div>
<bean:define id="target" name="target" type="com.marimba.apps.subscription.common.objects.Target" toScope="request"/>
<logic:present name="selected_target_id">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr valign="middle" class="smallButtons">
        <td class="tableSearchRow"><b>
            <bean:define id="ID" name="target" property="id" toScope="request"/>
            <bean:define id="Name" name="target" property="name" toScope="request"/>
            <bean:define id="Type" name="target" property="type" toScope="request"/>
            <jsp:include page="/includes/target_display_single.jsp"/>
        </b></td>
        <td class="tableSearchRow" align="right">
            <table border="0" cellpadding="5" cellspacing="3">
                <tr>
                    <logic:notPresent name="viewcompliance">
                        <logic:present name="hasContent">
                            <logic:equal name="hasContent" value="true">
                                <td style="border: 1px solid #999999; cursor: pointer;" onclick="sendredirect(document.securityTargetDetailsForm,'/distEdit.do')" onmouseover="MakeTip('<b>&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="edit"/>&nbsp;</b>');" onmouseout="CloseTip();">
                                    <span class="text-aqua" style="font-size: 40px;"><i class="fa fa-pencil-square-o" aria-hidden="true"></i></span>
                                </td>
                            </logic:equal>
                            <logic:notEqual name="hasContent" value="true">
                                <td style="border: 1px solid #999999; cursor: pointer;" onclick="alert('&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="edit.block"/>&nbsp;')" onmouseover="MakeTip('<b>&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="edit"/>&nbsp;</b>');" onmouseout="CloseTip();">
                                    <span class="text-aqua" style="font-size: 40px;"><i class="fa fa-pencil-square-o" aria-hidden="true"></i></span>
                                </td>
                            </logic:notEqual>
                        </logic:present>
                        <logic:notPresent name="hasContent">
                            <td style="border: 1px solid #999999; cursor: pointer;" onclick="alert('&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="edit.block"/>&nbsp;')" onmouseover="MakeTip('<b>&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="edit"/>&nbsp;</b>');" onmouseout="CloseTip();">
                                <span class="text-aqua" style="font-size: 40px;"><i class="fa fa-pencil-square-o" aria-hidden="true"></i></span>
                            </td>
                        </logic:notPresent>
                        <td style="border: 1px solid #999999; cursor: pointer;" onclick="sendredirect(document.securityTargetDetailsForm,'/distDelete.do')" onmouseover="MakeTip('<b>&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="delete"/>&nbsp;</b>');" onmouseout="CloseTip();">
                            <span class="text-teal" style="font-size: 40px;"><i class="fa fa-trash-o" aria-hidden="true"></i></span>
                        </td>
                        <td style="border: 1px solid #999999; cursor: pointer;" onclick="sendredirect(document.securityTargetDetailsForm,'/copyExist.do')" onmouseover="MakeTip('<b>&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="copy"/>&nbsp;</b>');" onmouseout="CloseTip();">
                            <span class="text-yellow" style="font-size: 40px;"><i class="fa fa-files-o" aria-hidden="true"></i></span>
                        </td>
                        <td style="border: 1px solid #999999; cursor: pointer;" onclick="sendredirect(document.securityTargetDetailsForm,'/compTgtView.do')" onmouseover="MakeTip('<b>&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="compliance"/>&nbsp;</b>');" onmouseout="CloseTip();">
                            <span class="text-blue" style="font-size: 40px;"><i class="fa fa-tachometer" aria-hidden="true"></i></span>
                        </td>
                    </logic:notPresent>
                    <logic:present name="viewcompliance">
                        <td style="border: 1px solid #999999; cursor: pointer;" onclick="sendredirect(document.securityTargetDetailsForm,'/initTargetView.do')" onmouseover="MakeTip('<b>&nbsp;<webapps:pageText shared="true" type="security_policy_details" key="ViewPolicy"/>&nbsp;</b>');" onmouseout="CloseTip();">
                            <span class="text-green" style="font-size: 40px;"><i class="fa fa-cogs" aria-hidden="true"></i></span>
                        </td>
                    </logic:present>
                </tr>
            </table>
        </td>
    </tr>
</table>
<div class="headerSection" style="width:100%; overflow:hidden; text-align:left;" id="FOO_headerDiv">
    <table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable" width="100%">
        <logic:present name="viewcompliance">
            <colgroup width="3%"/><colgroup width="65%"/><colgroup width="70%"/><colgroup width="100%"/>
        </logic:present>
        <logic:notPresent name="viewcompliance">
            <colgroup width="70%"/><colgroup width="100%"/>
        </logic:notPresent>
        <thead>
            <tr>
                <logic:present name="viewcompliance">
                    <td class="tableHeaderCell">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                    <td class="tableHeaderCell"><webapps:pageText key="policyname"/></td>
                    <td class="tableHeaderCell">Compliance</td>
                </logic:present>
                <logic:notPresent name="viewcompliance">
                    <td class="tableHeaderCell"><webapps:pageText key="policyname"/></td>
                    <td class="tableHeaderCell">Directly Assigned To</td>
                </logic:notPresent>
            </tr>
        </thead>
    </table>
</div>

<div id="FOO_dataDiv" onscroll="syncScroll('FOO');" style="width:100%; overflow:auto;margin-top:0; border:1px solid #999999; height:340px;">
    <logic:present name="securityTargetDetailsForm" property="assignSecurityDetailsBean">
        <table width="100%" border="0" cellpadding="0" cellspacing="0" id="FOO_dataTable">
            <logic:present name="viewcompliance">
                <colgroup width="3%"/><colgroup width="65%"/><colgroup width="70%"/><colgroup width="100%"/>
            </logic:present>
            <logic:notPresent name="viewcompliance">
                <colgroup width="70%"/><colgroup width="100%"/>
            </logic:notPresent>
            <tbody>
                <logic:iterate id="policyDetails" indexId="index" name="securityTargetDetailsForm" property="assignSecurityDetailsBean">
                    <tr <% if(index.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                        <logic:present name="viewcompliance">
                            <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
                                <logic:equal name="policyDetails" property="categoryType" value="scap">
                                    <a href="javascript:void(0);" onClick="window.parent.showSCAPComplianceDetails();"><img src="/shell/common-rsrc/images/info.gif" width="12" height="12" border="0"></a>
                                </logic:equal>
                                <logic:equal name="policyDetails" property="categoryType" value="desktop">
                                    <a href="javascript:void(0);" onClick="window.parent.showDesktopComplianceDetails();"><img src="/shell/common-rsrc/images/info.gif" width="12" height="12" border="0"></a>
                                </logic:equal>
                                <logic:equal name="policyDetails" property="categoryType" value="usgcb">
                                    <a href="javascript:void(0);" onClick="window.parent.showUSGCBComplianceDetails();"><img src="/shell/common-rsrc/images/info.gif" width="12" height="12" border="0"></a>
                                </logic:equal>
                            </td>
                        </logic:present>
                        <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
                            <bean:write name="policyDetails" property="selectedSecurityContentName"/>
							<logic:notEqual name="policyDetails" property="customTemplateName" value="">
								(<bean:write name="policyDetails" property="customTemplateName"/>)
							</logic:notEqual>
                        </td>

                        <logic:present name="viewcompliance">
                            <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
                                <font color="red">NON-COMPLIANT</font>
                            </td>
                        </logic:present>
                        <td class="rowLevel1" <logic:equal name="index" value="0">style="border-top:0px;"</logic:equal>>
                            <bean:define id="ID" name="policyDetails" property="assignedToID" toScope="request"/>
                            <bean:define id="Name" name="policyDetails" property="assginedToName" toScope="request"/>
                            <bean:define id="Type" name="policyDetails" property="assginedToType" toScope="request"/>
                            <jsp:include page="/includes/target_display_single.jsp"/>
                        </td>
                    </tr>
                </logic:iterate>
            </tbody>
        </table>
    </logic:present>
    <logic:notPresent name="securityTargetDetailsForm" property="assignSecurityDetailsBean">
        <table width="100%" border="0" cellpadding="0">
            <tr>
                <td valign="top" class="textGeneral">
                    <strong><webapps:pageText shared="true" type="security_policy_details" key="nopolicy"/></strong>
                </td>
            </tr>
        </table>
    </logic:notPresent>
</div>
<div id="FOO_endOfData">&nbsp;</div>
</logic:present>
<logic:notPresent name="selected_target_id">
    <table width="100%" border="0" cellpadding="0">
        <tr>
            <td valign="top" class="textGeneral">
                <strong><webapps:pageText shared="true" type="security_policy_details" key="nopolicy" /></strong>
            </td>
        </tr>
    </table>
</logic:notPresent>
</div>

<script type="text/javascript">
    resizeDataSection('FOO_dataDiv','FOO_endOfData', 0);
</script>
</html:form>
</body>
</html>
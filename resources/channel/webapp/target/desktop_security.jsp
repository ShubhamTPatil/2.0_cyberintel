<%--
    Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/9.0.01/resources/channel/webapp/target/desktop_security.jsp

    @author : Venkatesh Jeyaraman
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<% Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN); %>

<webapps:helpContext context="sm" topic="security" />

<script>
    // This function saves the form attributes to the DistributionBean,
    // before forwarding to the next page.
    // IMPORTANT: Always use to navigate between pages so that the changes are
    // persistified in the session bean.

    function saveState(forwardaction) {
      document.desktopSecurityForm["value(forward)"].value = forwardaction;
      send(document.desktopSecurityForm, '/desktopSecuritySave.do');
    }

    function alterProfileSettings(profileName) {
        var forward = '/desktopSecurityLoad.do';
        document.desktopSecurityForm.selectedProfile.value=profileName;
        send(document.desktopSecurityForm, forward);
        return false;
    }

    function getSelectedOption() {
        var hibRadioButtons = document.desktopSecurityForm.elements["value(marimba.subscription.desktop.enable)"];
        for(var i=hibRadioButtons.length; i--;) {
            if (hibRadioButtons[i].checked) {
                inactiveSecurityOptionParameters(hibRadioButtons[i].value);
            }
        }
    }

    function disableSecurityProfile() {
        var securityProfileLength = document.desktopSecurityForm["value(marimba.subscription.desktop.template.name)"].length;
        if(securityProfileLength == 1) {
            document.getElementById('enable_securityoption').disabled=true;
            contentDivVisibility("hidden");
            inactiveSecurityOptionParameters("false");
        }
    }

    function inactiveSecurityOptionParameters(securityOptionValue) {
        if(securityOptionValue == 'true') {
            document.desktopSecurityForm["value(marimba.subscription.desktop.template.name)"].disabled = false;
            contentDivVisibility("visible");
        } else  if (securityOptionValue == 'exclude') {
            document.desktopSecurityForm["value(marimba.subscription.desktop.template.name)"].disabled = true;
            contentDivVisibility("hidden");
        } else {
            document.desktopSecurityForm["value(marimba.subscription.desktop.template.name)"].disabled = true;
            contentDivVisibility("hidden");
        }
    }

    function contentDivVisibility(option) {
        document.getElementById('profileDetails').style.visibility = option;
        document.getElementById('priority_box').style.visibility = option;
    }
</script>
</head>

<body>
<html:form name="desktopSecurityForm" action="/desktopSecuritySave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DesktopSecurityForm">
<html:hidden property="value(forward)" />
<html:hidden property="selectedProfile" />

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
    <div class="pageHeader"><span class="title"><webapps:pageText key="policy" type="pgtitle" shared="true"/></span></div>
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

    <div class="formContent" id="dataSection" style="text-align:left; overflow:auto;">
        <div class="sectionInfo"><webapps:pageText key="SectionInfo"/></div>
            <table border="0" cellspacing="0" cellpadding="3" width="100%">
                <tr> <td align="left"> <webapps:pageText key="label.secOption"/> </td> <td align="left" colspan="2"> </td> </tr>

                <tr id="disable_pwropt">
                    <td align="right">
                        <html:radio property="value(marimba.subscription.desktop.enable)" styleId="disable_securityoption" value="false" onclick="inactiveSecurityOptionParameters(this.value)"/>
                    </td>
                    <td align="left"> <label for="disable_securityoption"> <webapps:pageText key="noSecurityOption"/> </label> </td>
                    <td align="left"> </td>
                </tr>

                <tr id="exclude_pwropt">
                    <td align="right">
                        <html:radio property="value(marimba.subscription.desktop.enable)" styleId="exclude_securityoption" value="exclude" onclick="inactiveSecurityOptionParameters(this.value)"/>
                    </td>
                    <td align="left"> <label for="exclude_securityoption"> <webapps:pageText key="excludeSecurityOption"/> </label> </td>
                    <td align="left"> </td>
                </tr>

                <tr id="enable_pwropt">
                    <td align="right">
                        <html:radio property="value(marimba.subscription.desktop.enable)" styleId="enable_securityoption" value="true" onclick="inactiveSecurityOptionParameters(this.value)"/>
                    </td>

                    <td align="left">
                        <label for="enable_securityoption"> <webapps:pageText key="setSecurityOption"/> </label> &nbsp;&nbsp;
                        <html:select property="value(marimba.subscription.desktop.template.name)" onchange="alterProfileSettings(this.value)">
                            <html:options name="profiles"/>
                        </html:select>
                    </td>

                    <td id="priority_box">
                        &nbsp;&nbsp;&nbsp; <webapps:pageText key="priority"/> &nbsp;&nbsp;&nbsp;
                        <html:text property="value(priority_value)" size="5" maxlength="5" onkeypress="return restrictKeyPressPositive(event)"/> &nbsp;&nbsp;
                        <webapps:pageText key="validpriority"/>
                    </td>
                </tr>

                <tr id="profileDetails"> <td colspan="3"> <%@ include file="/target/desktop_security_options.jsp" %> </td> </tr>

            </table>
        </div> <!--end formContent-->

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
            <input name="Submit32" type="submit" class="mainBtn" onClick="javascript:saveState('/desktopSecurityLoad.do?action=preview')" value="<webapps:pageText key="preview" type="button" shared="true"/>"> &nbsp;
            <logic:present name="taskid">
                <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
            </logic:present>
            <logic:notPresent name="taskid">
                <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
            </logic:notPresent>
        </div>

    </div> <!--end supder div for padding-->

</div> <!--end super div for centering-->

</html:form>

<script>
    CMSOnResizeHandler.addHandler("resizeDataSection('dataSection','pageNav');");
    resizeDataSection('dataSection','pageNav');
    disableSecurityProfile();
    getSelectedOption();
</script>

</body>

</html>

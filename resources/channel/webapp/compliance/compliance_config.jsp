<%@ page contentType="text/html;charset=UTF-8" %>
<%--
 Copyright 2004-2012, BMC Software Inc. All Rights Reserved.
 Confidential and Proprietary Information of BMC Software Inc.
 Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
 6,381,631, and 6,430,608. Other Patents Pending.

 $File$
 
 @version   $Revision$, $Date$
--%>

<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.compliance.view.ConfigBean" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<script>

    function validateNaN( elementName ){
        var element = document.packageComplianceForm['value('+elementName+')'];
        var fieldVal = element.value;
        if( isNaN( fieldVal ) || ( isNaN( parseInt( fieldVal ) ) ) ){
            alert( '<webapps:pageText shared="true" type="compliance_config" key="NullValMsg" escape="js"/>' );
            element.focus();
        }
    }

    // restrict a key press to only to -/+ values
    function validateNCITime( evt ) {
        var key;
        var keychar;
        var oldval;
        if (window.event) {
            key = window.event.keyCode;
            oldval = window.event.srcElement.value;
        } else if (evt) {
            key = evt.which;
            oldval = evt.target.valueOf();
            if (key == "0"|| key == "8"){
                return true;
            }
        } else {
            return true;
        }
        keychar = String.fromCharCode(key);
        if( "-".indexOf(keychar) != -1 ){
            var fieldVal = document.packageComplianceForm['value(checkin_limit)'].value;
            var value = parseInt( fieldVal );
            // checking already existing -ve sign
            if( value < 0 ){
                return false;
            } else if( fieldVal == '-' ){
                return false;
            }
        } else if (-1 == ("0123456789".indexOf(keychar))) {
            // digits only
            return false;
        }
        return true;
    }
</script>
<%@ include file="/includes/common_js.jsp" %>
<webapps:helpContext context="sm" topic="compliance_options" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ include file="/includes/body.html" %>

<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>
<div align="center">
    <div class="commonPadding">
        <html:form name="packageComplianceForm" action="/compCfgView.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">
        <input type="hidden" name="value(configOption)" value="save"/>
        <div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
        <%@ include file="/includes/usererrors.jsp" %>
        <%@ include file="/includes/help.jsp" %>
        <div style="width:75%">
            <div class="formTabs">
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_lft.gif" width="5" height="5"></td>
                        <td width="100%" style="border-top:1px solid #CCCCCC; height:5px;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="4"></td>
                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_rt.gif" width="5" height="5"></td>
                    </tr>
                </table>
            </div>
            <div class="formContent" align="left">
                <table cellpadding="5" cellspacing="0">
                    <colgroup width="0*"></colgroup>
                    <colgroup width="100%"></colgroup>
                    <tr>
                        <td nowrap style="padding-left:15px; border-bottom:1px;">
                            <webapps:pageText shared="true" type="set_compliance" key="enablespm.question.inventory"/></td>
                        <td>
                            <html:select styleId="collectCompEnabledOption" property="value(coll_comp)" value="<%=( ( ConfigBean )request.getAttribute( "compCfg" ) ).getCollectCompEnabled()%>">
                                <html:option value="enable"><webapps:pageText shared="true" type="set_compliance" key="enablespm.enable"/></html:option>
                                <html:option value="disable"><webapps:pageText shared="true" type="set_compliance" key="enablespm.disable"/></html:option>
                            </html:select>
                        </td>
                    <tr>
                        <td nowrap style="padding-left:15px; border-bottom:1px;">
                            <webapps:pageText shared="true" type="compliance_config" key="CacheListMax"/>
                        </td>
                        <td>
                            <input type="text" name="value(cache_list_max)" value="<bean:write name="compCfg" property="cacheListMax" />" size=10 onkeypress="return restrictKeyPressPositive(event)" onblur="validateNaN( 'cache_list_max' )" >
                        </td>
                    </tr>

                    <tr>
                        <td nowrap style="padding-left:15px; border-bottom:1px;">
                            <webapps:pageText shared="true" type="compliance_config" key="CacheObjMax"/>
                        </td>
                        <td>
                            <input type="text" name="value(cache_obj_max)" value="<bean:write name="compCfg" property="cacheObjMax" />" size=10 onkeypress="return restrictKeyPressPositive(event)" onblur="validateNaN( 'cache_obj_max' )">
                        </td>
                    </tr>

                    <tr>
                        <td nowrap style="padding-left:15px; border-bottom:1px;">
                            <webapps:pageText shared="true" type="compliance_config" key="QueryWait"/>
                        </td>
                        <td>
                            <input type="text" name="value(wait_time)" value="<bean:write name="compCfg" property="waitTime" />" size=10 onkeypress="return restrictKeyPressPositive(event)" onblur="validateNaN( 'wait_time' )">
                        </td>
                    </tr>
                    <tr>
                        <td nowrap style="padding-left:15px; border-bottom:1px;">
                            <webapps:pageText shared="true" type="compliance_config" key="LastCheckIn"/>
                        </td>
                        <td>
                            <input type="text" name="value(checkin_limit)" value="<bean:write name="compCfg" property="checkInLimit" />" size=10 onkeypress="return validateNCITime(event)" onblur="validateNaN( 'checkin_limit' )">
                        </td>
                    </tr>
                </table>
            </div>
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
                <input type="submit" class="mainBtn" name="save" value=" <webapps:pageText key="OK" type="global" /> ">
                <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.packageComplianceForm,'/pmSettingsCancel.do');" >
            </div>
            </html:form>
        </div>
    </div>
</div>
<%@ include file="/includes/footer.jsp" %>
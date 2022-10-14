<%--
    Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/9.0.01/resources/channel/webapp/target/desktop_security_restrict_policy.jsp

    @author author: Venkatesh Jeyaraman
--%>

<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" %>

<table cellpadding="5" cellspacing="5" style="width: 75%">
	<colgroup span="4"></colgroup>

	<!--tr valign="top"> <td colspan="4" valign="top"> <hr style="color:black;" size="1"> </td> </tr-->

	<tr align="absmiddle">
	    <td colspan="4" valign="top">
            <html:select styleId="selectedApp" name="desktopSecurityProfileForm" property="selectedApp" style="width:200px" styleClass="easyui-combobox">
                <html:option value=""></html:option>
                <html:options property="appSet"/>
            </html:select> &nbsp;&nbsp;&nbsp;
            <a href="#" id="btn_allowed" class="easyui-linkbutton" data-options="iconCls:'icon-cus_unlock'" onclick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=addAllow');"> Allowed </a>
                &nbsp;&nbsp;&nbsp;
            <a href="#" id="btn_blocked" class="easyui-linkbutton" data-options="iconCls:'icon-cus_lock'" onclick="javascript:saveState(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=addBlock');"> Blocked </a>
	    </td>
	</tr>

	<tr valign="top"> <td colspan="4" valign="top"> <hr style="color:black;" size="1"> </td> </tr>

	<tr align="absmiddle">
		<td><b><webapps:pageText key="applicationLabel" /></b></td>
		<td><b><webapps:pageText key="allowLabel" /></b></td>
		<td><b><webapps:pageText key="blockLabel" /></b></td>
		<td><b><webapps:pageText key="removeLabel" /></b></td>
	</tr>

	<tr valign="top"> <td colspan="4" valign="top"> <hr style="color:black;" size="1"> </td> </tr>

    <logic:present name="desktopSecurityProfileForm" property="managedApps">
        <logic:iterate id="appsItr" name="desktopSecurityProfileForm" property="managedApps">
            <bean:define id="appBean" name="appsItr" property="value"/>
            <tr align="absmiddle">
                <td><b><bean:write name="appsItr" property="key"/></b></td>

                <logic:equal name="appBean" property="type" value="allowed">
                    <td><input type="radio" name='<bean:write name="appsItr" property="key"/>' id='<bean:write name="appsItr" property="key"/>' checked="checked" value="allowed" /></td>
                    <td><input type="radio" name='<bean:write name="appsItr" property="key"/>' id='<bean:write name="appsItr" property="key"/>' value="blocked" /></td>
                </logic:equal>

                <logic:equal name="appBean" property="type" value="blocked">
                    <td><input type="radio" name='<bean:write name="appsItr" property="key"/>' id='<bean:write name="appsItr" property="key"/>' value="allowed" /></td>
                    <td><input type="radio" name='<bean:write name="appsItr" property="key"/>' id='<bean:write name="appsItr" property="key"/>' checked="checked" value="blocked" /></td>
                </logic:equal>

                <td><a href="#" class="easyui-linkbutton" data-options="iconCls:'icon-no'" onclick="javascript:removeApp(document.desktopSecurityProfileForm, '/desktopSecurityTemplateListing.do?action=removeApp', '<bean:write name="appsItr" property="key"/>');" > </a></td>
            </tr>
        </logic:iterate>
    </logic:present>

</table>

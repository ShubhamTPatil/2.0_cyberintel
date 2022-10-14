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
	<colgroup span="5"></colgroup>
	<tr align="absmiddle">
		<td align="right"><b><webapps:pageText key="restrictOptionsLabel" /></b></td>
		<td><b><webapps:pageText key="enableLabel" /></b></td>
		<td><b><webapps:pageText key="disableLabel" /></b></td>
		<td><b><webapps:pageText key="notConfiguredLabel" /></b></td>
		<td><b><webapps:pageText key="inputValueLabel" /></b></td>
	</tr>
	<tr valign="top"> <td colspan="5" valign="top"> <hr style="color:black;" size="1"> </td> </tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="cmdPrompt" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="cmdPrompt" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="cmdPrompt" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="cmdPrompt" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="fileShare" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="fileSharing" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="fileSharing" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="fileSharing" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userReadFloppy" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadFloppy" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadFloppy" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadFloppy" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userWriteFloppy" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteFloppy" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteFloppy" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteFloppy" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userReadCDDVD" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadCDDVD" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadCDDVD" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadCDDVD" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userWriteCDDVD" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteCDDVD" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteCDDVD" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteCDDVD" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userReadWPD" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadWPD" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadWPD" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userReadWPD" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userWriteWPD" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteWPD" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteWPD" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userWriteWPD" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userEnableScreen" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userEnableScreen" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userEnableScreen" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userEnableScreen" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userSecureScreen" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userSecureScreen" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userSecureScreen" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userSecureScreen" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userScreenTimeout" /></td>
		<td><html:radio name="desktopSecurityProfileForm" styleId="userScreenTimeout" property="userScreenTimeout" value="true" onclick="handleUserScreenTimeout()" /></td>
		<td><html:radio name="desktopSecurityProfileForm" styleId="userScreenTimeout" property="userScreenTimeout" value="false" onclick="handleUserScreenTimeout()" /></td>
		<td><html:radio name="desktopSecurityProfileForm" styleId="userScreenTimeout" property="userScreenTimeout" value="notconfigured" onclick="handleUserScreenTimeout()" /></td>
		<td align="left">
		    <div id="userScreenTimeoutDiv">
		        <html:text property="userScreenTimeoutVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>&nbsp;&nbsp;
		        <webapps:pageText key="secsLabel"/>
            </div>
		</td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userForceSpecificScreen" /></td>
		<td><html:radio name="desktopSecurityProfileForm" styleId="userForceSpecificScreen" property="userForceSpecificScreen" value="true" onclick="handleUserForceSpecificScreenSaver()" /></td>
		<td><html:radio name="desktopSecurityProfileForm" styleId="userForceSpecificScreen" property="userForceSpecificScreen" value="false" onclick="handleUserForceSpecificScreenSaver()" /></td>
		<td><html:radio name="desktopSecurityProfileForm" styleId="userForceSpecificScreen" property="userForceSpecificScreen" value="notconfigured" onclick="handleUserForceSpecificScreenSaver()" /></td>
		<td align="left">
		    <div id="userForceSpecificScreenDiv">
		        <html:text styleId="userForceSpecificScreenVal" property="userForceSpecificScreenVal" size="30" maxlength="30" style="width:150px" styleClass="easyui-textbox"/>&nbsp;&nbsp;
		        <webapps:pageText key="screenSaverNameLabel"/>
            </div>
        </td>
	</tr>
	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userInternet" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userInternet" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userInternet" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="userInternet" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>
</table>


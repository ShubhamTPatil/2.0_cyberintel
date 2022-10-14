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
</table>


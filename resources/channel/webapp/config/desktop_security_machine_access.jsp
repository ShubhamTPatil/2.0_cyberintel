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
		<td><b></b></td>
	</tr>

	<tr valign="top"> <td colspan="5" valign="top"> <hr style="color:black;" size="1"> </td> </tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userReadFloppy" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machReadFloppy" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machReadFloppy" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machReadFloppy" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>

    <tr align="absmiddle">
        <td align="right"><webapps:pageText key="userWriteFloppy" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machWriteFloppy" value="true" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machWriteFloppy" value="false" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machWriteFloppy" value="notconfigured"/></td>
        <td align="left"></td>
    </tr>

    <tr align="absmiddle">
        <td align="right"><webapps:pageText key="userReadCDDVD" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machReadCDDVD" value="true" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machReadCDDVD" value="false" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machReadCDDVD" value="notconfigured"/></td>
        <td align="left"></td>
    </tr>

    <tr align="absmiddle">
        <td align="right"><webapps:pageText key="userWriteCDDVD" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machWriteCDDVD" value="true" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machWriteCDDVD" value="false" /></td>
        <td><html:radio name="desktopSecurityProfileForm" property="machWriteCDDVD" value="notconfigured"/></td>
        <td align="left"></td>
    </tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userReadWPD" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machReadWPD" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machReadWPD" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machReadWPD" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userWriteWPD" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machWriteWPD" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machWriteWPD" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machWriteWPD" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="userInternet" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machInternet" value="true" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machInternet" value="false" /></td>
		<td><html:radio name="desktopSecurityProfileForm" property="machInternet" value="notconfigured"/></td>
		<td align="left"></td>
	</tr>

</table>

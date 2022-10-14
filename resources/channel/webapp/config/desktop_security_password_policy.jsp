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
		<td align="right"><b><webapps:pageText key="restrictOptionsLabel"/></b></td>
		<td><b></b></td>
		<td align="center"><b><webapps:pageText key="inputValueLabel"/></b></td>
		<td><b></b></td>
		<td><b></b></td>
	</tr>

	<tr valign="top"> <td colspan="5" valign="top"> <hr style="color:black;" size="1"> </td> </tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="minPwdStrength"/></td>
		<td></td>
        <td align="right">
            <div id="minPwdStrengthDiv">
                <html:text styleId="minPwdStrengthVal" property="minPwdStrengthVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td></td>
		<td></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="maxPwdAge"/></td>
		<td></td>
        <td align="right">
            <div id="maxPwdAgeDiv">
                <html:text styleId="maxPwdAgeVal" property="maxPwdAgeVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td align="left"><webapps:pageText key="daysLabel"/></td>
		<td></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="minPwdAge"/></td>
		<td></td>
        <td align="right">
            <div id="minPwdAgeDiv">
                <html:text styleId="minPwdAgeVal" property="minPwdAgeVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td align="left"><webapps:pageText key="daysLabel"/></td>
		<td></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="forcedLogoutTime"/></td>
		<td></td>
        <td align="right">
            <div id="forcedLogoutTimeDiv">
                <html:text styleId="forcedLogoutTimeVal" property="forcedLogoutTimeVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td align="left"><webapps:pageText key="secsLabel"/></td>
		<td></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="enforcePwdHistory"/></td>
		<td></td>
        <td align="right">
            <div id="enforcePwdHistoryDiv">
                <html:text styleId="enforcePwdHistoryVal" property="enforcePwdHistoryVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td align="left"></td>
		<td></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="accountLockoutThreshold"/></td>
		<td></td>
        <td align="right">
            <div id="accountLockoutThresholdDiv">
                <html:text styleId="accountLockoutThresholdVal" property="accountLockoutThresholdVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td align="left"><webapps:pageText key="attemptsLabel"/></td>
		<td></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="resetAccountLockoutCounter"/></td>
		<td></td>
        <td align="right">
            <div id="resetAccountLockoutCounterDiv">
                <html:text styleId="resetAccountLockoutCounterVal" property="resetAccountLockoutCounterVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td align="left"><webapps:pageText key="minsLabel"/></td>
		<td></td>
	</tr>

	<tr align="absmiddle">
		<td align="right"><webapps:pageText key="accountLockoutCounter"/></td>
		<td></td>
        <td align="right">
            <div id="accountLockoutCounterDiv" style="width:150px">
                <html:text styleId="accountLockoutCounterVal" property="accountLockoutCounterVal" size="10" style="width:150px" styleClass="easyui-numberspinner"/>
            </div>
        </td>
		<td align="left"><webapps:pageText key="minsLabel"/></td>
		<td></td>
	</tr>

</table>

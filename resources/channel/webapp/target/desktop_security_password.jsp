<%--
    Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/9.0.01/resources/channel/webapp/target/desktop_security_machine.jsp

    @author author: Venkatesh Jeyaraman
--%>
<table border="0" cellspacing="0" cellpadding="5" width="100%">

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.minPwdStrength"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.minPwdStrength)" /> </b></font>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.maxPwdAge"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.maxPwdAge)" /> </b></font>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.minPwdAge"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.minPwdAge)" /> </b></font>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.forcedLogoutTime"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.forcedLogoutTime)" /> </b></font>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.enforcePwdHistory"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.enforcePwdHistory)" /> </b></font>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.accountLockoutThreshold"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.accountLockoutThreshold)" /> </b></font>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.resetAccountLockoutCounter"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.resetAccountLockoutCounter)" /> </b></font>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <font color="green"><b> <webapps:pageText key="label.accountLockoutCounter"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.accountLockoutCounter)" /> </b></font>
        </td>
    </tr>

</table>

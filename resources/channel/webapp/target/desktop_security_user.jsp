<%--
    Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/9.0.01/resources/channel/webapp/target/desktop_security_user.jsp

    @author author: Venkatesh Jeyaraman
--%>
<table border="0" cellspacing="0" cellpadding="5" width="100%">

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.fileSharing)" value="true">
                <font color="green"><b> <webapps:pageText key="label.fileSharing"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.fileSharing)" value="false">
                <font color="red"><b> <webapps:pageText key="label.fileSharing"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.fileSharing)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.fileSharing"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.cmdPrompt)" value="true">
                <font color="green"><b> <webapps:pageText key="label.cmdPrompt"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.cmdPrompt)" value="false">
                <font color="red"><b> <webapps:pageText key="label.cmdPrompt"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.cmdPrompt)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.cmdPrompt"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.floppyRead)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userReadFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.floppyRead)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userReadFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.floppyRead)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userReadFloppy"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.floppyWrite)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userWriteFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.floppyWrite)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userWriteFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.floppyWrite)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userWriteFloppy"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.cddvdRead)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userReadCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.cddvdRead)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userReadCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.cddvdRead)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userReadCDDVD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.cddvdWrite)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userWriteCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.cddvdWrite)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userWriteCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.cddvdWrite)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userWriteCDDVD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.wpdRead)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userReadWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.wpdRead)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userReadWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.wpdRead)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userReadWPD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.wpdWrite)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userWriteWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.wpdWrite)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userWriteWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.wpdWrite)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userWriteWPD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.internet)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userInternet"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.internet)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userInternet"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.internet)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userInternet"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.enableScreenSaver)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userEnableScreen"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.enableScreenSaver)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userEnableScreen"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.enableScreenSaver)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userEnableScreen"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.secureScreenSaver)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userSecureScreen"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.secureScreenSaver)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userSecureScreen"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.secureScreenSaver)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userSecureScreen"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.screenSaverTimeout)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userScreenTimeout"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.screenSaverTimeout.value)" /> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.screenSaverTimeout)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userScreenTimeout"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.screenSaverTimeout)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userScreenTimeout"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td colspan="3" nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.forceSpecificScreenSaver)" value="true">
                <font color="green"><b> <webapps:pageText key="label.userForceSpecificScreen"/> - <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.forceSpecificScreenSaver.value)" /> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.forceSpecificScreenSaver)" value="false">
                <font color="red"><b> <webapps:pageText key="label.userForceSpecificScreen"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.user.forceSpecificScreenSaver)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.userForceSpecificScreen"/> </b></font>
            </logic:equal>
        </td>
    </tr>

</table>

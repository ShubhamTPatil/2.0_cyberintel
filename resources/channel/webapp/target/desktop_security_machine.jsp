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
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.floppyRead)" value="true">
                <font color="green"><b> <webapps:pageText key="label.machReadFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.floppyRead)" value="false">
                <font color="red"><b> <webapps:pageText key="label.machReadFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.floppyRead)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.machReadFloppy"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.floppyWrite)" value="true">
                <font color="green"><b> <webapps:pageText key="label.machWriteFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.floppyWrite)" value="false">
                <font color="red"><b> <webapps:pageText key="label.machWriteFloppy"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.floppyWrite)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.machWriteFloppy"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.cddvdRead)" value="true">
                <font color="green"><b> <webapps:pageText key="label.machReadCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.cddvdRead)" value="false">
                <font color="red"><b> <webapps:pageText key="label.machReadCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.cddvdRead)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.machReadCDDVD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.cddvdWrite)" value="true">
                <font color="green"><b> <webapps:pageText key="label.machWriteCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.cddvdWrite)" value="false">
                <font color="red"><b> <webapps:pageText key="label.machWriteCDDVD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.cddvdWrite)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.machWriteCDDVD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.wpdRead)" value="true">
                <font color="green"><b> <webapps:pageText key="label.machReadWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.wpdRead)" value="false">
                <font color="red"><b> <webapps:pageText key="label.machReadWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.wpdRead)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.machReadWPD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.wpdWrite)" value="true">
                <font color="green"><b> <webapps:pageText key="label.machWriteWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.wpdWrite)" value="false">
                <font color="red"><b> <webapps:pageText key="label.machWriteWPD"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.wpdWrite)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.machWriteWPD"/> </b></font>
            </logic:equal>
        </td>
    </tr>

    <tr>
        <td nowrap style="padding-left:25px;">
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.internet)" value="true">
                <font color="green"><b> <webapps:pageText key="label.machInternet"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.internet)" value="false">
                <font color="red"><b> <webapps:pageText key="label.machInternet"/> </b></font>
            </logic:equal>
            <logic:equal name="desktopSecurityForm" property="value(marimba.subscription.desktop.machine.internet)" value="notconfigured">
                <font color="blue"><b> <webapps:pageText key="label.machInternet"/> </b></font>
            </logic:equal>
        </td>
    </tr>

</table>

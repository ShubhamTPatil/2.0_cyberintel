<%--
    Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/9.0.01/resources/channel/webapp/target/desktop_security_software.jsp

    @author author: Venkatesh Jeyaraman
--%>

<table border="0" cellspacing="0" cellpadding="5" width="100%">

    <logic:present name="desktopSecurityForm" property="allowedSoftwareList">
        <logic:iterate id="anApp" name="desktopSecurityForm" property="allowedSoftwareList">
            <tr>
                <td>
                    <font color="green"><b> <bean:write name="anApp" /> </b></font>

                </td>
            </tr>
        </logic:iterate>
    </logic:present>

    <logic:present name="desktopSecurityForm" property="blockedSoftwareList">
        <logic:iterate id="anApp" name="desktopSecurityForm" property="blockedSoftwareList">
            <tr>
                <td>
                    <font color="red"><b> <bean:write name="anApp" /> </b></font>
                </td>
            </tr>
        </logic:iterate>
    </logic:present>

</table>

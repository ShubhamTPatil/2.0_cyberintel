<%--
    Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/9.0.01/resources/channel/webapp/target/desktop_security_options.jsp

    @author author: Venkatesh Jeyaraman
--%>
<table border="0" cellspacing="0" cellpadding="5" width="100%">

    <tr> <td colspan="4"> &nbsp;</td> </tr>

    <logic:present name="desktopSecurityForm" property="schemeDisplay">
        <logic:equal name="desktopSecurityForm" property="schemeDisplay" value="true">
            <tr>
                <td align="right">
                    <b>
                        <webapps:pageText key="label.templateName"/> &nbsp;&nbsp;&nbsp;
                        <bean:write name="desktopSecurityForm" property="value(marimba.subscription.desktop.template.name)"/>
                    </b>
                </td>

                <td></td>

                <td align="left">
                    <b>
                        <logic:match name="desktopSecurityForm" property="value(marimba.subscription.desktop.immediate)" value="true">
                            <webapps:pageText key="immediateUpdateEnabled"/>
                        </logic:match>
                        <logic:notMatch name="desktopSecurityForm" property="value(marimba.subscription.desktop.immediate)" value="true">
                            <webapps:pageText key="immediateUpdateDisabled"/>
                        </logic:notMatch>
                    </b>
                </td>

                <td align="left">
                    <b>
                        <webapps:pageText key="forceApply"/>
                        <logic:match name="desktopSecurityForm" property="value(marimba.subscription.desktop.forceapply)" value="true">
                            <webapps:pageText key="forceApplyEnabled"/>
                        </logic:match>
                        <logic:notMatch name="desktopSecurityForm" property="value(marimba.subscription.desktop.forceapply)" value="true">
                            <webapps:pageText key="forceApplyDisabled"/>
                        </logic:notMatch>
                    </b>
                </td>
            </tr>

            <tr> <td colspan="4"> &nbsp;</td> </tr>

            <tr>
                <td align="left"> <webapps:pageText key="label.user.restrict"/> </td>
                <td align="left"> <webapps:pageText key="label.machine.restrict"/> </td>
                <td align="left"> <webapps:pageText key="label.password.policy"/> </td>
                <td align="left"> <webapps:pageText key="label.allowed.software"/> </td>
            </tr>

            <tr>
                <td valign="top"> <%@ include file="/target/desktop_security_user.jsp" %>  </td>
                <td valign="top"> <%@ include file="/target/desktop_security_machine.jsp" %> </td>
                <td valign="top"> <%@ include file="/target/desktop_security_password.jsp" %> </td>
                <td valign="top"> <%@ include file="/target/desktop_security_software.jsp" %> </td>
            </tr>

        </logic:equal>
    </logic:present>

</table>

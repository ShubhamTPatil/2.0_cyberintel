<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants,
                   com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>

<%@ include file="/includes/directives.jsp" %>

<logic:present name="main_page_m_packages">
    <table width="99%" cellpadding="0" cellspacing="0" border="0">
        <logic:iterate id="channel" name="main_page_m_packages" type="com.marimba.apps.subscription.common.objects.Channel">
            <tr>
                <td valign="top" align="left" nowrap>
                    <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                        <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                    </logic:equal>
                    <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                        <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                    </logic:equal>

                    <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                        <a style="cursor:help;" target="_blank" href="/sm/getPatches.do?patchGroupUrl=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getUrl())%>&title=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getTitle()) %>" onmouseover="return Tip('<webapps:stringescape><bean:write name="channel" property="url" filter="false" /></webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();">
                    </logic:equal>

                    <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                        <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return Tip('<bean:write name="channel" property="url" filter="false" />', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();">
                    </logic:equal>

                    <bean:write name="channel" property="title" filter="true" /></a>
                </td>
        </logic:iterate>
        </tr>
    </table>
</logic:present> <!-- closing tag for logic:present main_page_m_packages -->


<logic:notPresent name="main_page_m_packages">
    <logic:present name="main_page_package">
        <table width="100%" cellpadding="0" cellspacing="0" border="0">
            <tr>
                <logic:iterate id="channel" name="main_page_package" type="com.marimba.apps.subscription.common.objects.Channel">
                    <td valign="top" align="left" nowrap>
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                        </logic:equal>
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                        </logic:equal>

                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <a style="cursor:help;" target="_blank" href="/sm/getPatches.do?patchGroupUrl=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getUrl())%>&title=<%=com.marimba.tools.util.URLUTF8Encoder.encode(channel.getTitle()) %>" onmouseover="return Tip('<webapps:stringescape><bean:write name="channel" property="url" filter="false" /></webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();">
                        </logic:equal>
                        <logic:equal name="channel" property="<%=ISubscriptionConstants.CH_TYPE_KEY%>" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return Tip('<webapps:stringescape><bean:write name="channel" property="url" filter="false" /></webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();">
                        </logic:equal>

                        <bean:write name="channel" property="title" filter="true" /></a>
                    </td>
                </logic:iterate>
            </tr>
        </table>
    </logic:present> <!-- closing tag for logic:present main_page_package -->
</logic:notPresent>
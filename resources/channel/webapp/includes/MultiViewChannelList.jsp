<%--Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$
--%>
<%--
     Displays <Package type icon> <package name> with a roll over that shows the
     target's ID.

     Used wherever we have a package object that needs to be displayed.

     @author Selvaraj Jegatheesan
     @version 4, 2009/03/16
--%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/common_js.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants" %>

<head>
    <div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
    <link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css" />
    <script language="JavaScript" src="/shell/common-rsrc/js/table.js" ></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/master.js" ></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/overlib.js"></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/intersect.js" ></script>
</head>

<html:form name="packageDetailsViewForm" action="/removePackageSelect.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageDetailsViewForm">

    <div style="padding-left:10px; padding-right:10px;padding-top:10px; padding-bottom:10px;">
    <div class="formTop">
            <table style="width:600px" cellpadding="0" cellspacing="0">
            <tr>
              <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_top_lft.gif"></td>
              <td style="border-top:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
              <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_top_rt.gif"></td>
            </tr>
            </table>
    </div>
    <div class="formContent" id="mainSection">
    <div class="pageHeader"><span class="title"><webapps:pageText key="selected_packages" type="colhdr" shared="true"/></span></div>
    <div style="width: 100%;overflow: hidden;">
          <table border="0" cellspacing="0" cellpadding="0" style="width:100%">
                <tr>
                    <bean:size id="channelSize" name="packageDetailsViewForm" property="channelsList"/>
                    <%
                        String selectedPkgSize = channelSize.toString();
                        int pkgSize = Integer.parseInt(selectedPkgSize);
                        if (pkgSize != 0) {
                    %>
                    <td align="left" class="tableRowActions">
                        &nbsp;&nbsp;<b><bean:write name="channelSize"/>&nbsp;<webapps:pageText key="selectedPackages" type="colhdr" shared="true"/></b>
                    </td>
                    <%
                        } else {
                    %>
                    <td align="left" class="tableRowActions">
                    </td>
                    <%
                        }
                    %>
                </tr>
             </table>
        </div>
        <div class="headerSection" style="width:100%; text-align:left;" id="FOO_headerDiv">
          <table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable" style="width:100%">
                <colgroup width="8%"></colgroup>
                <colgroup width="92%"></colgroup>
                <thead>
                    <tr id="FOO_headerTable_firstRow">
                        <td nowrap class="tableHeaderCell" width="8%"><webapps:pageText key="contentType" type="colhdr" shared="true"/></td>
                        <td nowrap class="tableHeaderCell" width="84%"><webapps:pageText key="multiPackages" type="colhdr" shared="true"/></td>
                    </tr>
                </thead>
          </table>
        </div>
        <div id="dataSection" style="overflow:auto; width:580px;height:350px;">
         <%
            if (pkgSize != 0) {
        %>
         <table border="0" cellspacing="0" cellpadding="0" style="width:100%;">
            <% int folderContentsRowCount = 0; %>
             <tr>
                 <td></td><td></td>
             </tr>
             <colgroup width="8%"></colgroup>
             <colgroup width="92%"></colgroup>
                <logic:iterate id="channel" name="packageDetailsViewForm" property="channelsList" indexId="pkgidx">
                    <% if (folderContentsRowCount == 0){ %>
						  <tr BGCOLOR="#FFFFFF">
                        <% } else if (folderContentsRowCount % 2 == 0){ %>
                          <tr BGCOLOR="#FFFFFF">
                        <% } else { %>
	                      <tr class="alternateRowColor">
                        <% } %>
                            <bean:define id="channelurl" name="channel" property="url" toScope="request"/>
                            <bean:define id="channeltitle" name="channel" property="title" toScope="request"/>
                            <bean:define id="channeltype" name="channel" property="type" toScope="request"/>

                            <%
                                String patchGroupUrl = com.marimba.tools.util.URLUTF8Encoder.encode(channelurl.toString());
                                String patchGroupTitle = com.marimba.tools.util.URLUTF8Encoder.encode(channeltitle.toString());
                             %>
                              <td nowrap align="center" class="rowLevel1" width="8%">
                                <logic:equal name="channeltype" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                                    <a style="cursor:help;" target="_blank" href="/sm/getPatches.do?patchGroupUrl=<%= patchGroupUrl%>&title=<%= patchGroupTitle%>" onmouseover="return overlib(wrapDN('<webapps:stringescape><bean:write name="channelurl" /></webapps:stringescape>', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();"><img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                                </logic:equal>
                                <logic:equal name="channeltype" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                                    <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return overlib(wrapDN('<webapps:stringescape><bean:write name="channelurl" /></webapps:stringescape>', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();"><img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                                </logic:equal>
                            </td>


                            <td nowrap nowrap style="padding-left:3px;" class="rowLevel1" width="92%">
                                <logic:equal name="channeltype" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                                    <a style="cursor:help;" target="_blank" href="/sm/getPatches.do?patchGroupUrl=<%= patchGroupUrl%>&title=<%= patchGroupTitle%>" onmouseover="return overlib(wrapDN('<webapps:stringescape><bean:write name="channelurl" /></webapps:stringescape>', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();">
                                </logic:equal>
                                <logic:equal name="channeltype" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                                    <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return overlib(wrapDN('<webapps:stringescape><bean:write name="channelurl" /></webapps:stringescape>', 100), WIDTH, '150', DELAY, '200');" onmouseout="return nd();">
                                </logic:equal>

                                <bean:write name="channeltitle"/></a>
                            </td>
                   </tr>
            <% folderContentsRowCount++;%>
                </logic:iterate>
        </table>

     <%
        } else {
     %>
     <table style="width: 100%;">
        <tr></tr>
        <tr>
            <strong><font size="2"><webapps:pageText shared="true" type="package_details_area" key="NoPackageSelectedShort" /></font></strong>
        </tr>
     </table>
     <% } %>

    </div>
</div>
<div class="formBottom">
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
            <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
            <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
        </tr>
</table>
</div>
</div>
    <div align="right" style="padding-right:15px;">
             &nbsp;&nbsp;<input type="button" value="<webapps:pageText key="close" type="button" shared="true"/>" onClick="javascript:parent.hideMultiChannelsArea();">
    </div>
</html:form>
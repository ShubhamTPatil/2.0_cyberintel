<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Rahul Ravulur
	@version  $Revision$,  $Date$
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/MultiChannels.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="pkg_det" />
<%@ include file="/includes/endHeadSection.jsp" %>
<body>

<%
    try{
%>

<%@ include file="/pkg/package_view_area.jsp" %>
<%@ include file="/includes/advanced_package_search.jsp" %>

<script>
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','FOO_endOfData',0);");
    CMSOnResizeHandler.addHandler("resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);");

    var formObject;
    // Getting form value from package_navigation_area, this is used when the search is performed from the Glasspane.
    function sendURL(searchQuery) {
        // split the actual search query and text based search criteria based on #$text%& delimitter
        var srchArray = searchQuery.split("#$text%&");

        if(null != formObject) {
            formObject.elements["value(searchType)"].value = 'advanced';
            formObject.elements["value(searchQuery)"].value = srchArray[0];
            formObject.elements["value(searchText)"].value = srchArray[1];
            send(formObject, '/pkgviewInit.do?actionType=searchAction');
        }
    }
</script>

<div id="pageContent" width="100%">

    <div style="width:100%;" class="pageHeader">
        <span class="title"><webapps:pageText key="Title"/></span>
    </div>

    <%-- Errors Display --%>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <%@ include file="/includes/usererrors.jsp" %>
    </table>

    <!--<div style="width:100%">-->
    <%@ include file="/includes/help.jsp" %>
    <!--</div>-->

    <table cellpadding="0" cellspacing="0" border="0" style="margin-bottom:10px;" width="100%">
        <tr>
            <td valign="top" align="left" width="322px">
                <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0px; margin:0px; padding:0px; overflow:visible; display:block;">
                    <iframe name="pkgsFrame" src="<webapps:fullPath path="/pkg/package_navigation_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                </div>
                <div id="FOO_endOfGroupList"></div>
            </td>
            <td valign="top" style="padding-left:15px;">
                <% if (request.getParameter("page") == null) { %>
                <logic:present name="session_multipkgbool">
                    <div id="FOO_dataDiv" style="height:100px; border:0px; margin:0px; padding:0px; overflow:auto; display:block;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/pkg/package_m_details_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfData"></div>
                </logic:present>
                <logic:notPresent name="session_multipkgbool">
                    <!--look at this one for testing-->
                    <div id="FOO_dataDiv" style="height:100px; border:0px; margin:0px; padding:0px; overflow:auto; display:block;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/pkg/package_details_area.jsp" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfData"></div>
                </logic:notPresent>
                <% } else { %>
                <logic:present name="session_multipkgbool">
                    <div id="FOO_dataDiv" style="height:100px; border:0px; margin:0px; padding:0px; overflow:auto; display:block;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/pkg/package_m_details_area.jsp?page=current" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfData"></div>
                </logic:present>
                <logic:notPresent name="session_multipkgbool">
                    <div id="FOO_dataDiv" style="height:100px; border:0px; margin:0px; padding:0px; overflow:auto; display:block;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/pkg/package_details_area.jsp?page=current" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfData"></div>
                </logic:notPresent>
                <% } %>
            </td>
        </tr>
    </table>
</div>

<script>
    resizeDataSection('FOO_dataDiv','FOO_endOfData',0);
    resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);
</script>

<%
    }
    catch(Exception ex) {
        out.println(ex.toString());
    }
%>
</body>
</html>
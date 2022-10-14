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
<script type="text/javascript">
    function showAll() {
        document.packageViewForm.elements["value(search)"].value='*';
        document.packageViewForm.elements["value(searchType)"].value='basic';
        send(document.packageViewForm, '/pkgviewInit.do?actionType=searchAction');
    }

    function search() {
        document.packageViewForm.elements["value(searchType)"].value='basic';
        send(document.packageViewForm, '/pkgviewInit.do?actionType=searchAction');
    }
</script>
<style type="text/css">
    .col1 {
        width: 25%;
    }
    .col2 {
        width: 75%;
    }
    .searchTabActive {
        background-color: #FFFFFF;
        border-top:1px solid #CCCCCC;
        padding-top:2px;
        padding-bottom:2px;
    }
    .searchTabInactive {
        background-color: #627EB3;
        color: #FFFFFF;
        border-top:none;
        padding-top:2px;
        padding-bottom:2px;
    }
</style>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/common_js.jsp" %>

<script language="javascript" src="/shell/common-rsrc/js/master.js"></script>
<script type="text/javascript">
    function hideBasicSection() {
        document.getElementById("basic_cell").className = "searchTabInactive";
        document.getElementById("basic_link").style.color = "#FFFFFF";
        document.getElementById("basic_link").style.textDecoration = "underline";
        document.getElementById("basic_left_img").src = "../images/invisi_shim.gif";
        document.getElementById("basic_right_img").src = "../images/invisi_shim.gif";
        document.getElementById("basic_section").style.display = "none";
    }

    function showSearch(type) {
        if(type == "basic") {
            document.getElementById("basic_cell").className = "searchTabActive";
            document.getElementById("advanced_cell").className = "searchTabInactive";
            document.getElementById("basic_link").style.color = "#000000";
            document.getElementById("basic_link").style.textDecoration = "none";
            document.getElementById("advanced_link").style.textDecoration = "underline";
            document.getElementById("basic_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
            document.getElementById("basic_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
            document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("advanced_link").style.color = "#FFFFFF";
            document.getElementById("basic_section").style.display = "";
            document.getElementById("advanced_section").style.display = "none";
            setCookie("toggleSearchCookie_adv_pkg","basic",nextYear);
        } else if (type == "advanced") {
            document.getElementById("advanced_cell").className = "searchTabActive";
            document.getElementById("basic_cell").className = "searchTabInactive";
            document.getElementById("basic_link").style.color = "#FFFFFF";
            document.getElementById("advanced_link").style.color = "#000000";
            document.getElementById("advanced_link").style.textDecoration = "none";
            document.getElementById("basic_link").style.textDecoration = "underline";
            document.getElementById("basic_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("basic_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/tab_form_left_a.gif";
            document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/tab_form_right_a.gif";
            document.getElementById("basic_section").style.display = "none";
            document.getElementById("advanced_section").style.display = "";
            document.packageViewForm.elements["value(search)"].value='*';
            setCookie("toggleSearchCookie_adv_pkg","advanced",nextYear);
        } else if (type == "hide") {
            document.getElementById("advanced_cell").className = "searchTabInactive";
            document.getElementById("basic_cell").className = "searchTabInactive";
            document.getElementById("basic_link").style.color = "#FFFFFF";
            document.getElementById("advanced_link").style.color = "#FFFFFF";
            document.getElementById("advanced_link").style.textDecoration = "underline";
            document.getElementById("basic_link").style.textDecoration = "underline";
            document.getElementById("basic_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("basic_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("advanced_left_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("advanced_right_img").src = "/shell/common-rsrc/images/invisi_shim.gif";
            document.getElementById("basic_section").style.display = "none";
            document.getElementById("advanced_section").style.display = "none";
            setCookie("toggleSearchCookie_adv_pkg","hide",nextYear);
        }
    }
</script>

<%-- This var is used in the included package_navigation_area.jsp page.  It determines which
     action is called when the package text link is selected.
--%>
<% String packageAddAction = request.getContextPath() + "/packageDetailsAddAction.do"; %>
<% String packageAddMultiAction = request.getContextPath() + "/packageDetailsMultiAddAction.do"; %>

<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants" %>
<%@ page import = "com.marimba.webapps.intf.IMapProperty" %>
<bean:define id="pageBeanName" value="pkglist_bean" toScope="request" />

<body onLoad="resizeDataSection('dataSection','endOfGui');" onResize="resizeDataSection('dataSection','endOfGui');">
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<html:form name="packageViewForm" action="/pkgviewInit.do?actionType=searchAction" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageViewForm">

    <sm:setPagingResults formName="packageViewForm" beanName="<%= (String)pageBeanName %>" resultsName="page_pkgs_dep_rs"  />


<table border="0" cellspacing="0" cellpadding="0" style="width:99%;">
    <tr>
        <td class="tableTitle"><webapps:pageText key="Pkgs" /></td>
        <td class="pagination" align="right"><logic:present name="display_rs">
            <%-- previous/next --%>
            <% request.setAttribute("targetFrame", "pkgsFrame"); %>
            <jsp:include page="/includes/genPrevNext.jsp" />
        </logic:present>
            <logic:notPresent name="display_rs">&nbsp;</logic:notPresent>
        </td>
    </tr>
</table>

<div class="tableWrapper" style="width:99%;">
<table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:#627eb3;">
<tr valign="middle">
    <td style="padding-top:2px; padding-left:3px; border-top:1px solid #435d8d;">
        <table width="100%" border="0" cellspacing="0" cellpadding="0" class="textSmall" style="color:#FFFFFF;">
            <colgroup span="6" width="0*"/>
            <colgroup width="100%"/>
            <colgroup width="0*"/>
            <tr>
                <td width="5" align="right" valign="bottom"><img id="basic_left_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="30"></td>
                <td align="center" nowrap class="searchTabInactive" id="basic_cell"><a href="javascript:void(0);" style="color:#FFFFFF;" id="basic_link" onClick="showSearch('basic');resizeDataSection('dataSection','endOfGui');"><webapps:text key="page.ldap_nav.BasicSearch"/></a></td>
                <td width="5" align="left" valign="bottom"><img id="basic_right_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="30"></td>
                <td width="5"><img id="advanced_left_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="30"></td>
                <td align="center" nowrap class="searchTabInactive" id="advanced_cell">
                    <a href="javascript:void(0);" id="advanced_link" style="color:#FFFFFF;" onClick="showSearch('advanced');resizeDataSection('dataSection','endOfGui');"><webapps:text key="page.ldap_nav.AdvancedSearch"/></a>
                </td>
                <td width="5"><img id="advanced_right_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="30"></td>
                <td>&nbsp;</td>
                <td width="20" align="right">
                    <a href="javascript:void(0);" onClick="showSearch('hide');resizeDataSection('dataSection','endOfGui');"
                       onmouseover="MakeTip('<webapps:text key="page.ldap_nav.HideSearch" escape="js"/>');"
                       onmouseout="CloseTip();">
                        <img src="/shell/common-rsrc/images/minimize.gif" width="12" height="12" border="0">
                    </a>&nbsp;
                </td>
            </tr>
        </table>
    </td>
</tr>

<tbody id="basic_section" style="display:none;">
    <tr valign="middle">
        <td style="padding-left:3px; padding-right:3px; padding-bottom:3px;">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <colgroup width="0*"/>
                <colgroup width="100%"/>
                <colgroup width="0*"/>
                <tr>
                    <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="5"></td>
                    <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="1"></td>
                    <td><img src="/shell/common-rsrc/images/search_corner_top_rt.gif" width="5" height="5"></td>
                </tr>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="2" class="textSmall" style="background-color:#FFFFFF; color:#000000;">
                <tr>
                    <td align="right"><webapps:pageText key="search"/></td>
                    <td>
                        <html:text property="value(search)" size="20" maxlength="100" value='<%= (String) session.getAttribute("page_pkgs_dep_search") %>' onkeypress="javascript:checkTyping(document.packageViewForm, '/packageSearchDeployed.do?forwardPage=/pkg/package_navigation_area.jsp', event);"/>
                        <html:hidden property="value(searchQuery)" value='<%= (String) session.getAttribute("page_pkgs_dep_search") %>' />
                        <html:hidden property="value(searchType)"/>
                        <input type="button" name="go" value=" <webapps:pageText key="Go" type="button" /> " onclick="javascript:search();">
                        &nbsp;
                        <html:link href="javascript:showAll();"><font color="black"><webapps:pageText key="Reset"/></font></html:link>
                    </td>
                </tr>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <colgroup width="0*"/>
                <colgroup width="100%"/>
                <colgroup width="0*"/>
                <tr>
                    <td><img src="/shell/common-rsrc/images/search_corner_bot_lft.gif" width="5" height="5"></td>
                    <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="1"></td>
                    <td><img src="/shell/common-rsrc/images/search_corner_bot_rt.gif" width="5" height="5"></td>
                </tr>
            </table>
        </td>
    </tr>
</tbody>

<tbody id="advanced_section" style="display:none;">
    <tr valign="middle">
        <td style="padding-left:3px; padding-right:3px; padding-bottom:3px;">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <colgroup width="0*"/>
                <colgroup width="100%"/>
                <colgroup width="0*"/>
                <tr>
                    <td><img src="/shell/common-rsrc/images/search_corner_top_lft.gif" width="5" height="5"></td>
                    <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="1"></td>
                    <td><img src="/shell/common-rsrc/images/search_corner_top_rt.gif" width="5" height="5"></td>
                </tr>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="2" class="textSmall" style="background-color:#FFFFFF; color:#000000;">
                <tr>
                    <td align="right" valign="top" class="col1"><webapps:text key="page.ldap_nav.LDAPQuery"/></td>
                    <td class="col2" style="padding-right:3px;">
                        <html:textarea property="value(searchText)" rows="3" style="width:100%;"></html:textarea>
                    </td>
                </tr>
                <tr>
                    <td class="col1">&nbsp;</td>
                    <td align="right" class="col2" style="padding-right:3px;">
                        <input type="button" name="advsearch" class="smallButtons" value="<webapps:pageText shared="true" type="button" key="edit"/>" onclick="parent.invokeDiv();" /> <br>
                    </td>
                </tr>
            </table>
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <colgroup width="0*"/>
                <colgroup width="100%"/>
                <colgroup width="0*"/>
                <tr>
                    <td><img src="/shell/common-rsrc/images/search_corner_bot_lft.gif" width="5" height="5"></td>
                    <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="1"></td>
                    <td><img src="/shell/common-rsrc/images/search_corner_bot_rt.gif" width="5" height="5"></td>
                </tr>
            </table>
        </td>
    </tr>
</tbody>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr class="smallButtons">
        <td class="tableSearchRow">
            <table width="100%" border="0" cellspacing="0" cellpadding="2">
                <colgroup width="0*"/>
                <colgroup width="100%"/>
                <tr>
                    <td align="right" nowrap><webapps:pageText key="SelectionMode" type="global" />: </td>
                    <td>
                        <logic:present name="session_multipkgbool">
                            <%-- Since we are in the multiple select mode, the action for adding a package
                            must be set.  This is used when constructing the actions for adding --%>
                            <% packageAddAction = packageAddMultiAction; %>
                            <a href="<%=request.getContextPath()%>/switchMode.do?type=package&to=single&fwdURL=/pkg/package_frameset.jsp" target="_top" style="color:black;"><webapps:pageText key="SingleMode" type="global" /></a> | <strong><webapps:pageText key="MultipleMode" type="global" /></strong>
                        </logic:present>

                        <logic:notPresent name="session_multipkgbool">
                            <strong><webapps:pageText key="SingleMode" type="global" /></strong>
                        </logic:notPresent>
                    </td>
                </tr>
                <tr>
                    <td align="right" nowrap><webapps:pageText key="Show" /></td>
                    <td>
                            <%-- This sections is for the title of the packages.  This will sort according to the name of the title --%>
                        <logic:notPresent name="packageViewForm" property="value(show_url)">
                            <% ((IMapProperty) session.getAttribute("packageViewForm")).setValue("show_url", "false"); %>
                        </logic:notPresent>

                        <logic:equal name="packageViewForm" property="value(show_url)" value="false">
                            <strong><webapps:pageText key="PackageTitle" /></strong>
                            | <a href="<%=request.getContextPath()%>/pkgviewInit.do?displayType=url&actionType=flipAction" style="color:black;"><webapps:pageText key="PackageURL" /></a>
                        </logic:equal>

                            <%-- This sections is for the urk of the packages.  This will sort according to the name of the url --%>
                        <logic:equal name="packageViewForm" property="value(show_url)" value="true">
                            <a href="<%=request.getContextPath()%>/pkgviewInit.do?displayType=title&actionType=flipAction" style="color:black;"><webapps:pageText key="PackageTitle" /></a>
                            |
                            <strong><webapps:pageText key="PackageURL" /></strong>
                        </logic:equal>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>

<div class="headerSection" style="width:100%;">
    <table border="0" cellpadding="0" cellspacing="0" width="100%">
        <thead>
            <colgroup width="20%"/>
            <colgroup width="80%"/>
            <tr>
                <td class="tableHeaderCell">
                    <logic:notPresent name="packageViewForm" property="value(sortorder)">
                        <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=false&sorttype=type&lastsort=type&actionType=sortAction" class="columnHeading"> <webapps:pageText shared="true" type="colhdr" key="contentType" /></a> <img src="/shell/common-rsrc/images/sort_up.gif" width="7" height="6">
                    </logic:notPresent>
                    <logic:equal name="packageViewForm" property="value(sortorder)" value="true">
                        <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=false&sorttype=type&lastsort=type&actionType=sortAction" class="columnHeading"> <webapps:pageText shared="true" type="colhdr" key="contentType" /></a>&nbsp;
                        <logic:equal name="packageViewForm" property="value(lastsort)" value="type">
                            <img src="/shell/common-rsrc/images/sort_up.gif" width="7" height="6">
                        </logic:equal>
                    </logic:equal>
                    <logic:equal name="packageViewForm" property="value(sortorder)" value="false">
                        <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=true&sorttype=type&lastsort=type&actionType=sortAction" class="columnHeading"> <webapps:pageText shared="true" type="colhdr" key="contentType" /></a>&nbsp;
                        <logic:equal name="packageViewForm" property="value(lastsort)" value="type">
                            <img src="/shell/common-rsrc/images/sort_down.gif" width="7" height="6">
                        </logic:equal>
                    </logic:equal>
                </td>
                <td class="tableHeaderCell">
                        <%-- This sections is for the title of the packages.  This will sort according to the name of the title--%>
                    <logic:equal name="packageViewForm" property="value(show_url)" value="false">
                        <logic:notPresent name="packageViewForm" property="value(sortorder)">
                            <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=false&sorttype=title&lastsort=title&actionType=sortAction" class="columnHeading"> <webapps:pageText key="PkgName" /></a> <img src="/shell/common-rsrc/images/sort_up.gif" width="7" height="6">
                        </logic:notPresent>
                        <logic:present name="packageViewForm" property="value(sortorder)">
                            <logic:equal name="packageViewForm" property="value(sortorder)" value="true">
                                <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=false&sorttype=title&lastsort=title&actionType=sortAction" class="columnHeading"> <webapps:pageText key="PkgName" /></a>&nbsp;
                                <logic:equal name="packageViewForm" property="value(lastsort)" value="title">
                                    <img src="/shell/common-rsrc/images/sort_up.gif" width="7" height="6">
                                </logic:equal>
                            </logic:equal>
                            <logic:equal name="packageViewForm" property="value(sortorder)" value="false">
                                <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=true&sorttype=title&lastsort=title&actionType=sortAction" class="columnHeading"> <webapps:pageText key="PkgName" /></a>&nbsp;
                                <logic:equal name="packageViewForm" property="value(lastsort)" value="title">
                                    <img src="/shell/common-rsrc/images/sort_down.gif" width="7" height="6">
                                </logic:equal>
                            </logic:equal>
                        </logic:present>
                    </logic:equal>

                        <%-- This sections is for the url of the packages.  This will sort according to the name of the url --%>
                    <logic:equal name="packageViewForm" property="value(show_url)" value="true">
                        <logic:notPresent name="packageViewForm" property="value(sortorder)">
                            <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=false&sorttype=url&lastsort=url&actionType=sortAction" class="columnHeading"><webapps:pageText key="URL"/></a>&nbsp;<img src="/shell/common-rsrc/images/sort_up.gif" width="7" height="6">
                        </logic:notPresent>
                        <logic:present name="packageViewForm" property="value(sortorder)">
                            <logic:equal name="packageViewForm" property="value(sortorder)" value="true">
                                <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=false&sorttype=url&lastsort=url&actionType=sortAction" class="columnHeading"> <webapps:pageText key="URL"/></a>&nbsp;
                                <logic:equal name="packageViewForm" property="value(lastsort)" value="url">
                                    <img src="/shell/common-rsrc/images/sort_up.gif" width="7" height="6">
                                </logic:equal>
                            </logic:equal>
                            <logic:equal name="packageViewForm" property="value(sortorder)" value="false">
                                <a href="<%=request.getContextPath()%>/pkgviewInit.do?sortorder=true&sorttype=url&lastsort=url&actionType=sortAction" class="columnHeading"> <webapps:pageText key="URL"/></a>&nbsp;
                                <logic:equal name="packageViewForm" property="value(lastsort)" value="url">
                                    <img src="/shell/common-rsrc/images/sort_down.gif" width="7" height="6">
                                </logic:equal>
                            </logic:equal>
                        </logic:present>
                    </logic:equal>
                </td>
            </tr>
        </thead>
    </table>
</div>

<div id="dataSection" style="height:100px; width:100%; overflow:auto;">
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <colgroup width="20%"/>
        <colgroup width="80%"/>
        <logic:present name="page_pkgs_dep_rs">
            <logic:iterate id="channel" name="display_rs" type="com.marimba.webapps.tools.util.PropsBean" indexId="iteridx">
                <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>
                    <td class="rowLevel1" align="center">
                        <logic:equal name="channel" property="value(type)" value="<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>" >
                            <img src="/shell/common-rsrc/images/patch_group.gif" height="16" width="16" border="0" />
                        </logic:equal>
                        <logic:equal name="channel" property="value(type)" value="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" >
                            <img src="/shell/common-rsrc/images/package.gif" height="16" width="16" border="0" />
                        </logic:equal>
                    </td>
                    <td class="rowLevel1" >
                        <logic:equal name="packageViewForm" property="value(show_url)" value="true">
                            <!-- <a href="<%=packageAddAction %>?channelType=<%=(String) channel.getValue("type")%>&channelTitle=<%= java.net.URLEncoder.encode((String) channel.getValue("title")) %>&channelURL=<%= java.net.URLEncoder.encode((String) channel.getValue("url")) %>" target="mainFrame" class="hoverLink" onmouseover="return overlib('<bean:write name="channel" property="value(title)" filter="true" />', WIDTH, '200', DELAY, '150');" onmouseout="return nd();" ><bean:write name="channel" property="value(url)" filter="true" /></a> -->
                            <a href="<%=packageAddAction %>?channelType=<%=(String) channel.getValue("type")%>&channelTitle=<%= com.marimba.tools.util.URLUTF8Encoder.encode((String) channel.getValue("title")) %>&channelURL=<%= com.marimba.tools.util.URLUTF8Encoder.encode((String) channel.getValue("url")) %>" target="mainFrame" class="hoverLink" onmouseover="MakeTip('<webapps:stringescape><bean:write name="channel" property="value(title)" filter="false" /></webapps:stringescape>');" onmouseout="CloseTip();" ><bean:write name="channel" property="value(url)" filter="true" /></a>
                            <!-- Symbio modified 05/19/2005 -->
                        </logic:equal>
                        <logic:equal name="packageViewForm" property="value(show_url)" value="false">
                            <!-- <a href="<%= packageAddAction %>?channelType=<%=(String) channel.getValue("type")%>&channelTitle=<%= java.net.URLEncoder.encode((String) channel.getValue("title")) %>&channelURL=<%= java.net.URLEncoder.encode((String) channel.getValue("url")) %>" target="mainFrame" class="hoverLink" onmouseover="return overlib('<bean:write name="channel" property="value(url)" filter="true" />', WIDTH, '200', DELAY, '150');" onmouseout="return nd();" ><bean:write name="channel" property="value(title)" filter="true" /></a> -->
                            <a href="<%= packageAddAction %>?channelType=<%=(String) channel.getValue("type")%>&channelTitle=<%= com.marimba.tools.util.URLUTF8Encoder.encode((String) channel.getValue("title")) %>&channelURL=<%= com.marimba.tools.util.URLUTF8Encoder.encode((String) channel.getValue("url")) %>" target="mainFrame" class="hoverLink" onmouseover="MakeTip('<webapps:stringescape><bean:write name="channel" property="value(url)" filter="false" /></webapps:stringescape>');" onmouseout="return CloseTip();" ><bean:write name="channel" property="value(title)" filter="true" /></a>
                            <!-- Symbio modified 05/19/2005 -->
                        </logic:equal>
                    </td>
                </tr>
            </logic:iterate>
        </logic:present>
    </table>
</div>

</div>
</html:form>

<div id="endOfGui"></div>
<script>
    resizeDataSection('dataSection','endOfGui');
    parent.formObject = document.packageViewForm;
    // This is for hide/display basic/advanced search tab on page load
    cookie = getCookie("toggleSearchCookie_adv_pkg");
    if(cookie != null) {
        showSearch(cookie);
    }
</script>
<%@ include file="/includes/footer.jsp" %>

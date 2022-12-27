<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Michele Lin
	@version  $Revision$,  $Date$
	@version 1.69, 11/21/2002
--%>

<%@ include file="/includes/directives.jsp" %>

<bean:define id="targetAddAction" name="targetAddAction" scope="request" />
<bean:define id="targetAddMultiAction" name="targetAddMultiAction" scope="request" />
<bean:define id="numEntries" name="numEntries" scope="request" />

<jsp:useBean id="session_ldap" class="com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean" scope="session"/>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.util.Crumb" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>


<%if(session.getAttribute("disablemultimode") != null) {
    pageContext.removeAttribute("multimode");
}
%>

<div class="tableWrapper" style="width:318px;"> <%-- this div includes the dataSection --%>
<table width="100%" cellpadding="0" cellspacing="0">
<tr>
<td>
<%-- Search --%>

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
                <a id="advanced_link"></a>

                </td>
                <td width="5"><img id="advanced_right_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="30"></td>
                <td>&nbsp;</td>
                <td width="20" align="right">
                    <a href="javascript:void(0);" onClick="showSearch('hide');resizeDataSection('dataSection','endOfGui');"
                       onmouseover="MakeTip('<webapps:text key="page.ldap_nav.HideSearch" escape="js"/>');"
                       onmouseout="CloseTip();">
                        <img src="/shell/common-rsrc/images/minimize.gif" width="12" height="12" border="0">
                    </a>&nbsp;</td>
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
                    <td align="right" class="col1"><webapps:text key="page.ldap_nav.SearchFor"/></td>
                    <td style="padding-left:4px;" class="col2">
                        <html:text style="smallButtons" property="searchString" size="30" value='<%= request.getParameter("searchString") %>' onkeypress="checkTypingInLDAPNav('send',event);" />
                        <html:text name = "basicLink" style="display:none;" property="basicLink" size="30" maxlength="50" value="false" />
                    </td>
                </tr>
                <tr>
                    <td align="right" class="col1"><webapps:text key="page.ldap_nav.LimitTo"/> </td>
                    <td class="col2" style="padding-left:4px;">
                        <select name="searchType">
                            <option value="All types"><webapps:text key="page.ldap_nav.AllTypes"/></option>
                            <option value="User"><webapps:text key="page.ldap_nav.User"/></option>
                            <option value="Group"><webapps:text key="page.ldap_nav.Group"/></option>
                            <option value="Machine"><webapps:text key="page.ldap_nav.Machine"/></option>
                            <option value="Collection"><webapps:text key="page.ldap_nav.Collection"/></option>
                            <option value="Container"><webapps:text key="page.ldap_nav.Container"/></option>
                        </select>
                    </td>
                </tr>
                <logic:notEqual name="session_ldap" property="container" value="top">
                    <tr>
                        <td class="col1">&nbsp;</td>
                        <td class="col2">
                            <input type="checkbox" name="limitSearch" value="true" id="limit_search">
                            <label for="limit_search"><webapps:text key="page.ldap_nav.DoNotSearch"/></label>
                        </td>
                    </tr>
                </logic:notEqual>
                <tr>
                    <td class="col1">&nbsp;</td>
                    <td align="right" class="col2" style="padding-right:3px;">
                        <input name="Submit322" id="Submit322" type="button" class="btn btn-sm btn-primary" value="<webapps:text key="page.ldap_nav.Go" />" onClick="javascript:send('search');">
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
<logic:empty name="isTargetView" scope="session">
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


<table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color:#F8F8FF;">
<tr valign="middle">
    <td style="padding-left:3px; padding-right:3px; padding-bottom:3px;">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <colgroup span="6" width="0*"/>
            <colgroup width="100%"/>
            <colgroup width="0*"/>
            <tr>
                <td width="5" align="right" valign="bottom" ><img id="inner_advanced_left_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="19"></td>
                <td align="center" nowrap class="formSubItemSel" id="inner_advanced_cell" style="font-size:10px;">
                    <a href="javascript:void(0);" id="inner_advanced_link" onClick="showSearch('inner_advanced');resizeDataSection('dataSection','endOfGui');"><webapps:text key="page.ldap_nav.LdapQuery"/></a>
                </td>
                <td width="5" align="left" valign="bottom"><img id="inner_advanced_right_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="19"></td>
                <td width="5" style=" background-color:#F8F8FF;"><img id="inner_policy_left_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="19"></td>
                <td align="center" nowrap class="formSubItemSel" id="inner_policy_cell"  style="font-size:10px;" >
                    <a href="javascript:void(0);" id="inner_policy_link" style="color:#FFFFFF;" onClick="showSearch('inner_policy');resizeDataSection('dataSection','endOfGui');"><webapps:text key="page.ldap_nav.PolicySearch"/></a>
                </td>
                <td width="5"><img id="inner_policy_right_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="19"></td>
                <td>&nbsp;</td>
            </tr>
        </table>
    </td>
</tr>
<tbody id="inner_advanced_section" style="display:none;">
    <tr valign="middle">
        <td>
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
                    <td class="col2" colspan="2" align="center" style="padding-right:3px;">
                        <textarea name="advSearchString" rows="3" property="advSearchString" style="width:95%;"></textarea>
                    </td>
                </tr>
                <logic:notEqual name="session_ldap" property="container" value="top">
                    <tr>
                        <td class="col1">&nbsp;</td>
                        <td class="col2"><input type="checkbox" name="advLimitSearch" value="true" id="adv_limit_search">
                            <label for="adv_limit_search"><webapps:text key="page.ldap_nav.DoNotSearch"/></label></td>
                    </tr>
                </logic:notEqual>
                <tr>
                    <td class="col1">&nbsp;</td>
                    <td align="right" class="col2" style="padding-right:3px;">
                        <input name="Submit3222" id="Submit3223" type="button" class="smallButtons" value="<webapps:text key="page.ldap_nav.Go"/>" onClick="javascript:send('search');">
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
<tbody id="inner_policy_section" style="display:none;">
    <tr valign="middle">
        <td>
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
                    <td colspan="2" class="col2" style="padding-right:3px;" align="center">
                        <html:hidden property="value(searchQuery)" value=""/>
                        <textarea name="policyCriteria" rows="3" property="policyCriteria" style="width:95%;" readonly><logic:notEqual parameter="policyCriteria" value=""><%=request.getParameter("policyCriteria")%></logic:notEqual></textarea>
                    </td>
                </tr>
                <tr>
                    <td class="col1">&nbsp;</td>
                    <td align="right" class="col2" style="padding-right:3px;">
                        <input name="Submit3222" id="Submit3224" type="button" class="smallButtons" value="<webapps:text key="page.ldap_nav.Edit" />"  onclick="parent.invokeDiv();">
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

</table>
</td>
</tr>
</logic:empty>

<logic:notEmpty name="isTargetView" scope="session">
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
                        <textarea name="advSearchString" rows="3" property="advSearchString" style="width:100%;"></textarea>
                    </td>
                </tr>
                <logic:notEqual name="session_ldap" property="container" value="top">
                    <tr>
                        <td class="col1">&nbsp;</td>
                        <td class="col2"><input type="checkbox" name="advLimitSearch" value="true" id="adv_limit_search">
                            <label for="adv_limit_search"><webapps:text key="page.ldap_nav.DoNotSearch"/></label>
                        </td>
                    </tr>
                </logic:notEqual>
                <tr>
                    <td class="col1">&nbsp;</td>
                    <td align="right" class="col2" style="padding-right:3px;">
                        <input name="Submit3222" id="Submit3223" type="button" class="smallButtons" value="<webapps:text key="page.ldap_nav.Go"/>" onClick="javascript:send('search');">
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
</logic:notEmpty>

</tbody>
</table>

<%-- Javascript --%>
<script language="JavaScript" type="text/javascript">

    cookie = getCookie("toggleSearchCookie");
    if(cookie != null) {
        showSearch(cookie);
    }

</script>

<%-- Single/Multi Select Mode --%>
<logic:present name="multimode">

<table width="100%" border="0" cellspacing="0" cellpadding="0" >
    <tr valign="middle">
        <td class="tableSearchRow">
                <%--
                This should deal with multiple target.  If the request parameter, multi, exists,
        the main_page_multitarget session variable should be added.  This is done so that if
        the user leaves the navigation page and comes back, the state of what is selected will
        be saved.
         --%>

            <webapps:pageText key="SelectionMode" type="global" />:&nbsp;
            <logic:present name="session_multitgbool">
                <%-- Since we are in the multiple select mode, the action for adding a target
               must be set.  This is used when constructing the actions for adding
               --%>

                <bean:define id="targetAddAction" name="targetAddMultiAction" scope="request" toScope="request" />
                <a style="color: #000000;" href="<%=request.getContextPath()%>/switchModeTargetSingle.do?type=target&to=single&fwdURL=/main_view.jsp" target="_top"><webapps:pageText key="SingleMode" type="global" /></a>&nbsp;|&nbsp;<b><webapps:pageText key="MultipleMode" type="global" /></b>
            </logic:present>

            <logic:notPresent name="session_multitgbool">
                <strong><webapps:pageText key="SingleMode" type="global" /></strong>
            </logic:notPresent>
        </td>
    </tr>
    </logic:present>
</table>
<logic:notPresent name="multimode">

<table width="100%" border="0" cellspacing="0" cellpadding="0" >
    <tbody><tr valign="middle">
            <td class="tableSearchRow">
    <a href="/spm/overView.do?action1=overall_dashboard&name=All Endpoints&type=all&id=all"  target='mainFrame'>Overall Dashboard</a></td></tr>
    </tbody>
</table>
</logic:notPresent>
<%-- LDAP bread crumb trail --%>
<%-- leaveBreadCrumb stands for "leave bread crumb alone" --%>
<logic:notEqual name="session_ldap" property="leaveBreadCrumb" value="true">
    <logic:equal name="session_ldap" property="isGroup" value="true">
        <logic:equal name="session_ldap" property="usersInLDAP" value="false">
            <sm:setBreadCrumbTrail dn='<%= session_ldap.getGroup()  %>' objectClass='<%= session_ldap.getObjectClass()==null? "NULL":session_ldap.getObjectClass()  %>' />
        </logic:equal>
    </logic:equal>

    <logic:notEqual name="session_ldap" property="isGroup" value="true">
        <sm:setBreadCrumbTrail dn='<%= session_ldap.getContainer()  %>' objectClass='<%= session_ldap.getObjectClass()==null? "NULL":session_ldap.getObjectClass()  %>' />
    </logic:notEqual>

</logic:notEqual>


<jsp:include page ="/includes/breadCrumbTrail.jsp" />

<div id="dataSection" style="height:100px; width:100%; overflow:auto;">

    <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <colgroup span="1" width="100%"/>

        <%-- Display 'This container is empty.' or 'Search results empty.' message
   if there are no results --%>

        <%  int num_entries = Integer.parseInt((String)numEntries);
            if (( num_entries <= 0) && ("true".equals(request.getParameter("search")))) { %>

        <tr>
            <td class="rowLevel1">
                <webapps:pageText key="searchempty" type="global" />
            </td>
        </tr>
        <% } else if (num_entries <= 0) { %>
        <tr>
            <td class="rowLevel1">
                <webapps:pageText key="containerempty" type="global" />
            </td>
        </tr>
        <% } %>

        <%-- List LDAP Entries --%>
        <jsp:include page="/includes/ldap_nav_list.jsp" flush="false" />

    </table>
</div>
</td>
</tr>
</table>
</div>

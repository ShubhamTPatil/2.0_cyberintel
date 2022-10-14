<%@ include file="/includes/body.html" %>

<%--    <script src="js/help.js"></script>    --%>

<%-- the top half of the banner --%>

<%--
<%@include file="/common-rsrc/header/header.jsp" %>
--%>

<jsp:include page="/common-rsrc/header/header.jsp" flush="false" />


<%-- These tabs are only to be shown if the error did not occur on initialization --%>
<logic:notPresent name="initerror">

<sm:setActiveTab />
<table width="100%" cellspacing="0" cellpadding="0" class="blackColor" border="0">
  <tr>
    <td class="blackColor">
      <table border="0" cellspacing="0" cellpadding="0">
        <tr>
	  <%-- inactive Target View Tab --%>
          <td width="5">&nbsp;</td>

	  <logic:notEqual name="active_tab" value="target_view">
            <td valign="bottom" align="right" width="5"><a href="#"><img src="/shell/common-rsrc/images/tab_left_inactive.gif" width="6" height="22" border="0"></a></td>
            <td class="grayColor1" align="center" valign="middle" nowrap><a class="tabLink" <a href="<webapps:fullPath path="/initTargetView.do" />" target="_top">Target View </a></td>
            <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_right_inactive.gif" width="5" height="21" border="0"></a></td>
	  </logic:notEqual>

	  <%-- active Target View Tab --%>
	  <logic:equal name="active_tab" value="target_view">
            <td valign="bottom" align="right" width="5"><a href="#"><img src="/shell/common-rsrc/images/tab_left_active.gif" width="6" height="22" border="0"></a></td>
            <td class="accentColor1" align="center" valign="middle" nowrap><a class="noUnderLineLink" href="<webapps:fullPath path="/initTargetView.do" />" target="_top"><font class="generalText"><b>Target View </b></font></a></td>
            <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_right_active.gif" width="5" height="21" border="0"></a></td>
	  </logic:equal>


	  <%-- inactive Package View Tab --%>
          <td width="5">&nbsp;</td>

	  <logic:notEqual name="active_tab" value="package_view">
            <td valign="bottom" align="right"><a href="#"><img src="/shell/common-rsrc/images/tab_left_inactive.gif" width="6" height="22" border="0"></a></td>
            <td class="grayColor1" align="center" valign="middle" nowrap><a href="<%= request.getContextPath() %>/package/package_frameset.jsp" class="tabLink" target="_top">Package View </a></td>
            <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_right_inactive.gif" width="5" height="21" border="0"></a></td>
	  </logic:notEqual>

	  <%-- active Package View Tab --%>
	  <logic:equal name="active_tab" value="package_view">
            <td valign="bottom" align="right"><a href="#"><img src="/shell/common-rsrc/images/tab_left_active.gif" width="6" height="22" border="0"></a></td>
            <td class="accentColor1" align="center" valign="middle" nowrap><a href="<%= request.getContextPath() %>/package/package_frameset.jsp" class="noUnderlineLink" target="_top"><font class="generalText"><b>Package View </b></font></a></td>
            <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_right_active.gif" width="5" height="21" border="0"></a></td>
	  </logic:equal>

	  <%-- inactive New Distribution Tab --%>
          <td width="5">&nbsp;</td>

	  <logic:notEqual name="active_tab" value="new_assignment">
            <td valign="bottom" align="right"><img src="/shell/common-rsrc/images/tab_left_inactive.gif" width="6" height="22"></td>
            <td class="grayColor1" align="center" valign="middle" nowrap><a href="<webapps:fullPath path="/distInit.do?selectedTab=true" />" class="tabLink" target="_top">New Assignment </a></td>
            <td valign="bottom" align="left"><img src="/shell/common-rsrc/images/tab_right_inactive.gif" width="5" height="21"></td>
	  </logic:notEqual>

	  <%-- active New Distribution Tab --%>
	  <logic:equal name="active_tab" value="new_assignment">
            <td valign="bottom" align="right"><img src="/shell/common-rsrc/images/tab_left_active.gif" width="6" height="22"></td>
            <td class="accentColor1" align="center" valign="middle" nowrap><a href="<webapps:fullPath path="/distInit.do?selectedTab=true" />" class="noUnderlineLink" target="_top"><font class="generalText"><b>New Assignment </b></font></a></td>
            <td valign="bottom" align="left"><img src="/shell/common-rsrc/images/tab_right_active.gif" width="5" height="21"></td>
	  </logic:equal>


	  <%-- active Configuration Tab --%>
          <td width="5">&nbsp;</td>

	  <logic:equal name="active_tab" value="configuration">
          <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_left_active.gif" width="6" height="22" border="0"></a></td>
          <td valign="middle" align="center" class="accentColor1" nowrap><a href="<webapps:fullPath path="/config/config.jsp" />" class="noUnderlineLink" target="_top"><font class="generalText"><b>Configuration </b></font></a></td>
          <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_right_active.gif"  width="5" height="21" border="0"></a></td>
	  </logic:equal>

	  <%-- inactive Configuration tab --%>
	  <logic:notEqual name="active_tab" value="configuration">
          <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_left_inactive.gif" width="6" height="22" border="0"></a></td>
          <td valign="middle" align="center" class="grayColor1" nowrap><a href="<webapps:fullPath path="/config/config.jsp" />" class="tabLink" target="_top">Configuration</a></td>
          <td valign="bottom" align="left"><a href="#"><img src="/shell/common-rsrc/images/tab_right_inactive.gif"  width="5" height="21" border="0"></a></td>
	  </logic:notEqual>

          <td valign="bottom">&nbsp;</td>
        </tr>
      </table>
    </td>

    <td align="right" class="blackColor" valign="middle">
     <jsp:include page="/includes/info.jsp" flush="false" />
      <a href="javascript:CMSOpenHelp();">
        <img src="/shell/common-rsrc/images/help.gif"  width="12" height="12" align="absmiddle" border="0"></a>&nbsp;
        <a href="javascript:CMSOpenHelp();" class="bannerLink">Help
      </a>&nbsp;&nbsp;&nbsp;
    </td>
  </tr>
  <tr>
    <td colspan="2" height="5" class="accentColor1"><img src="/shell/common-rsrc/images/shim.gif" width="1" height="5"></td>
  </tr>
</table>

</logic:notPresent> <%-- End check for if this is an initialization error --%>


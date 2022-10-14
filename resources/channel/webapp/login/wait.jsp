<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/banner.jsp" %>
<%-- Javascript --%>
  <script language="JavaScript">
    function autoRefresh(){
	window.location = "<webapps:fullPath path="/initApp.do" />";	
    }
    setTimeout("autoRefresh()", 5000);
  </script>

<%-- Body content --%>
<table width="100%" border="0" cellspacing="0" cellpadding="0" class="generalText" height="100%">
  <tr>
    <td align="center" valign="middle">
      <p><font size="+2"><b>Initializing user ...</b></font></p>
      <p><img src="/shell/common-rsrc/images/status_animation.gif" width="180" height="72">
      </p>
    </td>
  </tr>
</table>

<%@ include file="/includes/footer.jsp" %>

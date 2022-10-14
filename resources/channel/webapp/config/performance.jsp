<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2004, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.

     @author	Theen-Theen Tan
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="performance" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%-- Body content --%>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="cfgview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="cfgview"/>
<% } %>
<html:form name="performanceForm" action="/performanceSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PerformanceForm">
<div align="center"> 
  <div id="contentPadding" style="width:800px">
				<div class="pageHeader"><span class="title"><webapps:pageText key="Title"/></span></div>
				<%@ include file="/includes/usererrors.jsp" %>
				<%@ include file="/includes/help.jsp" %>
				<div class="formTabs">
						<table width="100%" border="0" cellspacing="0" cellpadding="0">
									<tr> 
											<td width="5"><img src="/shell/common-rsrc/images/form_corner_top_lft.gif" width="5" height="5"></td>
											<td width="100%" style="border-top:1px solid #CCCCCC; height:5px;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="4"></td>
											<td width="5"><img src="/shell/common-rsrc/images/form_corner_top_rt.gif" width="5" height="5"></td>
									</tr>
							</table>
					</div>
					<div class="formContent" align="left">
								<table cellpadding="5" cellspacing="0">
										<colgroup width="0*"></colgroup>
										<colgroup width="100%"></colgroup>
										<tr>
										  <td colspan="2"><html:checkbox name="performanceForm" property="<%= IWebAppConstants.PERFORMANCE_SCRUBBERON %>" value="true" />
          <webapps:pageText key="Compliance" /></td>
										</tr>
								</table>
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
				<div id="pageNav"> 
      <input type="submit" class="mainBtn" name="save" value=" <webapps:pageText key="OK" type="global" /> ">      
      <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.performanceForm,'/performanceCancel.do');" >
    </div>
		</div>
</div>

</html:form>

<%@ include file="/includes/footer.jsp" %>


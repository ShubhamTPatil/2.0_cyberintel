<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Enumeration,
                 com.marimba.apps.subscription.common.objects.Subscription,
                 com.marimba.apps.subscription.common.objects.Channel,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<webapps:helpContext context="sm" topic="profile" />

<script>
function saveState(forwardaction) {
  document.tunerProfileForm.forward.value = forwardaction;
  send(document.tunerProfileForm, '/tunerProfile.do');
}
</script>

</head>

<body>
<logic:notPresent name="taskid">
    <% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>
</logic:notPresent>
<logic:present name="taskid">
    <% request.setAttribute("nomenu", "true"); %>
    <webapps:tabs tabset="bogustabname" tab="noneselected"/>
</logic:present>

<html:form name="tunerProfileForm" action="tunerProfile.do?action=load" type="com.marimba.apps.subscriptionmanager.webapp.forms.TunerProfileForm" >
<html:hidden property="forward" />
<div align="center">
  <div style="padding-left:25px; padding-right:25px;">
    <div class="pageHeader"><span class="title"><webapps:pageText key="policy" type="pgtitle" shared="true"/></span></div>
    <logic:present name="taskid">
        <div class="pageHeader">
            <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
            <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
        </div>
    </logic:present>
     <%-- Errors Display --%>
    <div style="width:100%; ">
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
    <%@ include file="/includes/usererrors.jsp" %>
    </table>
				</div>
				
				<div class="pageInfo">
      <table cellspacing="0" cellpadding="2" border="0">
        <tr>
          <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>
          <td><webapps:pageText key="IntroShort"/></td>
        </tr>
      </table>
    </div>
   
    <div class="itemStatus">
      <table cellspacing="0" cellpadding="3" border="0">
        <tr>
          <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>: </td>
		    <logic:iterate id="target" name="session_dist" property="targets">
		      <td align="left">
		        <% //String tgLabel="target"; %>
                 <bean:define id="ID" name="target" property="id" toScope="request"/>
                 <bean:define id="Name" name="target" property="name" toScope="request"/>
                 <bean:define id="Type" name="target" property="type" toScope="request"/>
                 <jsp:include page="/includes/target_display_single.jsp"/>
		      </td>
		    </logic:iterate>
        </tr>
      </table>
    </div>
        <webapps:formtabs tabset="dist" tab="adv" subtab="tunerprofile" />
        										<div class="formContent" id="dataSection" style="text-align:left; overflow:auto;">
            <div class="sectionInfo"><webapps:pageText key="SectionInfo"/></div>
            <table border="0" cellspacing="0" cellpadding="3">
             <tr>
                <td align="right" nowrap><webapps:pageText key="profile"/></td>
                <td> <bean:write name="tunerProfileForm" property="value(tunerUpdateProfile)"/> &nbsp; <input name="Submit2" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.tunerProfileForm, '/tunerProfile.do?action=start');"></td>
              </tr>
              <tr>
                <td align="right" nowrap><webapps:pageText key="profilelocation"/></td>
                <td> <bean:write name="tunerProfileForm" property="value(txName)"/></td>
              </tr>
              <%-- Profile type has been left out for now this feature will be added
              once the mim code in moved to a common area
              <tr>
                <td align="right" nowrap>Profile type: </td>
                <td><bean:write name="tunerProfileForm" property="type"/></td>
              </tr>
              --%>
             <tbody id="type_tuner_settings">
			 </tbody>
             <tbody id="type_package_settings" style="display:none;">
			 </tbody>
            </table>
            <br>
          </div>

      <!--end formContent-->
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
      <input name="Submit32" type="button" class="mainBtn" onClick="javascript:send(document.tunerProfileForm,'/distInit.do?action=preview')" value="<webapps:pageText key="preview" type="button" shared="true"/>" />
        &nbsp;
      <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:present>
      <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:notPresent>
    </div>

  <!--end supder div for padding-->
</div>
</div>
</html:form>

<script>
CMSOnResizeHandler.addHandler("resizeDataSection('dataSection','pageNav');");
resizeDataSection('dataSection','pageNav');
</script>

</body>
</html>

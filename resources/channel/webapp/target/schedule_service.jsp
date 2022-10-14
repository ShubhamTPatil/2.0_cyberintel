<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.

     Confidential and Proprietary Information of Marimba, Inc.

     @(#)schedule_service.jsp



     @author Theen-Theen Tan

     @version 1.11, 09/15/2002

--%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%@ page import="com.marimba.apps.subscription.common.objects.Target" %>
<title><webapps:pageText key="m6" type="global"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">
<style type="text/css">

<!--

/* These styles are used exclusively for this page*/



.col1 {

	width: 20%;

}

.col2 {

	width: 80%;

}

-->

</style>
<script>

// This function saves the form attributes to the DistributionBean,

// before forwarding to the next page.

// IMPORTANT: Always use to navigate between pages so that the changes are

// persistified in the session bean.

function saveState(forwardaction) {

  document.scheduleEditForm.forward.value = forwardaction;

  send(document.scheduleEditForm, '/serviceSchedSave.do');

}
function setChecked() {
    var bool = false;
    for(var i=0; i<document.scheduleEditForm.elements.length; i++)
    {
         var temp = document.scheduleEditForm.elements[i].type;
         if((temp == "radio") && (document.scheduleEditForm.elements[i].checked)) {
            if (document.scheduleEditForm.elements[i].value=="AT") {
                bool = true;
                break;
            }
         }
     }

	if (bool) {
         setUpdateTimeDisabled(false);
         setUpdateEveryDisabled(true);

	} else {
         setUpdateTimeDisabled(true);
		 setUpdateEveryDisabled(false);

	}
}

var schedule_type = new Array("false_settings","true_settings");

</script>
<script>
    var schedule_type = new Array("false_settings","true_settings");
     masterToggles = new Array("windowsTunerAnon","solarisTunerAnon")
    assocToggles = new Array();
    assocToggles[0] = new Array("windowTunerUser","windowTunerPass");
    assocToggles[1] = new Array("solarisTunerUser","solarisTunerPass");
</script>



<% ScheduleEditForm schedForm = (ScheduleEditForm) session.getAttribute("scheduleEditForm");

   boolean hasUpdate = true;

   boolean hasActive = false;

   boolean isService = true;

%>
<webapps:helpContext context="sm" topic="sub_sched" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%-- Javascript --%>
<%@ include file="/distribution/schedule.js" %>
<%-- Body content --%>
<html:form name="scheduleEditForm" action="/serviceSchedSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm">
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
<html:hidden property="forward" />
<div align="center">
  <div style="padding-left:25px; padding-right:25px;">
    <div class="pageHeader"> <span class="title"> <webapps:pageText key="policy" type="pgtitle" shared="true"/> </span> </div>
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
          <td align="left"><% //String tgLabel="target"; %>
            <bean:define id="ID" name="target" property="id" toScope="request"/>
            <bean:define id="Name" name="target" property="name" toScope="request"/>
            <bean:define id="Type" name="target" property="type" toScope="request"/>
            <jsp:include page="/includes/target_display_single.jsp"/>
          </td>
          </logic:iterate> </tr>
      </table>
    </div>
    <webapps:formtabs tabset="dist" tab="sched" />
    <%--

  Above section is similar to /distribution/schedule_header.jsp except for the listing

  of target

--%>
    <div class="formContent" id="dataSection" style="text-align:left; overflow:auto;">
      <div class="sectionInfo"> <webapps:pageText key="SectionInfo"/> </div>

      <table cellpadding="3" cellspacing="0" width="100%">
        <tr>
        <TD class=col1 align=right>
            <webapps:pageText key="schedule"/>
        </TD>
        <TD class=col2>
            <html:select styleId="SET_SCHEDULE" property="value(SET_SCHEDULE)" onchange="showSection(schedule_type,options[selectedIndex].value)">
                <html:option value="false"><webapps:pageText key="followendptschedule"/></html:option>
                <html:option value="true"><webapps:pageText key="spmsetschedule"/></html:option>
            </html:select>
          </TD>
        </tr>
        <TBODY id=false_settings></TBODY>
        <TBODY id=true_settings style="DISPLAY: none" align=center>
            <tr>
                <td/>
                <td colSpan=2>
                    <jsp:include page="/schedule.jsp"/>
                 </td>
                </tr>
        </TBODY>
      </table>


      <table border="0" cellspacing="0" cellpadding="0">
						</table>
						<table border="0" cellspacing="0" cellpadding="0" style="margin-top:10px; ">
      </table>
    </div>
    <!--end formContent-->
    <%--

      Called to initialize the widgets the from the data set by the server

    --%>
    <script>

<%--      initCalDetailsVisible();--%>
<%----%>
<%--      initDisabled();--%>

    </script>
    <div class="formBottom">
      <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
          <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
          <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
          <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
        </tr>
      </table>
    </div>
    <!--end formBottom-->
    <div id="pageNav">
      <input name="Ok" type="button" class="mainBtn" onClick="javascript:saveState('/distInit.do?action=preview');" value="<webapps:pageText key="preview" type="button" shared="true"/>">
      &nbsp;
      <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:present>
      <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:notPresent>
	</div>
  </div>
  <!--end supder div for padding-->
</div>
<!--end super div for centering-->
</html:form>

<script>
CMSOnLoadHandler.addHandler("resizeDataSection('dataSection','pageNav');");
CMSOnResizeHandler.addHandler("resizeDataSection('dataSection','pageNav');");
resizeDataSection('dataSection','pageNav');
</script>
<script>
    var showSectionVar = document.forms[0]["value(SET_SCHEDULE)"];
    var showSectionIndex = showSectionVar.selectedIndex;
    showSection(schedule_type,showSectionVar.options[showSectionIndex].value);
</script>
<script>
    var scheduleFrequencySelect = document.forms[0]["value(scheduleFrequencySelect)"];
    scheduleFrequencySelect.options[0]=null;
</script>


<jsp:include page="/schedule_refresh.jsp"/>



</body>
</html>

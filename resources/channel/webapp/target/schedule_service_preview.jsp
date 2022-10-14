<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.

     Confidential and Proprietary Information of Marimba, Inc.

     @(#)schedule_service.jsp



     @author Theen-Theen Tan

     @version 1.11, 09/15/2002

--%>



<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>

<%@ page import="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm,
                 com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean,
                 java.util.ArrayList" %>


<%@ page import="com.marimba.apps.subscription.common.objects.Target" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

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

</script>



<% ScheduleEditForm schedForm = (ScheduleEditForm) session.getAttribute("scheduleEditForm");

   boolean hasUpdate = true;

   boolean hasActive = false;

   boolean isService = true;

%>



<webapps:helpContext context="sm" topic="policy_preview" />

<%@ include file="/includes/endHeadSection.jsp" %>





<%-- Javascript --%>

<%@ include file="/distribution/schedule.js" %>



<%-- Body content --%>

<html:form name="scheduleEditForm" action="/serviceSchedSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm">

<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">

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

<html:hidden property="value(SET_SCHEDULE)" value="true" />



<div align="center"

  <div style="padding-left:25px; padding-right:25px;">

    <div class="pageHeader"> 

    <span class="title">
        <logic:present name="copy_preview" >
            <webapps:pageText key="copyPreview" type="pagehdr" shared="true"/>
        </logic:present>
        <logic:notPresent name="copy_preview" >
            <webapps:pageText key="Title"/>
        </logic:notPresent>
    </span>
    </div>
    <logic:present name="taskid">
        <div class="pageHeader">
            <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
            <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
        </div>
    </logic:present>
                <logic:present name="policy_exists">
                <div class="statusMessage" id="warning">
                   <h6>&nbsp;</h6>

                   <p><webapps:pageText key="Warningbefore"/>
                   <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
                   <webapps:pageText key="Warningafter"/></p>
                </div>
                </logic:present>

    <div class="pageInfo">

      <table cellspacing="0" cellpadding="2" border="0">

        <tr>

          <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>

          <td><webapps:pageText key="IntroShort"/></td>

        </tr>

      </table>

    </div>



	<%-- Errors Display --%>

    <%@ include file="/includes/usererrors.jsp" %>





    <div class="itemStatus">

      <table cellspacing="0" cellpadding="3" border="0">

        <tr>

        <td valign="top"><logic:present name="session_copy" ><webapps:pageText key="copyfrom" type="colhdr" shared="true"/></logic:present><logic:notPresent name="session_copy" ><webapps:pageText key="targets" type="colhdr" shared="true"/></logic:notPresent>: </td>

		  <logic:iterate id="target" name="session_dist" property="targets">

		    <td align="left">

                <bean:define id="ID" name="target" property="id" toScope="request"/>
                <bean:define id="Name" name="target" property="name" toScope="request"/>
                <bean:define id="Type" name="target" property="type" toScope="request"/>
		        <jsp:include page="/includes/target_display_single.jsp"/>

		    </td>

		  </logic:iterate>

        </tr>

      </table>
              <logic:present name="session_copy" >
              <table cellspacing="0" cellpadding="3" border="0">
              <tr>
                  <td valign="top" align="right">&nbsp;&nbsp;&nbsp;&nbsp;<webapps:pageText key="copyto" type="colhdr" shared="true"/>: </td>
                  <jsp:include page="/copy/copy_target_display.jsp" />
              </tr>
              </table>
              </logic:present>

    </div>
  
    <logic:present name="copy_preview">
        <div valign="top" align="left" style="margin-bottom:8px; " class="tableTitle"><webapps:pageText key="Heading" /></div>
    </logic:present>

        <logic:notPresent name="copy_preview">
            <webapps:formtabs tabset="distPreview" tab="sched" />
        </logic:notPresent>
        <logic:present name="copy_preview">
            <webapps:formtabs tabset="copyPreview" tab="sched" />
        </logic:present>






<%--

  Above section is similar to /distribution/schedule_header.jsp except for the listing

  of target

--%>



    <div class="formContent" id="mainSection" style="text-align:left; overflow:auto;">


      <table border="0" cellspacing="0" cellpadding="5">

	    <tr>
        <logic:equal name="scheduleEditForm" value="true" property="value(SET_SCHEDULE)" >
          <td noWrap align=right>

            <webapps:pageText key="updateFrequency" />

          </td>


	      <td>

            <webapps:translateSchedule name="scheduleEditForm" property="scheduleString"/>

          </td>

        </logic:equal>

        <logic:equal name="scheduleEditForm" value="false" property="value(SET_SCHEDULE)" >

              <td align=middle>  <webapps:pageText key="noPropertiesSet" /> </td>

        </logic:equal>


		</tr>

	  </table>

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



	<!--end formBottom-->

    <div id="pageNav">
      <logic:present name="copy_preview" >
            <input name="copy" type="button" class="mainBtn" accesskey="N" onClick="javascript:send(document.scheduleEditForm, '/copySaveTarget.do');" value="<webapps:pageText key="copy" type="button" shared="true"/>">
            &nbsp;
            <input name="Submit32" type="button" onClick="javascript:send(document.scheduleEditForm,'/copyAdd.do?action=back');" value="<webapps:pageText key="backToEdit" type="button" shared="true"/>">
            &nbsp;
            <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/copyCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:present>
      <logic:notPresent name="copy_preview">
	  <input name="save" type="submit" class="mainBtn" accesskey="N" onClick="javascript:send(document.scheduleEditForm, '/distSave.do');" value="<webapps:pageText key="save" type="button" shared="true"/>">
      &nbsp;

      <input name="Submit32" type="submit" onClick="javascript:send(document.scheduleEditForm, '/serviceSchedLoad.do');" value="<webapps:pageText key="backToEdit" type="button" shared="true"/>">
      &nbsp;

      <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:present>
      <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:notPresent>
       </logic:notPresent>
    </div>

  </div>

  <!--end supder div for padding-->

</div>

<!--end super div for centering-->

</html:form>


</body>
<script>
CMSOnResizeHandler.addHandler("resizeDataSection('mainSection','pageNav');");
resizeDataSection('mainSection','pageNav');
</script>

</html>

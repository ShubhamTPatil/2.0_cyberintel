<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>

<%@ page import = "java.util.*" %>

<%@ page import = "com.marimba.apps.subscription.common.objects.Channel" %>

<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>

<%@ page import = "com.marimba.webapps.intf.IMapProperty" %>

<%@ include file="/includes/startHeadSection.jsp" %>

<webapps:helpContext context="sm" topic="common_sched" />

<title><webapps:pageText key="m6" type="global"/></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>


<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">

<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>


<html:form name="addTargetEditForm" action="/addTargetPackageState.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.AddTargetEditForm">

<html:hidden property="forward" />

<div style="text-align:center;">

  <div style="padding-left:15px; padding-right:15px;">

    <div class="pageHeader" style="width:720px;"><span class="title"><webapps:pageText key="Title"/></span></div>

    <div class="pageInfo" style="width:720px;">

      <table cellspacing="0" cellpadding="2" border="0">

        <tr>

          <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>

          <td><webapps:pageText key="IntroShort"/></td>

        </tr>

      </table>

    </div>





  <%-- Errors Display --%>

  <table width="720" border="0" cellspacing="0" cellpadding="0">

    <%@ include file="/includes/usererrors.jsp" %>

  </table>



    <div class="itemStatus" style="width:720px;">

      <table cellspacing="0" cellpadding="3" border="0">

        <tr>

          <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>:	   </td>


                   <logic:present name="add_selected_list" >
                   <logic:iterate id="target" name="add_selected_list" type="com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap">

                    <td nowrap align="left">
                    <% //String tgLabel="target"; %>
                    <bean:define id="ID" name="target" property="id" toScope="request"/>
                    <bean:define id="Name" name="target" property="name" toScope="request"/>
                    <bean:define id="Type" name="target" property="type" toScope="request"/>
                    <jsp:include page="/includes/target_display_single.jsp"/>
                    </td>
                    </logic:iterate>
                    </logic:present>
        </tr>

        </table>
        <table cellspacing="0" cellpadding="3" border="0">

	<tr>

          <td valign="top"><webapps:pageText key="pkgs" type="colhdr" shared="true"/>:
	  </td>

	  <td nowrap align="left">
          <logic:present name="main_page_m_packages" >
              <logic:iterate id="app" name="main_page_m_packages">
                    <a href="javascript:void(0);" class="noUnderlineLink" style="cursor:help;" onmouseover="return overlib('<webapps:stringescape><bean:write name="app" property="url" filter="false" /></webapps:stringescape>', WIDTH, '150', DELAY, '200', LEFT, OFFSETX, 50);" onmouseout="return nd();">
                    <img src="/shell/common-rsrc/images/package.gif" border="0" />
                    <bean:write name="app" property="title" filter="true"/>
                    </a>&nbsp;
              </logic:iterate>
          </logic:present>
          <logic:notPresent name="main_page_m_packages" >
            <logic:iterate id="app" name="main_page_package">
                <a href="javascript:void(0);" class="noUnderlineLink" style="cursor:help;" onmouseover="return overlib('<webapps:stringescape><bean:write name="app" property="url" filter="false" /></webapps:stringescape>', WIDTH, '150', DELAY, '200', LEFT, OFFSETX, 50);" onmouseout="return nd();">
                <img src="/shell/common-rsrc/images/package.gif" border="0" />
                <bean:write name="app" property="title" filter="true"/>
                </a>&nbsp;
            </logic:iterate>
          </logic:notPresent>
	  </td>

        </tr>

      </table>

    </div>



 <table width="720" border="0" cellspacing="0" cellpadding="3">

      <colgroup span="2" width="50%">
</colgroup>

      <tr>

        <td valign="top">

	   <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">

            <tr>

              <td class="tableRowActions" style="height:20px;">

		  <table width="100%" border="0" cellspacing="0" cellpadding="0">

                     <tr>

                        <td class="textWhite">

			  <strong><webapps:pageText key="priStateTitle" shared="true" type="distribution_assignment"/></strong>

			</td>

                        <td align="right">

			    <input name="Submit3" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.addTargetEditForm, '/addTargetSchedEdit.do?schedType=initial');" >

                        </td>

         
           </tr>

                 </table>

	      </td>

            </tr>

            <tr>
                   <td valign="top">
                   <logic:present name="add_common_target" property="initSchedule">
                      <logic:equal name="add_common_target" property="initSchedule" value="inconsistent">
                     <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                     </logic:equal>
                  </logic:present>
                  <sm:scheduleDisplay name="add_common_target" property="initSchedule" type="initial" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" />
                    </td>
                  </tr>


          </table></td>

	  <td valign="top">

	  <table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">

	   <tr>

	     <td class="tableRowActions" style="height:20px;">

		<table width="100%" border="0" cellspacing="0" cellpadding="0">

                  <tr>

                    <td height="21" class="textWhite">

			<strong><webapps:pageText key="updateTitle" shared="true" type="distribution_assignment"/></strong>

		    </td>

                    <td align="right">

	              
   <input name="Submit33" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.addTargetEditForm, '/addTargetSchedEdit.do?schedType=update');" >

                     </td>

                  </tr>
                </table>

              </td>

            </tr>

	    <tr>
              <td valign="top">
                <logic:present name="add_common_target" property="updateSchedule">
                      <logic:equal name="add_common_target" property="updateSchedule" value="inconsistent">
                     <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                     </logic:equal>
                  </logic:present>
                <sm:scheduleDisplay name="add_common_target" property="updateSchedule" type="udpate" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" /></td>

            </tr>

          </table>

	  </td>

      </tr>

      <tr>

        <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">

            <tr>

              <td class="tableRowActions" style="height:20px;">

		  <table width="100%" border="0" cellspacing="0" cellpadding="0">

                     <tr>

                       <td class="textWhite">

				<strong><webapps:pageText key="secStateTitle" shared="true" type="distribution_assignment"/></strong>

		 
      </td>

                       <td align="right">

				<input name="Submit342" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.addTargetEditForm, '/addTargetSchedEdit.do?schedType=secondary');" >

                       </td>

                     </tr>

                 </table>

		</td>

            </tr>

            <tr>
              <td valign="top">
              <logic:present name="add_common_target" property="secSchedule">
                      <logic:equal name="add_common_target" property="secSchedule" value="inconsistent">
                     <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                     </logic:equal>
                  </logic:present>
            <sm:scheduleDisplay name="add_common_target" property="secSchedule" type="secondary" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" /></td>

            </tr>

          </table></td>

        <td valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="2" style="border:1px solid #435d8d; height:75px;">

            <tr>

              <td class="tableRowActions" style="height:20px;">

	        
<table width="100%" border="0" cellspacing="0" cellpadding="0">

                     <tr>

                       <td class="textWhite">

				<strong><webapps:pageText key="verifyTitle" shared="true" type="distribution_assignment" /></strong>

		       </td>

             
          <td align="right">

				<input name="Submit342" type="button" class="smallButtons" value="<webapps:pageText key="edit" type="button" shared="true"/>" onClick="javascript:send(document.addTargetEditForm, '/addTargetSchedEdit.do?schedType=verrepair');" >

                  
     </td>

                     </tr>

                 </table>

	      </td>

            </tr>

            <tr>
              <td valign="top">
                <logic:present name="add_common_target" property="verRepairSchedule">
                      <logic:equal name="add_common_target" property="verRepairSchedule" value="inconsistent">
                     <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16">
                     </logic:equal>
                  </logic:present>
            <sm:scheduleDisplay name="add_common_target" property="verRepairSchedule" type="verrepair" activeFont="textGeneral" inactiveFont="inactiveText" doubleSpace="false" /></td>

            </tr>

          </table></td>

      </tr>

    </table>

    <div id="pageNav" style="width:720px;">
       <input name="Ok" type="button" class="mainBtn" onClick="javascript:send(document.addTargetEditForm, '/addTargetSchedCommonSave.do');" value="<webapps:pageText key="OK" type="global" shared="true"/>">
      &nbsp;

      <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/addTargetSchedCommonSave.do?action=cancel');" value="<webapps:pageText key="Cancel" type="global" shared="true"/>">

    </div>

  </div>

  <!--end supder div for padding-->

</div>

<!--end super div for centering-->

</html:form>



<%@ include file="/includes/footer.jsp" %>

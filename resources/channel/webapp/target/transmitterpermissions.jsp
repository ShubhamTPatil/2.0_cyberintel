<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Enumeration,

                 com.marimba.apps.subscription.common.objects.Subscription,

                 com.marimba.apps.subscription.common.objects.Channel"%>


<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>

<webapps:helpContext context="sm" topic="tx_login" />
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
 <%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<script>

function saveState(forwardaction) {

  document.transmitterPermissionsForm.forward.value = forwardaction;

  send(document.forms.transmitterPermissionsForm, '/transLoginSave.do');

}

</script>


<script>

var singleOptionElements = new Array("button_edit_assignment","button_delete_assignment")

var multiOptionElements = new Array()

</script>

</head>

<bean:define name="session_tloginbean" id="tloginbean" scope="session" type="com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean"/>

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


<html:form name="transmitterPermissionsForm" action="/transLoginSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TransLoginForm" >

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

					<div>

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

		        <% ///String tgLabel="target"; %>
                 <bean:define id="ID" name="target" property="id" toScope="request"/>
                 <bean:define id="Name" name="target" property="name" toScope="request"/>
                 <bean:define id="Type" name="target" property="type" toScope="request"/>
                 <jsp:include page="/includes/target_display_single.jsp"/>

		      </td>

		    </logic:iterate>
                                                                    
        </tr>

      </table>

    </div>

        <webapps:formtabs tabset="dist" tab="adv" subtab="txperm" />

       <div class="formContent" style="text-align:left;">

      <div class="sectionInfo"><webapps:pageText key="SectionInfo"/></div>

      <div class="tableWrapper" style="width:100%; overflow:hidden; margin-top:15px;">

	    <table width="100%" border="0" cellspacing="0" cellpadding="0">

		    <tr valign="middle" class="smallButtons">

			    <td class="tableRowActions">

                <input type="button"disabled="true" id="button_edit_assignment" onClick="javascript:send(document.forms.transmitterPermissionsForm,'/transLoginUserEdit.do');" value="<webapps:pageText key="Edit" />">

                <input type="button" disabled="true" id="button_delete_assignment" onClick="javascript:send(document.forms.transmitterPermissionsForm,'/transLoginUserRemove.do');" value="<webapps:pageText key="Delete" />">

    		    </td>

                <td align="right" class="tableRowActions"><input type="button"  onClick="javascript:redirect('/transLoginUserNew.do');" value="<webapps:pageText key="Add" />">
				</td>

			</tr>

		</table>

		<div class="headerSection" style="width:100%; text-align:left">

		<table border="0" cellpadding="0" cellspacing="0">

		<colgroup width="0*">

		</colgroup>

		<colgroup span="2" width="50%">

		</colgroup>

		<thead>

		<tr>

		<td class="tableHeaderCell">

        <input type="checkbox" name="trans_all" value="checkbox" id="trans_all" onClick="checkboxToggle('trans')"></td>

		<td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText key="Transmitter"/></a></td>

		<td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText key="LoginName" /></a></td>

		</tr>

		</thead>

		</table>

		</div>

		<!--end headerSecion-->

        <div id="dataSection" style="height:150px; width:100%; overflow:auto; text-align:left">

            <table border="0" cellpadding="0" cellspacing="0">

			<colgroup width="0*">

			</colgroup>

			<colgroup span="2" width="50%">

			</colgroup>

            <%-- Iterate through the list of transmitters that are stored in the session variable --%>

	            <logic:iterate id="trans" name="tloginbean" property="transmitters" indexId="iteridx">

			    <tr <% if(iteridx.intValue() % 2 == 1) {%>class="alternateRowColor"<%}%>>

			        <td class="rowLevel2">

                        <html:checkbox property='value(transmitterlist)' value='<%="trans_" + (iteridx.intValue())%>' styleId='<%="trans_" + iteridx.toString() %>'  onclick="processCheckbox(this.id)"></html:checkbox>

                    </td>

			        <td class="rowLevel2" ><bean:write name="trans" /></td>

			        <td class="rowLevel2" >

                     <%= (tloginbean.getUser((String)trans) == null

                    || tloginbean.getUser((String)trans).length() == 0) ?

                    "&nbsp;" : tloginbean.getUser((String)trans) %>

                    </td>

				</tr>

                </logic:iterate>

		    </table>

		</div>

     </div>




  </div>

      <!--end formContent-->

          <div class="formBottom">      <table width="100%" cellpadding="0" cellspacing="0">

        <tr>

          <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>

          <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>

          <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>

        </tr>

      </table>

    </div>

    <div id="pageNav">

        <input name="Submit32" type="button" class="mainBtn" onClick="javascript:saveState('/distInit.do?action=preview')" value="<webapps:pageText key="preview" type="button" shared="true"/>" />

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


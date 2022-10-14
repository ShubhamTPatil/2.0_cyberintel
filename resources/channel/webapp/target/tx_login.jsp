<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)tx_login.jsp

     @author Angela Saval
     @version 1.7, 03/21/2002
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="sm" topic="tx_login" />
<%@ include file="/includes/endHeadSection.jsp" %>

<%@ include file="/includes/banner.jsp" %>

<script src="../table.js" language="JavaScript"></script>
<script>
var singleOptionElements = new Array("button_edit_assignment","button_delete_assignment")
var multiOptionElements = new Array()
</script>


<bean:define name="session_tloginbean" id="tloginbean" scope="session" type="com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean"/>
<html:form name="transmitterLoginForm" action="/transLoginSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TransLoginForm" >

<br>
<table width="60%" border="0" cellspacing="0" cellpadding="5" class="generalText">
  <tr>
    <td><font class="pageTitle"><webapps:pageText key="Title" /> </font></td>
  </tr>

  <bean:define id="tgDisplayList" name="main_page_target" />
  <%@ include file="/includes/target_display_comma.jsp" %>

  <%@ include file="/includes/usererrors.jsp" %>

  <tr><td>
      <%@ include file="/includes/help.jsp" %>
  </td></tr>

  <tr>
  <td>
      <input type="button" id="button_new_assignment" onClick="javascript:redirect('/transLoginUserNew.do');" value="<webapps:pageText key="AddTransmitterLogin" />">
  </td>
  </tr>
   <tr>
    <td>
      <table border="1" cellspacing="0" cellpadding="5" bordercolor="cccccc" class="generalText" id="db_list_table" width="100%">
        <tr valign="middle">
          <td class="coreColor1" colspan="3" align="right"><font class="whiteText"><webapps:pageText key="ForSelectedTransmitter"/>:</font> &nbsp;
            <input type="button"  disabled="true" id="button_edit_assignment" onClick="javascript:send(document.transmitterLoginForm,'/transLoginUserEdit.do');" value="<webapps:pageText key="Edit" />">
            <input type="button"  disabled="true" id="button_delete_assignment" onClick="javascript:send(document.transmitterLoginForm,'/transLoginUserRemove.do');" value="<webapps:pageText key="Delete" />">
          </td>
        </tr>
        <tr>
          <td class="accentColor1" width="1%">&nbsp;</td>
          <td class="accentColor1" width="55%">
	  <font class="generalText"><webapps:pageText key="Transmitter"/></font></td>
          <td class="accentColor1" width="40%"><webapps:pageText key="LoginName" />
	  </td>
        </tr>
	 <%-- Iterate through the list of transmitters that are stored in the session variable --%>
	 <logic:iterate id="trans" name="tloginbean" property="transmitters" indexId="iteridx">
	 <tr>
          <td>
            <html:radio property="transmitterlist" value='<%="trans_" + iteridx.toString()%>' styleId='<%="trans_" + iteridx.toString() %>'  onclick="processCheckbox(this.id)"/>
          </td>
          <td><font class="generalText"><bean:write name="trans" /></font></td>
          <td><font class="generalText">
            <%= (tloginbean.getUser((String)trans) == null
                    || tloginbean.getUser((String)trans).length() == 0) ?
                    "&nbsp;" : tloginbean.getUser((String)trans) %>
         </font></td>
        </tr>
	</logic:iterate>
      </table>
    </td>
  </tr>
  <tr>
    <td>
      <hr size="1" noshade width="100%">
    </td>
  </tr>
  <tr align="right">
    <td>
      <input type="submit" id="OK" value=' <webapps:pageText key="OK" /> '>
      &nbsp;
      <input type="button" name="Cancel" value=" Cancel " onClick="javascript:send(document.transmitterLoginForm,'/transLoginCancel.do');" styleId="Cancel">
      </td>
  </tr>
</table>
</html:form>
<%@ include file="/includes/copyright.html" %>
</body>
</html:html>

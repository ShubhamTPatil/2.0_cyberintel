<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2002, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)delete_preview_body.jsp, 1.0, 07/30/2002     
     
     @author Theen-Theen Tan
     @version 	1.0, 07/30/2002     
--%>

<table width="100%" border="0" cellspacing="0" cellpadding="10" class="generalText">
  <tr> 
    <td colspan="2"><font class="pageTitle"><webapps:pageText key="Title" /></font></td>
  </tr>
  
  <tr> 
    <td valign="middle" width="20"><img src="/shell/common-rsrc/images/warning.gif" width="32" height="28"></td>
    <td valign="middle"><font class="orangeText"><b><%@ include file="/includes/help.jsp" %></b></font></td>
  </tr>
  <tr> 
    <td valign="middle">&nbsp;</td>
    <td valign="middle">
      <table width="400" border="1" cellspacing="0" cellpadding="5" bordercolor="cfdced">
        <tr> 
          <td width="200" class="coreColor3"><font class="generalText"><b><webapps:pageText key="Targets" /></b></font></td>
        </tr>
	
	 <logic:iterate id="target" name='page_tgs_todelete' type="com.marimba.apps.subscription.common.objects.Target">
          <tr>
	    <td align="left">
	       <% //String tgLabel="target"; %>
             <bean:define id="ID" name="target" property="id" toScope="request"/>
             <bean:define id="Name" name="target" property="name" toScope="request"/>
             <bean:define id="Type" name="target" property="type" toScope="request"/>
             <jsp:include page="/includes/target_display_single.jsp"/>
	    </td>
	  </tr>	  
          </logic:iterate>
	  
      </table>
      <br>
      <table width="400" border="1" cellspacing="0" cellpadding="5" bordercolor="cfdced">
        <tr> 
          <td valign="top" class="coreColor3"><font class="generalText"><b><webapps:pageText key="Packages" /></b></font></td>
        </tr>

	<logic:iterate id="app" name="page_pkgs_todelete">
        <tr> 
          <td valign="top"><font class="generalText"><bean:write name="app" property="url" /></font></td>
        </tr>
	</logic:iterate>
      </table>
    </td>
  </tr>

<!--
     This section only appears if the user has selected all of the packages to
     be deleted.  This selection is only available in single select mode.
-->
<logic:equal name="display_delete_props" value="true">
  <tr> 
    <td valign="middle">&nbsp;</td>
    <td valign="middle">
      <p><webapps:pageText key="DeleteAll" /></p>
      <p>
        <html:radio property="page_pkgs_delete_all" value="true" ><webapps:pageText key="Yes" /></html:radio>
	<html:radio property="page_pkgs_delete_all" value="false" ><webapps:pageText key="No" /></html:radio>
      </p>
    </td>
  </tr>
</logic:equal>

<logic:equal name="display_delete_props" value="false">	
  <html:hidden property="page_pkgs_delete_all" value="false" />
</logic:equal>

<!-- End Section -->  

  <tr> 
    <td valign="middle" colspan="2"> 
      <hr size="1" noshade width="100%">
    </td>
  </tr>
  <tr align="right"> 
    <td colspan="2"> 
      <input type="submit" name="save" value=" <webapps:pageText key="OK" type="global" /> ">
      &nbsp; 
      <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:redirect('/returnToOrigin.do');" >
    </td>
  </tr>
</table>

<p>&nbsp;</p>
</html:form>

<%@ include file="/includes/footer.jsp" %>

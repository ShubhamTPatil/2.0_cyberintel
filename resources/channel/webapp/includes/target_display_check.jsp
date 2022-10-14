<%-- Copyright 2002, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)target_display_comma.jsp

     @author Michele Lin
     @author Rahul Ravulur
     @author Theen-Theen Tan
     @version 1.4, 11/27/2002
--%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%--
     Displays <target type icon> <target name> with a roll over that shows the
     target's ID.
     Expects the a page variable call "tgLabel" that stores the name of the
     target object.
     Used wherever we have a target object that needs to be displayed.
--%>
  <logic:equal name="Type" value="domain">
    href="javascript:void(0);" style="cursor:help;">
	   <img src="/shell/common-rsrc/images/domain.gif" border="0" align="absmiddle" />
  </logic:equal>
  <logic:equal name="Type" value="container">
    href="javascript:void(0);" style="cursor:help;">
	   <img src="/shell/common-rsrc/images/folder.gif" border="0" align="absmiddle" />
  </logic:equal>
  <logic:equal name="Type" value="usergroup">
	   href="javascript:void(0);" style="cursor:help;">
    <img src="/shell/common-rsrc/images/user_group.gif" border="0" align="absmiddle" />
  </logic:equal>
  <logic:equal name="Type" value="machinegroup">
	   href="javascript:void(0);" style="cursor:help;">
    <img src="/shell/common-rsrc/images/user_group.gif" border="0" align="absmiddle" />
  </logic:equal>
  <logic:equal name="Type" value="user">
	   href="javascript:void(0);" style="cursor:help;">
	   <img src="/shell/common-rsrc/images/user.gif" border="0" align="absmiddle" />
  </logic:equal>
  <logic:equal name="Type" value="machine">
    <logic:present name="linktorc" scope="page">
	    	 href="javascript:openReport('<webapps:stringescape><bean:write name="Name" property="name" filter="false" /></webapps:stringescape>');"
	   </logic:present>
    <logic:notPresent name="linktorc" scope="page">
		    href="javascript:void(0);" style="cursor:help;"
	   </logic:notPresent>
 	   >
	    <img src="/shell/common-rsrc/images/machine.gif" border="0" align="absmiddle" />
   </logic:equal>
   <logic:equal name="Type" value="collection">
	     href="javascript:void(0);" style="cursor:help;">
	     <img src="/shell/common-rsrc/images/collections.gif" border="0" align="absmiddle" />
   </logic:equal>
   <logic:equal name="Type" value="external">
     href="javascript:void(0);" style="cursor:help;">
	    <img src="/shell/common-rsrc/images/external_target.gif" border="0" align="absmiddle" />
   </logic:equal>
   <logic:equal name="Type" value="all">
     href="javascript:void(0);" style="cursor:help;">
	    <img src="/shell/common-rsrc/images/all.gif" border="0" align="absmiddle" />
   </logic:equal>
	<logic:equal name="Type" value="<%=com.marimba.apps.subscription.common.LDAPVars.ORPHAN_POLICY%>">
     href="javascript:void(0);" style="cursor:help;">
	    <img src="/shell/common-rsrc/images/trash.gif" border="0" align="absmiddle" />
   </logic:equal>
   <logic:equal name="Type" value="site">
     href="javascript:void(0);" style="cursor:help;">
	    <img src="/shell/common-rsrc/images/network.gif" border="0" align="absmiddle" />
   </logic:equal>
      <logic:equal name="Type" value="device_groups">
     href="javascript:void(0);" style="cursor:help;">
	    <img src="/shell/common-rsrc/images/devicegroups.gif" border="0" align="absmiddle" />
   </logic:equal>
      <logic:equal name="Type" value="device_group">
     href="javascript:void(0);" style="cursor:help;">
	    <img src="/shell/common-rsrc/images/devicegroup.gif" border="0" align="absmiddle" />
   </logic:equal>
      <logic:equal name="Type" value="device">
     href="javascript:void(0);" style="cursor:help;">
	    <img src="/shell/common-rsrc/images/device.gif" border="0" align="absmiddle" />
   </logic:equal>
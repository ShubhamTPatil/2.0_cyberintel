<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)breadCrumbTrail.jsp

     @author Michele Lin
     @version 1.12, 11/19/2002
--%>
<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.util.Crumb" %>
     
<%! int count; %>
<%! int length; %>
<% boolean isSearch = new Boolean(request.getParameter("search")).booleanValue(); %>

<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr valign="middle" class="smallButtons">
   <td class="tableRowActions">
      <font class="textWhite">
        <% length = ((Crumb[]) session.getAttribute("page_breadcrumb")).length; %>
	<% count = 0; %>
        <logic:iterate id="bc" name="page_breadcrumb" scope="session" type="com.marimba.apps.subscriptionmanager.webapp.util.Crumb">
	  <%-- The '>' breadcrumb separator --%>	
	  <% if (count == 0) {} else { %>&nbsp;&gt;&nbsp;<% } %>
	  
	  <%-- entry point image --%>
	  <%String gifpath="/shell/common-rsrc/images/folder_entry.gif";%>
	  <logic:equal name="bc" property="type" value="ep">
		  <logic:equal name="session_ldap" property="usersInLDAP" value="false">
		     <%String txorgroup="false";%>

		     <logic:equal name="bc" property="msgId" value="page.global.People">
		       <%txorgroup="true";
		         gifpath="/shell/common-rsrc/images/tx.gif";
		       %>		       
		     </logic:equal>
		     <logic:equal name="bc" property="msgId" value="page.global.Groups">
		       <%txorgroup="true";
		         gifpath="/shell/common-rsrc/images/tx.gif";
			 %>	       
		     </logic:equal>
		     <%if ("false".equals(txorgroup)) {%>
		         <%gifpath="/shell/common-rsrc/images/folder_entry.gif";	%>
		     <%}%>	     
		  </logic:equal>
		  <logic:notEqual name="session_ldap" property="usersInLDAP" value="false">
		         <%gifpath="/shell/common-rsrc/images/folder_entry.gif";	%>
		  </logic:notEqual>	
		  <logic:equal name="bc" property="msgId" value="page.global.Sites">
		       <%gifpath="/shell/common-rsrc/images/network.gif";%>	       
		  </logic:equal>
		  <logic:equal name="bc" property="msgId" value="page.global.DeviceGroups">
		       <%gifpath="/shell/common-rsrc/images/network.gif";%>	       
		  </logic:equal>
  		  <logic:equal name="bc" property="msgId" value="page.global.DeviceGroup">
		       <%gifpath="/shell/common-rsrc/images/network.gif";%>	       
		  </logic:equal>

		  <logic:equal name="bc" property="msgId" value="page.global.Device">
		       <%gifpath="/shell/common-rsrc/images/network.gif";%>	       
		  </logic:equal>
          <% if (count < length - 1 || isSearch) { %>
	    <%-- make the crumb a link --%>
	    <a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><img src="<%=gifpath%>" border="0" align="absmiddle"></a>&nbsp;<a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><bean:write name="bc" filter="true" property="name"/></a>
	  <% } else { %>
	    <img src="<%=gifpath%>" border="0" align="absmiddle">&nbsp;<bean:write name="bc" filter="true" property="name"/>
	  <% } %>	  
	  </logic:equal>
	  
	  <%-- container image --%>
	  <logic:equal name="bc" property="type" value="container">	  
          <% if (count < length - 1 || isSearch) { %>
	    <%-- make the crumb a link --%>
	    <a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><img src="/shell/common-rsrc/images/folder.gif"  border="0" align="absmiddle"></a>&nbsp;<a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><bean:write name="bc" filter="true" property="name"/></a>
	  <% } else { %>
	    <img src="/shell/common-rsrc/images/folder.gif" border="0" align="absmiddle">&nbsp;<bean:write name="bc" filter="true" property="name"/>
	  <% } %>	  
	  </logic:equal>

	  <%-- group image --%>	  
	  <logic:equal name="bc" property="type" value="group">	  
          <% if (count < length - 1 || isSearch) { %>
	    <%-- make the crumb a link --%>
	    <a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><img src="/shell/common-rsrc/images/user_group.gif" border="0" align="absmiddle"></a>&nbsp;<a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><bean:write name="bc" filter="true" property="name"/></a>
	  <% } else { %>
	    <img src="/shell/common-rsrc/images/user_group.gif"  border="0" align="absmiddle">&nbsp;<bean:write name="bc" filter="true" property="name"/>
	  <% } %>	  
	  </logic:equal>

	  <%-- domain image --%>	  
	  <logic:equal name="bc" property="type" value="domain">	  
          <% if (count < length - 1 || isSearch) { %>
	    <%-- make the crumb a link --%>
	    <a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><img src="/shell/common-rsrc/images/domain.gif" border="0" align="absmiddle"></a>&nbsp;<a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><bean:write name="bc" filter="true" property="name"/></a>
	  <% } else { %>
	    <img src="/shell/common-rsrc/images/domain.gif"  border="0" align="absmiddle">&nbsp;<bean:write name="bc" filter="true" property="name"/>
	  <% } %>	  
	  </logic:equal>

	  <%-- collection image --%>	  
	  <logic:equal name="bc" property="type" value="collxn">	  
          <% if (count < length - 1 || isSearch) { %>
	    <%-- make the crumb a link --%>
	    <a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><img src="/shell/common-rsrc/images/collections.gif" border="0" align="absmiddle"></a>&nbsp;<a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><bean:write name="bc" filter="true" property="name"/></a>
	  <% } else { %>
	    <img src="/shell/common-rsrc/images/collections.gif"  border="0" align="absmiddle">&nbsp;<bean:write name="bc" filter="true" property="name"/>
	  <% } %>	  
	  </logic:equal>

	  <%-- home --%>
	  <logic:equal name="bc" property="type" value="type_home">
            <% if (length == 1 && !isSearch) { %>	    
	      <bean:write name="bc" filter="true" property="name"/>
	    <% } else { %>
	      <a class="textWhite" href="<bean:write name="bc" filter="true" property="link"/>"><bean:write name="bc" filter="true" property="name"/></a>
	    <% } %>
	  </logic:equal>	  
	  <% count++; %>
	    
        </logic:iterate>
	<% if (isSearch) { %>
	  &nbsp;&gt;&nbsp;<webapps:pageText key="SearchResults" type="global" />
	<% } %> 
      </font>
    </td>
 </tr>
</table>
<%-- Set it in the request so that ldap_nav can access "count" after this include --%>
<% request.setAttribute("count", Integer.toString(count)); %>

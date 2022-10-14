<%@ page import="com.marimba.tools.ldap.LDAPPagedSearch"%>

 <%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.

     @(#)previous_next.jsp


     @author Michele Lin

     @version 1.11, 02/06/2002

--%>
<%@ include file="/includes/directives.jsp" %>
<bean:define id="ldapPageAction" name="ldapPageAction" scope="request" />
<bean:define id="instance" name="instance" scope="request" />

<jsp:useBean id="session_ldap" class="com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean" scope="session"/>
<jsp:useBean id="session_page" class="com.marimba.apps.subscriptionmanager.webapp.system.PagingBean" scope="session"/>



<%-- baseURL stores the name of this jsp page.  It is used by the included

     ldap_nav page to reload this page in the frame when a link is selected that

     changes the left hand pane- for example, a bread crumb link or

     switching between single select mode and multi-select mode.  --%>

<% String baseURL = request.getRequestURI(); %>


  <!-- Previous/Next entry navigation -->


	  <%-- < previous image link and text (inactive) --%>

	  <logic:equal name="session_page" property="startIndex" value="0">

	  <font class="textInactive"><webapps:pageText shared="true" type="target_details_area" key="previous"/></font>
	  </logic:equal>
	  
	  
	  <%-- < previous image link (active) --%>	  

	  <logic:greaterThan name="session_page" property="startIndex" value="0">
		 <!--<a href="<%= ldapPageAction %>?container=<%= java.net.URLEncoder.encode(session_ldap.getContainer()) %>&baseURL=<%= java.net.URLEncoder.encode(baseURL) %>&<sm:getPrevPageParam />">previous</a> -->
		 <a href="<%= ldapPageAction %>?container=<%= com.marimba.tools.util.URLUTF8Encoder.encode(session_ldap.getContainer()) %>&baseURL=<%= com.marimba.tools.util.URLUTF8Encoder.encode(baseURL) %>&<sm:getPrevPageParam />"><webapps:pageText shared="true" type="target_details_area" key="previous"/></a>
		 <!-- Symbio modified 05/19/2005 -->
	  </logic:greaterThan>
	    	    
	    	    
	  <%-- Drop down box --%>  

        <sm:getDropDownOptions instance="<%= Integer.parseInt((String)instance) %>" />


	  <%-- next text (inactive) --%>

	  <%

	  if ((LDAPPagedSearch.OID_VLV.equals((String) session.getAttribute("session_pagingtype")) && session_page.getStartIndex() + session_page.getCountPerPage() >= session_page.getTotal()) || (!session_page.getHasMoreResults() && session_page.getStartIndex() + session_page.getCountPerPage() >= session_page.getTotal())) { %> 

	  <font class="textInactive"><webapps:pageText shared="true" type="target_details_area" key="next"/></font>
	  <% } else { %>
	  
	  
	  <%-- next text (active) --%>
      <!-- <a href="<%= ldapPageAction %>?container=<%= java.net.URLEncoder.encode(session_ldap.getContainer()) %>&baseURL=<%= java.net.URLEncoder.encode(baseURL) %>&<sm:getNextPageParam />">next</a> -->
      <a href="<%= ldapPageAction %>?container=<%= com.marimba.tools.util.URLUTF8Encoder.encode(session_ldap.getContainer()) %>&baseURL=<%= com.marimba.tools.util.URLUTF8Encoder.encode(baseURL) %>&<sm:getNextPageParam />"><webapps:pageText shared="true" type="target_details_area" key="next"/></a>
      <!-- Symbio modified 05/19/2005 -->
	  <% } %>





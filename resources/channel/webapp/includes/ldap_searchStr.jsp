<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)ldap_searchStr.jsp

     @author Michele Lin
     @version 1.1, 12/14/2002
--%>

<%@ page import="com.marimba.apps.subscription.common.util.LDAPUtils" %>
<%@ page import="com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%@ page import="com.marimba.apps.subscription.common.intf.IUser" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "java.util.Map" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%
	ServletContext context = config.getServletContext();
	IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
    SubscriptionMain main = TenantHelper.getTenantSubMain(context, request.getSession(), user.getTenantName());
    Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();
%>

<webapps:pageText key="Search_entire" type="global" />

<logic:present name="session_ldap" property="searchText">
   <bean:write name="session_ldap" property="searchText"/>
</logic:present>

<logic:present name="session_ldap" property="objectclass">

     <logic:equal name="session_ldap" property="objectClass" value="<%= LDAPVarsMap.get("DOMAIN_CLASS") %>">
       <webapps:pageText key="Domain" type="global" />
     </logic:equal>

     <logic:equal name="session_ldap" property="objectClass" value="<%= LDAPVarsMap.get("CONTAINER_CLASS") %>">
       <webapps:pageText key="Container" type="global" />
     </logic:equal>

     <logic:equal name="session_ldap" property="objectClass" value="<%= LDAPVarsMap.get("CONTAINER_CLASS2") %>">
       <webapps:pageText key="Container" type="global" />
     </logic:equal>

     <logic:equal name="session_ldap" property="objectclass" value="<%= LDAPVarsMap.get("GROUP_CLASS") %>">
       <webapps:pageText key="Groups" type="global" />
     </logic:equal>
 </logic:present>




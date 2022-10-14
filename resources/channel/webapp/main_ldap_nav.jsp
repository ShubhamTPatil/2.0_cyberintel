<%@ page contentType="text/html;charset=UTF-8" %>
<%--
Copyright 1997-2003, Marimba Inc. All Rights Reserved.
Confidential and Proprietary Information of Marimba, Inc.
Protected by or for use under one or more of the following patents:
U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
and 6,430,608. Other Patents Pending.
@author Michele Lin
@author Devendra Vamathevan
@version $Revision$, $Date$
--%>

<%-- This var is used in the included ldap_nav page.  It determines which
     action is called when the target text link is selected.  This is because
     the ldap_nav page is included by both the Target View page and the Add/Remove
     Targets page and they need to store their selected targets in their respective
     session variables. --%>
<% //String targetAddAction = request.getContextPath() + "/targetDetailsAdd.do"; %>
<% //String targetAddMultiAction = request.getContextPath() + "/targetDetailsAddMulti.do"; %>
<%@ include file="/includes/directives.jsp" %>
<%-- These are the remaining actions that are used throughout the navigation. They
   also must be specific to the page that is being viewed.  In this case, the add_remove_ldap
--%>

<% //String ldapPageAction = request.getContextPath() + "/ldapPage.do"; %>

<% //String formAction = "/ldapSearch.do"; %>
<bean:define id="formAction"  toScope="request" type="java.lang.String" value="/ldapSearch.do"/>
<bean:define id="targetAddAction"  toScope="request" type="java.lang.String" value="<%=request.getContextPath() + "/targetDetailsAdd.do" %>" />
<bean:define id="targetAddMultiAction"  toScope="request" type="java.lang.String" value="<%=request.getContextPath() + "/targetDetailsAddMulti.do" %>" />
<bean:define id="ldapPageAction"  toScope="request" type="java.lang.String" value="<%=request.getContextPath() + "/ldapPage.do" %>" />

<jsp:include page="/includes/ldap_nav_body.jsp" />

<% //include file="/includes/ldap_nav_body.jsp" %>

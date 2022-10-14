<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)add_remove_ldap_nav.jsp

     @author Michele Lin
     @version 1.26, 08/07/2002
--%>

<%-- This var is used in the included ldap_nav page.  It determines which
     action is called when the target text link is selected.  This is because
     the ldap_nav page is included by both the Target View page and the Add/Remove
     Targets page and they need to store their selected targets in their respective
     session variables. --%>
<%@ include file="/includes/directives.jsp" %>

<%-- These are the remaining actions that are used throughout the navigation. They
   also must be specific to the page that is being viewed.  In this case, the add_remove_ldap
--%>
<bean:define id="targetAddAction"  toScope="request" value="<%=request.getContextPath() + "/addRemoveAdd.do"%>"/>
<bean:define id="ldapBrowseTopAction"  toScope="request" value="<%=request.getContextPath() + "/ldapBrowseTopAddRem.do" %>" />
<bean:define id="ldapBrowseEPAction"  toScope="request" value="<%=request.getContextPath() + "/ldapBrowseEPAddRem.do" %>" />
<bean:define id="ldapBrowseOUAction"  toScope="request" value="<%=request.getContextPath() + "/ldapBrowseOUAddRem.do" %>" />
<bean:define id="ldapBrowseGroupAction"  toScope="request" value="<%=request.getContextPath() + "/ldapBrowseGroupAddRem.do" %>" />
<bean:define id="txBrowseGroupAction" toScope="request" value="<%=request.getContextPath() + "/txBrowseGroupAddRem.do" %>" />
<bean:define id="ldapPageAction"  toScope="request" value="<%=request.getContextPath() + "/ldap/ldapPage.do" %>" />

<%-- This needs to be defined because it is in the ldap_nav.jsp.  It is needed for that
     page to compile, but is not used.  This variable is only used by the multi select
--%>
<bean:define id="targetAddMultiAction" name="targetAddAction" scope="request" toScope="request" />
<bean:define id="formAction"  toScope="request" value="/ldapSearchAddRem.do" />
<jsp:include page="/includes/ldap_nav_body.jsp" />

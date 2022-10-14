<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)ldap_title.jsp

     @author Michele Lin
     @version 1.5, 11/21/2002
--%>
<%@ include file="/includes/directives.jsp" %>
<bean:define id="ldapPageAction" name="ldapPageAction" scope="request" toScope="request" />
<bean:define id="pageBeanName" name="pageBeanName" scope="request" toScope="request" />
<bean:define id="numEntries" name="numEntries" scope="request" />

<jsp:useBean id="session_ldap" class="com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean" scope="session"/>
<jsp:useBean id="session_page" class="com.marimba.apps.subscriptionmanager.webapp.system.PagingBean" scope="session"/>

<table width="320" border="0" cellspacing="0" cellpadding="0">
    <tr>
	<%-- REMIND t3 pageText  --%>
      <td class="tableTitle">
        <logic:notPresent name="add_principals_title">
          <webapps:pageText key="TableTitle" />
        </logic:notPresent>
        <logic:present name="add_principals_title">
          <webapps:pageText key="TabTitUserGrp" />
   	      <% session.removeAttribute("add_principals_title"); %>
        </logic:present>
	  </td>

	  <td class="pagination" align="right" nowrap>
		<%-- Previous/Next entry navigation --%>
		<% int num_entries = Integer.parseInt((String)numEntries); %>
        <bean:define id="instance"  toScope="request" type="java.lang.String" value="1" />
		<% if ( num_entries > 0 && !session_ldap.getUseLDAPPaging()) { %>
  		<bean:define id="targetFrame" toScope="request" value="ldapnav" />
      				<jsp:include page="/includes/genPrevNext.jsp" />
		<% } else if ( num_entries > 0) { %>
  			<jsp:include page="/includes/previous_next.jsp" />
		<% } %>
	  </td>
    </tr>
</table>

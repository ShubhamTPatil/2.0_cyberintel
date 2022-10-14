<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)targetPaging.jsp

     @author Rahul Ravulur
     @version 1.0, 01/31/2002
--%>

<%@ page import="java.util.ArrayList" %>

<%   int resultsLen = ((ArrayList) session.getAttribute("page_targets_frompkgs_rs")).size();
     int instance =1;
     if (resultsLen > 50) {
	   instance = 2;
     }
%>



  <!-- Previous/Next entry navigation -->
	
	  <%-- < previous image link and text (inactive) --%>
	  <logic:equal name="session_page" property="startIndex" value="0">
	  <td align="right" class="inactiveText"><img src="/shell/common-rsrc/images/previous_inactive.gif" width="12" height="12" border="0">&nbsp;previous 50
	  </logic:equal>
	  
	  <%-- < previous image link (active) --%>	  
	  <logic:greaterThan name="session_page" property="startIndex" value="0">
          <td align="right"><a href="/spm/targetPage.do?<sm:getPrevPageParam />"><img src="/shell/common-rsrc/images/previous_active.gif" width="12" height="12" border="0"></a><font class="smallText">

	  <%-- previous text (active) --%>
	  <a href="/spm/targetPage.do?<sm:getPrevPageParam />">&nbsp;previous 
            50</a></font>
	  </logic:greaterThan>
	    	    
	  <%-- Drop down box --%>  
          <font class="smallText"><sm:getDropDownOptions instance="<%= instance %>" /></font>
    <jsp:useBean id="session_page" class="com.marimba.apps.subscriptionmanager.webapp.system.PagingBean" scope="session" />
	  <%-- next text (inactive) --%>
	  <% if (session_page.getStartIndex() + session_page.getCountPerPage() >= session_page.getTotal()) { %> 
	  <class="inactiveText">&nbsp;next 50<img src="/shell/common-rsrc/images/next_inactive.gif" width="12" height="12" border="0"></td>
	  <% } else { %>
	  
	  <%-- next text (active) --%>
          <font class="smallText"> <a href="/spm/targetPage.do?<sm:getNextPageParam />">next 50</a></font> 

	  <%-- > next image link (active) --%>
	  <a href="/spm/targetPage.do?<sm:getNextPageParam />"><img src="/shell/common-rsrc/images/next_active.gif" width="12" height="12" border="0"></a></td>
	  <% } %>


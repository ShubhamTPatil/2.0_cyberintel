<%-- Copyright 2001, Marimba Inc. All Rights Reserved.

     Confidential and Proprietary Information of Marimba, Inc.

     @(#)genPrevNext.jsp


     @author Michele Lin

     @version 1.9, 05/06/2002

--%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>


<% String context = request.getContextPath(); %>
<bean:define id="pageBeanName" name="pageBeanName" scope="request" />


<bean:define name="<%= (String)pageBeanName %>" id="pageBean" type="com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" scope="session" />

<% String requestURI = ""; %>

<% String formName = ""; %>

<% String jsNoFrameSubmit = "send"; %>

<% String loadActionFromSelectionPkg =""; %>

<logic:present name="genericPagingAction" scope="request">

 <% requestURI = ((String) request.getAttribute("genericPagingAction")) + "?" + request.getRequestURI();

 %>

</logic:present>

<logic:notPresent name="genericPagingAction" scope="request">

<% requestURI = request.getRequestURI(); %>

</logic:notPresent>

<% String forwardURL = requestURI;%> 


<logic:present name="formName" scope="request">

<% formName = (String) request.getAttribute("formName"); %>

</logic:present>


<logic:present name="page_jsNoFrameSubmit">

<% jsNoFrameSubmit = (String) request.getAttribute("page_jsNoFrameSubmit"); 

 loadActionFromSelectionPkg =(String) request.getAttribute("selectForm");

%>


</logic:present>


<%@ page import = "com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>

<% int PAGE_SIZE = IWebAppConstants.DEFAULT_GENPAGING_COUNT_PER_PAGE; %>


  <!-- Generic Previous/Next navigation -->

  <%-- REMIND --%>

	  <%-- < previous image link and text (inactive) --%>

	  <logic:equal name="<%= (String)pageBeanName %>" property="startIndex" value="0">

    	  <span class="textInactive"><webapps:pageText shared="true" type="target_details_area" key="previous"/></span>
	  </logic:equal>

	  <%-- < previous image link (active) --%>	  

	  <logic:greaterThan name="<%= (String)pageBeanName %>" property="startIndex" value="0">

	  <logic:present name="submitPaging">

	    <logic:present name="targetFrame">	  

	      <a href="javascript:submitActionFromFrames(<%= formName %>, '<%= requestURI %>?page=prev',  '<webapps:stringescape><bean:write name="targetFrame" filter="false" /></webapps:stringescape>');">

	    </logic:present>

	    <logic:notPresent name="targetFrame">

	        <a href="javascript:<%=jsNoFrameSubmit%>(<%= formName %>, '<%= requestURI %>?page=prev');">

	    </logic:notPresent>	    

	  </logic:present>

	  <logic:notPresent name="submitPaging">

          <a href="<%= requestURI %>?page=prev">	 

	  </logic:notPresent>

	  </a>&nbsp;

	  <logic:present name="submitPaging">

	      <logic:present name="targetFrame">	  	    

	        <a href="javascript:submitActionFromFrames(<%= formName %>, '<%= requestURI %>?page=prev',  '<webapps:stringescape><bean:write name="targetFrame" filter="false" /></webapps:stringescape>');">

	      </logic:present>

	      <logic:notPresent name="targetFrame">	  

	        <a href="javascript:<%=jsNoFrameSubmit%>(<%= formName %>, '<%= requestURI %>?page=prev');">

	      </logic:notPresent>	  	       	    	      

	  </logic:present>

	  <logic:notPresent name="submitPaging">

          <a href="<%= requestURI %>?page=prev">	 

	  </logic:notPresent>	    	  	  

	  <webapps:pageText shared="true" type="target_details_area" key="previous"/></a>
	  </logic:greaterThan>

	  <%-- Drop down box --%>

<%	  if (forwardURL.startsWith(context)) {

	       forwardURL = forwardURL.substring(context.length()); } %>


		  <logic:greaterThan name="<%= (String)pageBeanName %>" property="total" value="0">


	  <%-- the paging is not implemented within a frame --%>

	  <logic:notPresent name="targetFrame">

	    <%-- No submit needed (e.g. Tx listing/currently deployed pages) --%>	  	    

		<logic:notPresent name="submitPaging">

	      <select name="genPageSelect" onChange="javascript:loadActionFromSelection(this.form.genPageSelect);">

	    </logic:notPresent>

	    <%-- Submit needed for check boxes(e.g. target details, package details) --%>

	    <logic:present name="submitPaging">

			<logic:present name="selectForm">

			<select name="genPageSelect" onChange="javascript:<%=loadActionFromSelectionPkg%>(this.form.genPageSelect, <%= formName %>);">	

			</logic:present>	    

		    <logic:notPresent name="selectForm">

			<select name="genPageSelect" onChange="javascript:loadActionFromSelection(this.form.genPageSelect, <%= formName %>);">

		    </logic:notPresent>

	    </logic:present>	    

	  </logic:notPresent>


	  <%-- the paging IS implemented within a frame --%>

	  <logic:present name="targetFrame">

	    <%-- There is a frame.  No Submit needed

	    (e.g. package view left hand pane) --%>

	    <logic:notPresent name="submitPaging">

	      <select class="smallButtons" name="genPageSelect" onChange="javascript:loadActionFromSelectionFrames(this.form.genPageSelect, '', '<bean:write name="targetFrame" />');">

	    </logic:notPresent>

	    <%-- There is a frame.  Submit needed

	    (e.g. package view right hand pane) --%>	    

	    <logic:present name="submitPaging">

	      <select name="genPageSelect" onChange="javascript:loadActionFromSelectionFrames(this.form.genPageSelect, <%= formName %>,  '<webapps:stringescape><bean:write name="targetFrame" filter="false" /></webapps:stringescape>');">

	    </logic:present>

	  </logic:present>
	  
	  
	      <% for (int i = 0; i*PAGE_SIZE < pageBean.getTotal(); i++) { %>

	      <option value="<%= forwardURL %>?page=<%= i %>" <% if ((i*PAGE_SIZE) == pageBean.getStartIndex()) { %>selected<% } %>><%= (i*PAGE_SIZE) + 1 %>-<%= (i*PAGE_SIZE)+PAGE_SIZE > pageBean.getTotal() ? pageBean.getTotal() : (i*PAGE_SIZE)+PAGE_SIZE %> <webapps:pageText shared="true" type="target_details_area" key="of"/> <%= pageBean.getTotal() %>

	      <% } %>

	    </select>

	  </logic:present>

	  </logic:greaterThan>
	  



	  <%-- next text (inactive) --%>

	  <% if (pageBean.getStartIndex() + pageBean.getCountPerPage() >= pageBean.getTotal()) { %> 

	  <span class="textInactive"><webapps:pageText shared="true" type="target_details_area" key="next"/></span>
	  <% } else { %>
	  
	  
	  <%-- next text (active) --%>

	  <logic:present name="submitPaging">

	      <logic:present name="targetFrame">	  	    

	        <a href="javascript:submitActionFromFrames(<%= formName %>, '<%= requestURI %>?page=next',  '<webapps:stringescape><bean:write name="targetFrame" filter="false" /></webapps:stringescape>');">

	      </logic:present>

	      <logic:notPresent name="targetFrame">	  

	        <a href="javascript:<%=jsNoFrameSubmit%>(<%= formName %>, '<%= requestURI %>?page=next');">

	      </logic:notPresent>

	  </logic:present>  

	  <logic:notPresent name="submitPaging">

            <a href="<%= requestURI %>?page=next">

	  </logic:notPresent>  

	  <webapps:pageText shared="true" type="target_details_area" key="next"/></a>


	  <%-- > next image link (active) --%>

	  <logic:present name="submitPaging">		  

	      <logic:present name="targetFrame">	  	    

	        <a href="javascript:submitActionFromFrames(<%= formName %>, '<%= requestURI %>?page=next',  '<bean:write name="targetFrame" />');">

	      </logic:present>

	      <logic:notPresent name="targetFrame">	  

	        <a href="javascript:<%=jsNoFrameSubmit%>(<%= formName %>, '<%= requestURI %>?page=next');">

	      </logic:notPresent>		    

	  </logic:present>

	  <logic:notPresent name="submitPaging">

            <a href="<%= requestURI %>?page=next">

	  </logic:notPresent>  	  

	  </a>

	  <% } %>



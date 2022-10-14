<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.

     Confidential and Proprietary Information of Marimba, Inc.

     @(#)internalerror.jsp



     This page server several purposes.  Essentially, it handles all internal and JSPExceptions.

     Therefore, whenever the business logic throw a com.marimba.webapps.intf.InternalException,

     the request is forwarded here.  Additionally, there is a subclass of internal called

     CriticalException.  This exception is generated when the problem that occurs cannot be

     fixed within the web application framework.  For example, the ldap server schema is wrong.

     It is a problem that can be addressed by the user.  Finally, all JSPExceptions are redirected

     to this page.  This is so that we can handle exceptions that occur within tags.

     

     @author Angela Saval

     @version 1.18, 08/20/2002

--%>



<%@ page import = "com.marimba.webapps.intf.WebAppJspException" %>

<%@ page import = "javax.servlet.jsp.PageContext" %>

<%@ page import = "javax.servlet.jsp.JspException" %>

<%@ page import = "javax.servlet.http.HttpServletRequest" %>



<%@ page isErrorPage="true" %>



<%-- Determine how this error was generated.  If it is a WebAppJspException, it was created

     by WebAppUtils.saveTagException and the error is saved in the INTERNALERROR.  otherwise,

     the JspException was generated by the servlet container.  We clear out INTERNALERROR if

     this is the case --%>



<%

   if (exception!=null) {

      if (!(exception instanceof WebAppJspException)) {

	 pageContext.getSession().removeAttribute("INTERNALERROR");

	 pageContext.getSession().removeAttribute("INTERNALERROR_EXC");

	 pageContext.getSession().removeAttribute("INTERNALERROR_RETURNPAGE");

	 pageContext.getSession().setAttribute("jspException",exception);

      } else {

         //it is an instance of WebAppJspException, therefore it's error message is the return page

         pageContext.getSession().removeAttribute("jspException");
         pageContext.getSession().removeAttribute("INTERNALERROR_RETURNPAGE");
         pageContext.setAttribute("errorReturnPage",exception.getMessage());

      }

   }



%>     



<%@ include file="/includes/directives.jsp" %>



<%--Check the exception to see where it came from.  If it was from a tag, do not

    include the head section.  The request uri the caused the error can be found

    in the exception message.  This message should be passed into the tag

    isInFrameTag(REMIND: create a tag for this)  --%>

<%

	String uri = null;

	String requri = ((HttpServletRequest)pageContext.getRequest()).getRequestURI();

	String inFrame = "false";

	if (exception !=null) {

	  uri = exception.getMessage();

        } else {

	  uri = (String)pageContext.getSession().getAttribute("INTERNALERROR_RETURNPAGE");

	}



	  if (exception!=null || pageContext.getSession().getAttribute("INTERNALERROR") !=null) {

	       if (uri !=null) {

	          //target details area for single and multiple select are in a frame,  this

		  // handles both situations

	  	  if (uri.indexOf("target_details_area") > 0) {

		     inFrame = "true";

		  //package details area for single and multiple select are in a frame, this

		  //handles both situations  

		  } else if (uri.indexOf("package_details_area") > 0) {

		     inFrame = "true";

		  } else if (uri.indexOf("package_m_details_area") > 0) {

		     inFrame = "true";

		  }  else if (uri.indexOf("main_ldap_nav.") > 0) {

		     inFrame = "true";

		  } else if (uri.indexOf("ldapBrowse") > 0) {

		     inFrame = "true";

		  } else if (uri.indexOf("txBrowseGroup") >0) {

		     inFrame= "true";

		  } else if (uri.indexOf("targetDetailsAdd") > 0) {

		     inFrame = "true";		   

		  }  else if (uri.indexOf("ldapSearch") > 0) {

		     inFrame = "true";

		  }  else if (uri.indexOf("ldapRemember") > 0) {

		     inFrame = "true";		  

     		  }  else if (uri.indexOf("ldapPage.") > 0) {

		     inFrame = "true";

		  } else if (uri.indexOf("select_exclude.") > 0) {

		     inFrame = "false";

		  }  else if (uri.indexOf("add_remove_ldap_nav.") > 0) {

		     inFrame = "false";

		  }  else if (uri.indexOf("package_navigation_area.") > 0) {

		     inFrame = "false";

		  }		  		  

	  }	  

       } else {

          requri = ((HttpServletRequest)pageContext.getRequest()).getRequestURI();

	    //on the internal error page you can expand and contract, therefore

	    //maintain the inframe value if simply doing this

	    if (requri.indexOf("internalerror.") > 0) {

	        inFrame = (String)pageContext.getSession().getAttribute("inFrame");

		if (inFrame==null || "".equals(inFrame)) {

		   inFrame = "false";

		}

	    } else {

	    

	    }

       }	         

       pageContext.getSession().setAttribute("inFrame",inFrame);



%>

<%@ include file="/includes/headSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>


<%-- Depending on whether or not a jspexception exist, we show a blue banner

bar.  This is because when a JSPException occurs that is not thrown from

the WebAPPUtils.saveTagException, we want to not include the tabs.

--%>



<logic:present name="jspException">

<%@ include file="/includes/body.html" %>

<table width="100%" border="0" cellspacing="0" cellpadding="0">

    <tr> 

     <td bgcolor="336699">&nbsp;</td>

    </tr>

</table>



</logic:present>



<%--

since a general JspException was not produced by the servlet container,

we determine whether to show or hide the banner according to whether it

it is a frame.

--%>



<logic:notPresent name="jspException">

 <logic:equal name="inFrame" value="false">

 <%@ include file="/includes/body.html" %>

<logic:notPresent name="taskid">
     <logic:notPresent name="emergencyUser">
<% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>
        </logic:notPresent>
        <logic:present name="emergencyUser">
            <webapps:tabs tabset="bogustabname" tab="noneselected"/>
        </logic:present>
</logic:notPresent>
<logic:present name="taskid">
    <% request.setAttribute("nomenu", "true"); %>
    <webapps:tabs tabset="bogustabname" tab="noneselected"/>
</logic:present>

 </logic:equal>



 <logic:equal name="inFrame" value="true">

 <%@ include file="/includes/body.html" %>

 </logic:equal>



</logic:notPresent>

 

<webapps:congregateErrors /> 



<html:form name="internalErrorForm" action="/internalErrorState.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.InternalErrorForm" >   

<div align="left" style="margin-left:20px; margin-right:20px;">
<div class="statusMessage" id="critical">
   <h6>
   <logic:present name="CRITICALERROR">

                <logic:equal name="CRITICALERROR" value="true">

				<webapps:pageText shared="true" type="internalerror" key="CriticalErrorMsg" />

				</logic:equal>

				<logic:notEqual name="CRITICALERROR" value="true">

				<webapps:pageText shared="true" type="internalerror" key="InternalErrorMsg"/>

				</logic:notEqual>

	</logic:present>
    <logic:notPresent name="CRITICALERROR">

	<webapps:pageText shared="true" type="internalerror" key="InternalErrorMsg"/>

   </logic:notPresent>   
   </h6>

   <p>
   <logic:present name="INTERNALERROR">

				<logic:iterate id="error" name="internalerrors" >

				<webapps:writeError name="error" bundle="com.marimba.webapps.intf.SYSTEMERRORS" />

                </logic:iterate>

	</logic:present>

								<%--This will display the error message produced by an JspException that occurred

										and was NOT produced by one of the tags.  This would occur is there were some problem with

										compilation on another page --%>

			    <br>

	<logic:present name="jspException">

	<bean:write name="jspException" property="message"/>

   </logic:present>



			<%-- The request uri is always the message of the Web App Exception, so we can use this for the return page --%>

			<logic:present name="errorReturnPage">
             <br>&nbsp;<br>

               <%String errURI = (String)pageContext.getAttribute("errorReturnPage");

				%>

				<a href="<%=errURI%>"> <webapps:pageText shared="true" type="internalerror" key="ReturnPageMessage"/></a>

				</logic:present>

		          <logic:notPresent name="taskid">

				<logic:present name="INTERNALERROR_RETURNPAGE">
                  <br>&nbsp;<br>

                <%String errURI = (String)pageContext.getSession().getAttribute("INTERNALERROR_RETURNPAGE");

				%>

				<a href="<%=errURI%>"> <webapps:pageText shared="true" type="internalerror" key="ReturnPageMessage"/></a>

				</logic:present>

             </logic:notPresent>
   </p>
</div>
  <div align="left" style="width:100%; margin-top:15px;">
		

   <table border="0" cellspacing="0" cellpadding="10" class="textGeneral">
    <tr>

    <td valign="middle">&nbsp;</td>

    <td valign="middle">

        <logic:notPresent name="internalErrorForm" property="value(showDetails)">

         <a href="<webapps:fullPath path="/internalErrorState.do?showDetails=true"/>" class="textGeneral">

         <img src="/shell/common-rsrc/images/arrow_contracted.gif" width="7" height="13" border="0"></a>

									<webapps:pageText shared="true" type="internalerror" key="ShowDetails" /></a><br>
							
								</logic:notPresent>

								<logic:present name="internalErrorForm" property="value(showDetails)">
							
								<logic:equal name="internalErrorForm" property="value(showDetails)" value="false">
							
																<a href="<webapps:fullPath path="/internalErrorState.do?showDetails=true"/>" class="generalText">
							
																<img src="/shell/common-rsrc/images/arrow_contracted.gif"  width="7" height="13" border="0"></a>
							
									<webapps:pageText shared="true" type="internalerror" key="ShowDetails" /></a><br>
							
									</logic:equal>
							
								<logic:equal name="internalErrorForm" property="value(showDetails)" value="true">
							
																<a href="<webapps:fullPath path="/internalErrorState.do?showDetails=false"/>" class="generalText">
							
																<img src="/shell/common-rsrc/images/arrow_expanded.gif" width="13" height="7" border="0"></a>
							
									<webapps:pageText shared="true" type="internalerror" key="HideDetails" /></a><br>
							
									</logic:equal>
							
									</logic:present>
	 
    </td>
   </tr>

  <%-- Only if show details has been set should this column show --%>

  <logic:present name="internalErrorForm" property="value(showDetails)">

  <logic:equal name="internalErrorForm" property="value(showDetails)" value="true">

  <tr>

    <td valign="middle">&nbsp;</td>

    <td valign="middle">

      <logic:present name="com.marimba.webapps.intf.INTERNALERROR_EXC">     

      <pre>

      <webapps:writeStackTrace name="com.marimba.webapps.intf.INTERNALERROR_EXC"/>

      </pre>

      </logic:present>

      <%--If we got a straight JspException that was not saved into the internal errors

         key, we will also show this exception stack trace

	 --%>

      <logic:present name="jspException">

	 <br>

	 Page exception generated:

	 <br>

	 <pre>

	 <webapps:writeStackTrace name="jspException"/>

	 </pre>

      </logic:present>

    </td>   

  </tr>

  </logic:equal>

</logic:present> <%-- end of the present check for show details --%>   


</table>

</div>
</div>

</html:form>



</logic:present> <%-- end of the present check for  internal errors--%>



<%@ include file="/includes/footer.jsp" %>


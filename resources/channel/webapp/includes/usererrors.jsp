<%-- Confidential and Proprietary Information of Marimba, Inc.
     @(#)usererrors.jsp

     @author Angela Saval
     @version 1.7, 01/09/2002
--%>

<%@ page import = "org.apache.struts.action.ActionErrors" %>
<logic:present name="org.apache.struts.action.ERROR" >


    <webapps:congregateErrors /> <%-- This tag will go through the request scope and obtain
				      the various iterators for errors that have occurred
				      for each component (form validation, known system errors )
				  --%>			      

<tr>
 <td>
    <div class="statusMessage" id="critical">
       <h6><webapps:text key="page.usererrors.beforeproceeding"/></h6>

       <p><ul><logic:iterate id="error" name="inputerrors"><li><webapps:writeError name="error" bundle="com.marimba.webapps.intf.INPUTERRORS" /></li></logic:iterate></ul></p>
    </div>
  </td>
</tr>
</logic:present>

<logic:present name="KNOWNERROR" >

    <webapps:congregateErrors /> <%-- This tag will go through the request scope and obtain
				      the various iterators for errors that have occurred
				      for each component (form validation, known system errors )
				  --%>			      

<tr>
 <td>
    <div class="statusMessage" id="critical">
       <h6><webapps:text key="page.usererrors.beforeproceeding"/></h6>

       <p><ul><logic:iterate id="error" name="knownerrors"><li><webapps:writeError name="error" bundle="com.marimba.webapps.intf.SYSTEMERRORS" /></li></logic:iterate></ul></p>
    </div>
  </td>
</tr>
</logic:present>

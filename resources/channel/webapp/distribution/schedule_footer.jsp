<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)schedule_footer.jsp

     @author Theen-Theen Tan
     @version 1.2, 02/13/2002
--%>

  <tr> 
    <td>&nbsp;</td>
  </tr>
  <tr> 
    <td> 
      <hr size="1" noshade width="100%">
    </td>
  </tr>
  <tr> 
    <td align="right"> 
      <input type="submit" id="OK" value=" <webapps:pageText key="OK" type="global" /> ">
      &nbsp; 
      <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.scheduleEditForm, '/distAsgSchedSave.do?action=cancel');" >
    </td>
  </tr>
</html:form>

</table>

  <%-- Called to initialize the widgets the from the data set by the server --%>
  <script>
    <%-- This JSP file is not included any where. However, to avoid compilation errors, declaring hasUpdate variable --%>
    <% boolean hasUpdate = true;
     if (hasUpdate) { %>
         initCalDetailsVisible();
    <% } %>
    initDisabled();
    initActiveDates();
  </script>


<%@ include file="/includes/footer.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)copy_preview_refresh_top.jsp

     @author Jayaprakash Paramasivam
     @version 1.0, 01/20/2005
--%>
<%-- Javascript --%>

<%@ include file="/includes/common_js.jsp" %>

<script language="JavaScript">

top.location.href = "<%=request.getContextPath()%>"+"/copy/copy_assignment_preview.jsp";

</script>

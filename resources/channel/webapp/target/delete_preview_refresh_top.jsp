<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)delete_preview_refresh_top.jsp

     @author Devendra Vamathevan
     @author Alex Homes
     @version 1.40, 05/30/2003
--%>
<%-- Javascript --%>

<%@ include file="/includes/common_js.jsp" %>

<script language="JavaScript">

top.location.href = "<%=request.getContextPath()%>"+"/target/delete_preview.jsp";

</script>

<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page language="java" %>

<%--
This is an intermediary login page that is specific to the SM.  We use it to
solve two problems :
-Always redirect back to the main page. In the case that user was in the middle
 of an action, their session variable would have been destroyed when the
 session timed out.  If the user is sent to the page they originally timed out
 the state of the page will be inconsistent.
-Break out of frames in the login page.
--%>

<%
    String returnPage = (String) session.getAttribute("com.marimba.servlet.login.returnPage");
    if (returnPage != null){
    	session.setAttribute(IWebAppConstants.ATTR_LOGIN_SAVED_PAGE, returnPage);
    }
    String v = request.getRequestURI();
    int index = v.indexOf("/login/login.jsp");
    v = v.substring(0, index);
    // Always sends user back to the main_view.jsp page if non ccmpolicy page
    session.setAttribute("com.marimba.servlet.login.returnPage", v + "/initApp.do");
    
%>


<html>
<head>
<title>Session time out</title>
<script language="JavaScript">

<%--
If the login page is requested from a frame, break out of the frame
and display the login page at the top window.
Redirect to CMS's common login page.
--%>
function init() {
    if (window != top) {
      top.location.href = "<%= v %>/common-rsrc/login/login.jsp";
    } else {
      window.document.location.href = "<%= v %>/common-rsrc/login/login.jsp";
    }
}

</script>
<link rel="stylesheet" href="<%= v %>/common-rsrc/css/main.css" type="text/css">
</head>

<body onLoad="init()">
</body>
</html>

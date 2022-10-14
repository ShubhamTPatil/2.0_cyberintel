<%@ page language="java" %>

<%@ page import="java.util.*,
                 org.apache.struts.util.MessageResources" %>
<%@ page import="com.marimba.intf.msf.*" %>
<%@ page import="com.marimba.intf.util.*" %>
<%@ page import="com.marimba.webapps.intf.*" %>
<%@ page import="com.marimba.tools.ldap.*" %>
<%@ page import="com.marimba.apps.subscription.common.intf.*" %>
<%@ page import="com.marimba.apps.subscriptionmanager.*" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.*" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%
   String ldapHost = "N/A";
   HttpSession sess = request.getSession();
   ServletContext context = config.getServletContext();
   
   IUserPrincipal userPrin = (IUserPrincipal) request.getUserPrincipal();
   SubscriptionMain main = TenantHelper.getTenantSubMain(context, sess, userPrin.getTenantName());
   if (LDAPConnUtils.getInstance(main.getTenantName()).isADWithAutoDiscovery(main.getLDAPConfig())) {
        ldapHost = main.getLDAPConfig().getProperty("domainAsDN");
   } else {
      ldapHost = main.getLDAPConfig().getProperty("host");
   }
   IUser user = (IUser) sess.getAttribute(IWebAppConstants.SESSION_SMUSER);
   // user may be null if there's an error during SubscriptionInitServlet
   // createUser.  In which case, we want to allow the internalerror page
   // to be loaded properly.
	MessageResources resources = main.getAppResources();
    Locale locale = request.getLocale();
    String ldap = resources.getMessage(locale,"page.info.ldap");
    String container = resources.getMessage(locale,"page.info.container");
    String aclenabled = resources.getMessage(locale,"page.info.aclenabled");
    String updatesenabled = resources.getMessage(locale,"page.info.updatesenabled");
    String yes = resources.getMessage(locale,"page.global.yes");
    String no = resources.getMessage(locale,"page.global.no");
    String trueStr = resources.getMessage(locale,"page.global.true");
    String falseStr = resources.getMessage(locale,"page.global.false");
	if(main.isCloudModel()) {
		ldapHost = "*******";
	}
   if (user != null) {
     String subContainer = user.getNameSpace();
     if (subContainer != null && subContainer.length() > 0) {
       String [] userInfo = {ldap, ldapHost, container, user.getNameSpace()};
       sess.setAttribute("webapp.info", userInfo);
     } else {
       String [] userInfo = {ldap, ldapHost};
       sess.setAttribute("webapp.info", userInfo);
     }
   } else {
       String [] userInfo = {aclenabled, main.isAclsOn() ? trueStr:falseStr, updatesenabled, main.isPushEnabled()?trueStr:falseStr};
       sess.setAttribute("webapp.info", userInfo);
   }
%>

<%--<webapps:include page="/common-rsrc/header/info.jsp" />--%>





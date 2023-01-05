<%@ page contentType="text/html;charset=UTF-8" %>
<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Theen-Theen Tan
	@version  $Revision$,  $Date$
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.intf.util.*,
                 com.marimba.tools.ldap.LDAPConnUtils" %>
<%@ page import="com.marimba.webapps.intf.IWebAppsConstants" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import="com.marimba.apps.subscriptionmanager.SubscriptionMain" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%@ page import = "com.marimba.intf.msf.IUserPrincipal" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.TenantHelper" %>
<%
    Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%@ include file="/includes/startHeadSection.jsp" %>
<title><webapps:pageText key="m6" type="global"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!-- <link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css"> -->

<%
    String textPfx = "Target";
    String singleViewPage = "/tgviewInit.do";
    String multiViewPage = "/tgviewMultiInit.do";
    String singleDetailPage = "/tgdetailsInit.do";
    String multiDetailPage = "/tgdetailsMultiInit.do";
    String multibool ="session_multitgbool";

    if (IWebAppConstants.PKG_VIEW.equals((String) session.getAttribute(IWebAppConstants.SESSION_RETURN_PAGETYPE))) {
        textPfx = "Package";
        singleViewPage = "/pkgviewInit.do?actionType=initAction";
        multiViewPage = "/pkgviewMultiInit.do";
        singleDetailPage = "/pkgdetailsInit.do";
        multiDetailPage = "/pkgdetailsMultiInit.do";
        multibool = "session_multipkgbool";
    }

    ServletContext context = config.getServletContext();
    IUserPrincipal user = (IUserPrincipal) request.getUserPrincipal();
    SubscriptionMain main = TenantHelper.getTenantSubMain(context, request.getSession(), user.getTenantName());
    boolean isApprovalPolicy = main.isPeerApprovalEnabled();
    String approvalType = main.getPeerApprovalType();
    boolean isServiceNowApproval = (null == approvalType) ? false : "servicenow".equalsIgnoreCase(approvalType);
	String serviceNowLatestChangeRequestId = "";
	if(isServiceNowApproval) {
		if(null == main.getDBStorage()) {
			serviceNowLatestChangeRequestId = "Change request id is not able to retrieve";
		} else {
			serviceNowLatestChangeRequestId = main.getDBStorage().getServiceNowLatestChangeRequestId();
		}
	}
    boolean isRemedyForceApproval = (null == approvalType) ? false : "remedyforce".equalsIgnoreCase(approvalType);
	String remedyForceLatestChangeRequestId = "";
	if(isRemedyForceApproval) {
		if(null == main.getDBStorage()) {
			remedyForceLatestChangeRequestId = "Change request id is not able to retrieve";
		} else {
			remedyForceLatestChangeRequestId = main.getDBStorage().getRemedyForceLatestChangeRequestId();
		}
	}
	boolean hasPolicyChange = main.hasPolicyChange();
%>

<script type="text/javascript">

$(function () {
	$('#assignments').addClass('nav-selected');
});

</script>

<%@ include file="/includes/endHeadSection.jsp" %>

<main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename">Assignments<span style="font-size: small;">&nbsp;>&nbsp;<webapps:pageText key="Title"/></span></span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="Policy Confirmation"><i
              class="fa-solid fa-circle-info text-primary"></i></span>
        </div>
        <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i></div>
        <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Download" style="cursor: pointer;">
          <i class="fa-solid fa-download"></i>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center"> <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left"
              style="margin-right: 5px;"></i>CMS Home</a>
        </div>
      </div>

    </div>

    <section class="section dashboard">
	    <div class="card">
	    	<div class="card-body">
	    		<div class="card-title">
	    			<strong><webapps:pageText key="Confirm"/></strong>
	    		</div>
	    		
	    		<p>
	                <%if(isApprovalPolicy) {%>
	                    <%if(isServiceNowApproval) {%>
	                        <webapps:pageText key="approvalenabled.servicenow1"/><b><%=serviceNowLatestChangeRequestId%></b><webapps:pageText key="approvalenabled.servicenow2"/><webapps:pageText key='<%= textPfx + "Desc" %>' /><br>&nbsp;<br>
	                    <% } else if(isRemedyForceApproval) {%>
	                        <webapps:pageText key="approvalenabled.remedyforce1"/><b><%=remedyForceLatestChangeRequestId%></b><webapps:pageText key="approvalenabled.remedyforce2"/><webapps:pageText key='<%= textPfx + "Desc" %>' /><br>&nbsp;<br>
	                    <% } else {
							if(hasPolicyChange) { %>
								<webapps:pageText key="approvalenabled"/><webapps:pageText key='<%= textPfx + "Desc" %>' /><br>&nbsp;<br>
							<% } else { %>
								<webapps:pageText key="approvalenabled.nochange"/><webapps:pageText key='<%= textPfx + "Desc" %>' /><br>&nbsp;<br>
							<% } 
						}%>
	                <% } else { %>
	                <webapps:pageText key='<%= textPfx + "Desc" %>' /><br>&nbsp;<br>
	                <%}
	                    if (LDAPConnUtils.getInstance(main.getTenantName()).isADWithAutoDiscovery(main.getLDAPConfig())) {
	                %>
	                <webapps:pageText key="ADreplicate" /><br>&nbsp;<br>
	                <% } %>
	                <logic:notPresent name="<%=multibool%>">
	                    <html:link page="<%=singleViewPage%>"><webapps:pageText key='<%= textPfx + "View" %>'/></html:link> &nbsp;
	                </logic:notPresent>
	                <logic:present name="<%=multibool%>">
	                    <html:link page="<%=multiViewPage%>"><webapps:pageText key='<%= textPfx + "View" %>'/></html:link> &nbsp;
	                </logic:present>
	            </p>
	    		
	    	</div>
	    </div>
    </section>
</main>
<%@ include file="/includes/footer.jsp" %>

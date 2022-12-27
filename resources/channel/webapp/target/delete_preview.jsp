<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2002, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)delete_preview.jsp, 1.10, 05/30/2003
     
     @author Theen-Theen Tan
     @version 	1.10, 05/30/2003
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
    <webapps:helpContext context="sm" topic="del_prev" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import = "java.util.*" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Target" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
List<Target> sameUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_SAMEUSER);
List<Target> diffUserPendingPolicy = (List<Target>)session.getAttribute(IWebAppConstants.PENDING_POLICY_DIFFUSER);

%>

<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<main id="main" class="main">
        <div class="pagetitle">

          <div class="d-flex bd-highlight justify-content-center">
            <div class="p-2 flex-grow-1 bd-highlight">
              <span class="pagename">Assignment <span style="font-size: small">>Delete Preview</span> </span>
              <span data-bs-toggle="tooltip" data-bs-placement="right" title="Title"><i
                  class="fa-solid fa-circle-info text-primary"></i></span>
            </div>
            <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
              title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i></div>
            <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
              title="Download" style="cursor: pointer;">
              <i class="fa-solid fa-download"></i>
            </div>
            <div class="p-2 bd-highlight text-primary align-self-center">
            <a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home</a>
            </div>
          </div>

        </div>
    	<section class="section dashboard">


		    <html:form styleId="targetDetailsForm" action="/distDeleteSave.do" target="_top">


            <div align="center">
            
            <div class="card">
            <div class="card-body">
            
            <div style="text-align:left; width:800px;">
              <div class="pageHeader"><span class="title"><webapps:pageText key="Title" /></span></div>
                  <logic:present name="session_multitgbool">
                <% if((null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) && (null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0)) {%>
                <div class="statusMessage" id="warning">
            	           <h6>&nbsp;</h6>
            	           <p>
            	           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            	           <webapps:pageText key="approval_both_Warning" />
            	           </p>
            	        </div>
                <%} else { %>
            	    <% if(null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) {%>
            	        <div class="statusMessage" id="warning">
            	           <h6>&nbsp;</h6>
            	           <p>
            	           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            	           <webapps:pageText key="approval_suser_Warning" />
            	           </p>
            	        </div>
            	        <%} else { %>
            	        <% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
            	        <div class="statusMessage" id="warning">
            	           <h6>&nbsp;</h6>
            	           <p>
            	           <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
            	           <webapps:pageText key="approval_duser_Warning" />
            	           </p>
            	        </div>
            		<%}} %>
                <%} %>
                </logic:present>
                <logic:notPresent name="session_multitgbool">
                <% if(null != sameUserPendingPolicy && sameUserPendingPolicy.size() > 0) {%>
                    <div class="statusMessage" id="warning">
                       <h6>&nbsp;</h6>
                       <p>
                       <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
                       <webapps:pageText key="approval_suser_Warning" />
                       </p>
                    </div>
                    <%} else { %>
                    <% if(null != diffUserPendingPolicy && diffUserPendingPolicy.size() > 0) {%>
                    <div class="statusMessage" id="warning">
                       <h6>&nbsp;</h6>
                       <p>
                       <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
                       <webapps:pageText key="approval_duser_Warning" />
                       </p>
                    </div>
            	<%}} %>
                </logic:notPresent>



            <div class="itemStatus" style="width:800px; ">
              <table width="100%" border="0" cellspacing="0" cellpadding="3">
            				<tr>
            						<td><strong><webapps:pageText key="Targets" /></strong></td>
            				</tr>

            				<logic:iterate id="target" name='page_tgs_todelete' type="com.marimba.apps.subscription.common.objects.Target">
            				<tr>
            						<td>
            						<% //String tgLabel="target"; %>
                                    <bean:define id="ID" name="target" property="id" toScope="request"/>
                                    <bean:define id="Name" name="target" property="name" toScope="request"/>
                                    <bean:define id="Type" name="target" property="type" toScope="request"/>
                                    <jsp:include page="/includes/target_display_single.jsp"/>
            						</td>
            				</tr>
            				</logic:iterate>

              </table>
            </div>

            <logic:equal name="display_delete_props" value="false">
              <html:hidden property="page_pkgs_delete_all" value="false" />
            </logic:equal>

            <div id="pageNav" style="width:800px; ">
            		<input type="button" class="btn btn-sm btn-primary" value="<webapps:pageText shared="true" type="delete_preview" key="YesDeleteAll"/>" id="yes_all" onClick="javascript:send(document.forms.targetDetailsForm,'/distDeleteSave.do?all=true');"/>
            		<input type="button" class="btn btn-sm btn-outline-primary" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:redirect('/returnToOrigin.do');" >
            </div>

</div>
</div>
</div>
</div>
            </html:form>

		</section>
		</main>
</body>



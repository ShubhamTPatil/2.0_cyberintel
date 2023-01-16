<%@ page contentType="text/html;charset=UTF-8" %>
<%--
 Copyright 2004-2012, BMC Software Inc. All Rights Reserved.
 Confidential and Proprietary Information of BMC Software Inc.
 Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
 6,381,631, and 6,430,608. Other Patents Pending.

 $File$

 @author    Theen-Theen Tan
 @version   $Revision$, $Date$

--%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.forms.NamespaceForm" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="spm" topic="namesp" />
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<%-- Body content --%>
<body>
<html:form name="namespaceForm" action="/namespaceSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.NamespaceForm">

<main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename"><webapps:pageText key="Title"/></span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="Set Child Container"><i
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
				<br/>
		        <%@ include file="/includes/usererrors.jsp" %>
		        <%@ include file="/includes/help.jsp" %>
		        <br/>
		        <div style="width:90%; text-align: center; margin: auto;">
		            <div class="formTabs">
		                <table width="100%" border="0" cellspacing="0" cellpadding="0">
		                    <tr>
		                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_lft.gif" width="5" height="5"></td>
		                        <td width="100%" style="border-top:1px solid #CCCCCC; height:5px;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="4"></td>
		                        <td width="5"><img src="/shell/common-rsrc/images/form_corner_top_rt.gif" width="5" height="5"></td>
		                    </tr>
		                </table>
		            </div>
		            <div class="formContent">
		                <table cellpadding="5" cellspacing="0" style="margin:auto;">
		
		                    <tr>
		                        <td align="right" nowrap><webapps:pageText key="CurrentContainerPrompt" /></td>
		                        <td><bean:write name="namespaceForm" property="rootContainer"/></td>
		                    </tr>
		                    <tr>
		                        <td align="right" nowrap><webapps:pageText key="Prompt" /></td>
		                        <td>
		                            <select property="namespace" class="form-select" value='<%= ((NamespaceForm) session.getAttribute("namespaceForm")).getNamespace() %>' >
		                                <option value=""><webapps:pageText key="DefLoc" /></option>
		                                <options property="namespaceList" />
		                            </select>
		                        </td>
		                    </tr>
		                    <tr>
		                        <td align="right"><input class="form-check-input" type="checkbox" name="namespaceForm" styleId="skipSettingBox" property="value(namespace_skipsetting)" value="true" style="margin-top:0;"/></td>
		                        <td><label class="form-check-label" for="skipSettingBox"><webapps:pageText key="SkipSetting" /></label></td>
		                    </tr>
		                </table>
		            </div>
		            
		            <div class="formBottom">
		                <table width="100%" cellpadding="0" cellspacing="0">
		                    <tr>
		                        <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
		                        <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
		                        <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
		                    </tr>
		                </table>
		            </div>
		            <br/>
		            <div id="pageNav">
		                <input type="submit" class="btn btn-sm btn-primary mainBtn" name="save" value=" <webapps:pageText key="OK" type="global" /> ">
		                <input type="button" class="btn btn-sm btn-outline-primary" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.namespaceForm, '/namespaceRedirect.do');" >
		            </div>
		        </div>
		    </div>	
		 </div>


</section>
</main>

</html:form>

<%@ include file="/includes/footer.jsp" %>


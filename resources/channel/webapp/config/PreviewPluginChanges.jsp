<%@ page contentType="text/html;charset=UTF-8" %>
<%--
      Copyright 2003, Marimba Inc. All Rights Reserved.
      Confidential and Proprietary Information of Marimba, Inc.
      @(#)PreviewPluginChanges.jsp
      @author Devendra Vamathevan
      @version 1.3, 02/05/2003

--%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<webapps:helpContext context="spm" topic="prev_pi" />

<script type="text/javascript">
$(function () { $('#settings').addClass('nav-selected'); });
</script>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>


<%@ include file="/includes/body.html" %>

<%@ include file="/includes/info.jsp" %>

<%-- <webapps:tabs tabset="main" tab="cfgview"/> --%>

<%--@ include file="/includes/banner.jsp" --%>


<%@ page import = "java.util.*" %>
<%@ page import = "com.marimba.apps.subscription.common.LDAPVars" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants" %>

<%-- Body content --%>

<body onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<html:form name="setPluginForm" action="/pluginPublish.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm" >

<html:hidden name="setPluginForm" property="value(pallowprov)"/>
<html:hidden name="setPluginForm" property="value(usessl)"/>
<logic:equal name="setPluginForm" property="value(vendor)" value="ActiveDirectory">
	<html:hidden name="setPluginForm" property="value(authmethod)"/>
</logic:equal>


<main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename"><webapps:pageText key="Title"/></span>
          <span data-bs-toggle="tooltip" data-bs-placement="right" title="<webapps:pageText key="Title"/>"><i
              class="fa-solid fa-circle-info text-primary"></i></span>
        </div>
        <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i></div>
        <div class="p-2 bd-highlight text-primary align-self-center"> <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left"
              style="margin-right: 5px;"></i>CMS Home</a>
        </div>
      </div>

    </div>

    <section class="section dashboard">
    
    	<div class="card">
   		<div class="card-body" style="width:70%; align-self: center;">
        <logic:present scope="request" name="duplicateChangeRequestExists">
            <div class="statusMessage" id="critical" align="left">
                <h6>&nbsp;</h6>
                <logic:equal name="duplicateChangeRequestExists" value="servicenow">
                    <p><webapps:pageText key="servicenow.duplicate"/></p>
                </logic:equal>
                <logic:equal name="duplicateChangeRequestExists" value="remedyforce">
                    <p><webapps:pageText key="remedyforce.duplicate"/></p>
                </logic:equal>
            </div>
        </logic:present>
        
        <logic:iterate id="previewValues" name="preview_values" type="com.marimba.webapps.tools.util.PropsBean">
            <logic:present name="previewValues" property="value(showall)">
                <div class="formTabs">
                
                <logic:equal name="previewValues" property="value(showall)" value="true">
                	<nav>
					  <div class="nav nav-tabs" id="nav-tab" role="tablist">
					    <button class="nav-link" type="button" role="tab" aria-selected="false">
					    	<a href="/spm/pluginSave.do?showAll=false"><webapps:pageText key="mychanges" /></a>
					    </button>
					    <button class="nav-link active" type="button" role="tab" aria-selected="true">
					    	<webapps:pageText key="allsettings" />
					    </button>
					  </div>
					</nav>
                </logic:equal>
                <logic:notEqual name="previewValues" property="value(showall)" value="true">
                	<nav>
					  <div class="nav nav-tabs" id="nav-tab" role="tablist">
					    <button class="nav-link active" type="button" role="tab" aria-selected="true">
					    	<webapps:pageText key="mychanges" />
					    </button>
					    <button class="nav-link" type="button" role="tab" aria-selected="false">
					    	<a href="/spm/pluginSave.do?showAll=true"><webapps:pageText key="allsettings" /></a>
					    </button>
					  </div>
					</nav>
                </logic:notEqual>
                
                
                    <%-- <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <logic:equal name="previewValues" property="value(showall)" value="true">
                            <tr>
                                <td width="100" align="center" class="formTabInactive"><a href="/spm/pluginSave.do?showAll=false" class="noUnderlineBlackLink"><webapps:pageText key="mychanges" /></a></td>
                                <td width="100" align="center" class="formTabActive"><webapps:pageText key="allsettings" /></td>
                                <td></td>
                            </tr>
                        </logic:equal>
                        <logic:notEqual name="previewValues" property="value(showall)" value="true">
                            <tr>
                                <td width="100" align="center" class="formTabActive"><webapps:pageText key="mychanges" /></td>
                                <td width="100" align="center" class="formTabInactive"><a href="/spm/pluginSave.do?showAll=true" class="noUnderlineBlackLink"><webapps:pageText key="allsettings" /></a></td>
                                <td></td>
                            </tr>
                        </logic:notEqual>
                    </table> --%>
                </div>

                <table border="0" cellspacing="0" cellpadding="3">
            </logic:present>
					<logic:present name="previewValues" property="value(title)">
                        <tr>
                            <td colspan="2">
                                <div class="card-title"><webapps:pageText key='<%= (String)previewValues.getValue("title")%>'/></div>
                            </td>
                        </tr>
					</logic:present>
					<logic:present name="previewValues" property="value(displayname)">
                        <logic:notPresent name="previewValues" property="value(nodisplay)">
                        <logic:notPresent name="previewValues" property="value(hidden)">
                            <logic:notPresent name="previewValues" property="value(changed)">
                                <tr>
                                    <td align="right">
                                        <webapps:pageText key='<%= (String)previewValues.getValue("displayname")%>'  />
                                    </td>
                                <td>
                                    <logic:present name="previewValues" property="value(externalized)">
                                        <webapps:pageText key="<%= (String)previewValues.getValue("assignedValue")%>"/>
                                    </logic:present>
                                    <logic:notPresent name="previewValues" property="value(externalized)">
                                        <bean:write name="previewValues" property="value(assignedValue)" filter="true" />
                                    </logic:notPresent>
                                </td>
                            </logic:notPresent>
							<logic:present name="previewValues" property="value(changed)">
                                <td align="right">
                                    <webapps:pageText key='<%= (String)previewValues.getValue("displayname")%>'  />
                                </td>
                                <td>
                                    <span class="textHighlighted">
                                        <logic:present name="previewValues" property="value(externalized)">
                                           <webapps:pageText key="<%= (String)previewValues.getValue("assignedValue")%>"/>
                                        </logic:present>
                                        <logic:notPresent name="previewValues" property="value(externalized)">
                                            <bean:write name="previewValues" property="value(assignedValue)" filter="true" />
                                        </logic:notPresent>
                                    </span>
                                </td>
					        </logic:present>
							</tr>
						</logic:notPresent>
						</logic:notPresent>
					</logic:present>
		</logic:iterate>
        <tr> <td colspan="2"> &nbsp;</td> </tr>

        </table>
		
		
    	</div>
    	</div>
    	
    	<div class="col" style="text-align: right;">
            <input type="submit" class="btn btn-sm btn-primary" value="<webapps:pageText key="btnpublish" />" styleClass="mainBtn">
            <input type="button" class="btn btn-sm btn-outline-primary" value="<webapps:pageText key="btncancel" />" onClick="javascript:send(document.setPluginForm,'/pluginCancel.do');" >
		</div>
		
	</section>
</main>
		
</html:form>
<%@ include file="/includes/footer.jsp" %>
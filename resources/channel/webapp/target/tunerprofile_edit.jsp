<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Enumeration,

                 com.marimba.apps.subscription.common.objects.Subscription,

                 com.marimba.apps.subscription.common.objects.Channel,

                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>


<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<webapps:helpContext context="sm" topic="profile_choose" />


<script>

function saveState(forwardaction) {

  document.tunerProfileForm.forward.value = forwardaction;

  send(document.tunerProfileForm, '/tunerProfile.do?load');

}

</script>


</head>


<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">

<logic:notPresent name="taskid">
    <% if(null != EmpirumContext) {%>
        <webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
    <% } else { %>
        <webapps:tabs tabset="main" tab="tgtview"/>
    <% } %>

</logic:notPresent>
<logic:present name="taskid">
    <% request.setAttribute("nomenu", "true"); %>
    <webapps:tabs tabset="bogustabname" tab="noneselected"/>
</logic:present>


<html:form styleId="tunerProfileForm" action="/tunerProfile.do?action=getProfile">

<html:hidden property="forward" />

<div style="text-align:center;">

    <div style="padding-left:15px; padding-right:15px;">

        <div class="pageHeader" style="width:850px;"><span class="title"><webapps:pageText key="Title"/></span></div>

        <logic:present name="taskid">
            <div class="pageHeader" style="width:850px;">
                <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
                <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
            </div>
        </logic:present>

        <div class="pageInfo" style="width:850px;">

        <table cellspacing="0" cellpadding="2" border="0">

            <tr>

                <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>

                <td><webapps:pageText key="IntroShort"/></td>

            </tr>

        </table>

        </div>

        <%-- Errors Display --%>

        <table width="100%" border="0" cellspacing="0" cellpadding="0">

           <%@ include file="/includes/usererrors.jsp" %>

        </table>

        <table cellpadding="0" cellspacing="0">

        <tr>

            <td valign="top">

            <table cellpadding="0" cellspacing="0">

                <tr>

                <td>

				    <table width="350" border="0" cellspacing="0" cellpadding="0">

                    <tr>

                        <td class="tableTitle"><webapps:pageText key="list"/></td>
                        <td align="right" class="pagination">&nbsp;</td>

                    </tr>

                    </table>

                    <table width="350" border="0" cellspacing="0" cellpadding="0">

                    <tr valign="middle" class="smallButtons">

                        <td class="tableRowActions"><webapps:pageText key="location"/>
                            <html:text property="value(txName)" styleId="txName" styleClass="requiredField" size="30" />

                            <input type="button" value="<webapps:pageText shared="true" type="button" key="go"/>"  onClick="javascript:send(document.tunerProfileForm, '/tunerProfile.do?action=getProfile');" />
                        </td>

                    </tr>

                    </table>

					<div class="tableWrapper" style="width:350px;">

                    <div class="headerSection" style="width:100%;">

					<table width="332" border="0" cellpadding="0" cellspacing="0">


					<%--    <colgroup width="70%">

					    </colgroup>

					    <colgroup width="30%"></colgroup>

                        --%>

                        <colgroup width="100%">

					    </colgroup>

					    <thead>

						<tr>

						<td class="tableHeaderCell"><webapps:pageText key="name"/></td>
                        <%--

						<td class="tableHeaderCell">Profile Type</td>

                        --%>

						</tr>

						</thead>

					</table>

					</div>

				    <div id="dataSection" style="height:300px; width:100%; overflow:auto;">

                    <table width="332" border="0" cellpadding="0" cellspacing="0">

                    <colgroup width="70%">

			        </colgroup>

				    <colgroup width="30%"></colgroup>

                    <logic:present name='<%=IWebAppConstants.SESSION_DISPLAY_RS%>'>

                    <logic:iterate id="app" name='<%=IWebAppConstants. SESSION_DISPLAY_RS%>' type="com.marimba.webapps.tools.util.PropsBean">

			        <tr>

			            <td class="rowLevel1" style="border-top:0px;">

                        <img src="/shell/common-rsrc/images/marimba_m.gif" width="16" height="16" align="absmiddle"/>

                        <!-- <a href='<%= request.getContextPath() + "/tunerProfile.do?action=add&profile=" + com.marimba.tools.util.URLUTF8Encoder.encode((String)app.getValue("displayname"))+"&type=" + java.net.URLEncoder.encode((String)app.getValue("dn")) %>' class="hoverLink"><%= (String) app.getValue("displayname") %> -->
                        <a href='<%= request.getContextPath() + "/tunerProfile.do?action=add&profile=" + com.marimba.tools.util.URLUTF8Encoder.encode((String)app.getValue("displayname"))+"&type=" + com.marimba.tools.util.URLUTF8Encoder.encode((String)app.getValue("dn")) %>' class="hoverLink"><%= (String) app.getValue("displayname") %>
                        <%-- //Symbio modified 05/19/2005 --%></a></td>

                        <%-- <td class="rowLevel1" style="border-top:0px;"> <%= (String) app.get("dn") %></td> --%>

                    </tr>

                    </logic:iterate>

                    </logic:present>

                    </table>

                    </div>

					</div>

                </td>

                </tr>

            </table>

        </td>

        <td valign="top" style="padding-left:50px;">

          <table width="418" border="0" cellspacing="0" cellpadding="0">

            <tr>

              <td class="tableTitle"><webapps:pageText key="selected"/></td>
            </tr>

          </table>

          <div class="tableWrapper" style="width:418px;">

            <table width="418" border="0" cellspacing="0" cellpadding="0">

              <tr valign="middle" class="smallButtons">

                <td class="tableRowActions"><input type="button" value="<webapps:pageText shared="true" type="button" key="remove"/>" id="delete_btn"  onClick="javascript:send(document.tunerProfileForm, '/tunerProfile.do?action=add&profile=_remove_');">
                </td>

              </tr>

            </table>

            <div id="dataSection" style="height:50px; width:100%; overflow:auto;">

              <table width="400" cellpadding="0" cellspacing="0">

                <colgroup width="100%">

                </colgroup>

                <tr>

                  <td class="rowLevel1" style="border-top:0px;">

                  <img src="/shell/common-rsrc/images/marimba_m.gif" width="16" height="16" align="absmiddle"/>&nbsp;<bean:write name="tunerProfileForm" property="value(tunerUpdateProfile)"/></td>

                </tr>

              </table>

            </div>

            <!--end dataSection-->

          </div>

          <!--end tableWrapper -->

          <div id="pageNav" style="width:418px;">

            <input type="button" class="mainBtn" value="<webapps:pageText shared="true" type="button" key="ok"/>" id="OK"    onClick="javascript:send(document.tunerProfileForm, '/tunerProfile.do?action=ok');" />
            &nbsp;

            <input type="button" value="<webapps:pageText shared="true" type="button" key="cancel"/>"  onClick="javascript:send(document.tunerProfileForm, '/tunerProfile.do?action=cancel');" />
          </div>

        </td>

      </tr>

    </table>

  <!--end supder div for padding-->

</div>

</div>

</html:form>


<%@ include file="/includes/footer.jsp" %>


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
<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>


<%@ include file="/includes/body.html" %>

<%@ include file="/includes/info.jsp" %>

<webapps:tabs tabset="main" tab="cfgview"/>

<%--@ include file="/includes/banner.jsp" --%>


<%@ page import = "java.util.*" %>
<%@ page import = "com.marimba.apps.subscription.common.LDAPVars" %>
<%@ page import = "com.marimba.apps.subscription.common.ISubscriptionConstants" %>

<%-- Body content --%>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onResize="domMenu_activate('domMenu_keramik'); repositionMenu()">
<html:form name="setPluginForm" action="/pluginPublish.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm" >

<html:hidden name="setPluginForm" property="value(pallowprov)"/>
<html:hidden name="setPluginForm" property="value(usessl)"/>
<logic:equal name="setPluginForm" property="value(vendor)" value="ActiveDirectory">
	<html:hidden name="setPluginForm" property="value(authmethod)"/>
</logic:equal>

<div align="center">
    <div style="width:800px">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
            <tr>
                <td>
                    <div class="pageHeader"><span class="title"><webapps:pageText key="Title" /></span></div>
                </td>
            </tr>
        </table>
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
                    <table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <logic:equal name="previewValues" property="value(showall)" value="true">
                            <tr>
                                <td width="5"><img src="/shell/common-rsrc/images/tab_form_left_i.gif" width="5" height="19"></td>
                                <td width="75" align="center" class="formTabInactive"><a href="/spm/pluginSave.do?showAll=false" class="noUnderlineBlackLink"><webapps:pageText key="mychanges" /></a></td>
                                <td width="5" style="background-color: #F0F0F0;"><img src="/shell/common-rsrc/images/tab_form_i_a.gif" width="5" height="19"></td>
                                <td width="75" align="center" class="formTabActive"><webapps:pageText key="allsettings" /></td>
                                <td width="5"><img src="/shell/common-rsrc/images/tab_form_right_a.gif" width="5" height="19"></td>
                                <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
                                <td width="5">&nbsp;</td>
                            </tr>
                        </logic:equal>
                        <logic:notEqual name="previewValues" property="value(showall)" value="true">
                            <tr>
                                <td width="5"><img src="/shell/common-rsrc/images/tab_form_left_a.gif" width="5" height="19"></td>
                                <td width="75" align="center" class="formTabActive"><webapps:pageText key="mychanges" /></td>
                                <td width="5" style="background-color: #F0F0F0;"><img src="/shell/common-rsrc/images/tab_form_a_i.gif" width="5" height="19"></td>
                                <td width="75" align="center" class="formTabInactive"><a href="/spm/pluginSave.do?showAll=true" class="noUnderlineBlackLink"><webapps:pageText key="allsettings" /></a></td>
                                <td width="5"><img src="/shell/common-rsrc/images/tab_form_right_i.gif" width="5" height="19"></td>
                                <td style="border-bottom:1px solid #CCCCCC;">&nbsp;</td>
                                <td width="5">&nbsp;</td>
                            </tr>
                        </logic:notEqual>
                        <tr>
                            <td colspan="6" style="border-left:1px solid #CCCCCC; height:5px;">
                                <img src="/shell/common-rsrc/images/invisi_shim.gif">
                            </td>
                            <td>
                                <img src="/shell/common-rsrc/images/form_corner_sub_top_rt.gif" width="5" height="5">
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="formContent" align="left">

                <table border="0" cellspacing="0" cellpadding="3">
            </logic:present>
					<logic:present name="previewValues" property="value(title)">
                        <tr>
                            <td colspan="2">
                                <h3><webapps:pageText key='<%= (String)previewValues.getValue("title")%>'/></h3>
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
		<div class="formBottom">
			<table width="100%" cellpadding="0" cellspacing="0">
                <tr>
                    <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
                    <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
                    <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
                </tr>
			</table>
		</div>
		<div id="pageNav">
            <input type="submit" value="<webapps:pageText key="btnpublish" />" styleClass="mainBtn">
            <input type="button" value="<webapps:pageText key="btncancel" />" onClick="javascript:send(document.setPluginForm,'/pluginCancel.do');" >
		</div>
		</div>
</div>
</html:form>
<%@ include file="/includes/footer.jsp" %>
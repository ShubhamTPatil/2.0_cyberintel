<%--
    Copyright 1997-2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/8.1.00/resources/channel/webapp/target/advancedpkgview.jsp
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Enumeration,
                 com.marimba.apps.subscription.common.objects.Subscription,
                 com.marimba.apps.subscription.common.objects.Channel,
                 java.util.HashMap"%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<webapps:helpContext context="sm" topic="tc_props" />

<script>
// This function saves the form attributes to the DistributionBean,
// before forwarding to the next page.
// IMPORTANT: Always use to navigate between pages so that the changes are
// persistified in the session bean.
function saveState(forwardaction) {
  document.forms.advancepkgForm.forward.value = forwardaction;
  send(document.forms.advancepkgForm,'/saveAdvancedProperties.do');
}

function appendSend(form) {
    document.forms.advancepkgForm.forward.value = "/target/advancedpkgview.jsp";
    var selectbox = document.advancepkgForm["value(common_tunerprops)"];
    var fullpath = "<html:rewrite page='" + selectbox.options[selectbox.selectedIndex].value + "' />";
    form.action = fullpath ;
    form.submit();
}

var type_array = new Array("type_tuner_settings","type_package_settings");
</script>
</head>

<body>
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
<html:form name="advancepkgForm" action="/tunerChPropsSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TunerChPropsForm" >
<bean:define name="page_tchprops_sub" id="sub" type="com.marimba.apps.subscription.common.objects.Subscription"/>
<html:hidden property="forward" />
<%Enumeration tpropskeys = ((Subscription)sub).getPropertyKeys("");
  pageContext.setAttribute("tpropskeys",tpropskeys);
  Enumeration spropskeys = ((Subscription)sub).getPropertyKeys("service");
  pageContext.setAttribute("spropskeys",spropskeys);
  Enumeration chpropskeys = ((Subscription)sub).getPropertyKeys("subscribers");
  pageContext.setAttribute("chpropskeys",chpropskeys);
  Enumeration allchspropskeys = ((Subscription)sub).getPropertyKeys("*");
  pageContext.setAttribute("allchspropskeys",allchspropskeys);
  Enumeration crssettingkeys = ((Subscription)sub).getPropertyKeys("crs");
  pageContext.setAttribute("crssettingkeys",crssettingkeys);
%>

<div align="center">
    <div style="padding-left:25px; padding-right:25px;">
        <div class="pageHeader"><span class="title"><webapps:pageText key="policy" type="pgtitle" shared="true"/></span></div>
        <logic:present name="taskid">
            <div class="pageHeader">
                <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
                <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
            </div>
        </logic:present>
            <%-- Errors Display --%>
              <div style="width:100%; ">
														<table width="100%" border="0" cellspacing="0" cellpadding="0">
                <%@ include file="/includes/usererrors.jsp" %>
              </table>
														</div>
												
												<div class="pageInfo">
                <table cellspacing="0" cellpadding="2" border="0">
                <tr>
                    <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>
                    <td><webapps:pageText key="IntroShort"/></td>
                </tr>
                </table>

            </div><!-- pageinfo -->
                       
    <div class="itemStatus">
      <table cellspacing="0" cellpadding="3" border="0">
        <tr>
          <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>: </td>
		    <logic:iterate id="target" name="session_dist" property="targets">
		      <td align="left">
		        <% //String tgLabel="target"; %>
                <bean:define id="ID" name="target" property="id" toScope="request"/>
                <bean:define id="Name" name="target" property="name" toScope="request"/>
                <bean:define id="Type" name="target" property="type" toScope="request"/>
                <jsp:include page="/includes/target_display_single.jsp"/>
		      </td>
		    </logic:iterate>
        </tr>
      </table>
    </div>

          <webapps:formtabs tabset="dist" tab="adv" subtab="props" />
         <div class="formContent" id="dataSection" style="text-align:left; overflow:auto;">
            <div class="sectionInfo"><webapps:pageText key="SectionInfo"/></div>
          <table border="0" cellspacing="0" cellpadding="3">
          <tr>
            <td align="right" nowrap><webapps:pageText key="Add"/></td>
                <td>
                <select name="tunerChPropsForm" name="prop_type" onChange="showSection(type_array,options[selectedIndex].value)">
                    <option value="type_tuner"><webapps:pageText key="selecttuner"/></option>
                    <option value="type_package"><webapps:pageText key="selectpackage"/></option>
                </select>
             </td>
          </tr>
          <tbody id="type_tuner_settings">
		    <tr>
            <td align="right" nowrap><webapps:pageText key="tunerproperty"/></td>
            <td>
            <html:select name="tunerChPropsForm" property="value(common_tunerprops)" onchange="javascript:appendSend(document.forms.advancepkgForm);">
                <html:option value="/saveAdvancedProperties.do?tunerprop=marimba.subscription.nodelete&tunerproptype=boolean"><webapps:pageText key="nodelete"/></html:option>
                <html:option value="/saveAdvancedProperties.do?tunerprop=marimba.subscription.reboot.allowcancel&tunerproptype=boolean"><webapps:pageText key="allowcancel"/></html:option>
                <html:option value="/saveAdvancedProperties.do?tunerprop=marimba.subscription.retrytime&tunerproptype=integer"><webapps:pageText key="retrytime"/></html:option>
                <html:option value="/saveAdvancedProperties.do?tunerprop=marimba.subscription.retrycount&tunerproptype=integer"><webapps:pageText key="retrycount"/></html:option>
                <html:option value="/saveAdvancedProperties.do?tunerprop=marimba.subscription.useshortcuts&tunerproptype=boolean"><webapps:pageText key="usechortcuts"/></html:option>
                <html:option value="/saveAdvancedProperties.do?tunerprop=marimba.subscription.installmode&tunerproptype=installmode"><webapps:pageText key="installmode"/></html:option>
                <html:option value="/saveAdvancedProperties.do?tunerprop=marimba.subscription.reapplyconfigonfail&tunerproptype=boolean"><webapps:pageText key="reapplyonfailure"/></html:option>
              </html:select>


		        <logic:equal name="tunerChPropsForm" property="value(tunerproptype)" value="boolean">
                    <font class="generalText"><webapps:pageText key="value"/></font>
                    <html:radio property="value(tunerpropvalue)" value="true" />
                        <label for="no_delete_true"><webapps:pageText key="true"/></label>
                    <html:radio property="value(tunerpropvalue)" value="false" />
                        <label for="no_delete_false"><webapps:pageText key="false"/></label> &nbsp;&nbsp;&nbsp;
                </logic:equal>

	            <logic:equal name="tunerChPropsForm" property="value(tunerproptype)" value="integer">
                    <font class="generalText"><webapps:pageText key="intvalue"/></font>
                    <html:text property="value(tunerpropvalue)" size="10" maxlength="50"/>
	            </logic:equal>
	            <logic:equal name="tunerChPropsForm" property="value(tunerproptype)" value="installmode">
	                <html:select property="value(tunerpropvalue)">
	                    <html:option value="silent"><webapps:pageText key="silent"/></html:option>
	                    <html:option value="aspackaged"><webapps:pageText key="packaged"/></html:option>
	                </html:select>
	            </logic:equal>
                    <input type="button" onClick="javascript:send(document.forms.advancepkgForm,'/tunerChPropsTSet.do')" value="<webapps:pageText key="apply" />" />
                </td>
              </tr>
			</tbody>
			<tbody id="type_package_settings" style="display:none;">
			    <tr>
				    <td align="right"><webapps:pageText key="package"/></td>
					<td>
                        <html:select name="tunerChPropsForm" property="value(chpropstype)">
                            <html:option value="*"><webapps:pageText key="allpackages"/></html:option>
                            <html:option value="subscribers"><webapps:pageText key="subscribedpackages"/></html:option>
                            <html:option value="service"><webapps:pageText key="servicechannel"/></html:option>
		                    <%-- iterate through the channel urls in the subscription--%>
		                    <logic:iterate id="ch" name="sub" property="channels">
		                        <html:option value="<%=((Channel)ch).getUrl()%>"> <bean:write name="ch" property="url" /> </html:option>
		                    </logic:iterate>
                        </html:select>
                    </td>
		        </tr>
		        <tr>
				    <td align="right"><webapps:pageText key="propertyandvalue"/> </td>
                    <td>
					    <input type="text" name="value(channel_prop_name)" size="20"/>
																  =
						<input type="text" name="value(channel_prop_value)" size="10">
                        <input type="button" onClick="javascript:send(document.forms.advancepkgForm,'/tunerChPropsChSet.do?apply=property')" value="<webapps:pageText key="apply" />" />
				    </td>
			    </tr>
			</tbody>
                 <tr>
                     <td align="right" valign="top">
                        <webapps:pageText key="priority"/>
                     </td>
                     <td align="left" valign="top">
                       <input type="text" name="priority_value" size="5" maxlength="5" onkeypress="return restrictKeyPressPositive(event)"/> &nbsp;&nbsp;
                       <input type="button" onclick="javascript:send(document.forms.advancepkgForm,'/tunerChPropsPriority.do?apply=priority')" value="<webapps:pageText key="apply" />"  />
                       <webapps:pageText key="validpriority"/>
                     </td>
                 </tr>
                <tr>
                <td align="right" valign="top"><webapps:pageText key="properties"/> </td>
          <td>
	    <%--
	        Note that the line breaks in the JSP file affects the line breaks displayed
	        in the text area. Particularly there should be a line break after
	        each getProperty call.
	    --%>
        <textarea name="value(allproperties)" rows="12" cols="60"><logic:present name="tpropskeys"><logic:iterate name="tpropskeys" id="key"><bean:write name="key" />=<%=((Subscription)sub).getProperty("",(String)key)%>
</logic:iterate></logic:present><logic:present name="spropskeys"><logic:iterate name="spropskeys" id="key" indexId="propsidx"><bean:write name="key" />,service=<%=((Subscription)sub).getProperty("service",(String)key)%>
</logic:iterate></logic:present><logic:present name="chpropskeys"><logic:iterate name="chpropskeys" id="key" indexId="propsidx"><bean:write name="key" />,subscribers=<%=((Subscription)sub).getProperty("subscribers",(String)key)%>
</logic:iterate></logic:present><logic:present name="allchspropskeys"><logic:iterate name="allchspropskeys" id="key" indexId="propsidx"><bean:write name="key" />,*=<%=((Subscription)sub).getProperty("*",(String)key)%>
</logic:iterate></logic:present><logic:present name="crssettingkeys"><logic:iterate name="crssettingkeys" id="key" indexId="propsidx"><bean:write name="key" />,crs=<%=((Subscription)sub).getProperty("crs",(String)key)%>
</logic:iterate></logic:present><logic:present name="sub" property="channels"><logic:iterate name="sub" property="channels" id="channel" type="com.marimba.apps.subscription.common.objects.Channel"><logic:iterate name="channel" property="propertyKeys" id="key"><bean:write name="key"/>,<bean:write name="channel" property="url"/>=<%=((Channel)channel).getProperty((String)key) %>
</logic:iterate></logic:iterate></logic:present><logic:present name="sub" property="dummyChannels"><logic:iterate name="sub" property="dummyChannels" id="channel" type="com.marimba.apps.subscription.common.objects.Channel"><logic:iterate name="channel" property="propertyKeys" id="key"><bean:write name="key"/>,<bean:write name="channel" property="url"/>=<%=((Channel)channel).getProperty((String)key) %>
</logic:iterate></logic:iterate></logic:present>
</textarea>
            </td>
	    </tr>
    </table>
    <br>
    </div>
      <!--end formContent-->
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
        <input name="Submit32" type="button" class="mainBtn" onClick="javascript:saveState('/distInit.do?action=preview')" value="<webapps:pageText key="preview" type="button" shared="true"/>" />
        &nbsp;
      <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:present>
      <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:notPresent>
    </div>

</div><!--end supder div for padding-->
</div><!-- end super div for centering -->
</html:form>

<script>
CMSOnResizeHandler.addHandler("resizeDataSection('dataSection','pageNav');");
resizeDataSection('dataSection','pageNav');
</script>

</body>
</html>

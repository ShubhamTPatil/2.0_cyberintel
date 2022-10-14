<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)properties.jsp

     @author Angela Saval
     @version 1.17, 08/22/2002
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Subscription" %>
<%@ page import = "com.marimba.apps.subscription.common.objects.Channel" %>
<%@ page import = "com.marimba.webapps.intf.IMapProperty" %>
<%@ page import = "java.util.*" %>

<%@ include file="/includes/startHeadSection.jsp" %>
    <webapps:helpContext context="sm" topic="tc_props" />
<%@ include file="/includes/endHeadSection.jsp" %>




<%@ include file="/includes/banner.jsp" %>

<script language="JavaScript">
  function appendSend(form) {
    var selectbox = document.tunerChPropsForm.common_tunerprops;
    var fullpath = "<html:rewrite page='" + selectbox.options[selectbox.selectedIndex].value + "' />";
    form.action = fullpath ;
    form.submit();
  }

</script>


<%-- Body content --%>
<html:form name="tunerChPropsForm" action="/tunerChPropsSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TunerChPropsForm" >

<bean:define name="page_tchprops_sub" id="sub" type="com.marimba.apps.subscription.common.objects.Subscription"/>

<%-- This is used for obtaining the enumerations for channels and property keys from the subscription.  A scriptlet
 is used because the method calls on the subscription object take a parameter, and the bean interface does not allow
 for this.
--%>
<%Enumeration tpropskeys = ((Subscription)sub).getPropertyKeys("");
  pageContext.setAttribute("tpropskeys",tpropskeys);
  Enumeration spropskeys = ((Subscription)sub).getPropertyKeys("service");
  pageContext.setAttribute("spropskeys",spropskeys);
  Enumeration chpropskeys = ((Subscription)sub).getPropertyKeys("subscribers");
  pageContext.setAttribute("chpropskeys",chpropskeys);
  Enumeration allchspropskeys = ((Subscription)sub).getPropertyKeys("*");
  pageContext.setAttribute("allchspropskeys",allchspropskeys);
  Enumeration powersettingkeys = ((Subscription)sub).getPropertyKeys("power");
  pageContext.setAttribute("powersettingkeys",powersettingkeys);

%>

<table width="100%" border="0" cellpadding="5" class="generalText">
  <tr> 
    <td colspan="2">
      <table width="100%" border="0">
        <tr>
	  <td align="left">
	    <font class="pageTitle"><webapps:pageText key="Title" /></font>
	  </td>
	  <td align="right">
	    <input type="submit" name="Submit" value=" OK " id="OK">&nbsp;&nbsp;&nbsp;<input type="button" name="Cancel" value=" Cancel " onClick="javascript:send(document.tunerChPropsForm,'/tunerChPropsCancel.do');" styleId="Cancel">
	  </td>
	</tr>
      </table>
    </td>
  </tr>
  <bean:define id="tgDisplayList" name="main_page_target" />
  <%@ include file="/includes/target_display_comma.jsp" %>
  <%@ include file="/includes/usererrors.jsp" %>
  <tr> 
    <td colspan="2">  <%@ include file="/includes/help.jsp" %></td>
  </tr>

  
  <tr> 
    <td colspan="2">&nbsp;</td>
  </tr>
  <tr> 
    <td width="50%"> 
      <table border="1" cellspacing="0" cellpadding="5" class="generalText" bordercolor="cccccc" width="100%" id="tuner_props_table">
        <tr valign="middle"> 
          <td class="coreColor1"><font class="whiteText"><b><webapps:pageText key="Common_Properties" /></b></font></td>
        </tr>
        <tr valign="middle"> 
          <td class="coreColor3" nowrap> 
            <p><font class="generalText"><webapps:pageText key="Common_Properties" />:

	    </font>&nbsp;
              <html:select property="common_tunerprops" onchange="javascript:appendSend(document.tunerChPropsForm);">
                <html:option value="/tunerChPropsPageState.do?tunerprop=marimba.subscription.nodelete&tunerproptype=boolean">No Delete</html:option>
                <html:option value="/tunerChPropsPageState.do?tunerprop=marimba.subscription.reapplyconfigonfail&tunerproptype=boolean">Re-apply Config on Failure</html:option>
                <html:option value="/tunerChPropsPageState.do?tunerprop=marimba.subscription.reboot.allowcancel&tunerproptype=boolean">Allow Cancel</html:option>
                <html:option value="/tunerChPropsPageState.do?tunerprop=marimba.subscription.retrytime&tunerproptype=integer">Retry Time</html:option>
                <html:option value="/tunerChPropsPageState.do?tunerprop=marimba.subscription.retrycount&tunerproptype=integer">Retry Count</html:option>
                <html:option value="/tunerChPropsPageState.do?tunerprop=marimba.subscription.useshortcuts&tunerproptype=boolean">Use Shortcuts</html:option>
		<html:option value="/tunerChPropsPageState.do?tunerprop=marimba.subscription.installmode&tunerproptype=installmode">Install Mode</html:option>
              </html:select>
            </p>
	    <%-- Depending on the type, select the appropriate form elements to display
	    --%>
	    <p>
	    <webapps:errorsPresent property="tunerpropvalue">
	        <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
	    </webapps:errorsPresent>
	    <logic:equal name="tunerChPropsForm" property="tunerproptype" value="boolean">
            <font class="generalText">Value:</font> 
              <html:radio property="tunerpropvalue" value="true" />
              <font class="generalText">True</font>
              <html:radio property="tunerpropvalue" value="false" />
              <font class="generalText">False</font> &nbsp;&nbsp;&nbsp;
	     
	    </logic:equal>
	    <logic:equal name="tunerChPropsForm" property="tunerproptype" value="integer">
            <font class="generalText">Integer Value:</font> 	    
            <html:text property="tunerpropvalue" size="10" maxlength="50"/>
	    
	    </logic:equal>
	    <logic:equal name="tunerChPropsForm" property="tunerproptype" value="installmode">	    
	    <html:select property="tunerpropvalue">	      <html:option value="silent">Silent </html:option>
	      <html:option value="aspackaged">As Packaged </html:option>
	    </html:select>
	    </logic:equal>
            <input type="button" onClick="javascript:send(document.tunerChPropsForm,'/tunerChPropsTSet.do')" value="Apply" >
	        
	    </p>
          </td>
        </tr>
      </table>
    </td>
    <td width="50%"> 
      <table border="1" cellspacing="0" cellpadding="5" class="generalText" bordercolor="cccccc" width="100%" id="channel_props_table">
        <tr valign="middle"> 
          <td class="coreColor1"><font class="whiteText"><b><webapps:pageText key="ChannelProperties" /></b></font></td>
        </tr>
        <tr valign="middle"> 
          <td class="coreColor3" nowrap> 
            <p><font class="generalText">For Package:</font>&nbsp; 
              <html:select property="chpropstype">
                <html:option value="*">All Packages</html:option>
                <html:option value="subscribers">All Subscribed Packages</html:option>
                <html:option value="service">Subscription Policy Service Channel</html:option>
		<%-- iterate through the channel urls in the subscription
		--%>
		<logic:iterate id="ch" name="sub" property="channels">
		   <html:option value="<%=((Channel)ch).getUrl()%>"><bean:write name="ch" property="url" /> </html:option>
		</logic:iterate>
              </html:select>
            </p>
            <p><font class="generalText">Property:</font> 
              <input type="text" name="channel_prop_name" size="20">
              &nbsp; <font class="generalText">=</font> &nbsp; 
              <input type="text" name="channel_prop_value" size="10">
              &nbsp;&nbsp;&nbsp;
             <input type="button" onClick="javascript:send(document.tunerChPropsForm,'/tunerChPropsChSet.do')" value="Apply" >	      
            </p>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr> 
    <td colspan="2">&nbsp;</td>
  </tr>
  <tr valign="middle" align="center"> 
    <td colspan="2"> 
      <table width="60" border="0" cellspacing="0" cellpadding="2">
        <tr>
          <td><font class="generalText"><font class="generalText">All Tuner and Package Properties to Change on Targets</font></font></td>
        </tr>
	<tr>
          <td>
	  <%--
	    Note that the line breaks in the JSP file affects the line breaks displayed 
	    in the text area. Particularly there should be a line break after
	    each getProperty call.
	  --%> 
            <textarea name="allproperties" rows="12" cols="60"><logic:present name="tpropskeys"><logic:iterate name="tpropskeys" id="key"><bean:write name="key" />=<%=((Subscription)sub).getProperty("",(String)key)%>
</logic:iterate></logic:present><logic:present name="spropskeys"><logic:iterate name="spropskeys" id="key" indexId="propsidx"><bean:write name="key" />,service=<%=((Subscription)sub).getProperty("service",(String)key)%>
</logic:iterate></logic:present><logic:present name="chpropskeys"><logic:iterate name="chpropskeys" id="key" indexId="propsidx"><bean:write name="key" />,subscribers=<%=((Subscription)sub).getProperty("subscribers",(String)key)%>
</logic:iterate></logic:present><logic:present name="allchspropskeys"><logic:iterate name="allchspropskeys" id="key" indexId="propsidx"><bean:write name="key" />,*=<%=((Subscription)sub).getProperty("*",(String)key)%>
</logic:iterate></logic:present><logic:present name="powersettingkeys"><logic:iterate name="powersettingkeys" id="key" indexId="propsidx"><bean:write name="key" />,power=<%=((Subscription)sub).getProperty("power",(String)key)%>
</logic:iterate></logic:present><logic:present name="sub" property="channels"><logic:iterate name="sub" property="channels" id="channel" type="com.marimba.apps.subscription.common.objects.Channel"><logic:iterate name="channel" property="propertyKeys" id="key"><bean:write name="key"/>,<bean:write name="channel" property="url"/>=<%=((Channel)channel).getProperty((String)key) %>
</logic:iterate></logic:iterate></logic:present><logic:present name="sub" property="dummyChannels"><logic:iterate name="sub" property="dummyChannels" id="channel" type="com.marimba.apps.subscription.common.objects.Channel"><logic:iterate name="channel" property="propertyKeys" id="key"><bean:write name="key"/>,<bean:write name="channel" property="url"/>=<%=((Channel)channel).getProperty((String)key) %>
</logic:iterate></logic:iterate></logic:present>
</textarea>
       </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr class="generalText"> 
    <td colspan="2"> 
      <hr size="1" noshade width="100%">
    </td>
  </tr>
  <tr align="right" class="generalText"> 
    <td colspan="2"> 
      <input type="submit" name="Submit" value=" OK " id="OK">
      &nbsp; 
      <input type="button" name="Cancel" value=" Cancel " onClick="javascript:send(document.tunerChPropsForm,'/tunerChPropsCancel.do');" styleId="Cancel">
    </td>
  </tr>
</table>
<p>&nbsp;</p>
</html:form>

<%@ include file="/includes/footer.jsp" %>

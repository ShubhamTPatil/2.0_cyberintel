<%--
    Copyright 1997-2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/8.1.00/resources/channel/webapp/target/transmitterpermission_preview.jsp

    @author : Bharath M
--%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.Enumeration,
                 com.marimba.apps.subscription.common.objects.Subscription,
                 com.marimba.apps.subscription.common.objects.Channel,
                 com.marimba.apps.subscriptionmanager.intf.IWebAppConstants,
                 com.marimba.apps.subscriptionmanager.webapp.system.TLoginBean,
                 com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean,
                 java.util.ArrayList"%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/startHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>

<webapps:helpContext context="sm" topic="policy_preview" />

<script>
function saveState(forwardaction) {
  document.tunerProfileForm.forward.value = forwardaction;
  send(document.forms.tunerProfileForm, '/transLoginSave.do');
}
</script><body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
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

<html:form name="tunerProfileForm" action="/transLoginSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TunerProfileForm" >
<html:hidden property="forward" />
<%
  Subscription sub = (Subscription) session.getAttribute("page_tchprops_sub");
  pageContext.setAttribute("sub",sub);
  TLoginBean tloginbean  = (TLoginBean) session.getAttribute("session_tloginbean");

  if (sub != null){
    Enumeration tpropskeys = sub.getPropertyKeys("");
    pageContext.setAttribute("tpropskeys",tpropskeys);
    Enumeration spropskeys = sub.getPropertyKeys("service");
    pageContext.setAttribute("spropskeys",spropskeys);
    Enumeration chpropskeys = sub.getPropertyKeys("subscribers");
    pageContext.setAttribute("chpropskeys",chpropskeys);
    Enumeration allchspropskeys = sub.getPropertyKeys("*");
    pageContext.setAttribute("allchspropskeys",allchspropskeys);
    Enumeration crspropskeys = sub.getPropertyKeys("crs");
    pageContext.setAttribute("crspropskeys",crspropskeys);
  }
%>
<div align="center">
  <div style="padding-left:25px; padding-right:25px;">
    <div class="pageHeader">
    <span class="title">
        <logic:present name="copy_preview" >
            <webapps:pageText key="copyPreview" type="pagehdr" shared="true"/>
        </logic:present>
        <logic:notPresent name="copy_preview" >
            <webapps:pageText key="Title"/>
        </logic:notPresent>
    </span>
    </div>
    <logic:present name="taskid">
        <div class="pageHeader">
            <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>
            <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>
        </div>
    </logic:present>
                <logic:present name="policy_exists">
                <div class="statusMessage" id="warning">
                   <h6>&nbsp;</h6>

                   <p><webapps:pageText key="Warningbefore"/>
                    <img src="/shell/common-rsrc/images/alert_sm.gif" width="16" height="16" border="0" align="absmiddle">
                    <webapps:pageText key="Warningafter"/></p>
                </div>
                </logic:present>

    <div class="pageInfo">
      <table cellspacing="0" cellpadding="2" border="0">
        <tr>
          <td valign="top"><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>
          <td><webapps:pageText key="IntroShort"/></td>
        </tr>
      </table>
    </div>
    <%-- Errors Display --%>
    <table style="width:100%;" border="0" cellspacing="0" cellpadding="0">
    <%@ include file="/includes/usererrors.jsp" %>
    </table>

    <div class="itemStatus">
      <table cellspacing="0" cellpadding="3" border="0">
        <tr>
            <td valign="top"><logic:present name="session_copy" ><webapps:pageText key="copyfrom" type="colhdr" shared="true"/></logic:present><logic:notPresent name="session_copy" ><webapps:pageText key="targets" type="colhdr" shared="true"/></logic:notPresent>: </td>		    <logic:iterate id="target" name="session_dist" property="targets">
		      <td align="left">
         	 <bean:define id="ID" name="target" property="id" toScope="request"/>
             <bean:define id="Name" name="target" property="name" toScope="request"/>
             <bean:define id="Type" name="target" property="type" toScope="request"/>
             <jsp:include page="/includes/target_display_single.jsp"/>
	
		      </td>
		    </logic:iterate>
        </tr>
      </table>
            <logic:present name="session_copy" >
              <table cellspacing="0" cellpadding="3" border="0">
              <tr>
                  <td valign="top" align="right">&nbsp;&nbsp;&nbsp;&nbsp;<webapps:pageText key="copyto" type="colhdr" shared="true"/>: </td>
                    <jsp:include page="/copy/copy_target_display.jsp" />
              </tr>
              </table>
            </logic:present>
    </div>

    <logic:present name="copy_preview">
        <div valign="top" align="left" style="margin-bottom:8px; " class="tableTitle"><webapps:pageText key="Heading" /></div>
    </logic:present>

        <logic:notPresent name="copy_preview">
            <webapps:formtabs tabset="distPreview" tab="adv" />
        </logic:notPresent>
        <logic:present name="copy_preview">
            <webapps:formtabs tabset="copyPreview" tab="adv" />
        </logic:present>
    <div class="formContent" id="mainSection" style="overflow:auto; text-align:left">
      <table width="90%" border="0" cellspacing="0" cellpadding="3">
            <tr>
              <td class="previewCol1header"><webapps:pageText key="TunerPackageProperties"/></td>
              <td class="previewCol2">&nbsp;</td>
            </tr>
            <logic:present name="tpropskeys">
            <logic:iterate name="tpropskeys" id="key">
            <tr>
                <td class="previewCol1"><webapps:pageText key="Property"/></td>
                <td class="previewCol2">
                    <bean:write name="key" />=<%=((Subscription)sub).getProperty("",(String)key)%>
                </td>
           </tr>
           </logic:iterate>
           </logic:present>
           <logic:present name="spropskeys">
           <logic:iterate name="spropskeys" id="key" indexId="propsidx">
           <tr>
                <td class="previewCol1"><webapps:pageText key="Property"/></td>
                <td class="previewCol2">
                    <bean:write name="key" />,service=<%=((Subscription)sub).getProperty("service",(String)key)%>
                </td>
           </tr>
           </logic:iterate>
           </logic:present>
           <logic:present name="chpropskeys">
           <logic:iterate name="chpropskeys" id="key" indexId="propsidx">
           <tr>
               <td class="previewCol1"><webapps:pageText key="Property"/></td>
               <td class="previewCol2">
                <bean:write name="key" />,subscribers=<%=((Subscription)sub).getProperty("subscribers",(String)key)%>
                </td>
           </tr>
           </logic:iterate>
           </logic:present>
           <logic:present name="allchspropskeys">
           <logic:iterate name="allchspropskeys" id="key" indexId="propsidx">
           <tr>
                <td class="previewCol1"><webapps:pageText key="Property"/></td>
                <td class="previewCol2">
                <bean:write name="key" />,*=<%=((Subscription)sub).getProperty("*",(String)key)%>
                </td>
           </tr>
           </logic:iterate>
           </logic:present>
           <logic:present name="crspropskeys">
           <logic:iterate name="crspropskeys" id="key" indexId="propsidx">
           <tr>
                <td class="previewCol1"><webapps:pageText key="Property"/></td>
                <td class="previewCol2">
                <bean:write name="key" />,crs=<%=((Subscription)sub).getProperty("crs",(String)key)%>
                </td>
           </tr>
           </logic:iterate>
           </logic:present>
           <logic:present name="sub" property="channels">
           <logic:iterate name="sub" property="channels" id="channel" type="com.marimba.apps.subscription.common.objects.Channel">
           <logic:iterate name="channel" property="propertyKeys" id="key">
           <tr>
                <td class="previewCol1"><webapps:pageText key="Property"/></td>
                <td class="previewCol2">
                <bean:write name="key"/>,<bean:write name="channel" property="url"/>=<%=((Channel)channel).getProperty((String)key) %>
                </td>
           </tr>
           </logic:iterate>
           </logic:iterate>
           </logic:present>
           <logic:present name="sub" property="dummyChannels">
           <logic:iterate name="sub" property="dummyChannels" id="channel" type="com.marimba.apps.subscription.common.objects.Channel"><logic:iterate name="channel" property="propertyKeys" id="key">
           <tr>
                <td class="previewCol1"><webapps:pageText key="Property"/></td>
                <td class="previewCol2">
                    <bean:write name="key"/>,<bean:write name="channel" property="url"/>=<%=((Channel)channel).getProperty((String)key) %>
                </td>
           </tr>
           </logic:iterate>
           </logic:iterate>
           </logic:present>
            <tr>
              <td class="previewCol1">&nbsp;</td>
              <td class="previewCol2">&nbsp;</td>
            </tr>
            <tr>
              <td class="previewCol1header"><webapps:pageText key="TransmitterPermissions"/></td>
              <td class="previewCol2">&nbsp;</td>
            </tr>
            <logic:present  name="session_tloginbean" >
            <logic:iterate id="trans" name="session_tloginbean" property="transmitters" indexId="iteridx">
			    <tr>
                    <td class="previewCol1"><webapps:pageText key="Transmitter"/></td>
                    <td class="previewCol2"><bean:write name="trans" /></td>
                </tr>
                <tr>
                    <td class="previewCol1"><webapps:pageText key="Username"/></td>
                    <td class="previewCol2"><%= tloginbean.getUser((String)trans) %></td>
                </tr>
            <tr>
              <td class="previewCol1">&nbsp;</td>
              <td class="previewCol2">&nbsp;</td>
            </tr>
            </logic:iterate>
            </logic:present>
          </table>
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
          <logic:present name="copy_preview" >
                <input name="copy" type="button" class="mainBtn" accesskey="N" onClick="javascript:send(document.forms.tunerProfileForm, '/copySaveTarget.do');" value="<webapps:pageText key="copy" type="button" shared="true"/>">
                &nbsp;
                <input name="Submit32" type="button" onClick="javascript:send(document.forms.tunerProfileForm,'/copyAdd.do?action=back');" value="<webapps:pageText key="backToEdit" type="button" shared="true"/>">
                &nbsp;
                <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/copyCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
          </logic:present>
          <logic:notPresent name="copy_preview">

      <input name="save" type="submit" class="mainBtn" accesskey="N" onClick="javascript:send(document.forms.tunerProfileForm, '/distSave.do');" value="<webapps:pageText key="save" type="button" shared="true"/>">
      &nbsp;
      <input name="Submit32" type="submit" onClick="javascript:send(document.forms.tunerProfileForm, '/distInit.do?action=back');" value="<webapps:pageText key="backToEdit" type="button" shared="true"/>" />
      &nbsp;
      <logic:present name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/arDistCancel.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:present>
      <logic:notPresent name="taskid">
        <input name="Cancel" type="button" onclick="javascript:conditionalRedirect('<webapps:pageText key="cancelconfirmation" type="global" shared="true" escape="js"/>', '/returnToOrigin.do');" value="<webapps:pageText key="cancel" type="button" shared="true"/>">
      </logic:notPresent>
      </logic:notPresent>
    </div>
    </div>
  <!--end supder div for padding-->
</div>

</html:form>

<script>
CMSOnResizeHandler.addHandler("resizeDataSection('mainSection','pageNav');");
resizeDataSection('mainSection','pageNav');
</script>

</body>
</html>


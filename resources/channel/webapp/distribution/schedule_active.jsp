<%-- Copyright 2001-2002, Marimba Inc. All Rights Reserved.

     Confidential and Proprietary Information of Marimba, Inc.

     @(#)schedule_active.jsp



     
@author Theen-Theen Tan

     @version 1.11, 04/17/2002

--%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<%
    String activeType = request.getParameter("activeType");
    boolean showRadio = new Boolean(request.getParameter("showRadio")).booleanValue();
    String type = request.getParameter("type");
%>
<bean:define id="schedForm" name="scheduleEditForm" scope="request" toScope="request" type="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm"/>

	<html:hidden property="value(SET_SCHEDULE)" value="true" />
	<html:hidden property='<%= "value(ACTIVE_PERIOD_" + activeType + "_DATE)" %>' />



    <tr>

      <td valign="top" align="right">
        <% if (!"postpone".equals(type)) { %>
            <webapps:pageText shared="true" type="schedule" key='<%=activeType + "Title" %>' />:
        <% } %>
	  </td>

      <td colspan="3">

        <table width="100%" border="0" cellspacing="0" cellpadding="3">

          <tr valign="middle">

            <td colspan="2">
		      <% if (showRadio) {
                    if (!"postpone".equals(schedForm.getType())) { %>
                        <html:radio property='<%= "value(ACTIVE_PERIOD_" + activeType +")" %>' value="false"
		                onclick='<%= "setActiveDisabled(\'END\', true),setActiveDateTimeDisabled(\'" + activeType + "\', true) " %>'/>
                    <%} else { %>
                        <html:radio property='<%= "value(ACTIVE_PERIOD_" + activeType +")" %>' value="false"
		                onclick='<%= "setActiveDateTimeDisabled(\'" + activeType + "\', true) " %>'/>
                    <% }
                    if ("postpone".equals(schedForm.getType())) { %>
                        <webapps:pageText shared="true" type="schedule" key='noPostponeSched' />
		        <% } else if (!"secondary".equals(schedForm.getType()) || "END".equals(activeType)) { %>
		          <webapps:pageText shared="true" type="schedule" key='<%= activeType + "NoSched" %>' />
		         <% } else { %>
		          <webapps:pageText shared="true" type="schedule" key="STARTNoSecSched" />

		        <% } %>

		      <% } %>

		    </td>

          </tr>

          <tr valign="middle">

            <td>

		      <% if (showRadio) {
                    if (!"postpone".equals(type)) { %>
                        <html:radio property='<%= "value(ACTIVE_PERIOD_" + activeType +")" %>' value="true"
                        onclick='<%= "setActiveDisabled(\'END\', true), initDisabled() " %>' />
                    <% } else { %>
                        <html:radio property='<%= "value(ACTIVE_PERIOD_" + activeType +")" %>' value="true"
                        onclick='<%= "setActiveDisabled(\'START\', true), initDisabled() " %>' />
                    <% } %>
		      <% } else { %>
                    <html:hidden property='<%= "value(ACTIVE_PERIOD_" + activeType +")" %>' value="true" />
                    &nbsp;&nbsp;&nbsp;&nbsp;
		      <% }
                 if ("postpone".equals(type)) {%>
                    <webapps:pageText shared="true" type="schedule" key='postponeSched'/>
                 <%} else {%>
                 <webapps:pageText shared="true" type="schedule" key='<%=activeType + "OnDate" %>' />
                <% } %>
                      <webapps:errorsPresent property="<%= "ACTIVE_PERIOD_" + activeType + "_DATETIME"%>">
	                <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                      </webapps:errorsPresent>
   		      <html:text property="<%= "value(ACTIVE_PERIOD_" + activeType + "_DATETIME)"%>"/>
                      <span class="title">
                            <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return overlib('<webapps:pageText shared="true" type="global" key="example" escape="js"/><webapps:stringescape><webapps:datetime locale="request" dateStyle="<%=java.text.DateFormat.SHORT%>" timeStyle="<%=java.text.DateFormat.SHORT%>" view="sample" /></webapps:stringescape>', WIDTH, '150', DELAY, '200');" onmouseout="return nd();">
                                <webapps:datetime locale="request" dateStyle="<%=java.text.DateFormat.SHORT%>" timeStyle="<%=java.text.DateFormat.SHORT%>" view="pattern" />
                            </a>
                       </span>
        </td>

      </tr>


   </table>

  </td>

</tr>


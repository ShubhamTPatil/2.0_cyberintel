<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.

     Confidential and Proprietary Information of Marimba, Inc.

     @(#)schedule_header.jsp



     @author Theen-Theen Tan

     @version 1.9, 09/12/2002

--%>



<%@ include file="/includes/directives.jsp" %>

<%@ page import="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm" %>

<% ScheduleEditForm schedForm = (ScheduleEditForm) session.getAttribute("scheduleEditForm");

   boolean hasUpdate = !"initial".equals(schedForm.getType()) && !"secondary".equals(schedForm.getType());

   boolean hasActive = true;

   boolean isService = false;

   boolean showRadio = true;

%>



<%@ include file="/includes/banner.jsp" %>



<%-- Javascript --%>

<%@ include file="/distribution/schedule.js" %>



<html:form name="scheduleEditForm" action="/distAsgSchedSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm" />



<%-- Body content --%>

<table width="100%" border="0" cellspacing="0" cellpadding="5" class="generalText" />

  <tr>

    <td>

      <table width="100%" border="0" cellspacing="0" cellpadding="0">

      <tr>

        <td><font class="pageTitle"><webapps:pageText shared="true" type="schedule" key='<%= schedForm.getType() + "PageTitle" %>' /> </font>

        </td>

	    <td align="right">

	      <input type="submit" id="OK" value=" <webapps:pageText key="OK" type="global" /> ">

	      &nbsp;

	      <input type="button" name="cancel" value=" <webapps:pageText key="Cancel" type="global" /> " onclick="javascript:send(document.scheduleEditForm, '/distAsgSchedCancel.do');" >

	    </td>

      </tr>

      </table>

    </td>

  </tr>



  <%-- Errors Display --%>

  <tr><td>

  <%@ include file="/includes/usererrors.jsp" %>

  </td></tr>

  <bean:define id="tgDisplayList" name="session_dist" property="targets" />

  <%@ include file="/includes/target_display_comma.jsp" %>


<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)schedule.jsp

     @author Theen-Theen Tan
     @version 1.12, 09/12/2002
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm" %>
<% //ScheduleEditForm schedForm = (ScheduleEditForm) session.getAttribute("scheduleEditForm"); %>
<bean:define id="scheduleEditForm" name="scheduleEditForm" scope="session" toScope="request" type="com.marimba.apps.subscriptionmanager.webapp.forms.ScheduleEditForm" />

<%@ include file="/includes/startHeadSection.jsp" %>
   <logic:equal name="scheduleEditForm" property="type" value="initial">
        <webapps:helpContext context="spm" topic="1_sched" />
    </logic:equal>
    <logic:equal name="scheduleEditForm" property="type" value="secondary">
        <webapps:helpContext context="spm" topic="2_sched" />
    </logic:equal>
    <logic:equal name="scheduleEditForm" property="type" value="update">
        <webapps:helpContext context="spm" topic="up_sched" />
    </logic:equal>
    <logic:equal name="scheduleEditForm" property="type" value="verrepair">
        <webapps:helpContext context="spm" topic="vr_sched" />
    </logic:equal>
    <logic:equal name="scheduleEditForm" property="type" value="postpone">
        <webapps:helpContext context="spm" topic="vr_sched" />
    </logic:equal>
<%@ include file="/includes/endHeadSection.jsp" %>

<%-- change the help above to use the right keys if these keys change in the future --%>

<logic:equal name="scheduleEditForm" property="type" value="initial">
  <logic:forward name="schedule_onetime" />
</logic:equal>

<logic:equal name="scheduleEditForm" property="type" value="secondary">
  <logic:forward name="schedule_onetime" />
</logic:equal>

<logic:equal name="scheduleEditForm" property="type" value="update">
  <logic:forward name="schedule_recurring" />
</logic:equal>

<logic:equal name="scheduleEditForm" property="type" value="verrepair">
  <logic:forward name="schedule_recurring" />
</logic:equal>

<logic:equal name="scheduleEditForm" property="type" value="postpone">
  <logic:forward name="schedule_onetime" />
</logic:equal>

</body>
</html:html>
<%-- don't use footer or copyright, since this is just a page that forwards requests --%>

<%-- Copyright 2002, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)target_display_comma.jsp

     @author Michele Lin     
     @author Theen-Theen Tan
     @version 1.1, 03/27/2002
--%>

<%--
     Display a comma delimited list of
     For Target(s) : <target type icon> <target name>
     Excpects a bean by the name of "tgDisplayList" that is holds a list of targets
     objects.
     Used by blackout, install priority, tuner/ch property,
     service schedule, tx login, schedule pages under distribution assignment pages
--%>
  <tr> 
    <td><b><font class="generalText"><webapps:pageText type="global" key="forTarget" />: </font></b>
    <logic:iterate id="target" name="tgDisplayList" type="com.marimba.apps.subscription.common.objects.Target" indexId="idx">
      <logic:notEqual name="idx" value="0">      
      ,
      </logic:notEqual>
      <% //String tgLabel="target"; %>
             <bean:define id="ID" name="target" property="id" toScope="request"/>
             <bean:define id="Name" name="target" property="name" toScope="request"/>
             <bean:define id="Type" name="target" property="type" toScope="request"/>
             <jsp:include page="/includes/target_display_single.jsp"/>
    </logic:iterate>
    </td>
  </tr>

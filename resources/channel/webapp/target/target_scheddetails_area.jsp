<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)target_scheddetails_area.jsp

     @author Angela Saval
     @version 1.5, 01/30/2002
--%>
<%-- put struts-bean back so that bean:define works --%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<bean:define id="targetsched" value="defined" toScope="request" />
<webapps:helpContext context="sm" topic="targ_det" />
<jsp:include page="/target/target_details_area.jsp"/>


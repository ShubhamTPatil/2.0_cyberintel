<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)target_scheddetails_area_m.jsp

     @author Angela Saval
     @version 1.6, 02/05/2002
--%>
<%-- put struts-bean back so that bean:define works --%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<bean:define id="targetsched" value="defined" toScope="request" />
<jsp:include page="/target/target_details_area_m.jsp"/>

<%@ page contentType="text/html;charset=UTF-8" %>
<%--

 Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)package_scheddetails_area.jsp

     @author Rahul Ravulur
     @version 1.0, 01/21/2002
--%>
<%-- put struts-bean back so that bean:define works --%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<bean:define id="packagesched" value="defined" toScope="request" />
<jsp:include page="/pkg/package_m_details_area.jsp" />


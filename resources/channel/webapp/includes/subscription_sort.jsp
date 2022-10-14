<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>

<% //String order = request.getParameter("order"); %>
<bean:define id="order" name="order" scope="request" />


<a class="columnHeading" href="/spm/subscriptionSort.do?doaction=sort&order=<%=(order.equals("0")?1:0)%>&sortobject=<%=IWebAppConstants.SESSION_PC_COMPSTATUS%>&sortcolumn=name">Name</a> <img src="/shell/common-rsrc/images/<%=(order.equals("0")?"sort_up.gif":"sort_down.gif")%>" width="7" height="6">
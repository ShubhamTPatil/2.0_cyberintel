<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/includes/directives.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean,
                 java.util.ArrayList"%>

<%
    int count = 0;
    DistributionBean distbean = (DistributionBean) session.getAttribute("session_copy");
    ArrayList list = distbean.getTargets();
%>

<logic:iterate id="target" name="session_copy" property="targets">
    <td nowrap valign="top" align="left" >
        <bean:define id="name" name="target" property="name" />
        <logic:present name="<%=(String)name%>" scope="session" >
            <logic:equal name="<%=(String)name%>" value="true" >
                <img src='/shell/common-rsrc/images/alert_sm.gif' width="16" height="16" >
            </logic:equal>
        </logic:present>
        <bean:define id="type" name="target" property="type" />
        <a href="javascript:void(0);" style="cursor:help;" class="noUnderlineLink" onmouseover="return Tip('<webapps:stringescape> <bean:write name="target" property="id" filter="false" /> </webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();" >
            <logic:equal name="target" property="type" value="machinegroup" >
                <img src='/shell/common-rsrc/images/user_group.gif' border="0" align="absmiddle" />
            </logic:equal>
            <logic:notEqual name="target" property="type" value="machinegroup" >
	            <logic:equal name="target" property="type" value="site">
	                <img src='<%= "/shell/common-rsrc/images/network.gif" %>' border="0" align="absmiddle" />
	            </logic:equal>
	            <logic:notEqual name="target" property="type" value="site" >
	                <img src='<%= "/shell/common-rsrc/images/" + type.toString() + ".gif" %>' border="0" align="absmiddle" />
	            </logic:notEqual>
	        </logic:notEqual>
            <bean:write name="target" property="name" />
        </a>
    <%
        count++;
        int flag = count % 5;
        if(count < list.size() && flag != 0) {
    %>
        ,&nbsp;</td>
    <%} else {%>
        </td>
    <%  }
        if( flag == 0) {
    %>
        </tr><tr><td></td>
    <%}%>
</logic:iterate>
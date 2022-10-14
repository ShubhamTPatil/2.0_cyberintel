<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>

    <logic:present name="links" scope="session">
    <table border="0">
      <tr valign="top"> 
        <td align="right" nowrap>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
      </tr>
      <logic:iterate id="link" name="links">
      <tr valign="top"> 
        <td align="right" nowrap><strong><a href="<%=request.getContextPath() %>/<bean:write name="link" property="href"/>"><bean:write name="link" property="name"/></a></strong></td>
        <td>&nbsp;</td>
        <td> <div><bean:write name="link" property="description"/></div></td>
      </tr>
      <tr valign="top"> 
        <td align="right" nowrap>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
      </tr>
      </logic:iterate>
    </table>
    </logic:present>

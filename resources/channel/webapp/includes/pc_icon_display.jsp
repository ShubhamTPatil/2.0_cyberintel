<%-- Copyright 2002, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)target_display_comma.jsp

     @author Narasimhan L Mahendrakumar
     @version 1.1, 09/22/2003
--%>
<%--
     Displays <target type icon>
     Expects the a bean variable call "beanName"from where it can access the property "propertyName"
     Used wherever we have a target object that needs to be displayed.
--%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<logic:equal name="Type" value="domain">
	<html:img page="/images/domain.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="container">
	<img src="/shell/common-rsrc/images/folder.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="usergroup">
	<img src="/shell/common-rsrc/images/user_group.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="user">
	<img src="/shell/common-rsrc/images/user.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="machinegroup">
	<img src="/shell/common-rsrc/images/user_group.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="machine">
	<img src="/shell/common-rsrc/images/machine.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="collection">
	<img src="/shell/common-rsrc/images/collections.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="external">
	<img src="/shell/common-rsrc/images/external_target.gif" border="0" align="absmiddle" />
</logic:equal>
<logic:equal name="Type"  value="all">
	<img src="/shell/common-rsrc/images/all.gif" border="0" align="absmiddle" />
</logic:equal>

<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author Michele Lin
    @author Rahul Ravulur
    @author Theen-Theen Tan
	@version  $Revision$,  $Date$
    @since 1.5, 12/13/2002
--%>
<%--
     Displays <target type icon> <target name> with a roll over that shows the
     target's ID.
     Expects the a page variable call "tgLabel" that stores the name of the
     target object.
     Used wherever we have a target object that needs to be displayed.
--%>
<%@ include file="/includes/directives.jsp" %>
<table cellspacing="0" cellpadding="0" border="0">    
    <tr>
        <td><a class="noUnderlineLink" style="cursor:help; " onmouseover="MakeTip('<webapps:stringescape><bean:write name="ID" filter="false" /></webapps:stringescape>');" onmouseout="CloseTip();" <%@ include file="/includes/target_display_check.jsp" %></td>
        <td style="padding-left:3px; "><a href="javascript:void(0);" style="cursor:help;" class="noUnderlineLink" onmouseover="MakeTip('<webapps:stringescape><bean:write name="ID" filter="false" /></webapps:stringescape>');" onmouseout="CloseTip();" ><bean:write name="Name" /></a></td>
    </tr>
</table>
<%--Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$
--%>
<%--
     Displays <target type icon> <target name> with a roll over that shows the
     target's ID.
     Expects the a page variable call "tgLabel" that stores the name of the
     target object.
     Used wherever we have a target object that needs to be displayed.
     
     @author Selvaraj Jegatheesan
     @version 4, 2009/03/12
--%>
<%@ include file="/includes/directives.jsp" %>

<div style="text-align:left;">
    <table style="width:100%" cellpadding="0" cellspacing="0">
        <% int targetCount = 0; %>
        <tbody id='targets-0'>
            <tr><td><a href="javascript:toggleSection('targets-0')"><img border="0" id="widget-targets-0" src="/shell/common-rsrc/images/list_arrow_c.gif" width="11" height="11" class="widget"></a>
            <webapps:pageText key="targets" type="colhdr" shared="true"/>
            <logic:iterate id="target" name="session_dist" property="targets">
                <% targetCount++;%>
            </logic:iterate>
            :<%= targetCount%></td>
            </tr>
        </tbody>
    <% int targetClmCount = 0; %>

    <tbody id='targets-0_1' style='display:none; overflow:auto;'>
        <tr>
            <td align="left" style="padding-left:15px;padding-right:18px; ">
                <table style="width:100px; table-layout:fixed;" cellspacing="3" cellpadding="0">
                    <p style="text-align:justify;">

                        <tr style="width:100px;">
                            <td style="width:160px;">&nbsp;</td>
                            <td style="width:160px;">&nbsp;</td>
                            <td style="width:160px;">&nbsp;</td>
                            <td style="width:160px;">&nbsp;</td>
                            <td style="width:160px;">&nbsp;</td>
                            <td style="width:160px;">&nbsp;</td>
                        </tr>

                        <tr>
                            <logic:iterate id="target" name="session_dist" property="targets">

                                <bean:define id="ID" name="target" property="id" toScope="request"/>
                                <bean:define id="Name" name="target" property="name" toScope="request"/>
                                <bean:define id="Type" name="target" property="type" toScope="request"/>
                                <bean:define id="targetname" name="target" property="name" toScope="request"/>
                                <%
                                    if (targetClmCount >= 6) {
                                        targetClmCount = 0;
                                        %></tr><tr><%
                                    }
                                    targetClmCount++;
                                %>
                                <%
                                    int targetLength = targetname.toString().length();

                                    if (targetLength > 25 && targetLength <= 55) {
                                        targetClmCount++;
                                        if (targetClmCount >= 7 ) {
                                            targetClmCount = 0;
                                            targetClmCount+=2;
                                            %></tr><tr><%
                                        }

                                        %><td colspan="2" nowrap><%
                                    } else if (targetLength > 55) {
                                        targetClmCount+=2;
                                        if (targetClmCount >= 8 ) {
                                            targetClmCount = 0;
                                            targetClmCount+=3;
                                            %></tr><tr><%
                                        }
                                        %><td colspan="3" nowrap><%
                                    } else {
                                            %><td nowrap><%

                                    }
                                %>
                                <a class="noUnderlineLink" style="cursor:help; " onmouseover="return Tip(wrapDN('<webapps:stringescape><bean:write name="ID" filter="false" /></webapps:stringescape>', 200),WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);" onmouseout="return UnTip();"<%@ include file="/includes/target_display_check.jsp" %><bean:write name="Name" />
                                </td>
                            </logic:iterate>
                        </tr><tr><td>&nbsp;</td></tr></p>
                    </table>
                </td>
            </tr>
        </tbody>
   </table>                                      
</div>

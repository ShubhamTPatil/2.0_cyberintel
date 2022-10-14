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
     @version 4, 2009/03/13
--%>
<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/common_js.jsp" %>

<head>
    <div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
    <link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css" />
    <script language="JavaScript" src="/shell/common-rsrc/js/table.js" ></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/master.js" ></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/overlib.js"></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/intersect.js" ></script>
</head>

<script type="text/javascript">
function checkSingle(thisform) {

    var selectedTarget = 0, checkTargetId = 0;

    for(var count=0;count<thisform.elements.length;count++) {
	    if(thisform.elements[count].name == 'targetid') {
		    checkTargetId+=1;
			    if(thisform.elements[count].checked) {
				    thisform.remove.disabled = false;
                    thisform.selectAll.value = '<webapps:pageText key="unSelectAll" type="colhdr" shared="true"/>';
                } else {
                    selectedTarget+=1;
                    thisform.remove.disabled = true;
                    thisform.selectAll.value = '<webapps:pageText key="selectAll" type="colhdr" shared="true"/>';
                }
        }
    }

    if(checkTargetId == selectedTarget) {
	    thisform.remove.disabled = true;
        thisform.selectAll.value = '<webapps:pageText key="selectAll" type="colhdr" shared="true"/>';
    }
    
    if((checkTargetId != selectedTarget)&&(checkTargetId >selectedTarget)) {
        thisform.selectAll.value = '<webapps:pageText key="unSelectAll" type="colhdr" shared="true"/>';
        thisform.remove.disabled=false;
	}

}
function checkMultiple(thisform) {
    for(var count=0;count<thisform.elements.length;count++) {
	    if(thisform.elements[count].name == 'selectAll') {
            if(thisform.elements[count].value == "SelectAll") {
			    thisform.elements[count].value = '<webapps:pageText key="unSelectAll" type="colhdr" shared="true"/>';
                for(var targetCount=0;targetCount<thisform.elements.length;targetCount++) {
				    if(thisform.elements[targetCount].name=='targetid') {
					    thisform.elements[targetCount].checked=true;
					}
				}
				thisform.remove.disabled=false;
			} else {
            for(var idCount=0;idCount<thisform.elements.length;idCount++) {
                if(thisform.elements[idCount].name=='targetid') {
                    thisform.elements[idCount].checked=false;
                }
            }
            thisform.remove.disabled=true;
            thisform.elements[count].value = '<webapps:pageText key="selectAll" type="colhdr" shared="true"/>';
       }
       }
    }
}
    function send(form, submitaction) {
        var fullpath = "<webapps:fullPath path='" + submitaction + "' />";
        form.action = fullpath;
       <%-- saveAutoComplete(form); --%>
        form.submit();
  }

</script>
<html:form name="targetDetailsViewForm" action="/clearList.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TargetDetailsViewForm">
    <div style="padding-left:10px; padding-right:10px;padding-top:10px; padding-bottom:10px;">
    <div class="formTop">
            <table style="width:600px" cellpadding="0" cellspacing="0">
            <tr>
              <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_top_lft.gif"></td>
              <td style="border-top:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
              <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_top_rt.gif"></td>
            </tr>
            </table>
    </div>
    <div class="formContent" id="mainSection">
    <div class="pageHeader"><span class="title"><webapps:pageText key="selected_targets" type="colhdr" shared="true"/></span></div>
    <div style="width: 100%;overflow: hidden;">
          <table border="0" cellspacing="0" cellpadding="0" style="width:100%">
                <tr>
                    <bean:size id="targetSize" name="targetDetailsViewForm" property="targetsList"/>
                    <%
                        String selectedTgtSize = targetSize.toString();
                        int tgtSize = Integer.parseInt(selectedTgtSize);
                        if (tgtSize != 0) {
                    %>
                    <td align="left" class="tableRowActions">
                        &nbsp;&nbsp;<b><bean:write name="targetSize"/>&nbsp;<webapps:pageText key="selectedTargets" type="colhdr" shared="true"/></b>
                    </td>
                    <td align="right" style="padding-right:5px;" class="tableRowActions">
                        <input type="button" name="selectAll" onClick="checkMultiple(targetDetailsViewForm);" value="<webapps:pageText key="selectAll" type="colhdr" shared="true"/>">
                    </td>
                    <%
                        } else {
                    %>
                    <td align="left" class="tableRowActions">
                    </td>
                    <%
                        }
                    %>
                </tr>
             </table>
        </div>
        <div class="headerSection" style="width:100%; text-align:left;" id="FOO_headerDiv">
          <table border="0" cellspacing="0" cellpadding="0" id="FOO_headerTable" style="width:100%">
                <colgroup width="8%"></colgroup>
                <colgroup width="84%"></colgroup>
                <colgroup width="8%"></colgroup>
                <thead>
                    <tr id="FOO_headerTable_firstRow">
                        <td nowrap class="tableHeaderCell" width="8%"><webapps:pageText key="contentType" type="colhdr" shared="true"/></td>
                        <td nowrap class="tableHeaderCell" width="84%"><webapps:pageText key="multiTargets" type="colhdr" shared="true"/></td>
                        <td nowrap class="tableHeaderCell" width="8%"><webapps:pageText key="select" type="colhdr" shared="true"/></td>
                    </tr>
                </thead>
          </table>
        </div>
        <div id="dataSection" style="overflow:auto; width:580px;height:350px;">
            <%
                if (tgtSize != 0) {
            %>
         <table border="0" cellspacing="0" cellpadding="0" style="width:100%;">
            <% int folderContentsRowCount = 0; %>
             <tr>
                 <td></td><td></td>
             </tr>
             <colgroup width="8%"></colgroup>
             <colgroup width="84%"></colgroup>
             <colgroup width="8%"></colgroup>
                <logic:iterate id="target" name="targetDetailsViewForm" property="targetsList" indexId="tgidx">
                    <% if (folderContentsRowCount == 0){ %>
						  <tr BGCOLOR="#FFFFFF">
                        <% } else if (folderContentsRowCount % 2 == 0){ %>
                          <tr BGCOLOR="#FFFFFF">
                        <% } else { %>
	                      <tr class="alternateRowColor">
                        <% } %>
                        <bean:define id="index" value='<bean:write name="tgidx" />'/>
                        <bean:define id="ID" name="target" property="id" toScope="request"/>
                        <bean:define id="Name" name="target" property="name" toScope="request"/>
                        <bean:define id="Type" name="target" property="type" toScope="request"/>
                        <td nowrap align="center" class="rowLevel1" width="8%">
                            <a class="noUnderlineLink" style="cursor:help; " onmouseover="return overlib(wrapDN('<webapps:stringescape><bean:write name="ID" filter="false" /></webapps:stringescape>', 200), WIDTH , '50', DELAY, '200',ABOVE,LEFT);" onmouseout="return nd();"<%@ include file="/includes/target_display_check.jsp" %>
                        </td>
                        <td nowrap style="padding-left:3px;" class="rowLevel1" width="84%">
                            <a href="javascript:void(0);" style="cursor:help;" class="noUnderlineLink" onmouseover="return overlib('<webapps:stringescape><bean:write name="ID" filter="false" /></webapps:stringescape>', WIDTH, '50', DELAY, '200',ABOVE,LEFT);" onmouseout="return nd();" ><bean:write name="Name" /></a>
                        </td>
                        <td nowrap class="rowLevel1" width="8%">
                            <input type="checkbox" name="targetid" value="<bean:write name="tgidx" />" onClick="javascript:checkSingle(targetDetailsViewForm);">
                        </td>
                   </tr>
            <% folderContentsRowCount++;%>
                </logic:iterate>
        </table>
            <%
                } else {
            %>
            <table style="width: 100%;">
                <tr></tr>
                <tr>
                    <strong><font size="2"><webapps:pageText shared="true" type="target_details_area" key="NoTargetSelectedShort" /></font></strong>
                </tr>
            </table>
            <% } %>

    </div>
    </div>
    <div class="formBottom">
            <table width="100%" cellpadding="0" cellspacing="0">
            <tr>
              <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_lft.gif"></td>
              <td style="border-bottom:1px solid #CCCCCC;"><img src="/shell/common-rsrc/images/invisi_shim.gif"></td>
              <td height="5" width="5"><img src="/shell/common-rsrc/images/form_corner_bot_rt.gif"></td>
            </tr>
            </table>
        </div>
    </div>
    <%
        String requiredRemove = (String)session.getAttribute("requiredRemove");
    %>
    <input type="hidden" name="checkRemove" value="<%=requiredRemove%>" >
    <div align="right" style="padding-right:15px;">
        <%
            if (tgtSize != 0) {
        %>
             <input type="button" name="remove" DISABLED value="<webapps:pageText key="remove" type="button" shared="true"/>" onClick="javascript:send(targetDetailsViewForm,'/clearList.do?');">
        <% } %>
             &nbsp;&nbsp;<input type="button" value="<webapps:pageText key="close" type="button" shared="true"/>" onClick="javascript:parent.hideMultiTargetsArea();parent.pageRefresh(targetDetailsViewForm);">
    </div>

</html:form>
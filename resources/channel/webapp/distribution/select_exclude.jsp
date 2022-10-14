<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)select_exclude.jsp

     @author Michele Lin
     @version 1.26, 04/23/2002
--%>

<%@ include file="/includes/directives.jsp" %>

<jsp:useBean id="session_dist" class="com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean" scope="session" />

<%-- Create temp obj when page is first loaded --%>
<jsp:useBean id="add_remove_selected_page_targets" class="java.util.ArrayList" scope="session" />

<%-- Header info --%>
<%//@ include file="/includes/startheadSection.jsp" %>
<%-- Symbio Added 05/13/2005 --%>
<%@ include file="/includes/startHeadSection.jsp" %>
<%-- Body content --%>
<%@ include file="/includes/body.html" %>
<%@ include file="/includes/common_js.jsp" %>

<%-- Javascript --%>

<script language="JavaScript" type="text/javascript">
  function sendOK(actionDo)
  {
    var fullpath = "<html:rewrite page='" + actionDo + "' />";
    top.location = fullpath;
  }
  // this function checks/unchecks all rows when
// "select-all" is checked/unchecked
function setChecked(val) {
    var colForm = document.forms.targetEditForm;
    var len = colForm.elements.length;
    var i = 0;

	for (i = 0; i < len; i++) {
		if (colForm.elements[i].name == 'targets') {
			colForm.elements[i].checked = val;
	    }
    }
}

// this function does the reverse of the above function
// if all te rows are checked, "select-all" gets checked.
function setCheckedAll(val) {
    var colForm = document.forms.targetEditForm;
    var len = colForm.elements.length;
    var i = 0;
	var allChecked = true;
	var rowsPresent = false;

	for (i = 0; i < len; i++) {
		if (colForm.elements[i].name == 'targets') {
			rowsPresent = true;
			if (!colForm.elements[i].checked) {
				allChecked = false;
				break;
			}
	    }
    }

	if (rowsPresent) {
		colForm.selectAll.checked = allChecked;
	}
}
var singleOptionElements = new Array()
var multiOptionElements = new Array("edit_btn","delete_btn")
</script>


<%-- Body content --%>
<html:form name="targetEditForm" styleId="targetEditFormID"  action="/targetEdit.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.TargetEditForm">
  <html:hidden property="submittedTargetsStr" />
  <html:hidden property="forwardURL"/>
<div id="pageContent">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr width="100%">
  <td colspan="2">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <%@ include file="/includes/usererrors.jsp" %>
    </table>
  </td>
</tr>


<tr>
					<td valign="top" style="padding-top:24px; padding-left:50px;">
						  <table width="418" border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td class="tableTitle"><webapps:pageText key="Selected_Targets" /></td>
										</tr>
								</table>
						  <div class="tableWrapper" style="width:418px;">
          <table width="418" border="0" cellspacing="0" cellpadding="0">
            <tr valign="middle" class="smallButtons">
              <td class="tableRowActions">
		        <input type="button" id="delete_btn" value="<webapps:pageText key="Remove"  type="button" />" onClick="javascript:send(document.forms.targetEditFormID,'/targetEdit.do?action=remove','1');" />
              </td>
			    <td align="right" class="tableRowActions">&nbsp;</td>
            </tr>
          </table>
	   <div class="headerSection" style="width:100%;">
            <table width="400" border="0" cellpadding="0" cellspacing="0">
              <colgroup width="0*">
              </colgroup>
              <colgroup width="100%">
              </colgroup>
              <thead>
			    <tr>
				  <td class="tableHeaderCell">
                  <input type=checkbox name="selectAll" onClick="setChecked(checked)">
                  <td class="tableHeaderCell"><a class="columnHeading" href="#">Target</a></td>
                </tr>
              </thead>
            </table>
          </div>
          <!--end headerSection-->
          <div id="dataSection" style="height:150px; width:100%; overflow:auto;">
            <table width="400" cellpadding="0" cellspacing="0">
              <colgroup width="0*">
              </colgroup>
              <colgroup width="100%">
              </colgroup>
              <% int contentsRowCount = 0; %>
		      <logic:iterate id="app" name="add_remove_selected_page_targets" indexId="iteridx" type="com.marimba.apps.subscription.common.objects.Target">
               <% if (contentsRowCount % 2 == 0){ %>
               <tr>
               <% } else { %>
	           <tr class="alternateRowColor">
               <% } %>
                   <td class="rowLevel1">
                   	<html:multibox styleId='<%= "box_"+ iteridx %>' property="targets" onclick="setCheckedAll()" >
				            <bean:write name="app" property="id"/>
			        </html:multibox>
                	</td>
                	<td class="rowLevel1">
                        <img src='<%="/shell/common-rsrc/images/" + app.getType() +".gif" %>' width="16" height="16"/>
                        <bean:write name="app" property="name" filter="true" /></td>
              		</tr>
                 <% contentsRowCount++;%>
                 </logic:iterate>
		        </table>
          </div><!--end dataSection-->
        </div><!--end tableWrapper -->
        <div id="pageNav" style="width:418px;">
			<input type="button" name="Submit" value=" OK " onClick="javascript:sendOK('/targetEdit.do?action=ok');" class="mainBtn" >
			&nbsp;
			<input type="button" name="Submit" value=" Cancel " onClick="javascript:sendOK('/targetEdit.do?action=cancel');">
        </div>
    </td>
  </tr>
</table>
</div>
</html:form>

<%-- do not use /includes/footer.jsp here because we don't want
     the copyright statement in the right-hand pane --%>
</body>
</html:html>

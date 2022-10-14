<%--
    Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, and 6,430,608. Other Patents Pending.

    $File://depot/ws/products/subscriptionmanager/9.0.01/resources/channel/webapp/target/desktop_security_blocked_software.jsp

    @author author: Venkatesh Jeyaraman
--%>

<div title="List of blcoked software" style="padding:18px;">
		<div id="blockedsw_table_toolsbar" style="height:40px;">
			<div style="margin-top:6px;">
				<a style="margin-left:8px;" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-add" onclick="append();">
					<bean:message key="page.button.add"/>
				</a>
				<a style="margin-left:8px;" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-remove" onclick="removeit();">
					<bean:message key="page.button.remove"/>
				</a>
				<%--<a style="margin-left:8px;" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-save" onclick="accept('blockedsw_table');">
					Accept
				</a>
				<a style="margin-left:8px;" href="javascript:void(0)" class="easyui-linkbutton" iconCls="icon-undo" onclick="reject();">
					Reject
				</a> --%>
			</div>
		</div>

		<table id="blockedsw_table" class="easyui-datagrid"
			   data-options="singleSelect:false, toolbar: '#blockedsw_table_toolsbar', onClickCell: onClickCell,onBeforeSortColumn: onBeforeSortColumn , checkOnSelect:false,
			   selectOnCheck:true, remoteSort:false,sortName:'swname', sortOrder:'asc'" style="width: 640px; height: 248px;">
			<thead>
				<tr>
					<th data-options="field:'ck', checkbox:true">&nbsp;</th>
					<th data-options="field:'swname',sortable:true,resizable:false,editor:'text',required:true" width="610px"><webapps:pageText key="applicationName" /></th>
				</tr>
			</thead>
			<tbody>
			<logic:present name="desktopSecurityProfileForm" property="blockedSoftwareList">
				<logic:iterate id="blockedswList" name="desktopSecurityProfileForm" property="blockedSoftwareList">
					<tr><td>&nbsp;</td><td><bean:write name="blockedswList" property="name"/></td></tr>
				</logic:iterate>
			</logic:present>
			</tbody>
		</table>
	</div>

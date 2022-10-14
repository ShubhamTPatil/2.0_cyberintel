<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<table id="step1Table" width="100%" border=0>
    <tr width="100%">
        <td width="100%" style="padding-left:20px;" valign="top">
            <b><webapps:pageText key="windows.title" type="security_content_assignment" shared="true"/></b>
            <hr>
            <p><webapps:pageText key="windows.text" type="security_content_assignment" shared="true"/></p>

            
            <table id="windows_contents_data_table" width="100%" class="table table-bordered table-striped dataTable">
                <thead>
                    <th><input name="select_all" type="checkbox"></th>
                    <th style="width: 473px; vertical-align: middle;">Security Content</th>
                    <th>Content Type</th>
					<th>Assessment Type</th>
                    <th>Security Profile</th>
                    <th>View Profile</th>
                </thead>
                <tbody>
                    <logic:iterate name="scapBeansListWindows" id="usgcbList" scope="session" indexId="indexId">
                        <bean:define id="profile" name="usgcbList" property="sortedProfiles" toScope="request"/>
                        <bean:define id="selectedProfile" name="usgcbList" property="selectedProfile" toScope="request"/>
                        <tr>
                            <td>
                                <logic:equal name="usgcbList" property="selected" value="true">
                                    <input type="checkbox" id="wchk1_<bean:write name="usgcbList" property="fileName"/>" checked>
                                </logic:equal>
                                <logic:equal name="usgcbList" property="selected" value="false">
                                    <input type="checkbox" id="wchk1_<bean:write name="usgcbList" property="fileName"/>">
                                </logic:equal>
                            </td>
                            <td><bean:write name="usgcbList" property="title"/></td>
                            <td><bean:write name="usgcbList" property="type"/></td>
							<td><bean:write name="usgcbList" property="assessmentType"/></td>
                            <td>
                                <select id="wprofilekey_<%= indexId.toString() %>">
                                    <logic:iterate name="profile" id="pro_file">
                                        <bean:define id="currProfileKey" name="pro_file" property="key"/>
                                        <% if (currProfileKey.equals(selectedProfile)) { %>
                                        <option value="<bean:write name="pro_file" property="key"/>" selected="selected"><bean:write name="pro_file" property="value"/></option>
                                        <% } else {%>
                                        <option value="<bean:write name="pro_file" property="key"/>"><bean:write name="pro_file" property="value"/></option>
                                        <% } %>
                                    </logic:iterate>
                                </select>
                            </td>
                            <logic:equal name="usgcbList" property="type" value="custom">
                                <td><button type="button" class="btn btn-info" onclick="showProfileDetails('<bean:write name="usgcbList" property="fileName"/>', $('#wprofilekey_<%= indexId.toString() %>').val(), 'custom');return false;"><i class='fa fa-eye'></i>&nbsp;View</button></td>
                            </logic:equal>
                            <logic:notEqual name="usgcbList" property="type" value="custom">
                                <td><button type="button" class="btn btn-info" onclick="showProfileDetails('<bean:write name="usgcbList" property="fileName"/>', $('#wprofilekey_<%= indexId.toString() %>').val(), 'windows');return false;"><i class='fa fa-eye'></i>&nbsp;View</button></td>
                            </logic:notEqual>
                        </tr>
                    </logic:iterate>
                </tbody>
            </table>
        </td>
    </tr>
</table>

<script type="text/javascript">
    $(document).ready(function() {
        populateWindowsContentsDataTable('#windows_contents_data_table');
        persistCheckbox('#windows_contents_data_table');
    });
</script>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<table id="step2Table" width="100%" border=0>
    <tr width="100%">
        <td width="100%" style="padding-left:20px;" valign="top">
            <b><webapps:pageText key="nonwindows.title" type="security_content_assignment" shared="true"/></b>
            <hr>
            <p><webapps:pageText key="nonwindows.text" type="security_content_assignment" shared="true"/></p>
            <table id="nonwindows_contents_data_table" class="table table-bordered table-striped dataTable">
                <thead>
                    <th><input name="select_all" type="checkbox"></th>
                    <th style="width: 473px; vertical-align: middle;">Security Content</th>
                    <th>Content Type</th>
					<th>Assessment Type</th>
                    <th>Security Profile</th>
                    <th>View Profile</th>
                </thead>
                <tbody>
                    <logic:iterate name="scapBeansListNonWindows" id="scapList" scope="session" indexId="indexId">
                        <bean:define id="profile" name="scapList" property="sortedProfiles" toScope="request"/>
                        <bean:define id="selectedProfile" name="scapList" property="selectedProfile" toScope="request"/>
                        <tr>
                            <td>
                                <logic:equal name="scapList" property="selected" value="true">
                                    <input type="checkbox" id="nwchk1_<bean:write name="scapList" property="fileName"/>" checked name="chk_box">
                                </logic:equal>
                                <logic:equal name="scapList" property="selected" value="false">
                                    <input type="checkbox" id="nwchk1_<bean:write name="scapList" property="fileName"/>">
                                </logic:equal>
                            </td>
                            <td><bean:write name="scapList" property="title"/></td>
                            <td><bean:write name="scapList" property="type"/></td>
							<td><bean:write name="scapList" property="assessmentType"/></td>
                            <td>
                                <select id="nwprofilekey_<%= indexId.toString() %>">
                                    <logic:iterate name="profile" id="pro_file">
                                        <bean:define id="currProfileKey" name="pro_file" property="key"/>
                                        <% if (currProfileKey.equals(selectedProfile)) { %>
                                        <option value="<bean:write name="pro_file" property="key"/>" selected><bean:write name="pro_file" property="value"/></option>
                                        <% } else {%>
                                        <option value="<bean:write name="pro_file" property="key"/>"><bean:write name="pro_file" property="value"/></option>
                                        <% } %>
                                    </logic:iterate>
                                </select>

                            </td>
                            <logic:equal name="scapList" property="type" value="custom">
                                <td><button type="button" class="btn btn-info" onclick="showProfileDetails('<bean:write name="scapList" property="fileName"/>', $('#nwprofilekey_<%= indexId.toString() %>').val(),'custom');return false;"><i class='fa fa-eye'></i>&nbsp;View</button></td>
                            </logic:equal>
                            <logic:notEqual name="scapList" property="type" value="custom">
                                <td><button type="button" class="btn btn-info" onclick="showProfileDetails('<bean:write name="scapList" property="fileName"/>', $('#nwprofilekey_<%= indexId.toString() %>').val(),'nonwindows');return false;"><i class='fa fa-eye'></i>&nbsp;View</button></td>
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
    populateNonWindowsContentsDataTable('#nonwindows_contents_data_table');
    persistCheckbox('#nonwindows_contents_data_table');
});
</script>
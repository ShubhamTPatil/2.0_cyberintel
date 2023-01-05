<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<table id="step3Table" width="100%" border=0>
    <tr width="100%">
        <td width="100%" style="padding-left:20px;" valign="top">
            <b><webapps:pageText key="customize.title" type="security_content_assignment" shared="true"/></b>
            <hr>
            <p><webapps:pageText key="customize.text" type="security_content_assignment" shared="true"/></p>
            <table id="customize_contents_data_table"  width="100%" class="table table-bordered table-striped dataTable">
                <thead>
                    <th>Security Content</th>
                    <th>Security Profile</th>
                    <th>Content Type</th>
					<th>Assessment Type</th>
                    <th>Edit Profile</th>
                </thead>
                <tbody id="customize_contents_table_body">
                </tbody>
            </table>
            <b><webapps:pageText key="priority" type="security_content_assignment" shared="true"/></b>
            <hr>
            <p><webapps:pageText key="priority.text" type="security_content_assignment" shared="true"/></p>
            <input type="text" id="customize_priority" value="<bean:write name="securityContentAssignmentForm" property="priority"/>">
            <br><br>
            <b><webapps:pageText key="remediation" type="security_content_assignment" shared="true"/></b>
            <hr>
            <p><webapps:pageText key="remediation.text" type="security_content_assignment" shared="true"/></p>
            <input type="checkbox" <logic:equal name="securityContentAssignmentForm" property="remediateEnabled" value="true">checked</logic:equal> id="customize_remediateEnabled">&nbsp;Perform remediation
        </td>
    </tr>
</table>

<script type="text/javascript">
    function loadCustomizeContentDataTable() {
        var tbodyDetails = '';
        var selectedRows = getSelectedWindowsData(windowsContentsDataTable);
        if (selectedRows != null && selectedRows != '') {
            var rows = selectedRows.split(":_:");
            for (var i=0;i<rows.length; i++) {
                var row = rows[i];
                var contents = row.split("$-$");
                if (contents.length == 6) {
                    var contentFileName = contents[0];
                    var contentTitle = contents[1];
                    var profileId = contents[2];
                    var profileTitle = contents[3];
                    var type = contents[4];
                    var assessmentType = contents[5];
                    var targetType = 'windows';
                    if (type == 'custom') {
                        targetType = 'custom';
                    }
                    tbodyDetails = tbodyDetails + "<tr><td>"+contentTitle+"</td><td>"+profileTitle+"</td><td>Windows - " + type + "</td><td>" + assessmentType + "</td>" +
                        "<td><button type='button' class='btn btn-sm btn-primary' onClick='editProfileDetails(\""+ contentFileName +"\",\"" + profileId +"\",\"" + profileTitle +"\",\""+ targetType+"\")'><i class='fa fa-edit'></i>&nbsp;Edit</button></tr>";
                }
            }
        }

        selectedRows = getSelectedNonWindowsData(nonWindowsContentsDataTable);
        if (selectedRows != null && selectedRows != '') {
            var rows = selectedRows.split(":_:");
            for (var i=0;i<rows.length; i++) {
                var row = rows[i];
                var contents = row.split("$-$");
                if (contents.length == 6) {
                    var contentFileName = contents[0];
                    var contentTitle = contents[1];
                    var profileId = contents[2];
                    var profileTitle = contents[3];
                    var type = contents[4];
                    var assessmentType = contents[5];
                    var targetType = 'nonwindows';
                    if (type == 'custom') {
                        targetType = 'custom';
                    }

                    tbodyDetails = tbodyDetails + "<tr><td>"+contentTitle+"</td><td>"+profileTitle+"</td><td>Non Windows - " + type +"</td><td>" + assessmentType + "</td>" +
                        "<td><button type='button' class='btn btn-sm btn-primary' onClick='editProfileDetails(\""+ contentFileName +"\",\"" + profileId +"\", \"" + profileTitle +"\",\"" + targetType + "\")'><i class='fa fa-edit'></i>&nbsp;Edit</button></tr>";
                }
            }
        }
        if (customizeContentsDataTable) customizeContentsDataTable.fnDestroy();
        $("#customize_contents_table_body").html(tbodyDetails);
        customizeContentsDataTable = $("#customize_contents_data_table").dataTable( {
          "bPaginate": false
        } );
    }
</script>
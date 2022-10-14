<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<table id="step4Table" width="100%"  border=0>
    <tr width="100%">
        <td width="100%" style="padding-left:20px;" valign="top">
            <b><webapps:pageText key="preview.title" type="security_content_assignment" shared="true"/></b>
            <hr>
            <p><webapps:pageText key="preview.text" type="security_content_assignment" shared="true"/></p>
            <table id="preview_contents_data_table"  width="100%" class="table table-bordered table-striped dataTable">
                <thead>
                    <th>SCAP Content</th>
                    <th>Profile Name</th>
                    <th>Target Type</th>
                    <th>Assessment Type</th>
                    <th>Customized?</th>
                </thead>
                <tbody id="preview_contents_table_body">
                </tbody>
            </table>
            <br>
            <b>Additional Settings</b>
            <table>
                <tr class="rowNew">
                    <td>Priority:</td>
                    <td id="preview_priority"></td>
                </tr>
                <tr class="rowNew">
                    <td>Is Remediation Enabled:</td>
                    <td id="preview_remediation_enabled"></td>
                </tr>
            </table>
        </td>
    </tr>
</table>

<script type="text/javascript">
    function loadPreviewContentDataTable() {
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
                    var assessmentType = contents[5];
                    var isModified = "No";
                    if (customizedProfileStr != '') {
                        if (customizedProfileStr.indexOf(contentFileName) != -1) {
                            isModified = "Yes";
                        }
                    }
                    tbodyDetails = tbodyDetails + "<tr><td>"+contentTitle+"</td><td>"+profileTitle+"</td><td>Windows</td><td>" +assessmentType+"</td>"+
                            "<td>"+ isModified +"</td></tr>";
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
                    var assessmentType = contents[5];
                    var isModified = "No";
                    if (customizedProfileStr != '') {
                        if (customizedProfileStr.indexOf(contentFileName) != -1) {
                            isModified = "Yes";
                        }
                    }
                    tbodyDetails = tbodyDetails + "<tr><td>"+contentTitle+"</td><td>"+profileTitle+"</td><td>Non Windows</td><td>" +assessmentType+"</td>"+
                            "<td>"+ isModified +"</td></tr>";
                }
            }
        }
        if (previewContentsDataTable) previewContentsDataTable.fnDestroy();
        $("#preview_contents_table_body").html(tbodyDetails);
        previewContentsDataTable = $("#preview_contents_data_table").dataTable( {
            "bPaginate": false
        } );


        $("#preview_priority").html(getPriorityValue());
        $("#preview_remediation_enabled").html(isRemediationEnabled());

    }
</script>
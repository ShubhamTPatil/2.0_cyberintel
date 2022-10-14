<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<bean:define id="profile" name="securityProfileReportForm" property="securityProfileDetailsBean"/>
<bean:define id="ovalProfile" name="securityProfileReportForm" property="securityOvalGeneralDetailsBean"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Security Compliance Report - Vulnerability Assessment</title>

    <link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-multiselect.css"/>
    <link rel="stylesheet" type="text/css" href="/spm/css/bootstrap-dialog.min.css"/>

    <script type="text/javascript" src="/spm/js/jquery.min.js"></script>
    <script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
    <script type="text/javascript" src="/spm/js/jquery.filedownload.js"></script>
    <script type="text/javascript" src="/spm/js/bootstrap-multiselect.js"></script>
    <script type="text/javascript" src="/spm/js/application.js"></script>

    <style type="text/css">
        table {border: 1px solid #000000; width: 100%; border-spacing: 0; margin: 2px 0;}
        .noborder {border: none;}
        .nomargin {margin: 0;}
        td {padding: 0 4px 1px 4px;}
        .SmallLabel {font-family: Geneva, Arial, Helvetica, sans-serif; color: #000000; font-size: 9pt; font-weight: bold; white-space: nowrap;}
        .SmallText {font-family: Geneva, Arial, Helvetica, sans-serif; color: #000000; font-size: 9pt;}
        .Label {font-family: Geneva, Arial, Helvetica, sans-serif; color: #000000; font-size: 10pt; font-weight: bold; white-space: nowrap;}
        .TitleLabel {font-family: Geneva, Arial, Helvetica, sans-serif; color: #ffffff; font-size: 10pt; font-weight: bold; white-space: nowrap;}
        .Text {font-family: Geneva, Arial, Helvetica, sans-serif; color: #000000; font-size: 10pt;}
        .Title {color: #FFFFFF; background-color: #706c60; padding: 0 4px 1px 4px; font-size: 10pt; border-bottom: 1px solid #000000;}
        .Center {text-align: center;}

        a {color:#676c63;}
        a.Hover:hover {color:#7b0e0e; text-decoration:underline;}

        .LightRow {background-color: #FFFFFF;}
        .DarkRow {background-color: #DDDDD8;}

        .resultbadA{background-color: #FFBC8F;}
        .resultbadB{background-color: #FFE0CC;}
        .resultgoodA{background-color: #ACD685;}
        .resultgoodB{background-color: #CBE6B3;}
        .resultunknownA{background-color: #AEC8E0;}
        .resultunknownB{background-color: #DAE6F1;}
        .resulterrorA{background-color: #FFDD75;}
        .resulterrorB{background-color: #FFECB3;}
        .resultotherA{background-color: #EEEEEE;}
        .resultotherB{background-color: #FFFFFF;}

        .Classcompliance{background-color: #93C572;}
        .Classinventory{background-color: #AEC6CF;}
        .Classmiscellaneous{background-color: #9966CC;}
        .Classpatch{background-color: #FFDD75;}
        .Classvulnerability{background-color: #FF9966;}
        .ColorBox{width: 2px;}
        #result_table td{text-align: center; white-space: normal; word-break: break-all;font-family: Geneva, Arial, Helvetica, sans-serif; color: black;}
    </style>
</head>
<body id="body_content">

<nav class="navbar navbar-default" role="navigation">
    <div class="navbar-header" style="float: none;background-color:#206276">
        <div style="float: left;color:#FFFFFF;padding-left:10px;"><h2>Security Compliance Report - Vulnerability Assessment</h2></div>
        <div style="float: right;padding-right:15px;padding-top:7px;"><img width="100px" height="60px" src="/spm/images/harman_logo_blue.png"></div>
    </div>
</nav>
<div class="container" style="width: 95%;">
<div class="row pull-right"><button id="do_print" type="button" class="btn btn-primary">Download As PDF</button></div>
<div class="row"><h2 style="margin-top: 0;"><bean:write name="profile" property="contentTitle"/></h2></div>
<div class="row"><blockquote>with profile <mark><bean:write name="profile" property="profileTitle"/></mark></blockquote></div>
<div class="row"><div class="description"><bean:write name="profile" property="profileDescription"/></div></div>
<div class="row">
    <div class="alert alert-info">
        Do not attempt to implement any of the settings in this guide without first testing them in a non-operational environment. The
        creators of this guidance assume no responsibility whatsoever for its use by other parties, and makes no guarantees, expressed or implied, about its
        quality, reliability, or any other characteristic.
    </div>
</div>
<div class="row">
    <div class="col-sm-6">
        <table border="1">
            <tr class="Title"><td class="TitleLabel" colspan="5">OVAL Results Generator Information</td></tr>
            <tr class="DarkRow Center">
                <td class="SmallLabel">Product Name</td>
                <td class="SmallLabel">Module Name</td>
                <td class="SmallLabel">Product Version</td>
                <td class="SmallLabel">Date</td>
                <td class="SmallLabel">Time</td>
            </tr>
            <tr class="LightRow">
                <td class="SmallText"><bean:write name="ovalProfile" property="productName"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="moduleName"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="productVersion"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="date"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="time"/></td>
            </tr>
            <tr class="DarkRow Center">
                <td class="SmallLabel" style="width: 20%;" title="Non-Compliant/Vulnerable/Unpatched">#Fail</td>
                <td class="SmallLabel" style="width: 20%;" title="Compliant/Non-Vulnerable/Patched">#Pass</td>
                <td class="SmallLabel" style="width: 20%;" title="Error">#Error</td>
                <td class="SmallLabel" style="width: 20%;" title="Unknown">#Unknown</td>
                <td class="SmallLabel" style="width: 20%;" title="Inventory/Miscellaneous class, or Not Applicable/Not Evaluated result">#Other</td>
            </tr>
            <tr class="LightRow Center" style="height:auto;">
                <td class="SmallText resultbadB" title="Non-Compliant/Vulnerable/Unpatched" style="width:20%"><bean:write name="ovalProfile" property="totalFail"/></td>
                <td class="SmallText resultgoodB" title="Compliant/Non-Vulnerable/Patched" style="width:20%"><bean:write name="ovalProfile" property="totalPass"/></td>
                <td class="SmallText resulterrorB" title="Error" style="width:20%"><bean:write name="ovalProfile" property="totalError"/></td>
                <td class="SmallText resultunknownB" title="Unknown" style="width:20%"><bean:write name="ovalProfile" property="totalUnknown"/></td>
                <td class="SmallText resultotherB" title="Inventory/Miscellaneous class, or Not Applicable/Not Evaluated result" style="width:20%"><bean:write name="ovalProfile" property="totalOther"/></td>
            </tr>
        </table>
    </div>
    <div class="col-sm-6">
        <table border="1">
            <tr class="Title"><td class="TitleLabel" colspan="5">OVAL Definition Generator Information</td></tr>
            <tr class="DarkRow Center">
                <td class="SmallLabel">Schema Version</td>
                <td class="SmallLabel">Product Name</td>
                <td class="SmallLabel">Product Version</td>
                <td class="SmallLabel">Date</td>
                <td class="SmallLabel">Time</td>
            </tr>
            <tr class="LightRow">
                <td class="SmallText"><bean:write name="ovalProfile" property="generatorSchemaVersion"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="generatorProductName"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="generatorProductVersion"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="generatorDate"/></td>
                <td class="SmallText"><bean:write name="ovalProfile" property="generatorTime"/></td>
            </tr>
            <tr class="DarkRow Center">
                <td class="SmallLabel" style="width: 20%;">#Definitions</td>
                <td class="SmallLabel" style="width: 20%;">#Tests</td>
                <td class="SmallLabel" style="width: 20%;">#Objects</td>
                <td class="SmallLabel" style="width: 20%;">#States</td>
                <td class="SmallLabel" style="width: 20%;">#Variables</td>
            </tr>
            <tr class="LightRow Center">
                <td class="SmallText Center">
                    <bean:write name="ovalProfile" property="totalDefinitions"/> Total
                    <br/>
                    <table class="noborder">
                        <tr class="Center">
                            <td class="SmallText Classcompliance" title="compliance" style="width:20%"><bean:write name="ovalProfile" property="totalComplianceDefinitions"/></td>
                            <td class="SmallText Classinventory" title="inventory" style="width:20%"><bean:write name="ovalProfile" property="totalInventoryDefinitions"/></td>
                            <td class="SmallText Classmiscellaneous" title="miscellaneous" style="width:20%"><bean:write name="ovalProfile" property="totalMiscellaneousDefinitions"/></td>
                            <td class="SmallText Classpatch" title="patch" style="width:20%"><bean:write name="ovalProfile" property="totalPatchDefinitions"/></td>
                            <td class="SmallText Classvulnerability" title="vulnerability" style="width:20%"><bean:write name="ovalProfile" property="totalVulnerabilityDefinitions"/></td>
                        </tr>
                    </table>
                </td>
                <td class="SmallText Center"><bean:write name="ovalProfile" property="totalTests"/></td>
                <td class="SmallText Center"><bean:write name="ovalProfile" property="totalObjects"/></td>
                <td class="SmallText Center"><bean:write name="ovalProfile" property="totalStates"/></td>
                <td class="SmallText Center"><bean:write name="ovalProfile" property="totalVariables"/></td>
            </tr>
        </table>
    </div>
</div>
<div class="row"><hr/></div>
<a class="Hover" name="a_1" id="a_1" style="text-decoration:none;"></a>
<div class="row">
    <div class="col-sm-12">
        <table border="1">
            <tr class="Title"><td class="TitleLabel" colspan="2">Policy Information</td></tr>
            <tr class="DarkRow">
                <td class="Label" width="20%">Host Name</td>
                <td class="Text" width="80%"><bean:write name="profile" property="targetName"/></td>
            </tr>
            <tr class="LightRow">
                <td class="Label" width="20%">Assigned To</td>
                <td class="Text" width="80%"><bean:write name="profile" property="assginedToName"/></td>
            </tr>
            <tr class="DarkRow">
                <td class="Label" width="20%">Scanned By</td>
                <td class="Text" width="80%"><bean:write name="profile" property="performedBy"/></td>
            </tr>
            <tr class="LightRow">
                <td class="Label" width="20%">Compliance</td>
                <td class="Text" width="80%"><bean:write name="profile" property="complaintLevel"/></td>
            </tr>
        </table>
    </div>
</div>
<div class="row"><hr/></div>
<a class="Hover" name="a_2" id="a_2" style="text-decoration:none;"></a>

<div class="row">
<div class="col-sm-12">

<table border="1">
    <tr class="Title"><td class="TitleLabel" colspan="5">OVAL Definition Results</td></tr>
    <tr class="DarkRow">
        <td colspan="2">
            <table class="noborder nomargin" style="width:auto;">
                <tr>
                    <td>
                        <table border="1">
                            <tr class="LightRow">
                                <td class="resultbadA ColorBox"></td>
                                <td class="resultbadB ColorBox"></td>
                                <td class="Text" title="Non-Compliant/Vulnerable/Unpatched">Fail</td>
                            </tr>
                        </table>
                    </td>
                    <td>
                        <table border="1">
                            <tr class="LightRow">
                                <td class="resultgoodA ColorBox"></td>
                                <td class="resultgoodB ColorBox"></td>
                                <td class="Text" title="Compliant/Non-Vulnerable/Patched">Pass</td>
                            </tr>
                        </table>
                    </td>
                    <td>
                        <table border="1">
                            <tr class="LightRow">
                                <td class="resulterrorA ColorBox"></td>
                                <td class="resulterrorB ColorBox"></td>
                                <td class="Text">Error</td>
                            </tr>
                        </table>
                    </td>
                    <td>
                        <table border="1">
                            <tr class="LightRow">
                                <td class="resultunknownA ColorBox"></td>
                                <td class="resultunknownB ColorBox"></td>
                                <td class="Text">Unknown</td>
                            </tr>
                        </table>
                    </td>
                    <td>
                        <table border="1">
                            <tr class="LightRow">
                                <td class="resultotherA ColorBox"></td>
                                <td class="resultotherB ColorBox"></td>
                                <td class="Text" title="Inventory/Miscellaneous class, or Not Applicable/Not Evaluated result">Other</td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </td>
        <td colspan="3" align="right" id="filter_part">
            <table class="noborder nomargin" style="width: 100%;">
                <tr>
                    <td>Filter:
                        <select id="search_option" multiple="multiple">
                            <optgroup label="Result" class="group-1">
                                <option value="fail" selected>Vulnerable/Not Installed/Fail</option>
                                <option value="pass" selected>Non Vulnerable/Installed/Pass</option>
                                <option value="error" selected>Error</option>
                                <option value="unknown" selected>Unknown</option>
                                <option value="other" selected >Other</option>
                            </optgroup>
                            <optgroup label="Class" class="group-2">
                                <option value="inventory" selected>Inventory</option>
                                <option value="compliance" selected>Compliance</option>
                                <option value="patch" selected>Patch</option>
                                <option value="vulnerability" selected>Vulnerability</option>
                                <option value="miscellaneous" selected>Miscellaneous</option>
                            </optgroup>
                        </select>
                    </td>
                    <td></td>
                    <td align="right">Search:<input type="text" id="search_txt"></td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
    </tr>
</table>
<table id="result_table" class="table table-bordered">
    <thead>
        <tr class="TitleLabel">
            <th class="Title" align="center">ID</th>
            <th class="Title" align="center">Result</th>
            <th class="Title" align="center">Class</th>
            <th class="Title" align="center">Reference ID</th>
            <th class="Title" align="center">Title</th>
        </tr>
    </thead>
    <tbody><%int numFail = 0; int numError = 0; int numOther = 0; int numUnknown = 0; int numPass = 0;%>
        <logic:iterate id="securityOvalFailDefinitionDetailsBean" name="ovalProfile" property="securityOvalFailDefinitionDetailsBeans">
            <tr class="<%String _class = (numFail % 2) == 0 ? "resultbadA" : "resultbadB"; out.print(_class);%>">
                <td width="10%"><bean:write name="securityOvalFailDefinitionDetailsBean" property="id"/></td>
                <td width="10%"><bean:write name="securityOvalFailDefinitionDetailsBean" property="result"/></td>
                <td width="10%"><bean:write name="securityOvalFailDefinitionDetailsBean" property="classType"/></td>
                <td width="30%"><logic:iterate id="securityOvalFailDefinitionDetailsBeanReference" name="securityOvalFailDefinitionDetailsBean" property="references">[<a class='Hover' target='_blank' href='<bean:write name="securityOvalFailDefinitionDetailsBeanReference" property="value"/>'><bean:write name="securityOvalFailDefinitionDetailsBeanReference" property="key"/></a>] </logic:iterate></td>
                <td width="40"><bean:write name="securityOvalFailDefinitionDetailsBean" property="title"/></td>
            </tr><%numFail++;%>
        </logic:iterate> <logic:iterate id="securityOvalErrorDefinitionDetailsBean" name="ovalProfile" property="securityOvalErrorDefinitionDetailsBeans">
        <tr class="<%String _class = (numError % 2) == 0 ? "resulterrorA" : "resulterrorB"; out.print(_class);%>">
            <td width="10%"><bean:write name="securityOvalErrorDefinitionDetailsBean" property="id"/></td>
            <td width="10%"><bean:write name="securityOvalErrorDefinitionDetailsBean" property="result"/></td>
            <td width="10%"><bean:write name="securityOvalErrorDefinitionDetailsBean" property="classType"/></td>
            <td width="30%"><logic:iterate id="securityOvalErrorDefinitionDetailsBeanReference" name="securityOvalErrorDefinitionDetailsBean" property="references">[<a class='Hover' target='_blank' href='<bean:write name="securityOvalErrorDefinitionDetailsBeanReference" property="value"/>'><bean:write name="securityOvalErrorDefinitionDetailsBeanReference" property="key"/></a>] </logic:iterate></td>
            <td width="40"><bean:write name="securityOvalErrorDefinitionDetailsBean" property="title"/></td>
        </tr><%numError++;%>
    </logic:iterate> <logic:iterate id="securityOvalOtherDefinitionDetailsBean" name="ovalProfile" property="securityOvalOtherDefinitionDetailsBeans">
        <tr class="<%String _class = (numOther % 2) == 0 ? "resultotherA" : "resultotherB"; out.print(_class);%>">
            <td width="10%"><bean:write name="securityOvalOtherDefinitionDetailsBean" property="id"/></td>
            <td width="10%"><bean:write name="securityOvalOtherDefinitionDetailsBean" property="result"/></td>
            <td width="10%"><bean:write name="securityOvalOtherDefinitionDetailsBean" property="classType"/></td>
            <td width="30%"><logic:iterate id="securityOvalOtherDefinitionDetailsBeanReference" name="securityOvalOtherDefinitionDetailsBean" property="references">[<a class='Hover' target='_blank' href='<bean:write name="securityOvalOtherDefinitionDetailsBeanReference" property="value"/>'><bean:write name="securityOvalOtherDefinitionDetailsBeanReference" property="key"/></a>] </logic:iterate></td>
            <td width="40"><bean:write name="securityOvalOtherDefinitionDetailsBean" property="title"/></td>
        </tr>
        <%numOther++;%>
    </logic:iterate> <logic:iterate id="securityOvalUnknownDefinitionDetailsBean" name="ovalProfile" property="securityOvalUnknownDefinitionDetailsBeans">
        <tr class="<%String _class = (numUnknown % 2) == 0 ? "resultunknownA" : "resultunknownB"; out.print(_class);%>">
            <td width="10%"><bean:write name="securityOvalUnknownDefinitionDetailsBean" property="id"/></td>
            <td width="10%"><bean:write name="securityOvalUnknownDefinitionDetailsBean" property="result"/></td>
            <td width="10%"><bean:write name="securityOvalUnknownDefinitionDetailsBean" property="classType"/></td>
            <td width="30%"><logic:iterate id="securityOvalUnknownDefinitionDetailsBeanReference" name="securityOvalUnknownDefinitionDetailsBean" property="references">[<a class='Hover' target='_blank' href='<bean:write name="securityOvalUnknownDefinitionDetailsBeanReference" property="value"/>'><bean:write name="securityOvalUnknownDefinitionDetailsBeanReference" property="key"/></a>] </logic:iterate></td>
            <td width="40"><bean:write name="securityOvalUnknownDefinitionDetailsBean" property="title"/></td>
        </tr><%numUnknown++;%>
    </logic:iterate> <logic:iterate id="securityOvalPassDefinitionDetailsBean" name="ovalProfile" property="securityOvalPassDefinitionDetailsBeans">
        <tr class="<%String _class = (numPass % 2) == 0 ? "resultgoodA" : "resultgoodB"; out.print(_class);%>">
            <td width="10%"><bean:write name="securityOvalPassDefinitionDetailsBean" property="id"/></td>
            <td width="10%"><bean:write name="securityOvalPassDefinitionDetailsBean" property="result"/></td>
            <td width="10%"><bean:write name="securityOvalPassDefinitionDetailsBean" property="classType"/></td>
            <td width="30%"><logic:iterate id="securityOvalPassDefinitionDetailsBeanReference" name="securityOvalPassDefinitionDetailsBean" property="references">[<a class='Hover' target='_blank' href='<bean:write name="securityOvalPassDefinitionDetailsBeanReference" property="value"/>'><bean:write name="securityOvalPassDefinitionDetailsBeanReference" property="key"/></a>]&nbsp;</logic:iterate></td>
            <td width="40"><bean:write name="securityOvalPassDefinitionDetailsBean" property="title"/></td>
        </tr><%numPass++;%>
    </logic:iterate>
    </tbody>
</table>
</div>
</div>
<div class="row text-center"><p class="muted credit">Generated using <a href="http://harman.com">Clarinet</a></p></div>
</div>
<script type="text/javascript">
    $(document).ready(function() {
        $('#search_txt').keyup(function() {
            var value = $(this).val().toLowerCase();
            $('#result_table > tbody > tr').filter(function() {
                $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1);
            });
        });
        $('#search_option').multiselect({
            includeSelectAllOption: true,
            selectAllValue: 'select-all-value',
            onChange: function(option, checked, select) {
                var filterArr = [];
                var rows = $('#result_table > tbody > tr');
                rows.hide();
                $('#search_option option:selected').each(function() {
                    filterArr.push($(this).val().toLowerCase());
                });
                rows.filter(function() {
                    var result_text = $(this).find('td:nth-child(2)').text().toLowerCase();
                    var class_text = $(this).find('td:nth-child(3)').text().toLowerCase();
                    var result_filter_met = 'false';
                    var class_filter_met = 'false';
                    $.each(filterArr, function(index, filter) {
                        if (('fail' == filter) && (result_text === 'vulnerable' || result_text === 'non-compliant' || result_text === 'not-installed')) {
                            result_filter_met = 'true';
                        } else if (('pass' == filter) && (result_text === 'non-vulnerable' || result_text === 'compliant' || result_text === 'installed')) {
                            result_filter_met = 'true';
                        } else if (('error' == filter) && (result_text === 'error')) {
                            result_filter_met = 'true';
                        } else if (('unknown' == filter) && (result_text === 'unknown')) {
                            result_filter_met = 'true';
                        } else if (('other' == filter) && (result_text === 'not evaluated' || result_text === 'not applicable')) {
                            result_filter_met = 'true';
                        }

                        if (('inventory' == filter) && (class_text === 'inventory')) {
                            class_filter_met = 'true';
                        } else if (('patch' == filter) && (class_text === 'patch')) {
                            class_filter_met = 'true';
                        } else if (('compliance' == filter) && (class_text === 'compliance')) {
                            class_filter_met = 'true';
                        } else if (('vulnerability' == filter) && (class_text === 'vulnerability')) {
                            class_filter_met = 'true';
                        } else if (('miscellaneous' == filter) && (class_text === 'miscellaneous')) {
                            class_filter_met = 'true';
                        }
                    });
                    return ((result_filter_met === 'true') && (class_filter_met === 'true'));
                }).show();

            },
            onSelectAll: function() {$('#result_table > tbody > tr').show();},
            onDeselectAll: function() {$('#result_table > tbody > tr').show();}
        });
        $('#do_print').click(function() {
            $('#filter_part, #do_print').hide();
            var body = $('body').html();
            var style = $('style').html();
            var custom_style = 'body {font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;font-size: 12px;} #result_table td{border: 1px solid #ffffff;  border-collapse: collapse;text-align: left; white-space: normal; word-break: break-all;color: #000000; font-family: Geneva, Arial, Helvetica, sans-serif;}';
            custom_style = custom_style + '.alert-info {color: #31708f;background-color: #d9edf7;border-color: #bce8f1;}';
            custom_style = custom_style + '.alert {padding: 15px;margin-bottom: 20px;border: 1px solid transparent;border-top-color: transparent;border-right-color: transparent;border-bottom-color: transparent;border-left-color: transparent;border-radius: 4px;}';
            custom_style = custom_style + 'blockquote {padding: 10px 20px;margin: 0 0 20px;font-size: 17.5px;border-left: 5px solid #eee;}';
            custom_style = custom_style + '.h2, h2 {font-size: 20px;margin-bottom: 2px;text-align:left;}';
            custom_style = custom_style + '.row {margin-right: -15px;margin-left: -15px;}';
            custom_style = custom_style + 'mark {color: #000;background: #ff0;background-color: rgb(255, 255, 0);}';
            custom_style = custom_style + '.container {padding-right: 15px;padding-left: 15px;margin-right: auto;margin-left: auto;width: 98%}';
            var content = '<html><head><style type="text/css">'+ $.trim(style + custom_style) +'</style></head><body>'+ $.trim(body) +'</body></html>';
            content = content.replace(/\s/g, ' ')
            $.fileDownload('./pdfgenerator', {httpMethod: 'POST', data: {html: content},
                prepareCallback:function(e){showSpinner('body_content');},
                successCallback: function(url) {$('#filter_part, #do_print').show(); closeSpinner('body_content');},
                abortCallback: function (url) {alertError('Download aborted'); $('#filter_part, #do_print').show(); closeSpinner('body_content');},
                failCallback: function(responseHtml, url) {alertError('Failed to download: ' + responseHtml); $('#filter_part, #do_print').show(); closeSpinner('body_content');}
            });
            return false;
        });
    });
</script>
</body>
</html>

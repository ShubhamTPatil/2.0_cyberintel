<%@ include file="/includes/directives.jsp" %>

<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/common_js.jsp" %>


<script type="text/javascript" src="/shell/common-rsrc/js/master.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/table.js"></script>
<script type="text/javascript" src="/shell/common-rsrc/js/domMenu.js"></script>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />

<script type="text/javascript" src="/spm/js/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/jstree.min.js"></script>
<script type="text/javascript" src="/spm/js/jstreegrid.js"></script>

<style type="text/css">
    #tab_tx_diagnostics a, #tab_tx_operations a {color: blue; font-weight:bold;}
    #tx_content_tree .folder { background:url('../assets/vendor/jstree/css/file_sprite.png') right bottom no-repeat; height: 20px; line-height: 19px; width: 18px;}
    #tx_content_tree .channel {background: url(../assets/img/package.gif);background-size: 16px 14px;background-repeat: no-repeat;margin-right: -3px;margin-top: 6px;}
    #tx_content_tree .transmitter {background: url(../assets/img/master.gif);background-size: 22px 19px;background-repeat: no-repeat;}
    .jstree-default  a.jstree-search {color: black;}
    .form-control {box-shadow: 0 1px 1px rgba(0, 0, 0, 0.075) inset; height: 31px; padding: 5px; width: 40%;}
    #tx_channel_tree .folder { background:url('../assets/vendor/jstree/css/file_sprite.png') right bottom no-repeat; height: 20px; line-height: 19px; width: 18px;}
    #tx_channel_tree .channel {background: url(../assets/img/package.gif);background-size: 16px 14px;background-repeat: no-repeat;margin-right: -3px;margin-top: 6px;}
    #tx_channel_tree .transmitter {background: url(../assets/img/master.gif);background-size: 22px 19px;background-repeat: no-repeat;}

</style>
<body>
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>

<html:form name="vDeskReportForm" action="/reports.do" type="com.marimba.apps.securitymgr.webapp.forms.VDeskReportForm">
    <table width="320" cellspacing="0" cellpadding="0" border="0">
        <tr><td class="tableTitle">&nbsp;<webapps:pageText key="reports"/></td></tr>
    </table>
    <div class="tableWrapper" style="width: 318px;">
        <div id="dataSection" style="height: 100px; width: 96%; overflow: auto; padding-left: 8px; line-height: 5px;">
        <div id="query_content_tree">&nbsp;</div>
        </div>
    </div>

</html:form>
<div id="endOfGui"></div>
<script type="text/javascript">
var jsTreeObj;
function loadTxContentTree() {
    if (jsTreeObj) jsTreeObj.jstree('destroy');
    jsTreeObj = $('#query_content_tree').jstree({
        core : {
            data : function (node1, cb) {
                var urlFormatted = (node1.id === '#') ?
                                                   '/spm/reports.do?action=list_queries' : '/spm/reports.do?action=list_queries&path=' + node1.id;
                $.ajax({
                    url: urlFormatted, type: "get", dataType: "json",
                    success: function (response) {
                        cb(response.contents);
                    }
                });
            }
        },
    });
}

$('#query_content_tree').on('changed.jstree', function (e, data) {
    var i, j, r = [];
    for(i = 0, j = data.selected.length; i < j; i++) {
      r.push(data.instance.get_node(data.selected[i]).id);
    }

    top.frames['mainFrame'].location.href = '/spm/reports.do?action=selected_query&path='+ r.join(', ');
  })
resizeDataSection('dataSection','endOfGui');
$(document).ready(function() {
    loadTxContentTree();
});
</script>
</body>
</html>
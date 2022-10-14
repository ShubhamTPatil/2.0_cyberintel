<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants,
                 java.util.Locale"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<% String packageComplianceURL = request.getContextPath() + "/packageCompliance.do"; %>
<%
  // For getting locale information
  Locale locale = request.getLocale();

  String language = locale.getLanguage();
  String country = locale.getCountry();
%>

<%@ include file="/includes/startHeadSection.jsp" %>
<script type="text/javascript" src="/sm/includes/jsonrpc.js"></script>
<script type="text/javascript" src="/sm/includes/complianceJsonClient.js"></script>
<script type="text/javascript" src="/sm/includes/htmlescape.js"></script>
<script type="text/javascript" src="/sm/includes/calendar.js"></script>
<script>

var packageComplianceURL = '<%=packageComplianceURL%>';

var pkgResult = null;
var packages = null;
var inSearch = false;
var searchPkgName = '';

<%-- Date validation Parameters --%>
var validDateTime = false;
var validDateResult = null;
var localeStr = '<%= language %>' + ','  +'<%= country %>';

function resetValues(pkgName, pkgTime){
      pkgName.value="";
      pkgTime.value="";
}

//e.keyCode         = supported by IE, Opera

//e.which           = supported by Firefox, all mozilla engine based browsers

//e.charCode        = supported by Old Netscape browser

function enterSearch( pkgName, pkgTime, e ){
    var keyCode = e.keyCode ? e.keyCode : e.which ? e.which : e.charCode;
    if (keyCode == 13) {
        searchPackages( pkgName, pkgTime );
    }
}

function searchPackages( pkgName, pkgTime ){
    if (inSearch) {
        alert("<webapps:pageText key="CannotProcessSearch" escape="js"/>");
    } else {
        inSearch = true;
        searchPkgName = pkgName;
        showPackageNames();
        if( pkgTime == '' ) {
            jsonrpc.packageviewservice.getPackages(getResult, pkgName, pkgTime);
        } else {
            validDateResult = jsonrpc.packageviewservice.isValidDateTime( pkgTime, localeStr);
            if( validDateResult == "invalid" ) {
                inSearch = false;
                displayDateStatusMessage();
            } else {
                jsonrpc.packageviewservice.getPackages(getResult, pkgName, validDateResult);
            }
        }
    }
}

function getResult(result, exception) {
    if (exception) {
        alert(exception);
        pkgResult = null;
        packages = null;
    } else {
        pkgResult = result;
        if (!pkgResult.error) {
            packages = pkgResult.list.list;
        } else {
            packages = null;
        }
    }
    inSearch = false;
    showPackageNames();
}

function displayDateStatusMessage() {
    document.getElementById('dataSection').innerHTML = '&nbsp;<font color=red><webapps:pageText key="invaliddateformat" escape="js"/></font>';
}

function displaySearchMessage() {
    document.getElementById('dataSection').innerHTML = '&nbsp;<webapps:pageText key="SearchingFor" escape="js"/> "' + htmlescape(searchPkgName) + '" ...';
}

function displayPackageNotFound() {
    document.getElementById('dataSection').innerHTML = '&nbsp;<webapps:pageText key="packageNotFound" escape="js"/>';
}

function showPackageNames(){
    var packageName = "";
        if (inSearch) {
            displaySearchMessage();
        } else if (!pkgResult) {
           // Do nothing
        } else if (pkgResult.error) {
           document.getElementById('dataSection').innerHTML = '&nbsp;<font color=red><webapps:pageText key="packagesearchfailed" escape="js"/></font>';
	} else if( packages.length > 0 ) {
		var inner_html = "";
		var indx = 0;
        var content_type = "";
        var content_type_patch = '<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>';
        var content_type_app = '<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>';

        inner_html = '<div id="data"><table width="281" border="0" cellpadding="1" cellspacing="1"><colgroup span="1" width="100%"></colgroup><tbody id="pkgs_available">';
		for( indx=0;indx<packages.length;indx++ ){
			packageName = packages[indx].name;
            content_type = packages[indx].content_type;
            encodedPackageName = packages[indx].encodedName;
            if(content_type == content_type_patch) {
                inner_html = inner_html+'<tr valign="middle"><td class="rowLevel1" style="border-top:0px;">'+
                        '<img src="/shell/common-rsrc/images/patch_group.gif" width="16" height="16">';
            } else if (content_type == content_type_app) {
                inner_html = inner_html+'<tr valign="middle"><td class="rowLevel1" style="border-top:0px;">'+
                        '<img src="/shell/common-rsrc/images/package.gif" width="16" height="16">';
            }else {
                inner_html = inner_html+'<tr valign="middle"><td class="rowLevel1" style="border-top:0px;">'+
                        '<img src="/shell/common-rsrc/images/mcafee_group.gif" width="16" height="16">';
            }
            inner_html = inner_html+'&nbsp;&nbsp;<a href="'+packageComplianceURL+'?channelTitle='+ encodedPackageName +
                        '&channelURL='+ packages[indx].encodedUrl +'&content_type='+escape(content_type)+
                        '" target="right-frame" class="hoverLink" onmouseover="return Tip(\''+
                         escapeJavascriptAndHTML(packages[indx].url, 100)+'\', WIDTH, \'-450\',BGCOLOR, \'#F5F5F2\',FONTCOLOR, \'#000000\',OFFSETY, 20,BORDERCOLOR, \'#333300\',FADEIN, 100);" onmouseout="return UnTip();">'+
                        htmlescape(packageName)+'</a></td></tr>';
		}
		inner_html = inner_html+'</tbody></table></div>';
		document.getElementById('dataSection').innerHTML = inner_html;
	} else {
            displayPackageNotFound();
    }
}

function showPackageURLs(){
    var packageName = "";
        if (inSearch) {
            displaySearchMessage();
        } else if (!pkgResult) {
           // Do nothing
        } else if (pkgResult.error) {
           document.getElementById('dataSection').innerHTML = '&nbsp;<font color=red><webapps:pageText key="packagesearchfailed" escape="js"/></font>';
	} else if( packages.length > 0 ) {
		var inner_html = "";
		var indx = 0;
        var content_type = "";
        var content_type_patch = '<%=ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP%>';
        var content_type_app = '<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>';

        inner_html = '<div id="data"><table width="281" border="0" cellpadding="2" cellspacing="1"><colgroup span="1" width="100%"></colgroup><tbody id="pkgs_available">';
		for( indx=0;indx<packages.length;indx++ ){
			packageName = packages[indx].name;
            content_type = packages[indx].content_type;
            encodedPackageName = packages[indx].encodedName;
		    if(content_type == content_type_patch) {
                inner_html = inner_html+'<tr valign="middle"><td class="rowLevel1" style="border-top:0px;">'+
                        '<img src="/shell/common-rsrc/images/patch_group.gif" width="16" height="16">';
            } else if (content_type == content_type_app) {
                inner_html = inner_html+'<tr valign="middle"><td class="rowLevel1" style="border-top:0px;">'+
                        '<img src="/shell/common-rsrc/images/package.gif" width="16" height="16">';
            } else {
                inner_html = inner_html+'<tr valign="middle"><td class="rowLevel1" style="border-top:0px;">'+
                        '<img src="/shell/common-rsrc/images/mcafee_group.gif" width="16" height="16">';
            }
            inner_html = inner_html+'&nbsp;&nbsp;<a href="'+packageComplianceURL+'?channelTitle='+ encodedPackageName +
                        '&channelURL='+ packages[indx].encodedUrl +'&content_type='+escape(content_type)+
                        '" target="right-frame" class="hoverLink" onmouseover="return Tip(\''+
                        escapeJavascriptAndHTML(packageName, 100)+'\', WIDTH, \'-450\',BGCOLOR, \'#F5F5F2\',FONTCOLOR, \'#000000\',OFFSETY, 20,BORDERCOLOR, \'#333300\',FADEIN, 100);" onmouseout="return UnTip();">'+
                        htmlescape(packages[indx].url)+'</a></td></tr>';
		}
		inner_html = inner_html+'</tbody></table></div>';
		document.getElementById('dataSection').innerHTML = inner_html;
	} else {
            displayPackageNotFound();
        }
}
</script>

<style type="text/css">
.searchTabActive {
	background-color: #FFFFFF;
	border-top:1px solid #CCCCCC;
	padding-top:2px;
	padding-bottom:2px;
}
.searchTabInactive {
	background-color: #627EB3;
	color: #FFFFFF;
	border-top:none;
	paddoneing-top:2px;
	padding-bottom:2px;
}

<!-- Style for calendar control on Package Compliance page.//-->

body {
	font-family: Verdana, Tahoma, Arial, Helvetica, sans-serif;
	font-size: .8em;
	}

.dpDiv {
	}

.dpTable {
	font-family: Tahoma, Arial, Helvetica, sans-serif;
	font-size: 12px;
	text-align: center;
	color: #505050;
	background-color: beige;
	border: 1px solid #AAAAAA;
	}

.dpTR {
	}

.dpTitleTR {
	}

.dpDayTR {
	}

.dpTodayButtonTR {
	}

.dpTD {
	cursor: pointer;
	}

.dpDayHighlightTD {
	background-color: #CCCCCC;
	border: 1px solid #AAAAAA;
	cursor: pointer;
	}

.dpTitleTD {
	}

.dpButtonTD {
	}

.dpTodayButtonTD {
	}

.dpDayTD {
	background-color: #CCCCCC;
	border: 1px solid #AAAAAA;
	color: blue;
	}

.dpTitleText {
	font-size: 12px;
	color: black;
	font-weight: bold;
	}

.dpDayHighlight {
	color: 4060ff;
	font-weight: bold;
	}

.dpButton {
	font-family: Verdana, Tahoma, Arial, Helvetica, sans-serif;
	font-size: 10px;
	color: black;
	background: #d8e8ff;
	font-weight: bold;
	padding: 0px;
	}

.dpTodayButton {
	font-family: Verdana, Tahoma, Arial, Helvetica, sans-serif;
	font-size: 10px;
	color: black;
	background: #d8e8ff;
	font-weight: bold;
	}

</style>
<%@ include file="/includes/endHeadSection.jsp" %>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad = "resizeDataSection('dataSection','endOfGui');" onResize="resizeDataSection('dataSection','endOfGui');">
<script type="text/javascript" src="/shell/common-rsrc/js/wz_tooltip.js"></script>
<html:form name="packageComplianceForm" action="/packageCompliance.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageComplianceForm">

<table width="320" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="tableTitle"><webapps:pageText key="Pkgs" /></td>
  </tr>
</table>

    <div class="tableWrapper" width="320px" align="left">
        <table border="0" cellspacing="0" cellpadding="0" style="background-color:#627eb3;" width="320px">
            <tr valign="middle">
                <td style="padding-top:2px; padding-left:3px; border-top:1px solid #435d8d;">
                    <table border="0" cellspacing="0" cellpadding="0" class="textSmall" style="color:#FFFFFF;" width="100%">
                        <colgroup span="3" width="0*"></colgroup>
                        <colgroup width="100%"></colgroup>
                        <colgroup width="0*"></colgroup>
                        <tr>
                            <td width="5" align="right" valign="bottom"><img id="basic_left_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="19"></td>
                            <td align="center" nowrap class="searchTabInactive" id="basic_cell"><a href="javascript:void(0);" style="color:#FFFFFF;" id="basic_link" onClick="showSearch('basic');resizeDataSection('dataSection','endOfGui');"><webapps:pageText key="Search" /></a></td>
                            <td width="5" align="left" valign="bottom"><img id="basic_right_img" src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="19"></td>
                            <td>&nbsp;</td>
                            <td width="20" align="right"><a href="javascript:void(0);" onClick="showSearch('hide');resizeDataSection('dataSection','endOfGui');" onmouseover="return Tip('<webapps:text key="page.ldap_nav.HideSearch" escape="js"/>', WIDTH, '-450',BGCOLOR, '#F5F5F2',FONTCOLOR, '#000000',OFFSETY, 20,BORDERCOLOR, '#333300',FADEIN, 100);" onmouseout="return UnTip();"><img src="/shell/common-rsrc/images/minimize.gif" width="12" height="12" border="0"></a></td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tbody id="basic_section">
                <tr valign="middle">
                    <td style="padding-left:3px; padding-right:3px; padding-bottom:3px;">
                        <table border="0" cellspacing="0" cellpadding="0">
                            <colgroup width="0*"></colgroup>
                            <colgroup width="100%"></colgroup>
                            <colgroup width="0*"></colgroup>
                            <tr>
                                <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="5" height="5"></td>
                                <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="1"></td>
                                <td><img src="/shell/common-rsrc/images/search_corner_top_rt.gif" width="5" height="5"></td>
                            </tr>
                        </table>
                        <table border="0" cellspacing="0" cellpadding="2" class="textSmall" style="background-color:#FFFFFF; color:#000000;" width="100%">
                            <tr>
                                <td align="right"><webapps:pageText key="Name"/>:</td>
                                <td><input name="packageName" type="text" size="25" onkeypress="javascript:enterSearch( packageName.value, packageTime.value, event );">
                                </td>
                            </tr>
                            <tr>
                                <td align="right" valign="middle"><webapps:pageText key="Published"/></td>
                                <td valign="middle">
                                    <input name="packageTime" type="text" size="10">
                                    <a style="cursor:help;" href="javascript:void(0);" class="noUnderlineLink" onmouseover="return Tip('<webapps:pageText shared="true" type="global" key="example" escape="js"/><webapps:stringescape><webapps:datetime locale="request" dateStyle="<%=java.text.DateFormat.SHORT%>" type="date" view="sample" /></webapps:stringescape>', WIDTH, '-450',BGCOLOR, '#F5F5F2',FONTCOLOR, '#000000',OFFSETY, 20,BORDERCOLOR, '#333300',FADEIN, 100);" onmouseout="return UnTip();">
                                        <webapps:datetime locale="request" dateStyle="<%=java.text.DateFormat.SHORT%>" type="date" view="pattern" />
                                        <br>
                                        <webapps:pageText shared="true" type="global" key="example"/>&nbsp;
                                        <webapps:datetime locale="request" dateStyle="<%=java.text.DateFormat.SHORT%>" type="date" view="sample" />
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td class="col1">&nbsp;</td>
                                <td align="right" class="col2" style="padding-right:3px;">
                                    <input name="go_button" id="go_button" type="button" class="smallButtons" value="<webapps:pageText key="Go" />" onclick="searchPackages( packageName.value, packageTime.value )">
                                    <input name="reset_button" id="reset_button" type="button" class="smallButtons" value="<webapps:pageText key="Reset" />"  onclick="resetValues(packageName, packageTime)"></td>
                            </tr>
                        </table>
                        <table border="0" cellspacing="0" cellpadding="0" width="100%">
                            <colgroup width="0*"></colgroup>
                            <colgroup width="100%"></colgroup>
                            <colgroup width="0*"></colgroup>
                            <tr>
                                <td><img src="/shell/common-rsrc/images/search_corner_bot_lft.gif" width="5" height="5"></td>
                                <td style="background-color:#FFFFFF;"><img src="/shell/common-rsrc/images/invisi_shim.gif" width="1" height="1"></td>
                                <td><img src="/shell/common-rsrc/images/search_corner_bot_rt.gif" width="5" height="5"></td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </tbody>

        </table>

        <div class="headerSection">
            <table width="320px" border="0" cellpadding="0" cellspacing="0">
                <thead>
                    <tr>
                        <td class="tableHeaderCell"><a class="columnHeading" href="#"><webapps:pageText key="Pkg" /></a> </td>
                        <td align="right" class="tableHeaderCell" style="border-left-style:none "><a class="columnHeading" href="#" onClick="showPackageNames()"><webapps:pageText key="Name" /></a> | <a class="columnHeading" href="#" onClick="showPackageURLs()"><webapps:pageText key="URL" /></a> </td>
                    </tr>
                </thead>
            </table>
        </div>
        <div id="dataSection" style="height:100px;overflow:auto;" onresize="resizeDataSection('dataSection','endOfGui');">&nbsp;</div>
    </div>
</html:form>
<div id="endOfGui"></div>
<script>
    resizeDataSection('dataSection','endOfGui');
    initJSONRPC();
</script>

</body>
</html>

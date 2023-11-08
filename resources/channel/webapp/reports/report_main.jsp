<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>

<!DOCTYPE html>
<html lang="en">
<head>

    <title><webapps:pageText key="Title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <meta http-equiv="Cache-control" content="no-cache">

    <link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css"/>
	<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css"/>
	<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css"/>
	<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css"/>
	<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css"/>

	<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
	<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
	<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
	<script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
	<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
	<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>

<script type="text/javascript">
    
    function CMSEventHandler() {
        this.handlers = new Array();
        this.addHandler = CMSEventHandler_addHandler;
        this.handleEvent = CMSEventHandler_handleEvent;

        function CMSEventHandler_addHandler(handler) {
            this.handlers[this.handlers.length] = new Function(handler);
        }

        function CMSEventHandler_handleEvent(event) {
            for (var i = 0; i < this.handlers.length; i++) {
                this.handlers[i](event);
            }
        }
    }

    var CMSOnResizeHandler = new CMSEventHandler();
    onresize = function (event) {
        CMSOnResizeHandler.handleEvent(event);
    }

    var CMSOnLoadHandler = new CMSEventHandler();
    onload = function (event) {
        CMSOnLoadHandler.handleEvent(event);
    }

    var CMSOnMouseUpHandler = new CMSEventHandler();
    document.onmouseup = function (event) {
        CMSOnMouseUpHandler.handleEvent(event);
    }

    //This function sets the tabIndex of all form elements that use the tags: input, textarea, and select
    function setTabIndex() {
        var allINPUT = document.getElementsByTagName("input");
        var allTEXTAREA = document.getElementsByTagName("textarea");
        var allSELECT = document.getElementsByTagName("select");

        var allFields = new Array();
        allFields.concat(allINPUT, allTEXTAREA, allSELECT);

        for(var i=0; i<allFields.length; i++) {
            allFields[i].tabIndex = 1;
        }
    }

    CMSOnLoadHandler.addHandler("setTabIndex();");
</script>

<%--
<%@ include file="/includes/startHeadSection.jsp" %>
<jsp:include page="/common-rsrc/header/header.jsp" flush="false"/>
--%>

<style type="text/css">
    .slider-arrow {
        background: #d9dada none repeat scroll 0 0; float: left;
        font-size: 25px; position: fixed; margin-left: -2px;

    }
</style>
<style type="text/css">
    #pageContent {
        padding-left: 5px;
        padding-right: 5px;
    }
</style>
	
</head>
<body>
<html:form name="vDeskReportForm" action="/reports.do" type="com.marimba.apps.securitymgr.webapp.forms.VDeskReportForm">
<html:hidden property="action"/>
<jsp:include page="/includes/common_js.jsp" />
<jsp:include page="/dashboard/header.jsp" />
<jsp:include page="/dashboard/sidebar.jsp" />

  <main id="main" class="main">

    <div class="pagetitle">

        <div class="d-flex bd-highlight justify-content-center">
          <div class="p-2 flex-grow-1 bd-highlight">
            <span class="pagename">Scan Results</span>
            <span data-bs-toggle="tooltip" data-bs-placement="right" title="Scan Results"><i
                class="fa-solid fa-circle-info text-primary"></i></span>
          </div>
          <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
            title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i></div>
          <div class="p-2 bd-highlight text-primary align-self-center"> <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left"
                style="margin-right: 5px;"></i>CMS Home</a>
          </div>
        </div>
  
      </div>

    <section class="section dashboard">

        <div class="row">
            <div class="col-lg-3">
                <div class="card info-card customers-card">
                    <div class="card-body">
                        <iframe name="queries" src="<webapps:fullPath path="/reports.do?action=left" />" width="100%" frameborder="0" style="height: 80vh;padding:0px; margin:0px; border:0px; min-height: 400px;"></iframe>
                    </div>
                </div>
            </div>

            <div class="col-lg-9">
                <div class="card info-card customers-card">
                    <div class="card-body">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/reports.do?action=selected_query" />" width="100%" frameborder="0" hspace="0" marginheight="0" marginwidth="0" style="height: 80vh; padding:0px; margin:0px; border:0px; min-height: 400px;"></iframe>
                    </div>
                </div>
            </div>
        </div>

    </section>
<!-- 
    <div align="center" style="padding-top:6px;">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr>
                <td valign="top" width="322px">
                    <div id="FOO_GROUP_LISTING_iframe" style="height:100px; border:0; margin:0; padding:0; overflow:visible; display:block;">
                        <iframe name="queries" src="<webapps:fullPath path="/reports.do?action=left" />" width="100%" height="100%" frameborder="0"></iframe>
                    </div>
                    <div id="FOO_endOfGroupList">&nbsp;</div>
                </td>
                <td valign="top" style="padding-left:25px;" align="left">
                    <div id="FOO_dataDiv" style="height:100px; border:0; margin:0; padding:0; display:block; overflow:auto;">
                        <iframe name="mainFrame" src="<webapps:fullPath path="/reports.do?action=selected_query" />" width="110%" height="100%" frameborder="0" hspace="0" marginheight="0" marginwidth="0" style="padding:0px; margin:0px; border:0px;"></iframe>
                    </div>
                    <div id="FOO_endOfData">&nbsp;</div>
                </td>
            </tr>
        </table>
    </div> -->

  </main>
</html:form>
    <script type="text/javascript">
    
        // CMSOnResizeHandler.addHandler("resizeDataSection('FOO_dataDiv','FOO_endOfData',0);");
        // CMSOnResizeHandler.addHandler("resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);");
        // resizeDataSection('FOO_dataDiv','FOO_endOfData',0);
        // resizeDataSection('FOO_GROUP_LISTING_iframe','FOO_endOfGroupList',0);
    
    </script>
</body>
</html>
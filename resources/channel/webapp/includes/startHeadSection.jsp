<%--
	Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

	@author   Theen-Theen Tan
	@version  $Revision$,  $Date$
--%>

<%--
This is the starting section of the headSection.jsp file
the reason this needs to be done is because the help id tags have to go into the head of the page
--%>

<html>
<head>
    <title><webapps:pageText key="Title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <meta http-equiv="Cache-control" content="no-cache">
    <link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css"/>
    <script language="javascript" src="/shell/common-rsrc/js/master.js"></script>
    <script language="javascript" src="/spm/includes/selectoption.js"></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/table.js"></script>
    <script language="JavaScript" src="/shell/common-rsrc/js/tableSync.js"></script>
    <script language="javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>
    <script language="javascript" src="/shell/common-rsrc/js/domMenu.js"></script>
    <!-- These two lines of code need to be here in order for overlib to work.  They have to be placed outside the form in the head section -->
<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
<script language="JavaScript" src="/shell/common-rsrc/js/overlib.js"></script>
<script language="JavaScript" src="/shell/common-rsrc/js/intersect.js"></script>

<style type="text/css">
    #pageContent {
        padding-left: 10px;
        padding-right: 10px;
    }
</style>
<%@ include file="/includes/common_js.jsp" %>
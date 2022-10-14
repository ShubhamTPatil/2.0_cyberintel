<html>
  <head>
    <title><webapps:pageText key="Title" /></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="-1">
    <meta http-equiv="Cache-control" content="no-cache">
    <link rel="stylesheet" href="<%=request.getContextPath() %>/common-rsrc/css/main.css" type="text/css">
   <!-- These two lines of code need to be here in order for overlib to work.  They have to be placed outsi
    de the form in the head section -->
        <div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
        <script language="JavaScript" src="/shell/common-rsrc/js/overlib.js"></script>
        <script language="JavaScript" src="/shell/common-rsrc/js/tableSync.js" ></script>        
  </head>

  <!-- This Javascript is here to fix 39695 so that CMS menu can deactivate if user click in an iframe -->
  <!-- endHeadSection and headSection are the only file that every page in the system includes -->
        <script language="JavaScript">
         document.onmouseup = function (event) {
            top.CMSOnMouseUpHandler.handleEvent(event);
         }
        </script>
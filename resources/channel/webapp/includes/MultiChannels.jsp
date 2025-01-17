<%--Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$
--%>
<%--
     This class When we select the selected target hyperlink from package view page, selected channels will be displayed in a glass pane with editable option 

     @author Selvaraj Jegatheesan
     @version 4, 2009/03/16
--%>

<!-- Transparent cover -->
<%String channelListAction = request.getContextPath() + "/channelListDisplay.do"; %>
<div id="transparency" style="background-color: #F8F8FF;visibility:hidden; position:absolute; left:0px; top:0px; width:100%; height:100%; z-index:101; filter: alpha(opacity=65); -moz-opacity: 0.65; zoom: 1; opacity: 0.65; display: none;">
    </div>
    <div id="windowcontent" style="position:absolute;width:620px; height:500px; display:none; z-index:300; border: black 1px solid; border-bottom: black 1px solid; border-top-style: ridge;">
        <iframe id="multiEditHolder" name="multiEditHolder" src="<%=channelListAction%>" style="width:100%;height:100%;" scrolling="no" frameborder="0">
    </iframe>
</div>

<script type="text/javascript">

    function showMultiChannels() {
        document.getElementById('multiEditHolder').src = '<webapps:fullPath path="/channelListDisplay.do" />';
        var width = 620;
        var height = 500;
        var transparencyWidth = document.body.scrollWidth;
        var left = (screen.availWidth - width) / 2;
        var top = (screen.availHeight - height) / 5;
        var transparentCoverObj =  document.getElementById ('transparency');
        var editObj = document.getElementById ('windowcontent');
        var transparencyHeight = document.body.scrollHeight;
        toggleFrameSelect(false);
        transparentCoverObj.style.height = transparencyHeight + 'px';
        transparentCoverObj.style.display="";
        transparentCoverObj.style.width = transparencyWidth + 'px';
        transparentCoverObj.style.visibility ="visible";
        editObj.style.visibility ="visible";
        editObj.style.display="";
        editObj.style.top = top + 'px';
        editObj.style.left = left + 'px';

    }

    function hideMultiChannelsArea() {
        document.getElementById ('transparency').style.display='none';
        document.getElementById ('windowcontent').style.display='none';
        document.getElementById ('transparency').style.visibility="hidden";
        document.getElementById ('windowcontent').style.visibility="hidden";
        toggleFrameSelect(true);
    }
    function pageRefresh(thisform) {
        var isNeedRefresh = thisform.checkRemove.value;
        if (isNeedRefresh == "1") {
            var forwardPage = "/pkg/package_frameset.jsp";
            window.location.href="<webapps:fullPath path='" + forwardPage + "' />";
        }
        <%session.removeAttribute("requiredRemove");%>
    }

    function emptyPackage() {
        var forwardPage = "/pkg/package_frameset.jsp";
        window.location.href="<webapps:fullPath path='" + forwardPage + "' />";
    }

</script>



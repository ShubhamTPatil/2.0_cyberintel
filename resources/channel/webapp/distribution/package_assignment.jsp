<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page buffer="none" %>

<%--
    Copyright 2004-2012, BMC Software Inc. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software Inc.
    Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
    6,381,631, and 6,430,608. Other Patents Pending.


    $File$



     @author Theen-Theen Tan

     @version 1.35, 05/07/2002

--%>



<%@ include file="/includes/directives.jsp" %>

<%@ page import = "com.marimba.webapps.tools.util.PropsBean,

                   com.marimba.apps.subscription.common.ISubscriptionConstants" %>

<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>

<%@ page import = "java.util.*" %>

<%@page errorPage="/includes/internalerror.jsp" %>



<%@ include file="/includes/startHeadSection.jsp" %>



<title><webapps:pageText key="m6" type="global"/></title>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" href="/shell/common-rsrc/css/main.css" type="text/css">

<style type="text/css">

    <!--

    /* These styles are used exclusively for this page*/



    .col1 {

        width: 30%;

    }

    .col2 {

        width: 70%;

    }

    -->

</style>

<script language="javascript" src="/shell/common-rsrc/js/master.js"></script>

<script language="javascript" src="/shell/common-rsrc/js/ypSlideOutMenusC.js"></script>

<script language="javascript" src="/shell/common-rsrc/js/domMenu.js"></script>

<script language="javascript" src="/shell/common-rsrc/js/domMenu_items2.js"></script>



<script language="JavaScript" type="text/JavaScript">

    // this function checks/unchecks all rows when

    // "select-all" is checked/unchecked

    function setChecked(val) {

        var colForm = document.packageEditForm;

        var len = colForm.elements.length;

        var i = 0;



        for (i = 0; i < len; i++) {

            if (colForm.elements[i].name == 'packages') {

                colForm.elements[i].checked = val;

            }

        }

    }



    // this function does the reverse of the above function

    // if all te rows are checked, "select-all" gets checked.

    function setCheckedAll(val) {

        var colForm = document.packageEditForm;

        var len = colForm.elements.length;

        var i = 0;

        var allChecked = true;

        var rowsPresent = false;

        for (i = 0; i < len; i++) {

            if (colForm.elements[i].name == 'packages') {

                rowsPresent = true;

                if (!colForm.elements[i].checked) {

                    allChecked = false;

                    break;

                }

            }

        }

        if (rowsPresent) {

            colForm.selectAll.checked = allChecked;

        }



    }



    // This function Checks whether atleast one check box is checked or not

    // if atleast one checkbox is checked then remove button will be enabled.

    function isAnySelected() {

        var colForm = document.packageEditForm;

        var len = colForm.elements.length;

        var i = 0;

        var noOneChecked = true;



        for (i = 0; i < len; i++) {

            if (colForm.elements[i].name == 'packages' ) {

                if (colForm.elements[i].checked) {

                    noOneChecked = false;

                    break;

                }

            }

        }

        colForm.remove.disabled = noOneChecked;

    }



    //e.keyCode         = supported by IE, Opera



    //e.which           = supported by Firefox, all mozilla engine based browsers



    //e.charCode        = supported by Old Netscape browser





    function checkTypingPackageSearch(form, submitaction, e){

        var keyCode = e.keyCode ? e.keyCode : e.which ? e.which : e.charCode;

        if (keyCode == 13) {

            send(form, submitaction);

        }
    }

    function sendFolder(form, action, selection){
        var url = document.getElementById('url').value;
        document.getElementById('selection').value = selection;
        send(form, action);
    }

    function sendGoBrowse(form, action) {
        document.getElementById('selection').value = "";
        send(form, action);
    }

    function sendAddPg(form, action, url) {
        document.getElementById('addUrl').value = url;
        send(form, action);
    }
</script>



<webapps:notEmpty parameter="session_pkg_srctype">

    <logic:equal name="session_pkg_srctype" value="srctype_tx">

        <webapps:helpContext context="spm" topic="ar_pkg_tx" />

    </logic:equal>

    <logic:notEqual name="session_pkg_srctype" value="srctype_tx">

        <webapps:helpContext context="spm" topic="ar_pkg" />

    </logic:notEqual>

</webapps:notEmpty>

<%@ include file="/includes/endHeadSection.jsp" %>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants" %>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>



<logic:present parameter="srctype">

    <% session.setAttribute("session_pkg_srctype", request.getParameter("srctype")); %>

    <% session.removeAttribute("txlist_bean"); %>

    <% session.removeAttribute("deplist_bean"); %>

</logic:present>

<logic:notPresent parameter="srctype">

    <logic:notPresent name="session_pkg_srctype">

        <% session.setAttribute("session_pkg_srctype","srctype_tx"); %>

        <% session.removeAttribute("txlist_bean"); %>

        <% session.removeAttribute("deplist_bean"); %>

    </logic:notPresent>

</logic:notPresent>



<% //String pageBeanName = "txlist_bean"; %>

<%-- Tag included for when dealing with the transmitter listing.  These tags must

      be dealt with before we include the banner, since the banner includes a flush

  to the response --%>

<logic:equal name="session_pkg_srctype" value="srctype_tx">

    <% //pageBeanName = "txlist_bean"; %>

    <bean:define id="pageBeanName" value="txlist_bean" toScope="request" />



    <webapps:empty parameter="page">

        <logic:present name="txlist_currenturl" scope="session">            
          

            <%-- This sets the paging results that used for the next and previous--%>

            <sm:setPagingResults formName="packageEditForm" beanName="<%= (String)pageBeanName %>" resultsName="txlist_listing" property="list" />

        </logic:present>

    </webapps:empty>





    <webapps:notEmpty parameter="page">

        <sm:setPagingResults formName="packageEditForm" beanName="<%= (String)pageBeanName %>" resultsName="txlist_listing" property="list" />

    </webapps:notEmpty>

</logic:equal>



<%-- Tag included for when dealing with the currently deployed.  These tags must

     be dealt with before we include the banner, since the banner includes a flush

     to the response --%>



<logic:notEqual name="session_pkg_srctype" value="srctype_tx">

    <% //pageBeanName = "pkglist_bean"; %>

    <bean:define id="pageBeanName"  value="pkglist_bean" toScope="request" />

    <webapps:empty parameter="page">

        <webapps:empty name="curpage" scope="request" >

            <sm:getDeployedPackages contentType="<%=ISubscriptionConstants.CONTENT_TYPE_APPLICATION%>" search='<%= (String) session.getAttribute("page_pkgs_dep_search") %>' stateBean="PackageNavigateForm" />

        </webapps:empty>

    </webapps:empty>

    <sm:setPagingResults formName="packageEditForm" beanName="<%= (String)pageBeanName %>" resultsName="page_pkgs_dep_rs"/>

</logic:notEqual>



<%-- Body content --%>

<html:form name="packageEditForm" action="/packageSave.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm" onsubmit="return false">

<logic:notPresent name="taskid">

    <% if(null != EmpirumContext) {%>
	<webapps:tabs tabset="ldapEmpirumView" tab="tgtview"/>
<% } else { %>
	<webapps:tabs tabset="main" tab="tgtview"/>
<% } %>

</logic:notPresent>

<logic:present name="taskid">

    <% request.setAttribute("nomenu", "true"); %>

    <webapps:tabs tabset="bogustabname" tab="noneselected"/>

</logic:present>

<div class="pageHeader" style="padding-left:25px"> <span class="title"><webapps:pageText key="Title" /></span> </div>

<logic:present name="taskid">

    <div class="pageHeader">

        <logic:present name="taskid"><span class="title"><webapps:pageText key="taskid" type="global" shared="true"/></span><bean:write name="taskid" /></logic:present>

        <logic:present name="changeid"><span class="title"><webapps:pageText key="changeid" type="global" shared="true"/></span><bean:write name="changeid" /></logic:present>

    </div>

</logic:present>

<div class="pageInfo" style="padding-left:25px">

    <table cellpadding="2" cellspacing="0" width="100%">

        <tr>

            <td valign=top width=0*><img src="/shell/common-rsrc/images/info_circle.gif" width="24" height="24" class="infoImage"></td>

            <td width="100%"><webapps:pageText key="IntroShort"/></td>

        </tr>

    </table>

</div>

<div id="pageContent" width="95%" align="center">

<table cellpadding="0" cellspacing="0" width="95%" border="0">

    <tr>

        <td valign="top">

            <logic:present name="session_multitgbool">

                <jsp:include page="/includes/target_display.jsp"/><br>

            </logic:present>

            <logic:notPresent name="session_multitgbool">

                <logic:present name="multi_trgts_pkg">

                    <logic:equal name="multi_trgts_pkg" value="true">

                        <jsp:include page="/includes/target_display.jsp"/><br>

                    </logic:equal>

                </logic:present>

                <logic:notPresent name="multi_trgts_pkg">

                    <div class="itemStatus">

                        <table cellspacing="0" cellpadding="3" border="0">

                            <tr>

                                <td valign="top"><webapps:pageText key="targets" type="colhdr" shared="true"/>: </td>

                                <logic:iterate id="target" name="session_dist" property="targets">

                                    <td align="left">

                                        <% //String tgLabel="target"; %>

                                        <bean:define id="ID" name="target" property="id" toScope="request"/>

                                        <bean:define id="Name" name="target" property="name" toScope="request"/>

                                        <bean:define id="Type" name="target" property="type" toScope="request"/>

                                        <jsp:include page="/includes/target_display_single.jsp"/>

                                    </td>

                                </logic:iterate>

                            </tr>

                        </table>

                    </div>

                </logic:notPresent>

            </logic:notPresent>

        </td>

    </tr>

</table>

<table cellSpacing="0" cellPadding="0" border="0" width="95%" align="center">

    <%@ include file="/includes/usererrors.jsp" %>

</table>



<table cellpadding="0" cellspacing="0" border="0" width="95%">

<tr>

<td valign="top" width="335px">

    <table cellpadding="0" cellspacing="0" border="0" width="98%">

        <tr>

            <td>

                <table cellpadding="0" cellspacing="0" border="0" width="100%">

                    <colgroup width="50*"/>

                    <colgroup width="50*"/>

                    <tr>

                        <td class="tableTitle"><webapps:pageText key="SelectPackage" /></td>

                        <td class="pagination" align="right">

                            <logic:present name="display_rs">

                                <jsp:include page="/includes/genPrevNext.jsp" />

                            </logic:present>

                        </td>

                    </tr>

                </table>

            </td>

        </tr>

            <%--

              If request parameter "srctype" is present copy it into a session scope bean

             named "session_srctype" (overwriting "session_srctype" if already exists).

             If request parameter "srctype" is not present, use "session_srctype".  If

             "session_srctype" is not present, create it in the session.

            --%>

        <tr class="smallButtons" valign="middle">

            <bean:define id="TYPE_TX" value="srctype_tx" />

            <bean:define id="TYPE_DEP" value="srctype_dep" />



            <logic:equal name="session_pkg_srctype" value="srctype_tx">

                <td class="tableSearchRow" colspan="2">

                    <webapps:pageText key="View" /> &nbsp;

                    <html:link page="/distribution/package_assignment.jsp" paramName="TYPE_DEP" paramId="srctype" styleClass="textWhite"><webapps:pageText key="CurrentlyDeployed" /></html:link> | <STRONG><webapps:pageText key="FromTransmitter" /></STRONG>

                </td>

            </logic:equal>



            <logic:notEqual name="session_pkg_srctype" value="srctype_tx">

                <td class="tableSearchRow" colspan="2">

                    <webapps:pageText key="View" />&nbsp;<STRONG><webapps:pageText key="CurrentlyDeployed" /></STRONG> |

                    <html:link page="/distribution/package_assignment.jsp" paramName="TYPE_TX" paramId="srctype" styleClass="textWhite"><webapps:pageText key="FromTransmitter" /></html:link>

                </td>

            </logic:notEqual>

        </tr>

        <tr><td colspan="2">

        <div width="99%" class="tableWrapper">

            <TABLE cellSpacing=0 cellPadding=0 border="0" width="99%">

                <logic:equal name="session_pkg_srctype" value="srctype_tx">

                    <jsp:include page="/distribution/select_tx.jsp" flush="true" />

                    </logic:equal>

                    <logic:notEqual name="session_pkg_srctype" value="srctype_tx">

                        <jsp:include page="/distribution/select_dep.jsp" flush="true" />

                    </logic:notEqual>

        </div>

        </td></tr>

    </table>

</td>

<td style="padding-left:10px;" valign="top">

    <table cellpadding="0" cellspacing="0" width="100%">

        <tr>

            <td valign="top">

                <table cellpadding="0" cellspacing="0" border="0" width="100%">

                    <tr valign="top">

                        <td class="tableTitle"><webapps:pageText key="PackageList" /></td>

                    </tr>

                    <tr class="smallButtons" valign="top">

                        <td class="tableRowActions">

                            <input type="button" name="remove" value="<webapps:pageText key="remove" type="button" shared="true" /> " disabled onclick="javascript:send(document.packageEditForm, '/packageRemove.do');" >

                        </td>

                        <td class="tableRowActions" align="right">

                            <webapps:pageText shared="true" type="package_assignment" key="Info"/>

                        </td>

                    </tr>

                    <tr>

                        <td valign="top" colspan="2" width="100%">

                            <div class="headerSection" style="width:100%">

                                <table cellpadding="0" cellspacing="0" width="100%">

                                    <colgroup width=0*  />

                                    <colgroup width="100%" />

                                    <tr>

                                        <td class="tableHeaderCell">

                                            <input type=checkbox name="selectAll" onClick="setChecked(checked);isAnySelected();">

                                        </td>

                                        <td class="tableHeaderCell">

                                            <webapps:pageText key="Package" />

                                        </td>

                                    </tr>

                                </table>

                            </div>

                        </td>

                    </tr>

                    <tr>

                        <td valign="top" colspan="2" width="100%" class="tableWrapper">

                            <div id="dataSection" style="overflow: auto; width: 100%; height:280px">

                                <table cellpadding="0" cellspacing="0" width="100%">

                                    <colgroup width=0* class="tableHeaderCell" />

                                    <colgroup width="100%" class="tableHeaderCell" />

                                    <logic:present name="page_pkgs" scope="session">

                                        <% int contentsRowCount = 0; %>

                                        <logic:iterate id="pkg" name="page_pkgs" property="packages">

                                            <bean:define id="pkgProp" name="pkg" property="collection" type="java.util.Map" />

                                            <tr <% if (contentsRowCount % 2 == 0){ %> class="alternateRowColor"<% } %> >

                                                <td class=rowLevel1>

                                                    <html:multibox property="packages" onclick="setCheckedAll();isAnySelected();">

                                                        <bean:write name="pkgProp" property="url" filter="false"/>

                                                    </html:multibox>

                                                </td>

                                                <td class=rowLevel1>

                                                    <img src="/shell/common-rsrc/images/package.gif" hspace="8" height="16" width="16" border="0" />

                                                    <a href="#" class="noUnderlineLink" style="cursor:help;" onmouseover="return Tip('<webapps:stringescape><bean:write name="pkgProp" property="url" filter="false"/></webapps:stringescape>', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);"

                                                       onmouseout="return UnTip();">

                                                        <bean:write name="pkgProp" property="title"/>

                                                    </a>

                                                </td>

                                            </tr>

                                            <% contentsRowCount++;%>

                                        </logic:iterate>

                                    </logic:present>

                                </table>

                            </div>

                        </td>

                    </tr>

                </table>

            </td>

        </tr>

        <tr>

            <td colspan="2">

                <div id="pageNav">

                    <input type="button" class="mainBtn" name="save" value=" <webapps:pageText key="ok" type="button" shared="true" /> "  onclick="javascript:document.packageEditForm.submit();">

                    &nbsp;

                    <input type="button" name="cancel" value=" <webapps:pageText key="cancel" type="button" shared="true" /> " onclick="javascript:send(document.packageEditForm, '/packageCancel.do');" >

                </div>

            </td>

        </tr>

    </table>

</td>

</tr>

</table>

</div>



</html:form>

<%@ include file="/includes/footer.jsp" %>


<%--
	Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
	Confidential and Proprietary Information of BMC Software Inc.
	Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
	6,381,631, and 6,430,608. Other Patents Pending.

	$File$

    @author Angela Saval
    @version 1.31, 03/17/2003
--%>
<%@ page contentType="text/html;charset=UTF-8"%>

<%@ include file="/includes/directives.jsp"%>
<%@ include file="/includes/startHeadSection.jsp"%>
<%@ include file="/includes/endHeadSection.jsp"%>
<%@ page import="com.marimba.apps.subscriptionmanager.intf.IWebAppConstants"%>
<%@ include file="/includes/info.jsp"%>
<%@ page import="com.marimba.webapps.intf.IMapProperty"%>
<script type="text/javascript" src="/spm/js/application.js"></script>
<%
Object EmpirumContext = session.getAttribute(IWebAppConstants.EMPIRUM_APP_MAIN);
%>
<!-- <script type="text/javascript" src="/spm/js/jquery.min.js"></script> -->
<webapps:helpContext context="spm" topic="pi_config" />

<%-- Javascript --%>
<script language="JavaScript">

$(function () { 
	$('#settings').addClass('nav-selected');
});

function vInspectorUrlRequired(flag) {

	if(isEmpty($('input[name="value(publishurl)"]').val())) {
		$('#vInspector-url-error').html("<webapps:pageText key="empty.vInspector" />");
		if(!flag) {
			$('html, body').animate({
			    scrollTop: $('#pluginStatusCard').offset().top
			}, 2000);
		}
		flag = true;
	} else {
		$('#vInspector-url-error').html("");
	}
	return flag;
}

function vDefUrlRequired(flag) {
	if(isEmpty($('input[name="value(securityinfo.url)"]').val())) {
		$('#vDef-url-error').html("<webapps:pageText key="empty.vDef" />");
		if(!flag) {
  		$('html, body').animate({
          scrollTop: $('#vInspectorCard').offset().top
      }, 2000);
		}
		flag = true;
	} else {
		$('#vDef-url-error').html("");
	}
	return flag;
}

function cveDownloaderRequired(flag) {
	if(isEmpty($('input[name="value(cvedownloader.url)"]').val())) {
		$('#cve-downloader-url-error').html("<webapps:pageText key="empty.cveDownloader" />");
		if(!flag) {
  		$('html, body').animate({
          scrollTop: $('#vDefCard').offset().top
      }, 2000);
		}
		flag = true;
	} else {
		$('#cve-downloader-url-error').html("");
	}
	return flag;
}

function vMediateUrlRequired(flag) {
	if(isEmpty($('input[name="value(customscanner.url)"]').val())) {
		$('#vMediate-url-error').html("<webapps:pageText key="empty.vMediate" />");
		if(!flag) {
  		$('html, body').animate({
          scrollTop: $('#cveCard').offset().top
      }, 2000);
		}
		flag = true;
	} else {
		$('#vMediate-url-error').html("");
	}
	return flag;
}

function dbDetailsRequired(flag) {
	
	let hostname = $('input[name="value(db.hostname)"]').val();
	let port = $('input[name="value(db.port)"]').val();
	let sid = $('input[name="value(db.name)"]').val();
	let uname = $('input[name="value(db.username)"]').val();
	let pass = $('input[name="value(db.password)"]').val();
	
	if(isEmpty(hostname) || isEmpty(port) || isEmpty(sid) || isEmpty(uname) || isEmpty(pass)) {
		$('#db-error').html("<webapps:pageText key="empty.dbDetails" />");
		if(!flag) {
  		$('html, body').animate({
          scrollTop: $('#vMediateCard').offset().top
      }, 2000);
		}
		flag = true;
	} else {
		$('#db-error').html("");
	}
	return flag;
}

function dbConnectionRequired(flag) {
	
	let min = $('input[name="value(db.thread.min)"]').val();
	let max = $('input[name="value(db.thread.max)"]').val();
	
	if(isEmpty(min) || isEmpty(max)) {
		$('#db-connection-error').html("<webapps:pageText key="empty.dbConnections" />");
		if(!flag) {
  		$('html, body').animate({
          scrollTop: $('#dbCard').offset().top
      }, 2000);
		}
		flag = true;
	} else {
		$('#db-connection-error').html("");
	}
	return flag;
}

function elasticUrlRequired(flag) {
	
	if(isEmpty($('input[name="value(elasticurl)"]').val())) {
		$('#elastic-url-error').html("<webapps:pageText key="empty.elasticInsertion" />");
		if(!flag) {
  		$('html, body').animate({
          scrollTop: $('#repeaterCard').offset().top
      }, 2000);
		}
		flag = true;
	} else {
		$('#elastic-url-error').html("");
	}
	return flag;
}

function ldapRequired(flag) {
	
	let hostname = $('input[name="value(ldaphost)"]').val();
	let basedn = $('input[name="value(basedn)"]').val();
	let binddn = $('input[name="value(binddn)"]').val();
	let poolsize = $('input[name="value(poolsize)"]').val();
	let bindpasswd = $('#bindpasswd').val();
	
	if(isEmpty(hostname) || isEmpty(basedn) || isEmpty(binddn) || isEmpty(poolsize) || isEmpty(bindpasswd)) {
		$('#ldap-error').html("<webapps:pageText key="empty.ldapDetails" />");
		if(!flag) {
  		$('html, body').animate({
          scrollTop: $('#elasticCard').offset().top
      }, 2000);
		}
		flag = true;
	} else {
		$('#bindpasswd2').val(bindpasswd);
		$('#ldap-error').html("");
	}
	return flag;
}


function doSubmit(frm) {
	
	let flag = false;
	
	flag = vInspectorUrlRequired(flag);
	flag = vDefUrlRequired(flag);
	flag = cveDownloaderRequired(flag);
	flag = vMediateUrlRequired(flag);
	flag = dbDetailsRequired(flag);
	flag = dbConnectionRequired(flag);
	flag = ldapRequired(flag);
	
	if(!flag) 
		frm.submit();
}

function isEmpty(str) {
	if(typeof str != "undefined" && str != null && str.trim() != "") 
		return false; 
	else 
		return true;
}

function redirect(submitaction) {
    top.location = "<html:rewrite page='" + submitaction + "' />";
}

$(document).ready(function() {
 var input =  $("#authError").val();
 if(input === 'true')
 $('#bindpasswd').val("");
});

function checkHosts() {	
	let hostname = $('input[name="value(ldaphost)"]').val();
	let basedn = $('input[name="value(basedn)"]').val();
	let binddn = $('input[name="value(binddn)"]').val();
	let poolsize = $('input[name="value(poolsize)"]').val();
	let bindpasswd = $('#bindpasswd').val();
	
	if(isEmpty(hostname) || isEmpty(basedn) || isEmpty(binddn) || isEmpty(poolsize) || isEmpty(bindpasswd)) {
		$('#ldap-error').html("<webapps:pageText key="empty.ldapDetails" />");
		$('html, body').animate({
        scrollTop: $('#elasticCard').offset().top
    }, 2000);
	} else {
		$('#ldap-error').html("");
		$('#bindpasswd2').val(bindpasswd);
		send(document.forms[0], '/checkHosts.do');
	}
}

// restrict a key press to only integers
function restrictKeyPressInteger(evt) {
    var key;
    var keychar;
    var oldval;
    if (window.event) {
        key = window.event.keyCode;
        oldval = window.event.srcElement.value;
    } else if (evt) {
        key = evt.which;
        oldval = evt.target.valueOf();
    } else {
        return true;
    }
    keychar = String.fromCharCode(key);

    if ((-1 == ("0123456789".indexOf(keychar))) && (key != "8") && (key != 0)) {
        // digits only
        return false;
    }

    return true;
}

// restrict a key press to only to positive values
function restrictKeyPressPositive(evt) {
    var key;
    var keychar;
    var oldval;
    if (window.event) {
        key = window.event.keyCode;
        oldval = window.event.srcElement.value;
    } else if (evt) {
        key = evt.which;
        oldval = evt.target.valueOf();
    } else {
        return true;
    }
    keychar = String.fromCharCode(key);

    if (-1 == ("123456789".indexOf(keychar))) {
        // digits only
        return false;
    }

    return true;
}

// this function checks that the thread pool size is between
// 1 and 100
function checkBounds() {
    count = document.getElementById('poolsize').value;
    if((count > 0) && (count <= 100)) {
        return true;
    }
    alert("<webapps:pageText key="poolSizeBoundsError" />");
    document.forms.setPluginForm.poolsize.value=document.forms.setPluginForm.prepoolsize.value;
    document.getElementById('poolsize').focus();
    document.getElementById('poolsize').select();
}


// Symbio added 05/18/2005: *** Start ***
// this function checks that the tread expiration time is a valid number
function checkNum() {
    count = document.getElementById('exptime').value;
    if(count > 0) {
        return true;
    }
    document.getElementById('exptime').focus();
    document.getElementById('exptime').select();
    alert("<webapps:pageText key="exptimeInvalidNumber" />");
}
// Symbio added 05/18/2005: *** End ***


// this function checks that the password and the confirm password are the same

/* function checkPasswords() {

    pass1 = document.getElementById('bindpasswd').value;
    pass2 = document.getElementById('bindpasswd2').value;

    if(pass1 == pass2) {
        return true;
    }
    document.getElementById('bindpasswd2').value="";
    document.getElementById('bindpasswd').focus();
    document.getElementById('bindpasswd').select();
    alert("<webapps:pageText key="PasswordMatchError" />");
    return false;

}
 */
function setCheckBoxValue(eid, propertyname) {
    if(eid.checked) {
        document.forms.setPluginForm[propertyname].value='true';
    } else {
        document.forms.setPluginForm[propertyname].value='false';
    }
}
 
/* function changePasswordLdap(form,formPwdElem) {
    form['value(changedPassword)'].value="true";
    formPwdElem.value="";
} */

function checkElasticServerStatus(){
	var elasticurl = $('#elastic_url').val();
	
	if(isEmpty(elasticurl)) {
		$('#elastic_server_status').html("<span class=\"redText\"><webapps:pageText key="empty.elasticInsertion" /></span>");
	}	else {
		$('#elastic_server_status').html("<div class=\"spinner-border spinner-border-sm text-primary\" role=\"status\"><span class=\"visually-hidden\">Loading...</span></div>");
  	$.ajax({
          type: "GET", dataType: "json", url: "/spm/checkElasticStatus.do",
          data: {elasticurl : elasticurl},
          success: function (data) {
         			if(data.elasticstatus == "Up") {
         				$('#elastic_server_status').html('<span class="greenText">Server is Up and running</span>');}
         			else if(data.elasticstatus == "Down") {
         				$('#elastic_server_status').html('<span class="redText">Server is currently Unavailable</span>');}
         			else {
         				$('#elastic_server_status').html('<span class="redText">Please Check Elastic Insertion URL  </span>');}
          	}
    	});
	}
}
</script>



<%-- Body content --%>
<body>

  <html:form name="setPluginForm" action="/pluginSave.do?showAll=false" type="com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm">
    <html:hidden property="value(prevPage)" />
    <html:hidden property="value(changedPassword)" value="false" />
    <html:hidden property="value(changedPublishPwd)" value="false" />
    <html:hidden property="value(changedSubscribePwd)" value="false" />
    <html:hidden property="value(changedInfoSubPassword)" value="false" />
    <html:hidden property="value(changedCustomSubPassword)" value="false" />

    <main id="main" class="main">
      <div class="pagetitle">

        <div class="d-flex bd-highlight justify-content-center">
          <div class="p-2 flex-grow-1 bd-highlight">
            <span class="pagename"><webapps:pageText key="Title" /></span> <span data-bs-toggle="tooltip" data-bs-placement="right"><i class="fa-solid fa-circle-info text-primary"></i></span>
          </div>
          <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh" style="cursor: pointer;">
            <i class="fa-solid fa-arrows-rotate"></i>
          </div>
          <div class="p-2 bd-highlight text-primary align-self-center">
            <a href="/shell/dashboard.do"> <i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home
            </a>
          </div>
        </div>
      </div>

      <section class="section dashboard">

        <table border="0" cellspacing="1" cellpadding="5">
          <tr>
            <td colspan="2">
              <%-- Errors Display --%>
              <table width="90%">
                <%@ include file="/includes/usererrors.jsp"%>
                <logic:present scope="request" name="errors">
                  <div class="statusMessage" id="critical">
                    <h6>
                      <webapps:text key="page.usererrors.beforeproceeding" />
                    </h6>
                    <p>
                    <ul>
                      <logic:iterate id="error" name="errors">
                        <li><%=error%></li>
                      </logic:iterate>
                    </ul>
                    </p>
                  </div>
                </logic:present>
              </table>

              <div class="card">
                <div class="card-body">
                  <div class="card-title">

                    <webapps:pageText key="LastPublishedTime" />
                    <logic:present name="setPluginForm" property="value(lastpublishedtime)" scope="session">
                      <webapps:writeDate name="setPluginForm" property="value(lastpublishedtime)" />
                    </logic:present>

                    <logic:notPresent name="setPluginForm" property="value(lastpublishedtime)" scope="session">
                      <webapps:pageText key="NotYetPublished" />
                    </logic:notPresent>

                  </div>
                  <%@ include file="/includes/help.jsp"%>
                </div>
              </div>


              <div class="card" id="pluginStatusCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="pluginStatus.hdr" />
                  </div>
                  <webapps:pageText key="pluginStatus.sectInfo" />

                  <br /> <br /> <span class="textGeneral"><webapps:pageText key="pluginStatus" /></span>

                  <html:select property="value(pluginStatus)">
                    <html:option value="enable">
                      <webapps:pageText key="plugin.enable" />
                    </html:option>
                    <html:option value="disable">
                      <webapps:pageText key="plugin.disable" />
                    </html:option>
                  </html:select>
                </div>
              </div>

              <div class="card" id="vInspectorCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="securityplugin.hdr" />
                    <span id="vInspector-url-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>
                  <webapps:pageText key="securityplugin.sectInfo" />
                  <br /> <br />

                  <table border="0" cellspacing="1" cellpadding="5">

                    <tr>
                      <td align="right" valign="top"><webapps:pageText key="SubscriptionURL" /></td>
                      <td valign="top"><webapps:errorsPresent property="publishurl">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:text property="value(publishurl)" styleId="port2" size="65" maxlength="110" styleClass="requiredField" /> <webapps:txbrowser field="port2" styleId="typeurlBrowse" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="PublishUserName" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="publishurl.username">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:text property="value(publishurl.username)" size="20" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="PublishPassword" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="publishPasswd">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:password name="setPluginForm" property="publishPasswd" styleId="ppasswd" onfocus="changePublishPassword(document.forms.setPluginForm['value(changedPublishPwd)'],document.forms.setPluginForm,document.getElementById('ppasswd'))'])" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="SubscribeUserName" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="publishurl.subscribeuser">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:text property="value(publishurl.subscribeuser)" size="20" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="SubscribePassword" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="subscribePasswd">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:password name="setPluginForm" property="subscribePasswd" styleId="spasswd" onfocus="changePublishPassword(document.forms.setPluginForm['value(changedSubscribePwd)'],document.forms.setPluginForm,document.getElementById('spasswd'))'])" styleClass="optionalField" /></td>
                    </tr>
                  </table>
                </div>
              </div>

              <div class="card" id="vDefCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="securityinfo.hdr" />
                    <span id="vDef-url-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>

                  <webapps:pageText key="securityinfo.sectInfo" />
                  <br /> <br />
                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="security.info.channel" /></td>
                      <td valign="top"><html:text property="value(securityinfo.url)" styleId="port3" size="65" maxlength="110" styleClass="requiredField" /> <webapps:txbrowser field="port3" styleId="securityInfoBrowse" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="infoSubscribeUserName" /></span></td>
                      <td valign="top"><html:text property="value(securityinfo.subscribeuser)" size="20" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="infoSubscribePassword" /></span></td>
                      <td valign="top"><html:password name="setPluginForm" property="securityInfoPassword" styleId="securityInfoPassword" onfocus="changePublishPassword(document.forms.setPluginForm['value(changedInfoSubPassword)'],document.forms.setPluginForm,document.getElementById('securityInfoPassword'))'])" styleClass="optionalField" /></td>
                    </tr>
                  </table>
                </div>
              </div>

              <div class="card" id="cveCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="cvedownloader.hdr" />
                    <span id="cve-downloader-url-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>

                  <webapps:pageText key="cvedownloader.sectInfo" />
                  <br /> <br />
                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="cvedownloder.info.channel" /></td>
                      <td valign="top"><html:text property="value(cvedownloader.url)" styleId="port4" size="65" maxlength="110" styleClass="requiredField" /> <webapps:txbrowser field="port4" styleId="cvedownloaderBrowse" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="cveSubscribeUserName" /></span></td>
                      <td valign="top"><html:text property="value(cvedownloader.subscribeuser)" size="20" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="cveSubscribePassword" /></span></td>
                      <td valign="top"><html:password name="setPluginForm" property="cvedownloaderPassword" styleId="cvedownloaderPassword" onfocus="changePublishPassword(document.forms.setPluginForm['value(changedCveSubPassword)'],document.forms.setPluginForm,document.getElementById('cvedownloaderPassword'))'])" styleClass="optionalField" /></td>
                    </tr>
                  </table>
                </div>
              </div>

              <div class="card" id="vMediateCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="customscanner.hdr" />
                    <span id="vMediate-url-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>
                  <webapps:pageText key="customscanner.sectInfo" />
                  <br /> <br />
                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="customscanner.channel" /></td>
                      <td valign="top"><html:text property="value(customscanner.url)" styleId="port4" size="65" maxlength="110" styleClass="requiredField" /> <webapps:txbrowser field="port4" styleId="customScannerBrowse" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="customScannerSubUserName" /></span></td>
                      <td valign="top"><html:text property="value(customscanner.subscribeuser)" size="20" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="customScannerSubPassword" /></span></td>
                      <td valign="top"><html:password name="setPluginForm" property="customScannerPassword" styleId="customScannerPassword" onfocus="changePublishPassword(document.forms.setPluginForm['value(changedCustomSubPassword)'],document.forms.setPluginForm,document.getElementById('customScannerPassword'))'])" styleClass="optionalField" /></td>
                    </tr>
                  </table>
                </div>
              </div>

              <div class="card" id="bdCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="database.title" />
                    <span id="db-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>

                  <webapps:pageText key="database.info" />

                  <br /> <br />
                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="dbtype" /></td>
                      <td valign="top"><html:select styleId="dbType" property="value(db.type)">
                          <html:option value="oracle">
                            <webapps:pageText key="db.oracle" />
                          </html:option>
                          <!--<html:option value="oracle_rac"><webapps:pageText key="db.oraclerac"/></html:option> -->
                          <html:option value="sqlserver">
                            <webapps:pageText key="db.sqlserver" />
                          </html:option>
                        </html:select></td>
                    </tr>

                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="db.hostname" /></td>
                      <td valign="top"><html:text property="value(db.hostname)" styleClass="requiredField" size="20" /></td>
                    </tr>

                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="db.port" /></td>
                      <td valign="top"><html:text property="value(db.port)" styleClass="requiredField" size="20" /></td>
                    </tr>

                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="db.name" /></td>
                      <td valign="top"><html:text property="value(db.name)" styleClass="requiredField" size="20" /></td>
                    </tr>

                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="db.username" /></td>
                      <td valign="top"><html:text property="value(db.username)" styleClass="requiredField" size="20" /></td>
                    </tr>

                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="db.password" /></td>
                      <td valign="top"><html:password property="value(db.password)" styleClass="requiredField" size="20" /></td>
                    </tr>
                  </table>
                </div>
              </div>

              <div class="card" id="dbConnectionsCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="db.connection.title" />
                    <span id="db-connection-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>

                  <webapps:pageText key="db.connection.title.info" />
                  <br /> <br />
                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="db.min.connection" /></td>
                      <td valign="top"><html:text property="value(db.thread.min)" maxlength="2" styleClass="requiredField" size="3" /></td>
                    </tr>
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="db.max.connection" /></td>
                      <td valign="top"><html:text property="value(db.thread.max)" maxlength="3" styleClass="requiredField" size="3" /></td>
                    </tr>
                  </table>
                </div>
              </div>

              <div class="card" id="repeaterCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="repeater.title" />

                  </div>

                  <webapps:pageText key="repeater.title.info" />
                  <br /> <br />
                  <html:hidden property="value(repeaterInsert)" />
                  <logic:equal name="setPluginForm" property="value(repeaterInsert)" value="true">
                    <input type="checkbox" name="value(repeaterInsert2)" checked value="true" onclick="setCheckBoxValue(this, 'value(repeaterInsert)');" />
                  </logic:equal>
                  <logic:equal name="setPluginForm" property="value(repeaterInsert)" value="false">
                    <input type="checkbox" name="value(repeaterInsert2)" value="true" onclick="setCheckBoxValue(this, 'value(repeaterInsert)');" />
                  </logic:equal>
                  <font class="textGeneral"><webapps:pageText key="repeater.label" /></font>
                </div>
              </div>

              <div class="card" id="elasticCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="elastic.title" />
                    <span id="elastic-url-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>

                  <webapps:pageText key="elastic.title.info" />

                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="elastic.url" /></td>
                      <td valign="top"><html:text property="value(elasticurl)" styleId="elastic_url" size="65" maxlength="110" styleClass="requiredField" /></td>
                    </tr>

                    <tr class="textGeneral">
                      <td align="right" valign="middle"><webapps:pageText key="testelasticserver" /></td>
                      <td valign="middle"><input type="button" value="<webapps:pageText shared="true" type="button" key="go" />" onClick="checkElasticServerStatus();">&nbsp; <span id="elastic_server_status"></span></td>
                    </tr>
                  </table>

                </div>
              </div>

              <div class="card" id="ldapCard">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="ldap.hdr" />
                    <span id="ldap-error" class="text-danger" style="float: right; font-size: medium;"></span>
                  </div>

                  <webapps:pageText key="ldap.sectInfo" />

                  <br /> <br /> <a name="LDAP"></a>
                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="top">
                      <td align="right" width="200"><span class="textGeneral"> <webapps:pageText key="DirectoryType" /></span></td>
                      <td nowrap><span class="textGeneral"> <webapps:pageText key='<%="Vendor" + ((IMapProperty) session.getAttribute("setPluginForm")).getValue("vendor")%>' />
                      </span></td>
                    </tr>

                    <logic:equal name="setPluginForm" property="value(canDisplayBaseDN)" value="true">
                      <tr valign="middle">
                        <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="HostnamePort" /></span></td>
                        <td valign="top"><webapps:errorsPresent property="ldaphost">
                            <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                          </webapps:errorsPresent> <logic:equal name="setPluginForm" property="disableModify" value="true">*******</logic:equal> <logic:equal name="setPluginForm" property="disableModify" value="false">
                            <html:text property="value(ldaphost)" size="65" maxlength="125" styleClass="requiredField" />
                          </logic:equal></td>
                      </tr>
                      <tr valign="middle">
                        <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="BaseDN" /></span></td>
                        <td valign="top"><webapps:errorsPresent property="basedn">
                            <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                          </webapps:errorsPresent> <html:text property="value(basedn)" size="50" maxlength="100" styleClass="requiredField" /></td>
                      </tr>
                    </logic:equal>
                    <logic:equal name="setPluginForm" property="value(vendor)" value="ActiveDirectory">
                      <tr valign="middle">
                        <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="authmethod" /></span></td>
                        <td valign="top"><webapps:errorsPresent property="authmethod">
                            <img src="<webapps:fullPath path="/images/errorsmall.gif"/>" width="19" height="16" border="0">
                          </webapps:errorsPresent> <html:select property="value(authmethod)">
                            <html:option value="simple">
                              <webapps:pageText key="authSimple" />
                            </html:option>
                            <html:option value="kerberos">
                              <webapps:pageText key="authKerberos" />
                            </html:option>
                          </html:select></td>
                      </tr>
                    </logic:equal>
                    <tr valign="middle">
                      <td align="right" width="200">&nbsp;</td>
                      <td><webapps:errorsPresent property="usessl">
                          <img src="<webapps:fullPath path="/images/errorsmall.gif"/>" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:hidden property="value(usessl)" /> <logic:equal name="setPluginForm" property="value(usessl)" value="true">
                          <input type="checkbox" name="value(usessl2)" checked value="true" checked onclick="setCheckBoxValue(this, 'value(usessl)');" />
                        </logic:equal> <logic:equal name="setPluginForm" property="value(usessl)" value="false">
                          <input type="checkbox" name="value(usessl2)" value="true" onclick="setCheckBoxValue(this, 'value(usessl)');" />
                        </logic:equal> <font class="textGeneral"><webapps:pageText key="UseSSL" /></font></td>
                    </tr>
                    <tr valign="middle">
                      <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="BindDN" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="binddn">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:text property="value(binddn)" styleClass="requiredField" size="50" maxlength="100" /></td>
                    </tr>
                    <tr valign="middle">
                      <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="Password" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="bindpasswd">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:password property="bindpasswd" name="setPluginForm" styleClass="requiredField" styleId="bindpasswd" onfocus="changePassword(document.forms.setPluginForm,document.getElementById('bindpasswd'),document.getElementById('bindpasswd2'))"/></td>
                    </tr>
                    <tr valign="middle" style="display:none;">
                      <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="ConfirmPassword" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="bindpasswd2">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:password property="bindpasswd2" name="setPluginForm" styleClass="requiredField" styleId="bindpasswd2" /></td>
                    </tr>
                    <tr valign="middle">
                      <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="LDAPPoolSize" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="poolsize">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <input type='hidden' name='prepoolsize' value='<bean:write name="setPluginForm" property="value(poolsize)"/>'> <html:text property="value(poolsize)" styleId="poolsize" styleClass="requiredField" size="6" maxlength="100" onkeypress="return restrictKeyPressInteger(event)" onblur="checkBounds()" /></td>
                    </tr>
                    <tr valign="middle">
                      <td align="right" width="200" valign="top"><span class="textGeneral"><webapps:pageText key="LastGoodHostExpTime" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="lastgoodhostexptime">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <%-- html:text property="value(lastgoodhostexptime)" styleClass="optionalField" size="12" maxlength="6" onkeypress="return restrictKeyPressInteger(event)"/ --%> <html:text property="value(lastgoodhostexptime)" styleId="exptime" styleClass="optionalField" size="12" maxlength="6" onkeypress="return restrictKeyPressInteger(event)" onblur="checkNum()" /> <%-- Symbio modified 05/18/2005 --%></td>
                    </tr>
                    <tr class="textGeneral">
                      <td align="right" valign="middle">&nbsp;</td>
                      <td valign="middle">&nbsp;</td>
                    </tr>
                    <tr class="textGeneral">
                      <td align="right" valign="middle"><webapps:pageText key="testdirectory" /></td>
                      <td valign="middle"><a name="go_loc"></a> <input type="button" value="<webapps:pageText shared="true" type="button" key="go" />" onClick="checkHosts();"></td>
                    </tr>
                    <logic:present name="host_test_results">
                      <tr>
                        <td align="right">&nbsp;</td>
                        <td>
                          <table cellspacing="2" cellpadding="2" border="0">
                            <logic:iterate id="host" name="host_test_results" type="com.marimba.webapps.tools.util.PropsBean">
                              <tr>
                                <logic:equal name="setPluginForm" property="disableModify" value="false">
                                  <td width="0*" align="left" valign="top"><span class="textGeneral"> <bean:write name="host" property="value(hostName)" />:
                                  </span></td>
                                </logic:equal>
                                <td><logic:equal name="host" property="value(status)" value="Success">
                                    <font class="greenText"><webapps:pageText key="Success" /></font>
                                  </logic:equal> <logic:equal name="host" property="value(status)" value="Failed">
                                    <span class="errorText"> <logic:equal name="host" property="value(failure_detail)" value="2">
                                        <webapps:pageText key="FailConnect" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="3">
                                        <webapps:pageText key="FailPassword" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="4">
                                        <webapps:pageText key="FailSubConfig" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="5">
                                        <webapps:pageText key="SearchFailedSubConfig" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="6">
                                        <webapps:pageText key="FailedWrongPortForGC" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="7">
                                        <webapps:pageText key="FailedNoPortForGC" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="8">
                                        <webapps:pageText key="FailedBasedn" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="9">
                                        <webapps:pageText key="FailUnknown" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="10">
                                        <webapps:pageText key="userwrongpwd" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="11">
                                        <webapps:pageText key="userlogonfailure"/>
                                        <input type="text" style="display:none" id="authError" value="true">
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="12">
                                        <webapps:pageText key="nosuchuser" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="13">
                                        <webapps:pageText key="invalidlogonhours" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="14">
                                        <webapps:pageText key="invalidworkstation" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="15">
                                        <webapps:pageText key="passwordexpired" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="16">
                                        <webapps:pageText key="accountdisabled" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="17">
                                        <webapps:pageText key="accountexpired" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="18">
                                        <webapps:pageText key="passwordmustchange" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="19">
                                        <webapps:pageText key="useraclockedout" />
                                      </logic:equal> <logic:equal name="host" property="value(failure_detail)" value="20">
                                        <webapps:pageText key="usersessionexists" />
                                      </logic:equal>
                                    </span>
                                  </logic:equal></td>
                              </tr>
                            </logic:iterate>
                          </table>
                        </td>
                      </tr>

                    </logic:present>
                  </table>
                </div>
              </div>

              <div class="card">
                <div class="card-body">
                  <div class="card-title">
                    <webapps:pageText key="endpoint.title" />
                  </div>

                  <webapps:pageText key="endpoint.sectInfo" />
                  <br />
                  <div class="card-title">
                    <webapps:pageText key="endpoint.vulnerability.title" />
                  </div>

                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr valign="middle">
                      <td align="right" valign="top"><webapps:pageText key="endpoint.vulnerability.cvefilters.dir" /></td>
                      <td valign="top"><html:text property="value(cveFiltersDir)" styleId="cve_filters_dir" size="65" /></td>
                    </tr>
                  </table>

                </div>
              </div>

              <div class="col" style="text-align: right;">
                <input type="button" onclick="doSubmit(this.form)" class="mainBtn btn btn-sm btn-primary" value="<webapps:pageText shared="true" type="button" key="preview"/>" /> <input type="button" class="btn btn-sm btn-outline-primary" value="<webapps:pageText shared="true" type="button" key="cancel"/>" onClick="javascript:send(document.setPluginForm,'/pluginCancel.do');">
              </div>

              </section>
              </main> </html:form> <%@ include file="/includes/footer.jsp"%>
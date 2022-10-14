<%@ page contentType="text/html;charset=UTF-8"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <title>E-mail Settings Configuration</title>
    <link rel="stylesheet" type="text/css" href="/shell/common-rsrc/css/main.css"/>
    <link rel="stylesheet" type="text/css" href="/spm/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="/spm/includes/assets/adminlte/css/adminlte.min.css">
    <link rel="stylesheet" type="text/css" href="/spm/css/application.css"/>

    <style type="text/css">
        select {border: 1px solid #ccc; height: 32px; padding: 5px; width: auto; border-radius: 5px;}
        .table > tbody > tr > td:first-child {text-align: right; white-space: nowrap; vertical-align: middle;}
        .form-control {width: 30%;}
    </style>
    <script type="text/javascript" src="/spm/js/jquery.min.js"></script>
    <script type="text/javascript" src="/spm/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="/spm/js/bootstrap-dialog.min.js"></script>
    <script type="text/javascript" src="/spm/js/application.js"></script>
</head>
<body>
<webapps:tabs tabset="main" tab="cfgview"/>
<section class="content-header">
    <h1><bean:message key="page.email.link"/></h1>
</section>
<section class="content">
<html:form action="/mailconfig.do" type="com.marimba.apps.securitymgr.webapp.forms.MailConfigForm">
<html:hidden property="action"/>
<div class="row">
    <div class="col-sm-12">
        <div class="pageInfo" style="padding: 5px 5px 25px;"><webapps:text key="page.email.intro"/></div>
        <logic:messagesPresent>
            <div class="box box-danger box-solid">
                <div class="box-header"><h3 class="box-title"><bean:message key="errors.intro"/></h3></div>
                <div class="box-body"><ul><html:messages id="error"><li><bean:write name="error"/></li></html:messages></ul></div>
            </div>
        </logic:messagesPresent>
        <div class="box box-warning box-solid" id="warning" style="display:none;">
            <div class="box-header"><h3 class="box-title"><bean:message key="page.email.removeconfirm.header"/></h3></div>
            <div class="box-body">
                <bean:message key="page.email.removeconfirm.msg"/>
                <div class="pull-right">
                    <input type="button" class="btn bg-aqua-active" onclick="doSubmit(this.form, 'removeEmail');" value='<bean:message key="page.global.yes"/>'>&nbsp;&nbsp;
                    <input type="button" class="btn btn-default" onclick="doSubmit(this.form, 'cancelRemove');" value='<bean:message key="page.global.no"/>'>&nbsp;
                </div>
            </div>
        </div>
    </div>
</div>
<div class="row">
<div class="col-sm-12">
<table class="table no-border">
<colgroup width="20%"/><colgroup width="*"/>
<tr>
    <td><webapps:text key="page.email.host"/></td>
    <td>
        <html:text property="host" styleClass="form-control requiredField"/>
        <webapps:errorsPresent property="host">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="host"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td><webapps:text key="page.email.port"/></td>
    <td>
        <html:text property="port" maxlength="5" styleClass="form-control requiredField"/>
        <webapps:errorsPresent property="port">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="port"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td><webapps:text key="page.email.encryption"/></td>
    <td>
        <html:radio property="encryption" value="none"/>&nbsp;<bean:message key="page.email.encryption.none"/>&nbsp;
        <html:radio property="encryption" value="tls"/>&nbsp;<bean:message key="page.email.encryption.tls"/>&nbsp;
        <html:radio property="encryption" value="ssl"/>&nbsp;<bean:message key="page.email.encryption.ssl"/>
    </td>
</tr>
<tr>
    <td><html:checkbox property="useAuth" styleId="useAuth" value="true"/></td>
    <td><webapps:text key="page.email.useAuth"/></td>
</tr>
<tr>
    <td>&nbsp;</td>
    <td>
        <webapps:text key="page.email.user"/>&nbsp;
        <html:text property="user" styleId="user" styleClass="form-control"/>
        <webapps:errorsPresent property="user">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="user"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td>&nbsp;</td>
    <td>
        <webapps:text key="page.email.password"/>&nbsp;
        <html:password property="password" size="20" styleId="password" styleClass="form-control"/>
        <webapps:errorsPresent property="password">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="password"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td><webapps:text key="page.email.sendermail"/></td>
    <td>
        <html:text property="senderMail" styleClass="form-control requiredField"/>
        <webapps:errorsPresent property="senderMail">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="senderMail"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td><webapps:text key="page.email.sendername"/></td>
    <td>
        <html:text property="senderName" styleClass="form-control"/>
        <webapps:errorsPresent property="senderName">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="senderName"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td><webapps:text key="page.email.bccmail"/></td>
    <td>
        <html:textarea property="bccMail" cols="65" rows="2" styleClass="form-control"/>
        <webapps:errorsPresent property="bccMail">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="bccMail"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td><webapps:text key="page.email.receivermail"/></td>
    <td>
        <html:textarea property="receiverMails" cols="65" rows="4" styleClass="form-control requiredField"/>
        <webapps:errorsPresent property="receiverMails">
            <img src="/shell/common-rsrc/images/error_sm.gif">
            <html:messages id="error" property="receiverMails"><span class="txt_red"><bean:write name="error"/></span></html:messages>
        </webapps:errorsPresent>
    </td>
</tr>
<tr>
    <td><input type="button" class="btn bg-aqua-active" id="btn1" onclick="doSubmit(this.form, 'testEmail');" value="Send Test Mail"></td>
    <td>
        <a name="test_result"></a>
        <logic:present scope="request" name="testResult">
            <script type="text/javascript">document.location.hash = 'test_result'</script>
            <logic:equal name="testResult" value="ok">
                <img src="/shell/common-rsrc/images/check_confirm.gif">
                <span class="txt_green"><b>Test mail sent successfully by using the above configuration.</b></span>
            </logic:equal>
            <logic:equal name="testResult" value="not_ok">
                <img src="/shell/common-rsrc/images/error_sm.gif">
                <span class="txt_red"><b>Unable to send test mail by using the above configuration.</b>
                <logic:present name="exceptionMsg">&nbsp; Exception: <bean:write name="exceptionMsg"/></logic:present>
                </span>
            </logic:equal>
        </logic:present>
    </td>
</tr>
</table>
</div>
</div>
<div class="row">
    <div class="col-sm-12">
        <div id="pageNav">
            <input type="button" class="btn bg-aqua-active" id="btn1" onclick="doSubmit(this.form, 'setEmail');" value="<bean:message key="page.global.save"/>"> &nbsp;
            <input type="button" class="btn bg-aqua-active" id="btn2" onclick="doSubmit(this.form, 'removeConfirm');" value="<bean:message key="page.global.remove"/>"> &nbsp;
            <input type="button" class="btn btn-default" id="btn3" onclick="doSubmit(this.form, 'done');" value="<bean:message key="page.global.cancel"/>"> &nbsp;
        </div>
    </div>
</div>
</html:form>
</section>
</body>
<script type="text/javascript">
    $(document).ready(function() {
        var checked = $('#useAuth').prop('checked');
        $('#user, #password').prop('disabled', !checked);
    });

    $('#useAuth').click(function() {
        $('#user, #password').prop('disabled', !this.checked);
    });

    function doSubmit(form, act) {
        if ('removeConfirm' == act) {
            $('#warning').slideToggle();
            showHideButtons(["btn1", "btn2", "btn3"], true);
        } else if ('cancelRemove' == act) {
            $('#warning').slideToggle();
            showHideButtons(["btn1", "btn2", "btn3"], false);
        } else {
            form.action.value = act;
            form.submit();
        }
    }
    function showHideButtons(btns, visibility) {
        for (var x = 0 in btns) $('#' + btns[x]).prop('disabled', visibility);
    }

</script>
</html>
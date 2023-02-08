<%@ page contentType="text/html;charset=UTF-8"%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<%@ taglib uri="/WEB-INF/app.tld" prefix="sm"%>
<!DOCTYPE html>
<%@ include file="/includes/startHeadSection.jsp"%>

<style type="text/css">
select {
	border: 1px solid #ccc;
	height: 32px;
	padding: 5px;
	width: auto;
	border-radius: 5px;
}

.table>tbody>tr>td:first-child {
	text-align: right;
	white-space: nowrap;
	vertical-align: middle;
}

.form-control {
	width: 30%;
}

.box.box-solid.box-danger {
	border: 1px solid #dd4b39;
	border-radius: 3px;
	width: 100%;
	box-shadow: 0 1px 1px rgb(0 0 0/ 10%);
	"
}

.box.box-solid.box-danger>.box-header {
	background: #dd4b39;
	background-color: #dd4b39;
	display: block;
	padding: 12px;
	position: relative;
}

.box-header .box-title {
	color: white;
	display: inline-block;
	font-family: 'Source Sans Pro', sans-serif;
	font-size: 18px;
	font-family: inherit;
	font-weight: 500;
	line-height: 1.1;
	margin-bottom: 0.1em;
}

.box-body {
	font-size: 14px;
	border-top-left-radius: 0;
	border-top-right-radius: 0;
	border-bottom-right-radius: 3px;
	border-bottom-left-radius: 3px;
	padding: 10px;
}
</style>
<%@ include file="/includes/endHeadSection.jsp"%>
<body>



	<html:form action="/mailconfig.do"
		type="com.marimba.apps.securitymgr.webapp.forms.MailConfigForm">
		<html:hidden property="action" />
		<main id="main" class="main">
			<div class="pagetitle">

				<div class="d-flex bd-highlight justify-content-center">
					<div class="p-2 flex-grow-1 bd-highlight">
						<span class="pagename"><bean:message key="page.email.link" /></span>
						<span data-bs-toggle="tooltip" data-bs-placement="right" title=""><i
							class="fa-solid fa-circle-info text-primary"></i></span>
					</div>
					<div
						class="refresh p-2 bd-highlight text-primary align-self-center"
						data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh"
						style="cursor: pointer;">
						<i class="fa-solid fa-arrows-rotate"></i>
					</div>
					<div class="p-2 bd-highlight text-primary align-self-center"
						data-bs-toggle="tooltip" data-bs-placement="right"
						title="Download" style="cursor: pointer;">
						<i class="fa-solid fa-download"></i>
					</div>
					<div class="p-2 bd-highlight text-primary align-self-center">
						<a href="/shell/dashboard.do"> <i
							class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS
							Home
						</a>
					</div>
				</div>

			</div>
			<section class="section dashboard">

				<div class="card">
					<br>
					<div class="card-body">

						<div class="row">
							<div class="col-sm-12">
								<div class="pageInfo">
									<div class="p-2 mb-2 text-dark"
										style="padding: 3px 3px 12px; font-size: medium; background-color: #d9edf7;">
										<webapps:text key="page.email.intro" />
									</div>
									<logic:messagesPresent>
										<div class="box box-danger box-solid">

											<div class="box-header">
												<h3 class="box-title">
													<bean:message key="errors.intro" />
												</h3>
											</div>

											<div class="box-body">
												<ul>
													<html:messages id="error">
														<li><bean:write name="error" /></li>
													</html:messages>
												</ul>
											</div>
										</div>
									</logic:messagesPresent>
								</div>
							</div>
						</div>
						<br />
						<div class="row">
							<div class="col-sm-12">
								<table class="table table-borderless">
									<colgroup width="20%" />
									<colgroup width="*" />
									<tr>
										<td><webapps:text key="page.email.host" /></td>
										<td style="text-align: left"><html:text property="host"
												styleClass="form-control requiredField" /> <webapps:errorsPresent
												property="host">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="host">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>
									<tr>
										<td><webapps:text key="page.email.port" /></td>
										<td><html:text property="port" maxlength="5"
												styleClass="form-control requiredField" /> <webapps:errorsPresent
												property="port">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="port">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>
									<tr>
										<td><webapps:text key="page.email.encryption" /></td>
										<td style="text-align: left;"><html:radio
												property="encryption" value="none" />&nbsp;<bean:message
												key="page.email.encryption.none" />&nbsp; <html:radio
												property="encryption" value="tls" />&nbsp;<bean:message
												key="page.email.encryption.tls" />&nbsp; <html:radio
												property="encryption" value="ssl" />&nbsp;<bean:message
												key="page.email.encryption.ssl" /></td>
									</tr>
									<tr>
										<td><html:checkbox property="useAuth" styleId="useAuth"
												value="true" /></td>
										<td style="text-align: left;"><webapps:text
												key="page.email.useAuth" /></td>
									</tr>
									<tr>
										<td>&nbsp;</td>
										<td style="text-align: left;"><webapps:text
												key="page.email.user" />&nbsp; <html:text property="user"
												styleId="user" style="width:30%" styleClass="form-control" />
											<webapps:errorsPresent property="user">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="user">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>
									<tr>
										<td>&nbsp;</td>
										<td style="text-align: left;"><webapps:text
												key="page.email.password" />&nbsp; <html:password
												property="password" size="20" style="width:30%"
												styleId="password" styleClass="form-control" /> <webapps:errorsPresent
												property="password">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="password">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>
									<tr>
										<td><webapps:text key="page.email.sendermail" /></td>
										<td style="text-align: left"><html:text
												property="senderMail"
												styleClass="form-control requiredField" /> <webapps:errorsPresent
												property="senderMail">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="senderMail">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>
									<tr>
										<td><webapps:text key="page.email.sendername" /></td>
										<td><html:text property="senderName"
												styleClass="form-control" /> <webapps:errorsPresent
												property="senderName">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="senderName">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>
									<tr>
										<td><webapps:text key="page.email.bccmail" /></td>
										<td><html:textarea property="bccMail" cols="65" rows="2"
												styleClass="form-control" /> <webapps:errorsPresent
												property="bccMail">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="bccMail">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>
									<tr>
										<td><webapps:text key="page.email.receivermail" /></td>
										<td style="text-align: left"><html:textarea
												property="receiverMails" cols="65" rows="4"
												styleClass="form-control requiredField" /> <webapps:errorsPresent
												property="receiverMails">
												<img src="/shell/common-rsrc/images/error_sm.gif">
												<html:messages id="error" property="receiverMails">
													<span class="txt_red"><bean:write name="error" /></span>
												</html:messages>
											</webapps:errorsPresent></td>
									</tr>



									<tr>
										<td><input type="button"
											class="btn btn-sm btn-primary mainBtn" id="btn1"
											onclick="doSubmit(this.form, 'testEmail');"
											value="Send Test Mail"></td>
										<td><a name="test_result"></a> <logic:present
												scope="request" name="testResult">
												<script type="text/javascript">
													document.location.hash = 'test_result'
												</script>
												<logic:equal name="testResult" value="ok">
													<img src="/shell/common-rsrc/images/check_confirm.gif">
													<span class="txt_green"><b>Test mail sent
															successfully by using the above configuration.</b></span>
												</logic:equal>
												<logic:equal name="testResult" value="not_ok">
													<img src="/shell/common-rsrc/images/error_sm.gif">
													<span class="txt_red"><b>Unable to send test
															mail by using the above configuration.</b> <logic:present
															name="exceptionMsg">&nbsp; Exception: <bean:write
																name="exceptionMsg" />
														</logic:present> </span>
												</logic:equal>
											</logic:present></td>
									</tr>


								</table>
							</div>
						</div>

						<hr style="border-bottom: 2px solid black;">

						<div class="row">
							<div class="col-sm-12">
								<div id="pageNav">
									<!--  class="btn bg-aqua-active", "btn btn-default" -->
									<input type="button" class="btn btn-sm btn-primary mainBtn"
										style="position: absolute; right: 150px" id="btn1"
										onclick="doSubmit(this.form, 'setEmail');"
										value="<bean:message key="page.global.save"/>"> &nbsp;
									<input type="button" class="btn btn-sm btn-outline-primary"
										data-bs-toggle="modal"
										data-bs-target="#removeConfirmationModal"
										style="position: absolute; right: 80px" id="btn2"
										onclick="doSubmit(this.form, 'removeConfirm');"
										value="<bean:message key="page.global.remove"/>">
									&nbsp; <input type="button"
										class="btn btn-sm btn-outline-primary" style="float: right;"
										id="btn3" onclick="doSubmit(this.form, 'done');"
										value="<bean:message key="page.global.cancel"/>">
									&nbsp;

								</div>
							</div>

						</div>
					</div>
				</div>
			</section>
		</main>
		<div class="modal fade" id="removeConfirmationModal" tabindex="-1"
			aria-labelledby="removeConfirmationModalLabel" aria-hidden="true"
			style="display: none;">

			<div class="modal-dialog modal-lg">
				<div class="modal-content">
					<div class="modal-header">
						<h5 class="modal-title" id="removeConfirmationModalLabel">
							<bean:message key="page.email.removeconfirm.header" />
						</h5>
						<button type="button" class="btn-close" data-bs-dismiss="modal"
							aria-label="Close"></button>
					</div>
					<div class="modal-body">
						<bean:message key="page.email.removeconfirm.msg" />
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-sm btn-primary"
							onclick="doSubmit(this.form, 'removeEmail');">
							<bean:message key="page.global.yes" />
						</button>
						<button type="button" class="btn btn-sm btn-outline-primary"
							data-bs-dismiss="modal"
							onclick="doSubmit(this.form, 'cancelRemove');">
							<bean:message key="page.global.no" />
						</button>


					</div>
				</div>
			</div>
		</div>
	</html:form>


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
			showHideButtons([ "btn1", "btn2", "btn3" ], true);
		} else if ('cancelRemove' == act) {
			$('#warning').slideToggle();
			showHideButtons([ "btn1", "btn2", "btn3" ], false);
		} else {
			form.action.value = act;
			form.submit();
		}
	}
	function showHideButtons(btns, visibility) {
		for ( var x = 0 in btns)
			$('#' + btns[x]).prop('disabled', visibility);
	}
</script>
</html>
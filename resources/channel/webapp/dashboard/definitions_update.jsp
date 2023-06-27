<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>

<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>

<!DOCTYPE html>
<html lang="en">

<head>
<title>Definitions Updates</title>

<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css" />
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/smart_wizard_all.min.css" />

<script type="text/javascript" src="/shell/common-rsrc/js/master.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/jquery.smartWizard.min.js"></script>

<script type="text/javascript">

	$(function() {
		
		$('#definitionsUpdate').addClass('nav-selected');
		
		startSmartWizard();
		
		var alertModal = new bootstrap.Modal(document.getElementById('alertModal'), {
			  keyboard: false
		});
		
		$('#cveUpdateNow').click(function() {
			/* 
			let cveStorageDir = $('input[name="cveStorageDir"]').val();
			let prevCveStorageDir = '<bean:write name="definitionUpdateForm" property="cveStorageDir" filter="false" />';
			
			if(cveStorageDir.replace('\\\\','\\') != prevCveStorageDir) {
				$('#alertMessage').html('Can not use the Restart option after changing CVE download location path.');
				alertModal.show();
			} else {				
				cveUpdateNow("regular");
			} */
			cveUpdateNow("regular");
		});
		
		$('#cveForceUpdate').click(function() {
			cveUpdateNow("force");
		});

		const cveJsonLastUpdated = '<bean:write name="definitionUpdateForm" property="cveJsonLastUpdated" filter="false" />'
		$('#cveJsonLastUpdated').html(cveJsonLastUpdated);
		if(cveJsonLastUpdated == "Not Updated") {
			$('#cveJsonLastUpdated').addClass('text-danger');
		} else {
			$('#cveJsonLastUpdated').addClass('text-success');
		}
		
		const vdefLastUpdated = '<bean:write name="definitionUpdateForm" property="vdefLastUpdated" filter="false" />';
		$('#vdefLastUpdated').html(vdefLastUpdated);
		if(vdefLastUpdated == "Not Updated") {
			$('#vdefLastUpdated').addClass('text-danger');
		} else {
			$('#vdefLastUpdated').addClass('text-success');
		}
		
		const step = <bean:write name="definitionUpdateForm" property="cveJsonUpdateStep" filter="false" />;
		
		const msg = '<bean:write name="definitionUpdateForm" property="cveJsonUpdateMsg" filter="false" />';
		const msgDivId = '#step-'+(step+1)+'-msg';
		$(msgDivId).html(msg);
		
		const error = '<bean:write name="definitionUpdateForm" property="cveJsonUpdateError" filter="false" />';
		const divId = '#step-'+(step+1)+'-error';
		$(divId).html(error);
		
		const vDefError = '<bean:write name="definitionUpdateForm" property="vDefError" filter="false" />';
		$('#vDefError').html(vDefError);
		
		const isThreadRunning = <bean:write name="definitionUpdateForm" property="cveJsonUpdateThreadRunning" filter="false" />;
		
		if(isThreadRunning) {
			$('#cveUpdateNow').prop('disabled', true);
			$('#cveForceUpdate').prop('disabled', true);
			$('.processing').show();
			asyncCall();
		} else {
			if(step == 0) {
				$('#cveUpdateNow').prop('disabled', true);
				$('#cveForceUpdate').val('Update Now');
			} else if(step == 6) {
				$('#cveUpdateNow').prop('disabled', true);
				$('#cveForceUpdate').val('Start Over');
			} else {
				$('#cveUpdateNow').prop('disabled', false);
	  		$('#cveForceUpdate').val('Start Over');
			}
			$('.processing').hide();
		}
		

		setTimeout(() => {
  		$('#smartwizard').smartWizard("goToStep", step);
		}, 250);
		
	});
	
	
	function checkAfter10Seconds(timeout) {
		
		console.log("timeout = "+timeout);
		console.log(new Date());
		
			  return new Promise(resolve => {
		    setTimeout(() => {		    	
			    	$.ajax({
							url : './definitionupdate.do',
							type : 'POST',
							dataType : 'text json',
							data : {
								action : "getCveUpdateStatus"
							},
							beforeSend : function() {
							},
							complete : function(xhr, status) {},
							success : function(response) {
								resolve(response);
							}
						});  
		    	
		    }, timeout);
		  });
		}

		async function asyncCall() {
			let timeout = 500;
			
		  while(1) {
  		  const result = await checkAfter10Seconds(timeout);
  			timeout = 10000;
  		  
  			// Expected output: "resolved"
  			
  			if(typeof result != "undefined" && typeof result.status != "undefined" && result.status != null) {
  				$('#smartwizard').smartWizard("goToStep", result.status);
  				if(result.status == 6) {
  					console.log("Completed");
  					$('#cveUpdateNow').prop('disabled', false);
  					$('#cveForceUpdate').prop('disabled', false);
  					$('.processing').hide();
  					break;
  				}
  		  }
  		  
  		  if(result.status > 0) {
  				$('#cveForceUpdate').val('Start Over');
  		  }
  		  
  			const msgDivId = '#step-'+(result.status+1)+'-msg';
				$(msgDivId).html(result.message);
  		
  			const divId = '#step-'+(result.status+1)+'-error';
  			
  			if(result.status == 0 && result.error != "") {
    			var alertModal = new bootstrap.Modal(document.getElementById('alertModal'), {
  				  keyboard: false
      		});
      		$('#alertMessage').html(result.error);
      		alertModal.show();
  			}
				
    		if(typeof result != "undefined" && typeof result.error != "undefined" && result.error != null && result.error.trim() != "") {
  				$(divId).html(result.error);
  				$('#cveUpdateNow').prop('disabled', false);
  				$('#cveForceUpdate').prop('disabled', false);
  				$('.processing').hide();
    			break;
    		} else {
    			$(divId).text("");
    		} 		  
  		  
			}
		}

		function cveUpdateNow(updateType) {
			
			$('#smartwizard').smartWizard("reset");

			let cveStorageDir = $('input[name="cveStorageDir"]').val();
			
			if (typeof cveStorageDir != "undefined" && cveStorageDir != null
					&& cveStorageDir.trim() != "") {
				
				$('#cveUpdateNow').prop('disabled', true);
				$('#cveForceUpdate').prop('disabled', true);
				$('.processing').show();
				
				var wizardStep = 0;
				
			    	$.ajax({
							url : './definitionupdate.do',
							type : 'POST',
							dataType : 'text json',
							data : {
								action : "getCveUpdateStatus"
							},
							beforeSend : function() {
							},
							complete : function(xhr, status) {},
							success : function(response) {
								
								if(typeof response != "undefined" && response != null) {
									
				  				if(updateType != "force") {
				  					
				  					const msgDivId = '#step-'+(response.status+1)+'-msg';
				  					$(msgDivId).html(response.message);
				  				
				  					const divId = '#step-'+(response.status+1)+'-error';
				  					if(typeof response != "undefined" && typeof response.error != "undefined" && response.error != null && response.error.trim() != "") {
				  						$(divId).html(response.error);
				  	    		} else {
				  	    			$(divId).text("");
				  	    		}
				  					
				  					wizardStep = (typeof response != "undefined" && typeof response.status != "undefined" && response.status != null) ? response.status : 0;
				  					$('#smartwizard').smartWizard("goToStep", wizardStep);
				  				}
				  				else {
				  					wizardStep = 0;
				  				}
				  				
				  				$.ajax({
				  					url : './definitionupdate.do',
				  					type : 'POST',
				  					dataType : 'text json',
				  					data : {
				  						action : 'update_cvejson',
				  						cveStorageDir : cveStorageDir,
				  						updateCvejsonStartStep:	wizardStep
				  					},
				  					beforeSend : function() {
				  					},
				  					complete : function(xhr, status) {
				  					},
				  					success : function(response) {
				  						asyncCall();
				  					}
				  				}); //end update_cvejson
								}
							}
				  }); // end getCveUpdateStatus

			} else {
				var alertModal = new bootstrap.Modal(document.getElementById('alertModal'), {
					  keyboard: false
				});
				$('#alertMessage').html('CVE download location should not be empty.');
				alertModal.show();
			}
		}
		
		
		
		function startSmartWizard() {
			
			$('#smartwizard').smartWizard({
	        selected: 0,
	        autoAdjustHeight: true,
	        theme: 'square', // basic, arrows, square, round, dots
	        transition: {
	          //animation:'slideHorizontal' // none|fade|slideHorizontal|slideVertical|slideSwing|css
	        },
	        toolbar: {
	          showNextButton: false, // show/hide a Next button
	          showPreviousButton: false, // show/hide a Previous button
	        },
	    		keyboard : {
	    			keyNavigation : false
	    		},
	        anchor: {
	            enableNavigation: false, // Enable/Disable anchor navigation 
	            enableNavigationAlways: false, // Activates all anchors clickable always
	            enableDoneState: true, // Add done state on visited steps
	            markPreviousStepsAsDone: true, // When a step selected by url hash, all previous steps are marked done
	            unDoneOnBackNavigation: true, // While navigate back, done state will be cleared
	            enableDoneStateNavigation: false // Enable/Disable the done state navigation
	        },
	        disabledSteps: [], // Array Steps disabled
	        errorSteps: [], // Highlight step with errors
	        hiddenSteps: [], // Hidden steps
	        // getContent: (idx, stepDirection, selStep, callback) => {
	        //   console.log('getContent',selStep, idx, stepDirection);
	        //   callback('<h1>'+idx+'</h1>');
	        // }
	    });
		}

	function doSubmit(frm, action) {
		
		
		let publishTxUrl = $('input[name="publishTxUrl"]').val();
		
		if (typeof publishTxUrl != "undefined" && publishTxUrl != null
			&& publishTxUrl.trim() != "") {
			frm.action.value = action;
			frm.submit();
		} else {
			var alertModal = new bootstrap.Modal(document.getElementById('alertModal'), {
				  keyboard: false
			});
			$('#alertMessage').html('Master transmitter URL should not be empty.');
			alertModal.show();
		}
	}


</script>

<style>
.table>thead>tr>th {
	text-align: left;
	vertical-align: middle;
}

.table>tbody>tr>td {
	text-align: left;
	vertical-align: middle;
}
</style>
</head>

<body>

  <jsp:include page="header.jsp" />
  <jsp:include page="sidebar.jsp" />

  <html:form name="definitionUpdateForm" action="/definitionupdate.do" type="com.marimba.apps.subscriptionmanager.webapp.forms.DefinitionUpdateForm" onsubmit="return false;">

    <html:hidden property="action" />

    <main id="main" class="main">
      <div class="pagetitle">

        <div class="d-flex bd-highlight justify-content-center">
          <div class="p-2 flex-grow-1 bd-highlight">
            <span class="pagename">Definitions Update</span> <span data-bs-toggle="tooltip" data-bs-placement="right" title="Definitions Update"><i class="fa-solid fa-circle-info text-primary"></i></span>
          </div>
          <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh" style="cursor: pointer;">
            <i class="fa-solid fa-arrows-rotate"></i>
          </div>
          <div class="p-2 bd-highlight text-primary align-self-center">
            <a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS Home</a>
          </div>
        </div>

      </div>


      <section class="section dashboard">

          <nav style="background-color: #fff;">
            <div class="nav nav-tabs nav-title" id="nav-tab" role="tablist">
              <button class="nav-link active" id="nav-home-tab" data-bs-toggle="tab" data-bs-target="#nav-home" type="button" role="tab" aria-controls="nav-home" aria-selected="true" style="background-color: #fff; z-index:1;">CVE and Vulnerabilities Definitions Updates</button>
              <a href="/spm/updates.do" class="nav-link">Repository Updates</a>
            </div>
          </nav>

        <div class="tab-content" id="nav-tabContent">
          <div class="tab-pane fade show active" id="nav-home" role="tabpanel" aria-labelledby="nav-home-tab">

            <div class="card">
              <div class="card-body">

                <br />

                <div class="p-2 mb-2 text-dark" style="font-size: medium; background-color: #d9edf7;">
                  <i class="fa-solid fa-circle-info text-primary"></i> For assessing your end points against the latest CVE’s and Vulnerabilities, it is recommended to always have the latest CVE and Vulnerability data.
                </div>


                <h5 class="card-title">
                  CVE Definitions <span style="float: right; font-size: medium;">Last updated on: <span id="cveJsonLastUpdated" class="fw-bold"></span></span>
                </h5>
                <hr class="divider" />
                <div class="p-2 mb-2 text-dark">
                  <i class="fa-solid fa-circle-info text-primary"></i> Please ensure the CVE’s information is up-to-date to verify your end points against the latest published CVE’s.
                </div>

                <div class="row g-3 align-items-center">
                  <div class="col-auto">
                    <label for="definitionUpdateForm" class="col-form-label"><webapps:pageText key="cvejson.storagedir" /></label>
                  </div>
                  <div class="col-auto">
                    <webapps:errorsPresent property="cveStorageDir">
                      <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                    </webapps:errorsPresent>
                    <html:text name="definitionUpdateForm" property="cveStorageDir" size="30" styleClass="requiredField form-control" />
                  </div>
                </div>

                <div class="row">
                  <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                    <input type="button" id="cveForceUpdate" class="btn btn-sm btn-primary" value="Start Over">
                    <input type="button" id="cveUpdateNow" class="btn btn-sm btn-outline-primary" value="Restart from Failed Point">
                  </div>
                </div>


                <!-- SmartWizard html -->
                <div id="smartwizard" style="margin-top: 30px;">
                  <ul class="nav nav-progress">
                    <li class="nav-item"><a class="nav-link">
                        <div class="num">1</div> Start
                    </a></li>
                    <li class="nav-item"><a class="nav-link"> <span class="num">2</span> Download
                    </a></li>
                    <li class="nav-item"><a class="nav-link"> <span class="num">3</span> Unzip
                    </a></li>
                    <li class="nav-item"><a class="nav-link"> <span class="num">4</span> CVE Downloader
                    </a></li>
                    <li class="nav-item"><a class="nav-link"> <span class="num">5</span> CSV Update
                    </a></li>
                    <li class="nav-item"><a class="nav-link"> <span class="num">6</span> DB Update
                    </a></li>
                    <li class="nav-item"><a class="nav-link"> <span class="num">7</span> Completed
                    </a></li>
                  </ul>

                  <div class="tab-content">
                    <div id="step-1" class="tab-pane cveJsonMessage" role="tabpanel" aria-labelledby="step-1">
                      <div id="step-1-msg"></div>
                      <div id="step-1-error" style="display:none;" class="text-danger"></div>
                    </div>
                    <div id="step-2" class="tab-pane cveJsonMessage" role="tabpanel" aria-labelledby="step-2">
                      <div class="d-flex align-items-center">
                        <div id="step-2-msg"></div>
                        <div class="processing text-primary spinner-border ms-auto" role="status" aria-hidden="true"></div>
                      </div>
                      <div id="step-2-error" class="text-danger"></div>
                    </div>
                    <div id="step-3" class="tab-pane cveJsonMessage" role="tabpanel" aria-labelledby="step-3">
                      <div class="d-flex align-items-center">
                        <div id="step-3-msg"></div>
                        <div class="processing text-primary spinner-border ms-auto" role="status" aria-hidden="true"></div>
                      </div>
                      <div id="step-3-error" class="text-danger"></div>
                    </div>
                    <div id="step-4" class="tab-pane cveJsonMessage" role="tabpanel" aria-labelledby="step-4">
                      <div class="d-flex align-items-center">
                        <div id="step-4-msg"></div>
                        <div class="processing text-primary spinner-border ms-auto" role="status" aria-hidden="true"></div>
                      </div>
                      <div id="step-4-error" class="text-danger"></div>
                    </div>
                    <div id="step-5" class="tab-pane cveJsonMessage" role="tabpanel" aria-labelledby="step-5">
                      <div class="d-flex align-items-center">
                        <div id="step-5-msg"></div>
                        <div class="processing text-primary spinner-border ms-auto" role="status" aria-hidden="true"></div>
                      </div>
                      <div id="step-5-error" class="text-danger"></div>
                    </div>
                    <div id="step-6" class="tab-pane cveJsonMessage" role="tabpanel" aria-labelledby="step-6">
                      <div class="d-flex align-items-center">
                        <div id="step-6-msg"></div>
                        <div class="processing text-primary spinner-border ms-auto" role="status" aria-hidden="true"></div>
                      </div>
                      <div id="step-6-error" class="text-danger"></div>
                    </div>
                    <div id="step-7" class="tab-pane cveJsonMessage" role="tabpanel" aria-labelledby="step-7">
                      <div id="step-7-msg"></div>
                      <div id="step-7-error" class="text-danger"></div>
                    </div>
                  </div>

                  <!-- Include optional progressbar HTML -->
                  <div class="progress">
                    <div class="progress-bar" role="progressbar" style="width: 0%" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"></div>
                  </div>

                </div>

              </div>
            </div>

            <div class="card">
              <div class="card-body">

                <h5 class="card-title">
                  Vulnerability and Patch Definitions Update Settings <span id="vDefError" class="text-danger fw-bold" style="margin-right:10px"></span> <span style="float: right; font-size: medium;">Last updated on: <span id="vdefLastUpdated" class="fw-bold"></span></span>
                </h5>
                <hr class="divider" />
                <div class="p-2 mb-2 text-dark">
                  <i class="fa-solid fa-circle-info text-primary"></i> Please ensure the Vulnerability and Patch information is up-to-date to verify your end points against the latest published Vulnerability and Patch definitions. <br /> <i class="fa-solid fa-circle-info text-primary"></i> Specify the Master Transmitter where the vDef channel shall be published.
                </div>

                <div class="row" style="margin-top: 20px;">
                  <table border="0" cellspacing="1" cellpadding="5">
                    <tr>
                      <td align="right" valign="top"><webapps:pageText key="mastertxurl" /></td>
                      <td valign="top"><webapps:errorsPresent property="publishTxUrl">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:text name="definitionUpdateForm" property="publishTxUrl" size="30" maxlength="110" styleClass="requiredField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="PublishUserName" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="publishUserName">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:text name="definitionUpdateForm" property="publishUserName" size="25" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="PublishPassword" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="publishPassword">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:password name="definitionUpdateForm" property="publishPassword" size="25" styleId="ppasswd" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="ChannelStoreUserName" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="channelStoreUserName">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:text name="definitionUpdateForm" property="channelStoreUserName" size="25" styleClass="optionalField" /></td>
                    </tr>
                    <tr>
                      <td align="right" valign="top"><span class="textGeneral"><webapps:pageText key="ChannelStorePassword" /></span></td>
                      <td valign="top"><webapps:errorsPresent property="channelStorePassword">
                          <img src="/shell/common-rsrc/images/errorsmall.gif" width="19" height="16" border="0">
                        </webapps:errorsPresent> <html:password name="definitionUpdateForm" property="channelStorePassword" size="25" styleId="spasswd" styleClass="optionalField" /></td>
                    </tr>
                  </table>
                </div>

                <div class="row">
                  <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                    <input type="button" id="vdefpublish" onclick="doSubmit(this.form, 'update_vdef')" class="btn btn-sm btn-primary" value="UPDATE NOW">
                  </div>
                </div>

              </div>

            </div>
          </div>
        </div>


      </section>
    </main>
    
    <div class="modal fade" id="alertModal" tabindex="-1" aria-labelledby="alertModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="alertModalLabel">Alert</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div id="alertMessage"></div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal">OK</button>
          </div>
        </div>
      </div>
    </div>
    
  </html:form>
</body>
</html>
var types = [BootstrapDialog.TYPE_DEFAULT, BootstrapDialog.TYPE_INFO, BootstrapDialog.TYPE_PRIMARY,
    BootstrapDialog.TYPE_SUCCESS, BootstrapDialog.TYPE_WARNING, BootstrapDialog.TYPE_DANGER];

var pleaseWaitPannel = $("<div class='modal' id='processDialog' data-backdrop='static' data-keyboard='false'><div class='modal-dialog'><div class='modal-content'><div class='modal-header'><b>Processing...</b></div><div class='modal-body'><div class='progress'><div class='progress-bar progress-bar-striped active' role='progressbar' aria-valuenow='100' aria-valuemin='100' aria-valuemax='100' style='width:100%'>Please Wait...</div></div></div></div></div></div>");
var default_timeout = 5 * 60 * 1000;
var nextYear = new Date();
nextYear.setTime(nextYear.getTime() + (365 * 24 * 60 * 60 * 1000));//use this date object as the default expiration for Marimba cookies

var app = app || (function () {
    return {
        initApp: initApp,
        showPleaseWait: function () {pleaseWaitPannel.modal('show');},
        hidePleaseWait: function () {pleaseWaitPannel.modal('hide');},
        showSpinner: showSpinner,
        closeSpinner: closeSpinner
    };
})();

function setRequestTimeout(timeout) {
    default_timeout = parseInt(timeout);
}

function initApp() {
    doCleanUp();
    $.ajaxSetup({
        cache: false,
        headers: {'x-marimba-appdirect-header': 'marimba-vdesk'},
        beforeSend: function(xhr) {},
        error: function (xhr, status, error) {ajaxErrorHandler(xhr, status, error);},
        dataFilter: function (origdata, type) {
            if (origdata.indexOf('<title id="logout_title">') >= 0) {
                redirectPage('/shell/common-rsrc/login/login.jsp?logout=true');
                return false;
            }
            return origdata;
        }
    });
    $(document)
            .ajaxStart(function() {showSpinner('content');})
            .ajaxComplete(/*function() {closeSpinner('content');}*/)
            .ajaxStop(function() {closeSpinner('content');})
            .ajaxError(function() {closeSpinnerForce('content');});
}
function doCleanUp() {
    //    $.fn.tooltipster('setDefaults', {
    //        contentAsHTML: true, minWidth: 50, maxWidth: 200
    //    });
}
function showSpinner(divName) {
    var spinnerDivStyle = '<style type="text/css" id="style_'+divName+'"> #'+divName+' .processing{background: rgba(255, 255, 255, 0.7) none repeat scroll 0 0;z-index: 2500;}#'+divName+' > .processing, .overlay-wrapper > .processing, #'+divName+' > .loading-img, .overlay-wrapper > .loading-img .loading-img {top: 0;left: 0;width: 100%;height: 100%;position: absolute;} #'+divName+' .processing > .fa, .overlay-wrapper .processing > .fa {left: '+parseInt(($('#' + divName).height()/2*100)/$('#' + divName).height())+'%;top: '+parseInt(($('#' + divName).height()/2*100)/$('#' + divName).height())+'%;color: #3c8dbc;font-size: 50px;margin-left: -15px;margin-top: -15px;position: absolute;}</style>';
    $('#' + divName).append(spinnerDivStyle);$('#' + divName).append('<div class="processing"><i class="fa fa-refresh fa-spin"></i></div>');
}
function closeSpinner(divName) {if ($.active == 0) $('#style_' + divName).remove();$('#' + divName + ' .processing').remove();}
function closeSpinnerForce(divName) {$('#style_' + divName).remove();$('#' + divName + ' .processing').remove();}
function ajaxErrorHandler(xhr, status, error) {
    var responseTitle= $(xhr.responseText).filter('title').get(0);
    var statusMsg = '';
    if (status === 0) {
        statusMsg = 'Server Not connected.\nPlease verify your network connection.';
    } else if (status === 403) {
        window.location = '/';
    } else if (status === 404) {
        statusMsg = 'The requested page not found. [404]';
    } else if (status === 500) {
        statusMsg = 'Server request failed; please check the logs';
    } else if (error === 'parsererror') {
        statusMsg = 'Requested JSON parse failed; please check the logs.';
    } else if (error === 'timeout') {
        statusMsg = 'Server request Timed out: unable to process the task within the time out interval';
    } else if (error === 'abort') {
        statusMsg = 'Server request aborted; please check the logs';
    }
    if (statusMsg != '') {
        alertError($(responseTitle).text(), statusMsg);
    }
}
function loadHtmlPage(pageUrl, targetDiv, urlParams, callBackFunction) {
    urlParams = (undefined == urlParams) ? {} : urlParams;
    $.ajax({
        url: pageUrl, data: urlParams, type: "GET", dataType: "html",
        success: function (data) {
            $('#' + targetDiv).html('').html(data);
            if (callBackFunction != undefined) callBackFunction();
        },
        timeout: default_timeout
    });
}
function loadHtmlPageWithAuth(pageUrl, targetDiv, urlParams, loginSuccessPage, callBackFunction) {
    urlParams = (undefined == urlParams) ? {} : urlParams;
    $.ajax({
        url: pageUrl, data: urlParams, type: "GET", dataType: "html", beforeSend: function() {},
        success: function (data) {
            if (data.toLowerCase().indexOf("auth_required") >= 0) {
                if ($.parseJSON(data).auth_required) invokeLoginDialog(loginSuccessPage);
            } else {
                $('#' + targetDiv).html('').html(data);
                if (callBackFunction != undefined) callBackFunction();
            }
        },
        timeout: default_timeout
    });
}
function redirectPage(url) {
    var ua = navigator.userAgent.toLowerCase();
    var isIE = ua.indexOf('msie') !== -1;
    var version = parseInt(ua.substr(4, 2), 10);

    if (isIE && version < 9) { // Internet Explorer 8 and lower
        var link = document.createElement('a');
        link.href = url;
        document.body.appendChild(link);
        link.click();
    } else {
        window.location.href = url;
    }
}
function toggleTextDisplay() {
    var mode = true;
    var ck = getCookie('MRBA_verboseMode');
    if ('false' == ck) {
        mode = false;
    }
    mode = !mode;
    setCookie('MRBA_verboseMode', mode, nextYear);
    if (mode) $('.pageInfo, .sectionInfo').show(500);
    else $('.pageInfo, .sectionInfo').hide(500);
}
function setCookie(name, value, expires, path, domain, secure) {
    var thisCookie = name + "=" + escape(value) +
                     ((expires) ? "; expires=" + expires.toGMTString() : "") +
                     ((path) ? "; path=" + path : "; path=/") +
                     ((domain) ? "; domain=" + domain : "") +
                     ((secure) ? "; secure" : "");
    document.cookie = thisCookie;
}
function getCookie(name) {
    var C = document.cookie;
    var cookiePairs = C.split(";");
    var rtnVal = null;
    for (var i=0; i < cookiePairs.length; i++) {
        var cookieNameValue = cookiePairs[i].split("=");
        var cookieNameTrim = $.trim(cookieNameValue[0]);
        if (cookieNameTrim == name) {
            var cookieValue = cookieNameValue[1];
            return unescape(cookieValue);
        }
    }
    return rtnVal;
}

function alertSuccess(dialogTitle, dialogMessage) {
    invokeSimpleDialog(BootstrapDialog.TYPE_SUCCESS, dialogTitle, dialogMessage);
}
function alertError(dialogTitle, dialogMessage) {
    invokeSimpleDialog(BootstrapDialog.TYPE_DANGER, dialogTitle, dialogMessage);
}
function alertWarning(dialogTitle, dialogMessage) {
    invokeSimpleDialog(BootstrapDialog.TYPE_WARNING, dialogTitle, dialogMessage);
}
function alertInfo(dialogTitle, dialogMessage) {
    invokeSimpleDialog(BootstrapDialog.TYPE_INFO, dialogTitle, dialogMessage);
}
function invokeSimpleDialog(type, dialogTitle, dialogMessage) {
    BootstrapDialog.show({
        type: type, title: dialogTitle, message: dialogMessage, buttons: [{label: 'Close', action: function(dialogRef) {dialogRef.close();}}]
    });
}
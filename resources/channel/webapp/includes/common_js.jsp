<%--
  Used to submit the form to a specific action instead of submitting to the "action"
  parameter on the form

  @input form the form object to submit
  @input submitaction the relative path of the action of the submitted form, e.g. "/packageSave.do"
--%>

<script language="JavaScript" type="text/javascript">

function send(form, submitaction) {
    form.action = "<html:rewrite page='" + submitaction + "' />";
<%-- saveAutoComplete(form); --%>
    form.submit();
}

<%-- Used by select boxes to bring up the selected URL value. --%>
function loadActionFromSelection(selectbox, form) {
    var url = selectbox.options[selectbox.selectedIndex].value;
    if (url.length == "") {
        return;
    }
    if (form == null) {
        top.location = "<html:rewrite page='" + url + "' />";
    } else {
        send(form, url);
    }
}

function loadActionFromSelectionFrames(selectbox, form, frameNum) {
    var url = selectbox.options[selectbox.selectedIndex].value;
    if (url.length == "") {
        return;
    }
    if (form == null || form == '') {
        parent.frames[frameNum].location = "<html:rewrite page='" + url + "' />";
    } else {
        submitActionFromFrames(form, url, frameNum);
    }
}

function submitActionFromFrames(form, url, frameNum) {
    form.target = parent.frames[frameNum].name;
    send(form, url);
}

<%--
Used to clear out the confirm password when the password is modified.
This is needed because when we return a password for editing, it is in
base64 encoding.  If the user wishes to change it, then they must
also confirm the password
--%>
function changePassword(form,formPwdElem, formPwdConfirmElem) {
    form['value(changedPassword)'].value="true";
    formPwdElem.value="";
    formPwdConfirmElem.value="";
}

function changePublishPassword(changeprop,form,formPwdElem) {
    changeprop.value="true";
    formPwdElem.value="";
}

<%--
  Captures user's typing event. Submits the specified action through the
  send function if an "enter key" is pressed.  Applied to the onkeypress
  event for a text widget.

  @input form the form object to submit
  @input form the event
  @input submitaction the relative path of the action of
   the submitted form, e.g. "/packageBrowse.do"
--%>

//e.keyCode         = supported by IE, Opera

//e.which           = supported by Firefox, all mozilla engine based browsers

//e.charCode        = supported by Old Netscape browser

function checkTyping(form, submitaction,e){
    var keyCode = e.keyCode ? e.keyCode : e.which ? e.which : e.charCode;
    if (keyCode == 13) {
        send(form, submitaction);
    }
}

<%-- Checks whether browser supports DHTML --%>
function hasDHTML() { // IE 5 and Netscape 6
    return (document.getElementById);
}

function redirect(submitaction) {
    top.location.href = "<html:rewrite page='" + submitaction + "' />";
}

function redirectSameFrame(submitaction) {
    document.location = "<html:rewrite page='" + submitaction + "' />";
}

// confirm before redirecting
function conditionalRedirect(msg, forwardaction) {
    if (confirm(msg)) {
        javascript:redirect(forwardaction);
    }
}

<%-- Saves the autocomplete information.  Only works for IE 5.0 above Reference
http://msdn.microsoft.com/library/default.asp?url=/workshop/author/dhtml/reference/methods/autocompletesaveform.asp
Need this method otherwise the data is only saved when the submitted action is the
same as the defined form action
--%>
function saveAutoComplete(form) {
    if (window.external) {
        window.external.AutoCompleteSaveForm(form);
    }
}

<%-- strategically adds spaces to a long dn to encourage the text to wrap
     when the text is displayed in a pop-up window

     for ex.
     wrapDN('abcdefghijklmnopqrstuvwxyz', 15) returns "abcdefghijklmno pqrstuvwxyz"
     wrapDN('cn=Machines,dc=marimba,dc=com', 15) returns "cn=Machines, dc=marimba, dc=com"
     This is used on the Target View and Package View pages with overlib:
     <a href="#" class="noUnderlineLink" onmouseover="return overlib(wrapDN('<%= (String) app.get("dn") %>', 30), DELAY, '200', WIDTH, '30');" onmouseout="return nd();"><font class="generalText">link name</a>

     Note that the width attribute passed into the wrapDN method correlates
     to the WIDTH attribute passed into overlib.
--%>
function wrapDN(dn, width) {
    var p = width;
    var segment = dn.substring(0, p);
    var space = segment.lastIndexOf(" ");
    var comma = segment.lastIndexOf(",");
    if (dn.length > width) {
        if (space > comma) {
            space += 1;
            return dn.substring(0, space) + wrapDN(dn.substring(space, dn.length), width);
        } else {
            if (comma > space) {
                p = comma + 1;
            }
            return dn.substring(0, p) + " " + wrapDN(dn.substring(p, dn.length), width);
        }
    } else {
        return dn;
    }
}

function checkAll(form,var1,var2) {
    //alert("inside");
    if(form.box_all.checked) {
        form.action=var1+"true"+var2;
    } else {
        form.action=var1+"false"+var2;
    }
    form.submit();
}

function openReport(target) {
    window.open("/im/machine/machine.do?name="+target,"ReportCenter","width=700,height=600,left=150,top=50,resizable=yes");
}

function MM_openBrWindow(theURL,winName,features) { //v2.0
    window.open(theURL,winName,features);
}

function openReportWithURL(targetURL) {
    var host = targetURL;
    var protocolIndex=targetURL.indexOf('://');
    if(protocolIndex>=0){
        host=targetURL.substring(protocolIndex+3);
        if(host.indexOf('/')>=0)
            host=host.substring(0,host.indexOf('/'));
        if (host.indexOf(':')>=0)
            host=host.substring(0,host.indexOf(':'));
    }
    window.open("/im/machine/machine.do?name="+host,"ReportCenter","width=700,height=600,left=150,top=50,resizable=yes");
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
        if (key == "0"|| key == "8"){
            return true;
        }
    } else {
        return true;
    }
    keychar = String.fromCharCode(key);
    if (-1 == ("0123456789".indexOf(keychar))) {
        // digits only
        return false;
    }
    return true;
}

// To escape Javascript and HTML characters.
var SPECIAL_CHAR = new Array('\'', '\\');

function escapeJavascriptAndHTML(comp) {
    if (comp == '') {
        return;
    }

    var escapedString = '';
    var i = 0;

    while (i < comp.length) {
        var c = comp.charAt(i);
        if (isEscape(c) == "true") {
            // Java script error
            escapedString = escapedString +'\\';
            escapedString = escapedString + c;
        } else {
            switch (c) {
                case '<':
                    escapedString = escapedString + "&lt;";
                    break;
                case '>':
                    escapedString = escapedString + "&gt;";
                    break;
                case '&':
                    escapedString = escapedString + "&amp;";
                    break;
                case '"':
                    escapedString = escapedString + "&quot;";
                    break;
                default:
                    escapedString = escapedString + c;
            }
        }
        i++;
    }
    return escapedString;
}

function isEscape(c) {
    for (var i = 0; i < SPECIAL_CHAR.length; i++) {
        if (c == SPECIAL_CHAR[i]) {
            return "true";
        }
    }
    return "false";
}

function MakeTip(txtToDisplay) {
 return Tip(txtToDisplay, WIDTH, '-250', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', OFFSETY, 20, BORDERCOLOR, '#333300', FADEIN, 100);
}

function CloseTip() {
    UnTip();
}

</script>

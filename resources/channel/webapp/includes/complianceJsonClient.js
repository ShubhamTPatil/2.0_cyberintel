jsonRpc = null;



function initJSONRPC() {

    try {

        jsonrpc = new JSONRpcClient("/sm/JSON-RPC");

    } catch(e) {

        alert(e);

    }

}



function hideBasicSection() {

    document.getElementById("basic_cell").className = "searchTabInactive";

    document.getElementById("basic_link").style.color = "#FFFFFF";

    document.getElementById("basic_link").style.textDecoration = "underline";

    document.getElementById("basic_left_img").src = "../images/invisi_shim.gif";

    document.getElementById("basic_right_img").src = "../images/invisi_shim.gif";

    document.getElementById("basic_section").style.display = "none";

}



function showSearch(type) {

    if(type == "basic") {

        //show basic section

        document.getElementById("basic_cell").className = "searchTabActive";

        document.getElementById("basic_link").style.color = "#000000";

        document.getElementById("basic_link").style.textDecoration = "none";

        document.getElementById("basic_left_img").src = "../images/tab_form_left_a.gif";

        document.getElementById("basic_right_img").src = "../images/tab_form_right_a.gif";

        document.getElementById("basic_section").style.display = "";

        setCookie("toggleSearchCookie","basic",nextYear);

    }

    else if (type == "hide") {

        hideBasicSection();

        setCookie("toggleSearchCookie","hide",nextYear);

    }

}



var inCalculation = false;

var calculateForm = null;

var calculateIndex = 0;

var calculateElm = null;

var calculateTarget = "";

var calculatePolicy = "";

var calculateURL = "";

var targetType = "";

// This should be the url to the query library

var qryLibraryPrefix = '/im/machine/query.do?complianceRequest=complinaceToRC&treePath=%2flibrary%2fPolicy+Compliance%2f';

var qryCompliantMachines = 'CompliantMachineByTargetPolicyURLExt&pageType=result';

var qryNonCompliantMachines = 'NonCompliantMachineByTargetPolicyURLExt&pageType=result';

var qryNotCheckInMachines = 'NotCheckInMachineByTargetPolicyURLExt&pageType=result';

// SBRP Site machines

var qryCompliantSiteMachines = 'CompliantSiteMachineByTargetPolicyURLExt&pageType=result';

var qryNonCompliantSiteMachines = 'NonCompliantSiteMachineByTargetPolicyURLExt&pageType=result';

var qryNotCheckInSiteMachines = 'NotCheckInSiteMachineByTargetPolicyURLExt&pageType=result';

// overall compliance queries

var qryOverallCompliantMachine = 'OverallCompliantMachineByInventory';

var qryOverallNonCompliantMachin = 'OverallNonCompliantMachineByInventory';

var qryOverallNotCheckInMachine = 'OverallNotCheckInMachineListByInventory';
// overall compliance queries for SBRP site

var qryOverallCompliantSiteMachine = 'OverallCompliantSiteMachineByInventory';

var qryOverallNonCompliantSiteMachine = 'OverallNonCompliantSiteMachineByInventory';

var qryOverallNotCheckInSiteMachine = 'OverallNotCheckInSiteMachineListByInventory';

//power group compliance

var qryPowerCompliantMachine = 'PowerCompliantMachineByTargetPolicy';

var qryPowerNonCompliantMachine = 'PowerNonCompliantMachineByTargetPolicy';

var qryPowerNotCheckInMachine = 'PowerNotCheckInMachineByTargetPolicy';

//power group compliance for SBRP site

var qryPowerCompliantSiteMachine = 'PowerCompliantSiteMachineByTargetPolicy';

var qryPowerNonCompliantSiteMachine = 'PowerNonCompliantSiteMachineByTargetPolicy';

var qryPowerNotCheckInSiteMachine = 'PowerNotCheckInSiteMachineByTargetPolicy';

// complince level queries

var qryCompliantMachinesByCId = 'CompliantMachineReportByCID';

var qryNonCompliantMachinByCId = 'NonCompliantMachineReportByCID';

var qryNotCheckInMachineByCId = 'NotCheckInMachineReportByCID';


function getRCFormattedDateString(dt){

    // Report center expects a time string in this format ("MM/dd/yyyy hh:mm:ss aa")
    var mth = dt.getMonth() + 1; // because month is between 0 - 11
    var dy  = dt.getDate();
    var yr  = dt.getFullYear();
    var hr  = dt.getHours();
    var ampm = (hr>=12) ? "PM" : "AM";
    if(hr>=13) hr -= 12;   // convert this into a 12hr time. 16:00 is converted to 4:00
    if(hr==0) hr=12;       // hour 0 is 12.
    var mns = dt.getMinutes();
    var secs = dt.getSeconds()

    var dt_time = mth +'/' + dy + '/' + yr + ' ' + hr+':'+mns+':'+secs+' '+ampm

    return dt_time;

}

function getCompliaceLevelQuery( qryStrPrefix, cId ) {
    return qryStrPrefix +'&extra_0=' + escape(cId);
}

function getPowerComplianceQuery(qryStrPrefix) {
    if(calculateTarget.toLowerCase() == 'all')
        calculateTarget = 'all_all';

    return qryStrPrefix + '&extra_0=' + encodeURIComponent(calculateTarget.toLowerCase());
}

function getOverallComplianceQuery( qryStrPrefix, notCheckInLimit){

    if(calculateTarget.toLowerCase() == 'all')
        calculateTarget = 'all_all';

    if( notCheckInLimit == 0 ){
        return qryStrPrefix + '&extra_0=' + encodeURIComponent(calculateTarget.toLowerCase()) + '&extra_1=' + getRCFormattedDateString(new Date());
    } else {
        var _newdate = new Date().setTime( ( new Date() ).getTime() + ( notCheckInLimit*3600*1000 ) );
        var _temp = new Date( _newdate );
        return qryStrPrefix + '&extra_0=' + encodeURIComponent(calculateTarget.toLowerCase()) + '&extra_1=' + getRCFormattedDateString(_temp);
    }
}



function getCompliantSummaryQueryURL(qryStrPrefix) {

    return qryStrPrefix +

           '&extra_0=' + encodeURIComponent(calculateTarget) +

           '&extra_1=' + encodeURIComponent(calculatePolicy) +

           '&extra_2=' + encodeURIComponent(calculateURL);

}

var calElements = new Array();
function calculateSummary( form ){
    if (inCalculation) {
        alert(waitForCalc);
    } else {
        inCalculation = true;
        calculateIndex = 0;
        calculateForm = form;
        calculateBatch();
    }
}


function shiftTo( displayType, compText ){
    resultType = displayType;
    if( resultType == 'numbers' ){
        var complianceType = document.getElementById( 'compliance_type' );
        complianceType.innerHTML = '<a class="columnHeading" href="#">'+compText+'</a>&nbsp;&nbsp;&nbsp;<a href="javascript:shiftDisplay( \'percentage\' );"><img src="/sm/images/show_percent.gif" width="16" height="16" border="0" align="absmiddle"></a>&nbsp;<img src="/sm/images/show_numbers_sel.gif" width="16" height="16" border="0" align="absmiddle">';
    } else {
        var complianceType = document.getElementById( 'compliance_type' );
        complianceType.innerHTML = '<a class="columnHeading" href="#">'+compText+'</a>&nbsp;&nbsp;&nbsp;<img src="/sm/images/show_percent_sel.gif" width="16" height="16" border="0" align="absmiddle">&nbsp;<a href="javascript:shiftDisplay( \'numbers\' );"><img src="/sm/images/show_numbers.gif" width="16" height="16" border="0" align="absmiddle"></a>';
    }
    for( indx=0; indx < reports.length; indx++ ){
        var report = reports[indx];
        setCurrentElement( report.elementId );
        calculateElm = document.getElementById( 'comp_'+report.elementId );
        resetElement = document.getElementById( 'comp_'+report.elementId );
        resetElement.innerHTML = getComplianceReport( report.elementId, report.compliant, report.noncompliant, report.notcheckedin, report.state, report.targetType );
        if( report.state == STATE_ERROR ){
            setException( report.elementId, report.exception );
        }
    }
}

var setIntrRef = null; // reference for timer object
var setIntrPeriod = 10000 // 10 seconds

function calculateBatch(){
    calElements = getSelectedElements();
    window.clearInterval( setIntrRef );
    submitElements( calElements, true );
    // setting up timer for updating pending reports on load
    setIntrRef = setInterval("updatePendingReports()", setIntrPeriod );
}

function getSelectedElements(){
    var selElmnts = new Array();
    while( calculateIndex < calculateForm.elements.length ){
        var e = calculateForm.elements[calculateIndex];
        if ( e.type == 'checkbox' && ( e.name ).indexOf( 'target_sel_' ) == 0 && e.checked == true ) {
            calculateElm = document.getElementById('comp_' + e.name);
            calculateElm.innerHTML = calc;
            e.checked = false;
            selElmnts[ selElmnts.length ] = e.name;
        }
        calculateIndex++;
    }
    return selElmnts;
}

function updatePendingReports(){
    if( hasPendingReports == true ){
        calElements = getPendingReports();
        if( calElements.length > 0 ){
            if( setIntrRef == null ){
                setIntrRef = setInterval("updatePendingReports()", setIntrPeriod );
            }
            submitElements( calElements, false );
        } else {
            hasPendingReports = false;
        }
    } else{
        window.clearInterval( setIntrRef );
        setIntrRef = null;
    }
}

function getPendingReports(){
    var pendElmnts = new Array();
    var report = null;
    for( indx = 0; indx < reports.length; indx++ ){
        report  = reports[indx];
        if( report.state == STATE_IN_QUEUE || report.state == STATE_IN_QUERY){
            pendElmnts[ pendElmnts.length ] = report.elementId;
        }
    }
    return pendElmnts;
}

function submitElements( selElements, recalculate ){
    var targets = new Array();
    var policies = new Array();
    var packages = new Array();
    var targetTypes = new Array();
    for( indx = 0; indx < selElements.length; indx ++ ){
        var targetId = document.getElementById( 'trgtid_'+selElements[indx] ).value;
        var type = document.getElementById( 'trgttype_'+selElements[indx] ).value;
        // Save the current calculation context
        targets[ targets.length ] = targetId;
        targetTypes[targetTypes.length] = type;
        policies[ policies.length ] = getPolicy( selElements[indx] );
        packages[ packages.length ] = getPackageUrl( selElements[indx] );
    }
    jsonrpc.compliancesummaryservice.getComplianceSummary( getResults, policies, targets, packages, recalculate, view, targetTypes );
    // Calculation finished
    if( recalculate == true ){
        inCalculation = false;
        calculateForm.target_sel_all.checked = false;
        calculateForm.calculate_btn.disabled = true;
    }
}

function getResults( results, exception ){
    var indx = 0;
    if( !exception ){
        var result = null;
        for( indx = 0; indx < results.length; indx ++ ){
            result = results[ indx ];
            setCurrentElement( calElements[indx] );
            calculateElm.innerHTML = getComplianceReport( calElements[indx], result.compliant, result.noncompliant, result.notcheckedin, result.state, result.targetType);
            if( result.state == STATE_ERROR ){
                setException( calElements[indx], result.exception );
            }
        }
    }else{
        for( indx = 0; indx < calElements.length; indx ++ ){
            setCurrentElement( calElements[indx] );
            calculateElm.innerHTML = getComplianceReport( calElements[indx], 0, 0, 0, STATE_ERROR,'');
            setException( calElements[indx], exception );
        }
    }
    calElements = new Array();
}

function setException( elementId, exception ){
    for( iIndx=0; iIndx < reports.length; iIndx++ ){
        var report = reports[iIndx];
        if( report.elementId == elementId ){
            report.state = STATE_ERROR;
            report.exception = exception;
        }
    }
}

// resport javascript class
function ComplianceReport( elementId, compliant, noncompliant, notcheckedin, state) {
    ComplianceReport( elementId, compliant, noncompliant, notcheckedin, state, '');
}

function ComplianceReport( elementId, compliant, noncompliant, notcheckedin, state, targetType ) {
    this.elementId=elementId;

    this.compliant=compliant;

    this.noncompliant=noncompliant;

    this.notcheckedin=notcheckedin;

    this.state = state;

    this.targetType = targetType;

    this.exception = null;
}



var resultType = 'percentage';

var reports = new Array();

function addReport( elementId, succeed, failed, notchkdin, state ){
    addReport( elementId, succeed, failed, notchkdin, state,'' );
}

function addReport( elementId, succeed, failed, notchkdin, state, targetType ){

    var indx = 0;

    var added = false;

    for( indx=0; indx < reports.length; indx++ ){

        var report = reports[indx];

        if( report.elementId == elementId ){

            reports[indx] = new ComplianceReport( elementId, succeed, failed, notchkdin, state, targetType );

            added = true;

        }

    }

    if( added == false ){

        reports[ reports.length ] = new ComplianceReport( elementId, succeed, failed, notchkdin, state, targetType );

    }

}

/*  compliance state codes should match with
 Compliance query states declared in ComplianceConstants.java
 */

var STATE_IN_QUEUE = 0;
var STATE_IN_QUERY = 1;
var STATE_DONE = 2;
var STATE_ERROR = 3;
var STATE_NOT_CALCULATE = 4;
var hasPendingReports = false;
function getComplianceReport( elementId, succeed, failed, notchkdin, state){
    getComplianceReport( elementId, succeed, failed, notchkdin, state, '');
}
function getComplianceReport( elementId, succeed, failed, notchkdin, notapplicable, state, targetType){

    addReport( elementId, succeed, failed, notchkdin, state, targetType );

    if( state == STATE_DONE ){

        var total = succeed+failed+notchkdin+notapplicable;
        var sPer = getPer( succeed, total );
        var fPer = getPer( failed, total );
        var ncPer = getPer( notchkdin, total );
        var napplicablePer = getPer( notapplicable, total );
        var component = '';
        var urlCompliant = '';
        var urlNonCompliant = '';
        var urlNotCheckIn = '';
        var urlNotApplicable = '';
        if("site" == targetType) {
            urlCompliant = getCompliantSummaryQueryURL(qryLibraryPrefix + qryCompliantSiteMachines);
            urlNonCompliant = getCompliantSummaryQueryURL(qryLibraryPrefix + qryNonCompliantSiteMachines);
            urlNotCheckIn = getCompliantSummaryQueryURL(qryLibraryPrefix + qryNotCheckInSiteMachines);
        } else {
            urlCompliant = getCompliantSummaryQueryURL(qryLibraryPrefix + qryCompliantMachines);
            urlNonCompliant = getCompliantSummaryQueryURL(qryLibraryPrefix + qryNonCompliantMachines);
            urlNotCheckIn = getCompliantSummaryQueryURL(qryLibraryPrefix + qryNotCheckInMachines);
        }

        var hideLink = disableLink();
        if(hideLink) {
            if( resultType == 'numbers' ){
                component = getComplianceStatus( succeed, failed, notchkdin, notapplicable, sPer, fPer, ncPer, napplicablePer )+
                            '<span>'+succeed+'</span>&nbsp;'+
                            '<span>'+failed+'</span>&nbsp;'+
                            '<span>'+notchkdin+'</span>'+
                            '<span>'+notapplicable+'</span>';;
            } else {
                component = getComplianceStatus( succeed, failed, notchkdin, notapplicable, sPer, fPer, ncPer, napplicablePer )+
                            '<span class="textGreen" style="cursor: default" onmouseover="return MakeTip(\''+succeed+'&nbsp;\')" onmouseout="return CloseTip();">'+sPer+'%</span>&nbsp;'+
                            '<span class="textRed" style="cursor: default" onmouseover="return MakeTip(\''+failed+'&nbsp;\')" onmouseout="return CloseTip();">'+fPer+'%</span>&nbsp;'+
                            '<span class="textBlue" style="cursor: default" onmouseover="return MakeTip(\''+notchkdin+'&nbsp;\')" onmouseout="return CloseTip();">'+ncPer+'% </span>'+
                            '<span class="textOrange" style="cursor: default" onmouseover="return MakeTip(\''+notapplicable+'&nbsp;\')" onmouseout="return CloseTip();">'+napplicablePer+'% </span>';;
            }
        } else {
            if( resultType == 'numbers' ){
                component = getComplianceStatus( succeed, failed, notchkdin, notapplicable, sPer, fPer, ncPer, napplicablePer )+
                            '<span class="textGreen">'+succeed+'</span>&nbsp;'+
                            '<span class="textRed">'+failed+' </span>&nbsp;'+
                            '<span class="textBlue">'+notchkdin+' </span>'+
                            '<span class="textOrange">'+notapplicable+' </span>';
            } else {

                component = getComplianceStatus( succeed, failed, notchkdin, notapplicable, sPer, fPer, ncPer, napplicablePer )+
                            '<span class="textGreen">'+sPer+'%</span>&nbsp;'+
                            '<span class="textRed">'+fPer+'%</span>&nbsp;'+
                            '<span class="textBlue">'+ncPer+'%</a></span>'+
                            '<span class="textOrange">'+napplicablePer+'%</a></span>';
            }
        }
    } else if( state == STATE_ERROR ){
        component = '<span class=textRed onmouseover="return overlib( getFailOverException( \''+elementId+'\' ) );" onmouseout="return nd();">'+getState( STATE_ERROR )+'</span>';
    } else if( state == STATE_IN_QUEUE || state == STATE_IN_QUERY ){
        hasPendingReports = true;
        component = "<span>"+getState( state )+"</span>";
    } else {
        component = "<span>"+getState( state )+"</span>";
    }
    return component;
}

function getFailOverException( elementId ){
    for( indx=0; indx < reports.length; indx++ ){
        var report = reports[indx];
        if( report.elementId == elementId ){
            return report.exception;
        }
    }
}

function getComplianceStatus( succeed, failed, notchkdin, notapplicable, sPer, fPer, ncPer, notapplicablePer){
    var statusbar = '<img src="/shell/common-rsrc/images/invisi_shim.gif" width="'+sPer+'" height="8" onmouseover="return MakeTip(\''+altComp+': '+sPer+'% ('+succeed+') &nbsp;\', WIDTH, \'100\',  DELAY, \'100\');" onmouseout="return UnTip();" style="background-color:#66cc66;">'+
                    '<img src="/shell/common-rsrc/images/invisi_shim.gif" width="'+fPer+'" height="8" onmouseover="return MakeTip(\''+altNComp+': '+fPer+'% ('+failed+') &nbsp;\', WIDTH, \'100\', DELAY, \'100\');" onmouseout="return UnTip();" style="background-color:#ff6666;">'+
                    '<img src="/shell/common-rsrc/images/invisi_shim.gif" width="'+ncPer+'" height="8" onmouseover="return MakeTip(\''+altNCI+': '+ncPer+'% ('+notchkdin+') &nbsp;\', WIDTH, \'100\', DELAY, \'100\');" onmouseout="return UnTip();" style="background-color:#3399ff;">'+
                    '<img src="/shell/common-rsrc/images/invisi_shim.gif" width="'+notapplicablePer+'" height="8" onmouseover="return MakeTip(\''+altNApp+': '+notapplicablePer+'% ('+notapplicable+') &nbsp;\', WIDTH, \'100\', DELAY, \'100\');" onmouseout="return UnTip();" style="background-color:#FF851B;"> ';
    return statusbar;
}

function getPer( comp, total ){

    if (total != 0) {

        return Math.round((comp/total)*100);

    } else {

        return 0;

    }

}
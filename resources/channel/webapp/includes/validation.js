var dtCh = "/";
var minYear = 1900;
var maxYear = 2200;

var INVALID_DATE = 0;
var INVALID_MONTH = 1;
var INVALID_DAY = 2;
var INVALID_YEAR = 3;


function isInteger(s){
	var i;	
	
    for (i = 0; i < s.length; i++){       
        var c = s.charAt(i);
        if (((c < "0") || (c > "9"))) return false;
    }
    return true;
}

function stripCharsInBag(s, bag){
    var i;
    var returnString = "";
    
    for (i = 0; i < s.length; i++){   
        var c = s.charAt(i);
        if (bag.indexOf(c) == -1) returnString += c;
    }
    return returnString;
}

function daysInFebruary (year){
    return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
}

function DaysArray(n) {
	for (var i = 1; i <= n; i++) {
		this[i] = 31
		if (i==4 || i==6 || i==9 || i==11) {this[i] = 30}
		if (i==2) {this[i] = 29}
   } 
   return this;
}

function isValidDate(dtStr){
	var daysInMonth = DaysArray(12);
	var pos1 = dtStr.indexOf(dtCh);
	var pos2 = dtStr.indexOf(dtCh, pos1 + 1);
	var strMonth = dtStr.substring(0, pos1);
	var strDay = dtStr.substring(pos1 + 1, pos2);
	var strYear = dtStr.substring(pos2 + 1);
	
	strYr = strYear;
	if (strDay.charAt(0) == "0" && strDay.length > 1) strDay = strDay.substring(1);
	if (strMonth.charAt(0) == "0" && strMonth.length > 1) strMonth = strMonth.substring(1);
	for (var i = 1; i <= 3; i++) {
		if (strYr.charAt(0) == "0" && strYr.length > 1) strYr = strYr.substring(1);
	}
	month = parseInt(strMonth);
	day = parseInt(strDay);
	year = parseInt(strYr);
	if (pos1 == -1 || pos2 == -1){
		return INVALID_DATE;		
	}
	if (strMonth.length < 1 || month < 1 || month > 12){
		return INVALID_MONTH;		
	}
	if (strDay.length < 1 || day < 1 || day > 31 || (month == 2 && day > daysInFebruary(year)) || day > daysInMonth[month]){
		return INVALID_DAY;		
	}
	if (strYear.length != 4 || year == 0 || year < minYear || year > maxYear){
		return INVALID_YEAR;	
	}
	if (dtStr.indexOf(dtCh,pos2+1) != -1 || isInteger(stripCharsInBag(dtStr, dtCh)) == false){
		return INVALID_DATE;	
	}
return -1
}

function isValidDateRange(strDateFrom, strDateTo){
	var datDateFrom = Date.parse(strDateFrom);
	var datDateTo = Date.parse(strDateTo);
	var dateDiff = datDateTo - datDateFrom;
	if(dateDiff < 0 || isNaN(dateDiff)){
		return true;
	}
	return false;
}

function isBlank(val){
      if(val == null){
          return true;
      }
      if(trim(val).length == 0){
          return true;
      }
      return false;     
 }

function trim(str) {
    while (str.substring(0, 1) == ' ') {
        str = str.substring(1, str.length);
    }
    while (str.substring(str.length - 1, str.length) == ' ') {
        str = str.substring(0, str.length - 1);
    }
    return str;
}


<script>

///////////////
// common utility functions
// cp d:/zeus/products/subscriptionmanager/rsrc/web/distribution/schedule.js d:/zeus/install.win32/channels/any/subscriptionmanager/webapp/distribution
// cp d:/zeus/products/subscriptionmanager/rsrc/web/distribution/schedule_recurring.jsp d:/zeus/install.win32/channels/any/subscriptionmanager/webapp/distribution
//
///////////////

// return whether the browser is believed to be Internet Explorer 4.0 or
// greater
function isIE() {
  if (document.all) {
      return true;
  } else {
      return false;
  }
}

// make a <div> visible or hidden
function setVisibleDiv(divName, visible) {
    if (document.getElementById) { // DHTML - IE 5 and Netscape 6
	if (visible) {
	    document.getElementById(divName).style.visibility = "visible";
	} else {
	    document.getElementById(divName).style.visibility = "hidden";
	}
    } else if (isIE()) { // IE 4 or 5
	if (visible) {
	    eval("document.all."+divName+".style.visibility='visible';");
	} else {
	    eval("document.all."+divName+".style.visibility='hidden';");
	}
    } else { // Netscape 4
    }
}

// set disabled on a div by setting the color to gray (disabled) or
// black (enabled)
function setDisabledDiv(divName, disabled) {
    var color = (disabled ? "gray" : "black");
    if (document.getElementById) { // DHTML - IE 5 or Netscape 6
	document.getElementById(divName).style.color = color;
    } else if (isIE()) { // IE 4
	eval("document.all." + divName + ".style.color='" + color + "';");
    } else { // Netscape 4
    }
}

function getRadioValue(obj) {
   for (var i = 0; i < obj.length; i++) {
      if (obj[i].checked) {
        return obj[i].value;
      }
   }
}

function setMultiValueDisabled(obj, disabled) {
    for (var i = 0; i < obj.length; i++) {
      obj[i].disabled = disabled;
    }
}


//////////////
// schedule-specific functions
//////////////

function initCalDetailsVisible() {
    for (var i = 0; i < document.scheduleEditForm['value(CALENDAR_PERIOD)'].length; i++) {
      if (document.scheduleEditForm['value(CALENDAR_PERIOD)'][i].checked) {
        setCalDetailsVisible('div_caltype_' + document.scheduleEditForm['value(CALENDAR_PERIOD)'][i].value.toLowerCase());
      }
    }

}

// REMIND : Update time and recurrence widgets may not be present for active schedules
// Active Period is not present for service schedule

function initDisabled() {
    <% if (hasUpdate) { %>
       setRecurrenceDisabled(document.scheduleEditForm['value(SET_SCHEDULE)'].value == "false");
       setScheduleDisabled((document.scheduleEditForm['value(SET_SCHEDULE)'].value == "false") || (document.scheduleEditForm['value(scheduleFrequencySelect)'].value == "schedule_frequency_never"));
    <% } else { %>
      setScheduleDisabled(false);
    <% } %>

    var checkUpdate = true;
    <% if (hasUpdate) { %>
        var sch = document.getElementById("SET_SCHEDULE");
        checkUpdate = (sch.value == "true");
    <% } %>

    if (checkUpdate) {
    <% if (hasActive) { %>
        setActiveDateTimeDisabled("START", getRadioValue(document.scheduleEditForm['value(ACTIVE_PERIOD_START)']) == "false");
        setActiveDisabled("END", getRadioValue(document.scheduleEditForm['value(ACTIVE_PERIOD_END)']) == "false");
        setActiveDateTimeDisabled("END", getRadioValue(document.scheduleEditForm['value(ACTIVE_PERIOD_END)'])=="false");
      <% } %>
    }
    <% if (hasUpdate) { %>
        showFrequencySection(schedule_frequency_type,document.scheduleEditForm['value(scheduleFrequencySelect)'].value,document.scheduleEditForm['value(scheduleFrequencySelect)'].id);
    <% } %>
}

// set visibility on divs containing details about the day
function setCalDetailsVisible(divName) {
    <% if (!isService) { %>
    setVisibleDiv("div_caltype_never", divName == "div_caltype_never");
    <% } %>
    setVisibleDiv("div_caltype_daily", divName == "div_caltype_daily");
    setVisibleDiv("div_caltype_weekly", divName == "div_caltype_weekly");
    setVisibleDiv("div_caltype_monthly", divName == "div_caltype_monthly");
}

// set disabled on day repeat fields (monthly)
function setRecurrenceDisabled(disabled) {
//    setMultiValueDisabled(document.scheduleEditForm['value(CALENDAR_PERIOD)'], disabled);
  //  for (var i = 1; i < document.scheduleEditForm['value(CALENDAR_PERIOD)'].length; i++) {
    //  if (document.scheduleEditForm['value(CALENDAR_PERIOD)'][i].checked) {
      //    eval("set" + document.scheduleEditForm['value(CALENDAR_PERIOD)'][i].value + "Disabled(" + disabled + ");");
	//}
    //}
    document.scheduleEditForm['value(scheduleFrequencySelect)'].disabled = disabled;
}

// Note that the function names should have upper case

function setDAILYDisabled(disabled) {
  setMultiValueDisabled(document.scheduleEditForm['value(DAILY_WEEKDAYS)'], disabled);
  document.scheduleEditForm['value(DAY_INTERVAL)'].disabled = disabled;
  setDisabledDiv("div_caltype_daily", disabled);
}

function setWEEKLYDisabled(disabled) {
  for (var i = 0; i < 6; i++) {
    eval("document.scheduleEditForm['value(daysOfWeek_" + i + ")'].disabled = " + disabled + ";");
  }
  document.scheduleEditForm['value(WEEK_INTERVAL)'].disabled = disabled;
  setDayIntervalDisabled(getRadioValue(document.scheduleEditForm['value(DAILY_WEEKDAYS)']) == "true");
  setDisabledDiv("div_caltype_weekly", disabled);
}

function setMONTHLYDisabled(disabled) {
  document.scheduleEditForm['value(DAY_OF_MONTH)'].disabled = disabled;
  document.scheduleEditForm['value(MONTH_INTERVAL)'].disabled = disabled;
  setDisabledDiv("div_caltype_monthly", disabled);
}

function setDayIntervalDisabled(disabled) {
  document.scheduleEditForm['value(DAY_INTERVAL)'].disabled = disabled;
}

function setScheduleDisabled(disabled) {
  <% if (hasActive) { %>
    setActiveDisabled("START", disabled);
    setActiveDisabled("END", disabled);
  <% } %>
}

function setUpdateDisabled(disabled) {
  setMultiValueDisabled(document.scheduleEditForm['value(TIME_PERIOD)'],disabled);
  setUpdateTimeDisabled(disabled);
  setUpdateEveryDisabled(disabled);
}

function setUpdateTimeDisabled(disabled) {
  document.scheduleEditForm['value(AT_TIME_HOUR)'].disabled=disabled;
  document.scheduleEditForm['value(AT_TIME_MIN)'].disabled=disabled;
  document.scheduleEditForm['value(AT_TIME_AMPM)'].disabled=disabled;
  document.scheduleEditForm['value(AT_TIME_AMPM)'].disabled=disabled;

}

function setBetweenChecked() {
  if (document.scheduleEditForm['value(BETWEEN_TIME)'].checked) {
    setBetweenDisabled(false);
  } else {
    setBetweenDisabled(true);
  }
}
function setBetweenDisabled(disabled) {
  document.scheduleEditForm['value(BETWEEN_TIME_HOUR_START)'].disabled=disabled;
  document.scheduleEditForm['value(BETWEEN_TIME_HOUR_END)'].disabled=disabled;
  document.scheduleEditForm['value(BETWEEN_TIME_MIN_START)'].disabled=disabled;
  document.scheduleEditForm['value(BETWEEN_TIME_MIN_END)'].disabled=disabled;
  document.scheduleEditForm['value(BETWEEN_TIME_AMPM_START)'].disabled=disabled;
  document.scheduleEditForm['value(BETWEEN_TIME_AMPM_END)'].disabled=disabled;
}
function setUpdateEveryDisabled(disabled) {
  document.scheduleEditForm['value(EVERY_TIME_INTERVAL)'].disabled=disabled;
  document.scheduleEditForm['value(EVERY_TIME_INTERVAL_UNIT)'].disabled=disabled;
  document.scheduleEditForm['value(BETWEEN_TIME)'].disabled=disabled;
  if (!disabled) {
    //need to handle the enabling and disabling of the between appropriately
    //determine the value of the between_time
    if (document.scheduleEditForm['value(BETWEEN_TIME)'].checked) {
       setBetweenDisabled(false);
    } else {
       setBetweenDisabled(true);
    }

 }

}



function setActiveDisabled(type, disabled) {
  var isNever = false;

  <% if (hasUpdate) { %>
  isNever = (document.scheduleEditForm['value(scheduleFrequencySelect)'].value == "schedule_frequency_never");
  <% } %>
  eval("setMultiValueDisabled(document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + ")'], " +
  ((disabled  &&
  (getRadioValue(document.scheduleEditForm['value(ACTIVE_PERIOD_START)']) == "false" ||
  getRadioValue(document.scheduleEditForm['value(SET_SCHEDULE)']) == "false")) || isNever) + ");");

  eval("setActiveDateTimeDisabled('" + type + "', " +
  ((disabled &&
  (getRadioValue(document.scheduleEditForm['value(ACTIVE_PERIOD_START)']) == "false" ||
  getRadioValue(document.scheduleEditForm['value(SET_SCHEDULE)']) == "false")) || isNever) + ");");

}

function setActiveTimeDisabled(type, disabled) {
  eval("document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + "_HOUR)'].disabled=" + disabled + ";");
  eval("document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + "_MIN)'].disabled=" + disabled + ";");
  eval("document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + "_AMPM)'].disabled=" + disabled + ";");
  eval("document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + "_MONTH)'].disabled=" + disabled + ";");
  eval("document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + "_DAY)'].disabled=" + disabled + ";");
  eval("document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + "_YEAR)'].disabled=" + disabled + ";");
}

function setActiveDateTimeDisabled(type, disabled) {
  var isNever = false;
  <% if (hasUpdate) { %>
  isNever = (document.scheduleEditForm['value(scheduleFrequencySelect)'].value == "schedule_frequency_never");
  <% } %>
  eval("document.scheduleEditForm['value(ACTIVE_PERIOD_" + type + "_DATETIME)'].disabled=" + (disabled || isNever || getRadioValue(document.scheduleEditForm['value(ACTIVE_PERIOD_START)'])=="false") + ";");
}


</script>

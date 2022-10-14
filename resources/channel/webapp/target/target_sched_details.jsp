<%@ page contentType="text/html;charset=UTF-8" %>
<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)target_sched_details.jsp

     @author Angela Saval
     @version 1.5, 01/31/2002
--%>

<%@ include file="/includes/directives.jsp" %>
<%@ include file="/includes/headSection.jsp" %>
<%@ include file="/includes/banner.jsp" %>

<%-- Body content --%>
<table width="100%" border="0" cellpadding="5" class="generalText">
  <tr> 
    <td><font class="pageTitle"><webapps:pageText key="Title" /></font></td>
  </tr>
  
  <%-- Errors Display --%>
  <%@ include file="/includes/usererrors.jsp" %>


  
  <tr> 
    <td> 
      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="generalText">
        <tr> 
          <td valign="baseline"><b><font class="generalText">User Group: Traders 
            - New York</font></b></td>
          <td align="right"> 
            <input type="submit" id="button_blackout_period" value="Blackout Period">&nbsp;
            <input type="submit" id="button_properties" value="Tuner/Channel Properties">
          </td>
        </tr>
        <tr> 
          <td colspan="2"> 
            <hr noshade size="1">
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr> 
    <td> 
      <table border="1" bordercolor="cccccc" class="generalText" cellpadding="5" cellspacing="0" id="main_details_table">
        <tr> 
          <td colspan="7" class="coreColor1"> 
            <table width="100%" border="0" cellspacing="0" cellpadding="4">
              <tr> 
                <td align="right" valign="middle" nowrap> <font class="whiteText">For 
                  selected package(s):</font> &nbsp;&nbsp; 
                  <input type="submit" id="button_edit_assignment" value="Edit Assignment">
                  &nbsp; 
                  <input type="submit" id="button_install_priority" value="Set Install Priority">
                  &nbsp; 
                  <input type="submit" id="button_package_schedule_details" value="View Package Schedule Details">
                  &nbsp; </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr> 
          <td colspan="7" class="accentColor2"> 
            <table width="100%" border="0" cellspacing="0" cellpadding="0" class="generalText">
              <tr> 
                <td><a href="#" class="generalText" id="link_check_all">Check All</a> | <a href="#" class="generalText" id="link_clear_all">Clear 
                  All</a> | <a href="#" class="generalText" id="link_show_URL">Show URL</a></td>
                <td align="right"><img src="images/previous_inactive.gif" width="12" height="12" border="0"><font class="inactiveText">&nbsp;previous 
                  15 &nbsp;&nbsp;&nbsp;</font> 
                  <select name="select_record_nav" class="smallText">
                    <option selected>1-7 of 7</option>
                  </select>
                  &nbsp;&nbsp;&nbsp;<font class="inactiveText">next 15</font> 
                  <img src="images/next_inactive.gif" width="12" height="12" border="0"> 
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr valign="middle"> 
          <td class="accentColor1" width="5">&nbsp;</td>
          <td width="150" class="accentColor1"><font class="generalText"><a href="#" class="columnHeading" id="col_package">Package</a></font>&nbsp;<img src="images/descending_sort.gif" width="16" height="16" border="0"></td>
          <td width="50" class="accentColor1"><a href="#" class="columnHeading" id="col_install_priority"><font class="generalText">Install 
            Priority</font></a></td>
          <td class="accentColor1"><a href="#" class="columnHeading" id="col_primary_sched"><font class="generalText">Primary 
            Installation<br>
            Schedule</font></a></td>
          <td class="accentColor1"><a href="#" class="columnHeading" id="col_secondary_sched"><font class="generalText">Secondary<br>
            Installation<br>
            Schedule</font></a> </td>
          <td class="accentColor1"><a href="#" class="columnHeading" id="col_update_sched"><font class="generalText">Update<br>
            Schedule</font></a> </td>
          <td class="accentColor1"><a href="#" class="columnHeading" id="col_verify_sched"><font class="generalText">Verify/Repair<br>
            Schedule</font></a> </td>
        </tr>
        <tr> 
          <td> 
            <input type="checkbox" name="checkbox" value="checkbox">
          </td>
          <td><a href="#" class="hoverLink" onmouseover="return overlib('http://someURL', WIDTH, '100', DELAY, '200');" onmouseout="return nd();"><font class="generalText">Bloomberg A</font></a></td>
          <td valign="middle" align="center"><font class="generalText">1</font></td>
          <td valign="top"> 
            <p><font class="generalText">Stage</font></p>
            <p><font class="generalText">Active: 11:00PM 9/27/2001</font></p>
          </td>
          <td valign="top"> 
            <p><font class="generalText">Install</font></p>
            <p><font class="generalText">Active: 5:00AM 10/01/2001</font></p>
          </td>
          <td valign="top"><font class="generalText"> Recurrence: 6:00AM Every 
            5 days</font></td>
          <td valign="top"><font class="generalText"> Recurrence: 6:00AM Every 
            2 week(s)</font></td>
        </tr>
        <tr> 
          <td> 
            <input type="checkbox" name="checkbox" value="checkbox">
          </td>
          <td><a href="#" class="hoverLink" onMouseOver="return overlib('http://someURL', WIDTH, '100', DELAY, '200');" onMouseOut="return nd();"><font class="generalText">Bloomberg 
            B </font></a></td>
          <td valign="middle" align="center"><font class="generalText">2</font></td>
          <td valign="top"> 
            <p><font class="generalText">Stage</font></p>
            <p><font class="generalText">Active: 11:00PM 9/27/2001</font></p>
          </td>
          <td valign="top"> 
            <p><font class="generalText">Install</font></p>
            <p><font class="generalText">Active: 5:30AM 10/01/2001</font></p>
          </td>
          <td valign="top"> <font class="generalText"> Recurrence: 6:00AM Every 
            5 days</font>
          </td>
          <td valign="top"> <font class="generalText">Recurrence: 6:00AM Every 
            2 week(s)</font> 
          </td>
        </tr>
        <tr> 
          <td> 
            <input type="checkbox" name="checkbox" value="checkbox">
          </td>
          <td><a href="#" class="hoverLink" onMouseOver="return overlib('http://someURL', WIDTH, '100', DELAY, '200');" onMouseOut="return nd();"><font class="generalText">Bond 
            Trader Plus</font></a></td>
          <td valign="middle" align="center">&nbsp;</td>
          <td valign="top"> 
            <p><font class="generalText">Advertise</font></p>
            <p><font class="generalText">Active: 6:00AM 10/01/2001</font></p>
          </td>
          <td valign="top"> 
            <p><font class="generalText">Install</font></p>
            <p><font class="generalText">Active: 5:00AM 11/01/2001</font></p>
          </td>
          <td valign="top"> <font class="generalText"> Recurrence: 6:00AM Every 
            5 days</font>
            <p><font class="generalText">Active: 5:00AM 11/01/2001</font></p>
          </td>
          <td valign="top"> <font class="generalText">Recurrence: 6:00AM Every 
            2 week(s)</font> 
            <p><font class="generalText">Active: 5:00AM 11/01/2001</font></p>
          </td>
        </tr>
        <tr> 
          <td> 
            <input type="checkbox" name="checkbox2" value="checkbox">
          </td>
          <td><a href="#" class="hoverLink" onMouseOver="return overlib('http://someURL', WIDTH, '100', DELAY, '200');" onMouseOut="return nd();"><font class="generalText">Excel</font></a></td>
          <td valign="middle" align="center">&nbsp;</td>
          <td valign="top"><font class="generalText"> Install</font></td>
          <td valign="top"><font class="inactiveText">N/A</font></td>
          <td valign="top"> 
            <font class="generalText">Recurrence: 6:00AM Every 4 week(s)</font>
          </td>
          <td valign="top"><font class="generalText">Recurrence: 6:30AM Every 
            1 week(s)</font></td>
        </tr>
        <tr> 
          <td> 
            <input type="checkbox" name="checkbox3" value="checkbox">
          </td>
          <td><a href="#" class="hoverLink" onMouseOver="return overlib('http://someURL', WIDTH, '100', DELAY, '200');" onMouseOut="return nd();"><font class="generalText">Norton 
            Anti-Virus</font></a></td>
          <td valign="middle" align="center"><font class="generalText">3</font></td>
          <td valign="top"><font class="generalText"> Install</font></td>
          <td valign="top"> 
            <p><font class="inactiveText">N/A</font></p>
          </td>
          <td valign="top"><font class="inactiveText">unspecified</font></td>
          <td valign="top"><font class="inactiveText">unspecified</font></td>
        </tr>
        <tr> 
          <td> 
            <input type="checkbox" name="checkbox4" value="checkbox">
          </td>
          <td><font class="generalText">Virus Definitions </font></td>
          <td valign="middle" align="center"><font class="generalText">4</font></td>
          <td valign="top"><font class="generalText">Install</font></td>
          <td valign="top"><font class="inactiveText">N/A</font></td>
          <td valign="top"><font class="generalText">Recurrence: 6:00AM Every 
            2 days</font></td>
          <td valign="top"><font class="generalText">Recurrence: 6:00AM Every 
            1 week(s)</font></td>
        </tr>
        <tr> 
          <td> 
            <input type="checkbox" name="checkbox42" value="checkbox">
          </td>
          <td><font class="generalText">Winzip</font></td>
          <td valign="middle" align="center">&nbsp;</td>
          <td valign="top"><font class="generalText">Install</font></td>
          <td valign="top"><font class="inactiveText">N/A</font></td>
          <td valign="top"><font class="inactiveText">unspecified</font></td>
          <td valign="top"><font class="inactiveText">unspecified</font></td>
        </tr>
        <tr> 
          <td colspan="7" class="accentColor2"> 
            <table width="100%" border="0" cellspacing="0" cellpadding="0" class="generalText">
              <tr> 
                <td><a href="#" class="generalText" id="link_check_all">Check 
                  All</a> | <a href="#" class="generalText" id="link_clear_all">Clear 
                  All</a> | <a href="#" class="generalText" id="link_show_URL">Show 
                  URL</a></td>
                <td align="right"><img src="images/previous_inactive.gif" width="12" height="12" border="0"><font class="inactiveText">&nbsp;previous 
                  15 &nbsp;&nbsp;&nbsp;</font> 
                  <select name="select" class="smallText">
                    <option selected>1-7 <webapps:pageText shared="true" type="target_details_area" key="of"/> 7</option>
                  </select>
                  &nbsp;&nbsp;&nbsp;<font class="inactiveText">next 15</font> 
                  <img src="images/next_inactive.gif" width="12" height="12" border="0"></td>
              </tr>
            </table>
          </td>
        </tr>
        <tr> 
          <td colspan="7" class="coreColor1"> 
            <table width="100%" border="0" cellspacing="0" cellpadding="4">
              <tr> 
                <td align="right" nowrap> <font class="whiteText">For selected 
                  package(s):</font> &nbsp;&nbsp; 
                  <input type="submit" id="button_edit_assignment" value="Edit Assignment" name="submit3">
                  &nbsp; 
                  <input type="submit" id="button_install_priority" value="Set Install Priority" name="submit2">
                  &nbsp; 
                  <input type="submit" id="button_package_schedule_details" value="View Package Schedule Details" name="submit">
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
<br>

<%@ include file="/includes/footer.jsp" %>

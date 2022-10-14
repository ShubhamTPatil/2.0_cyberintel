<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>

<!DOCTYPE html><html xmlns="http://www.w3.org/1999/xhtml" xmlns:arf="http://scap.nist.gov/schema/asset-reporting-format/1.1" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
<meta charset="utf-8"></meta>
<meta http-equiv="X-UA-Compatible" content="IE=edge"></meta>
<meta name="viewport" content="width=device-width, initial-scale=1"></meta>
<title>Security Compliance Report - Configuration Assessment</title>

<link rel="stylesheet" href="/spm/css/compliance.css" type="text/css" />
<script src="/spm/js/compliance.js"></script>

</head>
<body>
<bean:define id="profile" name="securityProfileReportForm" property="securityProfileDetailsBean" />
<nav class="navbar navbar-default" role="navigation">
    <div class="navbar-header" style="float: none;background-color:#206276">
        <div  style="float: left;color:#FFFFFF;padding-left:10px;"><h2>Security Compliance Report - Configuration Assessment</h2></div>
        <div  style="float: right;padding-right:15px;padding-top:7px;"><img width="100px" height="60px" src="/spm/images/harman_logo_blue.png"></div>
    </div>
</nav>
<div class="container">
    <div id="content">
        <div id="introduction">
            <div class="row">
                <h2><bean:write name="profile" property="contentTitle" /></h2>
                <blockquote>
                    with profile <mark><bean:write name="profile" property="profileTitle" /></mark>
                    <div class="col-md-12 well well-lg horizontal-scroll">
                        <div class="description">
                            <small>
                                <bean:write name="profile" property="profileDescription" />
                            </small>
                        </div>
                    </div>
                </blockquote>
                <div class="description">
                    This guide presents a catalog of security-relevant configuration settings for firefox formatted in the eXtensible Configuration Checklist Description Format (XCCDF).
                    <br>
                    <br>
                    <bean:write name="profile" property="contentDescription" />
                </div>
                <div class="top-spacer-10">
                    <div class="alert alert-info">
                        Do not attempt to implement any of the settings in this guide without first testing them in a non-operational environment. The
                        creators of this guidance assume no responsibility whatsoever for its use by other parties, and makes no guarantees, expressed or implied, about its
                        quality, reliability, or any other characteristic.
                    </div>
                </div>
            </div>
        </div>
        <div id="characteristics">
            <h2>Evaluation Characteristics</h2>
            <div class="row">
                <div class="col-md-10 well well-lg horizontal-scroll">
                    <table class="table table-bordered">
                        <tr><th>Target machine</th><td><bean:write name="profile" property="targetName" /></td></tr>
                        <tr><th>Target Operating System</th><td><bean:write name="profile" property="targetOS" /></td></tr>
                        <tr><th>Benchmark URL</th><td><bean:write name="profile" property="contentFileName" /></td></tr>
                        <tr><th>Benchmark ID</th><td><bean:write name="profile" property="contentName" /></td></tr>
                        <tr><th>Profile ID</th><td><bean:write name="profile" property="profileName" /></td></tr>
                        <tr><th>Profile Title</th><td><bean:write name="profile" property="profileTitle" /></td></tr>
                        <tr><th>Rules Count</th><td><bean:write name="profile" property="rulesCount" /></td></tr>
                        <tr><th>Started at</th><td><bean:write name="profile" property="startTime" /></td></tr>
                        <tr><th>Finished at</th><td><bean:write name="profile" property="finishTime" /></td></tr>
                        <tr><th>Performed by</th><td><bean:write name="profile" property="performedBy" /></td></tr>
                        <tr><th>Compliance Status</th><td><bean:write name="profile" property="complaintLevel" /></td></tr>
                    </table>
                </div>
           </div>
         </div>
        <div id="compliance-and-scoring">
            <h2>Compliance and Scoring</h2>
            <div class="alert alert-danger">
                <strong>The target system did not satisfy the conditions of <bean:write name="profile" property="failedRulesCount" /> rules!</strong>
                    Please review rule results and consider applying remediation.
            </div>
            <h3>Rule results</h3>
            <div class="progress" title="Displays proportion of passed/fixed, failed/error, and other rules (in that order). There were <bean:write name="profile" property="failedRulesCount" /> rules taken into account.">
                <div class="progress-bar progress-bar-success" style="width: <bean:write name="profile" property="passedRulesPercentage"/>%"><bean:write name="profile" property="passedRulesCount" /> passed</div>
                <div class="progress-bar progress-bar-danger" style="width: <bean:write name="profile" property="failedRulesPercentage"/>%"><bean:write name="profile" property="failedRulesCount" /> failed</div>
                <div class="progress-bar progress-bar-warning" style="width: <bean:write name="profile" property="otherRulesPercentage"/>%"><bean:write name="profile" property="otherRulesCount" /> other</div>
            </div>
            <h3>Severity of failed rules</h3>
            <div class="progress" title="Displays proportion of high, medium, low, and other severity failed rules (in that order). There were <bean:write name="profile" property="failedRulesCount" /> total failed rules.">
                <div class="progress-bar progress-bar-success" style="width: <bean:write name="profile" property="failedRulesOtherSeverityPercentage"/>%"><bean:write name="profile" property="failedRulesOtherSeverity"/> other</div>
                <div class="progress-bar progress-bar-info" style="width: <bean:write name="profile" property="failedRulesLowSeverityPercentage"/>%"><bean:write name="profile" property="failedRulesLowSeverity"/> low</div>
                <div class="progress-bar progress-bar-warning" style="width: <bean:write name="profile" property="failedRulesMediumSeverityPercentage"/>%"><bean:write name="profile" property="failedRulesMediumSeverity"/> medium</div>
                <div class="progress-bar progress-bar-danger" style="width: <bean:write name="profile" property="failedRulesHighSeverityPercentage"/>%"><bean:write name="profile" property="failedRulesHighSeverity"/> high</div>
            </div>
            <h3 title="As per the XCCDF specification">Score</h3>
            <table class="table table-striped table-bordered">
                <thead>
                    <tr>
                        <th>Scoring system</th>
                        <th class="text-center">Score</th>
                        <th class="text-center">Maximum</th>
                        <th class="text-center" style="width: 40%">Percent</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>urn:xccdf:scoring:default</td><td class="text-center"><bean:write name="profile" property="passedRulesPercentage"/></td><td class="text-center">100.000000</td><td><div class="progress"><div class="progress-bar progress-bar-success" style="width: <bean:write name="profile" property="passedRulesPercentage"/>%"><bean:write name="profile" property="passedRulesPercentage"/>%</div></div></td>
                    </tr>
                </tbody>
            </table>

            <h2>Rule Overview</h2>
            <div class="form-group hidden-print">
                <div class="row">
                    <div title="Filter rules by their XCCDF result">
                        <div class="col-sm-2 toggle-rule-display-success">
                            <div class="checkbox">
                                <label>
                                    <input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="pass"></input>
                                    pass</label>
                            </div>
                            <div class="checkbox">
                                <label><input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="fixed"></input>
                                    fixed</label>
                            </div>
                            <div class="checkbox">
                                <label><input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="informational"></input>
                                    informational</label>
                            </div>
                        </div>
                        <div class="col-sm-2 toggle-rule-display-danger">
                            <div class="checkbox">
                                <label><input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="fail"></input>
                                    fail</label>
                            </div>
                            <div class="checkbox">
                                <label><input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="error"></input>
                                    error</label>
                            </div>
                            <div class="checkbox">
                                <label><input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="unknown"></input>
                                    unknown</label>
                            </div>
                        </div>
                        <div class="col-sm-2 toggle-rule-display-other">
                            <div class="checkbox">
                                <label>
                                <input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="notchecked"></input>
                                notchecked</label>
                            </div>
                            <div class="checkbox">
                                <label><input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" value="notselected"></input>
                                    notselected</label>
                            </div>
                            <div class="checkbox">
                                <label>
                                    <input class="toggle-rule-display" type="checkbox" onclick="toggleRuleDisplay(this)" checked value="notapplicable"></input>
                                    notapplicable</label>
                            </div>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="input-group">
                            <input type="text" class="form-control" placeholder="Search through XCCDF rules" id="search-input" oninput="ruleSearch()"></input>
                            <div class="input-group-btn">
                                <button class="btn btn-default" onclick="ruleSearch()">Search</button>
                            </div>
                        </div>
                        <p id="search-matches"></p>
                    </div>
                </div>
            </div>
            <table class="treetable table table-bordered">
                <thead>
                    <tr>
                        <th>Title</th>
                        <th style="width: 120px; text-align: center">Severity</th>
                        <th style="width: 120px; text-align: center">Result</th>
                    </tr>
                </thead>
                <tbody>
                   <logic:iterate id = "group" name = "profile" property = "groups">
                       <tr data-tt-id="<bean:write name="group" property="id"/>" class="rule-overview-inner-node rule-overview-inner-node-id-<bean:write name="group" property="id"/>" data-tt-parent-id ="<bean:write name="profile" property="contentName"/>">
                           <td colspan="3" style="padding-left: 0px">
                               <strong><bean:write name="group" property="title"/></strong>
                               <span class="rule-result-pass badge"><bean:write name="group" property="passedRulesCount"/>x pass</span>
                               <span class="rule-result-fail badge"><bean:write name="group" property="failedRulesCount"/>x failed</span>
                               <span class="badge"><bean:write name="group" property="otherRulesCount"/>x other</span>
                           </td>
                       </tr>

                       <logic:iterate id = "groupLevel1" name = "group" property = "groups">
                           <tr data-tt-id="<bean:write name="groupLevel1" property="id"/>" class="rule-overview-inner-node rule-overview-inner-node-id-<bean:write name="group" property="id"/>" data-tt-parent-id="<bean:write name="group" property="id"/>">
                               <td colspan="3" style="padding-left: 19px">
                                   <strong><bean:write name="groupLevel1" property="title"/></strong>
                                   <span class="rule-result-pass badge"><bean:write name="groupLevel1" property="passedRulesCount"/>x pass</span>
                                   <span class="rule-result-fail badge"><bean:write name="groupLevel1" property="failedRulesCount"/>x failed</span>
                                   <span class="badge"><bean:write name="groupLevel1" property="otherRulesCount"/>x other</span>
                               </td>
                           </tr>
                           <logic:iterate id = "groupLevel2" name = "groupLevel1" property = "groups">
                               <tr data-tt-id="<bean:write name="groupLevel2" property="id"/>" class="rule-overview-inner-node rule-overview-inner-node-id-<bean:write name="groupLevel2" property="id"/>" data-tt-parent-id="<bean:write name="groupLevel1" property="id"/>">
                                   <td colspan="3" style="padding-left: 38px">
                                       <bean:write name="groupLevel2" property="title"/>
                                       <span class="rule-result-pass badge"><bean:write name="groupLevel1" property="passedRulesCount"/>x pass</span>
                                       <span class="rule-result-fail badge"><bean:write name="groupLevel1" property="failedRulesCount"/>x failed</span>
                                       <span class="badge"><bean:write name="groupLevel1" property="otherRulesCount"/>x other</span>
                                   </td>
                               </tr>
                               <logic:iterate id = "rule" name = "groupLevel2" property = "rules">
                                   <logic:equal name = "rule" property = "result" value="pass">
                                       <tr data-tt-id="<bean:write name="rule" property="id"/>" class="rule-overview-leaf rule-overview-leaf-pass rule-overview-needs-attention" id="rule-overview-leaf-id<bean:write name="rule" property="id"/>" data-tt-parent-id="<bean:write name="groupLevel2" property="id"/>" >
                                  </logic:equal>
                                   <logic:equal name = "rule" property = "result" value="fail">
                                       <tr data-tt-id="<bean:write name="rule" property="id"/>" class="rule-overview-leaf rule-overview-leaf-fail rule-overview-needs-attention" id="rule-overview-leaf-id<bean:write name="rule" property="id"/>" data-tt-parent-id="<bean:write name="groupLevel2" property="id"/>" >
                                   </logic:equal>
                                       <td style="padding-left: 57px">
                                           <a href="#" <logic:equal name = "rule" property = "result" value="pass">style="color:blue"</logic:equal> onclick="openRuleDetailsDialog('<bean:write name="rule" property="uuid"/>')"><bean:write name="rule" property="title"/></a>
                                       </td>
                                       <td class="rule-severity" style="text-align: center"><bean:write name="rule" property="severity"/></td>
                                           <logic:equal name = "rule" property = "result" value="pass">
                                                <td class="rule-result rule-result-pass">
                                           </logic:equal>
                                            <logic:notEqual name = "rule" property = "result" value="pass">
                                                <logic:equal name = "rule" property = "result" value="fail">
                                                <td class="rule-result rule-result-fail">
                                                </logic:equal>
                                                <logic:notEqual name = "rule" property = "result" value="fail">
                                                    <td class="rule-result">
                                                </logic:notEqual>
                                            </logic:notEqual>
                                               <div>
                                                       <abbr title="<bean:write name="rule" property="title"/>"><bean:write name="rule" property="result"/></abbr>
                                                   </div>
                                             </td>
                                   </tr>
                               </logic:iterate>
                           </logic:iterate>

                           <logic:iterate id = "rule" name = "groupLevel1" property = "rules">
                               <logic:equal name = "rule" property = "result" value="pass">
                                   <tr data-tt-id="<bean:write name="rule" property="id"/>" class="rule-overview-leaf rule-overview-leaf-pass rule-overview-needs-attention" id="rule-overview-leaf-id<bean:write name="rule" property="id"/>" data-tt-parent-id="<bean:write name="groupLevel1" property="id"/>" >
                              </logic:equal>
                               <logic:equal name = "rule" property = "result" value="fail">
                                   <tr data-tt-id="<bean:write name="rule" property="id"/>" class="rule-overview-leaf rule-overview-leaf-fail rule-overview-needs-attention" id="rule-overview-leaf-id<bean:write name="rule" property="id"/>" data-tt-parent-id="<bean:write name="groupLevel1" property="id"/>" >
                               </logic:equal>
                                   <td style="padding-left: 38px">
                                       <a href="#" <logic:equal name = "rule" property = "result" value="pass">style="color:blue"</logic:equal> onclick="openRuleDetailsDialog('<bean:write name="rule" property="uuid"/>')"><bean:write name="rule" property="title"/></a>
                                   </td>
                                   <td class="rule-severity" style="text-align: center"><bean:write name="rule" property="severity"/></td>
                                       <logic:equal name = "rule" property = "result" value="pass">
                                            <td class="rule-result rule-result-pass">
                                       </logic:equal>
                                        <logic:notEqual name = "rule" property = "result" value="pass">
                                            <logic:equal name = "rule" property = "result" value="fail">
                                            <td class="rule-result rule-result-fail">
                                            </logic:equal>
                                            <logic:notEqual name = "rule" property = "result" value="fail">
                                                <td class="rule-result">
                                            </logic:notEqual>
                                        </logic:notEqual>
                                           <div>
                                                   <abbr title="<bean:write name="rule" property="title"/>"><bean:write name="rule" property="result"/></abbr>
                                               </div>
                                         </td>
                               </tr>
                           </logic:iterate>
                       </logic:iterate>

                       <logic:iterate id = "rule" name = "group" property = "rules">
                           <logic:equal name = "rule" property = "result" value="pass">
                               <tr data-tt-id="<bean:write name="rule" property="id"/>" class="rule-overview-leaf rule-overview-leaf-pass rule-overview-needs-attention" id="rule-overview-leaf-id<bean:write name="rule" property="id"/>" data-tt-parent-id="<bean:write name="group" property="id"/>" >
                          </logic:equal>
                           <logic:equal name = "rule" property = "result" value="fail">
                               <tr data-tt-id="<bean:write name="rule" property="id"/>" class="rule-overview-leaf rule-overview-leaf-fail rule-overview-needs-attention" id="rule-overview-leaf-id<bean:write name="rule" property="id"/>" data-tt-parent-id="<bean:write name="group" property="id"/>" >
                           </logic:equal>
                               <td style="padding-left: 19px">
                                   <a href="#" <logic:equal name = "rule" property = "result" value="pass">style="color:blue"</logic:equal> onclick="openRuleDetailsDialog('<bean:write name="rule" property="uuid"/>')"><bean:write name="rule" property="title"/></a>
                               </td>
                               <td class="rule-severity" style="text-align: center"><bean:write name="rule" property="severity"/></td>
                               <logic:equal name = "rule" property = "result" value="pass">
                                    <td class="rule-result rule-result-pass">
                               </logic:equal>
                                <logic:notEqual name = "rule" property = "result" value="pass">
                                    <logic:equal name = "rule" property = "result" value="fail">
                                    <td class="rule-result rule-result-fail">
                                    </logic:equal>
                                    <logic:notEqual name = "rule" property = "result" value="fail">
                                        <td class="rule-result">
                                    </logic:notEqual>
                                </logic:notEqual>
                                       <div>
                                           <abbr title="<bean:write name="rule" property="title"/>"><bean:write name="rule" property="result"/></abbr>
                                       </div>
                                 </td>
                           </tr>
                       </logic:iterate>
                   </logic:iterate>
                </tbody>
            </table>
        </div>
        <div class="js-only hidden-print">
            <button type="button" class="btn btn-info" onclick="return toggleResultDetails(this)">Show all result details</button>
        </div>
        <div id="result-details">
            <h2>Result Details</h2>
            <logic:iterate id = "rule" name = "profile" property = "rules">
                <div class="panel panel-default rule-detail rule-detail-fail rule-detail-id-<bean:write name="rule" property="id"/>" id="rule-detail-<bean:write name="rule" property="uuid"/>">
                    <div class="keywords sr-only"><bean:write name="rule" property="id"/></div>
                    <div class="panel-heading"><h3 class="panel-title"><bean:write name="rule" property="title"/></h3></div>
                    <div class="panel-body">
                        <table class="table table-striped table-bordered">
                            <tbody>
                                <tr><td class="col-md-3">Rule ID</td><td class="rule-id col-md-9"><bean:write name="rule" property="id"/></td></tr>
                                <tr><td class="col-md-3">Rule Value</td><td class="rule-id col-md-9"><bean:write name="rule" property="value"/></td></tr>
                                <tr><td>Result</td>
                                <logic:equal name="rule" property="result" value="pass">
                                    <td class="rule-result rule-result-pass">
                                </logic:equal>
                                <logic:equal name="rule" property="result" value="fail">
                                    <td class="rule-result rule-result-fail">
                                </logic:equal>
                                <div><abbr title="<bean:write name="rule" property="title"/>"><bean:write name="rule" property="result"/></abbr></div>
                                </td></tr>
                                <tr><td>Time</td><td><bean:write name="profile" property="finishTime"/></td></tr>
                                <tr><td>Severity</td><td><bean:write name="rule" property="severity"/></td></tr>
                                <tr>
                                    <td>Description</td><td><div class="description"><p>
                                        <bean:write name="rule" property="description"/>
                                    </p></div></td>
                                </tr>
                                <tr>
                                    <td>Rationale</td><td><div class="description"><p>
                                        <bean:write name="rule" property="rationale"/>
                                    </p></div></td>
                                </tr>
                                <logic:equal name="rule" property="result" value="fail">
                                <tr><td colspan="2">
                                    <div class="remediation">
                                        <span class="label label-success">Remediation script:</span>
                                        <pre>
                                            <code>
                                                <bean:write name="rule" property="fixScript"/>
                                            </code>
                                        </pre>
                                    </div>
                                </td>
                                </tr>
                                </logic:equal>
                            </tbody>
                        </table>
                    </div>
                </div>
            </logic:iterate>
        </div>
        <div id="rear-matter">
            <div class="row top-spacer-10">
                <div class="col-md-12 well well-lg">
                    <div class="rear-matter">Harman and Harman Connected Connected Services are either registered
                        trademarks or trademarks of Harman in the United States and other
                        countries. All other names are registered trademarks or trademarks of their
                        respective companies.
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<footer id="footer">
    <div class="container">
        <p class="muted credit">Generated using <a href="http://harman.com">Clarinet</a></p>
    </div>
</footer>
</body>
</html>

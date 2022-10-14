<%@ page import="com.marimba.apps.subscriptionmanager.intf.IAppConstants"%>
<script language="JavaScript">
function checkEnter2(event, form) {
    var code = 0;
    if (NS4) {
        code = event.which;
    } else {
        code = event.keyCode;
    }
    if (code == 13) {
        submitForm(form, 'result', '');
    }
}
</script>

<table width="90%" border="0" cellpadding="3" cellspacing="0">
  <logic:iterate id="arg" indexId="argIndex" name="atlas.form" property="args">
  <tr>
    <td align="right" valign="middle" class="col1"><bean:write name="arg" property="name" />:</td>
    <td nowrap class="col2">
      <logic:equal name="arg" property="type" value="string">
      <input type="text" name="av_<bean:write name="argIndex" />_0" class="optionalField" onkeypress="checkEnter2(event, this.form)">
      </logic:equal>
      <logic:equal name="arg" property="type" value="integer">
      <input type=text name="av_<bean:write name="argIndex" />_0" class="requiredField" onkeypress="checkEnter2(event, this.form)">
      </logic:equal>
      <logic:equal name="arg" property="type" value="boolean">
      <select name="av_<bean:write name="argIndex" />_0" class="requiredField">
    <option value="true"> true </option>
    <option value="false"> false </option>
      </select>
      </logic:equal>
      <logic:equal name="arg" property="type" value="date">
          <input type="text" name="av_<bean:write name="argIndex" />_0" size="30" maxlength="30" class="requiredField"><br>
            <webapps:pageText key="actfmt" type="repctr" shared="true"/>
            <webapps:datetime locale="request" dateStyle="<%= IAppConstants.INPUT_RC_DATESTYLE %>" timeStyle="<%= IAppConstants.INPUT_RC_TIMESTYLE %>" view="pattern" type="date" />
            <webapps:pageText key="egfmt" type="repctr" shared="true"/>
            <webapps:datetime locale="request" dateStyle="<%= IAppConstants.INPUT_RC_DATESTYLE %>" timeStyle="<%= IAppConstants.INPUT_RC_TIMESTYLE %>" view="sample" type="date" />
      </logic:equal>
      <logic:equal name="arg"  property="type" value="time">
          <input type="text" name="av_<bean:write name="argIndex" />_0" size="30" maxlength="30" class="requiredField"><br>
            <web apps:pageText key="actfmt" type="repctr" shared="true"/>
            <webapps:datetime locale="request" dateStyle="<%= IAppConstants.INPUT_RC_DATESTYLE %>" timeStyle="<%= IAppConstants.INPUT_RC_TIMESTYLE %>" view="pattern" type="datetime" />
            <webapps:pageText key="egfmt" type="repctr" shared="true"/>
            <webapps:datetime locale="request" dateStyle="<%= IAppConstants.INPUT_RC_DATESTYLE %>" timeStyle="<%= IAppConstants.INPUT_RC_TIMESTYLE %>" view="sample" type="datetime" />
      </logic:equal>
    </td>
  </tr>
  </logic:iterate>
</table>

 <%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)select_tx.jsp

     @author Theen-Theen Tan
     @version 1.18, 06/05/2002
--%>
<%@ include file="/includes/directives.jsp" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean" %>
<%@ page import = "org.apache.commons.lang.StringEscapeUtils" %>

<script type="text/javascript">
    function addPkgCfrm(form, action, url) {
        var cfrmMessage="<webapps:pageText key="addPkgCfrmMessage"/>";
        var checkCfrm=confirm(cfrmMessage + "\n \n" + url);

        document.getElementById('addUrl').value = url;

        if (checkCfrm) {
            send(form, action);
        }
    }

    function addPkg(form, action, url) {
        document.getElementById('addUrl').value = url;
        send(form, action);
    }

    function sendParentFolder(form, action, selection){
            document.getElementById('url').value = selection;
            document.getElementById('selection').value = '';
            send(form, action);
    }

</script>

<% String pageBeanName = "txlist_bean"; %>
<html:hidden name="packageEditForm" styleId="selection" property="selection"/>
<html:hidden name="packageEditForm" styleId="addUrl" property="addUrl"/>
    <TR class=smallButtons vAlign=center>
        <TD class=tableRowActions>
            <table>
                <tr>
                    <td align="right" nowrap> <webapps:pageText shared="true"  key="Transmitter" type="pkgbrowsing"/>&nbsp;</td>
                    <td align="left"> <html:text name="packageEditForm" styleId="url" property="url" size="40" value='<%= (String) session.getAttribute("txlist_currenturl") %>' onkeypress="checkTypingPackageSearch(document.packageEditForm, '/packageBrowse.do', event);" /> </td>
                </tr>
                <tr>
                    <td align="right" nowrap> <webapps:pageText shared="true"  key="copy.Username" type="pkgbrowsing"/>&nbsp; </td>
                    <td align="left"> <html:text name="packageEditForm" styleId="user" property="user" size="15" value='<%= (String) session.getAttribute("txlist_username") %>' />&nbsp; </td>
                </tr>
                <tr>
                    <td align="right" nowrap> <webapps:pageText shared="true"  key="copy.Password" type="pkgbrowsing"/>&nbsp; </td>
                    <td align="left"> <html:password name="packageEditForm" styleId="pwd" property="pwd" size="15" value='<%= (String) session.getAttribute("txlist_password") %>' />&nbsp;</td>
                </tr>
                <tr>
                    <td> </td>
                    <td align="left"> <input type="button" name="browse" value=" <webapps:pageText shared="true"  key="Go" type="pkgbrowsing"/> " onclick="javascript:sendGoBrowse(document.packageEditForm, '/packageBrowse.do');" > </td>
                </tr>
            </table>
        </TD>
    </TR>
    </TBODY>
    </TABLE>

    <DIV id=dataSection style="OVERFLOW: auto; WIDTH: 100%; HEIGHT: 300px">
      <TABLE cellSpacing=0 cellPadding=0 width=332 border=0>
        <TBODY>
	      <logic:notPresent name="txlist_currenturl" scope="session">
   		    <%-- No transmitter URL --%>
			<tr>
 	       	  <td>
		        <font class="inactiveText"><webapps:pageText shared="true"  key="TransmitterURLPrompt" type="pkgbrowsing"/></font>
		      </td>
	        </tr>
	      </logic:notPresent>

	      <logic:present name="display_rs">
            <% int contentsRowCount = 0; %>
	        <logic:iterate id="channel" name="display_rs">
                <bean:define id="channelProps" name="channel" property="collection" type="java.util.Map"/>
               <tr>
                <logic:equal name="channelProps" property="type" value="channel">
	        <%-- Displaying a channel. Two columns are Tile, Description --%>
 	       	      <td class=rowLevel1>
		            <table cellpadding="0" cellspacing="0" width="100%">
		              <tr>
		                <td>
   		                  <a page="/packageAdd.do" paramName="channelProps" paramProperty="url" paramId="addurl">
       	                     <img src="/shell/common-rsrc/images/package.gif" hspace="8" height="16" width="16" border="0" />
                          </a>
                          <bean:define id="chn" name="channelProps" property="url"/>
  		                  <a class="hoverLink" onclick="addPkg(document.packageEditForm, '/packageAdd.do', '<bean:write name="channelProps" property="url"/>');"
  		                  onmouseover="<%="return Tip('"+ StringEscapeUtils.escapeJavaScript(chn.toString()) +"', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);"%>"
                          onmouseout="return UnTip();"> <bean:write name="channelProps" property="title" filter="true" /> </a>
		                </td>
					  </tr>

		              <logic:present name="display_desc">
		                <logic:present name="channelProps" property="description">
		                  <tr>
		                    <td>&nbsp;</td>
		                    <td>
	                          <i><bean:write name="channelProps" property="description" filter="true" />&nbsp;</i>
							</td>
						  </tr>
		                </logic:present>
   		              </logic:present>
		            </table>
	              </td>
	            </logic:equal>
                <logic:equal name="channelProps" property="type" value="upfolder">
      		      <%-- Displaying an up folder. Only one column showing the Title --%>
	       	      <td class=rowLevel1>
		            <a onclick="sendParentFolder(document.packageEditForm, '/packageBrowse.do', '<bean:write name="channelProps" property="url"/>');">
    	              <img src="/shell/common-rsrc/images/folder_up.gif" hspace="8" height="16" width="16" border="0" />
		            </a>
	                <a styleClass="hoverLink" onclick="sendParentFolder(document.packageEditForm, '/packageBrowse.do', '<bean:write name="channelProps" property="url"/>');">
                       <webapps:pageText shared="true"  key="upFolder" type="pkgbrowsing" />
   		            </a>
	              </td>
	            </logic:equal>

                <logic:equal name="channelProps" property="type" value="folder">
      		      <%-- Displaying a folder. Only one column showing the Title --%>
	       	      <td class=rowLevel1>
		            <a onclick="sendFolder(document.packageEditForm, '/packageBrowse.do', '<bean:write name="channelProps" property="name"/>');">
    	              <img src="/shell/common-rsrc/images/folder_plus.gif" hspace="8" height="16" width="16" border="0" />
		            </a>
  		            <bean:define id="folder" name="channelProps" property="url"/>
                    <a class="hoverLink" onclick="addPkgCfrm(document.packageEditForm, '/packageAdd.do', '<bean:write name="channelProps" property="url"/>');"
                      onmouseover="<%="return Tip('"+ StringEscapeUtils.escapeJavaScript(folder.toString()) +"', WIDTH, '-1',BGCOLOR, '#F5F5F2',FONTCOLOR, '#000000',BORDERCOLOR, '#333300',OFFSETX, 22,OFFSETY, 16,FADEIN, 100);"%>"
                      onmouseout="return UnTip();">
	                  <bean:write name="channelProps" property="title" filter="true" />
					</a>
	              </td>
	            </logic:equal>
	          </tr>
            <% contentsRowCount++;%>
	        </logic:iterate>
	      </logic:present>
        </TBODY>
	  </TABLE>
	</DIV>

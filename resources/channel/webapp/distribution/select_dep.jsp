<%-- Copyright 2001, Marimba Inc. All Rights Reserved.
     Confidential and Proprietary Information of Marimba, Inc.
     @(#)select_dep.jsp

     @author Theen-Theen Tan
     @version 1.16, 06/05/2002
--%>

<%@ include file="/includes/directives.jsp" %>

<%-- Javascript --%>
<script>
  function showAll() {
    document.packageEditForm['value(search)'].value = '*';
    send(document.packageEditForm, '/packageSearchDeployed.do?forwardPage=/distribution/package_assignment.jsp');
  }
</script>


<%@ page buffer = "none" %>
<%@ page import = "com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean,
                   com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm,
                   com.marimba.webapps.intf.IMapProperty" %>
<% String pageBeanName = "pkglist_bean"; %>
		<TR class=smallButtons vAlign=center>
          <TD class=tableRowActions>
            <TABLE style="FONT-SIZE: 10px; COLOR: #ffffff; FONT-FAMILY: Verdana,Arial,sans-serif" cellSpacing=0 cellPadding=0 width="100%" border=0>
              <TBODY>
			    <TR>
                  <TD align=right>
				    <webapps:pageText shared="true"  key="Search" type="pkgbrowsing"/>&nbsp;
				  </TD>
		          <TD>
				<html:text property="value(search)" value='<%= (String) session.getAttribute("page_pkgs_dep_search") %>' onkeypress="checkTypingPackageSearch(document.packageEditForm, '/packageSearchDeployed.do?forwardPage=/distribution/package_assignment.jsp', event);"/>			
  		            <input type="button" name="go" value=" <webapps:pageText shared="true"  key="Go" type="pkgbrowsing" /> " onclick="javascript:send(document.packageEditForm, '/packageSearchDeployed.do?forwardPage=/distribution/package_assignment.jsp');" > &nbsp;<html:link styleClass="textWhite" href="javascript:showAll();"><webapps:pageText shared="true"  key="Reset" type="pkgbrowsing"/></html:link>
                  </TD>
                </TR>
		        <TR>
                  <TD align=right>
				    <webapps:pageText shared="true"  key="Show" type="pkgbrowsing"/>&nbsp;
				  </TD>
                  <TD>
                     <logic:notPresent name="packageEditForm" property="value(show_url)">
 					    <% ((IMapProperty) session.getAttribute("packageEditForm")).setValue("show_url", "false"); %>
                     </logic:notPresent>
		  	        <%-- Flipping between displaying package title or url --%>
			        <logic:equal name="packageEditForm" property="value(show_url)" value="false">
			          <strong>
					    <webapps:pageText shared="true"  key="PackageTitle" type="pkgbrowsing" />
				      </strong>
			          |
			          <a href="<%=request.getContextPath()%>/packageEditFlipState.do?displayType=url" class="textWhite" >
					    <webapps:pageText shared="true"  key="URL" type="pkgbrowsing"/>
					  </a>
			        </logic:equal>
			        <logic:equal name="packageEditForm" property="value(show_url)" value="true">
			          <a href="<%=request.getContextPath()%>/packageEditFlipState.do?displayType=title" class="textWhite" >
					    <webapps:pageText shared="true"  key="PackageTitle" type="pkgbrowsing"/>
					  </a>
			          |
			          <strong>
					    <webapps:pageText shared="true"  key="URL" type="pkgbrowsing" />
					  </strong>
			        </logic:equal>
		          </TD>
		        </TR>
	          </TBODY>
            </TABLE>
	      </TD>
	    </TR>
	  </TBODY>
    </TABLE>
    <DIV class=headerSection style="WIDTH: 100%">
      <TABLE cellSpacing=0 cellPadding=0 width=332 border=0>
        <THEAD>
          <TR>
            <TD class=tableHeaderCell><webapps:pageText shared="true"  key="Package" type="pkgbrowsing" />
	          <logic:equal name="packageEditForm" property="value(show_url)" value="true">
	            <webapps:pageText shared="true"  key="URL" type="pkgbrowsing"/>
	          </logic:equal>
	          <logic:equal name="packageEditForm" property="value(show_url)" value="false">
	            <webapps:pageText shared="true"  key="PackageTitle" type="pkgbrowsing" />
	          </logic:equal>
	        </TD>
		  </TR>
		</THEAD>
      </TABLE>
	</DIV><!--end headerSection-->
    <DIV id=dataSection style="OVERFLOW: auto; WIDTH: 100%; HEIGHT: 300px">
      <TABLE cellSpacing=0 cellPadding=0 width=332 border=0>
        <TBODY>
	      <logic:present name="page_pkgs_dep_rs">
		  <% int contentsRowCount = 0; %>
	        <logic:iterate id="channel" name="display_rs" type="com.marimba.webapps.tools.util.PropsBean">
                <bean:define id="chProps" name="channel" property="collection" type="java.util.Map" />
               <% if (contentsRowCount % 2 == 0){ %>
               <tr>
               <% } else { %>
	           <tr class="alternateRowColor">
               <% } %>
                <TD class=rowLevel1>
   		          <html:link page="/packageAddDeployed.do" paramName="chProps" paramProperty="url" paramId="addurl" styleClass="hoverLink">
		            <img src="/shell/common-rsrc/images/package.gif" hspace="8" height="16" width="16" border="0" />
			      </html:link>
                  <logic:equal name="packageEditForm" property="value(show_url)" value="true">
                  <bean:define id="pqr" name="chProps" property="title" />
		          <html:link page="/packageAddDeployed.do" paramName="chProps" paramProperty="url" paramId="addurl" styleClass="hoverLink" onmouseover="<%="return Tip('"+ org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(pqr.toString()) +"', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);"%>"
                  onmouseout="return UnTip();">

			          <bean:write name="chProps" property="url" filter="true" />
                       </html:link>
			        </logic:equal>
			        <logic:equal name="packageEditForm" property="value(show_url)" value="false">
                    <bean:define id="pqr" name="chProps" property="url" />
                    <html:link page="/packageAddDeployed.do" paramName="chProps" paramProperty="url" paramId="addurl" styleClass="hoverLink" onmouseover="<%="return Tip('"+ org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(pqr.toString()) +"', WIDTH, '-1', BGCOLOR, '#F5F5F2', FONTCOLOR, '#000000', BORDERCOLOR, '#333300', OFFSETX, 22, OFFSETY, 16, FADEIN, 100);"%>"
                     onmouseout="return UnTip();">
			    	  <bean:write name="chProps" property="title" filter="true" />
                       </html:link>
			        </logic:equal>
		         
   		        </td>
              </tr>
			  <% contentsRowCount++;%>
	        </logic:iterate>
          </logic:present>
        </TBODY>
	  </TABLE>
	</DIV>

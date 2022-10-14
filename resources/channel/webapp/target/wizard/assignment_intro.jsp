<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>
<table id="step1Table" width="100%" height="325px" border=0>
    <tr width="100%">
        <td valign="top">
            <img src="/spm/images/security.jpg" width="150px"/><br>
        </td>
        <td width="100%" style="padding-left:20px;" valign="top">
            <b><webapps:pageText key="introduction.title" type="security_content_assignment" shared="true"/></b><hr>
            <webapps:pageText key="introduction.text" type="security_content_assignment" shared="true"/>
        </td>
    </tr>
</table>
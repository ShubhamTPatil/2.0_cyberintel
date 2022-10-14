// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.validations;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.PeerApprovalForm;
import static com.marimba.intf.msf.AppManagerConstants.*;

import com.marimba.intf.msf.*;
import com.marimba.intf.util.IDirectory;
import com.marimba.tools.util.QuotedTokenizer;
import com.marimba.webapps.struts.validation.ValidationUtil;
import com.marimba.webapps.tools.util.KnownActionError;
import org.apache.struts.action.ActionErrors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.StringTokenizer;
import java.util.Vector;
import com.marimba.tools.config.ConfigProps;
import java.io.File;
import com.marimba.tools.config.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Validate Peer approval settings
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class PeerApprovalValidation implements IErrorConstants, IWebAppConstants {

    public static boolean validateAllSettings(java.lang.Object bean, ActionErrors errors,
                                              HttpServletRequest request) {
        String action = request.getParameter("action");
        if (!"save".equals(action)) { // Do not worry about other action like load
            return true;
        }
        PeerApprovalForm form = (PeerApprovalForm) bean;

        boolean mailSettings = ("true".equals(form.getMailSettings()));
        boolean peersSettings = ("true".equals(form.getPeersSettings()));
        boolean isLdapApproval = !("servicenow".equals(form.getPeersType()) || "remedyforce".equals(form.getPeersType()));

        String mailToList = form.getMailToAddress();
        String peersToList = form.getPeersToAddress();
    	
    	
        if (mailSettings) {
            if (!ValidationUtil.required(mailToList)) {
                errors.add("mailToAddress", new KnownActionError(EMAIL_ID_REQUIRED, mailToList));
            } else {
                StringTokenizer st = new StringTokenizer(mailToList, ",");
                while(st.hasMoreTokens()) {
                    String tmp_id = st.nextToken();
                    if (!ValidationUtil.checkEmail(tmp_id)) {
                        errors.add("mailToAddress", new KnownActionError(EMAIL_ID_INVALID, tmp_id));
                    }
                }
            }            
        }
        if (peersSettings && isLdapApproval) {
            if (!ValidationUtil.required(peersToList)) {
                errors.add("peersToAddress", new KnownActionError(LDAP_GROUP_REQUIRED, peersToList));
            } else {
                String[] invalidGrps = checkLdapGroups(peersToList, request);
                if (invalidGrps != null) {
                    for (String grp : invalidGrps) {
                        errors.add("peersToAddress", new KnownActionError(LDAP_GROUP_INVALID, grp));
                    }
                }else{
                	//All given groups are valid , check whether they are not a part of operator group .
                    QuotedTokenizer tok = new QuotedTokenizer(peersToList, ", ", '\\');
                    List operatorGrpList = new ArrayList();
                	List peerApprovalGrpList = new ArrayList();
                	List childGroupsList = new ArrayList();
                	Vector<String> validGrps = new Vector<String>();
                    
                    while (tok.hasMoreTokens()) {
                    	validGrps.add(tok.nextToken().trim());
                    }
                	try{
                        LDAPGroupsValidation validation = new LDAPGroupsValidation(request);
                    	operatorGrpList =validation.getOperatorGroups();
                    	//Empty list if no operator group configured .
                    	//otherwise Operator group size > 0 , check whether group contains childgroups. 
                    	if(operatorGrpList.size() > 0){
                    		childGroupsList = validation.getChildMembers(operatorGrpList);
                    		peerApprovalGrpList = validation.getPeerApprovalGroups(validGrps);
                    		validation.compareOperAndPeerApproval(peerApprovalGrpList,childGroupsList,errors);
                    	}
                    }catch(Exception e){
                    	e.printStackTrace();
                    }
                }
            }
        }
        return errors.isEmpty();
    }

    protected static String[] checkLdapGroups(String groups, HttpServletRequest request) {
        IUserPrincipal userPrincipal = (IUserPrincipal) request.getUserPrincipal();
        ITenant tenant = userPrincipal.getTenant();
        IAccessControlMgr acmgr = tenant.getAccessControlMgr();
        Vector<String> grp = new Vector<String>();
        QuotedTokenizer tok = new QuotedTokenizer(groups, ", ", '\\');

        while (tok.hasMoreTokens()) {
            grp.add(tok.nextToken().trim());
        }

        String[] grps = new String[grp.size()];
        grp.copyInto(grps);
        String activeLdap = acmgr.getActive();

        return acmgr.validateGroups(activeLdap, grps);
    }
}
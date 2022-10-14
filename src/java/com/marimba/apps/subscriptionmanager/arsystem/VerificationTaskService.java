// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.arsystem;

import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

import org.w3c.dom.*;


import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.*;
import com.marimba.apps.subscriptionmanager.users.CLIUser;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.intf.msf.task.*;
import com.marimba.intf.msf.arsys.*;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.IWebServiceHandler;
import com.marimba.webapps.intf.*;


/**
 * @author $Author$
 * @version $version$
 * $Id$
 */

public final class VerificationTaskService extends AbstractAction implements IWebServiceHandler, IErrorConstants    {

    final static String VERIFY_TASK_SERVICE_NAMESPACE = "http://www.bmc.com/schemas/scm/cms/security";
    final static String SERVICE_NS_PREFIX = "impl";
    final static String TASKID_TAG = "taskid";
    final static String TAG_VERIFY_RETURN = "verifyTaskReturn";
    final static String TAG_DETAILS_RETURN = "Details";

    protected SubscriptionMain main;
    protected ITaskMgr taskMgr = null;
    static ARLogMgr arLogMgr = null;

    public VerificationTaskService(SubscriptionMain main, ITaskMgr taskMgr)  {
        this.main = main;
        this.taskMgr = taskMgr;
        arLogMgr = new ARLogMgr(main, main.getAppLog());

    }

    public Element[] handleService(Element[] inputMsg, IUserPrincipal userPrincipal) throws Exception    {

        try {
            CLIUser cliUser = new CLIUser(userPrincipal, main);
            initUser(cliUser);
            Map formParameters = new HashMap();

            formParameters.put(IARConstants.PARAM_TASK_ID, VerificationTaskService.getTaskId(inputMsg));
            arLogMgr.log(ARTaskLogConstants.LOG_AR_TASK_ID, LOG_AUDIT, VerificationTaskService.getTaskId(inputMsg), null, AR_TASK_VERIFIER);
            ARRequestProcessor arRequest = new ARRequestProcessor(main, formParameters, cliUser, taskMgr);
            String returnCode = arRequest.getARObjectSource().doVerifyTaskAction();
            if ("true".equalsIgnoreCase(returnCode)){
                arLogMgr.log(ARTaskLogConstants.LOG_AR_VERIFYTASK_CREATE_OK, LOG_AUDIT, returnCode, null, AR_TASK_VERIFIER);
                return VerificationTaskService.returnSuccess();
            }   else {
                //get message here
                arLogMgr.log(ARTaskLogConstants.LOG_AR_VERIFYTASK_CREATE_ERROR, LOG_MAJOR, returnCode);
                return VerificationTaskService.returnFailure("failed");
            }
        } catch (SystemException syEx) {
              arLogMgr.log(ARTaskLogConstants.LOG_AR_EXCEPTION, LOG_MAJOR, null, syEx);
              return VerificationTaskService.returnFailure(syEx.getMessage());
		}
    }
    public byte[] getResponse() throws Exception {
    	return null;
    }
    public ARRequestProcessor handleRequest(HttpServletRequest request) throws ARManagerException, SystemException {
        IUser user = (IUser) request.getSession().getAttribute(SESSION_SMUSER);
        ARRequestProcessor arRequest = new ARRequestProcessor(main, request.getParameterMap(), user, taskMgr);
        return arRequest;
    }

    public static String getTaskId(Element[] command) {
        NodeList nodeList = (command[0]).getElementsByTagName(TASKID_TAG);
        for (int j = 0; j < nodeList.getLength(); j++) {
            if (nodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                Node ci = nodeList.item(j).getFirstChild();
                if (ci.getNodeType() == Node.TEXT_NODE) {
                    String taskid = ci.getNodeValue();
                    return taskid;
                }
            }
        }
        return null;
    }

    public static Element[] returnFailure(String msg) throws Exception {
        Element[] result = new Element[2];
        try {
            DocumentBuilder docBuilder = null;
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            result[0] = createTextElement(doc, VERIFY_TASK_SERVICE_NAMESPACE, TAG_VERIFY_RETURN, IARConstants.TMS_RETURN_CODE_ERROR);
            arLogMgr.log(ARTaskLogConstants.LOG_AR_RETURN_ELEMENT, LOG_AUDIT, ARUtils.ElementToString(result[0]), null, AR_TASK_VERIFIER);
            result[1] = createTextElement(doc, VERIFY_TASK_SERVICE_NAMESPACE, TAG_DETAILS_RETURN, msg);
            arLogMgr.log(ARTaskLogConstants.LOG_AR_RETURN_ELEMENT, LOG_AUDIT, ARUtils.ElementToString(result[1]), null, AR_TASK_VERIFIER);
            return result;
        } catch (ParserConfigurationException e) {
            arLogMgr.log(ARTaskLogConstants.LOG_AR_EXCEPTION, LOG_MAJOR, null, e);
        }
        return null;
    }

    public static Element[] returnSuccess() throws Exception {
        Element[] result = new Element[1];
        try {
            DocumentBuilder docBuilder = null;
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            result[0] = createTextElement(doc, VERIFY_TASK_SERVICE_NAMESPACE, TAG_VERIFY_RETURN, IARConstants.TMS_RETURN_CODE_OK);
            arLogMgr.log(ARTaskLogConstants.LOG_AR_RETURN_ELEMENT, LOG_AUDIT, ARUtils.ElementToString(result[0]), null, AR_TASK_VERIFIER);
            return result;
        } catch (ParserConfigurationException e) {
            arLogMgr.log(ARTaskLogConstants.LOG_AR_EXCEPTION, LOG_MAJOR, null, e);
        }
        return null;
    }

    private static Element createTextElement(Document doc, String ns, String xmlPath, String text) {
        Element element = doc.createElementNS(ns, xmlPath);
        Text textNode = doc.createTextNode(text);
        element.appendChild(textNode);
        return element;
    }

}


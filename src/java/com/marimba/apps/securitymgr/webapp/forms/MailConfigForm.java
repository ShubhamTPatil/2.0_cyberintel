package com.marimba.apps.securitymgr.webapp.forms;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Apr 28, 2017
 * Time: 11:47:56 AM
 * To change this template use File | Settings | File Templates.
 */

public class MailConfigForm extends ActionForm {
    private String action;
    private String host;
    private String port = "25";
    private String user;
    private String bccMail;
    private String useAuth;
    private String password;
    private String encryption;
    private String senderMail;
    private String senderName;
    private String receiverMails;


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUseAuth() {
        return useAuth;
    }

    public void setUseAuth(String useAuth) {
        this.useAuth = useAuth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBccMail() {
        return bccMail;
    }

    public void setBccMail(String bccMail) {
        this.bccMail = bccMail;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getSenderMail() {
        return senderMail;
    }

    public void setSenderMail(String senderMail) {
        this.senderMail = senderMail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverMails() {
        return receiverMails;
    }

    public void setReceiverMails(String receiverMails) {
        this.receiverMails = receiverMails;
    }

    public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {
        this.useAuth = "false";
    }
}

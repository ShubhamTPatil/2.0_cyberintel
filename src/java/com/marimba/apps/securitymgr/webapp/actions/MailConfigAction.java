package com.marimba.apps.securitymgr.webapp.actions;

import com.marimba.apps.securitymgr.webapp.forms.MailConfigForm;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.intf.util.IConfig;
import com.marimba.mail.core.ClarinetTestMailer;
import com.marimba.mail.intf.IMailer;
import static com.marimba.mail.intf.IMailerConstants.*;
import com.marimba.tools.config.ConfigPrefix;
import com.marimba.tools.config.ConfigUtil;
import com.marimba.tools.util.DebugFlag;
import com.marimba.webapps.tools.action.DelayedAction;
import org.apache.struts.Globals;
import org.apache.struts.action.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Apr 28, 2017
 * Time: 11:47:23 AM
 * To change this template use File | Settings | File Templates.
 */

public class MailConfigAction extends DelayedAction {
    int DEBUG = DebugFlag.getDebug("SUB/MAIL");

    public Task create(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        return new EMailTask((MailConfigForm) form, mapping, request);
    }

    public ActionForward done(Task task, ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        EMailTask emailTask = (EMailTask) task;
        if (emailTask.errors != null && !emailTask.errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, emailTask.errors);
        }
        return emailTask.forward;
    }

    private class EMailTask extends Task {
        String action;
        Locale locale;
        HttpSession sess;
        ActionErrors errors;
        MailConfigForm emailForm;
        ActionMapping mapping;
        ActionForward forward;
        HttpServletRequest request;

        EMailTask(MailConfigForm emailForm, ActionMapping mapping, HttpServletRequest request) {
            this.request = request;
            this.mapping = mapping;
            this.emailForm = emailForm;
            this.sess = request.getSession();
            this.locale = request.getLocale();
            this.action = emailForm.getAction();
            this.errors = (ActionErrors)request.getAttribute(Globals.ERROR_KEY);
            if (this.errors == null) this.errors = new ActionErrors();
        }

        public void execute() {
            ServletContext sc = servlet.getServletConfig().getServletContext();
            SubscriptionMain main = TenantHelper.getTenantSubMain(sc, request);
            IConfig mailConfig = new ConfigPrefix("smtp.", main.getConfig());
            forward = mapping.findForward("view");
            if (action == null || "view".equals(action)) {
                configureForm(emailForm, mailConfig);
                return;
            } else if ("testEmail".equals(action)) {
                if (!validateSettings(emailForm, errors)) {
                    return;
                }
                applySettings(emailForm, mailConfig);
                IMailer mailer = new ClarinetTestMailer(mailConfig);
                String toAddresses = mailConfig.getProperty(EMAIL_PROP_RECEIVER_MAILIDS);
                String bccddresses = mailConfig.getProperty(EMAIL_PROP_BCC_MAILID);
                boolean result = mailer.sendMail(toAddresses, null, bccddresses);
                request.setAttribute("testResult", result ? "ok" : "not_ok");
                request.setAttribute("exceptionMsg", mailer.getRootCause());
                return;
            } else if ("setEmail".equals(action)) {
                if (!validateSettings(emailForm, errors)) {
                    return;
                }
                applySettings(emailForm, mailConfig);
                main.getConfig().save();
            } else if ("removeEmail".equals(action)) {
                removeSettings(mailConfig);
                main.getConfig().save();
            }
            forward = mapping.findForward("done");
        }

        public String getWaitMessage() {
            return "Please Wait...."; //getString("page.global.pleasewait", locale);
        }

        public String getString0(String key) {
            return "Please Wait..."; //getString(key, locale);
        }
    }

    private void configureForm(MailConfigForm form, IConfig mailConfig) {
        ConfigUtil tmpConfig = new ConfigUtil(mailConfig);
        form.setHost(tmpConfig.getProperty(EMAIL_PROP_HOST, ""));
        form.setUser(tmpConfig.getProperty(EMAIL_PROP_USER, ""));
        form.setPassword(tmpConfig.getProperty(EMAIL_PROP_PASSWORD, ""));
        form.setUseAuth(tmpConfig.getProperty(EMAIL_PROP_USEAUTH, "false"));
        form.setEncryption(tmpConfig.getProperty(EMAIL_PROP_ENCRYPTION, "none"));
        form.setPort(tmpConfig.getInteger(EMAIL_PROP_PORT, EMAIL_DEFAULT_PORT) + "");
        form.setSenderMail(tmpConfig.getProperty(EMAIL_PROP_SENDER_MAILID, ""));
        form.setSenderName(tmpConfig.getProperty(EMAIL_PROP_SENDER_NAME, ""));
        form.setBccMail(tmpConfig.getProperty(EMAIL_PROP_BCC_MAILID, ""));
        form.setReceiverMails(tmpConfig.getProperty(EMAIL_PROP_RECEIVER_MAILIDS, ""));
    }

    private void applySettings(MailConfigForm mailForm, IConfig emailConfig) {
        emailConfig.setProperty(EMAIL_PROP_HOST, mailForm.getHost());
        emailConfig.setProperty(EMAIL_PROP_PORT, mailForm.getPort());
        emailConfig.setProperty(EMAIL_PROP_USER, mailForm.getUser());
        emailConfig.setProperty(EMAIL_PROP_USEAUTH, mailForm.getUseAuth());
        emailConfig.setProperty(EMAIL_PROP_PASSWORD, mailForm.getPassword());
        emailConfig.setProperty(EMAIL_PROP_ENCRYPTION, mailForm.getEncryption());
        emailConfig.setProperty(EMAIL_PROP_SENDER_MAILID, mailForm.getSenderMail());
        emailConfig.setProperty(EMAIL_PROP_SENDER_NAME, mailForm.getSenderName());
        emailConfig.setProperty(EMAIL_PROP_BCC_MAILID, mailForm.getBccMail());
        emailConfig.setProperty(EMAIL_PROP_RECEIVER_MAILIDS, mailForm.getReceiverMails());
        emailConfig.setProperty(EMAIL_PROP_MAX_BODY_SIZE, EMAIL_DEFAULT_MAX_BODY_SIZE + "");
        emailConfig.setProperty(EMAIL_PROP_MAX_ATTACHMENT_SIZE, EMAIL_DEFAULT_MAX_ATTACHMENT_SIZE + "");
    }

    private void removeSettings(IConfig mailConfig) {
        String[] allProps = new String[] {
                EMAIL_PROP_HOST, EMAIL_PROP_PORT, EMAIL_PROP_USER, EMAIL_PROP_USEAUTH, EMAIL_PROP_PASSWORD, EMAIL_PROP_ENCRYPTION, EMAIL_PROP_SENDER_MAILID,
                EMAIL_PROP_SENDER_NAME, EMAIL_PROP_BCC_MAILID, EMAIL_PROP_RECEIVER_MAILIDS, EMAIL_PROP_MAX_BODY_SIZE, EMAIL_PROP_MAX_ATTACHMENT_SIZE
        };
        for (String prop : allProps) mailConfig.setProperty(prop, null);
    }

    private boolean validateSettings(MailConfigForm mailForm, ActionErrors errors) {
        boolean ok = true;

        String host = mailForm.getHost();
        String port = mailForm.getPort();
        String user = mailForm.getUser();
        String password = mailForm.getPassword();
        String senderMail = mailForm.getSenderMail();
        String receiverMails = mailForm.getReceiverMails();
        boolean useAuth = "true".equals(mailForm.getUseAuth());

        if (host == null || host.trim().isEmpty()) {
            ok = false;
            errors.add("host", new ActionMessage("errors.email.invalidServer"));
        }
        if (!isValidSize(port) || Integer.parseInt(port) > EMAIL_PORT_MAX_RANGE) {
            ok = false;
            errors.add("port", new ActionMessage("errors.email.invalidPort"));
        }

        if (useAuth) {
            if (user == null || user.isEmpty()) {
                ok = false;
                errors.add("user", new ActionMessage("errors.email.invalidUser"));
            }
            if (password == null || password.isEmpty()) {
                ok = false;
                errors.add("password", new ActionMessage("errors.email.invalidPassword"));
            }
        }

        if (senderMail == null || senderMail.trim().isEmpty()) {
            ok = false;
            errors.add("senderMail", new ActionMessage("errors.email.invalidSenderid"));
        }
        if (receiverMails == null || receiverMails.trim().isEmpty()) {
            ok = false;
            errors.add("receiverMails", new ActionMessage("errors.email.invalidReceiverid"));
        }

        if (!errors.isEmpty()) {
            return ok;
        }
        return ok;
    }

    private boolean isValidSize(String str) {
        try {
            int val = Integer.parseInt(str.trim());
            if (val > 0) {
                return true;
            }
        } catch(NumberFormatException nfe) {/**/}
        return false;
    }

    public static boolean isValidEmailAddress(String email) {
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}

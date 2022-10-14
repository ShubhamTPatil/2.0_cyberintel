// Copyright 1996-2012, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager;

import static com.marimba.apps.subscriptionmanager.intf.IAppConstants.*;
import com.marimba.tools.util.DebugFlag;

import javax.mail.*;
import static javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Description about the class PolicyMailSender
 *
 * @author Tamilselvan Teivasekamani
 * @version $Revision$,  $Date$
 */

public class MailSender implements Runnable {
    int DEBUG  = DebugFlag.getDebug("SUB/MAIL");

    private Map<String, String> props;
    private MailFormatter mailFormatter;

    private int intPort = 25; //DEFAULT_SMTP_PORT
    private String CONTENT_TYPE = "text/html; charset=UTF-8";

    public MailSender(Map<String, String> props, MailFormatter mailFormatter) {
        this.props = props;
        this.mailFormatter = mailFormatter;
    }

    public void run() {
        debug("Invoking send mail operations......");

        String host = props.get(PROP_SMTP_HOST);
        String from = props.get(PROP_SMTP_USER);
        String port = props.get(PROP_SMTP_PORT);
        Set<String> toAddressList = getRecipientsList(props.get(PROP_MAIL_TO));
        Set<String> ccAddressList = getRecipientsList(props.get(PROP_MAIL_CC));

        if (toAddressList.isEmpty() || null == host) {
            debug("Problem in sending E-Mail, either the to address is empty or the host is null");
            return;
        }

        try {
            intPort = Integer.parseInt(port);
        } catch (NumberFormatException nfex) {
            if (DEBUG > 3) nfex.printStackTrace();
        }

        // try to set from address as policy user instead of blank, but it may throws exception "530 SMTP authentication is required."
        // for this we have to enable "Use authentication" from CMS E-Mail settings
        from =  (null == from) ? mailFormatter.getCreatedBy() : from;

        debug("Mailing to " + toAddressList + " by [" + from + "] via [" + host + "/" + intPort + "]");

        Properties mailProps = new Properties();

        mailProps.setProperty(MAIL_SMTP_HOST, host);
        mailProps.setProperty(MAIL_SMTP_PORT, String.valueOf(intPort));
        mailProps.setProperty(MAIL_SMTP_AUTH, props.get(PROP_USE_AUTH));
        mailProps.setProperty(MAIL_DEBUG, (DEBUG > 3) ? "true" : "false");

        Session session;
        Transport transport = null;

        if ("true".equalsIgnoreCase(props.get(PROP_USE_AUTH))) {
            SMTPAuthenticator auth = new SMTPAuthenticator(props.get(PROP_SMTP_USER), props.get(PROP_SMTP_PWD));
            session = Session.getDefaultInstance(mailProps, auth);
        } else {
            session = Session.getDefaultInstance(mailProps);
        }

        session.setDebug(DEBUG > 3);

        try {
            javax.mail.Message message = new javax.mail.internet.MimeMessage(session);

            message.setSentDate(new Date());
            message.setFrom(new InternetAddress(from, encode(from)));
            message.addRecipients(RecipientType.TO, getToAddress(toAddressList));
            if (!ccAddressList.isEmpty()) {
                message.addRecipients(RecipientType.CC, getToAddress(ccAddressList));
            }
            message.setSubject(encode(mailFormatter.getMailSubject().toString()));

            StringBuilder mailContent = new StringBuilder(1024);

            mailContent.append(mailFormatter.getMailHeader());
            mailContent.append(mailFormatter.getMailBody());
            mailContent.append(mailFormatter.getMailFooter());

            debug("Mail content: " + mailContent.toString());

            Multipart mp = new MimeMultipart();
            BodyPart pixPart = new MimeBodyPart();
            pixPart.setContent(mailContent.toString(), CONTENT_TYPE);
            mp.addBodyPart(pixPart);

            message.setContent(mp);

            transport = session.getTransport("smtp");
            transport.connect();
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            System.out.println("Successfully sent policy update status mail to addresses: " + toAddressList);
        } catch(AuthenticationFailedException auex) {
            debug("AuthenticationFailedException occured while sending mail to the address: " + toAddressList + ", exception: " + auex.getMessage());
            if (DEBUG > 3) auex.printStackTrace();
        } catch (MessagingException mex) {
            debug("ERROR : Failed to send E-Mail form [" + from + "] " + "to the address: " + toAddressList + ", exception: " + mex.getMessage());
            if (DEBUG > 3) mex.printStackTrace();
        } catch (Exception ex) {
            debug("Exception occured while sending mail to the address: " + toAddressList + ", exception: " + ex.getMessage());
            if (DEBUG > 3) ex.printStackTrace();
        } finally {
            if (null != transport) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    if (DEBUG > 3) e.printStackTrace();
                }
            }
        }
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        String userName;
        String password;

        private SMTPAuthenticator(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password);
        }
    }

    private Set<String> getRecipientsList(String recipients) {
        Set<String> rec = new HashSet<String>();
        if (null == recipients) {
            return rec;
        }
        StringTokenizer tokens = new StringTokenizer(recipients, "\n\r\t;, ");
        while (tokens.hasMoreTokens()) {
            rec.add(tokens.nextToken());
        }
        return rec;
    }

    private Address[] getToAddress(Set<String> addressList) {
        int aInt = 0;
        Address[] add = new Address[addressList.size()];
        for (String aAddressStr : addressList) {
            try {
                add[aInt] = new InternetAddress(encode(aAddressStr), false);
                aInt ++;
            } catch (AddressException e) {
                if (DEBUG > 3) e.printStackTrace();
            } catch (MessagingException e) {
                if (DEBUG > 3) e.printStackTrace();
            }            
        }
        return add;
    }

    public String encode(String text) throws MessagingException {
        try {
            // encode the text using "UTF-8" charset and Base64 encoding.
            text = MimeUtility.encodeText(text,"UTF-8", "B");
        } catch(UnsupportedEncodingException ue) {
            debug("Encoding Error : " + ue.getMessage());
            throw new MessagingException(ue.getMessage());
        }
        return text;
    }

    private void debug(String msg) {
        if (DEBUG > 0) System.out.println("PolicyMailSender: " + msg);
    }
}

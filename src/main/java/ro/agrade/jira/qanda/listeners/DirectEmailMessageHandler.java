/*
 * Created on 11/12/13
 */
package ro.agrade.jira.qanda.listeners;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The direct way of sending emails
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class DirectEmailMessageHandler extends AbstractEmailMessageHandler {
    private static final Log LOG = LogFactory.getLog(DirectEmailMessageHandler.class);

    private final MailServerManager mailServerManager;

    public DirectEmailMessageHandler(MailServerManager mailServerManager, ApplicationProperties props) {
        super(props);
        this.mailServerManager = mailServerManager;
    }

    @Override
    protected void sendMail(String [] recipients,
                          String subject,
                          String message ,
                          String from) throws MessagingException {

        SMTPMailServer server = mailServerManager.getDefaultSMTPMailServer();
        if(server == null) {
            LOG.debug("Email server is not configured. QandA is unable to send mails ...");
            return;
        }
        LOG.debug("Email message: initializing.");
        //Set the host smtp address
        Properties props = new Properties();

        String proto = server.getMailProtocol().getProtocol();

        props.put("mail.transport.protocol", proto);
        props.put("mail." + proto + ".host", server.getHostname());
        props.put("mail." + proto + ".port", server.getPort());

        String username = server.getUsername();
        String password = server.getPassword();


        Authenticator auth = null;

        if(username != null && password != null) {
            auth = new SMTPAuthenticator(username, password);
            props.put("mail." + proto + ".auth", "true");
        }
        Session session;
        try {
            session = auth != null
                    ? Session.getDefaultInstance(props, auth)
                    : Session.getDefaultInstance(props);
        } catch (SecurityException ex){
            LOG.warn("Could not get default session. Attempting to create a new one.");
            session = auth != null
                    ? Session.getInstance(props, auth)
                    : Session.getInstance(props);
        }

        // create a message
        MimeMessage msg = new MimeMessage(session);
        Multipart multipart = new MimeMultipart();

        if(from == null) {
            from = server.getDefaultFrom();
        }
        // set the from address
        if(from != null) {
            InternetAddress addressFrom = new InternetAddress(from);
            msg.setFrom(addressFrom);
        }

        if(recipients != null && recipients.length > 0 ) {
            // set TO address(es)
            InternetAddress[] addressTo = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++){
                addressTo[i] = new InternetAddress(recipients[i]);
            }
            msg.setRecipients(Message.RecipientType.TO, addressTo);
        }


        // Setting the Subject
        msg.setSubject(subject);

        // Setting text content
        MimeBodyPart contentPart = new MimeBodyPart();
        contentPart.setContent(message, "text/html; charset=utf-8");
        multipart.addBodyPart(contentPart);

        msg.setContent(multipart);
        Transport.send(msg);
        LOG.debug("Email message sent successfully.");
    }
}

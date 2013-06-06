/*
 * Created on 6/4/13
 */
package ro.agrade.jira.qanda.listeners;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.sal.api.message.I18nResolver;

import ro.agrade.jira.qanda.QandAEvent;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The email message handler
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class EmailMessageHandler implements MessageHandler {
    private static final Log LOG = LogFactory.getLog(EmailMessageHandler.class);
    private final MailServerManager mailServerManager;
    private final I18nResolver i18nResolver;
    private final ApplicationProperties props;

    public EmailMessageHandler(final MailServerManager mailServerManager,
                               final ApplicationProperties props,
                               final I18nResolver i18nResolver) {
        this.mailServerManager = mailServerManager;
        this.i18nResolver = i18nResolver;
        this.props = props;
    }

    @Override
    public void handleMessage(User user, QandAEvent qaEvent) {
        if(user == null || user.getEmailAddress() == null) {
            LOG.warn(String.format("Cannot sent mail to user %s", user != null ? user.getEmailAddress() : "-?-"));
            return;
        }
        try {
            sendMail(new String[] {user.getEmailAddress()},
                     createSubjectFromEvent(qaEvent),
                     createBodyFromEvent(qaEvent),
                     null);
        } catch(MessagingException e) {
            LOG.error(String.format("Could not sent mail to recipient: %s", user.getEmailAddress()), e);
        }
    }

    private String createBodyFromEvent(QandAEvent qaEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        sb.append(JIRAUtils.getIssueJIRAPath(props, qaEvent.getIssueKey() + "?page=ro.agrade.jira.qanda:qanda-tabpanel"));
        sb.append("\n\n");
        sb.append(i18nResolver.getText("qanda.mail.firstline.1"));
        sb.append(" ");
        sb.append(qaEvent.getUser().getDisplayName());
        sb.append(" [ ").append(qaEvent.getUser().getEmailAddress()).append(" ] ");
        sb.append(i18nResolver.getText("qanda.mail.firstline.2." + qaEvent.getType().name()));
        sb.append("\n\n");
        sb.append(qaEvent.getText());
        sb.append("\n");
        return sb.toString();
    }

    private String createSubjectFromEvent(QandAEvent qaEvent) {
        String action = i18nResolver.getText("qanda.mail.subject." + qaEvent.getType().name());
        return String.format("[JIRA Q&A: %s] %s %s",
                             qaEvent.getIssueKey(),
                             qaEvent.getUser().getDisplayName(),
                             action);
    }

    private void sendMail(String [] recipients,
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
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(message, "text/plain");
        multipart.addBodyPart(textPart);

        msg.setContent(multipart);
        Transport.send(msg);
        LOG.debug("Email message sent successfully.");
    }
}

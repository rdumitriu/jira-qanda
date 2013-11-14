/*
 * Created on 11/12/13
 */
package ro.agrade.jira.qanda.listeners;

import javax.mail.*;
import javax.mail.internet.*;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mail.Email;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.SingleMailQueueItem;

/**
 * The mailer that puts everything into JIRA's queue
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class QueuedEmailMessageHandler extends AbstractEmailMessageHandler {
    private final MailQueue queue;

    public QueuedEmailMessageHandler(MailQueue queue, ApplicationProperties props) {
        super(props);
        this.queue = queue;
    }

    /**
     * Send an email
     *
     * @param recipients to
     * @param subject    subject
     * @param message    the message
     * @param from       from.
     * @throws javax.mail.MessagingException
     */
    @Override
    protected void sendMail(String[] recipients, String subject, String message, String from) throws MessagingException {
        StringBuilder recs = new StringBuilder(recipients[0]);
        if(recipients.length > 1) {
            for(int i = 1; i < recipients.length; i++) {
                recs.append(",").append(recipients[i]);
            }
        }
        Email email = new Email(recs.toString());
        email.setSubject(subject);
        if(from != null) {
            email.setFrom(from);
        }

        Multipart multipart = new MimeMultipart();
        // Setting text content
        MimeBodyPart contentPart = new MimeBodyPart();
        contentPart.setContent(message, "text/html; charset=utf-8");
        multipart.addBodyPart(contentPart);

        email.setMultipart(multipart);

        SingleMailQueueItem mailItem = new SingleMailQueueItem(email);
        queue.addItem(mailItem);
    }
}

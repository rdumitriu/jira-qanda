/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/4/13
 */
package ro.agrade.jira.qanda.listeners;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.i18n.JiraI18nResolver;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import ro.agrade.jira.qanda.QandAEvent;
import ro.agrade.jira.qanda.QandAException;
import ro.agrade.jira.qanda.plugin.PluginStorage;
import ro.agrade.jira.qanda.utils.ApplicationContextProvider;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import javax.mail.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The email message handler
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public abstract class AbstractEmailMessageHandler implements MessageHandler {
    private static final Log LOG = LogFactory.getLog(AbstractEmailMessageHandler.class);
    private final ApplicationProperties props;

    public AbstractEmailMessageHandler(final ApplicationProperties props) {
        this.props = props;
    }

    @Override
    public void handleMessage(ApplicationUser user, QandAEvent qaEvent) {
        if(user == null || user.getEmailAddress() == null) {
            LOG.warn(String.format("Cannot sent mail to user %s", user != null ? user.getEmailAddress() : "-?-"));
            return;
        }
        PluginStorage.submitTask(new EmailTask(user, qaEvent));
        LOG.debug("Email task submitted");
    }

    /**
     * Our beloved mail task
     */
    class EmailTask implements Runnable {
        private ApplicationUser user;
        private QandAEvent qaEvent;
        private final I18nResolver i18nResolver;

        EmailTask(ApplicationUser user, QandAEvent qaEvent) {
            this.user = user;
            this.qaEvent = qaEvent;
            //this is stupid, it cannot be resolved by ComponentAccessor ?!? crazy stuff here, my friend
            this.i18nResolver = new JiraI18nResolver(ComponentAccessor.getJiraAuthenticationContext(),
                                                     ComponentAccessor.getI18nHelperFactory());
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            ClassLoader origCL = Thread.currentThread().getContextClassLoader();
            try {
                String body = createBodyFromEvent(qaEvent);
                Thread.currentThread().setContextClassLoader(ComponentManager.class.getClassLoader());
                sendMail(new String[]{user.getEmailAddress()},
                         createSubjectFromEvent(qaEvent),
                         body,
                         null);
            } catch(MessagingException e) {
                LOG.error(String.format("Could not send mail to recipient: %s", user.getEmailAddress()), e);
            } catch (Throwable t) {
                LOG.error(String.format("Could not send mail to recipient: %s", user.getEmailAddress()), t);
            } finally {
                Thread.currentThread().setContextClassLoader(origCL);
            }
        }

        private String createBodyFromEvent(QandAEvent qaEvent) {
            TemplateRenderer tr = ApplicationContextProvider.getBean("templateRenderer", TemplateRenderer.class);
            if(tr == null){
                String msg = "Could not find template renderer for email";
                LOG.error(msg);
                throw new QandAException(msg);
            }

            Map<String, Object> vp = new HashMap<String, Object>();
            vp = JiraVelocityUtils.getDefaultVelocityParams(vp, ComponentAccessor.getJiraAuthenticationContext());
            vp.put("e", qaEvent);
            vp.put("issueLink", JIRAUtils.getIssueJIRAPath(props, qaEvent.getIssueKey() + "?page=ro.agrade.jira.qanda:qanda-tabpanel"));
            vp.put("wikiRenderer", ComponentAccessor.getRendererManager().getRendererForType("atlassian-wiki-renderer"));
            vp.put("renderContext", new IssueRenderContext(qaEvent.getIssue()));
            StringWriter sw = new StringWriter();
            try {
                tr.render("/templates/email.vm", vp, sw);
                sw.flush();
            } catch (IOException e) {
                String msg = "Could not render template";
                LOG.error(msg, e);
                throw new QandAException(msg, e);
            }
            return sw.toString();
        }

        private String createSubjectFromEvent(QandAEvent qaEvent) {
            String action = i18nResolver.getText("qanda.mail.subject." + qaEvent.getType().name());
            return String.format("[JIRA Q&A: %s] %s %s",
                                 qaEvent.getIssueKey(),
                                 qaEvent.getUser().getDisplayName(),
                                 action);
        }
    }

    /**
     * Send an email
     * @param recipients to
     * @param subject subject
     * @param message the message
     * @param from from.
     * @throws MessagingException
     */
    protected abstract void sendMail(String [] recipients,
                          String subject,
                          String message ,
                          String from) throws MessagingException;
}

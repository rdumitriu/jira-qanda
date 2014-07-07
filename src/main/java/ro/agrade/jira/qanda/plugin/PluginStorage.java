/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 */
package ro.agrade.jira.qanda.plugin;

import java.util.concurrent.locks.*;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.component.ComponentAccessor;

import ro.agrade.jira.qanda.ExpertGroupService;
import ro.agrade.jira.qanda.QandAListener;
import ro.agrade.jira.qanda.listeners.*;
import ro.agrade.jira.qanda.utils.ApplicationContextProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The plugin storage
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class PluginStorage {
    private static final Log LOG = LogFactory.getLog(PluginStorage.class);
    private static final int DEFAULT_NO_THREADS = 2;
    public static final String CONFIG_KEY_PERMS_GROUP = "perms.group";
    public static final String CONFIG_KEY_MAIL_NO_THREADS = "mail.no.threads";
    public static final String CONFIG_KEY_MAIL_USE_QUEUE = "mail.use.queue";
    private static PluginStorage ourInstance;
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private PluginConfiguration config;
    private Group qandaGroup;
    private AsyncRunner runner;
    private QandAListener [] listeners;

    /**
     * Get the instance for this plugin
     * @return the one and only instance
     */
    private static PluginStorage getInstance() {
        // we need to lazy load this because our components
        // are not yet initialized at bundle start()
        try {
            lock.readLock().lock();
            if(ourInstance == null) {
                lock.readLock().unlock();
                try {
                    lock.writeLock().lock();
                    if(ourInstance == null) {
                        ourInstance = new PluginStorage();
                    }
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            return ourInstance;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Call it at init
     */
    private void init() {
        this.config = new PluginConfiguration();

        this.qandaGroup = null;
        //cache here the value for the QandA group, if any
        String groupName = config.getStringConfig(CONFIG_KEY_PERMS_GROUP, null);
        if(groupName != null) {
            qandaGroup = ComponentAccessor.getGroupManager().getGroup(groupName);
            if(qandaGroup == null) {
                LOG.warn(String.format("Wrong configuration for QandA. Please define '%s' group", groupName));
            }
        }

        runner = new AsyncRunner(config.getIntConfig(CONFIG_KEY_MAIL_NO_THREADS, DEFAULT_NO_THREADS));
        LOG.debug("QandA is now initialized");
    }

    private void cleanup() {
        runner.shutdown();
        listeners = null;
    }

    /**
     * Submits a task
     * @param r the task
     */
    public static void submitTask(Runnable r) {
        getInstance().runner.runTask(r);
    }

    /**
     * Gets the configuration
     * @return the configuration
     */
    public static PluginConfiguration getConfig() {
        return getInstance().config;
    }

    /**
     * Gets the group if you configure it to be available only for a certain group
     * @return the group
     */
    public static Group getGroup() {
        return getInstance().qandaGroup;
    }

    /**
     * Call this on shutdown.
     */
    public static void shutdown() {
        try {
            lock.writeLock().lock();
            if(ourInstance != null) {
                ourInstance.cleanup();
                ourInstance = null;
            }
        } finally {
            lock.writeLock().unlock();
        }

    }

    /**
     * Reconfigures the singleton
     */
    public static void reconfigure() {
        shutdown();
        getInstance(); //reinit here
    }

    /**
     * We do not care to synchronize this, it's read only
     * @return the list of listeners
     */
    public static QandAListener [] getConfiguredListeners() {
        return getInstance().getInternalConfiguredListeners();
    }

    private QandAListener [] getInternalConfiguredListeners() {
        try {
            lock.readLock().lock();
            if(listeners == null) {
                lock.readLock().unlock();
                try {
                    lock.writeLock().lock();
                    if(listeners == null) {
                        initListeners();
                    }
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
            return listeners;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void initListeners() {
        MessageHandler handler;
        if(this.config.getBooleanConfig(CONFIG_KEY_MAIL_USE_QUEUE, true)) {
            LOG.info("QandA: Configured mailer via JIRA queue");
            handler = new QueuedEmailMessageHandler(ComponentAccessor.getMailQueue(),
                                                    ComponentAccessor.getApplicationProperties());
        } else {
            LOG.info("QandA: Configured mailer - direct");
            handler = new DirectEmailMessageHandler(ComponentAccessor.getMailServerManager(),
                                                    ComponentAccessor.getApplicationProperties());
        }
        listeners = new QandAListener[1];
        listeners[0] = new LicensedListener(ComponentAccessor.getUserManager(),
                                            handler,
                                            ApplicationContextProvider.getBean(ExpertGroupService.class),
                                            ComponentAccessor.getWatcherManager());
    }

    private PluginStorage() {
        init();
    }
}

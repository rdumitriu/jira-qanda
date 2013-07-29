package ro.agrade.jira.qanda.plugin;

import com.atlassian.jira.component.ComponentAccessor;

import ro.agrade.jira.qanda.QandAListener;
import ro.agrade.jira.qanda.listeners.EmailMessageHandler;
import ro.agrade.jira.qanda.listeners.StandardListener;

/**
 * The plugin storage
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class PluginStorage {
    private static final int DEFAULT_NO_THREADS = 2;
    private static PluginStorage ourInstance = new PluginStorage();
    private AsyncRunner runner;
    private QandAListener [] listeners;

    /**
     * Get the instance for this plugin
     * @return the one and only instance
     */
    public static PluginStorage getInstance() {
        return ourInstance;
    }

    /**
     * Call it at init
     */
    public void init() {
        runner = new AsyncRunner(DEFAULT_NO_THREADS);
        listeners = new QandAListener[1];
        listeners[0] = new StandardListener(ComponentAccessor.getUserManager(),
                                            new EmailMessageHandler(ComponentAccessor.getMailServerManager(),
                                                                    ComponentAccessor.getApplicationProperties()));
    }

    /**
     * Submits a task
     * @param r the task
     */
    public void submitTask(Runnable r) {
        runner.runTask(r);
    }

    /**
     * Call this on shutdown.
     */
    public void cleanup() {
        runner.shutdown();
    }

    /**
     * We do not care to synchronize this, it's read only
     * @return the list of listeners
     */
    public QandAListener [] getConfiguredListeners() {
        return listeners;
    }

    private PluginStorage() {
    }
}

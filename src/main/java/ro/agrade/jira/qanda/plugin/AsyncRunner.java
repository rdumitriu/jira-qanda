/*
 * Created at: 6/8/12, 4:55 PM  
 * 
 * File: AsyncRunner.java
 */
package ro.agrade.jira.qanda.plugin;

import java.util.concurrent.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This should be used to run asynchronously some tasks. Our email handling
 * pretty much
 * <ol type='a'>
 * <li>post a callable which will produce at some point a result T</li>
 * <li>You will receive an id of the task (an int)</li>
 * <li>from time to time, check if there's an result using that id; you can call
 * it from other threads, not necessary from the one you posted it. DO NOT
 * busy-wait on it !!! If you do, you have other processing paradigm!</li>
 * <li>if you have a result, get the result and continue</li>
 * </ol>
 *
 * It will be usually included into some singleton, since you will have to care
 * about its life-cycle (shutdown) and make sure there's only one instance of
 * it.
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 */
public class AsyncRunner {
    private static final Log LOG = LogFactory.getLog(AsyncRunner.class);

    private final ExecutorService executorPool;
    public static final int DEFAULT_NO_THRS = 1;

    /**
     * Construct an async runner.
     */
    public AsyncRunner() {
        this(DEFAULT_NO_THRS);
    }

    /**
     * Construct an async runner.
     *
     * @param nThreads the number of threads
     */
    public AsyncRunner(int nThreads) {
        executorPool = Executors.newFixedThreadPool(nThreads);
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("AsyncRunner instance: no threads configured to %d",
                                    nThreads));
        }
    }

    /**
     * You MUST call this to perform cleanup of resources
     */
    public void shutdown() {
        LOG.debug("Shutting down AsyncRunner instance");
        executorPool.shutdown();
    }

    /**
     * Adds a task (a callable) to be executed on the executors pool
     * @param runnable the callable
     */
    public void runTask(Runnable runnable) {
        executorPool.submit(runnable);
    }

}

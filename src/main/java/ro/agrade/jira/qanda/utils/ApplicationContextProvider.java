/*
 *
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Date: 6/17/13
 * Time: 11:29 PM
 */
package ro.agrade.jira.qanda.utils;

import org.springframework.context.ApplicationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manual Spring - step 2
 *
 * @author Florin Manaila (florin.manaila@gmail.com)
 */
public class ApplicationContextProvider {
    
    private static final Log LOG = LogFactory.getLog(ApplicationContextProvider.class);

    private static ApplicationContext context;
    private static volatile boolean started;

    static void setApplicationContext(ApplicationContext appContext){
        context = appContext;
        started = true;
    }

    public static boolean isStarted() {
        return started;
    }

    /**
     * Gets the bean from the spring context. Works better than ComponentAccessor.
     * @param name the name
     * @param type the type
     * @return
     */
    public static <T> T getBean(String name, Class<T> type){
        if(context == null){
            LOG.warn("Context still null. Maybe not yet initialized?");
            return null;
        }
        return (T) context.getBean(name, type);
    }

    /**
     * Gets the bean from the spring context. Works better than ComponentAccessor.
     * @param name the name
     * @param type the type
     * @return
     */
    public static <T> T getBean(Class<T> type){
        if(context == null){
            LOG.warn("Context still null. Maybe not yet initialized?");
            return null;
        }
        String [] names = context.getBeanNamesForType(type);
        return (names != null && names.length > 0 ? getBean(names[0], type) : null);
    }

}

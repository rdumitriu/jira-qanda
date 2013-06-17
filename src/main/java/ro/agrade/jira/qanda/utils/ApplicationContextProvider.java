/**
 * Created with IntelliJ IDEA.
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

    static void setApplicationContext(ApplicationContext appContext){
        context = appContext;
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

}

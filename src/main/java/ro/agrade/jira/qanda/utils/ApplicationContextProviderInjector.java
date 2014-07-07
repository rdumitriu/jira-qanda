/*
 *
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Date: 6/17/13
 * Time: 11:28 PM
 */
package ro.agrade.jira.qanda.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Manual Spring - step 1
 *
 * @author Florin Manaila (florin.manaila@gmail.com)
 */
public class ApplicationContextProviderInjector implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.setApplicationContext(applicationContext);
    }

}

/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/4/13
 */
package ro.agrade.jira.qanda.listeners;

import com.atlassian.jira.user.ApplicationUser;

import ro.agrade.jira.qanda.QandAEvent;

/**
 * That's the message handler interface
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public interface MessageHandler {

    /**
     * Handles the message
     *
     * @param user the user
     * @param qaEvent the event
     */
    public abstract void handleMessage(ApplicationUser user, QandAEvent qaEvent);
}

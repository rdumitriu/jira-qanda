/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/4/13
 */
package ro.agrade.jira.qanda.listeners;

import java.util.*;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import ro.agrade.jira.qanda.*;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The standard listener.
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class StandardListener implements QandAListener {
    private static final Log LOG = LogFactory.getLog(StandardListener.class);
    private MessageHandler handler;
    private UserManager userManager;

    public StandardListener(UserManager userManager, MessageHandler handler) {
        this.handler = handler;
        this.userManager = userManager;
    }

    /**
     * Called on the event
     *
     * @param qaEvent the event
     */
    @Override
    public void onEvent(QandAEvent qaEvent) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Received event %s (Issue %s, User %s)",
                                    qaEvent.getType(),
                                    qaEvent.getIssueKey(),
                                    qaEvent.getUser() != null ? qaEvent.getUser().getDisplayName() : ""));
        }
        Set<String> extractedUsers = calculateUserSet(qaEvent);

        for(String s : extractedUsers) {
            handleNotify(s, qaEvent);
        }
    }

    public MessageHandler getHandler() {
        return handler;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Calculates the user set
     * @param qaEvent the event
     * @return the set of users to be notified
     */
    protected Set<String> calculateUserSet(QandAEvent qaEvent) {
        //1: extract users
        Set<String> extractedUsers = new HashSet<String>();
        extractUsersFromText(qaEvent.getPreambleText(), extractedUsers);
        extractUsersFromText(qaEvent.getText(), extractedUsers);

        if(qaEvent.getAdditionalUsers() != null) {
            extractedUsers.addAll(qaEvent.getAdditionalUsers());
        }

        //2: remove current user (or should I still send it?)
        if(qaEvent.getUser() != null) {
            extractedUsers.remove(qaEvent.getUser().getName());
        }
        return extractedUsers;
    }

    /**
     * Handles the unknown mention. By default, does nothing
     *
     * @param s the unknown mention
     * @param qaEvent the event
     */
    protected void handleUnknownMention(String s, QandAEvent qaEvent) {
    }

    private void extractUsersFromText(String text, Set<String> extractedUsers) {
        if(text != null) {
            Set<String> extr = extractUsers(text);
            if(extr != null) {
                extractedUsers.addAll(extr);
            }
        }
    }

    private void handleNotify(String s, QandAEvent qaEvent) {
        ApplicationUser user = JIRAUtils.toUserObject(userManager, s);
        if(user != null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Notify user %s on event %s", s, qaEvent.getType()));
            }
            handler.handleMessage(user, qaEvent);
        } else { //user was null, maybe it's a group
            handleUnknownMention(s, qaEvent);

        }
    }

    private Set<String> extractUsers(String text) {
        if(text == null) {
            return null;
        }
        Set<String> ret = new HashSet<String>();
        int index = text.indexOf("[~");
        int totalLen = text.length();
        while(index >= 0 && index < totalLen) {
            StringBuilder sb = new StringBuilder();
            for(int i = index + 2; i < totalLen && text.charAt(i) != ']'; i++) {
                sb.append(text.charAt(i));
            }
            String userName = sb.toString();
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Detected username >>%s<<", userName));
            }
            int originalLength = userName.length() + 3; // [~]
            userName = userName.trim(); //just to make sure there are no extra spaces
            if(!"".equals(userName)) {
                //we have a real username, so we can check it afterwards
                ret.add(userName);
            }
            //increment the index
            index += originalLength;
            if(index < totalLen) {
                index = text.indexOf("[~", index);
            }
        }
        return ret.size() == 0 ? null : ret;
    }
}

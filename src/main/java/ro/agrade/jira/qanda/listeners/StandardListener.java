/*
 * Created on 6/4/13
 */
package ro.agrade.jira.qanda.listeners;

import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserManager;

import ro.agrade.jira.qanda.QandAEvent;
import ro.agrade.jira.qanda.QandAListener;

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
        Set<String> extractedUsers = extractUsers(qaEvent.getText());
        if(extractedUsers != null) {
            for(String s : extractedUsers) {
                handleNotify(s, qaEvent);
            }
        }
    }

    private void handleNotify(String s, QandAEvent qaEvent) {
        User user = toUserObject(s);
        if(user != null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Notify user %s on event %s", s, qaEvent.getType()));
            }
            handler.handleMessage(user, qaEvent);
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

    private User toUserObject(String uNameOrKey) {
        //::TODO:: redo for Jira 6
        User ret = userManager.getUser(uNameOrKey);
        if(ret == null) {
            ret = userManager.getUserObject(uNameOrKey);
        }
        return ret;
    }
}

/*
 * Created on 6/2/13
 */
package ro.agrade.jira.qanda;

import com.atlassian.crowd.embedded.api.User;

/**
 * The event passed on the listener
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class QandAEvent {

    /**
     * The event
     */
    public enum Type {
        QUESTION_ADDED,
        QUESTION_MODIFIED,
        QUESTION_DELETED,
        QUESTION_SOLVED,
        QUESTION_REOPENED,
        ANSWER_ADDED,
        ANSWER_MODIFIED,
        ANSWER_DELETED,
    }

    /**
     * The type
     */
    private Type type;
    /**
     * The text
     */
    private String text;
    /**
     * The user
     */
    private User user;
    /**
     * The issue key
     */
    private String issueKey;

    /**
     * Constructor
     * @param type the type of the event
     * @param currentUserObject the user
     * @param text the text
     * @param issueKey the issue key
     */
    public QandAEvent(Type type, User currentUserObject, String text, String issueKey) {
        this.type = type;
        this.user = currentUserObject;
        this.text = text;
        this.issueKey = issueKey;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the current user
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the issue key
     */
    public String getIssueKey() {
        return issueKey;
    }
}

/*
 * Created on 6/2/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;

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
     * The secondary text (if it's an answer, this is the question)
     */
    private String preambleText;

    /**
     * The user
     */
    private User user;

    /**
     * Additional users
     */
    private Set<String> additionalUsers;
    /**
     * The issue key
     */
    private Issue issue;

    /**
     * Constructor
     * @param type the type of the event
     * @param currentUserObject the user
     * @param additionalUsers the additional users
     * @param text the text
     * @param issue the issue key
     */
    public QandAEvent(Type type, User currentUserObject, Set<String> additionalUsers,
                      String text, Issue issue) {
        this(type, currentUserObject, additionalUsers, null, text, issue);
    }

    /**
     * Constructor
     * @param type the type of the event
     * @param currentUserObject the user
     * @param additionalUsers the additional users
     * @param preambleText the preamble text (question, if != null)
     * @param text the text
     * @param issue the issue key
     */
    public QandAEvent(Type type, User currentUserObject, Set<String> additionalUsers,
                      String preambleText, String text, Issue issue) {
        this.type = type;
        this.user = currentUserObject;
        this.additionalUsers = additionalUsers;
        this.preambleText = preambleText;
        this.text = text;
        this.issue = issue;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the preamble text
     */
    public String getPreambleText() {
        return preambleText;
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
     * @return the additional users
     */
    public Set<String> getAdditionalUsers() {
        return additionalUsers;
    }

    /**
     * @return the issue key
     */
    public String getIssueKey() {
        return issue.getKey();
    }

    /**
     * @return the issue key
     */
    public Issue getIssue() {
        return issue;
    }
}

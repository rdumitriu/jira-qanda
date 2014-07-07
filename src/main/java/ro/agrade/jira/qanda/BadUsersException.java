/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/14/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

/**
 * The bad user exception
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class BadUsersException extends Exception {
    private List<String> wrongUsers;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public BadUsersException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public BadUsersException(String message, List<String> wrongUsers) {
        super(message);
        this.wrongUsers = wrongUsers;
    }

    /**
     * @return the list of inexistent users
     */
    public List<String> getWrongUsers() {
        return wrongUsers;
    }
}

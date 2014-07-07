/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/14/13
 */
package ro.agrade.jira.qanda;

/**
 * Exception thrown when the expert group is duplicate
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class DuplicateExpertGroupException extends Exception {
    private String name;

    public DuplicateExpertGroupException(String msg, String name) {
        super(msg);
    }

    public String getDuplicateName() {
        return name;
    }
}

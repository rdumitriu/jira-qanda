/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda.utils;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * This is a base dao service. Usually, no such service like
 * auth service should be injected, but we're doing all our operations
 * on current user and makes no sense to load an extra param in the
 * routines.
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class BaseUserAwareService {
    private final JiraAuthenticationContext authContext;

    /**
     * Constructor
     * @param authContext this is to be injected
     */
    public BaseUserAwareService(JiraAuthenticationContext authContext) {
        this.authContext = authContext;
    }

    /**
     * Gets the auth context, if you need it
     * @return the auth context
     */
    public JiraAuthenticationContext getAuthContext() {
        return authContext;
    }

    /**
     * Gets the current user
     *
     * @return the current user
     */
    public String getCurrentUser() {
        return authContext.getLoggedInUser().getName();
    }

    /**
     * Gets the current user
     *
     * @return the current user
     */
    public User getCurrentUserObject() {
        return authContext.getLoggedInUser();
    }
}
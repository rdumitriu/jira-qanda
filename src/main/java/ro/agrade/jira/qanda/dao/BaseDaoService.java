/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda.dao;

import com.atlassian.jira.security.JiraAuthenticationContext;

/**
 * ::TODO:: documentation
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class BaseDaoService {
    private final JiraAuthenticationContext authContext;

    public BaseDaoService(JiraAuthenticationContext authContext) {
        this.authContext = authContext;
    }

    public JiraAuthenticationContext getAuthContext() {
        return authContext;
    }

    public String getActingUser() {
        return authContext.getLoggedInUser().getName();
    }
}

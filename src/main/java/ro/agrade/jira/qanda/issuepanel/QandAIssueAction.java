/*
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda.issuepanel;

import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.security.*;
import com.atlassian.jira.user.util.UserManager;
import ro.agrade.jira.qanda.Question;


/**
 * The Q & A action. Actually produces the list of questions to be
 * presented on the screen.
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class QandAIssueAction extends AbstractIssueAction {
    private final JiraAuthenticationContext authContext;
    private final ApplicationProperties properties;
    private final UserManager userManager;
    private final Issue issue;
    private final RendererManager rendererMgr;
    private final PermissionManager permissionManager;
    private final Question question;

    public QandAIssueAction(final IssueTabPanelModuleDescriptor descriptor,
                            final ApplicationProperties properties,
                            final Issue issue,
                            final JiraAuthenticationContext authContext,
                            final UserManager userManager,
                            final RendererManager rendererMgr,
                            final PermissionManager permissionManager,
                            final Question question) {
        super(descriptor);
        this.properties = properties;
        this.authContext = authContext;
        this.userManager = userManager;
        this.issue = issue;
        this.rendererMgr = rendererMgr;
        this.permissionManager = permissionManager;
        this.question = question;
    }

    @Override
    public Date getTimePerformed() {
        return new Date();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void populateVelocityParams(Map map) {
        User currentUser = authContext.getLoggedInUser();
        map.put("permitAdd", true); //license triggered.
        map.put("overrideActions", userCanOverrideActions(currentUser, issue));
        map.put("currentUser", currentUser);
        map.put("baseJIRAURL", properties.getString("jira.baseurl"));
        map.put("issue", issue);
        map.put("uiFormatter", new UIFormatter(userManager, authContext, properties, rendererMgr, issue));
        map.put("question", question);
    }

    private boolean userCanOverrideActions(User currentUser, Issue issue) {
        if(permissionManager.hasPermission(Permissions.ADMINISTER, currentUser) ||
           permissionManager.hasPermission(Permissions.PROJECT_ADMIN, issue.getProjectObject(), currentUser) ||
           permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)) {
            return true;
        }
        if(issue.getProjectObject().getLead() != null &&
                issue.getProjectObject().getLead().getName().equals(currentUser.getName())) {
            return true;
        }
        if(issue.getComponentObjects() != null) {
            for(ProjectComponent cmpt : issue.getComponentObjects()) {
                if(cmpt.getLead() != null && cmpt.getLead().equals(currentUser.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}

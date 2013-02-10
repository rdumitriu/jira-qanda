package ro.agrade.jira.qanda.issuepanel;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import ro.agrade.jira.qanda.QandAService;
import ro.agrade.jira.qanda.Question;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class QandAIssuePanel extends AbstractIssueTabPanel {
    private final WebResourceManager webResourceManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authContext;
    private final ApplicationProperties properties;
    private final UserManager userManager;
    private final QandAService service;
    private final RendererManager rendererMgr;

    public QandAIssuePanel(final WebResourceManager webResourceManager,
                           final ApplicationProperties properties,
                           final UserManager userManager,
                           final PermissionManager permissionManager,
                           final JiraAuthenticationContext authContext,
                           final QandAService service,
                           final RendererManager rendererMgr) {
        this.webResourceManager = webResourceManager;
        this.properties = properties;
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.authContext = authContext;
        this.service = service;
        this.rendererMgr = rendererMgr;
    }

    private String getText(String key) {
        return descriptor.getI18nBean().getText(key);
    }

    @Override
    public List<IssueAction> getActions(final Issue issue, User user) {
        webResourceManager.requireResource("ro.agrade.jira.qanda:qanda-resources");
        webResourceManager.requireResource("com.atlassian.auiplugin:aui-experimental-lozenge");

        User currentUser = authContext.getLoggedInUser();
        boolean canOverrideActions = userCanOverrideActions(currentUser, issue);
        UIFormatter formatter = new UIFormatter(userManager, authContext, properties, rendererMgr, issue);
        String baseURL = properties.getString("jira.baseurl");

        List<IssueAction> actions = new ArrayList<IssueAction>();
        List<Question> questions = service.loadQuestionsForIssue(issue.getKey());
        actions.add(new QandAIssueAction(descriptor, issue, currentUser, null,
                                         false, canOverrideActions, baseURL, formatter));
        if(questions == null || questions.size() == 0){
        	return actions;
        }
        for(int i = 0; i < questions.size(); i++) {
            boolean canAdd = (i == 0); //explicit
            actions.add(new QandAIssueAction(descriptor, issue, currentUser,  questions.get(i),
                                             canAdd, canOverrideActions, baseURL, formatter));
        }
        return actions;
    }

    @Override
    public boolean showPanel(Issue issue, User user) {
        return permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, user) &&
               permissionManager.hasPermission(Permissions.BROWSE, issue.getProjectObject(), user);
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

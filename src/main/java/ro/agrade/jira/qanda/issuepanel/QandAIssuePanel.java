package ro.agrade.jira.qanda.issuepanel;

import com.atlassian.crowd.embedded.api.User;
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

        List<IssueAction> actions = new ArrayList<IssueAction>();
        List<Question> questions = service.loadQuestionsForIssue(issue.getKey());
        actions.add(new QandAIssueAction(descriptor, properties, issue, authContext, userManager, rendererMgr, permissionManager, null));
        if(questions == null || questions.size() == 0){
        	return actions;
        }
        for(Question q : questions){
        	actions.add(new QandAIssueAction(descriptor, properties, issue, authContext, userManager, rendererMgr, permissionManager, q));
        }
        return actions;
    }

    @Override
    public boolean showPanel(Issue issue, User user) {
        return permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, user);
    }


}

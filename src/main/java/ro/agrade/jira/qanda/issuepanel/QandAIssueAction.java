/*
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda.issuepanel;

import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import ro.agrade.jira.qanda.Question;


/**
 * The Q & A action. Actually produces the list of questions to be
 * presented on the screen.
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class QandAIssueAction extends AbstractIssueAction {
    private final Issue issue;
    private final Question question;
    private final boolean canOverrideActions;
    private final User currentUser;
    private final String jiraBaseUrl;
    private final UIFormatter uiFormatter;

    public QandAIssueAction(final IssueTabPanelModuleDescriptor descriptor,
                            final Issue issue,
                            final User currentUser,
                            final Question question,
                            final boolean canOverrideActions,
                            final String jiraBaseUrl,
                            final UIFormatter uiFormatter) {
        super(descriptor);
        this.issue = issue;
        this.question = question;
        this.currentUser = currentUser;
        this.canOverrideActions = canOverrideActions;
        this.jiraBaseUrl = jiraBaseUrl;
        this.uiFormatter = uiFormatter;
    }

    @Override
    public Date getTimePerformed() {
        return new Date();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void populateVelocityParams(Map map) {
        map.put("overrideActions", canOverrideActions);
        map.put("currentUser", currentUser);
        map.put("baseJIRAURL", jiraBaseUrl);
        map.put("issue", issue);
        map.put("uiFormatter", uiFormatter);
        map.put("question", question);
    }
}

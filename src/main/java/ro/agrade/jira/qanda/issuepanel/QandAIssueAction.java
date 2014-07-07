/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda.issuepanel;

import java.util.*;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;

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
    private final boolean canAddToIssueDescription;
    private final ApplicationUser currentUser;
    private final String jiraBaseUrl;
    private final UIFormatter uiFormatter;
    private final QandAStatistics stats;

    public QandAIssueAction(final IssueTabPanelModuleDescriptor descriptor,
                            final Issue issue,
                            final ApplicationUser currentUser,
                            final Question question,
                            final boolean canOverrideActions,
                            final boolean canAddToIssueDescription,
                            final String jiraBaseUrl,
                            final QandAStatistics stats,
                            final UIFormatter uiFormatter) {
        super(descriptor);
        this.issue = issue;
        this.question = question;
        this.currentUser = currentUser;
        this.canOverrideActions = canOverrideActions;
        this.canAddToIssueDescription = canAddToIssueDescription;
        this.jiraBaseUrl = jiraBaseUrl;
        this.uiFormatter = uiFormatter;
        this.stats = stats;
    }

    @Override
    public Date getTimePerformed() {
        return new Date();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void populateVelocityParams(Map map) {
        map.put("overrideActions", canOverrideActions);
        map.put("addToIssueDescription", canAddToIssueDescription);
        map.put("currentUser", currentUser.getDirectoryUser());
        map.put("baseJIRAURL", jiraBaseUrl);
        map.put("issue", issue);
        map.put("uiFormatter", uiFormatter);
        map.put("question", question);
        map.put("stats", stats);
    }
}

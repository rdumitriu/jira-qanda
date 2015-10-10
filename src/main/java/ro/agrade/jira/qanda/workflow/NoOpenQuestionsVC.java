/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 7/22/13
 */
package ro.agrade.jira.qanda.workflow;

import java.util.*;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.user.ApplicationUser;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.*;

import ro.agrade.jira.qanda.QandAService;
import ro.agrade.jira.qanda.Question;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validator and condition code
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class NoOpenQuestionsVC {

    private static final Log LOG = LogFactory.getLog(NoOpenQuestionsVC.class);
    private QandAService service;

    /**
     * Constructor
     */
    public NoOpenQuestionsVC(QandAService service) {
        this.service = service;
    }

    /**
     * Condition routine
     *
     * @param transientVars the transient Vars
     * @param args the arguments
     * @param ps the property set
     * @return true if condition is true, false otherwise
     */
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        return !checkIssueForOpenQuestions(issue);
    }

    private boolean checkIssueForOpenQuestions(MutableIssue issue) {
        List<Question> questions = service.loadQuestionsForIssue(issue.getKey());
        if(questions != null) {
            for(Question q : questions) {
                if(!q.isClosed()) { return true; }
            }
        }
        return false;
    }

    /**
     * Validation routine
     * @param transientVars the transient Vars
     * @param args the arguments
     * @param ps the property set
     * @throws InvalidInputException if validation fails
     */
    public void validate(Map transientVars, Map args, PropertySet ps)
            throws InvalidInputException {

        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        boolean hasOpenQuestions = checkIssueForOpenQuestions(issue);

        if(hasOpenQuestions) {
            InvalidInputException ex = new InvalidInputException();
            ApplicationUser currentUser = ComponentAccessor.getJiraAuthenticationContext().getUser();
            String text = currentUser != null
                        ? ComponentAccessor.getI18nHelperFactory().getInstance(currentUser).getText("qanda.validator.message.openq")
                        : ComponentAccessor.getI18nHelperFactory().getInstance(Locale.getDefault()).getText("qanda.validator.message.openq");
            ex.addError(text);
            throw ex;
        }
    }
}

/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.search.*;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.*;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import ro.agrade.jira.qanda.dao.AnswerDataService;
import ro.agrade.jira.qanda.dao.QuestionDataService;
import ro.agrade.jira.qanda.utils.BaseUserAwareService;
import ro.agrade.jira.qanda.utils.PermissionChecker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Our implementation for the service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class QandAServiceImpl extends BaseUserAwareService implements QandAService {
    private static final Log LOG = LogFactory.getLog(QandAServiceImpl.class);
    private final QuestionDataService qImpl;
    private final AnswerDataService aImpl;
    private final IssueManager issueManager;
    private final SearchProvider searchProvider;
    private final PermissionManager permissionManager;

    public QandAServiceImpl(final IssueManager issueManager,
                            final JiraAuthenticationContext authContext,
                            final SearchProvider searchProvider,
                            final PermissionManager permissionManager,
                            final QuestionDataService qImpl,
                            final AnswerDataService aImpl) {
        super(authContext);
        this.qImpl = qImpl;
        this.aImpl = aImpl;
        this.issueManager = issueManager;
        this.searchProvider = searchProvider;
        this.permissionManager = permissionManager;
    }

    /**
     * Get all the questions, along with the answers for a issue key
     *
     * @param key the key
     * @return the list of questions
     */
    @Override
    public List<Question> loadQuestionsForIssue(String key) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Loading questions for issue %s", key));
        }
        Issue issue = issueManager.getIssueObject(key);
        List<Question> questionList = qImpl.getQuestionsForIssue(issue.getId());
        List<Answer> answers = aImpl.getAnswersForIssue(issue.getId());
        questionList = compileQuestionsWithAnswers(questionList, answers);
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Done loading questions for issue %s", key));
        }
        Collections.sort(questionList, new Comparator<Question>() {
            @Override
            public int compare(Question o1, Question o2) {
                return (int)(o2.getTimeStamp() - o1.getTimeStamp());
            }
        });
        return questionList;
    }

    // Unfortunately, Qfbiz simply doesn't know too much about joins
    private List<Question> compileQuestionsWithAnswers(List<Question> questionList, List<Answer> answers) {
        if(questionList == null) {
            return new ArrayList<Question>();
        }
        if(answers == null) {
            return questionList;
        }
        //manual inner join (using a hash)
        Map<Long, Question> questionsMap = new HashMap<Long, Question>();
        for(Question q : questionList) {
            questionsMap.put(q.getId(), q);
        }
        for(Answer a : answers) {
            Question q = questionsMap.get(a.getQuestionId());
            if(q == null) {
                LOG.warn(String.format("Got answer %d, but could not find the question %d ?!?",
                                       a.getAnswerId(), a.getQuestionId()));
                continue;
            }
            q.getAnswers().add(a);
        }
        return questionList;
    }

    /**
     * Get all the questions which are unresolved for the specified project
     *
     * @param project the project
     * @param timePeriod the time period taken into account
     * @return the project questions
     */
    @Override
    public List<Question>
    getUnsolvedQuestionsForProject(String project, TimePeriod timePeriod) {
        try {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Loading questions for project %s", project));
            }
            JqlQueryBuilder jqlQueryBuilder = JqlQueryBuilder.newBuilder();
            jqlQueryBuilder.where().resolution().isEmpty();
            jqlQueryBuilder.where().and().project().eq(project);
            jqlQueryBuilder.where().and().updatedAfter(timePeriod.getDateFromNow());

            Query query = jqlQueryBuilder.buildQuery();

            User user = getCurrentUserObject();
            DefaultIndexManager.flushThreadLocalSearchers();
            JiraAuthenticationContextImpl.clearRequestCache();

            SearchResults results = searchProvider.search(query, user, PagerFilter.getUnlimitedFilter());

            List<Issue> issues = results.getIssues();
            List<Long> issueIds = new ArrayList<Long>();
            for(Issue iss : issues) {
                issueIds.add(iss.getId());
            }

            List<Question> questionList = qImpl.getUnresolvedQuestionsForIssues(issueIds);
            List<Answer> answerList = aImpl.getAnswersForIssues(issueIds);

            questionList = compileQuestionsWithAnswers(questionList, answerList);
            Collections.sort(questionList, new Comparator<Question>() {
                @Override
                public int compare(Question o1, Question o2) {
                    return (int)(o1.getTimeStamp() - o2.getTimeStamp());
                }
            });
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Done loading questions for project %s", project));
            }
            return questionList;
        } catch (SearchException e) {
            String msg = "Exception while getting questions for project " + project;
            LOG.error(msg, e);
            throw new QandAException(msg, e);
        }
    }

    /**
     * Adds a question
     *
     * @param issueKey the issue key
     * @param question the question text
     */
    @Override
    public void addQuestion(String issueKey, String question) {
        Issue issue = issueManager.getIssueObject(issueKey);
        checkQuestionAddPermission(issue);
        qImpl.addQuestion(issue.getId(), question);
    }

    /**
     * Deletes a question
     *
     * @param qid the question id
     */
    @Override
    public void deleteQuestion(long qid) {
        Question q = qImpl.getQuestion(qid);
        if(q == null) {
            return;
        }
        checkQuestionPermission(q);
        qImpl.removeQuestion(qid);
    }

    /**
     * Add an answer to a question
     *
     * @param qid    the question id
     * @param answer the text
     */
    @Override
    public void addAnswer(long qid, String answer) {
        Question q = qImpl.getQuestion(qid);
        if(q == null) {
            LOG.error("Question may be already deleted");
            return;
        }
        Issue issue = issueManager.getIssueObject(q.getIssueId());
        checkAnswerAddPermission(issue);
        aImpl.addAnswer(qid, issue.getId(), answer);
    }

    /**
     * Deletes an answer
     *
     * @param aid the answer id
     */
    @Override
    public void deleteAnswer(long aid) {
        Answer a = aImpl.getAnswer(aid);
        checkAnswerPermission(a);
        aImpl.removeAnswer(aid);
    }

    /**
     * Sets the answer approval for a answer
     *
     * @param aid the answer id
     * @param flg the flag
     */
    @Override
    public void setAnswerApprovalFlag(long aid, boolean flg) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Setting flag '%b' for answer id %d. Maybe deleted ?", flg, aid));
        }
        Answer a = aImpl.setAnswerAcceptedFlag(aid, flg);
        if(a == null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("There is no answer with id %d. Maybe deleted ?", aid));
            }
            return;
        }
        checkAnswerPermission(a);
        Question q = qImpl.getQuestion(a.getQuestionId());
        if(q == null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Question %d is null. Maybe already deleted ?", a.getQuestionId()));
            }
            //already deleted
            return;
        }
        if(!q.isClosed() && flg) {
            //if question is not closed already, close it.
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Setting question %d status to: CLOSED", q.getId()));
            }
            qImpl.setQuestionFlag(q.getId(), QuestionStatus.CLOSED);
        } else if(q.isClosed() && !flg && !isAnotherAnswerApprovedBut(q, a)) {
            // we need to verify the answers which are approved.
            //if question is not open already, close it.
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Setting back question %d status to: OPEN", q.getId()));
            }
            qImpl.setQuestionFlag(q.getId(), QuestionStatus.OPEN);
        }
    }

    private boolean isAnotherAnswerApprovedBut(Question q, Answer a) {
        List<Answer> list = aImpl.getAnswersForQuestion(q.getId());
        if(list == null || list.size() == 0) {
            return false;
        }
        for(Answer x : list) {
            if(x.getAnswerId() == a.getAnswerId()) { continue; }
            if(x.isAccepted()) {
                return true;
            }
        }
        return false;
    }

    private void checkQuestionAddPermission(Issue issue) {
        //does nothing at this point
    }

    private void checkQuestionPermission(Question q) {
        Issue issue = issueManager.getIssueObject(q.getIssueId());
        if(!PermissionChecker.isUserOwner(permissionManager, issue,  getCurrentUserObject(), q.getUser())) {
            String msg = String.format("Permission violation while accessing question %d", q.getId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }

    private void checkAnswerAddPermission(Issue issue) {
        //does nothing at this point
    }

    private void checkAnswerPermission(Answer a) {
        Issue issue = issueManager.getIssueObject(a.getIssueId());
        if(!PermissionChecker.isUserOwner(permissionManager, issue,  getCurrentUserObject(), a.getUser())) {
            String msg = String.format("Permission violation while accessing answer %d", a.getAnswerId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }
}

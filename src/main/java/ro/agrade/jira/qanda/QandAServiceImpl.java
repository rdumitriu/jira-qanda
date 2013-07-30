/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.index.DefaultIndexManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.search.*;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.*;
import com.atlassian.jira.util.BuildUtilsInfoImpl;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import ro.agrade.jira.qanda.dao.AnswerDataService;
import ro.agrade.jira.qanda.dao.QuestionDataService;
import ro.agrade.jira.qanda.plugin.PluginStorage;
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
    private final IssueIndexManager issueIndexManager;
    private final SearchProvider searchProvider;
    private final PermissionManager permissionManager;

    public QandAServiceImpl(final IssueManager issueManager,
                            final IssueIndexManager issueIndexManager,
                            final JiraAuthenticationContext authContext,
                            final SearchProvider searchProvider,
                            final PermissionManager permissionManager,
                            final QuestionDataService qImpl,
                            final AnswerDataService aImpl) {
        super(authContext);
        this.qImpl = qImpl;
        this.aImpl = aImpl;
        this.issueManager = issueManager;
        this.issueIndexManager = issueIndexManager;
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
                if(LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Got answer %d, but could not find the question %d ?!?",
                                            a.getAnswerId(), a.getQuestionId()));
                }
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
     * Loads a question, along with the answers
     *
     * @param qid the key
     * @return the loaded question, null if it was deleted.
     */
    @Override
    public Question loadQuestion(long qid) {
        Question ret = qImpl.getQuestion(qid);
        if(ret == null) {
            return null;
        }
        List<Answer> answers = aImpl.getAnswersForQuestion(qid);
        ret.setAnswers(answers != null ? answers : new ArrayList<Answer>());
        return ret;
    }

    /**
     * Gets only the text, useful for editing the question
     *
     * @param qid the question id
     * @return the text of the question, null if the question was already deleted
     */
    @Override
    public String getQuestionText(long qid) {
        Question q = qImpl.getQuestion(qid);
        return q != null ? q.getQuestionText() : null;
    }

    /**
     * Be given a question, this routine adds it into the description of the issue
     *
     * @param qid the question id
     */
    @Override
    public void addQuestionToIssue(long qid) {
        Question q = loadQuestion(qid);
        if(q == null) {
            //just return
            return;
        }
        MutableIssue issue = issueManager.getIssueObject(q.getIssueId());
        checkEditIssuePermission(q, issue);
        //format text for the question and throw it back into the issue
        String textToAdd = calculateText(q);
        updateIssueDescription(issue, textToAdd);
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

        notifyQAInternal(new QandAEvent(QandAEvent.Type.QUESTION_ADDED,
                                        getCurrentUserObject(),
                                        null,
                                        question, issue));
    }

    /**
     * Edits a question
     *
     * @param qid      the question id
     * @param question the question text
     */
    @Override
    public void editQuestion(long qid, String question) {
        Question q = qImpl.getQuestion(qid);
        if(q == null) {
            return;
        }
        Issue issue = issueManager.getIssueObject(q.getIssueId());
        checkQuestionPermission(q, issue);
        qImpl.updateQuestion(qid, question);
        notifyQAInternal(new QandAEvent(QandAEvent.Type.QUESTION_MODIFIED,
                                        getCurrentUserObject(),
                                        buildUserSet(q, null), question, issue));
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
        Issue issue = issueManager.getIssueObject(q.getIssueId());
        checkQuestionPermission(q, issue);
        qImpl.removeQuestion(qid);
        notifyQAInternal(new QandAEvent(QandAEvent.Type.QUESTION_DELETED,
                                        getCurrentUserObject(),
                                        buildUserSet(q, null),
                                        q.getQuestionText(), issue));
    }

    /**
     * Gets only the text, useful for editing the question
     *
     * @param aid the answer id
     * @return the text of the answer, null if the answer was already deleted
     */
    @Override
    public String getAnswerText(long aid) {
        Answer a = aImpl.getAnswer(aid);
        return a != null ? a.getAnswerText() : null;
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
            LOG.error(String.format("Question %d may be already deleted", qid));
            return;
        }
        Issue issue = issueManager.getIssueObject(q.getIssueId());
        checkAnswerAddPermission(issue);
        aImpl.addAnswer(qid, issue.getId(), answer);
        notifyQAInternal(new QandAEvent(QandAEvent.Type.ANSWER_ADDED,
                                        getCurrentUserObject(),
                                        buildUserSet(q, null),
                                        q.getQuestionText(), answer, issue));
    }

    /**
     * Edits an answer
     *
     * @param aid    the answer id
     * @param answer the answer text
     */
    @Override
    public void editAnswer(long aid, String answer) {
        Answer a = aImpl.getAnswer(aid);
        if(a == null) {
            LOG.error(String.format("Answer %d may be already deleted", aid));
            return;
        }
        Question q = qImpl.getQuestion(a.getQuestionId());
        if(q == null) {
            LOG.error(String.format("Question %d may be already deleted", a.getQuestionId()));
            return;
        }
        Issue issue = issueManager.getIssueObject(a.getIssueId());
        checkAnswerPermission(a, issue);

        aImpl.updateAnswer(aid, answer);
        notifyQAInternal(new QandAEvent(QandAEvent.Type.ANSWER_MODIFIED,
                                        getCurrentUserObject(),
                                        buildUserSet(q, a),
                                        q.getQuestionText(), answer, issue));
    }

    /**
     * Deletes an answer
     *
     * @param aid the answer id
     */
    @Override
    public void deleteAnswer(long aid) {
        Answer a = aImpl.getAnswer(aid);
        if(a == null) {
            return;
        }
        Question q = qImpl.getQuestion(a.getQuestionId());
        if(q == null) {
            LOG.error(String.format("Question %d may be already deleted", a.getQuestionId()));
            return;
        }
        Issue issue = issueManager.getIssueObject(a.getIssueId());
        checkAnswerPermission(a, issue);

        aImpl.removeAnswer(aid);
        notifyQAInternal(new QandAEvent(QandAEvent.Type.ANSWER_DELETED,
                                        getCurrentUserObject(),
                                        buildUserSet(q, a),
                                        q.getQuestionText(), a.getAnswerText(), issue));
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
            LOG.debug(String.format("Setting flag '%b' for answer id %d.", flg, aid));
        }
        Answer a = aImpl.setAnswerAcceptedFlag(aid, flg);
        if(a == null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("There is no answer with id %d. Maybe already deleted ?", aid));
            }
            return;
        }
        Question q = qImpl.getQuestion(a.getQuestionId());
        if(q == null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Question %d is null. Maybe already deleted ?", a.getQuestionId()));
            }
            //already deleted
            return;
        }
        Issue issue = issueManager.getIssueObject(q.getIssueId());
        checkToggleAnswerPermission(q, issue);

        if(!q.isClosed() && flg) {
            //if question is not closed already, close it.
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Setting question %d status to: CLOSED", q.getId()));
            }
            qImpl.setQuestionFlag(q.getId(), QuestionStatus.CLOSED);
            notifyQAInternal(new QandAEvent(QandAEvent.Type.QUESTION_SOLVED,
                                            getCurrentUserObject(),
                                            buildUserSet(q, a),
                                            q.getQuestionText(), issue));
        } else if(q.isClosed() && !flg && !isAnotherAnswerApprovedBut(q, a)) {
            // we need to verify the answers which are approved.
            //if question is not open already, close it.
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Setting back question %d status to: OPEN", q.getId()));
            }
            qImpl.setQuestionFlag(q.getId(), QuestionStatus.OPEN);
            notifyQAInternal(new QandAEvent(QandAEvent.Type.QUESTION_REOPENED,
                                            getCurrentUserObject(),
                                            buildUserSet(q, a),
                                            q.getQuestionText(), issue));
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

    private void checkEditIssuePermission(Question q, MutableIssue issue) {
        if(!q.isClosed()) {
            String msg = String.format("Cannot add question id %d (still open)", q.getId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
        if(!PermissionChecker.isUserOwner(permissionManager, issue,  getCurrentUserObject(), q.getUser())) {
            String msg = String.format("Permission violation while accessing question %d", q.getId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
        if(!PermissionChecker.isIssueEditable(permissionManager, issue, getCurrentUserObject())) {
            String msg = String.format("Permission violation while accessing question %d", q.getId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }

    private void checkQuestionAddPermission(Issue issue) {
        if(!PermissionChecker.canViewIssue(permissionManager, issue, getCurrentUserObject())) {
            String msg = "Permission violation while adding question for issue:" + issue.getKey();
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }

    private void checkQuestionPermission(Question q, Issue issue) {
        if(q.isClosed()) {
            String msg = String.format("Cannot modify question id %d (already closed)", q.getId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
        if(!PermissionChecker.isUserOwner(permissionManager, issue,  getCurrentUserObject(), q.getUser())) {
            String msg = String.format("Permission violation while accessing question %d", q.getId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }

    private void checkAnswerAddPermission(Issue issue) {
        if(!PermissionChecker.canViewIssue(permissionManager, issue, getCurrentUserObject())) {
            String msg = "Permission violation while adding answer for issue:" + issue.getKey();
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }

    private void checkAnswerPermission(Answer a, Issue issue) {
        if(a.isAccepted()) {
            String msg = String.format("Cannot modify answer id %d (already accepted)", a.getAnswerId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
        if(!PermissionChecker.isUserOwner(permissionManager, issue,  getCurrentUserObject(), a.getUser())) {
            String msg = String.format("Permission violation while accessing answer %d", a.getAnswerId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }

    private void checkToggleAnswerPermission(Question q, Issue issue) {
        if(!PermissionChecker.isUserOwner(permissionManager, issue,  getCurrentUserObject(), q.getUser())) {
            String msg = String.format("Permission violation while toggle answer for question %d", q.getId());
            LOG.error(msg);
            throw new QandAPermissionException(msg);
        }
    }


    private void updateIssueDescription(MutableIssue issue, String description) {
        String origText = issue.getDescription();
        issue.setDescription(origText + description);

        boolean wasIndexing =  ImportUtils.isIndexIssues();
        ImportUtils.setIndexIssues(true);
        try {
            // updateIssue (should) also handle the re-index of the issue
            issueManager.updateIssue(getCurrentUserObject(), issue,
                    EventDispatchOption.ISSUE_UPDATED,
                    true);
            try {
                String version = new BuildUtilsInfoImpl().getVersion();
                issueIndexManager.release();
                issueIndexManager.reIndex(issue);
                issueIndexManager.release();
            } catch(Exception ex) {
                LOG.warn("Could not reindex ?!?.", ex);
                throw ex;
            }
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Updated issue >>%s<<", issue.getKey()));
            }
        } catch(Exception ex) {
            String msg = String.format("Update failed for issue >>%s<<, error was %s",
                                       issue.getKey(), ex);
            LOG.error(msg);
            throw new QandAException(msg, ex);
        } finally {
            ImportUtils.setIndexIssues(wasIndexing);
        }
    }

    private String calculateText(Question q) {
        //::TODO:: what if the answer / question already contain a panel
        //well, for now, this is a known bug :)
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n{panel:title=Q&A|borderStyle=dashed|borderColor=#999|titleColor=#FFFFFF|titleBGColor=#326ca6|bgColor=#FFFFFF}\n\n(?) ")
          .append(q.getQuestionText())
          .append("\n");
        for(Answer a : q.getAnswers()) {
            if(a.isAccepted()) {
                sb.append("(on) ").append(a.getAnswerText()).append("\n");
            }
        }
        sb.append("{panel}\n");
        return sb.toString();
    }

    private Set<String> buildUserSet(Question q, Answer a) {
        Set<String> users = new HashSet<String>();
        if(q != null) {
            users.add(q.getUser());
        }
        if(a != null) {
            users.add(a.getUser());
        }
        return users;
    }

    private void notifyQAInternal(QandAEvent qaEvent) {
        for(QandAListener l : PluginStorage.getInstance().getConfiguredListeners()) {
            l.onEvent(qaEvent);
        }
    }
}

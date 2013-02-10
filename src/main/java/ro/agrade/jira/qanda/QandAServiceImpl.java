/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;

import ro.agrade.jira.qanda.dao.AnswerDataService;
import ro.agrade.jira.qanda.dao.QuestionDataService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Our implementation for the service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class QandAServiceImpl implements QandAService {
    private static final Log LOG = LogFactory.getLog(QandAServiceImpl.class);
    private final QuestionDataService qImpl;
    private final AnswerDataService aImpl;
    private final IssueManager issueManager;

    public QandAServiceImpl(final IssueManager issueManager,
                            final QuestionDataService qImpl,
                            final AnswerDataService aImpl) {
        this.qImpl = qImpl;
        this.aImpl = aImpl;
        this.issueManager = issueManager;
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
        questionList = compileQuestionsWithAnswers(issue, questionList);
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
    private List<Question> compileQuestionsWithAnswers(Issue issue, List<Question> questionList) {
        if(questionList == null) {
            return new ArrayList<Question>();
        }
        //manual hash join
        Map<Long, Question> questionsMap = new HashMap<Long, Question>();
        for(Question q : questionList) {
            questionsMap.put(q.getId(), q);
        }
        List<Answer> answers = aImpl.getAnswersForIssue(issue.getId());
        if(answers != null) {
            for(Answer a : answers) {
                Question q = questionsMap.get(a.getQuestionId());
                if(q == null) {
                    LOG.warn(String.format("Got answer %d, but could not find the question %d ?!?",
                                           a.getAnswerId(), a.getQuestionId()));
                    continue;
                }
                q.getAnswers().add(a);
            }
        }
        return questionList;
    }

//    //::TODO:: PANEL
//    /**
//     * Get all the questions which are unresolved for the specified project
//     *
//     * @param project the project
//     * @return the project questions
//     */
//    @Override
//    public List<Question> getUnsolvedQuestionsForProject(String project) {
//        if(LOG.isDebugEnabled()) {
//            LOG.debug(String.format("Loading questions for project %s", project));
//        }
//        List<Question> questionList = qImpl.getUnresolvedQuestionsForProject(project);
//        questionList = compileQuestionsWithAnswers(questionList);
//        Collections.sort(questionList, new Comparator<Question>() {
//            @Override
//            public int compare(Question o1, Question o2) {
//                return (int)(o1.getTimeStamp() - o2.getTimeStamp());
//            }
//        });
//        if(LOG.isDebugEnabled()) {
//            LOG.debug(String.format("Done loading questions for project %s", project));
//        }
//        return questionList;
//    }

    /**
     * Adds a question
     *
     * @param issueKey the issue key
     * @param question the question text
     */
    @Override
    public void addQuestion(String issueKey, String question) {
        Issue issue = issueManager.getIssueObject(issueKey);
        qImpl.addQuestion(issue.getId(), question);
    }

    /**
     * Deletes a question
     *
     * @param qid the question id
     */
    @Override
    public void deleteQuestion(long qid) {
        qImpl.removeQuestion(qid);
    }

    /**
     * Add an answer to a question
     *
     * @param qid    the question id
     * @param answer the text
     */
    @Override
    public void addAnswer(long qid, String issueKey, String answer) {
        Issue issue = issueManager.getIssueObject(issueKey);
        aImpl.addAnswer(qid, issue.getId(), answer);
    }

    /**
     * Deletes an answer
     *
     * @param aid the answer id
     */
    @Override
    public void deleteAnswer(long aid) {
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
}

/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

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
    private QuestionDataService qImpl;
    private AnswerDataService aImpl;

    public QandAServiceImpl(QuestionDataService qImpl, AnswerDataService aImpl) {
        this.qImpl = qImpl;
        this.aImpl = aImpl;
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
        List<Question> questionList = qImpl.getQuestionsForIssue(key);
        questionList = compileQuestionsWithAnswers(questionList);
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Done loading questions for issue %s", key));
        }
        return questionList;
    }

    private List<Question> compileQuestionsWithAnswers(List<Question> questionList) {
        if(questionList != null) {
            for(Question q : questionList) {
                List<Answer> answers = aImpl.getAnswersForQuestion(q.getId());
                if(answers != null) {
                    Collections.sort(answers, new Comparator<Answer>() {
                        @Override
                        public int compare(Answer o1, Answer o2) {
                            return (int)(o1.getAnswerId() - o2.getAnswerId());
                        }
                    });
                }
                q.setAnswers(answers);
            }
        } else {
            questionList = new ArrayList<Question>();
        }
        return questionList;
    }

    /**
     * Get all the questions which are unresolved for the specified project
     *
     * @param project the project
     * @return the project questions
     */
    @Override
    public List<Question> getUnsolvedQuestionsForProject(String project) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Loading questions for project %s", project));
        }
        List<Question> questionList = qImpl.getUnresolvedQuestionsForProject(project);
        questionList = compileQuestionsWithAnswers(questionList);
        if(LOG.isDebugEnabled()) {
            LOG.debug(String.format("Done loading questions for project %s", project));
        }
        return questionList;
    }

    /**
     * Adds a question
     *
     * @param issueKey the issue key
     * @param question the question text
     */
    @Override
    public void addQuestion(String issueKey, String question) {
        qImpl.addQuestion(issueKey, question);
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
    public void addAnswer(long qid, String answer) {
        aImpl.addAnswer(qid, answer);
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

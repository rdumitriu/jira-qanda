/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda.dao;

import ro.agrade.jira.qanda.Answer;

import java.util.List;

/**
 * The answer data service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public interface AnswerDataService {

    /**
     * Gets one single answer
     * @param aid the answer id
     * @return the answer
     */
    public abstract Answer getAnswer(long aid);

    /**
     * Gets all the answers for the question
     * @param qid the question id
     * @return the list of answers
     */
    public abstract List<Answer> getAnswersForQuestion(long qid);

    /**
     * Gets all the answers for the issue
     * @param issueId the issue id
     * @return the list of answers
     */
    public abstract List<Answer> getAnswersForIssue(long issueId);

    /**
     * Gets all the answers that are designated for a list of issues
     *
     * @param issueIds the issue ids
     * @return the list of answers
     */
    public abstract List<Answer> getAnswersForIssues(List<Long> issueIds);

    /**
     * Add an answer
     * @param qid the question id
     * @param issueId the issue id
     * @param answer the answer text
     */
    public abstract void addAnswer(long qid, long issueId, String answer);

    /**
     * Removes an answer
     * @param aid the answer id
     * @param answerText the answer text
     */
    public abstract void updateAnswer(long aid, String answerText);

    /**
     * Removes an answer
     * @param aid the answer id
     */
    public abstract void removeAnswer(long aid);

    /**
     * Updates the flag on the answer
     *
     * @param aid the answer
     * @param flg the flag
     * @return the answer, modified
     */
    public abstract Answer setAnswerAcceptedFlag(long aid, boolean flg);
}

/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda.dao;

import ro.agrade.jira.qanda.Question;
import ro.agrade.jira.qanda.QuestionStatus;

import java.util.List;

/**
 * The question data service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public interface QuestionDataService {

    /**
     * Gets a specific question by id
     * @param questionId the id
     * @return the question if not deleted, null otherwise
     */
    public abstract Question getQuestion(long questionId);

    /**
     * Gets all undeleted questions
     * @param key the issue key
     * @return the list of questions
     */
    public abstract List<Question> getQuestionsForIssue(String key);

    /**
     * Gets all unresolved questions
     *
     * @param project the project
     * @return the questions for the project which are not resolved, if any
     */
    public abstract List<Question> getUnresolvedQuestionsForProject(String project);

    /**
     * Add a question
     * @param issueKey the key
     * @param question the text
     */
    public abstract void addQuestion(String issueKey, String question);

    /**
     * Deletes a question
     * @param qid the question id
     */
    public abstract void removeQuestion(long qid);

    /**
     * Sets a question flag
     * @param id the id
     * @param sts the status
     */
    public abstract void setQuestionFlag(long id, QuestionStatus sts);
}

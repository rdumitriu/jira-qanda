/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
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
     * @param issueId the issue id
     * @return the list of questions
     */
    public abstract List<Question> getQuestionsForIssue(long issueId);

    /**
     * Gets all unresolved questions
     *
     * @param issueIds the project
     * @param user the owner of the questions, can be null
     * @return the questions for the project which are not resolved, if any
     */
    public abstract List<Question> getUnresolvedQuestionsForIssues(List<Long> issueIds, String user);

    /**
     * Add a question
     * @param issueId the id of the issue
     * @param question the text
     */
    public abstract void addQuestion(long issueId, String question);

    /**
     * Edit a question
     * @param qid the id of the question
     * @param questionText the text
     */
    public abstract void updateQuestion(long qid, String questionText);

    /**
     * Deletes a question
     * @param qid the question id
     */
    public abstract void removeQuestion(long qid);

    /**
     * Sets a question flag
     * @param id the id of the question
     * @param sts the status
     */
    public abstract void setQuestionFlag(long id, QuestionStatus sts);


}

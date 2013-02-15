/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

/**
 * The services we expose
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public interface QandAService {

    /**
     * The time period used into this plugin
     */
    public enum TimePeriod {
        /** 6 months */
        SIX_MONTHS ("6mo", 6),
        /** 3 months */
        THREE_MONTHS ("3mo", 3),
        /** 1 month */
        ONE_MONTH ("1mo", 1)
        ;
        private String label;
        private int months;

        TimePeriod(String label, int months) {
            this.label = label;
            this.months = months;
        }

        /**
         * Gets the label for this enum
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Calculates the date from now
         * @return the past date, according to the number of months
         */
        public Date getDateFromNow() {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -months);
            return c.getTime();
        }
    }

    /**
     * Loads a question, along with the answers
     * @param qid the key
     * @return the loaded question, null if it was deleted.
     */
    public abstract Question loadQuestion(long qid);

    /**
     * Gets only the text, useful for editing the question
     * @param qid the question id
     * @return the text of the question, null if the question was already deleted
     */
    public abstract String getQuestionText(long qid);

    /**
     * Get all the questions, along with the answers for a issue key
     * @param key the key
     * @return the list of questions
     */
    public abstract List<Question> loadQuestionsForIssue(String key);

    /**
     * Get all the questions which are unresolved for the specified project
     *
     * @param project the project
     * @param timePeriod the time period taken into account
     * @return the project questions
     */
    public abstract List<Question> getUnsolvedQuestionsForProject(String project, TimePeriod timePeriod);

    /**
     * Be given a question, this routine adds it into the description of the issue
     * @param qid the question id
     */
    public abstract void addQuestionToIssue(long qid);

    /**
     * Adds a question
     * @param issueKey the issue key
     * @param question the question text
     */
    public abstract void addQuestion(String issueKey, String question);

    /**
     * Edits a question
     * @param qid the question id
     * @param question the question text
     */
    public abstract void editQuestion(long qid, String question);

    /**
     * Deletes a question
     * @param qid the question id
     */
    public abstract void deleteQuestion(long qid);

    /**
     * Gets only the text, useful for editing the question
     * @param aid the answer id
     * @return the text of the answer, null if the answer was already deleted
     */
    public abstract String getAnswerText(long aid);

    /**
     * Add an answer to a question
     * @param qid the question id
     * @param answer the text
     */
    public abstract void addAnswer(long qid, String answer);

    /**
     * Edits an answer
     * @param aid the answer id
     * @param answer the answer text
     */
    public abstract void editAnswer(long aid, String answer);

    /**
     * Deletes an answer
     * @param aid the answer id
     */
    public abstract void deleteAnswer(long aid);

    /**
     * Sets the answer approval for a answer
     * @param aid the answer id
     * @param flg the flag
     */
    public abstract void setAnswerApprovalFlag(long aid, boolean flg);
}

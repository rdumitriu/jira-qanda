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
        SIX_MONTHS ("6mo", 6),
        THREE_MONTHS ("3mo", 3),
        ONE_MONTH ("1mo", 1)
        ;
        private String label;
        private int months;

        TimePeriod(String label, int months) {
            this.label = label;
            this.months = months;
        }

        public String getLabel() {
            return label;
        }

        public Date getDateFromNow() {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -months);
            return c.getTime();
        }
    }

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
     * Adds a question
     * @param issueKey the issue key
     * @param question the question text
     */
    public abstract void addQuestion(String issueKey, String question);

    /**
     * Deletes a question
     * @param qid the question id
     */
    public abstract void deleteQuestion(long qid);

    /**
     * Add an answer to a question
     * @param qid the question id
     * @param issueKey the issue key
     * @param answer the text
     */
    void addAnswer(long qid, String issueKey, String answer);

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

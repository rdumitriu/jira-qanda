/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda;

/**
 * The answer
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class Answer {
    private long answerId;
    private long questionId;
    private long issueId;
    private String answerText;
    private String user;
    private long timeStamp;
    private boolean accepted;

    /**
     * The creation constructor, answer is not accepted
     * @param answerId the answer id
     * @param questionId the question id
     * @param issueId the issue id
     * @param answerText the answer itself
     * @param user the user creating this answer
     */
    public Answer(long answerId, long questionId, long issueId,
                  String answerText, String user) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.issueId = issueId;
        this.answerText = answerText;
        this.user = user;
        this.timeStamp = System.currentTimeMillis();
        this.accepted = false;
    }

    /**
     * The most general constructor
     * @param answerId the answer id
     * @param questionId the question id
     * @param issueId the issue id
     * @param answerText the answer itself
     * @param user the user creating this answer
     * @param timeStamp the TS
     * @param accepted if this answer is accepted or not
     */
    public Answer(long answerId, long questionId, long issueId,
                  String answerText, String user,
                  long timeStamp, boolean accepted) {
        this(answerId, questionId, issueId, answerText, user);
        this.timeStamp = timeStamp;
        this.accepted = accepted;
    }

    /**
     * Get answer id
     * @return the answer id
     */
    public long getAnswerId() {
        return answerId;
    }

    /**
     * Gets the question id
     * @return the question id
     */
    public long getQuestionId() {
        return questionId;
    }

    /**
     * This is introduced for the shortcut it provides
     * @return the issue id
     */
    public long getIssueId() {
        return issueId;
    }

    /**
     * The actual text
     * @return the text
     */
    public String getAnswerText() {
        return answerText;
    }

    /**
     * Gets the user
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Is this answer accepted ?
     * @return true if it is
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Gets the TS of the answer
     * @return the ts
     */
    public long getTimeStamp() {
        return timeStamp;
    }
}

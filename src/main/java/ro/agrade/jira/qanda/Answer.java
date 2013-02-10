/*
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda;

//import javax.xml.bind.annotation.*;

/**
 * The answer
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Answer {
    private long answerId;
    private long questionId;
    private String answerText;
    private String user;
    private long timeStamp;
    private boolean accepted;

    public Answer() {
    }

    public Answer(long answerId, long questionId, String answerText, String user) {
        this.answerId = answerId;
        this.questionId = questionId;
        this.answerText = answerText;
        this.user = user;
        this.timeStamp = System.currentTimeMillis();
        this.accepted = false;
    }

    public Answer(long answerId, long questionId, String answerText, String user, long timeStamp, boolean accepted) {
        this(answerId, questionId, answerText, user);
        this.timeStamp = timeStamp;
        this.accepted = accepted;
    }

    public long getAnswerId() {
        return answerId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public String getUser() {
        return user;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}

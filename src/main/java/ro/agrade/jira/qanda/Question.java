/*
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.*;

/**
 * The question.
 *
 * Status can be 'C-closed', 'O-Open'
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Question {
    private long id;
    private String questionText;
    private String issueKey;
    private long timeStamp;
    private String user;
    private QuestionStatus status;
    private List<Answer> answers;

    public Question() {
    }

    public Question(long id, String issueKey, String questionText, String user) {
        this.id = id;
        this.issueKey = issueKey;
        this.questionText = questionText;
        this.user = user;
        this.timeStamp = System.currentTimeMillis();
        this.status = QuestionStatus.OPEN;
        this.answers = new ArrayList<Answer>();
    }

    public Question(long id, String issueKey, String questionText, String user, long timeStamp, QuestionStatus status, List<Answer> answers) {
        this(id, issueKey, questionText, user);
        this.timeStamp = timeStamp;
        this.status = status;
        this.answers = answers;
    }

    public long getId() {
        return id;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getUser() {
        return user;
    }

    public boolean isAnswered() {
        return (answers != null && answers.size() > 0);
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public QuestionStatus getStatus() {
        return status;
    }

    public boolean isClosed() {
        return status == QuestionStatus.CLOSED;
    }

    public List<Answer> getAnswers() {
        return answers != null ? Collections.unmodifiableList(answers) : null;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public void setStatus(QuestionStatus status) {
        this.status = status;
    }
}

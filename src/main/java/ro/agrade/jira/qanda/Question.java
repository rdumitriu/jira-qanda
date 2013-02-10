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
    private long issueId;
    private long timeStamp;
    private String user;
    private QuestionStatus status;
    private List<Answer> answers;

    /**
     * Constructor, default, required by JAXB
     */
    public Question() {
    }

    /**
     * Base fields for the question
     * @param id the id of the question
     * @param issueId the issue id
     * @param questionText the question text
     * @param user the user asking the question
     */
    public Question(long id, long issueId, String questionText, String user) {
        this.id = id;
        this.issueId = issueId;
        this.questionText = questionText;
        this.user = user;
        this.timeStamp = System.currentTimeMillis();
        this.status = QuestionStatus.OPEN;
        this.answers = new ArrayList<Answer>();
    }

    /**
     * Constructor. All fields for the question
     * @param id the id of the question
     * @param issueId the issue id
     * @param questionText the question text
     * @param user the user asking the question
     */
    public Question(long id, long issueId, String questionText,
                    String user, long timeStamp,
                    QuestionStatus status, List<Answer> answers) {
        this(id, issueId, questionText, user);
        this.timeStamp = timeStamp;
        this.status = status;
        this.answers = answers;
    }

    /**
     * Gets the id of the question
     * @return the id of the question
     */
    public long getId() {
        return id;
    }

    /**
     * @return the issue id
     */
    public long getIssueId() {
        return issueId;
    }

    /**
     * Sets the question text
     * @param questionText the question text
     */
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    /**
     * Gets the question text
     * @return the question text
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Gets the user of the project
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets the question's answer
     * @return true if the question has at least an answer
     */
    public boolean isAnswered() {
        return (answers != null && answers.size() > 0);
    }

    /**
     * Gets the creation TS
     * @return the creation TS
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return the status
     */
    public QuestionStatus getStatus() {
        return status;
    }

    /**
     * @return true if this question is closed
     */
    public boolean isClosed() {
        return status == QuestionStatus.CLOSED;
    }

    /**
     * Gets the list of answers
     * @return the list of answers
     */
    public List<Answer> getAnswers() {
        if(answers == null) {
            answers = new ArrayList<Answer>();
        }
        return answers;
    }

    /**
     * Sets the list of answers
     * @param answers the answers
     */
    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    /**
     * Sets the status of the question
     * @param status the new status
     */
    public void setStatus(QuestionStatus status) {
        this.status = status;
    }
}

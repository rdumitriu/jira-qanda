/*
 * Created on 2/14/13
 */
package ro.agrade.jira.qanda.gadget;


import javax.xml.bind.annotation.*;


/**
 * This is the bean which transports the info to the gadget
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class GadgetQuestion {
    public String issueKey;
    public String issueSummary;
    public String questionText;
    public String status;
    public boolean answered;

    /**
     * Default constructor
     */
    public GadgetQuestion() {
    }

    /**
     * Constructor, the right part.
     * @param issueKey
     * @param issueSummary
     * @param questionText
     * @param status
     * @param answered
     */
    public GadgetQuestion(String issueKey, String issueSummary,
                          String questionText,
                          String status, boolean answered) {
        this.issueKey = issueKey;
        this.issueSummary = issueSummary;
        this.questionText = questionText;
        this.status = status;
        this.answered = answered;
    }
}

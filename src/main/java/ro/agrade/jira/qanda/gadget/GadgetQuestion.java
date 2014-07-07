/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
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
	public long id;
	public int noAnswers;
    public String issueKey;
    public String issueSummary;
    public String questionText;
    public String status;
    public String user;
    public boolean answered;
	public String timestamp;

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
    public GadgetQuestion(long id, int noAnswers, 
    					  String issueKey, String issueSummary,
                          String questionText,
                          String status,
                          String user,
                          boolean answered,
                          String timestamp) {
    	this.id = id;
    	this.noAnswers = noAnswers;
        this.issueKey = issueKey;
        this.issueSummary = issueSummary;
        this.questionText = questionText;
        this.status = status;
        this.user = user;
        this.answered = answered;
        this.timestamp = timestamp;
    }
    
}

/*
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda;

import javax.xml.bind.annotation.*;

/**
 * The status of the question
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public enum QuestionStatus {
    /** Question either has no answer or the answers are not accepted */
    OPEN,
    /** Question has an accepted answer */
    CLOSED
}

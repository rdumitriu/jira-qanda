/*
 * Created by Florin Manaila on 10-Oct-15.
 */
package ro.agrade.jira.qanda.workflow;

import com.opensymphony.workflow.Condition;
import ro.agrade.jira.qanda.QandAService;

/**
 * Condition class
 *
 * @author Florin Manaila (florin.manaila@gmail.com)
 */
public class NoOpenQuestionsC extends NoOpenQuestionsVC implements Condition {
    /**
     * Constructor
     *
     * @param service
     */
    public NoOpenQuestionsC(QandAService service) {
        super(service);
    }
}

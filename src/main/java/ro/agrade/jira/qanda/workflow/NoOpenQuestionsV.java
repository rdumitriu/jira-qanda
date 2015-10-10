/*
 * Created by Florin Manaila on 10-Oct-15.
 */
package ro.agrade.jira.qanda.workflow;

import com.opensymphony.workflow.Validator;
import ro.agrade.jira.qanda.QandAService;

/**
 * Validator class
 *
 * @author Florin Manaila (florin.manaila@gmail.com)
 */
public class NoOpenQuestionsV extends NoOpenQuestionsVC implements Validator {
    /**
     * Constructor
     *
     * @param service
     */
    public NoOpenQuestionsV(QandAService service) {
        super(service);
    }
}

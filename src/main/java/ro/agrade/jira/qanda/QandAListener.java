/*
 * Created on 6/2/13
 */
package ro.agrade.jira.qanda;

/**
 * The listener interface
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public interface QandAListener {

    /**
     * Called on the event
     * @param qaEvent the event
     */
    public abstract void onEvent(QandAEvent qaEvent);

}

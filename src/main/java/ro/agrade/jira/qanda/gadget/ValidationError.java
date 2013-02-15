/*
 * Created on 2/5/13
 */
package ro.agrade.jira.qanda.gadget;

import java.util.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This bean is also required by the gadget configuration
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@XmlRootElement
public class ValidationError {
    // The field the error relates to
    @XmlElement
    public String field;
    // The Error key...
    @XmlElement
    public String error;

    @XmlElement
    public Collection<String> params;
}

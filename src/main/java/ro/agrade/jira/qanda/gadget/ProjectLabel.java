/*
 * Created on 2/5/13
 */
package ro.agrade.jira.qanda.gadget;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * ::TODO:: documentation
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@XmlRootElement
public class ProjectLabel {
    @XmlElement
    public String label;
    @XmlElement
    public String value;
}

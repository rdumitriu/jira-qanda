/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/14/13
 */
package ro.agrade.jira.qanda.admin;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response bean
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@XmlRootElement
public class ExpertGroupError {
    private String duplicateGroupName;
    private String [] wrongUsers;
    private boolean error;

    public ExpertGroupError() {
        error = false;
    }

    public ExpertGroupError(List<String> wrongUsers) {
        if(wrongUsers != null) {
            this.wrongUsers = wrongUsers.toArray(new String[wrongUsers.size()]);
        }
        this.error = true;
    }

    public ExpertGroupError(String duplicateGroupName) {
        this.duplicateGroupName = duplicateGroupName;
        this.error = true;
    }

    @XmlAttribute
    public String getDuplicateGroupName() {
        return duplicateGroupName;
    }

    @XmlAttribute
    public String[] getWrongUsers() {
        return wrongUsers;
    }
}

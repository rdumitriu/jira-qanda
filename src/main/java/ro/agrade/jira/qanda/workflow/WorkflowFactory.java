/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 7/22/13
 */
package ro.agrade.jira.qanda.workflow;

import java.util.*;

import com.atlassian.jira.plugin.workflow.*;
import com.opensymphony.workflow.loader.AbstractDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ::TODO:: documentation
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class WorkflowFactory implements WorkflowPluginValidatorFactory, WorkflowPluginConditionFactory {

    private static final Log LOG = LogFactory.getLog(WorkflowFactory.class);

    public WorkflowFactory() {
    }

    /**
     * No parameters, we have nothing to configure
     * @param abstractDescriptor the descriptor
     * @return a map of parameters used in view, edit, etc
     */
    @Override
    public Map<String, ?> getVelocityParams(String s, AbstractDescriptor abstractDescriptor) {
        return new HashMap<String, Object>();
    }

    /**
     * No parameters, we have nothing to configure
     * @param stringObjectMap the provided ones
     * @return a map of parameters used in descriptor
     */
    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> stringObjectMap) {
        return stringObjectMap != null ? stringObjectMap : new HashMap<String, Object>();
    }
}

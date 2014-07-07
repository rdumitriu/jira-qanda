/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/9/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

/**
 * The expert group service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public interface ExpertGroupService {
    /**
     * Gets an expert group
     * @param groupId the group id
     * @return the expert group, if any
     */
    public abstract ExpertGroup getExpertGroup(long groupId);

    /**
     * Gets an expert group
     * @param name the group name
     * @return the expert group, if any
     */
    public abstract ExpertGroup getExpertGroup(String name);

    /**
     * Gets an expert group
     * @param project the project name
     * @return the expert group, if any
     */
    public abstract List<ExpertGroup> getExpertGroupsForProject(String project);

    /**
     * Adds an expert group
     * @param eg the expert group
     * @return true if it was added, false if not (name clash)
     */
    public abstract boolean add(ExpertGroup eg) throws BadUsersException, DuplicateExpertGroupException;

    /**
     * removes an expert group
     * @param id the expert group
     */
    public abstract void remove(long id);

    /**
     * updates an expert group
     * @param eg the expert group
     * @return true if it was updated, false if not (name clash)
     */
    public abstract boolean update(ExpertGroup eg) throws BadUsersException, DuplicateExpertGroupException;
}

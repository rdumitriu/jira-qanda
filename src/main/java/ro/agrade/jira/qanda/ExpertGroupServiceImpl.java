/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/9/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.UserManager;

import ro.agrade.jira.qanda.dao.ExpertGroupDataService;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Expert group service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class ExpertGroupServiceImpl implements ExpertGroupService {
    private static final Log LOG = LogFactory.getLog(ExpertGroupServiceImpl.class);
    public static final String PROJECT_EMPTY = "-";
    private final ExpertGroupDataService service;
    private final UserManager userManager;

    public ExpertGroupServiceImpl(final ExpertGroupDataService service,
                                  final UserManager userManager) {
        this.service = service;
        this.userManager = userManager;
    }

    /**
     * Gets an expert group
     *
     * @param groupId the group id
     * @return the expert group, if any
     */
    @Override
    public ExpertGroup getExpertGroup(long groupId) {
        return service.getExpertGroup(groupId);
    }

    /**
     * Gets an expert group
     *
     * @param name the group name
     * @return the expert group, if any
     */
    @Override
    public ExpertGroup getExpertGroup(String name) {
        return service.getExpertGroup(name);
    }

    /**
     * Gets an expert group
     *
     * @param project the project name
     * @return the expert group, if any
     */
    @Override
    public List<ExpertGroup> getExpertGroupsForProject(String project) {
        List<ExpertGroup> globalExperts = service.getExpertGroupsForProject(PROJECT_EMPTY);
        List<ExpertGroup> projectExperts = service.getExpertGroupsForProject(project);
        List<ExpertGroup> results = new ArrayList<ExpertGroup>();
        if(projectExperts != null) {
            results.addAll(projectExperts);
        }
        if(globalExperts != null) {
            results.addAll(globalExperts);
        }
        return results;
    }

    /**
     * Adds an expert group
     *
     * @param eg the expert group
     * @return true if it was added, false if not (name clash)
     */
    @Override
    public boolean add(ExpertGroup eg) throws BadUsersException, DuplicateExpertGroupException {
        correctAndValidateEGData(eg);
        return service.add(eg);
    }

    /**
     * removes an expert group
     *
     * @param id the expert group
     */
    @Override
    public void remove(long id) {
        service.remove(id);
    }

    /**
     * updates an expert group
     *
     * @param eg the expert group
     * @return true if it was updated, false if not (name clash)
     */
    @Override
    public boolean update(ExpertGroup eg) throws BadUsersException, DuplicateExpertGroupException {
        correctAndValidateEGData(eg);
        return service.update(eg);
    }

    private void correctAndValidateEGData(ExpertGroup eg) throws BadUsersException, DuplicateExpertGroupException {
        if(eg.getProject() == null || "".equals(eg.getProject().trim())) {
            eg.setProject(PROJECT_EMPTY);
        }
        List<String> badUsers = new ArrayList<String>();
        for(String s : eg.getGroupMembers()) {
            User user = JIRAUtils.toDirectoryUserObject(userManager, s);
            if(user == null) {
                badUsers.add(s);
            }
        }
        if(badUsers.size() > 0) {
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Will throw up, bad users found: %s", badUsers.toString()));
            }
            throw new BadUsersException("Bad users found", badUsers);
        }
        ExpertGroup other = getExpertGroup(eg.getName());
        if(other != null && other.getId() != eg.getId()) {
            throw new DuplicateExpertGroupException("Duplicate expert group", eg.getName());
        }
    }


}

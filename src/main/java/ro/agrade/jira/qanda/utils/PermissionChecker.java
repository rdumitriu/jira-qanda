/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 2/14/13
 */
package ro.agrade.jira.qanda.utils;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;

import ro.agrade.jira.qanda.plugin.PluginStorage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Gathers all permission-related functionalities
 * ::TODO:: why this is not a bean ?
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class PermissionChecker {
    private static final Log LOG = LogFactory.getLog(PermissionChecker.class);

    /**
     *
     * @param permissionManager the permission manager
     * @param issue the issue
     * @param user the user
     * @return true if the user is allowed to see it
     */
    public static boolean
    canViewIssue(PermissionManager permissionManager, Issue issue, ApplicationUser user) {
        return  user != null && isUserInQandAGroup(user) &&
        		permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, user) &&
                permissionManager.hasPermission(Permissions.BROWSE, issue.getProjectObject(), user);
    }



    /**
     *
     * @param permissionManager the permission manager
     * @param project the project
     * @param user the user
     * @return true if the user is allowed to see it
     */
    public static boolean
    canViewProjectPanel(PermissionManager permissionManager, Project project, ApplicationUser user) {
        if(user == null || !isUserInQandAGroup(user)) {
            return false;
        }
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
               permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user) ||
               permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user) ||
               permissionManager.hasPermission(Permissions.BROWSE, project, user);
    }

    /**
     *
     * @param permissionManager the permission manager
     * @param project the project
     * @param user the user
     * @return true if the user is allowed to see it
     */
    public static boolean
    canEditProjectExperts(PermissionManager permissionManager, Project project, ApplicationUser user) {
        if(user == null || !isUserInQandAGroup(user)) {
            return false;
        }
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
                permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user) ||
                permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    /**
     *
     * @param permissionManager the permission manager
     * @param project the project
     * @param user the user
     * @return true if the user is allowed to see it
     */
    public static boolean
    canEditGlobalExperts(PermissionManager permissionManager, Project project, ApplicationUser user) {
        if(user == null || !isUserInQandAGroup(user)) {
            return false;
        }
        return permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
               permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, user) ||
               permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    /**
     *
     * @param permissionManager the permission manager
     * @param issue the issue
     * @param user the user
     * @return true if the user is admin or lead for that issue
     */
    public static boolean
    isUserLeadOrAdmin(PermissionManager permissionManager, Issue issue, ApplicationUser user) {
    	if(user == null || !isUserInQandAGroup(user)) {
    		return false;
    	}
        if(permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
                permissionManager.hasPermission(Permissions.PROJECT_ADMIN, issue.getProjectObject(), user) ||
                permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)) {
            return true;
        }
        if(issue.getProjectObject().getProjectLead() != null &&
                issue.getProjectObject().getProjectLead().getKey().equals(user.getKey())) {
            return true;
        }
        if(issue.getComponentObjects() != null) {
            for(ProjectComponent cmpt : issue.getComponentObjects()) {
                if(cmpt.getLead() != null && cmpt.getLead().equals(user.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param permissionManager the permission manager
     * @param issue the issue
     * @param user the user
     * @param owner the owner
     * @return true if the user is admin or lead for that issue of if it is simply the owner
     */
    public static boolean
    isUserOwner(PermissionManager permissionManager, Issue issue, ApplicationUser user, String owner) {
        if(user == null || !isUserInQandAGroup(user)) {
            return false;
        }
        return (owner.equals(user.getName()) ||
                isUserLeadOrAdmin(permissionManager, issue, user));
    }

    /**
     *
     * @param permissionManager the permission manager
     * @param issue the issue
     * @param user the user
     * @return true if s/he can edit the issue
     */
    public static boolean isIssueEditable(PermissionManager permissionManager, Issue issue, ApplicationUser user) {
        if(user == null || !isUserInQandAGroup(user)) {
            return false;
        }
        return permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user);
    }

    private static boolean isUserInQandAGroup(ApplicationUser user) {
        try {
            Group g = PluginStorage.getGroup();
            if(g == null) {
                //no configuration means that we do not care about
                return true;
            }
            return ComponentAccessor.getGroupManager().isUserInGroup(user.getDirectoryUser(), g);
        } catch(Exception e) {
            LOG.error(String.format("Exception while trying to establish group membership for %s", user.getUsername()));
        }
        return false;
    }
}

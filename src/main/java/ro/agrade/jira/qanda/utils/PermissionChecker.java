/*
 * Created on 2/14/13
 */
package ro.agrade.jira.qanda.utils;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * Gathers all permission-related functionalities
 * ::TODO:: why this is not a bean ?
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class PermissionChecker {

    /**
     *
     * @param permissionManager the permission manager
     * @param issue the issue
     * @param user the user
     * @return true if the user is allowed to see it
     */
    public static boolean
    canViewIssue(PermissionManager permissionManager, Issue issue, User user) {
        return  user != null &&
        		permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, user) &&
                permissionManager.hasPermission(Permissions.BROWSE, issue.getProjectObject(), user);
    }

    /**
     *
     * @param permissionManager the permission manager
     * @param issue the issue
     * @param user the user
     * @return true if the user is admin or lead for that issue
     */
    public static boolean
    isUserLeadOrAdmin(PermissionManager permissionManager, Issue issue, User user) {
    	if(user == null) {
    		return false;
    	}
        if(permissionManager.hasPermission(Permissions.ADMINISTER, user) ||
                permissionManager.hasPermission(Permissions.PROJECT_ADMIN, issue.getProjectObject(), user) ||
                permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user)) {
            return true;
        }
        if(issue.getProjectObject().getLead() != null &&
                issue.getProjectObject().getLead().getName().equals(user.getName())) {
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
    isUserOwner(PermissionManager permissionManager, Issue issue, User user, String owner) {
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
    public static boolean isIssueEditable(PermissionManager permissionManager, Issue issue, User user) {
        return permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user);
    }
}

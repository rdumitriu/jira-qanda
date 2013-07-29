/*
 * Created on 4/13/13
 */
package ro.agrade.jira.qanda.utils;

import java.net.URI;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

/**
 * The utilities. Right now, only to create the relative path to the JIRA
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public final class JIRAUtils {

    /** Never instantiate me */
    private JIRAUtils() {}

    /**
     * Getting rid of the problems appearing when you install JIRA with two or more
     * DNS names
     * @param props the application properties
     * @return the path, relative to the server
     */
    public static String getRelativeJIRAPath(ApplicationProperties props) {
        URI uri = URI.create(props.getString("jira.baseurl"));
        String rawBase = uri.getPath(); //this returns the last path of the JIRA install
        return ((rawBase.length() > 0)
                    ? (rawBase.charAt(rawBase.length() - 1) == '/' ? rawBase.substring(0, rawBase.length() - 1) : rawBase)
                    : "");
    }

    /**
     * The full JIRA path, excluding an eventual ending /
     * @param props the application properties
     * @return the path, full with host
     */
    public static String getFullJIRAPath(ApplicationProperties props) {
        String ret = props.getString("jira.baseurl");
        if(ret.endsWith("/")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }

    /**
     * Gets an issue full URI (or project)
     * @param props the application properties
     * @param issueKey the issue key
     * @return the path, relative to the server
     */
    public static String getIssueJIRAPath(ApplicationProperties props, String issueKey) {
        return getFullJIRAPath(props) + "/browse/" + issueKey;
    }

    /**
     * Creates an user object from a string. If not found, simply returns null
     * @param userManager the user manager
     * @param uNameOrKey the key
     * @return the user, if found
     */
    public static User toDirectoryUserObject(UserManager userManager, String uNameOrKey) {
        ApplicationUser appUser = userManager.getUserByKey(uNameOrKey);
        if(appUser == null) {
            appUser = userManager.getUserByName(uNameOrKey);
        }
        return appUser != null ? appUser.getDirectoryUser() : null;
    }

    /**
     * Creates an user object from a string. If not found, simply returns null
     * @param userManager the user manager
     * @param uNameOrKey the key
     * @return the user, if found
     */
    public static ApplicationUser toUserObject(UserManager userManager, String uNameOrKey) {
        ApplicationUser appUser = userManager.getUserByKey(uNameOrKey);
        if(appUser == null) {
            appUser = userManager.getUserByName(uNameOrKey);
        }
        return appUser;
    }
}

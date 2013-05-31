/*
 * Created on 4/13/13
 */
package ro.agrade.jira.qanda.utils;

import java.net.URI;

import com.atlassian.jira.config.properties.ApplicationProperties;

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
}

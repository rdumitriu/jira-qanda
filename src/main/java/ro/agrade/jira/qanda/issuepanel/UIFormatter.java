/*
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda.issuepanel;

import java.text.*;
import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar.Size;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * Formats the
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class UIFormatter {
    private static final Log LOG = LogFactory.getLog(UIFormatter.class);

    private final UserManager userManager;
    private final JiraAuthenticationContext authContext;
    private final String baseURL;
    private final RendererManager rendererMgr;
    private final IssueRenderContext renderContext;
    private final AvatarService avatarService;
    private static final String STDFORMAT = "yyyy-MM-dd HH:mm";

    public UIFormatter(final UserManager userManager,
                       final JiraAuthenticationContext authContext,
                       final AvatarService avatarService,
                       final ApplicationProperties properties,
                       final RendererManager rendererMgr,
                       final Issue issue) {
        this.userManager = userManager;
        this.authContext = authContext;
        this.avatarService = avatarService;
        this.baseURL = properties.getString("jira.baseurl");
        this.rendererMgr = rendererMgr;
        this.renderContext = new IssueRenderContext(issue);
    }

    /**
     * Formats the TS
     * @param ts the timestamp
     * @return the formatted TS, as a string
     */
    public String formatTimeStamp(long ts) {
        Date d = new Date();
        d.setTime(ts);
        DateFormat fmt = new SimpleDateFormat(STDFORMAT, authContext.getLocale());
        return fmt.format(d);
    }

    /**
     * Formats the user to put a link to her/his profile. At this very moment we do not
     * care if the user has the right to view the profile (we can safely assume that
     * in most projects profiles to the other members can be seen)
     *
     * @param user the user
     * @return the user link
     */
    public String formatUser(String user) {
        User userObj = null;
        String avatarUrl = null;
        try {
            userObj = userManager.getUserObject(user);
            avatarUrl = avatarService.getAvatarUrlNoPermCheck(user, Size.SMALL).toString();
        } catch(Throwable t) { /* we do not care */ }
        if(null == userObj) {
            return user; // unknown, deleted
        }
        return String.format("<a class='qandaemails user-hover %s' %s rel='%s' href=\"%s/secure/ViewProfile.jspa?name=%s\">%s</a>",
        					 avatarUrl != null ? "user-avatar" : "",
        					 avatarUrl != null 
        					 	? String.format("style=\"background-image: url(%s);\"", avatarUrl)
        					 	: "",
        					 userObj.getName(),
                             baseURL,
                             userObj.getName(),
                             userObj.getDisplayName());
    }

    /**
     * Wiki style formatter for our text
     * @param text the text to be formatted
     * @return the formatted text
     */
    public String formatText(String text) {
        return rendererMgr.getRendererForType("atlassian-wiki-renderer")
                                            .render(text, renderContext);
    }
}

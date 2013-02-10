/*
 * Created on 1/19/13
 */
package ro.agrade.jira.qanda.issuepanel;

import java.text.*;
import java.util.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;

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
    private static final String STDFORMAT = "yyyy-MM-dd HH:mm";

    public UIFormatter(final UserManager userManager,
                       final JiraAuthenticationContext authContext,
                       final ApplicationProperties properties,
                       final RendererManager rendererMgr,
                       final Issue issue) {
        this.userManager = userManager;
        this.authContext = authContext;
        this.baseURL = properties.getString("jira.baseurl");
        this.rendererMgr = rendererMgr;
        this.renderContext = new IssueRenderContext(issue);
    }

    public String formatTimeStamp(long ts) {
        Date d = new Date();
        d.setTime(ts);
        DateFormat fmt = new SimpleDateFormat(STDFORMAT, authContext.getLocale());
        return fmt.format(d);
    }

    public String formatUser(String user) {
        User userObj = null;
        try {
            userObj = userManager.getUserObject(user);
        } catch(Throwable t) { /* we do not care */ }
        if(null == userObj) {
            return user; // unknown, deleted
        }
        return String.format("<a class='qandaemails' href=\"%s/secure/ViewProfile.jspa?name=%s\">%s</a>",
                             baseURL,
                             userObj.getName(),
                             userObj.getDisplayName());
    }

    public String formatText(String text) {
//        if(LOG.isDebugEnabled()) {
//            for(JiraRendererPlugin plg : rendererMgr.getAllActiveRenderers()) {
//                LOG.debug("Renderer: " + plg.getRendererType() + " desc:");
//            }
//        }
        return rendererMgr.getRendererForType("atlassian-wiki-renderer")
                                            .render(text, renderContext);
    }
}

/*
 * Created on 1/21/13
 */
package ro.agrade.jira.qanda.gadget;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.*;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.JiraDateUtils;

import ro.agrade.jira.qanda.*;
import ro.agrade.jira.qanda.issuepanel.UIFormatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Our rest service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@Path("/gadget")
public class GadgetRestService {
    private static final Log LOG = LogFactory.getLog(GadgetRestService.class);

    private final QandAService service;
    private final RendererManager rendererMgr;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authContext;
    private final PermissionManager permMgr;
	private final ApplicationProperties properties;
	private final AvatarService avatarService;
	private final UserManager userManager;

    public GadgetRestService(final QandAService service,
                             final RendererManager rendererMgr,
                             final IssueManager issueManager,
                             final JiraAuthenticationContext authContext,
                             final PermissionManager permMgr,
                             final ApplicationProperties appProps,
                             final AvatarService avatarServ,
                             final UserManager userMgr) {
        this.service = service;
        this.rendererMgr = rendererMgr;
        this.issueManager = issueManager;
        this.authContext = authContext;
        this.permMgr = permMgr;
		this.properties = appProps;
		this.avatarService = avatarServ;
		this.userManager = userMgr;
    }

    /**
     * Validates the gadget configuration
     * @param project the project
     * @param issinterval the interval
     * @return always null
     */
    @GET
    @Path("/validate")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public ErrorCollection validate(@QueryParam("project") String project,
                                    @QueryParam("issinterval") String issinterval) {
        //at this point, I see no reason to validate this ...
        //of course it should, but I have no time for unlikely errors :)
        return null;
    }

    /**
     * Gets the projects accessible by this user
     *
     * @return the list of projects to be shown in the config ('Edit')
     */
    @GET
    @Path("/projects")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public List<GadgetConfigLabel> getProjects() {
        List<GadgetConfigLabel> lbls = new ArrayList<GadgetConfigLabel>();
        Collection<Project> browsePrj = permMgr.getProjectObjects(Permissions.BROWSE, authContext.getLoggedInUser());
        if(browsePrj != null) {
            User user = authContext.getLoggedInUser();
            for(Project p : browsePrj) {
                if(permMgr.hasPermission(Permissions.COMMENT_ISSUE, p, user) &&
                        permMgr.hasPermission(Permissions.BROWSE, p, user)) {
                    GadgetConfigLabel pl = new GadgetConfigLabel();
                    pl.label = p.getName();
                    pl.value = p.getKey();
                    lbls.add(pl);
                }
            }
        }
        Collections.sort(lbls, new Comparator<GadgetConfigLabel>() {
            @Override
            public int compare(GadgetConfigLabel o1, GadgetConfigLabel o2) {
                return o1.label.compareTo(o2.label);
            }
        });
        return lbls;
    }

    /**
     * Gets the intervals
     *
     * @return the intervals
     */
    @GET
    @Path("/intervals")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public List<GadgetConfigLabel> getIntervals() {
        List<GadgetConfigLabel> ret = new ArrayList<GadgetConfigLabel>();
        for(QandAService.TimePeriod tp : QandAService.TimePeriod.values()) {
            GadgetConfigLabel gcl = new GadgetConfigLabel();
            gcl.label = tp.getLabel();
            gcl.value = tp.name();
            ret.add(gcl);
        }
        return ret;
    }


    /**
     * Gets the questions asked on the configured project for the given interval
     * @param project the project
     * @param interval the interval
     * @return the questions
     */
    @POST
    @Path("/getquestions")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public List<GadgetQuestion> getQuestions(@FormParam("project") String project,
                                             @FormParam("interval") String interval) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Getting questions for project:" + project + "<");
        }
        QandAService.TimePeriod tp = QandAService.TimePeriod.SIX_MONTHS;
        if(interval != null && !"".equals(interval.trim())) {
            tp = QandAService.TimePeriod.valueOf(interval);
        }
        List<Question> questions = service.getUnsolvedQuestionsForProject(project, tp);
        if(questions != null) {
            return formatQuestions(questions);
        }
        return new ArrayList<GadgetQuestion>();
    }

    /* ========================================================================
     * I N T E R N A L  K I T C H E N
     * ====================================================================== */

    private List<GadgetQuestion> formatQuestions(List<Question> questions) {
        List<GadgetQuestion> ret = new ArrayList<GadgetQuestion>();
        for(Question q : questions) {
            Issue issue = issueManager.getIssueObject(q.getIssueId());
            String qtext = formatQuestionText(issue, q);
            UIFormatter formatter = new UIFormatter(userManager, authContext, avatarService, properties, rendererMgr, issue);
            ret.add(new GadgetQuestion(q.getId(), 
            						   q.getAnswers() != null ? q.getAnswers().size() : 0,
            						   issue.getKey(), 
            						   issue.getSummary(),
                                       qtext, 
                                       q.getStatus().name(), 
                                       formatter.formatUser(q.getUser()),
                                       q.isAnswered(),
                                       formatter.formatTimeStamp(q.getTimeStamp())));
        }
        return ret;
    }

    private String formatQuestionText(Issue issue, Question q) {
        try {
            return rendererMgr.getRendererForType("atlassian-wiki-renderer")
                    .render(q.getQuestionText(), new IssueRenderContext(issue));
        } catch(Exception e) {
            LOG.warn(String.format("Error rendering question %d for issue %d",
                                   q.getId(), q.getIssueId()));
        }
        return "";
    }

}

/*
 * Created on 1/21/13
 */
package ro.agrade.jira.qanda.gadget;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import com.atlassian.jira.issue.*;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.*;

import ro.agrade.jira.qanda.QandAService;
import ro.agrade.jira.qanda.Question;

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

    public GadgetRestService(final QandAService service,
                             final RendererManager rendererMgr,
                             final IssueManager issueManager,
                             final JiraAuthenticationContext authContext,
                             final PermissionManager permMgr) {
        this.service = service;
        this.rendererMgr = rendererMgr;
        this.issueManager = issueManager;
        this.authContext = authContext;
        this.permMgr = permMgr;
    }

    @GET
    @Path("/validate")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public ErrorCollection validate(@QueryParam("project") String project) {
        return null;
    }

    @GET
    @Path("/projects")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public List<ProjectLabel> getProjects() {
        return getAccessibleProjects();
    }


    public List<ProjectLabel> getAccessibleProjects() {
        List<ProjectLabel> lbls = new ArrayList<ProjectLabel>();
        Collection<Project> browsePrj = permMgr.getProjectObjects(Permissions.BROWSE, authContext.getLoggedInUser());
        if(browsePrj != null) {
            for(Project p : browsePrj) {
                if(permMgr.hasPermission(Permissions.COMMENT_ISSUE, p, authContext.getLoggedInUser())) {
                    ProjectLabel pl = new ProjectLabel();
                    pl.label = p.getName();
                    pl.value = p.getKey();
                    lbls.add(pl);
                }
            }
        }
        Collections.sort(lbls, new Comparator<ProjectLabel>() {
            @Override
            public int compare(ProjectLabel o1, ProjectLabel o2) {
                return o1.label.compareTo(o2.label);
            }
        });
        return lbls;
    }


    @POST
    @Path("/getquestions")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public List<Question> getQuestions(@FormParam("project") String project) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Getting questions for project:" + project + "<");
        }
        //::TODO::List<Question> questions = service.getUnsolvedQuestionsForProject(project);
        //formatText(questions);
        return new ArrayList<Question>();
    }

    private void formatText(List<Question> questions) {
        if(questions == null) {
            return;
        }
        for(Question q : questions) {
            formatQuestionText(q);
        }
    }

    private void formatQuestionText(Question q) {
        try {
            Issue issue = issueManager.getIssueObject(q.getIssueId());
            q.setQuestionText(rendererMgr.getRendererForType("atlassian-wiki-renderer")
                    .render(q.getQuestionText(), new IssueRenderContext(issue)));
        } catch(Exception e) {
            LOG.warn(String.format("Error rendering question %d for issue %d",
                                   q.getId(), q.getIssueId()));
        }
    }


}

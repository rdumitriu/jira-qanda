/**
 *
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Date: 7/2/13
 * Time: 9:04 PM
 */
package ro.agrade.jira.qanda.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.DefaultUserPickerSearchService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.*;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;
import ro.agrade.jira.qanda.ExpertGroup;
import ro.agrade.jira.qanda.ExpertGroupService;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The REST service for autocomplete
 *
 * @author Florin Manaila (florin.manaila@gmail.com)
 */
@Path("/mentions")
public class QandAMentionsRestService {

    private static final Log LOG = LogFactory.getLog(QandAMentionsRestService.class);

    public static final int DEFAULT_USERS_RETURNED = 50;
    public static final int MAX_USERS_RETURNED = 1000;


    private UserPickerSearchService upss;
    private TimeZoneManager timeZoneManager;
    private JiraAuthenticationContext authContext;
    private EmailFormatter emailFormatter;
    private ApplicationProperties appProps;
    private JiraBaseUrls jiraBaseUrls;
    private ExpertGroupService egService;
    private ProjectManager projectManager;

    public QandAMentionsRestService(final UserManager userManager,
                                    final ApplicationProperties applicationProperties,
                                    final PermissionManager permissionManager,
                                    final JiraAuthenticationContext authContext,
                                    final JiraBaseUrls jiraBaseUrls,
                                    final ProjectManager projectManager,
                                    final ExpertGroupService egService,
                                    final GroupManager groupManager,
                                    final ProjectRoleManager projectRoleManager) {
        this.appProps = applicationProperties;
        this.jiraBaseUrls = jiraBaseUrls;
        this.egService = egService;
        this.projectManager = projectManager;
        this.timeZoneManager = ComponentAccessor.getComponentOfType(TimeZoneManager.class);
        this.authContext = authContext;
        this.emailFormatter = ComponentAccessor.getComponentOfType(EmailFormatter.class);
        try {
            Constructor ct = DefaultUserPickerSearchService.class.getDeclaredConstructor(UserManager.class, ApplicationProperties.class, PermissionManager.class);
            this.upss = (DefaultUserPickerSearchService)ct.newInstance(userManager, applicationProperties, permissionManager);
        } catch(Throwable ex) {
            //JIRA 6.2
            try {
                Constructor ct = DefaultUserPickerSearchService.class.getDeclaredConstructor(UserManager.class, ApplicationProperties.class,
                                                                                             JiraAuthenticationContext.class, PermissionManager.class,
                                                                                             GroupManager.class, ProjectManager.class, ProjectRoleManager.class);
                this.upss = (DefaultUserPickerSearchService)ct.newInstance(userManager, applicationProperties,
                                                                           authContext, permissionManager,
                                                                           groupManager, projectManager,
                                                                           projectRoleManager);
            } catch(Throwable ex2) {
                LOG.fatal("Cannot instantiate the user picker search service. We're doomed. Future calls will result in NPE!");
            }

        }
    }

    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response getAutocomplete(@QueryParam("username") final String username,
                                    @QueryParam ("startAt") Integer startAt,
                                    @QueryParam ("maxResults") Integer maxResults,
                                    @QueryParam ("projectKey") String projectKey) throws URISyntaxException {
        List<UserBean> convertedEgs = new ArrayList<UserBean>();


        List<ExpertGroup> egs = egService.getExpertGroupsForProject(projectKeyFromName(projectKey));
        Map<String, URI> egAvatars = new HashMap<String, URI>();
        egAvatars.put("16x16", new URI(JIRAUtils.getRelativeJIRAPath(appProps).concat("/images/icons/filter_public.gif")));

        // create expert group autocomplete
        for(ExpertGroup eg : egs){
            if(eg.getName().toLowerCase().startsWith(username.toLowerCase()) ||
               eg.getDescription().toLowerCase().startsWith(username.toLowerCase())){
                convertedEgs.add(convertExpertGroupToUserBean(eg, egAvatars));
            }
        }

        final List<User> page = limitUserSearch(startAt, maxResults, findUsers(username));
        List<UserBean> userBeans = makeUserBeans(page);

        // EGs first
        convertedEgs.addAll(userBeans);
        return Response.ok(convertedEgs).cacheControl(com.atlassian.jira.rest.api.http.CacheControl.never()).build();
    }

    private JiraServiceContext createContext() {
        ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getUser();
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        return new JiraServiceContextImpl(user, errorCollection);
    }

    private List<User> limitUserSearch(Integer startAt, Integer maxResults, List<User> users) {
        int start = startAt != null ? Math.max(0, startAt) : 0;
        int end = (maxResults != null
                            ? Math.min(MAX_USERS_RETURNED, maxResults)
                            : DEFAULT_USERS_RETURNED)
                   + start;
        return users.subList(start, Math.min(users.size(), end));
    }

    private List<User> findUsers(final String searchString) {
        if (searchString == null){
            throw new RESTException(Response.Status.NOT_FOUND,
                                    ErrorCollection.of("Null search string."));
        }
        return upss.findUsers(createContext(), searchString);
    }

    private List<UserBean> makeUserBeans(Collection<User> users) {
        List<UserBean> beans =  new ArrayList<UserBean>();
        for (User user : users) {
            UserBeanBuilder builder = new UserBeanBuilder(jiraBaseUrls).user(user);
            builder.loggedInUser(authContext.getUser());
            builder.emailFormatter(emailFormatter);
            builder.timeZone(timeZoneManager.getLoggedInUserTimeZone());
            beans.add(builder.buildMid());
        }
        return beans;
    }

    private UserBean convertExpertGroupToUserBean(ExpertGroup eg, Map<String, URI> egAvatars) {
        return new UserBean(null,
                            eg.getName(),
                            eg.getName(),
                            "[EG] ".concat(eg.getName()),
                            true,
                            StringUtils.isEmpty(eg.getDescription()) ? " " : eg.getDescription(),
                            null,
                            egAvatars,
                            null);
    }

    private String projectKeyFromName(String projectKeyOrName) {
        Project p = projectManager.getProjectObjByKey(projectKeyOrName);
        if(p == null) {
            p = projectManager.getProjectObjByName(projectKeyOrName);
        }
        if(p != null) {
            return p.getKey();
        }
        //if project is null, just believe you got the good data and cross your fingers
        return projectKeyOrName;
    }
}

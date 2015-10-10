/**
 *
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Date: 7/2/13
 * Time: 9:04 PM
 */
package ro.agrade.jira.qanda.rest;

import com.atlassian.jira.avatar.JiraAvatarSupport;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ro.agrade.jira.qanda.ExpertGroup;
import ro.agrade.jira.qanda.ExpertGroupService;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    private UserSearchService uss;
    private TimeZoneManager timeZoneManager;
    private JiraAuthenticationContext authContext;
    private I18nHelper.BeanFactory beanFactory;
    private EmailFormatter emailFormatter;
    private ApplicationProperties appProps;
    private JiraBaseUrls jiraBaseUrls;
    private ExpertGroupService egService;
    private ProjectManager projectManager;
    private JiraAvatarSupport jiraAvatarSupport;

    public QandAMentionsRestService(final ApplicationProperties applicationProperties,
                                    final UserSearchService uss,
                                    final TimeZoneManager timeZoneManager,
                                    final JiraAuthenticationContext authContext,
                                    final I18nHelper.BeanFactory beanFactory,
                                    final JiraBaseUrls jiraBaseUrls,
                                    final ProjectManager projectManager,
                                    final ExpertGroupService egService,
                                    final JiraAvatarSupport jiraAvatarSupport,
                                    final EmailFormatter emailFormatter) {
        this.appProps = applicationProperties;
        this.uss = uss;
        this.timeZoneManager = timeZoneManager;
        this.beanFactory = beanFactory;
        this.jiraBaseUrls = jiraBaseUrls;
        this.egService = egService;
        this.projectManager = projectManager;
        this.jiraAvatarSupport = jiraAvatarSupport;
        this.emailFormatter = emailFormatter;
        this.authContext = authContext;
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

        final List<ApplicationUser> page = limitUserSearch(startAt, maxResults, findUsers(username));
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

    private List<ApplicationUser> limitUserSearch(Integer startAt, Integer maxResults, List<ApplicationUser> users) {
        int start = startAt != null ? Math.max(0, startAt) : 0;
        int end = (maxResults != null
                            ? Math.min(MAX_USERS_RETURNED, maxResults)
                            : DEFAULT_USERS_RETURNED)
                   + start;
        return users.subList(start, Math.min(users.size(), end));
    }

    private List<ApplicationUser> findUsers(final String searchString) {
        if (searchString == null){
            throw new RESTException(Response.Status.NOT_FOUND,
                                    ErrorCollection.of("Null search string."));
        }
        return uss.findUsers(createContext(), searchString);
    }

    private List<UserBean> makeUserBeans(List<ApplicationUser> users) {
        List<UserBean> beans =  new ArrayList<UserBean>();
        for (ApplicationUser user : users) {
            UserBeanBuilder builder = new UserBeanBuilder(jiraBaseUrls, jiraAvatarSupport).user(user);
            builder.loggedInUser(authContext.getUser())
                    .emailFormatter(emailFormatter)
                    .timeZone(timeZoneManager.getLoggedInUserTimeZone())
                    .i18nBeanFactory(beanFactory);
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

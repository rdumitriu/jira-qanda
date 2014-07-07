/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/10/13
 */
package ro.agrade.jira.qanda.admin;

import java.util.*;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.projectpanel.impl.AbstractProjectTabPanel;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.webresource.WebResourceManager;

import ro.agrade.jira.qanda.ExpertGroupService;
import ro.agrade.jira.qanda.plugin.LicenseUtil;
import ro.agrade.jira.qanda.utils.JIRAUtils;
import ro.agrade.jira.qanda.utils.PermissionChecker;

/**
 * The project configuration panel
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class ProjectConfigurationPanel extends AbstractProjectTabPanel {
    private final ExpertGroupService egService;
    private final PermissionManager permissionManager;
    private final WebResourceManager webResourceManager;
    private final ApplicationProperties props;
    private final UserManager userManager;

    public ProjectConfigurationPanel(JiraAuthenticationContext jiraAuthenticationContext,
                                     WebResourceManager webResourceManager,
                                     ApplicationProperties props,
                                     PermissionManager permissionManager,
                                     UserManager userManager,
                                     ExpertGroupService egService) {
        super(jiraAuthenticationContext);
        this.props = props;
        this.webResourceManager = webResourceManager;
        this.egService = egService;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
    }

    @Override
    protected Map<String, Object> createVelocityParams(BrowseContext ctx) {
        Map<String, Object> ret = super.createVelocityParams(ctx);
        if(ret == null) {
            ret = new HashMap<String, Object>();
        }
        webResourceManager.requireResource("ro.agrade.jira.qanda-pro:qanda-admin-resources");
        if(ctx.getProject() != null) {
            ret.put("projectKey", ctx.getProject().getKey());
            ret.put("baseJIRAUrl", JIRAUtils.getRelativeJIRAPath(props));
            ret.put("expertLists", egService.getExpertGroupsForProject(ctx.getProject().getKey()));
            ret.put("canEditProjectExperts", PermissionChecker.canEditProjectExperts(permissionManager, ctx.getProject(),
                                                                                     JIRAUtils.toUserObject(userManager, ctx.getUser().getName())));
            ret.put("canEditGlobalExperts", PermissionChecker.canEditGlobalExperts(permissionManager, ctx.getProject(),
                                                                                   JIRAUtils.toUserObject(userManager, ctx.getUser().getName())));
            ret.put("userManager", ComponentAccessor.getUserManager());
            ret.put("licenseIsValid", LicenseUtil.isLicenseValid());
        }
        return ret;
    }

    @Override
    public boolean showPanel(BrowseContext ctx) {
        return PermissionChecker.canViewProjectPanel(permissionManager, ctx.getProject(),
                                                     JIRAUtils.toUserObject(userManager, ctx.getUser().getName()));
    }
}

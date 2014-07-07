/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/9/13
 */
package ro.agrade.jira.qanda.listeners;

import java.util.*;

import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import ro.agrade.jira.qanda.*;
import ro.agrade.jira.qanda.utils.JIRAUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This appear only when there's a license
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class LicensedListener extends StandardListener {
    private static final Log LOG = LogFactory.getLog(StandardListener.class);
    private final ExpertGroupService service;
    private final WatcherManager watcherManager;

    public LicensedListener(UserManager userManager,
                            MessageHandler handler,
                            ExpertGroupService service,
                            WatcherManager watcherManager) {
        super(userManager, handler);
        this.service = service;
        this.watcherManager = watcherManager;
    }

    @Override
    protected Set<String> calculateUserSet(QandAEvent qaEvent) {
        Set<String> ret = super.calculateUserSet(qaEvent);

        if(qaEvent.getIssue().getAssignee() != null) {
            ret.add(qaEvent.getIssue().getAssigneeId());
        }
        if(qaEvent.getIssue().getReporter() != null) {
            ret.add(qaEvent.getIssue().getReporterId());
        }
        //watchers:
        if(watcherManager.isWatchingEnabled()) {
            List<String> watchers = watcherManager.getCurrentWatcherUsernames(qaEvent.getIssue());
            if(watchers != null) {
                ret.addAll(watchers);
            }
        }

        if(qaEvent.getUser() != null) {
            ret.remove(qaEvent.getUser().getName());
        }
        return ret;
    }

    @Override
    protected void handleUnknownMention(String s, QandAEvent qaEvent) {
        ExpertGroup group = service.getExpertGroup(s);
        if(group != null) {
            List<String> experts = group.getGroupMembers();
            if(experts != null) {
                for(String expert : experts) {
                    ApplicationUser user = JIRAUtils.toUserObject(getUserManager(), expert);
                    if(user != null) {
                        if(LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Notify expert user %s on event %s", s, qaEvent.getType()));
                        }
                        getHandler().handleMessage(user, qaEvent);
                    }
                }
            }
        }
    }
}

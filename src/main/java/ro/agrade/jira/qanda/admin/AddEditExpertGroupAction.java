/**
 *
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Date: 7/19/13
 * Time: 10:47 PM
 */
package ro.agrade.jira.qanda.admin;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ro.agrade.jira.qanda.*;

/**
 * TODO
 *
 * @author Florin Manaila (florin.manaila@gmail.com)
 */
public class AddEditExpertGroupAction extends JiraWebActionSupport {

    public static final Log LOG = LogFactory.getLog(AddEditExpertGroupAction.class);

    private long egId;
    private String name;
    private String description;
    private String members;
    private String project;
    private boolean isGlobal;

    private ExpertGroupService egServ;

    public AddEditExpertGroupAction(ExpertGroupService egServ){
        this.egServ = egServ;
        isGlobal = false;
    }

    /**
     * Shows the add new EG screen
     * @return
     */
    public String doAdd(){
        return SUCCESS;
    }

    /**
     * Shows the edit screen
     * @return
     */
    public String doEdit(){
        ExpertGroup eg = egServ.getExpertGroup(egId);
        if(eg == null){
            addErrorMessage("Invalid expert group id.");
            return ERROR;
        }
        this.name = eg.getName();
        this.description = eg.getDescription();
        this.members = eg.getGroupMembersAsString();
        this.isGlobal = eg.getProject() == null ||
                        "".equals(eg.getProject()) ||
                        ExpertGroupServiceImpl.PROJECT_EMPTY.equals(eg.getProject());
        if(!isGlobal){
            this.project = eg.getProject();
        }

        return SUCCESS;
    }

    public String doSave(){
        if(StringUtils.isEmpty(this.name)){
            addErrorMessage("Name is mandatory.");
            return ERROR;
        }
        if(StringUtils.isEmpty(this.members)){
            addErrorMessage("Group must have at least one member.");
            return ERROR;
        }
        if(!isGroupNameValid(this.name)){
            addErrorMessage("Invalid expert group name. A user with this name already exists.");
            return ERROR;
        }
        return egId <= 0 ? createNew() : editExisting();
    }

    public String doDelete(){
        if(egId <= 0){
            LOG.warn(String.format("Cannot delete. Invalid " +
                                   "expert group id %s.", egId));
        } else {
            egServ.remove(egId);
        }
        return returnComplete("/browse/"+project+"#selectedTab=ro.agrade.jira.qanda-pro%3Aqanda-config-page");
    }

    private String editExisting() {
        if(LOG.isDebugEnabled()){
            LOG.debug(String.format("Updating expert group to: %s, %s, %s, (isGlobal)%s, %s",
                    this.name, this.description,
                    this.members, isGlobal, project));
        }
        ExpertGroup eg = egServ.getExpertGroup(egId);
        if(eg == null){
            addErrorMessage("Invalid expert group id.");
            return ERROR;
        }
        eg.setName(this.name);
        eg.setDescription(this.description);
        eg.setGroupMembers(this.members);
        if(isGlobal){
            eg.setProject("-");
        } else {
            eg.setProject(project);
        }
        try {
            egServ.update(eg);
        } catch (Exception e) {
            addErrorMessage(e.getMessage());
            return ERROR;
        }
        return returnComplete("/browse/"+project+"#selectedTab=ro.agrade.jira.qanda-pro%3Aqanda-config-page");

    }

    private String createNew() {
        ExpertGroup
        eg = new ExpertGroup(0L,
                             this.name,
                             this.description,
                             isGlobal ? null : this.project,
                             members);
        if(LOG.isDebugEnabled()){
            LOG.debug(String.format("Creating expert group: %s, %s, %s, (isGlobal)%s, %s",
                                    this.name, this.description,
                                    this.members, isGlobal, project));
        }
        try {
            egServ.add(eg);
        } catch (Exception e) {
            addErrorMessage(e.getMessage());
            return ERROR;
        }
        return returnComplete("/browse/"+project+"#selectedTab=ro.agrade.jira.qanda-pro%3Aqanda-config-page");
    }

    private boolean isGroupNameValid(String name){
        return ComponentAccessor.getUserManager().getUserByKey(name) == null &&
               ComponentAccessor.getUserManager().getUserByName(name) == null;
    }

    public String getEgId() {
        return String.valueOf(egId);
    }

    public long getEgIdAsLong() {
        return egId;
    }

    public void setEgId(String egId) {
        if(StringUtils.isEmpty(egId)){
            this.egId = 0;
            return;
        }
        // hopefully no ex here
        this.egId = Long.parseLong(egId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        if(members != null){
            members = StringUtils.trim(members);
            if(members.endsWith(",") && members.length() > 1){
                members = members.substring(0, members.length()-1);
            }
        }
        this.members = members;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getIsGlobal() {
        return String.valueOf(isGlobal);
    }

    public void setIsGlobal(String global) {
        isGlobal = Boolean.valueOf(global);
    }
}

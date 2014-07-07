/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/8/13
 */
package ro.agrade.jira.qanda;

import java.util.*;

/**
 * The expert group
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class ExpertGroup {
    private long id;
    private String name;
    private String description;
    private String project;
    private List<String> groupMembers;

    public ExpertGroup(long id, String name, String description, String project, List<String> groupMembers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.project = project == null || "".equals(project) ? "-" : project;
        this.groupMembers = groupMembers;
    }

    public ExpertGroup(long id, String name, String description, String project, String groupMembers) {
        this(id, name, description, project, new ArrayList<String>());
        String [] arr = groupMembers.split(",");
        if(arr != null) {
            for(String s : arr) {
                if(s != null && !"".equals(s)) {
                    this.groupMembers.add(s.trim());
                }
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public boolean isGlobal() {
        return "-".equals(project);
    }

    public List<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public void setGroupMembers(String members){
        this.groupMembers = new ArrayList<String>();
        if(members == null){
            return;
        }
        String [] arr = members.split(",");
        if(arr != null) {
            for(String s : arr) {
                if(s != null && !"".equals(s)) {
                    this.groupMembers.add(s.trim());
                }
            }
        }
    }

    public String getGroupMembersAsString() {
        StringBuilder sb = new StringBuilder();
        if(groupMembers != null) {
            for(String s : groupMembers) {
                sb.append(s).append(",");
            }
        }
        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}

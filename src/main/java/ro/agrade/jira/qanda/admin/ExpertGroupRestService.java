/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/13/13
 */
package ro.agrade.jira.qanda.admin;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import ro.agrade.jira.qanda.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Expert group service
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@Path("/qandaconfig")
public class ExpertGroupRestService {
    private static final Log LOG = LogFactory.getLog(ExpertGroupRestService.class);

    private ExpertGroupService service;

    /**
     * The panel rest service
     * @param service the service
     */
    public ExpertGroupRestService(ExpertGroupService service) {
        this.service = service;
    }

    /* ================================================================
     * E X P E R T S  M G M T
     * ================================================================ */

    @POST
    @Path("/addExpertGroup")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public ExpertGroupError addExpertGroup(@FormParam("projectKey") String projectKey,
                                           @FormParam("name") String name,
                                           @FormParam("description") String description,
                                           @FormParam("members") String groupMembers) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Adding group:" + name + "< >" + groupMembers + "<");
        }
        name = name != null ? name.trim() : null;
        description = description != null ? description.trim() : null;
        groupMembers = groupMembers != null ? groupMembers.trim() : null;
        if(name == null || description == null || groupMembers == null ||
           "".equals(name) || "".equals(description) || "".equals(groupMembers)) {
            return new ExpertGroupError((String)null);
        }
        try {
            ExpertGroup eg = new ExpertGroup(0, name, description, projectKey, groupMembers);
            service.add(eg);
            return new ExpertGroupError();
        } catch(BadUsersException e) {
            return new ExpertGroupError(e.getWrongUsers());
        } catch (DuplicateExpertGroupException e) {
            return new ExpertGroupError(e.getDuplicateName());
        }
    }
}

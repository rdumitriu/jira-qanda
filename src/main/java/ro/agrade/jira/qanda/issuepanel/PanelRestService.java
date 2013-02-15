/*
 * Created on 1/21/13
 */
package ro.agrade.jira.qanda.issuepanel;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import ro.agrade.jira.qanda.QandAService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Our rest service for the panel
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
@Path("/panel")
public class PanelRestService {
    private static final Log LOG = LogFactory.getLog(PanelRestService.class);

    private QandAService service;

    /**
     * The panel rest service
     * @param service the service
     */
    public PanelRestService(QandAService service) {
        this.service = service;
    }

    /* ================================================================
     * Q U E S T I O N  M G M T
     * ================================================================ */

    @POST
    @Path("/addquestion")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean addQuestion(@FormParam("issueKey") String issueKey,
                               @FormParam("question") String question) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Adding question:" + issueKey + "< >" + question + "<");
        }
        if(issueKey == null || question == null) {
            return false;
        }
        service.addQuestion(issueKey, question);
        return true;
    }

    @POST
    @Path("/questiontext")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public String getQuestionText(@FormParam("questionId") String qId) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Getting question text:" + qId);
        }
        if(qId == null) {
            return null;
        }
        try {
            return service.getQuestionText(Long.parseLong(qId));
        } catch (NumberFormatException e) {
            LOG.error("Question id >" + qId + "< doesn't seem exactly an id");
        }
        return null;
    }

    @POST
    @Path("/editquestion")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean editQuestion(@FormParam("questionId") String qId,
                                @FormParam("question") String question) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Editing question:" + qId + " >>" + question + "<<");
        }
        if(qId == null || question == null) {
            return false;
        }
        try {
            service.editQuestion(Long.parseLong(qId), question);
            return true;
        } catch (NumberFormatException e) {
            LOG.error("Question id >" + qId + "< doesn't seem exactly an id");
        }
        return false;
    }

    @POST
    @Path("/deletequestion")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean deleteQuestion(@FormParam("questionId") String qId) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Deleting question:" + qId);
        }
        if(qId == null) {
            return false;
        }
        try {
            service.deleteQuestion(Long.parseLong(qId));
            return true;
        } catch (NumberFormatException e) {
            LOG.error("Question id >" + qId + "< doesn't seem exactly an id");
        }
        return false;
    }

    /* ================================================================
     * A N S W E R  M G M T
     * ================================================================ */

    @POST
    @Path("/answertext")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public String getAnswerText(@FormParam("answerId") String answerId) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Getting answer text:" + answerId );
        }
        if(answerId == null) {
            return null;
        }
        try {
            return service.getAnswerText(Long.parseLong(answerId));
        } catch (NumberFormatException e) {
            LOG.error("Answer id >" + answerId + "< doesn't seem exactly an id");
        }
        return null;
    }


    @POST
    @Path("/addanswer")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean addAnswer(@FormParam("questionId") String questionId,
                             @FormParam("answer") String answer) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Adding answer:" + questionId + " >" + answer + "<");
        }
        if(questionId == null || answer == null) {
            return false;
        }
        try {
            service.addAnswer(Long.parseLong(questionId), answer);
            return true;
        } catch (NumberFormatException e) {
            LOG.error("Question id >" + questionId + "< doesn't seem to be an id");
        }
        return false;
    }

    @POST
    @Path("/editanswer")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean editAnswer(@FormParam("answerId") String answerId,
                              @FormParam("answer") String answer) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Edit answer:" + answerId + " >>" + answer + "<<");
        }
        if(answerId == null || answer == null) {
            return false;
        }
        try {
            service.editAnswer(Long.parseLong(answerId), answer);
            return true;
        } catch (NumberFormatException e) {
            LOG.error("Answer id >" + answerId + "< doesn't seem exactly an id");
        }
        return false;
    }

    @POST
    @Path("/deleteanswer")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean deleteAnswer(@FormParam("answerId") String answerId) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Delete answer:" + answerId );
        }
        if(answerId == null) {
            return false;
        }
        try {
            service.deleteAnswer(Long.parseLong(answerId));
            return true;
        } catch (NumberFormatException e) {
            LOG.error("Answer id >" + answerId + "< doesn't seem exactly an id");
        }
        return false;
    }

    @POST
    @Path("/setapproval")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean answerApproval(@FormParam("answerId") String answerId,
                                  @FormParam("approval") String approval) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Set approval for answer:" + answerId + " >>" + approval);
        }
        if(answerId == null || approval == null) {
            return false;
        }
        try {
            service.setAnswerApprovalFlag(Long.parseLong(answerId),
                                          Boolean.parseBoolean(approval));
            return true;
        } catch (NumberFormatException e) {
            LOG.error("Answer id >" + answerId + "< doesn't seem exactly an id");
        }
        return false;
    }

    /* ================================================================
     * I S S U E  O P S
     * ================================================================ */

    @POST
    @Path("/addtoissue")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.APPLICATION_JSON})
    public boolean addQuestionToIssue(@FormParam("questionId") String qId) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Commenting question:" + qId);
        }
        if(qId == null) {
            return false;
        }
        try {
            service.addQuestionToIssue(Long.parseLong(qId));
            return true;
        } catch (NumberFormatException e) {
            LOG.error("Question id >" + qId + "< doesn't seem exactly an id");
        }
        return false;
    }
 }

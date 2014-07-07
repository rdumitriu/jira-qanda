/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda.dao;

import java.util.*;

import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.*;

import ro.agrade.jira.qanda.*;
import ro.agrade.jira.qanda.utils.BaseUserAwareService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Answer data service implementation
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class AnswerDataServiceImpl extends BaseUserAwareService implements AnswerDataService  {
    private static final Log LOG = LogFactory.getLog(AnswerDataServiceImpl.class);
    private final GenericDelegator delegator;
    private static final int INPAGE = 950;

    /**
     * Constructor
     * @param authContext the auth context to be injected
     */
    public AnswerDataServiceImpl(JiraAuthenticationContext authContext) {
        super(authContext);
        this.delegator = GenericDelegator.getGenericDelegator("default");
    }

    public List<Answer> getAllAnswers() {
        try {
            List<GenericValue> vals = delegator.findAll(ENTITY);
            return fromGenericValue(vals);
        } catch(GenericEntityException e) {
            String msg = "Could not load all answers ?!?";
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    public void changeUser(Answer a, String newName) {
        try {
            GenericValue v = delegator.findByPrimaryKey(makePk(a.getAnswerId()));
            v.setString(USER_FIELD, newName);
            delegator.store(v);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not update answer %d ?!?", a.getAnswerId());
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Gets one single answer
     *
     * @param aid the answer id
     * @return the answer
     */
    @Override
    public Answer getAnswer(long aid) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ID_FIELD, aid);
            GenericValue val = delegator.findByPrimaryKey(delegator.makePK(ENTITY, map));
            return fromGenericValue(val);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not load answer %d ?!?", aid);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Gets all the answers for the issue in question
     *
     * @param qid the question id
     * @return the list of answers
     */
    @Override
    public List<Answer> getAnswersForQuestion(long qid) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(QUESTIONID_FIELD, qid);
            map.put(DELETED_FIELD, "N");
            List<GenericValue> list = delegator.findByAnd(ENTITY, map);
            return fromGenericValue(list);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not load question(%d) answers ?!?", qid);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Gets all the answers that are designated for a list of issues
     *
     * @param issueIds the issue ids
     * @return the list of answers
     */
    @Override
    public List<Answer> getAnswersForIssues(List<Long> issueIds) {
        try {
            List<Answer> results = new ArrayList<Answer>();
            int i = 0;
            while( i < issueIds.size() ) {
                //copy between i and i + page
                List<Long> subqids = new ArrayList<Long>();
                for(int ndx = i; ndx < i + INPAGE && ndx < issueIds.size(); ndx++) {
                    subqids.add(issueIds.get(ndx));
                }
                i += INPAGE;
                if(subqids.size() > 0) {
                    List<EntityCondition> conds = new ArrayList<EntityCondition>();
                    conds.add(new EntityExpr(ISSUEID_FIELD, EntityOperator.IN, subqids));
                    conds.add(new EntityExpr(DELETED_FIELD, EntityOperator.EQUALS, "N"));

                    List<GenericValue> list = delegator.findByAnd(ENTITY, conds);
                    results.addAll(fromGenericValue(list));
                }
            }
            return results;
        } catch(GenericEntityException e) {
            String msg = "Could not load answers ?!?";
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Gets all the answers for the issue
     *
     * @param issueId the issue id
     * @return the list of answers
     */
    @Override
    public List<Answer> getAnswersForIssue(long issueId) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ISSUEID_FIELD, issueId);
            map.put(DELETED_FIELD, "N");
            List<GenericValue> list = delegator.findByAnd(ENTITY, map);
            return fromGenericValue(list);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not load issue (%d) answers ?!?", issueId);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Add an answer
     *
     * @param qid the question id
     * @param issueId the issue id
     * @param answer the answer text
     */
    @Override
    public void addAnswer(long qid, long issueId, String answer) {
        try {
            Answer q = new Answer(delegator.getNextSeqId(ENTITY), qid, issueId, answer, getCurrentUser());
            delegator.create(toGenericValue(q));
        } catch(GenericEntityException e) {
            String msg = String.format("Could not create answer on question %d ?!?", qid);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Removes an answer
     *
     * @param aid the answer id
     * @param answerText the answer
     */
    @Override
    public void updateAnswer(long aid, String answerText) {
        try {
            GenericValue v = delegator.findByPrimaryKey(makePk(aid));
            v.setString(TEXT_FIELD, answerText);
            delegator.store(v);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not update answer %d ?!?", aid);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Removes an answer
     *
     * @param aid the answer id
     */
    @Override
    public void removeAnswer(long aid) {
        try {
            GenericValue v = delegator.findByPrimaryKey(makePk(aid));
            v.setString(DELETED_FIELD, "Y");
            delegator.store(v);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not remove answer %d ?!?", aid);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    /**
     * Updates the flag on the answer
     *
     * @param aid the answer
     * @param flg the flag
     * @return the answer, modified
     */
    @Override
    public Answer setAnswerAcceptedFlag(long aid, boolean flg) {
        try {
            GenericValue v = delegator.findByPrimaryKey(makePk(aid));
            v.setString(STATUS_FIELD, flg ? "Y" : "N");
            delegator.store(v);
            return fromGenericValue(v);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not update answer %d ?!?", aid);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    private GenericPK makePk(long aid) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID_FIELD, aid);
        return delegator.makePK(ENTITY, map);
    }

    private GenericValue toGenericValue(Answer a) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID_FIELD, a.getAnswerId());
        map.put(QUESTIONID_FIELD, a.getQuestionId());
        map.put(ISSUEID_FIELD, a.getIssueId());
        map.put(TEXT_FIELD, a.getAnswerText());
        map.put(TS_FIELD, a.getTimeStamp());
        map.put(USER_FIELD, a.getUser());
        map.put(STATUS_FIELD, a.isAccepted() ? "Y" : "N");
        map.put(DELETED_FIELD, "N");
        return delegator.makeValue(ENTITY, map);
    }

    private Answer fromGenericValue(GenericValue genval) {
        if(genval == null) {
            return null;
        }
        long id = (Long)genval.get(ID_FIELD);
        long qid = (Long)genval.get(QUESTIONID_FIELD);
        long issid = (Long)genval.get(ISSUEID_FIELD);
        String text = (String)genval.get(TEXT_FIELD);
        long timeStamp = (Long)genval.get(TS_FIELD);
        String user = (String)genval.get(USER_FIELD);
        String statusCode = (String)genval.get(STATUS_FIELD);

        return new Answer(id, qid, issid, text, user, timeStamp, "Y".equals(statusCode));
    }

    private List<Answer> fromGenericValue(List<GenericValue> l) {
        List<Answer> result = new ArrayList<Answer>();
        if(l == null) {
            return result;
        }
        for(GenericValue gv : l) {
            result.add(fromGenericValue(gv));
        }
        return result;
    }

    private static final String ENTITY = "QANDAA";
    private static final String ID_FIELD = "a_id";
    private static final String QUESTIONID_FIELD = "q_id";
    private static final String ISSUEID_FIELD = "a_issueid";
    private static final String TS_FIELD = "a_creationts";
    private static final String USER_FIELD = "a_user";
    private static final String STATUS_FIELD = "a_status";
    private static final String TEXT_FIELD = "a_text";
    private static final String DELETED_FIELD = "a_deleted";
}

/*
 * Created on 1/28/13
 */
package ro.agrade.jira.qanda.dao;

import java.util.*;

import com.atlassian.jira.security.JiraAuthenticationContext;
import org.ofbiz.core.entity.*;

import ro.agrade.jira.qanda.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Answer data service implementation
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class AnswerDataServiceImpl extends BaseDaoService implements AnswerDataService  {
    private static final Log LOG = LogFactory.getLog(AnswerDataServiceImpl.class);
    private final GenericDelegator delegator;

    public AnswerDataServiceImpl(JiraAuthenticationContext authContext) {
        super(authContext);
        this.delegator = GenericDelegator.getGenericDelegator("default");
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
     * Add an answer
     *
     * @param qid    the question id
     * @param answer the answer text
     */
    @Override
    public void addAnswer(long qid, String answer) {
        try {
            Answer q = new Answer(delegator.getNextSeqId(ENTITY), qid, answer, getActingUser());
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
        String text = (String)genval.get(TEXT_FIELD);
        long timeStamp = (Long)genval.get(TS_FIELD);
        String user = (String)genval.get(USER_FIELD);
        String statusCode = (String)genval.get(STATUS_FIELD);

        return new Answer(id, qid, text, user, timeStamp, "Y".equals(statusCode));
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
    private static final String TS_FIELD = "a_creationts";
    private static final String USER_FIELD = "a_user";
    private static final String STATUS_FIELD = "a_status";
    private static final String TEXT_FIELD = "a_text";
    private static final String DELETED_FIELD = "a_deleted";

}

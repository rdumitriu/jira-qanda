/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * Created on 6/8/13
 */
package ro.agrade.jira.qanda.dao;

import java.util.*;

import org.ofbiz.core.entity.*;

import ro.agrade.jira.qanda.ExpertGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Experts, implementation
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class ExpertGroupDataServiceImpl implements ExpertGroupDataService {
    private static final Log LOG = LogFactory.getLog(ExpertGroupDataServiceImpl.class);
    private final GenericDelegator delegator;

    public ExpertGroupDataServiceImpl() {
        this.delegator = GenericDelegator.getGenericDelegator("default");
    }

    @Override
    public ExpertGroup getExpertGroup(long groupId) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(ID_FIELD, groupId);
            GenericValue val = delegator.findByPrimaryKey(delegator.makePK(ENTITY, map));
            return fromGenericValue(val);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not load group id %d ?!?", groupId);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    @Override
    public ExpertGroup getExpertGroup(String name) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(NAME_FIELD, name);
            List<GenericValue> val = delegator.findByAnd(ENTITY, map);
            if(val != null && val.size() > 0) {
                return fromGenericValue(val.get(0));
            }
            return null;
        } catch(GenericEntityException e) {
            String msg = String.format("Could not load group named %s ?!?", name);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    @Override
    public List<ExpertGroup> getExpertGroupsForProject(String project) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(PROJECT_FIELD, project);
            List<GenericValue> val = delegator.findByAnd(ENTITY, map);
            return fromGenericValue(val);
        } catch(GenericEntityException e) {
            String msg = String.format("Could not load groups for project %s ?!?", project);
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    @Override
    public boolean add(ExpertGroup eg) {
        try {
            eg.setId(delegator.getNextSeqId(ENTITY));
            delegator.create(toGenericValue(eg));
            return true;
//        } catch(GenericDuplicateKeyException e) { //::TODO:: this does not work
//            return false;
        } catch(GenericEntityException e) {
            String msg = "Could not add Expert group ?!?";
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    @Override
    public void remove(long id) {
        try {
            delegator.removeByPrimaryKey(makePk(id));
        } catch(GenericEntityException e) {
            String msg = "Could not delete Expert group ?!?";
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    @Override
    public boolean update(ExpertGroup eg) {
        try {
            delegator.store(toGenericValue(eg));
            return true;
//        } catch(GenericDuplicateKeyException e) { //::TODO:: this does not work
//            return false;
        } catch(GenericEntityException e) {
            String msg = "Could not create Expert group ?!?";
            LOG.error(msg);
            throw new OfbizDataException(msg, e);
        }
    }

    private GenericPK makePk(long id) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID_FIELD, id);
        return delegator.makePK(ENTITY, map);
    }

    private GenericValue toGenericValue(ExpertGroup eg) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ID_FIELD, eg.getId());
        map.put(NAME_FIELD, eg.getName());
        map.put(DESCRIPTION_FIELD, eg.getDescription());
        map.put(PROJECT_FIELD, eg.getProject());
        map.put(USERLIST_FIELD, eg.getGroupMembersAsString());
        return delegator.makeValue(ENTITY, map);
    }

    private ExpertGroup fromGenericValue(GenericValue genval) {
        if(genval == null) {
            return null;
        }
        long id = (Long)genval.get(ID_FIELD);
        String name = (String)genval.get(NAME_FIELD);
        String desc = (String)genval.get(DESCRIPTION_FIELD);
        String proj = (String)genval.get(PROJECT_FIELD);
        String userlist = (String)genval.get(USERLIST_FIELD);


        return new ExpertGroup(id, name, desc, proj, userlist);
    }

    private List<ExpertGroup> fromGenericValue(List<GenericValue> l) {
        List<ExpertGroup> result = new ArrayList<ExpertGroup>();
        if(l == null) {
            return result;
        }
        for(GenericValue gv : l) {
            result.add(fromGenericValue(gv));
        }
        return result;
    }

    private static final String ENTITY = "QANDAE";
    private static final String ID_FIELD = "e_id";
    private static final String NAME_FIELD = "e_shortname";
    private static final String DESCRIPTION_FIELD = "e_description";
    private static final String PROJECT_FIELD = "e_project";
    private static final String USERLIST_FIELD = "e_userlist";
}

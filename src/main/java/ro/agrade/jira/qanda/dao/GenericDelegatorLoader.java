/*
 * Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
 * or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
 *
 * File: GenericDelegatorLoader.java
 */
package ro.agrade.jira.qanda.dao;

import java.util.*;
import java.io.*;

import org.ofbiz.core.config.GenericConfigException;
import org.ofbiz.core.config.ResourceHandler;
import org.ofbiz.core.entity.*;
import org.ofbiz.core.entity.config.EntityConfigUtil;
import org.ofbiz.core.entity.model.*;
import org.ofbiz.core.util.*;
import org.w3c.dom.*;

import ro.agrade.jira.qanda.utils.ResourceUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class for adding extra functionality to generic delegator. Given a generic
 * delegator name, it has a method that adds entities and groups from 2 xml
 * files to the generic delegator represented by the name.
 *
 * @author Radu Dumitriu (rdumitriu@gmail.com)
 * @since 1.0
 */
public class GenericDelegatorLoader {
	private static final
    Log LOG = LogFactory.getLog(GenericDelegatorLoader.class);

	private String delegatorName;
    private Class<?> callerClazz;

	/**
	 * Constructs a new instance of <code>GenericDelegatorLoader</code> with the
	 * specified delegator name.
	 * 
	 * @param delegatorName The name of the generic delegator.
     * @param caller the caller class
	 */
	public GenericDelegatorLoader(String delegatorName, Class<?> caller) {
	    this.delegatorName = delegatorName != null ? delegatorName : "default";
        callerClazz = caller != null ? caller : GenericDelegatorLoader.class;
	}

    /**
	 * Constructs a new instance of <code>GenericDelegatorLoader</code> with the
	 * specified delegator name.
	 *
	 * @param delegatorName The name of the generic delegator.
	 */
	public GenericDelegatorLoader(String delegatorName) {
        this(delegatorName, null);
    }

    /**
     * Default constructor, uses the 'default' delegator
     */
    public GenericDelegatorLoader() {
        this(null, null);
    }

	/**
	 * Load additional entitymodel and entitygroup xml files and join
	 * informations to the delegator represented by delegator name instance
	 * variable. It is used instead of declaring an entitymodel.xml and an
	 * entitygroup.xml files in entityengine.xml. 
	 * The declaration in entityengine.xml would be like: 
	 * <entity-model-reader name="main">
	 * 		<resource loader="..." location="..."/> 
	 * </entity-model-reader>
	 * <entity-group-reader name="main" loader="..." location="..." />
	 * 
	 * @param loaderEnt The loader name for the xml for entities(Ex: maincp).
	 *        Corresponds to loader from <entity-model-reader> tag.
	 * @param locationEnt The location for the xml file for entities(Ex:
	 *        com/keplerrominfo/jira/commons/ofbiz/entities/entitymodel.xml).
	 *        Corresponds to location from <entity-model-reader> tag.
	 * @param loaderGrp The loader name for the xml for groups(Ex: maincp).
	 *        Corresponds to loader from <entity-group-reader> tag.
	 * @param locationGrp The location for the xml file for groups(Ex:
	 *        com/keplerrominfo/jira/commons/ofbiz/entities/entitygroup.xml).
	 *        Corresponds to location from <entity-group-reader> tag
	 * @throws OfbizDataException if an error occurs when loading the 2 xml
	 *         files
	 */
	public void loadXMLFiles(String loaderEnt, final String locationEnt,
		                     String loaderGrp, final String locationGrp) throws OfbizDataException {
		GenericDelegator delegator =
			GenericDelegator.getGenericDelegator(delegatorName);
		if (delegator == null) {
			LOG.warn("Null delegator in loadXMLFiles() method.");
			return;
		}
		try {
			ModelReader modelR = delegator.getModelReader();

			ResourceHandler rhEntities =
                                    new ResourceHandler(
                                        EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME,
                                        loaderEnt,
                                        locationEnt) {
                @Override
                public InputStream getStream() throws GenericConfigException {
                    if(!locationEnt.startsWith("/")) {
                        return super.getStream();
                    }
                    try {
                        return ResourceUtils.getAsInputStream(locationEnt,
                                callerClazz);
                    } catch(Exception e) {
                        throw new GenericConfigException("I/O error", e);
                    }
                }
            };
			addEntitiesToEntityCache(modelR, rhEntities, loaderEnt, locationEnt);

			ModelGroupReader modelGroupR = delegator.getModelGroupReader();
			ResourceHandler rhEntityGroup =
                                    new ResourceHandler(
                                        EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME,
                                        loaderGrp,
                                        locationGrp) {
                @Override
                public InputStream getStream() throws GenericConfigException {
                    if(!locationGrp.startsWith("/")) {
                        return super.getStream();
                    }
                    try {
                        return ResourceUtils.getAsInputStream(locationGrp,
                                                              callerClazz);
                    } catch(Exception e) {
                        throw new GenericConfigException("I/O error", e);
                    }
                }
            };
			addGroupsToGroupCache(modelGroupR, rhEntityGroup);

			initializeHelpersAndDatasourceCheck();

		} catch (Exception ex) {
			String msg  = "GOT exception ";
			LOG.error(msg, ex);
			throw new OfbizDataException(msg, ex);
		}
	}

	/**
	 * Add the associations "entity name"-"group name" from the xml file
	 * represented by the ResourceHandler rh to the group cache from the model
	 * reader modelGroupR. The code is a part from getGroupCache() method from
	 * ModelGroupReader class.
	 * 
	 * @param modelGroupReader The model group reader.
	 * @param resourceHandler The resource handler for entity groups associations.
	 * @throws OfbizDataException if an error occurs when adding groups to
	 * group cache
	 */
	private void addGroupsToGroupCache(ModelGroupReader modelGroupReader,
		                               ResourceHandler resourceHandler)
                                                    throws OfbizDataException {
		if(modelGroupReader == null || resourceHandler == null) {
			LOG.warn(String.format("Null timereport group reader or resource " +
					               "handler in addGroupsToGroupCache() method." +
					               " Model reader: %s resource handler: %s",
					               modelGroupReader, resourceHandler));
			return;
		}
		try {
			Map<?, ?> groupCache =  modelGroupReader.getGroupCache();
            Document document;

			synchronized (ModelGroupReader.class) {

				try {
					document = resourceHandler.getDocument();
				} catch (GenericConfigException e) {
					String msg = "Error loading entity group timereport";
					LOG.error(msg, e);
					throw new OfbizDataException(msg, e);
				}
				if (document == null) {
					String msg = String.format("Could not get document for %s",
                                               resourceHandler);
					LOG.error(msg);
					throw new OfbizDataException(msg);
				}

				Element docElement = document.getDocumentElement();

				if (docElement == null) {
                    String msg = "NULL doc element.";
					LOG.error(msg);
					throw new OfbizDataException(msg);
				}
				docElement.normalize();
				Node curChild = docElement.getFirstChild();

				if (curChild != null) {
					do {
						if (curChild.getNodeType() == Node.ELEMENT_NODE &&
							"entity-group".equals(curChild.getNodeName())) {
							Element curEntity = (Element) curChild;
							String entityName =
                                    UtilXml.checkEmpty(curEntity.getAttribute("entity"));
							String groupName =
                                    UtilXml.checkEmpty(curEntity.getAttribute("group"));

							if(groupName == null ||
							   entityName == null) {
							    continue;
                            }
                            safelyMapAdd(groupCache, entityName, groupName);
						}
					} while ((curChild = curChild.getNextSibling()) != null);
				} else {
					LOG.warn("[addGroupsToGroupCache()] No child nodes found.");
				}
			}
		} catch (Exception ex) {
			String msg = String.format("Got exception when adding groups " +
					                   "from resource handler: %s", resourceHandler);
			LOG.error(msg, ex);
			throw new OfbizDataException(msg, ex);
		}
	}

	/**
	 * Add the model entities from the xml file represented by the
	 * ResourceHandler rhEntity to the entity cache from the model reader
	 * modelR. The code is(with little changes) a part from getEntityCache()
	 * method in ModelReader class.
	 * 
	 * @param modelReader The model reader.
	 * @param resourceHandler The resource handler.
	 * @param loaderEnt The loader name for the xml for entities.
	 * @param locationEnt The location for the xml file for entities.
	 * @throws OfbizDataException if an error occurs when adding entities to
	 * entity cache
	 */
	private void addEntitiesToEntityCache(ModelReader modelReader,
		                                  ResourceHandler resourceHandler,
                                          String loaderEnt,
                                          String locationEnt) throws OfbizDataException {
		if (modelReader == null || resourceHandler == null) {
			LOG.warn(String.format("Null reader or resource handler" +
					               " in addEntitiesToEntityCache() method. Model" +
                                   " reader: %s Resource handler: %s",
                                   modelReader, resourceHandler));
			return;
		}
		try {
            Document document;
			Map<String, ModelEntity> entityCache = modelReader.getEntityCache();
            List<ModelViewEntity> tempViewEntityList
                                            = new LinkedList<ModelViewEntity>();
            Hashtable<String, String> docElementValues
                                            = new Hashtable<String, String>();

			synchronized (ModelReader.class) {
				try {
					document = resourceHandler.getDocument();
				} catch (GenericConfigException e) {
                    String msg = "Error getting document from resource handler";
                    LOG.error(msg);
					throw new GenericEntityConfException(msg, e);
				}
				if(document == null) {
					String msg = String.format("Could not get document for %s",
                                               resourceHandler);
					LOG.error(msg);
					throw new OfbizDataException(msg);
				}

				Element docElement = document.getDocumentElement();

				if (docElement == null) {
					String msg = "NULL doc element.";
					LOG.error(msg);
					throw new OfbizDataException(msg);
				}
				docElement.normalize();
				Node curChild = docElement.getFirstChild();

				int i = 0;

				if (curChild != null) {
					do {
						boolean isEntity = "entity".equals(curChild.getNodeName());
						boolean isViewEntity = "view-entity".equals(curChild.getNodeName());

						if ((isEntity || isViewEntity) &&
							curChild.getNodeType() == Node.ELEMENT_NODE) {
							i++;
							Element curEntity = (Element) curChild;
							String entityName =
								UtilXml.checkEmpty(curEntity.getAttribute("entity-name"));

							// check to see if entity with same name has already
							// been read
							if (entityCache.containsKey(entityName)) {
                                if(LOG.isDebugEnabled()) {
                                    LOG.debug(String.format(
                                        "Entity %s is defined more than once, most " +
                                        "recent will overwrite previous definition",
                                        entityName));
                                }
							}

							// add entity to entityResourceHandlerMap map
							modelReader.addEntityToResourceHandler(entityName,
								                                   loaderEnt,
                                                                   locationEnt);

							ModelEntity entity;

							if (isEntity) {
								entity = createModelEntity(modelReader,
                                                           curEntity,
										                   docElement,
                                                           null,
                                                           docElementValues);
                                if(LOG.isDebugEnabled()){
                                    LOG.debug(String.format("[Entity]: # %d: %s",
                                                            i, entityName));
                                }
							} else {
								entity = createModelViewEntity(modelReader,
                                                               curEntity,
										                       docElement,
                                                               null,
                                                               docElementValues);
								// put the view entity in a list to get ready
								// for the second pass to populate fields...
								tempViewEntityList.add((ModelViewEntity)entity);
                                if(LOG.isDebugEnabled()){
                                    String msg = String.format("[ViewEntity]: # %d: %s",
                                                               i, entityName);
                                    LOG.debug(msg);
                                }
							}

							if (entity != null) {
                                safelyMapAdd(entityCache, entityName, entity);
							} else {
								LOG.warn(String.format("Could not create entity " +
                                                       "for entityName: %s",
										               entityName));
							}
						}
					} while ((curChild = curChild.getNextSibling()) != null);
				} else {
					LOG.warn("No child nodes found.");
				}

				modelReader.rebuildResourceHandlerEntities();

				// do a pass on all of the view entities now that all of the
				// entities have loaded and populate the fields
                for (ModelViewEntity me : tempViewEntityList) {
                    me.populateFields(entityCache);
                }
				LOG.debug("FINISHED LOADING ENTITIES.");
			}
		} catch (Exception ex) {
			String msg = String.format("Got exception when adding entities " +
					"from resource handler: %s", resourceHandler);
			LOG.error(msg, ex);
			throw new OfbizDataException(msg, ex);
		}
	}

    @SuppressWarnings("unchecked")
    private void safelyMapAdd(Map map, String name, Object entity) {
        map.put(name, entity);
    }

    /**
	 * For the delegator instance, initialize helpers by group and do the data
	 * source check. The code is a part from GenericDelegator(String
	 * delegatorName) constructor.
	 * @throws OfbizDataException if an error occurs
	 */
	private void initializeHelpersAndDatasourceCheck()
		throws OfbizDataException {
		GenericDelegator delegator =
			GenericDelegator.getGenericDelegator(delegatorName);
		if (delegator == null) {
			LOG.warn("Null delegator in initializeHelpersAndDatasourceCheck().");
			return;
		}
		// initialize helpers by group
		Iterator<?> groups =
			UtilMisc.toIterator(delegator.getModelGroupReader().getGroupNames());

		while (groups != null &&
			groups.hasNext()) {
			String groupName = (String)groups.next();
			String helperName = delegator.getGroupHelperName(groupName);
            if(LOG.isDebugEnabled()) {
                LOG.debug(String.format("Delegator %s initializing helper %s " +
                                        "for entity group %s ",
                                        delegator.getDelegatorName(),
                                        helperName,
                                        groupName));
            }
			TreeSet<String> helpersDone = new TreeSet<String>();

			if (helperName != null &&
				helperName.length() > 0) {
				// make sure each helper is only loaded once
				if (helpersDone.contains(helperName)) {
                    if(LOG.isDebugEnabled()) {
					    LOG.debug(String.format("Helper %s already initialized," +
						                        " not re-initializing.",
                                                helperName));
                    }
					continue;
				}
				helpersDone.add(helperName);
				// pre-load field type defs, the return value is ignored
				ModelFieldTypeReader.getModelFieldTypeReader(helperName);
				// get the helper and if configured, do the datasource check
				GenericHelper helper =
					GenericHelperFactory.getHelper(helperName);

				try {
					helper.checkDataSource(delegator
						.getModelEntityMapByGroup(groupName), null, true);
				} catch (GenericEntityException e) {
					LOG.warn(e);
				}
			}
		}
	}


	private ModelEntity createModelEntity(ModelReader modelR,
		                                  Element entityElement,
                                          Element docElement,
                                          UtilTimer utilTimer,
		                                  Hashtable<String, String> docElementValues) {
		if (entityElement == null) {
			return null;
        }

		return new ModelEntity(modelR, entityElement,
                               docElement, utilTimer,
				               docElementValues);
	}

	
	private ModelViewEntity createModelViewEntity(ModelReader modelR,
                                                  Element entityElement,
                                                  Element docElement,
                                                  UtilTimer utilTimer,
                                                  Hashtable<String, String> docElementValues) {
		if (entityElement == null) {
			return null;
        }
		return new ModelViewEntity(modelR, entityElement,
                                   docElement, utilTimer,
				                   docElementValues);
	}

}

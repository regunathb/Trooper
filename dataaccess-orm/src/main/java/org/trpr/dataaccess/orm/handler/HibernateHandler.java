/*
 * Copyright 2012-2015, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trpr.dataaccess.orm.handler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.trpr.dataaccess.RDBMSHandler;
import org.trpr.dataaccess.RDBMSIdentifier;
import org.trpr.platform.core.spi.logging.PerformanceMetricsLogger;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.IncorrectResultSizePersistenceException;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistenceHandler;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * The <code>HibernateHandler</code> is a sub-type of {@link RDBMSHandler} that uses Hibernate as the persistence framework.
 * Hibernate is used via the Spring {@link HibernateTemplate}.
 * 
 * This class is also instrumented to log performance metrics using the platform-core {@link PerformanceMetricsLogger}
 * 
 * @author Ashok Ayengar, Raja S, Regunath B
 * @version 1.0, 24/05/2012
 */
@ManagedResource(objectName = "spring.application:type=Trooper,application=Performance-Metrics,name=HibernateMetrics-", description = "Hibernate Performance Metrics Logger")
public class HibernateHandler extends RDBMSHandler {

	/**
	 * Hibernate Template to provide the hibernate API.
	 */
	private HibernateTemplate template;

	/**
	 * No arg constructor.
	 */
	public HibernateHandler(){
	}	
		
	/**
	 * Interface method implementation
	 * @see PersistenceHandler#findEntities(Criteria)
	 */
	@SuppressWarnings("unchecked")
	public Collection<PersistentEntity> findEntities(final Criteria criteria) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		
		if (criteria.getQueryType() == Criteria.NATIVE_QUERY) {
			return findObjectBySQLQuery(criteria);
		}
		Collection<PersistentEntity> results = new LinkedList<PersistentEntity>(); // create an empty list
		
		if (criteria.getMaxResults() > 0) {
			results = (Collection<PersistentEntity>) this.getTemplate().execute(new HibernateCallback(){
				public Object doInHibernate(Session session) throws HibernateException, SQLException { 
					Query query = null;
					if(Criteria.NAMED_QUERY == criteria.getQueryType()){
						query = session.getNamedQuery(criteria.getQuery()); 
					}else{
						query = session.createQuery(criteria.getQuery());
					}
					query.setFirstResult(criteria.getFirstResult());
					query.setMaxResults(criteria.getMaxResults());

					for(String paramKey : criteria.getParamsMap().keySet()) {
						if (criteria.getParamsMap().get(paramKey) instanceof Collection) {
							query.setParameterList(paramKey, (Collection)criteria.getParamsMap().get(paramKey));
						}
						else if (criteria.getParamsMap().get(paramKey) instanceof Object[]) {
							query.setParameterList(paramKey, (Object[])criteria.getParamsMap().get(paramKey));
						}
						else {
							query.setParameter(paramKey, criteria.getParamsMap().get(paramKey));
						}
					}
					List result = query.list();
					return result;
				}
			});
		} else {
			if(Criteria.NAMED_QUERY == criteria.getQueryType()){
				results = this.getTemplate().findByNamedQueryAndNamedParam(criteria.getQuery(), criteria.getParamNamesArray(), criteria.getParamValuesArray());
			} else {
				results =  this.getTemplate().findByNamedParam(criteria.getQuery(), criteria.getParamNamesArray(), criteria.getParamValuesArray());
			}
		}
		
		// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HibernateHandler.findEntities", criteria.toConciseString());
		
		return results;
	}

	/**
	 * Interface method implementation
	 * @see PersistenceHandler#findEntity(Criteria)
	 */
	public PersistentEntity findEntity(Criteria criteria) throws PersistenceException {
		Collection<PersistentEntity> results = this.findEntities(criteria);
		if (results.size() != 1) { // we expect to get only ONE result
			throw new IncorrectResultSizePersistenceException(1,results.size());
		}
		return (PersistentEntity)((List<PersistentEntity>)results).get(0);
	}

	/**
	 * Interface method implementation
	 * @see PersistenceHandler#findEntity(PersistentEntity)
	 */
	public PersistentEntity findEntity(PersistentEntity entity) throws PersistenceException {						
		if (entity.getIdentifier() != null) { // first preference is given to the identifier. Use the load criteria otherwise.
			// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
			this.performanceMetricsLogger.startPerformanceMetricsCapture();
			String message = entity.getEntityName() + ":" + entity.getIdentifier();
			this.getTemplate().load(entity, ((RDBMSIdentifier)entity.getIdentifier()).getObjectId());
			// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
			this.performanceMetricsLogger.logPerformanceMetrics("HibernateHandler.findObject", message);
			return entity;
		} else {
			return findEntity(entity.getCriteriaForLoad());
		}
	}	
	
	/**
	 * Interface method implementation
	 * @see PersistenceHandler#makePersistent(PersistentEntity)
	 */
	public PersistentEntity makePersistent(PersistentEntity entity) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		
		try {
			if(entity.getEntityName() != null) {
				template.saveOrUpdate(entity.getEntityName(), entity);
			} else {
				template.saveOrUpdate(entity);
			}
		}catch(DataIntegrityViolationException die){
			throw new org.trpr.platform.core.spi.persistence.DataIntegrityViolationException(
					"Data integrity violation for entity:id " + entity.getEntityName() + ":" + ((RDBMSIdentifier)entity.getIdentifier()).getObjectId(), die);
		} catch (DataAccessException de) {
			throw new PersistenceException(
					"Persistence failure for entity:id " + entity.getEntityName() + ":" + ((RDBMSIdentifier)entity.getIdentifier()).getObjectId(), de);
		} catch (Exception e) {
			throw new PersistenceException(
					"Unrecognized/Unhandled Exception while persisting entity:id " + entity.getEntityName() + ":" + ((RDBMSIdentifier)entity.getIdentifier()).getObjectId(), e);			
		} finally {
			template.clear();			
		}
		
		// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HibernateHandler.makePersistent", entity.getEntityName() + ":" + ((RDBMSIdentifier)entity.getIdentifier()).getObjectId());
		
		return entity;
	}

	/**
	 * Interface method implementation.
	 * @see PersistenceHandler#makeTransient(PersistentEntity)
	 */
	public void makeTransient(PersistentEntity entity) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		
		try {
			if(entity.getEntityName() != null) {
				template.delete(entity.getEntityName(), entity);
			} else {
				template.delete(entity);
			}
		} catch (DataAccessException e) {
			throw new PersistenceException(
					"Delete failure for entity:id " + entity.getEntityName() + ":" + ((RDBMSIdentifier)entity.getIdentifier()).getObjectId(), e);
		} catch (Exception e) {
			throw new PersistenceException(
					"Unrecognized/Unhandled Exception while deleting entity:id " + entity.getEntityName() + ":" + ((RDBMSIdentifier)entity.getIdentifier()).getObjectId(), e);			
		} finally {
			template.clear();			
		}
		
		// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HibernateHandler.makeTransient", entity.getEntityName() + ":" + ((RDBMSIdentifier)entity.getIdentifier()).getObjectId());		
	}

	/**
	 * Interface method implementation.
	 * @see PersistenceHandler#update(Criteria)
	 */
	public int update(Criteria criteria) throws PersistenceException {
		// signal performance metrics capture. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.startPerformanceMetricsCapture();
		int result = template.bulkUpdate(criteria.getQuery(),  criteria.getParamValuesArray());
		// log performance metrics captured. actual capture will happen only if it has been enabled via #startPerformanceMetricsLogging(). Default is off
		this.performanceMetricsLogger.logPerformanceMetrics("HibernateHandler.update", criteria.toConciseString());
		return result;		
	}
	
	/**
	 * Equals method implementation. Checks if the database URLs of the underlying session factory match
	 * @param persistenceHandler the PersistenceHandler to check for equals
	 * @return true if the specified PersistenceHandler equals this one
	 * @throws PersistenceException in case of SQL exceptions in accessing the handlers' properties
	 */
	public boolean equals(PersistenceHandler persistenceHandler) throws PersistenceException {
		DataSource currentDatasource = SessionFactoryUtils.getDataSource(this.template.getSessionFactory());
		DataSource otherDatasource = SessionFactoryUtils.getDataSource(((HibernateHandler) persistenceHandler).getTemplate()
				.getSessionFactory());
		try {
			return (currentDatasource.getConnection().getMetaData().getURL()
						.equalsIgnoreCase(otherDatasource.getConnection().getMetaData().getURL()));
		} catch (SQLException e) {
			throw new PersistenceException("Error evaluating PersistenceHandler#equals() : " + e.getMessage(), e);
		}
	}
	
	/** Start Spring DI style Setter/Getter methods */
	public void setTemplate(HibernateTemplate template) {
		this.template = template;
	}
	public HibernateTemplate getTemplate() {
		return this.template;
	}	
	public PerformanceMetricsLogger getPerformanceMetricsLogger() {
		return this.performanceMetricsLogger;
	}
	public void setPerformanceMetricsLogger(PerformanceMetricsLogger performanceMetricsLogger) {
		this.performanceMetricsLogger = performanceMetricsLogger;
	}	
	/** End Spring DI style Setter/Getter methods */
	
	/**
	 * Helper method to execute a native SQL query and return the results as a List of PersistentEntity instances.
	 * Note that this implementation doesnot support scalar values to be returned and instead expects all resultant data to be
	 * mapped to a PersistentEntity defined for the purpose. This approach helps to maintain consistency in the {@link #findEntities(Criteria)}
	 * interface implementation and also provide for standard Hibernate-to-Java entity type mapping semantics.
	 */
	@SuppressWarnings("unchecked")
	private List<PersistentEntity> findObjectBySQLQuery(final Criteria criteria) {		
		return (List<PersistentEntity>) this.getTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException { 
				SQLQuery sqlQueryObject = session.createSQLQuery(criteria.getQuery());				
				sqlQueryObject.addEntity(criteria.getManagedClass());
				sqlQueryObject.setFirstResult(criteria.getFirstResult());
				if(criteria.getMaxResults() > 0){
					sqlQueryObject.setMaxResults(criteria.getMaxResults());
				}								
				return sqlQueryObject.list();
			}
		});		
	}

}

package com.expanset.jersey.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.expanset.hk2.persistence.config.MultipleDatabasesPersistenceConfiguratorSettings;
import com.expanset.hk2.persistence.config.PersistenceConfigurator;
import com.expanset.hk2.persistence.config.PersistenceConfiguratorSettings;
import com.expanset.hk2.persistence.config.SingleDatabasePersistenceConfiguratorSettings;
import com.expanset.hk2.persistence.transactions.LocalTransactionsBinder;

/**
 * Registration of base services for support entity persistence.
 * Also it registers support of {@link javax.transaction.Transactional} annotation, 
 * supports injection of {@link javax.transaction.Transaction}.
 * <p>Configuration parameters:</p>
 * <ul>
 * <li>{@link com.expanset.jersey.persistence.PersistenceFeature#CONFIG_PREFIX}</li>
 * <li>{@link com.expanset.jersey.persistence.PersistenceFeature#CONFIG_PREFIXES_PROPERTY}</li>
 * <li>{@link com.expanset.jersey.persistence.PersistenceFeature#DB_BASE_PATH}</li>
 * </ul>
 */
public abstract class PersistenceFeature implements Feature {
	
	/**
	 * Property name for path substitution of database URL.
	 * <p>Example:</p>
	 * <pre> 
	 * db2.javax.persistence.jdbc.url=jdbc:h2:${basePath}/site2
	 * </pre> 
	 */
	public final static String DB_BASE_PATH_PROPERTY = "basePath";	
	
	/**
	 * {@link String} property defining the prefix of database settings.
	 * <p>Example ('db' prefix):</p>
	 * <pre>
	 * db.javax.persistence.jdbc.url=jdbc:h2:path/site
	 * db.javax.persistence.jdbc.user=user
	 * db.javax.persistence.jdbc.password=password
	 * </pre>
	 */
	public final static String CONFIG_PREFIX = PersistenceFeature.class.getName() + ".configPrefix";
	
	/**
	 * {@link String} property defining the configuration file property name with db prefix list.
	 * You may access connection to required database through injecting persistence context with
	 * {@link javax.inject.Named} annotation, where name it is one of settings prefixes.
	 * If you need to select desired database in runtime you may use
	 * {@link com.expanset.hk2.persistence.PersistenceSessionManager#beginSession(java.util.Map)} method to
	 * override name, tuned by {@link javax.inject.Named} annotation, by another name of database settings.   
	 * <p>Example below ('dbs' settings key).</p>
	 * <p>Configuration file:</p>
	 * <pre>
	 * dbs=db1,db2
	 * db1.javax.persistence.jdbc.url=jdbc:h2:path/db1
	 * db2.javax.persistence.jdbc.url=jdbc:h2:path/db2
	 * </pre>
	 * <p>Injections:</p>
	 * <pre>
	 * {@literal @}Named("db1")
	 * {@literal @}PersistenceContext
	 * EntityManager entityManager;
	 * </pre>
	 */
	public final static String CONFIG_PREFIXES_PROPERTY = PersistenceFeature.class.getName() + ".configPrefixesProperty";	

	/**
	 * {@link String} property defining the default prefix of database settings. This prefix will be used when 
	 * {@link javax.inject.Named} annotation does not exist.
	 */
	public final static String CONFIG_DEFAULT_PREFIX = PersistenceFeature.class.getName() + ".configDefaultPrefix";
		
	/**
	 * {@link String} property defining the path to databases. It can be substituted of database urls like 
	 * <code>db1.javax.persistence.jdbc.url=jdbc:h2:${basePath}/db1</code>
     */
	public final static String DB_BASE_PATH = PersistenceFeature.class.getName() + ".dbBasePath";	
	
	/**
	 * {@link String} property defining the usage of local transactions. You may use own transaction support like Atomikos.
	 */
	public final static String USE_LOCAL_TRANSACTIONS = PersistenceFeature.class.getName() + ".useLocalTransactions";
	
	@Override
	public boolean configure(FeatureContext context) {
		final Configuration config = context.getConfiguration();

		context.register(RequestPersistenceSessionManager.class);
		
		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				final Boolean useLocalTransactions = (Boolean)config.getProperty(USE_LOCAL_TRANSACTIONS);
				if(useLocalTransactions == null || useLocalTransactions.booleanValue()) {
					context.register(new LocalTransactionsBinder());
				}
				
				PersistenceConfiguratorSettings settings;
				
				final String configPrefix = (String)config.getProperty(CONFIG_PREFIX);
				if(StringUtils.isNotEmpty(configPrefix)) {
					settings = new SingleDatabasePersistenceConfiguratorSettings(configPrefix);
				} else {
					final String configPrefixesProperty = (String)config.getProperty(CONFIG_PREFIXES_PROPERTY);
					final String configDefaultPrefixProperty = (String)config.getProperty(CONFIG_DEFAULT_PREFIX);
					if(StringUtils.isNotEmpty(configPrefixesProperty)) {
						settings = new MultipleDatabasesPersistenceConfiguratorSettings(
								configPrefixesProperty, configDefaultPrefixProperty);
					} else {
						settings = new PersistenceConfiguratorSettings();
					}
				}
				
				final Map<String, String> commonProperties = new HashMap<>();
				fillCommonProperties(config, commonProperties);
				settings.setCommonProperties(commonProperties);
								
				bind(settings).to(PersistenceConfiguratorSettings.class);
				
				addActiveDescriptor(PersistenceConfigurator.class);
				addActiveDescriptor(RequestPersistenceSessionManager.class);
			}
		});
		
		return true;
	}
	
	protected void fillCommonProperties(Configuration config, Map<String, String> commonProperties) {
		// NOTE Override to setup custom properties.
		
		final String dbBasePath = (String)config.getProperty(DB_BASE_PATH);
		commonProperties.put(DB_BASE_PATH_PROPERTY, dbBasePath);		
	}
}

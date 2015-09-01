package com.expanset.jersey.persistence.jpa;

import java.util.Map;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

import org.apache.commons.lang3.StringUtils;

import com.expanset.hk2.persistence.jpa.JpaPersistenceBinder;
import com.expanset.jersey.persistence.PersistenceFeature;

/**
 * Using JPA persistence engine (Java Persistence API).
 * You may use Eclipselink, Hibernate or other JPA implementation.
 * <p>Read more about configure persistence environment here {@link PersistenceFeature}.</p>
 * <p>You should fill unitName in {@link javax.persistence.PersistenceContext} annotation.</p>  
 */
@ConstrainedTo(RuntimeType.SERVER)
public class JpaPersistenceFeature extends PersistenceFeature {
	
	/**
	 * {@link String} property defining the default unit name. Not required.
	 */
	public final static String DEFAULT_UNUT_NAME = PersistenceFeature.class.getName() + ".defaultUnitName";
	
	@Override
	public boolean configure(FeatureContext context) {
		super.configure(context);
		
		context.register(new JpaPersistenceBinder());
							
		return true;
	}
	
	@Override
	protected void fillCommonProperties(Configuration config, Map<String, String> commonProperties) {
		super.fillCommonProperties(config, commonProperties);

		final String unitName = (String)config.getProperty(DEFAULT_UNUT_NAME);
		if(StringUtils.isNotEmpty(unitName)) {
			commonProperties.put(JpaPersistenceBinder.DEFAULT_UNIT_NAME, unitName);
		}
	}
}

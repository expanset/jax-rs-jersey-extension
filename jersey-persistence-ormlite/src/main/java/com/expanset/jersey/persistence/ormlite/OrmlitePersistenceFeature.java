package com.expanset.jersey.persistence.ormlite;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

import com.expanset.hk2.persistence.ormlite.OrmlitePersistenceBinder;
import com.expanset.jersey.persistence.PersistenceFeature;

/**
 * Using ORMlite persistence engine.
 * <p>Read more about configure persistence environment here {@link PersistenceFeature}.</p>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class OrmlitePersistenceFeature extends PersistenceFeature {

	@Override
	public boolean configure(FeatureContext context) {
		super.configure(context);
		
		context.register(new OrmlitePersistenceBinder());
							
		return true;
	}
}

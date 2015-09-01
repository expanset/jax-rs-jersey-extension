package com.expanset.jersey.jackson;

import javax.ws.rs.core.FeatureContext;

/**
 * Using Jackson library for JSON requests and responses.
 * <p>You can register manually configured {@link com.fasterxml.jackson.databind.ObjectMapper} in the container of services</p>
 */
public class JacksonFeature extends org.glassfish.jersey.jackson.JacksonFeature {

	@Override
	public boolean configure(FeatureContext context) {
		super.configure(context);
		
		context.register(ObjectMapperProvider.class);

		return true;
	}
}

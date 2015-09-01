package com.expanset.jersey.jackson;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import com.expanset.jackson.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link com.fasterxml.jackson.databind.ObjectMapper} provider that supplies context information to resource
 * classes and other providers.
 */
@Service
@Provider
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
	
	/**
	 * {@link com.fasterxml.jackson.databind.ObjectMapper}. If the object isn't set up, it is created.
	 */
    @Inject
    @Optional
    protected ObjectMapper objectMapper;
	
	@Override
	public synchronized ObjectMapper getContext(Class<?> type) {
		if(objectMapper == null) {
			objectMapper = JacksonUtils.createObjectMapper();
		}
		
        return objectMapper;
	}
}

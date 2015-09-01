package com.expanset.jersey.validation;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import org.jvnet.hk2.annotations.Service;

import com.expanset.jackson.JacksonUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Output {@link com.expanset.jersey.validation.ValidationResult} as JSON.
 * <p>JSON example:</p>
 * <pre> 
 * {'fieldName': 'error description'} 
 * </pre>
 * <p>where 'fieldName' - property name.</p>
 */
@Service
@Produces(MediaType.APPLICATION_JSON)
public class ValidationResultMessageBodyWriter implements MessageBodyWriter<ValidationResult> {

	@Inject
    protected Providers providers;
    
	protected ObjectMapper mapper;
	
	private static final String ERROR_TEMPLATE = "%s.";
	
	private static final String ERROR_SEPARATOR = " ";
	
	@PostConstruct
	public void initialize() {
        final ContextResolver<ObjectMapper> contextResolver =
                providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE);
        if(contextResolver != null) {
            mapper = contextResolver.getContext(ObjectMapper.class);
        }
        if(mapper == null) {
        	mapper = JacksonUtils.createObjectMapper();
        }
	}
	
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    	return ValidationError.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(
    		ValidationResult validationResult, 
    		Class<?> type, 
    		Type genericType, 
    		Annotation[] annotations, 
    		MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(
    		ValidationResult validationResult, 
    		Class<?> type, 
    		Type genericType,
            Annotation[] annotations, 
            MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, 
            OutputStream outStream) 
            		throws IOException, WebApplicationException {
    	if (validationResult != null && !validationResult.isSuccess()) {
            final JsonFactory jsonFactory = new JsonFactory(mapper);
            final JsonGenerator generator = jsonFactory.createGenerator(outStream);
            try {
            	generator.writeStartObject();
            	for(String property : validationResult.getInvalidProperties()) {
                    generator.writeStringField(property, validationResult.getErrors(property, ERROR_TEMPLATE, ERROR_SEPARATOR));
            	}
                generator.writeEndObject();
            } finally {
                generator.close();
            }
        }
    }
}

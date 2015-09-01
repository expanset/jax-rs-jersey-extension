package com.expanset.jersey.mvc.mustache;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;

import com.github.mustachejava.Mustache;

/**
 * Implementation of {@link org.glassfish.jersey.server.mvc.spi.TemplateProcessor template processor} that 
 * support for Mustache template engine.
 */
public class MustacheViewProcessor extends AbstractTemplateProcessor<Mustache> {
	
	protected final MustacheTemplateObjectFactory factory;
		
	private final static String SUPPORTED_EXTENSIONS = "ftl";
	
	@Inject
	public MustacheViewProcessor(			
			Configuration webConfig, 
			ServletContext servletContext,
			ServiceLocator serviceLocator) {
		super(webConfig, servletContext, MustacheMvcFeature.SUFFIX, SUPPORTED_EXTENSIONS);
		
        this.factory = getTemplateObjectFactory(
        		serviceLocator, 
        		MustacheTemplateObjectFactory.class,
        		() -> serviceLocator.createAndInitialize(MustacheTemplateObjectFactory.class));
	}

    @Override
    public Mustache resolve(final String name, final MediaType mediaType) {
        try {
			return resolve(name, (Reader)null);
		} catch (Exception e) {
			throw new ContainerException(e);
		}
    }	
	
	@Override
	protected Mustache resolve(String templatePath, Reader reader)
			throws Exception {
		return factory.resolve(templatePath);
	}

	@Override
	public void writeTo(
			Mustache template, 
			Viewable viewable,
			MediaType mediaType, 
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream out) 
					throws IOException {
    	final Charset encoding = setContentType(mediaType, httpHeaders);
        
        factory.process(
        		template, 
        		viewable,
        		new OutputStreamWriter(out, encoding));
	}
}

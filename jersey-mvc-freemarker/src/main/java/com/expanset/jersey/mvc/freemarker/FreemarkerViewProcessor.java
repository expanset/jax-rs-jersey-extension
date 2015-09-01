package com.expanset.jersey.mvc.freemarker;

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

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Implementation of {@link org.glassfish.jersey.server.mvc.spi.TemplateProcessor template processor} that 
 * support for Freemarker template engine.
 */
public class FreemarkerViewProcessor extends AbstractTemplateProcessor<Template> {
	
	protected final FreemarkerTemplateObjectFactory factory;
		
	private final static String SUPPORTED_EXTENSIONS = "ftl";
	
	@Inject
	public FreemarkerViewProcessor(			
			Configuration webConfig, 
			ServletContext servletContext,
			ServiceLocator serviceLocator) {
		super(webConfig, servletContext, FreemarkerMvcFeature.SUFFIX, SUPPORTED_EXTENSIONS);
		
        this.factory = getTemplateObjectFactory(
        		serviceLocator, 
        		FreemarkerTemplateObjectFactory.class,
        		() -> serviceLocator.createAndInitialize(FreemarkerTemplateObjectFactory.class));
	}

    @Override
    public Template resolve(final String name, final MediaType mediaType) {
        try {
			return resolve(name, (Reader)null);
		} catch (Exception e) {
			throw new ContainerException(e);
		}
    }	
	
	@Override
	protected Template resolve(String templatePath, Reader reader)
			throws Exception {
		return factory.resolve(templatePath);
	}

	@Override
	public void writeTo(
			Template template, 
			Viewable viewable,
			MediaType mediaType, 
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream out) 
					throws IOException {
        try {
        	final Charset encoding = setContentType(mediaType, httpHeaders);
            
            factory.process(
            		template, 
            		viewable,
            		new OutputStreamWriter(out, encoding));
        } catch (TemplateException te) {
            throw new ContainerException(te);
        }		
	}
}

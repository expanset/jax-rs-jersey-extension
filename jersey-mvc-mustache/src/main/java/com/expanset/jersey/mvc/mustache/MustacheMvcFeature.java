package com.expanset.jersey.mvc.mustache;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.server.mvc.MvcFeature;

/**
 * Ability to use Mustache template engine (http://mustache.github.io/, Java implementation - https://github.com/spullara/mustache.java).
 * The class currently recognizes following properties:
 * <ul>
 * <li>{@link com.expanset.jersey.mvc.mustache.MustacheMvcFeature#TEMPLATE_BASE_PATH}</li>
 * <li>{@link com.expanset.jersey.mvc.mustache.MustacheMvcFeature#TEMPLATE_OBJECT_FACTORY}</li>
 * <li>{@link com.expanset.jersey.mvc.mustache.MustacheMvcFeature#ENCODING}</li>
 * </ul>
 * <p>Note, refreshing of Mustache layout templates is not supported due to Mustache implementation.</p>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class MustacheMvcFeature extends com.expanset.jersey.mvc.MvcFeature {
	
	/**
	 * Suffix for this template engine.
	 */
	public final static String SUFFIX = "mustache";

	/**
     * {@link String} property defining the base path to MVC templates.
     * <p>Value is relative to current {@link javax.servlet.ServletContext servlet context}.</p>
     * <p>There is no default value.</p>
	 */
    public final static String TEMPLATE_BASE_PATH = MvcFeature.TEMPLATE_BASE_PATH + "." + SUFFIX;
    
    /**
     * Property used to pass user-configured factory able to create template objects.
     * <p>Default value is {@link MustacheTemplateObjectFactory}.</p>
     */    
    public final static String TEMPLATE_OBJECT_FACTORY = MvcFeature.TEMPLATE_OBJECT_FACTORY + "." + SUFFIX;
    
    /**
     * Property defines output encoding produced by {@link org.glassfish.jersey.server.mvc.spi.TemplateProcessor}. The value
     * must be a valid encoding defined that can be passed to the {@link java.nio.charset.Charset#forName(String)} method.
     * <p>The default value is {@code UTF-8}.</p>
     */
    public final static String ENCODING = MvcFeature.ENCODING + "." + SUFFIX;

	@Override
	public boolean configure(FeatureContext context) {
		super.configure(context);
		
        context.property(MvcFeature.CACHE_TEMPLATES + "." + SUFFIX, false);
        
        context.register(MustacheViewProcessor.class);
        
        return true;
	}
}

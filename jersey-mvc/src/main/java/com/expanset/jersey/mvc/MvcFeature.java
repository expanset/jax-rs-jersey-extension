package com.expanset.jersey.mvc;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.expanset.jersey.mvc.templates.PopulateTemplateWithMethodInterceptor;
import com.expanset.jersey.mvc.templates.TemplatePopulatorService;

/**
 * Base feature for MVC implementations.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class MvcFeature implements Feature {
    
	/**
     * {@link String} property defining the path to global error page.
     * <p>There is no default value.</p>
	 */
    public final static String ERROR_PAGE = ViewableThrowableExceptionMapper.class.getName() + ".errorPage";	

	@Override
	public boolean configure(FeatureContext context) {
		final Configuration config = context.getConfiguration();
		
        if (!config.isRegistered(org.glassfish.jersey.server.mvc.MvcFeature.class)) {
            context.register(org.glassfish.jersey.server.mvc.MvcFeature.class);
        }
        
        final String errorPage = (String)config.getProperty(ERROR_PAGE);
        if(StringUtils.isNotEmpty(errorPage)) {
        	registerViewableThrowableExceptionMapper(context);
        }
        
        registerTemplatePopulator(context);
        registerTemplateCacheManager(context);
        
        return true;
	}

	protected void registerViewableThrowableExceptionMapper(FeatureContext context) {
		context.register(ViewableThrowableExceptionMapper.class);
	}

	protected void registerTemplatePopulator(FeatureContext context) {
		context.register(PopulateTemplateWithMethodInterceptor.class);
		
        context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				addActiveDescriptor(TemplatePopulatorService.class);
			}
		});
	}
	
	protected void registerTemplateCacheManager(FeatureContext context) {	
        context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				addActiveDescriptor(TemplateCacheManager.class);
			}
		});
	}	
}

package com.expanset.jersey.mvc.freemarker;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import org.apache.commons.lang.Validate;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.internal.TemplateHelper;
import org.jvnet.hk2.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.jersey.mvc.templates.TemplatePopulatorService;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModelException;

/**
 * Factory of the main objects for Freemarker template engine.
 */
public class FreemarkerTemplateObjectFactory {
	
	protected final Configuration freemarkerConfig;

	@Inject
	@Optional
	protected Provider<Locale> localeProvider;
	
	@Inject
	@Optional
	protected TemplatePopulatorService templatePopulatorService;
	
	private final static Logger log = LoggerFactory.getLogger(FreemarkerTemplateObjectFactory.class);
	
	/**
	 * @param webConfig Jersey configuration.
	 * @param servletContext Servlet context.
	 * @param freemarkerConfig Freemarker configuration. If configuration does not exit, default configuration has been created. 
	 */
	@Inject
	public FreemarkerTemplateObjectFactory(
			ServletContext servletContext,
			javax.ws.rs.core.Configuration webConfig,
			@Optional Configuration freemarkerConfig) {
		if(freemarkerConfig != null) {
			this.freemarkerConfig = freemarkerConfig;
		} else {
			this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_22);
			this.freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			
			log.trace("Use default config");
		}
		
		final Object templateBasePath = webConfig.getProperty(FreemarkerMvcFeature.TEMPLATE_BASE_PATH);
		if(templateBasePath != null) {
			log.trace("Template loading direcory {}", templateBasePath);
			
			this.freemarkerConfig.setServletContextForTemplateLoading(servletContext, templateBasePath.toString());
		}
		
		final Charset encoding = TemplateHelper.getTemplateOutputEncoding(webConfig, FreemarkerMvcFeature.SUFFIX);
		if(encoding != null) {
			this.freemarkerConfig.setDefaultEncoding(encoding.name());
		}
	}
	
	/**
	 * Search and load template. Language of localization of a template undertakes from the current request.
	 * @param templatePath Path to template.
	 * @return Loaded template to process.
	 * @throws Exception Load template error.
	 */
	public Template resolve(@Nonnull String templatePath) 
			throws Exception {
		Validate.notEmpty(templatePath, "templatePath");
		
		final Locale locale = localeProvider != null ? localeProvider.get() : null;
		return freemarkerConfig.getTemplate(templatePath, locale);
	}

	/**
	 * Template processing.
	 * @param template Template.
	 * @param viewable Template data.
	 * @param out Output stream for template processing result.
	 * @throws IOException Load template error.
	 * @throws TemplateException Process template error.
	 */
	public void process(
			@Nonnull Template template, 
			@Nonnull Viewable viewable, 
			@Nonnull OutputStreamWriter out)
					throws TemplateException, IOException {
		Validate.notNull(template, "template");
		Validate.notNull(viewable, "viewable");
		Validate.notNull(out, "out");
		
		final Environment env = template.createProcessingEnvironment(viewable.getModel(), out);
		setupEnvironment(env, template, viewable);
		env.process();
	}
	
	/**
	 * Setup template processing environment before template processing.
	 * @param env Template processing environment.
	 * @param template Template.
	 * @param viewable Template data.
	 * @throws TemplateModelException Setup environment error.
	 */
	protected void setupEnvironment(Environment env, Template template, Viewable viewable) 
			throws TemplateModelException {
		Validate.notNull(env, "env");
		Validate.notNull(template, "template");
		Validate.notNull(viewable, "viewable");
		
		final Locale locale = localeProvider != null ? localeProvider.get() : null;
		if(locale != null) {
			env.setLocale(locale);
		}

		if(templatePopulatorService != null) {
			final Map<String, Object> sharedData = new HashMap<>();
			final ObjectWrapper objectWrapper = env.getObjectWrapper();
			templatePopulatorService.populate(viewable, sharedData);
			
			for(Entry<String, Object> entry : sharedData.entrySet()) {
				env.setGlobalVariable(entry.getKey(), objectWrapper.wrap(entry.getValue()));
				
				if(entry.getValue() instanceof TimeZone) {
					env.setTimeZone((TimeZone)entry.getValue());
				}
			}
		}
	}
}

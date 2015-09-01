package com.expanset.jersey.mvc.mustache;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;
import org.jvnet.hk2.annotations.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.errors.ExceptionAdapter;
import com.expanset.jersey.mvc.TemplateCacheManager;
import com.expanset.jersey.mvc.templates.TemplatePopulatorService;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Factory of the main objects for Mustache template engine.
 */
public class MustacheTemplateObjectFactory {
	
	protected final MustacheFactory mustacheConfig;
	
	@Inject
	@Optional
	protected Provider<Locale> localeProvider;
	
	@Inject
	@Optional
	protected TemplatePopulatorService templatePopulatorService;
	
	@Inject
	protected TemplateCacheManager templateCacheManager;
	
	private final static Logger log = LoggerFactory.getLogger(MustacheTemplateObjectFactory.class);
	
	/**
	 * @param webConfig Jersey configuration.
	 * @param servletContext Servlet context.
	 * @param mustacheConfig Mustache configuration. If configuration does not exit, default configuration has been created. 
	 * @throws IOException Error.
	 */
	@Inject
	public MustacheTemplateObjectFactory(
			ServletContext servletContext,
			javax.ws.rs.core.Configuration webConfig,
			@Optional MustacheFactory mustacheConfig) throws IOException {
		if(mustacheConfig != null) {
			this.mustacheConfig = mustacheConfig;
		} else {
			log.trace("Use default config");
			
			this.mustacheConfig = new DefaultMustacheFactory(
					(resourceName) -> ExceptionAdapter.get(() -> templateCacheManager.getTemplateReader(MustacheMvcFeature.SUFFIX, resourceName))) {
				@Override
				public Mustache compile(String resourceName) {
					if(ExceptionAdapter.get(() -> templateCacheManager.isNeedReloadTemplate(MustacheMvcFeature.SUFFIX, resourceName))) {
						this.mustacheCache.remove(resourceName);
					}

					return super.compile(resourceName);
				}				
			};
		}		
	}
	
	/**
	 * Search and load template. Language of localization of a template undertakes from the current request.
	 * @param templatePath Path to template.
	 * @return Loaded template to process.
	 * @throws Exception Load template error.
	 */
	public Mustache resolve(@Nonnull String templatePath) 
			throws Exception {
		Validate.notEmpty(templatePath, "templatePath");

		return mustacheConfig.compile(templatePath);
	}

	/**
	 * Template processing.
	 * @param template Template.
	 * @param viewable Template data.
	 * @param out Output stream for template processing result.
	 * @throws IOException Load template error.
	 */
	public void process(
			@Nonnull Mustache template, 
			@Nonnull Viewable viewable, 
			@Nonnull OutputStreamWriter out)
					throws IOException {
		Validate.notNull(template, "template");
		Validate.notNull(viewable, "viewable");
		Validate.notNull(out, "out");
				
		final Map<String, Object> sharedData = new HashMap<>();
		setupEnvironment(sharedData, template, viewable);
		
		final Writer mustacheWriter = template.execute(
				out, new Object[] { viewable.getModel(), sharedData });
		mustacheWriter.flush();
	}
	
	/**
	 * Setup template processing environment before template processing.
	 * @param env Template processing environment.
	 * @param template Template.
	 * @param viewable Template data.
	 */
	protected void setupEnvironment(Map<String, Object> env, Mustache template, Viewable viewable) {
		Validate.notNull(env, "env");
		Validate.notNull(template, "template");
		Validate.notNull(viewable, "viewable");
		
		final Locale locale = localeProvider != null ? localeProvider.get() : Locale.getDefault();
		if(locale != null) {
			env.put("lang", StringUtils.isNotEmpty(locale.getLanguage()) ? locale.getLanguage().replace('_', '-') : StringUtils.EMPTY);
		} else {
			env.put("lang", StringUtils.EMPTY);
		}

		if(templatePopulatorService != null) {
			templatePopulatorService.populate(viewable, env);
			
			for(Entry<String, Object> entry : env.entrySet()) {
				env.put(entry.getKey(), entry.getValue());
			}
		}
	}
}

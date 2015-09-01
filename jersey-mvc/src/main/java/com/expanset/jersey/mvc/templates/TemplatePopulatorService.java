package com.expanset.jersey.mvc.templates;

import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.servlet.WebConfig;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Service to use in template processors to populate them with shared data.
 */
@Service
@Contract
public class TemplatePopulatorService {

	/**
	 * Name for named service of population template with shared data.
	 */
	public final static String GLOBAL_TEMPLATE_POPULATOR = "GlobalTemplatePopulator";	

	/**
	 * Variable name in template with information about servlet config of type {@link javax.servlet.ServletConfig}.
	 */
	public final static String SERVLET_CONFIG = "servletConfig";	
	
	/**
	 * Variable name in template with information about servlet context of type {@link javax.servlet.ServletContext}.
	 */
	public final static String SERVLET_CONTEXT = "servletContext";	

	/**
	 * Variable name in template with information about servlet request of type {@link javax.servlet.http.HttpServletRequest}.
	 */
	public final static String SERVLET_REQUEST = "servletRequest";			
	
	/**
	 * Variable name in template with information about request of type {@link ContainerRequestContext}.
	 */
	public final static String REQUEST = "request";	

	/**
	 * Variable name in template with information about request URL of type {@link javax.ws.rs.core.UriInfo}.
	 */
	public final static String URI_INFO = "uriInfo";
	
	/**
	 * Variable name in template with information about servlet context path.
	 */
	public final static String URI_PREFIX = "uriPrefix";		
	
	/**
	 * Variable name in template with resources of the program of type {@link ResourceBundle}.
	 */
	public final static String RESOURCE_BUNDLE_PROPERTY = "resources";

	/**
	 * Variable name in template with resources of the program of type {@link java.security.Principal}.
	 */
	public final static String PRINCIPAL_PROPERTY = "principal";

	/**
	 * Variable name in template with resources of the program of type {@link javax.ws.rs.core.SecurityContext}.
	 */
	public final static String SECURITY_CONTEXT_PROPERTY = "securityContext";

	/**
	 * Variable name in template with configuration of the program of type {@link org.apache.commons.configuration.Configuration}.
	 */
	public final static String CONFIGURATION_PROPERTY = "config";
	
	/**
	 * Property name for {@link PopulateTemplateWith} in request.
	 */
	public final static String TEMPLATE_POPULATOR_ANNOTATION_PROPERTY = TemplatePopulatorService.class.getName() + ".templatePopulator";
	
	@Inject
	protected ServiceLocator serviceLocator;

	@Inject
	protected Provider<WebConfig> webConfigProvider;	
	
	@Inject
	protected Provider<ContainerRequestContext> requestProvider;
	
	
	@Inject
	protected Provider<HttpServletRequest> servletRequestProvider;	
	
	@Inject
	@Optional
	protected Provider<ResourceBundle> resourceBundleProvider;	

	@Inject
	@Optional
	@Named(GLOBAL_TEMPLATE_POPULATOR)
	protected Provider<TemplatePopulator> globalTemplatePopulatorProvider;	
	
	@Inject
	@Optional
	protected Configuration config;		
	
	public void populate(@Nonnull Viewable viewable, @Nonnull Map<String, Object> model) {
		final WebConfig webConfig = webConfigProvider.get();
		final HttpServletRequest servletRequest = servletRequestProvider.get();
		final ContainerRequestContext request = requestProvider.get();
		
		model.put(REQUEST, request);
		model.put(CONFIGURATION_PROPERTY, config);
		model.put(SERVLET_CONFIG, webConfig.getServletConfig());
		model.put(SERVLET_CONTEXT, webConfig.getServletContext());
		model.put(SERVLET_REQUEST, servletRequestProvider.get());
		model.put(URI_INFO, request.getUriInfo());
		model.put(URI_PREFIX, servletRequest.getContextPath());
		model.put(SECURITY_CONTEXT_PROPERTY, request.getSecurityContext());
		model.put(PRINCIPAL_PROPERTY, request.getSecurityContext() != null ? request.getSecurityContext().getUserPrincipal() : null);
		model.put(RESOURCE_BUNDLE_PROPERTY, resourceBundleProvider.get());
		for(String propertyName : request.getPropertyNames()) {
			model.put(propertyName, request.getProperty(propertyName));
		}
		
		final TemplatePopulator globalTemplatePopulator = globalTemplatePopulatorProvider.get();
		if(globalTemplatePopulator != null) {
			globalTemplatePopulator.populate(viewable, model);
		}
		
		final PopulateTemplateWith annotation = 
				(PopulateTemplateWith)requestProvider.get().getProperty(TEMPLATE_POPULATOR_ANNOTATION_PROPERTY);
		if(annotation != null) {
			TemplatePopulator templatePopulator = null;
			if(StringUtils.isEmpty(annotation.name())) {
				templatePopulator = 
						serviceLocator.getService(annotation.value());
			} else {
				templatePopulator = 
						serviceLocator.getService(annotation.value(), annotation.name());
			}
			if(templatePopulator == null) {
				templatePopulator = 
						serviceLocator.createAndInitialize(annotation.value());
			}
			templatePopulator.populate(viewable, model);
		}
	}
}

package com.expanset.jersey.i18n;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.expanset.hk2.i18n.PropertyResourceBundleBinder;

/**
 * Ability to use {@link ResourceBundle} in template processing, validation messages etc. 
 * <p>Resources are loaded from the properties file. Reloading in case of file change is supported.</p>
 * <p>The class currently recognizes following properties:</p>
 * <ul>
 * <li>{@link com.expanset.jersey.i18n.I18nFeature#RESOURCE_BUNDLE}</li>
 * <li>{@link com.expanset.jersey.i18n.I18nFeature#RESOURCE_BUNDLE_ENCODING}</li>
 * <li>{@link com.expanset.jersey.i18n.I18nFeature#RESOURCE_BUNDLE_TTL}</li>
 * </ul>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class I18nFeature implements Feature {

	/**
     * {@link String} property defining the base path resource file (without language suffix and file extension).
     * <p>Value is relative to current {@link javax.servlet.ServletContext servlet context}.</p>
     * <p>There is no default value.</p>
	 */		
	public final static String RESOURCE_BUNDLE = I18nFeature.class.getName() + ".resourceBundle";

	/**
     * Property defines encoding of properties file.
     * <p>Default value is {@code UTF-8}.</p>
	 */		
	public final static String RESOURCE_BUNDLE_ENCODING = I18nFeature.class.getName() + ".resourceBundleEncoding";

	/**
	 * Default value for {@link com.expanset.jersey.i18n.I18nFeature#RESOURCE_BUNDLE_ENCODING}.
	 */
	public final static String RESOURCE_BUNDLE_ENCODING_DEFAULT = "utf-8";
	
	/**
     * Time (milliseconds) through which verification of the file on need of reset is allowed.
     * <p>Default value is 1 min.</p>
	 */		
	public final static String RESOURCE_BUNDLE_TTL = I18nFeature.class.getName() + ".resourceBundleTTL";
	
	/**
	 * Default value for {@link com.expanset.jersey.i18n.I18nFeature#RESOURCE_BUNDLE_TTL}.
	 */
	public final static long RESOURCE_BUNDLE_TTL_DEFAULT = 1000 * 60;

	/**
     * {@link String} property defines language name for locale.
     * <p>Default value loaded from HTTP header "Accept-Language".</p>
	 */			
	public final static String DEFAULT_LOCALE = I18nFeature.class + ".defaultLocale";
	
	/**
	 * {@link Boolean} property flags to use cookie as locale store.
	 */
	public final static String USE_LOCALE_COOKIE = I18nFeature.class + ".useCookie";
	
	/**
     * {@link String} property defines name of cookie with locale.
     * <p>Default value is {@code lang}.</p>
	 */				
	public final static String LOCALE_COOKIE_NAME = I18nFeature.class + ".localeCookieName";
	
	/**
	 * Default value for locale cookie name.
	 */
	public final static String LOCALE_COOKIE_NAME_DEFAULT = "lang";
		
	@Inject
	protected ServletContext servletContext;

	@Inject
	protected ServiceLocator serviceLocator;		
	
	@Override
	public boolean configure(FeatureContext context) {
		final Configuration config = context.getConfiguration();
		
		String fileName = (String)config.getProperty(I18nFeature.RESOURCE_BUNDLE);
		fileName = servletContext.getRealPath(fileName);
		
		String encoding = (String)config.getProperty(I18nFeature.RESOURCE_BUNDLE_ENCODING);
		if(encoding == null) {
			encoding = I18nFeature.RESOURCE_BUNDLE_ENCODING_DEFAULT;
		}
		
		Long timeToLive = (Long)config.getProperty(I18nFeature.RESOURCE_BUNDLE_TTL);
		if(timeToLive == null) {
			timeToLive = I18nFeature.RESOURCE_BUNDLE_TTL_DEFAULT;
		}
		
		registerResourceBundleBinder(context, fileName, timeToLive, encoding);
		
		final Boolean useLocaleCookie = 
				(Boolean)config.getProperty(USE_LOCALE_COOKIE);		
		if(useLocaleCookie != null && useLocaleCookie.booleanValue()) {
			registerRequestScopeLocaleFromCookie(context);
		} else {
			registerRequestScopeLocale(context);
		}

		return true;
	}
	
	protected void registerResourceBundleBinder(
			FeatureContext context, 
			String fullFileName,
			Long timeToLive, 
			String encoding) {
		context.register(new PropertyResourceBundleBinder(fullFileName, timeToLive, encoding) {
			@Override
			protected void bindLocaleManager() {			
				// Do nothing. Register locale manager in feature.
			}			
		});
	} 
	
	protected void registerRequestScopeLocale(FeatureContext context) {
		context.register(RequestScopeLocaleFilter.class);

		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				addActiveDescriptor(RequestScopeLocaleManager.class);
			}
		});
	}	
	
	protected void registerRequestScopeLocaleFromCookie(FeatureContext context) {
		context.register(RequestScopeLocaleFilter.class);

		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				addActiveDescriptor(RequestScopeLocaleFromCookieManager.class);
			}
		});
	}		
		
	@PreMatching
	@Priority(Priorities.AUTHENTICATION - 500)
	protected static class RequestScopeLocaleFilter implements ContainerRequestFilter {
		
		@Inject
		protected RequestScopeLocaleManager manager;
		
		@Override
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			manager.intializeCurrentRequest();
		}
	}	
}

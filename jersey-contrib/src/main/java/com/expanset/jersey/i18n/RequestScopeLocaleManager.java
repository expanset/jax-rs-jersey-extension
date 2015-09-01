package com.expanset.jersey.i18n;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.glassfish.jersey.process.internal.RequestScope;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import com.expanset.hk2.i18n.ThreadScopeLocaleManager;
import com.expanset.jersey.utils.RequestScopeUtils;

/**
 * {@link Locale} manager for web application.
 */
@Service
@Contract
public class RequestScopeLocaleManager extends ThreadScopeLocaleManager {

	public final static String LOCALE_PROPERTY = RequestScopeLocaleManager.class.getName() + ".locale";

	protected final Locale defaultLocale;
	
	@Inject
	protected Provider<ContainerRequestContext> requestProvider; 

	@Inject
	protected RequestScope requestScope;	
	
	@Inject
	public RequestScopeLocaleManager(Configuration webConfig) {
		final String defaultLocaleLanguage = 
				(String)webConfig.getProperty(I18nFeature.DEFAULT_LOCALE);
		this.defaultLocale = StringUtils.isNotEmpty(defaultLocaleLanguage) ? 
				new Locale(defaultLocaleLanguage) : null;
	}	
	
	public void intializeCurrentRequest() {
		beginScope(determineLocale());
	}
	
	@Override
	public AutoCloseable beginScope(@Nonnull Locale locale) {
		Validate.notNull(locale, "locale");
		
		if(RequestScopeUtils.isInRequestScope(requestScope)) {
			requestProvider.get().setProperty(LOCALE_PROPERTY, locale);
			
			return new AutoCloseable() {
				@Override
				public void close() 
						throws Exception {
					requestProvider.get().setProperty(LOCALE_PROPERTY, null);
				}
			};		
		}		
		
		return super.beginScope(locale);
	}	
	
	@Override
	public Locale getCurrentLocale() {
		if(RequestScopeUtils.isInRequestScope(requestScope)) {
			return (Locale)requestProvider.get().getProperty(LOCALE_PROPERTY);			
		}
		
		return super.getCurrentLocale();
	}
	
	protected Locale determineLocale() {
		final ContainerRequestContext request = requestProvider.get();
		
		Locale currentLocale = defaultLocale;
		if(currentLocale == null) {
			currentLocale = request.getLanguage();
		}
		if(currentLocale == null) {
			final MultivaluedMap<String, String> headers = request.getHeaders();
			final List<String> langs = headers.get(HttpHeaders.ACCEPT_LANGUAGE);
			if(langs != null && langs.size() > 0) {
				try {
					currentLocale = new Locale(langs.get(0));
				} catch (Throwable e) {	
					// Do nothing
				}
			}
		}
		
		return currentLocale != null ? currentLocale : Locale.getDefault();
	}
}

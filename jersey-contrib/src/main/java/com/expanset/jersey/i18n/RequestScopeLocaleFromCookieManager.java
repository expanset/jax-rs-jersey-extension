package com.expanset.jersey.i18n;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.RememberOptions;
import com.expanset.jersey.RememberOptionsInCookie;

/**
 * {@link Locale} manager for web application, uses {@link Locale} in the cookie.
 */
@Service
@Contract
public class RequestScopeLocaleFromCookieManager extends RequestScopeLocaleManager {
		
	protected final String localeCookieName;
	
	@Inject
	protected Provider<HttpServletResponse> responseProvider;
	
	private final static Logger log = LoggerFactory.getLogger(RequestScopeLocaleFromCookieManager.class);	
	
	@Inject
	public RequestScopeLocaleFromCookieManager(Configuration webConfig) {
		super(webConfig);
		
		String localeCookieName = (String)webConfig.getProperty(I18nFeature.LOCALE_COOKIE_NAME);
		if(StringUtils.isEmpty(localeCookieName)) {
			localeCookieName = I18nFeature.LOCALE_COOKIE_NAME_DEFAULT;	
		}
		
		this.localeCookieName = localeCookieName;
	}
		
	@Override
	public void saveLocale(@Nonnull Locale locale, @Nullable RememberOptions rememberOptions) {
		final javax.servlet.http.Cookie languageCookie = new javax.servlet.http.Cookie(
				localeCookieName, 
				locale.getLanguage());

		final RememberOptionsInCookie rememberSettingsInCookie = 
				RememberOptionsInCookie.convert(rememberOptions);		
		rememberSettingsInCookie.setupCookie(requestProvider.get(), languageCookie);
		languageCookie.setHttpOnly(true);
		
		responseProvider.get().addCookie(languageCookie);
	}

	@Override
	public void removeLocale(@Nullable RememberOptions rememberOptions) {
		final javax.servlet.http.Cookie languageCookie = new javax.servlet.http.Cookie(
				localeCookieName, 
				StringUtils.EMPTY);		

		final RememberOptionsInCookie rememberSettingsInCookie = 
				RememberOptionsInCookie.convert(rememberOptions);		
		rememberSettingsInCookie.setupCookie(requestProvider.get(), languageCookie);		
		languageCookie.setMaxAge(0);
		languageCookie.setSecure(false);
		languageCookie.setHttpOnly(false);

		responseProvider.get().addCookie(languageCookie);	
	}	
	
	@Override
	protected Locale determineLocale() {
		final ContainerRequestContext request = requestProvider.get();
		
		String localeValue = null; 
		final Cookie localeCookie = request.getCookies().get(localeCookieName);
		if(localeCookie != null) {
			localeValue = localeCookie.getValue();
		}
		
		Locale requestLocale = null;
		if(StringUtils.isNotEmpty(localeValue)) {
			try {
				requestLocale = new Locale(localeValue);
			} catch (Throwable e) {
				log.error("Invalid locale {}", localeValue, e);
			}
		}		
		
		if(requestLocale != null) {
			return requestLocale;
		}
		
		return super.determineLocale();
	}
}

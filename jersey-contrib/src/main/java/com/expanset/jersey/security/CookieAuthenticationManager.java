package com.expanset.jersey.security;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.PBEConfig;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.RandomSaltGenerator;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.RememberOptions;
import com.expanset.hk2.security.AbstractCredentials;
import com.expanset.hk2.security.AuthenicationResult;
import com.expanset.hk2.security.AuthenticationManager;
import com.expanset.hk2.security.AuthenticationService;
import com.expanset.hk2.security.TokenCredentials;
import com.expanset.jersey.RememberOptionsInCookie;

/**
 * Encrypted cookie authentication service.
 */
@Service
@Contract
public class CookieAuthenticationManager implements AuthenticationManager {

	@Inject
	protected Provider<AuthenticationService> authenticationServiceProvider;

	@Inject
	protected Provider<ContainerRequestContext> requestProvider;

	@Inject
	protected Provider<HttpServletResponse> responseProvider;
		
	protected final String cookieName;
	
	protected final StringEncryptor encryptor;

	private final static Logger log = LoggerFactory.getLogger(CookieAuthenticationManager.class);
	
	@Inject
	public CookieAuthenticationManager(Configuration webConfig) {
		String cookieName = (String)webConfig.getProperty(CookieAuthenticationFeature.COOKIE_NAME);
		if(StringUtils.isEmpty(cookieName)) {
			cookieName = CookieAuthenticationFeature.COOKIE_NAME_DEFAULT;
		}			
		
		StringEncryptor encryptor = 
				(StringEncryptor)webConfig.getProperty(CookieAuthenticationFeature.ENCRYPTOR);
		if(encryptor == null) {
			encryptor = createDefaultEncryptor(webConfig);
		}		
		
		this.cookieName = cookieName;
		this.encryptor = encryptor;
	}

	public void authenticateCurrentRequest() {
		final ContainerRequestContext request = requestProvider.get();
		final Cookie authCookie = request.getCookies().get(cookieName);
		final String authCookieValue = authCookie != null ? authCookie.getValue() : StringUtils.EMPTY;
		
		if(StringUtils.isEmpty(authCookieValue)) {
			return;
		}

		String[] parts;
		try {
			parts = StringUtils.split(encryptor.decrypt(authCookieValue), '\0');
		} catch(Throwable e) {
			log.error("Decrypt authentication cookie error", e);
			
			return;
		}		

		if(parts.length == 2) {
			Date tokenCreationDate;
			try {
				final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				tokenCreationDate = isoDateFormat.parse(parts[0]);
			} catch(Throwable e) {
				log.error("Parse data error, string: {}", parts[0]);

				return;
			}		
			
			final TokenCredentials credentials = new TokenCredentials(
					tokenCreationDate, 
					parts[1], 
					StringUtils.endsWithIgnoreCase("https", request.getUriInfo().getRequestUri().getScheme()));
			final Optional<AuthenicationResult> authenicationResult = 
					authenticationServiceProvider.get().authenticate(credentials);
			if(authenicationResult.isPresent()) {
				request.setSecurityContext(new DefaultSecurityContext(
						SecurityContext.FORM_AUTH,
						authenicationResult.get(), 
						credentials.isSecure()));
			}
		} else {
			log.error("Authentication cookie invalid format, parts: {}", parts.length);
		}
	}		
	
	@Override
	public void saveAuthentication(
			@Nonnull AbstractCredentials credentials, 
			@Nullable RememberOptions rememberOptions) {
		Validate.notNull(credentials, "credentials");
			
		final String token = credentials instanceof TokenCredentials ? 
				((TokenCredentials)credentials).getToken() : credentials.toString();
		final DateFormat isoDateFormat = 
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		final String nowDate = 
				isoDateFormat.format(new Date());

		final javax.servlet.http.Cookie authCookie = new javax.servlet.http.Cookie(
				cookieName, 
				encryptor.encrypt(nowDate + "\0" + token));

		final RememberOptionsInCookie rememberSettingsInCookie = 
				RememberOptionsInCookie.convert(rememberOptions);		
		rememberSettingsInCookie.setupCookie(requestProvider.get(), authCookie);
		authCookie.setHttpOnly(true);
		
		responseProvider.get().addCookie(authCookie);
	}

	@Override
	public void removeAuthentication(@Nullable RememberOptions rememberOptions) {
		final javax.servlet.http.Cookie authCookie = new javax.servlet.http.Cookie(
				cookieName, 
				StringUtils.EMPTY);		

		final RememberOptionsInCookie rememberSettingsInCookie = 
				RememberOptionsInCookie.convert(rememberOptions);		
		rememberSettingsInCookie.setupCookie(requestProvider.get(), authCookie);		
		authCookie.setMaxAge(0);
		authCookie.setSecure(false);
		authCookie.setHttpOnly(true);

		responseProvider.get().addCookie(authCookie);		
	}
	
	protected static StringEncryptor createDefaultEncryptor(final Configuration config) {
		StringEncryptor encryptor;
		log.trace("Use default encryptor");
		
		final StandardPBEStringEncryptor defaultEncryptor = new StandardPBEStringEncryptor();
		PBEConfig encryptorConfig = (PBEConfig)config.getProperty(CookieAuthenticationFeature.ENCRYPTOR_CONFIG);
		if(encryptorConfig == null) {
			encryptorConfig = createDefaultEncryptorConfig(config);
		}

		String algorithm = (String)config.getProperty(CookieAuthenticationFeature.ENCRYPTOR_ALGORITHM);
		if(StringUtils.isEmpty(algorithm)) {
			algorithm = CookieAuthenticationFeature.ENCRYPTOR_ALGORITHM_DEFAULT;
		}
		if(StringUtils.isEmpty(encryptorConfig.getAlgorithm())) {
			defaultEncryptor.setAlgorithm(algorithm);
			
			log.trace("Default encryptor algorithm: {}", algorithm);
		}
		
		defaultEncryptor.setConfig(encryptorConfig);
		encryptor = defaultEncryptor;
		return encryptor;
	}

	protected static PBEConfig createDefaultEncryptorConfig(final Configuration config) {
		PBEConfig encryptorConfig;
		log.trace("Use default encryptor config");
		
		final SimpleStringPBEConfig defaultEncryptorConfig = new SimpleStringPBEConfig();
		defaultEncryptorConfig.setSaltGenerator(new RandomSaltGenerator());

		final String password = (String)config.getProperty(CookieAuthenticationFeature.ENCRYPTOR_PASSWORD);
		if(StringUtils.isEmpty(password)) {
			throw new IllegalStateException("Property CookieAuthenticationFeature.ENCRYPTOR_PASSWORD must be filled");
		}
		defaultEncryptorConfig.setPassword(password);
		encryptorConfig = defaultEncryptorConfig;
		return encryptorConfig;
	}	
}

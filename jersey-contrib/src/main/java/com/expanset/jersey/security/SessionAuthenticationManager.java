package com.expanset.jersey.security;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.RememberOptions;
import com.expanset.hk2.security.AbstractCredentials;
import com.expanset.hk2.security.AuthenicationResult;
import com.expanset.hk2.security.AuthenticationManager;
import com.expanset.hk2.security.AuthenticationService;

/**
 *  Session stored credentials authentication service.
 */
@Service
@Contract
public class SessionAuthenticationManager implements AuthenticationManager {

	@Inject
	protected Provider<ContainerRequestContext> requestProvider;	
	
	@Inject
	protected Provider<HttpSession> sessionProvider;
	
	@Inject
	protected Provider<AuthenticationService> authenticationServiceProvider;	
	
	protected final String sessionKey;
	
	@Inject
	public SessionAuthenticationManager(Configuration webConfig) {
		String sessionKey = (String)webConfig.getProperty(SessionAuthenticationFeature.SESSION_KEY);
		if(StringUtils.isEmpty(sessionKey)) {
			sessionKey = SessionAuthenticationFeature.SESSION_KEY_DEFAULT;
		}		
				
		this.sessionKey = sessionKey;
	}

	@Override
	public void saveAuthentication(
			@Nonnull AbstractCredentials credentials, 
			@Nullable RememberOptions rememberOptions) {
		Validate.notNull(credentials, "credentials");
		
		sessionProvider.get().setAttribute(sessionKey, credentials);
	}
	
	@Override
	public void removeAuthentication(@Nullable RememberOptions rememberOptions) {
		sessionProvider.get().removeAttribute(sessionKey);
	}
	
	public void authenticateCurrentRequest() {
		final ContainerRequestContext request = requestProvider.get();
		final AbstractCredentials credentials = 
				(AbstractCredentials)sessionProvider.get().getAttribute(sessionKey);
		if(credentials != null) {
			final Optional<AuthenicationResult> authenicationResult = 
					authenticationServiceProvider.get().authenticate(credentials);
			if(authenicationResult.isPresent()) {
				request.setSecurityContext(new DefaultSecurityContext(
						SecurityContext.FORM_AUTH,
						authenicationResult.get(), 
						credentials.isSecure()));
			}
		}
	}
}

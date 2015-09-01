package com.expanset.jersey.security;

import java.security.Principal;

import javax.annotation.Nonnull;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.Validate;

import com.expanset.hk2.security.AuthenicationResult;

/**
 * Default implementation of {@link SecurityContext}.
 */
public class DefaultSecurityContext implements SecurityContext {

	protected final String authenticationScheme;
	
	protected final AuthenicationResult authenicationResult;
			
	protected final boolean secure;

	/**
	 * @param authenticationScheme Authentication scheme, see {@link SecurityContext}.
	 * @param authenicationResult Result of authentication process.
	 * @param secure Credentials is received via the protected channel.
	 */
	public DefaultSecurityContext(
			@Nonnull String authenticationScheme, 
			@Nonnull AuthenicationResult authenicationResult, 
			boolean secure) {
		Validate.notNull(authenticationScheme, "authenticationScheme");
		Validate.notNull(authenicationResult, "authenicationResult");

		this.authenticationScheme = authenticationScheme;
		this.authenicationResult = authenicationResult;
		this.secure = secure;
	}
	
	@Override
	public Principal getUserPrincipal() {
		return authenicationResult.getPrincipal();
	}

	@Override
	public boolean isUserInRole(String role) {
		final Boolean result = authenicationResult.getUserInRoleCallback().apply(role);
		if(result != null) {
			return result.booleanValue();
		}
		return false;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public String getAuthenticationScheme() {
		return authenticationScheme;
	}
}

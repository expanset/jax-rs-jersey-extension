package com.expanset.jersey.security;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
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
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

/**
 * Support of authentication through browser cookie with encrypted user token.
 * <p>
 * You may inject {@link CookieAuthenticationManager} to setup authentication cookie through
 * {@link CookieAuthenticationManager#saveAuthentication(com.expanset.hk2.security.AbstractCredentials)}
 * or remove cookie after logout with
 * {@link CookieAuthenticationManager#removeAuthentication()}
 * </p>
 * <p>Configuration parameters:</p>
 * <ul>
 * <li>{@link com.expanset.jersey.security.CookieAuthenticationFeature#COOKIE_NAME}</li>
 * <li>{@link com.expanset.jersey.security.CookieAuthenticationFeature#ENCRYPTOR}</li>
 * <li>{@link com.expanset.jersey.security.CookieAuthenticationFeature#ENCRYPTOR_CONFIG}</li>
 * <li>{@link com.expanset.jersey.security.CookieAuthenticationFeature#ENCRYPTOR_ALGORITHM}</li>
 * <li>{@link com.expanset.jersey.security.CookieAuthenticationFeature#ENCRYPTOR_PASSWORD}</li>
 * </ul>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class CookieAuthenticationFeature implements Feature {

	/**
     * {@link String} property defining the name of browser cookie with encrypted user token.
     * <p>Default value is {@code AUTH}.</p>
	 */		
	public final static String COOKIE_NAME = CookieAuthenticationFeature.class.getName() + ".cookieName";

	/**
	 * Default cookie name.
	 */
	public final static String COOKIE_NAME_DEFAULT = "AUTH";
	
	/**
     * {@link org.jasypt.encryption.StringEncryptor} property with configured encryption service.
     * <p>If not filled it is created automatically.</p>
	 */		
	public final static String ENCRYPTOR = CookieAuthenticationFeature.class.getName() + ".encryptor";

	/**
     * {@link org.jasypt.encryption.pbe.config.PBEConfig} property with configuration for encryption service.
     * <p>If not filled it is created automatically.</p>
	 */		
	public final static String ENCRYPTOR_CONFIG = CookieAuthenticationFeature.class.getName() + ".encryptorConfig";

	/**
     * {@link String} property defining the algorithm that will be used for token encryption.
     * <p>Default value is {@code PBEWITHSHA1ANDDESEDE}.</p>
	 */		
	public final static String ENCRYPTOR_ALGORITHM = CookieAuthenticationFeature.class.getName() + ".encryptorAlgorithm";
	
	/**
	 * Default value of algorithm for token encryption.
	 */
	public final static String ENCRYPTOR_ALGORITHM_DEFAULT = "PBEWITHSHA1ANDDESEDE";

	/**
	 * {@link String} property defining the password that will be used for token encryption.
	 * More secure to use access to password through {@link org.jasypt.encryption.pbe.config.EnvironmentPBEConfig}.
	 * Configured {@link org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig} may be filled by property
	 * {@link com.expanset.jersey.security.CookieAuthenticationFeature#ENCRYPTOR_CONFIG}.
	 * Need to set {@link com.expanset.jersey.security.CookieAuthenticationFeature#ENCRYPTOR_CONFIG} or 
	 * {@link com.expanset.jersey.security.CookieAuthenticationFeature#ENCRYPTOR_PASSWORD}.
	 */
	public static final String ENCRYPTOR_PASSWORD = CookieAuthenticationFeature.class.getName() + ".encryptorPassword";

	@Inject
	protected ServiceLocator serviceLocator;	

	@Override
	public boolean configure(FeatureContext context) {
		final Configuration config = context.getConfiguration();
		if(!config.isRegistered(RolesAllowedDynamicFeature.class)) {
			context.register(RolesAllowedDynamicFeature.class);
		}
		
		registerCookieAuthenticationManager(context);
		
		return true;
	}

	protected void registerCookieAuthenticationManager(FeatureContext context) {
		context.register(CookieAuthenticationFilter.class);	
		
		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				addActiveDescriptor(CookieAuthenticationManager.class);
			}
		});
	}
	
	@PreMatching
	@Priority(Priorities.AUTHENTICATION)
	protected static class CookieAuthenticationFilter implements ContainerRequestFilter {

		@Inject
		protected CookieAuthenticationManager service;
		
		@Override
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			service.authenticateCurrentRequest();
		}
	} 
}

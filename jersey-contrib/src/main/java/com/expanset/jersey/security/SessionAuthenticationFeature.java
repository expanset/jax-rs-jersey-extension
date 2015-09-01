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

import com.expanset.jersey.session.SessionFeature;

/**
 * Support of store authentication information in user session.
 * <p>
 * You may inject {@link SessionAuthenticationManager} to setup authentication cookie through
 * {@link SessionAuthenticationManager#saveAuthentication(com.expanset.hk2.security.AbstractCredentials)}
 * or remove cookie after logout with
 * {@link SessionAuthenticationManager#removeAuthentication}
 * </p>
 * <p>Configuration parameters:</p>
 * <ul>
 * <li>{@link com.expanset.jersey.security.SessionAuthenticationFeature#SESSION_KEY}</li>
 * </ul>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class SessionAuthenticationFeature implements Feature {

	/**
     * {@link String} property defining the key for {@link javax.ws.rs.core.SecurityContext} in session.
	 */		
	public final static String SESSION_KEY = SessionAuthenticationFeature.class.getName() + ".sessionKey";

	/**
	 * Default key name.
	 */
	public final static String SESSION_KEY_DEFAULT = SessionAuthenticationFeature.class.getName() + ".sessionKey";
		
	@Inject
	protected ServiceLocator serviceLocator;
	
	@Override
	public boolean configure(FeatureContext context) {
		final Configuration config = context.getConfiguration();
		if(!config.isRegistered(RolesAllowedDynamicFeature.class)) {
			context.register(RolesAllowedDynamicFeature.class);
		}
		if(config.isRegistered(SessionFeature.class)) {
			context.register(SessionFeature.class);
		}
		
		registerSessionAuthenticationManager(context);

		return true;
	}

	protected void registerSessionAuthenticationManager(FeatureContext context) {
		context.register(SessionAuthenticationFilter.class);
		
		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				addActiveDescriptor(SessionAuthenticationManager.class);
			}
		});	
	}
	
	@PreMatching
	@Priority(Priorities.AUTHENTICATION)
	protected static class SessionAuthenticationFilter implements ContainerRequestFilter {

		@Inject
		protected SessionAuthenticationManager service;
		
		@Override
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			service.authenticateCurrentRequest();
		}
	}
}

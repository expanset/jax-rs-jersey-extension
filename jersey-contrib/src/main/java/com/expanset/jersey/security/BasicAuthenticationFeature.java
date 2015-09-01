package com.expanset.jersey.security;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import com.expanset.hk2.security.AuthenicationResult;
import com.expanset.hk2.security.AuthenticationService;
import com.expanset.hk2.security.LoginPasswordCredentials;

/**
 * Basic authentication scheme.
 * <p>Configuration parameters:</p>
 * <ul>
 * <li>{@link com.expanset.jersey.security.BasicAuthenticationFeature#REALM}</li>
 * <li>{@link com.expanset.jersey.security.BasicAuthenticationFeature#ENCODING}</li>
 * </ul>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class BasicAuthenticationFeature implements Feature {

	/**
     * {@link String} property defining the name of the authentication realm used in basic authentication scheme.
	 */		
	public final static String REALM = BasicAuthenticationFeature.class.getName() + ".realm";
	
	/**
     * {@link String} property defining the encoding of user credentials.
     * <p>Default value is {@code UTF-8}.</p>
	 */		
	public final static String ENCODING = BasicAuthenticationFeature.class.getName() + ".encoding";

	/**
	 * Default value for authentication encoding.
	 */
	public final static String ENCODING_DEFAULT = "utf-8";

	@Inject
	protected ServiceLocator serviceLocator;
	
	@Override
	public boolean configure(FeatureContext context) {
		final Configuration config = context.getConfiguration();
		if(!config.isRegistered(RolesAllowedDynamicFeature.class)) {
			context.register(RolesAllowedDynamicFeature.class);
		}

		registerRequiredAuthenticationFeature(context);
		
		String realm = (String)config.getProperty(BasicAuthenticationFeature.REALM);
		if(realm == null) {
			realm = StringUtils.EMPTY;
		}				
		String encoding = (String)config.getProperty(BasicAuthenticationFeature.ENCODING);
		if(realm == null) {
			encoding = ENCODING_DEFAULT;
		}			
		
        context.register(createFilter(realm, encoding));
	
        return true;
	}

	protected void registerRequiredAuthenticationFeature(FeatureContext context) {
		context.register(BasicAuthenticationRequiredFeature.class);
	}

	protected ContainerRequestFilter createFilter(String realm, String encoding) {
		ContainerRequestFilter filter = new BasicAuthenticationFilter(realm, encoding);
		serviceLocator.inject(filter);
		return filter;
	}
	
	@PreMatching
	@Priority(Priorities.AUTHENTICATION)
	protected static class BasicAuthenticationFilter implements ContainerRequestFilter {

		@Inject
		protected Provider<AuthenticationService> authenticationServiceProvider;
		
		protected final String realm;
			
		protected final String encodng;
		
		private final static String BASIC_PREFIX = "Basic ";
		
		private final static String AUTH_HEADER = "Authorization";

		public BasicAuthenticationFilter(@Nonnull String realm, @Nonnull String encodng) {
			Validate.notNull(realm, "realm");
			Validate.notEmpty(encodng, "encodng");
			
			this.realm = realm;
			this.encodng = encodng;
		}
		
		@Override
		public void filter(final ContainerRequestContext requestContext) 
				throws IOException {
			String authorizationToken = requestContext.getHeaderString(AUTH_HEADER);
			if(!StringUtils.isEmpty(authorizationToken) && 
					StringUtils.startsWithIgnoreCase(authorizationToken, BASIC_PREFIX)) {
				authorizationToken = authorizationToken.substring(BASIC_PREFIX.length());
				final String[] credentialParts = 
						StringUtils.split(new String(Base64.getDecoder().decode(authorizationToken), encodng), ':');

				final LoginPasswordCredentials credentials = new LoginPasswordCredentials(
						credentialParts.length != 0 ? credentialParts[0] : null,
						credentialParts.length != 1 ? credentialParts[1] : null, 
						realm,
						StringUtils.endsWithIgnoreCase("https", requestContext.getUriInfo().getRequestUri().getScheme()));
				final Optional<AuthenicationResult> authenicationResult = 
						authenticationServiceProvider.get().authenticate(credentials);
				if(authenicationResult.isPresent()) {
					requestContext.setSecurityContext(new DefaultSecurityContext(
							SecurityContext.BASIC_AUTH,
							authenicationResult.get(), 
							credentials.isSecure()));
				}			
			}
		}
	}
	
	protected static class BasicAuthenticationRequiredFeature implements DynamicFeature {

		@Override
		public void configure(ResourceInfo resourceInfo, FeatureContext context) {
			final Configuration config = context.getConfiguration();
			
			String realm = (String)config.getProperty(BasicAuthenticationFeature.REALM);
			if(realm == null) {
				realm = StringUtils.EMPTY;
			}			
			
			final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
	        if (am.getAnnotation(RolesAllowed.class) != null 
	        		|| resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class) != null) {
	        	context.register(createFilter(realm));
	        }
		}

		protected BasicAuthenticationRequiredFilter createFilter(String realm) {
			return new BasicAuthenticationRequiredFilter(realm);
		}
	}	
	
	@Priority(Priorities.AUTHENTICATION + 1)
	protected static class BasicAuthenticationRequiredFilter  implements ContainerRequestFilter {

		protected final String realm;
		
		public BasicAuthenticationRequiredFilter(String realm) {
			this.realm = realm;
		}	
		
		@Override
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			if(requestContext.getSecurityContext() == null 
					|| requestContext.getSecurityContext().getUserPrincipal() == null) {
				requestContext.abortWith(Response.status(HttpServletResponse.SC_UNAUTHORIZED)
						.header("WWW-Authenticate", "Basic realm=\"" + realm +  "\"").build());
			}
		}
	}		
}

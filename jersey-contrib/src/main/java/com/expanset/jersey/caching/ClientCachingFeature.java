package com.expanset.jersey.caching;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Priorities;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.configuration.Configuration;
import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.jvnet.hk2.annotations.Optional;

/**
 * Ability to use a browser caching annotations for resource's methods.
 * <p>To use file based configuration you should register 
 * {@link org.apache.commons.configuration.Configuration} in the service locator.</p>
 * <p>Example:</p>
 * <pre>
 * {@literal @}ClientNoCache
 * public Viewable userCards() {
 * ....
 * 
 * {@literal @}ClientCacheConfig("cache.default")
 * public Object userAvatar() {
 * ....
 * 
 * {@literal @}ClientCacheMaxAge(time=10,unit=TimeUnit.MINUTES)
 * public Object userAvatar() {
 * ....
 * </pre>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class ClientCachingFeature implements DynamicFeature {

	/**
	 * Access to configuration in file if registered {@link org.apache.commons.configuration.Configuration}.
	 */
	@Inject
	@Optional
	protected Configuration config;		
	
	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
        
		ClientCacheMaxAge clientMaxAge = 
				resourceInfo.getResourceClass().getAnnotation(ClientCacheMaxAge.class);
		if(clientMaxAge == null) {
			clientMaxAge = am.getAnnotation(ClientCacheMaxAge.class);
		}
		
		ClientNoCache clientNoCache = 
				resourceInfo.getResourceClass().getAnnotation(ClientNoCache.class);
		if(clientNoCache == null) {
			clientNoCache = am.getAnnotation(ClientNoCache.class);
		}

		ClientCacheConfig clientCacheConfig = 
				resourceInfo.getResourceClass().getAnnotation(ClientCacheConfig.class);
		if(clientCacheConfig == null) {
			clientCacheConfig = am.getAnnotation(ClientCacheConfig.class);
		}
		
		if (clientMaxAge != null) {
			final CacheControl cacheControl = new CacheControl();
			cacheControl.setMaxAge((int)clientMaxAge.unit().toSeconds(clientMaxAge.time()));
			context.register(createFilter(resourceInfo, context, cacheControl));
		} else if (clientNoCache != null) {
			final CacheControl cacheControl = new CacheControl();
			cacheControl.setNoCache(true);
			cacheControl.setNoStore(true);
			context.register(createFilter(resourceInfo, context, cacheControl));
		} else if (clientCacheConfig != null) {
			if(config == null) {
				throw new IllegalStateException("To use externally configured cache settings you must register on org.apache.commons.configuration.Configuration");
			}
			context.register(createFilter(resourceInfo, context, clientCacheConfig.value(), clientCacheConfig.def()));
		} 		
	}
	
	protected ContainerResponseFilter createFilter(
			ResourceInfo resourceInfo, 
			FeatureContext context, 
			CacheControl cacheControl) {
		return new CacheResponseFilter(cacheControl);
	}

	protected ContainerResponseFilter createFilter(
			ResourceInfo resourceInfo, 
			FeatureContext context, 
			String key, 
			String defaultValue) {
		return new CacheConfigResponseFilter(config, key, defaultValue);
	}
	
	@Priority(Priorities.HEADER_DECORATOR)
	protected class CacheResponseFilter implements ContainerResponseFilter {
		
		protected final CacheControl headerValue;		

		CacheResponseFilter(CacheControl headerValue) {
			assert headerValue != null;
			
			this.headerValue = headerValue;
		}

		@Override
		public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
			responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, headerValue);
		}
	}
	
	@Priority(Priorities.HEADER_DECORATOR)
	protected class CacheConfigResponseFilter implements ContainerResponseFilter {

		protected final Configuration config;		
		
		protected final String key;
		
		protected final String defaultValue;
	
		public CacheConfigResponseFilter(Configuration config, String key, String defaultValue) {
			this.config = config;
			this.key = key;
			this.defaultValue = defaultValue;
		}

		@Override
		public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
			responseContext.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, config.getString(key, defaultValue));
		}
	}
}

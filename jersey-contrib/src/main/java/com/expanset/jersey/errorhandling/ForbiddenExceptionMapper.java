package com.expanset.jersey.errorhandling;

import java.net.URI;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.errors.ExceptionAdapter;
import com.expanset.jersey.utils.HttpUtils;

/**
 * Exception mapper for 403 error. Redirects to login page.
 */
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException>  {

	/**
     * {@link Class} property defining the resource class with method to redirect.
     * <p>There is no default value.</p>
	 */
    public final static String REDIRECT_RESOURCE_CLASS = ForbiddenExceptionMapper.class.getName() + ".redirectResource";

	/**
     * {@link String} property defining the resource method to redirect.
     * <p>There is no default value.</p>
	 */
    public final static String REDIRECT_RESOURCE_METHOD = ForbiddenExceptionMapper.class.getName() + ".redirectResourceMethod";

	/**
     * Name of return URL quiery parameter.
	 */
    public final static String RETURN_URL = "return";
    
	@Inject
	protected Provider<ContainerRequestContext> requestProvider;

	@Inject
	protected Provider<UriInfo> uriInfoProvider;
	
	protected Class<?> resourceClass;
	
	protected String resourceMethod;
	
    private final static Logger log = LoggerFactory.getLogger(ForbiddenExceptionMapper.class);		

    @Inject
    public ForbiddenExceptionMapper(Configuration webConfig) {
		resourceClass = (Class<?>)webConfig.getProperty(REDIRECT_RESOURCE_CLASS);
		if(resourceClass == null) {
			throw new IllegalStateException("REDIRECT_RESOURCE property should be initialized");
		}
		resourceMethod = (String)webConfig.getProperty(REDIRECT_RESOURCE_METHOD);
		if(StringUtils.isEmpty(resourceMethod)) {
			throw new IllegalStateException("REDIRECT_RESOURCE_METHOD property should be initialized");
		}    	
    }
    
	@Override
	public Response toResponse(ForbiddenException exception) {
		final ContainerRequestContext request = requestProvider.get();
		
		log.debug("Forbidden, url: {}, user: {}", 
				request.getUriInfo(), 
				request.getSecurityContext().getUserPrincipal() != null ? 
						request.getSecurityContext().getUserPrincipal().getName() : "(not authenticated)");
		
		if(HttpUtils.isAjaxRequest(request)) {
			// Use default behaviour for AJAX requests.
			return exception.getResponse();
		}
		
		final UriInfo uriInfo = uriInfoProvider.get();
		final URI requestUri = uriInfo.getRequestUri();
		final StringBuilder returnUrl = new StringBuilder();
		returnUrl.append(uriInfo.getPath(false));
		if(StringUtils.isNotEmpty(requestUri.getRawQuery())) {
			returnUrl.append("?");
			returnUrl.append(requestUri.getRawQuery());
		}
		
		final URI uri = UriBuilder
				.fromResource(resourceClass)
				.path(resourceClass, resourceMethod)
				.queryParam(RETURN_URL, ExceptionAdapter.get(
						() -> URLEncoder.encode(returnUrl.toString(), "utf-8")))
				.build();
		return Response.seeOther(uri).build();	
	}
}

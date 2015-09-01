package com.expanset.jersey.utils;

import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;

/**
 * Http helpers.
 */
public final class HttpUtils {
	
	public final static String XRequestedWithHeader = "X-Requested-With";
	
	public final static String XMLHttpRequest = "XMLHttpRequest";
	
	/**
	 * Returns true if current request is AJAX request. 
	 * @param request Http request.
	 * @return true if current request is AJAX request. 
	 */
	public static boolean isAjaxRequest(ContainerRequestContext request) {
		final String requestedWith = request.getHeaderString(XRequestedWithHeader);
		return StringUtils.endsWithIgnoreCase(requestedWith, XMLHttpRequest);
	}

	/**
	 * Generate redirect to specified resource (used in the
     * redirect-after-POST (aka POST/redirect/GET) pattern.)
	 * @param resourceClass Resource class for redirection to.
	 * @return Response with tuned redirect.
	 */
	public static ResponseBuilder seeOther(Class<?> resourceClass) {
		final URI redirectUri = UriBuilder
				.fromResource(resourceClass)
				.build();
		return Response.seeOther(redirectUri);		
	}

	/**
	 * Generate redirect to specified resource method (used in the
     * redirect-after-POST (aka POST/redirect/GET) pattern.)
	 * @param resourceClass Resource class for redirection to.
	 * @param resourceMethod Resource method for redirection to.
	 * @return Response with tuned redirect.
	 */
	public static ResponseBuilder seeOther(Class<?> resourceClass, String resourceMethod) {
		final URI redirectUri = UriBuilder
				.fromResource(resourceClass)
				.path(resourceClass, resourceMethod)
				.build();
		return Response.seeOther(redirectUri);	
	}
	
	private HttpUtils() {}
}

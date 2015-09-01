package com.expanset.jersey.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.mvc.Viewable;

import com.expanset.jersey.errorhandling.ThrowableExceptionMapper;
import com.expanset.jersey.utils.HttpUtils;

/**
 * Global exception mapper. Shows error page.
 */
public class ViewableThrowableExceptionMapper extends ThrowableExceptionMapper {
	    
	@Inject
	protected Configuration webConfig;
	
	@Inject
	protected Provider<ContainerRequestContext> requestProvider;	
	
	protected Response handleError(Throwable exception, Response response) {
		response = super.handleError(exception, response); 
		if(HttpUtils.isAjaxRequest(requestProvider.get())) {
			// Use default behaviour for AJAX requests.
			return response;
		}		
		
		final String errorPage = (String)webConfig.getProperty(MvcFeature.ERROR_PAGE);
		if(StringUtils.isNotEmpty(errorPage)) {
			final Map<String, Object> model = new HashMap<>();
			model.put("exception", exception);
			model.put("status", response.getStatus());
			return Response
					.fromResponse(response)
					.entity(new Viewable((String)webConfig.getProperty(MvcFeature.ERROR_PAGE), model))
					.build();
		} else {
			throw new IllegalStateException("ERROR_PAGE property should be initialized");
		}
	}
}

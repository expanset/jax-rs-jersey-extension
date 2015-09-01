package com.expanset.jersey.errorhandling;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an exception handler for all not handled exceptions. Simply writes exception to log.
 * You may override {@link ThrowableExceptionMapper#handleError} to implement you own behavior.
 */
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    private final static String ENCODING = "utf-8";
    
    private final static Logger log = LoggerFactory.getLogger(ThrowableExceptionMapper.class);
    
	@Override
	public Response toResponse(Throwable exception) {
		
		Response response;
		if(exception instanceof WebApplicationException) {
			response = ((WebApplicationException)exception).getResponse();
		} else {
			response = Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.encoding(ENCODING)
					.build();		
		}

		return handleError(exception, response);
	}

	protected Response handleError(Throwable exception, Response response) {
		if(exception instanceof WebApplicationException) {
			if(exception instanceof ClientErrorException) {
				final ClientErrorException clientException = (ClientErrorException)exception;
				if(clientException.getResponse() != null 
						&& clientException.getResponse().getStatus() < 400) {
					log.info("Web request failed, class: {}, message: {}", exception.getClass().getName(), exception.getMessage());
				} else if(clientException.getResponse() != null 
						&& clientException.getResponse().getStatus() < 500) {
					log.warn("Web request failed, class: {}, message: {}", exception.getClass().getName(), exception.getMessage());
				} else {
					log.error("Web request failed, class: {}, message: {}", exception.getClass().getName(), exception.getMessage());
				}
			} else {
				log.error("Web request failed", exception);
			}
		} else {
			log.error("Web request failed", exception);
		}		
				
		return response;
	}
}

package com.expanset.jersey.validation;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.jvnet.hk2.annotations.Service;

/**
 * Jersey exception mapper for {@link javax.validation.ConstraintViolationException}.
 * <p>
 * It generates response with  {@link ValidationResult} body and 
 * {@link javax.ws.rs.core.Response.Status#BAD_REQUEST} code.
 * </p>
 */
@Service
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {	
	
	@Override
    public Response toResponse(ConstraintViolationException exception) {
		assert exception != null;
		
		final ValidationResult validationResult = new ValidationResult();
		validationResult.setViolations(exception.getConstraintViolations());
		
        return Response.status(Status.BAD_REQUEST).entity(validationResult).build();
    }
}

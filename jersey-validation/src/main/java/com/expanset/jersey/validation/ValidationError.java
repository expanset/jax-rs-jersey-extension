package com.expanset.jersey.validation;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;

import org.apache.commons.lang.Validate;

/**
 * Validation error.
 */
public class ValidationError {

	private final String property;
	
	private final ConstraintViolation<?> violation;
	
	private final String message;

	/**
	 * @param property Property with error.
	 * @param violation Validation error description.
	 */
	public ValidationError(@Nonnull String property, @Nonnull ConstraintViolation<?> violation) {
		Validate.notNull(property, "property");
		Validate.notNull(violation, "violation");
		
		this.property = property;
		this.violation = violation;
		this.message = violation.getMessage();
	}

	public ValidationError(@Nonnull String property, @Nonnull String message) {
		Validate.notNull(property, "property");
		Validate.notEmpty(message, "message");
		
		this.property = property;
		this.violation = null;
		this.message = message;		
	}

	/**
	 *  @return Property with error.
	 */
	public String getProperty() {
		return property;
	}

	/**
	 *  @return Validation error description.
	 */
	public ConstraintViolation<?> getViolation() {
		return violation;
	}

	/**
	 * @return Validation error message.
	 */
	public String getMessage() {
		return message;
	}
}

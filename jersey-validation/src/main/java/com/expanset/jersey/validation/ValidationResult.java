package com.expanset.jersey.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.Node;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.hk2.annotations.Contract;

/**
 * Result of model validation.
 */
@Contract
public class ValidationResult {

	/**
	 * Variable name for using in template engine.
	 */
	public static final String PROPERTY_NAME = "validation";
	
	protected Set<ConstraintViolation<?>> violations = null;
	
	protected Map<String, List<ValidationError>> validationErrors = null;

	public ValidationResult() {
		this.violations = null;
		this.validationErrors = null;
	}

	/**
	 * @return true - validation is successful.
	 */
	public boolean isSuccess() {
		return validationErrors == null || validationErrors.size() == 0;
	}

	/**
	 * @return Validation errors.
	 */
	public Set<ConstraintViolation<?>> getViolations() {
		return violations;
	}	

	/**
	 * Set validation errors.
	 * @param violations Validation errors
	 */
	@SuppressWarnings("unchecked")
	public void setViolations(
			@Nonnull Object violations) { // NOTE To avoid warning "is not resolvable to a concrete type".
		Validate.notNull(violations, "violations");

		this.violations = (Set<ConstraintViolation<?>>)violations;
		this.validationErrors = new HashMap<>(this.violations.size());
		for(ConstraintViolation<?> violation : this.violations) {
			final String propertyName = pathToPropertyName(violation.getPropertyPath());
			List<ValidationError> errors = validationErrors.get(propertyName);
			if(errors == null) {
				errors = new ArrayList<>();
				validationErrors.put(propertyName, errors);
			}
			errors.add(new ValidationError(propertyName, violation));
		}
	}
	
	/**
	 * Returns list of error messages for the concrete property.
	 * @param property Property name.
	 * @return List of error messages for the concrete property.
	 */
	public List<String> getErrorList(@Nonnull String property) {
		Validate.notNull(property, "property");
		
		if(validationErrors == null) {
			return Collections.emptyList();
		}
		
		final List<ValidationError> errors = validationErrors.get(property);
		if(errors == null) {
			return Collections.emptyList();
		}
		
		final List<String> result = new ArrayList<>(errors.size());
		for(ValidationError error : errors) {
			result.add(error.getMessage());
		}
		return result;
	}

	/**
	 * Returns error flag for the concrete property.
	 * @param property Property name.
	 * @return true - property has error.
	 */
	public boolean hasErrors(@Nonnull String property) {
		Validate.notNull(property, "property");
		
		if(validationErrors == null) {
			return false;
		}
		
		final List<ValidationError> errors = validationErrors.get(property);
		if(errors == null || errors.size() == 0) {
			return false;
		}
		
		return true;
	}
		
	/**
	 * Returns concatenated error messages for the concrete property.
	 * @param property Property name.
	 * @return Concatenated error messages for the concrete property.
	 */
	public String getErrors(String property) {
		return getErrors(property, "%s.", " ");
	}
	
	/**
	 * Returns concatenated error messages for the concrete property.
	 * @param property Property name.
	 * @param template Formatting template for one error message ({@link String#format} is using).
	 * @return Concatenated error messages for the concrete property.
	 */
	public String getErrors(String property, String template) {
		return getErrors(property, template, StringUtils.EMPTY);
	}
	
	/**
	 * Returns concatenated error messages for the concrete property.
	 * @param property Property name.
	 * @param template Formatting template for one error message ({@link String#format} is using).
	 * @param separator Separator for error messages.
	 * @return Returns concatenated error messages for the concrete property.
	 */
	public String getErrors(String property, String template, String separator) {
		Validate.notNull(property, "property");
		Validate.notNull(template, "template");
		
		if(validationErrors == null) {
			return StringUtils.EMPTY;
		}
		
		final List<ValidationError> errors = validationErrors.get(property);
		if(errors == null) {
			return StringUtils.EMPTY;
		}
		
		final StringBuilder errorMessages = new StringBuilder();
		for(ValidationError error : errors) {
			if(errorMessages.length() > 0 && StringUtils.isNoneEmpty(separator)) {
				errorMessages.append(separator);
			}
			errorMessages.append(String.format(template, error.getMessage()));
		}
		return errorMessages.toString();
	}
	
	/**
	 * @return List of properties with invalid values.
	 */
	public Set<String> getInvalidProperties() {
		if(validationErrors == null) {
			return Collections.emptySet();
		}
		return validationErrors.keySet();
	}

	public void addError(String property, String message) {
		if(validationErrors == null) {
			validationErrors = new HashMap<>();
		}
		
		List<ValidationError> errors = validationErrors.get(property);
		if(errors == null) {
			errors = new ArrayList<>();
			validationErrors.put(property, errors);
		}
		errors.add(new ValidationError(property, message));
	}	
	
    protected String pathToPropertyName(Path path) {
    	final StringBuilder builder = new StringBuilder();
    	final Iterator<Node> iterator = path.iterator();
    	while(iterator.hasNext()) {
    		final Node node = iterator.next();
    		if(node.getKind() == ElementKind.PROPERTY) {
    			if(builder.length() > 0) {
    				builder.append(".");
    			}
    			builder.append(node.getName());
    		}
    	}
    	return builder.toString();
    }
}

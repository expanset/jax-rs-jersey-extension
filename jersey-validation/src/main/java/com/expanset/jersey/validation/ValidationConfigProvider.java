package com.expanset.jersey.validation;

import java.util.Locale;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.validation.ValidationConfig;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import com.expanset.hk2.i18n.ResourceBundleProvider;

/**
 * Configure {@link org.glassfish.jersey.server.validation.ValidationConfig}.
 */
@Service
@Provider
public class ValidationConfigProvider implements ContextResolver<ValidationConfig> {
	
	@Inject
	@Optional
	protected javax.inject.Provider<Locale> localeProvider;

    @Inject
    @Optional
    protected ValidationConfig validationConfig;
    
    @Inject
    @Optional
	protected ResourceBundleProvider resourceBundleProvider;    
	
	@Override
	public ValidationConfig getContext(Class<?> type) {
		if(validationConfig == null) {
			validationConfig = new ValidationConfig();
		}
		if(validationConfig.getMessageInterpolator() == null && resourceBundleProvider != null) {
			validationConfig.messageInterpolator(
					new RequestResourceBundleMessageInterpolator(
							locale -> resourceBundleProvider.get(locale)));
		}
		
        return validationConfig;
	}
	
	protected class RequestResourceBundleMessageInterpolator extends ResourceBundleMessageInterpolator {

		public RequestResourceBundleMessageInterpolator(ResourceBundleLocator resourceLocator) {
			super(resourceLocator);
		}

		@Override
		public String interpolate(String message, javax.validation.MessageInterpolator.Context context) {
			Locale locale = localeProvider != null ? localeProvider.get() : null;
			if(locale == null) {
				locale = Locale.getDefault();
			}
			final String interpolatedMessage = interpolate(message, context, locale);
			return interpolatedMessage;
		}	
	}	
}

package com.expanset.jersey.validation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.aopalliance.intercept.ConstructorInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InterceptionService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher;
import org.jvnet.hk2.annotations.Service;

/**
 * Support of model validation. 
 * <p>If resource method has validatable parameter you may get access to validation result through 
 * {@link com.expanset.jersey.validation.ValidationResult} parameter of method. Also result can be accessed in template engine
 * in the variable named 'validation'.
 * </p>
 * <pre>Example:</pre>
 * <pre>
 * public Object login(@BeanParam {@literal @}Valid LoginModel model, {@literal @}Context ValidationResult validationResult) {
 *     return new Object();
 * }		
 * </pre>
 * <p>
 * If resource method with validatable parameter does not contain {@link com.expanset.jersey.validation.ValidationResult} parameter, 
 * then {@link javax.validation.ConstraintViolationException} will be thrown and exception is converted 
 * to Bad Request HTTP code with json like:
 * </p>
 * <pre> 
 * {'fieldName': 'error description'} 
 * </pre>
 * <p>where 'fieldName' - property name.</p>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class ValidationFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
        context.property(ServerProperties.BV_FEATURE_DISABLE, false);
        
        context.register(ValidationConfigProvider.class);
        context.register(ValidationResultMessageBodyWriter.class);
        context.register(ConstraintViolationExceptionMapper.class);

        context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bindFactory(ValidationResultFactory.class)
					.to(ValidationResult.class)
					.in(PerLookup.class);
				addActiveDescriptor(ResourceMethodDispatcherProviderInterceptorService.class);
			}
		});
        
        return true;
	}

	@Service
	private static class ValidationResultFactory implements Factory<ValidationResult> {

		@Override
		@PerLookup
		public ValidationResult provide() {
			return new ValidationResult();
		}

		@Override
		public void dispose(ValidationResult instance) {
		}
	}

	@Service
	private static class ResourceMethodDispatcherProviderInterceptorService implements InterceptionService {

		@Inject
		private Provider<ContainerRequest> containerRequestProvider;
		
		private final List<MethodInterceptor> interceptors = new ArrayList<>();
		
		@SuppressWarnings("unused")
		public ResourceMethodDispatcherProviderInterceptorService() {
			interceptors.add(new ConfiguredValidatorInterceptor());
		}
		
		@Override
		public Filter getDescriptorFilter() {
			return BuilderHelper.createContractFilter(ResourceMethodDispatcher.Provider.class.getName());
		}

		@Override
		public List<MethodInterceptor> getMethodInterceptors(Method method) {
			if(method.getName().equals("create")) {
				return interceptors;
			}
			return null;
		}

		@Override
		public List<ConstructorInterceptor> getConstructorInterceptors(Constructor<?> constructor) {
			return null;
		}
		
		private class ConfiguredValidatorInterceptor implements MethodInterceptor {

			@Override
			public Object invoke(MethodInvocation invocation) throws Throwable {
				boolean found = false;
				final Object[] args = invocation.getArguments();
				for(int i = 0; i < args.length; i++) {
					if(args[i] instanceof ConfiguredValidator) {
						found = true;
						final ConfiguredValidator validator = (ConfiguredValidator)args[i];
						args[i] = new ConfiguredValidator() {

							@Override
							public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
								return validator.validate(object, groups);
							}

							@Override
							public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
								return validator.validateProperty(object, propertyName, groups);
							}

							@Override
							public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
								return validator.validateValue(beanType, propertyName, value, groups);
							}

							@Override
							public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
								return validator.getConstraintsForClass(clazz);
							}

							@Override
							public <T> T unwrap(Class<T> type) {
								return validator.unwrap(type);
							}

							@Override
							public ExecutableValidator forExecutables() {
								return validator.forExecutables();
							}

							@Override
							public void validateResourceAndInputParams(Object resource, Invocable resourceMethod, Object[] args)
									throws ConstraintViolationException {
								ValidationResult validationResult = null;
								for(int i = 0; i < args.length; i++) {
									if(args[i] instanceof ValidationResult) {
										validationResult = (ValidationResult)args[i];
										containerRequestProvider.get().setProperty(ValidationResult.PROPERTY_NAME, validationResult);
										break;
									}
								}
								
								if(validationResult != null) {
									try {
										validator.validateResourceAndInputParams(resource, resourceMethod, args);
									} catch(ConstraintViolationException e) {
										validationResult.setViolations(e.getConstraintViolations());
									}
								} else {
									validator.validateResourceAndInputParams(resource, resourceMethod, args);
								}
							}

							@Override
							public void validateResult(Object resource, Invocable resourceMethod, Object result)
									throws ConstraintViolationException {
								validator.validateResult(resource, resourceMethod, result);
							}
						};
						
						break;
					}
				}
				
				if(!found) {
					throw new RuntimeException("Required argument (ConfiguredValidator) not found in method ResourceMethodDispatcher.Provider.create. This version of Jersey is not supported");
				}

				return invocation.proceed();
			}
		}
	}
}

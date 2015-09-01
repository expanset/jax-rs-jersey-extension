package com.expanset.jersey.caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation for resource methods adding the HTTP header Cache-Control to the response.
 * <p>Value retrieved from the configuration file.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientCacheConfig {

	/**
	 * @return Property name in the configuration file.
	 */
	String value();

	/**
	 * @return Default value if it is not set in the configuration file.
	 */
	String def() default "";
} 
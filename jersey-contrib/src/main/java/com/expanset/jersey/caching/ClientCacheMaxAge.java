package com.expanset.jersey.caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * The annotation for resource methods adding the HTTP header Cache-Control to the response.
 * <p>Value constructed basing on annotation properties.</p>
  */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientCacheMaxAge {

	/**
	 * @return Duration of storage in the browser cache.
	 */
	long time();

	/**
	 * @return Type of temporary period.
	 */
	TimeUnit unit();
}
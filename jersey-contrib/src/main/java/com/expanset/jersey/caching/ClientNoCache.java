package com.expanset.jersey.caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation for resource methods adding the HTTP header Cache-Control to the response.
 * <p>The value forbidding a caching is formed.</p>
  */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientNoCache {
}
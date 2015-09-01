package com.expanset.jersey.mvc.templates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used for setup view template with {@link TemplatePopulator}.  
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PopulateTemplateWith {
    
	/**
	 * @return Class with {@link TemplatePopulator} interface or named service with {@link TemplatePopulator} contract.
	 */
	Class<? extends TemplatePopulator> value();
	
	/**
	 * @return Name for service if necessary.
	 */
	String name() default "";
}

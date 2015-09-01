package com.expanset.jersey.mvc.templates;

import java.util.Map;

import javax.annotation.Nonnull;

import org.glassfish.jersey.server.mvc.Viewable;
import org.jvnet.hk2.annotations.Contract;

/**
 * Ability to populate template with required shared data.  
 */
@Contract
public interface TemplatePopulator {
	
	/**
	 * Populates template with required shared data.
	 * @param viewable Current template data.
	 * @param model Model to populate with shared data.
	 */
	void populate(@Nonnull Viewable viewable, @Nonnull Map<String, Object> model);
}

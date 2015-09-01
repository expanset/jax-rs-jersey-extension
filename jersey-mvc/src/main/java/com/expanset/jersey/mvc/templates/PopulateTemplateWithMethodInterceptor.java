package com.expanset.jersey.mvc.templates;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * Initializes request with {@link TemplatePopulator}, if {@link PopulateTemplateWith} exists in resource. 
 */
@Priority(Priorities.ENTITY_CODER)
public class PopulateTemplateWithMethodInterceptor implements WriterInterceptor {

	@Inject
	protected ResourceInfo resourceInfo;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) 
    		throws IOException, WebApplicationException {
        PopulateTemplateWith annotation = 
        		getPopulateTemplateWithAnnotation(context.getAnnotations());
        if(annotation == null && resourceInfo.getResourceClass() != null) {
        	annotation = resourceInfo.getResourceClass().getAnnotation(PopulateTemplateWith.class);
        }
        
        if(annotation != null) {
        	context.setProperty(TemplatePopulatorService.TEMPLATE_POPULATOR_ANNOTATION_PROPERTY, annotation);
        }
        
        context.proceed();
    }
        
    public static PopulateTemplateWith getPopulateTemplateWithAnnotation(Annotation[] annotations) {
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof PopulateTemplateWith) {
                    return (PopulateTemplateWith) annotation;
                }
            }
        }

        return null;
    }    
}

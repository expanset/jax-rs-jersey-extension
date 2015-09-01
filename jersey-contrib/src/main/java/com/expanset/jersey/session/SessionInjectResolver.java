package com.expanset.jersey.session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.jvnet.hk2.annotations.Service;

import com.expanset.hk2.utils.HK2Utils;

/**
 * Ability to use session scoped data injection through {@link SessionInject} annotation.
 */
@Service
public class SessionInjectResolver implements InjectionResolver<SessionInject> {

	@Inject
	protected Provider<HttpSession> sessionProvider;

	@Inject
	protected ServiceLocator serviceLocator;
	
	@Override
	public Object resolve(Injectee injectee, ServiceHandle<?> root) {
		String sessionKey = ReflectionHelper.getNameFromAllQualifiers(
				injectee.getRequiredQualifiers(), injectee.getParent());
		if(StringUtils.isEmpty(sessionKey)) {
			sessionKey = injectee.getRequiredType().getTypeName();
		}
		
		final HttpSession session = sessionProvider.get();
		
		assert session != null : "You must setup sessions";
		
		Object attribute = session.getAttribute(sessionKey);
		if(attribute == null && (!injectee.isOptional())) {
			attribute = createAttribute(injectee, root);
			if(attribute != null) {
				session.setAttribute(sessionKey, attribute);
			}
		}

		return attribute;
	}

	@Override
	public boolean isConstructorParameterIndicator() {
		return true;
	}

	@Override
	public boolean isMethodParameterIndicator() {
		return true;
	}
	
	protected Object createAttribute(Injectee injectee, ServiceHandle<?> root) {
		assert injectee != null;
		
		ActiveDescriptor<?> ad = serviceLocator.getInjecteeDescriptor(injectee);
        if (ad == null) {
			return serviceLocator.createAndInitialize(ReflectionHelper.getRawClass(injectee.getRequiredType()));
        }

        final ActiveDescriptor<?> proxyableDescriptor = 
        		HK2Utils.createProxyableActiveDescriptor(serviceLocator, ad, RequestScoped.class.getName());
		return serviceLocator.getService(proxyableDescriptor, root, injectee);
	}
}	

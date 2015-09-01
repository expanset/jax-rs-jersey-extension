package com.expanset.jersey.session;

import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Ability to use session scoped data injection through {@link SessionInject} annotation.
 * Session key determined form {@literal @}Named annotation or class name used. 
 * <p>Support of {@link javax.servlet.http.HttpSession} injection.</p> 
 * <p>Session must be enabled in servlet container.</p>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class SessionFeature implements Feature {

	@Override
	public boolean configure(FeatureContext context) {
		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(SessionInjectResolver.class)
					.to(new TypeLiteral<InjectionResolver<SessionInject>>(){})
					.in(Singleton.class);
				bindFactory(HttpSessionFactory.class)
					.to(HttpSession.class)
					.in(RequestScoped.class)					
					.proxyForSameScope(false);
			}
		});

		return true;
	}
}

package com.expanset.jersey.session;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ProxyForSameScope;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.jvnet.hk2.annotations.Service;

/**
 * Support of {@link javax.servlet.http.HttpSession} injection.
 */
@Service
public class HttpSessionFactory implements Factory<HttpSession> {

	@Inject
    protected Provider<HttpServletRequest> requestProvider;
 
    @Override
    @RequestScoped
    @ProxyForSameScope(false)
    public HttpSession provide() {
    	HttpSession session = requestProvider.get().getSession();
    	
    	assert session != null : "You must setup sessions";
    	
    	return session;
    }
 
    @Override
    public void dispose(HttpSession t) {
    }
}

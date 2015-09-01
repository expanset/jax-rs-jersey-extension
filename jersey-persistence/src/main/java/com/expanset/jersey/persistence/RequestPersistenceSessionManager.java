package com.expanset.jersey.persistence;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent.Type;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.errors.ExceptionAdapter;
import com.expanset.hk2.persistence.PersistenceSession;
import com.expanset.hk2.persistence.ThreadScopePersistenceSessionManager;
import com.expanset.jersey.utils.RequestScopeUtils;

/**
 * Persistence session manager based on web request.
 */
@Service
public class RequestPersistenceSessionManager 
	extends ThreadScopePersistenceSessionManager
	implements ApplicationEventListener {

	public static final String PERSISTENCE_SESSION = 
			RequestPersistenceSessionManager.class.getName() + ".persistenceSession";

	public final static String PERSISTENCE_SESSION_SCOPE = 
			RequestPersistenceSessionManager.class.getName() + ".persistenceSessionScope";	

	/**
	 * Request property to setup factory name overrides for all persistence sessions in request.
	 */
	public static final String FACTORY_NAME_OVERRIDES = 
			RequestPersistenceSessionManager.class.getName() + ".factoryNameOverrides";
	
	@Inject
	protected RequestScope requestScope;
	
	@Inject
	protected Provider<ContainerRequestContext> requestProvider;	
	
	protected final RequestEventListener eventListener = 
			event -> ExceptionAdapter.run(() -> RequestPersistenceSessionManager.this.onEvent(event));	
	
	public RequestPersistenceSessionManager() {
		scopes.add(RequestScoped.class.getName());
	}
	
	@Override
	public AutoCloseable beginSession(Map<String, String> factoryNameOverrides) {
		if(!RequestScopeUtils.isInRequestScope(requestScope)) {
			return super.beginSession(factoryNameOverrides);
		}
		
		final ContainerRequestContext request = requestProvider.get();
		if(request.getProperty(PERSISTENCE_SESSION) != null) {
			return super.beginSession(factoryNameOverrides);
		}
		
		final PersistenceSession persistenceSession = new PersistenceSession(factoryNameOverrides);
		serviceLocator.inject(persistenceSession);
		request.setProperty(PERSISTENCE_SESSION, persistenceSession);
		
		return () -> requestProvider.get().setProperty(PERSISTENCE_SESSION, null);
	}	
	
	@Override
	public PersistenceSession getCurrentSession() {
		if(!RequestScopeUtils.isInRequestScope(requestScope)) {
			return super.getCurrentSession();
		}
		
		final ContainerRequestContext request = requestProvider.get();
		final PersistenceSession session = 
				(PersistenceSession)request.getProperty(PERSISTENCE_SESSION);
		if(session == null) {
			@SuppressWarnings("unchecked")
			final Map<String, String> factoryNameOverrides = 
					(Map<String, String>)request.getProperty(FACTORY_NAME_OVERRIDES);
			
			request.setProperty(PERSISTENCE_SESSION_SCOPE, beginSession(factoryNameOverrides));
			return (PersistenceSession)request.getProperty(PERSISTENCE_SESSION);
		}
		
		return session;
	}
	
	@Override
	public void onEvent(ApplicationEvent event) {
	}

	@Override
	public RequestEventListener onRequest(RequestEvent requestEvent) {
		return eventListener;
	}

	protected void onEvent(RequestEvent event) 
			throws Exception {
		if(event.getType() == Type.RESOURCE_METHOD_FINISHED) {
			// Clear all persistence contexts, created in request thread. 
			// Useful for async requests, that continue work in other thread.  
			final PersistenceSession persistenceSession = getCurrentSession();
			if(persistenceSession != null) {
				persistenceSession.evictInCurrentThread();
			}
		} else if(event.getType() == Type.RESP_FILTERS_FINISHED) {
			// Clear all persistence contexts. Here exception mapping is supported.
			final PersistenceSession persistenceSession = getCurrentSession();
			if(persistenceSession != null) {
				persistenceSession.evict();
			}
		} else if(event.getType() == Type.FINISHED) {
			AutoCloseable scope = (AutoCloseable)event.getContainerRequest().getProperty(PERSISTENCE_SESSION_SCOPE);
			if(scope != null) {
				scope.close();
			}
			event.getContainerRequest().setProperty(PERSISTENCE_SESSION_SCOPE, null);	
		}
	}
}

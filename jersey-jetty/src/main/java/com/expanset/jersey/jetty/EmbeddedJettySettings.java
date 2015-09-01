package com.expanset.jersey.jetty;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Application;

import org.apache.commons.lang.Validate;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

/**
 * Embedded Jetty settings.
 */
@Service
@Contract
public class EmbeddedJettySettings {

	private String baseResourcePath;

	private XmlConfiguration serverConfig;
	
	private SessionManager sessionManager;
	
	private SessionIdManager sessionIdManager; 	
	
	private Class<? extends Application> applicationClass;
	
	private boolean useSecurity;

	/**
	 * @return Base path to static assets.
	 */
	public String getBaseResourcePath() {
		return baseResourcePath;
	}

	public void setBaseResourcePath(@Nonnull String baseResourcePath) {
		Validate.notNull(baseResourcePath, "baseResourcePath");
		
		this.baseResourcePath = baseResourcePath;
	}

	/**
	 * @return Jetty configuration.
	 */
	public XmlConfiguration getServerConfig() {
		return serverConfig;
	}

	public void setServerConfig(XmlConfiguration serverConfig) {
		this.serverConfig = serverConfig;
	}

	/**
	 * @return Session manager (if sessions is used).
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/**
	 * @return Session ID manager (if sessions is used).
	 */	
	public SessionIdManager getSessionIdManager() {
		return sessionIdManager;
	}

	public void setSessionIdManager(SessionIdManager sessionIdManager) {
		this.sessionIdManager = sessionIdManager;
	}

	/**
	 * @return Jersey application.
	 */	
	public Class<? extends Application> getApplicationClass() {
		return applicationClass;
	}

	public void setApplicationClass(@Nonnull Class<? extends Application> applicationClass) {
		Validate.notNull(applicationClass, "applicationClass");
		
		this.applicationClass = applicationClass;
	}

	/**
	 * @return true - use Jetty security settings.
	 */
	public boolean getUseSecurity() {
		return useSecurity;
	}

	public void setUseSecurity(boolean useSecurity) {
		this.useSecurity = useSecurity;
	}
}

package com.expanset.jersey.jetty;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Application;

import org.apache.commons.lang.Validate;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Register {@link EmbeddedJetty} to use embedded web server that handles Jersey resources.
 * Application with embedded web server can work without any servlet container.
 */
public class EmbeddedJettyBinder extends AbstractBinder {
	
	protected final XmlConfiguration serverConfig;
	
	protected final String baseResourcePath;
	
	protected final boolean useSessions;
	
	protected final boolean useSecurity;
	
	protected final Class<? extends Application> applicationClass;
	
	/**
	 * @param baseResourcePath Path to static web content.
	 * @param serverConfig Jetty configuration.
	 * @param applicationClass Jersey application.
	 * @param useSessions true - use web server sessions.
	 */
	public EmbeddedJettyBinder(
			@Nonnull String baseResourcePath, 			
			@Nonnull XmlConfiguration serverConfig, 
			@Nonnull Class<? extends Application> applicationClass,
			boolean useSessions) {
		Validate.notNull(baseResourcePath, "baseResourcePath");
		Validate.notNull(serverConfig, "serverConfig");
		Validate.notNull(applicationClass, "applicationClass");
		
		this.baseResourcePath = baseResourcePath;
		this.serverConfig = serverConfig;
		this.applicationClass = applicationClass;
		this.useSessions = useSessions;
		this.useSecurity = false;
	}

	/**
	 * @param baseResourcePath Path to static web content.
	 * @param serverConfig Jetty configuration.
	 * @param applicationClass Jersey application.
	 * @param useSessions true - use web server sessions.
	 * @param useSecurity true - use Jetty security.
	 */
	public EmbeddedJettyBinder(
			@Nonnull String baseResourcePath, 			
			@Nonnull XmlConfiguration serverConfig, 
			@Nonnull Class<? extends Application> applicationClass,
			boolean useSessions,
			boolean useSecurity) {
		Validate.notNull(baseResourcePath, "baseResourcePath");
		Validate.notNull(serverConfig, "serverConfig");
		Validate.notNull(applicationClass, "applicationClass");
		
		this.baseResourcePath = baseResourcePath;
		this.serverConfig = serverConfig;
		this.applicationClass = applicationClass;
		this.useSessions = useSessions;
		this.useSecurity = useSecurity;
	}
	
	/**
	 * @param baseResourcePath Path to static web content.
	 * @param serverConfigPath Jetty configuration file.
	 * @param applicationClass Jersey application.
	 * @param useSessions true - use web server sessions.
	 * @throws Exception Error when load configuration. 
	 */
	public EmbeddedJettyBinder(
			@Nonnull String baseResourcePath, 			
			@Nonnull String serverConfigPath, 
			@Nonnull Class<? extends Application> applicationClass,
			boolean useSessions) 
					throws Exception {
		Validate.notNull(baseResourcePath, "baseResourcePath");
		Validate.notNull(serverConfigPath, "serverConfigPath");
		Validate.notNull(applicationClass, "applicationClass");
		
		this.baseResourcePath = baseResourcePath;
		this.serverConfig = new XmlConfiguration(Files.newInputStream(Paths.get(serverConfigPath)));
		this.applicationClass = applicationClass;
		this.useSessions = useSessions;
		this.useSecurity = false;
	}

	/**
	 * @param baseResourcePath Path to static web content.
	 * @param serverConfigPath Jetty configuration file.
	 * @param applicationClass Jersey application.
	 * @param useSessions true - use web server sessions.
	 * @param useSecurity true - use Jetty security.
	 * @throws Exception Error when load configuration. 
	 */
	public EmbeddedJettyBinder(
			@Nonnull String baseResourcePath, 			
			@Nonnull String serverConfigPath, 
			@Nonnull Class<? extends Application> applicationClass,
			boolean useSessions,
			boolean useSecurity) 
					throws Exception {
		Validate.notNull(baseResourcePath, "baseResourcePath");
		Validate.notNull(serverConfigPath, "serverConfigPath");
		Validate.notNull(applicationClass, "applicationClass");
		
		this.baseResourcePath = baseResourcePath;
		this.serverConfig = new XmlConfiguration(Files.newInputStream(Paths.get(serverConfigPath)));
		this.applicationClass = applicationClass;
		this.useSessions = useSessions;
		this.useSecurity = useSecurity;
	}
	
	@Override
	protected void configure() {
		final EmbeddedJettySettings embeddedWebServerSettings = new EmbeddedJettySettings();
		embeddedWebServerSettings.setBaseResourcePath(baseResourcePath);
		embeddedWebServerSettings.setServerConfig(serverConfig);
		embeddedWebServerSettings.setApplicationClass(applicationClass);
		embeddedWebServerSettings.setUseSecurity(useSecurity);
		if(useSessions) {
			setupessionManager(embeddedWebServerSettings);
		}
		bind(embeddedWebServerSettings).to(EmbeddedJettySettings.class);
		
		addActiveDescriptor(EmbeddedJetty.class);
	}

	/**
	 * Default Jetty session manager.
	 * @param embeddedWebServerSettings Jetty settings.
	 */
	protected void setupessionManager(EmbeddedJettySettings embeddedWebServerSettings) {
		embeddedWebServerSettings.setSessionManager(new HashSessionManager());
		embeddedWebServerSettings.setSessionIdManager(new HashSessionIdManager());
	}
}

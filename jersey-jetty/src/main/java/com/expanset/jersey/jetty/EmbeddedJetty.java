package com.expanset.jersey.jetty;

import java.nio.file.Paths;

import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty web server that can be embedded to application and handle Jersey resources.
 */
@Service
@Contract
public class EmbeddedJetty implements PreDestroy {

	/**
	 * Initialization parameter name of boolean value that indicates using embedded server.  
	 */
	public static final String USING = "embeddedWebServer";
	
	protected final Server server;
		
	private static final Logger log = LoggerFactory.getLogger(EmbeddedJetty.class);
	
	/**
	 * @param serviceLocator HK2 service locator.
	 * @param serverSettings Embedded Jetty settings.
	 * @throws Exception Configuration error.
	 */
	@Inject
	public EmbeddedJetty(ServiceLocator serviceLocator, EmbeddedJettySettings serverSettings) 
			throws Exception {
		this.server = (Server)serverSettings.getServerConfig().configure();

		final ServletContextHandler contextHandler = 
				new ServletContextHandler(null, "/", serverSettings.getSessionManager() != null, 
				serverSettings.getUseSecurity());
		contextHandler.setInitParameter(USING, "true");
		String baseResourcePath = serverSettings.getBaseResourcePath();
		if(StringUtils.isEmpty(baseResourcePath)) {
			baseResourcePath = Paths.get(".").toAbsolutePath().toString();
		}
		contextHandler.setBaseResource(Resource.newResource(baseResourcePath));
		if(serverSettings.getSessionManager() != null) {
			contextHandler.setSessionHandler(new SessionHandler(serverSettings.getSessionManager()));			
		}
		if(serverSettings.getSessionIdManager() != null) {
			this.server.setSessionIdManager(serverSettings.getSessionIdManager());
		}

		@SuppressWarnings("serial")
		final ServletContainer servletContainer = new ServletContainer(
				ResourceConfig.forApplicationClass(serverSettings.getApplicationClass())) {
			@Override
			public void init() 
					throws ServletException {
				getServletContext().setAttribute(ServletProperties.SERVICE_LOCATOR, serviceLocator);
				super.init();
			}
		};

		final ServletHolder defaultServletHolder = new ServletHolder("default", DefaultServlet.class);
		// TODO Initialize this parameters through settings. 
		defaultServletHolder.setInitParameter("dirAllowed", "false");
		defaultServletHolder.setInitParameter("pathInfoOnly", "false");
		contextHandler.getServletHandler().addServlet(defaultServletHolder);		
		
		final ServletHolder servletHolder = new ServletHolder(servletContainer);
		contextHandler.getServletHandler().addServletWithMapping(servletHolder, "/*");
		
		this.server.setHandler(contextHandler);
	}
	
	/**
	 * Start web server.
	 * @throws Exception Starting error.
	 */
	public void start() 
			throws Exception {
		log.debug("start(starting)");
		
		server.start();		
		
		log.debug("start(started)");
	}

	/**
	 * Stop web server.
	 * @throws Exception Stopping error.
	 */
	public void stop() 
			throws Exception {
		log.debug("stop(stopping)");
		
		server.stop();
		
		log.debug("stop(stopped)");
	}
	
	@Override
	public void preDestroy() {
		log.debug("preDestroy");
		
		server.destroy();
	}
}

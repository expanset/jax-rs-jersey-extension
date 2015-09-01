package com.expanset.jersey.assets;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;

/**
 * Possibility of processing of static files, setup of their caching on the client.
 * <ul>
 * <li>{@link com.expanset.jersey.assets.AssetsBundlesFeature#ASSETS}</li>
 * <li>{@link com.expanset.jersey.assets.AssetsBundlesFeature#CACHE_CONTROL}</li>
 * </ul>
 */
@ConstrainedTo(RuntimeType.SERVER)
public class AssetsBundlesFeature implements Feature {

	/**
     * {@link AssetsBundle[]} property defining the array of URL patterns for static files, like /robots.txt, /css/* etc.
     * <p>There is no default value.</p>
	 */		
	public final static String ASSETS = AssetsBundlesFeature.class + ".assests";

	/**
     * {@link String} property defining default Cache-Control HTTP header for static files.
	 */			
	public final static String CACHE_CONTROL = AssetsBundlesFeature.class + ".cacheControl";
	
	@Inject
	protected ServletContext servletContext;
	
	@Override
	public boolean configure(FeatureContext context) {
		final Configuration config = context.getConfiguration();
		
		final String cacheControl = (String)config.getProperty(CACHE_CONTROL);
		final AssetsBundle[] assetsBundles = (AssetsBundle[])config.getProperty(ASSETS);
		for(AssetsBundle bundle : assetsBundles) {
			servletContext.addServlet(
					bundle.getMapping(), 
					createsStaticFilesServlet(bundle.getCacheControl() == null ? cacheControl : bundle.getCacheControl()))
						.addMapping(bundle.getMapping());
		}

		return true;
	}

	protected HttpServlet createsStaticFilesServlet(String cacheControl) {
		return new StaticFilesServlet(cacheControl);
	}
	
	/**
	 * Static files servlet. Redirects request to default servet in container of servlets.
	 */
	@SuppressWarnings("serial")
	protected static class StaticFilesServlet extends HttpServlet {

		protected final String cacheControl;
		
		public StaticFilesServlet(String cacheControl) {
			assert cacheControl != null; 
			
			this.cacheControl = cacheControl;
		}
		
		@Override
		public void doGet(final HttpServletRequest req, HttpServletResponse resp)
		    	throws ServletException, IOException {
			if(StringUtils.isNotEmpty(cacheControl)) {
				resp.setHeader(HttpHeaders.CACHE_CONTROL, cacheControl);
			}
			
	    	final RequestDispatcher rd = getServletContext().getNamedDispatcher("default");
	    	if(rd == null) {
	    		throw new IllegalStateException("You must register default servlet in servlet container");
	    	}
	    	
	    	rd.forward(req, resp);
	    }
	}
}
